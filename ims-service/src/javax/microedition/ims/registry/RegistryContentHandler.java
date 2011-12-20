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

import javax.microedition.ims.core.registry.property.CapabilityProperty;
import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import java.util.Map;

/**
 * 
 * @author Andrei Khomushko
 *
 */
interface RegistryContentHandler {
    void streamPropertyParsed(String typeValue, String[] types);

    void framePropertyParsed(String[] contentTypes, int maxSize);

    void basicPropertyParsed(String[] contentTypes);

    void eventPropertyParsed(final String[] packages);

    void coreServicePropertyParsed(String serviceId, String[] iARIs,
            String[] iCSIs, String[] featureTags);

    void qosPropertyParsed(String serviceId, String contentType,
            String sendFlowspec, String receiveFlowspec);

    void registerPropertyParsed(String serviceId, String agentId, Map<String, String> headers);

    void capabilityPropertyParsed(CapabilityProperty.SectorType sectorId, CapabilityProperty.MessageType messageType,
            Attribute[] sdpFields);

    void mprofPropertyParsed(String serviceId, String mediaProfile);

    void connectionPropertyParsed(String serviceId);

    void writePropertyParsed(String[] headers);

    void readPropertyParsed(String[] headers);
    
    void pagerPropertyParsed(String[] contentTypes);
    
    void authenticationPropertyParsed(String username, String password);
    
    void xdmAuthenticationPropertyParsed(String username, String password);
}
