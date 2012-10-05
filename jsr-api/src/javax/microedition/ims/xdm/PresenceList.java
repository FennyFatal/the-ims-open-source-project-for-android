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

package javax.microedition.ims.xdm;

/**
 * A presence list is used by a presence server to subscribe, on behalf of a
 * watcher, to the presence status of a list of presentities.
 *
 *
 * A list entry consists of either a single user URI or a reference to an
 * already existing URI list. Each entry can provide an optional display name.
 * </p><p>For detailed implementation guidelines and for complete API docs, 
 * please refer to JSR-281 and JSR-235 documentation.
 *
 * 
 * @author Andrei Khomushko
 * 
 */
public class PresenceList {
    
    private String serviceUri;
    private String uriListReference;
    
    /**
     * Constructor for a new PresenceList. Both a serviceUri and a
     * uriListReference must be supplied.
     * 
     * @param serviceUri
     *            - the service URI identifying this presence list
     * @param uriListReference
     *            - the URI list reference for this presence list
     * @throws IllegalArgumentException
     *             - if the serviceUri argument or the uriListReference argument
     *             is null
     */
    public PresenceList(String serviceUri, String uriListReference) {
        if (serviceUri == null) {
            throw new IllegalArgumentException("the serviceUri is null");
        }

        if (uriListReference == null) {
            throw new IllegalArgumentException("the uriListReference is null");
        }

        this.serviceUri = serviceUri;
        this.uriListReference = uriListReference;
    }

    /**
     * Sets the service URI for this presence list.
     * 
     * @param serviceUri
     *            - the service URI identifying this presence list
     * @throws IllegalArgumentException
     *             - if the serviceUri argument is null
     */
    public void setServiceURI(String serviceUri) {
        if (serviceUri == null) {
            throw new IllegalArgumentException("The serviceUri is null");
        }
        
        this.serviceUri = serviceUri;
    }

    /**
     * Returns the service URI for this PresenceList.
     * 
     * @return the service URI
     */
    public String getServiceURI() {
        return serviceUri;
    }

    /**
     * Sets the URI list reference for this PresenceList.
     * 
     * @param uriListReference
     *            - the URI list reference
     * @throws IllegalArgumentException
     *             - if the uriListReference argument is null
     */
    public void setURIListReference(String uriListReference) {
        if (uriListReference == null) {
            throw new IllegalArgumentException("the uriListReference is null");
        }
        
        this.uriListReference = uriListReference;
    }

    /**
     * Returns the URI list reference for this PresenceList.
     * 
     * @return the URI list reference
     */
    public String getURIListReference() {
        return uriListReference;
    }

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((serviceUri == null) ? 0 : serviceUri.hashCode());
        result = prime
                * result
                + ((uriListReference == null) ? 0 : uriListReference.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PresenceList other = (PresenceList) obj;
        if (serviceUri == null) {
            if (other.serviceUri != null)
                return false;
        } else if (!serviceUri.equals(other.serviceUri))
            return false;
        if (uriListReference == null) {
            if (other.uriListReference != null)
                return false;
        } else if (!uriListReference.equals(other.uriListReference))
            return false;
        return true;
    }

    
    public String toString() {
        return "PresenceList [serviceUri=" + serviceUri + ", uriListReference="
                + uriListReference + "]";
    }
}
