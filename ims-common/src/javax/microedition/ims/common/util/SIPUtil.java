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

package javax.microedition.ims.common.util;

import javax.microedition.ims.common.ServerAddress;
import javax.microedition.ims.common.UUIDGenerator;
import javax.microedition.ims.common.sip.HeaderMessagePart;
import javax.microedition.ims.common.sip.HeaderMessagePartDefaultImpl;
import javax.microedition.ims.common.sip.MessagePartDefaultImpl;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 16-Dec-2009
 * Time: 09:59:46
 */
final public class SIPUtil {

    private static final String CONTENT_LENGTH_HEADER_SHORTCUT = "l:";
    public static final String SIP_TERMINATOR = "\r\n";
    public final static String SIP_VERSION = "SIP/2.0";
    public static final String BODY_TERMINATOR = SIP_TERMINATOR + SIP_TERMINATOR;
    public static final byte[] BODY_TERMINATOR_BYTES = BODY_TERMINATOR.getBytes();
    public static final String SIP_MARKER_STRING = "SIP/2.0";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length:";

    private SIPUtil() {
    }

    public static String newCallId() {
        return UUIDGenerator.generateCallId(NetUtils.guessCurrentAddress());
    }

    public static String newTag() {
        return UUIDGenerator.generateUUID();
    }

    public static String randomBranchTail() {
        return UUIDGenerator.generateUUID();
    }

    public static String generateBranchWithMagicCookie(String tail) {
        return "z9hG4bK-" + SIPUtil.randomBranchTail() + tail;
    }


    public static String randomClientNonce() {
        return UUIDGenerator.generateUUID();
    }


    public static String toSipURI(final ServerAddress serverAddress) {
        return new StringBuilder().append("sip:").append(serverAddress.toURI()).toString();
    }

    public static String getSIPVersion() {
        return SIP_VERSION;
    }


    /**
     * Extracts headers part of SIP message and some indexing information about headers position in byte array.
     * Method suppose that parameter is part of  body terminated (SIP_TERMINATOR+SIP_TERMINATOR or \r\n\r\n in other words) sequence
     * or at least contains this sequence itself.
     *
     * @param msgHeaderTrafficPartBytes
     * @return
     */
    public static HeaderMessagePart extractMsgHeaderPart(byte[] msgHeaderTrafficPartBytes) {
        //SIP is textual protocol. So we suppose passed messages rfc 3261 compilant.
        //That means all headers separated by \r\n(SIP_TERMINATOR) sequence.
        //According to sentences above we can convert bytes to String.
        String msgHeaderTrafficPartString = new String(msgHeaderTrafficPartBytes);

        int startSearchFromIndex = msgHeaderTrafficPartString.indexOf(BODY_TERMINATOR);
        if (startSearchFromIndex < 0) {
            startSearchFromIndex = msgHeaderTrafficPartString.length();
        }
        startSearchFromIndex = startSearchFromIndex > 0 ? startSearchFromIndex - 1 : 0;

        //This method suppose that parameter is part of body terrminated sequence.
        //here we start backward search of SIP marker (SIP/2.0) to find where message begins from
        //SIP request and respones differs in way they use SIP marker:
        //requests ends with SIP marker+SIP terminator, and responces starts with SIP marker+' '(one whitespace)
        //As far as we don't know what we are reading we have to try to find both of them
        final int possibleRqstIndex = msgHeaderTrafficPartString.lastIndexOf(
                SIP_MARKER_STRING + SIP_TERMINATOR, startSearchFromIndex
        );
        final int possibleRspnsIndex = msgHeaderTrafficPartString.lastIndexOf(
                SIP_TERMINATOR + SIP_MARKER_STRING + " ", startSearchFromIndex
        );
        final boolean responseStartsTrafficPart = msgHeaderTrafficPartString.startsWith(SIP_MARKER_STRING + " ");

        int msgStartIndex = -1;

        //this branch is run if we found request marker
        boolean sipMarkerFound = true;
        if (possibleRqstIndex > 0) {
            //to find message start position we have to find previous sip terminator or start from beggining of the string
            msgStartIndex = msgHeaderTrafficPartString.lastIndexOf(SIP_TERMINATOR, possibleRqstIndex);
            //if we find previous terminator we have to compensate it length otherwise start from 0 positon
            msgStartIndex = msgStartIndex < 0 ? 0 : msgStartIndex + SIP_TERMINATOR.length();
        }
        //this branch is run if we found response marker
        else if (possibleRspnsIndex > 0 || responseStartsTrafficPart) {
            //if we find previous terminator we have to compensate it length otherwise start from 0 positon
            msgStartIndex = possibleRspnsIndex < 0 ? 0 : possibleRspnsIndex + SIP_TERMINATOR.length();
        }
        else {
            sipMarkerFound = false;
        }


        int contentLength = -1;
        final int headerBytesLength = startSearchFromIndex - msgStartIndex + 1;
        byte[] msgHeaderBytes = new byte[0];

        if (sipMarkerFound && headerBytesLength > 0) {
            msgHeaderBytes = new byte[headerBytesLength];
            System.arraycopy(msgHeaderTrafficPartBytes, msgStartIndex, msgHeaderBytes, 0, msgHeaderBytes.length);

            final String headersString = new String(msgHeaderBytes);
            contentLength = extractContentLength(headersString);
            contentLength = contentLength < 0 ? 0 : contentLength;
        }

        return contentLength < 0 ?
                null :
                new HeaderMessagePartDefaultImpl(
                        new MessagePartDefaultImpl(
                                msgStartIndex,
                                startSearchFromIndex,
                                msgHeaderBytes
                        ),
                        contentLength
                );
    }

    private static int extractContentLength(String headersString) {
        int retValue = -1;
        
        String actualHeaderName = CONTENT_LENGTH_HEADER;
        int contentLengthStartIndex = headersString.indexOf(CONTENT_LENGTH_HEADER);
        if(contentLengthStartIndex < 0) {
            contentLengthStartIndex = headersString.indexOf(CONTENT_LENGTH_HEADER_SHORTCUT);
            actualHeaderName = CONTENT_LENGTH_HEADER_SHORTCUT;
        }
        
        if (contentLengthStartIndex > 0) {
            int contentLengthEndIndex = headersString.indexOf(SIP_TERMINATOR, contentLengthStartIndex);
            contentLengthEndIndex = contentLengthEndIndex < 0 ? headersString.length() : contentLengthEndIndex;

            final String contentLengthStringValue = headersString.substring(
                    contentLengthStartIndex + actualHeaderName.length(),
                    contentLengthEndIndex
            );

            try {
                retValue = Integer.parseInt(contentLengthStringValue.trim());
            }
            catch (NumberFormatException e) {
                //do nothing, code above will interpret -1 default value;
            }
        }

        return retValue;
    }
}
