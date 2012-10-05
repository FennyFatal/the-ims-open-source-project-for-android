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

package javax.microedition.ims.core;

import javax.microedition.ims.core.AcceptContactDescriptor.Pair;
import javax.microedition.ims.core.registry.ClientRegistry;
import javax.microedition.ims.core.registry.property.*;
import javax.microedition.ims.core.registry.property.StreamProperty.StreamType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class AcceptContactDescriptorCreator {

    public static AcceptContactDescriptor[] create(ClientRegistry clientRegistry, boolean containFT) {
        List<AcceptContactDescriptor> headers = new ArrayList<AcceptContactDescriptor>();

        //StreamMedia
        StreamProperty streamProperty = clientRegistry.getStreamProperty();
        if (streamProperty != null && streamProperty.getTypes() != null && !streamProperty.getTypes().isEmpty()) {
            if (containsType(streamProperty.getTypes(), StreamType.AUDIO)) {
                appendOperation(headers, new AcceptContactDescriptor.Pair("audio", null), false, false);
            }
            if (containsType(streamProperty.getTypes(), StreamType.VIDEO)) {
                appendOperation(headers, new AcceptContactDescriptor.Pair("video", null), false, false);
            }
        }


        //FramedMedia
        FrameProperty frameProperty = clientRegistry.getFrameProperty();
        if (frameProperty != null && frameProperty.getContentTypes() != null
                && frameProperty.getContentTypes().length > 0 && frameProperty.getMaxSize() > 0) {
            appendOperation(headers, new AcceptContactDescriptor.Pair("message", null), false, false);
        }

        //BasicMedia
        BasicProperty basicProperty = clientRegistry.getBasicProperty();
        if (basicProperty != null && basicProperty.getContentTypes() != null) {
            for (String contentType : basicProperty.getContentTypes()) {
                if (contentType != null && contentType.length() > 0) {
                    String[] contentPartTypes = parseContentType(contentType);
                    if (contentPartTypes[0].equalsIgnoreCase("application")) {
                        appendOperation(headers, new AcceptContactDescriptor.Pair("application", null), false, false);
                        appendOperation(headers, new AcceptContactDescriptor.Pair("app_subtype", contentPartTypes[1]), false, false);

                    }
                    else if (contentPartTypes[0].equalsIgnoreCase("video")) {
                        appendOperation(headers, new AcceptContactDescriptor.Pair("video", null), false, false);

                    }
                    else if (contentPartTypes[0].equalsIgnoreCase("audio")) {
                        appendOperation(headers, new AcceptContactDescriptor.Pair("audio", null), false, false);
                    }
                }
            }
        }

        //Event
        EventProperty eventProperty = clientRegistry.getEventProperty();
        if (eventProperty != null && eventProperty.getPackages() != null) {
            for (String pkg : eventProperty.getPackages()) {
                if (pkg != null && pkg.length() > 0) {
                    appendOperation(headers, new AcceptContactDescriptor.Pair("events", pkg), false, false);
                }
            }
        }

        //CoreService
        CoreServiceProperty coreServiceProperty = clientRegistry.getCoreServiceProperty();
        if (coreServiceProperty != null) {
            if (coreServiceProperty.getIARIs() != null) {
                for (String iari : coreServiceProperty.getIARIs()) {
                    if (iari != null && iari.length() > 0) {
                        appendOperation(headers, new AcceptContactDescriptor.Pair("+g.3gpp.iari-ref", iari), false, true);
                    }
                }
            }
            if (coreServiceProperty.getICSIs() != null) {
                for (String icsi : coreServiceProperty.getICSIs()) {
                    if (icsi != null && icsi.length() > 0) {
                        boolean explicit = hasSubValue(icsi, "explicit");
                        boolean require = hasSubValue(icsi, "require");
                        if (explicit)   icsi = icsi.replace(";explicit", "");
                        if (require)    icsi = icsi.replace(";require", "");
                        appendOperation(headers, new AcceptContactDescriptor.Pair("+g.3gpp.icsi-ref", icsi), explicit, require);
                    }
                }
            }
            if (coreServiceProperty.getFeatureTags() != null && containFT) {
                for (String ft : coreServiceProperty.getFeatureTags()) {
                    if (ft != null && ft.length() > 0) {
                        boolean explicit = hasSubValue(ft, "explicit");
                        boolean require = hasSubValue(ft, "require");
                        if (explicit)   ft = ft.replace(";explicit", "");
                        if (require)    ft = ft.replace(";require", "");
                        appendOperation(headers, new AcceptContactDescriptor.Pair(ft, null), explicit, require);
                    }
                }
            }
        }
        for (AcceptContactDescriptor h : headers) {
            if (h.isExplicit())
                h.addRecord(new AcceptContactDescriptor.Pair("explicit", null));
            if (h.isRequire())
                h.addRecord(new AcceptContactDescriptor.Pair("require", null));
        }
        return headers.toArray(new AcceptContactDescriptor[headers.size()]);
    }

    private static boolean hasSubValue(String value, String subvalue) {
        StringTokenizer st = new StringTokenizer(value, ";");
        while (st.hasMoreTokens()) {
            if (subvalue.equalsIgnoreCase(st.nextToken())) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsType(Set<StreamType> types, StreamType streamType) {
        for (StreamType type : types) {
            if (type.equals(streamType)) {
                return true;
            }
        }
        return false;
    }

    private static String[] parseContentType(String contentType) {
        int slashInd = contentType.indexOf('/');
        if (slashInd == -1) {
            return new String[]{contentType};
        }
        String[] result = new String[2];
        result[0] = contentType.substring(0, slashInd);
        result[1] = contentType.substring(slashInd + 1);
        return result;
    }

    /*
      * operation Fs <= T
      */
    private static void appendOperation(List<AcceptContactDescriptor> headers, AcceptContactDescriptor.Pair T, boolean explicit, boolean require) {
        /*
           * T is already part
           */
        for (AcceptContactDescriptor h : headers) {
            if (h.contains(T)) {
                return;
            }
        }

        /*
           * add to header which is free from tag and with the same flags
           */
        AcceptContactDescriptor hFound = searchFreeFromTagWithTheSameFlags(headers, T, explicit, require);
        if (hFound != null) {
            hFound.addRecord(T);
        }
        else {
            /*
                * create new header and mark with associated flags
                */
            AcceptContactDescriptor newHeader = new AcceptContactDescriptor();
            newHeader.addRecord(new Pair("*", null));
            newHeader.addRecord(T);
            if (explicit) {
                newHeader.setExplicit();
            }
            if (require) {
                newHeader.setRequire();
            }
            headers.add(newHeader);
        }
    }

    private static AcceptContactDescriptor searchFreeFromTagWithTheSameFlags(List<AcceptContactDescriptor> headers,
                                                                             AcceptContactDescriptor.Pair T, boolean explicit, boolean require) {

        AcceptContactDescriptor hFound = null;

        for (AcceptContactDescriptor h : headers) {
            if (h.isFreeFromTag(T.getKey()) && explicit == h.isExplicit() && require == h.isRequire()) {
                hFound = h;
                break;
            }
        }
        return hFound;
    }

}
