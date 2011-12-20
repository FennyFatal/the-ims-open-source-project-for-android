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
