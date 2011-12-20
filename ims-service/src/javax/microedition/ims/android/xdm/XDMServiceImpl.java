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
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.AkaException;
import javax.microedition.ims.core.ClientIdentity;
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
import javax.microedition.ims.core.sipservice.subscribe.SubscribeService;
import javax.microedition.ims.core.xdm.*;
import javax.microedition.ims.core.xdm.data.DocumentBean;
import javax.microedition.ims.core.xdm.data.Identity;
import javax.microedition.ims.dns.DNSResolverDNSJavaImpl;
//import javax.microedition.ims.entrypoint.MockStackRegistryHelper;
//import javax.microedition.ims.entrypoint.config.MockConfBuilderRegistry;
import javax.microedition.ims.transport.messagerouter.Router;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * DMS Service default implementation.
 *
 * @author Andrei Khomushko
 */
public class XDMServiceImpl extends IXDMService.Stub {
    private static final String TAG = "Service - XDMServiceImpl";

    private final XDMService xdmServicePeer;
    private final ClientIdentity localParty;
    private final XDMConfig xdmConfig;

    private final RemoteListenerHolder<IXDMServiceListener> listenerHolder = new RemoteListenerHolder<IXDMServiceListener>(
            IXDMServiceListener.class);

    //private final DialogStorage dialogStorage;

    private final SubscribeService subscribeService;

    public XDMServiceImpl(
            final ClientIdentity callingParty,
            final XDMService xdmService,
            final XDMConfig xdmConfig,
            /*final DialogStorage dialogStorage,*/
            final SubscribeService subscribeService) {

        //this.dialogStorage = dialogStorage;
        this.subscribeService = subscribeService;
        assert callingParty != null;
        this.localParty = callingParty;

        assert xdmService != null;
        this.xdmServicePeer = xdmService;

        assert xdmConfig != null;
        this.xdmConfig = xdmConfig;
    }


    public void close() throws RemoteException {
        listenerHolder.shutdown();
    }


    public String getAppId() throws RemoteException {
        return localParty.getAppID();
    }


    public String getSheme() throws RemoteException {
        return localParty.getSchema();
    }


    public IDocumentSubscriber createDocumentSubscriber(String[] urls,
                                                        IExceptionHolder exceptionHolder) throws RemoteException {

        // TODO if no subscription proxy is available throw exception

        return new DocumentSubscriberImpl(
                urls,
                subscribeService,
                localParty,
                localParty.getUserInfo().toUri()
        );
    }


    public IXCAPResponse sendXCAPRequest(final IXCAPRequest request,
                                         final IExceptionHolder exceptionHolder) throws RemoteException {
        IXCAPResponse retValue = null;

        try {
            XDMResponse xcapResponse = doSendXCAPRequest(request);
            retValue = new IXCAPResponse(xcapResponse.buildContent(),
                    xcapResponse.getEtag(), xcapResponse.getMimeType());
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        } catch (SAXException e) {
            Log.e(TAG, e.getMessage(), e);

            //suppress warning here bacause we just create wrapper around exception and pass it to upper level
            //noinspection ThrowableInstanceNeverThrown
            exceptionHolder.setParcelableException(Utils.createIXCAPException(new IOException(e.getMessage())));
        }

        return retValue;
    }

    private XDMResponse doSendXCAPRequest(final IXCAPRequest request) throws XCAPException, SAXException {
        return xdmServicePeer.sendXCAPRequest(Utils.createXDMRequest(request));

    }


    public IDocumentEntry[] listDocuments(String auid,
                                          IExceptionHolder exceptionHolder) throws RemoteException {
        IDocumentEntry[] retValue = null;

        try {
            final List<DocumentBean> documentBeans = xdmServicePeer
                    .listDocuments(auid);

            final Collection<IDocumentEntry> iDocumentEntries = CollectionsUtils
                    .transform(
                            documentBeans,
                            new CollectionsUtils.Transformer<DocumentBean, IDocumentEntry>() {

                                public IDocumentEntry transform(
                                        DocumentBean documentBean) {
                                    return new IDocumentEntry(documentBean
                                            .getDocumentSelector(),
                                            documentBean.getEtag(),
                                            documentBean.getLastModified(),
                                            documentBean.getSize());
                                }
                            });
            retValue = iDocumentEntries.toArray(new IDocumentEntry[0]);
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        }

        return retValue;
    }


    public IURIListDocument createURIListDocument(IXCAPRequest request,
                                                  IURIListsHolder uriListHolder, IExceptionHolder exceptionHolder)
            throws RemoteException {
        IURIListDocument retValue = null;

        XDMRequest xcapRequest = Utils.createXDMRequest(request);
        try {
            URIListDocumentData xdmDocumentData = xdmServicePeer
                    .retrieveDocument(xcapRequest,
                            XDMDocumentDescriptor.URI_LIST_DOCUMENT_DESCRIPTOR);

            retValue = new URIListDocument(xdmServicePeer, xdmDocumentData
                    .getEtag(), xdmDocumentData.getDocumentSelector(),
                    xdmDocumentData.getXMLContent());

            uriListHolder.setEtag(xdmDocumentData.getEtag());
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        }

        return retValue;
    }


    public void deleteURIListDocument(IXCAPRequest request,
                                      IExceptionHolder exceptionHolder) throws RemoteException {
        // TODO notify document storage
        try {
            doSendXCAPRequest(request);
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        } catch (SAXException e) {
            Log.e(TAG, e.getMessage(), e);

            //suppress warning here bacause we just create wrapper around exception and pass it to upper level
            //noinspection ThrowableInstanceNeverThrown
            exceptionHolder.setParcelableException(Utils.createIXCAPException(new IOException(e.getMessage())));
        }
    }


    public IURIListDocument retrieveURIListDocument(final IXCAPRequest request,
                                                    IURIListsHolder uriListHolder, IExceptionHolder exceptionHolder)
            throws RemoteException {
        IURIListDocument retValue = null;

        XDMRequest xcapRequest = Utils.createXDMRequest(request);
        try {

            URIListDocumentData documentData = xdmServicePeer.retrieveDocument(
                    xcapRequest,
                    XDMDocumentDescriptor.URI_LIST_DOCUMENT_DESCRIPTOR);
            Collection<IURIList> iUriLists = createIURIList(documentData
                    .getUriListData());

            uriListHolder.setIUriLists(iUriLists.toArray(new IURIList[0]));
            uriListHolder.setEtag(documentData.getEtag());

            retValue = new URIListDocument(xdmServicePeer, documentData
                    .getEtag(), documentData.getDocumentSelector(),
                    documentData.getXMLContent());
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        }

        return retValue;
    }

    private static Collection<IURIList> createIURIList(
            Collection<URIListData> listData) {
        return CollectionsUtils.transform(listData,
                new CollectionsUtils.Transformer<URIListData, IURIList>() {

                    public IURIList transform(final URIListData uriList) {
                        final IURIList iUriList = new IURIList(uriList
                                .getListName(), uriList.getDisplayName());
                        for (ListEntryData entryData : uriList
                                .getListEntryData()) {
                            iUriList.getListEntries().add(
                                    new IListEntry(entryData.getType(), entryData.getURI(),
                                            entryData.getDisplayName()));
                        }
                        return iUriList;
                    }
                });
    }

    private static Collection<IPresenceList> createIPresenceList(
            Collection<PresenceListData> listData) {
        return CollectionsUtils
                .transform(
                        listData,
                        new CollectionsUtils.Transformer<PresenceListData, IPresenceList>() {

                            public IPresenceList transform(
                                    final PresenceListData presenceList) {
                                return new IPresenceList(presenceList
                                        .getServiceURI(), presenceList
                                        .getURIListReference());
                            }
                        });
    }


    public IURIListDocument loadURIListDocument(String documentSelector,
                                                String etag, String source, IURIListsHolder uriListHolder,
                                                IExceptionHolder exceptionHolder) throws RemoteException {

        IURIListDocument retValue = null;

        try {
            URIListDocumentData documentData = xdmServicePeer.loadDocument(
                    documentSelector,
                    XDMDocumentDescriptor.URI_LIST_DOCUMENT_DESCRIPTOR, source);
            Collection<IURIList> iUriLists = createIURIList(documentData
                    .getUriListData());
            uriListHolder.setIUriLists(iUriLists.toArray(new IURIList[0]));
            uriListHolder.setEtag(documentData.getEtag());

            retValue = new URIListDocument(xdmServicePeer, documentData
                    .getEtag(), documentData.getDocumentSelector(),
                    documentData.getXMLContent());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        }

        return retValue;
    }

/*    private void notifyServiceClosed(IReasonInfo reasonInfo) {
        try {
            listenerHolder.getNotifier().serviceClosed(reasonInfo);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
*/

    public void addXDMServiceListener(IXDMServiceListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }


    public void removeXDMServiceListener(IXDMServiceListener listener)
            throws RemoteException {
        listenerHolder.removeListener(listener);
    }


    public IPresenceListDocument createPresenceListDocument(
            IXCAPRequest request, IPresenceListsHolder presenceListsHolder,
            IExceptionHolder exceptionHolder) throws RemoteException {
        IPresenceListDocument retValue = null;

        XDMRequest xcapRequest = Utils.createXDMRequest(request);
        try {
            PresenceListDocumentData xdmDocumentData = xdmServicePeer
                    .retrieveDocument(
                            xcapRequest,
                            XDMDocumentDescriptor.PRESENCE_LIST_DOCUMENT_DESCRIPTOR);

            retValue = new PresenceListDocument(xdmServicePeer, xdmDocumentData
                    .getEtag(), xdmDocumentData.getDocumentSelector(),
                    xdmDocumentData.getXMLContent());

            presenceListsHolder.setEtag(xdmDocumentData.getEtag());
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        }

        return retValue;
    }


    public void deletePresenceListDocument(IXCAPRequest request,
                                           IExceptionHolder exceptionHolder) throws RemoteException {
        try {
            doSendXCAPRequest(request);
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        } catch (SAXException e) {
            Log.e(TAG, e.getMessage(), e);

            //suppress warning here bacause we just create wrapper around exception and pass it to upper level
            //noinspection ThrowableInstanceNeverThrown
            exceptionHolder.setParcelableException(Utils.createIXCAPException(new IOException(e.getMessage())));
        }
    }


    public IPresenceListDocument retrievePresenceListDocument(
            IXCAPRequest request, IPresenceListsHolder presenceListsHolder,
            IExceptionHolder exceptionHolder) throws RemoteException {
        IPresenceListDocument retValue = null;

        XDMRequest xcapRequest = Utils.createXDMRequest(request);
        try {

            PresenceListDocumentData documentData = xdmServicePeer
                    .retrieveDocument(
                            xcapRequest,
                            XDMDocumentDescriptor.PRESENCE_LIST_DOCUMENT_DESCRIPTOR);
            Collection<IPresenceList> iPresenceLists = createIPresenceList(documentData
                    .getUriListData());

            presenceListsHolder.setIPresenceLists(iPresenceLists
                    .toArray(new IPresenceList[0]));
            presenceListsHolder.setEtag(documentData.getEtag());

            retValue = new PresenceListDocument(xdmServicePeer, documentData
                    .getEtag(), documentData.getDocumentSelector(),
                    documentData.getXMLContent());
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        }

        return retValue;
    }


    public IPresenceListDocument loadPresenceListDocument(
            String documentSelector, String etag, String source,
            IPresenceListsHolder presenceListsHolder,
            IExceptionHolder exceptionHolder) throws RemoteException {

        IPresenceListDocument retValue = null;

        try {
            PresenceListDocumentData documentData = xdmServicePeer
                    .loadDocument(
                            documentSelector,
                            XDMDocumentDescriptor.PRESENCE_LIST_DOCUMENT_DESCRIPTOR,
                            source);
            Collection<IPresenceList> iPresenceLists = createIPresenceList(documentData
                    .getUriListData());
            presenceListsHolder.setIPresenceLists(iPresenceLists
                    .toArray(new IPresenceList[0]));
            presenceListsHolder.setEtag(documentData.getEtag());

            retValue = new PresenceListDocument(xdmServicePeer, documentData
                    .getEtag(), documentData.getDocumentSelector(),
                    documentData.getXMLContent());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils
                    .createIXCAPException(e));
        }

        return retValue;
    }


    public String getXCAPRoot() throws RemoteException {
        return xdmConfig.getXcapRoot();
    }

    public boolean isXdmSendFullDoc() throws RemoteException {
        return xdmConfig.isSendFullDoc();
    }


    public String getXUI() throws RemoteException {
        return xdmConfig.getXuiName();
    }


    public void deletePresenceAuthorizationDocument(IXCAPRequest request,
                                                    IExceptionHolder exceptionHolder) throws RemoteException {
        Log.i(TAG, "deletePresenceAuthorizationDocument#started");
        try {
            doSendXCAPRequest(request);
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        } catch (SAXException e) {
            Log.e(TAG, e.getMessage(), e);

            //suppress warning here bacause we just create wrapper around exception and pass it to upper level
            //noinspection ThrowableInstanceNeverThrown
            exceptionHolder.setParcelableException(Utils.createIXCAPException(new IOException(e.getMessage())));
        }
        Log.i(TAG, "deletePresenceAuthorizationDocument#finished");
    }

    public IPresenceAuthorizationDocument createPresenceAuthorizationDocument(
            IXCAPRequest request, IPresenceAuthorizationRulesHolder presenceAuthorizationRulesHolder,
            IExceptionHolder exceptionHolder) throws RemoteException {
        Log.i(TAG, "createPresenceAuthorizationDocument#started");
        IPresenceAuthorizationDocument retValue = null;

        XDMRequest xcapRequest = Utils.createXDMRequest(request);
        try {
            PresenceAuthorizationDocumentData xdmDocumentData = xdmServicePeer.retrieveDocument(
                    xcapRequest,
                    XDMDocumentDescriptor.PRESENCE_AUTHORIZATION_DOCUMENT_DESCRIPTOR);

            retValue = new PresenceAuthorizationDocument(
                    xdmServicePeer,
                    xdmDocumentData.getEtag(),
                    xdmDocumentData.getDocumentSelector(),
                    xdmDocumentData.getXMLContent()
            );

            presenceAuthorizationRulesHolder.setEtag(xdmDocumentData.getEtag());
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        }

        Log.i(TAG, "createPresenceAuthorizationDocument#finished");

        return retValue;
    }


    public IPresenceAuthorizationDocument retrievePresenceAuthorizationDocument(
            IXCAPRequest request,
            IPresenceAuthorizationRulesHolder presenceAuthorizationRulesHolder,
            IExceptionHolder exceptionHolder) throws RemoteException {

        IPresenceAuthorizationDocument retValue = null;

        XDMRequest xcapRequest = Utils.createXDMRequest(request);

        try {

            PresenceAuthorizationDocumentData documentData = xdmServicePeer
                    .retrieveDocument(
                            xcapRequest,
                            XDMDocumentDescriptor.PRESENCE_AUTHORIZATION_DOCUMENT_DESCRIPTOR);
            Collection<PresenceAuthorizationRuleData> ruleData = documentData.getRuleData();
            //Log.d(TAG, "ruleData = " + ruleData);
            Collection<IPresenceAuthorizationRule> iRules = createIPresenceAuthorizationRule(ruleData);
            //Log.d(TAG, "iRules = " + iRules);

            presenceAuthorizationRulesHolder.setiPresenceAuthorizationRules(
                    iRules.toArray(new IPresenceAuthorizationRule[0])
            );

            presenceAuthorizationRulesHolder.setEtag(documentData.getEtag());

            retValue = new PresenceAuthorizationDocument(
                    xdmServicePeer,
                    documentData.getEtag(),
                    documentData.getDocumentSelector(),
                    documentData.getXMLContent()
            );
        } catch (XCAPException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        }

        return retValue;
    }

    private static Collection<IPresenceAuthorizationRule> createIPresenceAuthorizationRule(
            Collection<PresenceAuthorizationRuleData> listData) {
        return CollectionsUtils.transform(
                listData,
                new CollectionsUtils.Transformer<PresenceAuthorizationRuleData, IPresenceAuthorizationRule>() {
                    public IPresenceAuthorizationRule transform(final PresenceAuthorizationRuleData ruleData) {
                        return new IPresenceAuthorizationRule(
                                ruleData.getRuleId(),
                                ruleData.getAction(),
                                ruleData.getURIListReference(),
                                createIIdentity(ruleData.getIdentity())
                        );
                    }
                }
        );
    }

    private static IIdentity createIIdentity(Identity identity) {
        return identity != null ? new IIdentity(identity.getAllowedDomain(),
                identity.getIdentityList(),
                identity.getIdentityType().getIdentityValue()) : null;
    }

    public static void main(String[] args) throws IMSStackException, AkaException { /*
        final ConnectionManager connManager = StackHelper.newMockConnectionManager(ConnState.CONNECTED);
        final Configuration configuration = MockConfBuilderRegistry.AlternativeServer._79262948587.build();
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


        try {
            final IMSStack<IMSMessage> imsStack = StackHelper.newIMSSipStack(
                    new DefaultStackContext.Builder().
                            configuration(configuration).router(messageRouter).
                            router(messageRouter).
//                            environment(EnvironmentDefaultImpl.Builder.build(ConnState.CONNECTED)).
                            environment(env).
                            stackRegistry(stackRegistry).
                            akaAuthProvider(new AKAAuthProviderMockImpl()).
                            repetitiousTaskManager(repetitiousTaskManager).
                            build()
            );


            final XDMRequest xdmRequest = new XDMRequestImpl(
                    HttpMethod.GET,
                    null,
                    new HashMap<String, String>(),
                    ""
            );

            final PresenceAuthorizationDocumentData authorizationDocumentData =
                    imsStack.getXDMService().retrieveDocument(
                            xdmRequest,
                            XDMDocumentDescriptor.PRESENCE_AUTHORIZATION_DOCUMENT_DESCRIPTOR
                    );

            System.out.println("" + authorizationDocumentData);
        } catch (IMSStackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XCAPException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    */}

}
