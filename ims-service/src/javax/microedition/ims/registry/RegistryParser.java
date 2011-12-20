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

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.core.registry.TypeName;
import javax.microedition.ims.core.registry.property.CapabilityProperty;
import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Parse registeries.
 * 
 * @author Andrei Khomushko
 *
 */
public final class RegistryParser {
    private static final String TAG = "RegistryParser"; 
    
    private AtomicReference<Boolean> done = new AtomicReference<Boolean>(false);
    private ListenerHolder<RegistryContentHandler> listenerHolder = new ListenerHolder<RegistryContentHandler>(RegistryContentHandler.class);
    
    public void addRegistryContentHandler(RegistryContentHandler handler) {
        listenerHolder.addListener(handler);
    }
    
    public void removeRegistryContentHandler(RegistryContentHandler handler) {
        listenerHolder.removeListener(handler);
    }
    
    /**
     * Parse registries.
     * 
     * @param registries - source
     * 
     * @throws IllegalStateException - if parser has been realized.
     */
    public void parse(String[][] registries) {
        if(done.get()) {
            throw new IllegalStateException("Parser has been closed");
        }     
        
        for (String[] registry : registries) {
            final TypeName typeName = TypeName.parse(registry[0]);
            if (typeName != null) {
                final String[] values = new String[registry.length - 1];
                System.arraycopy(registry, 1, values, 0, values.length);

                switch (typeName) {
                case Stream: 
                    parseStreamProperty(values);
                    break;
                case Framed:
                    parseFrameProperty(values);
                    break;
                case Basic:
                    parseBasicProperty(values);
                    break;
                case Event:
                    parseEventProperty(values);
                    break;
                case CoreService:
                    parseCoreServiceProperty(values);
                    break;
                case Qos:
                    parseQosProperty(values);
                    break;
                case Reg: 
                    parseRegisterProperty(values);
                    break;
                case Auth: 
                    parseAuthenticationProperty(values);
                    break;
                case XdmAuth: 
                    parseXdmAuthenticationProperty(values);
                    break;
                case Cap:
                    parseCapabilityProperty(values);
                    break;
                case Mprof:
                    parseMprofProperty(values);
                    break;
                case Connection:
                    parseConnectionProperty(values);
                    break;
                case Write:
                    parseWriteProperty(values);
                    break;
                case Read:
                    parseReadProperty(values);
                    break;
                case Pager:
                    parsePagerProperty(values);
                    break;
                default:
                    Logger.log(TAG, "Unhandled typeName = " + typeName);
                    assert false : "Unhandled typeName = " + typeName;
                }
            } else {
                Logger.log(TAG, "Unknown typeName = " + typeName);
                assert false : "Unknown typeName = " + typeName;
            }
        }
    }
    
    private void parseStreamProperty(final String[] values) {
        final String typeValue = values[0];
        final String[] types = StringUtils.isEmpty(typeValue)?  new String[0]: typeValue.split(" ");
        
        listenerHolder.getNotifier().streamPropertyParsed(typeValue, types);
    }
    
    private void parseFrameProperty(final String[] values) {
        final String[] contentTypes = values[0].split(" ");
        final int maxSize = Integer.parseInt(values[1]);
        
        listenerHolder.getNotifier().framePropertyParsed(contentTypes, maxSize);
    }
    
    private void parseBasicProperty(final String[] values) {
        final String[] contentTypes = values[0].split(" ");
        
        listenerHolder.getNotifier().basicPropertyParsed(contentTypes);
    }
    
    private void parseEventProperty(final String[] values) {
        final String[] packages = values[0].split(" ");
        
        listenerHolder.getNotifier().eventPropertyParsed(packages);
    }
    
    private void parseCoreServiceProperty(final String[] values) {
        final String serviceId = values[0];
        
        final String iARIsValue = values[1].trim();
        final String iCSIsValue = values[2].trim();
        final String featureTagsValue = values[3].trim();
        
        final String[] iARIs = "".equals(iARIsValue)? new String[0]: iARIsValue.split(" ");
        final String[] iCSIs = "".equals(iCSIsValue)? new String[0]: iCSIsValue.split(" ");
        final String[] featureTags = "".equals(featureTagsValue)? new String[0]: featureTagsValue.split(" ");
        
        listenerHolder.getNotifier().coreServicePropertyParsed(serviceId, iARIs, iCSIs, featureTags);
    }
    
    private void parseQosProperty(final String[] values) {
        final String serviceId = values[0];
        final String contentType = values[1];
        final String sendFlowspec = values[2];
        final String receiveFlowspec = values[3];
        
        listenerHolder.getNotifier().qosPropertyParsed(serviceId, contentType, sendFlowspec, receiveFlowspec);
    }
    
    private void parseRegisterProperty(final String[] values) {
        final String serviceId = values[0];
        final String agentId = values[1];

        final String[] headers = new String[values.length - 2];
        System.arraycopy(values, 2, headers, 0, headers.length);
        
        Map<String, String> headersMap = new HashMap<String, String>();
        for(String header: headers) {
            String[] vars = header.split(":");
            headersMap.put(vars[0].trim(), vars[1].trim());
        }

        listenerHolder.getNotifier().registerPropertyParsed(serviceId, agentId, headersMap);
    }
    
    private void parseAuthenticationProperty(final String[] values) {
        final String username = values[0];
        final String password = values[1];

        listenerHolder.getNotifier().authenticationPropertyParsed(username, password);
    }

    private void parseXdmAuthenticationProperty(final String[] values) {
        final String username = values[0];
        final String password = values[1];

        listenerHolder.getNotifier().xdmAuthenticationPropertyParsed(username, password);
    }
    
    private void parseCapabilityProperty(final String[] values) {
        final String sectorId = values[0];
        final String messageId = values[1];
        final String[] sdpFields = new String[values.length - 2];
        System.arraycopy(values, 2, sdpFields, 0, sdpFields.length);

        CapabilityProperty.SectorType sectorType = CapabilityProperty.SectorType.parse(sectorId);
        CapabilityProperty.MessageType messageType = CapabilityProperty.MessageType.parse(messageId); 

        Set<Attribute> attributes = new HashSet<Attribute>();
        for(String field: sdpFields) {
            String[] exprs = field.split("=");
            String attrValue = exprs[1];
            exprs = attrValue.split(":");
            attributes.add(new Attribute(exprs[0], exprs[1]));
        }
        
        listenerHolder.getNotifier().capabilityPropertyParsed(sectorType, messageType, attributes.toArray(new Attribute[0]));
    }
    
    private void parseMprofProperty(final String[] values) {
        final String serviceId = values[0];
        final String mediaProfile = values[1];
        
        
        listenerHolder.getNotifier().mprofPropertyParsed(serviceId, mediaProfile);
    }
    
    private void parseConnectionProperty(final String[] values) {
        final String serviceId = values[0];
        
        listenerHolder.getNotifier().connectionPropertyParsed(serviceId);
    }

    private void parseWriteProperty(String[] values) {
        listenerHolder.getNotifier().writePropertyParsed(values);
    }

    private void parseReadProperty(String[] values) {
        listenerHolder.getNotifier().readPropertyParsed(values);
    }
    
    private void parsePagerProperty(String[] values) {
        String contentTypesValue = values[0];
        String[] contentTypes = contentTypesValue.split(" ");
        listenerHolder.getNotifier().pagerPropertyParsed(contentTypes);
    }

    public void close() {
        listenerHolder.shutdown();
        done.compareAndSet(false, true);
    }
}
