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

package javax.microedition.ims.core.transaction;

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.RepetitiousTaskManager.RepetitiousTimeStrategy;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.sipservice.State;
import javax.microedition.ims.core.sipservice.StateChangeReason;
import javax.microedition.ims.core.sipservice.TransactionState;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.DefaultTransactionStateChangeEvent;
import javax.microedition.ims.messages.wrappers.sip.Request;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 18-Dec-2009
 * Time: 13:42:44
 */
public abstract class CommonTransaction<M extends IMSMessage> implements Transaction<Boolean, M>, Shutdownable {

    private final String TAG = "CommonTransaction";

    private final Object transactionId = new Object();
    private final StackContext stackContext;

    private final ReentrantSynchronizationContext<Boolean> context;

    protected final AtomicBoolean transactionComplete = new AtomicBoolean(false);
    private final AtomicReference<TransactionResult<Boolean>> transactionValue =
            new AtomicReference<TransactionResult<Boolean>>(null);

    private final AtomicBoolean done = new AtomicBoolean(false);
    protected final AtomicReference<M> initialMessage = new AtomicReference<M>();
    protected final AtomicReference<M> lastInMessage = new AtomicReference<M>();
    protected final AtomicReference<M> lastInRequest = new AtomicReference<M>();
    protected final AtomicReference<M> lastOutMessage = new AtomicReference<M>();

    private final ReentrantSynchronizationContext.ContextCallback<Boolean> wrappedContextCallback;
    protected final ListenerHolder<TransactionListener> listenerHolder = new ListenerHolder<TransactionListener>(TransactionListener.class);


    //TODO: make atomic reference
    protected volatile TransactionState<? extends Transaction, M> currentState;

    public CommonTransaction(final StackContext stackContext) {
        this.stackContext = stackContext;

        final ReentrantSynchronizationContext.ContextCallback<Boolean> contextCallback =
                new ReentrantSynchronizationContext.ContextCallback<Boolean>() {
                    public void initiateTransaction() {

                        assert TransactionUtils.isTransactionExecutionThread() :
                                "Code run in wrong thread. Must be run in TransactionThread. Now in " +
                                        Thread.currentThread();

                        getListenerHolder().getNotifier().onTransactionInit(createTransactionEvent(null, null));

                        //if we need start new transaction
                        //onTransactionCreate new message
                        //final BaseSipMessage message = createInitialMessage(messageBuilder, parameters);
                        boolean needSendInitialMessage = false;

                        boolean needBuildInitialMessage = needBuildInitialMessage();
                        if (needBuildInitialMessage) {
                            beforeInitMessageCreated();
                            M message = buildInitialMessage();

                            //remember it for future processing of answer
                            initialMessage.compareAndSet(null, message);
                            //handle message to upper level
                            needSendInitialMessage = true;
                        }
                        else {
                            M firstIncomingMessage = getFirstIncomingMessage();
                            initialMessage.compareAndSet(null, firstIncomingMessage);
                        }

                        if (needSendInitialMessage) {
                            sendMessage(initialMessage.get(), null);
                            startResendTimer();
                        }

                        getListenerHolder().getNotifier().onTransactionInited(createTransactionEvent(null, null));
                        onTransactionInited();

                    }

                    public void finalizeTransaction(final TransactionResult<Boolean> result) {
                        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
                        tryToComplete(result);
                    }


                    public String toString() {
                        return CommonTransaction.this.toString() + " " + super.toString();
                    }

                    @PriorityResolver
                    public Priority priority(Method method) {
                        return CommonTransaction.this.priority(method);
                    }
                };

        this.wrappedContextCallback = TransactionUtils.wrap(
                contextCallback,
                ReentrantSynchronizationContext.ContextCallback.class
        );

        this.context = new ReentrantSynchronizationContext<Boolean>(this.wrappedContextCallback, Boolean.FALSE, stackContext.getRepetitiousTaskManager());
    }

    protected abstract M buildInitialMessage();

    protected boolean needBuildInitialMessage() {
        return true;
    }

    protected void beforeInitMessageCreated() {
    }

    protected Priority priority(Method method) {
        return null;
    }

    public Object getTransactionId() {
        return transactionId;
    }

    public StackContext getStackContext() {
        return stackContext;
    }

    public void push(final M inMessage) {
        assert !done.get() : "transaction already finished: " + CommonTransaction.this;

        if (!done.get()) {

            this.lastInMessage.set(inMessage);
            if (inMessage instanceof Request) {
                this.lastInRequest.set(inMessage);
            }
            final M outMessage = lastOutMessage.get();

            TransactionUtils.invokeLaterSmart(
                    new TransactionRunnable("push[" + inMessage.shortDescription() + "] transaction[" + CommonTransaction.this.toString() + "]") {
                        public void run() {
                            doPushMessage(inMessage, outMessage);
                        }
                    }
            );
        }
    }

    private void doPushMessage(final M inMessage, final M outMessage) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();
        assert !done.get() : "transaction already finished: " + CommonTransaction.this;

        if (!done.get()) {
            final TransactionEvent transactionEvent = createTransactionEvent(inMessage, outMessage);
            getListenerHolder().getNotifier().onIncomingMessage(transactionEvent);

            Boolean transactionResult = onMessage(initialMessage.get(), inMessage);
            tryToComplete(new TransactionResultImpl<Boolean>(transactionResult, TransactionResult.Reason.RELEASE));
        }
    }

    protected M getFirstIncomingMessage() {
        throw new UnsupportedOperationException();
    }

    protected abstract RepetitiousTimeStrategy getResendRequestInterval();

    protected void onTransactionInited() {
    }

    protected void startResendTimer() {
        final RepetitiousTimeStrategy strategy = getResendRequestInterval();


        //run resender task to resend messages if there is no answer for a long time
        //if (getStackContext().getConfig().getConnectionType() == Protocol.UDP) {
        if (isUnreliableTransport()) {
            stackContext.getRepetitiousTaskManager().startRepetitiousTask(
                    getTransactionId()/*initialMessage.get()*/,
                    new RepetitiousTaskManager.Repeater<Object>() {
                        
                        @Override
                        public void onRepeat(Object transactionId, final Shutdownable task) {
                            M msg = initialMessage.get();
                            Logger.log(TAG, "startResendTimer().RepetitiousTaskManager.onRepeat()   msg=" + msg.shortDescription());

                            //Logger.log(TAG, "transactionComplete.get()=" + transactionComplete.get());
                            ///handle message to upper level
                            if (!transactionComplete.get() && lastInMessage == null) {
                                //resend always the same message
                                sendMessage(msg, new MessageStateListener() {
                                    @Override
                                    public void onMessageSent() {
                                        task.shutdown();
                                    }
                                });
                            }
                            else {
                                stackContext.getRepetitiousTaskManager().cancelTask(transactionId);
                                task.shutdown();
                            }
                        }
                    },
                    strategy
            );
        }
    }

    private boolean isUnreliableTransport() {
        return getStackContext().getConnectionType() == Protocol.UDP;
    }

    public void addListener(TransactionListener listener) {
        listenerHolder.addListener(listener);
    }

    public void removeListener(TransactionListener listener) {
        listenerHolder.removeListener(listener);
    }

    protected ListenerHolder<TransactionListener> getListenerHolder() {
        return listenerHolder;
    }

    public boolean isComplete() {
        return transactionComplete.get();

    }

    public TransactionResult<Boolean> getTransactionValue() {
        return transactionValue.get();
    }

    public void appendNoBlock() {
        doAppend(null, false);
    }

    public void appendNoBlock(final TimeoutUnit timeoutUnit) {
        doAppend(timeoutUnit, false);
    }

    public TransactionResult<Boolean> append() {
        doAppend(null, true);

        assert transactionValue.get() != null : "returning before setting invariant";
        return transactionValue.get();
    }

    public TransactionResult<Boolean> append(final TimeoutUnit timeoutUnit) {
        doAppend(timeoutUnit, true);

        assert transactionValue.get() != null : "returning before setting invariant";
        return transactionValue.get();
    }

    private TransactionResult<Boolean> doAppend(final TimeoutUnit timeoutUnit, final boolean block) {
        Logger.log("CommontTransaction", "doAppend# timeoutUnit = " + timeoutUnit + ", block = " + block + ", done = " + done.get() + ", transactionComplete.get() = " + transactionComplete.get());

        //wait for end of transaction
        final TransactionResult<Boolean> retValue;

        if (!done.get()) {
            if (!transactionComplete.get()) {
                if (block) {
                    retValue = context.append(timeoutUnit);
                }
                else {
                    retValue = null;
                    context.appendNoBlock(timeoutUnit);
                }
            }
            else {
                retValue = transactionValue.get();
            }
        }
        else {
            throw new IllegalArgumentException("Transaction already shutdown.");
        }

        return retValue;
    }

    //TODO: it's public only for StateHolder purposes. Must be refactored. StateHolder MUST not have direct access to transaction. Only through special handler.
    //modificator must be made protected

    public void sendMessage(M outMessage, final MessageStateListener listener) {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        lastOutMessage.set(outMessage);
        doSendMessage(outMessage, listener);
        //getListenerHolder().getNotifier().onOutgoingMessage(createTransactionEvent(lastInMessage.get(), lastOutMessage.get()));
    }
    
    public interface MessageStateListener {
        void onMessageSent();
    }

    private void doSendMessage(final M outMessage, final MessageStateListener listener) {

        final M inMessage = lastInMessage.get();

        final String lastInMsgDescr = inMessage == null ? "NONE" : inMessage.shortDescription();
        final String lastOutMsgDescr = outMessage == null ? "NONE" : outMessage.shortDescription();
        TransactionUtils.invokeLaterSmart(
                new TransactionRunnable("doSendMessage[lastInMsg " + lastInMsgDescr + " lastOutMsg " + lastOutMsgDescr + "]") {
                    public void run() {
                        getListenerHolder().getNotifier().onOutgoingMessage(createTransactionEvent(inMessage, outMessage));
                        
                        if(listener != null) {
                            listener.onMessageSent();
                        }
                    }
                }
        );

    }

    //TODO: invert flag byTimeout to make it be byMessage

    public void tryToComplete(final TransactionResult<Boolean> transactionResult) {

        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        /*if (transactionResult == null) {
            throw new IllegalArgumentException("Transaction MUST always have non null result value. Now it has " + transactionResult);
        }*/

        Logger.log("try to complete. result: " + transactionResult);

        if (transactionResult.getValue() != null && done.compareAndSet(false, true)) {

            Logger.log("try to complete. done: " + done.get());

            transactionValue.compareAndSet(null, transactionResult);

            if (transactionComplete.compareAndSet(false, true)) {

                //let the outer code the chance to reInvite transaction maps and so on
                //release awaiting threads in register methods

                stackContext.getRepetitiousTaskManager().cancelTask(initialMessage.get());

                //TODO temporary solution
                IMSMessage message = lastOutMessage.get();
                if(message != null && transactionResult.getReason() == TransactionResult.Reason.TIMEOUT) {
                    message.expire();
                }

                if(transactionResult.getReason() == TransactionResult.Reason.TIMEOUT &&
                    currentState != null && currentState.shortName().contains("TryingState") &&
                    message != null && message.toString().contains("OPTIONS")) {
                    TransactionStateChangeEvent<M> event =
                        new DefaultTransactionStateChangeEvent<M>(
                            this, //transaction,
                            State.TRYING,
                            StateChangeReason.TIMER_TIMEOUT,
                            null
                        );

                    notifyTU(event);
                }

                //let the outer code the chance to reInvite transaction maps and so on
                getListenerHolder().getNotifier().onTransactionComplete(
                        createTransactionEvent(lastInMessage.get(), lastOutMessage.get()),
                        transactionResult.getReason()
                );
                /*if (currentState != null) {
                    currentState.shutdown();
                }*/
                //release awaiting threads in register methods
                context.release(transactionValue.get().getValue());
                context.shutdown();
                listenerHolder.shutdown();
            }
        }
    }

    /**
     * The way to release resources on some outer signal. For example IMS-STACK shutdown and restart.
     * In normal routine MUST NOT be invoked.
     */
    public void shutdown() {

        TransactionUtils.invokeLater(
                new TransactionRunnable("CommonSIPTransaction.shutdown[]") {
                    public void run() {
                        doShutdown();
                    }
                }
        );
    }

    private void doShutdown() {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        log("Cancelling transaction", "shutdown");

        tryToComplete(new TransactionResultImpl<Boolean>(Boolean.FALSE, TransactionResult.Reason.OUTER_INTERRUPT));

        log("transaction cancelled", "shutdown");
    }

    protected TransactionEvent<M> createTransactionEvent(final M lastInMessage, final M lastOutMessage) {
        return new DefaultTransactionEvent<M>(
                this,
                initialMessage.get(),
                lastInMessage,
                lastOutMessage
        );
    }

    /**
     * @param initialMessage
     * @param lastMessage
     * @return null if transaction is not complete otherwise result of transaction
     */
    protected abstract Boolean onMessage(M initialMessage, M lastMessage);

    private static void log(String msg, String prefix) {
        Logger.log(prefix, msg);
    }

    //abstract protected M createInitialMessage(MessageBuilder builder, Dialog parameters);


    public String toString() {

        final String wholeName = getTransactionType().getImplementationClass().getName();
        String[] nameParts = wholeName.split("\\.");
        String shortName = nameParts == null || nameParts.length == 0 ? wholeName : nameParts[nameParts.length - 1];

        final M initMsg = initialMessage.get();
        final String initMessageStr = initMsg == null ? "NONE" : initMsg.shortDescription();
        return new StringBuilder().append(shortName).append(" ").append(initMessageStr).toString();
    }

    public M getInitialMessage() {
        return initialMessage.get();
    }

    public M getLastOutMessage() {
        return lastOutMessage.get();
    }

    public M getLastInMessage() {
        return lastInMessage.get();
    }

    public void transitToState(final TransactionState<?, M> newState, final TransactionStateChangeEvent<M> event) {

        final String currStateName = currentState == null ? "NOSTATE" : currentState.shortName();
        TransactionUtils.invokeLaterSmart(
                new TransactionRunnable("transitToState[from " + currStateName + " to " + newState + "]") {
                    public void run() {
                        doTransitToState(newState, event);
                    }
                }
        );
    }

    private void doTransitToState(final TransactionState<?, M> newState, final TransactionStateChangeEvent<M> event) {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        Logger.log("Transiting to state: " + newState);

        if (currentState != null && currentState != newState) {
            currentState.onStateCompleted();
        }
        this.currentState = newState;
        currentState.onStateInitiated(event);
    }

    public void notifyTU(final TransactionStateChangeEvent<M> event) {
        getListenerHolder().getNotifier().onStateChanged(event);
    }


    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Transaction)) {
            return false;
        }

        final Transaction that = (Transaction) o;

        return !(transactionId != null ? !transactionId.equals(that.getTransactionId()) : that.getTransactionId() != null);

    }


    public int hashCode() {
        return transactionId != null ? transactionId.hashCode() : 0;
    }
}
