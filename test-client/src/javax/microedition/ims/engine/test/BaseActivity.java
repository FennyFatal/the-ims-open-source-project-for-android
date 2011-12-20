package javax.microedition.ims.engine.test;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.ConnectionState;
import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.*;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.engine.test.AppContext.ConnectionStateType;
import javax.microedition.ims.engine.test.AppContext.ImsStateType;
import javax.microedition.ims.engine.test.AppContext.IncomingSessionUpdateAction;
import javax.microedition.ims.engine.test.invite.DialogCallInProgress;
import javax.microedition.ims.engine.test.invite.DialogCallSwitch;
import javax.microedition.ims.engine.test.invite.DialogIncomingCall;
import javax.microedition.ims.engine.test.invite.DialogVideoCall;
import javax.microedition.ims.engine.test.refer.DialogIncomingRefer;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseActivity extends Activity implements AppContext.ContextListener {
    protected final String TAG = getClass().getSimpleName();

    private static final int MSG_INCOMING_CALL = 0;
    private static final int MSG_INCOMING_CALL_CANCELED = 1;
    private static final int MSG_INCOMING_CALL_TERMINATED = 2;
    private static final int MSG_CONNECTION_STATE_CHANGED = 3;
    private static final int MSG_SESSION_UPDATE_RECEIVED = 4;
    private static final int MSG_SESSION_UPDATED = 5;
//    private static final int MSG_CALL_UPDATED = 5;
    private static final int MSG_CALL_UPDATE_FAILED = 6;
    private static final int MSG_PAGE_MESSAGE_RECEIVED = 8;
    private static final int MSG_REFERENCE_RECEIVED = 9;
    private static final int TERMINATE_VIDEOCALL = 10;

    private String remoteParty;
    private boolean terminatedByLocalParty;
    

    private final Handler mHandler = new Handler() {
        
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INCOMING_CALL: {
                    Session incomingSession = (Session) msg.obj;
                    DialogIncomingCall.showDialog(BaseActivity.this, remoteParty, incomingSession);
                    break;
                }
                case MSG_REFERENCE_RECEIVED: {
                    showDialog(DialogIncomingRefer.DIALOG_ID);
                    break;
                }
                case MSG_INCOMING_CALL_CANCELED: {
                    Log.i(TAG, "MSG_INCOMING_CALL_CANCELED");
                    dismissDialog(DialogIncomingCall.DIALOG_ID);
                    Toast.makeText(BaseActivity.this, R.string.msg_call_canceled_by_remote, Toast.LENGTH_SHORT).show();
                    break;
                }
                case MSG_INCOMING_CALL_TERMINATED: {
                    Session session = (Session) msg.obj;
                    terminateCall(false, true, session);
                    DialogCallInProgress.hideDialog(BaseActivity.this);
                    break;
                }
                case MSG_CONNECTION_STATE_CHANGED: {
                    Object[] states = (Object[]) msg.obj;
                    ImsStateType imsState = (ImsStateType) states[0];
                    ConnectionStateType connectionState = (ConnectionStateType) states[1];
                    handleConnectionChanges(imsState, connectionState);
                    break;
                }
                case MSG_SESSION_UPDATE_RECEIVED: {
                    Session session = (Session) msg.obj;
                    callUpdateReceived(session);
                    break;
                }
                case MSG_SESSION_UPDATED: {
                    Session session = (Session) msg.obj;
                    mediaUpdated(session.getMedia());
                    break;
                }
//                case MSG_CALL_UPDATED: {
//                    callUpdated();
//                    break;
//                }
                case MSG_CALL_UPDATE_FAILED: {
                    callUpdateFailed();
                    break;
                }
                case MSG_PAGE_MESSAGE_RECEIVED: {
                    doPageMessageReceived((PageMessage) msg.obj);
                    break;
                }
                
                default:
                    super.handleMessage(msg);
            }
        }
    };

    
    protected void onStart() {
        super.onStart();
        AppContext.instance.addCoreServiceListener(this);
        //testIncomingCall();
    }

    
    protected void onStop() {
        AppContext.instance.removeCoreServiceListener(this);
        stopTests();
        super.onStop();
    }

    private Timer testExecutor;

    private void stopTests() {
        if (testExecutor != null) {
            testExecutor.cancel();
        }
    }

    private void testIncomingCall() {
        testExecutor = new Timer();
        testExecutor.schedule(new TimerTask() {
            
            public void run() {
                incomeCallReceved("remote@dummy.com", null);
            }
        }, 5000);
    }

    
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;
        switch (id) {
        
            case DialogCallSwitch.DIALOG_ID: {
                dialog = DialogCallSwitch.createDialog(this);
                break;
            }
        
            case DialogIncomingCall.DIALOG_ID: {
                dialog = DialogIncomingCall.createDialog(this);

                break;
            }
            
            case DialogIncomingRefer.DIALOG_ID: {
                if (this instanceof InviteActivity) {
                    dialog = DialogIncomingRefer.createDialog((InviteActivity)this);
                } else {
                    throw new IllegalStateException("Incoming REFER possible only for InviteActivity");
                }
                
                break;
            }
            
            case DialogCallInProgress.DIALOG_ID: {
                dialog = DialogCallInProgress.createDialog(this);
                
                break;
            }
            
            default:
                dialog = null;
        }
        return dialog;
    }

    
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        
            case DialogCallSwitch.DIALOG_ID: {
                DialogCallSwitch.onPrepare(this, dialog);
                break;
            }
        
            case DialogIncomingCall.DIALOG_ID: {
                ((DialogIncomingCall)dialog).onPrepare();
                break;
            }
            
            case DialogIncomingRefer.DIALOG_ID: {
                ((DialogIncomingRefer)dialog).onPrepare();
                break;
            }

            case DialogCallInProgress.DIALOG_ID: {
                ((DialogCallInProgress)dialog).onPrepare();
                break;
            }
        }
    }

    public void terminateCall(boolean byLocalParty, boolean needSessionTerminate, Session session) {
        terminatedByLocalParty = byLocalParty;
        
        DialogCallInProgress.hideDialog(this);

        if (byLocalParty) {
            if (session != null) {
                
                if (needSessionTerminate) {
                    Log.i(TAG,"terminateCall");
                    new SessionTerminateTask().execute(session);
                }
                
            } else{
                Log.e(TAG,"Cannot terminateCall -session is null");                
            }
        } else
				DialogVideoCall.hideDialog(this);
        
        String rp = null;
        if ((rp = AppContext.instance.getSessionList().removeSessionAndActivateAnother(session)) != null) {
            DialogCallInProgress.showDialog(this, rp);
        }
    }
    
    
    public class SessionTerminateTask extends AsyncTask<Session, Void, Void> {
        protected final String TAG = "SessionTerminateTask";
        
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        
        protected Void doInBackground(Session... params) {
            Log.i(TAG, "start");
            Session session = params[0];
            session.terminate();
            Log.i(TAG, "finish");
            DialogVideoCall.hideDialog(BaseActivity.this);
            return null;
        }

        
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    

    
    public void incomeCallReceved(final String remoteParty, final Session session) {
        Log.i(TAG, "incomeCallReceved#");
        setRemoteParty(remoteParty);
        if (session != null) {
            session.setListener(new IncomingCallListener());
        }
        
        Message msg = new Message();
        msg.what = MSG_INCOMING_CALL;
        msg.obj = session; 
        mHandler.sendMessage(msg);
    }

    
    public class IncomingCallListener extends SessionAdapter {
        
        public void sessionStartFailed(Session session) {
            mHandler.sendEmptyMessage(MSG_INCOMING_CALL_CANCELED);
        }

        
        public void sessionUpdateReceived(Session session) {
            Message msg = new Message();
            msg.what = MSG_SESSION_UPDATE_RECEIVED;
            msg.obj = session; 
            mHandler.sendMessage(msg);
        }

        
        public void sessionTerminated(Session session) {
            Message msg = new Message();
            msg.what = MSG_INCOMING_CALL_TERMINATED;
            msg.obj = session; 
            mHandler.sendMessage(msg);
            
            AppContext.instance.getSessionList().removeSessionOnTermination(session);
        }
        
        
        public void sessionUpdated(Session session) {
            if (AppContext.instance.getDialogSendRefer() != null) {
                AppContext.instance.getDialogSendRefer().onSessionUpdated();
            }
            
            Message msg = new Message();
            msg.what = MSG_SESSION_UPDATED;
            msg.obj = session; 
            mHandler.sendMessage(msg);
        }

        
        public void sessionReferenceReceived(Session session, Reference reference) {
            doSessionReferenceReceived(session, reference);
        }
    }
    
    
    /**
     * Entry point for incoming reference from CoreDervice
     */
    
    public void referenceReceived(CoreService service, Reference reference) {
        
        doSessionReferenceReceived(null, reference);
        
    }

    protected void callUpdateReceived(Session session) {
    	for(Media media: session.getMedia()) {
    		if(media.getState() == Media.STATE_PENDING) {
    			media.setMediaListener(new MediaRegistry.MediaListenerImpl());
    		}
    	}
    	
        try {
            if (AppContext.instance.getActionOnSessionUpdate() == IncomingSessionUpdateAction.ACCEPT) {
                session.accept();
                
                callUpdated(session);
                
            } else if (AppContext.instance.getActionOnSessionUpdate() == IncomingSessionUpdateAction.REJECT) {
                session.reject();
                
            } else {
                throw new IllegalStateException("Unsupported case!!! # callUpdateReceived()");
            }

        } catch (ImsException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    protected void callUpdated(Session session) {
        Toast.makeText(this, "Session updated", Toast.LENGTH_SHORT).show();

        Media[] medias = session.getMedia();
        mediaUpdated(medias);
    }

    protected void callUpdateFailed() {
        Toast.makeText(this, "Session update failed", Toast.LENGTH_SHORT).show();
    }

    
    public void connectionStateChanged(ImsStateType imsState, ConnectionStateType connectionState) {
        Log.i(TAG, "connectionStateChanged#imsState: " + imsState + ", connectionState = " + connectionState);
        Message message = new Message();
        message.what = MSG_CONNECTION_STATE_CHANGED;
        message.obj = new Object[]{imsState, connectionState};
        mHandler.sendMessage(message);
    }

    protected void setRemoteParty(String remoteParty) {
        this.remoteParty = remoteParty;
    }

    private void handleConnectionChanges(final ImsStateType imsState, final ConnectionStateType connectionState) {
        final int connTextResId;
        final int imageResId;

        switch (connectionState) {
            case CONNECTION_RESUMED:
                connTextResId = imsState == ImsStateType.IMS_CONNECTED ? R.string.connection_state_resumed_ims_connected : R.string.connection_state_resumed_ims_disconnected;
                break;
            case CONNECTION_SUSPENDED:
                connTextResId = imsState == ImsStateType.IMS_CONNECTED ? R.string.connection_state_suspended_ims_connected : R.string.connection_state_suspended_ims_disconnected;
                break;
            default:
                assert false;
                return;
        }

        imageResId = (imsState == ImsStateType.IMS_CONNECTED && connectionState == ConnectionStateType.CONNECTION_RESUMED ? R.drawable.state_active : R.drawable.state_inactive);

        ((ImageView) findViewById(R.id.connection_lbl)).setImageResource(imageResId);
        ((TextView) findViewById(R.id.connection_state)).setText(connTextResId);
    }

    protected void updateConnectionState(ConnectionState connectionState) {
        if (connectionState != null) {

            ImsStateType imsStateType = ImsStateType.valueOf(connectionState.isConnected());
            ConnectionStateType connectionStateType = ConnectionStateType.valueOf(connectionState.isSuspended());

            handleConnectionChanges(imsStateType, connectionStateType);
        } else {
            handleConnectionChanges(ImsStateType.IMS_DISCONNECTED, ConnectionStateType.CONNECTION_RESUMED);
        }
    }

    public void mediaUpdated(Media[] medias){
    	
    };

    private void doPageMessageReceived(final PageMessage pageMessage) {
        Log.i(TAG, "doPageMessageReceived#pageMessage = " + pageMessage);
        Toast.makeText(
                this,
                String.format("Message received from '%s': content type = '%s', content = %s", 
                        pageMessage.getRemoteUserId()[0], 
                        pageMessage.getContentType(),
                        new String(pageMessage.getContent())),
                Toast.LENGTH_LONG).show();
    }

    
    public void pageMessageReceived(final PageMessage pageMessage) {
        Message message = new Message();
        message.what = MSG_PAGE_MESSAGE_RECEIVED;
        message.obj = pageMessage;
        mHandler.sendMessage(message);
    }
    

	public void coreServiceClosed(CoreService service, ReasonInfo reason) {
		
	}
    
    
    protected void doSessionReferenceReceived(Session session, Reference reference) {
        //TODO:it is temporary, should be moved back to DialogIncomingRefer 
        try {
            reference.accept();
        } catch (ServiceClosedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        //TODO:it is temporary, should be moved back to DialogIncomingRefer
        
        AppContext.instance.setDialogIncomingReferParams(
            new DialogIncomingRefer.DlgParams(
                reference,
                reference.getReferToUserId(),
                reference.getReferMethod(),
                session
            )
        );
        
        Log.i("INFO_test", "after set dialog params");
        
        mHandler.sendEmptyMessage(MSG_REFERENCE_RECEIVED);
    }

    public boolean isTerminatedByLocalParty() {
        return terminatedByLocalParty;
    }
    
}
