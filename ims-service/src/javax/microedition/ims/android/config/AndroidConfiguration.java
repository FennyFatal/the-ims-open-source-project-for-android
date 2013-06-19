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

package javax.microedition.ims.android.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import javax.microedition.ims.DefaultFeatureMapper;
import javax.microedition.ims.FeatureMapper;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.android.auth.AKAAuthProviderAndroidImpl;
import javax.microedition.ims.android.config.ui.ListPreferenceMultiSelect;
import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.config.UserPassword;
import javax.microedition.ims.core.auth.AKAAuthProvider;
import javax.microedition.ims.core.sipservice.PrivacyInfo;
import javax.microedition.ims.core.sipservice.PrivacyInfoImpl;
import javax.microedition.ims.core.xdm.XDMConfig;
import javax.microedition.ims.core.xdm.XDMConfigImpl;
import javax.microedition.ims.messages.wrappers.sip.AuthType;
import javax.microedition.ims.messages.wrappers.sip.Refresher;
import java.util.*;

/**
 * This class responsible for reading configuration parameters from underlying
 * storage.
 *
 * @author Andrei Khomushko
 */
public class AndroidConfiguration implements Configuration,
        OnSharedPreferenceChangeListener {

    public static final String SIP_PROXY_HOST = "sip_proxy_host";
    public static final String SIP_PROXY_PORT = "sip_proxy_port";

    public static final String SIP_REGISTER_USERNAME = "sip_registrar_username";
    public static final String SIP_REGISTER_USERNAME_DOMAIN = "sip_registrar_username_domain";
    public static final String SIP_REGISTER_USERNAME_SHEMA = "sip_registrar_username_shema";

    public static final String SIP_PREFERRED_IDENTITY = "sip_preferred_identity";

    public static final String SIP_REGISTER_HOST = "sip_registrar_host";
    public static final String SIP_REGISTER_PORT = "sip_registrar_port";
    public static final String SIP_REGISTER_PROTOCOL_TYPE = "sip_registrar_protocol_type";
    public static final String SIP_GLOBAL_IP_DISCOVERY = "sip_global_ip_discovery";
    public static final String SIP_DNS_LOOKUP = "sip_dns_lookup";

    public static final String SIP_AUTH_TYPE = "sip_auth_type";
    public static final String SIP_AUTH_USERNAME_SCHEMA = "sip_auth_username_schema";
    public static final String SIP_AUTH_USERNAME = "sip_auth_username";
    public static final String SIP_AUTH_USERNAME_DOMAIN = "sip_auth_username_domain";
    public static final String SIP_AUTH_PASSWORD = "sip_auth_password";
    public static final String SIP_AUTH_REALM = "sip_auth_realm";
    public static final String SIP_AUTH_FORCE = "sip_auth_force";
    public static final String SIP_AUTH_SIMULTENEOUS = "sip_auth_simulteneous";
    public static final String SIP_AUTH_FORCES = "sip_auth_forces";

    public static final String SIP_USE_RPORT = "sip_use_rport";
    public static final String SIP_USE_RESOURCE_RESERVATION = "sip_use_resource_reservation";
    public static final String SIP_LOCAL_PORT = "sip_local_port";
    public static final String SIP_MAX_FORWARDS = "sip_max_forwards";
    public static final String SIP_USER_AGENT = "sip_user_agent";

    public static final String MSRP_CHUNK_SIZE = "key_msrp_chunk_size";
    public static final String MSRP_DEF_PORT = "key_msrp_local_port";

    public static final String SESSION_EXPIRES_TIME = "key_session_expires_time";
    public static final String MIN_SESSION_EXPIRES_TIME = "key_min_session_expires_time";
    public static final String REFRESHER = "key_refresher";
    public static final String SIP_USE_INVITE_REFRESH = "sip_use_invite_refresh";

    public static final String SIP_USE_SIMULTANEOUS_CONNECTION = "sip_use_simul_connection";

    public static final String XDM_XCAP_ROOT = "xdm_xcap_root";
    public static final String XDM_XUI_NAME = "xdm_xui_name";
    public static final String XDM_AUTH_NAME = "xdm_auth_name";
    public static final String XDM_PASSWORD = "xdm_password";
    public static final String XDM_SEND_FULL_DOC = "xdm_send_full_document";

    public static final String SIP_SUPPORTED_FEATURES = "sip_supported_features";
    public static final String SIP_SUPPORTED_100rel = "sip_supported_100rel";
    public static final String SIP_SUPPORTED_EVENTLIST = "sip_supported_eventlist";
    public static final String SIP_SUPPORTED_PATH = "sip_supported_path";

    public static final String SIP_EXPIRE_TIME = "sip_expire_time";
    public static final String SIP_REGISTER_EXPIRE_TIME = "sip_register_expire_time";
    public static final String SIP_SUBSCRIPTION_EXPIRE_TIME = "sip_subscription_expire_time";
    public static final String SIP_PUBLICATION_EXPIRE_TIME = "sip_publication_expire_time";

    public static final String SIP_REQUIRED_FEATURES = "sip_required_features";

    public static final String SIP_FORCE_SRTP = "sip_force_srtp";
    public static final String SIP_DTMF_PAYLOAD_TYPE = "sip_dtmf_payload_type";
    public static final String SIP_USE_FEATURE_TAGS = "sip_use_feature_tags";

    private final Context context;
    private final SharedPreferences preferences;

    private final AKAAuthProvider akaAuthProvider;

    private final FeatureMapper featureMapping = new DefaultFeatureMapper();

    private List<ConfigurationChangeListener> changeListeners = new ArrayList<ConfigurationChangeListener>();

    public AndroidConfiguration(Context context) {
        this.context = context;
        this.preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);
        akaAuthProvider = new AKAAuthProviderAndroidImpl(context);
    }

    private String getDefSettingValue(int defKey) {
        return context.getResources().getString(defKey);
    }

    private String getStringSettingValue(String prefKey, int defKey) {
        return preferences.getString(prefKey, getDefSettingValue(defKey));
    }

    private boolean getBooleanSettingValue(String prefKey, int defKey) {
        return preferences.getBoolean(prefKey,
                Boolean.parseBoolean(getDefSettingValue(defKey)));
    }

    public ServerAddress getProxyServer() {
        String host = getStringSettingValue(SIP_PROXY_HOST,
                R.string.def_proxy_host);
        String port = getStringSettingValue(SIP_PROXY_PORT,
                R.string.def_proxy_port);
        return new ServerAddress(host, Integer.parseInt(port));
    }

    public UserInfo getRegistrationName() {
        String prefix = getStringSettingValue(SIP_REGISTER_USERNAME_SHEMA, R.string.def_registrar_user_name_schema);
        String username = getStringSettingValue(SIP_REGISTER_USERNAME, R.string.def_registrar_user_name);
        String domain = getStringSettingValue(SIP_REGISTER_USERNAME_DOMAIN, R.string.def_registrar_user_name_domain);

        return new UserInfo(prefix, username, domain);
    }

    public UserInfo getPreferredName() {
        String identity = getStringSettingValue(SIP_PREFERRED_IDENTITY, R.string.def_preferred_identity).trim();

        UserInfo retValue;
        try {
            retValue = StringUtils.isEmpty(identity) ? null : UserInfo.valueOf(identity);
        } catch (Exception e) {
            //if can't parse preferred value just use null value
            Logger.log(Logger.Tag.WARNING, "" + e);
            retValue = null;
        }

        return retValue;
    }

    public ServerAddress getRegistrarServer() {
        AuthType type = getUserPassword().getPasswordType();
        String host;
        //if (AuthType.AKA == type) {
        //    host = akaAuthProvider.getHomeNetworkDomain();
        //} else {
            host = getStringSettingValue(SIP_REGISTER_HOST,
                R.string.def_registrar_host);
        //}
        String port = getStringSettingValue(SIP_REGISTER_PORT,
                R.string.def_registrar_port);
        return new ServerAddress(host, Integer.parseInt(port));
    }

    public ServerAddress getRegistrarServerSettings() {
        String host = getStringSettingValue(SIP_REGISTER_HOST,
                R.string.def_registrar_host);
        String port = getStringSettingValue(SIP_REGISTER_PORT,
                R.string.def_registrar_port);
        return new ServerAddress(host, Integer.parseInt(port));
    }

    public int getLocalPort() {
        return Integer.parseInt(getStringSettingValue(SIP_LOCAL_PORT,
                R.string.def_local_port));
    }

    public String getRealm() {
        AuthType type = getUserPassword().getPasswordType();
       // if (AuthType.AKA == type) {
        //    return akaAuthProvider.getHomeNetworkDomain();
      //  }
      //  else
            return getStringSettingValue(SIP_AUTH_REALM, R.string.def_realm);
    }

    public String getRealmSettings() {
        return getStringSettingValue(SIP_AUTH_REALM, R.string.def_realm);
    }

    public String getUserAgent() {
        return getStringSettingValue(SIP_USER_AGENT, R.string.def_user_agent);
    }

    public UserInfo getAuthUserName() {
        String schema = getStringSettingValue(SIP_AUTH_USERNAME_SCHEMA,
                R.string.def_auth_user_name_prefix);
        String username = getStringSettingValue(SIP_AUTH_USERNAME,
                R.string.def_auth_user_name_info);
        String domain = getStringSettingValue(SIP_AUTH_USERNAME_DOMAIN,
                R.string.def_auth_user_name_domain);
        return new UserInfo(schema, username, domain);
    }

    public UserPassword getUserPassword() {
        String defAuthType = getStringSettingValue(SIP_AUTH_TYPE,
                R.string.def_auth_type);
        AuthType authType = AuthType.parse(defAuthType);
        String password = getStringSettingValue(SIP_AUTH_PASSWORD,
                R.string.def_auth_user_password);
        return new UserPassword(authType, password);
    }

    public Protocol getConnectionType() {
        String protocolType = getStringSettingValue(SIP_REGISTER_PROTOCOL_TYPE,
                R.string.def_connection_type);
        return Enum.valueOf(Protocol.class, protocolType);
    }

    public int getMaxForwards() {
        String maxForwards = getStringSettingValue(SIP_MAX_FORWARDS,
                R.string.def_max_forwards);
        return Integer.parseInt(maxForwards);
    }

    public boolean useRPort() {
        return getBooleanSettingValue(SIP_USE_RPORT, R.string.def_use_rport);
    }

    public boolean forceSrtp() {
        return getBooleanSettingValue(SIP_FORCE_SRTP, R.string.def_force_srtp);
    }

    @Override
    public boolean useDNSLookup() {
        return getBooleanSettingValue(SIP_DNS_LOOKUP, R.string.def_dns_lookup);
    }

    public boolean useResourceReservation() {
        return getBooleanSettingValue(SIP_USE_RESOURCE_RESERVATION,
                R.string.def_use_resource_reservation);
    }
/*
    public boolean forceAuthorization() {
        return getBooleanSettingValue(SIP_AUTH_FORCE, R.string.def_auth_force);
    }
*/

    //use feature tags in INVITE message
    public boolean useFeatureTags() {
        return getBooleanSettingValue(SIP_USE_FEATURE_TAGS,
                R.string.def_use_feature_tags);
    }

    public boolean useSimultaneousAuth() {
        return getBooleanSettingValue(SIP_AUTH_SIMULTENEOUS, R.string.def_auth_simulteneous);
    }

    public Collection<ChallengeType> getAuthForceTypes() {
        final Set<ChallengeType> retValue = new HashSet<ChallengeType>();

        String authForces = getStringSettingValue(SIP_AUTH_FORCES, R.string.def_auth_forces);
        String[] forcesTypes = ListPreferenceMultiSelect.parseStoredValue(authForces);
        for (String forceType : forcesTypes) {
            ChallengeType challengeType = ChallengeType.valueOf(forceType.trim());
            retValue.add(challengeType);
        }
        return retValue;
    }


    public void addConfigurationListener(ConfigurationChangeListener listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    public void removeConfigurationListener(ConfigurationChangeListener listener) {
        if (listener != null) {
            changeListeners.remove(listener);
        }
    }

    private void notifyListeners(String key) {
        for (ConfigurationChangeListener listener : changeListeners) {
            listener.onConfigurationChanged(key);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        notifyListeners(key);
    }

    public long getRegistrationExpirationSeconds() {
        String regExpireTime = getStringSettingValue(SIP_REGISTER_EXPIRE_TIME,
                R.string.def_registrar_expire_time);
        return Long.parseLong(regExpireTime);
    }

    public long getSubscriptionExpirationSeconds() {
        String subExpireTime = getStringSettingValue(SIP_SUBSCRIPTION_EXPIRE_TIME,
                R.string.def_subscription_expire_time);
        return Long.parseLong(subExpireTime);
    }

    public long getPublicationExpirationSeconds() {
        String pubExpireTime = getStringSettingValue(SIP_PUBLICATION_EXPIRE_TIME,
                R.string.def_publication_expire_time);
        return Long.parseLong(pubExpireTime);
    }

    public void free() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /*
     *  public boolean isPrackSupported() { return
     * getBooleanSettingValue(SIP_PRACK_SUPPORTED,
     * R.string.def_prack_supported); }
     */

    public OptionFeature[] getSupportedFeatures() {
        List<OptionFeature> supportedFeatures = getFeatures();
        return supportedFeatures.toArray(new OptionFeature[supportedFeatures.size()]);
    }

    private List<OptionFeature> getFeatures() {
        List<OptionFeature> supportedFeatures = new ArrayList<OptionFeature>();

        boolean is100Relsupported = getBooleanSettingValue(SIP_SUPPORTED_100rel,
                R.string.def_supported_100rel);
        if (is100Relsupported) {
            supportedFeatures.add(OptionFeature._100REL);
        }

        boolean isEventlistSupported = getBooleanSettingValue(
                SIP_SUPPORTED_EVENTLIST, R.string.def_supported_eventlist);
        if (isEventlistSupported) {
            supportedFeatures.add(OptionFeature.EVENTLIST);
        }

        boolean isTimerSupported = getBooleanSettingValue(SIP_USE_INVITE_REFRESH,
                R.string.def_use_invite_refresh);
        if (isTimerSupported) {
            supportedFeatures.add(OptionFeature.TIMER);
        }

        boolean isPathSupported = getBooleanSettingValue(SIP_SUPPORTED_PATH,
                R.string.def_supported_path);
        if (isPathSupported) {
            supportedFeatures.add(OptionFeature.PATH);
        }
        return supportedFeatures;
    }

    /*
     *  public String[] getRequiredFeatures() { List<String>
     * requiredFeatures = new ArrayList<String>();
     * 
     * boolean supported100Rel = getBooleanSettingValue(SIP_REQUIRED_100rel,
     * R.string.def_required_100rel); if(supported100Rel) {
     * requiredFeatures.add(_100REL); } return requiredFeatures.toArray(new
     * String[0]); }
     */

    public boolean globalIpDiscovery() {
        return getBooleanSettingValue(SIP_GLOBAL_IP_DISCOVERY,
                R.string.def_global_ip_discovery);
    }

    public XDMConfig getXDMConfig() {
        String xcapRoot = getStringSettingValue(XDM_XCAP_ROOT,
                R.string.def_xdm_xcap_root);

        String xuiName = getStringSettingValue(XDM_XUI_NAME,
                R.string.def_xdm_xui_name);

        String xdmAuthName = getStringSettingValue(XDM_AUTH_NAME,
                R.string.def_xdm_auth_name);

        String xdmPassword = getStringSettingValue(XDM_PASSWORD,
                R.string.def_xdm_password);

        boolean xdmSendFullDoc = getBooleanSettingValue(XDM_SEND_FULL_DOC,
                R.string.def_xdm_send_full_document);

        return new XDMConfigImpl(xcapRoot, xuiName, xdmAuthName, xdmPassword, xdmSendFullDoc);
    }

    public PrivacyInfo getPrivacyInfo() {
        return PrivacyInfoImpl.NONE;
    }

    public UserInfo getPreferredIdentity() {
        return getPreferredName();
    }

    public int getMsrpLocalPort() {
        return Integer.parseInt(getStringSettingValue(MSRP_DEF_PORT,
                R.string.def_msrp_local_port));
    }

    public int getMsrpChunkSize() {
        return Integer.parseInt(getStringSettingValue(MSRP_CHUNK_SIZE,
                R.string.def_msrp_chunk_size));
    }

    public long getSessionExpiresTime() {
        return Long.parseLong(getStringSettingValue(SESSION_EXPIRES_TIME,
                R.string.def_session_expires_time));
    }

    public long getMinSessionExpiresTime() {
        return Long.parseLong(getStringSettingValue(MIN_SESSION_EXPIRES_TIME,
                R.string.def_min_session_expires_time));
    }

    public Refresher getRefresher() {
        String protocolType = getStringSettingValue(REFRESHER,
                R.string.def_refresher);
        return Enum.valueOf(Refresher.class, protocolType);
    }

    public boolean useInviteRefresh() {
        return getBooleanSettingValue(SIP_USE_INVITE_REFRESH,
                R.string.def_use_invite_refresh);
    }

    public boolean useSimultaneousConnections() {
        return getBooleanSettingValue(SIP_USE_SIMULTANEOUS_CONNECTION,
                R.string.def_use_simul_connection);
    }

    public FeatureMapper getFeatureMapper() {
        return featureMapping;
    }

    public Collection<String> getSpecialUris() {
        return Arrays.asList("urn:service:sos");
    }

    public boolean isFeatureSupported(OptionFeature feature) {
        return getFeatures().contains(feature);
    }
    
    public DtmfPayloadType getDtmfPayload() {
        String dtmfPayloadType = getStringSettingValue(SIP_DTMF_PAYLOAD_TYPE,
                R.string.def_dtmf_payload_type);
        return DtmfPayloadType.parse(dtmfPayloadType);
    }
}
