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

import java.util.Date;


public class IMessage implements Parcelable {

    private String sender;
    private String[] recipients;
    private String subject;

    private String messageId;
    private Date timestamp;

    private IContentPart[] contentParts;

    public static final Parcelable.Creator<IMessage> CREATOR = new Parcelable.Creator<IMessage>() {
        public IMessage createFromParcel(Parcel in) {
            return new IMessage(in);
        }

        public IMessage[] newArray(int size) {
            return new IMessage[size];
        }
    };

    private IMessage() {
    }

    private IMessage(Parcel in) {
        readFromParcel(in);
    }

    private IMessage(final IMessageBuilder messageBuilder) {
        this.sender = messageBuilder.sender;
        this.recipients = messageBuilder.recipients;
        this.subject = messageBuilder.subject;
        this.messageId = messageBuilder.messageId;
        this.timestamp = messageBuilder.timestamp;
        this.contentParts = messageBuilder.contentParts;
    }

    
    public int describeContents() {
        return 0;
    }

    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);

        int recipientsInt = (recipients != null) ? recipients.length : -1;
        dest.writeInt(recipientsInt);
        if (recipientsInt > 0) {
            dest.writeStringArray(recipients);
        }

        dest.writeString(subject);

        dest.writeString(messageId);

        long timeLong = -1;
        if (timestamp != null) {
            timeLong = timestamp.getTime();
        }
        dest.writeLong(timeLong);

        int contentPartsInt = (contentParts != null) ? contentParts.length : -1;
        dest.writeInt(contentPartsInt);
        if (contentPartsInt > 0) {
            for (IContentPart contentPart : contentParts) {
                contentPart.writeToParcel(dest, flags);
            }
        }
    }

    protected void readFromParcel(Parcel src) {
        sender = src.readString();

        int recipientsInt = src.readInt();
        if (recipientsInt > 0) {
            recipients = new String[recipientsInt];
            src.readStringArray(recipients);
        }

        subject = src.readString();

        messageId = src.readString();

        long timeLong = src.readLong();
        if (timeLong != -1) {
            timestamp = new Date(timeLong);
        }

        int contentPartsInt = src.readInt();
        if (contentPartsInt > 0) {
            contentParts = new IContentPart[contentPartsInt];
            for (int i = 0; i < contentPartsInt; i++) {
                contentParts[i] = new IContentPart(src);
            }
        }
    }


    public static class IMessageBuilder {
        private String sender;
        private String[] recipients;
        private String subject;

        private String messageId;
        private Date timestamp;

        private IContentPart[] contentParts;

        public IMessageBuilder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public IMessageBuilder recipients(String[] recipients) {
            this.recipients = recipients;
            return this;
        }

        public IMessageBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public IMessageBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public IMessageBuilder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public IMessageBuilder contentParts(IContentPart[] contentParts) {
            this.contentParts = contentParts;
            return this;
        }

        public IMessage build() {
            return new IMessage(this);
        }
    }


    public String getSender() {
        return sender;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessageId() {
        return messageId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public IContentPart[] getContentParts() {
        return contentParts;
    }

}
