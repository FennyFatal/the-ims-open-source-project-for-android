package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.msrp.IFileInfo;
import javax.microedition.ims.android.msrp.IFileSelector;
import javax.microedition.ims.android.msrp.IFileTransferManagerListener;

interface IFileTransferManager
{

    String sendFiles(String sender, in String[] recipients, String subject, in IFileInfo[] files, boolean deliveryReport);
    
    String requestFiles(String requestSender, String requestRecipient, String subject, in IFileSelector[] files);
    
    void cancel(String identifier);
    
    
    void addListener(IFileTransferManagerListener listener);
    void removeListener(IFileTransferManagerListener listener);
    
    boolean isAvailable4Cancel(String identifier);

}
