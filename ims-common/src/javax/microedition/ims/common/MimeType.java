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

package javax.microedition.ims.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains values for headers.
 *
 * @author ext-akhomush
 */
public enum MimeType {

    TEXT_PLAIN(MimeTypeClass.TEXT, "plain"),

    IMAGE_BMP(MimeTypeClass.IMAGE, "bmp"),
    IMAGE_GIF(MimeTypeClass.IMAGE, "gif"),
    IMAGE_JPEG(MimeTypeClass.IMAGE, "jpeg"),
    IMAGE_PNG(MimeTypeClass.IMAGE, "png"),
    IMAGE_TIFF(MimeTypeClass.IMAGE, "tiff"),

    MULTIPART(MimeTypeClass.MULTIPART),
    MULTIPART_MIME(MimeTypeClass.MULTIPART, "mime"),
    MULTIPART_MIXED(MimeTypeClass.MULTIPART, "mixed"),
    MULTIPART_RELATED(MimeTypeClass.MULTIPART, "related"),

    APP_REGINFO_XML(MimeTypeClass.APPLICATION, "reginfo+xml"),
    APP_PIDF_XML(MimeTypeClass.APPLICATION, "pidf+xml"),
    APP_RLMI_XML(MimeTypeClass.APPLICATION, "rlmi+xml"),
    APP_XCAP_DIFF_XML(MimeTypeClass.APPLICATION, "xcap-diff+xml"),
    APP_WATCHERINFO_XML(MimeTypeClass.APPLICATION, "watcherinfo+xml"),
    APP_RESOURCE_LISTS_XML(MimeTypeClass.APPLICATION, "resource-lists+xml"),
    APP_IM_ISCOMPOSING_XML(MimeTypeClass.APPLICATION, "im-iscomposing+xml"),
    APP_SDP(MimeTypeClass.APPLICATION, "sdp"),

    MSG_SIP_FRAG(MimeTypeClass.MESSAGE, "sipfrag");


    private final static Map<String, MimeType> mapping = new HashMap<String, MimeType>();

    static {
        for (MimeType featureName : values()) {
            mapping.put(featureName.stringValue, featureName);
        }
    }

    private String stringValue;
    private final MimeTypeClass mimeTypeClass;
    private final String subclassName;

    private MimeType(final MimeTypeClass mimeTypeClass, final String subclassName) {
        this.mimeTypeClass = mimeTypeClass;
        this.subclassName = subclassName;
        this.stringValue = mimeTypeClass.stringValue()+"/"+(subclassName == null ? "" : subclassName);
    }

    private MimeType(final MimeTypeClass mimeTypeClass) {
        this(mimeTypeClass, null);
    }

    public String stringValue() {
        return stringValue;
    }

    public MimeTypeClass getMimeTypeClass() {
        return mimeTypeClass;
    }

    public String getSubclassName() {
        return subclassName;
    }

    public static MimeType parse(final String featureName) {
        return mapping.get(featureName);
    }
}
