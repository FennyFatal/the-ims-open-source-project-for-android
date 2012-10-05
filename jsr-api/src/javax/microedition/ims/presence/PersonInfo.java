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

import com.android.ims.presence.AttachHelper;
import com.android.ims.presence.Attachable;
import com.android.ims.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The PersonInfo component models the personal information about the presentity
 * that the presence data is trying to describe.
 *
 *
 * </p><p></p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 * @author Andrei Khomushko
 * 
 */
public class PersonInfo {
    public static final int TIME_OFFSET_UNSET = -1000;

    private final String id;
    
    /**
     * <time-offset> 
     */
    private int timeOffset = TIME_OFFSET_UNSET;

    /**
     *  <rp:class>auth-A</rp:class>
     */
    private String classification;
    
    /**
     * <person><note> 
     */
    private String freeText;
    private GeographicalLocationInfo locationInfo;
    
    /**
     * <person><status><overriding-willingness><basic>open/closed</basic></overriding-willingness></status></person>
     * 
     * <op:overriding-willingness>
     *  <op:basic>open</op:basic>
     * </op:overriding-willingness>
     */
    private PresenceState overridingWillingness = PresenceState.UNSET;
    
    private StatusIcon statusIcon;
    
    private final Attachable attachable = new AttachHelper();
    /**
     * <dm:timestamp>2010-07-29T09:41:32Z</dm:timestamp>
     */
    private Date timestamp;
    
    /**
     *  xmlns:rp="urn:ietf:params:xml:ns:pidf:rpid"
     *  <rp:activities>
                 <rp:vacation/>
        </rp:activities>
     */
    private List<String> activities = new ArrayList<String>();//
    /**
     * <r:mood><r:happy/></r:mood>
     */
    private List<String> moods = new ArrayList<String>();
    private List<String> placetypes = new ArrayList<String>();
    
    /**
     * Constructor for a new PersonInfo. This will create an uninitialized
     * PersonInfo that can be modified by the application. An identifier will be
     * added to the PersonInfo when it is created.
     */
    public PersonInfo() {
        this(Utils.generatePseudoUUID(), null, null, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(),
                null, TIME_OFFSET_UNSET, PresenceState.UNSET, null, null);
    }

    /**
     * 
     * @param id
     * @param freeText
     * @param statusIcon
     * 
     * @throws IllegalArgumentException
     */
    private PersonInfo(String id, String freeText, StatusIcon statusIcon, List<String> moods, 
            List<String> activities, List<String> placetypes,
            String classification, int timeOffset, PresenceState overridingWillingness, 
            Date timestamp, GeographicalLocationInfo locationInfo) {
        if(id == null || "".endsWith(id.trim())) {
            throw new IllegalArgumentException("The id argument is null");
        }
        this.id = id;
        this.freeText = freeText;
        this.statusIcon = statusIcon;
        this.moods = moods;
        this.activities = activities;
        this.placetypes = placetypes;
        this.classification = classification;
        this.timeOffset = timeOffset;
        this.overridingWillingness = overridingWillingness;
        this.timestamp = timestamp;
        this.locationInfo = locationInfo;
    }

    
    private PersonInfo(PersonInfoBuilder builder) {
        this(builder.id, builder.note, builder.statusIcon, builder.moods, builder.activities, builder.placetypes,
                builder.classification, builder.timeOffset, builder.overridingWillingness, 
                builder.timestamp, builder.locationInfo);
    }

    public String getIdentifier() {
        return id;
    }

    /**
     * Returns the classification of this PersonInfo.
     * 
     * @return the classification or null if the classification is not available
     */
    public String getClassification() {
        return classification;
    }

    /**
     * Sets the classification of this PersonInfo. A null value removes any
     * existing value. This can be used to group similar components and is not
     * generally presented to the watcher user interface.
     * 
     * @param classification
     *            - the classification or null
     * @throws IllegalArgumentException
     *             - if the classification argument is an empty string
     */
    public void setClassification(String classification) {

        if (Utils.isExactlyEmpty(classification)) {
            throw new IllegalArgumentException(
                    "The classification argument is an empty string");
        }
        this.classification = classification;
    }

    public String getFreeText() {
        return freeText;
    }

    public void setFreeText(String freeText) {
        if (Utils.isExactlyEmpty(freeText)) {
            throw new IllegalArgumentException(
                    "The freeText argument is an empty string");
        }
        this.freeText = freeText;
    }

    /**
     * Returns the time offset of this PersonInfo to UTC in minutes.
     * 
     * @return the time offset or TIME_OFFSET_UNSET if the time offset is not
     *         available
     */
    public int getTimeOffset() {
        return timeOffset;
    }

    /**
     * Sets the time offset for this PersonInfo to UTC in minutes.
     * TIME_OFFSET_UNSET can be used to remove any existing value.
     * 
     * @param timeOffset
     *            - the time offset in minutes or TIME_OFFSET_UNSET
     */
    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    /**
     * Returns the activities of this PersonInfo.
     * 
     * @return the activities or an empty array if no activities are available
     */
    public String[] getActivities() {
        return activities.toArray(new String[0]);
    }

    /**
     * Sets the activities for this PersonInfo. This will replace any existing
     * values. The set of activities defined by [RFC4480] are included in the
     * Activities class. If any other activity is used, it will be placed as a
     * textual node under an <other> element. A null value of the activities
     * argument removes all existing values.
     * 
     * @param activities
     *            - an array of activities or null
     * @throws IllegalArgumentException
     *             - if any of the elements in the activities argument is null
     *             or an empty string
     */
    public void setActivities(String[] activitiesToSet) {
        if (activitiesToSet != null) {
            for (String activity : activitiesToSet) {
                if (Utils.isEmpty(activity)) {
                    throw new IllegalArgumentException(
                            "The element in the activities argument is null or empty string");
                }
            }
            activities.clear();

            for (String activity : activitiesToSet) {
                activities.add(activity);
            }
        } else {
            activities.clear();
        }
    }

    /**
     * Returns the moods of this PersonInfo.
     * 
     * @return the moods or an empty array if no moods are available
     */
    public String[] getMoods() {
        return moods.toArray(new String[0]);
    }

    /**
     * Sets the moods for this PersonInfo. This will replace any existing
     * values.The set of moods defined by [RFC4480] are included in the Moods
     * class. If any other mood is used, it will be placed as a textual node
     * under an <other> element. A null value of the moods argument removes all
     * existing values.
     * 
     * @param moods
     *            - an array of moods or null
     * @throws IllegalArgumentException
     *             - if any of the elements in the moods argument is null or an
     *             empty string
     */
    public void setMoods(String[] moodsToSet) {
        if (moodsToSet != null) {
            for (String mood : moodsToSet) {
                if (Utils.isEmpty(mood)) {
                    throw new IllegalArgumentException(
                            "The element in the moods argument is null or empty string");
                }
            }
            moods.clear();

            for (String mood : moodsToSet) {
                moods.add(mood);
            }
        } else {
            moods.clear();
        }
    }

    /**
     * Returns a GeographicalLocationInfo representing the geographical location
     * of this PersonInfo. Changes made to the GeographicalLocationInfo object
     * will not take effect on the PersonInfo until
     * setGeographicalLocationInfo(GeographicalLocationInfo) is called.
     * 
     * @return the GeographicalLocationInfo or null if the geographical location
     *         is not available
     */
    public GeographicalLocationInfo getGeographicalLocationInfo() {
        return locationInfo;
    }

    /**
     * Sets a GeographicalLocationInfo for this PersonInfo. This will replace
     * any existing GeographicalLocationInfo. A null value removes any existing
     * value.
     * 
     * @param geographicalLocationInfo
     *            - the GeographicalLocationInfo to set or null
     */
    public void setGeographicalLocationInfo(
            GeographicalLocationInfo newGeographicalLocationInfo) {
        if(locationInfo != null) {
            locationInfo.setParent(null);
        }
        
        if(newGeographicalLocationInfo != null) {
            newGeographicalLocationInfo.setParent(attachable); 
        }
        
        this.locationInfo = newGeographicalLocationInfo;
    }
    
    /**
     * Returns the time when this PersonInfo was added to the PresenceDocument.
     * 
     * @return the Date or null if this PersonInfo has not been added to a
     *         PresenceDocument
     */
    public Date getTimestamp() {
        //return attachable.getTimestamp();
        return timestamp;
    }

    /**
     * Sets the overriding willingness of this PersonInfo. Setting the state
     * parameter to PresenceState.OPEN indicates that the user is willing to
     * communicate and to receive incoming communication requests with all
     * services. PresenceState.CLOSED indicates that the user does not want to
     * communicate with any services. The overriding willingness can be removed
     * by setting the state parameter to PresenceState.UNSET.
     * 
     * @param state
     *            - PresenceState.OPEN, PresenceState.CLOSED or
     *            PresenceState.UNSET
     * @throws IllegalArgumentException
     *             - if the state argument is null
     */
    public void setOverridingWillingness(PresenceState state) {
        if (state == null) {
            throw new IllegalArgumentException("The state argument is null");
        }
        this.overridingWillingness = state;
    }

    /**
     * Returns the overriding willingness of this PersonInfo. If the value is
     * PresenceState.OPEN or PresenceState.CLOSED, this overrides the
     * willingness setting for all ServiceInfo components in the presence
     * document. Note: If the overriding willingness of this PersonInfo is set
     * to PresenceState.UNSET, the application should use getWillingness in the
     * specific ServiceInfo instead.
     * 
     * @return PresenceState.OPEN, PresenceState.CLOSED or PresenceState.UNSET
     */
    public PresenceState getOverridingWillingness() {
        return overridingWillingness;
    }

    /**
     * Sets the place types for this PersonInfo. This will replace any existing
     * values. The set of place types defined by [RFC4480] are included in the
     * Placetypes class. If any other place type is used, it will be placed as a
     * textual node under an <other> element. A null value of the placetypes
     * argument removes all existing values.
     * 
     * @param placetypes
     *            - an array of place types or null
     * @throws IllegalArgumentException
     *             - if any of the elements in the placetypes argument is null
     *             or an empty string
     */
    public void setPlacetypes(String[] placetypesToSet) {
        if (placetypesToSet != null) {
            for (String placeType : placetypesToSet) {
                if (Utils.isEmpty(placeType)) {
                    throw new IllegalArgumentException(
                            "The element in the moods argument is null or empty string");
                }
            }
            placetypes.clear();

            for (String placeType : placetypesToSet) {
                placetypes.add(placeType);
            }
        } else {
            placetypes.clear();
        }
    }

    /**
     * Returns the place types of this PersonInfo.
     * 
     * @return the place types or an empty array if no place types are available
     */
    public String[] getPlacetypes() {
        return placetypes.toArray(new String[0]);
    }

    /**
     * Returns the StatusIcon of this PersonInfo.
     * 
     * @return the StatusIcon or null if the status icon is not available
     */
    public StatusIcon getStatusIcon() {
        return statusIcon;
    }

    /**
     * Sets a StatusIcon for this PersonInfo. This will replace any existing
     * StatusIcon. A null value removes any existing value.
     * 
     * @param statusIcon
     *            - the StatusIcon to set or null
     */
    public void setStatusIcon(StatusIcon statusIcon) {
        this.statusIcon = statusIcon;
    }
    
    public Attachable getAttachable() {
        return attachable;
    }
    
    public static class PersonInfoBuilder {
        private String id;
        private StatusIcon statusIcon;
        private String note;
        private ArrayList<String> moods;
        private ArrayList<String> activities;
        private ArrayList<String> placetypes;
        private String classification;
        private int timeOffset;
        private PresenceState overridingWillingness;
        private Date timestamp;
        private GeographicalLocationInfo locationInfo;
        
        public PersonInfoBuilder buildId(String id) {
            this.id = id;
            return this;
        }
        
        public PersonInfoBuilder buildStatusIcon(StatusIcon statusIcon) {
            this.statusIcon = statusIcon;
            return this;
        }
        
        public PersonInfoBuilder buildNote(String note) {
            this.note = note;
            return this;
        }
        
        public PersonInfoBuilder buildMoods(ArrayList<String> moods) {
            this.moods = moods;
            return this;
        }
        
        public PersonInfoBuilder buildActivities(ArrayList<String> activities) {
            this.activities = activities;
            return this;
        }
        
        public PersonInfoBuilder buildPlacetypes(ArrayList<String> placetypes) {
            this.placetypes = placetypes;
            return this;
        }
        
        public PersonInfoBuilder buildClassification(String classification) {
            this.classification = classification;
            return this;
        }
        
        public PersonInfoBuilder buildTimeOffset(int timeOffset) {
            this.timeOffset = timeOffset;
            return this;
        }
        
        public PersonInfoBuilder buildOverridingWillingness(PresenceState overridingWillingness) {
            this.overridingWillingness = overridingWillingness;
            return this;
        }
        
        public PersonInfoBuilder buildTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public PersonInfoBuilder buildGeographicalLocationInfo(GeographicalLocationInfo locationInfo) {
            this.locationInfo = locationInfo;
            return this;
        }
        
        /**
         * Build PersonInfo instance.
         * @return PersonInfo instance.
         * 
         * @throws IllegalArgumentException - if instance can't be created.  
         */
        public PersonInfo build() {
            return new PersonInfo(this);
        }
    }
    
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((activities == null) ? 0 : activities.hashCode());
        result = prime * result
                + ((classification == null) ? 0 : classification.hashCode());
        result = prime * result
                + ((freeText == null) ? 0 : freeText.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((locationInfo == null) ? 0 : locationInfo.hashCode());
        result = prime * result + ((moods == null) ? 0 : moods.hashCode());
        result = prime
                * result
                + ((overridingWillingness == null) ? 0 : overridingWillingness
                        .hashCode());
        result = prime * result
                + ((placetypes == null) ? 0 : placetypes.hashCode());
        result = prime * result
                + ((statusIcon == null) ? 0 : statusIcon.hashCode());
        result = prime * result + timeOffset;
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PersonInfo other = (PersonInfo) obj;
        if (activities == null) {
            if (other.activities != null)
                return false;
        } else if (!activities.equals(other.activities))
            return false;
        if (classification == null) {
            if (other.classification != null)
                return false;
        } else if (!classification.equals(other.classification))
            return false;
        if (freeText == null) {
            if (other.freeText != null)
                return false;
        } else if (!freeText.equals(other.freeText))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (locationInfo == null) {
            if (other.locationInfo != null)
                return false;
        } else if (!locationInfo.equals(other.locationInfo))
            return false;
        if (moods == null) {
            if (other.moods != null)
                return false;
        } else if (!moods.equals(other.moods))
            return false;
        if (overridingWillingness == null) {
            if (other.overridingWillingness != null)
                return false;
        } else if (!overridingWillingness.equals(other.overridingWillingness))
            return false;
        if (placetypes == null) {
            if (other.placetypes != null)
                return false;
        } else if (!placetypes.equals(other.placetypes))
            return false;
        if (statusIcon == null) {
            if (other.statusIcon != null)
                return false;
        } else if (!statusIcon.equals(other.statusIcon))
            return false;
        if (timeOffset != other.timeOffset)
            return false;
        return true;
    }

    
    public String toString() {
        return "PersonInfo [id=" + id + ", timeOffset=" + timeOffset
                + ", classification=" + classification + ", freeText="
                + freeText + ", locationInfo=" + locationInfo
                + ", overridingWillingness=" + overridingWillingness
                + ", statusIcon=" + statusIcon + ", timestamp=" + timestamp
                + ", activities=" + activities + ", moods=" + moods
                + ", placetypes=" + placetypes + "]";
    }
}
