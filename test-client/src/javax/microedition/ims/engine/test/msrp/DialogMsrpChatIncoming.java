package javax.microedition.ims.engine.test.msrp;

import android.app.Dialog;
import android.util.Log;
import android.view.View;

import javax.microedition.ims.engine.test.MsrpChatActivity;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.im.ChatInvitation;

public class DialogMsrpChatIncoming extends Dialog {

    private final static String TAG = "DialogMsrpChatIncoming";
    
    private final MsrpChatActivity msrpChatActivity;
    private static ChatInvitation chatInvitation;
    private static boolean displayed = false;
    
    public static final int DIALOG_ID = 24;

    public DialogMsrpChatIncoming(final MsrpChatActivity msrpChatActivity) {
        super(msrpChatActivity);
        this.msrpChatActivity = msrpChatActivity;
        Log.i(TAG, "DialogMsrpChatIncoming()#");
    }
    
    public static DialogMsrpChatIncoming createDialog(MsrpChatActivity msrpChatActivity) {
        Log.i(TAG, "createDialog#started");
        
        final DialogMsrpChatIncoming dialog = new DialogMsrpChatIncoming(msrpChatActivity);
        
        dialog.setContentView(R.layout.dialog_msrp_chat_incoming);
        dialog.setTitle(R.string.msrp_chat_incoming_title);
        
        dialog.findViewById(R.id.msrp_chat_incoming_accept_btn).setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.doAccept();
                }
            }
        );
        
        dialog.findViewById(R.id.msrp_chat_incoming_reject_btn).setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.doReject();
                }
            }
        );
  
        Log.i(TAG, "createDialog#finished");
        
        return dialog;
    }
    
    public void onPrepare() {
        Log.i(TAG, "onPrepare#started");

        Log.i(TAG, "onPrepare#finished");
    }

    
    private void doAccept() {
        Log.i(TAG, "doAccept#started");
        
        chatInvitation.accept();
        hideDialog(msrpChatActivity);
        
        Log.i(TAG, "doAccept#finished");
    }
    
    void doReject() {
        Log.i(TAG, "doReject#started");
        
        chatInvitation.reject();
        hideDialog(msrpChatActivity);
        
        Log.i(TAG, "doReject#finished");
    }
    
    public static void showDialog(MsrpChatActivity activity, ChatInvitation inv) {
        Log.i(TAG, "showDialog#started");
        
        chatInvitation = inv;
        displayed = true;
        activity.showDialog(DIALOG_ID);
        
        Log.i(TAG, "showDialog#finished");
    }
    
    public static void hideDialog(MsrpChatActivity activity) {
        Log.i(TAG, "hideDialog#started");
        
        if (displayed) {
            activity.dismissDialog(DIALOG_ID);
            displayed = false;
        }
        
        Log.i(TAG, "hideDialog#finished");
    }
    
}
