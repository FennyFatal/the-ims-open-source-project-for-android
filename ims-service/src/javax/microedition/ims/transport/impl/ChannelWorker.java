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

package javax.microedition.ims.transport.impl;

import javax.microedition.ims.common.*;
import javax.microedition.ims.transport.ChannelIOException;
import javax.microedition.ims.transport.ChannelIOException.Reason;
import javax.microedition.ims.transport.MessageContext;
import javax.microedition.ims.transport.messagerouter.Route;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 16-Dec-2009
 * Time: 19:10:41
 */
class ChannelWorker<T extends IMSMessage> implements Shutdownable {

    private static final int CHANNEL_CREATION_TIMEOUT = 5000;

    private final ExecutorService executorService;

    private final Channel<T> channelMain;
    private final AtomicBoolean channelStarted = new AtomicBoolean(false);
    private final AtomicReference<Exception> exception = new AtomicReference<Exception>(null);
    private final Route initialRoute;
    private final AtomicReference<Route> realRoute = new AtomicReference<Route>(null);
    private final Channel.Creator creator = new Channel.Creator() {
        public void onCreate(final Route routeCreated) {
            realRoute.compareAndSet(null, routeCreated);
        }
    };

    private class MyChannelListenerAdapter extends ChannelListenerAdapter<T> {

        public void onChannelStart(ChannelEvent<T> event) {
            notifyChannelStarted();
        }


        public void onException(ChannelEvent<T> event) {
            exception.compareAndSet(null, event.getException());
            notifyChannelStarted();
            doShutdown(event.getException());
        }


        public void onChannelStop(ChannelEvent<T> event) {
            channelMain.removeListener(this);
            notifyChannelStarted();
            shutdown();
        }
    }

    public ChannelWorker(
            final Route initialRoute,
            final Consumer<T> inQueue,
            final MessageContext<T> messageContext,
            final ConnectionSecurityInfoProvider securityInfoProvider) throws ChannelIOException {

        this.initialRoute = initialRoute;
        this.executorService = Executors.newSingleThreadExecutor(new NamedDaemonThreadFactory("ChannelWorker"));

        try {
            switch (initialRoute.getTransportType()) {
                case TCP: {
                    SocketFactory socketFactory = new TcpSocketFactory();
                    channelMain = new TcpChannel<T>(initialRoute, inQueue, creator, messageContext, socketFactory);
                    break;

                }
                case TLS: {
                    SocketFactory socketFactory = new TlsSocketFactory(securityInfoProvider);
                    channelMain = new TcpChannel<T>(initialRoute, inQueue, creator, messageContext, socketFactory);
                    break;

                }
                case UDP: {
                    channelMain = new UdpChannel<T>(initialRoute, inQueue, creator, messageContext);
                    break;
                }
                default:
                    throw new ChannelIOException(initialRoute, Reason.UNSUPPORTED_TRANSPORT_TYPE);
            }

            doCommonConstruction();
        } catch (UnknownHostException e) {
            throw new ChannelIOException(
                    this.initialRoute,
                    ChannelIOException.Reason.DNS_LOOKUP_ERROR,
                    e.getMessage(),
                    e
            );
        } catch (SSLException e) {
            throw new ChannelIOException(
                    this.initialRoute,
                    ChannelIOException.Reason.SECURITY,
                    e.getMessage(),
                    e.getCause() == null ? e : e.getCause()
            );
        } catch (IOException e) {
            throw new ChannelIOException(
                    this.initialRoute,
                    ChannelIOException.Reason.UNKNOWN_ERROR,
                    e.getMessage(),
                    e
            );
        }
    }

    public ChannelWorker(
            final Socket socket,
            final Consumer<T> inQueue,
            final MessageContext<T> messageContext) throws ChannelIOException {

        this.initialRoute = new DefaultRoute(
                socket.getInetAddress().getHostAddress(),
                socket.getPort(),
                socket.getLocalPort(),
                Protocol.TCP,
                messageContext.getEntityType()
        );

        this.executorService = Executors.newSingleThreadExecutor();

        try {
            channelMain = new TcpChannel<T>(socket, inQueue, creator, messageContext);

            doCommonConstruction();
        } catch (IOException e) {
            throw new ChannelIOException(this.initialRoute);
        }
    }

    private void doCommonConstruction() throws IOException {

        channelMain.addListener(
                new MyChannelListenerAdapter()
        );

        executorService.execute((FutureTask) channelMain.readTask);

        waitTillChannelReady();
        final Exception initException = exception.get();
        if (initException != null) {
            doShutdown(initException);
            throw new ChannelInstantiationException("Can't create transport for initialRoute: " + initialRoute + " " + initException.getMessage());
        }
    }

    private void notifyChannelStarted() {
        synchronized (channelStarted) {
            channelStarted.set(true);
            channelStarted.notifyAll();
        }
    }

    void addListener(ChannelListener<T> listener) {
        channelMain.addListener(listener);
    }

    //TODO: cleanUpDialog listener must be used in some point

    void removeListener(ChannelListener<T> listener) {
        channelMain.removeListener(listener);
    }

    private void waitTillChannelReady() throws IOException {

        boolean needShutdown = false;

        synchronized (channelStarted) {
            long waitStarted = System.currentTimeMillis();

            while (!channelStarted.get() && !needShutdown) {
                try {
                    channelStarted.wait(CHANNEL_CREATION_TIMEOUT);
                } catch (InterruptedException e) {
                    //clear interrupt status
                    Thread.interrupted();
                } finally {
                    if (!channelStarted.get() && ((System.currentTimeMillis() - waitStarted) > CHANNEL_CREATION_TIMEOUT)) {
                        needShutdown = true;
                    }
                }
            }
        }

        if (needShutdown) {
            shutdown();
            throw new ChannelInstantiationException("Can't create transport for initialRoute: " + initialRoute);
        }
    }

    /*
    public Route getInitialRoute() {
        return initialRoute;
    }

    public Route getRealRoute() {
        return realRoute.get();
    }
    */

    public void shutdown() {
        doShutdown(null);
    }

    private void doShutdown(Exception e) {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning ChannelWorker. " + realRoute);
        if (e != null) {
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "EXCEPTION detected: " + e.toString());
            e.printStackTrace();
        }

        channelMain.shutdown();
        executorService.shutdown();

        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "ChannelWorker shutdown successfully. " + realRoute);
    }

    public void sendMessage(T sipMessage) throws ChannelIOException {
        try {
            channelMain.sendMessage(sipMessage);
        }
        catch (ChannelIOException e){
            throw e;
        }
        catch (IOException e) {
            //throw new ChannelIOException(realRoute.get());
            throw new ChannelIOException(initialRoute);
        }
    }


    public String toString() {
        return "ChannelWorker{" +
                "channelStarted=" + channelStarted +
                ", initialRoute=" + initialRoute +
                ", realRoute=" + realRoute +
                ", exception=" + exception +
                '}';
    }

    protected static void log(String msg, String prefix) {
        Logger.log(prefix, msg);
    }
}