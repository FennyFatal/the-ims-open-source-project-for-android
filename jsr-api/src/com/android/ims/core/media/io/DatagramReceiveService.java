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
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * This class responsible for reading data from specified socket.
 *
 * @author ext-akhomush
 */
public class DatagramReceiveService extends ConnectionService<DatagramSocket, DatagramReceiveListener> {
    private final static int RECEIVE_PACKAGE_TIMEOUT = 500;

    private final int localPort;

    private DatagramSocket serverSocket;

    public DatagramReceiveService(int localPort) {
        assert localPort > 0 : "port must be positive";

        this.localPort = localPort;
    }

    private void notifyDataReceived(final byte[] data) {
        for (DatagramReceiveListener listener : getServiceListeners()) {
            listener.dataReceived(data);
        }
    }

    private void notifyDataReceiveFailed(final String message) {
        for (DatagramReceiveListener listener : getServiceListeners()) {
            listener.dataReceiveFailed(message);
        }
    }

    
    protected void startInternal() {
        serverSocket = tryToCreateSocket(localPort);

        if (serverSocket != null) {
            try {
                startRead(serverSocket);
            } catch (IOException e) {
                Log.e(TAG, "startInternal#" + e.getMessage(), e);
                notifyDataReceiveFailed(e.getMessage());
            }
        }
    }

    
    protected DatagramSocket createServerSocket(int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        } catch (IOException e) {
            Log.i(TAG, "createServerSocket#Can't open server socket using port = " + localPort);
        }
        return socket;
    }

    private void startRead(final DatagramSocket srvSocket) throws IOException {
        Log.i(TAG, "startListen#started listen");
        srvSocket.setSoTimeout(RECEIVE_PACKAGE_TIMEOUT);


        byte[] buffer = new byte[2048];
        final DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

        while (getState() == ServiceState.STARTED) {
            try {
                srvSocket.receive(datagram);
                byte[] readedBuffer = new byte[datagram.getLength()];
                System.arraycopy(buffer, 0, readedBuffer, 0, readedBuffer.length);
                notifyDataReceived(readedBuffer);
            } catch (InterruptedIOException e) {
                // SO_TIMEOUT exception arrived
            }
        }
        Log.i(TAG, "startListen#finish listen");
    }

    
    protected void close() {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
