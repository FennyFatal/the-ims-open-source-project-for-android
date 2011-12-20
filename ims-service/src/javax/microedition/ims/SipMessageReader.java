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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.sip.HeaderMessagePart;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.messages.parser.message.MessageParser;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.transport.MessageReader;
import javax.microedition.ims.transport.impl.SipBufferOverflowException;
import javax.microedition.ims.transport.impl.Utils;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;


/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 14-Dec-2009
 * Time: 10:12:34
 */
class SipMessageReader implements MessageReader<BaseSipMessage> {

    private static final int MAX_SIP_BUFFER_SIZE = 1024 * 1024;
    //private static final int MAX_SIP_BUFFER_SIZE = 1300;

    private final AtomicReference<MessageReceiver<BaseSipMessage>> messageReceiver = new AtomicReference<MessageReceiver<BaseSipMessage>>(null);
    private ByteArrayOutputStream leftOversBuff = new ByteArrayOutputStream(64 * 1024);

    private HeaderMessagePart lastRedMsgHeaderPart = null;
    private Integer neededTrafficSize = null;

    private ByteArrayOutputStream compressedMessageCache = new ByteArrayOutputStream(64 * 1024);
    private int skipBytesCount;
    private boolean analyzeFisrtByte;

    public SipMessageReader() {
    }

    public void setMessageReceiver(final MessageReceiver<BaseSipMessage> messageReceiver) {
        this.messageReceiver.set(messageReceiver);
    }

    public synchronized void feedPart(byte[] trafficPartBytes) throws IOException {
        final int currentDataSize = leftOversBuff.size() + trafficPartBytes.length;
        if (currentDataSize > MAX_SIP_BUFFER_SIZE) {
            String errMsg = "Sip buffer overfow detected. Buffer size = " +
                    MAX_SIP_BUFFER_SIZE + " current data size = " + currentDataSize;

            Logger.log(Logger.Tag.WARNING, errMsg);
            Logger.log(Logger.Tag.WARNING, "Skipped data: " + new String(leftOversBuff.toByteArray()));

            final HeaderMessagePart lastRedMsgHeaderPartLocal = lastRedMsgHeaderPart;
            leftOversBuff = new ByteArrayOutputStream(64 * 1024);
            lastRedMsgHeaderPart = null;
            neededTrafficSize = null;

            if (lastRedMsgHeaderPartLocal != null) {
                throw new SipBufferOverflowException(lastRedMsgHeaderPartLocal.getBytes());
            }
        }
        else {
            doFeedPart(trafficPartBytes);
        }
    }

    public void feedCompressedPart(byte[] part, boolean isStream) throws IOException {
        Logger.log("received compressed data: " + part.length + " bytes " + new String(part));
        //StringBuilder sb = new StringBuilder();
        if (isStream) {
            for (int i = 0; i < part.length; i++) {
                //sb.append( (i % 40 == 0)? "\n" : "").append(" "+(int)part[i]);
                compressedMessageCache.write(part[i]);
                if (analyzeFisrtByte) {
                    analyzeFisrtByte = false;
                    if (part[i] > 0 && part[i + 1] < 128) {
                        skipBytesCount = part[i] + 1; //2 for 0xff and next char
                    }
                    else if (part[i] == -1) { //end of message
                        decompressAndParse(compressedMessageCache, true);
                    }
                }

                skipBytesCount--;

            }
            //Logger.log(sb.toString());
        }
        else {
            compressedMessageCache.write(part, 0, part.length);
            compressedMessageCache.flush();
            //parseMessage(decompressed);
            //compressedMessageCache.reset();
        }

    }

    private void decompressAndParse(ByteArrayOutputStream compressedMessageCache, boolean isStream) throws IOException {
        compressedMessageCache.flush();
        byte[] compressed = compressedMessageCache.toByteArray();
        compressedMessageCache.reset();
        //feedPart(decompressed);
    }

    private void doFeedPart(byte[] trafficPartBytes) throws IOException {

        //service variables. merely pointer in byte array of meassage we try to read out
        int prevMsgEndIndex = -1;
        int currBodyIndex = -1;

        resetIfCorruptedStreamDetected(trafficPartBytes);

        //all incoming traffic we add to buffer of previously recieved parts
        leftOversBuff.write(trafficPartBytes);

        //if we know traffic size for current message (and we do know it after we red header part of message) we
        //can wait and do not do further processing till traffic buffer will grow to at least awaited value.
        //traffic buffer growth by code line above
        if (neededTrafficSize == null || leftOversBuff.size() >= neededTrafficSize) {

            //if we have traffic buffer of enough size or we just don't know this size (e.g. just started to read new message)
            //we take all bytes from buffer and try to process them in furtehr code
            trafficPartBytes = leftOversBuff.toByteArray();

            //SIP message headers and their bodies separated by double SIP_TERMINATOR sequence.
            //SIP_TERMINATOR = \r\n = 0x0d 0x0a, double SIP_TERMINATOR = \r\n\r\n.
            //We use this property of SIP stream to separate SIP message header from bodies.
            //So we try to find sequence \r\n\r\n in traffic buffer in case we trying to read new message,
            //or just use index of header end calculated in previous iteration
            //So, currBodyIndex mean begining of message body.
            currBodyIndex = lastRedMsgHeaderPart == null ?
                    Utils.indexOf(
                            trafficPartBytes,
                            SIPUtil.BODY_TERMINATOR_BYTES,
                            prevMsgEndIndex > 0 ? prevMsgEndIndex : 0
                    ) :
                    lastRedMsgHeaderPart.getEndIndex() + 1;

            //if do found SIP_TERMINATOR sequence we clear traffic buffer and will completely process accumulated bytes
            // in code below
            if (currBodyIndex >= 0) {
                leftOversBuff.reset();
            }

            //in common case traffic buffer can contain only part of single message, one complete message and part
            //of next message or even multiple messages.
            //So we have do next processing in loop
            while (currBodyIndex > 0) {

                assert currBodyIndex > prevMsgEndIndex : "sip stream failure";

                //here we try to extract message header part in case we are reading new message
                if (lastRedMsgHeaderPart == null) {
                    byte[] msgHeaderTrafficPartBytes = new byte[currBodyIndex - prevMsgEndIndex - 1];
                    System.arraycopy(trafficPartBytes, prevMsgEndIndex + 1, msgHeaderTrafficPartBytes, 0, msgHeaderTrafficPartBytes.length);
                    //here we can extraxt message header part or null is SIP_MARKER was not found in bytes array
                    lastRedMsgHeaderPart = SIPUtil.extractMsgHeaderPart(msgHeaderTrafficPartBytes);

                    //calculate how many bytes must contain traffic buffer to allow us to read complete message.
                    //If at current time byte buffer doesn't contain enough bytes we will accumulate them till we have enough.
                    //this we can calculate only if have managed to read message header above
                    neededTrafficSize = lastRedMsgHeaderPart == null ?
                            null :
                            prevMsgEndIndex +
                                    lastRedMsgHeaderPart.getStartIndex() +
                                    1 +
                                    lastRedMsgHeaderPart.getEntireMessageLength();
                }

                //this block tries to read out whole message including header, body and header/body separator (BODY-SEPARATOR)
                if (neededTrafficSize == null || neededTrafficSize <= trafficPartBytes.length) {

                    //we can read the whole message only if we were able to read message header above
                    if (lastRedMsgHeaderPart != null) {
                        byte[] completeMsgBytes = extractCompleteMsg(trafficPartBytes, prevMsgEndIndex, lastRedMsgHeaderPart);
                        assert completeMsgBytes.length == lastRedMsgHeaderPart.getEntireMessageLength() : "Illegal SIP stream state";

                        //throw new SipBufferOverflowException(lastRedMsgHeaderPart.getBytes());
                        parseMessage(completeMsgBytes);
                    }

                    //here we move END_OF_PREVIOUS_MESSAGE marker to the new position
                    prevMsgEndIndex = lastRedMsgHeaderPart == null ?
                            currBodyIndex - 1 + SIPUtil.BODY_TERMINATOR_BYTES.length :
                            prevMsgEndIndex + lastRedMsgHeaderPart.getEntireMessageLength() + lastRedMsgHeaderPart.getStartIndex();

                    //try to find new message separator
                    currBodyIndex = Utils.indexOf(trafficPartBytes, SIPUtil.BODY_TERMINATOR_BYTES, prevMsgEndIndex > 0 ? prevMsgEndIndex : 0);

                    //clear state of previous message
                    neededTrafficSize = null;
                    lastRedMsgHeaderPart = null;
                }
                else {
                    break;
                }
            }
            processLeftOvers(trafficPartBytes, prevMsgEndIndex);
        }
    }

    /*
    The purpose of this method is to detect a corrupted stream of bytes.
    For example when we in a state awaiting next chunk of bytes to complete current message,
    and this chunk contains a new message. In this case we have no chance to complete current message,
    so we need to reset all buffers and counters and behave as if just started to recive bytes for a completely new message
     */
    private void resetIfCorruptedStreamDetected(byte[] trafficPartBytes) {
        //this means we are in the middle of reading something
        if (neededTrafficSize != null) {

            //try to find marker of a new sip message
            int i = Utils.indexOf(
                    trafficPartBytes,
                    SIPUtil.BODY_TERMINATOR_BYTES,
                    0
            );

            //if we probably find new message we need to make further investigation to extract headers and so on.
            if (i > 0) {
                HeaderMessagePart messagePart = SIPUtil.extractMsgHeaderPart(trafficPartBytes);

                //at this point we can say for sure we detected new message in a stream.
                if (messagePart != null) {
                    //here we check if this new message lays in rage of awaited bytes to complete current message
                    int awaitedBytesSize = neededTrafficSize - leftOversBuff.size();

                    //if so we need to reset state of reader.
                    if (messagePart.getStartIndex() <= awaitedBytesSize) {
                        Logger.log(Logger.Tag.WARNING, "Corrupted stream detected.");
                        Logger.log(Logger.Tag.WARNING, "Buffer will be discarded:");
                        Logger.log(Logger.Tag.WARNING, leftOversBuff.toString());

                        neededTrafficSize = null;
                        lastRedMsgHeaderPart = null;
                        leftOversBuff.reset();
                    }
                }
            }
        }
    }

    private void processLeftOvers(byte[] trafficPartBytes, int prevMsgEndIndex) throws IOException {
        //at some point we can't find more BODY_SEPARATOR's. Than we must check if tehre left some unprocessed bytes
        //and if so we have to save them for further processing together with next bytes chunk
        //here we calculate leftovers size
        final int leftOversSize = trafficPartBytes.length - 1 - prevMsgEndIndex;

        //leftOversBuff.size() == 0 means that at some point above we found BODY_SEPARATOR and
        //some processing of incoming bytes were undertaken. That means in turn we have to take care about byte leftovers.
        //If leftOversBuff.size() != 0 that means not BODY_SEPARATORs were found and leftOversBuffer
        //already contains all unprocessed bytes and we have no need to take special care obout them.
        if (leftOversBuff.size() == 0 && leftOversSize > 0) {
            byte[] currLeftOvers = new byte[leftOversSize];
            System.arraycopy(trafficPartBytes, prevMsgEndIndex + 1, currLeftOvers, 0, currLeftOvers.length);
            leftOversBuff.write(currLeftOvers);

            //as soon as we cut of some bytes we need to lessen awaited bytes amount by the same value
            if (neededTrafficSize != null) {
                neededTrafficSize = neededTrafficSize - (trafficPartBytes.length - currLeftOvers.length);
            }
        }
    }

    private byte[] extractCompleteMsg(
            final byte[] byteTrafficPart,
            final int prevMsgEndIndex,
            final HeaderMessagePart headerPart) {

        byte[] retValue =
                new byte[headerPart.getEntireMessageLength()];

        System.arraycopy(
                byteTrafficPart,
                prevMsgEndIndex + headerPart.getStartIndex() + 1,
                retValue,
                0,
                retValue.length
        );
        return retValue;
    }

    private BaseSipMessage parseMessage(final byte[] data) throws IOException {

        //Logger.log("Message to be parsed: " + new String(data));
        BaseSipMessage incomingMsg = MessageParser.parse(data);
        assert incomingMsg != null : "Ragel failure for :" + new String(data);

        if (incomingMsg != null) {
            final MessageReceiver<BaseSipMessage> receiver = messageReceiver.get();
            if (receiver != null) {
                receiver.onMessage(incomingMsg);
            }
        }
        return incomingMsg;
    }

    static String msg = "SIP/2.0 401 Unauthorized\r\n" +
            "To: \r\n" +
            "Call-ID: \r\n" +
            "From: \r\n" +
            "CSeq: 1 REGISTER\r\n" +
            "Via: \r\n" +
            "WWW-Authenticate: \r\n" +
            "Content-Length: 0\r\n\r\n";


    public static void main(String[] args) throws IOException {
        File file = new File("D:/Java/Project/gips_helsinki/ims-service/src/javax/microedition/ims/notify_msg.txt");
        InputStream is = new FileInputStream(file);
        byte[] buff = new byte[is.available()];
        is.read(buff);
        is.close();
        
        final BaseSipMessage baseSipMessage = MessageParser.parse(buff/*msg.getBytes()*/);
        System.out.println(""+baseSipMessage.buildContent());
    }
}

/*
    private final List<String> headers = new ArrayList<String>(20);
    private boolean hasBody;
    private boolean readingBody;
    private int bodyLength = -1, bodyLeft;
    private byte[] body;
    private boolean messageStarted;
    private boolean bodyReady;
    private boolean framedBody;
    private String fromPrevMsgPart = null;
 */

/*

      private void clean() {
        hasBody = false;
        bodyLength = 0;
        body = null;
        bodyLeft = 0;
        bodyReady = false;
        headers.clear();
        framedBody = false;
    }

    private boolean endMessage(final String txt) {
        return endHeaders(txt) && !hasBody;
    }*/

/*
private boolean endHeaders(final String txt) {
        return "".equalsIgnoreCase(txt.trim());
    }

    private void prepareAndParse() throws IOException {
        if (!messageStarted) {
            return;
        }
        messageStarted = false;

        String source = StringUtils.joinList(new ArrayList<String>(headers), SIPUtil.SIP_TERMINATOR) + SIPUtil.SIP_TERMINATOR + SIPUtil.SIP_TERMINATOR;
        //Logger.log("Source:"+source);
        byte[] mes = new byte[source.length() + bodyLength];
        System.arraycopy(source.getBytes(), 0, mes, 0, source.length()); //copying headers into byte array
        if (hasBody && body != null && bodyLength > 0) {
            //Logger.log("Body to set: "+new String(body));
            System.arraycopy(body, 0, mes, source.length(), bodyLength); //copying body into byte array
        }

        parseMessage(mes);

        clean();
    }
 */

/*
 
    public int feedText(String txt) throws IOException {
        int bodyLen = 0;
        if (!readingBody && !bodyReady) {
            //Logger.log("feedText: '" + txt + "'");
            headers.add(txt);
        }
        if (txt.contains(Header.Content_Length.stringValue())) {
            try {
                bodyLength = Integer.parseInt(txt.split(":")[1].trim());// BODY exists!
                //Logger.log("reader", "has body: " + bodyLength);
                bodyLen = bodyLength;
            }
            catch (NumberFormatException e) {
                //Logger.log("NumberFormatException: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            if (bodyLen > 0) {
                hasBody = true;
            }
        }

        if (endHeaders(txt) && messageStarted && bodyReady) {
            prepareAndParse();
        }
        return bodyLen;
    }
 */

/*
    private boolean sipMessageStartSequenceDetected(String sipTerminatedTrafficPart) {
        return sipTerminatedTrafficPart.startsWith(SIPUtil.SIP_MARKER_STRING) ||
                sipTerminatedTrafficPart.endsWith(SIPUtil.SIP_MARKER_STRING);
    }

    private void feedBody(byte[] body) {
        //Logger.log("Body read: "+new String(body));
        this.body = body;
    }
 */

/*
    public static void main(String[] args) {


        String input = "SIP/2.0 200 OK\r\n"
                + "From: <sip:movial11@dummy.com>;tag=df0ece4e-a423-42fb-89a7-865df46f31fa\r\n"
                + "To: <sip:movial11@dummy.com;pres-list=rcs>;tag=1684\r\n"
                + "Call-ID: bbc40772-6689-4fa2-a781-1dff0a778d24@10.0.2.15\r\n"
                + "CSeq: 1 SUBSCRIBE\r\n"
                + "Record-Route: <sip:95.130.218.67;lr;transport=tcp>\r\n"
                + "Contact: <sip:movial11@95.130.218.67:5063;transport=tcp>\r\n"
                + "Expires: 3600\r\n"
                + "Via: SIP/2.0/TCP 10.0.2.15:5061;alias;branch=z9hG4bKcf5fed11-c107-456c-af8b-e19483566ed7;received=195.222.87.225;rport=8816\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n"
                + "NOTIFY sip:movial11@10.0.2.15:5061;transport=TCP SIP/2.0\r\n"
                + "Call-ID: bbc40772-6689-4fa2-a781-1dff0a778d24@10.0.2.15\r\n"
                + "CSeq: 1 NOTIFY\r\n"
                + "From: <sip:movial11@dummy.com;pres-list=rcs>;tag=1684\r\n"
                + "To: <sip:movial11@dummy.com>;tag=df0ece4e-a423-42fb-89a7-865df46f31fa\r\n"
                + "Max-Forwards: 69\r\n"
                + "Contact: <sip:movial11@95.130.218.67:5063;transport=tcp>\r\n"
                + "Event: presence\r\n"
                + "P-Charging-Vector: icid-value=AS-95.130.218.67-1280996167423\r\n"
                + "Require: eventlist\r\n"
                + "Subscription-State: active;expires=3599\r\n"
                + "Via: SIP/2.0/TCP 95.130.218.67:5060;branch=z9hG4bK10f16e01f7415774b12c3ea9f3c36f30,SIP/2.0/TCP 95.130.218.67:5063;alias;branch=z9hG4bK772d9c0be60e035da8b67bc21f14be18\r\n"
                + "Content-Type: multipart/related;boundary=\"----=_Part_9446_1583120024.1280996167416\";start=\"<1280996167416--731115899@dummy.com>\";type=\"application/rlmi+xml\"\r\n"
                + "Record-Route: <sip:95.130.218.67;lr;transport=tcp>\r\n"
                + "Content-Length: 850\r\n"
                + "\r\n"
                + "------=_Part_9446_1583120024.1280996167416\r\n"
                + "Content-Type: application/rlmi+xml\r\n"
                + "Content-Transfer-Encoding: binary\r\n"
                + "Content-ID: <1280996167416--731115899@dummy.com>\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n"
                + "	<list xmlns=\"urn:ietf:params:xml:ns:rlmi\" fullState=\"true\" version=\"0\" uri=\"sip:movial11@dummy.com;pres-list=rcs\">\r\n"
                + "	<resource uri=\"sip:movial19@dummy.com\">\r\n"
                + "		<name>mov19</name>\r\n"
                + "	</resource>\r\n"
                + "	<resource uri=\"sip:movail90@dummy.com\">\r\n"
                + "		<name>nihao</name>\r\n"
                + "	</resource>\r\n"
                + "	<resource uri=\"sip:movial11@dummy.com\">\r\n"
                + "		<name>mov11</name>\r\n"
                + "	</resource>\r\n"
                + "	<resource uri=\"sip:movial10@dummy.com\">\r\n"
                + "		<name>mov10</name>\r\n"
                + "	</resource>\r\n";


        SipMessageReader reader = new SipMessageReader();


        try {
            reader.feedPart(input.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        input = "	<resource uri=\"sip:movial20@dummy.com\">\r\n"
                + "		<name>mov20</name>\r\n"
                + "	</resource>\r\n"
                + "</list>\r\n"
                + "\r\n"
                + "------=_Part_9446_1583120024.1280996167416--\r\n"
                + "\r\n";

        try {
            reader.feedPart(input.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String in = "NOTIFY sip:movial11@10.0.2.15:5061;transport=TCP SIP/2.0\r\n" +
                "Call-ID: 6d4507d5-c73c-42a5-9f61-ab27776330fe@10.0.2.15\r\n" +
                "CSeq: 1 NOTIFY\r\n" +
                "From: <sip:movial11@dummy.com;pres-list=rcs>;tag=6749\r\n" +
                "To: <sip:movial11@dummy.com>;tag=541151cc-8b56-49a9-bd0c-373e4e733af6\r\n" +
                "Max-Forwards: 69\r\n" +
                "Contact: <sip:movial11@95.130.218.67:5063;transport=tcp>\r\n" +
                "Event: presence\r\n" +
                "P-Charging-Vector: icid-value=AS-95.130.218.67-1281008099864\r\n" +
                "Require: eventlist\r\n" +
                "Subscription-State: active;expires=3599\r\n" +
                "Via: SIP/2.0/TCP 95.130.218.67:5060;branch=z9hG4bK0ddec1ccdf4b901cb69dd41c56b21e1d,SIP/2.0/TCP 95.130.218.67:5063;alias;branch=z9hG4bKf96663ae28de7a3463c794a02605864d\r\n" +
                "Content-Type: multipart/related;boundary=\"----=_Part_10037_1594277765.1281008099848\";start=\"<1281008099848--1960860726@dummy.com>\";type=\"application/rlmi+xml\"\r\n" +
                "Record-Route: <sip:95.130.218.67;lr;transport=tcp>\r\n" +
                "Content-Length: 853\r\n" +
                "\r\n" +
                "------=_Part_10037_1594277765.1281008099848\r\n" +
                "Content-Type: application/rlmi+xml\r\n" +
                "Content-Transfer-Encoding: binary\r\n" +
                "Content-ID: <1281008099848--1960860726@dummy.com>\r\n" +
                "\r\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n" +
                "<list xmlns=\"urn:ietf:params:xml:ns:rlmi\" fullState=\"true\" version=\"0\" uri=\"sip:movial11@dummy.com;pres-list=rcs\">\r\n" +
                "    <resource uri=\"sip:movial19@dummy.com\">\r\n" +
                "        <name>mov19</name>\r\n" +
                "    </resource>\r\n" +
                "    <resource uri=\"sip:movail90@dummy.com\">\r\n" +
                "        <name>nihao</name>\r\n" +
                "    </resource>\r\n" +
                "    <resource uri=\"sip:movial11@dummy.com\">\r\n" +
                "        <name>mov11</name>\r\n" +
                "    </resource>\r\n" +
                "    <resource uri=\"sip:movial10@dummy.com\">\r\n" +
                "        <name>mov10</name>\r\n" +
                "    </resource>\r\n" +
                "    <resource uri=\"sip:movial20@dummy.com\">\r\n" +
                "        <name>mov20</name>\r\n" +
                "    </resource>\r\n" +
                "</list>\r\n" +
                "\r\n" +
                "------=_Part_10037_1594277765.1281008099848--\r\n";

        BaseSipMessage incomingMsg = MessageParser.parse(in);

    }
 */

//
/*
public synchronized void feedPart1(byte[] byteTrafficPart) throws IOException {
    int startIndexOfBodyInsideData = 0;
    if (readingBody && bodyLength > 0) {
        if (bodyLeft <= byteTrafficPart.length) {
            readingBody = false;
            System.arraycopy(byteTrafficPart, 0, body, body.length - bodyLeft, bodyLeft);
            bodyReady = true;
            startIndexOfBodyInsideData += bodyLength;
            feedBody(body);
            prepareAndParse();
        }
        else {
            //Logger.log("continue readingBody");
            System.arraycopy(byteTrafficPart, 0, body, body.length - bodyLeft, byteTrafficPart.length);
            bodyLeft -= byteTrafficPart.length;
            feedBody(body);
            return;
        }
    }
    final String nextPart = new String(byteTrafficPart);
    String stringTrafficPart = fromPrevMsgPart == null ? nextPart : fromPrevMsgPart + nextPart;
    fromPrevMsgPart = null;

    String[] stringsInitail = stringTrafficPart.split(SIPUtil.SIP_TERMINATOR), trafficAsStrings;
    if (stringTrafficPart.endsWith(SIPUtil.BODY_TERMINATOR)) {
        trafficAsStrings = new String[stringsInitail.length + 1];
        System.arraycopy(stringsInitail, 0, trafficAsStrings, 0, stringsInitail.length);
        trafficAsStrings[trafficAsStrings.length - 1] = "";
    }
    //if we found situation when we fed with text like "Cal\n" instead of "Call-ID: <id>\r\n"
    //in other words when header is splitted not by sip terminator we need to get incomplete byteTrafficPart of header
    //and join it to the begining of the next msg byteTrafficPart.
    else if (!stringTrafficPart.endsWith(SIPUtil.SIP_TERMINATOR) && !stringTrafficPart.contains(SIPUtil.BODY_TERMINATOR)) {
        trafficAsStrings = new String[stringsInitail.length - 1];
        System.arraycopy(stringsInitail, 0, trafficAsStrings, 0, trafficAsStrings.length);
        fromPrevMsgPart = stringsInitail[stringsInitail.length - 1];
    }
    else {
        trafficAsStrings = stringsInitail;
    }

    for (String sipTerminatedTrafficPart : trafficAsStrings) {
        //Logger.log("sipTerminatedTrafficPart: '" + sipTerminatedTrafficPart + "'");
        if (sipMessageStartSequenceDetected(sipTerminatedTrafficPart)) {
            clean();
            messageStarted = true;
            //Logger.log("new messageStarted");
        }
        if ("".equals(sipTerminatedTrafficPart.trim())) {
            if (!hasBody) {
                int i = Utils.indexOf(byteTrafficPart, BODY_TERMINATOR_BYTES, startIndexOfBodyInsideData) + 4;
                if (i > startIndexOfBodyInsideData) {
                    startIndexOfBodyInsideData = i;
                }
            }
            if (hasBody && !readingBody && !bodyReady) {
                int i = Utils.indexOf(byteTrafficPart, BODY_TERMINATOR_BYTES, startIndexOfBodyInsideData) + 4;
                if (i > startIndexOfBodyInsideData) {
                    startIndexOfBodyInsideData = i;
                }
                if (bodyLength > 0) {
                    body = new byte[bodyLength];
                    if (i > 0 && i + bodyLength <= byteTrafficPart.length && !framedBody) {
                        readingBody = false;
                        bodyReady = true;
                        startIndexOfBodyInsideData += bodyLength;
                        System.arraycopy(byteTrafficPart, i, body, 0, bodyLength);
                        feedBody(body);
                    }
                    else if (i > 0 && i + bodyLength > byteTrafficPart.length) {
                        framedBody = true;
                        readingBody = true;
                        System.arraycopy(byteTrafficPart, i, body, 0, byteTrafficPart.length - i);
                        bodyLeft = bodyLength - (byteTrafficPart.length - i);
                        feedBody(body);
                    }
                }
            }
            if (!hasBody && "".equals(sipTerminatedTrafficPart.trim())) {
                bodyReady = true;
            }
            feedText(sipTerminatedTrafficPart.trim());
        }

        else {
            int length = feedText(sipTerminatedTrafficPart.trim());
            if (length > 0) {
                bodyLength = length;
            }
        }
    }


}
 */
