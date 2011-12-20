package javax.microedition.ims.engine.test;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;

import java.util.concurrent.atomic.AtomicInteger;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.PageMessage;
import javax.microedition.ims.core.PageMessageListener;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.engine.test.util.ContactsHolder;

/**
 * This class demonstrate page message functionality.
 * 
 * @author Andrei Khomushko
 *
 */
public class MessageActivity extends BaseActivity {
    protected final String TAG = getClass().getSimpleName();
    
    private static final String DEF_CONTENT_TYPE = "text/plain";
    
    private static final String REMOTE_IDENTITY_POS = "remoteMessageUserPos";
    private static final String MESSAGE_TEXT = "messageText";
    
    private static final int PAGE_MESSAGE_DELIVERED = 0;
    private static final int PAGE_MESSAGE_DELIVER_FAILED = 1;

    private AsyncTask<String, Void, Boolean> sendMessageTask;

    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTitle(R.string.message_activity_title);

        setContentView(R.layout.message);
        
        Spinner identityControl = (Spinner) findViewById(R.id.remote_identities);
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        //       this, remoteIdentitiesItems, android.R.layout.simple_spinner_item);
        String[] remoteIdentities = ContactsHolder.getContacts(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, remoteIdentities);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        identityControl.setAdapter(adapter);


        // restore previously stored data
        int remoteIdentityPos = savedInstanceState == null ? 0 : savedInstanceState.getInt(REMOTE_IDENTITY_POS);
        identityControl.setSelection(remoteIdentityPos);

        String messageText = savedInstanceState == null ? null
                : savedInstanceState.getString(MESSAGE_TEXT);

        EditText messageEdit = (EditText) findViewById(R.id.message_text);
        messageEdit.setText(messageText == null ? getResources().getText(R.id.def_page_message_text) : messageText);
        
        EditText contentEdit = (EditText) findViewById(R.id.content_type);
        contentEdit.setText(DEF_CONTENT_TYPE);

        findViewById(R.id.btn_send).setOnClickListener(sendMessageListener);
        findViewById(R.id.btn_back).setOnClickListener(backListener);
    }

    
    protected void onStart() {
        super.onStart();
        updateConnectionState(AppContext.instance.getConnectionState());
    }

    private final Handler mHandler = new Handler() {
        
        public void handleMessage(Message msg) {
            Log.i(TAG, "Message arrived: " + msg);
            switch (msg.what) {
            case PAGE_MESSAGE_DELIVERED: {
                doPageMessageDelevered((PageMessage) msg.obj);
                break;
            }
            case PAGE_MESSAGE_DELIVER_FAILED: {
                doPageMessageDeleverFailed((PageMessage) msg.obj);
                break;
            }
            default:
                super.handleMessage(msg);
            }
        }
    };
    
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

    private OnClickListener sendMessageListener = new OnClickListener() {
        public void onClick(View v) {
            final String remoteUser = getRemoteParty();
            
            EditText messageEdit = (EditText) findViewById(R.id.message_text);
            final String message = messageEdit.getText().toString();
            final String contentType= ((EditText) findViewById(R.id.content_type)).getText().toString();

            if (TextUtils.isEmpty(remoteUser)) {
                Toast.makeText(MessageActivity.this, "Remote party argument is null", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(message)) {
                Toast.makeText(MessageActivity.this, "Message argument is null", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(contentType)) {
                Toast.makeText(MessageActivity.this, "Content type argument is null", Toast.LENGTH_SHORT).show();
            } else {
                (sendMessageTask = new SendMessageTask()).execute(remoteUser, message, contentType);
            }
        }
    };

    private OnClickListener backListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MessageActivity.this, InviteActivity.class));
        }
    };

    private class SendMessageTask extends AsyncTask<String, Void, Boolean>
            implements PageMessageListener {

        
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.btn_send).setEnabled(false);
        }

        public byte[] hexStringToByteArray(String s) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
            }
            return data;
        }
        
        protected Boolean doInBackground(String... params) {
            final String remoteUser = params[0] + ";user=phone";          
            final String message = params[1];
            final String contentType = params[2];

            Boolean result = Boolean.FALSE;
            try {
                PageMessage msg = AppContext.instance.getConnection()
                        .createPageMessage(null, remoteUser);
                msg.setListener(this);                      
                msg.send(message.getBytes(), contentType);
                result = Boolean.TRUE;
            } catch (ServiceClosedException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (ImsException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return result;
        }
        
        

        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            findViewById(R.id.btn_send).setEnabled(true);

            if (result == Boolean.FALSE) {
                Toast.makeText(MessageActivity.this, "Message isn'n sent", Toast.LENGTH_SHORT).show();
            }
            if(++counter < MESSAGE_COUNT ) {
                Message message = handler.obtainMessage(555);
                handler.sendMessageDelayed(message, 2000l);
            }
        }

        public void pageMessageDelivered(final PageMessage pageMessage) {
            Message message = new Message();
            message.what = PAGE_MESSAGE_DELIVERED;
            message.obj = pageMessage;
            mHandler.sendMessage(message);
        }

        public void pageMessageDeliveryFailed(final PageMessage pageMessage) {
            Message message = new Message();
            message.what = PAGE_MESSAGE_DELIVER_FAILED;
            message.obj = pageMessage;
            mHandler.sendMessage(message);
        }
    }
    
    static final int MESSAGE_COUNT = 0;
    static int counter = 0;

    private final Handler handler = new Handler() {
      @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);


            (sendMessageTask = new SendMessageTask()).execute("sip:12345678@dummy.com;user=phone", "Message", "text/plain");



            }
    };


    private void doPageMessageDelevered(final PageMessage pageMessage) {
        pageMessage.setListener(null);
        Toast.makeText(this, R.string.msg_send_success, Toast.LENGTH_SHORT).show();
    }

    private void doPageMessageDeleverFailed(final PageMessage pageMessage) {
        pageMessage.setListener(null);
        Toast.makeText(this, R.string.msg_send_failure, Toast.LENGTH_SHORT).show();
    }

    private int getRemotePartyPosition() {
        Spinner remotePartiesControl = (Spinner) findViewById(R.id.remote_identities);
        return remotePartiesControl.getSelectedItemPosition();
    }
    
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");

        outState.putInt(REMOTE_IDENTITY_POS, getRemotePartyPosition());
        outState.putString(MESSAGE_TEXT, getMessageText());
        super.onSaveInstanceState(outState);
    }

    private String getMessageText() {
        return ((TextView) findViewById(R.id.message_text)).getText().toString();
    }

    protected void onDestroy() {
        super.onDestroy();

        if (sendMessageTask != null) {
            sendMessageTask.cancel(true);
        }
        Log.i(TAG, "onDestroy");
    }

    
    public void mediaUpdated(Media[] medias) {
        // TODO Auto-generated method stub
    }
}
