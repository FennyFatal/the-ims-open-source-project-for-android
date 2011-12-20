package javax.microedition.ims.android.xdm;

import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.xdm.IXDMServiceListener;
import javax.microedition.ims.android.xdm.IXCAPRequest;
import javax.microedition.ims.android.xdm.IXCAPResponse;
import javax.microedition.ims.android.xdm.IDocumentEntry;
import javax.microedition.ims.android.xdm.IURIListDocument;
import javax.microedition.ims.android.xdm.IURIList;
import javax.microedition.ims.android.xdm.IURIListsHolder;
import javax.microedition.ims.android.xdm.IPresenceListDocument;
import javax.microedition.ims.android.xdm.IPresenceList;
import javax.microedition.ims.android.xdm.IPresenceListsHolder;
import javax.microedition.ims.android.xdm.IDocumentSubscriber;
import javax.microedition.ims.android.xdm.IPresenceAuthorizationDocument;
import javax.microedition.ims.android.xdm.IPresenceAuthorizationRulesHolder;


interface IXDMService {
    String getAppId();
    String getSheme();
    String getXCAPRoot();
    String getXUI();
    void close();
    boolean isXdmSendFullDoc();
    
    void addXDMServiceListener(in IXDMServiceListener listener);
    void removeXDMServiceListener(in IXDMServiceListener listener);
    
    IDocumentSubscriber createDocumentSubscriber(in String[] urls, out IExceptionHolder exceptionHolder);
    IXCAPResponse sendXCAPRequest(in IXCAPRequest sipMsg, out IExceptionHolder exceptionHolder);
    IDocumentEntry[] listDocuments(String auid, out IExceptionHolder exceptionHolder);
    
    IURIListDocument createURIListDocument(in IXCAPRequest sipMsg, out IURIListsHolder uriListHolder, out IExceptionHolder exceptionHolder);
    void deleteURIListDocument(in IXCAPRequest sipMsg, out IExceptionHolder exceptionHolder);
    IURIListDocument retrieveURIListDocument(in IXCAPRequest sipMsg, out IURIListsHolder uriListHolder, out IExceptionHolder exceptionHolder);
    IURIListDocument loadURIListDocument(in String documentSelector, in String etag, in String source, out IURIListsHolder uriListHolder, out IExceptionHolder exceptionHolder);
    
    
    IPresenceListDocument createPresenceListDocument(in IXCAPRequest sipMsg, out IPresenceListsHolder presenceListsHolder, out IExceptionHolder exceptionHolder);
    void deletePresenceListDocument(in IXCAPRequest sipMsg, out IExceptionHolder exceptionHolder);
    IPresenceListDocument retrievePresenceListDocument(in IXCAPRequest sipMsg, out IPresenceListsHolder presenceListsHolder, out IExceptionHolder exceptionHolder);
    IPresenceListDocument loadPresenceListDocument(in String documentSelector, in String etag, in String source, out IPresenceListsHolder PresenceListsHolder, out IExceptionHolder exceptionHolder);
    
    
    IPresenceAuthorizationDocument createPresenceAuthorizationDocument(in IXCAPRequest sipMsg, out IPresenceAuthorizationRulesHolder presenceAuthorizationRulesHolder, out IExceptionHolder exceptionHolder);
    void deletePresenceAuthorizationDocument(in IXCAPRequest sipMsg, out IExceptionHolder exceptionHolder);
    IPresenceAuthorizationDocument retrievePresenceAuthorizationDocument(in IXCAPRequest sipMsg, out IPresenceAuthorizationRulesHolder presenceAuthorizationRulesHolder, out IExceptionHolder exceptionHolder);
    
}
