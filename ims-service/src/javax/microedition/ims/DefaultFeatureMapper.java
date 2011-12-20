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

import javax.microedition.ims.common.EventPackage;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.common.OptionFeature;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static javax.microedition.ims.common.MessageType.*;

public class DefaultFeatureMapper implements FeatureMapper {
    private final Map<MessageType, OptionFeature[]> featureMapping = new HashMap<MessageType, OptionFeature[]>();

    {
        featureMapping.put(SIP_INVITE, new OptionFeature[]{OptionFeature._100REL});
        featureMapping.put(SIP_ACK, new OptionFeature[]{});
        featureMapping.put(SIP_PRACK, new OptionFeature[]{OptionFeature._100REL});
        featureMapping.put(SIP_BYE, new OptionFeature[]{});
        featureMapping.put(SIP_CANCEL, new OptionFeature[]{});
        featureMapping.put(SIP_REFER, new OptionFeature[]{});
        featureMapping.put(SIP_NOTIFY, new OptionFeature[]{});
        featureMapping.put(SIP_MESSAGE, new OptionFeature[]{});
        featureMapping.put(SIP_OPTIONS, new OptionFeature[]{});
        featureMapping.put(SIP_UPDATE, new OptionFeature[]{});

        featureMapping.put(SIP_REGISTER, new OptionFeature[]{OptionFeature.PATH, OptionFeature._100REL, OptionFeature.EVENTLIST});
        featureMapping.put(SIP_SUBSCRIBE, new OptionFeature[]{OptionFeature._100REL, OptionFeature.EVENTLIST, OptionFeature.TIMER});
        featureMapping.put(SIP_PUBLISH, new OptionFeature[]{});
    }

    private static final EnumSet<MimeType> DEFAULT_MIME_SET = EnumSet.of(
            MimeType.APP_PIDF_XML,
            MimeType.APP_RLMI_XML,
            MimeType.MULTIPART_RELATED
    );
    private final Map<EventPackage, Set<MimeType>> mimeTypeMap = new HashMap<EventPackage, Set<MimeType>>();

    {
        mimeTypeMap.put(EventPackage.PRESENCE_WINFO, EnumSet.of(MimeType.APP_WATCHERINFO_XML));
        mimeTypeMap.put(EventPackage.WINFO, DEFAULT_MIME_SET);
        mimeTypeMap.put(EventPackage.DIALOG, DEFAULT_MIME_SET);
        mimeTypeMap.put(EventPackage.MESSAGE_SUMMARY, DEFAULT_MIME_SET);
        mimeTypeMap.put(EventPackage.PRESENCE, DEFAULT_MIME_SET);
        mimeTypeMap.put(EventPackage.REFER, DEFAULT_MIME_SET);
        mimeTypeMap.put(EventPackage.REG, EnumSet.of(MimeType.APP_REGINFO_XML));
        mimeTypeMap.put(EventPackage.XCAP_DIFF, DEFAULT_MIME_SET);
    }

    public OptionFeature[] getSupportedFeaturesByType(MessageType type) {
        return featureMapping.get(type);
    }

    public MimeType[] getTypesByEventPackage(final EventPackage eventPackage) {
        final Set<MimeType> typesSet = mimeTypeMap.get(eventPackage);
        return typesSet == null ?
                new MimeType[0] :
                typesSet.toArray(new MimeType[typesSet.size()]);
    }
}
