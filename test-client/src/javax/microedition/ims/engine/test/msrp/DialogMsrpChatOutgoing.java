package javax.microedition.ims.engine.test.msrp;

import android.app.Dialog;
import android.util.Log;
import android.view.View;

import javax.microedition.ims.engine.test.MsrpChatActivity;
import javax.microedition.ims.engine.test.R;

public class DialogMsrpChatOutgoing extends Dialog {

    private final static String TAG = "DialogMsrpChatOutgoing";
    
    private final MsrpChatActivity msrpChatActivity;
    private static boolean displayed = false;
    
    public static final int DIALOG_ID = 25;
    
    public DialogMsrpChatOutgoing(final MsrpChatActivity msrpChatActivity) {
        super(msrpChatActivity);
        this.msrpChatActivity = msrpChatActivity;
        Log.i(TAG, "DialogMsrpChatOutgoing()#");
    }

    public static DialogMsrpChatOutgoing createDialog(MsrpChatActivity msrpChatActivity) {
        Log.i(TAG, "createDialog#started");
        
        final DialogMsrpChatOutgoing dialog = new DialogMsrpChatOutgoing(msrpChatActivity);
        
        dialog.setContentView(R.layout.dialog_msrp_chat_outgoing);
        dialog.setTitle(R.string.msrp_chat_outgoing_title);
        
        dialog.findViewById(R.id.msrp_chat_outgoing_cancel_btn).setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.doCancel();
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
    
    private void doCancel() {
        Log.i(TAG, "doCancel#started");
        
        msrpChatActivity.cancelOutgoingChatSession();
        
        hideDialog(msrpChatActivity);
        
        Log.i(TAG, "doCancel#finished");
    }
    
    public static void showDialog(MsrpChatActivity activity) {
        Log.i(TAG, "showDialog#started");
        
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
