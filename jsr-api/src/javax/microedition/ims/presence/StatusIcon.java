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

package javax.microedition.ims.presence;

/**
 * The StatusIcon class provide metadata about a status icon that is stored on
 * the server. The StatusIcon class can be used to provide an URI to a status
 * icon in the presence document. The status icon is not included in the
 * presence document and must be retrieved from the URI that is specified. An
 * ETag can also be specified to indicate if the status icon has been updated.
 * 
 * Examples: Here are a few examples that show what the URI can look like:
 * <ul>
 * <li>"http://example.org/my-icons/busy"</li>
 * <li>"/org.openmobilealliance.pres-content/users/sip:hermione.blossom@example.org/oma_statusicon/icon_document"
 * </li>
 * </ul>
 * 
 * The status icon of the first URI can be retrieved by simply connecting to the
 * URI with HTTP. The second URI specifies an XDM document that is stored on a
 * Presence Content XDMS server, which can be retrieved by using the XDM
 * enabler.
 * 
 * @author Andrei Khomushko
 * 
 */
public class StatusIcon {
    private final String uri;
    private final String contentType;
    private final int size;
    private final String resolution;
    private final String etag;

    /**
     * Constructor for a new StatusIcon.
     * 
     * @param uri
     * @param contentType
     * @param size
     * @param resolution
     * @param etag
     * 
     * @throws IllegalArgumentException
     *             - if the uri argument is null
     */
    public StatusIcon(String uri, String contentType, int size,
            String resolution, String etag) {
        this.uri = uri;
        this.contentType = contentType;
        this.size = size;
        this.resolution = resolution;
        this.etag = etag;

        if (uri == null) {
            throw new IllegalArgumentException("The uri argument is null");
        }
    }

    public String getUri() {
        return uri;
    }

    public String getContentType() {
        return contentType;
    }

    public int getSize() {
        return size;
    }

    public String getResolution() {
        return resolution;
    }

    public String getEtag() {
        return etag;
    }

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((etag == null) ? 0 : etag.hashCode());
        result = prime * result
                + ((resolution == null) ? 0 : resolution.hashCode());
        result = prime * result + size;
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
        StatusIcon other = (StatusIcon) obj;
        if (contentType == null) {
            if (other.contentType != null)
                return false;
        } else if (!contentType.equals(other.contentType))
            return false;
        if (etag == null) {
            if (other.etag != null)
                return false;
        } else if (!etag.equals(other.etag))
            return false;
        if (resolution == null) {
            if (other.resolution != null)
                return false;
        } else if (!resolution.equals(other.resolution))
            return false;
        if (size != other.size)
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }

    
    public String toString() {
        return "StatusIcon [contentType=" + contentType + ", etag=" + etag
                + ", resolution=" + resolution + ", size=" + size + ", uri="
                + uri + "]";
    }
}
