/*
 * This software code is (c) 2010 T-Mobile USA, Inc. All Rights Reserved.
 *
 * Unauthorized redistribution or further use of this material is
 * prohibited without the express permission of T-Mobile USA, Inc. and
 * will be prosecuted to the fullest extent of the law.
 *
 * Removal or modification of these Terms and Conditions from the source
 * or binary code of this software is prohibited.  In the event that
 * redistribution of the source or binary code for this software is
 * approved by T-Mobile USA, Inc., these Terms and Conditions and the
 * above copyright notice must be reproduced in their entirety and in all
 * circumstances.
 *
 * No name or trademarks of T-Mobile USA, Inc., or of its parent company,
 * Deutsche Telekom AG or any Deutsche Telekom or T-Mobile entity, may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" AND "WITH ALL FAULTS" BASIS
 * AND WITHOUT WARRANTIES OF ANY KIND.  ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS OR WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT CONCERNING THIS SOFTWARE, ITS SOURCE OR BINARY CODE
 * OR ANY DERIVATIVES THEREOF ARE HEREBY EXCLUDED.  T-MOBILE USA, INC.
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE
 * OR ITS DERIVATIVES.  IN NO EVENT WILL T-MOBILE USA, INC. OR ITS
 * LICENSORS BE LIABLE FOR LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF T-MOBILE USA,
 * INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * THESE TERMS AND CONDITIONS APPLY SOLELY AND EXCLUSIVELY TO THE USE,
 * MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE, ITS SOURCE OR BINARY
 * CODE OR ANY DERIVATIVES THEREOF, AND ARE SEPARATE FROM ANY WRITTEN
 * WARRANTY THAT MAY BE PROVIDED WITH A DEVICE YOU PURCHASE FROM T-MOBILE
 * USA, INC., AND TO THE EXTENT PERMITTED BY LAW.
 */

package javax.microedition.ims;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.*;
import javax.microedition.ims.core.connection.ConnState;
import javax.microedition.ims.core.connection.MockNetworkInfoImpl;
import javax.microedition.ims.core.connection.NetworkType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.env.ConnectionManager;
import javax.microedition.ims.core.messagerouter.RouterKeyDeafultImpl;
import javax.microedition.ims.core.sipservice.register.*;
import javax.microedition.ims.core.sipservice.timer.TimeoutTimer;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;
import javax.microedition.ims.transport.TransportIO;
import javax.microedition.ims.transport.TransportListenerAdapter;
import javax.microedition.ims.transport.impl.DefaultRoute;
import javax.microedition.ims.transport.messagerouter.Route;
import javax.microedition.ims.transport.messagerouter.RouteDescriptorDefaultImpl;
import javax.microedition.ims.transport.messagerouter.Router;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 17-Feb-2010
 * Time: 16:17:06
 */
public final class StackHelper {

    private static final List<IMSStack<IMSMessage>> stackList = Collections.synchronizedList(new ArrayList<IMSStack<IMSMessage>>(10));

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        ClassLoader.getSystemClassLoader().setPackageAssertionStatus("javax.microedition.ims", true);
        StackHelper.class.getClassLoader().setDefaultAssertionStatus(true);
        StackHelper.class.getClassLoader().setPackageAssertionStatus("javax.microedition.ims", true);

        //Logger.disableLogging();

        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    
                    public void uncaughtException(final Thread t, final Throwable e) {
                        Logger.log(StackHelper.class, Logger.Tag.WARNING, "uncaught exception detected: " + e);
                        e.printStackTrace();
                        shutdownAllStacks(3000);
                    }
                }
        );

        /*Runtime.getRuntime().addShutdownHook(
                new Thread(
                        new Runnable() {
                            
                            public void run() {
                                shutdownAllStacks(1000);
                            }
                        }
                )
        );*/
    }

    private static void shutdownAllStacks(final Integer waitMillis) {
        List<IMSStack<IMSMessage>> stackListCopy;

        synchronized (stackList) {
            stackListCopy = new ArrayList<IMSStack<IMSMessage>>(stackList);
        }

        for (IMSStack<IMSMessage> stack : stackListCopy) {
            shutdownStack(stack, waitMillis);
        }
    }


    private StackHelper() {
        assert false;
    }

    public static IMSStack<IMSMessage> newIMSSipStackInstance(final StackContextExt stackContext) throws IMSStackException {
        return doInstantiateSipStack(stackContext);
    }


    public static IMSStack<IMSMessage> newIMSSipStack(final StackContextExt stackContext) throws IMSStackException {

        //Here we use producer/consumer idiom
        final IMSStack<IMSMessage> imsStack = doInstantiateSipStack(stackContext);

        final TransportIO<IMSMessage> transportIO = stackContext.getTransportIO();

        //here we listen what IMSStack produces and pass it to the consumer
        //in this particular case consumer is Transport Layer
        final ProducerConsumerPipe<IMSMessage> stackToTransportPipe = new NetworkAwareProducerConsumerPipe(
                stackContext,
                (SIPIMSStack) imsStack,
                transportIO
        );
        imsStack.addProducerListener(stackToTransportPipe);

        //here we listen what TransportLayer produces and pass it to the consumer
        //In this particular case consumer is IMSStack
        final ProducerConsumerPipe<IMSMessage> transportToStackPipe = new ProducerConsumerPipe<IMSMessage>(imsStack);
        transportIO.addProducerListener(transportToStackPipe);

        //channel error handler
        final TransportListenerAdapter<IMSMessage> transportErrorHandler = new TransportListenerAdapter<IMSMessage>() {
            
            public void onChannelError(final Route route, final Exception e) {
                final String errMsg = "Route " + route + " encountered an error. All 'inprogress' transactions will be shutdown";
                Logger.log(errMsg, "ENTRYPOINT");

                final StackOuterError stackOuterError = new StackOuterErrorDefaultImpl(
                        StackOuterErrorType.EXCEPTION,
                        errMsg,
                        null,
                        e
                );
                final SIPIMSStack sipImsStack = (SIPIMSStack) imsStack;
                sipImsStack.handleError(stackOuterError);
            }
        };
        transportIO.addTransportListener(transportErrorHandler);

        //clean up code here
        imsStack.addStackListener(
                new StackListenerAdapter() {
                    
                    public void onShutdown() {
                        imsStack.removeStackListener(this);
                        imsStack.removeProducerListener(stackToTransportPipe);
                        transportIO.removeProducerListener(transportToStackPipe);
                        transportIO.removeTransportListener(transportErrorHandler);
                    }
                }
        );

        return imsStack;
    }

    private static SIPIMSStack doInstantiateSipStack(final StackContextExt stackContext) throws IMSStackException {
        final SIPIMSStack imsStack = new SIPIMSStack(stackContext);
        TransactionUtils.addExceptionHandler(imsStack);
        imsStack.addStackListener(
                new StackListenerAdapter() {
                    
                    public void onShutdown() {
                        TransactionUtils.removeExceptionHandler(imsStack);
                    }
                }
        );

        imsStack.getRegisterService().addRegistrationListener(new RegistrationListenerAdapter() {
            public void onRegistered(RegisterEvent event) {
                if (stackContext instanceof DefaultStackContext) {
                    DefaultStackContext defaultStackContext = (DefaultStackContext) stackContext;
                    //defaultStackContext.updateRegistrationInfo(imsStack.getRegisterService().getRegistrationInfo());
                    defaultStackContext.updateRegistrationInfo(event.getRegistrationInfo());
                }
            }
        });

        imsStack.getRegisterService().addRegistrationRedirectListener(new RegistrationRedirectListener() {

            private RouterKeyDeafultImpl key = new RouterKeyDeafultImpl(IMSEntityType.MSRP);

            public void onRegistrationRedirect(final RedirectEvent event) {
                RedirectData redirectData = event.getRedirectData();
                final InetSocketAddress inetAddr = redirectData.getRedirectAddress();
                final Protocol protocol = redirectData.getProtocol();
                
                imsStack.getContext().updateProtocol(protocol);
                
                final DefaultRoute redirectRoute = new DefaultRoute(
                        inetAddr.getHostName(),
                        inetAddr.getPort(),
                        stackContext.getConfig().getLocalPort(),
                        redirectData.getProtocol(),
                        IMSEntityType.SIP
                );

                final Router<IMSMessage> router = imsStack.getContext().getMessageRouter();
                final Collection<Route> activeRoutes = router.getActiveRoutes();

                if (!activeRoutes.contains(redirectRoute)) {
                    final IMSMessage causeIMSMsg = redirectData.getCauseMessage();
                    if (causeIMSMsg != null) {
                        Route oldRoute = router.getRoute(causeIMSMsg);
                        if (oldRoute != null) {
                            Logger.log("redirect detected. new route" + redirectRoute);
                            Logger.log("redirect detected. old route " + oldRoute);
                            Logger.log("redirect detected. Shutdown old route and establish new one.");
                            imsStack.getContext().getTransportIO().shutdownRoute(oldRoute);
                        }
                    }
                    final Long expires = redirectData.getExpires();
                    if (expires == null || expires != 0) {
                        router.addRoute(
                                redirectRoute,
                                new RouteDescriptorDefaultImpl(key, expires)
                        );
                    }
                    else {
                        router.removeRoute(key);
                    }
                }
            }
        });

        stackList.add(imsStack);

        return imsStack;
    }

    public static ConnectionManager newMockConnectionManager(final ConnState connState) {
        return instantiateConnectionManager(
                "javax.microedition.ims.core.connection.MockConnectionManager",
                new MockNetworkInfoImpl(connState, NetworkType.MOBILE)
               //new MockNetworkInfoImpl(connState, NetworkType.WIFI)
                //new MockNetworkInfoImpl(connState, NetworkType.ETHERNET)
        );
    }

    public static ConnectionManager newAndroidConnectionManager(Object androidContext) {
        return instantiateConnectionManager(
                "javax.microedition.ims.android.connection.AndroidConnectionManager",
                androidContext
        );
    }

    private static ConnectionManager instantiateConnectionManager(final String connManagerClass, final Object buildUpObject) {
        ConnectionManager retValue = null;
        try {
            Class<?> connectionManagerClass = Class.forName(connManagerClass);
            Constructor<?> constructor = connectionManagerClass.getConstructor(Object.class);
            retValue = (ConnectionManager) constructor.newInstance(buildUpObject);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static void shutdownStack(final IMSStack<IMSMessage> stack, final Integer waitMillis) {
        doShutdownStack(stack);

        if (waitMillis != null) {
            try {
                Thread.sleep(waitMillis);
            }
            catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

        Thread[] arr;
        Thread.enumerate(arr = new Thread[Thread.activeCount()]);
        Logger.log("TrdArr: " + Arrays.asList(arr));
    }

    public static void shutdownStack(final IMSStack<IMSMessage> stack) {
        doShutdownStack(stack);
    }

    private static void doShutdownStack(final IMSStack<IMSMessage> stack) {
        ((Shutdownable) stack).shutdown();
        ((Shutdownable) stack.getContext()).shutdown();

        if (stack instanceof SIPIMSStack) {
            TransactionUtils.removeExceptionHandler((SIPIMSStack) stack);
        }
        else {
            assert false : "Unknown stack type appeared";
        }
        stack.getContext().getRepetitiousTaskManager().reset();
        TimeoutTimer.getInstance().reset();

        stackList.remove(stack);
    }

    public static Dialog searchDialogForIncomingMessage(final StackContext stackContext, final BaseSipMessage msg) {
        return stackContext.getDialogStorage().findDialogForMessage(msg);
    }

    public static String getRemotePartyURIForIncomingMessage(final BaseSipMessage incomingMessage) {
        final UriHeader header = incomingMessage instanceof Request ? incomingMessage.getFrom() : incomingMessage.getTo();
        return getUri(header);
    }

    public static String getRemotePartyURIForOutgoingMessage(final BaseSipMessage outgoingMessge) {
        final UriHeader header = outgoingMessge instanceof Request ? outgoingMessge.getTo() : outgoingMessge.getFrom();
        return getUri(header);
    }

    public static String getLocalPartyURIForIncomingMessage(final BaseSipMessage incomingMessage) {
        final UriHeader header = incomingMessage instanceof Request ? incomingMessage.getTo() : incomingMessage.getFrom();
        return getUri(header);
    }

    public static String getLocalPartyURIForOutgoingMessage(final BaseSipMessage outgoingMessage) {
        final UriHeader header = outgoingMessage instanceof Request ? outgoingMessage.getFrom() : outgoingMessage.getTo();
        return getUri(header);
    }

    public static String getRemotePartyDisplayNameForIncomingMessage(final BaseSipMessage incomingMessage) {
        final UriHeader header = incomingMessage instanceof Request ? incomingMessage.getFrom() : incomingMessage.getTo();
        return getDisplayName(header);
    }


    private static String getUri(final UriHeader header) {
        return header.getUri().getShortURI();
    }

    private static String getDisplayName(final UriHeader header) {
        return header.getUri().getDisplayName();
    }


    /*   public static void main(String[] args){

        String input  = "INVITE sip:movial19@10.0.2.15:5061;transport=TCP SIP/2.0\r\n" +
        "Call-ID: a36b238089464800d73796b502506a78@dummy.com\r\n" +
        "CSeq: 1 INVITE\r\n" +
        "From: \"movial11\" <sip:movial11@dummy.com>;tag=8712\r\n" +
        "To: <sip:movial19@dummy.com>\r\n" +
        "Max-Forwards: 69\r\n" +
        "Contact: <sip:Session_nxlsg4s4m0__c2lwOm1vdmlhbDExQDkzLjg0LjExMy4xNTI6NjA5MDY7dHJhbnNwb3J0PVVEUA!!@95.130.218.67:5071>\r\n" +
        "P-Asserted-Identity: \"movial11\" <sip:movial11@dummy.com>\r\n" +
        "P-Charging-Vector: icid-value=AS-95.130.218.67-1272288717012\r\n" +
        "Accept-Contact: *;+g.oma.sip-im\r\n" +
        "Server: IM-serv/OMA1.0 Dummy/v1.01\r\n" +
        "Session-Expires: 3600;refresher=uas\r\n" +
        "Min-SE: 90\r\n" +
        "Supported: timer\r\n" +
        "Via: SIP/2.0/TCP 95.130.218.67:5060;branch=z9hG4bK4d41af5dbb7416a9968c16973903a92c,SIP/2.0/UDP 95.130.218.67:5071;branch=z9hG4bKd20b0a8777c806f2332fad423107ac2a\r\n" +
        "Content-Type: application/sdp\r\n" +
        "Record-Route: <sip:95.130.218.67;lr;transport=tcp>\r\n" +
        "Content-Length: 223\r\n" +
        "\r\n" +
        "v=0\r\n" +
        "o=ConferenceFactory 0 0 IN IP4 95.130.218.67\r\n" +
        "s=-\r\n" +
        "c=IN IP4 95.130.218.67\r\n" +
        "t=0 0\r\n" +
        "m=message 2855 TCP/MSRP *" +
        "a=path:msrp://95.130.218.67:2855/01t1pks4m2;tcp\r\n" +
        "a=setup:actpass\r\n" +
        "a=msrp-acm\r\n" +
        "a=accept-types:* message/CPIM\r\n";

        BaseSipMessage mes= MessageParser.parse(input);
        System.out.println("Mes:"+mes.getContent());
        String value = mes.getEvent().getValue();
        String params = mes.getEvent().getParamsList().getContent();
    }*/
}
