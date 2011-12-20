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
import javax.microedition.ims.transport.MessageReader;
import javax.microedition.ims.transport.messagerouter.Route;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com), ext/akhomush
 * Date: 09-Dec-2009
 * Time: 17:56:00
 */
abstract class Channel<T> implements Shutdownable {
    //protected static final int BUFFER_SIZE = 4096;
    protected static final int BUFFER_SIZE = 1024*48;
    protected static final String SIP_TERMINATOR = "\r\n";
    protected static final int READ_TIMEOUT = 100;
    protected static final int SO_TIMEOUT = 10;
    protected ChannelType channelType = ChannelType.UNDEFINED_CHANNEL_TYPE;


    static interface Creator {
        void onCreate(Route routeCreated);
    }

    private final Route initialRoute;
    protected final AtomicReference<Route> realRoute = new AtomicReference<Route>();
    private final Consumer<T> consumer;
    private final MessageContext<T> messageContext;
    private final ListenerHolder<ChannelListener> listenerHolder = new ListenerHolder<ChannelListener>(ChannelListener.class);
    private final AtomicReference<T> firstMessage = new AtomicReference<T>(null);

    //put message hashes there
    private final FloodBlocker<Integer> floodBlocker;

    final Future<Object> readTask;


    private final AtomicBoolean done = new AtomicBoolean(false);

    //TODO:test code here
    private String message302 =
            " SIP/2.0 302 Moved temporarily\r\n" +
                    "From: <sip:12345678@dummy.com>;tag=5a181657-19d5-49f0-a055-fae25a1421fa\r\n" +
                    "To: <sip:1234567@dummy.com>;tag=123456789\r\n" +
                    "Call-ID: #replace\r\n" +
                    "CSeq: 1 REGISTER\r\n" +
                    "Expires: 300\r\n" +
                    //"Contact: <sip:12065748784@10.10.2.17:5061;transport=UDP>;expires=3600\r\n" +
                    //"Contact: <sip:10.10.2.17:5061;transport=UDP>;expires=3600\r\n" +
                    "Contact: <sip:huj:5061;transport=UDP>;expires=3600\r\n" +
                    "Service-Route:<sip:P2.HOME.EXAMPLE.COM;lr>,<sip:HSP.HOME.EXAMPLE.COM;lr>\r\n" +
                    "Content-Length: 0\r\n" +
                    //"Reason: Redirection;cause=CFI\r\n" +
                    "\r\n\r\n";

    private String message302_TM = "SIP/2.0 302 Moved Temporarily\r\n" +
            "To: <sip:12345678@dummy.com>;tag=aprqngfrt-jcj0dh0000020\r\n" +
            "Call-ID: #replace\r\n" +
            "From: <sip:12345678@dummy.com>;tag=d8245ce2-35c6-4411-aaa5-e45b06ad7e22\r\n" +
            "CSeq: 1 REGISTER\r\n" +
            "Via: SIP/2.0/UDP 173.153.36.18:5060;received=173.153.36.18;branch=z9hG4bK-7b409c2e-ab8d-4ae4-b2c2-26be441195e6;rport=5060\r\n" +
            "Contact: <sip:66.94.3.103;lr>\r\n" +
            "\r\n\r\n";


    Channel(final Route route, final Consumer<T> outerConsumer, final MessageContext<T> messageContext) {
        this.initialRoute = route;
        this.consumer = outerConsumer;
        this.messageContext = messageContext;

        int messagesPerSecond = messageContext.getMessageRate();
        TimeoutUnit messageLifeTime = messageContext.getMessageLifeTime();
        int floodBlockerSize = (int) (messagesPerSecond * messageLifeTime.getTimeoutUnit().toSeconds(messageLifeTime.getTimeout()));

        this.floodBlocker = new FloodBlockerImpl<Integer>(floodBlockerSize, messageLifeTime);

        messageContext.getMessageReader().setMessageReceiver(
                new MessageReader.MessageReceiver<T>() {

                    public void onMessage(final T msg) throws IOException {
                        //if we have too many messages per second it's probably a kind of DOS attack or
                        //other uncontrolled flood of messages. Than we should answer with '503 Service Unavailable'
                        if (!floodBlocker.isBlocked()) {

                            Integer msgHash = getMessageContext().getMessageHash(msg);
                            if (!floodBlocker.containsMessage(msgHash)) {
                                try {
                                    floodBlocker.addIncomingMessage(msgHash);
                                    handleNewMessage(msg);
                                }
                                catch (BlockedByFloodException e) {
                                    send503ServiceUnavailable(msg);
                                }
                            }
                            else {
                                String msgContent = getMessageContext().getMessageContentProvider().getContent(msg);
                                Logger.log(getClass(), Logger.Tag.WARNING, "Duplicate message detected : " + msgContent);
                            }
                        }
                        else {
                            send503ServiceUnavailable(msg);
                        }

                        channelSleep();
                    }

                    private void handleNewMessage(final T msg) {
                        if (firstMessage.compareAndSet(null, msg)) {
                            listenerHolder.getNotifier().onFirstMessage(createEvent(null));
                        }


                        String msgContent = getMessageContext().getMessageContentProvider().getContent(msg);
                        Logger.log(getClass(), Logger.Tag.SIP_MESSAGE_IN, msgContent);

                        consumer.push(msg);

                        /*
                        //TODO: debug code
                        //Service-Route: <sip:P2.HOME.EXAMPLE.COM;lr>,<sip:HSP.HOME.EXAMPLE.COM;lr>
                        if(msg instanceof Response){
                            final Response response = (Response) msg;
                            response.addCustomHeader("Service-Route", "<sip:P2.HOME.EXAMPLE.COM;lr>,<sip:HSP.HOME.EXAMPLE.COM;lr>");
                        }
                        String msgContent = getMessageContext().getMessageContentProvider().getContent(msg);
                        Logger.log(getClass(), Logger.Tag.SIP_MESSAGE_IN, msgContent);
                        consumer.push(msg);*/


                        /*
                        //TODO:debug code
                        final BaseSipMessage sipMessage = (BaseSipMessage) msg;
                        final String callId = sipMessage.getCallId();

                        final String msgToParse = message302_TM.replaceAll("#replace", callId);
                        //final String msgToParse = message302.replaceAll("#replace", callId);

                        BaseSipMessage mockMessage = MessageParser.parse(msgToParse);
                        final BaseSipMessage.Builder builder = mockMessage.getBuilder();
                        builder.resetVia();

                        for (Via via : sipMessage.getVias()) {
                            builder.via(via);
                        }

                        mockMessage = builder.build();

                        String msgContent = getMessageContext().getMessageContentProvider().getContent((T) mockMessage);
                        Logger.log(getClass(), Logger.Tag.SIP_MESSAGE_IN, msgContent);
                        consumer.push((T) mockMessage);
                        */
                    }

                    private void send503ServiceUnavailable(final T msg) throws IOException {
                        T serviceUnavailableMsg = messageContext.buildServiceUnavailableMessage(msg);
                        sendMessage(serviceUnavailableMsg);
                    }
                }
        );

        this.readTask =
                new FutureTask<Object>(
                        new Callable<Object>() {

                            public Object call() throws Exception {
                                final Object retValue;
                                Exception exception = null;

                                try {
                                    Thread.currentThread().setName("Channel Worker");

                                    listenerHolder.getNotifier().onChannelStart(createEvent(null));
                                    retValue = doCall();
                                }
                                catch (Exception e) {
                                    exception = e;
                                    try {
                                        listenerHolder.getNotifier().onException(createEvent(e));
                                    }
                                    catch (Exception innerException) {
                                        innerException.printStackTrace();
                                    }
                                    catch (Error err) {
                                        logThrowable(e);
                                        logThrowable(err);
                                        throw err;
                                    }

                                    logThrowable(e);
                                    throw e;
                                }
                                catch (Error e) {
                                    exception = new Exception(e);
                                    logThrowable(e);
                                    throw e;
                                }
                                finally {
                                    listenerHolder.getNotifier().onChannelStop(createEvent(exception));
                                }

                                return retValue;
                            }

                            private void logThrowable(Throwable e) {
                                e.printStackTrace();
                                Logger.log(Logger.Tag.WARNING, e.toString());
                            }

                            private Object doCall() throws IOException {

                                //TODO if isAlive == false it signals that communication counterpart has closed the socket
                                boolean isAlive = true;

                                while (!done.get() && isAlive) {
                                    try {
                                        isAlive = false;
                                        isAlive = onReadMessage(Channel.this.messageContext.getMessageReader());
                                    }
                                    catch (SipBufferOverflowException e) {
                                        send513MessageTooLarge(e.getMsgBytes());
                                    }
                                    catch (SocketException e){
                                        if(!done.get()){
                                            throw e;
                                        }
                                    }

                                    if (isAlive) {
                                        try {
                                            TimeUnit.MILLISECONDS.sleep(READ_TIMEOUT);
                                        }
                                        catch (InterruptedException e) {
                                            Thread.interrupted();
                                        }
                                    }
                                }

                                Logger.log(Channel.this.getClass(), Logger.Tag.SHUTDOWN, "Channel stopped. route=" + route);

                                if (!isAlive && !done.get()) {
                                    throw new ChannelIOException(route, Reason.CLOSED_BY_REMOTE_PARTY);
                                }

                                return new Object();
                            }

                            private void send513MessageTooLarge(final byte[] msg) throws IOException {
                                T messageTooLargeMsg = messageContext.buildMessageTooLargeMessage(msg);
                                if (messageTooLargeMsg != null) {
                                    sendMessage(messageTooLargeMsg);
                                }
                                else {
                                    Logger.log(Logger.Tag.WARNING, "Can not parse message '513 Message Too Large': " + new String(msg));
                                }
                            }

                        }
                );
    }

    void addListener(ChannelListener<T> listener) {
        listenerHolder.addListener(listener);
    }

    void removeListener(ChannelListener<T> listener) {
        listenerHolder.removeListener(listener);
    }

    public void sendMessage(T msg) throws IOException {
        if (!done.get()) {
            String msgContent = getMessageContext().getMessageContentProvider().getContent(msg);
            Logger.log(getClass(), Logger.Tag.SIP_MESSAGE_OUT, msgContent);

            //TODO: debug code. Block 200 OK here, to emulate transaction timeout
            /*final BaseSipMessage sipMessage = (BaseSipMessage) msg;
        boolean furtherProcessing = true;
        if (MessageType.SIP_CANCEL == MessageType.parse(sipMessage.getMethod())) {
            try {
                Thread.sleep(10000);
            }
            catch (InterruptedException e) {
            }
        }

        if (furtherProcessing) {
            onPushMessage(msg);
        }
        //TODO: end of debug code*/

            onPushMessage(msg);
        }
    }

    Route getInitialRoute() {
        return initialRoute;
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning Channel. route=" + initialRoute);

            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Cancelling Channel task");
            readTask.cancel(true);
            listenerHolder.shutdown();

            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Closing Channel socket");
            onShutdown();
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Channel shutdown successfully");
        }
    }

    protected MessageContext<T> getMessageContext() {
        return messageContext;
    }

    abstract void onPushMessage(T msg) throws IOException;

    /**
     * Return true is socket is alive, otherwise false
     *
     * @param messageReader
     * @return
     * @throws IOException
     */
    abstract boolean onReadMessage(MessageReader messageReader) throws IOException;

    abstract void onShutdown();

    private ChannelEvent<T> createEvent(Exception e) {
        //return new DefaultChannelEvent<T>(getInitialRoute(), null, e, firstMessage.get());
        return new DefaultChannelEvent<T>(getInitialRoute(), getRealRoute(), e, firstMessage.get());
    }

    private static void channelSleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        catch (InterruptedException e) {
            Thread.interrupted();
        }
    }


    static void log(String msg, String prefix) {
        Logger.log(prefix, msg);
    }

    protected void checkChannelType(byte[] data) {
        if (channelType == ChannelType.UNDEFINED_CHANNEL_TYPE) {
            channelType = ChannelType.PLAIN_DATA_CHANNEL_TYPE;
            log("Channel type changed to " + channelType, this.getClass().getSimpleName());
        }
    }

    protected Route getRealRoute() {
        return realRoute.get();
    }

    public static void main(String[] args) {
        String result = "SIP/2.0 200 OK\r\n" +
                "Via: SIP/2.0/TCP 10.0.2.15:5061;received=121.33.201.170;branch=z9hG4bK455b94d5-e9a4-457a-964f-360779be95d2;rport=3816\r\n" +
                "From: <sip:79262948587@multifon.ru>;tag=25e5347a-078a-42ca-9324-c879443a1275\r\n" +
                "To: <sip:79262948587@multifon.ru>;tag=aprqpm5q6q1-3mo7ob2000060\r\n" +
                "Call-ID: 3d9760e6-2100-40b8-ac3f-fa527d659d0c@10.0.2.15\r\n" +
                "CSeq: 3 REGISTER\r\n" +
                "P-Associated-URI:      \r\n" +
                "Contact: <sip:79262948587@10.0.2.15:5061;transport=TCP>;expires=100\r\n" +
                "Service-Route: <sip:79262948587@193.201.229.35:5060;transport=tcp;lr>\r\n" +
                "Content-Length: 0\r\n\r\n";

    }
}
