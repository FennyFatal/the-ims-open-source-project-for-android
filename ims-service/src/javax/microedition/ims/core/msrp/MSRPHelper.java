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

package javax.microedition.ims.core.msrp;

import javax.microedition.ims.common.SDPType;
import javax.microedition.ims.core.msrp.filetransfer.FileDescriptor;
import javax.microedition.ims.messages.parser.msrp.MsrpUriParser;
import javax.microedition.ims.messages.utils.MsrpUtils;
import javax.microedition.ims.messages.wrappers.msrp.MsrpUri;
import javax.microedition.ims.messages.wrappers.sdp.*;
import java.util.*;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 7.5.2010
 * Time: 19.09.05
 */
final class MSRPHelper {
    static MsrpUri obtainMSRPURI(final SdpMessage sdpMessage) {

        final Attribute msrpURIAttribute = lookUpSDPAttribute(
                sdpMessage,
                new FindFirstAppropriateAttributeAdapter() {
                    public boolean isAppropriateMedia(final Media media) {
                        return media.getProtocol().equals(MsrpUtils.TCP_MSRP);
                    }

                    public boolean isAppropriateAttribute(final Attribute mediaAttribute) {
                        String value = mediaAttribute.getValue();
                        return value != null && value.startsWith("msrp://");
                    }
                }
        );

        return msrpURIAttribute == null || msrpURIAttribute.getValue() == null ?
                null :
                MsrpUriParser.parse(msrpURIAttribute.getValue());
    }

    static Attribute obtainFileSelectorAttribute(final SdpMessage sdpMessage) {

        return lookUpSDPAttribute(
                sdpMessage,
                new FindFirstAppropriateAttributeAdapter() {
                    public boolean isAppropriateMedia(final Media media) {
                        return media.getProtocol().equals(MsrpUtils.TCP_MSRP);
                    }

                    public boolean isAppropriateAttribute(final Attribute mediaAttribute) {
                        return mediaAttribute.getName().startsWith("file-selector");
                    }
                }
        );
    }

    static interface AttributeLookUp {
        boolean isAppropriateMedia(Media media);

        boolean isAppropriateAttribute(Attribute mediaAttribute);

        boolean onAppropriateAttribute(Media media, Attribute mediaAttribute);
    }

    static class FindFirstAppropriateAttributeAdapter implements AttributeLookUp {
        public boolean isAppropriateMedia(Media media) {
            return true;
        }

        public boolean isAppropriateAttribute(Attribute mediaAttribute) {
            return true;
        }

        public boolean onAppropriateAttribute(Media media, Attribute mediaAttribute) {
            return false;
        }
    }

    private MSRPHelper() {
    }


    static List<Attribute> createMSRPMediaAttributes(boolean active, String fromPathStringValue) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("path", fromPathStringValue));
        attributes.add(new Attribute("setup", active ? "active" : "passive"));
        attributes.add(new Attribute("accept-types", "*"));
        return attributes;
    }

    static List<Attribute> createMSRPMediaAttributes(boolean active, String fromPathStringValue, FileDescriptor fileDescriptor) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("path", fromPathStringValue));
        attributes.add(new Attribute("setup", active ? "active" : "passive"));
        attributes.add(new Attribute("accept-types", "*"));
        // attributes.add(new Attribute("accept-wrapped-types", "*"));
        attributes.add(new Attribute("file-selector", fileDescriptor.buildContent()));
        //attributes.add(new Attribute("file-disposition", "attachment")); //"render"
        attributes.add(new Attribute("file-transfer-id", fileDescriptor.getFileId()));
        if (fileDescriptor.getCreationDate() != null) {
            attributes.add(new Attribute("file-date", fileDescriptor.getCreationDate().toString()));
        }
        //attributes.add(new Attribute("file-range", descriptor.getFileRange()[0]+"-"+descriptor.getFileRange()[1]));
        return attributes;
    }

    static List<Media> createMSRPMedias(int port, List<Attribute> attributes, MSRPSessionType sessionType) {
        Media media = new Media();
        media.setType("message");
        media.setPort(port);
        media.setFormats(Arrays.asList("*"));
        media.setProtocol("TCP/MSRP");
        for (Attribute a : attributes) {
            media.addAttribute(a);
        }
        if (sessionType == MSRPSessionType.FILE_IN) {
            media.setDirection(DirectionsType.DirectionReceiveOnly);
        }
        else if (sessionType == MSRPSessionType.FILE_OUT) {
            media.setDirection(DirectionsType.DirectionSendOnly);
        }

        return Arrays.asList(media);
    }

    static void updateSDPWithMSRPData(final SdpMessage sdpMessage, final MSRPSDPUpdateData updateData) {

        final String fromPathStringValue = updateData.getFromPath().buildContent();

        List<Attribute> attributes = createMSRPMediaAttributes(updateData.isActive(), fromPathStringValue);
        List<Media> mediaList = createMSRPMedias(updateData.getPort(), attributes, updateData.getSessionType());

        final String address = updateData.getAddress();
        sdpMessage.setSessionAddress(address);
        sdpMessage.setConnectionInfo(new ConnectionInfo(address, true));
        sdpMessage.addMedias(mediaList);
    }

    static void updateSDPWithMSRPData(final SdpMessage sdpMessage, final MSRPSDPUpdateData updateData, FileDescriptor fileDescriptor) {

        final String fromPathStringValue = updateData.getFromPath().buildContent();

        List<Attribute> attributes = createMSRPMediaAttributes(updateData.isActive(), fromPathStringValue, fileDescriptor);
        List<Media> mediaList = createMSRPMedias(updateData.getPort(), attributes, updateData.getSessionType());

        final String address = updateData.getAddress();
        sdpMessage.setSessionAddress(address);
        sdpMessage.setConnectionInfo(new ConnectionInfo(address, true));
        sdpMessage.addMedias(mediaList);
    }

    static Attribute lookUpSDPAttribute(final SdpMessage sdpMessage, final AttributeLookUp attributeLookUp) {

        assert sdpMessage != null : "null SDP message is not allowed here.";
        assert sdpMessage.typeSupported(SDPType.MSRP) : "SDP message doesn't support MSRP";

        if (sdpMessage == null) {
            throw new NullPointerException("null SDP message is not allowed here");
        }

        if (!sdpMessage.typeSupported(SDPType.MSRP)) {
            throw new IllegalArgumentException("SDP message doesn't support MSRP");
        }

        Attribute lastAppropriateMediaAttribute = null;

        stopLookUp:
        for (Media media : sdpMessage.getMedias()) {

            if (media != null && attributeLookUp.isAppropriateMedia(media)) {
                Iterator<Attribute> attributes = media.getAttributes();
                while (attributes.hasNext()) {
                    Attribute mediaAttribute = attributes.next();
                    if (mediaAttribute != null && attributeLookUp.isAppropriateAttribute(mediaAttribute)) {

                        lastAppropriateMediaAttribute = mediaAttribute;
                        final boolean stopSLookUp = !attributeLookUp.onAppropriateAttribute(media, mediaAttribute);

                        if (stopSLookUp) {
                            break stopLookUp;
                        }
                    }
                }
            }
        }

        return lastAppropriateMediaAttribute;
    }

    private static final List<Integer> assignedPorts = Collections.synchronizedList(new ArrayList<Integer>());

    public static int generateLocalPort(int initial) {
        int port = initial;
        while (assignedPorts.contains(port)) {
            port++;
        }
        assignedPorts.add(port);
        return port;
    }

    public static void freeLocalPort(int port) {
        if (assignedPorts.contains(port)) {
            assignedPorts.remove(new Integer(port));
        }
    }
}
