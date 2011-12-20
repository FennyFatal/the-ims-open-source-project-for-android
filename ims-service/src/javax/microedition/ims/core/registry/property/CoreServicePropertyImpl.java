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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CoreServicePropertyImpl implements CoreServiceProperty {
    private final String serviceId;
    private final Set<String> iARIs = new HashSet<String>();
    private final Set<String> iCSIs = new HashSet<String>();
    private final Set<String> featureTags = new HashSet<String>();

    public CoreServicePropertyImpl(String serviceId, String[] iARIs,
                                   String[] iCSIs, String[] featureTags) {
        this.serviceId = serviceId;
        this.iARIs.addAll(Arrays.asList(iARIs));
        this.iCSIs.addAll(Arrays.asList(iCSIs));
        this.featureTags.addAll(Arrays.asList(featureTags));
    }

    public String getServiceId() {
        return serviceId;
    }

    
    public String[] getIARIs() {
        return iARIs.toArray(new String[iARIs.size()]);
    }

    public String[] getICSIs() {
        return iCSIs.toArray(new String[iCSIs.size()]);
    }

    public String[] getFeatureTags() {
        return featureTags.toArray(new String[featureTags.size()]);
    }


    public String toString() {
        return "CoreServicePropertyImpl [featureTags=" + featureTags
                + ", iARIs=" + iARIs + ", iCSIs=" + iCSIs + ", serviceId="
                + serviceId + "]";
    }


    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((featureTags == null) ? 0 : featureTags.hashCode());
        result = prime * result + ((iARIs == null) ? 0 : iARIs.hashCode());
        result = prime * result + ((iCSIs == null) ? 0 : iCSIs.hashCode());
        result = prime * result
                + ((serviceId == null) ? 0 : serviceId.hashCode());
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
        CoreServicePropertyImpl other = (CoreServicePropertyImpl) obj;
        if (featureTags == null) {
            if (other.featureTags != null) {
                return false;
            }
        }
        else if (!featureTags.equals(other.featureTags)) {
            return false;
        }
        if (iARIs == null) {
            if (other.iARIs != null) {
                return false;
            }
        }
        else if (!iARIs.equals(other.iARIs)) {
            return false;
        }
        if (iCSIs == null) {
            if (other.iCSIs != null) {
                return false;
            }
        }
        else if (!iCSIs.equals(other.iCSIs)) {
            return false;
        }
        if (serviceId == null) {
            if (other.serviceId != null) {
                return false;
            }
        }
        else if (!serviceId.equals(other.serviceId)) {
            return false;
        }
        return true;
    }
}
