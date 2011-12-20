package javax.microedition.ims.android.xdm;

import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.xdm.IXCAPRequest;


interface IPresenceAuthorizationDocument {
    void applyChanges(String xcapDiffDocument);
    
    String syncDocumentChanges(in IXCAPRequest sipMsg, out IExceptionHolder exceptionHolder);
}
