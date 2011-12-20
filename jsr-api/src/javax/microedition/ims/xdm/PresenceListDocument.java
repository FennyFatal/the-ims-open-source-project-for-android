/*
 * This software code is (c) 2010 T-Mobile USA, Inc. All Rights Reserved.
 *
 * Unauthorized redistribution or further use of this material is
 * prohibited without the express permission of T-Mobile USA, Inc. and
 * will be prosecuted to the fullest extent of the law.
 *
 * Removal or modification of these Terms and Conditions from the source
 * or binary code of this software is prohibited.  In the event that
 * redistribution of the source or binary code for this software is
 * approved by T-Mobile USA, Inc., these Terms and Conditions and the
 * above copyright notice must be reproduced in their entirety and in all
 * circumstances.
 *
 * No name or trademarks of T-Mobile USA, Inc., or of its parent company,
 * Deutsche Telekom AG or any Deutsche Telekom or T-Mobile entity, may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" AND "WITH ALL FAULTS" BASIS
 * AND WITHOUT WARRANTIES OF ANY KIND.  ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS OR WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT CONCERNING THIS SOFTWARE, ITS SOURCE OR BINARY CODE
 * OR ANY DERIVATIVES THEREOF ARE HEREBY EXCLUDED.  T-MOBILE USA, INC.
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE
 * OR ITS DERIVATIVES.  IN NO EVENT WILL T-MOBILE USA, INC. OR ITS
 * LICENSORS BE LIABLE FOR LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF T-MOBILE USA,
 * INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * THESE TERMS AND CONDITIONS APPLY SOLELY AND EXCLUSIVELY TO THE USE,
 * MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE, ITS SOURCE OR BINARY
 * CODE OR ANY DERIVATIVES THEREOF, AND ARE SEPARATE FROM ANY WRITTEN
 * WARRANTY THAT MAY BE PROVIDED WITH A DEVICE YOU PURCHASE FROM T-MOBILE
 * USA, INC., AND TO THE EXTENT PERMITTED BY LAW.
 */

package javax.microedition.ims.xdm;

import android.os.RemoteException;
import com.android.ims.util.CollectionsUtils;
import com.android.ims.xdm.XDMDocumentImpl;
import com.android.ims.xdm.XDMServiceImpl;

import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.xdm.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The PresenceListDocument class represents the remotely stored XML document
 * that holds the user's presence lists. Presence lists are defined by
 * [OMA_PRES_RLS_XDM]. The document is retrieved with a call to the static
 * retrieveDocument method. The document can also be loaded from a cached copy
 * by using the static loadDocument method.
 * 
 * @see XDMDocument, PresenceList
 * 
 * @author Andrei Khomushko
 * 
 */
public final class PresenceListDocument extends XDMDocumentImpl {
    private static final String TAG = "JSR - PresenceListDocument";

    /**
     * Identifier for the Application Unique ID defined by OMA for this
     * document.
     */
    public static final String AUID = "rls-services";

    /**
     * Identifier for the recommended document path of this document defined by
     * OMA.
     */
    public static final String DEFAULT_DOCUMENT_PATH = "index";

    public static final String DEF_MIME_TYPE = "application/rls-services+xml";
    public static final String DEF_NAMESPACE = "urn:ietf:params:xml:ns:rls-services";

    private final List<PresenceList> presenceLists = new ArrayList<PresenceList>();
    private final IPresenceListDocument documentPeer;
    private final String xCAPRoot;
    private final boolean sendFullDoc;

    private PresenceListDocument(final XDMService xdmService,
            final String documentSelector, final String eTag,
            final IPresenceListDocument documentPeer,
            final List<PresenceList> presenceListsToAdd, final String xCAPRoot,
            final boolean sendFullDoc) {
        super(xdmService, documentSelector, eTag);

        assert documentPeer != null;
        this.documentPeer = documentPeer;

        this.xCAPRoot = xCAPRoot;
        this.sendFullDoc = sendFullDoc;

        if (presenceListsToAdd != null) {
            presenceLists.addAll(presenceListsToAdd);
        }

    }

    /**
     * Creates or replaces a presence list document on the XDM server. The
     * content of the document will be the root element as defined by OMA.
     * 
     * If the document does not exist it will be created. If the document
     * already exists it will be overwritten.
     * 
     * @param service
     *            - the XDMService associated with this document
     * @param documentPath
     *            - the document name, optionally prefixed by one or more
     *            subdirectories, or null to indicate the default document path
     * @return a PresenceListDocument
     * @throws IllegalArgumentException
     *             - if the service argument is null
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    public static PresenceListDocument createDocument(XDMService service,
            String documentPath) throws ServiceClosedException, XCAPException,
            IOException {
        if (service == null) {
            throw new IllegalArgumentException("the service argument is null");
        }

        if (!service.isOpen()) {
            throw new ServiceClosedException("Service alredy closed");
        }

        PresenceListDocument retValue = null;

        String content = XDMUtils.RlsServicesCreator.presenceDocToXml(null, DEF_NAMESPACE);
        
        String documentSelector = XCAPRequest.createUserDocumentSelector(AUID,
            documentPath != null ? documentPath : DEFAULT_DOCUMENT_PATH);
        
        XCAPRequest xcapRequest = XCAPRequest.createPutRequest(
            documentSelector, DEF_MIME_TYPE, content, null);

        retValue = doCreateDocument((XDMServiceImpl) service, xcapRequest,
            documentSelector);

        return retValue;
    }

    private static PresenceListDocument doCreateDocument(
            XDMServiceImpl xdmService, XCAPRequest request,
            String documentSelector) throws ServiceClosedException,
            IOException, XCAPException {
        PresenceListDocument retValue = null;

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();

        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xdmService
                .getXcapRoot());

        IExceptionHolder exceptionHolder = new IExceptionHolder();
        IPresenceListsHolder listHolder = new IPresenceListsHolder();
        try {
            IPresenceListDocument listDocument = xdmServicePeer
                    .createPresenceListDocument(iRequest, listHolder,
                            exceptionHolder);
            if (exceptionHolder.getParcelableException() == null) {
                // TODO retrieve etag
                retValue = new PresenceListDocument(xdmService,
                        documentSelector, listHolder.getEtag(), listDocument,
                        null, xdmService.getXcapRoot(), xdmService.isXdmSendFullDoc());
            } else {
                throw XDMUtils
                        .createXCAPException((IXCAPException) exceptionHolder
                                .getParcelableException());
            }

        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        return retValue;
    }

    /**
     * Deletes an existing presence list document on the XDM server.
     * 
     * The ETag parameter can be used to conditionally delete the document. The
     * document will only be deleted if the ETag of the document on the server
     * matches the ETag argument. To unconditionally delete the document, the
     * ETag argument can be set to null.
     * 
     * @param service
     *            - the XDMService associated with this document
     * @param documentPath
     *            - the document name, optionally prefixed by one or more
     *            subdirectories, or null to indicate the default document path
     * @param etag
     *            - the ETag as described above
     * @throws IllegalArgumentException
     *             - if the service argument is null
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    public static void deleteDocument(XDMService service, String documentPath,
            String etag) throws ServiceClosedException, XCAPException,
            IOException {
        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (!service.isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        String documentSelector = XCAPRequest.createUserDocumentSelector(AUID,
                documentPath);
        XCAPRequest xcapRequest = XCAPRequest.createDeleteRequest(
                documentSelector, null, etag);
        if (etag != null) {
            xcapRequest.addIfMathHeader(etag);
        }
        doDeleteDocument((XDMServiceImpl) service, xcapRequest,
                documentSelector);
    }

    private static void doDeleteDocument(XDMServiceImpl xdmService,
            XCAPRequest request, String documentSelector)
            throws ServiceClosedException, IOException, XCAPException {

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();
        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xdmService
                .getXcapRoot());
        IExceptionHolder exceptionHolder = new IExceptionHolder();
        try {
            xdmServicePeer
                    .deletePresenceListDocument(iRequest, exceptionHolder);

            if (exceptionHolder.getParcelableException() != null) {
                throw XDMUtils
                        .createXCAPException((IXCAPException) exceptionHolder
                                .getParcelableException());
            }
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Retrieves an existing presence list document from the XDM server.
     * 
     * The ETag parameter can be used to conditionally fetch the document. The
     * document will only be fetched if the ETag of the document on the server
     * is different from the ETag argument. To unconditionally fetch the
     * document, the ETag argument can be set to null.
     * 
     * @param service
     *            - the XDMService associated with this document
     * @param documentPath
     *            - the document name, optionally prefixed by one or more
     *            subdirectories, or null to indicate the default document path
     * @param xui
     *            - the XUI of the user, or null to indicate the current user,
     *            see [RFC4825]
     * @param etag
     *            - the ETag as described above
     * @return a PresenceListDocument
     * @throws IllegalArgumentException
     *             - if the service argument is null
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    public static PresenceListDocument retrieveDocument(XDMService service,
            String documentPath, String xui, String etag)
            throws ServiceClosedException, XCAPException, IOException {
        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (!service.isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        PresenceListDocument retValue = null;

        final String documentSelector;
        String docPath = (documentPath == null ? DEFAULT_DOCUMENT_PATH
                : documentPath);
        if (xui == null) {
            documentSelector = XCAPRequest.createUserDocumentSelector(AUID,
                    docPath);
        } else {
            documentSelector = XCAPRequest.createUserDocumentSelector(AUID,
                    xui, docPath);
        }

        XCAPRequest xcapRequest = XCAPRequest.createGetRequest(
                documentSelector, null, etag);
        if (etag != null) {
            xcapRequest.addIfNoneMathHeader(etag);
        }

        retValue = doRetrieveDocument((XDMServiceImpl) service, xcapRequest,
                documentSelector);

        return retValue;
    }

    private static PresenceListDocument doRetrieveDocument(
            XDMServiceImpl xdmService, XCAPRequest request,
            String documentSelector) throws ServiceClosedException,
            IOException, XCAPException {

        PresenceListDocument retValue = null;

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();

        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xdmService
                .getXcapRoot());

        IExceptionHolder exceptionHolder = new IExceptionHolder();
        IPresenceListsHolder iPresenceListsHolder = new IPresenceListsHolder();
        try {
            IPresenceListDocument presenceListDocument = xdmServicePeer
                    .retrievePresenceListDocument(iRequest,
                            iPresenceListsHolder, exceptionHolder);

            if (exceptionHolder.getParcelableException() == null) {
                retValue = createPresenceListDocument(xdmService,
                        documentSelector, presenceListDocument,
                        iPresenceListsHolder);

            } else {
                throw XDMUtils
                        .createXCAPException((IXCAPException) exceptionHolder
                                .getParcelableException());
            }

        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        return retValue;
    }

    private static PresenceListDocument createPresenceListDocument(
            XDMServiceImpl xdmService, String documentSelector,
            IPresenceListDocument listDocument,
            IPresenceListsHolder iPresenceListsHolder)
            throws ServiceClosedException, IOException {

        PresenceListDocument retValue = null;

        List<PresenceList> presenceLists = new ArrayList<PresenceList>();
        if (iPresenceListsHolder.getIPresenceLists() != null) {
            for (IPresenceList iPresenceList : iPresenceListsHolder
                    .getIPresenceLists()) {
                presenceLists.add(new PresenceList(iPresenceList
                        .getServiceUri(), iPresenceList.getUriListReference()));
            }
        }

        retValue = new PresenceListDocument(xdmService, documentSelector,
                iPresenceListsHolder.getEtag(), listDocument, presenceLists,
                xdmService.getXcapRoot(), xdmService.isXdmSendFullDoc());

        return retValue;
    }

    /**
     * Loads a PresenceListDocument from an InputStream.
     * 
     * The ETag parameter can be used to set an ETag on the loaded document.
     * This ETag is used for conditional operations on the document, meaning
     * that modifications on the document will only be successful if the ETag of
     * the document on the server matches the ETag argument. To only be able to
     * use unconditional operations, the ETag argument can be set to null when
     * loading the document.
     * 
     * @param service
     *            - the XDMService associated with this document
     * @param documentPath
     *            - the document name, optionally prefixed by one or more
     *            subdirectories, or null to indicate the default document path
     * @param etag
     *            - the ETag as described above
     * @param is
     *            - the input stream containing the content of the document
     * @return a PresenceListDocument
     * @throws IllegalArgumentException
     *             - if the service argument is null
     * @throws IllegalArgumentException
     *             - if the is argument is null
     * @throws IOException
     *             - if the is argument does not contain valid XML for this AUID
     */
    public static PresenceListDocument loadDocument(XDMService service,
            String documentPath, String etag, InputStream is)
            throws IOException {
        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (is == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        PresenceListDocument retValue = null;

        String docPath = (documentPath == null ? DEFAULT_DOCUMENT_PATH
                : documentPath);
        String documentSelector = XCAPRequest.createUserDocumentSelector(AUID,
                docPath);
        String source = XDMUtils.readToString(is);
        retValue = doLoadDocument((XDMServiceImpl)service, documentSelector, etag, source);

        return retValue;
    }

    private static PresenceListDocument doLoadDocument(XDMServiceImpl xdmService,
            String documentSelector, String etag, String source)
            throws ServiceClosedException, IOException {

        PresenceListDocument retValue = null;

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();

        IPresenceListsHolder iPresenceListsHolder = new IPresenceListsHolder();
        IExceptionHolder exceptionHolder = new IExceptionHolder();
        try {
            IPresenceListDocument uriListDocument = xdmServicePeer
                    .loadPresenceListDocument(documentSelector, etag, source,
                            iPresenceListsHolder, exceptionHolder);

            if (exceptionHolder.getParcelableException() == null) {
                retValue = createPresenceListDocument(xdmService,
                        documentSelector, uriListDocument, iPresenceListsHolder);
            } else {
                IXCAPException exception = (IXCAPException) exceptionHolder
                        .getParcelableException();
                throw new IOException(exception.getReasonPhrase());
            }
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        return retValue;
    }

    /**
     * Adds a PresenceList to this PresenceListDocument and submits the changes
     * to the document on the XDM server. This method is synchronous and will
     * block until the server responds.
     * 
     * If the PresenceList does not exist in the document it will be created. If
     * the PresenceList already exists it will be overwritten.
     * 
     * @param presenceList
     *            - the PresenceList to add to the PresenceListDocument
     * @throws IllegalArgumentException
     *             - if the presenceList argument is null
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    public void addPresenceList(PresenceList presenceList)
            throws ServiceClosedException, XCAPException, IOException {

        if (presenceList == null) {
            throw new IllegalArgumentException(
                    "the presenceList argument is null");
        }

        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }
        
        presenceLists.add(presenceList);
        
        XCAPRequest xcapRequest;
        
        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        } else {
            XCAPNodeSelector nodeSelector = new XCAPNodeSelector()
                .selectElementByName("rls-services")
                .selectElementByAttribute("service", "uri", presenceList.getServiceURI());

            String content = XDMUtils.RlsServicesCreator.presenceListToXml(presenceList, DEF_NAMESPACE);
        
            xcapRequest = XCAPRequest.createPutRequest(
                getDocumentSelector(), nodeSelector, content, getEtag());
        }

        doSyncDocumentChanges(xcapRequest);
    }

    private void doSyncDocumentChanges(XCAPRequest xcapRequest)
            throws XCAPException, IOException {
        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(xcapRequest, xCAPRoot);

        IExceptionHolder exceptionHolder = new IExceptionHolder();

        try {
            documentPeer.syncDocumentChanges(iRequest, exceptionHolder);
            if (exceptionHolder.getParcelableException() != null) {
                throw XDMUtils
                        .createXCAPException((IXCAPException) exceptionHolder
                                .getParcelableException());
            }
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Deletes the PresenceList identified by the serviceURI argument from the
     * document on the XDM server. This method is synchronous and will block
     * until the server responds.
     * 
     * @param serviceURI
     *            - the service URI of the PresenceList to be deleted
     * @throws IllegalArgumentException
     *             - if the PresenceList identified by the serviceURI argument
     *             does not exist in the PresenceListDocument
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    public void deletePresenceList(final String serviceURI)
            throws ServiceClosedException, XCAPException, IOException {

        PresenceList listToRemove = getPresenceList(serviceURI);

        if (listToRemove == null) {
            throw new IllegalArgumentException(
                    "The URIList identified by the name argument does not exist in the URIListDocument");
        }

        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        presenceLists.remove(listToRemove);
        
        XCAPRequest xcapRequest;
        
        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        } else {
            XCAPNodeSelector nodeSelector = new XCAPNodeSelector()
                .selectElementByName("rls-services")
                .selectElementByAttribute("service", "uri", serviceURI);
    
            xcapRequest = XCAPRequest.createDeleteRequest(
                getDocumentSelector(), nodeSelector, getEtag());
        }

        doSyncDocumentChanges(xcapRequest);
    }

    private XCAPRequest sendFullDocument() {
        XCAPRequest xcapRequest;
        String content = XDMUtils.RlsServicesCreator.presenceDocToXml(presenceLists, DEF_NAMESPACE);
        
        xcapRequest = XCAPRequest.createPutRequest(
            getDocumentSelector(), DEF_MIME_TYPE, content, getEtag());
        return xcapRequest;
    }

    /**
     * Returns the PresenceList identified by the serviceURI argument.
     * 
     * Note: Changes made to a PresenceList will not take effect on the
     * PresenceListDocument until addPresenceList is called.
     * 
     * @param serviceURI
     *            - the service URI of the presence list to be retrieved
     * @return the PresenceList or null if the PresenceList does not exist
     */
    public PresenceList getPresenceList(final String serviceURI) {
        return CollectionsUtils.find(presenceLists,
                new CollectionsUtils.Predicate<PresenceList>() {
                    
                    public boolean evaluate(final PresenceList presenceList) {
                        return presenceList.getServiceURI().equals(serviceURI);
                    }
                });
    }

    /**
     * Returns all PresenceList objects in this PresenceListDocument.
     * 
     * Note: Changes made to a PresenceList will not take effect on the
     * PresenceListDocument until addPresenceList is called.
     * 
     * @return an array of PresenceList or an empty array if no presence lists
     *         are available
     */
    public PresenceList[] getPresenceLists() {
        return presenceLists.toArray(new PresenceList[0]);
    }

    
    public void saveDocument(OutputStream os) throws IOException {
        if (os == null) {
            throw new IllegalArgumentException("The os argument is null");
        }

        //String xmlContent = null/* documentPeer.getXMLContent() */;
        String xmlContent = XDMUtils.RlsServicesCreator.presenceDocToXml(presenceLists, DEF_NAMESPACE);
        if (xmlContent != null) {
            XDMUtils.writeXmlToStream(xmlContent, os);
        } else {
            throw new IOException("Can't retrieve data.");
        }
    }

    
    public String toString() {
        return "PresenceListDocument [presenceLists=" + presenceLists
                + ", toString()=" + super.toString() + "]";
    }
}
