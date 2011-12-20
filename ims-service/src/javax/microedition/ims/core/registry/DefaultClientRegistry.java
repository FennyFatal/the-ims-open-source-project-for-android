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

import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.registry.property.*;
import javax.microedition.ims.core.registry.property.StreamProperty.StreamType;
import java.util.Arrays;
import java.util.Collection;

/**
 * Default implementation {@link ClientRegistry}
 *
 * @author Andrei Khomushko
 */
public class DefaultClientRegistry implements ClientRegistry {
    private final String appId;
    private final StreamProperty streamProperty;
    private final FrameProperty frameProperty;
    private final BasicProperty basicProperty;
    private final EventProperty eventProperty;
    private final CoreServiceProperty coreServiceProperty;
    private final QosProperty qosProperty;
    private final PagerProperty pagerProperty;

    private DefaultClientRegistry(final ClientRegistryBuilder builder) {
        this.appId = builder.appId;
        this.streamProperty = builder.streamProperty;
        this.frameProperty = builder.frameProperty;
        this.basicProperty = builder.basicProperty;
        this.eventProperty = builder.eventProperty;
        this.coreServiceProperty = builder.coreServiceProperty;
        this.qosProperty = builder.qosProperty;
        this.pagerProperty = builder.pagerProperty;
    }

    public String getAppId() {
        return appId;
    }

    
    public BasicProperty getBasicProperty() {
        return basicProperty;
    }

    
    public CoreServiceProperty getCoreServiceProperty() {
        return coreServiceProperty;
    }

    public PagerProperty getPagerProperty() {
        return pagerProperty;
    }

    
    public EventProperty getEventProperty() {
        return eventProperty;
    }

    
    public FrameProperty getFrameProperty() {
        return frameProperty;
    }

    
    public QosProperty getQosProperty() {
        return qosProperty;
    }

    
    public StreamProperty getStreamProperty() {
        return streamProperty;
    }


    public String toString() {
        return "DefaultClientRegistry [appId=" + appId + ", basicProperty="
                + basicProperty + ", coreServiceProperty="
                + coreServiceProperty + ", eventProperty=" + eventProperty
                + ", frameProperty=" + frameProperty + ", qosProperty="
                + qosProperty + ", streamProperty=" + streamProperty + "]";
    }


    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appId == null) ? 0 : appId.hashCode());
        result = prime * result
                + ((basicProperty == null) ? 0 : basicProperty.hashCode());
        result = prime
                * result
                + ((coreServiceProperty == null) ? 0 : coreServiceProperty
                .hashCode());
        result = prime * result
                + ((eventProperty == null) ? 0 : eventProperty.hashCode());
        result = prime * result
                + ((frameProperty == null) ? 0 : frameProperty.hashCode());
        result = prime * result
                + ((qosProperty == null) ? 0 : qosProperty.hashCode());
        result = prime * result
                + ((streamProperty == null) ? 0 : streamProperty.hashCode());
        return result;
    }


    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultClientRegistry other = (DefaultClientRegistry) obj;
        if (appId == null) {
            if (other.appId != null) {
                return false;
            }
        }
        else if (!appId.equals(other.appId)) {
            return false;
        }
        if (basicProperty == null) {
            if (other.basicProperty != null) {
                return false;
            }
        }
        else if (!basicProperty.equals(other.basicProperty)) {
            return false;
        }
        if (coreServiceProperty == null) {
            if (other.coreServiceProperty != null) {
                return false;
            }
        }
        else if (!coreServiceProperty.equals(other.coreServiceProperty)) {
            return false;
        }
        if (eventProperty == null) {
            if (other.eventProperty != null) {
                return false;
            }
        }
        else if (!eventProperty.equals(other.eventProperty)) {
            return false;
        }
        if (frameProperty == null) {
            if (other.frameProperty != null) {
                return false;
            }
        }
        else if (!frameProperty.equals(other.frameProperty)) {
            return false;
        }
        if (qosProperty == null) {
            if (other.qosProperty != null) {
                return false;
            }
        }
        else if (!qosProperty.equals(other.qosProperty)) {
            return false;
        }
        if (streamProperty == null) {
            if (other.streamProperty != null) {
                return false;
            }
        }
        else if (!streamProperty.equals(other.streamProperty)) {
            return false;
        }
        return true;
    }


    public static class ClientRegistryBuilder {
        private final String appId;
        private StreamProperty streamProperty;
        private FrameProperty frameProperty;
        private BasicProperty basicProperty;
        private EventProperty eventProperty;
        private CoreServiceProperty coreServiceProperty;
        private QosProperty qosProperty;
        private PagerProperty pagerProperty;

        public ClientRegistryBuilder(String appId) {
            if (appId == null) {
                throw new IllegalArgumentException("The appId argument is null");
            }

            this.appId = appId;
        }

        public ClientRegistryBuilder buildStreamProperty(final String[] types) {
            Collection<StreamType> streamTypes = CollectionsUtils
                    .transform(
                            Arrays.asList(types),
                            new CollectionsUtils.Transformer<String, StreamProperty.StreamType>() {
                                
                                public StreamProperty.StreamType transform(
                                        String stringValue) {
                                    return StreamProperty.StreamType
                                            .parse(stringValue);
                                }
                            });
            this.streamProperty = new StreamPropertyImpl(
                    streamTypes.toArray(new StreamType[streamTypes.size()])
            );
            return this;
        }

        public ClientRegistryBuilder buildFrameProperty(String[] contentTypes,
                                                        int maxSize) {
            this.frameProperty = new FramePropertyImpl(contentTypes, maxSize);
            return this;
        }

        public ClientRegistryBuilder buildBasicProperty(String[] contentTypes) {
            this.basicProperty = new BasicPropertyImpl(contentTypes);
            return this;
        }

        public ClientRegistryBuilder buildPagerProperty(String[] contentTypes) {
            this.pagerProperty = new PagerPropertyImpl(contentTypes);
            return this;
        }

        public ClientRegistryBuilder buildEventProperty(final String[] packages) {
            this.eventProperty = new EventPropertyImpl(packages);
            return this;
        }

        public ClientRegistryBuilder buildCoreServiceProperty(String serviceId,
                                                              String[] iARIs, String[] iCSIs, String[] featureTags) {
            this.coreServiceProperty = new CoreServicePropertyImpl(serviceId,
                    iARIs, iCSIs, featureTags);
            return this;
        }

        public ClientRegistryBuilder buildQosProperty(String serviceId,
                                                      String contentType, String sendFlowspec, String receiveFlowspec) {
            this.qosProperty = new QosPropertyImpl(serviceId, contentType,
                    sendFlowspec, receiveFlowspec);
            return this;
        }

        public DefaultClientRegistry build() {
            return new DefaultClientRegistry(this);
        }


        public String toString() {
            return "ClientRegistryBuilder [basicProperty=" + basicProperty
                    + ", coreServiceProperty=" + coreServiceProperty
                    + ", eventProperty=" + eventProperty + ", frameProperty="
                    + frameProperty + ", qosProperty=" + qosProperty
                    + ", streamProperty=" + streamProperty + "]";
        }
    }
}
