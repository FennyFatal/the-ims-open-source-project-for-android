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
 * This class contains an entry in a list that represents members in shared
 * lists [OMA_SHARED_LIST] and shared groups [OMA_SHARED_GROUP].
 * 
 * A list entry consists of either a single user URI or a reference to an
 * already existing URI list. Each entry can provide an optional display name.
 * 
 * @see URIList, URIListDocument, GroupDocument, SessionActivityPolicy
 * 
 * @author Andrei Khomushko
 * 
 */
public class ListEntry {
    /** Represents a single user URI. */
    public static final int URI_ENTRY = 1;

    /** Represents a reference to a URI list. */
    public static final int URI_LIST_ENTRY = 2;

    private int type;
    private String uri;
    private String displayName;

    /**
     * Constructor for a new ListEntry of the specified type.
     * 
     * @param type
     *            - URI_ENTRY or URI_LIST_ENTRY
     * @param uri
     *            - a URI representing either a single user identity or
     *            reference of an already existing URI list, depending on the
     *            type argument
     * 
     * @throws IllegalArgumentException
     *             - if the type argument is not URI_ENTRY or URI_LIST_ENTRY
     *             IllegalArgumentException - if the uri argument is null
     */
    public ListEntry(int type, final String uri) {
        if(uri == null) {
            throw new IllegalArgumentException("The uri argument is null");
        }
        
        if(!isTypeValid(type)) {
            throw new IllegalArgumentException("The type argument is invalid");
        }
        
        this.type = type;
        this.uri = uri;
    }
    
    private static boolean isTypeValid(int type) {
        return type == URI_ENTRY || type == URI_LIST_ENTRY;
    }

    /**
     * Returns the display name of this ListEntry.
     * 
     * @return the display name or null if the display name is not set
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of this ListEntry. The display name is a
     * human-readable string that describes the ListEntry. A null value removes
     * any existing display name.
     * 
     * @param displayName
     *            - the display name or null
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the type of this ListEntry.
     * 
     * @return URI_ENTRY or URI_LIST_ENTRY
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the URI or URI list reference of this ListEntry, depending on the
     * type.
     * 
     * @return the URI or URI list reference
     */
    public String getUri() {
        return uri;
    }

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type;
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ListEntry other = (ListEntry) obj;
        if (type != other.type)
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }

    
    public String toString() {
        return "ListEntry [displayName=" + displayName + ", type=" + type
                + ", uri=" + uri + "]";
    }
}
