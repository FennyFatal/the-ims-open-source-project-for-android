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


import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides utility methods for String instances.
 *
 * @author ext-akhomush
 */
public final class StringUtils {
    private static final byte OPEN_ARROW_CHAR = 60;
    private static final byte QUESTION_CHAR = 63;
    private static final byte CLOSE_ARROW_CHAR = 62;
    private static final Pattern XML_ENCODING_PATTERN = Pattern.compile("encoding\\w*=w*\"?([^\"]+)\"?");

    public static final String SIP_TERMINATOR = "\r\n";
    public static final String SPACE = " ";
    public static final String SIP_VERSION = "SIP/2.0";
    public static final String SIP = "sip:";
    public final static String DOTS = ": ";


    private StringUtils() {
        assert false;
    }

    //TODO: duplicate in CollectionsUtils

    public static String joinArray(String[] strings, int from, int to, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i != from) {
                sb.append(separator);
            }
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    //TODO: duplicate in CollectionsUtils

    public static String joinList(List<String> toJoin, String separator) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<String> iterator = toJoin.iterator(); iterator.hasNext();) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     * <p/>
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = true
     * StringUtils.isEmpty("alice")     = false
     * StringUtils.isEmpty("  alice  ") = false
     * </pre>
     * <p/>
     * This method trims the String.
     *
     * @param str - the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * @param str - the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     * @see #isEmpty(String)
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static final String HEXES = "0123456789ABCDEF";

    public static String byteArrayToHexString(byte[] raw) {
        if (raw == null) {
            return null;
        }

        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static String grabEncodingFromXMLBytes(byte[] content) {
        final String headerTag = grabHeaderTag(content);
        return headerTag == null ? null : grabEncoding(headerTag);
    }

    public static String grabEncoding(final String headerTag) {
        final Matcher matcher = XML_ENCODING_PATTERN.matcher(headerTag);
        String encoding = null;
        if (matcher.find()) {
            encoding = matcher.group(1);
        }
        return encoding == null ? null : encoding.trim();
    }

    public static String grabHeaderTag(final byte[] content) {
        int firstPos = -1;
        for (int i = 0; i < content.length; i++) {
            byte b = content[i];
            if (b == OPEN_ARROW_CHAR) {
                if (content.length > i + 1 && content[i + 1] == QUESTION_CHAR) {
                    firstPos = i;
                }
                break;
            }
        }

        int lastPos = -1;
        if (firstPos >= 0) {
            for (int i = firstPos; i < content.length; i++) {
                byte b = content[i];
                if (b == CLOSE_ARROW_CHAR) {
                    if (content[i - 1] == QUESTION_CHAR) {
                        lastPos = i;
                    }
                    break;
                }
            }
        }

        String headerTag = null;
        if (lastPos >= 0) {
            final byte[] headerTagBytes = new byte[lastPos - firstPos + 1];
            System.arraycopy(content, firstPos, headerTagBytes, 0, headerTagBytes.length);
            headerTag = new String(headerTagBytes);
        }

        return headerTag;
    }
}
