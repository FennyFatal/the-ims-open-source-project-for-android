package javax.microedition.ims.engine.test.msrp;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.engine.test.MsrpChatActivity;
import javax.microedition.ims.engine.test.MsrpChatActivity.FileTransMngrListener;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.im.FilePullRequest;
import javax.microedition.ims.im.FilePushRequest;
import java.util.ArrayList;

public class DialogMsrpFileIncoming extends Dialog {
    
    private final static String TAG = "DialogMsrpFileIncoming";
    
    private final MsrpChatActivity msrpChatActivity;
    
    public static final int DIALOG_ID = 23;
    
    private static final int ON_TRANSFER_PROGRESS = 101;
    private static final int ON_FILE_RECEIVED = 102;
    private static final int ON_FILE_RECEIVE_FAILED = 103;
    
//    private static FilePushRequest filePushRequest;
    private static ArrayList<FilePushRequest> filePushRequests = new ArrayList<FilePushRequest>();
    
    private static boolean inProgress = false;

	private static boolean enableAutoAccept;
    
    
    private final Handler mHandler = new Handler() {
        
        public void handleMessage(android.os.Message msg) {
            Log.i(TAG, "handleMessage#started");
            switch (msg.what) {
                case ON_TRANSFER_PROGRESS: {
                    Log.i(TAG, "handleMessage#ON_TRANSFER_PROGRESS");
                    String requestIdFirstSendFileTask = getFirstRequestId();
                    String requestIdSecondSendFileTask = getSecondRequestId();

                    IncomingFileStatusMsg incomingFileStatusMsg = (IncomingFileStatusMsg)msg.obj;
                    
                    if (incomingFileStatusMsg.getRequestId().equals(requestIdFirstSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_incoming_progress1)).setText(incomingFileStatusMsg.getProgresText());
                        ((TextView)findViewById(R.id.msrp_file_incoming_state1)).setText("In progress");
                    } else if (incomingFileStatusMsg.getRequestId().equals(requestIdSecondSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_incoming_progress2)).setText(incomingFileStatusMsg.getProgresText());
                        ((TextView)findViewById(R.id.msrp_file_incoming_state2)).setText("In progress");
                    } else {
                        throw new IllegalStateException("Not equal requestIds:   cur=" + incomingFileStatusMsg.getRequestId()
                            + "   first=" + requestIdFirstSendFileTask + "   second=" + requestIdSecondSendFileTask);
                    }
                }
                break;
                
                case ON_FILE_RECEIVED: {
                    Log.i(TAG, "handleMessage#ON_FILE_RECEIVED");
                    String requestIdFirstSendFileTask = getFirstRequestId();
                    String requestIdSecondSendFileTask = getSecondRequestId();

                    IncomingFileStatusMsg incomingFileStatusMsg = (IncomingFileStatusMsg)msg.obj;
                    
                    if (incomingFileStatusMsg.getRequestId().equals(requestIdFirstSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_incoming_path1)).setText("File: " + incomingFileStatusMsg.getFilePath());
                        ((TextView)findViewById(R.id.msrp_file_incoming_state1)).setText("Completed");
                    } else if (incomingFileStatusMsg.getRequestId().equals(requestIdSecondSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_incoming_path2)).setText("File: " + incomingFileStatusMsg.getFilePath());
                        ((TextView)findViewById(R.id.msrp_file_incoming_state2)).setText("Completed");
                    } else {
                        throw new IllegalStateException("Not equal requestIds:   cur=" + incomingFileStatusMsg.getRequestId()
                            + "   first=" + requestIdFirstSendFileTask + "   second=" + requestIdSecondSendFileTask);
                    }
                    
//                    findViewById(R.id.msrp_file_incoming_cancel_btn).setEnabled(false);
                }
                break;
                
                case ON_FILE_RECEIVE_FAILED: {
                    Log.i(TAG, "handleMessage#ON_FILE_RECEIVED");
                    String requestIdFirstSendFileTask = getFirstRequestId();
                    String requestIdSecondSendFileTask = getSecondRequestId();

                    IncomingFileStatusMsg incomingFileStatusMsg = (IncomingFileStatusMsg)msg.obj;
                    
                    if (incomingFileStatusMsg.getRequestId().equals(requestIdFirstSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_incoming_state1)).setText("Send failed");
                    } else if (incomingFileStatusMsg.getRequestId().equals(requestIdSecondSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_incoming_state2)).setText("Send failed");
                    } else {
                        throw new IllegalStateException("Not equal requestIds:   cur=" + incomingFileStatusMsg.getRequestId()
                            + "   first=" + requestIdFirstSendFileTask + "   second=" + requestIdSecondSendFileTask);
                    }
                    
//                    findViewById(R.id.msrp_file_incoming_cancel_btn).setEnabled(false);
                }
                break;
            
                default: {
                    super.handleMessage(msg);
                    break;
                }
            }
            Log.i(TAG, "handleMessage#finished");
        }

        private String getSecondRequestId() {
            String requestIdSecondSendFileTask = null;
            if (filePushRequests.size() >= 2) {
                requestIdSecondSendFileTask = filePushRequests.get(1).getRequestId();
            }
            return requestIdSecondSendFileTask;
        }

        private String getFirstRequestId() {
            String requestIdFirstSendFileTask = null;
            if (filePushRequests.size() >= 1) {
                requestIdFirstSendFileTask = filePushRequests.get(0).getRequestId();
            }
            return requestIdFirstSendFileTask;
        }
    };
    
    
    
    public DialogMsrpFileIncoming(MsrpChatActivity msrpChatActivity) {
        super(msrpChatActivity);
        this.msrpChatActivity = msrpChatActivity;
        Log.i(TAG, "DialogMsrpFileIncoming()#");        
    }
    
    public static DialogMsrpFileIncoming createDialog(MsrpChatActivity msrpChatActivity) {
        Log.i(TAG, "createDialog#started");
        final DialogMsrpFileIncoming dialog = new DialogMsrpFileIncoming(msrpChatActivity);
        
        dialog.setContentView(R.layout.dialog_msrp_file_incoming);
        dialog.setTitle(R.string.msrp_file_incoming_title);

        dialog.findViewById(R.id.msrp_file_incoming_accept_btn).setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.doAccept();
                }
            }
        );

        dialog.findViewById(R.id.msrp_file_incoming_reject_btn).setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.doReject();
                }
            }
        );

        dialog.findViewById(R.id.msrp_file_incoming_cancel_btn).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.doCancel();
                }
            }
        );
        
        dialog.findViewById(R.id.msrp_file_incoming_close_btn).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.doClose();
                }
            }
        );
        
        Log.i(TAG, "createDialog#finished");
        
        return dialog;
    }
    
    public void onPrepare() {
        Log.i(TAG, "onPrepare#started");
        msrpChatActivity.removeAllFileTransMngrListeners();
        msrpChatActivity.addFileTransMngrListener(fileTransMngrListener);

        reInit();
        
        disableEnableAcceptReject(!enableAutoAccept);
//        findViewById(R.id.msrp_file_incoming_cancel_btn).setEnabled(false);
        Log.i(TAG, "onPrepare#finished");
    }

    private void reInit() {
        ((TextView)findViewById(R.id.msrp_file_incoming_path1)).setText("File: ");
        ((TextView)findViewById(R.id.msrp_file_incoming_state1)).setText("Not started");
        ((TextView)findViewById(R.id.msrp_file_incoming_progress1)).setText("Progress: 0 from 0");
        
        ((TextView)findViewById(R.id.msrp_file_incoming_path2)).setText("File: ");
        ((TextView)findViewById(R.id.msrp_file_incoming_state2)).setText("Not started");
        ((TextView)findViewById(R.id.msrp_file_incoming_progress2)).setText("Progress: 0 from 0");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed#started");
        super.onBackPressed();
        onClose();
        Log.i(TAG, "onBackPressed#finished");
    }
    
    private void onClose() {
        Log.i(TAG, "onClose#started");
        inProgress = false;
        Log.i(TAG, "onClose#finished");
    }
    
    private void doAccept() {
        filePushRequests.get(0).accept();
        
        disableEnableAcceptReject(false);
        findViewById(R.id.msrp_file_incoming_cancel_btn).setEnabled(true);
    }

    private void doReject() {
    	filePushRequests.get(0).reject();
        
        disableEnableAcceptReject(false);
        findViewById(R.id.msrp_file_incoming_cancel_btn).setEnabled(false);
    }

    private void disableEnableAcceptReject(boolean enable) {
        findViewById(R.id.msrp_file_incoming_accept_btn).setEnabled(enable);
        findViewById(R.id.msrp_file_incoming_reject_btn).setEnabled(enable);
    }

    private void doCancel() {
        Log.i(TAG, "doCancel#started");
        new CancelFileTask().execute();
        msrpChatActivity.dismissDialog(DIALOG_ID);
        onClose();
        Log.i(TAG, "doCancel#finished");
    }
    
    private void doClose() {
        Log.i(TAG, "doClose#started");
        msrpChatActivity.dismissDialog(DIALOG_ID);
        onClose();
        Log.i(TAG, "doClose#finished");
    }
    
    public static void showDialog(MsrpChatActivity activity, FilePushRequest fpr, boolean enableAutoAccept) {
        Log.i(TAG, "showDialog#started    inProgress="+inProgress);
        DialogMsrpFileIncoming.enableAutoAccept = enableAutoAccept;
        
        if (inProgress == false) {
            inProgress = true;
            
            filePushRequests.clear();
            filePushRequests.add(fpr);
            //filePushRequest = fpr;
            
            activity.showDialog(DIALOG_ID);
        } else {
            
            if (filePushRequests.size() >= 2) {
                filePushRequests.clear();
                activity.dismissDialog(DIALOG_ID);
                activity.showDialog(DIALOG_ID);
            }
            
            filePushRequests.add(fpr);
        }
        
        if(enableAutoAccept) {
        	new AcceptFileTask().execute(fpr);
        } 
        
        Log.i(TAG, "showDialog#finished");
    }
    
    
    private class CancelFileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.i(TAG, "CancelFileTask.doInBackground#started");
            for(FilePushRequest fpr : filePushRequests) {
                String requestId = fpr.getRequestId();
                msrpChatActivity.getFileTransferManager().cancel(requestId);
            }
            Log.i(TAG, "CancelFileTask.doInBackground#finished");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    
    
    private static class AcceptFileTask extends AsyncTask<FilePushRequest, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(FilePushRequest... params) {
            Log.i(TAG, "AcceptFileTask.doInBackground#started");
            params[0].accept();
            Log.i(TAG, "AcceptFileTask.doInBackground#finished");
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    
    
    private FileTransMngrListener fileTransMngrListener = new FileTransMngrListener() {
        public void transferProgress(String requestId, String fileId, long bytesTransferred, long bytesTotal) {
            Log.i(TAG, "FileTransMngrListener.transferProgress#started");
            String progressText = "Progress: " + bytesTransferred + " from " + bytesTotal;
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_TRANSFER_PROGRESS;
            androidMsg.obj = new IncomingFileStatusMsg(requestId, progressText, "");
            mHandler.sendMessage(androidMsg);
            Log.i(TAG, "FileTransMngrListener.transferProgress#finished");
        }
        
        public void fileReceived(String requestId, String fileId, String filePath) {
            Log.i(TAG, "FileTransMngrListener.fileReceived#started");
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_FILE_RECEIVED;
            androidMsg.obj = new IncomingFileStatusMsg(requestId, "", filePath);
            mHandler.sendMessage(androidMsg);
            Log.i(TAG, "FileTransMngrListener.fileReceived#finished");
        }
        
        public void fileReceiveFailed(String requestId, String fileId, ReasonInfo reason) {
            Log.i(TAG, "FileTransMngrListener.fileReceiveFailed#started");
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_FILE_RECEIVE_FAILED;
            androidMsg.obj = new IncomingFileStatusMsg(requestId, "", "");
            mHandler.sendMessage(androidMsg);
            Log.i(TAG, "FileTransMngrListener.fileReceiveFailed#finished");
        }
        
        
        public void incomingFilePushRequest(FilePushRequest filePushRequest) {
            //nothing to do
        }
        
        
        public void incomingFilePullRequest(FilePullRequest filePullRequest) {
            //nothing to do
        }
        
        
        public void fileTransferFailed(String requestId, ReasonInfo reason) {
            //nothing to do
        }
        
        
        public void fileSent(String requestId, String fileId) {
            //nothing to do
        }
        
        
        public void fileSendFailed(String requestId, String fileId, ReasonInfo reason) {
            //nothing to do
        }
    }; 
    
    
    private class IncomingFileStatusMsg {
        private String requestId;
        private String progresText;
        private String filePath;
        
        public IncomingFileStatusMsg(String requestId, String progresText, String filePath) {
            this.requestId = requestId;
            this.progresText = progresText;
            this.filePath = filePath;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getProgresText() {
            return progresText;
        }

        public String getFilePath() {
            return filePath;
        }
    }

}
