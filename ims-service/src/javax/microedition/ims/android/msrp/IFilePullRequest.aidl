package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.msrp.IFileSelector;

interface IFilePullRequest
{

    void accept();
    
    void reject();
    
    String getRequestId();
    
    String getSender();
    
    String getRecipient();
    
    String getSubject();
    
    IFileSelector[] getFileSelectors();
    
    void setFilePath(in IFileSelector fileSelector, String filePath);
    
    boolean isExpired();

}
