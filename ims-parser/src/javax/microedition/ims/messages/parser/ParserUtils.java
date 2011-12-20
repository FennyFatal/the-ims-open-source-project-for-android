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

package javax.microedition.ims.messages.parser;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.ParamHeader;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ParserUtils {

    public static String urlDecode(String s) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%') {
                i++;
                ret.append((char) toNumber(s.substring(i, i + 2), 0, 16));
                i++;
            }
            else {
                ret.append(c);
            }
        }
        return ret.toString();
    }


    public static int toNumber(String parseMe, int defValue, int radix) {
        int ret = defValue;
        try {
            ret = Integer.parseInt(parseMe, radix);
        }
        catch (NumberFormatException e) {
            Logger.log(Logger.Tag.PARSER, "Error parsing int: " + parseMe);
        }
        return ret;
    }


    public static long toLongNumber(String parseMe, int defValue, int radix) {
        long ret = defValue;
        try {
            ret = Long.parseLong(parseMe, radix);
        }
        catch (NumberFormatException e) {
            Logger.log(Logger.Tag.PARSER, "Error parsing long: " + parseMe);
        }
        return ret;
    }


    public static String decodeDisplayName(String dn) {
        StringBuilder result = new StringBuilder();
        if (dn.length() < 2) {
            return dn;
        }
        for (int i = 0; i < dn.length(); ++i) {
            if (dn.charAt(i) == '\\' && dn.charAt(i + 1) == '\\') {
                result.append('\\');
                i++;
            }
            else if (dn.charAt(i) == '\\' && dn.charAt(i + 1) == '"') {
                result.append('"');
                i++;
            }
            else {
                result.append(dn.charAt(i));
            }
        }
        return result.toString();
    }

    public static String encodeDisplayName(String dn) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < dn.length(); ++i) {
            if (dn.charAt(i) == '\\') {
                result.append("\\\\");
            }
            else if (dn.charAt(i) == '"') {
                result.append("\\\"");
            }
            else {
                result.append(dn.charAt(i));
            }
        }
        return result.toString();
    }


    public static String buildStringValue(final Collection<String> list) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(" ,");
            }
        }
        return sb.toString();
    }

    public static String buildHeaderValue(final Collection<UriHeader> list) {
        return buildStringValue(asStringCollection(list));
    }

    public static Collection<String> asStringCollection(final Collection<? extends ParamHeader> list) {
        Collection<String> builtUris = new ArrayList<String>(list.size() * 2);
        for (ParamHeader header : list) {
            builtUris.add(header.buildContent());
        }
        return builtUris;
    }

    public static StringBuilder appendUriHeadersList(
            final StringBuilder sb,
            final Collection<UriHeader> list,
            final Header headerName) {

        if (list != null && !list.isEmpty()) {
            sb.append(headerName.stringValue()).append(StringUtils.DOTS);
            sb.append(buildStringValue(asStringCollection(list)));
            sb.append(StringUtils.SIP_TERMINATOR);
        }
        return sb;
    }

    public static StringBuilder appendStringList(
            final StringBuilder sb,
            final Collection<String> list,
            final Header headerName) {

        if (list != null && !list.isEmpty()) {
            sb.append(headerName.stringValue()).append(StringUtils.DOTS);
            sb.append(buildStringValue(list));
            sb.append(StringUtils.SIP_TERMINATOR);
        }
        return sb;
    }

    public static StringBuilder appendStringsList(
            final StringBuilder sb,
            final Collection<String> list,
            final Header headerName) {

        if (list != null && !list.isEmpty()) {
            sb.append(headerName.stringValue()).append(StringUtils.DOTS);
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    //sb.append(" ,");
                    sb.append(", ");
                }
            }
            sb.append(StringUtils.SIP_TERMINATOR);
        }
        return sb;
    }
}
