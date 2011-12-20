package javax.microedition.ims.engine.test;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.CoreService;
import javax.microedition.ims.core.Reference;
import javax.microedition.ims.core.Session;
import javax.microedition.ims.core.SessionListener;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.engine.test.MediaRegistry.MediaBuilder;
import javax.microedition.ims.engine.test.MediaRegistry.MediaBuilderType;
import javax.microedition.ims.engine.test.SessionList.SessonType;
import javax.microedition.ims.engine.test.invite.DialogCallInProgress;
import javax.microedition.ims.engine.test.invite.DialogOutgoingCall;
import javax.microedition.ims.engine.test.invite.DialogOutgoingCall.DlgParams;
import javax.microedition.ims.engine.test.invite.DialogVideoCall;
import javax.microedition.ims.engine.test.refer.DialogIncomingRefer;
import javax.microedition.ims.engine.test.refer.DialogSendRefer;
import javax.microedition.ims.engine.test.refer.DialogSendReferProgress;
import javax.microedition.ims.engine.test.util.ContactsHolder;

public class InviteActivity extends BaseActivity {
    private static final String REMOTE_IDENTITY_POS = "remoteIdentityPos";
    //private static final String FULL_URI_REGEXP = "(\\w+:[^@:\\s]+@[^@:\\s]+)";

    private static final int MSG_UPDATE_CALL_STATE = 0;
    private static final int MSG_CALL_ESTABLISHED = 1;
    private static final int MSG_CALL_REJECTED = 2;
    private static final int MSG_CALL_TERMINATED = 3;
    private static final int MSG_SESSION_UPDATE_RECEIVED = 4;
    private static final int MSG_CALL_UPDATED = 5;
    private static final int MSG_CALL_UPDATE_FAILED = 6;

    private static final int SESSION_START = 6;

    private static final int CODE_RINGING = 180;
    private static final int CODE_SESSION_PROGRESS = 183;

    private AsyncTask<String, Void, Object[]> inviteTask;

    //private MediaStateChecker mediaChecker1, mediaChecker2, mediaChecker3;
    private CheckBox initialMedia1, initialMedia2/*, initialMedia3*/;

    MediaStateChecker stateChecker1, stateChecker2, stateChecker3;

    // the ringtone
    private Ringtone ringtone;

    //TODO need to review
    private boolean userCanceled;

    private final Handler mHandler = new Handler() {
        
        public void handleMessage(Message msg) {
            System.out.println("Message arrived: " + msg);
            switch (msg.what) {
            case MSG_UPDATE_CALL_STATE: {
                callStateChanged(msg.arg1, (String) msg.obj);
                break;
            }
            case MSG_CALL_ESTABLISHED: {
                callEstablished((Session) msg.obj);
                break;
            }
            case MSG_CALL_REJECTED: {
                callRejected((javax.microedition.ims.core.Message[])msg.obj);
                clearMedias();
                break;
            }
            case MSG_CALL_TERMINATED: {
                Session session = (Session) msg.obj;
                callTerminated(session);
                clearMedias();
                break;
            }
            case MSG_SESSION_UPDATE_RECEIVED: {
                Session session = (Session) msg.obj;
                callUpdateReceived(session);
                break;
            }
            case MSG_CALL_UPDATED: {
                Session session = (Session) msg.obj;
                callUpdated(session);
                break;
            }
            case MSG_CALL_UPDATE_FAILED: {
                callUpdateFailed();
                //Session session = (Session) msg.obj;
                //mediaUpdated(session.getMedia());
                break;
            }

            default:
                super.handleMessage(msg);
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create the ringtone
        ringtone = RingtoneManager.getRingtone(getApplicationContext(),
                Uri.parse("android.resource://javax.microedition.ims.engine.test/raw/ringtone"));

        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTitle(String.format("%1$s, %2$s %3$s", getTitle(), getText(R.string.activity_pid), android.os.Process.myPid()));
        setContentView(R.layout.invite);


        Spinner identityControl = (Spinner) findViewById(R.id.remote_identities);
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        //        this, remoteIdentitiesItems, android.R.layout.simple_spinner_item);
        //String[] remoteIdentities = getResources().getStringArray(remoteIdentitiesItems);
        String[] remoteIdentities = ContactsHolder.getContacts(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, remoteIdentities);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        identityControl.setAdapter(adapter);

        //restore previously stored data
        int remoteIdentityPos = savedInstanceState == null ? 0 : savedInstanceState.getInt(REMOTE_IDENTITY_POS);
        identityControl.setSelection(remoteIdentityPos);
        
        findViewById(R.id.invite_btn).setOnClickListener(inviteListener);
        
        findViewById(R.id.show_refer_dlg_btn).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        DialogSendRefer.showDialog(
                                new DialogSendRefer.DlgParams(
                                        null, //currentStartedSession
                                        getRemoteParty() //to
                                ),
                                InviteActivity.this
                        );
                    }
                }
        );
        findViewById(R.id.show_subscribe_dlg_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(InviteActivity.this, Subscription281Activity.class));
                }
            }
        );
        findViewById(R.id.show_phbk_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(InviteActivity.this, PhbkActivity.class));
                }
            }
        );
    
        findViewById(R.id.msrp_msg_act_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(InviteActivity.this, MsrpChatActivity.class));
                }
            }
        );
        findViewById(R.id.msg_act_btn).setOnClickListener(messageActivityListener);
        findViewById(R.id.btn_show_hard_state_presence).setOnClickListener(presenceHardStateShowListener);
        findViewById(R.id.btn_show_jsr325_presence).setOnClickListener(presenceJsr325ShowListener);
        findViewById(R.id.btn_status_test_subscribe).setOnClickListener(testSubscribeListener);  
        
        findViewById(R.id.conference_act_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(InviteActivity.this, ConferenceActivity.class));
                }
            }
        );

        findViewById(R.id.btn_status_test_subscribe_list).setOnClickListener(testSubscribeListListener);  

        findViewById(R.id.xdm_docs_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(InviteActivity.this, XdmDocsActivity.class));
                }
            }
        );
        findViewById(R.id.cont_list_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(InviteActivity.this, ContListActivity.class));
                }
            }
        );

        
        initialMedia1 = (CheckBox) findViewById(R.id.initial_media1);
        initialMedia2 = (CheckBox) findViewById(R.id.initial_media2);
        //initialMedia3 = (CheckBox) findViewById(R.id.initial_media3);
    }

    private OnClickListener messageActivityListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(InviteActivity.this, MessageActivity.class));
        }
    };
    
    private OnClickListener presenceHardStateShowListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(InviteActivity.this, PresenceHardStateActivity.class));
        }
    };

    private OnClickListener presenceJsr325ShowListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(InviteActivity.this, Presence325Activity.class));
        }
    };
    
    private OnClickListener testSubscribeListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(InviteActivity.this, ContactPresenseTestActivity.class));
        }
    };
    
    private OnClickListener testSubscribeListListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(InviteActivity.this, ListPresenseTestActivity.class));
        }
    };

    
    protected void onStart() {
        super.onStart();
        updateConnectionState(AppContext.instance.getConnectionState());
        //testOutgoingCall();
    }

/*    
    private void testOutgoingCall() {
        invite(getRemoteParty());

        final Timer testExecutor = new Timer();
        testExecutor.schedule(new TimerTask() {
            
            public void run() {
                mHandler.sendEmptyMessage(MSG_CALL_ESTABLISHED);
                //mHandler.sendEmptyMessage(MSG_CALL_REJECTED);
                testExecutor.schedule(new TimerTask() {
                    
                    public void run() {
                        mHandler.sendEmptyMessage(MSG_CALL_TERMINATED);

                    }
                }, 5000);
            }
        }, 5000);
    }
*/

    
    protected void onDestroy() {
        super.onDestroy();

        if (inviteTask != null) {
            inviteTask.cancel(true);
        }

        Log.i(TAG, "onDestroy");
    }

    
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;

        switch (id) {
        case DialogOutgoingCall.DIALOG_ID: {
            dialog = DialogOutgoingCall.createDialog(this);
        }
        break;

        case DialogSendRefer.DIALOG_ID: {
            dialog = DialogSendRefer.createDialog(this);
        }
        break;
        
        case DialogSendReferProgress.DIALOG_ID: {
            dialog = DialogSendReferProgress.createDialog(this);
        }
        break;

        case DialogIncomingRefer.DIALOG_ID: {
            dialog = DialogIncomingRefer.createDialog(this);
        }
        break;

        case DialogVideoCall.DIALOG_ID: {
            dialog = DialogVideoCall.createDialog(this);
        }
        break;

        case DialogCallInProgress.DIALOG_ID: {
            dialog = super.onCreateDialog(id);
            //Session session = AppContext.instance.getSession();

            CheckBox mediaCheckbox1 = (CheckBox) dialog.findViewById(R.id.progress_media1);
            CheckBox mediaCheckbox2 = (CheckBox) dialog.findViewById(R.id.progress_media2);
            //CheckBox mediaCheckbox3 = (CheckBox) dialog.findViewById(R.id.progress_media3);

            //TODO mediaType hardCoded
            stateChecker1 = new MediaStateChecker(MediaBuilderType.StreamMediaAudio, mediaCheckbox1, "audio");
            String remoteParty = AppContext.instance.getSessionList().getInProgressSession().getRemoteIdentity(); 
            stateChecker2 = new VideoStateChecker(mediaCheckbox2, remoteParty);
            //stateChecker3 = new MediaStateChecker(MediaBuilderType.BasicRelaibleMedia, mediaCheckbox3, "application");

            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
        }
        }

        return dialog;
    }

    
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case DialogOutgoingCall.DIALOG_ID: {
            ((DialogOutgoingCall)dialog).onPrepare();
            break;
        }

        case DialogSendReferProgress.DIALOG_ID: {
            ((DialogSendReferProgress)dialog).onPrepare();
            break;
        }
        
        case DialogIncomingRefer.DIALOG_ID: {
            ((DialogIncomingRefer)dialog).onPrepare();
            break;
        }

		case DialogVideoCall.DIALOG_ID: {
			((DialogVideoCall)dialog).onPrepare();  
			break;
		}

        case DialogCallInProgress.DIALOG_ID: {
            stateChecker1.mediaRemovedByRemote();
            stateChecker2.mediaRemovedByRemote();
            //stateChecker3.mediaRemovedByRemote();
            //final Media[] medias = session.getMedia();
            //mediaUpdated(medias);
            super.onPrepareDialog(id, dialog);
            break;
        }
        default: {
            super.onPrepareDialog(id, dialog);
        }
        }
    }

    
    public void mediaUpdated(Media[] medias) {
    	if(stateChecker1 != null) {
    		stateChecker1.checkUpdates(medias);	
    	}
        
    	if(stateChecker2 != null) {
    		stateChecker2.checkUpdates(medias);	
    	}
    	
    	if(stateChecker3 != null) {
    		stateChecker3.checkUpdates(medias);	
    	}
    }

    class VideoStateChecker extends MediaStateChecker {
    	private final String remoteParty;

		public VideoStateChecker(CheckBox mediaCheckbox, String remoteParty) {
			super(MediaBuilderType.StreamMediaVideo, mediaCheckbox, "video");
			this.remoteParty = remoteParty;
			//dlgParams.getRemoteParty()
			//DialogIncomingCall.DlgParams incCallParams = AppContext.instance.getDialogIncomingCallParams();
		}
		
		@Override
		protected void mediaAcceptedByRemote(Media media) {
			super.mediaAcceptedByRemote(media);
			showSurface();
		}
		
		@Override
		protected void mediaAddedByRemote(Media media) {
			super.mediaAddedByRemote(media);
			
			showSurface();
		}
		
		@Override
		protected void mediaRemovedByLocal() {
			super.mediaRemovedByLocal();
			hideSurface();
		}
		
		@Override
		protected void mediaRemovedByRemote() {
			super.mediaRemovedByRemote();
			hideSurface();
		}
		
		private void showSurface() {
			Log.d(TAG, "showVideoSurface");
			DialogVideoCall.showDialog(InviteActivity.this, remoteParty);
		}
		
		private void hideSurface() {
			Log.d(TAG, "hideVideoSurface");
			DialogVideoCall.hideDialog(InviteActivity.this);
		}
    }
    
    class MediaStateChecker implements OnCheckedChangeListener {
        private final MediaBuilder mediaBuilder;

        private Media addedMedia;
        private final CheckBox mediaCheckbox;
        private final String mediaType;

        private boolean disableListener;

        public MediaStateChecker(final MediaBuilderType builderType,
                final CheckBox mediaCheckbox, final String mediaType) {
            assert mediaCheckbox != null;
            this.mediaType = mediaType;
            this.mediaCheckbox = mediaCheckbox;
            this.mediaCheckbox.setOnCheckedChangeListener(this);
            this.mediaBuilder = MediaRegistry.INSTANCE.findBuilder(builderType);
        }

        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (disableListener) return;

            Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
            if (isChecked) {
            	Media addMedia = mediaBuilder.addMedia(session);
            	mediaAddedByLocal(addMedia);
            } else {
            	mediaRemovedByLocal();
            }
        }

		public void checkUpdates(final Media[] medias) {
            Media media = findMedia(medias);
            if (addedMedia == null) {
            	if(media != null) {
            		if(media.getState() == Media.STATE_ACTIVE) {
            			mediaAddedByRemote(media);	
            		} else if(media.getState() == Media.STATE_DELETED){
            			mediaDeclinedByRemote(media);
            		}
            	}
            	
            } else if (addedMedia != null) {
            	if(media == null || media.getState() == Media.STATE_DELETED) {
            		mediaRemovedByRemote();	
            	} else if(media != null && media.getState() == Media.STATE_ACTIVE) {
            		mediaAcceptedByRemote(media);
            	}
            } else {
                Log.e(TAG, "checkUpdates# unhandled state");
            }
        }

        private Media findMedia(final Media[] medias) {
            Media retMedia = null;
            for (Media media : medias) {
                String mediaDescription = media.getMediaDescriptors()[0].getMediaDescription();
                if (mediaDescription.startsWith(mediaType)) {
                    retMedia = media;
                    break;
                }
            }
            return retMedia;
        }
        
        protected void mediaAddedByLocal(final Media media) {
        	addedMedia = media;
        	
        	Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
            try {
                session.update();
            } catch (ImsException e) {
                Log.e(TAG, "Cannot update session", e);
            }
        }

        protected void mediaRemovedByLocal() {
        	Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
            mediaBuilder.removeMedia(session, addedMedia);
            addedMedia = null;
		}
        
        protected void mediaAcceptedByRemote(final Media media) {
        	
        }
        
        protected void mediaDeclinedByRemote(final Media media) {
        	
        }
        
        protected void mediaAddedByRemote(final Media media) {
            this.addedMedia = media;
            setCheckedInternally(true);
        }

        protected void mediaRemovedByRemote() {
            this.addedMedia = null;
            setCheckedInternally(false);
        }

        private void setCheckedInternally(boolean checked) {
            disableListener = true;
            mediaCheckbox.setChecked(checked);
            disableListener = false;
        }

        //public void addMedia(final Session session) {
        //    mediaBuilder.addMedia(session);
        //}
    }

    ;


    private OnClickListener inviteListener = new OnClickListener() {
        public void onClick(View v) {
            SharedPreferences preferences = SettingsHelper.extractPreferences(InviteActivity.this);
            
            if (preferences != null)
                MediaRegistry.INSTANCE.setVideoCodec(
                    preferences.getInt(getResources().getString(R.string.codec_preferences_category), 0),
                    preferences.getInt(getResources().getString(R.string.codecsize_preferences_category), 0)
                );
            invite(getRemoteParty());
        }
    };
    
    

    private void invite(final String remoteIdentity) {
        if (TextUtils.isEmpty(remoteIdentity)) {
            Toast.makeText(this, R.string.wrong_argument_msg, Toast.LENGTH_SHORT).show();
        //} else if(!isMediasChecked()) {
        } else if(false) {
            Toast.makeText(this, R.string.media_empty_msg, Toast.LENGTH_SHORT).show();
        } else {
            String localParty = extractLocalParty();
            (inviteTask = new InviteTask()).execute(localParty, remoteIdentity);
        }
    }

    private String extractLocalParty() {
        return isAnonymousCall()? "sip:anonymous@anonymuos.invalid": null/*AppContext.instance.getConnection().getLocalUserId()*/;
    }
    private boolean isAnonymousCall() {
        return ((Checkable)findViewById(R.id.local_anonym)).isChecked();
    }

    private boolean isMediasChecked() {
        boolean ret = false;
        if (initialMedia1.isChecked()) {
            ret = true;
        } else if (initialMedia2.isChecked()) {
            ret = true;
        } /*else if (initialMedia3.isChecked()) {
            ret = true;
        }*/
        return ret;
    }
    
    private int getRemotePartyPosition() {
        Spinner remotePartiesControl = (Spinner) findViewById(R.id.remote_identities);
        return remotePartiesControl.getSelectedItemPosition();
    }

    
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");

        outState.putInt(REMOTE_IDENTITY_POS, getRemotePartyPosition());
        super.onSaveInstanceState(outState);
    }

    private enum InviteErrorCode {
        ILLEGAL_ARGUMENT,
        SESSION_CANNOT_BE_STARTED,
        SERVICE_ALREADY_CLOSED,
        SESSION_CANNOT_BE_CREATED,
    }

    private class InviteTask extends AsyncTask<String, Void, Object[]> {

        
        protected Object[] doInBackground(String... params) {
            Session outgoingSession = null;
            InviteErrorCode errorCode = null;

            String localIdentity = params[0];
            String remoteIdentity = params[1];

            CoreService connection = AppContext.instance.getConnection();
            if (connection != null) {
                try {
                    //DtfmPayload dtfmPayload = SettingsHelper.extractDtfmPayload(InviteActivity.this);
                    outgoingSession = connection.createSession(localIdentity, remoteIdentity/*, dtfmPayload*/);

                    //AppContext.instance.setSession(session);
                    AppContext.instance.getSessionList().addNewInProgressSession(outgoingSession, SessonType.OUTGOING);
                    setRemoteParty(remoteIdentity);
                } catch (IllegalArgumentException e) {
                    errorCode = InviteErrorCode.ILLEGAL_ARGUMENT;
                    Log.e(TAG, e.getMessage(), e);
                } catch (ServiceClosedException e) {
                    errorCode = InviteErrorCode.SERVICE_ALREADY_CLOSED;
                    Log.e(TAG, e.getMessage(), e);
                } catch (ImsException e) {
                    errorCode = InviteErrorCode.SESSION_CANNOT_BE_CREATED;
                    Log.e(TAG, e.getMessage(), e);
                }

                if (outgoingSession != null) {

                    //session.setListener(new CallListener());//?

                    buildInitialMedias(outgoingSession);

                    outgoingSession.setListener(new OutgoingCallListener());

                    try {
                        outgoingSession.start();
                    } catch (ImsException e) {
                        errorCode = InviteErrorCode.SESSION_CANNOT_BE_STARTED;
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }

            return new Object[]{outgoingSession, errorCode};
        }

        private void buildInitialMedias(final Session outgoingSession) {
            if (initialMedia1.isChecked()) {
                MediaRegistry.INSTANCE.findBuilder(MediaBuilderType.StreamMediaAudio).addMedia(outgoingSession);
            }
            if (initialMedia2.isChecked()) {
                MediaRegistry.INSTANCE.findBuilder(MediaBuilderType.StreamMediaVideo).addMedia(outgoingSession);
            }
/*            if (initialMedia3.isChecked()) {
                MediaRegistry.INSTANCE.findBuilder(MediaBuilderType.BasicRelaibleMedia).addMedia(outgoingSession);
            }
*/        }

        
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);

            findViewById(R.id.invite_btn).setEnabled(false);

            //DialogOutgoingCall.showDialog(InviteActivity.this, getRemoteParty());
        }

        
        protected void onPostExecute(Object[] objects) {
            Session outgoingSession = (Session) objects[0];
            InviteErrorCode errorCode = (InviteErrorCode) objects[1];

            setProgressBarIndeterminateVisibility(false);
            
            if(outgoingSession != null) {
                DialogOutgoingCall.showDialog(InviteActivity.this, getRemoteParty(), outgoingSession);
            }

            Button inviteBtn = (Button) findViewById(R.id.invite_btn);
            inviteBtn.setEnabled(true);

            if (outgoingSession == null) {
                DialogOutgoingCall.hideDialog(InviteActivity.this);

                int textResId;
                switch (errorCode) {
                case ILLEGAL_ARGUMENT:
                    textResId = R.string.wrong_argument_msg;
                    break;
                case SESSION_CANNOT_BE_STARTED:
                    textResId = R.string.session_cannot_be_started_msg;
                    break;
                case SESSION_CANNOT_BE_CREATED:
                    textResId = R.string.session_cannot_be_created_msg;
                    break;
                case SERVICE_ALREADY_CLOSED:
                    textResId = R.string.service_already_closed;
                    break;
                default:
                    textResId = R.string.msg_call_started_failed;
                    break;
                }
                Toast.makeText(InviteActivity.this, textResId, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearMedias() {
        initialMedia1.setChecked(false);
        initialMedia2.setChecked(false);
        //initialMedia3.setChecked(false);
    }

    public class OutgoingCallListener implements SessionListener {
        
        public void sessionAlerting(Session session) {
            Log.i(TAG, "sessionAlerting#");
            sessionStateUpdated(R.string.session_alerting_state);

            if (ringtone != null && isRingingAlerting(session)) {
                ringtone.play();
            }

            if (isSessionProgressAllertingNoMedia(session)) {
                if (ringtone != null && ringtone.isPlaying()) {
                    ringtone.stop();
                }
            }

            //TODO check history
            /* javax.microedition.ims.core.Message previousRequest = session.getPreviousRequest(javax.microedition.ims.core.Message.SESSION_START);
            String[] maxf = previousRequest.getHeaders("Max-Forwards"); 
            if(maxf != null && maxf.length > 0)
                Log.i(TAG, "maxf: " + maxf[0]);

            String[] contentLengths = previousRequest.getHeaders("Content-Length");
            assert contentLengths != null && contentLengths.length == 1;
            Log.i(TAG, "contentLengths: " + contentLengths[0]);

            String[] customs = previousRequest.getHeaders("Hello");
            //assert customs != null && customs.length == 1 && "world".equals(customs[0]);
            if(customs != null && customs.length > 0)
                Log.i(TAG, "customs: " + customs[0]);            

            javax.microedition.ims.core.Message[] previousResponses = session.getPreviousResponses(javax.microedition.ims.core.Message.SESSION_START);
            Log.i(TAG, "previousResponses: " + previousResponses.length);
            for(javax.microedition.ims.core.Message response: previousResponses) {
                Log.i(TAG, "inCycle");
                int statusCode = response.getStatusCode();
                Log.i(TAG, "status code: " + statusCode);
                assert statusCode > 0;

                String responsePhrase =  response.getReasonPhrase();
                Log.i(TAG, "reason phrase: " + responsePhrase);
                assert responsePhrase != null;

                String[] responseFroms = response.getHeaders("From");
                assert responseFroms != null && responseFroms.length == 1;
                Log.i(TAG, "responseFrom: " + responseFroms[0]);
            }*/
        }

        private boolean isRingingAlerting(Session session) {
            javax.microedition.ims.core.Message[] responses = session.getPreviousResponses(SESSION_START);

            // checking whether we have received any responses already,
            // in case there are no responses, there is nothing to check, returning
            if (responses.length == 0) {
                return false;
            }

            // checking whether there has been no 183 Session Progress before
            for (int i = 0; i < responses.length - 1; i++) {
                if (responses[i].getStatusCode() == CODE_SESSION_PROGRESS) {
                    return false;
                }
            }

            return responses[responses.length - 1].getStatusCode() == CODE_RINGING;
        }

        private boolean isSessionProgressAllertingNoMedia(Session session) {
            javax.microedition.ims.core.Message[] responses = session.getPreviousResponses(SESSION_START);

            // checking whether we have received any responses already,
            // in case there are no responses, there is nothing to check, returning
            if (responses.length == 0) {
                return false;
            }

            // checking the media presence of the last response received
            boolean isSdpInMessage = false;
            if (responses.length > 0) {
                for (String header : responses[responses.length - 1].getHeaders("Content-Type")) {
                    if (header.startsWith("application\\sdp")) {
                        isSdpInMessage = false;
                        break;
                    }
                }
            }

            return responses[responses.length - 1].getStatusCode() == CODE_SESSION_PROGRESS && !isSdpInMessage;
        }

        
        public void sessionStarted(Session session) {
            Log.i(TAG, "sessionStarted#");

            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }

            sessionStateUpdated(R.string.session_started_state);

            Message acceptMessage = new Message();
            acceptMessage.what = MSG_CALL_ESTABLISHED;
            acceptMessage.obj = session; 
            
            mHandler.sendMessage(acceptMessage);
        }

        
        public void sessionStartFailed(Session session) {
            Log.i(TAG, "sessionStartFailed#");

            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }

            javax.microedition.ims.core.Message[] responses = session.getPreviousResponses(javax.microedition.ims.core.Message.SESSION_START);
            String reasonPhrase = (responses != null && responses.length > 0 ? responses[responses.length - 1].getReasonPhrase() : null);
            Log.i(TAG, "sessionStartFailed#reasonPhrase: " + reasonPhrase);
            sessionStateUpdated(R.string.session_startfailed_state, reasonPhrase);
            
            Message rejectMessage = new Message();
            rejectMessage.what = MSG_CALL_REJECTED;
            rejectMessage.obj = responses; 
            
            mHandler.sendMessage(rejectMessage);
        }

        public void sessionTerminated(Session session) {
            Log.i(TAG, "sessionTerminated#");
            sessionStateUpdated(R.string.session_terminated_state);

            Message msg = new Message();
            msg.what = MSG_CALL_TERMINATED;
            msg.obj = session;
            mHandler.sendMessage(msg);
            
            AppContext.instance.getSessionList().removeSessionOnTermination(session);
        }

        ;

        public void sessionUpdated(Session session) {
            Log.i(TAG, "sessionUpdated#");
            sessionStateUpdated(R.string.session_updated_state);
            
            Message msg = new Message();
            msg.what = MSG_CALL_UPDATED;
            msg.obj = session;
            mHandler.sendMessage(msg);

            if (AppContext.instance.getDialogSendRefer() != null) {
                AppContext.instance.getDialogSendRefer().onSessionUpdated();
            }
        }

        public void sessionUpdateFailed(Session session) {
            Log.i(TAG, "sessionUpdateFailed#");
            sessionStateUpdated(R.string.session_updatefailed_state);
            
            Message msg = new Message();
            msg.what = MSG_CALL_UPDATE_FAILED;
            msg.obj = session;
            mHandler.sendMessage(msg);
        }

        
        public void sessionUpdateReceived(Session session) {
            Log.i(TAG, "sessionUpdateReceived#");
            sessionStateUpdated(R.string.session_updatereceived_state);

            Message message = new Message();
            message.what = MSG_SESSION_UPDATE_RECEIVED;
            message.obj = session;
            mHandler.sendMessage(message);
        }

        private void sessionStateUpdated(int textResId) {
            sessionStateUpdated(textResId, null);
        }

        private void sessionStateUpdated(int textResId, String reasonPhrase) {
            Message message = new Message();
            message.what = MSG_UPDATE_CALL_STATE;
            message.arg1 = textResId;
            message.obj = reasonPhrase;
            mHandler.sendMessage(message);
        }

        /**
         * Entry point for incoming reference from Session
         */
        
        public void sessionReferenceReceived(Session session, Reference reference) {

            doSessionReferenceReceived(session, reference);

        }
    }

    private void callStateChanged(int callStateResId, String reasonPhrase) {
        CharSequence sessionState = (reasonPhrase == null ? getText(callStateResId) : String.format("%s, %s", getText(callStateResId), reasonPhrase));
        ((TextView) findViewById(R.id.invite_state)).setText(sessionState);
    }

    private void callEstablished(Session session) {
		Log.d(TAG, "callEstablished");

        DialogOutgoingCall.hideDialog(this);

		DlgParams dlgParams = AppContext.instance.getDialogOutgoingCallParams();

		
		if (initialMedia2.isChecked()) {
			//Log.d(TAG, "Video Checked");
			DialogVideoCall.showDialog(this, dlgParams.getRemoteParty());
		} else {
			//Log.d(TAG, "Other Checked");
			DialogCallInProgress.showDialog(this, dlgParams.getRemoteParty());
		}
    }

    private void callRejected(final javax.microedition.ims.core.Message[] messages) {
        DialogOutgoingCall.hideDialog(this);

        int messageId = -1;
        
        javax.microedition.ims.core.Message starCodeResponse = checkAndRetrieveStartCodeDialResponse(messages);
        
        if(starCodeResponse != null) {
            switch (starCodeResponse.getStatusCode()) {
            case 488:
                messageId = R.string.msg_starcode_dial_succeeded;
                break;
            case 500:
                messageId = R.string.msg_starcode_dial_failed;        
                break;
            default: {
                messageId = R.string.msg_starcode_dial_failed;
            }    
            } 
            
        } else {
            messageId = R.string.msg_call_started_failed;
        }
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
    }
    
    private javax.microedition.ims.core.Message checkAndRetrieveStartCodeDialResponse(javax.microedition.ims.core.Message[] messages) {
        javax.microedition.ims.core.Message retValue = null;
        if(messages.length > 0) {
            String[] toHeaders = messages[0].getHeaders("To");
            if(toHeaders.length > 0) {
                String decodedToUri = UriUtils.decodeUri(toHeaders[0]);
                if(UriUtils.isUriStarCode(decodedToUri)) {
                    retValue = retrieveStartCodeDialResponse(messages);
                } 
            } 
        }
        return retValue;
    }
    
    private javax.microedition.ims.core.Message retrieveStartCodeDialResponse(javax.microedition.ims.core.Message[] messages) {
        javax.microedition.ims.core.Message retValue = null;
        for(javax.microedition.ims.core.Message message: messages) {
            int statusCode = message.getStatusCode();
            if(statusCode == 488 || statusCode == 500) {
                retValue = message;
                break;
            }
        }
        return retValue;
    }

    private void callTerminated(Session session) {
        if (!userCanceled) {
            //TODO fix
            terminateCall(false, true, session);
            userCanceled = false;
        }
    }

    private String getRemoteParty() {
        final String retValue;
        
        TextView remoteIdentity = (TextView)findViewById(R.id.remote_identity);
        String userInput =  remoteIdentity.getText().toString();
        if(!TextUtils.isEmpty(userInput)) {
            retValue = userInput;
        } else {
            Spinner remoteIdentities = (Spinner) findViewById(R.id.remote_identities);
            int pos = remoteIdentities.getSelectedItemPosition();
            //retValue = getResources().getStringArray(remoteIdentitiesItems)[pos];
            retValue = ContactsHolder.getContacts(this)[pos];
        }

        return UriUtils.encodeUri(retValue);
    }

    public void setUserCanceled(boolean userCanceled) {
        this.userCanceled = userCanceled;
    }

    private static final int MENU_SETTINGS = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SETTINGS, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(this, "javax.microedition.ims.engine.test.Settings");
        startActivity(intent);
        return true;
    }
}
