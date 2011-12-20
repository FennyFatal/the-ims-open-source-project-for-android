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

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.transport.ChannelIOException;
import javax.microedition.ims.transport.UASInstantiationException;
import javax.microedition.ims.transport.UASListener;
import javax.microedition.ims.transport.messagerouter.Route;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 20-Jan-2010
 * Time: 10:55:23
 */

//TODO: make thorough testing of this feature
//TODO: UDP UAS

abstract class CommonUAS implements UAS, Shutdownable {
    private static final int UAS_CREATION_TIMEOUT = 5000;
    private static final int INCOMING_CONNECTION_TIMEOUT = 500;
    private static final int SLEEP_TIME_MILLIS = 100;
    private static final int MAX_INCOMING_CONNECTIONS = 10;
    private static final Counter INCOMING_CONNECTION_COUNTER = new SimpleCounter();

    private final Route route;
    private final ListenerHolder<UASListener> listenerHolder;
    private final ExecutorService executorService;
    private final AtomicBoolean done = new AtomicBoolean(Boolean.FALSE);
    private final AtomicBoolean started = new AtomicBoolean(Boolean.FALSE);

    private final AtomicBoolean actuallyStarted = new AtomicBoolean(Boolean.FALSE);
    private final AtomicReference<Exception> exception = new AtomicReference<Exception>(null);

    private final Runnable uasRoutine = new Runnable() {
        public void run() {
            try {
                Thread.currentThread().setName("UAS");

                notifyAwaitingThread();
                Logger.log("CommonUAS", "run#try to create socket, port = " + route.getLocalPort());
                final ServerSocket srvSocket = new ServerSocket(route.getLocalPort());
                srvSocket.setSoTimeout(INCOMING_CONNECTION_TIMEOUT);

                Logger.log("CommonUAS", "run#startListen, srvSocket = " + srvSocket);
                while (!done.get()) {
                    if (INCOMING_CONNECTION_COUNTER.getValue() <= MAX_INCOMING_CONNECTIONS) {
                        doMainRoutine(srvSocket);
                    }
                    else {
                        MessageTransport.log("Maximum number of server connections reached: " + MAX_INCOMING_CONNECTIONS, "UAS");
                        try {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e) {
                            Thread.interrupted();
                        }
                    }
                }

            }
            catch (Exception e) {
                e.printStackTrace();
                exception.compareAndSet(null, e);
            }
            finally {
                notifyAwaitingThread();
                shutdown();
            }
        }

        private void doMainRoutine(final ServerSocket srvSocket) throws IOException {
            try {
                final Socket socket = srvSocket.accept();
                Logger.log("CommonUAS", "startListen#client arrived, socket = " + socket);
                final ChannelWorker channelWorker = createNewChannel(socket);
                channelWorker.addListener(
                        new ChannelListenerAdapter() {
                            public void onChannelStart(final ChannelEvent event) {
                                INCOMING_CONNECTION_COUNTER.increase();
                            }

                            public void onChannelStop(final ChannelEvent event) {
                                INCOMING_CONNECTION_COUNTER.decrease();
                                channelWorker.removeListener(this);
                            }
                        }
                );
            }
            catch (SocketTimeoutException e) {
                if (!done.get()) {
                    try {
                        Thread.sleep(SLEEP_TIME_MILLIS);
                    }
                    catch (InterruptedException e1) {
                        Thread.interrupted();
                    }
                }
            }
        }
    };


    private void notifyAwaitingThread() {
        synchronized (actuallyStarted) {
            actuallyStarted.set(true);
            actuallyStarted.notifyAll();
        }
    }

    public CommonUAS(final Route route, ListenerHolder<UASListener> listenerHolder) {
        this.route = route;
        this.listenerHolder = listenerHolder;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void startListen() throws UASInstantiationException {
        Logger.log("CommonUAS", "startListen#thread name = " + Thread.currentThread().getName());
        Logger.log("CommonUAS", "startListen#done = " + done.get());

        if (!done.get()) {
            if (started.compareAndSet(false, true)) {
                doStartListen();
            }
        }
    }

    private void doStartListen() throws UASInstantiationException {
        executorService.execute(uasRoutine);

        waitThreadStarted();

        if (!actuallyStarted.get()) {
            shutdown();
            throw new UASInstantiationException("Failed to create UAS in " + UAS_CREATION_TIMEOUT + " millis");
        }

        if (exception.get() != null) {
            shutdown();
            throw new UASInstantiationException(exception.get().getMessage());
        }
    }

    private void waitThreadStarted() {
        synchronized (actuallyStarted) {
            long creationTimeStamp = System.currentTimeMillis();
            while (!actuallyStarted.get()) {
                try {
                    actuallyStarted.wait(UAS_CREATION_TIMEOUT);
                }
                catch (InterruptedException e) {
                    Thread.interrupted();
                }

                if ((System.currentTimeMillis() - creationTimeStamp) > UAS_CREATION_TIMEOUT) {
                    break;
                }
            }
        }
    }

    protected abstract ChannelWorker createNewChannel(Socket socket) throws ChannelIOException;

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            listenerHolder.getNotifier().onUASShutdown(route.getLocalPort(), route.getTransportType(), exception.get());
        }
        executorService.shutdown();
    }
}
