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

package javax.microedition.ims.core.transaction.client;

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.DefaultTransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.subscribe.DefaultNotifyInfo;
import javax.microedition.ims.core.sipservice.subscribe.NotifyInfo;
import javax.microedition.ims.core.transaction.TransactionDescription;
import javax.microedition.ims.core.transaction.TransactionType;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.core.transaction.state.noninvite.sip.client.TryingState;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.concurrent.atomic.AtomicReference;

public class NotifyClientTransaction extends ClientTransaction implements NotifyClntTransaction {

    private final AtomicReference<NotifyInfo> notifyInfo = new AtomicReference<NotifyInfo>(null);

    //constructor called by reflection
    @SuppressWarnings({"UnusedDeclaration"})
    public NotifyClientTransaction(
            final StackContext stackContext,
            final Dialog dlg,
            final TransactionDescription description) {

        super(stackContext, dlg, description);
    }

    
    public TransactionType getTransactionType() {
        return TransactionType.SIP_NOTIFY_CLIENT;
    }

    
    protected void beforeInitMessageCreated() throws IllegalStateException {
        super.beforeInitMessageCreated();

        if (notifyInfo.get() == null) {
            throw new IllegalStateException("DefaultNotifyInfo must be set for " + this + " transaction before transaction could be started.");
        }

        getDialog().putCustomParameter(Dialog.ParamKey.NOTIFY_INFO, notifyInfo.get());
    }

    protected Boolean onMessage(final BaseSipMessage initialMessage, final BaseSipMessage lastMessage) {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        return super.onMessage(initialMessage, lastMessage);
    }

    
    protected void onTransactionInited() {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        final TransactionStateChangeEvent<BaseSipMessage> event =
                DefaultTransactionStateChangeEvent.createInitEvent(this, null);

        transitToState(new TryingState<BaseSipMessage>(this), event);
    }

    
    public void setNotifyInfo(final DefaultNotifyInfo notifyInfo) {
        this.notifyInfo.compareAndSet(null, notifyInfo);
    }

    
    protected MessageType getInitialBuilderType() {
        return MessageType.SIP_NOTIFY;
    }
}
