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

import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.registry.CommonRegistry;
import javax.microedition.ims.core.registry.StackRegistry;
import javax.microedition.ims.core.registry.property.RegisterProperty;
import javax.microedition.ims.core.sipservice.Privacy;
import javax.microedition.ims.core.sipservice.PrivacyInfo;
import javax.microedition.ims.messages.utils.MessageUtils;
import javax.microedition.ims.messages.wrappers.sip.AuthType;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Header;
import javax.microedition.ims.messages.wrappers.sip.SipUri;
import java.util.Map.Entry;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 15-Dec-2009
 * Time: 13:48:26
 */
public class RegisterMessageBuilder extends RequestMessageBuilder {

    public RegisterMessageBuilder(final Dialog dialog, final StackContext context) {
        super(dialog, context);
    }


    /*
SIP_REGISTER sip:dummy.com SIP/2.0
Via: SIP/2.0/TCP 62.236.91.3:38108;branch=z9hG4bKsutnjpu;rport;alias
From: <sip:movial5@dummy.com>;tag=96d6-d37c3260-f4c8a6f2-c2c3
To: <sip:movial5@dummy.com>
Call-ID: 7dd8bc7b-586c-0846-6e21-4c7509a82396
CSeq: 24650 SIP_REGISTER
Contact: <sip:movial5@62.236.91.3:38108;transport=TCP>;EXPIRES=700;q=1.0
Max-Forwards: 70
User-Agent: Movial Client
Content-Length: 0
     */

    //todo: refactor method

    protected BaseSipMessage.Builder buildCustomMessage() {

        //Request retValue = new Request();
        BaseSipMessage.Builder retValue = new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST);

        final SipUri.SipUriBuilder uriBuilder = MessageUtils.createURI(
                null,
                context.getConfig().getRegistrarServer().getAddress(),
                "sip"
        );
        setRequestUriHeader(uriBuilder.buildUri(), retValue);
        setMethodHeader(MessageType.SIP_REGISTER, retValue);
        generateViaHeader(context.getConfig(), retValue);
        addMaxForwardsHeader(context.getConfig(), retValue);
        addFromHeader(dialog, retValue);
        addToHeader(dialog, retValue);
        addCallIdHeader(dialog, retValue);
        addUserAgentHeader(context.getConfig(), retValue);
        addCSeqHeader(dialog, retValue);

        final GsmLocationInfo locationInfo = context.getEnvironment().getGsmLocationService().getGsmLocationInfo();
        addPLastAccessNetworkHeader(locationInfo, retValue);
        addPAccessNetworkHeader(locationInfo, retValue);

        CommonRegistry commonRegistry = context.getStackRegistry().getCommonRegistry();
        assert commonRegistry != null;

        for (RegisterProperty registerProperty : commonRegistry.getRegisterProperties()) {
            for (Entry<String, String> customHeader : registerProperty.getHeaders().entrySet()) {
                retValue.customHeader(customHeader.getKey(), customHeader.getValue());
            }
        }

        //addSupported(retValue);

        final String agentId = lookupAgentId(context.getStackRegistry());
        final String agentIdParam = agentId != null ? String.format("agentid=\"%s\"", agentId) : null;

        final AuthType type = context.getConfig().getUserPassword().getPasswordType();
        String locationInfoValue = getLocationInfoValue();

        //String sipInstance = "+sip.instance=\"%3Curn%3Auuid%3A" + deviceId + "%3E\"";
        //String sipInstance = "+sip.instance=\"<urn:uuid:" + formatedDeviceId + ">\"";
        String sipInstance = extractSipInstance();
        
        if (AuthType.AKA == type) {
            generateContactHeader(
                    context.getConfig(),
                    context.getStackRegistry(),
                    retValue,
                    agentIdParam,
                    "+g.3gpp.smsip",
                    //"+g.oma.sip-im",
                    "reg-id=1",
                    sipInstance,
                    locationInfoValue
            );
        } else {
            generateContactHeader(
                    context.getConfig(),
                    context.getStackRegistry(),
                    retValue,
                    agentIdParam,
                    "+g.3gpp.smsip",
                    sipInstance,
                    //"+g.oma.sip-im",
                    locationInfoValue
            );
        }

        //generateEmptyContactHeader(retValue);

        final String pathHeaderValue = (String) dialog.getCustomParameter(Dialog.ParamKey.PATH);
        if (pathHeaderValue != null) {
            retValue.customHeader("Path", pathHeaderValue);
        }

        final PrivacyInfo privacyInfo = context.getConfig().getPrivacyInfo();
        if (privacyInfo != null && privacyInfo.get().size() != 0) {
            final String privacyHeaderValue = Privacy.toString(privacyInfo.get());
            retValue.customHeader(Header.Privacy, privacyHeaderValue);
        }

        addPreferedIdentity(retValue);

        addAuthorizationHeader(retValue, MessageType.SIP_REGISTER);
        //addExpiresHeader(retValue);

        retValue.body(new byte[0]);

        return retValue;
    }

    private String lookupAgentId(StackRegistry registry) {
        String agentId = null;

        RegisterProperty[] registerProperties = registry.getCommonRegistry().getRegisterProperties();
        for (RegisterProperty property : registerProperties) {
            agentId = property.getAgentId();
        }
        return agentId;
    }

/*    protected void addToHeader(
            final Dialog dialog,
            final BaseSipMessage.Builder retValue) {
        UserInfo userInfo = extractUserInfo(dialog);

        SipUri.SipUriBuilder toURI = MessageUtils.createURI(
                userInfo.getName(),
                userInfo.getDomain(),
                userInfo.getSchema()
        );
        super.addToHeader(toURI, retValue);
    }

    protected UserInfo extractUserInfo(Dialog dialog) {
        AuthType type = context.getConfig().getUserPassword().getPasswordType();

        UserInfo userInfo;
        if (type == AuthType.AKA) {
            userInfo = context.getAkaAuthProvider().getImpu();

            if (userInfo == null) {
                userInfo = dialog.getLocalParty().getUserInfo();

                String errMsg = "Building uri. No AKA hardware present. " + userInfo.toFullName() + " will be used.";
                Logger.log(Logger.Tag.WARNING, errMsg);
            }

        } else {
            userInfo = dialog.getLocalParty().getUserInfo();
        }
        return userInfo;
    }
*/

/*    private void addSupportedHeader(Request retValue, String[] supportedEntities) {
        retValue.getSupported().addAll(Arrays.asList(supportedEntities));
    }
*/
}
