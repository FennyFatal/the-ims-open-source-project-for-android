package javax.microedition.ims.engine.test.invite;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.Toast;

import javax.microedition.ims.core.Session;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.engine.test.AppContext;
import javax.microedition.ims.engine.test.BaseActivity;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.media.IVideoCallback;
import javax.microedition.ims.media.Player;

public class DialogVideoCall extends Dialog implements IVideoCallback {
    protected final static String TAG = "DialogVideoCall";

    public static final int DIALOG_ID = 41;
    
    private BaseActivity baseActivity;
    private Player mPlayer;
    private static boolean mShow;

    public DialogVideoCall(BaseActivity baseActivity) {
        super(baseActivity);
        this.baseActivity = baseActivity;
    }
    
    public static DialogVideoCall createDialog(BaseActivity baseActivity) {
		Log.d(TAG, "DialogVideoCall createDialog");
        final DialogVideoCall dialog = new DialogVideoCall(baseActivity);
        
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_video_call);
        dialog.setTitle(R.string.title_call_in_progress);

        dialog.findViewById(R.id.btn_bye).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
                dialog.baseActivity.terminateCall(true, true, session);
            }
        });
        
        dialog.findViewById(R.id.btn_video_remove).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
                Media[] medias = session.getMedia();
                for (int i = 0; i < medias.length; i++) {
                	Media media = medias[i];
            		String mediaDescription = media.getMediaDescriptors()[0].getMediaDescription();
            		                
            		if(mediaDescription.startsWith("video")) {
            			session.removeMedia(media);
            		}
                }
            }
        });

        dialog.setOnDismissListener(new Dialog.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                Chronometer chronometer = (Chronometer) dialog.findViewById(R.id.chronometer_call_in_progress);
                chronometer.stop();
                Toast.makeText(dialog.baseActivity, dialog.baseActivity.isTerminatedByLocalParty() ? R.string.msg_call_terminated_by_local : R.string.msg_call_terminated_by_remote, Toast.LENGTH_SHORT).show();
            }
        });

        return dialog;
    }

    public void onPrepare() {
		Log.d(TAG, "DialogVideoCall prepareDialog");
		DlgParams dlgParams = AppContext.instance.getDialogVideoCallParams();

        Log.d(TAG, "chronometer start");
		Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer_call_in_progress);
		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.start();

        Session session = AppContext.instance.getSessionList().getInProgressSession().getSession();
        Media[] medias = session.getMedia();

        Log.d(TAG, "DialogVideoCall medias: "+medias.length);

        if (medias.length == 0) return;

        boolean canWrite = false, canRead = false;
        for (int i = 0; i < medias.length; i++) {
        	try {
        		StreamMedia media = (StreamMedia)medias[i];
        		String mediaDescription = media.getMediaDescriptors()[0].getMediaDescription();
        		                
        		if(mediaDescription.startsWith("video")) {
        			canWrite = media.canWrite();
        			canRead = media.canRead();
        			if (canWrite) {
        				mPlayer = media.getSendingPlayer();
        			} else if(canRead){
        				mPlayer = media.getReceivingPlayer();
        			} 
        			break;
        		}
        	}
        	catch (Exception e) {
				Log.d(TAG, "mPlayer = null. "+e.toString());
        		mPlayer = null;
        	}
        }

		if (mPlayer == null)	return;
		
		
		SurfaceView svLocal = canWrite? new SurfaceView(baseActivity): null;
		
		GLSurfaceView glSurfaceView = canRead? new GLSurfaceView(baseActivity): null;
		Log.d(TAG, "mPlayer.setSurfaces");
		mPlayer.setSurfaces(svLocal, glSurfaceView, mPlayer.getChannel());
		
		if(canWrite){
			LinearLayout mLlLocalSurface = (LinearLayout) findViewById(R.id.llLocalView);
	        //if (mLlLocalSurface!=null) {
				mLlLocalSurface.removeAllViews();
				mLlLocalSurface.addView(svLocal);
			//}
		}

		if(canRead) {
			LinearLayout mLlRemoteSurface = (LinearLayout) findViewById(R.id.llRemoteView);
			//if (mLlRemoteSurface!=null) {
				mLlRemoteSurface.removeAllViews();
				mLlRemoteSurface.addView(glSurfaceView);
			//}
		}

        mPlayer.setCallback(mPlayer.getChannel(), this);
    }

    public static void showDialog(Activity activity, String remoteParty) {
		Log.d(TAG, "showDialog. "+remoteParty);
        AppContext.instance.setDialogVideoCallParams(new DlgParams(remoteParty));
        
        activity.showDialog(DIALOG_ID);
        mShow = true;
    }
    
    public static void hideDialog(Activity activity) {
		Log.d(TAG, "hideDialog");
        if (mShow)
            activity.dismissDialog(DIALOG_ID);
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

    public void updateVideoStats(int frameRateI, int bitRateI, int frameRateO, int bitRateO, int packetLoss) {
        Log.i(TAG,  "Incoming: framerate: "+frameRateI+", bitrate: "+(bitRateI/1024)+
                    " Outgoing: framerate: "+frameRateO+", bitrate: "+(bitRateO/1024));
    }
}
