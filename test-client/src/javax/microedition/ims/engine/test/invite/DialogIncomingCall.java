package javax.microedition.ims.engine.test.invite;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.core.Session;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.engine.test.AppContext;
import javax.microedition.ims.engine.test.BaseActivity;
import javax.microedition.ims.engine.test.MediaRegistry;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.engine.test.SessionList.SessonType;

public class DialogIncomingCall extends Dialog {

    protected final static String TAG = "DialogIncomingCall";
    
    public static final int DIALOG_ID = 1;
    
    private BaseActivity baseActivity;

    public DialogIncomingCall(BaseActivity baseActivity) {
        super(baseActivity);
        this.baseActivity = baseActivity;
    }
    
    public static DialogIncomingCall createDialog(final BaseActivity baseActivity) {
        
        final DialogIncomingCall dialog = new DialogIncomingCall(
            baseActivity
        );
        
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_incoming_call);
        dialog.setTitle(R.string.title_incoming_call);

        dialog.findViewById(R.id.btn_answer_incoming).setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                dialog.acceptIncomingCall();
            }
        });

        dialog.findViewById(R.id.btn_decline_incoming).setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                dialog.declineIncomingCall();
            }
        });
        
        dialog.findViewById(R.id.btn_update_incoming).setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                dialog.updateIncomingCall();
            }
        });
        
        return dialog;
    }
    
    public void onPrepare() {
        DlgParams dlgParams = AppContext.instance.getDialogIncomingCallParams();
        
        String remoteParty = dlgParams.getRemoteParty();
        Session incomingSession = dlgParams.getIncomingSession();
        
        String title = String.format("'%s' calling", remoteParty);
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(title);

        TextView mediaView = (TextView) findViewById(R.id.medias);

        //create media description
        StringBuilder sb = new StringBuilder("Medias:\n");
        Media[] medias = incomingSession.getMedia();
        for (int i = 0; i < medias.length; i++) {
            sb.append(i + 1).append(".").append(medias[i].getMediaDescriptors()[0].getMediaDescription()).append("\n");

        }
        mediaView.setText(sb.toString());

        CheckBox mediaCheckbox1 = (CheckBox) findViewById(R.id.offered_media1);
        CheckBox mediaCheckbox2 = (CheckBox) findViewById(R.id.offered_media2);
        CheckBox mediaCheckbox3 = (CheckBox) findViewById(R.id.offered_media3);

        for (Media media : medias) {
            String mediaDescription = media.getMediaDescriptors()[0].getMediaDescription();
            if (mediaDescription.startsWith("audio")) {
                mediaCheckbox1.setChecked(true);
            } else if (mediaDescription.startsWith("video")) {
                mediaCheckbox2.setChecked(true);
            } else if (mediaDescription.startsWith("application")) {
                mediaCheckbox3.setChecked(true);
            }
        }
    }
    
    public static void showDialog(Activity activity, String remoteParty, Session incomingSession) {
        AppContext.instance.setDialogIncomingCallParams(
            new DlgParams(remoteParty, incomingSession)
        );

//        activity.dismissDialog(DialogOutgoingCall.DIALOG_ID);
//        activity.dismissDialog(DialogCallInProgress.DIALOG_ID);
        
        activity.showDialog(DIALOG_ID);
    }
    
    protected void acceptIncomingCall() {
        DlgParams dlgParams = AppContext.instance.getDialogIncomingCallParams();
        
        String remoteParty = dlgParams.getRemoteParty();
        Session incomingSession = dlgParams.getIncomingSession();

        Log.i(TAG, "acceptIncomingCall#");
        baseActivity.dismissDialog(DIALOG_ID);

        MediaRegistry.INSTANCE.subscribeLisnersToActiveMedia(incomingSession.getMedia());

        try {
            AppContext.instance.getSessionList().addNewInProgressSession(incomingSession, SessonType.INCOMING);
            
            incomingSession.accept();
            
        } catch (ImsException e) {
            Log.e(TAG, e.getMessage());
        }

		if (((CheckBox) findViewById(R.id.offered_media2)).isChecked()) {
			DialogVideoCall.showDialog(baseActivity, dlgParams.getRemoteParty());
		} else {
			DialogCallInProgress.showDialog(baseActivity, remoteParty);
		}	
    }


    private void declineIncomingCall() {
        DlgParams dlgParams = AppContext.instance.getDialogIncomingCallParams();
        
        String remoteParty = dlgParams.getRemoteParty();
        Session incomingSession = dlgParams.getIncomingSession();

        baseActivity.dismissDialog(DIALOG_ID);
        System.out.println("declineIncomingCall");

        if (incomingSession != null) {
            incomingSession.reject();
        }

        Toast.makeText(baseActivity, baseActivity.getString(R.string.msg_call_declined, remoteParty), Toast.LENGTH_SHORT).show();
    }
    
    private void updateIncomingCall() {        
        DlgParams dlgParams = AppContext.instance.getDialogIncomingCallParams();
        
        Session incomingSession = dlgParams.getIncomingSession();
        
        System.out.println("declineIncomingCall");

        if (incomingSession != null) {
            try {
                incomingSession.update();
            } catch (ImsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //Toast.makeText(BaseActivity.this, getString(R.string.msg_call_declined, remoteParty), Toast.LENGTH_SHORT).show();
    }
    
    
    public static class DlgParams {
        private String remoteParty;
        private Session incomingSession;

        public DlgParams(String remoteParty, Session incomingSession) {
            this.remoteParty = remoteParty;
            this.incomingSession = incomingSession;
        }

        public String getRemoteParty() {
            return remoteParty;
        }

        public void setRemoteParty(String remoteParty) {
            this.remoteParty = remoteParty;
        }

        public Session getIncomingSession() {
            return incomingSession;
        }

        public void setIncomingSession(Session incomingSession) {
            this.incomingSession = incomingSession;
        }
    }

}
