package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.IFilePushRequest;
import javax.microedition.ims.android.msrp.IFilePullRequest;

interface IFileTransferManagerListener
{

    void fileSent(String requestId, String fileId);
    
    void fileSendFailed(String requestId, String fileId, in IReasonInfo reason);
    
    void fileReceived(String requestId, String fileId, String filePath);
    
    void fileReceiveFailed(String requestId, String fileId, in IReasonInfo reason);
    
    void incomingFilePushRequest(IFilePushRequest filePushRequest);
    
    void incomingFilePullRequest(IFilePullRequest filePullRequest);
    
    void transferProgress(String requestId, String fileId, long bytesTransferred, long bytesTotal);
    
    void fileTransferFailed(String requestId, in IReasonInfo reason);

}
