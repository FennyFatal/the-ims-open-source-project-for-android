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

package javax.microedition.ims.core.auth;

import javax.microedition.ims.common.ChallengeType;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.config.UserPassword;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.messages.wrappers.sip.AuthChallenge;
import javax.microedition.ims.messages.wrappers.sip.AuthenticationChallenge;
import java.util.*;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 13-Jan-2010
 * Time: 10:07:40
 */
public class AuthorizationRegistry {
    private static final String LOG_TAG = "AuthorizationRegistry";
    
    private final Map<String, Map<ChallengeType, ? extends AuthorizationData>> authorizationDataMap =
            Collections.synchronizedMap(new HashMap<String, Map<ChallengeType, ? extends AuthorizationData>>());
    private final Configuration configuration;
    private final StackContext stackContext;

    public AuthorizationRegistry(final StackContext stackContext) {
        this.stackContext = stackContext;
        this.configuration = stackContext.getConfig();
    }
    
    //TODO
    public Map<ChallengeType, ? extends AuthorizationData> getAuthorizationData(final String realm) throws AuthorizationException {
        //Collection<? extends AuthorizationData> retValue;

        //we can suppress because the only place where authorizationDataMap is getting filled with data is
        //here in this method and therefore we know for sure exact data type.
        @SuppressWarnings({"unchecked"})
        Map<ChallengeType, AuthorizationData> realmAuthData = (Map<ChallengeType, AuthorizationData>) authorizationDataMap.get(realm);
        if (realmAuthData == null) {
            realmAuthData = doUpdateAll(realm, null);
        }

        return Collections.unmodifiableMap(realmAuthData);
    }


    public Map<ChallengeType, ? extends AuthorizationData> update(final Collection<AuthChallenge> authChallenges) throws AuthorizationException {
        Logger.log(LOG_TAG, "update#authChallenges = " + authChallenges);
        return doUpdate(authChallenges.toArray(new AuthChallenge[authChallenges.size()]));
    }

/*    public Map<ChallengeType, ? extends AuthorizationData> update(AuthChallenge... authChallenges) {
        return doUpdate(authChallenges);
    }
*/
    private Map<ChallengeType, ? extends AuthorizationData> doUpdate(AuthChallenge[] authChallenges) throws AuthorizationException {
        Logger.log(LOG_TAG, "doUpdate#authChallenges = " + authChallenges);
        if (authChallenges == null) {
            final String errMsg = "Challenges can not be null. Now it has value " + Arrays.toString(authChallenges);
            throw new IllegalArgumentException(errMsg);
        }

        final Map<ChallengeType, AuthorizationData> retValue;
        if (authChallenges.length == 1) {
            final AuthChallenge challenge = authChallenges[0];
            retValue = doUpdateAll(challenge.getRealm(), challenge);
        }
        else {
            retValue = new HashMap<ChallengeType, AuthorizationData>(authChallenges.length * 2);

            for (AuthChallenge challenge : authChallenges) {
                final ChallengeType challengeType = challenge.getChallengeType();
                retValue.put(
                        challengeType,
                        doUpdateDedicated(challenge.getRealm(), challenge).get(challengeType)
                );
            }
        }

        return Collections.unmodifiableMap(retValue);
    }

    private Map<ChallengeType, AuthorizationData> doUpdateAll(final String realm, final AuthChallenge challenge) throws AuthorizationException {
        Logger.log(LOG_TAG, "doUpdateAll#realm = " + realm + ", challenge = " + challenge);
        Map<ChallengeType, AuthorizationData> realmAuthData = ensureRealmEntryExists(realm);

        
        for (ChallengeType authType : ChallengeType.values()) {
        //for(ChallengeType authType : configuration.getAuthForceTypes()) {
            Logger.log(LOG_TAG, "doUpdateAll#authType = " + authType);
            AuthorizationData authorizationData = buildAuthorizationData(challenge, authType, configuration);
            Logger.log(LOG_TAG, "doUpdateAll#authorizationData = " + authorizationData);
            realmAuthData.put(authType, authorizationData);
        }

        return realmAuthData;
    }

    private Map<ChallengeType, AuthorizationData> doUpdateDedicated(final String realm, final AuthChallenge challenge) throws AuthorizationException {
        Map<ChallengeType, AuthorizationData> realmAuthData = ensureRealmEntryExists(realm);

        final ChallengeType challengeType = challenge.getChallengeType();
        realmAuthData.put(challengeType, buildAuthorizationData(challenge, challengeType, configuration));

        return realmAuthData;
    }

    private Map<ChallengeType, AuthorizationData> ensureRealmEntryExists(String realm) {
        //can suppress because we know for sure exact type from the only place where authorizationDataMap is getting filled with data is
        //in getAuthorizationData(...) method
        @SuppressWarnings({"unchecked"})
        Map<ChallengeType, AuthorizationData> realmAuthData = (Map<ChallengeType, AuthorizationData>) authorizationDataMap.get(realm);

        if (realmAuthData == null) {
            authorizationDataMap.put(
                    realm,
                    realmAuthData = new HashMap<ChallengeType, AuthorizationData>(ChallengeType.values().length * 2)
            );
        }
        return realmAuthData;
    }

    private AuthorizationData buildAuthorizationData(
            final AuthChallenge challenge,
            final ChallengeType authType,
            final Configuration configuration) throws AuthorizationException {

        final AuthorizationData authorizationData;
        byte qop = challenge == null ? 0 : chooseQop(challenge.getQop());

        final UserInfo userInfo = configuration.getAuthUserName();
        final UserPassword userPassword = configuration.getUserPassword();
        final String digestUri = configuration.getRegistrarServer().toSipURI();

        authorizationData = new AuthorizationImpl(
                stackContext.getAkaAuthProvider(),
                //userInfo.getName(),
                userInfo.toFullName(),
                userPassword.getPassword(),
                digestUri,
                challenge,
                qop,
                authType
        );

        return authorizationData;
    }

    private byte chooseQop(final byte challengeQop) {
        byte qop = 0;
        if (challengeQop > 0) {
            qop = (challengeQop & AuthenticationChallenge.QOP_AUTH) > 0
                    ? AuthenticationChallenge.QOP_AUTH
                    : AuthenticationChallenge.QOP_AUTH_INT;
        }
        return qop;
    }
}
