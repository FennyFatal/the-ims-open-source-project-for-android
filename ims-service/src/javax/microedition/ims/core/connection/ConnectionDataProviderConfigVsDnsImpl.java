package javax.microedition.ims.core.connection;

import javax.microedition.ims.common.ConnectionData;
import javax.microedition.ims.common.ConnectionDataDefaultImpl;
import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Logger.Tag;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.dns.DNSException;
import javax.microedition.ims.dns.DNSResolver;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 3/4/11
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionDataProviderConfigVsDnsImpl implements ConnectionDataProvider {

    private final Configuration configuration;
    private final DNSResolver dnsResolver;
    private final AtomicReference<ConnectionData> connectionData = new AtomicReference<ConnectionData>(null);
    
    private final ListenerHolder<ConnectionDataListener> listenerSupport = new ListenerHolder<ConnectionDataListener>(ConnectionDataListener.class);

    public ConnectionDataProviderConfigVsDnsImpl(
            final Configuration configuration,
            final DNSResolver dnsResolver) {

        this.configuration = configuration;
        this.dnsResolver = dnsResolver;
    }

    @Override
    public void refresh() throws IMSStackException {
        Logger.log(Logger.Tag.WARNING, "Connection data before refresh " + connectionData);
        ConnectionData newConnectionData = obtainConnectionData();
        ConnectionData oldConnectionData = connectionData.get();
        
        this.connectionData.set(newConnectionData);
        
        Logger.log(Logger.Tag.WARNING, String.format("Connection refreshed, old connection data is %s, new connection data is %s", oldConnectionData, newConnectionData));
        listenerSupport.getNotifier().onConnectiondataChanged(newConnectionData);
    }

    private ConnectionData obtainConnectionData() throws IMSStackException {
        ConnectionData retValue;

        Logger.log(Logger.Tag.WARNING, "*** configuration.useDNSLookup() = " + configuration.useDNSLookup());
        if (configuration.useDNSLookup()) {
            try {
                Logger.log(Logger.Tag.WARNING, "*** dnsResolver = " + dnsResolver);
                retValue = dnsResolver.lookUp(configuration.getRegistrationName());
            } catch (DNSException e) {
                throw new IMSStackException(e);
            }
        } else {
            retValue = new ConnectionDataDefaultImpl.Builder()
                    .address(configuration.getProxyServer().getAddress())
                    .port(configuration.getProxyServer().getPort())
                    .protocol(configuration.getConnectionType())
                    .build();
        }

        Logger.log(Logger.Tag.WARNING, "*** retValue (ConnectionData) = " + retValue);

        return retValue;
    }

    @Override
    public ConnectionData getConnectionData() {
        return connectionData.get();
    }
    
    @Override
    public DNSResolver getDNSResolver() {
        return dnsResolver;
    }
    
    @Override
    public void addListener(ConnectionDataListener listener) {
        listenerSupport.addListener(listener);
    }
    
    @Override
    public void removeListener(ConnectionDataListener listener) {
        listenerSupport.removeListener(listener);
    }

    @Override
    public String toString() {
        return "ConnectionDataProviderConfigVsDnsImpl{" +
                "configuration=" + configuration +
                ", dnsResolver=" + dnsResolver +
                ", connectionData=" + connectionData +
                '}';
    }
}
