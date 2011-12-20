package javax.microedition.ims.android.xdm;

import javax.microedition.ims.android.IReasonInfo;

interface IDocumentSubscriberListener {
    void subscriptionStarted();
    
    void subscriptionFailed(in IReasonInfo reason);
    
    void subscriptionTerminated();
    
    void documentUpdateReceived(String documentSelector, String xcapDiff);
            
    void documentDeleted(String documentSelector, String xcapDiff);        
}