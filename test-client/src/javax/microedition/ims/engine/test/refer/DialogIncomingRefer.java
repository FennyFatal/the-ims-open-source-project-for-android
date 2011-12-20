package javax.microedition.ims.engine.test.refer;

import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.CoreService;
import javax.microedition.ims.core.Reference;
import javax.microedition.ims.core.Session;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.core.media.MediaDescriptor;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.engine.test.AppContext;
import javax.microedition.ims.engine.test.InviteActivity;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.engine.test.SessionList.SessonType;
import javax.microedition.ims.engine.test.invite.DialogOutgoingCall;

public class DialogIncomingRefer extends Dialog {
    protected final String TAG = "DialogIncomingRefer";
    
    public static final int DIALOG_ID = 12;
    
    private InviteActivity inviteActivity;
    
    private DialogIncomingRefer(InviteActivity inviteActivity) {
        super(inviteActivity);
        this.inviteActivity = inviteActivity;
    }
    
//    public static void showDialog(DlgParams dlgParams, Activity activity) {
//        AppContext.instance.setDialogIncomingReferParams(dlgParams);
//        activity.showDialog(DIALOG_ID);
//    }
    
    public static DialogIncomingRefer createDialog(InviteActivity inviteActivity) {
        
        final DialogIncomingRefer dialog = new DialogIncomingRefer(
            inviteActivity
        );
        
        dialog.setContentView(R.layout.dialog_refer_incoming);
        dialog.setTitle(R.string.incoming_refer_title);
        
        dialog.findViewById(R.id.incoming_refer_btn_accept).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.onAccept();
                }
            }
        );

        dialog.findViewById(R.id.incoming_refer_btn_reject).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.onReject();
                }
            }
        );

        return dialog;
    }
    
    private void onAccept() {
        DlgParams dlgParams = AppContext.instance.getDialogIncomingReferParams();
        
        ReferAcceptExecutionContext ctx = new ReferAcceptExecutionContext(
            dlgParams.getCurrentStartedSession(), //currentStartedSession
            dlgParams.getReference() //reference
        );
        
        new ReferAcceptTask().execute(ctx);
    }

    private void onReject() {
        DlgParams dlgParams = AppContext.instance.getDialogIncomingReferParams();
        
        Reference reference = dlgParams.getReference();
        
        new ReferRejectTask().execute(reference);
    }
    
    public void onPrepare() {
        DlgParams dlgParams = AppContext.instance.getDialogIncomingReferParams();

        TextView text = (TextView) findViewById(R.id.incoming_refer_text);
        text.setText("Address: " + dlgParams.getReferTo() + "\nMethod: " + dlgParams.getMethod());
    }
    
    private void enableDisableButtons(boolean enableButtons) {
        findViewById(R.id.incoming_refer_btn_accept).setEnabled(enableButtons);
        findViewById(R.id.incoming_refer_btn_reject).setEnabled(enableButtons);
    }

    
    public static class DlgParams {
        private Reference reference;
        private String referTo;
        private String method;
        private Session currentStartedSession;
        
        public DlgParams(Reference reference,
                String referTo, String method, Session currentStartedSession) {
            this.reference = reference;
            this.referTo = referTo;
            this.method = method;
            this.currentStartedSession = currentStartedSession;
        }

        public Reference getReference() {
            return reference;
        }

        public void setReference(Reference reference) {
            this.reference = reference;
        }

        public String getReferTo() {
            return referTo;
        }

        public void setReferTo(String referTo) {
            this.referTo = referTo;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Session getCurrentStartedSession() {
            return currentStartedSession;
        }

        public void setCurrentStartedSession(Session currentStartedSession) {
            this.currentStartedSession = currentStartedSession;
        }
    }
    
    
    private class ReferRejectTask extends AsyncTask<Reference, Void, Boolean> {
        
        protected void onPreExecute() {
            inviteActivity.setProgressBarIndeterminateVisibility(true);
            enableDisableButtons(false);
            super.onPreExecute();
        }

        
        protected Boolean doInBackground(Reference... params) {
            Reference reference = params[0];
            
            Boolean result = Boolean.FALSE;
//            try {
//                reference.reject();
                result = Boolean.TRUE;
                
//            } catch (ServiceClosedException e) {
//                Log.e(TAG, e.getMessage(), e);
//            }
            return result;
        }

        
        protected void onPostExecute(Boolean result) {
            inviteActivity.setProgressBarIndeterminateVisibility(false);
            
            inviteActivity.dismissDialog(DIALOG_ID);
            enableDisableButtons(true);
            
            if (result == Boolean.FALSE) {
                Toast.makeText(inviteActivity, "Error occured during Reject!", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }
    }
    
    
    private class ReferAcceptTask extends AsyncTask<ReferAcceptExecutionContext, Void, Session> {
        
        protected void onPreExecute() {
            inviteActivity.setProgressBarIndeterminateVisibility(true);
            enableDisableButtons(false);

            super.onPreExecute();
        }

        
        protected Session doInBackground(ReferAcceptExecutionContext... params) {
            ReferAcceptExecutionContext ctx = params[0];
            Reference reference = ctx.getReference();
            
            Session result = null;
            try {
                //TODO temporary, accept() call moved to incoming call handler
                //reference.accept();
                
                if (ctx.getCurrentStartedSession() != null) {
                    inviteActivity.terminateCall(true, false, ctx.getCurrentStartedSession());
                }
                
                String localUserId = AppContext.instance.getConnection().getLocalUserId();
                String referToUserId = reference.getReferToUserId();

                CoreService coreService = AppContext.instance.getConnection();
                
                try {
                    //DtfmPayload dtfmPayload = SettingsHelper.extractDtfmPayload(DialogIncomingRefer.this.getContext());
                    Session session = coreService.createSession(localUserId, referToUserId/*, dtfmPayload*/);
                    
                    session.setListener(
                        inviteActivity.new OutgoingCallListener()
                        
                        
//                        new SessionStateAdapter() {
//                        }
                    );
                    
                    StreamMedia media = (StreamMedia) session.createMedia(Media.MediaType.StreamMedia, Media.DIRECTION_SEND_RECEIVE);
                    media.setStreamType(StreamMedia.STREAM_TYPE_AUDIO);
                    MediaDescriptor audioDescriptor = media.getMediaDescriptors()[0];
                    audioDescriptor.setMediaTitle("StreamMediaAudio");
                    
                    reference.connectReferMethod(session);
                    
                    session.start();
                    
                    AppContext.instance.getSessionList().addNewInProgressSession(session, SessonType.OUTGOING);
//                    AppContext.instance.setSession(session);
//                    AppContext.instance.setSessionByRefer(session);
                    
                    result = session;
                    
                } catch (ImsException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                
            } catch (ServiceClosedException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return result;
        }

        
        protected void onPostExecute(Session outgoingSession) {
            inviteActivity.setProgressBarIndeterminateVisibility(false);

            inviteActivity.dismissDialog(DIALOG_ID);
            enableDisableButtons(true);
            
            DlgParams dlgParams = AppContext.instance.getDialogIncomingReferParams();
            DialogOutgoingCall.showDialog(inviteActivity, dlgParams.getReferTo(), outgoingSession);
            
            if (outgoingSession == null) {
                Toast.makeText(inviteActivity, "Error occured during Accept!", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(outgoingSession);
        }
    }
    
    
    private class ReferAcceptExecutionContext {
        private Session currentStartedSession;
        private Reference reference;
        
        public ReferAcceptExecutionContext(Session currentStartedSession, Reference reference) {
            this.currentStartedSession = currentStartedSession;
            this.reference = reference;
        }
        
        public Session getCurrentStartedSession() {
            return currentStartedSession;
        }

        public void setCurrentStartedSession(Session currentStartedSession) {
            this.currentStartedSession = currentStartedSession;
        }

        public Reference getReference() {
            return reference;
        }
        
        public void setReference(Reference reference) {
            this.reference = reference;
        }
    }

}
