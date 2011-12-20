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

package javax.microedition.ims.android.msrp;

import android.os.Parcel;
import android.os.Parcelable;

public class IContentPart implements Parcelable {

    private byte[] content;
    private String contentType;
    private String disposition;

//    private ByteArrayOutputStream byteArrayOutputStream;

    public static final Parcelable.Creator<IContentPart> CREATOR = new Parcelable.Creator<IContentPart>() {
        public IContentPart createFromParcel(Parcel in) {
            return new IContentPart(in);
        }

        public IContentPart[] newArray(int size) {
            return new IContentPart[size];
        }
    };

    private IContentPart() {
    }

    IContentPart(Parcel in) {
        readFromParcel(in);
    }

    private IContentPart(final IContentPartBuilder messageBuilder) {
        this.content = messageBuilder.content;
        this.contentType = messageBuilder.contentType;
        this.disposition = messageBuilder.disposition;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        int contentInt = (content != null) ? content.length : -1;
        dest.writeInt(contentInt);
        if (contentInt > 0) {
            dest.writeByteArray(content);
        }

        dest.writeString(contentType);

        dest.writeString(disposition);
    }

    protected void readFromParcel(Parcel src) {
        int contentInt = src.readInt();
        if (contentInt > 0) {
            content = new byte[contentInt];
            src.readByteArray(content);
        }

        contentType = src.readString();

        disposition = src.readString();
    }


    public static class IContentPartBuilder {
        private byte[] content;
        private String contentType;
        private String disposition;

        public IContentPartBuilder content(byte[] content) {
            this.content = content;
            return this;
        }

        public IContentPartBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public IContentPartBuilder disposition(String disposition) {
            this.disposition = disposition;
            return this;
        }

        public IContentPart build() {
            return new IContentPart(this);
        }
    }


    public byte[] getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }

    public String getDisposition() {
        return disposition;
    }

}
