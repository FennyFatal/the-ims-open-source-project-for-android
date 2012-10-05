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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.StringUtils;
import java.util.*;
import java.util.Map.Entry;

public class Media {
    public static final String CRYPTO = "crypto";
    public static final String INLINE = "inline:";

    private String type;
    private int port;
    private int numberOfPorts;
    private String protocol;
    private List<String> formats;
    private boolean securedMedia;    
    private List<CryptoParam> cryptoParams = new ArrayList<CryptoParam>();

    private String information;
    private ConnectionInfo connectionInfo;
    private Bandwidth bandwidth;
    private EncryptionKey encryptionKeys;
    private List<Attribute> attributes;

    public Media() {
        this.attributes = new ArrayList<Attribute>();
        this.formats = new ArrayList<String>();
    }


    private Media(MediaBuilder builder) {
        Logger.log("constructor", "Media");

        this.type = builder.type;
        this.port = builder.port;
        this.connectionInfo = new ConnectionInfo(builder.address, builder.isOwnAddress);
        this.numberOfPorts = builder.numberOfPorts;
        this.protocol = builder.protocol;
        this.formats = builder.formats;
        this.information = builder.information;
        this.encryptionKeys = new EncryptionKey(builder.encryptionKeys);
        this.attributes = (builder.attributes != null? builder.attributes: new ArrayList<Attribute>());    
        this.bandwidth = builder.bandwidth;
        this.cryptoParams.addAll(builder.cryptoParams);
        
        if(builder.direction != null) {
            attributes.add(new Attribute(builder.direction, null));    
        }

        Logger.log("created", "Media");
    }
    
    public boolean isSecuredMedia() {
        return securedMedia;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getNumberOfPorts() {
        return numberOfPorts;
    }

    public void setNumberOfPorts(int numberOfPorts) {
        this.numberOfPorts = numberOfPorts;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public Bandwidth getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Bandwidth bandwidth) {
        this.bandwidth = bandwidth;
    }

    public EncryptionKey getEncryptionKeys() {
        return encryptionKeys;
    }

    public List<CryptoParam> getCryptoParams() {
        return cryptoParams;
    }

    public void setEncryptionKeys(EncryptionKey encryptionKeys) {
        this.encryptionKeys = encryptionKeys;
    }

    public Iterator<Attribute> getAttributes() {
        return attributes.iterator();
    }

    public boolean addCryptoParam(CryptoParam crypto) {
        return cryptoParams.add(crypto);
    }
    
    public boolean addAttribute(Attribute a) {
    	boolean add = true;
    	if(a.getName() != null && a.getName().startsWith(CRYPTO) && a.getValue() != null){    		
    		String[] items = a.getValue().trim().split(" ");
    		if(items.length >2 && items[2].startsWith(INLINE)){
    			try{
    				addCryptoParam(new CryptoParam(Integer.valueOf(items[0]), CryptoSuit.valueOf(items[1]), items[2].substring(INLINE.length())));
    				securedMedia = true;
    				add = false;
    			} catch (NumberFormatException e) {
					Logger.log("Errot in parsing crypto attribute tag");
				} catch (Exception e) {
					Logger.log("Errot in crypto suit type or  tag");
				}
    		}
    	}
        return add? attributes.add(a) : add;
    }

    public DirectionsType getDirection(SdpMessage sdp) {
        DirectionsType direction = DirectionsType.DirectionNotSet;
        // use session layer direction as default value
        for (Attribute att : sdp.getAttributes()) {
            if (att.getName().equals(DirectionsType.DirectionInactive.getValue())) {
                direction = DirectionsType.DirectionInactive;
            }
            else if (att.getName().equals(DirectionsType.DirectionSendReceive.getValue())) {
                direction = DirectionsType.DirectionSendReceive;
            }
            else if (att.getName().equals(DirectionsType.DirectionSendOnly.getValue())) {
                direction = DirectionsType.DirectionSendOnly;
            }
            else if (att.getName().equals(DirectionsType.DirectionReceiveOnly.getValue())) {
                direction = DirectionsType.DirectionReceiveOnly;
            }
        }
        for (Attribute att : attributes) {
            if (att.getName().equals(DirectionsType.DirectionInactive.getValue())) {
                direction = DirectionsType.DirectionInactive;
            }
            else if (att.getName().equals(DirectionsType.DirectionSendReceive.getValue())) {
                direction = DirectionsType.DirectionSendReceive;
            }
            else if (att.getName().equals(DirectionsType.DirectionSendOnly.getValue())) {
                direction = DirectionsType.DirectionSendOnly;
            }
            else if (att.getName().equals(DirectionsType.DirectionReceiveOnly.getValue())) {
                direction = DirectionsType.DirectionReceiveOnly;
            }
        }
        return direction;
    }

    public void setDirection(DirectionsType direction) {
        // remove existing
        for (Attribute att : attributes) {
            if (att.getName().equals("sendrecv") || att.getName().equals("sendonly") ||
                    att.getName().equals("recvonly") || att.getName().equals("inactive")) {
                attributes.remove(att);
            }
        }
        if (direction != DirectionsType.DirectionNotSet) {
            Attribute newAtt = new Attribute();
            newAtt.setName(direction.getValue());
            attributes.add(newAtt);
        }
    }


    public String getContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("m=").append(type).append(" ").append(port);
        if (numberOfPorts > 0) {
            sb.append("/").append(numberOfPorts);
        }
        sb.append(" ").append(protocol);
        if (formats != null) {
            for (String f : formats) {
                sb.append(" ").append(f);
            }
        }
        sb.append(StringUtils.SIP_TERMINATOR);
        if (information != null && information.length() > 0) {
            sb.append("i=").append(information).append(StringUtils.SIP_TERMINATOR);
        }
        if (connectionInfo != null 
                && connectionInfo.getAddress() != null 
                && connectionInfo.isOwnAddress()) {
            sb.append("c=").append(connectionInfo.getContent()).append(StringUtils.SIP_TERMINATOR);
        }
        if (bandwidth != null) {
            sb.append("b=").append(bandwidth.getContent()).append(StringUtils.SIP_TERMINATOR);
        }
        if (encryptionKeys != null && encryptionKeys.getValue() != null) {
            sb.append("k=").append(encryptionKeys.getValue()).append(StringUtils.SIP_TERMINATOR);
        }
        for (CryptoParam cp : cryptoParams) {
            sb.append("a=").append(cp.getContent()).append(StringUtils.SIP_TERMINATOR);
        }
        Logger.log("attributes: " + attributes);
        if (attributes != null) {
            for (Attribute a : attributes) {
                sb.append("a=").append(a.getContent()).append(StringUtils.SIP_TERMINATOR);
            }
        }

        return sb.toString();
    }

    public static class MediaBuilder {
        private String type; 
        private String address;
        private boolean isOwnAddress;
        private int port;
        private int numberOfPorts;
        private String protocol, direction;

        private String information;
        //private ConnectionInfo  connectionInfo;
        private Bandwidth bandwidth;
        private String encryptionKeys;
        private final List<Attribute> attributes = new ArrayList<Attribute>();
        private final List<String> formats = new ArrayList<String>();
        private final List<CryptoParam> cryptoParams = new ArrayList<CryptoParam>();

        public MediaBuilder type(String type) {
            this.type = type;
            return this;
        }

        public MediaBuilder address(String address, boolean isOwnAddress) {
            this.address = address;
            this.isOwnAddress = isOwnAddress;
            return this;
        }

        public MediaBuilder port(int port) {
            this.port = port;
            return this;
        }

        public MediaBuilder numberOfPorts(int numberOfPorts) {
            this.numberOfPorts = numberOfPorts;
            return this;
        }

        public MediaBuilder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public MediaBuilder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public MediaBuilder formats(List<String> formats) {
            this.formats.addAll(formats);
            return this;
        }

        public MediaBuilder information(String information) {
            this.information = information;
            return this;
        }

        public MediaBuilder encryptionKeys(String encryptionKeys) {
            this.encryptionKeys = encryptionKeys;
            return this;
        }

        public MediaBuilder attributes(Map<String, String> attributesToAdd) {
            for (Entry<String, String> e : attributesToAdd.entrySet()) {
                attributes.add(new Attribute(e.getKey(), e.getValue()));
            }
            return this;
        }

        public MediaBuilder attributes(List<Attribute> attributes) {
            if (attributes != null) {
                this.attributes.addAll(attributes);
            }
            return this;
        }
        
        public MediaBuilder cryptoParams(CryptoParam... cryptoParams) {
        	this.cryptoParams.addAll(Arrays.asList(cryptoParams));
        	return this;
        }

        public MediaBuilder bandwidths(List<Bandwidth> bandwidths) {
            this.bandwidth = (bandwidths != null && bandwidths.size() > 0 ? bandwidths.get(0) : null);
            return this;
        }

        public Media build() {
            return new Media(this);
        }
    }
}
