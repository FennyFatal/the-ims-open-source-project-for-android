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

package javax.microedition.ims;


import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.parser.msrp.MsrpParser;
import javax.microedition.ims.messages.utils.MsrpUtils;
import javax.microedition.ims.messages.wrappers.msrp.MsrpHeaders;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessageType;
import javax.microedition.ims.transport.MessageReader;
import javax.microedition.ims.transport.impl.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static javax.microedition.ims.common.util.SIPUtil.SIP_TERMINATOR;


class MsrpMessageReader implements MessageReader<MsrpMessage> {
    //private static final String TAG = "MsrpMessageReader";

    private final List<String> headers = new ArrayList<String>(20);
    private boolean hasBody;
    private boolean readingBody;
    private String transactionId, mesEnd;
    private byte[] body;
    private boolean messageStarted;
    private boolean bodyReady;
    private boolean reportMessage;
    private boolean framedBody;

    private final AtomicReference<MessageReceiver<MsrpMessage>> messageReceiver = new AtomicReference<MessageReceiver<MsrpMessage>>(null);
    private boolean startSkip;


    public MsrpMessageReader() {
    }

    public void setMessageReceiver(final MessageReceiver<MsrpMessage> messageReceiver) {
        this.messageReceiver.set(messageReceiver);
    }

    private int feedText(String txt) throws IOException {
        if (hasBody && "".equals(txt)) {
            startSkip = true;
        }
        if (!startSkip) {
            //Logger.log(TAG,"headers string: '" + txt + "'");
            headers.add(txt);
        }
        if (mesEnd != null && txt.startsWith(mesEnd)) {
            startSkip = false;
        }


        if (txt.contains(MsrpHeaders.Byte_Range.stringValue())) {
            if (!"0".equals(txt.split("/")[1]) && !reportMessage) {
                hasBody = true;
            }

        }

        if (endMessage(txt) && messageStarted && bodyReady) {
            prepareAndParse(hasBody ? txt + SIP_TERMINATOR : null);
        }
        return -1;
    }

    /*    private boolean endHeaders(final String txt) {
        return ("".equalsIgnoreCase(txt.trim()) && hasBody) || (txt.startsWith("-------" + transactionId) && !hasBody);
    }*/

    private boolean endMessage(final String txt) {
        return txt.startsWith("-------" + transactionId);
    }

    private void feedBody(byte[] body) {
        //Logger.log(TAG,"Body read: "+new String(body));
        this.body = body;
    }

    private void parseMessage(final byte[] data) throws IOException {
        //Logger.log(TAG,"Message to parse: " +new String(data));
        MsrpMessage incomingMsg = MsrpParser.parse(data);

        if (incomingMsg != null) {
            //Logger.log(TAG,"Message after parsing: " +incomingMsg.buildContent());
            final MessageReceiver<MsrpMessage> receiver = messageReceiver.get();
            if (receiver != null) {
                receiver.onMessage(incomingMsg);
            }
        }
    }

/*	
	public void feedMessage(byte[] message) throws IOException {
		parseMessage(message);		
	}*/

    private void prepareAndParse(String messageTerminator) throws IOException {
        //Logger.log(TAG,"prepareAndParse messageTerminator: "+messageTerminator);
        if (!messageStarted) {
            return;
        }
        messageStarted = false;

        String source = StringUtils.joinList(new ArrayList<String>(headers), SIP_TERMINATOR);
        if (body == null && !source.endsWith(SIP_TERMINATOR)) {
            source += SIP_TERMINATOR;
        }
        //Logger.log(TAG,"Source:"+source);
        byte[] mes = new byte[source.length() + (body != null ? body.length : 0) + (messageTerminator != null ? messageTerminator.getBytes().length : 0)];
        System.arraycopy(source.getBytes(), 0, mes, 0, source.length()); //copying headers into byte array
        if (hasBody && body != null) {
            ////Logger.log(TAG,"Body to set: "+new String(body));
            System.arraycopy(body, 0, mes, source.length(), body.length); //copying body into byte array
        }
        if (messageTerminator != null) {
            System.arraycopy(messageTerminator.getBytes(), 0, mes, source.length() + (body != null ? body.length : 0), messageTerminator.getBytes().length);
        }
        parseMessage(mes);

        clean();
    }

    private void clean() {
        hasBody = false;
        body = null;
        bodyReady = false;
        headers.clear();
        mesEnd = null;
        reportMessage = false;
        framedBody = false;
        startSkip = false;
    }

    public synchronized void feedPart(byte[] part) throws IOException {
        int startIndexOfBodyInsideData = 0;
        if (readingBody) {
            int j = Utils.indexOf(part, mesEnd.getBytes(), 0);
            if (j > 0 && ((part.length - j) > mesEnd.getBytes().length)) {
                //Logger.log(TAG,"readingBody lastChunk");
                readingBody = false;
                byte[] newPart = new byte[j + body.length + (mesEnd.getBytes().length + 3)];
                System.arraycopy(body, 0, newPart, 0, body.length);
                if (j + (mesEnd.getBytes().length + 3) > part.length) {
                    System.arraycopy(part, 0, newPart, body.length, part.length);
                }
                else {
                    System.arraycopy(part, 0, newPart, body.length, j + (mesEnd.getBytes().length + 3));
                }
                feedBody(newPart);
                bodyReady = true;
                startIndexOfBodyInsideData = j;
                prepareAndParse(null);
            }
            else {
                //Logger.log(TAG,"continue readingBody:"+new String(part));
                byte[] newPart = new byte[part.length + body.length];
                System.arraycopy(body, 0, newPart, 0, body.length);
                System.arraycopy(part, 0, newPart, body.length, part.length);
                feedBody(newPart);
                part = null;
                if (Utils.indexOf(newPart, mesEnd.getBytes(), 0) > 0) {
                    readingBody = false;
                    bodyReady = true;
                    startIndexOfBodyInsideData = j;
                    prepareAndParse(null);
                }
                return;
            }
        }
        String stringPart = new String(part);
        String[] strings = stringPart.split(SIP_TERMINATOR);
        for (String s : strings) {
            //Logger.log(TAG,"current string: '" + s + "'");
            if (s.startsWith(MsrpUtils.MSRP)) {
                clean();
                messageStarted = true;
                transactionId = s.split(" ")[1].trim();
                mesEnd = "-------" + transactionId;
                //Logger.log(TAG,"new messageStarted "+transactionId);
                if (s.trim().endsWith(MsrpMessageType.REPORT.name())) {
                    reportMessage = true;
                }
            }
            if ((mesEnd != null && s.trim().startsWith(mesEnd)) || ("".equals(s.trim()) && !bodyReady)) {
                if (hasBody && !readingBody && !bodyReady) {
                    int i = Utils.indexOf(part, (SIP_TERMINATOR + SIP_TERMINATOR).getBytes(), startIndexOfBodyInsideData);
                    if (i > startIndexOfBodyInsideData) {
                        startIndexOfBodyInsideData = i;
                    }
                    int j = Utils.indexOf(part, mesEnd.getBytes(), startIndexOfBodyInsideData);
                    if (j == -1) {
                        j = part.length;
                        framedBody = true;
                    }
                    int bodyLen = (j - i);
                    if (bodyLen > 0) {
                        body = new byte[bodyLen];
                        if (i > 0 && !framedBody) {
                            readingBody = false;
                            bodyReady = true;
                            startIndexOfBodyInsideData += bodyLen;
                            System.arraycopy(part, i, body, 0, bodyLen);
                            feedBody(body);
                        }
                        else if (i > 0 && framedBody) {
                            readingBody = true;
                            System.arraycopy(part, i, body, 0, part.length - i);
                            feedBody(body);
                        }
                    }
                }
                if (!hasBody && (mesEnd != null && s.trim().startsWith(mesEnd))) {
                    bodyReady = true;
                }
                feedText(s.trim());
            }
            else {
                feedText(s.trim());
            }
        }

    }

    public static void main(String[] args) {


        String input = "MSRP ah7a6eyxw7Hq SEND\r\n" +
                "To-Path: msrp://19980039991@66.226.206.165:11746/Uk6hdOhIsFj8TV7e9f6VwLQ8;tcp\r\n" +
                "From-Path: msrp://19980039999@172.17.172.202:8509/88lsNJzrGe3El;tcp\r\n" +
                "Message-ID: s9EmhbowapkGWCrF27g\r\n" +
                "Byte-Range: 4097-6144/6184\r\n" +
                "Content-Type: image/jpeg\r\n" +
                "\r\n" +
                "1323213213123123123123123\r\n" +
                "12312312312312312312312312\r\n" +
                "123123123123123123123123123\r\n" +
                "123123123123123123123123123\r\n";
        MsrpMessageReader reader = new MsrpMessageReader();
        try {
            reader.feedPart(input.getBytes());

            input = "123123123123123123123123123\r\n" +
                    "-------ah7a6eyxw7Hq+\r\n";

            reader.feedPart(input.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void feedCompressedPart(byte[] part, boolean isStream) throws IOException {
        assert false : "doesnt used for MSTP messages, wrong bracnh";
    }
}
