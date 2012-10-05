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

import android.util.Log;
import com.android.ims.xdm.XCAPURLEncoder;
import com.android.ims.xdm.XDMServiceImpl;

import javax.microedition.ims.xdm.XCAPNodeSelector.SelectorType;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an XCAP request.
 * 
 *
 * A list entry consists of either a single user URI or a reference to an
 * already existing URI list. Each entry can provide an optional display name.
 *
 * </p><p>For detailed implementation guidelines and for complete API docs, 
 * please refer to JSR-281 and JSR-235 documentation.
 * 
 * @author Andrei Khomushko
 * 
 */
public final class XCAPRequest {
    private static final String TAG = "XCAPRequest";

    public static final int HTTP_METHOD_DELETE = 2;
    public static final int HTTP_METHOD_GET = 1;
    public static final int HTTP_METHOD_PUT = 0;

    private static final String DOCUMENT_PATH_GLOBAL = "global";
    private static final String DOCUMENT_PATH_USERS = "users";

    private static final String CONTENT_TYPE_XML_ELEMENT = "application/xcap-el+xml";
    private static final String CONTENT_TYPE_XML_ATTRIBUTE = "application/xcap-att+xml";

    private static Map<RequestType, Integer> requestTypeMethodMapping = new HashMap<RequestType, Integer>();

    private String documentSelector;
    private XCAPNodeSelector nodeSelector;
    private Map<String, String> namespaces;

    private Map<String, String> headers = new HashMap<String, String>();
    private String content;
    private RequestType requestType;

    private static String currentUser;

    private enum Header {
        ETag("ETag"), Content_Type("Content-Type"), If_Match("If-Match"), If_None_Match(
                "If-None-Match");

        private Header(String content) {
            this.content = content;
        }

        private String content;

        private String getContent() {
            return content;
        }
    }

    private enum RequestType {
        GET, PUT_DOC, PUT_ELEM, DELETE, FETCH;
    }

    static {
        requestTypeMethodMapping.put(RequestType.GET, 1);
        requestTypeMethodMapping.put(RequestType.FETCH, 1);
        requestTypeMethodMapping.put(RequestType.PUT_DOC, 0);
        requestTypeMethodMapping.put(RequestType.PUT_ELEM, 0);
        requestTypeMethodMapping.put(RequestType.DELETE, 2);
    }

    private XCAPRequest(RequestType requestType, final String documentSelector,
            XCAPNodeSelector nodeSelector, String etag) {
        this.requestType = requestType;
        this.documentSelector = documentSelector;
        this.nodeSelector = nodeSelector;
        addHeader(Header.ETag, etag);
    }

    public void addIfMathHeader(String etag) {
        addHeader(Header.If_Match, etag);
    }

    public void addIfNoneMathHeader(String etag) {
        addHeader(Header.If_None_Match, etag);
    }

    private void addHeader(Header header, String value) {
        if (header != null && value != null) {
            headers.put(header.getContent(), value);
        }
    }

    private void addHeader(Header header) {
        if (header != null) {
            headers.put(header.getContent(), null);
        }
    }

    private String getHeader(Header header) {
        return headers.get(header.getContent());
    }

    /**
     * Creates an XCAP PUT request to create or replace a document.
     * 
     * If the document does not exist the request will create it. If it exists,
     * the request will replace it.
     * 
     * The ETag parameter can be used to conditionally replace the document. If
     * the ETag argument is non- null, the request will fail unless the document
     * exists on the server and has the same ETag.
     * 
     * @param documentSelector
     *            - the document selector
     * @param mimeType
     *            - the content MIME type, to set the content type header of the
     *            document
     * @param content
     *            - the document content as a string
     * @param etag
     *            - the ETag as described above
     * @return an XCAP request
     * @throws IllegalArgumentException
     *             - if the documentSelector argument, the mimeType argument, or
     *             the content argument is null
     */
    public static XCAPRequest createPutRequest(String documentSelector,
            String mimeType, String content, String etag) {
        if (documentSelector == null) {
            throw new IllegalArgumentException(
                    "the documentSelector argument is null");
        }

        if (mimeType == null) {
            throw new IllegalArgumentException("the mimeType argument is null");
        }

        if (content == null) {
            throw new IllegalArgumentException("the content argument is null");
        }

        XCAPRequest xcapRequest = new XCAPRequest(RequestType.PUT_DOC,
                documentSelector, null, etag);
        xcapRequest.addHeader(Header.Content_Type, mimeType);
        xcapRequest.content = content;

        return xcapRequest;
    }

    /**
     * Creates an XCAP PUT request to create or replace an XML element or XML
     * attribute.
     * 
     * If the element or attribute does not exist the request will create it. If
     * it exists, the request will replace it.
     * 
     * The ETag parameter can be used to create a conditional request. If the
     * ETag argument is non-null, the request will fail unless the document on
     * the server has the same ETag. To unconditionally modify the document, the
     * ETag argument can be set to null. It is recommended to always use an
     * ETag.
     * 
     * @param documentSelector
     *            - the document selector
     * @param nodeSelector
     *            - the node selector
     * @param content
     *            - the element or attribute content
     * @param etag
     *            - the ETag as described above
     * @return an XCAP request
     * @throws llegalArgumentException
     *             - if the documentSelector argument, the nodeSelector
     *             argument, or the content argument is null
     */
    public static XCAPRequest createPutRequest(String documentSelector,
            XCAPNodeSelector nodeSelector, String content, String etag) {
        if (documentSelector == null) {
            throw new IllegalArgumentException(
                    "the documentSelector argument is null");
        }

        if (nodeSelector == null) {
            throw new IllegalArgumentException(
                    "the nodeSelector argument is null");
        }

        if (content == null) {
            throw new IllegalArgumentException("the content argument is null");
        }

        XCAPRequest xcapRequest = new XCAPRequest(RequestType.PUT_ELEM,
                documentSelector, nodeSelector, etag);
        xcapRequest.content = content;

        return xcapRequest;
    }

    /**
     * Creates an XCAP GET request to fetch a document, an XML element, or an
     * XML attribute.
     * 
     * To fetch an entire document, set the nodeSelector attribute to null. To
     * fetch an element or attribute, set the nodeSelector to point to the
     * element or attribute.
     * 
     * The ETag parameter can be used to create a conditional request. If the
     * ETag argument is non-null, the request will only be carried out if the
     * ETag of the document on the server is different from the ETag argument.
     * To unconditionally fetch the document, element, or attribute, the ETag
     * argument can be set to null.
     * 
     * @param documentSelector
     *            - the document selector
     * @param nodeSelector
     *            - the node selector indicating the element or attribute to
     *            fetch, or null to fetch the entire document
     * @param etag
     *            - the ETag as described above
     * @return an XCAP request
     * @throws IllegalArgumentException
     *             - if the documentSelector argument is null
     */
    public static XCAPRequest createGetRequest(String documentSelector,
            XCAPNodeSelector nodeSelector, String etag) {

        if (documentSelector == null) {
            throw new IllegalArgumentException(
                    "the documentSelector argument is null");
        }

        return new XCAPRequest(RequestType.GET, documentSelector, nodeSelector,
                etag);
    }

    /**
     * Creates an XCAP DELETE request to delete a document, an XML element, or
     * an XML attribute.
     * 
     * To delete an entire document, set the nodeSelector attribute to null. To
     * delete an element or attribute, set the nodeSelector to point to the
     * element or attribute.
     * 
     * The ETag parameter can be used to create a conditional request. If the
     * ETag argument is non-null, the request will only be carried out if the
     * ETag of the document on the server is the same as the ETag argument. To
     * unconditionally delete the document, element, or attribute, the ETag
     * argument can be set to null. It is recommended to always use an ETag.
     * 
     * @param documentSelector
     *            - the document selector
     * @param nodeSelector
     *            - the node selector indicating the node to delete, or null to
     *            delete the entire document
     * @param etag
     *            - the ETag as described above
     * @return an XCAP request
     * @throws IllegalArgumentException
     *             - if the documentSelector argument is null
     */
    public static XCAPRequest createDeleteRequest(String documentSelector,
            XCAPNodeSelector nodeSelector, String etag) {

        if (documentSelector == null) {
            throw new IllegalArgumentException(
                    "the documentSelector argument is null");
        }

        return new XCAPRequest(RequestType.DELETE, documentSelector,
                nodeSelector, etag);
    }

    /**
     * Creates an XCAP request to fetch the namespace bindings of an element in
     * a document.
     * 
     * The ETag parameter can be used to conditionally fetch the namespace
     * bindings. The bindings will only be fetched if the ETag of the document
     * on the server is different from the ETag argument. To unconditionally
     * fetch the namespace bindings, the ETag argument can be set to null.
     * 
     * @param documentSelector
     *            - the document selector
     * @param nodeSelector
     *            - the node selector indicating the element for which to fetch
     *            the namespace bindings
     * @param etag
     *            - the ETag as described above
     * @return an XCAP request
     * @throws IllegalArgumentException
     *             - if the documentSelector argument or the nodeSelector
     *             argument is null
     */
    public static XCAPRequest createFetchNamespaceBindingsRequest(
            String documentSelector, XCAPNodeSelector nodeSelector, String etag) {

        if (documentSelector == null) {
            throw new IllegalArgumentException(
                    "the documentSelector argument is null");
        }

        if (nodeSelector == null) {
            throw new IllegalArgumentException(
                    "the nodeSelector argument is null");
        }

        // TODO review
        return new XCAPRequest(RequestType.FETCH, documentSelector,
                nodeSelector, etag);
    }

    /**
     * Returns the HTTP method of the request. Valid values are HTTP_METHOD_GET,
     * HTTP_METHOD_PUT, and HTTP_METHOD_DELETE.
     * 
     * @return the HTTP method
     */
    public int getHttpMethod() {
        return requestTypeMethodMapping.get(requestType);
    }

    /**
     * Returns the namespace bindings for the request's node selector.
     * 
     * The return value of the method is a Hashtable where each key is a
     * namespace prefix as a string, and each value is a namespace URI as a
     * string. If there are no namespace bindings, an empty table is returned.
     * 
     * @return the namespace bindings as described above
     */
    public Map<String, String> getNamespaceBindings() {
        return namespaces;
    }

    /**
     * Sets the namespace bindings for the request's node selector.
     * 
     * The namespaceBindings argument must be a Hashtable where each key is a
     * namespace prefix as a string, and each value is a namespace URI as a
     * string. An empty table indicates no namespace bindings.
     * 
     * All previously set namespace bindings will be replaced.
     * 
     * @param namespaceBindings
     *            - the namespace bindings as described above
     * @throws IllegalArgumentException
     *             - if the namespaceBindings argument is null or does not
     *             follow the structure described above
     */
    public void setNamespaceBindings(Map<String, String> namespaceBindings) {
        if (namespaceBindings == null) {
            throw new IllegalArgumentException(
                    "the namespaceBindings argument is null");
        }

        this.namespaces = namespaceBindings;
    }

    /**
     * Given an XCAP root, creates the URI for the XCAP request. The URI will be
     * created according to the rules in [RFC4825].
     * 
     * As described in [RFC4825] the XCAP root is a URI, most often with the
     * HTTP scheme. "http://xcap.example.org/" is an example of a valid XCAP
     * root.
     * 
     * A slash will be appended to the XCAP root if it does not already end with
     * one. This is to make sure that the XCAP root and document selector
     * produce a valid URI when concatenated (in this API the document selector
     * always starts with an AUID and not a slash). For example, the XCAP root
     * "http://xcap.example.org" will result in the same URI as the XCAP root
     * "http://xcap.example.org/".
     * 
     * @param xcapRoot
     *            - the XCAP root URI
     * @return the URI of the XCAP request
     * @throws IllegalArgumentException
     *             - if the xcapRoot argument is null
     */
    public String createRequestURI(final String xcapRoot) {
        if (xcapRoot == null) {
            throw new IllegalArgumentException("the xcapRoot argument is null");
        }

        // TODO check URLEncoder
        StringBuilder requestURI = new StringBuilder();

        requestURI.append(xcapRoot);
        if (!xcapRoot.endsWith("/")) {
            requestURI.append("/");
        }

        requestURI.append(documentSelector);

        if (nodeSelector != null) {
            requestURI.append("/~~");

            if (!documentSelector.startsWith("/")) {
                requestURI.append("/");
            }

            requestURI.append(nodeSelector.toString());

            if (namespaces != null && namespaces.size() > 0) {
                requestURI.append("?");
                for (String key : namespaces.keySet()) {
                    requestURI.append(
                            String.format("xmlns(%s=%s)", key, namespaces
                                    .get(key))).append("/n/r");
                }
            }
        }

        return requestURI.toString();
    }

    /**
     * Returns the value of the If-Match HTTP header of the request.
     * 
     * @return the value of the If-Match header, or null if the header is not
     *         set
     */
    public String getIfMatchHeader() {
        return getHeader(Header.If_Match);
    }

    /**
     * Returns the value of the If-None-Match HTTP header of the request.
     * 
     * @return the value of the If-None-Match header, or null if the header is
     *         not set
     */
    public String getIfNoneMatchHeader() {
        return getHeader(Header.If_None_Match);
    }

    /**
     * Returns the value of the Content-Type HTTP header of the request.
     * 
     * @return the value of the Content-Type header, or null if the header is
     *         not set
     */
    public String getContentTypeHeader() {
        return getHeader(Header.Content_Type);
    }

    /**
     * Returns the body of the HTTP request as a string.
     * 
     * @return the HTTP request body, or null if the body is not set
     */
    public String getMessageBody() {
        return content;
    }

    /**
     * Creates a document selector for a document belonging to the current user.
     * 
     * The documentPath parameter contains the document name, optionally
     * prefixed by a list of "/"-separated subdirectory names.
     * 
     * Note that certain characters that are legal in XUIs or document names
     * require special handling when included in the document selector. This
     * method will automatically percent encode such strings according to the
     * rules described in [RFC4825]. See createUserDocumentSelector(String,
     * String, String) for examples of percent encoding.
     * 
     * @param auid
     *            - the application usage ID that the document belongs to
     * @param documentPath
     *            - the document name, optionally prefixed by one or more
     *            subdirectories
     * @return a document selector
     * @throws IllegalArgumentException
     *             - if the auid argument or the documentPath argument is null
     * @throws IllegalStateException
     *             - if the current user isn't set properly.
     */
    public static String createUserDocumentSelector(String auid,
            String documentPath) {
        if (auid == null) {
            throw new IllegalArgumentException("the auid argument is null");
        }

        if (documentPath == null) {
            throw new IllegalArgumentException(
                    "the documentPath argument is null");
        }

        if (currentUser == null) {
            String defStackXUI = System.getProperty(XDMServiceImpl.DEF_XUI_KEY);
            if(defStackXUI == null) {
                throw new IllegalStateException(
                "The current user isn't set properly. Try to create xdm service before.");
            }
            currentUser = defStackXUI;
        }

        return createUserDocumentSelector(auid, currentUser, documentPath);
    }

    /**
     * Creates a document selector for a document belonging to a particular
     * user.
     * 
     * The documentPath contains the document name, optionally prefixed by a
     * list of "/"-separated subdirectory names.
     * 
     * Note that according to [RFC4825], certain characters that are legal in
     * XUIs or document names require special handling when included in the
     * document selector.
     * 
     * @param auid
     *            - the application usage ID that the document belongs to
     * @param xui
     *            - the XUI of the user
     * @param documentPath
     *            - the document name, optionally prefixed by one or more
     *            subdirectories
     * @return a document selector
     * @throws IllegalArgumentException
     *             - if the auid argument, the xui argument, or the documentPath
     *             argument is null
     */
    public static String createUserDocumentSelector(String auid, String xui,
            String documentPath) {
        if (auid == null) {
            throw new IllegalArgumentException("the auid argument is null");
        }

        if (xui == null) {
            throw new IllegalArgumentException("the xui argument is null");
        }

        if (documentPath == null) {
            throw new IllegalArgumentException(
                    "the documentPath argument is null");
        }

        return String.format("%s/%s/%s/%s", auid, DOCUMENT_PATH_USERS,
                XCAPURLEncoder.encode(xui), documentPath);
    }

    /**
     * Creates a document selector for a global document.
     * 
     * The documentPath contains the document name, optionally prefixed by a
     * list of "/"-separated subdirectory names.
     * 
     * Note that certain characters that are legal in document names require
     * special handling when included in the document selector. This method will
     * automatically percent encode such strings according to the rules
     * described in [RFC4825]. See createUserDocumentSelector(String, String,
     * String) for examples of percent encoding.
     * 
     * @param auid
     *            - the application usage ID that the document belongs to
     * @param documentPath
     *            - the document name, optionally prefixed by one or more
     *            subdirectories
     * @return a document selector
     * @throws IllegalArgumentException
     *             - if the auid argument or the documentPath argument is null
     */
    public static String createGlobalDocumentSelector(String auid,
            String documentPath) {
        if (auid == null) {
            throw new IllegalArgumentException("the auid argument is null");
        }

        if (documentPath == null) {
            throw new IllegalArgumentException(
                    "the documentPath argument is null");
        }

        return String.format("%s/%s/%s", auid, DOCUMENT_PATH_GLOBAL,
                documentPath);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void prepare() {
        prepareContentType();
    }

    private void prepareContentType() {
        if (requestType == RequestType.PUT_DOC
                || requestType == RequestType.PUT_ELEM) {
            if (getHeader(Header.Content_Type) == null) {
                SelectorType selectorType = nodeSelector.retrieveSelectorType();
                switch (selectorType) {
                case ATTRIBUTE:
                    addHeader(Header.Content_Type, CONTENT_TYPE_XML_ATTRIBUTE);
                    break;
                case ELEMENT:
                    addHeader(Header.Content_Type, CONTENT_TYPE_XML_ELEMENT);
                    break;
                default:
                    Log.i(TAG, "Can't determinate content type for request: "
                            + toString());
                    break;
                }
            }
        }
    }
}
