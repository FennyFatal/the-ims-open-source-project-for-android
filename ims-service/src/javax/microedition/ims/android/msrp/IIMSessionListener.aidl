package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.IIMSession;
import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.android.msrp.IFilePushRequest;

interface IIMSessionListener
{

    void composingIndicatorReceived(String sender, String messageBody);
    
    void fileReceived(String requestId, String fileId, String filePath);
    
    void fileReceiveFailed(String requestId, String fileId, in IReasonInfo reason);
    
    void fileSendFailed(String requestId, String fileId, in IReasonInfo reason);
    
    void fileSent(String requestId, String fileId);
    
    void fileTransferFailed(String requestId, in IReasonInfo reason);
    
    void fileTransferProgress(String requestId, String fileId, long bytesTransferred, long bytesTotal);
    
    void incomingFilePushRequest(IFilePushRequest filePushRequest);
    
    void messageReceived(in IMessage retValue);
    
    void messageSendFailed(String messageId, in IReasonInfo reason);
    
    void messageSent(String messageId);
    
    void sessionClosed(in IReasonInfo reason);
    
    void systemMessageReceived(in IMessage retValue);

}
