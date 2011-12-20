package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.android.msrp.ILargeMessageRequest;

interface IMessageManagerListener
{

    void incomingLargeMessage(ILargeMessageRequest largeMessageRequest);
    
    void messageReceived(in IMessage retValue);
    
    void messageReceiveFailed(String messageId, in IReasonInfo reason);
    
    void messageSendFailed(String messageId, in IReasonInfo reason);
    
    void messageSent(String messageId);
    
    void transferProgress(String messageId, int bytesTransferred, int bytesTotal);

}
