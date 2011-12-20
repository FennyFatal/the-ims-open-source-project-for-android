/*
 * This software code is � 2010 T-Mobile USA, Inc. All Rights Reserved.
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
 * THIS SOFTWARE IS PROVIDED ON AN �AS IS� AND �WITH ALL FAULTS� BASIS
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

package javax.microedition.ims;

import javax.microedition.ims.common.DefaultTimeoutUnit;
import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.TimeoutUnit;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.xdm.DummyXDMMessage;
import javax.microedition.ims.core.xdm.XDMMessage;
import javax.microedition.ims.transport.MessageContentProvider;
import javax.microedition.ims.transport.MessageReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 18-Feb-2010
 * Time: 15:41:14
 */
public class XdmMessageContext extends DefaultMessageContext<XDMMessage> {
    //private final StackContext context;

    public XdmMessageContext(final StackContext context) {
        super(
                new MessageContentProvider<XDMMessage>() {
                    
                    public String getContent(final XDMMessage msg) {
                        return null;
                    }

                    
                    public byte[] getByteContent(XDMMessage msg) {
                        assert false : "Shouldn't call this metjhod for this message";
                        return null;
                    }
                },
                new MessageReader<XDMMessage>() {
                    public void setMessageReceiver(MessageReceiver<XDMMessage> messageResolver) {
                    }

                    public void feedPart(byte[] part) throws IOException {
                    }

                    public void feedCompressedPart(byte[] part, boolean isStream) throws IOException {
                    }
                },
                new DummyXDMMessage()
        );
        //this.context = context;
    }

    
    public int getMessageRate() {
        return 30;
    }

    
    public XDMMessage buildServiceUnavailableMessage(final XDMMessage msg) {
        return new DummyXDMMessage();
    }

    public XDMMessage buildMessageTooLargeMessage(byte[] msg) {
        return new DummyXDMMessage();
    }

    public TimeoutUnit getMessageLifeTime() {
        return new DefaultTimeoutUnit(32, TimeUnit.SECONDS);
    }

    
    public int getMessageHash(final XDMMessage msg) {
        return msg.hashCode();
    }

    
    public IMSEntityType getEntityType() {
        return IMSEntityType.XDM;
    }
}
