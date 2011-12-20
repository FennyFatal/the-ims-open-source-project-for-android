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

package javax.microedition.ims.core.registry;

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.registry.property.*;
import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation {@link CommonRegistry}
 *
 * @author Andrei Khomushko
 */
public class DefaultCommonRegistry implements CommonRegistry {

    private final Set<RegisterProperty> registerProperties = new HashSet<RegisterProperty>();
    private final Set<CapabilityProperty> capabilityProperties = new HashSet<CapabilityProperty>();
    private final Set<MprofProperty> mprofProperties = new HashSet<MprofProperty>();
    private final Set<String> connectionValues = new HashSet<String>();
    private final Set<String> writeHeaders = new HashSet<String>();
    private final Set<String> readHeaders = new HashSet<String>();
    private final AuthenticationProperty authenticationProperty;
    private final AuthenticationProperty xdmAuthenticationProperty;

    private DefaultCommonRegistry(CommonRegistryBuilder builder) {
        this(builder.registerProperties, builder.capabilityProperties,
                builder.mprofProperties, builder.connections,
                builder.writeHeaders, builder.readHeaders,
                builder.authenticationProperty,
                builder.xdmAuthenticationProperty);
    }

    DefaultCommonRegistry(Set<RegisterProperty> registerProperties,
                          Set<CapabilityProperty> capabilityProperties,
                          Set<MprofProperty> mprofProperties, Set<String> connectionValues,
                          Set<String> writeHeaders, Set<String> readHeaders,
                          AuthenticationProperty authenticationProperty,
                          AuthenticationProperty xdmAuthenticationProperty) {
        this.registerProperties.addAll(registerProperties);
        this.capabilityProperties.addAll(capabilityProperties);
        this.mprofProperties.addAll(mprofProperties);
        this.connectionValues.addAll(connectionValues);
        this.writeHeaders.addAll(writeHeaders);
        this.readHeaders.addAll(readHeaders);
        this.authenticationProperty = authenticationProperty;
        this.xdmAuthenticationProperty = xdmAuthenticationProperty;
    }

    
    public CapabilityProperty[] getCapabilityProperties() {
        return capabilityProperties.toArray(new CapabilityProperty[capabilityProperties.size()]);
    }

    public AuthenticationProperty getAuthenticationProperty() {
        return authenticationProperty;
    }

    public AuthenticationProperty getXdmAuthenticationProperty() {
        return xdmAuthenticationProperty;
    }
    
    public MprofProperty[] getMprofProperties() {
        return mprofProperties.toArray(new MprofProperty[mprofProperties.size()]);
    }

    
    public MprofProperty getMprofProperty(final String serviceId) {
        return CollectionsUtils.find(mprofProperties,
                new CollectionsUtils.Predicate<MprofProperty>() {
                    
                    public boolean evaluate(MprofProperty property) {
                        return property.getServiceId().equals(serviceId);
                    }
                });
    }

    
    public String[] getReadHeaders() {
        return readHeaders.toArray(new String[readHeaders.size()]);
    }

    
    public RegisterProperty[] getRegisterProperties() {
        return registerProperties.toArray(new RegisterProperty[registerProperties.size()]);
    }

    
    public RegisterProperty getRegisterProperty(final String serviceId) {
        return CollectionsUtils.find(registerProperties,
                new CollectionsUtils.Predicate<RegisterProperty>() {
                    
                    public boolean evaluate(RegisterProperty property) {
                        return property.getServiceId().equals(serviceId);
                    }
                });
    }

    
    public String[] getWriteHeaders() {
        return writeHeaders.toArray(new String[writeHeaders.size()]);
    }

    
    public String getConnectionValue(final String serviceId) {
        return CollectionsUtils.find(connectionValues,
                new CollectionsUtils.Predicate<String>() {
                    
                    public boolean evaluate(String connection) {
                        return connection.equals(serviceId);
                    }
                });
    }

    
    public String[] getConnectionsValues() {
        return connectionValues.toArray(new String[connectionValues.size()]);
    }

    public String[] getEncodings() {
        return DefaultStackCapabilities.SUPPORTED_ENCODINGS;
    }

    
    public String[] getLanguages() {
        return DefaultStackCapabilities.SUPPORTED_LANGUAGES;
    }

    
    public MessageType[] getMethods() {
        return DefaultStackCapabilities.SUPPORTED_REQUESTS;
    }


    public String toString() {
        return "DefaultCommonRegistry [registerProperties=" + registerProperties
                + ", capabilityProperties=" + capabilityProperties + ", mprofProperties="
                + mprofProperties + ", connectionValues=" + connectionValues + ", writeHeaders="
                + writeHeaders + ", readHeaders=" + readHeaders + ", authenticationProperty="
                + authenticationProperty + ", xdmAuthenticationProperty="
                + xdmAuthenticationProperty + "]";
    }


    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((authenticationProperty == null) ? 0 : authenticationProperty.hashCode());
        result = prime * result
                + ((capabilityProperties == null) ? 0 : capabilityProperties.hashCode());
        result = prime * result + ((connectionValues == null) ? 0 : connectionValues.hashCode());
        result = prime * result + ((mprofProperties == null) ? 0 : mprofProperties.hashCode());
        result = prime * result + ((readHeaders == null) ? 0 : readHeaders.hashCode());
        result = prime * result
                + ((registerProperties == null) ? 0 : registerProperties.hashCode());
        result = prime * result + ((writeHeaders == null) ? 0 : writeHeaders.hashCode());
        result = prime * result
                + ((xdmAuthenticationProperty == null) ? 0 : xdmAuthenticationProperty.hashCode());
        return result;
    }


    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultCommonRegistry other = (DefaultCommonRegistry)obj;
        if (authenticationProperty == null) {
            if (other.authenticationProperty != null)
                return false;
        } else if (!authenticationProperty.equals(other.authenticationProperty))
            return false;
        if (capabilityProperties == null) {
            if (other.capabilityProperties != null)
                return false;
        } else if (!capabilityProperties.equals(other.capabilityProperties))
            return false;
        if (connectionValues == null) {
            if (other.connectionValues != null)
                return false;
        } else if (!connectionValues.equals(other.connectionValues))
            return false;
        if (mprofProperties == null) {
            if (other.mprofProperties != null)
                return false;
        } else if (!mprofProperties.equals(other.mprofProperties))
            return false;
        if (readHeaders == null) {
            if (other.readHeaders != null)
                return false;
        } else if (!readHeaders.equals(other.readHeaders))
            return false;
        if (registerProperties == null) {
            if (other.registerProperties != null)
                return false;
        } else if (!registerProperties.equals(other.registerProperties))
            return false;
        if (writeHeaders == null) {
            if (other.writeHeaders != null)
                return false;
        } else if (!writeHeaders.equals(other.writeHeaders))
            return false;
        if (xdmAuthenticationProperty == null) {
            if (other.xdmAuthenticationProperty != null)
                return false;
        } else if (!xdmAuthenticationProperty.equals(other.xdmAuthenticationProperty))
            return false;
        return true;
    }





    public static class CommonRegistryBuilder {
        private final Set<RegisterProperty> registerProperties = new HashSet<RegisterProperty>();
        private final Set<CapabilityProperty> capabilityProperties = new HashSet<CapabilityProperty>();
        private final Set<MprofProperty> mprofProperties = new HashSet<MprofProperty>();
        private final Set<String> connections = new HashSet<String>();
        private final Set<String> writeHeaders = new HashSet<String>();
        private final Set<String> readHeaders = new HashSet<String>();
        private AuthenticationProperty authenticationProperty;
        private AuthenticationProperty xdmAuthenticationProperty;

        public CommonRegistryBuilder buildRegisterProperty(String serviceId,
                                                           String agentId, Map<String, String> headers) {
            final RegisterProperty registerProperty = new RegisterPropertyImpl(
                    serviceId, agentId, headers);

            CollectionsUtils.replaceOrAdd(registerProperty, registerProperties,
                    new CollectionsUtils.Predicate<RegisterProperty>() {
                        
                        public boolean evaluate(RegisterProperty property) {
                            return property.getServiceId().equals(
                                    registerProperty.getServiceId());
                        }
                    });
            return this;
        }

        public CommonRegistryBuilder buildCapabilityProperty(CapabilityProperty.SectorType sectorType,
                                                             CapabilityProperty.MessageType messageType, Attribute[] sdpFields) {
            this.capabilityProperties.add(new CapabilityPropertyImpl(sectorType,
                    messageType, sdpFields));
            return this;
        }

        public CommonRegistryBuilder buildAuthenticationProperty(String username, String password) {
            this.authenticationProperty = new AuthenticationPropertyImpl(username, password);
            return this;
        }
        
        public CommonRegistryBuilder buildXdmAuthenticationProperty(String username, String password) {
            this.xdmAuthenticationProperty = new AuthenticationPropertyImpl(username, password);
            return this;
        }

        public CommonRegistryBuilder buildMprofProperty(String serviceId,
                                                        String mediaProfile) {
            final MprofProperty mprofProperty = new MprofPropertyImpl(
                    serviceId, mediaProfile);

            CollectionsUtils.replaceOrAdd(mprofProperty, mprofProperties,
                    new CollectionsUtils.Predicate<MprofProperty>() {
                        
                        public boolean evaluate(MprofProperty property) {
                            return property.getServiceId().equals(
                                    mprofProperty.getServiceId());
                        }
                    });
            return this;
        }

        public CommonRegistryBuilder buildConnectionValue(final String serviceId) {
            connections.add(serviceId);
            return this;
        }

        public CommonRegistryBuilder buildWriteHeaders(String[] headers) {
            writeHeaders.addAll(Arrays.asList(headers));
            return this;
        }

        public CommonRegistryBuilder buildReadHeaders(String[] headers) {
            readHeaders.addAll(Arrays.asList(headers));
            return this;
        }

        public DefaultCommonRegistry build() {
            return new DefaultCommonRegistry(this);
        }
    }
}
