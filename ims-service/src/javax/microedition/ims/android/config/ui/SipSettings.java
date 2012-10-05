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

package javax.microedition.ims.android.config.ui;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.android.config.AndroidConfiguration;
import javax.microedition.ims.android.config.ConfigurationChangeListener;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.OptionFeature;
import javax.microedition.ims.common.ServerAddress;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.core.xdm.XDMConfig;

import static javax.microedition.ims.android.config.AndroidConfiguration.*;

/**
 * This class represent general configuration for sip stack.
 *
 * @author Andrei Khomushko
 */
public class SipSettings extends PreferenceActivity implements ConfigurationChangeListener {
    private static final String TAG = "SipSettings";
    private static final String SIP_PROXY_SERVER = "sip_proxy_server";
    private static final String SIP_REGISTRAR_SERVER = "sip_registrar_server";
    private static final String SIP_AUTHENTICATION = "sip_authentication";
    private static final String XDM_SERVER = "xdm_server";
    private static final String MSRP_DATA = "msrp_data";
    private static final String INVITE_REFRESH_DATA = "invite_refresh_data";

    private AndroidConfiguration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sip_settings);

        configuration = new AndroidConfiguration(this);
        configuration.addConfigurationListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initControls();
    }

    @Override
    protected void onDestroy() {
        configuration.removeConfigurationListener(this);
        configuration.free();
        super.onDestroy();
    }

    private void initControls() {
        initProxyControl();
        initMsrpControl();
        initRegistrarControl();
        initAuthenticationControl();
        initXDMServerControl();
        initUseRPortControl();
        //initForceSrtpControl();
        initUseResourceReservationControl();
        initLocalPortControl();
        initMaxForwardsControl();
        initUserAgentControl();
        //initPrackSupported();
        initSupportedFeatures();
        initExpireTime();
        //initRequiredFeatures();
        initInviteRefreshControl();
        initUseSimulConnectionControl();
        initDtmfControl();
    }

    private void initProxyControl() {
        ServerAddress proxyServer = configuration.getProxyServer();
        Preference preference = findPreference(SIP_PROXY_SERVER);
        preference.setSummary(String.format("%s:%s", proxyServer.getAddress(), proxyServer.getPort()));
    }

    private void initMsrpControl() {
        Preference preference = findPreference(MSRP_DATA);
        preference.setSummary(String.format("%s:%s", configuration.getMsrpLocalPort(), configuration.getMsrpChunkSize()));
    }

    private void initInviteRefreshControl() {
        Preference preference = findPreference(INVITE_REFRESH_DATA);
        if (configuration.useInviteRefresh()) {
            preference.setSummary(String.format("%s:%s:%s", configuration.getRefresher(),
                    configuration.getSessionExpiresTime(), configuration.getMinSessionExpiresTime()));
        }
        else {
            preference.setSummary("off");
        }
    }

    private void initRegistrarControl() {
        ServerAddress regServer = configuration.getRegistrarServerSettings();
        UserInfo regName = configuration.getRegistrationName();
        Preference preference = findPreference(SIP_REGISTRAR_SERVER);
        preference.setSummary(String.format("%s:%s, %s:%s", regServer.getAddress(), regServer.getPort(), regName.getSchema(), regName.getName()));
    }

    private void initAuthenticationControl() {
        Preference preference = findPreference(SIP_AUTHENTICATION);
        preference.setSummary(String.format("%s, %s", configuration.getAuthUserName().getName(), configuration.getAuthUserName().getDomain()));
    }

    private void initXDMServerControl() {
        Preference preference = findPreference(XDM_SERVER);
        XDMConfig xdmConfig = configuration.getXDMConfig();
        preference.setSummary(String.format("%s, %s", xdmConfig.getXcapRoot(), xdmConfig.getXuiName()));
    }

    private void initUseRPortControl() {
        Preference preference = findPreference(SIP_USE_RPORT);
        preference.setSummary(String.valueOf(configuration.useRPort()));
    }

    /*private void initForceSrtpControl() {
        Preference preference = findPreference(SIP_FORCE_SRTP);
        preference.setSummary(String.valueOf(configuration.forceSrtp()));
    }*/

    private void initDtmfControl() {
        Preference preference = findPreference(SIP_DTMF_PAYLOAD_TYPE);
        preference.setSummary(String.valueOf(configuration.getDtmfPayload().name()));
    }

    private void initUseResourceReservationControl() {
        Preference preference = findPreference(SIP_USE_RESOURCE_RESERVATION);
        preference.setSummary(String.valueOf(configuration.useResourceReservation()));
    }

    private void initUseSimulConnectionControl() {
        Preference preference = findPreference(SIP_USE_SIMULTANEOUS_CONNECTION);
        preference.setSummary(String.valueOf(configuration.useSimultaneousConnections()));
    }

    /*    private void initPrackSupported() {
         Preference preference = findPreference(SIP_PRACK_SUPPORTED);
         preference.setSummary(Boolean.toString(configuration.isPrackSupported()));
     }
      */

    private void initSupportedFeatures() {
        Preference preference = findPreference(SIP_SUPPORTED_FEATURES);
        String supportedFeatures = concatenate(configuration.getSupportedFeatures());
        preference.setSummary(supportedFeatures);
    }

    private void initExpireTime() {
        Preference preference = findPreference(SIP_EXPIRE_TIME);
        preference.setSummary(String.format("%s:%s:%s", configuration.getRegistrationExpirationSeconds(),
                configuration.getSubscriptionExpirationSeconds(), configuration.getPublicationExpirationSeconds()));
    }

    /*    private void initRequiredFeatures() {
         Preference preference = findPreference(SIP_REQUIRED_FEATURES);
         String requiredFeatures = concatenate(configuration.getRequiredFeatures());
         preference.setSummary(requiredFeatures);
     }
      */

    private void initUseFeatureTags() {
        Preference preference = findPreference(SIP_USE_FEATURE_TAGS);
        preference.setSummary(String.valueOf(configuration.useFeatureTags()));
    }

    //TODO need to move in util class

    private static <S> String concatenate(OptionFeature[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, count = values.length; i < count; i++) {
            sb.append(values[i].getName());
            if (i != count - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private void initLocalPortControl() {
        Preference preference = findPreference(SIP_LOCAL_PORT);
        preference.setSummary(String.valueOf(configuration.getLocalPort()));
    }

    private void initMaxForwardsControl() {
        Preference preference = findPreference(SIP_MAX_FORWARDS);
        preference.setSummary(String.valueOf(configuration.getMaxForwards()));
    }

    private void initUserAgentControl() {
        Preference preference = findPreference(SIP_USER_AGENT);
        preference.setSummary(configuration.getUserAgent());
    }

    public void onConfigurationChanged(final String key) {
        if (SIP_PROXY_SERVER.equals(key)) {
            initProxyControl();
        }
        else if (MSRP_DATA.equals(key)) {
            initMsrpControl();
        }
        else if (INVITE_REFRESH_DATA.equals(key)) {
            initInviteRefreshControl();
        }
        else if (SIP_REGISTRAR_SERVER.equals(key)) {
            initRegistrarControl();
        }
        else if (SIP_AUTHENTICATION.equals(key)) {
            initAuthenticationControl();
        }
        else if (XDM_SERVER.equals(key)) {
            initXDMServerControl();
        }
        else if (SIP_USE_RPORT.equals(key)) {
            initUseRPortControl();
        }
        /*else if (SIP_FORCE_SRTP.equals(key)) {
            initForceSrtpControl();
        }*/
        else if (SIP_DTMF_PAYLOAD_TYPE.equals(key)) {
            initDtmfControl();
        }
        else if (SIP_USE_RESOURCE_RESERVATION.equals(key)) {
            initUseResourceReservationControl();
        }
        else if (SIP_USE_SIMULTANEOUS_CONNECTION.equals(key)) {
            initUseSimulConnectionControl();
        }
        else if (SIP_LOCAL_PORT.equals(key)) {
            initLocalPortControl();
        }
        else if (SIP_MAX_FORWARDS.equals(key)) {
            initMaxForwardsControl();
        }
        else if (SIP_USER_AGENT.equals(key)) {
            initUserAgentControl();
        }
        else if (SIP_SUPPORTED_FEATURES.equals(key)) {
            initSupportedFeatures();
        }
        else if (SIP_EXPIRE_TIME.equals(key)) {
            initExpireTime();
        }
        else if (SIP_REQUIRED_FEATURES.equals(key)) {
            //initRequiredFeatures();
        }
        else if (SIP_USE_FEATURE_TAGS.equals(key)) {
            initUseFeatureTags();
        }
        else {
            Logger.log(TAG, "Undefined key: " + key);
        }
    }
}
