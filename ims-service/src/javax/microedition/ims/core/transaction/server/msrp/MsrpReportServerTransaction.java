/*package javax.microedition.ims.core.transaction.server.msrp;

import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.DefaultTransactionStateChangeEvent;
import javax.microedition.ims.core.sipservice.TransactionStateChangeEvent;
import javax.microedition.ims.core.transaction.TransactionType;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.core.transaction.state.noninvite.msrp.server.TryingState;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;

public class MsrpReportServerTransaction extends MsrpServerTransaction {
    public MsrpReportServerTransaction(final StackContext stackContext, Dialog dlg) {
        super(stackContext, dlg);
    }

    
    public TransactionType getTransactionType() {
        return TransactionType.MSRP_REPORT_SERVER;
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
                DefaultTransactionStateChangeEvent.createInitEvent(this, getInitialMessage());

        transitToState(new TryingState(this), event);
    }

}
*/