package javax.microedition.ims.engine.test.invite;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.core.Session;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.core.media.MediaDescriptor;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.engine.test.AppContext;
import javax.microedition.ims.engine.test.InviteActivity;
import javax.microedition.ims.engine.test.R;


public class DialogOutgoingCall extends Dialog {
    protected final String TAG = "DialogOutgoingCall";
    
    public static final int DIALOG_ID = 13;
    
    private InviteActivity inviteActivity;

    public DialogOutgoingCall(InviteActivity inviteActivity) {
        super(inviteActivity);
        this.inviteActivity = inviteActivity;
    }
    
    public static DialogOutgoingCall createDialog(InviteActivity inviteActivity) {
        
        final DialogOutgoingCall dialog = new DialogOutgoingCall(
            inviteActivity
        );
        
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_outcoming_call);
        dialog.setTitle(R.string.title_outgoing_call);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                
                dialog.cancelCall();
            }
        });
        
        dialog.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                
                dialog.updateCall();
            }
        });

        return dialog;
    }
    


    public static void showDialog(Activity activity, String remoteParty, Session outgoingSession) {
        AppContext.instance.setDialogOutgoingCallParams(
            new DlgParams(remoteParty, outgoingSession)
        );
        
        activity.showDialog(DIALOG_ID);
    }
    
    public static void hideDialog(Activity activity) {
        activity.dismissDialog(DIALOG_ID);
    }
    
    protected void updateCall() {
        DlgParams dlgParams = AppContext.instance.getDialogOutgoingCallParams();
        
        Session outgoingSession = dlgParams.getOutgoingSession();
        
        if (outgoingSession != null) {
            Log.i(TAG, "updateCall");
            try {
                
//                for (Media media : session.getMedia()) {
//                    media.setDirection(Media.DIRECTION_SEND);
//                    media.setDirection(Media.DIRECTION_SEND_RECEIVE);
//                }
                
                
                StreamMedia media = (StreamMedia) outgoingSession.createMedia(Media.MediaType.StreamMedia, Media.DIRECTION_SEND_RECEIVE);
                media.setStreamType(StreamMedia.STREAM_TYPE_AUDIO);
                MediaDescriptor audioDescriptor = media.getMediaDescriptors()[0];
                audioDescriptor.setMediaTitle("StreamMediaAudio");                 
                
                
                outgoingSession.update();
            } catch (ImsException e) {                
                e.printStackTrace();
            }
         } else {
            Log.e(TAG,"Cannot cancelCall -session is null");                
         }
    }
    
    private void cancelCall() {
        DlgParams dlgParams = AppContext.instance.getDialogOutgoingCallParams();
        
        Session outgoingSession = dlgParams.getOutgoingSession();
        
        inviteActivity.dismissDialog(DIALOG_ID);
        inviteActivity.setUserCanceled(true);
        
        if (outgoingSession != null) {
            Log.i(TAG,"cancelCall");
            inviteActivity.terminateCall(true, true, outgoingSession);
//            new SessionTerminateTask().execute(outgoingSession);
         } else {
            Log.e(TAG,"Cannot cancelCall -session is null");                
         }
        Toast.makeText(inviteActivity, R.string.msg_call_canceled, Toast.LENGTH_SHORT).show();
    }
    
    public void onPrepare() {
        DlgParams dlgParams = AppContext.instance.getDialogOutgoingCallParams();

        TextView text = (TextView) findViewById(R.id.text);
        text.setText(inviteActivity.getString(R.string.msg_call_dialing, dlgParams.getRemoteParty()));
    }

    
    public static class DlgParams {
        private String remoteParty;
        private Session outgoingSession;

        public DlgParams(String remoteParty, Session outgoingSession) {
            this.remoteParty = remoteParty;
            this.outgoingSession = outgoingSession;
        }

        public String getRemoteParty() {
            return remoteParty;
        }

        public void setRemoteParty(String remoteParty) {
            this.remoteParty = remoteParty;
        }

        public Session getOutgoingSession() {
            return outgoingSession;
        }

        public void setOutgoingSession(Session outgoingSession) {
            this.outgoingSession = outgoingSession;
        }
    }
    
}
