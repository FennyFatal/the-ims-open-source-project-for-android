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

package javax.microedition.ims.core;

import javax.microedition.ims.common.ChallengeType;
import javax.microedition.ims.common.ListenerSupport;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.auth.AuthorizationData;
import javax.microedition.ims.core.auth.AuthorizationException;
import javax.microedition.ims.core.auth.AuthorizationRegistry;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.Dialog.ParamKey;
import javax.microedition.ims.core.transaction.TransactionEvent;
import javax.microedition.ims.core.transaction.TransactionListener;
import javax.microedition.ims.core.transaction.TransactionType;
import javax.microedition.ims.core.transaction.UnSubscribeOnLogicCompleteAdapter;
import javax.microedition.ims.core.transaction.client.RegisterTransaction;
import javax.microedition.ims.messages.wrappers.sip.AuthChallenge;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 17-Feb-2010
 * Time: 11:47:24
 */
class AuthRealmResolver extends UnSubscribeOnLogicCompleteAdapter<BaseSipMessage> {

    private static final String LOG_TAG = "AuthRealmResolver";

    private static final Set<TransactionType.Name> AUTH_SET = EnumSet.of(
            TransactionType.Name.SIP_LOGIN, TransactionType.Name.SIP_LOGOUT,
            TransactionType.Name.SIP_INVITE_CLIENT, TransactionType.Name.SIP_INVITE_SERVER,
            TransactionType.Name.SIP_REINVITE_CLIENT, TransactionType.Name.SIP_REINVITE_SERVER,
            TransactionType.Name.SIP_MESSAGE_CLIENT, TransactionType.Name.SIP_REFER_CLIENT,
            TransactionType.Name.SIP_NOTIFY_CLIENT,
            TransactionType.Name.SIP_SUBSCRIBE, TransactionType.Name.SIP_UNSUBSCRIBE,
            TransactionType.Name.SIP_PUBLISH_CLIENT
    );

    private final AuthorizationRegistry authorizationRegistry;
    private final Dialog dialog;
    private final Configuration configuration;

    public AuthRealmResolver(
            final ListenerSupport<TransactionListener<BaseSipMessage>> listenerSupport,
            final AuthorizationRegistry authorizationRegistry,
            final Dialog dialog,
            final Configuration configuration) {

        super(listenerSupport);

        assert authorizationRegistry != null;
        assert dialog != null;
        assert configuration != null;

        this.authorizationRegistry = authorizationRegistry;
        this.dialog = dialog;
        this.configuration = configuration;
    }

    @Override
    public void onAuthChallenge(final TransactionEvent<BaseSipMessage> event, final ChallengeType authType) {

        final Collection<AuthChallenge> authChallenges = event.getLastInMessage().getAuthenticationChallenges().values();

        if (authChallenges != null && authChallenges.size() > 0) {

            try {
/*                if(true) {
                    throw new AuthorizationException("", new Exception());
                }
*/                
                Map<ChallengeType, ? extends AuthorizationData> authData = authorizationRegistry.update(authChallenges);

                final TransactionType.Name transactionType = event.getTransaction().getTransactionType().getName();
                if (AUTH_SET.contains(transactionType)) {
                    dialog.setAuthorizationData(authData);
                }
                else {
                    Logger.log(Logger.Tag.WARNING, "onAuthChallenge event: '" + transactionType + "' not in authorization set.");
                }
            } catch (AuthorizationException e) {
                Logger.log(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        else {
            Logger.log(Logger.Tag.WARNING, "onAuthChallenge event doesn't contain chalenge info " + event);
        }
    }

    @Override
    public void onAuthFailed(TransactionEvent<BaseSipMessage> baseSipMessageTransactionEvent, ChallengeType authParty) {
        super.onAuthFailed(baseSipMessageTransactionEvent, authParty);

        //TODO: notify USER about the problem
        Logger.log(Logger.Tag.WARNING, "Incorrect password or other authentication error");
    }

    @Override    
    public void onTransactionInit(final TransactionEvent event) {
        if (event.getTransaction() instanceof RegisterTransaction
            /*|| event.getTransaction() instanceof SubscribeTransaction*/) {
            Collection<ChallengeType> authForceTypes = configuration.getAuthForceTypes();
            if (authForceTypes.size() > 0) {
                
                try {
                    Map<ChallengeType, ? extends AuthorizationData> authorizationData = authorizationRegistry.getAuthorizationData(configuration.getRealm());
                    dialog.putCustomParameter(ParamKey.CHALLENGE_TYPE, authForceTypes);
                    dialog.setAuthorizationData(authorizationData);
                } catch (AuthorizationException e) {
                    Logger.log(LOG_TAG, "onTransactionInit# e = " + e.getMessage());
                    e.printStackTrace();
                }


            }
        }
    }

    public String toString() {
        return "AuthRealmResolver{" +
                "authorizationRegistry=" + authorizationRegistry +
                ", DIALOG=" + dialog +
                ", configuration=" + configuration +
                '}';
    }
}
