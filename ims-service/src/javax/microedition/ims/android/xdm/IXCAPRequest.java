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

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data transfer bean for XDMRequest.
 *
 * @author Andrei Khomushko
 */
public class IXCAPRequest implements Parcelable {
    private int method;
    private String URI;
    private String body;
    private ContentValues headers = new ContentValues();

    public static final Parcelable.Creator<IXCAPRequest> CREATOR = new Parcelable.Creator<IXCAPRequest>() {
        public IXCAPRequest createFromParcel(Parcel in) {
            return new IXCAPRequest(in);
        }

        public IXCAPRequest[] newArray(int size) {
            return new IXCAPRequest[size];
        }
    };

    public static Parcelable.Creator<IXCAPRequest> getCreator() {
        return CREATOR;
    }

    public IXCAPRequest() {
    }

    public IXCAPRequest(int method, String uRI, String body,
                        Map<String, String> mHeaders) {
        this.method = method;
        this.URI = uRI;
        this.body = body;

        for (Entry<String, String> entry : mHeaders.entrySet()) {
            headers.put(entry.getKey(), entry.getValue());
        }
    }

    private IXCAPRequest(Parcel in) {
        readFromParcel(in);
    }

    public int getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public String getHeader(String key) {
        return headers.getAsString(key);
    }

    public Map<String, String> getHeaders() {
        Map<String, String> values = new HashMap<String, String>();

        for (Entry<String, Object> entry : headers.valueSet()) {
            String entryValue = entry.getValue() == null ? null : entry.getValue().toString();
            values.put(entry.getKey(), entryValue);
        }

        return values;
    }

    public String getURI() {
        return URI;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(method);
        dest.writeString(URI);
        dest.writeString(body);

        dest.writeParcelable(headers, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

/*        dest.writeInt(headers.size());
        for (String key: headers.keySet()) {
            dest.writeString(key);
            dest.writeString(headers.get(key));
        }        
*/
    }

    protected void readFromParcel(Parcel src) {
        method = src.readInt();
        URI = src.readString();
        body = src.readString();

        headers = src.readParcelable(getClass().getClassLoader());

/*        int count = src.readInt();
        for (int i = 0; i < count; i++) {
            headers.put(src.readString(), src.readString());
        }
*/
    }

    
    public String toString() {
        return "IXCAPRequest [URI=" + URI + ", body=" + body + ", headers="
                + headers + ", method=" + method + "]";
    }
}
