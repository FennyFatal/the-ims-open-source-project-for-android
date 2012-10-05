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

/**
 * 
 * The ServiceInfo component models the forms of communication that the
 * presentity potentially has access to.
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 * @author Andrei Khomushko
 * 
 */
public class ServiceInfo {
    // <tuple id="">
    private final String identifier;

    /**
     * <service-description> <service-id/> <version/> <description/>
     * </service-description>
     */
    private final String serviceId;
    private final String serviceVersion;
    private final String serviceDescription;

    // <willingness>
    private PresenceState willingnes = PresenceState.UNSET;

    // <status><basic>open/closed<basic><status>
    private PresenceState status = PresenceState.UNSET;

    private PresenceState barringState = PresenceState.UNSET;
    private PresenceState registrationState = PresenceState.UNSET;
    private PresenceState sessionParticipation = PresenceState.UNSET;

    private String contact;

    private StatusIcon statusIcon;

    // <class xmlns="urn:ietf:params:xml:ns:pidf:rpid">buddies</class>
    private String classification;

    // <deviceID
    // xmlns="urn:ietf:params:xml:ns:pidf:data-model">Mobile</deviceID>
    private String deviceId;

    // <timestamp>2010-03-17T16:06:08.4531991Z</timestamp>
    private final Attachable attachable = new AttachHelper();

    // TODO jsr extention
    // <note>
    private String freeText;

    /**
     * Constructor for a new ServiceInfo. This will create a ServiceInfo with
     * the given identity, version, and a general description.
     * 
     * An identifier will be added to the ServiceInfo when it is created.
     * 
     * @param serviceId
     *            - the service identity
     * @param serviceVersion
     *            - the service version
     * @param serviceDescription
     *            - the service description
     * 
     * @throws IllegalArgumentException
     *             - if any of the arguments are null
     */
    public ServiceInfo(String serviceId, String serviceVersion,
            String serviceDescription) {
        this(Utils.generatePseudoUUID(), serviceId, serviceVersion,
                serviceDescription, true);
    }

    private ServiceInfo(String identifier, String serviceId,
            String serviceVersion, String serviceDescription, boolean needValidate) {
        this(identifier, serviceId, serviceVersion, serviceDescription, null,
                null, null, PresenceState.UNSET, needValidate);
    }

    private ServiceInfo(String identifier, String serviceId,
            String serviceVersion, String serviceDescription, String contact,
            String deviceId, String freeText, PresenceState status, boolean needValidate) {

        if (identifier == null || "".equals(identifier.trim())) {
            throw new IllegalArgumentException(
                    "The identifier argument is null");
        }

        if (needValidate && serviceId == null) {
            throw new IllegalArgumentException("The serviceId argument is null");
        }

        if (needValidate && serviceVersion == null) {
            throw new IllegalArgumentException(
                    "The serviceVersion argument is null");
        }

        if (needValidate && serviceDescription == null) {
            throw new IllegalArgumentException(
                    "The serviceDescription argument is null");
        }

        this.identifier = identifier;
        this.serviceId = serviceId;
        this.serviceVersion = serviceVersion;
        this.serviceDescription = serviceDescription;

        this.contact = contact;
        this.deviceId = deviceId;
        this.freeText = freeText;
        this.status = status;
    }

    private ServiceInfo(ServiceInfoBuilder builder) {
        this(builder.id, builder.serviceId, builder.serviceVersion,
                builder.serviceDescription, builder.contact, builder.deviceId,
                builder.freeText, builder.status, false);
    }

    /**
     * Returns the identifier for this ServiceInfo.
     * 
     * @return the ServiceInfo identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the state of the service specific willingness.
     * 
     * Note: If the overriding willingness in PersonInfo is set to either
     * PresenceState.OPEN or PresenceState.CLOSED, the application should use
     * PersonInfo.getOverridingWillingness instead.
     * 
     * @return PresenceState.OPEN, PresenceState.CLOSED, or PresenceState.UNSET
     */
    public PresenceState getWillingnes() {
        // TODO
        return willingnes;
    }

    /**
     * Sets the service specific willingness for this ServiceInfo. Setting the
     * state parameter to PresenceState.OPEN indicates that the user is willing
     * to communicate and to receive incoming communication requests with the
     * specified service. PresenceState.CLOSED indicates that the user does not
     * want to communicate with the specified service.
     * 
     * The willingness can be removed by setting the state parameter to
     * PresenceState.UNSET. PresenceState.UNSET means that it is not known
     * whether the user wants to communicate with the service.
     * 
     * @param state
     *            - PresenceState.OPEN, PresenceState.CLOSED, or
     *            PresenceState.UNSET
     * 
     * @throws IllegalArgumentException
     *             - if the state argument is null
     */
    public void setWillingnes(PresenceState state) {
        if (willingnes == null) {
            throw new IllegalArgumentException(
                    "The willingnes argument is null");
        }

        this.willingnes = state;
    }

    /**
     * Returns the identity of this ServiceInfo.
     * 
     * @return the service identity or null if the identity is not available
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Returns the version of this ServiceInfo.
     * 
     * @return the version or null if the version is not available
     */
    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Returns the description of this ServiceInfo.
     * 
     * @return the description or null if the description is not available
     */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /**
     * Returns the status of this ServiceInfo .
     * 
     * @return PresenceState.OPEN, PresenceState.CLOSED, or PresenceState.UNSET
     */
    public PresenceState getStatus() {
        return status;
    }

    /**
     * Sets the status of this ServiceInfo .
     * 
     * @param status
     *            - PresenceState.OPEN, PresenceState.CLOSED, or
     *            PresenceState.UNSET
     * @throws IllegalArgumentException
     *             - if the state argument is null
     */
    public void setStatus(PresenceState status) {
        if (status == null) {
            throw new IllegalArgumentException("The status argument is null");
        }

        this.status = status;
    }

    /**
     * Returns the barring state of this ServiceInfo.
     * 
     * @return PresenceState.OPEN, PresenceState.CLOSED, or PresenceState.UNSET
     */
    public PresenceState getBarringState() {
        return barringState;
    }

    /**
     * Sets the barring state of this ServiceInfo.
     * 
     * @param state
     *            - PresenceState.OPEN, PresenceState.CLOSED, or
     *            PresenceState.UNSET
     * @throws IllegalArgumentException
     *             - if the state argument is null
     */
    public void setBarringState(PresenceState state) {
        if (state == null) {
            throw new IllegalArgumentException("The state argument is null");
        }

        this.barringState = state;
    }

    /**
     * Returns the registration state of this ServiceInfo.
     * 
     * @return PresenceState.OPEN, PresenceState.CLOSED, or PresenceState.UNSET
     */
    public PresenceState getRegistrationState() {
        return registrationState;
    }

    /**
     * Sets the registration state of this ServiceInfo.
     * 
     * @param state
     *            - PresenceState.OPEN, PresenceState.CLOSED, or
     *            PresenceState.UNSET
     * @throws IllegalArgumentException
     *             - if the state argument is null
     */
    public void setRegistrationState(PresenceState state) {
        if (state == null) {
            throw new IllegalArgumentException("The state argument is null");
        }

        this.registrationState = state;
    }

    /**
     * Returns the contact URI of this ServiceInfo.
     * 
     * @return the contact URI or null if the contact is not available
     */
    public String getContact() {
        return contact;
    }

    /**
     * Sets the contact URI for this ServiceInfo. A null value removes any
     * existing value.
     * 
     * @param contactURI
     *            - the contact URI or null
     * @throws IllegalArgumentException
     *             - if the contactURI argument does not follow syntax for a
     *             valid user identity
     */
    public void setContact(String contactURI) {
        if (contactURI != null && !isContactURIValid(contactURI)) {
            throw new IllegalArgumentException(
                    "The contactURI argument does not follow syntax for a valid user identity");
        }

        this.contact = contactURI;
    }

    private static boolean isContactURIValid(String contactURI) {
        // TODO not implemented yet
        return true;
    }

    /**
     * Returns the session participation state of this ServiceInfo.
     * 
     * @return PresenceState.OPEN, PresenceState.CLOSED, or PresenceState.UNSET
     */
    public PresenceState getSessionParticipation() {
        return sessionParticipation;
    }

    /**
     * Sets the session participation state for this ServiceInfo.
     * 
     * @param state
     *            - PresenceState.OPEN, PresenceState.CLOSED, or
     *            PresenceState.UNSET
     * 
     * @throws IllegalArgumentException
     *             - if the state argument is null
     */
    public void setSessionParticipation(PresenceState state) {
        if (state == null) {
            throw new IllegalArgumentException("The state argument is null");
        }
        this.sessionParticipation = state;
    }

    /**
     * Returns the StatusIcon of this ServiceInfo.
     * 
     * @return the StatusIcon or null if the status icon is not available
     */
    public StatusIcon getStatusIcon() {
        return statusIcon;
    }

    /**
     * Sets a StatusIcon for this ServiceInfo. This will replace any existing
     * StatusIcon. A null value removes any existing value.
     * 
     * @param statusIcon
     *            - the StatusIcon to set or null
     */
    public void setStatusIcon(StatusIcon statusIcon) {
        this.statusIcon = statusIcon;
    }

    /**
     * Returns the classification of this ServiceInfo.
     * 
     * @return the classification or null if the classification is not available
     */
    public String getClassification() {
        return classification;
    }

    /**
     * Sets the classification of this ServiceInfo. A null value removes any
     * existing value. This can be used to group similar components and is not
     * generally presented to the watcher user interface.
     * 
     * @param classification
     *            - the classification or null
     * @throws IllegalArgumentException
     *             - if the classification argument is an empty string
     */
    public void setClassification(String classification) {
        if (classification == null || "".equals(classification.trim())) {
            throw new IllegalArgumentException(
                    "The classification argument is an empty string");
        }
        this.classification = classification;
    }

    /**
     * Returns the device identifier of the device that the service is running
     * on.
     * 
     * @return the device identifier or null if the identifier is not available
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device identifier of the device that the service is running on.
     * A null value removes any existing value.
     * 
     * @param deviceId
     *            - the device identifier or null
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the time when this ServiceInfo was added to the PresenceDocument
     * .
     * 
     * @return the Date or null if this ServiceInfo has not been added to a
     *         PresenceDocument
     */
    public Date getTimestamp() {
        return attachable.getTimestamp();
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

    public static class ServiceInfoBuilder {
        private String id;
        private PresenceState status;
        private String freeText;

        private String serviceId;
        private String serviceVersion;
        private String serviceDescription;

        private String deviceId;
        private String contact;

        public ServiceInfoBuilder buildId(String id) {
            this.id = id;
            return this;
        }

        public ServiceInfoBuilder buildStatus(PresenceState status) {
            this.status = status;
            return this;
        }

        public ServiceInfoBuilder buildFreeText(String freeText) {
            this.freeText = freeText;
            return this;
        }

        public ServiceInfoBuilder buildServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public ServiceInfoBuilder buildServiceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        public ServiceInfoBuilder buildServiceDescription(
                String serviceDescription) {
            this.serviceDescription = serviceDescription;
            return this;
        }

        public ServiceInfoBuilder buildDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public ServiceInfoBuilder buildContact(String contact) {
            this.contact = contact;
            return this;
        }

        /**
         * Build ServiceInfo instance.
         * 
         * @return ServiceInfo instance
         * @throws IllegalArgumentException - if instance can't be created.
         */
        public ServiceInfo build() {
            return new ServiceInfo(this);
        }
    }
    
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((barringState == null) ? 0 : barringState.hashCode());
        result = prime * result
                + ((classification == null) ? 0 : classification.hashCode());
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result
                + ((deviceId == null) ? 0 : deviceId.hashCode());
        result = prime * result
                + ((freeText == null) ? 0 : freeText.hashCode());
        result = prime * result
                + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime
                * result
                + ((registrationState == null) ? 0 : registrationState
                        .hashCode());
        result = prime
                * result
                + ((serviceDescription == null) ? 0 : serviceDescription
                        .hashCode());
        result = prime * result
                + ((serviceId == null) ? 0 : serviceId.hashCode());
        result = prime * result
                + ((serviceVersion == null) ? 0 : serviceVersion.hashCode());
        result = prime
                * result
                + ((sessionParticipation == null) ? 0 : sessionParticipation
                        .hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result
                + ((statusIcon == null) ? 0 : statusIcon.hashCode());
        result = prime * result
                + ((willingnes == null) ? 0 : willingnes.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceInfo other = (ServiceInfo) obj;
        if (barringState == null) {
            if (other.barringState != null)
                return false;
        } else if (!barringState.equals(other.barringState))
            return false;
        if (classification == null) {
            if (other.classification != null)
                return false;
        } else if (!classification.equals(other.classification))
            return false;
        if (contact == null) {
            if (other.contact != null)
                return false;
        } else if (!contact.equals(other.contact))
            return false;
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
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (registrationState == null) {
            if (other.registrationState != null)
                return false;
        } else if (!registrationState.equals(other.registrationState))
            return false;
        if (serviceDescription == null) {
            if (other.serviceDescription != null)
                return false;
        } else if (!serviceDescription.equals(other.serviceDescription))
            return false;
        if (serviceId == null) {
            if (other.serviceId != null)
                return false;
        } else if (!serviceId.equals(other.serviceId))
            return false;
        if (serviceVersion == null) {
            if (other.serviceVersion != null)
                return false;
        } else if (!serviceVersion.equals(other.serviceVersion))
            return false;
        if (sessionParticipation == null) {
            if (other.sessionParticipation != null)
                return false;
        } else if (!sessionParticipation.equals(other.sessionParticipation))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (statusIcon == null) {
            if (other.statusIcon != null)
                return false;
        } else if (!statusIcon.equals(other.statusIcon))
            return false;
        if (willingnes == null) {
            if (other.willingnes != null)
                return false;
        } else if (!willingnes.equals(other.willingnes))
            return false;
        return true;
    }

    
    public String toString() {
        return "ServiceInfo [identifier=" + identifier + ", serviceId="
                + serviceId + ", serviceVersion=" + serviceVersion
                + ", serviceDescription=" + serviceDescription
                + ", willingnes=" + willingnes + ", status=" + status
                + ", barringState=" + barringState + ", registrationState="
                + registrationState + ", sessionParticipation="
                + sessionParticipation + ", contact=" + contact
                + ", statusIcon=" + statusIcon + ", classification="
                + classification + ", deviceId=" + deviceId + ", attachable="
                + attachable + ", freeText=" + freeText + "]";
    }
}
