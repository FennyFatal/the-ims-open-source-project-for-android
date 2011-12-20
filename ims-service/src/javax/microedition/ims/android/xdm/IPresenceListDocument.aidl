package javax.microedition.ims.android.xdm;

import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.xdm.IXCAPRequest;
import javax.microedition.ims.android.xdm.IListEntry;

interface IPresenceListDocument {
    void applyChanges(String xcapDiffDocument);
    
    String syncDocumentChanges(in IXCAPRequest sipMsg, out IExceptionHolder exceptionHolder);
}