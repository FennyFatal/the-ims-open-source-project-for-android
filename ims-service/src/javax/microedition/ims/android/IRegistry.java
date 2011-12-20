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

package javax.microedition.ims.android;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents clisent registry object.
 *
 * @author Andrei Khomushko
 */
public class IRegistry implements Parcelable {
    private final String appId;
    private final String classname;
    private final int qosLevel;
    private final String[][] properties;

    public static Parcelable.Creator<IRegistry> getCreator() {
        return CREATOR;
    }

    public static final Parcelable.Creator<IRegistry> CREATOR = new Parcelable.Creator<IRegistry>() {
        public IRegistry createFromParcel(Parcel in) {
            return IRegistry.createFromParcel(in);
        }

        public IRegistry[] newArray(int size) {
            return new IRegistry[size];
        }
    };

    public IRegistry(String appId, String classname, int qosLevel,
                     String[][] properties) {
        this.appId = appId;
        this.classname = classname;
        this.qosLevel = qosLevel;
        this.properties = properties;
    }

    public String getAppId() {
        return appId;
    }

    public String getClassname() {
        return classname;
    }

    public int getQosLevel() {
        return qosLevel;
    }

    public String[][] getProperties() {
        return properties;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appId);
        dest.writeString(classname);
        dest.writeInt(qosLevel);
        dest.writeInt(properties.length);
        for (String[] property : properties) {
            dest.writeInt(property.length);
            for (String value : property) {
                dest.writeString(value);
            }
        }
    }

    private static IRegistry createFromParcel(Parcel in) {
        String appId = in.readString();
        String classname = in.readString();
        int qosLevel = in.readInt();

        int propertiesCount = in.readInt();
        String[][] properties = new String[propertiesCount][];
        for (int i = 0; i < propertiesCount; i++) {
            int propertyLength = in.readInt();
            String[] property = new String[propertyLength];
            for (int j = 0; j < propertyLength; j++) {
                property[j] = in.readString();
            }
            properties[i] = property;
        }
        return new IRegistry(appId, classname, qosLevel, properties);
    }
}
