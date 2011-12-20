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

package com.android.ims.core.media.io;

import android.util.Log;
import com.android.ims.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base implementation for service.
 *
 * @author ext-akhomush
 */
public abstract class ConnectionService<T, L extends ServiceListener<T>> implements Service<L>, Runnable {
    protected final String TAG = this.getClass().getSimpleName();

    private final static int MAX_ATTEMPT_TO_OPEN_SERVER_SOCKET = 5;

    private final AtomicReference<ServiceState> state = new AtomicReference<ServiceState>(ServiceState.INITIAL);

    private final List<L> serviceListeners = new ArrayList<L>();

    protected enum ServiceState {
        INITIAL, STOPPED, STARTED
    }

    public void addServiceListener(final L listener) {
        if (!serviceListeners.contains(listener)) {
            serviceListeners.add(listener);
        }
    }

    public boolean removeServiceListener(final L listener) {
        return serviceListeners.remove(listener);
    }


    private void notifyServiceEstablished(final T serverSocket) {
        for (L listener : serviceListeners) {
            listener.serviceEstablished(serverSocket);
        }
    }

    private void notifyServiceEstablishFailed(final String message) {
        for (L listener : serviceListeners) {
            listener.serviceEstablishFailed(message);
        }
    }

    /**
     * Start service
     */
    public void start() {
        switch (state.get()) {
            case INITIAL: {
                new Thread(this, TAG).start();
                break;
            }
            case STARTED: {
                Log.i(TAG, "retrieveConnection#Connector already started");
                break;
            }
            case STOPPED: {
                Log.i(TAG, "retrieveConnection#Connector stopped");
                break;
            }
        }
    }

    
    public void run() {
        Log.i(TAG, "run#start thread, name = " + Thread.currentThread().getName());
        state.set(ServiceState.STARTED);

        startInternal();

        shutdown();

        Log.i(TAG, "run#finish thread, name" + Thread.currentThread().getName());
    }

    /**
     * Stop listen tcp connection
     */
    public void shutdown() {
        Log.i(TAG, "close#closed");
        if (state.get() != ServiceState.STOPPED) {

            try {
                close();
            } catch (IOException e) {
                Log.e(TAG, "close#" + e.getMessage(), e);
                notifyServiceEstablishFailed(e.getMessage());
            }

            serviceListeners.clear();
            state.set(ServiceState.STOPPED);
        }
    }

    protected T tryToCreateSocket(int localPort) {
        T socket = null;
        try {
            socket = createServerSocket(localPort, MAX_ATTEMPT_TO_OPEN_SERVER_SOCKET);
            notifyServiceEstablished(socket);
        } catch (IOException e) {
            Log.e(TAG, "createServerSocket#" + e.getMessage(), e);
            notifyServiceEstablishFailed(e.getMessage());
        }

        return socket;
    }

    private T createServerSocket(int localPort, int attemptCount) throws IOException {
        T serverSocket = createServerSocket(localPort);
        if (serverSocket == null) {
            if (attemptCount > 0) {
                int nextPort = Utils.generateRandomPortNumber();
                serverSocket = createServerSocket(nextPort, --attemptCount);
            } else {
                throw new IOException("Cann't create server socket, attemptCount " + attemptCount);
            }
        } else {
            Log.i(TAG, "createServerSocket#server socket created, port = " + localPort);
        }
        return serverSocket;
    }


    protected ServiceState getState() {
        return state.get();
    }

    protected List<L> getServiceListeners() {
        return serviceListeners;
    }

    protected abstract void startInternal();

    protected abstract T createServerSocket(int port);

    protected abstract void close() throws IOException;
}
