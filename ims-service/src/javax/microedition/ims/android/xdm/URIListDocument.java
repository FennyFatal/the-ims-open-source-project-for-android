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

package javax.microedition.ims.android.xdm;

import android.os.RemoteException;
import android.util.Log;
import org.xml.sax.SAXException;

import javax.microedition.ims.DefaultStackContext;
import javax.microedition.ims.StackHelper;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.AkaException;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.auth.AKAAuthProviderMockImpl;
import javax.microedition.ims.core.connection.ConnState;
import javax.microedition.ims.core.connection.ConnectionDataProvider;
import javax.microedition.ims.core.connection.ConnectionDataProviderConfigVsDnsImpl;
import javax.microedition.ims.core.connection.GsmLocationServiceDefaultImpl;
import javax.microedition.ims.core.env.*;
import javax.microedition.ims.core.messagerouter.MessageRouterComposite;
import javax.microedition.ims.core.messagerouter.MessageRouterMSRP;
import javax.microedition.ims.core.messagerouter.MessageRouterSIP;
import javax.microedition.ims.core.registry.DefaultStackRegistry;
import javax.microedition.ims.core.xdm.*;
import javax.microedition.ims.dns.DNSResolverDNSJavaImpl;
import javax.microedition.ims.transport.messagerouter.Router;
import java.io.IOException;
import java.util.HashMap;

/**
 * Default implementation IURIListDocument.aidl.
 *
 * @author Andrei Khomushko
 */
public class URIListDocument extends IURIListDocument.Stub {
    private static final String TAG = "URIListDocument";

    private final String etag;
    private final String documentSelector;
    private final String xmlContent;
    private final XDMService xdmServicePeer;

    public URIListDocument(
            final XDMService xdmServicePeer,
            final String etag,
            final String documentSelector,
            final String xmlContent) {

        assert xdmServicePeer != null;
        this.xdmServicePeer = xdmServicePeer;

        assert documentSelector != null;
        this.documentSelector = documentSelector;

        this.etag = etag;
        this.xmlContent = xmlContent;
    }


    public void applyChanges(String xcapDiffDocument) throws RemoteException {
        // TODO Auto-generated method stub

    }


    public String syncDocumentChanges(IXCAPRequest request,
                                      IExceptionHolder exceptionHolder) throws RemoteException {

        String etag = null;

        XDMRequest xcapRequest = Utils.createXDMRequest(request);

        try {
            XDMResponse response = xdmServicePeer.sendXCAPRequest(xcapRequest);
            etag = response.getEtag();
            //retrieve actual xml and update cache
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        } catch (SAXException e) {
            Log.e(TAG, e.getMessage(), e);

            //suppress warning here bacause we just create wrapper around exception and pass it to upper level
            //noinspection ThrowableInstanceNeverThrown
            exceptionHolder.setParcelableException(Utils.createIXCAPException(new IOException(e.getMessage())));
        }

        return etag;
    }


    public String toString() {
        return "URIListDocument [documentSelector=" + documentSelector
                + ", etag=" + etag + ", xmlContent=" + xmlContent + "]";
    }

    public static void main(String[] args) throws IMSStackException, SAXException, XCAPException, AkaException {
        /*
        final Configuration configuration = MockConfBuilderRegistry.AlternativeServer._79262948587.build();
        final ConnectionManager connManager = StackHelper.newMockConnectionManager(ConnState.CONNECTED);
        final DefaultStackRegistry stackRegistry = new DefaultStackRegistry(MockStackRegistryHelper.COMMON_REGISTRY);

        ConnectionDataProvider connDataProvider = new ConnectionDataProviderConfigVsDnsImpl(
                configuration,
                new DNSResolverDNSJavaImpl()
        );
        connDataProvider.refresh();

        RepetitiousTaskManager repetitiousTaskManager = new RepetitiousTaskManager(new DefaultScheduledService());
        
        final Router<IMSMessage> messageRouter =
                new MessageRouterComposite.Builder(configuration, connDataProvider).
                        addRouter(new MessageRouterMSRP()).
                        addRouter(new MessageRouterSIP(configuration, connDataProvider, repetitiousTaskManager)).
                        build();

        Environment env = new EnvironmentDefaultImpl.Builder()
                .connectionManager(connManager)
                .gsmLocationService(new GsmLocationServiceDefaultImpl())
                .hardwareInfo(new HardwareInfoDefaultImpl())
                .build();


        final IMSStack<IMSMessage> imsStack = StackHelper.newIMSSipStack(
                new DefaultStackContext.Builder().
                        configuration(configuration).router(messageRouter).
                        router(messageRouter).
                        environment(env).
                        stackRegistry(stackRegistry).
                        akaAuthProvider(new AKAAuthProviderMockImpl()).
                        repetitiousTaskManager(repetitiousTaskManager).
                        build()
        );

        final HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", MimeType.APP_RESOURCE_LISTS_XML.stringValue());
        headers.put("ETag", "16/09/2010 06:50:39.102");

        final String body = testString;

        final XDMResponse xdmResponse = imsStack.getXDMService().sendXCAPRequest(
                new XDMRequestImpl(
                        HttpMethod.PUT,
                        null,
                        headers,
                        body
                )
        );

        System.out.println("" + xdmResponse);
        */
    }

    private static final String testString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            " <rl:resource-lists xmlns:rl=\"urn:ietf:params:xml:ns:resource-lists\">\n" +
            " <rl:list name=\"yy\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:list>\n" +
            " <rl:list name=\"oma_buddylist\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " <rl:entry uri=\"sip:79262948580@multifon.ru\">\n" +
            " <rl:display-name>Liu jian</rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79262056390@multifon.ru\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79262948585@multifon.ru\">\n" +
            " <rl:display-name>xiong     liu</rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79222120948@multifon.ru\">\n" +
            " <rl:display-name>Valiksh</rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79262964291@multifon.ru\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79262951057@multifon.ru\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79265732241@multifon.ru\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79262948588@multifon.ru\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79262948589@multifon.ru\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79243942835@multifon.ru\">\n" +
            " <rl:display-name>Chat</rl:display-name>\n" +
            " </rl:entry>\n" +
            " <rl:entry uri=\"sip:79258450001@multifon.ru\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:entry>\n" +
            " </rl:list>\n" +
            " <rl:list name=\"yyy\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:list>\n" +
            " <rl:list name=\"generallist.xml\">\n" +
            " <rl:display-name></rl:display-name>\n" +
            " </rl:list>\n" +
            " <rl:list name=\"newgroup\">\n" +
            " <rl:display-name>newgroup</rl:display-name>\n" +
            " </rl:list>\n" +
            " </rl:resource-lists>";
}
