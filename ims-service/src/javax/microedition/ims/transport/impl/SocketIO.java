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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Shutdownable;
import java.io.*;
import java.net.Socket;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 11-Dec-2009
 * Time: 17:53:09
 */
class SocketIO implements Shutdownable {
    private static final int BUFF_SIZE = 1024 * 16;
    
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedOutputStream bout;
    private final BufferedInputStream bin;

    public SocketIO(Socket socket) throws IOException {
        this.socket = socket;

        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        bout = new BufferedOutputStream(socket.getOutputStream(), BUFF_SIZE);
        bin = new BufferedInputStream(socket.getInputStream(), BUFF_SIZE);
    }

    public boolean isReady() {
        boolean bound = socket.isBound();
        boolean closed = socket.isClosed();
        boolean connected = socket.isConnected();
        boolean inputShutdown = socket.isInputShutdown();
        boolean outputShutdown = socket.isOutputShutdown();

        final String logMsg =
                String.format(
                        "isReady#bound = %s, closed = %s, connected = %s, inputShutdown = %s, outputShutdown = %s",
                        bound,
                        closed,
                        connected,
                        inputShutdown,
                        outputShutdown
                );
        Logger.log("SocketIO", logMsg);

        boolean ready = bound && !closed && connected && !inputShutdown && !outputShutdown;
        Logger.log("SocketIO", String.format("isReady#ready = %s", ready));

        return ready;
    }

    public void shutdown() {
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Closing out stream");
        out.close();
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Out stream closed");

        try {
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Closing in stream");
            bin.close();
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "In stream closed");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Socket closing");
            socket.close();
            Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Socket closed");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    PrintWriter getOut() {
        return out;
    }

    BufferedOutputStream getByteOut() {
        return bout;
    }

    BufferedInputStream getByteIn() {
        return bin;
    }

    @Override
    public String toString() {
        return "SocketIO [socket=" + socket + "]";
    }
    
}
