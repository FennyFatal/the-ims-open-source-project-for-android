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

package javax.microedition.ims.messages.wrappers.sip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public final class ContactsList {
    private final Collection<UriHeader> contactsList;
    private final boolean asterisk;
    private final String stringValue;


    public ContactsList(Builder builder) {
        this.contactsList = Collections.unmodifiableList(new ArrayList<UriHeader>(builder.contactsList));
        this.asterisk = builder.asterisk;
        this.stringValue = doBuildStringValue();
    }

    public boolean isAsterisk() {
        return asterisk;
    }


    
    public String toString() {
        return stringValue;
    }

    private String doBuildStringValue() {
        return "ContactsList [asterisk=" + asterisk
                + ", contactsList=" + contactsList + ", \ns()="
                + super.toString() + "]";
    }

    public Collection<UriHeader> getContactsList() {
        return contactsList;
    }

    public static class Builder {
        private Collection<UriHeader> contactsList;
        private boolean asterisk;

        public Builder() {
            this.contactsList = new ArrayList<UriHeader>();
        }

        public Builder(final ContactsList contacts) {
            this.contactsList = contacts.contactsList;
            this.asterisk = contacts.asterisk;
        }

        public Builder contactsList(final List<UriHeader> contactsList) {
            ensureContactsListExists();

            this.contactsList.addAll(contactsList);

            return this;
        }

        public Builder contact(final UriHeader contact) {
            ensureContactsListExists();

            this.contactsList.add(contact);

            return this;
        }

        public Builder asterisk(final boolean asterisk) {
            this.asterisk = asterisk;
            return this;
        }

        public ContactsList build() {
            return new ContactsList(this);
        }

        private void ensureContactsListExists() {
            if (this.contactsList == null) {
                this.contactsList = new ArrayList<UriHeader>();
            }
        }

        
        public String toString() {
            return "Builder{" +
                    "contactsList=" + contactsList +
                    ", asterisk=" + asterisk +
                    '}';
        }
    }

}
