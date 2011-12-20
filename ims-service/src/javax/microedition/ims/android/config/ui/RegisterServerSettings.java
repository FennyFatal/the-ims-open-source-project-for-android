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
import javax.microedition.ims.config.UserInfo;

import static javax.microedition.ims.android.config.AndroidConfiguration.SIP_DNS_LOOKUP;
import static javax.microedition.ims.android.config.AndroidConfiguration.SIP_GLOBAL_IP_DISCOVERY;

/**
 * This class represent configuration for register server.
 *
 * @author Andrei Khomushko
 */
public class RegisterServerSettings extends PreferenceActivity implements ConfigurationChangeListener {
    private AndroidConfiguration configuration;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sip_registrar_settings);

        configuration = new AndroidConfiguration(this);
        configuration.addConfigurationListener(this);

        initControls();
    }

    protected void onDestroy() {
        configuration.removeConfigurationListener(this);
        configuration.free();
        super.onDestroy();
    }

    private void initControls() {
        initUserNameSchemaControl();
        initUserPreferredIdentityControl();
        initUserNameControl();
        initUserNameDomainControl();
        initHostControl();
        initPortControl();
        initConnectionTypeControl();
        initExpireTimeControl();
        initDNSLookup();
        initGlobalIpDiscovery();
    }

    private void initHostControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_REGISTER_HOST);
        preference.setSummary(configuration.getRegistrarServer().getAddress());
    }


    private void initPortControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_REGISTER_PORT);
        preference.setSummary(String.valueOf(configuration.getRegistrarServer().getPort()));
    }

    private void initUserNameControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_REGISTER_USERNAME);
        preference.setSummary(configuration.getRegistrationName().getName());
    }

    private void initUserNameDomainControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_REGISTER_USERNAME_DOMAIN);
        preference.setSummary(configuration.getRegistrationName().getDomain());
    }

    private void initUserNameSchemaControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_REGISTER_USERNAME_SHEMA);
        preference.setSummary(configuration.getRegistrationName().getSchema());
    }

    private void initUserPreferredIdentityControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_PREFERRED_IDENTITY);

        UserInfo identity = configuration.getPreferredIdentity();
        preference.setSummary(identity != null ? identity.toFullName() : "");
    }

    private void initConnectionTypeControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_REGISTER_PROTOCOL_TYPE);
        preference.setSummary(configuration.getConnectionType().toString());
    }

    private void initExpireTimeControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_REGISTER_EXPIRE_TIME);
        preference.setSummary(String.valueOf(configuration.getRegistrationExpirationSeconds()));
    }

    private void initDNSLookup() {
        Preference preference = findPreference(SIP_DNS_LOOKUP);
        preference.setSummary(Boolean.toString(configuration.useDNSLookup()));
    }

    private void initGlobalIpDiscovery() {
        Preference preference = findPreference(SIP_GLOBAL_IP_DISCOVERY);
        preference.setSummary(Boolean.toString(configuration.globalIpDiscovery()));
    }

    public void onConfigurationChanged(String key) {
        if (AndroidConfiguration.SIP_REGISTER_HOST.equals(key)) {
            initHostControl();
        } else if (AndroidConfiguration.SIP_REGISTER_PORT.equals(key)) {
            initPortControl();
        } else if (AndroidConfiguration.SIP_REGISTER_PROTOCOL_TYPE.equals(key)) {
            initConnectionTypeControl();
        } else if (AndroidConfiguration.SIP_REGISTER_EXPIRE_TIME.equals(key)) {
            initExpireTimeControl();
        } else if (AndroidConfiguration.SIP_REGISTER_USERNAME.equals(key)) {
            initUserNameControl();
        } else if (AndroidConfiguration.SIP_REGISTER_USERNAME_DOMAIN.equals(key)) {
            initUserNameDomainControl();
        } else if (AndroidConfiguration.SIP_REGISTER_USERNAME_SHEMA.equals(key)) {
            initUserNameSchemaControl();
        } else if (AndroidConfiguration.SIP_DNS_LOOKUP.equals(key)) {
            initDNSLookup();
        } else if (AndroidConfiguration.SIP_GLOBAL_IP_DISCOVERY.equals(key)) {
            initGlobalIpDiscovery();
        } else if (AndroidConfiguration.SIP_PREFERRED_IDENTITY.equals(key)) {
            initUserPreferredIdentityControl();
        } else {
            assert false;
        }
    }
}
