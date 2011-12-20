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

package javax.microedition.ims.registry;

import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.registry.ClientRegistry;
import javax.microedition.ims.core.registry.CommonRegistry;
import javax.microedition.ims.core.registry.TypeName;
import javax.microedition.ims.core.registry.property.*;
import javax.microedition.ims.core.registry.property.StreamProperty.StreamType;
import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author Andrei Khomushko
 * 
 */
public final class RegistrySerializer {
    private RegistrySerializer() {
        assert false;
    }
    
    public static String[][] serializeClientRegistry(ClientRegistry clientRegistry) {
        if(clientRegistry == null) {
            return new String[0][];
        }
        
        final String[][] retValue;

        Set<String[]> properties = new HashSet<String[]>();

        BasicProperty basicProperty = clientRegistry.getBasicProperty();
        if (basicProperty != null) {
            String contentTypes = CollectionsUtils.concatenate(basicProperty
                    .getContentTypes(), " ");
            properties
                    .add(new String[] { TypeName.Basic.name(), contentTypes });
        }

        CoreServiceProperty coreServiceProperty = clientRegistry
                .getCoreServiceProperty();
        if (coreServiceProperty != null) {
            String serviceId = coreServiceProperty.getServiceId();
            String iARIs = CollectionsUtils.concatenate(coreServiceProperty
                    .getIARIs(), " ");
            String iCSIs = CollectionsUtils.concatenate(coreServiceProperty
                    .getICSIs(), " ");
            String featureTags = CollectionsUtils.concatenate(
                    coreServiceProperty.getFeatureTags(), " ");
            properties.add(new String[] { TypeName.CoreService.name(),
                    serviceId, iARIs, iCSIs, featureTags });
        }

        EventProperty eventProperty = clientRegistry.getEventProperty();
        if (eventProperty != null) {
            String packages = CollectionsUtils.concatenate(eventProperty
                    .getPackages(), " ");
            properties.add(new String[] { TypeName.Event.name(), packages });
        }

        FrameProperty frameProperty = clientRegistry.getFrameProperty();
        if (frameProperty != null) {
            String contentTypes = CollectionsUtils.concatenate(frameProperty
                    .getContentTypes(), "");
            int maxSize = frameProperty.getMaxSize();
            properties.add(new String[] { TypeName.Framed.name(), contentTypes,
                    maxSize + "" });
        }

        QosProperty qosProperty = clientRegistry.getQosProperty();
        if (qosProperty != null) {
            String serviceId = qosProperty.getServiceId();
            String contentTypes = qosProperty.getContentType();
            String sendFlowspec = qosProperty.getSendFlowspec();
            String receiveFlowspec = qosProperty.getReceiveFlowspec();
            properties.add(new String[] { TypeName.Qos.name(), serviceId,
                    contentTypes, sendFlowspec, receiveFlowspec });

        }

        StreamProperty streamProperty = clientRegistry.getStreamProperty();
        if (streamProperty != null) {
            String types = CollectionsUtils.concatenate(streamProperty
                    .getTypes().toArray(new StreamType[0]), "",
                    new CollectionsUtils.Transformer<StreamType, String>() {
                        
                        public String transform(StreamType streamType) {
                            return streamType.name();
                        }
                    });
            properties.add(new String[] { TypeName.Stream.name(), types });
        }
        
        PagerProperty pagerProperty = clientRegistry.getPagerProperty();
        if (pagerProperty != null) {
            String contetnTypes = CollectionsUtils.concatenate(pagerProperty.
                    getContentTypes(), " ");

            properties
                    .add(new String[] { TypeName.Pager.name(), contetnTypes });
        }

        retValue = properties.toArray(new String[0][]);

        return retValue;
    }

    public static String[][] serializeCommonRegistry(CommonRegistry commonRegistry) {
        final String[][] retValue;

        Set<String[]> properties = new HashSet<String[]>();

        CapabilityProperty[] capabilityProperties = commonRegistry
                .getCapabilityProperties();
        for (CapabilityProperty capabilityProperty: capabilityProperties) {
            String sectorId = capabilityProperty.getSectorId().getStringValue();
            String messageType = capabilityProperty.getMessageType().getStringValue();
            Attribute[] attributes = capabilityProperty.getSdpFields();
            
            String[] sdpFileds = new String[attributes.length];
            for(int i = 0; i < attributes.length; i++) {
                Attribute attribute = attributes[i];
                sdpFileds[i] = String.format("a=%s:%s", attribute.getName(), attribute.getValue());
            }
            
            String[] property = new String[attributes.length + 3];
            property[0] = TypeName.Cap.name();
            property[1] = sectorId;
            property[2] = messageType;
            System.arraycopy(sdpFileds, 0, property, 3, sdpFileds.length);
            properties.add(property);
        }

        String[] conections = commonRegistry.getConnectionsValues();
        if (conections.length > 0) {
            String[] property = new String[conections.length + 1];
            property[0] = TypeName.Connection.name();
            System.arraycopy(conections, 0, property, 1, conections.length);
            properties.add(property);
        }

        MprofProperty[] mprofProperties = commonRegistry.getMprofProperties();
        for (MprofProperty mprofProperty : mprofProperties) {
            String serviceId = mprofProperty.getServiceId();
            String profile = mprofProperty.getMediaProfile();
            properties.add(new String[] { TypeName.Mprof.name(), serviceId,
                    profile });
        }

        String[] readHeaders = commonRegistry.getReadHeaders();
        if (readHeaders.length > 0) {
            String[] property = new String[readHeaders.length + 1];
            property[0] = TypeName.Read.name();
            System.arraycopy(readHeaders, 0, property, 1, readHeaders.length);
            properties.add(property);
        }

        RegisterProperty[] registerProperties = commonRegistry
                .getRegisterProperties();
        for (RegisterProperty registerProperty : registerProperties) {
            String serviceId = registerProperty.getServiceId();
            Map<String, String> regHeaders = registerProperty.getHeaders();
            
            Set<String> headerList = new HashSet<String>();
            for(Entry<String, String> regEntry: regHeaders.entrySet()) {
                headerList.add(String.format("%s: %s", regEntry.getKey(), regEntry.getValue()));
            }
            String[] headers = headerList.toArray(new String[0]);
            
            String[] property = new String[headers.length + 2];
            property[0] = TypeName.Reg.name();
            property[1] = serviceId;
            System.arraycopy(headers, 0, property, 2, headers.length);
            properties.add(property);
        }
        
        String[] writeHeaders = commonRegistry.getWriteHeaders();
        if (writeHeaders.length > 0) {
            String[] property = new String[writeHeaders.length + 1];
            property[0] = TypeName.Write.name();
            System.arraycopy(writeHeaders, 0, property, 1, writeHeaders.length);
            properties.add(property);
        }
        
        AuthenticationProperty authProperty = commonRegistry.getAuthenticationProperty();
        if(authProperty != null) {
            String[] property = new String[3];
            property[0] = TypeName.Auth.name();
            property[1] = authProperty.getUsername();
            property[2] = authProperty.getPassword();
            properties.add(property);
        }

        AuthenticationProperty xdmAuthProperty = commonRegistry.getXdmAuthenticationProperty();
        if(xdmAuthProperty != null) {
            String[] property = new String[3];
            property[0] = TypeName.XdmAuth.name();
            property[1] = xdmAuthProperty.getUsername();
            property[2] = xdmAuthProperty.getPassword();
            properties.add(property);
        }

        retValue = properties.toArray(new String[0][]);

        return retValue;
    }
}
