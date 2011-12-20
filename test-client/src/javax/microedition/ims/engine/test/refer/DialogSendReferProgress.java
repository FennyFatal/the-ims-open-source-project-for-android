package javax.microedition.ims.engine.test.refer;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import javax.microedition.ims.engine.test.AppContext;
import javax.microedition.ims.engine.test.InviteActivity;
import javax.microedition.ims.engine.test.R;

public class DialogSendReferProgress extends Dialog {
    
    public static final int DIALOG_ID = 11;
    
    private InviteActivity inviteActivity;
    
    private DialogSendReferProgress(InviteActivity inviteActivity) {
        super(inviteActivity);
        this.inviteActivity = inviteActivity;
    }
    
    public static void showDialog(DlgParams dlgParams, Activity activity) {
        AppContext.instance.setDialogSendReferProgressParams(dlgParams);
        activity.showDialog(DIALOG_ID);
    }
    
    public static DialogSendReferProgress createDialog(InviteActivity inviteActivity) {
        final DialogSendReferProgress dialog = new DialogSendReferProgress(inviteActivity);
        
        dialog.setContentView(R.layout.dialog_refer_send_progress);
        dialog.setTitle(R.string.send_refer_progress_title);
        
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.onCancel();
                }
            }
        );
        
        return dialog;
    }
    
    private void onCancel() {
        inviteActivity.dismissDialog(DIALOG_ID);
    }

    public void onPrepare() {
        DlgParams dlgParams = AppContext.instance.getDialogSendReferProgressParams();
        
        TextView text = (TextView) findViewById(R.id.send_refer_progress_text);
        text.setText("Asking " + dlgParams.getTo() + "\n for calling " + dlgParams.getReferTo());
    }
    
    
    public static class DlgParams {
        private String to;
        private String referTo;
        public DlgParams(String to, String referTo) {
            this.to = to;
            this.referTo = referTo;
        }
        public String getTo() {
            return to;
        }
        public void setTo(String to) {
            this.to = to;
        }
        public String getReferTo() {
            return referTo;
        }
        public void setReferTo(String referTo) {
            this.referTo = referTo;
        }
    }

}
