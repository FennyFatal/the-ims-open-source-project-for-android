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

package javax.microedition.ims.messages.builder;

import android.util.Log;

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.core.AcceptContactDescriptor;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.auth.AuthorizationData;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.connection.NetworkSubType;
import javax.microedition.ims.core.connection.NetworkType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.registry.ClientRegistry;
import javax.microedition.ims.core.registry.StackRegistry;
import javax.microedition.ims.core.registry.property.CoreServiceProperty;
import javax.microedition.ims.core.sipservice.register.RegistrationInfo;
import javax.microedition.ims.messages.parser.message.SipUriParser;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.*;
import java.util.*;

public abstract class RequestMessageBuilder extends BaseMessageBuilder implements IRequestMessageBuilder {
    private static final String LOG_TAG = "RequestMessageBuilder";
    private static final Set<Algorithm> SUPPORTED_ALGORITHMS = EnumSet.of(Algorithm.MD5, Algorithm.AKAv1_MD5);

    public RequestMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }

    public Request buildMessage() {
        assert dialog != null;
        assert dialog.getLocalParty() != null;
        assert dialog.getLocalParty().getUserInfo() != null;

        BaseSipMessage.Builder builder = buildCustomMessage();

        if (builder.getBody() == null || builder.getBody().length == 0) {
            addClientDataToMessage(builder, true);
        } else {
            addClientDataToMessage(builder, false);
        }

        final RegistrationInfo registrationInfo = context.getRegistrationInfo();
        final MessageType messageType = MessageType.parse(builder.getMethod());

        /*
        assert MessageType.SIP_REGISTER == messageType || MessageType.SIP_ACK == messageType || registrationInfo != null :
                "Probably called before registration.";
                */

        final List<OptionFeature> features = Arrays.asList(context.getConfig().getSupportedFeatures());
        Set<OptionFeature> optionFeatures = features.isEmpty() ? Collections.<OptionFeature>emptySet() : EnumSet.copyOf(features);
        if (optionFeatures.contains(OptionFeature.PATH)) {
            if (registrationInfo != null) {
                UserInfo userInfo = dialog.getLocalParty().getUserInfo();
                String serviceRoute = registrationInfo.getServiceRoute(userInfo);
                addRouteHeader(builder, serviceRoute);
            }
        }

        addSupported(builder, MessageType.parse(builder.getMethod()));

        return (Request) builder.build();
    }

    protected abstract BaseSipMessage.Builder buildCustomMessage();

    public BaseSipMessage.Builder updateRequestWithAuthorizationHeader(
            final Request sourceMsg,
            final MessageType method,
            final Collection<ChallengeType> challengeTypes,
            final boolean updateCSeq) {

        return updateRequestWithAuthorizationHeader(
                sourceMsg.getBuilder(),
                method,
                challengeTypes,
                updateCSeq
        );
    }

    public BaseSipMessage.Builder updateRequestWithAuthorizationHeader(
            final BaseSipMessage.Builder builder,
            final MessageType method,
            final Collection<ChallengeType> challengeTypes,
            final boolean updateCSeq) {

        final Map<ChallengeType, ? extends AuthorizationData> authData = dialog.getAuthorizationData();

        if (authData != null) {
            //private static final boolean USE_SIMULTANEOUS_AUTH = false;

            boolean useSimultaneousAuth = context.getConfig().useSimultaneousAuth();
            Collection<ChallengeType> needAuthTypes =
                    useSimultaneousAuth ?
                            Arrays.asList(ChallengeType.values()) :
                            challengeTypes;

            builder.authenticationChallenge(
                    buildChallngesMap(method, authData, needAuthTypes)
            );

            if (updateCSeq) {
                addCSeqHeader(dialog, builder);
            }
            updateTopmostViaBranch(context.getConfig(), builder);
        }

        return builder;
    }

    protected String getLocationInfoValue() {
        String locationInfoValue = context.getEnvironment().getGsmLocationService().getGsmLocationInfo() != null ?
                "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.e-location\"" :
                null;
        return locationInfoValue;
    }

    protected void addPreferedIdentity(BaseSipMessage.Builder retValue) {

        final AuthType type = context.getConfig().getUserPassword().getPasswordType();

        final UserInfo preferredIdentity = AuthType.AKA == type ?
                context.getAkaAuthProvider().getImpu() :
                context.getConfig().getPreferredIdentity();

        if (preferredIdentity != null) {
            final String preferredIdentityValue = preferredIdentity.toUri();
            if (preferredIdentityValue != null) {
                retValue.customHeader(Header.PPreferredIdentity, preferredIdentityValue);
            }
        }
    }

    private Map<ChallengeType, AuthChallenge> buildChallngesMap(
            final MessageType method,
            final Map<ChallengeType, ? extends AuthorizationData> authData,
            final Collection<ChallengeType> needAuthTypes) {

        Collection<AuthChallenge> challenges = buildChallenges(method, authData, needAuthTypes);
        final Map<ChallengeType, AuthChallenge> authChallenges = new HashMap<ChallengeType, AuthChallenge>(challenges.size() * 2);

        for (AuthChallenge challenge : challenges) {
            authChallenges.put(challenge.getChallengeType(), challenge);
        }
        return authChallenges;
    }

    private Collection<AuthChallenge> buildChallenges(
            MessageType method,
            Map<ChallengeType, ? extends AuthorizationData> authData,
            Collection<ChallengeType> needAuthTypes) {

        //final UserInfo userName = context.getConfig().getAuthUserName();
        final AuthType type = context.getConfig().getUserPassword().getPasswordType();
        final UserInfo userName = type == AuthType.AKA ?
                context.getAkaAuthProvider().getImpi() :
                context.getConfig().getAuthUserName();

        Collection<AuthChallenge> challenges = new ArrayList<AuthChallenge>();

        for (ChallengeType challengeType : needAuthTypes) {
            final AuthorizationData authorizationData = authData.get(challengeType);

            if (authorizationData != null) {
                final AuthChallenge challenge = buildChallengeResponse(
                        authorizationData,
                        userName,
                        method.stringValue()
                );
                challenges.add(challenge);
            }
        }

        return challenges;
    }

    protected void addAuthorizationHeader(final BaseSipMessage.Builder retValue, final MessageType method) {
        Collection<ChallengeType> challengeTypes = (Collection<ChallengeType>) dialog.getCustomParameter(
                Dialog.ParamKey.CHALLENGE_TYPE
        );

        updateRequestWithAuthorizationHeader(retValue, method, challengeTypes, false);
    }

    protected void addAcceptHeader(
            final BaseSipMessage.Builder retValue,
            final MimeType contentType) {

        retValue.customHeader(Header.Accept, contentType.stringValue());
    }

    private AuthChallenge buildChallengeResponse(
            final AuthorizationData authorization,
            final UserInfo userName,
            final String method) {
        AuthenticationChallenge authChallenge = (AuthenticationChallenge) authorization.getChallenge();

        //TODO: Don't throw exception. Do mo wise handling of unsupported algorithms
        //if (authChallenge != null && Algorithm.MD5 != authChallenge.getAlgorithm()) {
        if (authChallenge != null && !SUPPORTED_ALGORITHMS.contains(authChallenge.getAlgorithm())) {
            throw new IllegalArgumentException("Does not support " + authChallenge.getAlgorithm() + " authorization");
        }

        AuthenticationChallenge.Builder builder;
        final AuthType type = context.getConfig().getUserPassword().getPasswordType();

        if (authChallenge == null) {
            builder = AuthenticationChallenge.EMPTY_CHALLENGE.asBuilder();
            builder.realm(context.getConfig().getRealm());
            builder.type(type);
        } else {
            builder = authChallenge.asBuilder();
        }

        builder.algorithm(type == AuthType.AKA ? Algorithm.AKAv1_MD5 : Algorithm.MD5);
        builder.challengeType(authorization.getChallengeType());

        builder.username(userName.toUri());
        builder.uri(context.getConfig().getRegistrarServer().toSipURI());
        builder.response(authorization.calculateAuthResponse(method, new byte[0]));
        builder.nonceCount(authorization.getLastNC());
        builder.cNonce(authorization.getLastCNonce());
        builder.qop(authorization.getQop());

        //authChallenge.resetQop((byte)1);

        return builder.build();
    }

    protected void setRequestUriHeader(final Uri requestUri, final BaseSipMessage.Builder retValue) {
        retValue.requestUri(requestUri);
    }

    protected void addExpiresHeader(final BaseSipMessage.Builder retValue, long expTime) {
        //Long expTime = (Long) dialog.getCustomParameter(Dialog.ParamKey.REGISTRATION_EXPIRES);
        retValue.expires(expTime);
    }

    /**
     * Add 'ServiceRoute' header only if <code>serviceRoute<code> param not null and not empty
     *
     * @param retValue
     * @param serviceRoute
     */
    protected void addRouteHeader(final BaseSipMessage.Builder retValue, final String serviceRoute) {
        if (!StringUtils.isEmpty(serviceRoute)) {
            retValue.customHeader("Route", serviceRoute);
        }
    }

    protected void buildAcceptContactForClient(
            final BaseSipMessage.Builder retValue,
            final ClientIdentity clientIdentity) {

        AcceptContactDescriptor[] acceptContactHeaders =
                context.getClientRouter().buildClientPreferences(clientIdentity);

        if (acceptContactHeaders != null && acceptContactHeaders.length > 0) {
            for (AcceptContactDescriptor header : acceptContactHeaders) {
                retValue.customHeader(Header.AcceptContact.stringValue(), header.getContent());
            }
        }
    }

    protected void addRoutes(final BaseSipMessage.Builder retValue, final Collection<UriHeader> recordRoutes) {
        if (recordRoutes == null || recordRoutes.size() == 0) {
            return;
        }

        List<UriHeader> records = new ArrayList<UriHeader>(recordRoutes);
        Collections.reverse(records);

        for (UriHeader uriHeader : records) {
            retValue.route(uriHeader);
        }
    }

    protected Uri constructUri(final String remoteParty) {
        return context.getConfig().getSpecialUris().contains(remoteParty) ?
                new SipUri.SipUriBuilder().domain(remoteParty).buildUri() :
                SipUriParser.parseUri(remoteParty).getUri();
    }

    protected UriHeader constructUriHeader(final String remoteParty) {
        final Uri uri = context.getConfig().getSpecialUris().contains(remoteParty) ?
                new SipUri.SipUriBuilder().domain(remoteParty).buildUri() :
                SipUriParser.parseUri(remoteParty).getUri();

        return new UriHeader.UriHeaderBuilder(uri).build();
    }

    //examples:
    //P-Access-Network-Info: 3GPP2-1X-HRPD; ci-3gpp2=1234123412341234123412341234123411
    //P-Access-Network-Info: wlan-mac-addr=00BA5550EEFF
    //P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=544542332

    protected void addPAccessNetworkHeader(
            final GsmLocationInfo gsmLocationInfo,
            final BaseSipMessage.Builder retValue) {

        String headerValue = buildNetworkInfo(gsmLocationInfo);

        if (headerValue != null) {
            //retValue.customHeader("P-Access-Network-Info", headerValue);
            retValue.customHeader(Header.PAccessNetwork.stringValue(), headerValue);
        }
        else{
            String errMsg = Header.PAccessNetwork.stringValue() +
                    " is empty because GsmLocationInfo proceeded to null value. GsmLocationInfo = " + gsmLocationInfo;

            Logger.log(Logger.Tag.WARNING, errMsg);
        }
    }

    protected void addPLastAccessNetworkHeader(
            final GsmLocationInfo gsmLocationInfo,
            final BaseSipMessage.Builder retValue) {

        /*
        if (gsmLocationInfo != null && gsmLocationInfo.getCid() != 0) {
            retValue.customHeader(Header.PLastAccessNetwork, String.valueOf(gsmLocationInfo.getCid()));
        }
        */

        String headerValue = buildLastNetworkInfo(gsmLocationInfo);

        if (headerValue != null) {
            //retValue.customHeader("P-Access-Network-Info", headerValue);
            retValue.customHeader(Header.PLastAccessNetwork.stringValue(), headerValue);
        }
        else{
            String errMsg = Header.PLastAccessNetwork.stringValue() +
                    " is empty because GsmLocationInfo proceeded to null value. GsmLocationInfo = " + gsmLocationInfo;

            Logger.log(Logger.Tag.WARNING, errMsg);
        }
    }

    protected String buildMACHeaderPart() {
        final String pointMAC = context.getEnvironment().getConnectionManager().getAccessPointMAC();
        String escapedMAC = null;

        if (pointMAC != null) {
            escapedMAC = pointMAC.replaceAll(":", "").replaceAll("-", "");
        }

        return escapedMAC == null ? null : "i-wlan-node-id=" + escapedMAC;
    }

    private String buildNetworkInfo(GsmLocationInfo locationInfo) {
        final NetworkType networkType = context.getEnvironment().getConnectionManager().getNetworkType();
        final NetworkSubType networkSubType = context.getEnvironment().getConnectionManager().getNetworkSubType();

        String headerValue = null;

        if (networkSubType != null) {

            if (NetworkType.MOBILE == networkType && locationInfo != null) {
                // In emulator environment we don't necessarily have location info available
                //headerValue += "; utran-cell-id-3gpp=" + locationInfo.getCid();
                headerValue = locationInfo.toCellIdentity();
            } else if (NetworkType.WIFI == networkType) {
                final String macKeyValuePair = buildMACHeaderPart();
                if (macKeyValuePair != null) {
                    headerValue = networkSubType.stringValue() + "; " + macKeyValuePair;
                }
            }
        }
        return headerValue;
    }

    private String buildLastNetworkInfo(GsmLocationInfo locationInfo) {

        String retValue = null;

        if (locationInfo != null) {
            //return "3GPP-UTRAN-TDD; utran-cell-id-3gpp=" + locationInfo.toUtranCellId3gppValue();
            retValue = locationInfo.toCellIdentity();
        }

        return retValue;
    }


    protected void generateContactHeader(final Configuration config,
                                         final StackRegistry stackRegistry,
                                         final BaseSipMessage.Builder retValue,
                                         final String... customParams) {

        Set<String> aggregatedParams = new HashSet<String>(Arrays.asList(customParams));

        Set<String> clientParams = extractClientData(stackRegistry);
        aggregatedParams.addAll(clientParams);

        generateContactHeader(
                config,
                retValue,
                true,
                aggregatedParams.toArray(
                        new String[aggregatedParams.size()]
                )
        );

        //generateContactHeader(config, retValue, true);
    }

    /**
     * Extract client IARI, ICSIs, and Feature Tags from CoreService property.
     */
    private Set<String> extractClientData(StackRegistry stackRegistry) {
        final Set<String> clientData = new HashSet<String>();

        for (ClientRegistry registry : stackRegistry.getClientRegistries()) {
            CoreServiceProperty coreProperty = registry.getCoreServiceProperty();

            //add iARIs
            Collection<String> iARIs = CollectionsUtils.transform(Arrays.asList(coreProperty.getIARIs()),
                    new CollectionsUtils.Transformer<String, String>() {
                        public String transform(String source) {
                            return String.format("+g.3gpp.iari-ref=\"%s\"", source);
                        }
                    });
            clientData.addAll(iARIs);

            //add iCSIs
            Collection<String> iCSIs = CollectionsUtils.transform(Arrays.asList(coreProperty.getICSIs()),
                    new CollectionsUtils.Transformer<String, String>() {
                        public String transform(String source) {
                            return String.format("+g.3gpp.icsi-ref=\"%s\"", source);
                        }
                    });
            clientData.addAll(iCSIs);

            //add FeatureTags
            clientData.addAll(Arrays.asList(coreProperty.getFeatureTags()));
        }

        return clientData;
    }

    protected void generateContactHeader(final Configuration config,
                                         final BaseSipMessage.Builder retValue,
                                         final String... customParams) {

        generateContactHeader(config, retValue, true, customParams);
    }

    private static String formatImei(String source) {
        assert source.length() == 15;

        String formatedImei = String.format("%s-%s-%s",
            source.substring(0, 8),
            source.substring(8, 14),
            source.substring(14, 15));

        return formatedImei;
    }

   /*
    * The IMEISV drops the Luhn check digit in favour of an additional two digits
    * for the Software Version Number (SVN), making the format AA-BBBBBB-CCCCCC-EE.
    * EE is the SVN
    */
    private static String extractSVN(String source) {

        if (source == null || source.length() != 16) {
            Logger.log(Logger.Tag.WARNING, "extractedSVN#source expected not null and source.length equal 16 but actually source = " + source
                    + ", returning the default 00 value");
            return "00";
        }

        return source.substring(14, 16);
    }

    protected String extractSipInstance() {
        String deviceId = context.getEnvironment().getHardwareInfo().getDeviceId();
        
        if(deviceId == null) {
            Log.i(LOG_TAG, "extractSipInstance#deviceId is null");
            return null;
        }
        
        if(deviceId.length() != 15) {
            Log.i(LOG_TAG, String.format("extractSipInstance#Eexpected deviceId value length is 15, but actual value is = %s, device id value = %s", deviceId.length(), deviceId));
            return null;
        } 

        String deviceSoftwareVersion = context.getEnvironment().getHardwareInfo().getDeviceSoftwareVersion();
        
        StringBuffer formatedDeviceId = new StringBuffer();
        formatedDeviceId.append(formatImei(deviceId));

        formatedDeviceId.append(";svn=").append(extractSVN(deviceSoftwareVersion));

        //String sipInstance = "+sip.instance=\"<urn:gsma:imei:" + formatedDeviceId + ">\"";
        
        String sipInstanceValue = String.format("+sip.instance=\"<urn:gsma:imei:%s>\"", formatedDeviceId.toString());
        
        Log.d(LOG_TAG, "extractSipInstance#sipInstanceValue = " + sipInstanceValue);
        
        return sipInstanceValue;
    }
}
