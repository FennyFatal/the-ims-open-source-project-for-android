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

public class Point {
    private final String gmlId;
    private final String srsName;
    private final String coordinates;

    /**
     * Create the point including the Geography
     * Markup Language (GML) identifier, the Spartial Reference System (SRS)
     * name, and the coordinates. This will replace any existing point.
     * 
     * @param gmlId
     *            - the GML identity
     * @param srsName
     *            - the SRS name
     * @param coordinates
     *            - the coordinates
     * @throws IllegalArgumentException
     *             - if any of the arguments are null
     */
    public Point(String gmlId, String srsName, String coordinates) {
        if (gmlId == null) {
            throw new IllegalArgumentException("The gmlId is null");
        }

        if (srsName == null) {
            throw new IllegalArgumentException("The srsName is null");
        }

        if (coordinates == null) {
            throw new IllegalArgumentException("The coordinates is null");
        }

        this.gmlId = gmlId;
        this.srsName = srsName;
        this.coordinates = coordinates;
    }
    
    public String getGmlId() {
        return gmlId;
    }

    public String getSrsName() {
        return srsName;
    }

    public String getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "Point [gmlId=" + gmlId + ", srsName=" + srsName
                + ", coordinates=" + coordinates + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((coordinates == null) ? 0 : coordinates.hashCode());
        result = prime * result + ((gmlId == null) ? 0 : gmlId.hashCode());
        result = prime * result
                + ((srsName == null) ? 0 : srsName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point other = (Point) obj;
        if (coordinates == null) {
            if (other.coordinates != null)
                return false;
        } else if (!coordinates.equals(other.coordinates))
            return false;
        if (gmlId == null) {
            if (other.gmlId != null)
                return false;
        } else if (!gmlId.equals(other.gmlId))
            return false;
        if (srsName == null) {
            if (other.srsName != null)
                return false;
        } else if (!srsName.equals(other.srsName))
            return false;
        return true;
    }
}
