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

import com.android.ims.presence.Attachable;
import com.android.ims.presence.CivicAddressType;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.android.ims.presence.CivicAddressType.*;


/**
 * The GeographicalLocationInfo component models a person's or device's
 * geographical location. The GeographicalLocationInfo class is an
 * implementation of the GEOPRIV Location Object Format specified in [RFC4119].
 * 
 * @author Andrei Khomushko
 * 
 */
public class GeographicalLocationInfo {
    public static final int CIVIC_ADDRESS_ADDITIONAL_LOCATION_INFO = ADDITIONAL_LOCATION_INFO.getCode();
    public static final int CIVIC_ADDRESS_CITY = CITY.getCode();
    public static final int CIVIC_ADDRESS_CITY_DIVISION = CITY_DIVISION.getCode();
    public static final int CIVIC_ADDRESS_COUNTRY_CODE = COUNTRY_CODE.getCode();
    public static final int CIVIC_ADDRESS_COUNTRY = COUNTRY.getCode();
    public static final int CIVIC_ADDRESS_FLOOR = FLOOR.getCode();
    public static final int CIVIC_ADDRESS_HOUSE_NUMBER = HOUSE_NUMBER.getCode();
    public static final int CIVIC_ADDRESS_HOUSE_NUMBER_SUFFIX = HOUSE_NUMBER_SUFFIX.getCode();
    public static final int CIVIC_ADDRESS_LANDMARK = LANDMARK.getCode();
    public static final int CIVIC_ADDRESS_LEADING_STREET_DIRECTION = LEADING_STREET_DIRECTION.getCode();
    public static final int CIVIC_ADDRESS_NATIONAL_SUBDIVISION = NATIONAL_SUBDIVISION.getCode();
    public static final int CIVIC_ADDRESS_NEIGHBORHOOD = NEIGHBORHOOD.getCode();
    public static final int CIVIC_ADDRESS_POSTAL_CODE = POSTAL_CODE.getCode();
    public static final int CIVIC_ADDRESS_RESIDENCE = RESIDENCE.getCode();
    public static final int CIVIC_ADDRESS_STREET = STREET.getCode();
    public static final int CIVIC_ADDRESS_STREET_SUFFIX = STREET_SUFFIX.getCode();
    public static final int CIVIC_ADDRESS_TRAILING_STREET_SUFFIX = TRAILING_STREET_SUFFIX.getCode();

    private Point point;
    private Circle circle;
    private Date retentionExpires;
    // private Date bindTime;
    private boolean retransmissionAllowed;
    private final Map<Integer, String> civicAddresses = new HashMap<Integer, String>();
    private String method;
    
    private Attachable parent;

    /**
     * Returns the value of the civic address identifier of this
     * GeographicalLocationInfo.
     * 
     * @param identifier
     *            - the civic address identifier to get
     * @return the value of the civic address identifier or null if the value is
     *         not available
     * @throws IllegalArgumentException
     *             - if the identifier is invalid
     */
    public String getCivicAddress(int identifier) {
        if (!isIdentifierValid(identifier)) {
            throw new IllegalArgumentException("The identifier is invalid");
        }
        return civicAddresses.get(identifier);
    }

    private boolean isIdentifierValid(int identifier) {
        return CivicAddressType.isCodeValid(identifier);
    }

    /**
     * Adds a civic address element to this GeographicalLocationInfo. This will
     * replace any existing value for this identifier.
     * 
     * @param identifier
     * @param value
     * @throws IllegalArgumentException
     *             - if the identifier is invalid, or if the value is null
     */
    public void addCivicAddress(int identifier, String value) {
        if (!isIdentifierValid(identifier)) {
            throw new IllegalArgumentException("The identifier is invalid");
        }

        if (value == null) {
            throw new IllegalArgumentException("The value is null");
        }

        civicAddresses.put(identifier, value);
    }

    /**
     * Removes the civic address element with the given identifier from this
     * GeographicalLocationInfo.
     * 
     * @param identifier
     *            - the civic address identifier to remove
     * @throws IllegalArgumentException
     *             - if the identifier is invalid, or if no value has been set
     *             for this identifier
     */
    public void removeCivicAddress(int identifier) {
        if (!isIdentifierValid(identifier)) {
            throw new IllegalArgumentException("The identifier is invalid");
        }

        String deletedValue = civicAddresses.remove(identifier);

        if (deletedValue == null) {
            throw new IllegalArgumentException(
                    "No value has been set for this identifier");
        }
    }

    /**
     * Returns the coordinates for the circle of this GeographicalLocationInfo.
     * 
     * @return the coordinates or null if the coordinates are not available
     */
    public String getCircleCoordinates() {
        return circle != null ? circle.getCoordinates() : null;
    }

    /**
     * Sets the point of this GeographicalLocationInfo, including the Geography
     * Markup Language (GML) identifier, the Spartial Reference System (SRS)
     * name, and the coordinates. This will replace any existing point.
     * 
     * @param point
     *            - the point
     * @throws IllegalArgumentException
     *             - if point argument is null
     */
    public void setPoint(Point point) {
        if(point == null) {
            throw new IllegalArgumentException("the point argument can't be null");
        }

        this.point = point;
    }
    
    /**
     * Sets the circle of this GeographicalLocationInfo, including the Geography
     * Markup Language (GML) identifier, the Spartial Reference System (SRS)
     * name, and the coordinates and radius. This will replace any existing circle.
     * 
     * @param circle
     *            - the circle
     *            
     * @throws IllegalArgumentException
     *             - if the circle the arguments are null
     */
    public void setCircle(Circle circle) {
        if(circle == null) {
            throw new IllegalArgumentException("the circle argument can't be null");
        }

        this.circle = circle;
    }

    public void removePoint() {
        this.point = null;
    }
    
    public void removeCircle() {
        this.circle = null;
    }

    /**
     * Returns the point of this GeographicalLocationInfo .
     * 
     * @return the point or null if the point is not available
     */
    public Point getPoint() {
        return point;
    }

    /**
     * Returns the circle of this GeographicalLocationInfo .
     * 
     * @return the circle or null if the circle is not available
     */
    public Circle getCircle() {
        return circle;
    }
    
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Returns the time when retention of this GeographicalLocationInfo is no
     * longer allowed. If not set this value will default to 24 hours after the
     * time stamp of the PersonInfo or DeviceInfo that this
     * GeographicalLocationInfo is set on.
     * 
     * Note: The default value will not be properly returned if the time stamp
     * is not available on the PersonInfo or DeviceInfo that this
     * GeographicalLocationInfo is set on. If this is the case this method will
     * return null.
     * 
     * @return the expiration time or null
     */
    public Date getRetentionExpires() {
        Date retValue = null;

        if (retentionExpires != null) {
            retValue = retentionExpires;
        } else {
            Date timestamp;
            if ((timestamp = getTimestamp()) != null) {
                retValue = getDefRetentionExpires(timestamp);
            }   
        }
        return retValue;
    }

    private Date getTimestamp() {
        return parent != null? parent.getTimestamp(): null;
    }
    
    private static Date getDefRetentionExpires(Date bindTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    /**
     * Sets the time when retention of this GeographicalLocationInfo is no
     * longer allowed. If not set this value will default to 24 hours after the
     * time stamp of the PersonInfo or DeviceInfo that this
     * GeographicalLocationInfo is set on.
     * 
     * @param date
     *            - the time when retention expires
     * @throws IllegalArgumentException
     *             - if the date argument is null
     */
    public void setRetentionExpires(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date is null");
        }
        
        this.retentionExpires = date;
    }

    /**
     * Indicates whether retransmission of this GeographicalLocationInfo is
     * allowed or not. The default value is false.
     * 
     * @return true if retransmission is allowed, false otherwise
     */
    public boolean isRetransmissionAllowed() {
        return retransmissionAllowed;
    }

    /**
     * Sets a boolean that indicates whether retransmission of this
     * GeographicalLocationInfo is allowed or not. If not set this value will
     * default to false.
     * 
     * @param allowed
     *            - true if retransmission is allowed, false otherwise
     */
    public void setRetransmissionAllowed(boolean allowed) {
        this.retransmissionAllowed = allowed;
    }
    
    /**
     * Use this method for stamp of the PersonInfo or DeviceInfo.S
     * 
     * @param attachable
     */
    void setParent(Attachable attachable) {
        this.parent = attachable;
    }
    
    public boolean isCivicAddressesEmpty() {
        return civicAddresses.isEmpty();
    }

    @Override
    public String toString() {
        return "GeographicalLocationInfo [point=" + point + ", circle=" + circle
                + ", retentionExpires=" + retentionExpires + ", retransmissionAllowed="
                + retransmissionAllowed + ", civicAddresses=" + civicAddresses + ", method="
                + method + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((circle == null) ? 0 : circle.hashCode());
        result = prime * result + ((civicAddresses == null) ? 0 : civicAddresses.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((point == null) ? 0 : point.hashCode());
        result = prime * result + ((retentionExpires == null) ? 0 : retentionExpires.hashCode());
        result = prime * result + (retransmissionAllowed ? 1231 : 1237);
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
        GeographicalLocationInfo other = (GeographicalLocationInfo)obj;
        if (circle == null) {
            if (other.circle != null)
                return false;
        } else if (!circle.equals(other.circle))
            return false;
        if (civicAddresses == null) {
            if (other.civicAddresses != null)
                return false;
        } else if (!civicAddresses.equals(other.civicAddresses))
            return false;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        if (point == null) {
            if (other.point != null)
                return false;
        } else if (!point.equals(other.point))
            return false;
        if (retentionExpires == null) {
            if (other.retentionExpires != null)
                return false;
        } else if (!retentionExpires.equals(other.retentionExpires))
            return false;
        if (retransmissionAllowed != other.retransmissionAllowed)
            return false;
        return true;
    }
}
