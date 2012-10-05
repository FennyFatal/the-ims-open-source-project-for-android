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

package javax.microedition.ims.presence;

import com.android.ims.presence.AttachHelper;
import com.android.ims.presence.Attachable;
import com.android.ims.util.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The DeviceInfo component is used to describe the physical piece of equipment
 * in which services execute.
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 * 
 * @author Andrei Khomushko
 * 
 */
public class DeviceInfo {
    private final String identifier;
    private String deviceId;
    private String freeText;

    private GeographicalLocationInfo geographicalLocationInfo;
    private final Map<String, PresenceState> networks = new HashMap<String, PresenceState>();
    
    private final Attachable attachable = new AttachHelper();

    public DeviceInfo() {
        //TODO
        //this(Utils.generateUUID(), "Mobile", null);
        this(Utils.generatePseudoUUID(), Utils.generatePseudoUUID(), null, null);
    }

    private DeviceInfo(String identifier, String deviceId, 
            String freeText, GeographicalLocationInfo geographicalLocationInfo) {
        if(identifier == null || "".equals(identifier.trim())) {
            throw new IllegalArgumentException("The id argument is null");
        } 
        this.identifier = identifier;
        this.deviceId = deviceId;
        this.freeText = freeText;
        this.geographicalLocationInfo = geographicalLocationInfo;
    }
    
    private DeviceInfo(DeviceInfoBuilder builder) {
        this(builder.id, builder.deveiceId, builder.freeText, builder.geographicalLocationInfo); 
    }

    /**
     * Adds a network to the list of available networks for this DeviceInfo. If
     * this DeviceInfo already contains a network with the same networkId, the
     * state for that network will be updated.
     * 
     * @param networkId
     * @param state
     * 
     * @throws IllegalArgumentException
     *             - if the networkId argument is null or an empty string
     * @throws IllegalArgumentException
     *             - if the state argument is null or PresenceState.UNSET
     */
    public void addNetworkAvailability(String networkId, PresenceState state) {
        if (networkId == null || "".equals(networkId.trim())) {
            throw new IllegalArgumentException(
                    "The networkId argument is null or an empty string");
        }

        if (state == null) {
            throw new IllegalArgumentException("the state argument is null");
        } else if (state == PresenceState.UNSET) {
            throw new IllegalArgumentException(
                    "the state argument is PresenceState.UNSET");
        }

        networks.put(networkId, state);
    }

    /**
     * Returns the identifiers of the available networks for the device.
     * 
     * @return the network identifiers or an empty array if no networks are
     *         available
     */
    public String[] getAvailableNetworkIds() {
        return networks.keySet().toArray(new String[0]);
    }

    /**
     * Returns the state of the network with the given network identifier.
     * 
     * @param networkId
     *            - the identifier of the network
     * @return STATE_OPEN or STATE_CLOSED
     * @throws IllegalArgumentException
     *             - if a network identified by the networkId argument does not
     *             exist in this DeviceInfo
     */
    public PresenceState getNetworkState(String networkId) {
        if (networkId == null) {
            throw new IllegalArgumentException("the networkId argument is null");
        }

        if (!networks.containsKey(networkId)) {
            throw new IllegalArgumentException(
                    "a network identified by the networkId argument does not exist in this DeviceInfo");
        }

        return networks.get(networkId);
    }

    /**
     * Removes the network with the given network identifier from the list of
     * available networks for this DeviceInfo.
     * 
     * @param networkId
     *            - the network identifier
     * @throws IllegalArgumentException
     *             - if a network identified by the networkId argument does not
     *             exist in this DeviceInfo
     */
    public void removeNetworkAvailability(String networkId) {
        if (networkId == null) {
            throw new IllegalArgumentException("the networkId argument is null");
        }

        if (!networks.containsKey(networkId)) {
            throw new IllegalArgumentException(
                    "a network identified by the networkId argument does not exist in this DeviceInfo");
        }

        networks.remove(networkId);
    }

    /**
     * Returns a GeographicalLocationInfo for the geographical location of this
     * device. Changes made to the GeographicalLocationInfo object will not take
     * effect on the DeviceInfo until
     * setGeographicalLocationInfo(GeographicalLocationInfo) is called.
     * 
     * @return the geographical location or null if the geographical location is
     *         not available
     */
    public GeographicalLocationInfo getGeographicalLocationInfo() {
        return geographicalLocationInfo;
    }

    /**
     * Sets a GeographicalLocationInfo for this DeviceInfo. This will replace
     * any existing GeographicalLocationInfo. A null value removes any existing
     * value.
     * 
     * @param geographicalLocationInfo
     *            - the GeographicalLocationInfo to set or null
     */
    public void setGeographicalLocationInfo(
            GeographicalLocationInfo newGeographicalLocationInfo) {
        if (geographicalLocationInfo != null) {
            geographicalLocationInfo.setParent(null);
        }
        
        if(newGeographicalLocationInfo != null) {
            newGeographicalLocationInfo.setParent(attachable);
        }
        this.geographicalLocationInfo = newGeographicalLocationInfo;
    }

    /**
     * Returns the time when this DeviceInfo was added to the PresenceDocument .
     * 
     * @return the Date or null if this DeviceInfo has not been added to a
     *         PresenceDocument
     */
    public Date getTimestamp() {
        return attachable.getTimestamp();
    }

    /**
     * Returns a unique identifier for the device that this application is
     * running on.
     * 
     * @return the device identifier
     * @see ServiceInfo.setDeviceId(String)
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Returns the identifier for this DeviceInfo.
     * 
     * @return the DeviceInfo identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    public Attachable getAttachable() {
        return attachable;
    }
    
    public String getFreeText() {
        return freeText;
    }
    
    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }
    
    public static class DeviceInfoBuilder {
        private String id;
        private String deveiceId;
        private String freeText;
        private GeographicalLocationInfo geographicalLocationInfo;
        
        public DeviceInfoBuilder buildId(String id) {
            this.id = id;
            return this;
        }
        
        public DeviceInfoBuilder buildDeveiceId(String deveiceId) {
            this.deveiceId = deveiceId;
            return this;
        }
        
        public DeviceInfoBuilder buildFreeText(String freeText) {
            this.freeText = freeText;
            return this;
        }
        
        public DeviceInfoBuilder buildGeographicalLocationInfo(GeographicalLocationInfo locationInfo) {
            this.geographicalLocationInfo = locationInfo;
            return this;
        }
        
        /**
         * Build DeviceInfo instance. 
         * @return DeviceInfo instance
         * @throws IllegalArgumentException - if instance can't be created
         */
        public DeviceInfo build() {
            return new DeviceInfo(this);
        }
    }
    
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((deviceId == null) ? 0 : deviceId.hashCode());
        result = prime * result
                + ((freeText == null) ? 0 : freeText.hashCode());
        result = prime
                * result
                + ((geographicalLocationInfo == null) ? 0
                        : geographicalLocationInfo.hashCode());
        result = prime * result
                + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result
                + ((networks == null) ? 0 : networks.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeviceInfo other = (DeviceInfo) obj;
        if (deviceId == null) {
            if (other.deviceId != null)
                return false;
        } else if (!deviceId.equals(other.deviceId))
            return false;
        if (freeText == null) {
            if (other.freeText != null)
                return false;
        } else if (!freeText.equals(other.freeText))
            return false;
        if (geographicalLocationInfo == null) {
            if (other.geographicalLocationInfo != null)
                return false;
        } else if (!geographicalLocationInfo
                .equals(other.geographicalLocationInfo))
            return false;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (networks == null) {
            if (other.networks != null)
                return false;
        } else if (!networks.equals(other.networks))
            return false;
        return true;
    }

    
    public String toString() {
        return "DeviceInfo [identifier=" + identifier + ", deviceId="
                + deviceId + ", freeText=" + freeText
                + ", geographicalLocationInfo=" + geographicalLocationInfo
                + ", networks=" + networks + ", attachable=" + attachable + "]";
    }
}
