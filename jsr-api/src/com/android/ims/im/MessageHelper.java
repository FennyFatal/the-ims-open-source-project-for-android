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

package com.android.ims.im;

import javax.microedition.ims.android.msrp.IContentPart;
import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.im.ContentPart;
import javax.microedition.ims.im.Message;

public class MessageHelper {

    public static IMessage convertToIMessage(final Message msg) {
        
        ContentPart[] contentParts = msg.getContentParts();
        IContentPart[] iContentParts = null;
        if (contentParts != null) {
            iContentParts = new IContentPart[contentParts.length];
            for(int i = 0; i < contentParts.length; i++) {
                iContentParts[i] = new IContentPart.IContentPartBuilder()
                        .content(contentParts[i].getContent())
                        .contentType(contentParts[i].getContentType())
                        .disposition(contentParts[i].getContentDisposition())
                        .build();
                
                assert iContentParts[i] != null;
            }
        }
        
        IMessage iMessage = new IMessage.IMessageBuilder()
                .sender(msg.getSender())
                .recipients(msg.getRecipients())
                .subject(msg.getSubject())
                .messageId(msg.getMessageId())
                .timestamp(msg.getTimestamp())
                .contentParts(iContentParts)
                .build();

        assert iMessage != null;
        return iMessage;
    }
    
    public static Message convertToMessage(final IMessage msg) {
        
        Message res = new Message(
                msg.getSender(),
                msg.getRecipients(),
                msg.getSubject()
        );

        //TODO: someone uploaded none-compilable code
        //res.setTimestamp(msg.getTimestamp());
        res.setMessageId(msg.getMessageId());
        
        for (IContentPart contentPart : msg.getContentParts()) {
            ContentPart part = new ContentPart(contentPart.getContent(), contentPart.getContentType());
            part.setContentDisposition(contentPart.getDisposition());
            res.addContentPart(part);
        }
        
        return res;
    }
    
}
