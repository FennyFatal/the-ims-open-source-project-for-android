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
package javax.microedition.ims.core.connection;

import javax.microedition.ims.common.ConnectionData;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.dns.DNSResolver;
import javax.microedition.ims.transport.impl.ConnectionSecurityInfo;
import javax.microedition.ims.transport.impl.ConnectionSecurityInfoProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 3/17/11
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionSecurityInfoProviderImplDefault implements ConnectionSecurityInfoProvider {

    private final String TAG = "ConnectionSecurityInfoProviderImplDefault";

    private final DNSResolver dnsResolver;
    private final UserInfo registrationName;
    private ConnectionSecurityInfo.AddrCheckMode checkMode;

    private final AtomicReference<List<String>> allowedRemoteAddresses = new AtomicReference<List<String>>();

    public ConnectionSecurityInfoProviderImplDefault(
            final DNSResolver dnsResolver,
            final UserInfo registrationName) {

        this(dnsResolver, registrationName, null);
    }

    public ConnectionSecurityInfoProviderImplDefault(
            final DNSResolver dnsResolver,
            final UserInfo registrationName,
            final Configuration configuration) {

        this.dnsResolver = dnsResolver;
        this.registrationName = registrationName;
        this.checkMode = (configuration != null && configuration.useDNSLookup()) ?
                ConnectionSecurityInfo.AddrCheckMode.CHECK_EVERY_ADDRESS :
                ConnectionSecurityInfo.AddrCheckMode.ALLOW_ALL;
    }

    @Override
	public ConnectionSecurityInfo obtainSecurityInfo() {
        Logger.log(TAG, "Obtaining connection security info");
        return new ConnectionSecurityInfoImplDefault(this.allowedRemoteAddresses.get(), checkMode);
    }

    @Override
        public void onConnectiondataChanged(ConnectionData connectionData) {
            Logger.log(TAG, String.format("onConnectiondataChanged, connectionData to be used further is %s", connectionData));

            List<String> allowedRemoteAddresses;
            if (ConnectionSecurityInfo.AddrCheckMode.CHECK_EVERY_ADDRESS == checkMode) {
                allowedRemoteAddresses = Arrays.asList(connectionData.getAddress());
            } else {
                allowedRemoteAddresses = Collections.emptyList();
            }

            this.allowedRemoteAddresses.set(allowedRemoteAddresses);
    }

    @Override
    public String toString() {
        return "ConnectionSecurityInfoProviderImplDefault{" +
                "dnsResolver=" + dnsResolver +
                ", registrationName=" + registrationName +
                '}';
    }
}
