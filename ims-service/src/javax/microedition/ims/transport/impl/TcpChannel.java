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
import javax.microedition.ims.transport.MessageContext;
import javax.microedition.ims.transport.MessageReader;
import javax.microedition.ims.transport.messagerouter.Route;
import javax.net.ssl.SSLException;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is responsible for tcp transport.
 * 
 * @author ext-akhomush
 */
class TcpChannel<T extends IMSMessage> extends Channel<T> {
    private final static String TAG = "TcpChannel"; 
    private final static int WAIT_TRANSPORT_CHANGE_TIMEOUT = 5000;

    private final AtomicBoolean changeTransportInProgressMutex = new AtomicBoolean(false);

    private final AtomicReference<SocketIO> socketIO = new AtomicReference<SocketIO>();

    private final AtomicBoolean isDone = new AtomicBoolean(false);

    private final Creator creator;

    private final SocketFactory socketFactory;

    private final AtomicReference<T> lastOutMessageChache = new AtomicReference<T>();

    TcpChannel(final Route route, final Consumer<T> outerConsumer, final Creator creator,
            final MessageContext<T> messageContext, final SocketFactory socketFactory)
            throws IOException {
        this(route, null, outerConsumer, creator, messageContext, socketFactory);
    }

    TcpChannel(final Socket socket, final Consumer<T> outerConsumer, final Creator creator,
            final MessageContext<T> messageContext) throws IOException {
        this(null, socket, outerConsumer, creator, messageContext, null);
    }

    private TcpChannel(final Route initialRoute, final Socket socket,
            final Consumer<T> outerConsumer, final Creator creator,
            final MessageContext<T> messageContext, final SocketFactory socketFactory)
            throws IOException {

        super(initialRoute == null ? obtainRoute(socket, messageContext.getEntityType())
                : initialRoute, outerConsumer, messageContext);

        this.creator = creator;
        this.socketFactory = socketFactory;

        if (socket == null) {
            if (initialRoute == null) {
                throw new IllegalArgumentException("route can not be null if socket is null");
            }

            if (socketFactory == null) {
                throw new IllegalArgumentException(
                        "socket creator factory can not be null if socket is null");
            }

            Logger.log("TCPChannel$Constructor", " host: " + initialRoute.getDstHost() + " port:"
                    + initialRoute.getDstPort());
            // tcpSocket = new Socket(initialRoute.getDstHost(),
            // initialRoute.getDstPort());
        }

        final IOException transportCreationException = changeTransport();
        if (transportCreationException != null) {
            throw transportCreationException;
        } else if (getRealRoute() == null) {
            throw new IllegalStateException("Real route is null.");
        }
    }

    private IOException changeTransport() {

        IOException retValue = null;

        try {
            synchronized (changeTransportInProgressMutex) {
                changeTransportInProgressMutex.set(true);
                changeTransportInProgressMutex.notifyAll();
            }

            Socket tcpSocket = createSocket(getInitialRoute(), socketFactory);
            final SocketIO previousSocketIO = this.socketIO.getAndSet(new SocketIO(tcpSocket));

            Route realRoute = obtainRoute(tcpSocket, getMessageContext().getEntityType());
            this.realRoute.set(realRoute);
            creator.onCreate(realRoute);

            if (previousSocketIO != null) {
                previousSocketIO.shutdown();
            }

            synchronized (changeTransportInProgressMutex) {
                changeTransportInProgressMutex.set(false);
                changeTransportInProgressMutex.notifyAll();
            }
        } catch (IOException e) {
            retValue = e;
        }

        return retValue;
    }

    private void waitTransportChanged() throws IOException {
        try {
            final long waitTill = System.currentTimeMillis() + WAIT_TRANSPORT_CHANGE_TIMEOUT;

            synchronized (changeTransportInProgressMutex) {
                while (changeTransportInProgressMutex.get()) {
                    changeTransportInProgressMutex.wait(WAIT_TRANSPORT_CHANGE_TIMEOUT);

                    if (System.currentTimeMillis() >= waitTill) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        if (changeTransportInProgressMutex.get()) {
            throw new IOException("Underlying socket is not ready");
        }
    }

    private Socket createSocket(Route initialRoute, SocketFactory socketFactory) throws IOException {
        Socket tcpSocket;
        tcpSocket = socketFactory.createSocket(initialRoute);
        // tcpSocket = new Socket(initialRoute.getDstHost(),
        // initialRoute.getDstPort());

        // ****
        /*
         * System.out.println("****************Start creating tls socket");
         * TrustManager[] trustManagers = new TrustManager[] {new
         * TestTrustManager() }; try { SSLContext sslContext =
         * SSLContext.getInstance("TLS"); sslContext.init(null, trustManagers,
         * null); System.out.println("****************Context inited");
         * tcpSocket = (SSLSocket) sslContext.getSocketFactory().createSocket();
         * System.out.println("****************Socket created");
         * tcpSocket.connect(new InetSocketAddress(initialRoute.getDstHost(),
         * initialRoute.getDstPort()));
         * System.out.println("****************Socket connected");
         * ((SSLSocket)tcpSocket).startHandshake();
         * System.out.println("****************Socket handshaked"); } catch
         * (NoSuchAlgorithmException e) { e.printStackTrace(); } catch
         * (KeyManagementException e) { e.printStackTrace(); }
         * System.out.println("****************End creating tls socket"); //****
         * }
         */

        tcpSocket.setSoTimeout(SO_TIMEOUT);
        tcpSocket.setKeepAlive(true);
        return tcpSocket;
    }

    boolean onReadMessage(MessageReader messageReader) throws IOException {
        boolean isAlive = !isDone.get();
        if (!isDone.get()) {
            try {
                isAlive = doReadMessage(messageReader);
            } catch (SSLException e) {
                Logger.log(TAG, "onReadMessage#exception is arrised, e = " + e);
                if (isConnectionResetIssue(e)) {
                    Logger.log(TAG, "onReadMessage#tcp connection RST error is detected");
                    IOException transportChangeError = changeTransport();
                    if(transportChangeError != null) {
                        Logger.log(TAG, "onReadMessage#transport changed unsuccessfully, error message = " + transportChangeError.getMessage());
                        transportChangeError.printStackTrace();
                    } else {
                        Logger.log(TAG, "onReadMessage#transport changed successfully");
                    }
                    
                    isAlive = transportChangeError == null;

                    T lastOutMessage = lastOutMessageChache.get();
                    if (isAlive && lastOutMessage != null) {
                        Logger.log(TAG, "onReadMessage#resend last out message = " + lastOutMessage.buildContent());
                        onPushMessage(lastOutMessage);
                        Logger.log(TAG, "onReadMessage#message sent");
                    }
                } else {
                    throw e;
                }
            }
        }

        return isAlive;
    }

    private boolean doReadMessage(MessageReader messageReader) throws IOException {
        boolean isAlive = !isDone.get();
        
        int readCount = 0;
        byte[] buf = new byte[BUFFER_SIZE];

        try {
            while (!isDone.get() && (readCount = socketIO.get().getByteIn().read(buf)) > 0) {
                byte[] readData = new byte[readCount];
                checkChannelType(buf);
                System.arraycopy(buf, 0, readData, 0, readCount);
                // log(new String(readData), "TcpChannel");
                messageReader.feedPart(readData);
                readCount = 0;
                Thread.yield();
            }
        } catch (SocketTimeoutException e) {
            Thread.interrupted();
        }

        if (readCount == -1) {
            // System.out.println(new Date() +
            // "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: -1 detected ");
            isAlive = changeTransport() == null;
        }

        return isAlive;
    }

    private boolean isConnectionResetIssue(SSLException e) {
        // TODO need to think more how to check that SSLException provides RST
        // error code
        String message = e.getMessage();
        return message != null && message.contains("Connection reset by peer");
    }

    void onPushMessage(T msg) throws IOException {

        if (!isDone.get()) {
            waitTransportChanged();

            SocketIO socketIO = this.socketIO.get();

            if (!socketIO.isReady() || socketIO.getOut().checkError()) {
                throw new IOException("Unknown IO exception");
            }

            // TODO temp fix
            lastOutMessageChache.set(msg);

            final byte[] content = getMessageContext().getMessageContentProvider().getByteContent(
                    msg);
            socketIO.getByteOut().write(content);
            socketIO.getByteOut().flush();

            if (!socketIO.isReady() || socketIO.getOut().checkError()) {
                throw new IOException("Unknown IO exception");
            }
        }

    }

    void onShutdown() {
        if (isDone.compareAndSet(false, true)) {
            socketIO.get().shutdown();
        }
    }

    private static Route obtainRoute(Socket socket, IMSEntityType entityType) {
        return new DefaultRoute(socket.getInetAddress().getHostAddress(), socket.getPort(),
                socket.getLocalPort(), Protocol.TCP, entityType);
    }
}
