
package javax.microedition.ims.android;

import android.os.RemoteException;
import android.util.Log;

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
import javax.microedition.ims.core.connection.ConnectionDataProviderConfigVsDnsImpl;
import javax.microedition.ims.core.sipservice.register.RegResult;
import javax.microedition.ims.core.sipservice.register.RegisterService;
import javax.microedition.ims.dns.DNSException;

public class ConnectorBinder extends IConnector.Stub {
    private static final long REGISTRATION_TIMEOUT = 20000L;

    private static final String TAG = "ConnectorBinder";

    private final IMSStack<IMSMessage> imsStack;
    private final ConnectionDataProviderConfigVsDnsImpl connectionDataProvider;

    ConnectorBinder(final IMSStack<IMSMessage> imsStackInstance, final ConnectionDataProviderConfigVsDnsImpl connectionDataProvider) {
        assert imsStackInstance != null;
        this.imsStack = imsStackInstance;
        this.connectionDataProvider = connectionDataProvider;
    }

    @Override
    public ICoreService openCoreService(final String uri, final IExceptionHolder exceptionHolder)
            throws RemoteException {
        ICoreService coreService = null;
        try {
            Log.i(TAG, "openCoreService# uri = " + uri);
            coreService = null;

            if (checkIsDebugMode(uri)) {
                //coreService = createDebugCoreService(uri);
            } else {
                try {
                    coreService = createRealCoreService(uri);
                } catch (DnsLookupException e) {
                    Log.e(TAG, "openCoreService# e = " + e.getMessage());
                    //exceptionHolder.setParcelableException(new IError(IError.ERROR_DNS_LOOKUP, e
                    //        .getMessage()));
                    final IError iError = new IError(
                            ErrorsUtils.toIErrorCode(ReasonCode.CONNECTION_ERROR), e.getMessage(),
                            ReasonCode.CONNECTION_ERROR.getErrCodeString());
                    exceptionHolder.setParcelableException(iError);
                } catch (ConfigurationException e) {
                    Log.e(TAG, "openCoreService# e = " + e.getMessage());
                    exceptionHolder.setParcelableException(new IError(
                            IError.ERROR_STACK_CONFIGURATION, e.getMessage()));
                } catch (RegistrationException e) {
                    Log.e(TAG, "openCoreService# RegistrationException");
                    Log.e(TAG, "openCoreService# e = " + e.getMessage());
                    exceptionHolder.setParcelableException(new IError(e.getResponseCode(), e
                            .getReasonPhrase(), e.getReasonData()));
                } catch (CertificateException e) {
                    Log.e(TAG, "openCoreService# e = " + e.getMessage());

                    final IError iError = new IError(
                            ErrorsUtils.toIErrorCode(ReasonCode.CERT_NOT_VALID), e.getMessage(),
                            ReasonCode.CERT_NOT_VALID.getErrCodeString());

                    Log.e(TAG, "openCoreService#iError = " + iError);

                    exceptionHolder.setParcelableException(iError);
                } catch (IMSStackException e) {
                    Log.e(TAG, "openCoreService# e = " + e);
                    if (e.getThrowableCause() != null
                            && e.getThrowableCause() instanceof DNSException) {
                        DNSException dnsException = (DNSException)e.getThrowableCause();

                        final IError iError = new IError(ErrorsUtils.toIErrorCode(dnsException.getCode()),
                                dnsException.getDescription(), dnsException.getCode()
                                        .getErrCodeString());

                        Log.e(TAG, "openCoreService#iError = " + iError);

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

        return presenceService;
    }

    @Override
    public IIMService openIMService(String name) throws RemoteException {
        Log.i(TAG, "openIMService# clientId = " + name);
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
        Log.i(TAG, "createDebugCoreService#port = " + localPort + " uas = " + isUAS);

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
                Log.e(TAG, e.getMessage(), e);
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
            Log.e("ConnectorBinder", "createRealCoreService#e.message = " + e.getMessage(), e);
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
