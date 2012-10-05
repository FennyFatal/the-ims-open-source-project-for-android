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

package javax.microedition.ims.core.sipservice.register;

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.NetUtils;
import javax.microedition.ims.core.CertificateException;
import javax.microedition.ims.core.DnsLookupException;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.StackListener;
import javax.microedition.ims.core.StackOuterError;
import javax.microedition.ims.core.connection.*;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.AbstractService;
import javax.microedition.ims.core.sipservice.RefreshHelper;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.core.transaction.client.ClientTransaction;
import javax.microedition.ims.core.transaction.client.RegisterTransaction;
import javax.microedition.ims.messages.parser.ParserUtils;
import javax.microedition.ims.messages.utils.MessageData;
import javax.microedition.ims.messages.utils.MessageUtils;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.common.Param;
import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.*;
import javax.microedition.ims.transport.ChannelIOException;
import javax.microedition.ims.transport.ChannelIOException.Reason;
import javax.microedition.ims.transport.messagerouter.Route;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 10-Dec-2009
 * Time: 15:40:16
 */
public class RegisterServiceImpl extends AbstractService implements
        RegisterService, ConnStateListener,
        IPChangeListener, NetTypeChangeListener,
        TransactionBuildUpListener<BaseSipMessage>,
        StackListener {
    
    private static final String LOG_TAG = "RegisterServiceImpl";

    private static final String REGISTRAR_SERVER = "sip:RegistrarServer@somenetwork.net";
    private static final String REGISTER_UNIQUE_TAG = "SIP_REGISTER";

    private final AtomicBoolean done = new AtomicBoolean(false);
    private final AtomicBoolean registered = new AtomicBoolean(false);
    private final AtomicBoolean keepConnected = new AtomicBoolean(false);
    private final AtomicReference<RegResult> regResult = new AtomicReference<RegResult>(null);
    private final AtomicReference<RegResult> unregResult = new AtomicReference<RegResult>(null);
    private final AtomicReference<String> regDialogTag = new AtomicReference<String>(generateUniqueTag());

    private final ListenerHolder<RegistrationListener> registrationListenerHolder =
            new ListenerHolder<RegistrationListener>(RegistrationListener.class);
    private final ListenerHolder<RegistrationRedirectListener> registrationRedirectListenerHolder =
            new ListenerHolder<RegistrationRedirectListener>(RegistrationRedirectListener.class);

    private final long registrationExpirationSeconds;
    private final AtomicReference<RegistrationInfo> registrationInfo = new AtomicReference<RegistrationInfo>(null);

    private final AtomicReference<RedirectEvent> lastRedirect = new AtomicReference<RedirectEvent>(null);
    private final Collection<RedirectEvent> redirectEvents =
            Collections.synchronizedCollection(new ArrayList<RedirectEvent>());

    private static final DefaultTimeoutUnit REGISTRATION_TIMEOUT = new DefaultTimeoutUnit(32l, TimeUnit.SECONDS);
    private static final String REFRESH_NAME = "Register";

    private Route failureRoute;

    private Set<String> registrationProxies = new HashSet<String>();
    public static final int REDIRECT_ROUNDTRIPS_ALLOWED = 3;
    private AtomicReference<ChannelIOException> certificateValidationError = new AtomicReference<ChannelIOException>();

    public RegisterServiceImpl(
            final StackContext stackContext,
            final TransactionManager transactionManager,
            final long registrationExpirationSeconds) {

        super(stackContext, transactionManager);
        this.registrationExpirationSeconds = registrationExpirationSeconds;

        getTransactionManager().addListener(new TransactionBuildUpListener<BaseSipMessage>() {
            public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> transactionBuildUpEvent) {
                RegisterServiceImpl.this.onTransactionCreate(transactionBuildUpEvent);
            }
        }, TransactionType.Name.SIP_LOGIN
        );

        getTransactionManager().addListener(new TransactionBuildUpListener<BaseSipMessage>() {
            public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> transactionBuildUpEvent) {
                RegisterServiceImpl.this.onTransactionCreate(transactionBuildUpEvent);
            }
        }, TransactionType.Name.SIP_LOGOUT
        );

        this.registrationInfo.set(RegistrationInfoDefaultImpl.EMPTY_REGISTRATION_INFO);
    }


    public void onTransactionCreate(final TransactionBuildUpEvent<BaseSipMessage> event) {
        Dialog dialog = (Dialog) event.getEntity();

        //if (event.getTransaction() instanceof RegisterTransaction) {
        Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();

        final TransactionType<ClientTransaction, ? extends RegisterTransaction> type = transaction.getTransactionType();
        TransactionListener<BaseSipMessage> listener = obtainNewTransactionListener(dialog, type);

        transaction.addListener(listener);

        transaction.addListener(createRegistrationCompleteListener(type, dialog));
        //}
    }

    private TransactionListener<BaseSipMessage> obtainNewTransactionListener(
            final Dialog dialog,
            final TransactionType<ClientTransaction, ? extends RegisterTransaction> transactionType) {

        TransactionListener<BaseSipMessage> listener;
        if (TransactionType.SIP_LOGOUT == transactionType) {
            listener = new LogoutTransactionListener(dialog);

        }
        else if (TransactionType.SIP_LOGIN == transactionType) {
            listener = new javax.microedition.ims.core.sipservice.RefreshListener(
                    getStackContext().getRepetitiousTaskManager(),
                    REFRESH_NAME,
                    Dialog.ParamKey.REGISTRATION_EXPIRES,
                    dialog,
                    new RefreshHelper.Refresher() {

                        public void refresh(final long timeOutInMillis) {
                            refreshRegistration(new DefaultTimeoutUnit(timeOutInMillis, TimeUnit.MILLISECONDS));
                        }
                    },
                    registrationExpirationSeconds
            );
        }
        else {
            throw new IllegalArgumentException();
        }
        return listener;
    }


    public RegistrationInfo getRegistrationInfo() {
        return registrationInfo.get();
    }

    public void addRegistrationListener(final RegistrationListener listener) {
        registrationListenerHolder.addListener(listener);
    }

    public void removeRegistrationListener(final RegistrationListener listener) {
        registrationListenerHolder.removeListener(listener);
    }

    public void addRegistrationRedirectListener(final RegistrationRedirectListener listener) {
        registrationRedirectListenerHolder.addListener(listener);
    }

    public void removeRegistrationRedirectListener(final RegistrationRedirectListener listener) {
        registrationRedirectListenerHolder.removeListener(listener);
    }

    public RegResult register(final TimeoutUnit timeoutUnit) throws DnsLookupException, CertificateException {
        return doRegister(timeoutUnit);
    }

    public RegResult register() throws DnsLookupException, CertificateException {
        return doRegister(null);
    }

    /**
     * @param timeoutUnit description goes here
     * @return true if unregistered or was registered and then successfully unregistered
     */
    public RegResult unregister(final TimeoutUnit timeoutUnit) {
        RegResult result = null;
        try {
            result = doUnregister(timeoutUnit);
        }
        catch (IMSStackException e) {
            Logger.log(Logger.Tag.WARNING, e.getMessage());
        }
        return result;
    }

    /**
     * @return true if unregistered or was registered and then successfully unregistered
     */
    public RegResult unregister() {
        RegResult result = null;
        try {
            result = doUnregister(null);
        }
        catch (IMSStackException e) {
            Logger.log(Logger.Tag.WARNING, e.getMessage());
        }
        return result;
    }

    private RegResult doRegister(final TimeoutUnit timeoutUnit) throws DnsLookupException, CertificateException {
        RegResult retValue = null;
        keepConnected.set(true);

        if (registered.get()) {
            //if already registered
            refreshRegistration();
        }

        registered.compareAndSet(
                false,
                !registered.get() && (retValue = doRegistration(timeoutUnit, TransactionType.SIP_LOGIN)).isSuccessful()
        );

        if (retValue != null) {
            regResult.set(retValue);
        }

        return retValue == null ? regResult.get() : retValue;
    }

    /**
     * @param timeoutUnit description goes here
     * @return true if unregistered or was registered and then successfully unregistered
     */
    private RegResult doUnregister(final TimeoutUnit timeoutUnit) throws DnsLookupException, CertificateException {
        RegResult retValue = null;
        keepConnected.set(false);
        RefreshHelper.cancelRefresh(getStackContext().getRepetitiousTaskManager(), REFRESH_NAME);
        registered.compareAndSet(
                true,
                registered.get() && !(retValue = doRegistration(timeoutUnit, TransactionType.SIP_LOGOUT)).isSuccessful()
        );

        if (retValue != null) {
            unregResult.set(retValue);
        }

        return retValue == null ? unregResult.get() : retValue;
    }


    public boolean isRegistered() {
        return registered.get();
    }

    public RegResult refreshRegistration(final TimeoutUnit timeoutUnit) {
        RegResult retValue = null;

        log("Starting registration refresh...", "REGISTRATION REFRESH");
        try {
            retValue = doRegistration(timeoutUnit, TransactionType.SIP_LOGIN);
            registered.set(retValue.isSuccessful());
        }
        catch (IMSStackException e) {
            registered.set(false);
        } 

        if (retValue != null) {
            regResult.set(retValue);
        }

        return retValue == null ? regResult.get() : retValue;
    }

    public RegResult refreshRegistration() {
        return refreshRegistration(REGISTRATION_TIMEOUT);
    }

    private RegResult doRegistration(
            final TimeoutUnit timeoutUnit,
            final TransactionType<ClientTransaction, ? extends RegisterTransaction> transactionType
    ) throws DnsLookupException, CertificateException {
        RegResult retValue;

        if (!done.get()) {

            failureRoute = null;
            registrationProxies.clear();

            registrationProxies.add(getStackContext().getConfig().getProxyServer().getAddress());

            final Dialog dlg = extractRegistrationData();

            //democode
            //dlg.putCustomParameter(Dialog.ParamKey.PATH, "<sip:P3.EXAMPLEHOME.COM;lr>,<sip:P1.EXAMPLEVISITED.COM;lr>");

            ClientTransaction transaction, prevTransaction = null;
            int i = REDIRECT_ROUNDTRIPS_ALLOWED;
            do {
                transaction = getTransactionManager().lookUpTransaction(dlg, null, transactionType);
                assert transaction != prevTransaction;
                assert !transaction.isComplete();

                Logger.log("RegisterServiceImpl", "doRegistration#transaction.isComplete() = " + transaction.isComplete());
                runSynchronously(transaction, timeoutUnit);
                retValue = buildRegResult(transaction);

                handleRegistrationResults(retValue, transaction);

                prevTransaction = transaction;
            }
            while (retValue.getRedirectData() != null/*redirectDetected(retValue)*/ &&
                    !redirectEvents.contains(lastRedirect.get()) &&
                    i-- > 0);

            if (!retValue.isSuccessful() && !retValue.byTimeout()) {
                if(failureRoute != null && registrationProxies.contains(failureRoute.getDstHost())) {
                    throw new DnsLookupException("Cann't lookup host: " + failureRoute.getDstHost());
                } else if(certificateValidationError.get() != null) {
                    ChannelIOException exception = certificateValidationError.getAndSet(null);
                    throw new CertificateException(exception.getMessage(), exception.getCause());
                } 
            }

            redirectEvents.clear();
            lastRedirect.set(null);

            updateRegDialog(transactionType, retValue);
            notifyListeners(transactionType, retValue);
        }
        else {
            throw new IllegalStateException("Service already shutdown");
        }

        return retValue;
    }


    private Dialog extractRegistrationData() {
        //String toUri = SIPUtil.toSipURI(getStackContext().getConfig().getRegistrarServer());
        
        final Dialog dlg = getStackContext().getDialogStorage().getDialog(
                getStackContext().getRegistrationIdentity(),
                getStackContext().getRegistrationIdentity().getUserInfo().toUri(),
                regDialogTag.get()
        ); //TODO add registrar
        return dlg;
    }

    private void handleRegistrationResults(final RegResult retValue,
                                           final ClientTransaction transaction) {
        final Response response = getLastResponse(transaction);

        RedirectData redirectData = retValue.getRedirectData();
        if (redirectData != null) {
            String hostName = retValue.getRedirectData().getRedirectAddress().getHostName();
            registrationProxies.add(hostName);
        }

        if (retValue.getRedirectData() != null/*redirectDetected(retValue)*/) {
            handleRegistrationRedirect(response, redirectData);
        }
    }

    private void handleRegistrationRedirect(final Response response, RedirectData redirectData) {
        if (response != null) {
            //compute redirect event from response
            final RedirectEvent redirectEvent = new RedirectEventDefaultImpl(redirectData);

            //proceed only if revent not null (that means we recieve valid response and was able to handle it)
            if (redirectEvent != null) {

                //obtain redirect event from previous step
                final RedirectEvent redirectEventLast = lastRedirect.get();
                //and store it in list previously handled events 
                if (redirectEventLast != null) {
                    redirectEvents.add(redirectEventLast);
                }

                lastRedirect.set(redirectEvent);
                if (!redirectEvents.contains(redirectEvent)) {
                    registrationRedirectListenerHolder.getNotifier().onRegistrationRedirect(redirectEvent);
                }
                else {
                    final String errMsg = "Loop redirect request detected  = " + redirectEvent + " msg " +
                            response.shortDescription();
                    Logger.log(Logger.Tag.WARNING, errMsg);
                }
            }
            else {
                final String errMsg = "Redirect request detected, but can not be processed. Msg = "
                        + response.shortDescription();
                Logger.log(Logger.Tag.WARNING, errMsg);
            }
        }
        else {
            final String errMsg = "Redirect request detected, but can not be processed. Last message is '"
                    + response + "' but must be 301 or 302";
            Logger.log(Logger.Tag.WARNING, errMsg);
        }
    }

    private RedirectData createRedirectDate(final Response lastResponse) {

        RedirectData retValue = null;

        if (lastResponse != null) {
            final MessageData data = MessageUtils.grabMessageData(lastResponse);

            final boolean isRedirectDataAvailable = data.getContactDomain() != null;

            if (isRedirectDataAvailable) {
                Integer port = data.getContactPort();
                if (port == null) {
                    final int defaultPort = getStackContext().getConfig().getProxyServer().getPort();
                    Logger.log(Logger.Tag.WARNING, "port is unknown for redirect. Will be used default one" + defaultPort);
                    port = defaultPort;
                }

                Protocol protocol = data.getContactTransport();
                if (protocol == null) {
                    //protocol = getStackContext().getConfig().getConnectionType();
                    protocol = getStackContext().getConnectionType();
                    final String errMsg = "protocol is unknown for redirect. Will be used default one" + protocol;
                    Logger.log(Logger.Tag.WARNING, errMsg);
                }

                Long expires = data.getContactExpires() == null ? data.getExpires() : data.getContactExpires();

                retValue = new RedirectDataImpl(
                        new InetSocketAddress(data.getContactDomain(), port),
                        protocol,
                        expires,
                        lastResponse
                );
            }
            else {
                assert false : "Corrupted response " + lastResponse.buildContent();
            }
        }

        return retValue;
    }

    private boolean redirectDetected(Response response /*RegResult regResult*/) {
        boolean redirectDetected = false;
        if (response != null) {
            int statusCode = response.getStatusCode();
            redirectDetected = statusCode == StatusCode.MOVED_TEMPORARILY || statusCode == StatusCode.MOVED_PERMANENTLY;
        }

/*        return !(regResult == null || regResult.getStatusCode() == null) &&
                (StatusCode.MOVED_TEMPORARILY == regResult.getStatusCode() ||
                        StatusCode.MOVED_PERMANENTLY == regResult.getStatusCode());
*/
        return redirectDetected;

    }

    private RegResult buildRegResult(ClientTransaction transaction) {
        RegResult retValue;
        final TransactionResult<Boolean> transactionResult = transaction.getTransactionValue();
        final Boolean transactionValue = transactionResult.getValue();

        Response lastResponse = getLastResponse(transaction);

        boolean isRegistered = transactionValue == null ? false : transactionValue;
        boolean isTimeout = transactionResult == null || transactionResult.getReason() == TransactionResult.Reason.TIMEOUT;
        Integer responseCode = lastResponse == null ? null : lastResponse.getStatusCode();
        String reasonPhrase = lastResponse == null ? null : lastResponse.getReasonPhrase();
        String reasonData = lastResponse == null ? null : lastResponse.getCustomHeaderValue(Header.Reason);
        boolean redirectDetected = redirectDetected(lastResponse);
        RedirectData redirectDate = redirectDetected ? createRedirectDate(lastResponse) : null;

        //PEmergencyCallModePreference header
        String emergencyCallType = getPEmergencyCallModeHeader(lastResponse);

        String[] registerURIs = getRegisterURI(lastResponse);

        retValue = new RegResultImpl(
                isRegistered,
                isTimeout,
                responseCode,
                reasonPhrase,
                reasonData,
                redirectDate,
                emergencyCallType,
                registerURIs
        );

        return retValue;
    }


    private String getPEmergencyCallModeHeader(Response lastResponse) {
        String pEmergencyCallModeHeader = null;
        
        if(lastResponse != null) {
            List<String> customHeaders = lastResponse.getCustomHeader(Header.PEmergencyCallModePreference);
            pEmergencyCallModeHeader = customHeaders.size() > 0? customHeaders.get(0): null;
        }
        
        return pEmergencyCallModeHeader;
    }
    
    private String[] getRegisterURI(Response lastResponse) {
        String[] registerURIs = null;

        if (lastResponse != null && lastResponse.getContacts() != null && lastResponse.getContacts().getContactsList() != null) {

            UriHeader[] uris;

            ContactsList contacts = lastResponse.getContacts();
            if (contacts.getContactsList().size() > 0) {
                uris = contacts.getContactsList().toArray(new UriHeader[contacts.getContactsList().size()]);

                registerURIs = new String[uris.length];
                for (int i = 0; i < uris.length; i++) {
                    registerURIs[i] = uris[i].getUri().getShortURI();
                }
            }
        }

        return registerURIs;
    }
    

    private Response getLastResponse(ClientTransaction transaction) {
        final BaseSipMessage lastInMessage = transaction.getLastInMessage();
        Response retValue = null;
        if (lastInMessage instanceof Response) {
            retValue = (Response) lastInMessage;
        }
        return retValue;
    }

    private TransactionListenerAdapter<BaseSipMessage> createRegistrationCompleteListener(final TransactionType transactionType, final Dialog dlg) {
        return new TransactionListenerAdapter<BaseSipMessage>() {

            public void onTransactionComplete(
                    final TransactionEvent<BaseSipMessage> event,
                    final TransactionResult.Reason reason) {

                event.getTransaction().removeListener(this);
                //if transaction completes successfully we should make some clean up.
                if (event.getTransaction().getTransactionValue().getValue()) {
                    dlg.putCustomParameter(Dialog.ParamKey.PREV_REG_ADDRESS, null);
                }


                //A success response to any REGISTER request contains the complete list
                //of existing bindings, regardless of whether the request contained a
                //Contact HEADER field.  If no Contact HEADER field is present in a
                //REGISTER request, the list of bindings is left unchanged.
                if (TransactionType.SIP_LOGIN == transactionType && event.getTransaction().getTransactionValue().getValue()) {

                    final BaseSipMessage lastMessage = event.getLastInMessage();
                    if (lastMessage instanceof Response) {
                        final Response response = (Response) event.getLastInMessage();
                        registrationInfo.set(parseRegisterResponse(response));
                    }
                    else {
                        assert false :
                                "Last message for '" + TransactionType.SIP_LOGIN + "' transaction not a response. " + event;
                    }
                }
            }
        };
    }

    private void notifyListeners(
            final TransactionType transactionType,
            final RegResult regResult) {

        RegisterEvent registerEvent = createRegisterEvent(transactionType, regResult);
        registrationListenerHolder.getNotifier().onRegistrationAttempt(registerEvent);

        if (TransactionType.SIP_LOGOUT == transactionType) {
            if (registered.get() && regResult.isSuccessful()) {
                registrationListenerHolder.getNotifier().onUnregistered(registerEvent);
            }
        }
        else if (TransactionType.SIP_LOGIN == transactionType) {
            if (!registered.get() && regResult.isSuccessful()) {
                registrationListenerHolder.getNotifier().onRegistered(registerEvent);
                checkOnlineClient();
            }
            else if (registered.get() && regResult.isSuccessful()) {
                log("Registration refresh completed successfully", "REGISTRATION REFRESH");
                registrationListenerHolder.getNotifier().onRegistrationRefresh(registerEvent);
                checkOnlineClient();
            }
            else if (registered.get() && !regResult.isSuccessful()) {
                log("Registration failed. Client probably unregistered in SIP network", "REGISTRATION REFRESH");
                registrationListenerHolder.getNotifier().onRegistrationRefresh(registerEvent);
            }
        }
        else {
            assert false : "MUST never be there";
        }
    }

    private void updateRegDialog(
            final TransactionType transactionType,
            final RegResult regResult) {

        final Dialog dlg = getStackContext().getDialogStorage().getDialog(
                getStackContext().getRegistrationIdentity(),
                REGISTRAR_SERVER, regDialogTag.get()
        );

        if (
                TransactionType.SIP_LOGOUT == transactionType ||
                        TransactionType.SIP_LOGIN == transactionType && !regResult.isSuccessful()) {

            regDialogTag.set(generateUniqueTag());
            getStackContext().getDialogStorage().cleanUpDialog(dlg);
        }
    }

    public void shutdown() {

        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning RegisterServiceImpl");

        done.compareAndSet(false, true);
        registrationListenerHolder.shutdown();
        getTransactionManager().removeListener(this);
        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "RegisterServiceImpl shutdown successfully");
    }

    private DefaultRegisterEvent createRegisterEvent(
            final TransactionType transactionType,
            final RegResult regResult) {

        return new DefaultRegisterEvent(
                getStackContext().getRegistrationIdentity(),
                transactionType,
                regResult,
                registrationInfo.get()
        );
    }

    private RegistrationInfo parseRegisterResponse(final Response response) {

        final List<String> serviceRoutes = response.getCustomHeader("Service-Route");
        String serviceRoute = serviceRoutes != null && serviceRoutes.size() > 0 ? serviceRoutes.get(0) : null;
        if (serviceRoute == null) {
            final Collection<UriHeader> headers = response.getServiceRoutes();
            if (headers != null && headers.size() > 0) {
                serviceRoute = ParserUtils.buildHeaderValue(headers);
            }
        }

        return new RegistrationInfoDefaultImpl.Builder().
                knownContacts(handleContacts(response)).
                associatedURI(handleAssociatedURIList(response)).
                globalAddress(handleGlobalAddress(response)).
                serviceRoute(serviceRoute).
                build();
    }

    private String handleGlobalAddress(Response response) {
        String globalAddress = null;
        final boolean globalIPDiscoveryEnabled = getStackContext().getConfig().globalIpDiscovery();
        if (globalIPDiscoveryEnabled) {
            final List<Via> viaList = response.getVias();
            if (viaList != null && viaList.size() > 0) {
                final Via topmostVia = viaList.get(0);
                final ParamList paramList = topmostVia.getParamsList();
                if (paramList != null) {
                    final Param receivedParam = paramList.get("received");
                    if (receivedParam != null) {
                        globalAddress = receivedParam.getValue();
                    }
                }
            }
        }
        return globalAddress;
    }

    private List<String> handleAssociatedURIList(Response response) {
        //A UAC may receive a P-Associated-URI HEADER field in the 200 OK
        //response for a REGISTER.  The presence of the HEADER field in the 200
        //OK response for a REGISTER request implies that the extension is
        //supported at the registrar.
        //
        //The HEADER value contains a viaList of zero or more associated URIs to
        //the address-of-record URI.  The UAC MAY use any of the associated
        //URIs to populate the From HEADER value, or any other SIP HEADER value
        //that provides information of the identity of the calling party, in a
        //subsequent request.
        //
        //EXAMPLE:
        //P-Associated-URI: <sip:user_aor_2@example.net>,<sip:@example.net;user=phone>
        final List<String> associatedURIlist = response.getCustomHeader("P-Associated-URI");
        List<String> associatedURIStringList;
        if (!associatedURIlist.isEmpty()) {
            associatedURIStringList = new ArrayList<String>();
            for (String associatedURI : associatedURIlist) {
                final String[] uris = associatedURI.split(",");
                for (String uri : uris) {
                    if (!"".equals(uri)) {
                        associatedURIStringList.add(uri);
                    }

                }
            }
        }
        else {
            associatedURIStringList = Collections.emptyList();
        }
        return associatedURIStringList;
    }

    private List<String> handleContacts(Response response) {
        final List<String> contactStringList = new ArrayList<String>();

        //A success response to any REGISTER request contains the complete viaList
        //of existing bindings, regardless of whether the request contained a
        //Contact HEADER field.  If no Contact HEADER field is present in a
        //REGISTER request, the viaList of bindings is left unchanged.
        final ContactsList contactList = response.getContacts();
        if (contactList != null) {
            final Collection<UriHeader> uriHeaders = contactList.getContactsList();

            for (UriHeader uriHeader : uriHeaders) {
                final Uri uri = uriHeader.getUri();

                if (uriHeader.getUri() != null) {
                    contactStringList.add(uri.buildContent());
                }
            }
        }
        return contactStringList;
    }

    private String generateUniqueTag() {
        return REGISTER_UNIQUE_TAG + System.currentTimeMillis();
    }

    private static void log(String msg, String prefix) {
        Logger.log(prefix, msg);
    }


    public void onConnected(final ConnStateEvent event) {
        if (keepConnected.get() && !registered.get()) {
            refreshRegistration(REGISTRATION_TIMEOUT);
        }
    }

    public void onConnecting(final ConnStateEvent event) {
    }

    public void onDisconnected(final ConnStateEvent event) {
    }

    public void onDisconnecting(final ConnStateEvent event) {
    }

    public void onUnknown(final ConnStateEvent event) {

    }

    public void onIpChange(final IpChangeEvent event) {
        Logger.log(LOG_TAG, "onIpChange#event = " + event);
        
        if(NetUtils.isAddressLocal(event.getCurrentAddress())) {
            Logger.log(LOG_TAG, "onIpChange#new address is local, skip registration refresh");
            return;   
        }
        
        if(event.getCurrentNetworkType() == NetworkType.NONE) {
            Logger.log(LOG_TAG, "onIpChange#network type is none, skip registration refresh");
            return;
        }
        
        //RegisterServiceImpl
        if (keepConnected.get()) {
            final Dialog dlg = getStackContext().getDialogStorage().getDialog(
                    getStackContext().getRegistrationIdentity(),
                    REGISTRAR_SERVER, regDialogTag.get()
            );

            dlg.putCustomParameter(Dialog.ParamKey.PREV_REG_ADDRESS, event.getPreviousAddress());
            // should be triggered in text refresh
            //refreshRegistration(REGISTRATION_TIMEOUT);
            Logger.log(LOG_TAG, "onIpChange#wait for next refresh");
        }
    }

    public void onNetworkTypeChange(final NetworkTypeChangeEvent event) {
        Logger.log(LOG_TAG, "onNetworkTypeChange#event = " + event);
/*        if(event.getCurrentType() == NetworkType.NONE) {
            Logger.log(LOG_TAG, "onNetworkTypeChange#network type is none, skip registration refresh");
            return;
        }
        
        if (keepConnected.get()) {
            final Dialog dlg = getStackContext().getDialogStorage().getDialog(
                    getStackContext().getRegistrationIdentity(),
                    REGISTRAR_SERVER, regDialogTag.get()
            );

            //dlg.putCustomParameter(Dialog.ParamKey.PREV_REG_ADDRESS, event.getPreviousAddress());
            refreshRegistration(REGISTRATION_TIMEOUT);
        }
*/    }

    public void onError(StackOuterError outerError) {
        Logger.log(LOG_TAG, "onError#outerError = " + outerError);
        Throwable throwableCause = outerError.getThrowableCause();
        if (throwableCause instanceof ChannelIOException) {
            ChannelIOException exception = (ChannelIOException) throwableCause;
            if (exception.getReason() == Reason.DNS_LOOKUP_ERROR) {
                handleIOExeption(exception);
            } else if(exception.getReason() == Reason.SECURITY) {
                handleCertificateExeption(exception);
            } else if(exception.getReason() == Reason.UNKNOWN_ERROR) {
                handleIOExeption(exception);
            } else {
                Logger.log(LOG_TAG, "Unhandled reason = " + exception.getReason());
            }
        } 

    }


    private void handleIOExeption(ChannelIOException exception) {
        this.failureRoute = exception.getRoute();
        doFinalizeRegTransaction();
    }
    
    private void doFinalizeRegTransaction() {
        final Dialog registrationData = extractRegistrationData();
        final ClientTransaction transaction = getTransactionManager().findTransaction(registrationData, TransactionType.SIP_LOGIN);
        if(transaction != null && !transaction.isComplete()) {
            transaction.shutdown();
        }
    }

    private void handleCertificateExeption(ChannelIOException exception) {
        certificateValidationError.set(exception);
        doFinalizeRegTransaction();
    }

    public void onShutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getRegisterURIs() {
        return regResult != null ? regResult.get().getRegisterURIs() : null;
    }

    void checkOnlineClient() {
        if (Arrays.asList(getStackContext().getStackRegistry().getClientRegistries()).size() > 0)
            return;
        else
            notifyToDeregister();
    }

    public void notifyToDeregister() {
        registrationListenerHolder.getNotifier().onUnregistered(null);
    }
}
