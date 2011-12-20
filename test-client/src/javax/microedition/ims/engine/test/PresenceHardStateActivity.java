package javax.microedition.ims.engine.test;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import javax.microedition.ims.Connector;
import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.xdm.XCAPException;
import javax.microedition.ims.xdm.XCAPRequest;
import javax.microedition.ims.xdm.XDMService;
import java.io.IOException;

/**
 * This activity responsible for show presence functionality:
 * 1) hard state presence status
 * 
 * @author Andrei Khomushko
 *
 */
public class PresenceHardStateActivity extends BaseActivity{
    private final String TAG = getClass().getSimpleName();
    
    private static final String STATUS_URL_ID = "noteUrlId";
    private static final String STATUS_NOTE_TEXT = "noteText";
    
    private static final int statusIconsItems = R.array.presense_status_icon;
    
    private AsyncTask<String, Void, Boolean> updateStatusTask;
    
    
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTitle(R.string.activity_presence_hardstate_title);

        setContentView(R.layout.presence_hard_state);
        
        Spinner spinner = (Spinner) findViewById(R.id.status_icons);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, statusIconsItems, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        // restore previously stored data
        int statusURLId = savedInstanceState == null ? 0
                : savedInstanceState.getInt(STATUS_URL_ID);
        spinner.setSelection(statusURLId);
        
        String statusNoteText = savedInstanceState == null ? null
                : savedInstanceState.getString(STATUS_NOTE_TEXT);

        if(statusNoteText != null) {
            EditText statusNoteEdit = (EditText) findViewById(R.id.status_note_text);
            statusNoteEdit.setText(statusNoteText);
        }

        findViewById(R.id.btn_status_update).setOnClickListener(updateStatusListener);
        findViewById(R.id.btn_back).setOnClickListener(backListener);
    }
    
    
    protected void onStart() {
        super.onStart();
        updateConnectionState(AppContext.instance.getConnectionState());
    }
    
    private OnClickListener updateStatusListener = new OnClickListener() {
        public void onClick(View v) {
            String statusIconPath = getStatusIconText();
            String statusNote = getStatusNoteText();

            //String infoMessage = String.format("statusIcon=%s, statusNote=%s", statusIconPath, statusNote);
            //Toast.makeText(PresenceActivity.this, infoMessage, Toast.LENGTH_LONG).show();
            
            (updateStatusTask = new UpdateStatusTask()).execute(statusIconPath, statusNote);
        }
    };

    private String getStatusNoteText() {
        EditText statusNoteControl = (EditText) findViewById(R.id.status_note_text);
        return statusNoteControl.getText().toString();
    }

    private String getStatusIconText() {
        Spinner statusIconsControl = (Spinner) findViewById(R.id.status_icons);
        int pos = statusIconsControl.getSelectedItemPosition();
        return PresenceHardStateActivity.this.getResources().getStringArray(statusIconsItems)[pos];
    }
    
    private int getStatusIconPosition() {
        Spinner statusIconsControl = (Spinner) findViewById(R.id.status_icons);
        return statusIconsControl.getSelectedItemPosition();
    }
    
    private class UpdateStatusTask extends AsyncTask<String, Void, Boolean>{
        private static final String AUID = "pidf-manipulation";
        private static final String DEFAULT_DOCUMENT_PATH = "index";
        private static final String DEF_MIME_TYPE = "application/pidf+xml";

        
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.btn_status_update).setEnabled(false);
        }

        
        protected Boolean doInBackground(String... params) {
            final String statusIconPath = params[0];
            final String statusNote = params[1];

            boolean retValue = false;
            try {
                createDocument(statusIconPath, statusNote);
                retValue = true;
            } catch (XCAPException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (ImsException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            
            return retValue;
        }

        
        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            findViewById(R.id.btn_status_update).setEnabled(true);

            String message = result? "Status updated": "Status didn't updated";
            Toast.makeText(PresenceHardStateActivity.this, message, Toast.LENGTH_LONG).show();
        }
        
        private XDMService instantiateUAC() throws ImsException{
            XDMService coreServiceUAC = (XDMService) Connector
            .open("imsxdm://my.app.identity;" /*+ DEF_XUI*/,
                  PresenceHardStateActivity.this.getApplicationContext());
            
            return coreServiceUAC;
        }

        private void createDocument(String statusIcon, String statusNote) 
            throws ServiceClosedException, IOException, XCAPException, ImsException {
            final XDMService xdmService = instantiateUAC();
            
            String defStackXUI = xdmService.getDefXui();
            
            StringBuilder builder = new StringBuilder().
                    append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").
                    append(String.format("<presence entity=\"%s\" "+
                            "xmlns=\"urn:ietf:params:xml:ns:pidf\" " +
                            "xmlns:dm=\"urn:ietf:params:xml:ns:pidf:data-model\" " +
                            "xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\">", defStackXUI)).
                    append("<dm:person id=\"p1\">");
            
            if(statusIcon != null) {
                builder.append("<rpid:status-icon>").
                    append(statusIcon).
                    append("</rpid:status-icon>");
            }
                    
            if(statusNote != null) {
                builder.append("<rpid:note>").
                    append(statusNote).
                    append("</rpid:note>");
            };

            builder.append("</dm:person>").
                append("</presence>");
                                                                

            final String content = builder.toString();
            String documentSelector = XCAPRequest.createUserDocumentSelector(AUID, defStackXUI, DEFAULT_DOCUMENT_PATH);
            XCAPRequest xcapRequest = XCAPRequest.createPutRequest(documentSelector, DEF_MIME_TYPE, content, null);
            xdmService.sendXCAPRequest(xcapRequest);
    
            xdmService.close();
        }
    }

    private OnClickListener backListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(PresenceHardStateActivity.this, InviteActivity.class));
        }
    };

    
    protected void onDestroy() {
        super.onDestroy();

        if (updateStatusTask != null) {
            updateStatusTask.cancel(true);
        }
        Log.i(TAG, "onDestroy");
    }

    
    public void mediaUpdated(Media[] medias) {
        // TODO Auto-generated method stub
    }
    
    
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATUS_URL_ID, getStatusIconPosition());
        outState.putString(STATUS_NOTE_TEXT, getStatusNoteText());
        
        super.onSaveInstanceState(outState);
    }

}
