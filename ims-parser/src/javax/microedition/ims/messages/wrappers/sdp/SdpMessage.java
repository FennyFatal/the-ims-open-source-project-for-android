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

package javax.microedition.ims.messages.wrappers.sdp;

import javax.microedition.ims.common.SDPType;
import javax.microedition.ims.common.util.StringUtils;
import java.util.*;

public class SdpMessage {
    private int version;  // always 0 in RFC 4566
    private long sessionId = System.currentTimeMillis();
    private long sessionVersion = 1;
    private NetType netType = NetType.NETIN;
    private AddrType addrType = AddrType.IP4;
    private String username = "-", sessionAddress, sessionName = "-", sessionInformation, uri;
    private final List<String> emailAddresses = new ArrayList<String>();
    private final List<String> phoneNumbers = new ArrayList<String>();
    ;

    private ConnectionInfo connectionInfo;
    private EncryptionKey encryptionKey;
    private final List<Bandwidth> bandwidthList = new ArrayList<Bandwidth>();
    private final List<Timing> timings = new ArrayList<Timing>();
    private final List<ZoneAdjustment> zoneAdjustments = new ArrayList<ZoneAdjustment>();
    private final List<Attribute> attributes = new ArrayList<Attribute>();
    private final List<Media> medias = new ArrayList<Media>();

    private static Map<SDPType, Set<String>> sdpTypeMapping = new HashMap<SDPType, Set<String>>();

    static {
        Set<String> voipTypes = new HashSet<String>();
        voipTypes.add("audio");
        voipTypes.add("video");
        sdpTypeMapping.put(SDPType.VOIP, voipTypes);

        Set<String> msrpTypes = new HashSet<String>();
        msrpTypes.add("message");
        sdpTypeMapping.put(SDPType.MSRP, msrpTypes);
    }


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getSessionVersion() {
        return sessionVersion;
    }

    public void setSessionVersion(long sessionVersion) {
        this.sessionVersion = sessionVersion;
    }

    public NetType getNetType() {
        return netType;
    }

    public void setNetType(NetType netType) {
        this.netType = netType;
    }

    public AddrType getAddrType() {
        return addrType;
    }

    public void setAddrType(AddrType addrType) {
        this.addrType = addrType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionAddress() {
        return sessionAddress;
    }

    public void setSessionAddress(String sessionAddress) {
        this.sessionAddress = sessionAddress;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionInformation() {
        return sessionInformation;
    }

    public void setSessionInformation(String sessionInformation) {
        this.sessionInformation = sessionInformation;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public List<Bandwidth> getBandwidthList() {
        return bandwidthList;
    }

    public void addBandwidth(Bandwidth bandwidth) {
        this.bandwidthList.add(bandwidth);
    }

    public List<Timing> getTimings() {
        return timings;
    }

    public List<ZoneAdjustment> getZoneAdjustments() {
        return zoneAdjustments;
    }

    public EncryptionKey getEncryptionKeys() {
        return encryptionKey;
    }

    public void setEncryptionKeys(EncryptionKey encryptionKeys) {
        this.encryptionKey = encryptionKeys;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Media> getMedias() {
        return medias;
    }

    public void addMedias(List<Media> mediasToAdd) {
        medias.addAll(mediasToAdd);
    }

    public void addMedia(Media mediaToAdd) {
        medias.add(mediaToAdd);
    }

    public void clearMedias() {
        medias.clear();
    }

    /*    public void setMedias(List<Media> medias) {
            this.medias = medias;
        }
    */
    /*
      * v=0\r\n"+
 "o=jdoe 2890844526 2890842807 IN IP4 10.47.16.5\r\n"+
 "s=SDP Seminar\r\n"+
 "i=A Seminar on the session description protocol\r\n"+
 "u=http://www.example.com/seminars/sdp.pdf\r\n"+
 "e=j.doe@example.com (Jane Doe)\r\n"+
 "c=IN IP4 224.2.17.12/127\r\n"+
 "t=2873397496 2873404696\r\n"+
 "a=recvonly\r\n"+
 "m=video 49170 RTP/AVP 100 99 97\r\n"+
 "a=rtpmap:97 H264/90000\r\n"+
 "a=fmtp:97 profile-level-id=42A01E; packetization-mode=0;sprop-parameter-sets=Z0IACpZTBYmI,aMljiA==,As0DEWlsIOp==,KyzFGleR\r\n"+
 "a=rtpmap:99 H264/90000\r\n"+
 "a=fmtp:99 profile-level-id=42A01E; packetization-mode=1;sprop-parameter-sets=Z0IACpZTBYmI,aMljiA==,As0DEWlsIOp==,KyzFGleR; max-rcmd-nalu-size=3980\r\n"+
 "a=rtpmap:100 H264/90000\r\n"+
 "a=fmtp:100 profile-level-id=42A01E; packetization-mode=2;sprop-parameter-sets=Z0IACpZTBYmI,aMljiA==,As0DEWlsIOp==,KyzFGleR; sprop-interleaving-depth=60;sprop-deint-buf-req=86000; sprop-init-buf-time=156320;deint-buf-cap=128000; max-rcmd-nalu-size=3980\r\n";
      */
    public String getSessionOrigin() {
        StringBuilder sb = new StringBuilder();
        sb.append(username).append(" ").append(sessionId).append(" ").append(sessionVersion);
        sb.append(" ").append(netType.getValue()).append(" ").append(addrType.name()).append(" ").append(sessionAddress);
        return sb.toString();
    }

    public String getContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("v=0").append(StringUtils.SIP_TERMINATOR);
        sb.append("o=").append(getSessionOrigin()).append(StringUtils.SIP_TERMINATOR);
        if (sessionName != null) {
            sb.append("s=").append(sessionName).append(StringUtils.SIP_TERMINATOR);
        }
        if (sessionInformation != null) {
            sb.append("i=").append(sessionInformation).append(StringUtils.SIP_TERMINATOR);
        }
        if (uri != null) {
            sb.append("u=").append(uri).append(StringUtils.SIP_TERMINATOR);
        }

        for (String e : emailAddresses) {
            sb.append("e=").append(e).append(StringUtils.SIP_TERMINATOR);
        }

        for (String p : phoneNumbers) {
            sb.append("p=").append(p).append(StringUtils.SIP_TERMINATOR);
        }

        if (connectionInfo != null) {
            sb.append("c=").append(connectionInfo.getContent()).append(StringUtils.SIP_TERMINATOR);
        }

        for (Bandwidth bandwidth : bandwidthList) {
            sb.append("b=").append(bandwidth.getContent()).append(StringUtils.SIP_TERMINATOR);
        }

        if (timings.size() == 0) {
            sb.append("t=0 0").append(StringUtils.SIP_TERMINATOR);
        }
        else {
            for (Timing t : timings) {
                sb.append(t.getContent());
            }
        }

        if (zoneAdjustments.size() > 0) {
            sb.append("z=");
            for (ZoneAdjustment z : zoneAdjustments) {
                sb.append(z.getContent()).append(" ");
            }
            sb.setLength(sb.length() - 1);
            sb.append(StringUtils.SIP_TERMINATOR);

        }

        if (encryptionKey != null && encryptionKey.getValue() != null) {
            sb.append("k=").append(encryptionKey.getValue()).append(StringUtils.SIP_TERMINATOR);
        }

        for (Attribute a : attributes) {
            sb.append("a=").append(a.getContent()).append(StringUtils.SIP_TERMINATOR);
        }

        if (medias != null && medias.size() > 0) {
            for (Media m : medias) {
                sb.append(m.getContent());
            }
        }

/*        if (sb.substring(sb.length() - 2, sb.length()).equals("\r\n")) {
            sb.setLength(sb.length() - 2); //removing last CRLF
        }*/
        String result = sb.toString();
        return result;
    }

    public boolean typeSupported(final SDPType type) {
        boolean isSupported = false;
        for (Media media : getMedias()) {
            if (type == getSDPTypeByMedia(media)) {
                isSupported = true;
                break;
            }
        }
        return isSupported;
    }

    private static SDPType getSDPTypeByMedia(final Media media) {
        SDPType retValue = SDPType.UNKNOWN;

        for (SDPType sdpType : SDPType.values()) {
            Set<String> types = sdpTypeMapping.get(sdpType);
            if (types != null && types.contains(media.getType())) {
                retValue = sdpType;
                break;
            }
        }
        return retValue;
    }
}
