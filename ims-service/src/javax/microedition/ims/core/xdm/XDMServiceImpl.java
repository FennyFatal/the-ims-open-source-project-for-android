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

package javax.microedition.ims.core.xdm;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

//import android.os.SystemProperties;

import javax.microedition.ims.DefaultStackContext;
import javax.microedition.ims.StackHelper;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.AkaException;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.auth.AKAAuthProviderMockImpl;
import javax.microedition.ims.core.auth.AuthUtil;
import javax.microedition.ims.core.auth.DigestUtils;
import javax.microedition.ims.core.connection.ConnState;
import javax.microedition.ims.core.connection.ConnectionDataProvider;
import javax.microedition.ims.core.connection.ConnectionDataProviderConfigVsDnsImpl;
import javax.microedition.ims.core.env.DefaultScheduledService;
import javax.microedition.ims.core.env.EnvironmentDefaultImpl;
import javax.microedition.ims.core.messagerouter.MessageRouterComposite;
import javax.microedition.ims.core.messagerouter.MessageRouterMSRP;
import javax.microedition.ims.core.messagerouter.MessageRouterSIP;
import javax.microedition.ims.core.registry.DefaultStackRegistry;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.transaction.TransactionManager;
import javax.microedition.ims.core.xdm.data.DocumentBean;
import javax.microedition.ims.core.xdm.data.DocumentDataBean;
import javax.microedition.ims.dns.DNSResolverDNSJavaImpl;
//import javax.microedition.ims.entrypoint.MockStackRegistryHelper;
//import javax.microedition.ims.entrypoint.config.MockConfBuilderRegistry;
import javax.microedition.ims.messages.parser.message.ChallengeParser;
import javax.microedition.ims.messages.wrappers.sip.AuthenticationChallenge;
import javax.microedition.ims.transport.messagerouter.Router;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com) Date: 19.4.2010 Time: 17.30.11
 */
public class XDMServiceImpl extends AbstractService implements XDMService, Shutdownable {
    private static final String TAG = "XDMServiceImpl";

    private static final String DIGEST_TEMPLATE = "Digest username=\"#uname\", realm=\"#realm\", nonce=\"#nonce\", uri=\"#uri\", response=\"#responce\", opaque=\"#opaque\", qop=#qop, nc=#nc, cnonce=\"#cnonce\"";

    private static final String DIGEST = "Digest username=\"sip:movial11@dummy.com\", realm=\"dummy.com\", nonce=\"905af2e3e13a653da86bd3d32c820689\", uri=\"/services/resource-lists/users/sip:movial11@dummy.com\", response=\"ae5a6e2877402d6f8991818efaa05aa0\", opaque=\"21b11c1ec137d891c338a8c65c940ec3\", qop=auth, nc=00000003, cnonce=\"14d3e1cd6398960c\"";

    private static final String WRONG_DIGEST = "Digest username=\"sip:movial11@dummy.com\", realm=\"dummy.com\", nonce=\"05af2e3e13a653da86bd3d32c820689\", uri=\"/services/resource-lists/users/sip:movial11@dummy.com\", response=\"ae5a6e2877402d6f8991818efaa05aa0\", opaque=\"21b11c1ec137d891c338a8c65c940ec3\", qop=auth, nc=00000003, cnonce=\"14d3e1cd6398960c\"";

    private static final String DIGEST2 = "Digest username=\"sip:movial11@dummy.com\", realm=\"dummy.com\", nonce=\"403f83140fbf864a9110a29258e5dea7\", uri=\"/services/org.openmobilealliance.xcap-directory/users/sip%3Amovial11%40dummy.com/directory.xml\", response=\"20452a026e0da8e8db71b8d5e7a429e2\", opaque=\"b3addfe60e740417d60d2059b1aa43c6\", qop=auth, nc=00000005, cnonce=\"d776de233ae90908\"";

    private static final String URL_STR = "http://siptest.dummy.com:8080/services/resource-lists/users/sip:movial11@dummy.com/index";

    // http://siptest.dummy.com:8080/services/resource-lists/users/sip:movial11@dummy.com/index/~~/resource-lists/list/entry[@uri="tel:19728881041"]

    private static final String documentListXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<xcap-directory xmlns=\"urn:oma:xml:xdm:xcap-directory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "    <folder auid=\"org.openmobilealliance.group-usage-list\"/>\n"
            + "    <folder auid=\"simservs.ngn.etsi.org\"/>\n"
            + "    <folder auid=\"resource-lists\">\n"
            + "        <entry uri=\"http://siptest.dummy.com:8080/services/resource-lists/users/sip%3Amovial11%40dummy.com/index\" etag=\"W/'437-1271417541984'\" last-modified=\"2010-04-16T13:32:21.000+02:00\" size=\"437\"/>\n"
            + "    </folder>\n"
            + "    <folder auid=\"org.openmobilealliance.USER-profile\"/>\n"
            + "    <folder auid=\"pidf-manipulation\"/>\n"
            + "    <folder auid=\"pres-rules\"/>\n"
            + "    <folder auid=\"rls-services\">\n"
            + "        <entry uri=\"http://siptest.dummy.com:8080/services/rls-services/users/sip%3Amovial11%40dummy.com/index\" etag=\"W/'435-1261478532956'\" last-modified=\"2009-12-22T11:42:12.000+01:00\" size=\"435\"/>\n"
            + "    </folder>\n"
            + "    <folder auid=\"org.openmobilealliance.pres-rules\"/>\n"
            + "    <folder auid=\"org.openmobilealliance.pres-content\"/>\n"
            + "    <folder auid=\"org.openmobilealliance.access-rules\"/>\n"
            + "    <folder auid=\"org.openmobilealliance.groups\"/>\n" + "</xcap-directory>";

    private final DocumentBuilder db;

    private final AtomicReference<AuthenticationChallenge> lastChallenge = new AtomicReference<AuthenticationChallenge>(
            null);

    private static final String LIST_NODE_NAME = "list";

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String CHARSET_NAME = DEFAULT_CHARSET;

    private static final int BUFF_SIZE = 10 * 1024;

    private static class HttpClientHolder {
        private static HttpClient httpClient = createHttpClient();

        private static HttpClient createHttpClient() {
            /*
             * HttpParams parameters = new BasicHttpParams(); SchemeRegistry
             * schemeRegistry = new SchemeRegistry(); //SSLSocketFactory
             * sslSocketFactory = SSLSocketFactory.getSocketFactory();
             * LayeredSocketFactory sslSocketFactory = new
             * EasySSLSocketFactory();
             * //sslSocketFactory.setHostnameVerifier(SSLSocketFactory
             * .ALLOW_ALL_HOSTNAME_VERIFIER); schemeRegistry.register(new
             * Scheme("https", sslSocketFactory, 443)); ClientConnectionManager
             * manager = new ThreadSafeClientConnManager(parameters,
             * schemeRegistry); HttpClient httpClient = new
             * DefaultHttpClient(manager, parameters);
             */
            HttpParams httpParamters = new BasicHttpParams();
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParamters, timeoutConnection);
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParamters, timeoutSocket);

            final HttpClient httpClient = new DefaultHttpClient(httpParamters);
            try {
                prepareHttpClient(httpClient);
            } catch (InstantiationException e) {
                Logger.log(TAG, e.getMessage());
            }
            return httpClient;
        }

        private static void prepareHttpClient(HttpClient httpClient) throws InstantiationException {
            javax.net.ssl.SSLSocketFactory nativeSslSocketFactory;
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[] {
                    new FakeX509TrustManager()
                }, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(nativeSslSocketFactory = sc
                        .getSocketFactory());
                // SSLCertificateSocketFactory sslCertificateSocketFactory = new
                // SSLCertificateSocketFactory(500);
                // HttpsURLConnection.setDefaultSSLSocketFactory(sslCertificateSocketFactory);
            } catch (NoSuchAlgorithmException e) {
                Logger.log(TAG, e.getMessage());
                throw new InstantiationException(e.getMessage());
            } catch (KeyManagementException e) {
                Logger.log(TAG, e.getMessage());
                throw new InstantiationException(e.getMessage());
            }

            EasySSLSocketFactory sslSocketFactory = new EasySSLSocketFactory(nativeSslSocketFactory);

            sslSocketFactory.setHostnameVerifier(new AcceptAllHostnameVerifier());
            Scheme httpsShema = new Scheme("https", sslSocketFactory, 443);

            httpClient.getConnectionManager().getSchemeRegistry().register(httpsShema);
        }
    }

    public XDMServiceImpl(StackContext stackContext, TransactionManager transactionManager)
            throws IMSStackException {
        super(stackContext, transactionManager);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            db = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IMSStackException(e);
        }
    }

    public XDMResponse sendXCAPRequest(final XDMRequest request) throws XCAPException, SAXException {

        Logger.log("XCAP REQUEST",
                "------------------------------ Request Begin ------------------------------");
        Logger.log("XCAP REQUEST", "METHOD: " + request.getMethod());
        Logger.log("XCAP REQUEST", "URI: " + request.getURI());
        Logger.log("XCAP REQUEST", "HEADERS: " + request.getHeaders());
        Logger.log("XCAP REQUEST", "TYPE: " + request.getEntityType());
        Logger.log("XCAP REQUEST", "ID: " + request.getIMSEntityId());
        Logger.log("XCAP REQUEST", "BODY: " + request.getBody());
        Logger.log("XCAP REQUEST",
                "------------------------------ Request End ------------------------------");

        // final String content = "content goes here";
        // final String etag = "etag goes here";
        // final String mimeType = "mime type goes here";

        XDMResponse retValue = null;

        final HttpClient httpclient = HttpClientHolder.httpClient;

        HttpResponse response = null;
        try {
            // prepare and send first request
            HttpRequestBase httpRequest = createHttpRequest(request);

            prepareUrlConnection(request, httpRequest, lastChallenge.get());
            // HttpURLConnection urlConnection = (HttpURLConnection)
            // url.openConnection();

            /*
             * System.out.println("AllowUserInteraction = " +
             * urlConnection.getAllowUserInteraction());
             * System.out.println("DoInput = " + urlConnection.getDoInput());
             * System.out.println("DoOutput = " + urlConnection.getDoOutput());
             * System.out.println("DefaultUseCaches = " +
             * urlConnection.getDefaultUseCaches());
             * System.out.println("InstanceFollowRedirects = " +
             * urlConnection.getInstanceFollowRedirects());
             * System.out.println("UseCaches = " +
             * urlConnection.getUseCaches());
             * System.out.println("DefaultAllowUserInteraction = " +
             * HttpURLConnection.getDefaultAllowUserInteraction());
             * urlConnection.setDoInput(true); urlConnection.setDoOutput(true);
             * urlConnection.setAllowUserInteraction(true);
             * URLConnection.setDefaultAllowUserInteraction(true);
             */
            printRequest(httpRequest);
            response = httpclient.execute(httpRequest);
            printResponse(response);

            // if (urlConnection.getResponseCode() == 401) {//this line causes
            // additional request to server without auth challenge
            boolean isAuthRequired = response.containsHeader("WWW-Authenticate");
            if (isAuthRequired) {
                String wwwAuthenticateHeader = response.getFirstHeader("WWW-Authenticate")
                        .getValue();
                AuthenticationChallenge challenge = (AuthenticationChallenge)ChallengeParser
                        .consume(wwwAuthenticateHeader);
                lastChallenge.set(challenge);

                response.getEntity().consumeContent();

                // prepare and send second request with auth
                HttpRequestBase httpRequestWithAuth = createHttpRequest(request);
                prepareUrlConnection(request, httpRequestWithAuth, challenge);

                printRequest(httpRequest);
                response = httpclient.execute(httpRequestWithAuth);
                printResponse(response);
            }

            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == HttpStatus.SC_OK || responseCode == 201) {

                final Header etagHeader = response.getFirstHeader("ETag");
                Logger.log(TAG, "etagHeader: " + etagHeader);
                final Header contentTypeHeader = response.getFirstHeader("Content-Type");
                Logger.log(TAG, "contentTypeHeader: " + contentTypeHeader);

                final byte[] content = extractBody(response);
                Logger.log(TAG, "extracted");
                Logger.log(TAG, "BODY: "
                        + (content != null ? new String(content, DEFAULT_CHARSET) : ""));

                retValue = new XDMResponseImpl(content == null ? null
                        : db.parse(new ByteArrayInputStream(content)), content,
                        etagHeader != null ? etagHeader.getValue() : null,
                        contentTypeHeader != null ? contentTypeHeader.getValue() : null);
            } else {
                String reasonPhrase = response.getStatusLine().getReasonPhrase();
                XCAPException xcapException = new XCAPException(responseCode, reasonPhrase);
                if (responseCode == HttpStatus.SC_CONFLICT) {
                    // TODO Add XCAPError
                }
                throw xcapException;
            }
        } catch (SocketTimeoutException e) {
            Logger.log(TAG, e.getMessage());
            XCAPException xcapException = new XCAPException(408, "Request Timeout");
            throw xcapException;
        } catch (IOException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    if (response.getEntity() != null)
                        response.getEntity().consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return retValue;
    }

    private void printRequest(HttpRequestBase httpRequest) {
        Logger.log(TAG,
                "------------------------------ Request Begin ------------------------------");
        Logger.log(TAG, httpRequest.getRequestLine().toString());

        for (Header header : httpRequest.getAllHeaders()) {
            Logger.log(TAG, header.getName() + " = " + header.getValue());
        }
        Logger.log(TAG, "------------------------------ Request End ------------------------------");
    }

    private void printResponse(HttpResponse response) throws IOException {
        Logger.log(TAG,
                "------------------------------ Response Begin ------------------------------");
        Logger.log(TAG, response.getStatusLine().toString());

        for (Header header : response.getAllHeaders()) {
            Logger.log(TAG, header.getName() + " = " + header.getValue());
        }

        Logger.log(TAG,
                "------------------------------ Response End ------------------------------");
    }

    private byte[] extractBody(HttpResponse response) throws IOException,
            UnsupportedEncodingException {
        byte[] content = null;

        HttpEntity responseEntity = response.getEntity();
        long contentLength = responseEntity.getContentLength();

        if (contentLength > 0) {
            content = EntityUtils.toByteArray(responseEntity);
        }
        return content;
    }

    /*
     * private byte[] readAllAndClose(final InputStream inputStream, final int
     * possibleStreamLength) throws IOException { byte[] content;
     * BufferedInputStream in = new BufferedInputStream(inputStream, BUFF_SIZE);
     * ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
     * possibleStreamLength); byte[] dataChunk = new byte[BUFF_SIZE]; int
     * redBytes = 0; while ((redBytes = in.read(dataChunk)) != -1) {
     * byteArrayOutputStream.write(dataChunk, 0, redBytes); } in.close();
     * byteArrayOutputStream.close(); content =
     * byteArrayOutputStream.toByteArray(); return content; }
     */

    private HttpRequestBase createHttpRequest(final XDMRequest request) {
        HttpMethod httpMethod = request.getMethod();

        HttpRequestBase httpRequest;
        switch (httpMethod) {
            case GET: {
                httpRequest = new HttpGet(request.getURI());
                break;
            }
            case PUT:
                httpRequest = new HttpPut(request.getURI());
                break;
            case DELETE:
                httpRequest = new HttpDelete(request.getURI());
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        }
        return httpRequest;
    }

    private HttpRequestBase prepareUrlConnection(XDMRequest requestDescriptor,
            HttpRequestBase httpRequest, AuthenticationChallenge challenge) throws IOException {

        /*
         * if (request.getHeaders().get("Authorization") == null) {
         * urlConnection.setRequestProperty("Authorization", DIGEST); }
         */

        if (challenge != null) {
            String digest = calcAuthResponse(requestDescriptor, challenge);
            httpRequest.addHeader("Authorization", digest);
        }

        for (Entry<String, String> mapEntry : requestDescriptor.getHeaders().entrySet()) {
            httpRequest.addHeader(mapEntry.getKey(), mapEntry.getValue());
        }

        //String headerString = SystemProperties.get("ro.product.manufacturer") + "." +
        //                    SystemProperties.get("ro.product.model") + "." +
        //                    SystemProperties.get("ro.build.display.id");
        String headerString = "Test.0.1";

        httpRequest.addHeader("X-3GPP-Intended-Identity", "\"" + getStackContext().getRegistrationIdentity().getUserInfo().toUri() + "\"");
        httpRequest.addHeader("XCAP-User-Agent", headerString);

        String requestBody = requestDescriptor.getBody();

        if (httpRequest instanceof HttpPut) {
            HttpPut httpPutRequest = ((HttpPut)httpRequest);
            if (requestBody != null && !requestBody.equals("")) {
                StringEntity dataEntity = new StringEntity(requestDescriptor.getBody(),
                        CHARSET_NAME);
                httpPutRequest.setEntity(dataEntity);
            }
        }

        return httpRequest;
    }

    // see examples at
    // http://potaroo.net/ietf/all-ids/draft-smith-sip-auth-examples-00.txt

    private String calcAuthResponse(XDMRequest request, AuthenticationChallenge challenge) {
        final XDMConfig xdmConfig = getStackContext().getConfig().getXDMConfig();
        final String userName = xdmConfig.getAuthName();
        final String realm = challenge.getRealm();
        final String password = xdmConfig.getPassword();
        final String nonce = challenge.getNextNonce() != null ? challenge.getNextNonce()
                : challenge.getNonce();
        // final String uri =
        // "/services/org.openmobilealliance.xcap-directory/users/sip%3Amovial11%40dummy.com/directory.xml";
        final String uri = "/" + removeXCAPRoot(request.getURI());
        // final String clientNonce = "d776de233ae90908";
        final String clientNonce = DigestUtils.md5Hex("" + System.currentTimeMillis());
        final String auth = challenge.getQop() == 1 ? "auth" : "auth-int";
        // final String nc = "00000005";

        String nonceCount = challenge.getNonceCount();
        final String nc = AuthUtil.creatNounceCounterString(nonceCount == null ? 1 : Integer
                .parseInt(nonceCount));

        final HttpMethod method = request.getMethod();
        final String body = request.getBody();

        String ha1 = AuthUtil.calcHA1(userName, realm, password);
        String ha2 = AuthUtil.calcQopHA2(method.name(), body == null ? null : body.getBytes(), uri,
                challenge.getQop());

        final String authResponce = AuthUtil.calculateAuthResponse(ha1, ha2, nonce, nc,
                clientNonce, challenge.getQop());

        // $uname, $realm, $nonce, $uri, $responce, $cnonce, $qop, $nc, $cnonce;
        return DIGEST_TEMPLATE.replaceAll("#uname", userName).replaceAll("#realm", realm)
                .replaceAll("#nonce", nonce).replaceAll("#uri", uri)
                .replaceAll("#responce", authResponce).replaceAll("#opaque", challenge.getOpaque() != null ? challenge.getOpaque() : "")
                .replaceAll("#qop", auth).replaceAll("#nc", nc).replaceAll("#cnonce", clientNonce);
    }

    /*
     * private static String getMethod(int methodType) { String retValue = null;
     * switch (methodType) { case 0: retValue = "PUT"; break; case 1: retValue =
     * "GET"; break; case 2: retValue = "DELETE"; break; default:
     * Logger.log(TAG, "getMethod#unknown method type = " + methodType); break;
     * } return retValue; } /* <?xml version="1.0" encoding="UTF-8"?>
     * <resource-lists xmlns="urn:ietf:params:xml:ns:resource-lists"> <list
     * name="phbk"> <display-name>phbk</display-name> <gid
     * xmlns="voxmobili.xcap.dblink">119851</gid> <entry uri="tel:19728881041">
     * <display-name>T 1041</display-name> </entry> <entry
     * uri="tel:14257707516"> <display-name>User 7516</display-name> </entry>
     * </list> </resource-lists>
     */

    public List<DocumentBean> listDocuments(String auid) throws XCAPException {
        /**
         * 1. Urls for all auids
         * http://siptest.dummy.com:8080/services/org.openmobilealliance
         * .xcap-directory/users/sip%3Amovial11%40dummy.com/directory.xml 2.
         * Query for specified auid
         * http://siptest.dummy.com:8080/services/org
         * .openmobilealliance.xcap-
         * directory/users/sip%3Amovial11%40dummy.com
         * /directory.xml/~~/xcap-directory
         * /folder%5B@auid=%22resource-lists%22%5D Response: <folder
         * auid="resource-lists"> <entry uri=
         * "http://siptest.dummy.com:8080/services/resource-lists/users/sip%3Amovial11%40dummy.com/index"
         * etag="W/'437-1271417541984'"
         * last-modified="2010-04-16T13:32:21.000+02:00" size="437"/> </folder>
         */
        final HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", DIGEST2);

        String xcapURI = "http://siptest.dummy.com:8080/services/org.openmobilealliance.xcap-directory/users/sip%3Amovial11%40dummy.com/directory.xml";
        if (auid != null) {
            xcapURI = xcapURI + "/~~/xcap-directory/folder%5B@auid=%22" + auid + "%22%5D";
        }

        XDMRequest documentEntryRequest = new XDMRequestImpl(HttpMethod.GET, xcapURI, headers, null);

        // String xmlContent = documentListXML;
        List<DocumentBean> retValue = new ArrayList<DocumentBean>();

        try {
            final XDMResponse xdmResponse = sendXCAPRequest(documentEntryRequest);

            NodeList nodeList = xdmResponse.getDoc().getElementsByTagNameNS("*", "folder");
            for (int fldrIndex = 0; fldrIndex < nodeList.getLength(); fldrIndex++) {
                Node node = nodeList.item(fldrIndex);
                retValue.addAll(handleFolderNode(node));
            }

        } catch (SAXException e) {
            e.printStackTrace();
        }

        return retValue;
    }

    private List<DocumentBean> handleFolderNode(Node node) {
        List<DocumentBean> retValue = new ArrayList<DocumentBean>();

        if (node != null) {

            if (!"Folder".equalsIgnoreCase(node.getNodeName())) {
                throw new IllegalArgumentException("Must be 'Folder' node");
            }

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;

                NodeList nameNode = element.getElementsByTagNameNS("*", "entry");

                for (int entrIndex = 0; entrIndex < nameNode.getLength(); entrIndex++) {
                    if (nameNode.item(entrIndex).getNodeType() == Node.ELEMENT_NODE) {
                        Element entryElement = (Element)nameNode.item(entrIndex);
                        // "<entry
                        // uri=\"http://siptest.dummy.com:8080/services/resource-lists/users/sip%3Amovial11%40dummy.com/index\"
                        // etag=\"W/'437-1271417541984'\" last-modified=\"2010-04-16T13:32:21.000+02:00\" size=\"437\"/>\n"
                        // +

                        String uriAttrValue = entryElement.getAttribute("uri");
                        String etagAttrValue = entryElement.getAttribute("etag");
                        String lmAttrValue = entryElement.getAttribute("last-modified");
                        String sizeAttrValue = entryElement.getAttribute("size");

                        DocumentBean documentBean = new DocumentDataBean(uriAttrValue,
                                etagAttrValue, lmAttrValue, sizeAttrValue);

                        retValue.add(documentBean);
                    }
                }
            }
        }

        return retValue.size() != 0 ? retValue : Collections.<DocumentBean> emptyList();
    }

    private URIListData handleListNode(Node listNode) {

        URIListDataBean retValue;

        if (listNode != null) {

            if (!LIST_NODE_NAME.equalsIgnoreCase(listNode.getLocalName())) {
                throw new IllegalArgumentException("Must be 'list' listNode");
            }

            if (listNode.getNodeType() == Node.ELEMENT_NODE) {
                Element listElement = (Element)listNode;

                NodeList displayNameNode = listElement.getElementsByTagNameNS("*", "display-name");
                String displayName = /* "" */null;

                for (int entrIndex = 0; entrIndex < displayNameNode.getLength(); entrIndex++) {
                    if (displayNameNode.item(entrIndex).getNodeType() == Node.ELEMENT_NODE) {

                        Element entryElement = (Element)displayNameNode.item(entrIndex);
                        if (LIST_NODE_NAME.equalsIgnoreCase(entryElement.getParentNode()
                                .getLocalName())) {

                            final Node firstChildNode = entryElement.getFirstChild();
                            if (firstChildNode != null) {
                                displayName = firstChildNode.getNodeValue();
                            }
                            break;
                        }
                    }
                }

                String name = listElement.getAttribute("name");

                List<ListEntryData> listEntryData = new ArrayList<ListEntryData>();

                // extracts single user URI
                {
                    NodeList entryNode = listElement.getElementsByTagNameNS("*", "entry");
                    for (int entrIndex = 0; entrIndex < entryNode.getLength(); entrIndex++) {
                        if (entryNode.item(entrIndex).getNodeType() == Node.ELEMENT_NODE) {
                            Element entryElement = (Element)entryNode.item(entrIndex);
                            if (LIST_NODE_NAME.equalsIgnoreCase(entryElement.getParentNode()
                                    .getLocalName())) {
                                String entryURI = entryElement.getAttribute("uri");
                                NodeList entryDisplayNameNode = entryElement
                                        .getElementsByTagNameNS("*", "display-name");
                                String entryDisplayName = null/* "" */;
                                if (entryDisplayNameNode.getLength() > 0) {
                                    Node node = entryDisplayNameNode.item(0);
                                    final Node firstChildNode = node.getFirstChild();
                                    if (firstChildNode != null) {
                                        entryDisplayName = firstChildNode.getNodeValue();
                                    }
                                }

                                listEntryData.add(new ListEntryDataBean(ListEntryData.URI_ENTRY,
                                        entryDisplayName, entryURI));
                            }
                        }
                    }
                }

                // extracts references to an already existing URI list
                {
                    NodeList externalsNode = listElement.getElementsByTagNameNS("*", "external");
                    for (int entrIndex = 0; entrIndex < externalsNode.getLength(); entrIndex++) {
                        if (externalsNode.item(entrIndex).getNodeType() == Node.ELEMENT_NODE) {
                            Element externalElement = (Element)externalsNode.item(entrIndex);
                            if (LIST_NODE_NAME.equalsIgnoreCase(externalElement.getParentNode()
                                    .getLocalName())) {
                                String anchorURI = externalElement.getAttribute("anchor");
                                NodeList anchorDisplayNameNode = externalElement
                                        .getElementsByTagNameNS("*", "display-name");
                                String anchorDisplayName = null/* "" */;
                                if (anchorDisplayNameNode.getLength() > 0) {
                                    Node node = anchorDisplayNameNode.item(0);
                                    final Node firstChildNode = node.getFirstChild();
                                    if (firstChildNode != null) {
                                        anchorDisplayName = firstChildNode.getNodeValue();
                                    }
                                }

                                listEntryData
                                        .add(new ListEntryDataBean(ListEntryData.URI_LIST_ENTRY,
                                                anchorDisplayName, anchorURI));
                            }
                        }
                    }
                }

                retValue = new URIListDataBean(displayName, name, listEntryData);

            } else {
                throw new IllegalArgumentException("only " + Node.ELEMENT_NODE
                        + " is allowed as parameter. Passed " + listNode.getNodeType());
            }
        } else {
            throw new NullPointerException("listNode is null. Null is not allowed here.");
        }

        return retValue;
    }

    // public static void main(String[] args) {
    //
    // try {
    // URL url = new URL(URL_STR);
    // HttpURLConnection urlConnection = (HttpURLConnection)
    // url.openConnection();
    // urlConnection.setRequestProperty("Authorization", DIGEST);
    // urlConnection.connect();
    // InputStream in = (InputStream) urlConnection.getContent();
    //
    // String ha1 = AuthUtil.calcHA1("sip:movial11@dummy.com",
    // "dummy.com", "movial11");
    // String ha2 = AuthUtil.calcQopHA2("GET", null,
    // "/services/org.openmobilealliance.xcap-directory/users/sip%3Amovial11%40dummy.com/directory.xml",
    // (byte) 1);
    // final String s = AuthUtil.calculateAuthResponse(ha1, ha2,
    // "403f83140fbf864a9110a29258e5dea7", "00000005", "d776de233ae90908",
    // (byte) 1);
    // System.out.println("" + s);
    // /*InputStreamReader streamReader = new InputStreamReader(in);
    // BufferedReader bufferedReader = new BufferedReader(streamReader,
    // 10*1024);
    //
    // char [] responseBody = new char[urlConnection.getContentLength()];
    // bufferedReader.read(responseBody);
    //
    //
    // System.out.println("" + new String(responseBody));*/
    //
    //
    // //SAXReader saxReader = new SAXReader();
    // // Document document = null;
    // // try {
    // // document = saxReader.read(in);
    // // } catch (DocumentException e) {
    // // e.printStackTrace(); //To change body of catch statement use File |
    // Settings | File Templates.
    // // }
    //
    // //System.out.println("" + document);
    //
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

    public <T extends XDMDocumentData> T retrieveDocument(XDMRequest xdmRequest,
            XDMDocumentDescriptor<T> documentDescriptor) throws XCAPException, IOException {

        T retValue;

        final XDMDocumentType type = documentDescriptor.getDocumentType();

        switch (type) {
            case URI_LIST_DOCUMENT: {
                // supress inspection here, cause we know for sure the type of
                // ret value.
                // noinspection unchecked
                retValue = (T)retrieveURIListDocument(xdmRequest);
            }
                break;

            case PRESENCE_LIST_DOCUMENT: {
                // supress inspection here, cause we know for sure the type of
                // ret value.
                // noinspection unchecked
                retValue = (T)retrievePresenceListDocument(xdmRequest);
            }
                break;

            case PRESENCE_AUTHORIZATION_DOCUMENT: {
                // supress inspection here, cause we know for sure the type of
                // ret value.
                // noinspection unchecked
                retValue = (T)retrievePresenceAuthorizationDocument(xdmRequest);
            }
                break;

            default: {
                throw new UnsupportedOperationException("Operation " + type + " is not supported.");
            }
        }

        return retValue;
    }

    private URIListDocumentData retrieveURIListDocument(XDMRequest xdmRequest) throws IOException, XCAPException {
        final URIListDocumentData retValue;

        try {
            final XDMResponse xdmResponse = sendXCAPRequest(xdmRequest);
            final String documentSelector = removeNodeSelector(removeXCAPRoot(xdmRequest.getURI()));
            List<URIListData> dataList = new ArrayList<URIListData>();
            final String xmlContent = xdmResponse.buildContent();

            if (xmlContent != null) {
                dataList = parseURIListDocument(xdmResponse.getDoc());
            }

            retValue = new URIListDocumentDataBean(xdmResponse.getEtag(), documentSelector,
                    xmlContent, dataList);
        } catch (SAXException e) {
            e.printStackTrace();
            final String errMsg = "Cann't parse xml, e = " + e.getMessage();
            Logger.log(Logger.Tag.WARNING, errMsg);
            throw new IOException(errMsg);
        } catch (XCAPException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            final String errMsg = "Cann't parse xml, e = " + e.getMessage();
            Logger.log(Logger.Tag.WARNING, errMsg);
            throw new IOException(errMsg);
        }

        return retValue;

    }

    // TODO for test
    /*
     * source = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
     * "<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\" xmlns:xd=\"urn:oma:xml:xdm:xcap-directory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
     * + "<list name=\"rcs\">"+ "<entry uri=\"sip:movial7\" />"+
     * "<entry uri=\"sip:movial7@dummy.com\" />"+
     * "<entry uri=\"sip:movial8@dummy.com\" />"+
     * "<entry uri=\"welcome@app.on.im\">"+
     * "<display-name>welcome@app.on.im</display-name>"+ "</entry>"+ "</list>"+
     * "<list name=\"rcs_blockedcontacts\" />"+
     * "<list name=\"rcs_revokedcontacts\" />"+ "<list name=\"oma_buddylist\">"+
     * "<external anchor=\"http://siptest.dummy.com:8080/services/resource-lists/users/sip:movial8@dummy.com/index/~~/resource-lists/list%5b@name=%22rcs%22%5d\" />"
     * + "</list>"+ "<list name=\"oma_grantedcontacts\">"+
     * "<external anchor=\"http://siptest.dummy.com:8080/services/resource-lists/users/sip:movial8@dummy.com/index/~~/resource-lists/list%5b@name=%22rcs%22%5d\" />"
     * + "</list>"+ "<list name=\"oma_blockedcontacts\">"+
     * "<external anchor=\"http://siptest.dummy.com:8080/services/resource-lists/users/sip:movial8@dummy.com/index/~~/resource-lists/list%5b@name=%22rcs_revokedcontacts%22%5d\" />"
     * +
     * "<external anchor=\"http://siptest.dummy.com:8080/services/resource-lists/users/sip:movial8@dummy.com/index/~~/resource-lists/list%5b@name=%22rcs_blockedcontacts%22%5d\" />"
     * + "</list>"+ "</resource-lists>";
     */

    private List<URIListData> parseURIListDocument(final Document doc) {

        final List<URIListData> retValue = new ArrayList<URIListData>();
        NodeList nodeList = doc.getElementsByTagNameNS("*", LIST_NODE_NAME);

        for (int listIndex = 0; listIndex < nodeList.getLength(); listIndex++) {
            Node node = nodeList.item(listIndex);
            retValue.add(handleListNode(node));
        }

        return retValue;
    }

    public <T extends XDMDocumentData> T loadDocument(String documentSelector,
            XDMDocumentDescriptor<T> documentDescriptor, String source) throws IOException {
        T retValue;

        final XDMDocumentType type = documentDescriptor.getDocumentType();

        switch (type) {
            case URI_LIST_DOCUMENT: {
                // supress inspection here, cause we know for sure the type of
                // ret value.
                // noinspection unchecked
                retValue = (T)loadURIListDocument(documentSelector, source);
            }
                break;

            case PRESENCE_LIST_DOCUMENT: {
                // supress inspection here, cause we know for sure the type of
                // ret value.
                // noinspection unchecked
                retValue = (T)loadPresenceListDocument(documentSelector, source);
            }
                break;

            case PRESENCE_AUTHORIZATION_DOCUMENT: {
                // supress inspection here, cause we know for sure the type of
                // ret value.
                // noinspection unchecked
                retValue = (T)loadPresenceAuthorizationDocument(documentSelector, source);
            }
                break;

            default: {
                throw new UnsupportedOperationException("Opeartion " + type + " is not supported.");
            }
        }

        return retValue;
    }

    private URIListDocumentData loadURIListDocument(final String documentSelector,
            final String source) throws IOException {

        final URIListDocumentData retValue;

        try {
            List<URIListData> dataList = parseURIListDocument(stringToXMLDocument(source));
            retValue = new URIListDocumentDataBean(null, documentSelector, source, dataList);
        } catch (SAXException e) {
            e.printStackTrace();
            final String errMsg = "Cann't parse xml, e = " + e.getMessage();
            Logger.log(Logger.Tag.WARNING, errMsg);
            throw new IOException(errMsg);
        } catch (IOException e) {
            e.printStackTrace();
            final String errMsg = "Cann't parse xml, e = " + e.getMessage();
            Logger.log(Logger.Tag.WARNING, errMsg);
            throw new IOException(errMsg);
        } catch (Exception e) {
            e.printStackTrace();
            final String errMsg = "Cann't parse xml, e = " + e.getMessage();
            Logger.log(Logger.Tag.WARNING, errMsg);
            throw new IOException(errMsg);
        }

        return retValue;
    }

    private PresenceListDocumentData loadPresenceListDocument(final String documentSelector,
            final String source) throws IOException {

        final PresenceListDocumentData retValue;

        try {
            List<PresenceListData> dataList = parsePresenceListDocument(stringToXMLDocument(source));
            retValue = new PresenceListDocumentDataBean(null, documentSelector, source, dataList);
        } catch (SAXException e) {
            throw new IOException("Cann't parse xml, e = " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Cann't parse xml, e = " + e.getMessage());
        }

        return retValue;
    }

    private PresenceAuthorizationDocumentData loadPresenceAuthorizationDocument(
            final String documentSelector, final String source) throws IOException {

        final PresenceAuthorizationDocumentData retValue;

        try {
            List<PresenceAuthorizationRuleData> dataList = parsePresenceAuthorizationDocument(stringToXMLDocument(source));

            retValue = new PresenceAuthorizationDocumentDataBean(null, documentSelector, source,
                    dataList);
        } catch (SAXException e) {
            throw new IOException("Cann't parse xml, e = " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Cann't parse xml, e = " + e.getMessage());
        }

        return retValue;
    }

    private Document stringToXMLDocument(String source) throws SAXException, IOException {
        return db.parse(new InputSource(new StringReader(source)));
    }

    private PresenceListDocumentData retrievePresenceListDocument(XDMRequest xdmRequest)
            throws XCAPException, IOException {
        final PresenceListDocumentData retValue;

        try {
            final XDMResponse xdmResponse = sendXCAPRequest(xdmRequest);
            final String documentSelector = removeNodeSelector(removeXCAPRoot(xdmRequest.getURI()));

            List<PresenceListData> dataList = new ArrayList<PresenceListData>();
            if (xdmResponse.buildContent() != null) {
                dataList = parsePresenceListDocument(xdmResponse.getDoc());
            }

            retValue = new PresenceListDocumentDataBean(xdmResponse.getEtag(), documentSelector,
                    xdmResponse.buildContent(), dataList);
        } catch (SAXException e) {
            throw new IOException("Cann't parse xml, e = " + e.getMessage());
        }

        return retValue;
    }

    private List<PresenceListData> parsePresenceListDocument(final Document doc) {
        final List<PresenceListData> retValue = new ArrayList<PresenceListData>();

        NodeList nodeList = doc.getElementsByTagNameNS("*", XDMHelper.SERVICE_NODE_NAME);
        for (int listIndex = 0; listIndex < nodeList.getLength(); listIndex++) {
            Node node = nodeList.item(listIndex);
            retValue.add(XDMHelper.handleServiceNode(node));
        }
        return retValue;
    }

    // private String testXML = "" +
    // "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    // "   <cr:ruleset xmlns=\"urn:ietf:params:xml:ns:pres-rules\"\n" +
    // "    xmlns:pr=\"urn:ietf:params:xml:ns:pres-rules\"\n" +
    // "    xmlns:cr=\"urn:ietf:params:xml:ns:common-policy\">\n" +
    // "    <cr:rule id=\"a\">\n" +
    // "     <cr:conditions>\n" +
    // "      <cr:identity>\n" +
    // "       <cr:one id=\"sip:USER@example.com\"/>\n" +
    // "      </cr:identity>\n" +
    // "     </cr:conditions>\n" +
    // "     <cr:actions>\n" +
    // "      <pr:sub-handling>allow</pr:sub-handling>\n" +
    // "     </cr:actions>\n" +
    // "     <cr:transformations>\n" +
    // "      <pr:provide-services>\n" +
    // "        <pr:service-uri-scheme>sip</pr:service-uri-scheme>\n" +
    // "        <pr:service-uri-scheme>mailto</pr:service-uri-scheme>\n" +
    // "      </pr:provide-services>\n" +
    // "      <pr:provide-persons>\n" +
    // "        <pr:all-persons/>\n" +
    // "      </pr:provide-persons>\n" +
    // "      <pr:provide-activities>true</pr:provide-activities>\n" +
    // "      <pr:provide-USER-input>bare</pr:provide-USER-input>\n" +
    // "       <pr:provide-unknown-attribute\n" +
    // "        ns=\"urn:vendor-specific:foo-namespace\"\n" +
    // "        name=\"foo\">true</pr:provide-unknown-attribute>\n" +
    // "     </cr:transformations>\n" +
    // "    </cr:rule>\n" +
    // "   </cr:ruleset>";

    private PresenceAuthorizationDocumentData retrievePresenceAuthorizationDocument(
            XDMRequest xdmRequest) throws XCAPException, IOException {
        final PresenceAuthorizationDocumentData retValue;

        try {
            final XDMResponse xdmResponse = sendXCAPRequest(xdmRequest);
            final String documentSelector = removeNodeSelector(removeXCAPRoot(xdmRequest.getURI()));

            List<PresenceAuthorizationRuleData> dataList = new ArrayList<PresenceAuthorizationRuleData>();
            if (xdmResponse.buildContent() != null) {
                dataList = parsePresenceAuthorizationDocument(xdmResponse.getDoc());
            }

            retValue = new PresenceAuthorizationDocumentDataBean(xdmResponse.getEtag(),
                    documentSelector, xdmResponse.buildContent(), dataList);

        } catch (SAXException e) {
            throw new IOException("Cann't parse xml, e = " + e.getMessage());
        }

        return retValue;
    }

    private/* static */List<PresenceAuthorizationRuleData> parsePresenceAuthorizationDocument(
            final Document doc) {
        final List<PresenceAuthorizationRuleData> retValue = new ArrayList<PresenceAuthorizationRuleData>();

        // javax.xml.parsers.DocumentBuilderFactory factory =
        // javax.xml.parsers.DocumentBuilderFactory.newInstance();
        // javax.xml.parsers.DocumentBuilder db = null;
        // try {
        // db = factory.newDocumentBuilder();
        // } catch (ParserConfigurationException e) {
        //
        // }
        NodeList nodeList = doc.getElementsByTagNameNS("*", XDMHelper.RULE_NODE_NAME);

        for (int listIndex = 0; listIndex < nodeList.getLength(); listIndex++) {
            Node node = nodeList.item(listIndex);
            retValue.add(XDMHelper.handlePresenceAuthorizationRuleNode(node));
        }

        return retValue;
    }


    public static void main(String[] args) throws IMSStackException, IOException, SAXException,
            XCAPException, AkaException { /*
        final Configuration configuration = MockConfBuilderRegistry.AlternativeServer._79262948587
                .build();
        final DefaultStackRegistry stackRegistry = new DefaultStackRegistry(
                MockStackRegistryHelper.COMMON_REGISTRY);

        ConnectionDataProvider connDataProvider = new ConnectionDataProviderConfigVsDnsImpl(
                configuration, new DNSResolverDNSJavaImpl(configuration));

        connDataProvider.refresh();

        RepetitiousTaskManager repetitiousTaskManager = new RepetitiousTaskManager(new DefaultScheduledService());

        final Router<IMSMessage> messageRouter = new MessageRouterComposite.Builder(configuration,
                connDataProvider).addRouter(new MessageRouterMSRP())
                .addRouter(new MessageRouterSIP(configuration, connDataProvider, repetitiousTaskManager)).build();

        final IMSStack<IMSMessage> imsStack = StackHelper
                .newIMSSipStack(new DefaultStackContext.Builder().configuration(configuration)
                        .router(messageRouter).router(messageRouter)
                        .environment(EnvironmentDefaultImpl.Builder.build(ConnState.CONNECTED))
                        .stackRegistry(stackRegistry)
                        .akaAuthProvider(new AKAAuthProviderMockImpl())
                        .repetitiousTaskManager(repetitiousTaskManager)
                        .build());

        final URIListDocumentData data = ((XDMServiceImpl)imsStack.getXDMService())
                .retrieveURIListDocument(new XDMRequestImpl(HttpMethod.PUT, null,
                        new HashMap<String, String>(), ""));

        System.out.println("" + data);
        */
    }

    private static String removeXCAPRoot(String uri) {
        return uri.replaceAll("^https?://.*?/", "");
    }

    private static String removeNodeSelector(String uri) {
        return uri.replaceAll("/(\\s*)?[?~](\\s*)?[?~](\\s*)/?.*$", "");
    }

    public void shutdown() {
        // TODO: put real code here

        // do not add shutdown(), the HttpClientHolder is static
        // httpclient.getConnectionManager().shutdown();
    }

}
