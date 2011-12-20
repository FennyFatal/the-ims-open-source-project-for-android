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

package javax.microedition.ims.android.xdm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer bean for URIList.
 *
 * @author Andrei Khomushko
 */
public class IURIList implements Parcelable {
    private String listName;
    private String displayName;
    private List<IListEntry> listEntries = new ArrayList<IListEntry>();

    public static final Parcelable.Creator<IURIList> CREATOR = new Parcelable.Creator<IURIList>() {
        public IURIList createFromParcel(Parcel in) {
            return new IURIList(in);
        }

        public IURIList[] newArray(int size) {
            return new IURIList[size];
        }
    };

    public IURIList() {
    }

    private IURIList(Parcel in) {
        readFromParcel(in);
    }

    public IURIList(String listName, String displayName) {
        this.listName = listName;
        this.displayName = displayName;
    }

    public static Parcelable.Creator<IURIList> getCreator() {
        return CREATOR;
    }

    public String getListName() {
        return listName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<IListEntry> getListEntries() {
        return listEntries;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(listName);
        dest.writeString(displayName);

        dest.writeInt(listEntries == null ? -1 : listEntries.size());
        for (IListEntry listEntry : listEntries) {
            listEntry.writeToParcel(dest, flags);
        }
    }

    protected void readFromParcel(Parcel src) {
        listName = src.readString();
        displayName = src.readString();

        int entriesSize = src.readInt();
        for (int i = 0; i < entriesSize; i++) {
            listEntries.add(IListEntry.CREATOR.createFromParcel(src));
        }
    }

    
    public String toString() {
        return "IURIList [displayName=" + displayName + ", listEntries="
                + listEntries + ", listName=" + listName + "]";
    }
}
