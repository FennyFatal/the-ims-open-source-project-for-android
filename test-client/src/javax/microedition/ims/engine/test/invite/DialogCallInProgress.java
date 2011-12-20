package javax.microedition.ims.engine.test.invite;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.core.Session;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.engine.test.AppContext;
import javax.microedition.ims.engine.test.AppContext.IncomingSessionUpdateAction;
import javax.microedition.ims.engine.test.BaseActivity;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.engine.test.refer.DialogSendRefer;
import javax.microedition.ims.media.Player;

public class DialogCallInProgress extends Dialog implements View.OnTouchListener {

    protected final static String TAG = "DialogCallInProgress";
    
    public static final int DIALOG_ID = 0;
    
    private BaseActivity baseActivity;
    private Player dtmfPlayer;
    private static boolean mShow;

    public DialogCallInProgress(BaseActivity baseActivity) {
        super(baseActivity);
        this.baseActivity = baseActivity;
    }
    	
	
	public boolean onTouch(View v, MotionEvent event) { 
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			/* We hit the other cases often, so getting control is within the switch */
			dtmfPlayer.startDtmf(((TextView) v).getText().charAt(0));
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			dtmfPlayer.stopDtmf();
			break;
		}
			
		return false;
    }
    
    public static DialogCallInProgress createDialog(final BaseActivity baseActivity) {
        
        final DialogCallInProgress dialog = new DialogCallInProgress(
            baseActivity
        );
        
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_call_in_progress);
        dialog.setTitle(R.string.title_call_in_progress);

        dialog.setOnDismissListener(new Dialog.OnDismissListener() {
            
            public void onDismiss(DialogInterface dialogInterface) {
                Chronometer chronometer = (Chronometer) dialog.findViewById(R.id.chronometer_call_in_progress);
                chronometer.stop();
                //Toast.makeText(baseActivity, baseActivity.isTerminatedByLocalParty() ? R.string.msg_call_terminated_by_local : R.string.msg_call_terminated_by_remote, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.findViewById(R.id.btn_bye).setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
                baseActivity.terminateCall(true, true, session);
            }
        });
        
        dialog.findViewById(R.id.call_in_progress_btn_switch).setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                DialogCallSwitch.showDialog(baseActivity);
            }
        });
        
        final ToggleButton toggleButton = (ToggleButton)dialog.findViewById(R.id.call_in_progress_btn_hold);
        toggleButton.setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
                    
                    boolean puttSessionOnHold = toggleButton.isChecked();
                    if(puttSessionOnHold) {
                        boolean updated = updateSession(Media.DIRECTION_SEND, session);
                        if(updated) {
                            //toggleButton.toggle();
                            Toast.makeText(baseActivity, "Call on hold", Toast.LENGTH_SHORT).show();    
                        }
                    } else {
                        boolean updated = updateSession(Media.DIRECTION_SEND_RECEIVE, session);
                        if(updated) {
                            //toggleButton.toggle();
                            Toast.makeText(baseActivity, "Call resumed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
        
        dialog.findViewById(R.id.call_in_progress_btn_call_transfer).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    DlgParams dlgParams = AppContext.instance.getDialogCallInProgressParams();
                    
                    Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
                    
                    DialogSendRefer.showDialog(
                        new DialogSendRefer.DlgParams(
                            session, //currentStartedSession
                            dlgParams.getRemoteParty() //to
                        ),
                        baseActivity
                    );
                }
            }
        );
        
        
        RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.incoming_sesion_update_accept_reject);
        
        if (AppContext.instance.getActionOnSessionUpdate() == IncomingSessionUpdateAction.ACCEPT) {
            radioGroup.check(R.id.incoming_sesion_update_accept);
        } else if (AppContext.instance.getActionOnSessionUpdate() == IncomingSessionUpdateAction.REJECT) {
            radioGroup.check(R.id.incoming_sesion_update_reject);
        } else {
            throw new IllegalStateException("Unsupported case!");
        }
        
        radioGroup.setOnCheckedChangeListener(
            new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.incoming_sesion_update_accept) {
                        AppContext.instance.setActionOnSessionUpdate(IncomingSessionUpdateAction.ACCEPT);
                        
                    } else if (checkedId == R.id.incoming_sesion_update_reject) {
                        AppContext.instance.setActionOnSessionUpdate(IncomingSessionUpdateAction.REJECT);
                        
                    } else {
                        throw new IllegalStateException("Unsupported case!");
                    }
                }
            }
        );
        
        
        dialog.findViewById(R.id.ButtonDTMF0).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF1).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF2).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF3).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF4).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF5).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF6).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF7).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF8).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMF9).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMFstar).setOnTouchListener(dialog);
        dialog.findViewById(R.id.ButtonDTMFhash).setOnTouchListener(dialog);
        
        return dialog;
    }
    
    public void onPrepare() {
        DlgParams dlgParams = AppContext.instance.getDialogCallInProgressParams();
        
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(dlgParams.getRemoteParty());

        Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
        Media[] medias = session.getMedia();
        baseActivity.mediaUpdated(medias);

        for (int i = 0; i < medias.length; i++) {
        	try {
        		if (medias[i].canWrite()) {
        			dtmfPlayer = ((StreamMedia) medias[i]).getSendingPlayer();
        			break;
        		}
        	}
        	catch (Exception e) {
        		dtmfPlayer = null;
        	}
        }

        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer_call_in_progress);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }
    
    public static boolean updateSession(int direction, Session session) {
        boolean retValue = true;
        
        Media[] medias = session.getMedia();
        if (medias != null) {
            for(Media media : medias) {
                media.setDirection(direction);
            }

            try {
                session.update();
            } catch (ImsException e) {
                Log.e(TAG, e.getMessage());
                retValue = false;
            }
        }
        return retValue;
    }
    
    public static void showDialog(Activity activity, String remoteParty) {
        AppContext.instance.setDialogCallInProgressParams(
            new DlgParams(remoteParty)
        );
        
        activity.showDialog(DIALOG_ID);
        mShow = true;
    }
    
    public static void hideDialog(Activity activity) {
        if (mShow) {
            try {
                activity.dismissDialog(DIALOG_ID);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        mShow = false;
    }
    
    
    public static class DlgParams {
        private String remoteParty;

        public DlgParams(String remoteParty) {
            this.remoteParty = remoteParty;
        }

        public String getRemoteParty() {
            return remoteParty;
        }

        public void setRemoteParty(String remoteParty) {
            this.remoteParty = remoteParty;
        }
    }
    
}
