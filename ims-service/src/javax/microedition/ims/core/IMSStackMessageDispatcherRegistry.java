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

package javax.microedition.ims.core;


import javax.microedition.ims.common.Consumer;
import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.core.dispatcher.DispatcherConsumer;
import javax.microedition.ims.core.dispatcher.MessageDispatcher;
import javax.microedition.ims.core.dispatcher.MessageDispatcherRegistry;
import javax.microedition.ims.core.xdm.XDMMessage;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ext-plabada
 */
public class IMSStackMessageDispatcherRegistry implements MessageDispatcherRegistry, DispatcherConsumer<IMSMessage>, Shutdownable {

    private final Map<IMSEntityType, MessageDispatcher<? extends IMSMessage>> map =
            Collections.synchronizedMap(new HashMap<IMSEntityType, MessageDispatcher<? extends IMSMessage>>());

    private final AtomicBoolean done = new AtomicBoolean(false);

    public IMSStackMessageDispatcherRegistry() {
    }

    
    public void addDispatcher(final IMSEntityType type, final MessageDispatcher<? extends IMSMessage> messageDispatcher) {
        if (!done.get()) {
            map.put(type, messageDispatcher);
        }
    }

    
    public MessageDispatcher<? extends IMSMessage> getDispatcher(IMSEntityType type) {
        return map.get(type);
    }

    
    public DispatcherConsumer<IMSMessage> getDispatcherConsumer() {
        return this;
    }

    
    public Consumer<IMSMessage> getOuterConsumer() {
        return new Consumer<IMSMessage>() {
            
            public void push(IMSMessage msg) {
                final IMSEntityType type = msg.getEntityType();
                final MessageDispatcher<? extends IMSMessage> dispatcher = getDispatcher(type);

                switch (type) {
                    case SIP: {
                        //at this point we know for sure actual type of dispatcher
                        @SuppressWarnings({"unchecked"})
                        MessageDispatcher<BaseSipMessage> sipDispatcher = (MessageDispatcher<BaseSipMessage>) dispatcher;
                        BaseSipMessage sipMessage = (BaseSipMessage) msg;
                        sipDispatcher.getConsumer().getOuterConsumer().push(sipMessage);
                    }
                    break;


                    case MSRP: {
                        //at this point we know for sure actual type of dispatcher
                        @SuppressWarnings({"unchecked"})
                        MessageDispatcher<MsrpMessage> msrpDispatcher = (MessageDispatcher<MsrpMessage>) dispatcher;
                        MsrpMessage msrpMessage = (MsrpMessage) msg;
                        msrpDispatcher.getConsumer().getOuterConsumer().push(msrpMessage);
                    }
                    break;


                    default: {
                        assert false : "No support for " + type;
                    }
                    break;

                }
            }
        };
    }

    
    public Consumer<IMSMessage> getInnerConsumer() {
        return new Consumer<IMSMessage>() {
            
            public void push(IMSMessage msg) {
                final IMSEntityType type = msg.getEntityType();
                final MessageDispatcher<? extends IMSMessage> dispatcher = getDispatcher(type);

                switch (type) {
                    case SIP: {
                        //at this point we know for sure actual type of dispatcher
                        @SuppressWarnings({"unchecked"})
                        MessageDispatcher<BaseSipMessage> sipDispatcher = (MessageDispatcher<BaseSipMessage>) dispatcher;

                        BaseSipMessage sipMessage = (BaseSipMessage) msg;
                        sipDispatcher.getConsumer().getInnerConsumer().push(sipMessage);
                    }
                    break;

                    case MSRP: {
                        //at this point we know for sure actual type of dispatcher
                        @SuppressWarnings({"unchecked"})
                        MessageDispatcher<MsrpMessage> msrpDispatcher = (MessageDispatcher<MsrpMessage>) dispatcher;
                        MsrpMessage msrpMessage = (MsrpMessage) msg;
                        msrpDispatcher.getConsumer().getInnerConsumer().push(msrpMessage);

                    }
                    break;

                    case XDM: {
                        //at this point we know for sure actual type of dispatcher
                        @SuppressWarnings({"unchecked"})


                        MessageDispatcher<XDMMessage> xdmDispatcher = (MessageDispatcher<XDMMessage>) dispatcher;

                        XDMMessage xdmMessage = (XDMMessage) msg;
                        xdmDispatcher.getConsumer().getInnerConsumer().push(xdmMessage);
                    }
                    break;

                    default: {
                        assert false : "No support for " + type;
                    }
                    break;
                }
            }
        };
    }

    
    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            Map<IMSEntityType, MessageDispatcher<? extends IMSMessage>> mapCopy;

            synchronized (map) {
                mapCopy = new HashMap<IMSEntityType, MessageDispatcher<? extends IMSMessage>>(map);
                map.clear();
            }

            for (IMSEntityType imsEntityType : mapCopy.keySet()) {
                final MessageDispatcher<? extends IMSMessage> dispatcher = mapCopy.get(imsEntityType);
                if (dispatcher instanceof Shutdownable) {
                    Shutdownable shutdownable = (Shutdownable) dispatcher;
                    shutdownable.shutdown();
                }
            }
            mapCopy.clear();
        }
    }
}
