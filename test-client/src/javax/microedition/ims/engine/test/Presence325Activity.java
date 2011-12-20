package javax.microedition.ims.engine.test;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.Connector;
import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.presence.*;
import java.io.IOException;

/**
 * This activity responsible for show presence functionality:
 * 1) hard state presence status
 * 
 * @author Andrei Khomushko
 *
 */
public class Presence325Activity extends BaseActivity{
    //private static final String PRESENCE_SERVICE_ID = "org.openmobilealliance:PoC-alert";
    private static final String PRESENCE_SERVICE_ID = "presence";

    private final String TAG = getClass().getSimpleName();
    
    private static final String SERVICE_NOTE_ID = "serviceNoteId";
    
    
    private static final int MSG_PUBLICATION_TERMINATED = 0;
    private static final int MSG_PUBLICATION_DELEVERED = 1;
    private static final int MSG_PUBLICATION_DELEVER_FAILED = 2;
    
    private static final int iconsItems = R.array.service_note_types;
    
    private AsyncTask<String, Void, Boolean> publishStatusTask;
    private AsyncTask<Void, Void, Boolean> unpublishStatusTask;
    
    private PresenceSource presenceSource;
    
    private final Handler mHandler = new Handler() {
        
        public void handleMessage(Message msg) {
            System.out.println("Message arrived: " + msg);
            switch (msg.what) {
            case MSG_PUBLICATION_TERMINATED: {
                showMessage("Publication terminated");
                break;
            }
            case MSG_PUBLICATION_DELEVERED: {
                showMessage("Publication delivered");
                break;
            }
            case MSG_PUBLICATION_DELEVER_FAILED: {
                showMessage("Publication delivery failed");
                break;
            }
            default:
                super.handleMessage(msg);
            }
        }
        
        private void showMessage(String message) {
            Toast.makeText(Presence325Activity.this, message, Toast.LENGTH_LONG).show();
        }
    };
    
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTitle(R.string.activity_presence_325_title);

        setContentView(R.layout.presence_jsr325);
        
        String userId = AppContext.instance.getConnection().getLocalUserId();
        ((TextView)findViewById(R.id.presence_entity)).setText(userId);
        
        Spinner serviceNotesSpinner = (Spinner) findViewById(R.id.service_notes);
        ArrayAdapter<CharSequence> noteTypesAdapter = ArrayAdapter.createFromResource(
                this, iconsItems, android.R.layout.simple_spinner_item);

        noteTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceNotesSpinner.setAdapter(noteTypesAdapter);

        Spinner capableSpinner = (Spinner) findViewById(R.id.video_capable_status);
        ArrayAdapter<CharSequence> statusTypesAdapter = ArrayAdapter.createFromResource(
                this, R.array.base_status_types, android.R.layout.simple_spinner_item);

        statusTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capableSpinner.setAdapter(statusTypesAdapter);
        
        // restore previously stored data
        int statusURLId = savedInstanceState == null ? 0
                : savedInstanceState.getInt(SERVICE_NOTE_ID);
        serviceNotesSpinner.setSelection(statusURLId);
        
        findViewById(R.id.btn_status_publish).setOnClickListener(publishStatusListener);
        findViewById(R.id.btn_status_unpublish).setOnClickListener(unpublishStatusListener);            
        
        findViewById(R.id.btn_back).setOnClickListener(backListener);
    }
    
    
    protected void onStart() {
        super.onStart();
        updateConnectionState(AppContext.instance.getConnectionState());
    }
    
    private OnClickListener publishStatusListener = new OnClickListener() {
        public void onClick(View v) {
            String serviceNote = getServiceNote();
            (publishStatusTask = new PublishStatusTask()).execute(serviceNote);
        }
    };
    
    private OnClickListener unpublishStatusListener = new OnClickListener() {
        public void onClick(View v) {
            (unpublishStatusTask = new UnpublishStatusTask()).execute();
        }
    };
    
    private OnClickListener backListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(Presence325Activity.this, InviteActivity.class));
        }
    };
    
    private String getServiceNote() {
        int pos = getServiceNotePosition();
        return getResources().getStringArray(iconsItems)[pos];
    }
    
    private boolean isVideoTupleOpen() {
        Spinner statusControl = (Spinner) findViewById(R.id.video_capable_status);
        int pos = statusControl.getSelectedItemPosition();
        return pos == 0;
    }
    
/*    private String getPresenceEntity() {
        return ((TextView)findViewById(R.id.presence_entity)).getText().toString();
    }
*/    
    private int getServiceNotePosition() {
        Spinner statusIconsControl = (Spinner) findViewById(R.id.service_notes);
        return statusIconsControl.getSelectedItemPosition();
    }
    
    private PresenceSource getPresenceSource(PresenceSourceListener listener) throws ServiceClosedException, ImsException {
        if(presenceSource == null) {
            presenceSource = createPresenceSource(listener);
        }
        return presenceSource;
    }

    private PresenceSource createPresenceSource(PresenceSourceListener presenceSourceListener) throws ServiceClosedException,
            ImsException {
        
        PresenceSource retValue;
        
        final String appId = getResources().getText(R.id.def_app_id).toString();
        String name = String.format("%1$s://%2$s;%3$s", "imspresence",
                appId, "sip:fake_uri@movial.com");
        PresenceService presenceService = (PresenceService) Connector.open(
                name, Presence325Activity.this);
        PresenceSource presenceSource = presenceService.createPresenceSource();
        presenceSource.setListener(presenceSourceListener);
        
        retValue = presenceSource;
        
        return retValue;
    }
    
    private void updateDocumentNote(PresenceDocument document, String serviceNote, 
            PresenceState videoCapableState) {
        
        DeviceInfo[] deviceInfos = document.getDeviceInfo();
        final DeviceInfo deviceInfo;
        if (deviceInfos.length == 0) {
            deviceInfo = new DeviceInfo();
            deviceInfo.setFreeText("Device note");
            document.addDeviceInfo(deviceInfo);
        } else {
            deviceInfo = deviceInfos[0];
        }
        
        final String deviceId = deviceInfo.getDeviceId(); 
        
        
        ServiceInfo[] serviceInfos = document.getServiceInfo();
        final ServiceInfo serviceInfo;
        final ServiceInfo videoServiceInfo;
        if(serviceInfos.length == 0) {
            serviceInfo = new ServiceInfo(PRESENCE_SERVICE_ID, "1.000", "");
            serviceInfo.setDeviceId("urn:uuid:" + deviceId);
            serviceInfo.setStatus(PresenceState.OPEN);
            
            videoServiceInfo = new ServiceInfo("org.3gpp.cs-videotelephony", "1.0", "");
            videoServiceInfo.setDeviceId(deviceId);
            videoServiceInfo.setDeviceId("Mobile");
        } else {
            serviceInfo = serviceInfos[0];
            videoServiceInfo = serviceInfos[1];
        }
        serviceInfo.setFreeText(serviceNote);
        //jsr specific behavior
        document.addServiceInfo(serviceInfo);
        

        videoServiceInfo.setStatus(videoCapableState);
        //jsr specific behavior
        document.addServiceInfo(videoServiceInfo);
        
        PersonInfo personInfo = document.getPersonInfo();
        if(personInfo == null) {
            personInfo = new PersonInfo();
            personInfo.setOverridingWillingness(PresenceState.OPEN);
        }
        personInfo.setFreeText(serviceNote);
        //personInfo.setActivities(activitiesToSet)
        document.setPersonInfo(personInfo);
    }
    
    private class PublishStatusTask extends AsyncTask<String, Void, Boolean>{

        
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.btn_status_publish).setEnabled(false);
        }

        
        protected Boolean doInBackground(String... params) {
            final String serviceNote = params[0];

            boolean retValue = true;
            
            try {
                PresenceSource presenceSource = getPresenceSource(presenceSourceListener);
                PresenceDocument presenceDocument = presenceSource.getPresenceDocument();
                PresenceState state = isVideoTupleOpen()? PresenceState.OPEN: PresenceState.CLOSED;
                updateDocumentNote(presenceDocument, serviceNote, state);
                presenceSource.publish();
            } catch (ServiceClosedException e) {
                retValue = false;
            } catch (ImsException e) {
                retValue = false;
            } catch (IOException e) {
                retValue = false;
            }
            
            return retValue;
        }
        
        
        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            findViewById(R.id.btn_status_publish).setEnabled(true);

            String message = result? "Presence status sent": "Presence status didn't sent";
            Toast.makeText(Presence325Activity.this, message, Toast.LENGTH_LONG).show();
        }
    }
    
    private class UnpublishStatusTask extends AsyncTask<Void, Void, Boolean>{

        
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.btn_status_unpublish).setEnabled(false);
        }

        
        protected Boolean doInBackground(Void... params) {
            boolean retValue = true;
            
            try {
                PresenceSource presenceSource = getPresenceSource(presenceSourceListener);
                presenceSource.unpublish();
            } catch (ServiceClosedException e) {
                retValue = false;
            } catch (ImsException e) {
                retValue = false;
            } catch (IOException e) {
                retValue = false;
            } catch (IllegalStateException e) {
                retValue = false;   
            }
            
            return retValue;
        }

        
        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            findViewById(R.id.btn_status_unpublish).setEnabled(true);

            String message = result? "Presence status unpublished": "Presence status didn't unpublished";
            Toast.makeText(Presence325Activity.this, message, Toast.LENGTH_LONG).show();
        }
    }
    
    private final PresenceSourceListener presenceSourceListener = new PresenceSourceListener() {
        
        
        public void publicationTerminated(PresenceSource presenceSource,
                ReasonInfo reason) {
            mHandler.sendEmptyMessage(MSG_PUBLICATION_TERMINATED);
        }
        
        
        public void publicationFailed(PresenceSource presenceSource,
                ReasonInfo reason) {
            mHandler.sendEmptyMessage(MSG_PUBLICATION_DELEVER_FAILED);
        }
        
        
        public void publicationDelivered(PresenceSource presenceSource) {
            mHandler.sendEmptyMessage(MSG_PUBLICATION_DELEVERED);
        }
    };

    
    protected void onDestroy() {
        super.onDestroy();

        if (publishStatusTask != null) {
            publishStatusTask.cancel(true);
        }
        
        if (unpublishStatusTask != null) {
            unpublishStatusTask.cancel(true);
        }
        
        Log.i(TAG, "onDestroy");
    }

    
    
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SERVICE_NOTE_ID, getServiceNotePosition());
        
        super.onSaveInstanceState(outState);
    }

}
