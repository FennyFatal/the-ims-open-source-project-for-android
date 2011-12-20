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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class responsible for opening server socket
 * and listen client connection.
 * <p/>
 * If connection received then listeners will be notified.
 *
 * @author ext-akhomush
 */
public class ClientConnectionReceiver extends ConnectionService<ServerSocket, ClientConnectionListener> {
    private final static int INCOMING_CONNECTION_TIMEOUT = 500;

    private final int localPort;
    private final boolean multiplyClient;

    private ServerSocket serverSocket;

    public ClientConnectionReceiver(int port, boolean multiplyClient) {
        assert port > 0 : "port must be positive";
        this.localPort = port;

        this.multiplyClient = multiplyClient;
    }

    private void notifyConnectionReceived(final Socket connection) {
        for (ClientConnectionListener listener : getServiceListeners()) {
            listener.connectionReceived(connection);
        }
    }

    private void notifyConnectionReceiveFailed(final String message) {
        for (ClientConnectionListener listener : getServiceListeners()) {
            listener.connectionReceiveFailed(message);
        }
    }

    
    protected void startInternal() {
        serverSocket = tryToCreateSocket(localPort);

        if (serverSocket != null) {
            try {
                startListen(serverSocket);
            } catch (IOException e) {
                Log.e(TAG, "startListen#" + e.getMessage(), e);
                notifyConnectionReceiveFailed(e.getMessage());
            }
        }
    }

    
    protected ServerSocket createServerSocket(int port) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            Log.i(TAG, "createServerSocket#Can't open server socket using port = " + port);
        }
        return socket;
    }


    private void startListen(final ServerSocket srvSocket) throws IOException {
        Log.i(TAG, "startListen#started listen");
        srvSocket.setSoTimeout(INCOMING_CONNECTION_TIMEOUT);

        Socket socket = null;
        while (getState() == ServiceState.STARTED && (multiplyClient || socket == null)) {
            socket = srvSocket.accept();
            if (socket != null) {
                Log.i(TAG, String.format("startListen#connection received: local port = %s, remote port = %s, remote address = %s", socket.getLocalPort(), socket.getPort(), socket.getInetAddress()));
                if (getState() == ServiceState.STARTED) {
                    notifyConnectionReceived(socket);
                    socket = null;
                } else {
                    Log.i(TAG, "startListen#wrong connector state = " + getState());
                }
            }
        }
        Log.i(TAG, "startListen#finish listen");
    }

    
    protected void close() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
