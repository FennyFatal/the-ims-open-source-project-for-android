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

package javax.microedition.ims.transport;

import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.transport.impl.DefaultRoute;

public class MsrpRouteImpl extends DefaultRoute {

    public MsrpRouteImpl(String dstHost, int dstPort, int localPort,
                         Protocol transportType, IMSEntityType entityType) {
        super(dstHost, dstPort, localPort, transportType, entityType);
    }



    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dstHost == null) ? 0 : dstHost.hashCode());
        result = prime * result + dstPort;
        result = prime * result
                + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + localPort;
        result = prime
                * result
                + ((simultaneousProtocols == null) ? 0 : simultaneousProtocols
                .hashCode());
        result = prime * result
                + ((transportType == null) ? 0 : transportType.hashCode());
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
        DefaultRoute other = (DefaultRoute) obj;
        if (dstHost == null) {
            if (other.getDstHost() != null) {
                return false;
            }
        }
        else if (!dstHost.equals(other.getDstHost())) {
            return false;
        }
        if (dstPort != other.getDstPort()) {
            return false;
        }
        if (entityType != other.getEntityType()) {
            return false;
        }
        if (localPort != other.getLocalPort()) {
            return false;
        }
        if (simultaneousProtocols == null) {
            if (other.getSimultaneousRoutes() != null) {
                return false;
            }
        }
        else if (!simultaneousProtocols.equals(other.getSimultaneousRoutes())) {
            return false;
        }
        if (transportType != other.getTransportType()) {
            return false;
        }
        return true;
    }

}
