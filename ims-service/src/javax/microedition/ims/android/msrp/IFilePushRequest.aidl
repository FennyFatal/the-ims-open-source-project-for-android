package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.msrp.IFileInfo;

interface IFilePushRequest
{

    void accept();
    
    void reject();
    
    String getRequestId();
    
    String getSender();
    
    String[] getRecipients();
    
    String getSubject();
    
    IFileInfo[] getFileInfos();
    
    boolean isExpired();

}
