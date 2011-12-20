package javax.microedition.ims.engine.test;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;

import javax.microedition.ims.Connector;
import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.engine.test.msrp.*;
import javax.microedition.ims.engine.test.util.ContactsHolder;
import javax.microedition.ims.im.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MsrpChatActivity extends Activity {

    private final static String TAG = "MsrpChatActivity";
    
    private enum ComposingState {PASSIVE, ACTIVE};
    
    private static final String REMOTE_IDENTITY_POS = "remoteMsrpChatUserPos";
    private static final String MESSAGE_TEXT = "messageText";
    
    private ChatStartTask chatStartTask;
    private ChatStopTask chatStopTask;
    private SendMessageTask sendMessageTask;
    
    private IMService imService;
    private ConferenceManager conferenceManager;
    private FileTransferManager fileTransferManager;
    private AtomicReference<Chat> curChatRef = new AtomicReference<Chat>();
    private String chatSessionId;
    
    private ComposingIndicatorSender composingIndicatorSender;
    
    private Map<String, Message> messagesToSend = new HashMap<String, Message>();
    
    //private static final int GO_CHAT = 1;
    private static final int GO_OFFLINE = 2;
    private static final int ON_SEND_MSG = 3;
    private static final int ON_SESSION_CLOSED = 4;
    private static final int ON_INCOMING_FILE_PUSH_REQUEST = 5;
    private static final int ON_INCOMING_CHAT_REQUEST = 6;
    private static final int ON_CHAT_STARTED = 7;
    private static final int ON_CHAT_START_FAILED = 8;
    private static final int ON_REMOTE_COMPOSING_MSG = 9;
    
    private String remoteUserIdent;
    
    private final List<FileTransMngrListener> fileTransMngrListeners = new ArrayList<FileTransMngrListener>();
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "onCreate#started");
        
        Log.i(TAG, "onCreate 1 start");
        final String appId = getResources().getText(R.id.def_app_id).toString();
        String name = String.format("%1$s://%2$s", "imsim", appId);
        
        try {
            imService = (IMService)Connector.open(name, MsrpChatActivity.this);
        } catch (ImsException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, "onCreate 2 " + imService);
        
        imService.setListener(imServiceListener);
        Log.i(TAG, "onCreate 3");

        conferenceManager = imService.getConferenceManager();
        Log.i(TAG, "onCreate 4 " + conferenceManager);
        
        fileTransferManager = imService.getFileTransferManager();
        Log.i(TAG, "onCreate 5 " + fileTransferManager);
        
        conferenceManager.setListener(conferenceManagerListener);
        Log.i(TAG, "onCreate 6");
        
        fileTransferManager.setListener(fileTransferManagerListener);
        Log.i(TAG, "onCreate 7 finish");
        

        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTitle(R.string.msrp_chat_activity_title);
        setContentView(R.layout.dialog_msrp_chat);
        
        
        Spinner identityControl = (Spinner) findViewById(R.id.remote_identities);
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        //        this, remoteIdentitiesItems, android.R.layout.simple_spinner_item);
        String[] remoteIdentities = ContactsHolder.getContacts(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, remoteIdentities);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        identityControl.setAdapter(adapter);

        // restore previously stored data
        int remoteIdentityPos = savedInstanceState == null ? 0 : savedInstanceState.getInt(REMOTE_IDENTITY_POS);
        identityControl.setSelection(remoteIdentityPos);

        String messageText = savedInstanceState == null ? null
                : savedInstanceState.getString(MESSAGE_TEXT);

        EditText messageEdit = (EditText) findViewById(R.id.msrp_chat_message_text);
        messageEdit.setText(messageText == null ? getResources().getText(R.id.def_page_message_text) : messageText);

        
        //buttons press handlers
        findViewById(R.id.msrp_chat_start_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    if (curChatRef.get() != null) {
                        Toast.makeText(MsrpChatActivity.this, "Chat is started. Please, Stop it before", Toast.LENGTH_SHORT).show();
                    } else {
                        final String remoteUser = getRemoteParty();
                        
                        if (TextUtils.isEmpty(remoteUser)) {
                            Toast.makeText(MsrpChatActivity.this, "Remote party argument is null", Toast.LENGTH_SHORT).show();
                        } else {
                            (chatStartTask = new ChatStartTask()).execute(remoteUser);
                        }
                    }
                }
            }
        );
        
        findViewById(R.id.msrp_chat_stop_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    if (curChatRef.get() == null) {
                        Toast.makeText(MsrpChatActivity.this, "Chat is not started.", Toast.LENGTH_SHORT).show();
                    } else {
                        (chatStopTask = new ChatStopTask()).execute();
                    }
                }
            }
        );
        
        findViewById(R.id.msrp_chat_back_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(MsrpChatActivity.this, InviteActivity.class));
                    onDestroy();
                }
            }
        );
        
        findViewById(R.id.msrp_chat_send_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    if (curChatRef.get() == null) {
                        Toast.makeText(MsrpChatActivity.this, "Chat is not started.", Toast.LENGTH_SHORT).show();
                    } else {
                        EditText messageEdit = (EditText) findViewById(R.id.msrp_chat_message_text);

                        final String message = messageEdit.getText().toString();

                        if (TextUtils.isEmpty(message)) {
                            Toast.makeText(MsrpChatActivity.this, "Message argument is null", Toast.LENGTH_SHORT).show();
                        } else {
                            (sendMessageTask = new SendMessageTask()).execute(message);
                        }
                    }
                }
            }
        );
        
        findViewById(R.id.msrp_chat_file_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    remoteUserIdent = getRemoteParty();
                    
                    showDialog(DialogMsrpFileList.DIALOG_ID);
                }
            }
        );
        
        composingIndicatorSender = new ComposingIndicatorSender();
        
        ((EditText) findViewById(R.id.msrp_chat_message_text)).setFilters(
            new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence arg0, int arg1, int arg2, Spanned arg3, int arg4, int arg5) {
                        composingIndicatorSender.onKeyPressed();
                        return null;
                    }
                }
            }
        );
        
        Log.i(TAG, "onCreate#finished");
    }
    
    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
    }

    public void addFileTransMngrListener(FileTransMngrListener listener) {
        fileTransMngrListeners.add(listener);
    }
    
    public void removeAllFileTransMngrListeners() {
        fileTransMngrListeners.clear();
    }
    
    public FileTransferManager getFileTransferManager() {
        return fileTransferManager;
    }
    
    public String getRemoteUserIdent() {
        return remoteUserIdent;
    }

    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            Log.i(TAG, "handleMessage#started");
            switch (msg.what) {
//            case GO_CHAT: {
//                Log.i(TAG, "handleMessage#GO_CHAT");
//                TextView msrpChatStateView = (TextView) findViewById(R.id.msrp_chat_state);
//                msrpChatStateView.setText("chat with");
//                break;
//            }
            case GO_OFFLINE: {
                Log.i(TAG, "handleMessage#GO_OFFLINE");
                TextView msrpChatStateView = (TextView) findViewById(R.id.msrp_chat_state);
                msrpChatStateView.setText("offline");
                break;
            }
            case ON_SEND_MSG: {
                Log.i(TAG, "handleMessage#ON_SEND_MSG");
                LinearLayout msrpChatMessageHistoryLayout = (LinearLayout) findViewById(R.id.msrp_chat_message_history_layout);
                
                Msg m = (Msg)msg.obj;
                
                TextView textView = new TextView(MsrpChatActivity.this);
                textView.setText(m.getText());
                textView.setTextColor(Color.BLACK);
                
                msrpChatMessageHistoryLayout.addView(textView);
                
                break;
            }
            case ON_SESSION_CLOSED: {
                Log.i(TAG, "handleMessage#ON_SESSION_CLOSED");
                LinearLayout msrpChatMessageHistoryLayout = (LinearLayout) findViewById(R.id.msrp_chat_message_history_layout);
                msrpChatMessageHistoryLayout.removeAllViews();
                break;
            }
            
            case ON_INCOMING_FILE_PUSH_REQUEST: {
                Log.i(TAG, "handleMessage#ON_INCOMING_FILE_PUSH_REQUEST");
                FilePushRequest filePushRequest = (FilePushRequest)msg.obj;
                CheckBox box = (CheckBox) findViewById(R.id.msrp_chat_autoaccept);               
                DialogMsrpFileIncoming.showDialog(MsrpChatActivity.this, filePushRequest,  box.isChecked());
                
                break;
            }
            
            case ON_INCOMING_CHAT_REQUEST: {
                Log.i(TAG, "handleMessage#ON_INCOMING_CHAT_REQUEST");
                ChatInvitation chatInvitation = (ChatInvitation)msg.obj;
                
                DialogMsrpChatIncoming.showDialog(MsrpChatActivity.this, chatInvitation);
                
                break;
            }
            
            case ON_CHAT_STARTED: {
                Log.i(TAG, "handleMessage#ON_CHAT_STARTED");
                
                TextView msrpChatStateView = (TextView) findViewById(R.id.msrp_chat_state);
                msrpChatStateView.setText("chat with");

                DialogMsrpChatOutgoing.hideDialog(MsrpChatActivity.this);
                break;
            }
            
            case ON_CHAT_START_FAILED: {
                Log.i(TAG, "handleMessage#ON_CHAT_START_FAILED");
                
                DialogMsrpChatOutgoing.hideDialog(MsrpChatActivity.this);
                DialogMsrpChatIncoming.hideDialog(MsrpChatActivity.this);
                break;
            }
            
            case ON_REMOTE_COMPOSING_MSG: {
                Log.i(TAG, "handleMessage#ON_REMOTE_COMPOSING_MSG");
                
                Integer timeout = (Integer)msg.obj;
                
                if (timeout == 0) {
                    Toast.makeText(MsrpChatActivity.this, "Remote message composing stopped", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MsrpChatActivity.this, "Remote message composing in progress", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            
            default:
                super.handleMessage(msg);
            }
            Log.i(TAG, "handleMessage#finished");
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

    private int getRemotePartyPosition() {
        Spinner remotePartiesControl = (Spinner) findViewById(R.id.remote_identities);
        return remotePartiesControl.getSelectedItemPosition();
    }
    
    
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(REMOTE_IDENTITY_POS, getRemotePartyPosition());
        super.onSaveInstanceState(outState);
    }

    
    
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;

        switch (id) {
            case DialogMsrpFileList.DIALOG_ID: {
                dialog = DialogMsrpFileList.createDialog(this);
            }
            break;
            
            case DialogMsrpFileProgress.DIALOG_ID: {
                dialog = DialogMsrpFileProgress.createDialog(this);
            }
            break;
            
            case DialogMsrpFileIncoming.DIALOG_ID: {
                dialog = DialogMsrpFileIncoming.createDialog(this);
            }
            break;
            
            case DialogMsrpChatIncoming.DIALOG_ID: {
                dialog = DialogMsrpChatIncoming.createDialog(this);
            }
            break;
            
            case DialogMsrpChatOutgoing.DIALOG_ID: {
                dialog = DialogMsrpChatOutgoing.createDialog(this);
            }
            break;
            
            default: {
                dialog = super.onCreateDialog(id);
            }
            break;
        }
        return dialog;
    }

    
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DialogMsrpFileList.DIALOG_ID: {
                ((DialogMsrpFileList)dialog).onPrepare();
            }
            break;
    
            case DialogMsrpFileProgress.DIALOG_ID: {
                ((DialogMsrpFileProgress)dialog).onPrepare();
            }
            break;
            
            case DialogMsrpFileIncoming.DIALOG_ID: {
                ((DialogMsrpFileIncoming)dialog).onPrepare();
            }
            break;
            
            case DialogMsrpChatIncoming.DIALOG_ID: {
                ((DialogMsrpChatIncoming)dialog).onPrepare();
            }
            break;
            
            case DialogMsrpChatOutgoing.DIALOG_ID: {
                ((DialogMsrpChatOutgoing)dialog).onPrepare();
            }
            break;
    
            default: {
                super.onPrepareDialog(id, dialog);
            }
            break;
        }
    }
    
    
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        
        if (composingIndicatorSender != null) {
            composingIndicatorSender.stop();
            composingIndicatorSender = null;
        }

//        if (chatStopTask != null) {
//            chatStopTask.cancel(true);
//            chatStopTask = null;
//        }
        if (curChatRef.get() != null) {
            (chatStopTask = new ChatStopTask()).execute();
        }
        
        if (sendMessageTask != null) {
            sendMessageTask.cancel(true);
            sendMessageTask = null;
        }
        if (chatStartTask != null) {
            chatStartTask.cancel(true);
            chatStartTask = null;
        }
        if (imService != null) {
            imService.close();
            imService = null;
        }
    }

    
    private class ChatStartTask extends AsyncTask<String, Void, Boolean> {
        
        protected void onPreExecute() {
            super.onPreExecute();
        }

        
        protected Boolean doInBackground(String... params) {
            
            Boolean res = true;
            
            String remoteUserIdentity = params[0];
            
            Log.i(TAG, "ChatStartTask START");
            
            try {
                chatSessionId = conferenceManager.sendChatInvitation(
                        null,
                        remoteUserIdentity,
                        "Chat Test");
                Log.i(TAG, "ChatStartTask 2 - " + chatSessionId);
                
            } catch (ImsException e) {
                Log.i(TAG, e.getMessage());
                res = false;
            } catch (ServiceClosedException e) {
                Log.i(TAG, e.getMessage());
                res = false;
            }
            
            Log.i(TAG, "ChatStartTask FINISH");
            
            return res;
        }

        
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            
            DialogMsrpChatOutgoing.showDialog(MsrpChatActivity.this);
        }
    }
    
    
    public void cancelOutgoingChatSession() {
        Log.i(TAG, "cancelOutgoingChatSession#started");
        
        try {
            conferenceManager.cancelInvitation(chatSessionId);
            
        } catch (ServiceClosedException e) {
            Log.i(TAG, e.getMessage());
        }

        Log.i(TAG, "cancelOutgoingChatSession#finished");
    }
    

    private class ChatStopTask extends AsyncTask<String, Void, Boolean> {
        
        protected void onPreExecute() {
            super.onPreExecute();
        }

        
        protected Boolean doInBackground(String... params) {
            Log.i(TAG, "ChatStopTask#started");
            
            Boolean res = true;
            
            curChatRef.get().close();
            curChatRef.set(null);
            
            mHandler.sendEmptyMessage(GO_OFFLINE);
            
            Log.i(TAG, "ChatStopTask#finished");
            return res;
        }

        
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }
    

    private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        
        protected void onPreExecute() {
            super.onPreExecute();
        }

        
        protected Boolean doInBackground(String... params) {

            String messageBody = params[0];
            
            Log.i(TAG, "SendMessageTask START");
            
            Message message = new Message();
            messagesToSend.put(message.getMessageId(), message);
            
            ContentPart messageContent = new ContentPart(messageBody.getBytes(), "text/plain");
            message.addContentPart(messageContent);

            try {
                curChatRef.get().sendMessage(message, false);
                composingIndicatorSender.onMessageSend();
                Log.i(TAG, "SendMessageTask 2");                
                android.os.Message androidMsg = new android.os.Message();
                androidMsg.what = ON_SEND_MSG;
                androidMsg.obj = new Msg(messageBody, "me");
                mHandler.sendMessage(androidMsg);
                
            } catch (ImsException e) {
                Log.i(TAG, e.getMessage());
            }
            
            Log.i(TAG, "SendMessageTask FINISH");

            return null;
        }

        
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }
    
    
    private IMServiceListener imServiceListener = new IMServiceListener() {
        
        public void systemMessageReceived(IMService service, Message message) {
            Log.i(TAG, "IMServiceListener.systemMessageReceived");
        }
        
        
        public void serviceClosed(IMService service, ReasonInfo reason) {
            Log.i(TAG, "IMServiceListener.serviceClosed");
        }
        
        
        public void deliveryReportsReceived(IMService service, DeliveryReport[] reports) {
            Log.i(TAG, "IMServiceListener.deliveryReportsReceived");
        }
        
        
        public void advertisementMessageReceived(IMService service, Message message) {
            Log.i(TAG, "IMServiceListener.advertisementMessageReceived");
        }
    };
    
    
    private FileTransferManagerListener fileTransferManagerListener = new FileTransferManagerListener() {

        
        public void fileReceived(String requestId, String fileId, String filePath) {
            Log.i(TAG, "FileTransferManagerListener.fileReceived");
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("File received : " + filePath, "SYS");
            mHandler.sendMessage(androidMsg);
            
            for(FileTransMngrListener listener : fileTransMngrListeners) {
                listener.fileReceived(requestId, fileId, filePath);
            }
        }

        
        public void fileReceiveFailed(String requestId, String fileId, ReasonInfo reason) {
            Log.i(TAG, "FileTransferManagerListener.fileReceiveFailed");

            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("File receive failed", "SYS");
            mHandler.sendMessage(androidMsg);
            
            for(FileTransMngrListener listener : fileTransMngrListeners) {
                listener.fileReceiveFailed(requestId, fileId, reason);
            }
        }

        
        public void fileSendFailed(String requestId, String fileId, ReasonInfo reason) {
            Log.i(TAG, "FileTransferManagerListener.fileSendFailed");

            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("File send failed", "SYS");
            mHandler.sendMessage(androidMsg);
            
            for(FileTransMngrListener listener : fileTransMngrListeners) {
                listener.fileSendFailed(requestId, fileId, reason);
            }
        }

        
        public void fileSent(String requestId, String fileId) {
            Log.i(TAG, "FileTransferManagerListener.fileSent");

            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("File sent", "SYS");
            mHandler.sendMessage(androidMsg);
            
            for(FileTransMngrListener listener : fileTransMngrListeners) {
                listener.fileSent(requestId, fileId);
            }
        }

        
        public void fileTransferFailed(String requestId, ReasonInfo reason) {
            Log.i(TAG, "FileTransferManagerListener.fileTransferFailed");

            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("File transfer failed", "SYS");
            mHandler.sendMessage(androidMsg);
            
            for(FileTransMngrListener listener : fileTransMngrListeners) {
                listener.fileTransferFailed(requestId, reason);
            }
        }

        
        public void incomingFilePullRequest(FilePullRequest filePullRequest) {
            Log.i(TAG, "FileTransferManagerListener.incomingFilePullRequest");
            
//            try {
//                filePullRequest.accept();
//                
//            } catch (ImsException e) {
//                Log.i(TAG, e.getMessage());
//            }

            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("Incoming file pull request", "SYS");
            mHandler.sendMessage(androidMsg);
            
            for(FileTransMngrListener listener : fileTransMngrListeners) {
                listener.incomingFilePullRequest(filePullRequest);
            }
        }

        
        public void incomingFilePushRequest(FilePushRequest filePushRequest) {
            Log.i(TAG, "FileTransferManagerListener.incomingFilePushRequest");
            
//            filePushRequest.accept();

//            android.os.Message androidMsg = new android.os.Message();
//            androidMsg.what = ON_SEND_MSG;
//            androidMsg.obj = new Msg("Incoming file push request", "SYS");
//            mHandler.sendMessage(androidMsg);
            
            
            android.os.Message androidMsg2 = new android.os.Message();
            androidMsg2.what = ON_INCOMING_FILE_PUSH_REQUEST;
            androidMsg2.obj = filePushRequest;
            mHandler.sendMessage(androidMsg2);
            
            
            for(FileTransMngrListener listener : fileTransMngrListeners) {
                listener.incomingFilePushRequest(filePushRequest);
            }
        }

        public void transferProgress(String requestId, String fileId, long bytesTransferred, long bytesTotal) {
            Log.i(TAG, "FileTransferManagerListener.transferProgress");

            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("Transfer progress : " + bytesTransferred + " from " + bytesTotal, "SYS");
            mHandler.sendMessage(androidMsg);
            
            for(FileTransMngrListener listener : fileTransMngrListeners) {
                listener.transferProgress(requestId, fileId, bytesTransferred, bytesTotal);
            }
        }
    };
    
    
    private ConferenceManagerListener conferenceManagerListener = new ConferenceManagerListener() {
        
        public void conferenceStarted(Conference conference) {
            Log.i(TAG, "ConferenceManagerListener.conferenceStarted");
        }
        
        
        public void conferenceStartFailed(String sessionId, ReasonInfo reason) {
            Log.i(TAG, "ConferenceManagerListener.conferenceStartFailed");
        }
        
        
        public void conferenceInvitationReceived(ConferenceInvitation conferenceInvitation) {
            Log.i(TAG, "ConferenceManagerListener.conferenceInvitationReceived");
        }
        
        
        public void chatStarted(Chat chat) {
            Log.i(TAG, "ConferenceManagerListener.chatStarted");
            
            curChatRef.set(chat);
            curChatRef.get().setListener(chatListener);
            
//            mHandler.sendEmptyMessage(GO_CHAT);
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("Chat started", "SYS");
            mHandler.sendMessage(androidMsg);
            
            mHandler.sendEmptyMessage(ON_CHAT_STARTED);
        }
        
        
        public void chatStartFailed(String sessionId, ReasonInfo reason) {
            Log.i(TAG, "ConferenceManagerListener.chatStartFailed");
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("Chat start failed", "SYS");
            mHandler.sendMessage(androidMsg);
            
            mHandler.sendEmptyMessage(ON_CHAT_START_FAILED);
        }
        
        
        public void chatInvitationReceived(ChatInvitation chatInvitation) {
            Log.i(TAG, "ConferenceManagerListener.chatInvitationReceived");
            
            //chatInvitation.accept();
            android.os.Message androidMsg2 = new android.os.Message();
            androidMsg2.what = ON_INCOMING_CHAT_REQUEST;
            androidMsg2.obj = chatInvitation;
            mHandler.sendMessage(androidMsg2);
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("Incoming Chat session", "SYS");
            mHandler.sendMessage(androidMsg);
        }
    };
    
    
    private ChatListener chatListener = new ChatListener() {
        
        public void systemMessageReceived(IMSession session, Message message) {
            Log.i(TAG, "ChatListener.systemMessageReceived");
        }
        
        
        public void sessionClosed(IMSession session, ReasonInfo reason) {
            Log.i(TAG, "ChatListener.sessionClosed");
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SESSION_CLOSED;
            androidMsg.obj = new Msg("Chat session closed", "SYS");
            mHandler.sendMessage(androidMsg);
        }
        
        
        public void messageSent(IMSession session, String messageId) {
            Log.i(TAG, "ChatListener.messageSent");
            
            Message message = messagesToSend.get(messageId);
            
            String messageBody = message.getContentParts() == null ? "" : new String(message.getContentParts()[0].getContent());
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg(messageBody, "remote");
            mHandler.sendMessage(androidMsg);
            
        }
        
        
        public void messageSendFailed(IMSession session, String messageId,
                ReasonInfo reason) {
            Log.i(TAG, "ChatListener.messageSendFailed");
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("Message send failed", "SYS");
            mHandler.sendMessage(androidMsg);
        }
        
        
        public void messageReceived(IMSession session, Message message) {
            Log.i(TAG, "ChatListener.messageReceived");
            
            String messageBody = message.getContentParts() == null ? "" : new String(message.getContentParts()[0].getContent());
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg(messageBody, "remote");
            mHandler.sendMessage(androidMsg);
        }
        
        
        public void incomingFilePushRequest(IMSession session,
                FilePushRequest filePushRequest) {
            Log.i(TAG, "ChatListener.incomingFilePushRequest");
        }
        
        
        public void fileTransferProgress(IMSession session, String requestId,
                String fileId, long bytesTransferred, long bytesTotal) {
            Log.i(TAG, "ChatListener.fileTransferProgress");
        }
        
        
        public void fileTransferFailed(IMSession session, String requestId,
                ReasonInfo reason) {
            Log.i(TAG, "ChatListener.fileTransferFailed");
        }
        
        
        public void fileSent(IMSession session, String requestId, String fileId) {
            Log.i(TAG, "ChatListener.fileSent");
        }
        
        
        public void fileSendFailed(IMSession session, String requestId,
                String fileId, ReasonInfo reason) {
            Log.i(TAG, "ChatListener.fileSendFailed");
        }
        
        
        public void fileReceived(IMSession session, String requestId,
                String fileId, String filePath) {
            Log.i(TAG, "ChatListener.fileReceived");
        }
        
        
        public void fileReceiveFailed(IMSession session, String requestId,
                String fileId, ReasonInfo reason) {
            Log.i(TAG, "ChatListener.fileReceiveFailed");
        }
        
        
        public void composingIndicatorReceived(IMSession session, String sender,
                int timeout) {
            Log.i(TAG, "ChatListener.composingIndicatorReceived   sender=" + sender + "   timeout=" + timeout);
            
            android.os.Message androidMsg2 = new android.os.Message();
            androidMsg2.what = ON_REMOTE_COMPOSING_MSG;
            androidMsg2.obj = new Integer(timeout);
            mHandler.sendMessage(androidMsg2);

            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SEND_MSG;
            androidMsg.obj = new Msg("Composing message " + (timeout == 0 ? "stopped" : "in progress"), "remote");
            mHandler.sendMessage(androidMsg);
        }
        
        
        public void chatExtensionFailed(Chat chat, ReasonInfo reason) {
            Log.i(TAG, "ChatListener.chatExtensionFailed");
        }
        
        
        public void chatExtended(Chat chat, Conference conference) {
            Log.i(TAG, "ChatListener.chatExtended");
        }
    };
    
    
    private class Msg {
        private String body;
        private String sender;
        private Date time;
        
        public Msg(String body, String sender) {
            this.body = body;
            this.sender = sender;
            this.time = new Date();
        }
        
        public String getBody() {
            return body;
        }

        public String getSender() {
            return sender;
        }

        public Date getTime() {
            return time;
        }
        
        public String getText() {
            return getTimeStr() + " - " + sender + " - " + body;
        }
        
        private String getTimeStr() {
            return time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds();
        }
    }
    
    
    public static interface FileTransMngrListener {
        
        void fileSent(String requestId, String fileId);
        
        void fileSendFailed(String requestId, String fileId, ReasonInfo reason);
        
        void fileReceived(String requestId, String fileId, String filePath);
        
        void fileReceiveFailed(String requestId, String fileId, ReasonInfo reason);
        
        void incomingFilePushRequest(FilePushRequest filePushRequest);
        
        void incomingFilePullRequest(FilePullRequest filePullRequest);
        
        void transferProgress(String requestId, String fileId, long bytesTransferred, long bytesTotal);
        
        void fileTransferFailed(String requestId, ReasonInfo reason);
    }
    
    
    private class ComposingIndicatorSender {
        private static final int TIMER_A_INTERVAL_SEC = 60;
        private static final int TIMER_C_INTERVAL_SEC = 12;
        private static final int TIMER_D_INTERVAL_SEC = 4;
        
        private ComposingIndicatorSenderWorker worker;
        private AtomicReference<Date> prevKeyPressTime = new AtomicReference<Date>();
        
        public void stop() {
            Log.i(TAG, "ComposingIndicatorSender.STOP()");
            if (worker != null) {
                worker.cancel(true);
                worker = null;
            }
        }
        
        public void onMessageSend() {
            Log.i(TAG, "ComposingIndicatorSender.onMessageSend()");
            if (worker != null) {
                worker.sendComposingStoppedStatus();
                prevKeyPressTime.set(null);
            }
        }
        
        public void onKeyPressed() {
            Log.i(TAG, "ComposingIndicatorSender.onKeyPressed()");
            prevKeyPressTime.set(new Date());
            
            if (worker == null) {
                (worker = new ComposingIndicatorSenderWorker()).execute();
            }
        }
        
        private class ComposingIndicatorSenderWorker extends AsyncTask<Void, Void, Void> {
            private ComposingState composingState = ComposingState.PASSIVE;
            private Date prevSendActiveState;
            
            @Override
            protected Void doInBackground(Void... arg0) {
                
                while(true) {
                    Log.i(TAG, "ComposingIndicatorSenderWorker.doInBackground() - ITERATION");
                    Date curTime = new Date();

                    if (prevKeyPressTime.get() != null) {
                        long secFromPrevKeyPress = (curTime.getTime() - prevKeyPressTime.get().getTime())/1000;
                        
                        if (secFromPrevKeyPress < TIMER_C_INTERVAL_SEC) {
                            sendComposingInProgressStatus(curTime);
                        } else {
                            sendComposingStoppedStatus();
                        }
                    }

                    try {
                        TimeUnit.SECONDS.sleep(TIMER_D_INTERVAL_SEC);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                return null;
            }
            
            private void sendComposingInProgressStatus(Date curTime) {
                Log.i(TAG, "ComposingIndicatorSenderWorker.sendComposingInProgressStatus()");
                long secFromPrevActivation
                    = prevSendActiveState == null ? TIMER_A_INTERVAL_SEC : (curTime.getTime() - prevSendActiveState.getTime())/1000;
                if (composingState == ComposingState.PASSIVE
                 || composingState == ComposingState.ACTIVE && secFromPrevActivation >= TIMER_A_INTERVAL_SEC) {
                    if (curChatRef.get() != null) {
                        try {
                            curChatRef.get().sendComposingIndicator(TIMER_A_INTERVAL_SEC);
                            prevSendActiveState = new Date();
                            composingState = ComposingState.ACTIVE;
                        } catch (ImsException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            }
            
            private void sendComposingStoppedStatus() {
                Log.i(TAG, "ComposingIndicatorSenderWorker.sendComposingStoppedStatus()");
                if (composingState == ComposingState.ACTIVE) {
                    if (curChatRef.get() != null) {
                        try {
                            curChatRef.get().sendComposingIndicator(0);
                            composingState = ComposingState.PASSIVE;
                        } catch (ImsException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            }
        }
        
    }

    
}
