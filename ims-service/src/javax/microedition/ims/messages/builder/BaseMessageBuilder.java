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

import javax.microedition.ims.FeatureMapper;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.MimeType;
import javax.microedition.ims.common.OptionFeature;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.connection.NetworkType;
import javax.microedition.ims.core.connection.NetworkSubType;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.register.RegistrationInfo;
import javax.microedition.ims.messages.history.BodyPartData;
import javax.microedition.ims.messages.history.MessageDataImpl;
import javax.microedition.ims.messages.utils.BodyPartUtils;
import javax.microedition.ims.messages.utils.MessageUtils;
import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.*;
import javax.microedition.ims.util.MessageUtilHolder;
import java.util.*;

/**
 * Abstract class for message builders
 *
 * @author ext-achirko
 */
public abstract class BaseMessageBuilder {
    protected final Dialog dialog;
    protected final StackContext context;
    // private static final String EXPIRES = "EXPIRES";
    private static final String EXPIRES = "expires";
    private static final String BRANCH_PARAM = "branch";
    private static final String COMP = "comp";

    public BaseMessageBuilder(Dialog dialog, StackContext context) {
        this.dialog = dialog;
        this.context = context;
    }

    protected void addClientDataToMessage(BaseSipMessage.Builder builder, boolean addBody) {
        MessageDataImpl userMessageData;
        boolean request = false;
        if (BaseSipMessage.Builder.Type.RESPONSE == builder.getType()) {
            userMessageData = dialog.getMessageHistory().getNextResponseMessage();
        } else {
            request = true;
            userMessageData = dialog.getMessageHistory().nextRequestMessage();
        }

        if (addBody) {
            // TODO check this when client is ready to provide SDP, maybe adding to
            // custom headers can lead to the situation
            // when similar headers are distributed over generated SIP-message value
            if (userMessageData.getBodyParts().length == 1) {
                //builder.setBody(userMessageData.getBodyParts()[0].getContent());
                BodyPartData bodyPartData = userMessageData.getBodyParts()[0];
                builder.body(bodyPartData.getContent());
                builder.contentType(bodyPartData.getContentType());
                // headers must contain content-type
                for (String key : bodyPartData.getHeadersKeys()) {
                    String value = bodyPartData.getHeader(key);
                    builder.customHeader(key, value);
                }
            } else if (userMessageData.getBodyParts().length > 1) {

                String boundary = BodyPartUtils.generateBoundary();
                /*
                ParamHeader.ParamHeaderBuilder contentTypeBuilder = new ParamHeader.ParamHeaderBuilder(
                        ContentType.MULTIPART_MIXED.stringValue()
                );
                ParamHeader contentType = contentTypeBuilder.build();
                */
                //contentType.getParamsList().set("boundary", "\"" + boundary + "\"");

                builder.contentType(MimeType.MULTIPART_MIXED.stringValue());
                builder.getContentTypeBuilder().param("boundary", "\"" + boundary + "\"");
                builder.body(BodyPartUtils.getBodyPartsAsByteArray(userMessageData, boundary));
            }
        }

        for (String key : userMessageData.getHeadersKeys()) {
            String[] values = userMessageData.getHeaders(key);
            builder.customHeader(key,
                    StringUtils.joinArray(values, 0, values.length, ","));
        }
        if (request) {
            dialog.getMessageHistory().clearNextRequestMessage();
        } else {
            dialog.getMessageHistory().clearNextResponseMessage();
        }
    }

    protected void generateViaHeader(final Configuration config,
                                     final BaseSipMessage.Builder retValue) {
        //Via via = new Via();
        Via.Builder viaBuilder = new Via.Builder();

        viaBuilder.protocol(dialog.getPrefferedProtocol());
        Map<String, String> viaHeaders = new HashMap<String, String>();

        // TODO: if we make message retransmission we should use branch like in
        // initial message
        // viaHeaders.put("branch", "z9hG4bK" + SIPUtil.randomBranchTail() +
        // (configuration.useRPort() ? ";rport" : ""));
        viaHeaders.put(BRANCH_PARAM, SIPUtil.generateBranchWithMagicCookie(config.useRPort() ? ";rport" : ""));

        final SipUri.SipUriBuilder uriBuilder = MessageUtils.createURI(
                null,
                context.getEnvironment().getConnectionManager().getInetAddress(),
                config.getLocalPort(),
                null,
                viaHeaders
        );
        viaBuilder.uriBuilder(uriBuilder);

        retValue.via(viaBuilder);
    }

    protected void updateTopmostViaBranch(final Configuration config,
                                          final BaseSipMessage.Builder retValue) {

        final Collection<Via.Builder> viasCollection = retValue.getVias();
        final List<Via.Builder> vias = new ArrayList<Via.Builder>(viasCollection);

        if (vias != null && vias.size() > 0) {
            final Via.Builder topmostVia = vias.get(0);

            if (topmostVia.getUriBuilder().getHeaders() != null) {

                final ParamList paramList = topmostVia.getUriBuilder().getHeaders();

                assert paramList.containsKey(BRANCH_PARAM) : "incorrect via header=" + topmostVia.build().buildContent();

                paramList.set(BRANCH_PARAM, SIPUtil.generateBranchWithMagicCookie(config.useRPort() ? ";rport" : ""));

            } else {
                assert false : "incorrect via header=" + topmostVia.build().buildContent();
            }
        } else {
            assert false : "Empty via headers for message="
                    + retValue.shortDescription();
        }
    }

    protected void addViaHeader(final Via via, final BaseSipMessage.Builder retValue) {
        retValue.via(via);
    }

    protected void addMaxForwardsHeader(final Configuration config,
                                        final BaseSipMessage.Builder retValue) {
        retValue.maxForwards(config.getMaxForwards());
    }

    protected void addFromHeader(final Dialog dialog, final BaseSipMessage.Builder retValue) {
        UserInfo userInfo = dialog.getLocalParty().getUserInfo();

        final SipUri.SipUriBuilder uriBuilder = MessageUtils.createURI(userInfo);

        UriHeader.UriHeaderBuilder uriHeaderFromBuilder = new UriHeader.UriHeaderBuilder();
        uriHeaderFromBuilder.uriBuilder(uriBuilder);

        final String localTag = dialog.getLocalTag();
        uriHeaderFromBuilder.tag(localTag);
        uriHeaderFromBuilder.param("tag", localTag);

        retValue.from(uriHeaderFromBuilder);
    }

/*    protected void addFromHeader(UriHeader.UriHeaderBuilder uriHeaderBuilder,
            final BaseSipMessage.Builder retValue) {
        retValue.from(uriHeaderBuilder);
    }
*/
    protected void addFromHeader(UriHeader uriHeader,
            final BaseSipMessage.Builder retValue) {
        retValue.from(new UriHeader.UriHeaderBuilder(uriHeader));
    }

    protected void addToHeader(
            final Dialog dialog,
            final BaseSipMessage.Builder retValue) {
        //TODO review
        UserInfo userInfo = UserInfo.valueOf(dialog.getRemoteParty());

        SipUri.SipUriBuilder toURI = MessageUtils.createURI(
                userInfo.getName(),
                userInfo.getDomain(),
                userInfo.getSchema()
        );

        addToHeader(toURI, retValue);
    }

    protected void addToHeader(
            final Uri toURI,
            final BaseSipMessage.Builder retValue) {

        addToHeader(new UriHeader.UriHeaderBuilder(toURI), retValue);
    }

    private void addToHeader(
            final SipUri.SipUriBuilder toURI,
            final BaseSipMessage.Builder retValue) {

        UriHeader.UriHeaderBuilder uriHeaderToBuilder = new UriHeader.UriHeaderBuilder();
        uriHeaderToBuilder.uriBuilder(toURI);

        retValue.to(uriHeaderToBuilder);
    }

    protected void addToHeader(
            final UriHeader toURI,
            final BaseSipMessage.Builder retValue) {

        retValue.to(new UriHeader.UriHeaderBuilder(toURI));
    }

    protected void addToHeader(
            final UriHeader.UriHeaderBuilder toURI,
            final BaseSipMessage.Builder retValue) {

        retValue.to(toURI);
    }

    protected void generateContactHeader(final Configuration config,
                                         final BaseSipMessage.Builder retValue,
                                         final boolean containsExpireTime,
                                         final String... customParams) {

        final AuthType type = context.getConfig().getUserPassword().getPasswordType();

/*        String userName = type == AuthType.AKA ?
                context.getAkaAuthProvider().getImpu().getName() :
                //dialog.getLocalParty().getUserInfo().getName();
                config.getRegistrationName().getName();
*/

        String userName = context.getRegistrationIdentity().getUserInfo().getName();
        String currInetAddress = context.getEnvironment().getConnectionManager()
                .getInetAddress();
        if (config.globalIpDiscovery()) {
            final RegistrationInfo registrationInfo = context.getRegistrationInfo();

            if (registrationInfo != null) {
                final String globalAddress = registrationInfo.getGlobalAddress();

                if (globalAddress != null && !globalAddress.equals("")) {
                    currInetAddress = globalAddress;
                }
            }
        }

        final Number expireTime = containsExpireTime ?
                (Number) dialog.getCustomParameter(Dialog.ParamKey.REGISTRATION_EXPIRES)
                : null;

        retValue.getContactsBuilder().contact(
                createUriHeader(config, userName, currInetAddress, expireTime, customParams)
        );

        String prevInetAddress =
                dialog.getCustomParameter(Dialog.ParamKey.PREV_REG_ADDRESS) == null ?
                        null
                        : dialog.getCustomParameter(Dialog.ParamKey.PREV_REG_ADDRESS).toString();

        if (prevInetAddress != null) {
            retValue.getContactsBuilder().contact(
                    createUriHeader(config, userName, prevInetAddress, 0, customParams)
            );
        }
    }

    private UriHeader createUriHeader(final Configuration config,
                                      final String userName,
                                      final String currInetAddress,
                                      final Number expireTime,
                                      final String... customParams) {

        Map<String, String> contactParameters = new HashMap<String, String>();
        contactParameters.put("transport", dialog.getPrefferedProtocol()
                .toString());

        Map<String, String> contactParametersHeaders = new LinkedHashMap<String, String>();
        if (expireTime != null) {
            contactParametersHeaders.put(EXPIRES, expireTime.toString());
        }

        for (String customParam : customParams) {
            if (customParam != null) {
                contactParametersHeaders.put(customParam, null);
            }
        }

        final SipUri.SipUriBuilder uriBuilder = MessageUtils.createURI(
                userName,
                currInetAddress,
                config.getLocalPort(),
                contactParameters,
                contactParametersHeaders
        );

        UriHeader.UriHeaderBuilder retValue = new UriHeader.UriHeaderBuilder();
        retValue.uriBuilder(uriBuilder);

        return retValue.build();
    }

    protected void addAllow(final BaseSipMessage.Builder retValue) {
        MessageType[] supportedRequests = MessageUtilHolder.getSIPMessageUtil().getAllowedMessages(
                context.getStackRegistry().getCommonRegistry(),
                context.getConfig()
        );

        CollectionsUtils.forAllDo(Arrays.asList(supportedRequests),
                new CollectionsUtils.Closure<MessageType>() {
                    public void execute(MessageType messageType) {
                        retValue.allow(messageType.stringValue());
                    }
                });
    }

    protected void addCSeqHeader(
            final Dialog dialog,
            final BaseSipMessage.Builder retValue) {

        retValue.cSeq(dialog.obtainCSeq());
    }

    protected void addRSeqHeader(
            final Dialog dialog,
            final BaseSipMessage.Builder retValue) {

        retValue.customHeader(Header.RSeq, String.valueOf(dialog.obtainRSeq()));
    }

    /*
      * protected void addRequire(final BaseSipMessage retValue) { String[]
      * requiredFeatures = context.getConfig().getRequiredFeatures();
      *
      * for(String requiredFeature: requiredFeatures) {
      * retValue.addRequire(requiredFeature); } }
      */

    protected void addRequire(
            final BaseSipMessage.Builder retValue,
            final String requireValue) {

        retValue.require(requireValue);
    }

    /*    protected void addSupported(final BaseSipMessage retValue) {
         OptionFeature[] supportedFeatures = context.getConfig()
                 .getSupportedFeatures();

         for (OptionFeature supportedFeature : supportedFeatures) {
             retValue.addSupported(supportedFeature.getName());
         }
     }
      */

    protected void addSupported(
            final BaseSipMessage.Builder retValue,
            final MessageType type) {

        FeatureMapper featureMapper = context.getConfig().getFeatureMapper();
        OptionFeature[] messageFeatures = featureMapper.getSupportedFeaturesByType(type);

        //List<OptionFeature> supportedFeatures = Arrays.asList(context.getConfig().getSupportedFeatures());

        for (OptionFeature messageFeature : messageFeatures) {
            if (context.getConfig().isFeatureSupported(messageFeature)) {
                retValue.supported(messageFeature.getName());
            }
        }
    }

    protected void addSessionExpires(
            final BaseSipMessage.Builder retValue,
            final Refresher refresher,
            final long expiresValue) {

        //retValue.getSessionExpires().setRefresher(refresher);
        //retValue.getSessionExpires().setExpiresValue(expiresValue);

        retValue.getSessionExpiresBuilder().refresher(refresher);
        retValue.getSessionExpiresBuilder().expiresValue(expiresValue);
    }

    protected void addMinSessionExpires(
            final BaseSipMessage.Builder retValue,
            final long minExpiresValue) {

        //retValue.getSessionExpires().setMinExpiresValue(minExpiresValue);
        retValue.getSessionExpiresBuilder().minExpiresValue(minExpiresValue);
    }

    protected void addCSeqHeader(int cseq, final BaseSipMessage.Builder retValue) {
        retValue.cSeq(cseq);
    }

    protected void addUserAgentHeader(
            final Configuration config,
            final BaseSipMessage.Builder retValue) {

        retValue.userAgent(config.getUserAgent());
    }

    protected void addCallIdHeader(
            final Dialog dialog,
            final BaseSipMessage.Builder retValue) {

        retValue.callId(dialog.getCallId());
    }

    protected void addCallIdHeader(
            final String callId,
            final BaseSipMessage.Builder retValue) {

        retValue.callId(callId);
    }

    protected void setMethodHeader(
            final MessageType messageType,
            final BaseSipMessage.Builder retValue) {

        retValue.method(messageType.stringValue());
    }

    protected void addRetryAfterHeader(final BaseSipMessage.Builder retValue) {
        final Boolean retryAfter = (Boolean) dialog.getCustomParameter(Dialog.ParamKey.USE_RETRY_AFTER);

        if (retryAfter != null && retryAfter) {
            final int randomSeconds_0_10 = Double.valueOf(Math.floor(Math.random() * 11)).intValue();

            retValue.customHeader("Retry-After", Integer.toString(randomSeconds_0_10));
        }
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
}
