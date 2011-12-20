package javax.microedition.ims.engine.test.msrp;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.engine.test.MsrpChatActivity;
import javax.microedition.ims.engine.test.MsrpChatActivity.FileTransMngrListener;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.engine.test.UriUtils;
import javax.microedition.ims.engine.test.util.ContactsHolder;
import javax.microedition.ims.im.FileInfo;
import javax.microedition.ims.im.FilePullRequest;
import javax.microedition.ims.im.FilePushRequest;
import java.io.File;
import java.io.IOException;

public class DialogMsrpFileProgress extends Dialog {
    
    private final static String TAG = "DialogMsrpFileProgress";
    
    private final MsrpChatActivity msrpChatActivity;
    
    private String requestIdFirstSendFileTask;
    private String requestIdSecondSendFileTask;
    
    private static File file;

    public static final int DIALOG_ID = 22;
    
    private static final int ON_TRANSFER_PROGRESS = 101;
    private static final int ON_FILE_SENT = 102;
    private static final int ON_FILE_SEND_FAILED = 103;
    private static final int ON_TRANSFER_STARTED = 104;
    

    public enum TaskNumber {
        FIRST, SECOND
    }

    private final Handler mHandler = new Handler() {
        
        public void handleMessage(android.os.Message msg) {
            Log.i(TAG, "handleMessage#started");
            switch (msg.what) {
                case ON_TRANSFER_PROGRESS: {
                    Log.i(TAG, "handleMessage#ON_TRANSFER_PROGRESS");
                    FileStatusMsg fileStatusMsg = (FileStatusMsg)msg.obj;
                    
                    if (fileStatusMsg.getRequestId().equals(requestIdFirstSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_transfer_progress1)).setText(fileStatusMsg.getProgresText());
                    } else if (fileStatusMsg.getRequestId().equals(requestIdSecondSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_transfer_progress2)).setText(fileStatusMsg.getProgresText());
                    } else {
                        throw new IllegalStateException("Not equal requestIds:   cur=" + fileStatusMsg.getRequestId()
                            + "   first="+requestIdFirstSendFileTask + "   second=" + requestIdSecondSendFileTask);
                    }
                }
                break;
                
                case ON_FILE_SENT: {
                    Log.i(TAG, "handleMessage#ON_FILE_SENT");
                    FileStatusMsg fileStatusMsg = (FileStatusMsg)msg.obj;
                    
                    if (fileStatusMsg.getRequestId().equals(requestIdFirstSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_transfer_state1)).setText("Completed");
                    } else if (fileStatusMsg.getRequestId().equals(requestIdSecondSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_transfer_state2)).setText("Completed");
                    } else {
                        throw new IllegalStateException("Not equal requestIds:   cur=" + fileStatusMsg.getRequestId()
                            + "   first="+requestIdFirstSendFileTask + "   second=" + requestIdSecondSendFileTask);
                    }

                    findViewById(R.id.msrp_file_progress_cancel_btn).setEnabled(false);
                }
                break;
                
                case ON_FILE_SEND_FAILED: {
                    Log.i(TAG, "handleMessage#ON_FILE_SEND_FAILED");
                    FileStatusMsg fileStatusMsg = (FileStatusMsg)msg.obj;
                    
                    if (fileStatusMsg.getRequestId().equals(requestIdFirstSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_transfer_state1)).setText("Send failed");
                    } else if (fileStatusMsg.getRequestId().equals(requestIdSecondSendFileTask)) {
                        ((TextView)findViewById(R.id.msrp_file_transfer_state2)).setText("Send failed");
                    } else {
                        throw new IllegalStateException("Not equal requestIds:   cur=" + fileStatusMsg.getRequestId()
                            + "   first="+requestIdFirstSendFileTask + "   second=" + requestIdSecondSendFileTask);
                    }

                    findViewById(R.id.msrp_file_progress_cancel_btn).setEnabled(false);
                }
                break;
                
                case ON_TRANSFER_STARTED: {
                    Log.i(TAG, "handleMessage#ON_FILE_SEND_FAILED");
                    TaskNumber taskNumber = (TaskNumber)msg.obj;
                    
                    if (taskNumber == TaskNumber.FIRST) {
                        ((TextView)findViewById(R.id.msrp_file_transfer_state1)).setText("Started");
                    } else if (taskNumber == TaskNumber.SECOND) {
                        ((TextView)findViewById(R.id.msrp_file_transfer_state2)).setText("Started");
                    }
                }
                break;
            
                default: {
                    super.handleMessage(msg);
                    break;
                }
            }
            Log.i(TAG, "handleMessage#finished");
        }
    };


    public DialogMsrpFileProgress(MsrpChatActivity msrpChatActivity) {
        super(msrpChatActivity);
        this.msrpChatActivity = msrpChatActivity;
    }
    
    public static DialogMsrpFileProgress createDialog(MsrpChatActivity msrpChatActivity) {
        Log.i(TAG, "createDialog#started");
        final DialogMsrpFileProgress dialog = new DialogMsrpFileProgress(msrpChatActivity);
        
        dialog.setContentView(R.layout.dialog_msrp_file_progress);
        dialog.setTitle(R.string.msrp_file_progress_title);

        dialog.findViewById(R.id.msrp_file_progress_start1_btn).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.doStart();
                }
            }
        );

        dialog.findViewById(R.id.msrp_file_progress_start2_btn).setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.doStart2x();
                }
            }
        );
        
        dialog.findViewById(R.id.msrp_file_progress_cancel_btn).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.doCancel();
                }
            }
        );
        
        dialog.findViewById(R.id.msrp_file_progress_close_btn).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.doClose();
                }
            }
        );
        
        
        String[] remoteIdentities = ContactsHolder.getContacts(msrpChatActivity);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(msrpChatActivity, android.R.layout.simple_spinner_item, remoteIdentities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner recepient1Control = (Spinner) dialog.findViewById(R.id.msrp_file_recepient1);
        recepient1Control.setAdapter(adapter);
        Spinner recepient2Control = (Spinner) dialog.findViewById(R.id.msrp_file_recepient2);
        recepient2Control.setAdapter(adapter);
        
        Log.i(TAG, "createDialog#finished");
        
        return dialog;
    }
    
    private String getFirstRecepient() {
        Spinner firstRecepientControl = (Spinner) findViewById(R.id.msrp_file_recepient1);
        int pos = firstRecepientControl.getSelectedItemPosition();
        String retValue = ContactsHolder.getContacts(msrpChatActivity)[pos];
        return UriUtils.encodeUri(retValue);
    }    
    
    private String getSecondRecepient() {
        Spinner secondRecepientControl = (Spinner) findViewById(R.id.msrp_file_recepient2);
        int pos = secondRecepientControl.getSelectedItemPosition();
        String retValue = ContactsHolder.getContacts(msrpChatActivity)[pos];
        return UriUtils.encodeUri(retValue);
    }    
    
    public void onPrepare() {
        Log.i(TAG, "onPrepare#started");
        msrpChatActivity.removeAllFileTransMngrListeners();
        msrpChatActivity.addFileTransMngrListener(fileTransMngrListener);
        
        ((TextView)findViewById(R.id.msrp_file_transfer_path)).setText("File: " + file.getPath());
        ((TextView)findViewById(R.id.msrp_file_transfer_state1)).setText("Not started");
        ((TextView)findViewById(R.id.msrp_file_transfer_progress1)).setText("Progress: 0 from 0");
        ((TextView)findViewById(R.id.msrp_file_transfer_state2)).setText("Not started");
        ((TextView)findViewById(R.id.msrp_file_transfer_progress2)).setText("Progress: 0 from 0");
        
        requestIdFirstSendFileTask = null;
        requestIdSecondSendFileTask = null;
        
        findViewById(R.id.msrp_file_progress_start1_btn).setEnabled(true);
        findViewById(R.id.msrp_file_progress_start2_btn).setEnabled(true);
        findViewById(R.id.msrp_file_progress_cancel_btn).setEnabled(false);
        Log.i(TAG, "onPrepare#finished");
    }
    
    private void doStart() {
        Log.i(TAG, "doStart#started");
        new SendFileTask().execute(
            new SendFileTaskParams(file, "First file description", getFirstRecepient(), TaskNumber.FIRST)
        );
        findViewById(R.id.msrp_file_progress_start1_btn).setEnabled(false);
        findViewById(R.id.msrp_file_progress_start2_btn).setEnabled(false);
        findViewById(R.id.msrp_file_progress_cancel_btn).setEnabled(true);
        Log.i(TAG, "doStart#finished");
    }
    
    private void doStart2x() {
        Log.i(TAG, "doStart2x#started");
        new SendFileTask().execute(
            new SendFileTaskParams(file, "First file description", getFirstRecepient(), TaskNumber.FIRST)
        );
        new SendFileTask().execute(
            new SendFileTaskParams(file, "Second file description", getSecondRecepient(), TaskNumber.SECOND)
        );
        findViewById(R.id.msrp_file_progress_start1_btn).setEnabled(false);
        findViewById(R.id.msrp_file_progress_start2_btn).setEnabled(false);
        findViewById(R.id.msrp_file_progress_cancel_btn).setEnabled(true);
        Log.i(TAG, "doStart2x#finished");
    }
    
    private void doCancel() {
        Log.i(TAG, "doCancel#started");
        new CancelFileTask().execute();
        findViewById(R.id.msrp_file_progress_cancel_btn).setEnabled(false);
        Log.i(TAG, "doCancel#finished");
    }
    
    private void doClose() {
        Log.i(TAG, "doClose#started");
        msrpChatActivity.dismissDialog(DIALOG_ID);
        Log.i(TAG, "doClose#finished");
    }
    
    public static void showDialog(MsrpChatActivity activity, File f) {
        Log.i(TAG, "showDialog#started");
        file = f;
        activity.showDialog(DIALOG_ID);
        Log.i(TAG, "showDialog#finished");
    }
    
    
    private class SendFileTask extends AsyncTask<SendFileTaskParams, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(SendFileTaskParams... params) {
            Log.i(TAG, "SendFileTask#started");

            //String remoteUserIdent = msrpChatActivity.getRemoteUserIdent();
            String recepient = params[0].getRecepient();
            TaskNumber taskNumber = params[0].getTaskNumber();
            
            File file4Transfer = params[0].getFile4Transfer();
            String fileDescription = params[0].getFileDescription();
            FileInfo fileInfo = prepareFileInfo(file4Transfer, fileDescription);
            
            try {
//                msrpChatActivity.getFileTransferManager().sendFiles(String sender, String[] recipients, String subject,
//                        FileInfo[] files, boolean deliveryReport)
                
                String requestId = msrpChatActivity.getFileTransferManager().sendFiles(null,
                        new String[] {recepient}, "File transfer subject",
                        new FileInfo[] {fileInfo}, false);
                
                android.os.Message androidMsg = new android.os.Message();
                androidMsg.what = ON_TRANSFER_STARTED;
                androidMsg.obj = taskNumber;
                mHandler.sendMessage(androidMsg);
                
                if (taskNumber.equals(TaskNumber.FIRST)) {
                    requestIdFirstSendFileTask = requestId;
                } else if (taskNumber.equals(TaskNumber.SECOND)) {
                    requestIdSecondSendFileTask = requestId;
                }
                
            } catch (ImsException e) {
                Log.i("INFO_test", e.getMessage());
            } catch (IOException e) {
                Log.i("INFO_test", e.getMessage());
            }

            Log.i(TAG, "SendFileTask#finished");
            return null;
        }

        private FileInfo prepareFileInfo(File file4Transfer, String fileDescription) {
            String fileName = file4Transfer.getName();
            String temp = null;
            if(fileName.indexOf('.') > 0){
            	temp = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
            }
            String mimeType = null;
            if(temp != null && temp.length() > 0) {
            	mimeType =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(temp);
            }
            FileInfo fileInfo = new FileInfo(file4Transfer.getPath(), mimeType != null ? mimeType : "foo/bar");
            fileInfo.setDescription(fileDescription);
            fileInfo.setSize((int)file4Transfer.length());
            //TODO add hash:sha1 calculations to client
            fileInfo.setHash("f9124b646ff65615338baa207ee0e8b8918c661a");
            return fileInfo;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    
    private static class SendFileTaskParams {
        private File file4Transfer;
        private String fileDescription;
        private String recepient;
        private TaskNumber taskNumber;

        public SendFileTaskParams(File file4Transfer, String fileDescription, String recepient, TaskNumber taskNumber) {
            this.file4Transfer = file4Transfer;
            this.fileDescription = fileDescription;
            this.recepient = recepient;
            this.taskNumber = taskNumber;
        }
        
        public File getFile4Transfer() {
            return file4Transfer;
        }

        public String getFileDescription() {
            return fileDescription;
        }

        public String getRecepient() {
            return recepient;
        }

        public TaskNumber getTaskNumber() {
            return taskNumber;
        }
    }
    
    
    private class CancelFileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.i(TAG, "CancelFileTask#started");
            if (requestIdFirstSendFileTask != null) {
                msrpChatActivity.getFileTransferManager().cancel(requestIdFirstSendFileTask);
            }
            if (requestIdSecondSendFileTask != null) {
                msrpChatActivity.getFileTransferManager().cancel(requestIdSecondSendFileTask);
            }
            Log.i(TAG, "CancelFileTask#finished");
            return null;
        }

        @Override
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
            androidMsg.obj = new FileStatusMsg(requestId, progressText);
            mHandler.sendMessage(androidMsg);
            Log.i(TAG, "FileTransMngrListener.transferProgress#finished");
        }
        
        public void fileSent(String requestId, String fileId) {
            Log.i(TAG, "FileTransMngrListener.fileSent#started");
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_FILE_SENT;
            androidMsg.obj = new FileStatusMsg(requestId, "");
            mHandler.sendMessage(androidMsg);
            Log.i(TAG, "FileTransMngrListener.fileSent#finished");
        }
        
        public void fileSendFailed(String requestId, String fileId, ReasonInfo reason) {
            Log.i(TAG, "FileTransMngrListener.fileSendFailed#started");
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_FILE_SEND_FAILED;
            androidMsg.obj = new FileStatusMsg(requestId, "");
            mHandler.sendMessage(androidMsg);
            Log.i(TAG, "FileTransMngrListener.fileSendFailed#finished");
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
        
        
        public void fileReceived(String requestId, String fileId, String filePath) {
            //nothing to do
        }
        
        
        public void fileReceiveFailed(String requestId, String fileId, ReasonInfo reason) {
            //nothing to do
        }
    };
    
    private class FileStatusMsg {
        private String requestId;
        private String progresText;
        
        public FileStatusMsg(String requestId, String progresText) {
            this.requestId = requestId;
            this.progresText = progresText;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getProgresText() {
            return progresText;
        }
    }
    
}
