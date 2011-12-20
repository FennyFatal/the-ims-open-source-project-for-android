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

import com.android.ims.presence.ActivitiesValues;


/**
 * The Activities class defines constants that can be used by the PersonInfo
 * class. These constants are equivalent with the activities defined in
 * [RFC4480].
 * 
 * @author Andrei Khomushko
 * 
 */
public final class Activities {
    public static final String APPOINTMENT = ActivitiesValues.APPOINTMENT.getValue();
    public static final String AWAY = ActivitiesValues.AWAY.getValue();
    public static final String BREAKFAST = ActivitiesValues.BREAKFAST.getValue();
    public static final String BUSY = ActivitiesValues.BUSY.getValue();
    public static final String DINNER = ActivitiesValues.DINNER.getValue();
    public static final String HOLIDAY = ActivitiesValues.HOLIDAY.getValue();
    public static final String IN_TRANSIT = ActivitiesValues.IN_TRANSIT.getValue();
    public static final String LOOKING_FOR_WORK = ActivitiesValues.LOOKING_FOR_WORK.getValue();
    public static final String LUNCH = ActivitiesValues.LUNCH.getValue();
    public static final String MEAL = ActivitiesValues.MEAL.getValue();
    public static final String MEETING = ActivitiesValues.MEETING.getValue();
    public static final String ON_THE_PHONE = ActivitiesValues.ON_THE_PHONE.getValue();
    public static final String PERFORMANCE = ActivitiesValues.PERFORMANCE.getValue();
    public static final String PERMANENT_ABSENCE = ActivitiesValues.PERMANENT_ABSENCE.getValue();
    public static final String PLAYING = ActivitiesValues.PLAYING.getValue();
    public static final String PRESENTATION = ActivitiesValues.PRESENTATION.getValue();
    public static final String SHOPPING = ActivitiesValues.SHOPPING.getValue();
    public static final String SLEEPING = ActivitiesValues.SLEEPING.getValue();
    public static final String STEERING = ActivitiesValues.STEERING.getValue();
    public static final String TRAVEL = ActivitiesValues.TRAVEL.getValue();
    public static final String TV = ActivitiesValues.TV.getValue();
    public static final String UNKNOWN = ActivitiesValues.UNKNOWN.getValue();
    public static final String VACATION = ActivitiesValues.VACATION.getValue();
    public static final String WORKING = ActivitiesValues.WORKING.getValue();
    public static final String WORSHIP = ActivitiesValues.WORSHIP.getValue();
    
    private Activities() {
        assert false;
    }
}
