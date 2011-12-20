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
import android.util.Log;
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
 * The PresenceAuthorizationDocument class represents the remotely stored XML
 * document that holds the user's presence authorization rules. Presence
 * authorization rules are defined by [OMA_PRES_XDM].
 * <p/>
 * The document is retrieved with a call to the static retrieveDocument method.
 * The document can also be loaded from a cached copy by using the static
 * loadDocument method.
 * <p/>
 * Presence authorization rules enables the application to set authorization
 * rules for a predefined group of user URIs or for users that are defined in
 * standard lists such as oma_grantedcontacts and oma_blockedcontacts.
 * <p/>
 * A rule can be added to a PresenceAuthorizationDocument by creating a new
 * PresenceAuthorizationRule and then calling the addRule method in this class.
 *
 * @author Andrei Khomushko
 * @see XDMDocument, PresenceAuthorizationRule
 */
public final class PresenceAuthorizationDocument extends XDMDocumentImpl {
    private static final String TAG = "JSR - PresenceAuthorizationDocument";

    /**
     * Identifier for the Application Unique ID defined by OMA for this
     * document.
     */
    public static final String AUID = "org.openmobilealliance.pres-rules";

    /**
     * Identifier for the recommended document path of this document defined by
     * OMA.
     */
    public static final String DEFAULT_DOCUMENT_PATH = "pres-rules";

    public static final String DEF_MIME_TYPE = "application/auth-policy+xml";

    public static final String DEF_NAMESPACE_CR = "urn:ietf:params:xml:ns:common-policy";
    public static final String DEF_NAMESPACE_OP = "urn:oma:xml:prs:pres-rules";
    public static final String DEF_NAMESPACE_PR = "urn:ietf:params:xml:ns:pres-rules";
    public static final String DEF_NAMESPACE_OCP = "urn:oma:xml:xdm:common-policy";

    private final List<PresenceAuthorizationRule> presenceAuthorizationRules = new ArrayList<PresenceAuthorizationRule>();
    private final IPresenceAuthorizationDocument documentPeer;
    private final String xCAPRoot;
    private final boolean sendFullDoc;


    private PresenceAuthorizationDocument(
            final XDMService xdmService,
            final String documentSelector,
            final String eTag,
            final IPresenceAuthorizationDocument documentPeer,
            final List<PresenceAuthorizationRule> presenceAuthorizationRulesToAdd,
            final String xCAPRoot,
            final boolean sendFullDoc) {

        super(xdmService, documentSelector, eTag);

        assert documentPeer != null;
        this.documentPeer = documentPeer;

        this.xCAPRoot = xCAPRoot;
        this.sendFullDoc = sendFullDoc;

        if (presenceAuthorizationRulesToAdd != null) {
            presenceAuthorizationRules.addAll(presenceAuthorizationRulesToAdd);
        }

    }

    /**
     * Creates or replaces a presence authorization document on the XDM server.
     * The content of the document will be the root element as defined by OMA.
     * <p/>
     * If the document does not exist it will be created. If the document
     * already exists it will be overwritten.
     *
     * @param service      - the XDMService associated with this document
     * @param documentPath - the document name, optionally prefixed by one or more
     *                     subdirectories, or null to indicate the default document path
     * @return a PresenceAuthorizationDocument
     * @throws IllegalArgumentException - if the service argument is null
     * @throws ServiceClosedException   - if the service is closed
     * @throws XCAPException            - if the HTTP response from the XDM server has a status code
     *                                  other than 2xx Success
     * @throws IOException              - if an I/O error occurs
     */
    public static PresenceAuthorizationDocument createDocument(final XDMService service, final String documentPath)
            throws ServiceClosedException, XCAPException, IOException {
        Log.i(TAG, "createDocument#started");

        if (service == null) {
            throw new IllegalArgumentException("the service argument is null");
        }

        if (!service.isOpen()) {
            throw new ServiceClosedException("Service alredy closed");
        }

        PresenceAuthorizationDocument retValue = null;

        String content = XDMUtils.RuleSetCreator.presenceAuthorizationDocToXml(null,
                DEF_NAMESPACE_CR, DEF_NAMESPACE_OP, DEF_NAMESPACE_PR, DEF_NAMESPACE_OCP);

        String documentSelector = XCAPRequest.createUserDocumentSelector(AUID,
                documentPath != null ? documentPath : DEFAULT_DOCUMENT_PATH);

        XCAPRequest xcapRequest = XCAPRequest.createPutRequest(
                documentSelector, DEF_MIME_TYPE, content, null);

        retValue = doCreateDocument((XDMServiceImpl) service, xcapRequest, documentSelector);

        Log.i(TAG, "createDocument#finished");
        return retValue;
    }

    private static PresenceAuthorizationDocument doCreateDocument(
            XDMServiceImpl xdmService,
            XCAPRequest request,
            String documentSelector
    ) throws ServiceClosedException, IOException, XCAPException {

        PresenceAuthorizationDocument retValue = null;

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();

        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xdmService.getXcapRoot());

        IExceptionHolder exceptionHolder = new IExceptionHolder();
        IPresenceAuthorizationRulesHolder listHolder = new IPresenceAuthorizationRulesHolder();
        try {
            IPresenceAuthorizationDocument presenceAuthorizationDocument = xdmServicePeer
                    .createPresenceAuthorizationDocument(iRequest, listHolder, exceptionHolder);

            if (exceptionHolder.getParcelableException() == null) {
                // TODO retrieve etag
                retValue = new PresenceAuthorizationDocument(xdmService,
                        documentSelector, listHolder.getEtag(), presenceAuthorizationDocument,
                        null, xdmService.getXcapRoot(), xdmService.isXdmSendFullDoc());
            }
            else {
                throw XDMUtils.createXCAPException(
                        (IXCAPException) exceptionHolder.getParcelableException()
                );
            }

        }
        catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        return retValue;
    }

    /**
     * Deletes an existing presence authorization document on the XDM server.
     * <p/>
     * The ETag parameter can be used to conditionally delete the document. The
     * document will only be deleted if the ETag of the document on the server
     * matches the ETag argument. To unconditionally delete the document,' the
     * ETag argument can be set to null.
     *
     * @param service      - the XDMService associated with this document
     * @param documentPath - he document name, optionally prefixed by one or more
     *                     subdirectories, or null to indicate the default document path
     * @param etag         - he ETag as described above
     * @throws IllegalArgumentException - if the service argument is null
     * @throws ServiceClosedException   - if the service is closed
     * @throws XCAPException            - if the HTTP response from the XDM server has a status code
     *                                  other than 2xx Success
     * @throws IOException              - if an I/O error occurs
     */
    public static void deleteDocument(XDMService service, String documentPath, String etag)
            throws ServiceClosedException, XCAPException, IOException {
        Log.i(TAG, "deleteDocument#started");

        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (!service.isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        String documentSelector = XCAPRequest.createUserDocumentSelector(AUID, documentPath);

        XCAPRequest xcapRequest = XCAPRequest.createDeleteRequest(documentSelector, null, etag);
        if (etag != null) {
            xcapRequest.addIfMathHeader(etag);
        }

        doDeleteDocument((XDMServiceImpl) service, xcapRequest, documentSelector);
        Log.i(TAG, "deleteDocument#finished");
    }

    private static void doDeleteDocument(XDMServiceImpl xdmService,
                                         XCAPRequest request, String documentSelector)
            throws ServiceClosedException, IOException, XCAPException {

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();
        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xdmService.getXcapRoot());
        IExceptionHolder exceptionHolder = new IExceptionHolder();

        try {
            xdmServicePeer.deletePresenceAuthorizationDocument(iRequest, exceptionHolder);

            if (exceptionHolder.getParcelableException() != null) {
                throw XDMUtils.createXCAPException(
                        (IXCAPException) exceptionHolder.getParcelableException()
                );
            }
        }
        catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Retrieves an existing presence authorization document from the XDM
     * server.
     * <p/>
     * The ETag parameter can be used to conditionally fetch the document. The
     * document will only be fetched if the ETag of the document on the server
     * is different from the ETag argument. To unconditionally fetch the
     * document, the ETag argument can be set to null.
     *
     * @param service      - the XDMService associated with this document
     * @param documentPath - the document name, optionally prefixed by one or more
     *                     subdirectories, or null to indicate the default document path
     * @param xui          - the XUI of the user, or null to indicate the current user,
     *                     see [RFC4825]
     * @param etag         - the ETag as described above
     * @return a PresenceAuthorizationDocument
     * @throws IllegalArgumentException - if the service argument is null
     * @throws ServiceClosedException   - if the service is closed
     * @throws XCAPException            - if the HTTP response from the XDM server has a status code
     *                                  other than 2xx Success
     * @throws IOException              - if an I/O error occurs
     */
    public static PresenceAuthorizationDocument retrieveDocument(
            XDMService service, String documentPath, String xui, String etag)
            throws ServiceClosedException, XCAPException, IOException {
        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (!service.isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        PresenceAuthorizationDocument retValue = null;

        final String documentSelector;
        String docPath = (documentPath == null ? DEFAULT_DOCUMENT_PATH : documentPath);
        if (xui == null) {
            documentSelector = XCAPRequest.createUserDocumentSelector(AUID, docPath);
        }
        else {
            documentSelector = XCAPRequest.createUserDocumentSelector(AUID, xui, docPath);
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

    private static PresenceAuthorizationDocument doRetrieveDocument(
            XDMServiceImpl xdmService, XCAPRequest request,
            String documentSelector) throws ServiceClosedException,
            IOException, XCAPException {

        PresenceAuthorizationDocument retValue = null;

        IXDMService xdmServicePeer = xdmService.getXdmServicePeer();

        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xdmService.getXcapRoot());

        IExceptionHolder exceptionHolder = new IExceptionHolder();
        IPresenceAuthorizationRulesHolder listHolder = new IPresenceAuthorizationRulesHolder();
        try {
            IPresenceAuthorizationDocument presenceAuthorizationDocument = xdmServicePeer
                    .retrievePresenceAuthorizationDocument(iRequest, listHolder, exceptionHolder);

            if (exceptionHolder.getParcelableException() == null) {
                retValue = createPresenceAuthorizationDocument(xdmService,
                        documentSelector, presenceAuthorizationDocument, listHolder);
            }
            else {
                throw XDMUtils.createXCAPException(
                        (IXCAPException) exceptionHolder.getParcelableException());
            }

        }
        catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        return retValue;
    }

    private static PresenceAuthorizationDocument createPresenceAuthorizationDocument(
            XDMServiceImpl xdmService,
            String documentSelector,
            IPresenceAuthorizationDocument presenceAuthorizationDocument,
            IPresenceAuthorizationRulesHolder listHolder)
            throws ServiceClosedException, IOException {

        List<PresenceAuthorizationRule> authorizationRules = new ArrayList<PresenceAuthorizationRule>();

        if (listHolder.getiPresenceAuthorizationRules() != null) {
            for (IPresenceAuthorizationRule iPresenceAuthorizationRule : listHolder.getiPresenceAuthorizationRules()) {
                PresenceAuthorizationRule presenceAuthorizationRule = new PresenceAuthorizationRule();
                presenceAuthorizationRule.setRuleId(iPresenceAuthorizationRule.getRuleId());
                presenceAuthorizationRule.setAction(
                        getRuleAction(iPresenceAuthorizationRule.getAction())
                );
                
                final Identity identity = createIdentity(iPresenceAuthorizationRule.getIdentity());
                presenceAuthorizationRule.setConditionIdentity(identity);
                
                presenceAuthorizationRule.setConditionURIList(iPresenceAuthorizationRule.getUriListReference());
                //presenceAuthorizationRule.setPresenceContentFilter(presenceContentFilter)
                //TODO fill presenceAuthorizationRule

                authorizationRules.add(presenceAuthorizationRule);
            }
        }

        return new PresenceAuthorizationDocument(
                xdmService,
                documentSelector,
                listHolder.getEtag(),
                presenceAuthorizationDocument,
                authorizationRules,
                xdmService.getXcapRoot(),
                xdmService.isXdmSendFullDoc()
        );
    }

    private static Identity createIdentity(IIdentity iIdentity) {
        return iIdentity != null?  
            new Identity(iIdentity.getIdentityType(), 
                iIdentity.getIdentityList(), 
                iIdentity.getAllowedDomain()): null;
    }

    private static int getRuleAction(String action) {
        action = action.toLowerCase();
        if (action.equalsIgnoreCase("allow")) {
            return PresenceAuthorizationRule.ACTION_ALLOW;
        }
        else if (action.equalsIgnoreCase("block")) {
            return PresenceAuthorizationRule.ACTION_BLOCK;
        }
        else if (action.equalsIgnoreCase("confirm")) {
            return PresenceAuthorizationRule.ACTION_CONFIRM;
        }
        else if (action.equalsIgnoreCase("polite-block")) {
            return PresenceAuthorizationRule.ACTION_POLITE_BLOCK;
        }
        return -1;
    }

    /**
     * Loads a presence authorization document from an InputStream.
     * <p/>
     * The ETag parameter can be used to set an ETag on the loaded document.
     * This ETag is used for conditional operations on the document, meaning
     * that modifications on the document will only be successful if the ETag of
     * the document on the server matches the ETag argument. To only be able to
     * use unconditional operations, the ETag> argument can be set to null when
     * loading the document.
     *
     * @param service      - the XDMService associated with this document
     * @param documentPath - the document name, optionally prefixed by one or more
     *                     subdirectories, or null to indicate the default document path
     * @param etag         - the ETag as described above
     * @param is           - the input stream containing the content of the document
     * @return a PresenceAuthorizationDocument
     * @throws IllegalArgumentException - if the service argument is null
     * @throws IllegalArgumentException - if the is argument is null
     * @throws IOException              - if the is argument does not contain valid XML for this AUID
     */
    public static PresenceAuthorizationDocument loadDocument(
            XDMService service, String documentPath, String etag, InputStream is)
            throws IOException {
        if (service == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        if (is == null) {
            throw new IllegalArgumentException("The service argument is null");
        }

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Adds a rule to this PresenceAuthorizationDocument and submits the changes
     * to the document on the XDM server. This method is synchronous and will
     * block until the server responds.
     * <p/>
     * If the PresenceAuthorizationRule does not exist in the document it will
     * be created. If the PresenceAuthorizationRule already exists it will be
     * overwritten.
     *
     * @param rule - the rule to add
     * @throws IllegalArgumentException - if the rule argument is null
     * @throws ServiceClosedException   - if the service is closed
     * @throws XCAPException            - if the HTTP response from the XDM server has a status code
     *                                  other than 2xx Success
     * @throws IOException              - if an I/O error occurs
     */
    public void addRule(final PresenceAuthorizationRule rule)
            throws ServiceClosedException, XCAPException, IOException {

        if (rule == null) {
            throw new IllegalArgumentException("the rule argument is null");
        }

        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        CollectionsUtils.replaceOrAdd(rule, presenceAuthorizationRules, new CollectionsUtils.Predicate<PresenceAuthorizationRule>() {
            public boolean evaluate(PresenceAuthorizationRule authorizationRule) {
                return rule.getRuleId().equals(authorizationRule.getRuleId());
            }
        });

        XCAPRequest xcapRequest;

        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        }
        else {
            XCAPNodeSelector nodeSelector = new XCAPNodeSelector()
                    .selectElementByName("ruleset")
                    .selectElementByAttribute("rule", "id", rule.getRuleId());

            String content = XDMUtils.RuleSetCreator.presenceAuthorizationRuleToXml(rule,
                    DEF_NAMESPACE_CR, DEF_NAMESPACE_OP, DEF_NAMESPACE_PR, DEF_NAMESPACE_OCP);

            xcapRequest = XCAPRequest.createPutRequest(
                    getDocumentSelector(), nodeSelector, content, getEtag());
        }

        doSyncDocumentChanges(xcapRequest);
    }

    /**
     * Deletes the rule identified by the id argument from the document on the
     * XDM server. This method is synchronous and will block until the server
     * responds.
     *
     * @param id - the identifier of the PresenceAuthorizationRule
     * @throws IllegalArgumentException - if a PresenceAuthorizationRule identified by the id
     *                                  argument does not exist in this PresenceAuthorizationDocument
     * @throws ServiceClosedException   - if the service is closed
     * @throws XCAPException            - if the HTTP response from the XDM server has a status code
     *                                  other than 2xx Success
     * @throws IOException              - if an I/O error occurs
     */
    public void deleteRule(String id) throws ServiceClosedException,
            XCAPException, IOException {

        PresenceAuthorizationRule ruleToRemove = getRule(id);

        if (ruleToRemove == null) {
            throw new IllegalArgumentException(
                    "PresenceAuthorizationRule identified by the id argument does not exist in this PresenceAuthorizationDocument");
        }

        if (!getXDMService().isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        presenceAuthorizationRules.remove(ruleToRemove);

        XCAPRequest xcapRequest;

        if (sendFullDoc) {
            xcapRequest = sendFullDocument();

        }
        else {
            XCAPNodeSelector nodeSelector = new XCAPNodeSelector()
                    .selectElementByName("cr:ruleset")
                    .selectElementByAttribute("cr:rule", "id", id);

            xcapRequest = XCAPRequest.createDeleteRequest(
                    getDocumentSelector(), nodeSelector, getEtag());
        }

        doSyncDocumentChanges(xcapRequest);
    }

    private XCAPRequest sendFullDocument() {
        XCAPRequest xcapRequest;
        String content = XDMUtils.RuleSetCreator.presenceAuthorizationDocToXml(presenceAuthorizationRules,
                DEF_NAMESPACE_CR, DEF_NAMESPACE_OP, DEF_NAMESPACE_PR, DEF_NAMESPACE_OCP);

        xcapRequest = XCAPRequest.createPutRequest(
                getDocumentSelector(), DEF_MIME_TYPE, content, getEtag());
        return xcapRequest;
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
        }
        catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Returns the rule identified by the id argument.
     * <p/>
     * Note: Changes made to a PresenceAuthorizationRule will not take affect on
     * the PresenceAuthorizationDocument until addRule is called.
     *
     * @param id - the identifier of the PresenceAuthorizationRule
     * @return the PresenceAuthorizationRule or null if the rule does not exist
     */
    public PresenceAuthorizationRule getRule(final String id) {
        return CollectionsUtils.find(
                presenceAuthorizationRules,
                new CollectionsUtils.Predicate<PresenceAuthorizationRule>() {
                    public boolean evaluate(final PresenceAuthorizationRule presenceAuthorizationRule) {
                        return presenceAuthorizationRule.getRuleId().equals(id);
                    }
                }
        );
    }

    /**
     * Returns all rules in this PresenceAuthorizationDocument.
     * <p/>
     * Note: Changes made to a PresenceAuthorizationRule will not take effect on
     * the PresenceAuthorizationDocument until addRule is called.
     *
     * @return an array of PresenceAuthorizationRule objects or an empty array
     *         if no rules are available
     */
    public PresenceAuthorizationRule[] getRules() {
        return presenceAuthorizationRules.toArray(new PresenceAuthorizationRule[presenceAuthorizationRules.size()]);
    }

    
    public void saveDocument(OutputStream os) throws IOException {
        if (os == null) {
            throw new IllegalArgumentException("The os argument is null");
        }

        //String xmlContent = null/* documentPeer.getXMLContent() */;
        String xmlContent = XDMUtils.RuleSetCreator.presenceAuthorizationDocToXml(presenceAuthorizationRules,
                DEF_NAMESPACE_CR, DEF_NAMESPACE_OP, DEF_NAMESPACE_PR, DEF_NAMESPACE_OCP);
        if (xmlContent != null) {
            XDMUtils.writeXmlToStream(xmlContent, os);
        }
        else {
            throw new IOException("Can't retrieve data.");
        }
    }

    public static void main(String[] args) {
        
    }
}
