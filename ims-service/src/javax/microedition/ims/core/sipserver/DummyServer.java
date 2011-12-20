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

package javax.microedition.ims.core.sipserver;

import javax.microedition.ims.common.Logger;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 09-Dec-2009
 * Time: 17:22:40
 */
public class DummyServer {
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(5060/*9090*/);

        Logger.log("Dummy server started");

        Socket socket;
        while ((socket = serverSocket.accept()) != null) {
            socket.setSoTimeout(200);
            System.out.println("Point0");

            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Point1");

            String clientSays;
            String branch = null, callId = null;
            try {
                while ((clientSays = in.readLine()) != null) {
                    Logger.log("DummyServer>> client says " + clientSays);
                    if (clientSays.contains("Call-ID")) {
                        callId = clientSays.split(":")[1].trim();
                    }
                    else if (clientSays.contains("branch=")) {
                        branch = clientSays.split("branch=")[1].split(";")[0].trim();
                    }

                }
            }
            catch (SocketTimeoutException e) {
                // TODO: handle exception
            }

            System.out.println("callId = " + callId);
            System.out.println("branch = " + branch);

            String dummyResponse = getDummyResponse(branch, callId);
            System.out.println("Server echo: " + dummyResponse);
            writer.println(dummyResponse);

            writer.flush();

            try {
                Thread.sleep(10000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            writer.close();

            Logger.log("Dummy server socket closed");
            socket.close();
        }

        Logger.log("Dummy server shutdown");
    }

    private static String getDummyResponse(String branch, String callId) {
        final String dummyResponse;

        dummyResponse = "SIP/2.0 200 OK\r\n" +
                "Via: SIP/2.0/TCP 10.0.2.15:5061;alias;branch=%s;received=195.222.87.225;rport=3944\r\n" +
                "From: <sip:movial11@dummy.com>;tag=25ec086e-9486-4707-af41-25b2bb45c11c\r\n" +
                "To: <sip:movial11@dummy.com>;tag=298\r\n" +
                "Call-ID: %s\r\n" +
                "CSeq: 1 REGISTER\r\n" +
                "Contact: <sip:movial11@10.0.2.15:5061;transport=TCP>;expires=299;q=1.0\r\n" +
                "Content-Length: 0\r\n" +
                "\n";

        return String.format(dummyResponse, branch, callId);
    }
}
