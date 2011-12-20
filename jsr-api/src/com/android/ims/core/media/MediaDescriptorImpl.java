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

package com.android.ims.core.media;

import android.util.Log;
import com.android.ims.util.NumberUtils;

import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.core.media.MediaDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base implementation for {@link MediaDescriptor}
 *
 * @author ext-akhomush
 */
public class MediaDescriptorImpl implements MediaDescriptor {

    private final static String TAG = "MediaDescriptorImpl";

    private static final List<String> DISABLED_ATTRIBUTES = Arrays.asList(new String[]{"sendonly", "recvonly", "sendrecv"});

    private final List<String> attributes = Collections.synchronizedList(new ArrayList<String>());
    private final List<String> bandwidthInfo = Collections.synchronizedList(new ArrayList<String>());

    private String transport;
    private String mediaFormat;

    private Media media;
    private int direction;
    private String title;
    private String mediaType;
    private int port;
    private String connectionAddress;
    private final boolean isOwnAddress;
    private List<CryptoParam> cryptoParams = new ArrayList<CryptoParam>();

/*    public MediaDescriptorImpl(final Media media, int port, int direction,
            final String transport, final String mediaFormat) {
        this.media = media;
        this.direction = direction;
        this.port = port;
        this.transport = transport;
        this.mediaFormat = mediaFormat;
    }
*/

    private MediaDescriptorImpl(final Builder builder) {
        this.attributes.addAll(builder.attributes);
        this.bandwidthInfo.addAll(builder.bandwidthInfo);
        this.transport = builder.transport;
        this.mediaFormat = builder.mediaFormat;

        this.media = builder.media;
        this.direction = builder.direction;
        this.title = builder.title;
        this.mediaType = builder.mediaType;
        this.port = builder.port;
        this.connectionAddress = builder.connectionAddress;
        this.isOwnAddress = builder.isOwnAddress;
        
        if(builder.cryptoParams != null) {
        	cryptoParams.addAll(Arrays.asList(builder.cryptoParams));	
        }
    }

    public static class Builder {
        private final List<String> attributes = Collections.synchronizedList(new ArrayList<String>());
        private final List<String> bandwidthInfo = Collections.synchronizedList(new ArrayList<String>());

        private String transport;
        private String mediaFormat;

        private Media media;
        private int direction;
        private String title;
        private String mediaType;
        private int port;
        private String connectionAddress;
        private boolean isOwnAddress;
        private CryptoParam[] cryptoParams;

        public Builder attributes(final List<String> attributes) {
            if (attributes != null) {
                this.attributes.addAll(attributes);
            }
            return this;
        }

        public Builder bandwidthInfo(final List<String> bandwidthInfo) {
            if (bandwidthInfo != null) {
                this.bandwidthInfo.addAll(bandwidthInfo);
            }
            return this;
        }

        public Builder transport(final String transport) {
            this.transport = transport;
            return this;
        }

        public Builder mediaFormat(final String mediaFormat) {
            this.mediaFormat = mediaFormat;
            return this;
        }

        public Builder media(final Media media) {
            this.media = media;
            return this;
        }
        
        public Builder cryptoParams(final CryptoParam... cryptoParams) {
            this.cryptoParams = cryptoParams;
            return this;
        }

        public Builder direction(int direction) {
            this.direction = direction;
            return this;
        }

        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder mediaType(final String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        public Builder connectionAddress(final String connectionAddress) {
            this.connectionAddress = connectionAddress;
            return this;
        }
        
        public Builder isOwnAddress(final boolean isOwnAddress) {
            this.isOwnAddress = isOwnAddress;
            return this;
        }
        
        public MediaDescriptorImpl build() {
            return new MediaDescriptorImpl(this);
        }
    }

    /**
     * @see MediaDescriptor#addAttribute(String)
     */
    public void addAttribute(final String attribute) {
        if (!isAttributeValid(attribute)) {
            throw new IllegalArgumentException(String.format("The attribute '%s' argument is invalid.", attribute));
        }

        if (!isMediaStateValid(media.getState())) {
            throw new IllegalStateException("The Media is not in valid state, state: " + media.getState());
        }

        if (!isAttributeAllowed(attribute)) {
            throw new IllegalArgumentException(String.format("The attribute '%s' cann't be added. Reserved by IMS engine.", attribute));
        }

        Log.d(TAG, "*** addAttribute() media.getState() = " + media.getState());

        if (media.getState() == Media.STATE_ACTIVE) {
            ((MediaImpl) media).addAttributeToActiveMedia(this, attribute);
        } else {
            addAttributeInternal(attribute);
        }
    }

    /**
     * Adds an attribute without checking the media state.
     * <p/>
     * See {@link #addAttribute(String)}.
     *
     * @param attribute the attribute
     * @throws IllegalArgumentException - Attribute is not valid
     *                                  IllegalArgumentException - Attribute already exists
     */
    public void addAttributeInternal(String attribute) {
        if (!isAttributeValid(attribute)) {
            throw new IllegalArgumentException("Attribute is not valid, attribute: " + attribute);
        }
        if (attributes.contains(attribute)) {
            throw new IllegalArgumentException(attribute + ", already exists");
        }

        attributes.add(attribute);
    }

    /**
     * Check syntax of attribute:
     * a=<attribute>
     * a=<attribute>:<value>
     */
    private static boolean isAttributeValid(final String attribute) {
        if (attribute == null || attribute.trim().equals("")) {
            return false;
        }
        return true;

    }


    private static boolean isMediaStateValid(final int state) {
        return state == Media.STATE_ACTIVE || state == Media.STATE_INACTIVE;
    }

    private static boolean isAttributeAllowed(final String attribute) {
        String attributeName = resolveAttributeName(attribute);
        return !DISABLED_ATTRIBUTES.contains(attributeName);
    }

    /**
     * Get a attribute name by attribute
     * <p/>
     * Formats:
     * <attr_name>:<attr_value>
     * <attr_name>
     *
     * @param attribute - attribute
     * @return attribute name
     */
    private static String resolveAttributeName(final String attribute) {
        String[] exprs = attribute.split(":");
        return exprs.length > 0 ? exprs[0] : attribute;
    }

    /**
     * @see MediaDescriptor#getAttributes()
     */
    
    public String[] getAttributes() {
        return attributes.toArray(new String[0]);
    }

    /**
     * @see MediaDescriptor#getBandwidthInfo()
     */
    
    public String[] getBandwidthInfo() {
        return bandwidthInfo.toArray(new String[0]);
    }

    /**
     * @see MediaDescriptor#getMediaDescription()
     */
    
    public String getMediaDescription() {
        if (media.getState() == Media.STATE_INACTIVE) {
            throw new IllegalStateException("The Media is in STATE_INACTIVE, state: " + media.getState());
        }
        return String.format("%s %s %s %s", mediaType, port, transport, mediaFormat);
    }

    /**
     * @see MediaDescriptor#removeAttribute(String)
     */
    
    public void removeAttribute(final String attribute) {
        if (!isAttributeValid(attribute)) {
            throw new IllegalArgumentException("The attribute argument is invalid, attribute: " + attribute);
        }

        if (!isMediaStateValid(media.getState())) {
            throw new IllegalStateException("The Media is not in valid state, state: " + media.getState());
        }

        if (!isAttributeAllowed(attribute)) {
            throw new IllegalArgumentException(String.format("Attribute '%s' cann't be removed. Reserved by IMS engine.", attribute));
        }

        if (media.getState() == Media.STATE_ACTIVE) {
            ((MediaImpl) media).removeAttributeFromActiveMedia(this, attribute);
        } else {
            if (!containsAttribute(attribute)) {
                throw new IllegalArgumentException(attribute + "does not exist");
            }
            removeAttributeInternal(attribute);
        }
    }

    /**
     * Removes an attribute without checking the media state.
     * If the attribute is not set, nothing will be done.
     *
     * @param attribute - the attribute to delete
     */
    public void removeAttributeInternal(String attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("null is not a valid attribute");
        }
        attributes.remove(attribute);
    }


    /**
     * @see MediaDescriptor#setBandwidthInfo(String[])
     */
    
    public void setBandwidthInfo(final String[] info) {
        if (!isMediaStateValid(media.getState())) {
            throw new IllegalStateException("the Media is not in valid state");
        }

        if (!isBandwidthInfoValid(info)) {
            throw new IllegalArgumentException("Info argument is null or if the syntax is invalid");
        }

        bandwidthInfo.clear();
        for (String inf : info) {
            bandwidthInfo.add(inf);
        }
    }

    /**
     * Check syntax of BandwidthInfo:
     * <modifier>:<bandwidth-value>
     * <modifier>        - is a single alphanumeric word giving the meaning of the bandwidth figure.
     * <bandwidth-value> - is in kilobits per second
     */
    private static boolean isBandwidthInfoValid(final String[] infos) {
        if (infos == null) {
            return false;
        }

        boolean isValid = true;

        for (String info : infos) {
            String[] expr = info.split(":");
            if (expr.length != 2) {
                isValid = false;
                break;
            } else {
                if (!NumberUtils.checkIfLong(expr[1])) {
                    isValid = false;
                    break;
                }
            }
        }

        return isValid;
    }

    /**
     * @see MediaDescriptor#setMediaTitle(String)
     */
    
    public void setMediaTitle(final String title) {
        if (title == null) {
            throw new IllegalArgumentException("The title argument is null");
        }

        if (media.getState() != Media.STATE_INACTIVE) {
            throw new IllegalStateException("The Media is not in STATE_INACTIVE, media state: " + media.getState());
        }

        this.title = title;
    }

    /**
     * @see MediaDescriptor#getMediaTitle()
     */
    
    public String getMediaTitle() {
        return title;
    }

    /**
     * Returns the direction of the media.
     *
     * @return direction of the media
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Set direction for the media
     *
     * @param direction - media direction
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Attach media for media description.
     *
     * @param media - attached media
     */
    public void setMedia(final Media media) {
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }

    /**
     * Returns the media port.
     * <p/>
     * For a media with description <code>m=video 42170 RTP/AVP 31</code>,
     * the media port is "42170".
     *
     * @return the media port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the media port.
     * <p/>
     * For a media with description <code>m=video 42170 RTP/AVP 31</code>,
     * the media port is "42170".
     *
     * @param port the media port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the media type.
     * <p/>
     * For a media with description <code>m=video 46120/2 RTP/AVP 31</code>,
     * the media type is "video".
     *
     * @return the media type
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Sets the media type.
     * <p/>
     * For a media with description <code>m=video 46120/2 RTP/AVP 31</code>,
     * the media type is "video".
     *
     * @param mediaType the media type.
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Returns the media format description.
     * <p/>
     * For a media with description <code>m=video 49170/2 RTP/AVP 31</code>,
     * the format description is "31".
     *
     * @return the media format description
     */
    public String getMediaFormat() {
        return mediaFormat;
    }

    /**
     * Sets the media format description.
     * <p/>
     * For a media with description <code>m=video 49170/2 RTP/AVP 31</code>,
     * the format description is "31".
     *
     * @param format the media format description
     */
    public void setMediaFormat(String mediaFormat) {
        this.mediaFormat = mediaFormat;
    }

    /**
     * Returns the media transport protocol.
     * <p/>
     * For a media with description <code>m=video 41200/2 RTP/AVP 31</code>,
     * the transport protocol is "RTP/AVP".
     *
     * @return the media transport protocol
     */
    public String getTransport() {
        return transport;
    }

    /**
     * Sets the media transport protocol.
     * <p/>
     * For a media with description <code>m=video 41200/2 RTP/AVP 31</code>,
     * the transport protocol is "RTP/AVP".
     *
     * @param transport the media transport protocol
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }

    /**
     * Returns the connection address of the connection information for the
     * media.
     * <p/>
     * This is the value of the third part, &lt;connection-address&gt;, of the
     * "c=" SDP field of the media (see RFC 4566).
     *
     * @return The address of the connection information, or <code>null</code>
     *         if the descriptor does not specify a connection address.
     */
    public String getConnectionAddress() {
        return connectionAddress;
    }
    
    public boolean isOwnAddress() {
        return isOwnAddress;
    }
    
    public CryptoParam[] getCryptoParams() {
        return cryptoParams.toArray(new CryptoParam[0]);
    }

    /**
     * Sets the connection address of the media.
     *
     * @param address the connection address
     */
    public void setConnectionAddress(String connectionAddress) {
        this.connectionAddress = connectionAddress;
    }

    /**
     * Creates a copy of this.
     *
     * @return a copy
     */
    public MediaDescriptorImpl createCopy() {
        MediaDescriptorImpl copy = new Builder()
                .media(media)
                .port(port)
                .direction(direction)
                .transport(transport)
                .mediaFormat(mediaFormat)
                .mediaType(mediaType)
                .connectionAddress(connectionAddress)
                .isOwnAddress(isOwnAddress)
                .title(title)
                .bandwidthInfo(bandwidthInfo)
                .attributes(attributes)
                .cryptoParams(cryptoParams.toArray(new CryptoParam[0]))
                .build();
        return copy;
    }

    /**
     * Returns <code>true</code> if a particular attribute is set.
     *
     * @param attribute the attribute
     * @return <code>true</code> if the attribute is set
     */
    boolean containsAttribute(String attribute) {
        return attributes.contains(attribute);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result
				+ ((bandwidthInfo == null) ? 0 : bandwidthInfo.hashCode());
		result = prime
				* result
				+ ((connectionAddress == null) ? 0 : connectionAddress
						.hashCode());
		result = prime * result
				+ ((cryptoParams == null) ? 0 : cryptoParams.hashCode());
		result = prime * result + direction;
		result = prime * result
				+ ((mediaFormat == null) ? 0 : mediaFormat.hashCode());
		result = prime * result
				+ ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result + port;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result
				+ ((transport == null) ? 0 : transport.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaDescriptorImpl other = (MediaDescriptorImpl) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (bandwidthInfo == null) {
			if (other.bandwidthInfo != null)
				return false;
		} else if (!bandwidthInfo.equals(other.bandwidthInfo))
			return false;
		if (connectionAddress == null) {
			if (other.connectionAddress != null)
				return false;
		} else if (!connectionAddress.equals(other.connectionAddress))
			return false;
		if (cryptoParams == null) {
			if (other.cryptoParams != null)
				return false;
		} else if (!cryptoParams.equals(other.cryptoParams))
			return false;
		if (direction != other.direction)
			return false;
		if (mediaFormat == null) {
			if (other.mediaFormat != null)
				return false;
		} else if (!mediaFormat.equals(other.mediaFormat))
			return false;
		if (mediaType == null) {
			if (other.mediaType != null)
				return false;
		} else if (!mediaType.equals(other.mediaType))
			return false;
		if (port != other.port)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (transport == null) {
			if (other.transport != null)
				return false;
		} else if (!transport.equals(other.transport))
			return false;
		return true;
	}
	
	public void addCryptoParam(CryptoParam... cryptoParams) {
		this.cryptoParams.addAll(Arrays.asList(cryptoParams));
	}
	
	public void removeCryptoParam(CryptoParam cryptoParam) {
		cryptoParams.remove(cryptoParam);
	}
	
	protected boolean isSecured() {
		return cryptoParams.size() > 0;
	}

    @Override
    public String toString() {
        return "MediaDescriptorImpl [attributes=" + attributes + ", bandwidthInfo=" + bandwidthInfo
                + ", transport=" + transport + ", mediaFormat=" + mediaFormat + ", direction="
                + direction + ", title=" + title + ", mediaType=" + mediaType + ", port=" + port
                + ", connectionAddress=" + connectionAddress + ", isOwnAddress=" + isOwnAddress
                + ", cryptoParams=" + cryptoParams + "]";
    }
}
