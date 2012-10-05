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
package javax.microedition.ims.android;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.microedition.ims.android.core.CoreServiceImpl;
import javax.microedition.ims.android.core.ICoreService;
import javax.microedition.ims.android.msrp.IIMService;
import javax.microedition.ims.android.msrp.IMServiceImpl;
import javax.microedition.ims.android.presence.IPresenceService;
import javax.microedition.ims.android.presence.PresenceServiceImpl;
import javax.microedition.ims.android.xdm.IXDMService;
import javax.microedition.ims.android.xdm.XDMServiceImpl;
import javax.microedition.ims.common.DefaultTimeoutUnit;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.common.TimeoutUnit;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.core.CertificateException;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.ClientIdentityImpl;
import javax.microedition.ims.core.ConfigurationException;
import javax.microedition.ims.core.DnsLookupException;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.ReasonCode;
import javax.microedition.ims.core.RegistrationException;
import javax.microedition.ims.core.registry.StackRegistryEditor;
import javax.microedition.ims.core.connection.ConnectionDataProviderConfigVsDnsImpl;
import javax.microedition.ims.core.sipservice.register.RegResult;
import javax.microedition.ims.core.sipservice.register.RegisterService;
import javax.microedition.ims.dns.DNSException;

public class ConnectorBinder extends IConnector.Stub {
    private static final long REGISTRATION_TIMEOUT = 20000L;

    private static final String TAG = "ConnectorBinder";

    private final IMSStack<IMSMessage> imsStack;
    private final ConnectionDataProviderConfigVsDnsImpl connectionDataProvider;
    private List<DeathRecipient> mRecipientList = new ArrayList<DeathRecipient>();
    private CountDownLatch releaseLatch = null;

    ConnectorBinder(final IMSStack<IMSMessage> imsStackInstance, final ConnectionDataProviderConfigVsDnsImpl connectionDataProvider,
        final CountDownLatch latch) {
        assert imsStackInstance != null;
        this.imsStack = imsStackInstance;
        this.connectionDataProvider = connectionDataProvider;
        this.releaseLatch = latch;
    }

    private void addPresenceServceInDeathRecipient(IPresenceService presenceService) {
        for (DeathRecipient item : mRecipientList) {
            item.addPresenceService(presenceService);
        }
    }
    private class DeathRecipient implements IBinder.DeathRecipient {
        IBinder mBinder = null;
        ICoreService mCoreService = null;
        IPresenceService mPresenceService = null;
        CoreServiceImpl.CoreServiceStateListener mCoreListener = null;

        DeathRecipient(IBinder binder, ICoreService coreService) {
            super();
            mBinder = binder;
            mCoreService = coreService;
            Logger.log(TAG, "DeathRecipient: create "+this.toString());

            mCoreListener = new CoreServiceImpl.CoreServiceStateListener() {
                public void onCoreServiceClosed() {
                    Logger.log(TAG, "DeathRecipient: onCoreServiceClosed()");
                    clean();
                    checkReleaseSignal();
                }
            };
            ((CoreServiceImpl)mCoreService).addCoreServiceStateListener(mCoreListener);
            try {
                mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }
        public void binderDied() {
            Logger.log(TAG, "binderDied!"+this.toString());
            clean();
            try {
                String appId = mCoreService.getAppId();
                if (mPresenceService != null) {
                    mPresenceService.close();
                }
                mCoreService.close();

                // do Configuration.close() and ConnectionState.close()
                StackRegistryEditor stackRegistry = (StackRegistryEditor)imsStack.getContext().getStackRegistry();
                stackRegistry.dropClientData(appId);
                stackRegistry.dropCommonData(appId);
                imsStack.getRegisterService().refreshRegistration();
            } catch (RemoteException e) {
                Logger.log(TAG, "DeathRecipient: binderDied Exception: "+e);
            }
            checkReleaseSignal();
        }
        public void addPresenceService(IPresenceService presenceService) {
            try {
                if (mCoreService.getAppId().equals(presenceService.getAppId()))
                    mPresenceService = presenceService;
            } catch (RemoteException e) {
                Logger.log(TAG, "DeathRecipient: addPresenceService Exception: "+e);
            }
        }
        public String toString() {
            return String.format("DeathRecipient: mBinder = %s, mCoreService = %s, mPresenceService = %s",
                        mBinder, mCoreService, mPresenceService);
        }
        private void clean() {
            Logger.log(TAG, "DeathRecipient: clean() "+this.toString());
            ((CoreServiceImpl)mCoreService).removeCoreServiceStateListener(mCoreListener);
            mBinder.unlinkToDeath(this, 0);
            mRecipientList.remove(this);
        }
        private void checkReleaseSignal() {
            if (mRecipientList.size() == 0) {
                releaseLatch.countDown();
                Logger.log(TAG, "DeathRecipient countDown "+releaseLatch.getCount());
            }
        }
    }

    @Override
    public ICoreService openCoreService(final String uri, final IBinder binder, final IExceptionHolder exceptionHolder)
            throws RemoteException {
        ICoreService coreService = null;
        try {
            Logger.log(TAG, "openCoreService# uri = " + uri);
            coreService = null;

            if (checkIsDebugMode(uri)) {
                //coreService = createDebugCoreService(uri);
            } else {
                try {
                    coreService = createRealCoreService(uri);
                    DeathRecipient recipient = new DeathRecipient(binder, coreService);
                    mRecipientList.add(recipient);
                } catch (DnsLookupException e) {
                    Logger.log(TAG, "openCoreService# e = " + e.getMessage());
                    //exceptionHolder.setParcelableException(new IError(IError.ERROR_DNS_LOOKUP, e
                    //        .getMessage()));
                    final IError iError = new IError(
                            ErrorsUtils.toIErrorCode(ReasonCode.CONNECTION_ERROR), e.getMessage(),
                            ReasonCode.CONNECTION_ERROR.getErrCodeString());
                    exceptionHolder.setParcelableException(iError);
                } catch (ConfigurationException e) {
                    Logger.log(TAG, "openCoreService# e = " + e.getMessage());
                    exceptionHolder.setParcelableException(new IError(
                            IError.ERROR_STACK_CONFIGURATION, e.getMessage()));
                } catch (RegistrationException e) {
                    Logger.log(TAG, "openCoreService# RegistrationException");
                    Logger.log(TAG, "openCoreService# e = " + e.getMessage());
                    exceptionHolder.setParcelableException(new IError(e.getResponseCode(), e
                            .getReasonPhrase(), e.getReasonData()));
                } catch (CertificateException e) {
                    Logger.log(TAG, "openCoreService# e = " + e.getMessage());

                    final IError iError = new IError(
                            ErrorsUtils.toIErrorCode(ReasonCode.CERT_NOT_VALID), e.getMessage(),
                            ReasonCode.CERT_NOT_VALID.getErrCodeString());

                    Logger.log(TAG, "openCoreService#iError = " + iError);

                    exceptionHolder.setParcelableException(iError);
                } catch (IMSStackException e) {
                    Logger.log(TAG, "openCoreService# e = " + e);
                    if (e.getThrowableCause() != null
                            && e.getThrowableCause() instanceof DNSException) {
                        DNSException dnsException = (DNSException)e.getThrowableCause();

                        final IError iError = new IError(ErrorsUtils.toIErrorCode(dnsException.getCode()),
                                dnsException.getDescription(), dnsException.getCode()
                                        .getErrCodeString());

                        Logger.log(TAG, "openCoreService#iError = " + iError);

                        exceptionHolder.setParcelableException(iError);
                    } else {
                        exceptionHolder.setParcelableException(new IError(IError.ERROR_UNKNOWN, e
                                .toString(), ""));
                    }
                }
            }
        } catch (Error e) {
            Logger.log(Logger.Tag.WARNING, "Core service creation error " + e);
            e.printStackTrace(); // To change body of catch statement use File |
                                 // Settings | File Templates.
            throw e;
        }
        return coreService;
    }

    @Override
    public IXDMService openXDMService(String uri) throws RemoteException {
        final IXDMService xdmService;

        // UserInfo regUserInfo =
        // stackInstance.getContext().getConfig().getRegistrationName();
        String xuiName = imsStack.getContext().getConfig().getXDMConfig().getXuiName();
        UserInfo xdmUser = UserInfo.valueOf(xuiName);
        if ("reguser".equals(xdmUser.getName())) {
            Logger.log(TAG, "use reguser identity for XDMService");
            xdmUser = imsStack.getContext().getRegistrationIdentity().getUserInfo();
        }

        final ClientIdentity callingParty = ClientIdentityImpl.Creator.createFromUriAndUser(uri,
                xdmUser);

        xdmService = new XDMServiceImpl(callingParty, imsStack.getXDMService(), imsStack
                .getContext().getConfig().getXDMConfig(), /*imsStack.getContext().getDialogStorage(),*/
                imsStack.getSubscribeService());

        return xdmService;
    }

    @Override
    public IPresenceService openPresenceService(String uri) throws RemoteException {
        final IPresenceService presenceService;

        //UserInfo regUserInfo = imsStack.getContext().getConfig().getRegistrationName();
        //final ClientIdentity callingParty = ClientIdentityImpl.Creator.createFromUriAndUser(uri,
        //        regUserInfo);
        final ClientIdentity regParty = imsStack.getContext().getRegistrationIdentity();
        final ClientIdentity callingParty = ClientIdentityImpl.Creator.createFromUriAndUser(uri,
                regParty.getUserInfo());
        
        presenceService = new PresenceServiceImpl(callingParty, imsStack.getContext()
                .getDialogStorage(), imsStack.getPublishService(), imsStack.getSubscribeService(),
                imsStack.getContext());

        addPresenceServceInDeathRecipient(presenceService);
        return presenceService;
    }

    @Override
    public IIMService openIMService(String name) throws RemoteException {
        Logger.log(TAG, "openIMService# clientId = " + name);
        final IIMService imService;

        //UserInfo regUserInfo = imsStack.getContext().getConfig().getRegistrationName();
        //final ClientIdentity callingParty = ClientIdentityImpl.Creator.createFromUriAndUser(name,
        //        regUserInfo);
        final ClientIdentity regParty = imsStack.getContext().getRegistrationIdentity();
        final ClientIdentity callingParty = ClientIdentityImpl.Creator.createFromUriAndUser(name,
                regParty.getUserInfo());
        
        imService = new IMServiceImpl(callingParty, imsStack);

        return imService;
    }

    /** imscore://my.app.identity;android1005;debug=true,mode=uac,port=8082 */
/*    private ICoreService createDebugCoreService(final String uri) {
        final boolean isUAS = uri.contains("uas");
        final int localPort = Integer.parseInt(uri.replaceAll(".*port=", ""));
        Logger.log(TAG, "createDebugCoreService#port = " + localPort + " uas = " + isUAS);

        final Configuration configSnapshot = new BaseConfiguration.ConfigurationBuilder(
                imsStackHolder.get().getContext().getConfig()).buildLocalPort(localPort).build();
        final StackRegistry stackRegistry = imsStackHolder.get().getContext().getStackRegistry();
        final ConnectionManager connectionManager = imsStackHolder.get().getContext()
                .getEnvironment().getConnectionManager();

        final IMSStack<IMSMessage> imsStackInstance = instantiateStack(getApplicationContext(),
                configSnapshot, connectionManager, stackRegistry);
        // imsStacks.add(imsStackInstance);

        UserInfo regUserInfo = imsStackInstance.getContext().getConfig().getRegistrationName();
        final ClientIdentity callingParty = ClientIdentityImpl.Creator.createFromUriAndUser(uri,
                regUserInfo);

        if (isUAS) {
            try {
                Logger.log("ConnectorService", String.format(
                        "createDebugCoreService#port = %s, protocol = %s", localPort, Protocol.TCP));
                imsStackInstance.getContext().getTransportIO()
                        .startUAS(localPort, Protocol.TCP, IMSEntityType.SIP);
            } catch (UASInstantiationException e) {
                Logger.log(TAG, e.getMessage(), e);
            }
        }

        boolean forceSrtp = isForceSrtp(imsStackInstance, imsStackInstance.getRegisterService());
        Logger.log(TAG, "createRealCoreService#forceSrtp = " + forceSrtp);

        final CoreServiceImpl coreService = new CoreServiceImpl(callingParty, imsStackInstance,
                forceSrtp);

        coreService.addCoreServiceStateListener(new CoreServiceImpl.CoreServiceStateListener() {

            public void onCoreServiceClosed() {
                if (isUAS) {
                    imsStackInstance.getContext().getTransportIO()
                            .shutdownUAS(localPort, Protocol.TCP, IMSEntityType.SIP);
                }
                finalizeStack(imsStackInstance);
                // imsStacks.remove(imsStackInstance);
                coreService.removeCoreServiceStateListener(this);
            }
        });

        return coreService;
    }*/

    private ICoreService createRealCoreService(final String uri) throws ConfigurationException,
            RegistrationException, IMSStackException {
        final ICoreService coreService;

        //TODO review
        Logger.log(TAG, "*** ConnectorBinder.createRealCoreService#before - ConnectionDataProviderConfigVsDnsImpl.obtainSecurityInfo()");
        connectionDataProvider.refresh();
        Logger.log(TAG, "*** ConnectorBinder.createRealCoreService#after - ConnectionDataProviderConfigVsDnsImpl.obtainSecurityInfo()");

        RegResult regResult = null;
        try {
            TimeoutUnit regTimeout = new DefaultTimeoutUnit(REGISTRATION_TIMEOUT, TimeUnit.MILLISECONDS);
            RegisterService registerService = imsStack.getRegisterService();
            regResult = registerService.register(regTimeout);
        } catch (RuntimeException e) {
            Logger.log("ConnectorBinder", "createRealCoreService#e.message = " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        Logger.log(Logger.Tag.WARNING, "RegResult: " + regResult);
        if ((regResult == null) || !regResult.isSuccessful()) {
            int statusCode = regResult.getStatusCode() == null? 0: regResult.getStatusCode();
            throw new RegistrationException(statusCode, regResult.getReasonPhrase(),
                    regResult.getReasonData(), regResult.byTimeout());
        }

        boolean forceSrtp = isForceSrtp(imsStack);
        Logger.log(TAG, "createRealCoreService#forceSrtp = " + forceSrtp);

        //UserInfo regUserInfo = imsStack.getContext().getConfig().getRegistrationName();
        //final ClientIdentity callingParty = ClientIdentityImpl.Creator.createFromUriAndUser(uri,
        //        regUserInfo);
        final ClientIdentity regParty = imsStack.getContext().getRegistrationIdentity();
        
        final ClientIdentity callingParty = ClientIdentityImpl.Creator.createFromUriAndUser(uri,
                regParty.getUserInfo());

        coreService = new CoreServiceImpl(callingParty, imsStack, forceSrtp);

        return coreService;
    }

    private static boolean isForceSrtp(final IMSStack<IMSMessage> imsStackInstance) {
        //boolean forceSrtp = imsStackInstance.getContext().getConfig().forceSrtp();

        boolean isSelfTls = imsStackInstance.getContext().getConfig().getConnectionType() == Protocol.TLS;

        boolean isRedirectedToTls = isRedirectedToTls(imsStackInstance);

        return /*forceSrtp ||*/ isSelfTls || isRedirectedToTls;
    }

    private static boolean isRedirectedToTls(final IMSStack<IMSMessage> imsStackInstance) {
        Protocol configConnType = imsStackInstance.getContext().getConfig().getConnectionType();
        Protocol actualConnType = imsStackInstance.getContext().getConnectionType();
        return actualConnType == Protocol.TLS && configConnType != Protocol.TLS;
    }

    private static boolean checkIsDebugMode(String uri) {
        return uri.contains("debug=true");
    }
}
