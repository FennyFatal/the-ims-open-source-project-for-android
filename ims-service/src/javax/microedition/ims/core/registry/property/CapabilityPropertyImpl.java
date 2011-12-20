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

package javax.microedition.ims.core.registry.property;

import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CapabilityPropertyImpl implements CapabilityProperty {
    private final SectorType sectorId;
    private final MessageType messageType;
    private final Set<Attribute> sdpFields = new HashSet<Attribute>();

    public CapabilityPropertyImpl(SectorType sectorId, MessageType messageType,
                                  Attribute[] sdpFields) {
        this.sectorId = sectorId;
        this.messageType = messageType;
        this.sdpFields.addAll(Arrays.asList(sdpFields));
    }

    
    public SectorType getSectorId() {
        return sectorId;
    }

    
    public MessageType getMessageType() {
        return messageType;
    }

    
    public Attribute[] getSdpFields() {
        return sdpFields.toArray(new Attribute[sdpFields.size()]);
    }

    
    public String toString() {
        return "CapabilityPropertyImpl [messageType=" + messageType
                + ", sdpFields=" + sdpFields + ", sectorId=" + sectorId + "]";
    }

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((messageType == null) ? 0 : messageType.hashCode());
        result = prime * result
                + ((sdpFields == null) ? 0 : sdpFields.hashCode());
        result = prime * result
                + ((sectorId == null) ? 0 : sectorId.hashCode());
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
        CapabilityPropertyImpl other = (CapabilityPropertyImpl) obj;
        if (messageType == null) {
            if (other.messageType != null) {
                return false;
            }
        }
        else if (!messageType.equals(other.messageType)) {
            return false;
        }
        if (sdpFields == null) {
            if (other.sdpFields != null) {
                return false;
            }
        }
        else if (!sdpFields.equals(other.sdpFields)) {
            return false;
        }
        if (sectorId == null) {
            if (other.sectorId != null) {
                return false;
            }
        }
        else if (!sectorId.equals(other.sectorId)) {
            return false;
        }
        return true;
    }
}
