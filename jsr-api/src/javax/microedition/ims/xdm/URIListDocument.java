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
import android.text.TextUtils;
import com.android.ims.util.CollectionsUtils;
import com.android.ims.xdm.URIListImpl;
import com.android.ims.xdm.URIListImpl.URIListModificationListener;
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
 * The <code>URIListDocument</code> class represents the remotely stored XML
 * document that holds the user's URI lists. 
 * 
 * A list entry consists of either a single user URI or a reference to an
 * already existing URI list. Each entry can provide an optional display name.
 *
 * </p><p>For detailed implementation guidelines and for complete API docs, 
 * please refer to JSR-281 and JSR-235 documentation.
 *
 * See {@link XDMDocument} for more information and examples.
 * 
 * @author Andrei Khomushko
 * 
 */
public final class URIListDocument extends XDMDocumentImpl implements
        URIListModificationListener {
    private static final String TAG = "URIListDocument";

    /**
     * Identifier for the Application Unique ID defined by OMA for this
     * document.
     */
    public static final String AUID = "resource-lists";

    /**
     * Identifier for the recommended document path of this document defined by
     * OMA.
     */
    public static final String DEFAULT_DOCUMENT_PATH = "index";

    /** Identifier for the OMA-reserved URI list named oma_allcontacts. */
    public static final String OMA_ALL_CONTACTS = "oma_allcontacts";

    /** Identifier for the OMA-reserved URI list named oma_blockedcontacts. */
    public static final String OMA_BLOCKED_CONTACTS = "oma_blockedcontacts";

    /** Identifier for the OMA-reserved URI list named oma_buddylist. */
    public static final String OMA_BUDDYLIST = "oma_buddylist";

    /** Identifier for the OMA-reserved URI list named oma_grantedcontacts. */
    public static final String OMA_GRANTED_CONTACTS = "oma_grantedcontacts";

    public static final String DEF_MIME_TYPE = "application/resource-lists+xml";
    public static final String DEF_NAMESPACE = "urn:ietf:params:xml:ns:resource-lists";

    private final List<URIListImpl> uriLists = new ArrayList<URIListImpl>();
    private final IURIListDocument documentPeer;
    private final String xCAPRoot;
    private final boolean sendFullDoc;

    private URIListDocument(final XDMService xdmService,
            final String documentSelector, final String etag,
            final IURIListDocument documentPeer,
            final List<URIListImpl> uriListsToAdd, final String xCAPRoot,
            final boolean sendFullDoc) {
        super(xdmService, documentSelector, etag);
        assert documentPeer != null;
        this.documentPeer = documentPeer;
        this.xCAPRoot = xCAPRoot;
        this.sendFullDoc = sendFullDoc;

        if (uriListsToAdd != null) {
            uriLists.addAll(uriListsToAdd);
        }
    }

    /**
     * Creates or replaces a URI list document on the XDM server. The content of
     * the document will be the root element as defined by OMA.
     * 
     * @param service
     *            - the XDMService associated with this document
     * @param documentPath
     *            - the document name, optionally prefixed by one or more
     *            subdirectories, or null to indicate the default document path
     * 
     * @return a URIListDocument
     * 
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
    public static URIListDocument createDocument(final XDMService service,
            final String documentPath) throws ServiceClosedException,
            XCAPException, IOException {

        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (!service.isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        URIListDocument document = null;

        String content = XDMUtils.ResourceListsCreator.uriDocToXml(null, DEF_NAMESPACE);
        
        String documentSelector = XCAPRequest.createUserDocumentSelector(AUID,
            documentPath != null ? documentPath : DEFAULT_DOCUMENT_PATH);
        
        XCAPRequest xcapRequest = XCAPRequest.createPutRequest(
            documentSelector, DEF_MIME_TYPE, content, null);

        document = doCreateDocument((XDMServiceImpl) service, xcapRequest,
            documentSelector);
        
        return document;
    }

    private static URIListDocument doCreateDocument(XDMServiceImpl xdmService,
            XCAPRequest request, String documentSelector)
            throws ServiceClosedException, IOException, XCAPException {
        URIListDocument retValue = null;

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();

        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xdmService
                .getXcapRoot());

        IExceptionHolder exceptionHolder = new IExceptionHolder();
        IURIListsHolder listHolder = new IURIListsHolder();
        try {
            IURIListDocument listDocument = xdmServicePeer
                    .createURIListDocument(iRequest, listHolder,
                            exceptionHolder);
            if (exceptionHolder.getParcelableException() == null) {
                retValue = createURIListDocument(xdmService, documentSelector,
                        listDocument, listHolder);
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
     * Deletes an existing URI list document on the XDM server.
     * 
     * The ETag parameter can be used to conditionally delete the document. The
     * document will only be deleted if the ETag of the document on the server
     * matches the ETag argument. To unconditionally delete the document,' the
     * ETag argument can be set to null.
     * 
     * @param service
     *            - the XDMService associated with this document
     * @param documentPath
     *            - he document name, optionally prefixed by one or more
     *            subdirectories, or null to indicate the default document path
     * @param etag
     *            - he ETag as described above
     * 
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
            xdmServicePeer.deleteURIListDocument(iRequest, exceptionHolder);

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
     * Retrieves an existing URI list document from the XDM server.
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
     * @return a URIListDocument
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
    public static URIListDocument retrieveDocument(final XDMService service,
            final String documentPath, final String xui, final String etag)
            throws ServiceClosedException, XCAPException, IOException {
        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (!service.isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        URIListDocument retValue = null;

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

    private static URIListDocument doRetrieveDocument(
            XDMServiceImpl xdmService, XCAPRequest request,
            String documentSelector) throws ServiceClosedException,
            IOException, XCAPException {

        URIListDocument retValue = null;

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();

        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xdmService
                .getXcapRoot());

        IExceptionHolder exceptionHolder = new IExceptionHolder();
        IURIListsHolder iUriListHolder = new IURIListsHolder();
        try {
            IURIListDocument uriListDocument = xdmServicePeer
                    .retrieveURIListDocument(iRequest, iUriListHolder,
                            exceptionHolder);

            if (exceptionHolder.getParcelableException() == null) {
                retValue = createURIListDocument(xdmService, documentSelector,
                        uriListDocument, iUriListHolder);
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

    private static URIListDocument createURIListDocument(
            XDMServiceImpl xdmService, String documentSelector,
            IURIListDocument listDocument, IURIListsHolder uriListHolder)
            throws ServiceClosedException, IOException {

        URIListDocument retValue = null;

        List<URIListImpl> uriLists = new ArrayList<URIListImpl>();

        if (uriListHolder.getIUriLists() != null) {
            for (IURIList iUriList : uriListHolder.getIUriLists()) {
                URIListImpl uriList = new URIListImpl(iUriList.getListName(),
                        iUriList.getDisplayName());
                for (IListEntry iListEntry : iUriList.getListEntries()) {
                    ListEntry listEntry = new ListEntry(iListEntry.getType(),
                            iListEntry.getUri());
                    listEntry.setDisplayName(iListEntry.getDisplayName());
                    uriList.addListEntryInternally(listEntry);
                }
                uriLists.add(uriList);
            }
        }

        retValue = new URIListDocument(xdmService, documentSelector,
                uriListHolder.getEtag(), listDocument, uriLists,
                xdmService.getXcapRoot(), xdmService.isXdmSendFullDoc());

        for (URIListImpl uriList : uriLists) {
            uriList.addURIListModificationListener(retValue);
        }

        return retValue;
    }

    /**
     * Loads a URIListDocument from an InputStream.
     * 
     * The ETag parameter can be used to set an ETag on the loaded document.
     * This ETag is used for conditional operations on the document, meaning
     * that modifications on the document will only be successful if the ETag of
     * the document on the server matches the ETag argument. To only be able to
     * use unconditional operations, the ETag> argument can be set to null when
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
     * @return a URIListDocument
     * @throws IllegalArgumentException
     *             - if the service argument is null
     * @throws IllegalArgumentException
     *             - if the is argument is null
     * @throws IOException
     *             - if the is argument does not contain valid XML for this AUID
     */
    public static URIListDocument loadDocument(XDMService service,
            String documentPath, String etag, InputStream is)
            throws IOException {
        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (is == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        URIListDocument retValue = null;

        String docPath = (documentPath == null ? DEFAULT_DOCUMENT_PATH
                : documentPath);
        String documentSelector = XCAPRequest.createUserDocumentSelector(AUID,
                docPath);
        String source = XDMUtils.readToString(is);
        retValue = doLoadDocument((XDMServiceImpl)service, documentSelector, etag, source);

        return retValue;
    }

    private static URIListDocument doLoadDocument(XDMServiceImpl xdmService,
            String documentSelector, String etag, String source)
            throws ServiceClosedException, IOException {

        URIListDocument retValue = null;

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();

        IURIListsHolder iUriListHolder = new IURIListsHolder();
        IExceptionHolder exceptionHolder = new IExceptionHolder();
        try {
            IURIListDocument uriListDocument = xdmServicePeer
                    .loadURIListDocument(documentSelector, etag, source,
                            iUriListHolder, exceptionHolder);

            if (exceptionHolder.getParcelableException() == null) {
                retValue = createURIListDocument(xdmService, documentSelector,
                        uriListDocument, iUriListHolder);
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
     * Creates a new URIList and adds it to the document on the XDM server. This
     * method is synchronous and will block until the server responds.
     * 
     * If the URIList does not exist in the document it will be created. If the
     * URIList already exists it will be overwritten.
     * 
     * @param name
     *            - the name of the URI list to be created
     * @param displayName
     *            - the display name of the URI list or null
     * @param listEntries
     *            - an array of ListEntry objects to be included in the URIList
     *            or null
     * @return the created URIList
     * @throws IllegalArgumentException
     *             - if the name argument is null or an empty string
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    public URIList createURIList(String name, String displayName,
            ListEntry[] listEntries) throws ServiceClosedException,
            XCAPException, IOException {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException(
                    "the name argument is null or an empty string");
        }

        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }
        
        URIListImpl retValue = new URIListImpl(name, displayName, listEntries);
        retValue.addURIListModificationListener(this);
        uriLists.add(retValue);

        XCAPRequest xcapRequest;
        
        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        } else {
            XCAPNodeSelector nodeSelector = new XCAPNodeSelector()
                .selectElementByName("resource-lists")
                .selectElementByAttribute("list", "name", name);

            final String content = XDMUtils.ResourceListsCreator.uriListToXml(name, displayName,
                listEntries, DEF_NAMESPACE);
            
            xcapRequest = XCAPRequest.createPutRequest(
                getDocumentSelector(), nodeSelector, content, getEtag());
        }

        doSyncDataChanges(xcapRequest);

        return retValue;
    }

    /**
     * Deletes the URIList identified by the name argument from the document on
     * the XDM server. This method is synchronous and will block until the
     * server responds.
     * 
     * @param name
     *            - the URIList name
     * @throws IllegalArgumentException
     *             - if the URIList identified by the name argument does not
     *             exist in the URIListDocument
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    public void deleteURIList(final String name) throws ServiceClosedException,
            XCAPException, IOException {
        URIListImpl listToRemove = (URIListImpl) getURIList(name);

        if (listToRemove == null) {
            throw new IllegalArgumentException(
                    "The URIList identified by the name argument does not exist in the URIListDocument");
        }

        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }
        
        uriLists.remove(listToRemove);
        listToRemove.removeURIListModificationListener(this);

        XCAPRequest xcapRequest;
        
        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        } else {
            XCAPNodeSelector nodeSelector = new XCAPNodeSelector()
                .selectElementByName("resource-lists")
                .selectElementByAttribute("list", "name", name);

            xcapRequest = XCAPRequest.createDeleteRequest(
                getDocumentSelector(), nodeSelector, getEtag());
        }

        doSyncDataChanges(xcapRequest);
    }

    /**
     * Returns the URIList identified by the name argument.
     * 
     * @param name
     *            - the URIList name
     * @return the URIList or null if a URIList with the specified name does not
     *         exist
     */
    public URIList getURIList(final String name) {
        return CollectionsUtils.find(uriLists,
                new CollectionsUtils.Predicate<URIListImpl>() {
                    
                    public boolean evaluate(final URIListImpl uriList) {
                        return uriList.getListName().equals(name);
                    }
                });
    }

    /**
     * Returns all URIList objects in this URIListDocument.
     * 
     * @return an array of URIList or an empty array if no URIList are available
     */
    public URIList[] getURILists() {
        return uriLists.toArray(new URIList[0]);
    }

    
    public void saveDocument(OutputStream os) throws IOException {
        if (os == null) {
            throw new IllegalArgumentException("The os argument is null");
        }

        // String xmlContent = documentPeer.getXMLContent();
        String xmlContent = XDMUtils.ResourceListsCreator.uriDocToXml(uriLists, DEF_NAMESPACE);
        if (xmlContent != null) {
            XDMUtils.writeXmlToStream(xmlContent, os);
        } else {
            throw new IOException("Can't retrieve data.");
        }
    }

    public void displayNameChange(final URIList uriList, String oldDisplayName, final String newName)
            throws ServiceClosedException, XCAPException, IOException {

        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }
        
        XCAPRequest xcapRequest;
        
        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        } else {
            XCAPNodeSelector displayNamenodeSelector = new XCAPNodeSelector()
                .selectElementByName("resource-lists")
                .selectElementByAttribute("list", "name", oldDisplayName)
                .selectElementByName("display-name");
    
            if (newName == null) {
                xcapRequest = XCAPRequest.createDeleteRequest(
                    getDocumentSelector(), displayNamenodeSelector, getEtag());
            } else {
                String content = String.format("<display-name>%s</display-name>", newName);
                xcapRequest = XCAPRequest.createPutRequest(
                    getDocumentSelector(), displayNamenodeSelector, content, getEtag());
            }
        }

        doSyncDataChanges(xcapRequest);
    }

    public void listEntryAdd(URIList uriList, ListEntry entryToAdd)
            throws ServiceClosedException, XCAPException, IOException {

        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        XCAPRequest xcapRequest;
        
        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        } else {
            XCAPNodeSelector displayNamenodeSelector = new XCAPNodeSelector()
                .selectElementByName("resource-lists")
                .selectElementByAttribute("list", "name", uriList.getListName())
                .selectElementByAttribute("entry", "uri", entryToAdd.getUri());
    
            String content = XDMUtils.ResourceListsCreator.listEntryToXml(entryToAdd, DEF_NAMESPACE);
        
            xcapRequest = XCAPRequest.createPutRequest(
                getDocumentSelector(), displayNamenodeSelector, content, getEtag());
        }

        doSyncDataChanges(xcapRequest);
    }

    public void listEntryDelete(URIList uriList, ListEntry listEntry)
            throws ServiceClosedException, XCAPException, IOException {
        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }
        
        XCAPRequest xcapRequest;
        
        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        } else {
            XCAPNodeSelector displayNamenodeSelector = new XCAPNodeSelector()
                .selectElementByName("resource-lists")
                .selectElementByAttribute("list", "name", uriList.getListName())
                .selectElementByAttribute("entry", "uri", listEntry.getUri());

            xcapRequest = XCAPRequest.createDeleteRequest(
                getDocumentSelector(), displayNamenodeSelector, getEtag());
        }

        doSyncDataChanges(xcapRequest);
    }

    private XCAPRequest sendFullDocument() {
        XCAPRequest xcapRequest;
        String content = XDMUtils.ResourceListsCreator.uriDocToXml(uriLists, DEF_NAMESPACE);
        
        xcapRequest = XCAPRequest.createPutRequest(
            getDocumentSelector(), DEF_MIME_TYPE, content, getEtag());
        return xcapRequest;
    }

    private void doSyncDataChanges(final XCAPRequest request)
            throws XCAPException, IOException {

        final IExceptionHolder exceptionHolder = new IExceptionHolder();
        final IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xCAPRoot);

        try {
            String newEtag = documentPeer.syncDocumentChanges(iRequest,
                    exceptionHolder);
            if (exceptionHolder.getParcelableException() == null) {
                updateEtag(newEtag);
            } else {
                throw XDMUtils
                        .createXCAPException((IXCAPException) exceptionHolder
                                .getParcelableException());
            }
        } catch (RemoteException e) {
            throw new IOException("RemoteException occured.");
        }
    }

    
    public String toString() {
        return "URIListDocument [ " + super.toString() + ",uriLists="
                + uriLists + "]";
    }
}
