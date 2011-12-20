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


import javax.microedition.ims.common.Consumer;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.transport.MessageContext;
import javax.microedition.ims.transport.MessageReader;
import javax.microedition.ims.transport.messagerouter.Route;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;


/**
 * This class is responsible for udp transport.
 *
 * @author ext-akhomush
 */
class UdpChannel<T> extends Channel<T> {

    private InetAddress dstAddress;
    private final DatagramSocket socket;

    public UdpChannel(final Route route, final Consumer<T> outerConsumer, final Creator creator, final MessageContext<T> messageContext) throws IOException {
        super(route, outerConsumer, messageContext);
        this.socket = new DatagramSocket(route.getLocalPort());
        socket.setSoTimeout(SO_TIMEOUT);
        this.dstAddress = InetAddress.getByName(route.getDstHost());
        socket.connect(dstAddress, route.getDstPort());
        Route realRoute = obtainRoute(socket);
        this.realRoute.set(realRoute);
        creator.onCreate(realRoute);
    }

    boolean onReadMessage(MessageReader messageReader) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            socket.receive(packet);
            int packetLength = packet.getLength();

            if (packetLength > 0) {

                if(packetLength == BUFFER_SIZE){
                    Logger.log(Logger.Tag.WARNING, "Probably some bytes of last UDP packet was truncated.");
                }

                byte[] readData = new byte[packetLength];
                System.arraycopy(packet.getData(), packet.getOffset(), readData, 0, packetLength);
                checkChannelType(readData);
                //log("Server says: '" + new String(readData) + "'", "read message");
                messageReader.feedPart(readData);
            }
            /* String input = new String(packet.getElement(), 0, packet.getLength());
            final String[] message = input.split(SIP_TERMINATOR);

            for (String header : message) {
                log("Server says: '" + header + "'", "read message");
                messageReader.feedText(header);
            }
            messageReader.feedText(SIP_TERMINATOR);*/

        }
        catch (SocketTimeoutException e) {
            //just go out to main cycle in Channel to see if something happend
            // and if all is ok try go in onReadMessage() again
        }

        return true;
    }

    
    void onPushMessage(T msg) throws IOException {
        /* Build message. */
        byte[] buf = getMessageContext().getMessageContentProvider().getByteContent(msg);
        //log(new String(buf),"Udpchannel#real sent content");
        /* Create UDP-packet with data & destination(url+port) */
        //DatagramPacket packet = new DatagramPacket(buf, buf.length, dstAddress, route.getDstPort());
        DatagramPacket packet = new DatagramPacket(buf, buf.length, dstAddress, socket.getPort());      

        /* UDP sends seem to occasionally fail at the socket level because of error code 1.
           This causes the following type of output to logcat:

               E/OSNetworkSystem( 2357): unclassified errno 1 (Operation not permitted)

           According to man page this error is not exprected for this func, but according to some web 
           sources it seem to be related to that we send packets too quickly. That's why we wait a while
           and attempt a new transmission after a while. Unfortunately the Java won't expose the
           actual error code, so we need to investigate the message string instead. 
         */

        int delayTime = 20;

        while (delayTime < 100) {
            try {
                socket.send(packet);
                break;
            } catch (IOException e) {
                if (e.getMessage().equals("Operation failed")) {
                    try {
                        Thread.sleep(delayTime);
                    } catch (InterruptedException ie) {
                        // Ignore this one, we hopefully slept for some time at least
                    }
                    delayTime *= 2;
                } else {
                    throw e;
                }
            }
        }
    }

    
    void onShutdown() {
        socket.disconnect();
        socket.close();
    }

    private Route obtainRoute(DatagramSocket socket) {

        return new DefaultRoute(
                socket.getInetAddress().getHostAddress(),
                socket.getPort(),
                socket.getLocalPort(),
                Protocol.UDP,
                getMessageContext().getEntityType()
        );
    }
}
