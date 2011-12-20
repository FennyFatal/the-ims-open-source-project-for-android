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

import org.w3c.dom.Element;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.Service;
import javax.microedition.ims.ServiceClosedException;
import java.io.IOException;

/**
 * The XDMService is the entry point for handling XDM functionality according to
 * the OMA XDM 2.0 release [OMA_XDM_SPEC]. This includes functionality to:
 * 
 * <ul>
 * <li>Manipulate XML documents by using the IETF XML Configuration Access
 * Protocol (XCAP) as defined in [RFC4825].</li>
 * <li>Search for information in XML documents stored on XDM servers.</li>
 * <li>Subscribe to changes in XML documents stored on XDM servers.</li>
 * </ul>
 * 
 * XCAP defines a technique for using HTTP GET, PUT, and DELETE methods for
 * various document manipulation operations (create, delete, retrieve, and
 * modify). The XML documents can be manipulated either directly with XCAP
 * requests or by using one of the provided high-level APIs.
 * 
 * <p>
 * <h4>Creating an XDMService</h4>
 * </p>
 * <code>Connector.open(String name)</code> An XDMService is created with
 * Connector.open(), according to the Generic Connection Framework (GCF), using
 * a name string of the format: {scheme}:{target}[{params}] where
 * 
 * {scheme} is the protocol for XDM "imsxdm" {target} is always a double slash
 * "//" followed by the application id {params} is an optional parameter that
 * can be used to set the local user identity on the format ;userId=<sip user
 * identity>. This can be used to override the default user identity provisioned
 * by the IMS network and the sip user identity must be on the format described
 * in UserIdentity.
 * 
 * <p>
 * <h4>Closing an XDMService</h4>
 * </p>
 * The application SHOULD invoke close on XDMService when it is finished using
 * it. The IMS engine may also close the XDMService due to external events. This
 * will trigger a call to serviceClosed on the XDMServiceListener interface.
 * 
 * <p>
 * <h4>XDM documents</h4>
 * </p>
 * This API defines the following high-level XDM documents, see XDMDocument for
 * more information and examples. The application can use the listDocuments
 * method in this interface to list XML documents on the XDM server.
 * 
 * <p>
 * <h4>XCAP requests</h4>
 * </p>
 * The application can also do XCAP requests without using any of the provided
 * high-level XDM documents listed above. This can be useful for example if the
 * desired functionality is missing. See the sendXCAPRequest method in this
 * interface and the XCAPRequest class for more information and examples.
 * 
 * <p>
 * <h4>Searching document</h4>
 * </p>
 * The application can search for data in XML documents on the XDM server. It is
 * possible to make a generic search based on an XQuery query or to use any of
 * the high-level search classes listed below. See the Search class for more
 * information and examples.
 * <ul>
 * <li>GroupSearch</li>
 * <li>IMDeferredMessageSearch</li>
 * <li>IMHistorySearch</li>
 * <li>UserProfileSearch</li>
 * </ul>
 * 
 * <p>
 * <h4>Subscribing to document changes</h4>
 * </p>
 * The application can subscribe to changes in a set of documents on the XDM
 * server. When the subscription is active the application will be notified of
 * document changes through the documentUpdateReceived event in the
 * DocumentSubscriberListener.
 * 
 * @author Andrei Khomushko
 * 
 */
public interface XDMService extends Service {

    /**
     * Creates a DocumentSubscriber that subscribes to changes in a set of
     * documents on the XDM server.
     * 
     * The urls parameter indicates which document or documents to subscribe to.
     * Each string in the urls array can be one of the following:
     * <ul>
     * <li>A document selector, in which case the subscription is for changes on
     * that document. See XCAPRequest for details on document selectors. Note
     * that in this API, document selectors always start with an AUID and not a
     * forward slash. If the document selectors are created in any other way
     * than by using the methods in XCAPRequest, make sure this convention is
     * followed.</li>
     * <li>An URL that indicates a collection, that is, a set of documents.
     * Collection URLs end with a slash. A collection includes all documents in
     * the collection and all of its sub-collections. An example of a collection
     * URL is "org.openmobilealliance.groups/". See [XCAP-DIFF-EVENT] for
     * details.</li>
     * </ul>
     * 
     * A subscription proxy must be available to subscribe to documents from
     * more than one AUID or more than one XUI in a single DocumentSubscriber.
     * It is however possible to have more than one DocumentSubscriber active at
     * the same time.
     * 
     * @param urls
     *            - an array of URLs, containing either document selectors or
     *            collection URLs, see above
     * @return a new DocumentSubscriber
     * @throws IllegalArgumentException
     *             - if the urls argument is null or an empty array
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws ImsException
     *             - if the DocumentSubscriber could not be created
     * @throws ImsException
     *             - if the urls argument contains documents or collections from
     *             more than one AUID and no subscription proxy is available
     */
    DocumentSubscriber createDocumentSubscriber(String[] urls)
            throws ServiceClosedException, ImsException;

    /**
     * Returns the document entries that exist on the XDM server for a certain
     * AUID or for all AUIDs. This method is synchronous and will block until
     * the XDM server responds.
     * 
     * @param auid
     *            - the AUID for which the document entries should be returned,
     *            a null value indicates that the document entries for all AUIDs
     *            should be returned
     * @return an array of document entries, or an empty array if no entries
     *         could be retrieved
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IOException
     *             - if an I/O error occurs
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     */
    DocumentEntry[] listDocuments(String auid) throws ServiceClosedException,
            IOException, XCAPException;

    /**
     * Performs a search for data in XML documents.
     * 
     * What to search for is described by the Search instance passed as a
     * parameter. This method is synchronous and will block until the XDM server
     * responds.
     * 
     * The XDM server will return the results of the search as an XDM search
     * document, as described in [OMA_XDM_SPEC]. The method will parse the
     * result document and return a reference to the <response> XML element.
     * This element contains the search results. The format of the search
     * results depends on the type of search.
     * 
     * Note: It is possible to create a Search instance that does not describe a
     * valid search. This will result in either an IllegalArgumentException or a
     * XDM server error.
     * 
     * @param search
     *            - the search to perform
     * @return the results of the search, as a reference to the <response>
     *         element in the search result document
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IllegalArgumentException
     *             - if the search argument is null
     * @throws IllegalArgumentException
     *             - if the search argument describes a search that is invalid
     *             so that the search ID could not be obtained from the search
     *             document DOM, see [OMA_XDM_SPEC]
     * @throws IOException
     *             - if an I/O error occurs
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     */
    Element performSearch(Search search) throws ServiceClosedException,
            IOException, XCAPException;

    /**
     * Sends an XCAP request.
     * 
     * The request is described by the XCAPRequest instance passed as an
     * argument. This method is synchronous and will block until the XDM server
     * responds.
     * 
     * @param request
     *            - the request
     * @return the response, as received from the XDM server
     * @throws IllegalArgumentException
     *             - if the request argument is null
     * @throws ServiceClosedException
     *             - if the Service is closed
     * @throws IOException
     *             - if an I/O error occurs
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     */
    XCAPResponse sendXCAPRequest(XCAPRequest request)
            throws ServiceClosedException, IOException, XCAPException;

    /**
     * Sets a listener for this XDMService, replacing any previous
     * XDMServiceListener. A null value removes any existing listener.
     * 
     * @param listener
     *            - the listener to set, or null
     */
    void setListener(XDMServiceListener listener);
    
    String getDefXui();
    
    String getXcapRoot();
}
