/*package javax.microedition.ims.core.transaction.client.msrp;

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.DefaultTransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.transaction.TransactionType;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.core.transaction.state.noninvite.msrp.client.TryingState;

public class MsrpReportClientTransaction extends MsrpClientTransaction {
    
    public MsrpReportClientTransaction(final StackContext stackContext, Dialog dlg) {
        super(stackContext, dlg);
    }

    
    public TransactionType getTransactionType() {
        return TransactionType.MSRP_REPORT_CLIENT;
    }

    
    protected Object onMessage(final MsrpMessage initialMessage, final MsrpMessage lastMessage) {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        super.onMessage(initialMessage, lastMessage);
        return null;
    }

    
    protected void onTransactionInited() {
        assert TransactionUtils.isTransactionExecutionThread() :
                "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        final TransactionStateChangeEvent<MsrpMessage> event =
                DefaultTransactionStateChangeEvent.createInitEvent(this, null);

        transitToState(new TryingState<MsrpMessage>(this), event);
    }

    
    protected MessageType getInitialBuilderType() {
        return MessageType.MSRP_REPORT;
    }
}
*/