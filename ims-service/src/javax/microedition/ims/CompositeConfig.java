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

package javax.microedition.ims;

import javax.microedition.ims.common.*;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.config.UserPassword;
import javax.microedition.ims.core.registry.ClientRegistry;
import javax.microedition.ims.core.registry.CommonRegistry;
import javax.microedition.ims.core.registry.RegistryChangeEvent;
import javax.microedition.ims.core.registry.RegistryChangeListener;
import javax.microedition.ims.core.registry.property.AuthenticationProperty;
import javax.microedition.ims.core.sipservice.PrivacyInfo;
import javax.microedition.ims.core.xdm.XDMConfig;
import javax.microedition.ims.core.xdm.XDMConfigImpl;
import javax.microedition.ims.messages.wrappers.sip.Refresher;
import java.util.Collection;

public class CompositeConfig implements Configuration, RegistryChangeListener {
    private static final String TAG = "CompositeConfig";

    private final Configuration conf;
    private volatile UserInfo clientAuthUser;
    private volatile UserInfo clientRegUser;
    private volatile UserPassword clientPassword;
    private volatile XDMConfig xdmConfig;

    public CompositeConfig(Configuration configuration) {
        this.conf = configuration;
    }

    public UserInfo getRegistrationName() {
        return clientRegUser == null ? conf.getRegistrationName() : clientRegUser;
    }

    public UserInfo getAuthUserName() {
        return clientAuthUser == null ? conf.getAuthUserName() : clientAuthUser;
    }


    public UserPassword getUserPassword() {
        return clientPassword == null ? conf.getUserPassword() : clientPassword;
    }

    public int getLocalPort() {
        return conf.getLocalPort();
    }

    public String getUserAgent() {
        return conf.getUserAgent();
    }

    public String getRealm() {
        return conf.getRealm();
    }

    public ServerAddress getRegistrarServer() {
        return conf.getRegistrarServer();
    }

    public ServerAddress getProxyServer() {
        return conf.getProxyServer();
    }

    public Protocol getConnectionType() {
        return conf.getConnectionType();
    }

    public int getMaxForwards() {
        return conf.getMaxForwards();
    }

    public boolean useRPort() {
        return conf.useRPort();
    }

    public long getRegistrationExpirationSeconds() {
        return conf.getRegistrationExpirationSeconds();
    }

    /*
        public boolean forceAuthorization() {
            return conf.forceAuthorization();
        }
    */
    public boolean useSimultaneousAuth() {
        return conf.useSimultaneousAuth();
    }


    public Collection<ChallengeType> getAuthForceTypes() {
        return conf.getAuthForceTypes();
    }

    /*    
        public boolean isPrackSupported() {
            return conf.isPrackSupported();
        }
    */
    /*    
        public String[] getRequiredFeatures() {
            return conf.getRequiredFeatures();
        }
    */
    public OptionFeature[] getSupportedFeatures() {
        return conf.getSupportedFeatures();
    }

    public boolean isFeatureSupported(OptionFeature feature) {
        return conf.isFeatureSupported(feature);
    }

    public boolean useResourceReservation() {
        return conf.useResourceReservation();
    }

    public XDMConfig getXDMConfig() {
        return doGetXdmConfig();
    }

    private XDMConfig doGetXdmConfig() {
        return xdmConfig == null ? conf.getXDMConfig() : xdmConfig;
    }

    public PrivacyInfo getPrivacyInfo() {
        return conf.getPrivacyInfo();
    }

    public UserInfo getPreferredIdentity() {
        return clientRegUser == null ? conf.getPreferredIdentity() : clientRegUser;
    }

    public boolean globalIpDiscovery() {
        return conf.globalIpDiscovery();
    }

    public int getMsrpChunkSize() {
        return conf.getMsrpChunkSize();
    }

    public int getMsrpLocalPort() {
        return conf.getMsrpLocalPort();
    }

    public void clientRegistryChanged(RegistryChangeEvent<ClientRegistry> event) {
    }

    public void commonRegistryChanged(RegistryChangeEvent<CommonRegistry> event) {
        Logger.log(TAG, "commonConfigChanged#");
        final AuthenticationProperty oldAuth = event.getOldConfig().getAuthenticationProperty();
        final AuthenticationProperty newAuth = event.getNewConfig().getAuthenticationProperty();
        if (newAuth != null && !newAuth.equals(oldAuth)) {
            Logger.log(TAG, "commonConfigChanged#auth property changed");

            final String username = newAuth.getUsername();
            final String password = newAuth.getPassword();

            this.clientAuthUser = createUserInfo(conf.getAuthUserName(), username);
            this.clientRegUser = createUserInfo(conf.getRegistrationName(), username);
            this.clientPassword = createUserPassword(conf.getUserPassword(), password);

/*            configuration.getAuthUserName().name = newAuth.getUsername();
            configuration.getRegistrationName().name = newAuth.getUsername();
            configuration.getPreferredIdentity().name = newAuth.getUsername();
            registrationIdentity.getUserInfo().name = newAuth.getUsername();
            configuration.getUserPassword().password = newAuth.getPassword();
            Logger.log(TAG, "commonConfigChanged#configuration updated");
*/
        }

        AuthenticationProperty oldXdmAuth = event.getOldConfig().getXdmAuthenticationProperty();
        AuthenticationProperty newXdmAuth = event.getNewConfig().getXdmAuthenticationProperty();
        if (newXdmAuth != null && !newXdmAuth.equals(oldXdmAuth)) {
            XDMConfig currentXdmConfig = doGetXdmConfig();
            XDMConfig updatedXdmConfig = new XDMConfigImpl(
                    currentXdmConfig == null ? null : currentXdmConfig.getXcapRoot(),
                    currentXdmConfig == null ? null : currentXdmConfig.getXuiName(),
                    newXdmAuth.getUsername(),
                    newXdmAuth.getPassword(),
                    currentXdmConfig == null ? false : currentXdmConfig.isSendFullDoc()
            );
            this.xdmConfig = updatedXdmConfig;
        }
    }

    private UserInfo createUserInfo(UserInfo matchUser, String userName) {
        return new UserInfo(matchUser.getSchema(), userName, matchUser.getDomain());
    }

    private UserPassword createUserPassword(UserPassword matchPassword, String userPassword) {
        return new UserPassword(matchPassword.getPasswordType(), userPassword);
    }

    
    public String toString() {
        return "CompositeConfig [conf=" + conf + "]";
    }

    public long getSessionExpiresTime() {
        return conf.getSessionExpiresTime();
    }

    public long getMinSessionExpiresTime() {
        return conf.getMinSessionExpiresTime();
    }

    public Refresher getRefresher() {
        return conf.getRefresher();
    }

    public boolean useInviteRefresh() {
        return conf.useInviteRefresh();
    }

    public boolean useSimultaneousConnections() {
        return conf.useSimultaneousConnections();
    }

    public FeatureMapper getFeatureMapper() {
        return conf.getFeatureMapper();
    }

    public Collection<String> getSpecialUris() {
        return conf.getSpecialUris();
    }

    /*public boolean forceSrtp() {
        return conf.forceSrtp();
    }*/

    public boolean useDNSLookup() {
        return conf.useDNSLookup();
    }

    public DtmfPayloadType getDtmfPayload() {
        return conf.getDtmfPayload();
    }
}
