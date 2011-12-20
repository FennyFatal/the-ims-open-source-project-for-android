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

package javax.microedition.ims.android.core;

import android.os.IBinder;
import android.os.RemoteException;

import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.messages.history.BodyPartData;
import javax.microedition.ims.messages.history.MessageData;
import javax.microedition.ims.messages.wrappers.sip.Header;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class responsible for rpc communication
 *
 * @author ext-akhomush
 * @see Imessage.aidl
 */
public class MessageImpl extends IMessage.Stub {
    static class MethodId {
        /**
         * Identifier for the queryCapabilities method on the Capabilities interface.
         */
        static final int CAPABILITIES_QUERY = 1;

        /**
         * Identifier for the send method on the PageMessage interface.
         */
        static final int PAGEMESSAGE_SEND = 2;

        /**
         * Identifier for the publish method on the Publication interface.
         */
        static final int PUBLICATION_PUBLISH = 3;

        /**
         * Identifier for the unpublish method on the Publication interface.
         */
        static final int PUBLICATION_UNPUBLISH = 4;

        /**
         * Identifier for the REFER method on the Reference interface.
         */

        static final int REFERENCE_REFER = 5;
        /**
         * Identifier for the start method on the Session interface.
         */
        static final int SESSION_START = 6;

        /**
         * Identifier for the terminate method on the Session interface.
         */
        static final int SESSION_TERMINATE = 8;

        /**
         * Identifier for the reInvite method on the Session interface.
         */
        static final int SESSION_UPDATE = 7;

        /**
         * This state specifies that this Message was received from a remote endpoint.
         */
        static final int STATE_RECEIVED = 3;

        /**
         * This state specifies that the Message is sent from the local endpoint.
         */
        static final int STATE_SENT = 2;

        /**
         * This state specifies that the Message is unsent.
         */
        static final int STATE_UNSENT = 1;

        /**
         * Identifier for the poll method on the Subscription interface.
         */
        static final int SUBSCRIPTION_POLL = 11;

        /**
         * Identifier for the subscribe method on the Subscription interface.
         */
        static final int SUBSCRIPTION_SUBSCRIBE = 9;

        /**
         * Identifier for the unsubscribe method on the Subscription interface.
         */
        static final int SUBSCRIPTION_UNSUBSCRIBE = 10;

        private MethodId() {
            assert false;
        }
    }

    static final int STATE_RECEIVED = 10;
    static final int STATE_SENT = 11;
    static final int STATE_UNSENT = 12;

    static final int SUBSCRIPTION_POLL = 13;

    private final MessageData messageData;
    private final HeaderProcessor headerProcessor;

    private interface HeaderProcessor {
        void addHeader(String key, String value);

        String[] getHeaders(String key);
    }

    private class HeaderProcessorImpl implements HeaderProcessor {
        private final MessageData messageData;

        public HeaderProcessorImpl(MessageData messageData) {
            this.messageData = messageData;
        }


        public void addHeader(String key, String value) {
            messageData.addHeader(key, value);
        }


        public String[] getHeaders(String key) {
            return messageData.getHeaders(key);
        }
    }

    private class ContentTypeHeaderFilter implements HeaderProcessor {
        private final HeaderProcessor processor;

        public ContentTypeHeaderFilter(HeaderProcessor processor) {
            this.processor = processor;
        }

        public void addHeader(String key, String value) {
            if (Header.Content_Type.testAgainst(key)) {
                //can only be set on messages with more than one body part, and only to 'multipart/*'
                if (value != null && value.toLowerCase().startsWith(MimeType.MULTIPART.stringValue())) {
                    BodyPartData[] bodyParts = messageData.getBodyParts();
                    if (bodyParts.length > 1) {
                        processor.addHeader(key, value);
                    }
                }
            }
            else {
                processor.addHeader(key, value);
            }
        }

        public String[] getHeaders(String key) {
            return processor.getHeaders(key);
        }
    }

    private class HiddenHeaderFilter implements HeaderProcessor {

        private final List<Header> HEADERS_HIDDEN = Arrays.asList(
                Header.Authentication_Info,
                Header.Authorization,
                Header.Max_Forwards,
                Header.Min_Expires,
                Header.Proxy_Authenticate,
                Header.Proxy_Authorization,
                Header.RecordRoute,
                Header.Security_Client,
                Header.Security_Server,
                Header.Security_Verify,
                Header.ServiceRoutes,
                Header.Via
        );

        private final List<Header> HEADERS_HIDDEN_MODIFY = Arrays.asList(Header.AcceptContact,
                Header.Call_ID,
                Header.Contact,
                Header.Content_Length,
                Header.CSeq,
                Header.Event,
                Header.From,
                Header.PAccessNetwork,
                Header.PAssertedIdentities,
                Header.PAssociatedUris,
                Header.PPreferredIdentity,
                Header.RAck,
                Header.ReferTo,
                Header.Referred_By,
                Header.Replaces,
                Header.RSeq,
                Header.To,
                Header.WWW_Authenticate
        );

        private final HeaderProcessor processor;

        public HiddenHeaderFilter(HeaderProcessor processor) {
            this.processor = processor;
        }

        public void addHeader(final String key, String value) {
            CollectionsUtils.Predicate<Header> predicate = new CollectionsUtils.Predicate<Header>() {
                
                public boolean evaluate(Header header) {
                    return header.testAgainst(key);
                }
            };

            boolean isRestricted = CollectionsUtils.exists(HEADERS_HIDDEN, predicate) || CollectionsUtils.exists(HEADERS_HIDDEN_MODIFY, predicate);
            if (!isRestricted) {
                processor.addHeader(key, value);
            }
        }

        public String[] getHeaders(final String key) {
            boolean isRestricted = CollectionsUtils.exists(HEADERS_HIDDEN, new CollectionsUtils.Predicate<Header>() {

                public boolean evaluate(Header header) {
                    return header.testAgainst(key);
                }
            });

            return isRestricted ? new String[0] : processor.getHeaders(key);
        }
    }

    public MessageImpl(final MessageData messageData) {
        assert messageData != null;
        this.messageData = messageData;
        this.headerProcessor = new ContentTypeHeaderFilter(new HiddenHeaderFilter(new HeaderProcessorImpl(messageData)));
    }


    public void addHeader(String key, String value) throws RemoteException {
        headerProcessor.addHeader(key, value);
    }


    public String[] getHeaders(String key) throws RemoteException {
        return headerProcessor.getHeaders(key);
    }

    public IMessageBodyPart createBodyPart() throws RemoteException {
        return createBodyPartInternally();
    }

    protected MessageBodyPartImpl createBodyPartInternally() {
        BodyPartData bodyPartData = messageData.createBodyPart();
        return new MessageBodyPartImpl(bodyPartData);
    }

    public List<IBinder> getBodyParts() throws RemoteException {
        BodyPartData[] bodyParts = messageData.getBodyParts();
        List<IBinder> messageBodyParts = new ArrayList<IBinder>();
        for (BodyPartData bodyPartData : bodyParts) {
            messageBodyParts.add(new MessageBodyPartImpl(bodyPartData).asBinder());
        }
        return messageBodyParts;
    }

    protected BodyPartData getBodyPartInternally(int idx) {
        BodyPartData[] bodies = messageData.getBodyParts();
        return bodies.length > idx ? bodies[idx] : null;
    }


    public String getMethod() throws RemoteException {
        return messageData.getMethod();
    }


    public String getReasonPhrase() throws RemoteException {
        return messageData.getReasonPhrase();
    }


    public int getState() throws RemoteException {
        return messageData.getState();
    }


    public int getStatusCode() throws RemoteException {
        return messageData.getStatusCode();
    }

}
