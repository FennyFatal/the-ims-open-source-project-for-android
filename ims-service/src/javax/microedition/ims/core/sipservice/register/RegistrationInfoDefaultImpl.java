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

package javax.microedition.ims.core.sipservice.register;

import javax.microedition.ims.config.UserInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 7.6.2010
 * Time: 18.58.04
 */
public class RegistrationInfoDefaultImpl implements RegistrationInfo {

    public static final RegistrationInfo EMPTY_REGISTRATION_INFO = new RegistrationInfoDefaultImpl.Builder().
            knownContacts(Collections.<String>emptyList()).
            associatedURI(Collections.<String>emptyList()).
            globalAddress("127.0.0.1").
            build();

    private final List<String> knownContacts;
    private final List<String> associatedURI;
    private final List<String> integralURIList;
    private final String globalAddress;
    private final String serviceRoute;

    private final int hashCode;


    private RegistrationInfoDefaultImpl(final Builder builder) {
        this.globalAddress = builder.globalAddress;

        if (builder.knownContacts == null) {
            final String errMsg = "knownContacts must not be null. Now it has value '" + builder.knownContacts + "'";
            throw new IllegalArgumentException(errMsg);
        }

        if (builder.associatedURI == null) {
            final String errMsg = "associatedURI must not be null. Now it has value '" + builder.associatedURI + "'";
            throw new IllegalArgumentException(errMsg);
        }

        this.knownContacts = Collections.unmodifiableList(new ArrayList<String>(builder.knownContacts));
        this.associatedURI = Collections.unmodifiableList(new ArrayList<String>(builder.associatedURI));

        List<String> tempList = new ArrayList<String>(knownContacts.size() + associatedURI.size());
        tempList.addAll(knownContacts);
        tempList.addAll(associatedURI);
        this.integralURIList = Collections.unmodifiableList(new ArrayList<String>(tempList));
        this.serviceRoute = builder.serviceRoute;

        this.hashCode = calculateHashCode();
    }

    
    public List<String> getKnownContacts() {
        return knownContacts;
    }

    
    public List<String> getAssociatedURI() {
        return associatedURI;
    }

    
    public List<String> getIntegralURIList() {
        return integralURIList;
    }

    
    public String getGlobalAddress() {
        return globalAddress;
    }

    public String getServiceRoute(UserInfo fromUserInfo) {
        return serviceRoute;
    }

    private int calculateHashCode() {
        int result = knownContacts != null ? knownContacts.hashCode() : 0;
        result = 31 * result + (associatedURI != null ? associatedURI.hashCode() : 0);
        result = 31 * result + (globalAddress != null ? globalAddress.hashCode() : 0);
        return result;
    }


    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RegistrationInfoDefaultImpl that = (RegistrationInfoDefaultImpl) o;

        return hashCode == that.hashCode;

    }
/*
    
    public int hashCode() {
        return hashCode;
    }*/

    
    public String toString() {
        return "RegistrationInfoDefaultImpl{" +
                "knownContacts=" + knownContacts +
                ", associatedURI=" + associatedURI +
                ", integralURIList=" + integralURIList +
                '}';
    }

    public static class Builder {
        private List<String> knownContacts;
        private List<String> associatedURI;
        private List<String> integralURIList;
        private String globalAddress;
        private String serviceRoute;

        public Builder() {
        }


        public Builder knownContacts(final List<String> knownContacts) {
            this.knownContacts = knownContacts;
            return this;
        }

        public Builder associatedURI(final List<String> associatedURI) {
            this.associatedURI = associatedURI;
            return this;
        }

        public Builder integralURIList(final List<String> integralURIList) {
            this.integralURIList = integralURIList;
            return this;
        }

        public Builder globalAddress(final String globalAddress) {
            this.globalAddress = globalAddress;
            return this;
        }

        public Builder serviceRoute(final String serviceRoute) {
            this.serviceRoute = serviceRoute;
            return this;
        }

        RegistrationInfo build() {
            return new RegistrationInfoDefaultImpl(this);
        }
    }
}
