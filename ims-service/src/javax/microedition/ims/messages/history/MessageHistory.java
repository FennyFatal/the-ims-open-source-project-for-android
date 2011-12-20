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

package javax.microedition.ims.messages.history;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.common.ResponseClass;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Request;
import javax.microedition.ims.messages.wrappers.sip.Response;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;
import javax.microedition.ims.util.MessageUtilHolder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class stores message history for current SESSION and provides ways to manipulate client requests and responses
 *
 * @author ext-achirko
 */
public class MessageHistory implements Shutdownable {
    private static final String LOG_TAG = "MessageHistory";
    private static final String MESSAGE_HISTORY_ALREADY_SHUTDOWN_MSG = "Message history already shutdown.";
    private static final int ELEMENTS_CAPACITY = 20;

    private final Collection<RequestHistoryWrapper> requests;
    private final Collection<ResponseHistoryWrapper> responses;
    private Uri remoteContact;
    private MessageAddedListener messageAddedListener;


    private BaseSipMessage lastMessage, firstMessage, lastInMessage;

    private AtomicReference<MessageDataImpl> nextRequestMessage = new AtomicReference<MessageDataImpl>(null);
    private AtomicReference<MessageDataImpl> nextResponseMessage = new AtomicReference<MessageDataImpl>(null);

    private final AtomicBoolean isDone = new AtomicBoolean(false);
    
    private static class BoundedLinkedList<E> extends LinkedList<E>{
        private static final long serialVersionUID = 1L;
        private final int elementCount;
        
        private BoundedLinkedList(int elementCount) {
            this.elementCount = elementCount;
        }
        
        public boolean add(E e) {
            ensureCapacity();
            return super.add(e);
        }

        public void add(int index, E element) {
            ensureCapacity();
            
            super.add(index, element);
        };
        
        public void addFirst(E object) {
            ensureCapacity();
            
            super.addFirst(object);
        };
        
        public void addLast(E object) {
            ensureCapacity();
            
            super.addLast(object);
        };
        
        private void ensureCapacity() {
            if(size() == elementCount) {
                removeFirst();
            }
        };
    }


    /**
     * Constructor
     *
     * @param remoteParty
     */
    public MessageHistory() {
        requests = Collections.synchronizedList(new BoundedLinkedList<RequestHistoryWrapper>(ELEMENTS_CAPACITY));
        responses = Collections.synchronizedList(new BoundedLinkedList<ResponseHistoryWrapper>(ELEMENTS_CAPACITY));
        Logger.log(LOG_TAG, "init#" + this);
    }


    public BaseSipMessage getLastMessage() {
        return lastMessage;
    }

    public BaseSipMessage getLastInMessage() {
        return lastInMessage;
    }


    public Request getFirstMessage() {
        return (Request) firstMessage;
    }

    public Uri getRemoteContact() {
        return remoteContact;
    }

    /**
     * Adds message to history
     *
     * @param msg - message to add
     */
    public void addMessage(final BaseSipMessage msg, boolean income) {
        if (!isDone.get()) {
            if (msg == null) {
                throw new IllegalArgumentException("Not allowed to add null messages");
            }
            if (firstMessage == null) {
                firstMessage = msg;
            }
            if (income) {
                updateContact(msg);
                lastInMessage = msg;
                notifyMessageAddedListener(msg);
            }

            lastMessage = msg;

            if (msg instanceof Response) {
                responses.add(new ResponseHistoryWrapper((Response) msg, income));
            }
            else {
                requests.add(new RequestHistoryWrapper((Request) msg, income));
            }
            Logger.log(Logger.Tag.MESSAGE_HISTORY, "Message being added to history: " + msg.shortDescription());
        }
        else{
            throwIllegalState();
        }
    }


    private void updateContact(final BaseSipMessage msg) {
        if (!isRedirectResponse(msg)) {
            if (msg.getContacts() != null &&
                    msg.getContacts().getContactsList() != null &&
                    msg.getContacts().getContactsList().size() > 0) {
                Logger.log("RemoteContact start changing");

                final List<UriHeader> uriHeaders = new ArrayList<UriHeader>(msg.getContacts().getContactsList());
                remoteContact = uriHeaders.get(0).getUri();
                Logger.log("RemoteContact has changed to " + remoteContact.buildContent());
            }
        }
    }

    private static boolean isRedirectResponse(BaseSipMessage message) {
        boolean isRedirect = false;
        if (message instanceof Response) {
            int statusCode = ((Response) message).getStatusCode();
            isRedirect = (statusCode == StatusCode.MOVED_PERMANENTLY || statusCode == StatusCode.MOVED_TEMPORARILY);
        }
        return isRedirect;
    }

    public boolean hasDuplicateMessage(final BaseSipMessage msg) {
        boolean retValue = false;

        if (!isDone.get()) {
            Collection<? extends HistoryMessageWrapper> listCopy;

            listCopy = msg instanceof Request ?
                    copyCollection(requests, requests, false) :
                    copyCollection(responses, responses, false);

            retValue = false;

            for (HistoryMessageWrapper currentMessage : listCopy) {
                if (MessageUtilHolder.getSIPMessageUtil().isMessagesEqual(msg, currentMessage.getMessage())) {
                    retValue = true;
                    break;
                }
            }
        }
        else{
            throwIllegalState();
        }

        return retValue;
    }


    /**
     * Returns previous request by method name
     *
     * @param method - method name
     * @return - MessageData wrapper if request was found,  otherwise - null
     */
    public MessageData findPreviousRequestByMethod(MessageType method) {
        MessageData retValue = null;

        if (!isDone.get()) {
            Collection<RequestHistoryWrapper> requestsCopyInreverseOrder = copyCollection(requests, requests, true);

            if (method != null && requestsCopyInreverseOrder.size() > 0) {

                for (RequestHistoryWrapper requestWrapper : requestsCopyInreverseOrder) {
                    if (method == MessageType.parse(requestWrapper.getMessage().getMethod())) {
                        retValue = MessageDataBuilder.buildMessageData(requestWrapper.getMessage(), requestWrapper.isIncome());
                        break;
                    }
                }

            }
        }
        else{
            throwIllegalState();
        }

        return retValue;
    }

    public Response findLastRingingResponse() {

        Response retValue = null;

        if (!isDone.get()) {
            Collection<ResponseHistoryWrapper> responsesCopyInReverseOrder = copyCollection(responses, responses, true);

            retValue = null;
            for (ResponseHistoryWrapper responseWrapper : responsesCopyInReverseOrder) {
                if (responseWrapper.getMessage().getResponseClass() == ResponseClass.Informational &&
                        responseWrapper.getMessage().getStatusCode() != 100) {

                    retValue = responseWrapper.getMessage();
                    break;
                }
            }
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }

    public Response findLastResponse() {
        Response retValue = null;

        if (!isDone.get()) {
            List<ResponseHistoryWrapper> responsesCopy = copyCollectionAsList(responses, responses, false);

            if (responsesCopy.size() != 0) {
                retValue = responsesCopy.get(responsesCopy.size() - 1).getMessage();
            }
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }

    /**
     * Returns previous request by method name
     *
     * @param method - method name
     * @return - MessageData wrapper if request was found,  otherwise - null
     */
    public Request findLastRequestByMethod(final MessageType method) {

        Request retValue = null;

        if (!isDone.get()) {
            Collection<RequestHistoryWrapper> requestsCopyInReverseOrder = copyCollection(requests, requests, true);

            retValue = null;
            if (method != null && requestsCopyInReverseOrder.size() > 0) {
                for (RequestHistoryWrapper requestWrapper : requestsCopyInReverseOrder) {
                    final MessageType candidateMethod = MessageType.parse(requestWrapper.getMessage().getMethod());
                    if (method == candidateMethod) {
                        retValue = requestWrapper.getMessage();
                        break;
                    }
                }
            }
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }

    /**
     * Returns previous response by method name
     *
     * @param method - method name
     * @return - MessageData wrapper if request was found,  otherwise - null
     */
    public Response findLastResponseByMethod(final MessageType method) {

        Response retValue = null;

        if (!isDone.get()) {
            Collection<ResponseHistoryWrapper> responsesCopyInreverseOrder = copyCollection(responses, responses, true);

            retValue = null;
            if (method != null && responsesCopyInreverseOrder.size() > 0) {
                for (ResponseHistoryWrapper responseWrapper : responsesCopyInreverseOrder) {

                    final MessageType candidateMethod = MessageType.parse(responseWrapper.getMessage().getMethod());

                    if (method == candidateMethod) {
                        retValue = responseWrapper.getMessage();
                        break;
                    }
                }
            }
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }

    /**
     * @param request - Prack message
     * @return pracked provisional response or null if none found.
     */
    public Response findPrackAddressee(final Request request) {
        Response retValue = null;

        if (!isDone.get()) {
            Collection<ResponseHistoryWrapper> responsesCopy = copyCollection(responses, responses, false);
            retValue = doFindPrackAddressee(request, responsesCopy);
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }

    private Response doFindPrackAddressee(
            final Request request,
            final Collection<ResponseHistoryWrapper> responsesCopy) {

        if (request == null) {
            throw new IllegalArgumentException("null value not allowed here. How request is " + request);
        }

        if (MessageType.SIP_PRACK != MessageType.parse(request.getMethod())) {
            final String errMsg = "Message must be of '" + MessageType.SIP_PRACK.stringValue() +
                    "' now it has '" + request.getMethod() + "' value.";
            throw new IllegalArgumentException(errMsg);
        }

        Response retValue = null;

        if (responsesCopy.size() > 0) {

            //request.getCustomHeader("RAck").get(0).replaceAll("\\s+\\d+\\s+\\w+$", "_")
            final List<String> rackList = request.getCustomHeader("RAck");

            if (rackList != null && rackList.size() > 0) {

                assert rackList.size() == 1 : "too many RAck headers in PRACK message. " + rackList.size();

                final String[] rackParts = rackList.get(0).trim().split("\\s+");

                if (rackParts != null && rackParts.length >= 3) {

                    //here we get RAck header parts: Rseq, Cseq and Method
                    String prackedRSeq = rackParts[0];
                    String prackedCSeq = rackParts[1];
                    String prackedType = rackParts[2];

                    //then we iterate over all messages in order to find message with the same Rseq, Cseq and Method
                    for (ResponseHistoryWrapper responseWrapper : responsesCopy) {

                        final Response message = responseWrapper.getMessage();
                        final int statusCode = message.getStatusCode();

                        if (statusCode >= 100 && statusCode < 200) {
                            final List<String> rseqList = message.getCustomHeader("RSeq");
                            if (rseqList != null && rseqList.size() > 0) {
                                assert rseqList.size() == 1 : "To many RSeq fields";

                                String msgRSeq = rseqList.get(0);
                                String msgType = message.getMethod();
                                String msgCSeq = Integer.toString(message.getcSeq());

                                if (msgRSeq.equalsIgnoreCase(prackedRSeq) &&
                                        msgCSeq.equalsIgnoreCase(prackedCSeq) &&
                                        msgType.equalsIgnoreCase(prackedType)) {
                                    retValue = message;
                                    break;
                                }
                            }
                        }
                    }
                }
                else {
                    assert false : "RAck field is malformed";
                }
            }
            else {
                assert false : "Prack message doesn't contain RAck header";
            }
        }

        return retValue;
    }

    /**
     * Returns previous responses by method name
     *
     * @param typeToFind - method name
     * @return - MessageData array if responses were found,  otherwise - null
     */
    public MessageData[] findPreviousResponsesByMethod(final MessageType typeToFind) {

        MessageDataImpl[] retValue = new MessageDataImpl[]{};

        if (!isDone.get()) {
            Logger.log(Logger.Tag.MESSAGE_HISTORY, "getPreviousResponsesByMethod: Method name: " + typeToFind.name());

            Collection<ResponseHistoryWrapper> responsesCopy = copyCollection(responses, responses, false);

            List<MessageDataImpl> resultList = new ArrayList<MessageDataImpl>();
            for (ResponseHistoryWrapper responseWrapper : responsesCopy) {
                final MessageType candidateType = MessageType.parse(responseWrapper.getMessage().getMethod());

                if (candidateType == typeToFind) {
                    resultList.add(MessageDataBuilder.buildMessageData(responseWrapper.getMessage(), responseWrapper.isIncome()));
                }
            }
            Logger.log(Logger.Tag.MESSAGE_HISTORY, "Returning responses " + resultList);

            retValue = resultList.toArray(new MessageDataImpl[resultList.size()]);
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }

    /**
     * Returns wrapper which stores custom headers and body parts for client response
     *
     * @return - message wrapper
     */
    public MessageDataImpl getNextResponseMessage() {
        MessageDataImpl retValue = null;

        if (!isDone.get()) {
            if (nextResponseMessage.get() == null) {
                nextResponseMessage.compareAndSet(null, new MessageDataImpl());
            }

            retValue = nextResponseMessage.get();
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }

    /**
     * Returns wrapper which stores custom headers and body parts for client request
     *
     * @return - message wrapper
     */
    public MessageDataImpl nextRequestMessage() {
        MessageDataImpl retValue = null;

        if (!isDone.get()) {
            if (nextRequestMessage.get() == null) {
                nextRequestMessage.compareAndSet(null, new MessageDataImpl());
            }

            retValue = nextRequestMessage.get();
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }

    /**
     * Cleans next request added data
     */
    public void clearNextRequestMessage() {
        if (!isDone.get()) {
            doClearNextRequestMessage();
        }
        else {
            throwIllegalState();
        }
    }

    private void doClearNextRequestMessage() {
        MessageDataImpl messageData = nextRequestMessage.getAndSet(null);
        if (messageData != null) {
            messageData.clean();
            messageData = null;
        }
    }

    /**
     * Cleans next response added data
     */
    public void clearNextResponseMessage() {
        if (!isDone.get()) {
            doClearNextResponseMessage();
        }
        else {
            throwIllegalState();
        }
    }

    private void doClearNextResponseMessage() {
        MessageDataImpl messageData = nextResponseMessage.getAndSet(null);
        if (messageData != null) {
            messageData.clean();
            messageData = null;
        }
    }


    public Request findLastRequest() {
        Request retValue = null;

        if (!isDone.get()) {
            retValue = null;

            List<RequestHistoryWrapper> requestsCopy = copyCollectionAsList(requests, requests, false);
            if (requestsCopy.size() != 0) {
                retValue = requestsCopy.get(requestsCopy.size() - 1).getMessage();
            }
        }
        else {
            throwIllegalState();
        }

        return retValue;
    }


    public Request findLastOutRequestByMethod(final MessageType methodToFind) {

        Request retValue = null;

        if (!isDone.get()) {
            Collection<RequestHistoryWrapper> reverseCopy = copyCollection(requests, requests, true);

            retValue = null;
            for (RequestHistoryWrapper requestWrapper : reverseCopy) {
                if (methodToFind == MessageType.parse(requestWrapper.getMessage().getMethod())) {
                    retValue = requestWrapper.getMessage();
                    break;
                }
            }
        }
        else {
            return throwIllegalState();
        }

        return retValue;
    }

    private Request throwIllegalState() {
        throw new IllegalStateException(MESSAGE_HISTORY_ALREADY_SHUTDOWN_MSG);
    }


    public void removeMessageAddedListener() {
        messageAddedListener = null;
    }

    private void notifyMessageAddedListener(BaseSipMessage msg) {
        if (messageAddedListener != null) {
            messageAddedListener.messageAdded(msg);
        }
    }


    public void setMessageAddedListener(MessageAddedListener messageAddedListener) {
        this.messageAddedListener = messageAddedListener;
    }

    private <T> Collection<T> copyCollection(
            final Collection<T> collection,
            final Object mutex,
            final boolean orderInReverse) {

        List<T> retValue;

        synchronized (mutex) {
            retValue = new ArrayList<T>(collection);
        }

        if (orderInReverse) {
            Collections.reverse(retValue);
        }

        return retValue;
    }

    private <T> List<T> copyCollectionAsList(
            final Collection<T> collection,
            final Object mutex,
            final boolean orderInReverse) {

        return (List<T>) copyCollection(collection, mutex, orderInReverse);
    }

    /**
     * Shutdowns the history and cleans data
     */
    public void shutdown() {
        if (isDone.compareAndSet(false, true)) {
            synchronized (requests) {
                requests.clear();
            }
            synchronized (responses) {
                responses.clear();
            }
            doClearNextRequestMessage();
            doClearNextResponseMessage();
            messageAddedListener = null;
        }
    }

    public String toString() {
        String base = super.toString();
        synchronized (requests) {
            synchronized (responses) {
                return "base = " + base + ", MessageHistory [incmoing: " + requests + " client: " + responses + "]\n";
            }
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        Logger.log(LOG_TAG, "finalize#" + this);       
        super.finalize();
    }
}