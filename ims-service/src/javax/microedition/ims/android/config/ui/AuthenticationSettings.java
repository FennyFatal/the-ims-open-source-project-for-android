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
import javax.microedition.ims.messages.wrappers.sip.AuthType;

/**
 * This class represent configuration for authentication.
 *
 * @author Andrei Khomushko
 */
public class AuthenticationSettings extends PreferenceActivity implements ConfigurationChangeListener {
    private AndroidConfiguration configuration;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sip_authentication_settings);

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
        initUserSchemaControl();
        initUserNameControl();
        initUserDomainControl();

        initPasswordControl();
        initRealmControl();
        initAuthTypeControl();
        //initAuthForceControl();
        initAuthSimulteneousControl();
        initAuthForcesControl();
    }

    private void initAuthTypeControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_AUTH_TYPE);
        AuthType authType = configuration.getUserPassword().getPasswordType();
        preference.setSummary(authType.stringValue());
    }


    private void initUserSchemaControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_AUTH_USERNAME_SCHEMA);
        preference.setSummary(configuration.getAuthUserName().getSchema());
    }

    private void initUserNameControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_AUTH_USERNAME);
        preference.setSummary(configuration.getAuthUserName().getName());
    }

    private void initUserDomainControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_AUTH_USERNAME_DOMAIN);
        preference.setSummary(configuration.getAuthUserName().getDomain());
    }


    private void initPasswordControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_AUTH_PASSWORD);
        preference.setSummary(configuration.getUserPassword().getPassword().replaceAll(".", "*"));
    }

    private void initRealmControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_AUTH_REALM);
        preference.setSummary(configuration.getRealmSettings());
    }

/*    private void initAuthForceControl() {
        Preference preference = findPreference(SIP_AUTH_FORCE);
        preference.setSummary(String.valueOf(configuration.forceAuthorization()));
    }
*/

    private void initAuthSimulteneousControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_AUTH_SIMULTENEOUS);
        preference.setSummary(String.valueOf(configuration.useSimultaneousAuth()));
    }


    private void initAuthForcesControl() {
        Preference preference = findPreference(AndroidConfiguration.SIP_AUTH_FORCES);
        preference.setSummary(configuration.getAuthForceTypes().toString());
    }

    public void onConfigurationChanged(String key) {

        if (AndroidConfiguration.SIP_AUTH_USERNAME_SCHEMA.equals(key)) {
            initUserSchemaControl();
        }
        else if (AndroidConfiguration.SIP_AUTH_USERNAME.equals(key)) {
            initUserNameControl();
        }
        else if (AndroidConfiguration.SIP_AUTH_USERNAME_DOMAIN.equals(key)) {
            initUserDomainControl();

        }
        else if (AndroidConfiguration.SIP_AUTH_PASSWORD.equals(key)) {
            initPasswordControl();
        }
        else if (AndroidConfiguration.SIP_AUTH_REALM.equals(key)) {
            initRealmControl();
        }
        else if (AndroidConfiguration.SIP_AUTH_TYPE.equals(key)) {
            initAuthTypeControl();
        } /*else if(AndroidConfiguration.SIP_AUTH_FORCE.equals(key)) {
            initAuthForceControl();
        } */
        else if (AndroidConfiguration.SIP_AUTH_SIMULTENEOUS.equals(key)) {
            initAuthSimulteneousControl();
        }
        else if (AndroidConfiguration.SIP_AUTH_FORCES.equals(key)) {
            initAuthForcesControl();
        }
    }
}
