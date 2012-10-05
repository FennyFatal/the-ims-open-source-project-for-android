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
        Logger.log(Logger.Tag.WARNING, "getConnectionData: " + connectionData.get());
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
