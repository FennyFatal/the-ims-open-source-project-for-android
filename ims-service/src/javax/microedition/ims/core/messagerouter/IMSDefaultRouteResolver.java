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

package javax.microedition.ims.core.messagerouter;

import javax.microedition.ims.common.ConnectionData;
import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.connection.ConnectionDataProvider;
import javax.microedition.ims.core.connection.ConnectionDataProviderConfigVsDnsImpl;
import javax.microedition.ims.transport.impl.DefaultRoute;
import javax.microedition.ims.transport.impl.DefaultRouteResolver;
import javax.microedition.ims.transport.messagerouter.Route;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 26.4.2010
 * Time: 11.41.02
 */
class IMSDefaultRouteResolver implements DefaultRouteResolver {
    private final static String TAG = "IMSDefaultRouteResolver";

    //private static final int FAULT_TRY_COUNT = 1;
    private final Map<IMSEntityType, Route> defaultRouteMap = new ConcurrentHashMap<IMSEntityType, Route>(new HashMap<IMSEntityType, Route>());
    private final Configuration config;
    private final ConnectionDataProvider connDataProvider;

    //private final AtomicInteger faultCountBeforeRefresh = new AtomicInteger(FAULT_TRY_COUNT);
    
    public IMSDefaultRouteResolver(final Configuration config, final ConnectionDataProvider connDataProvider) {
        this.config = config;
        this.connDataProvider = connDataProvider;
    }

    private static Route createDefaultRoute(
            final Configuration config,
            final ConnectionData connectionData,
            final IMSEntityType imsEntityType) {

        Logger.log(TAG, "createDefaultRoute: Protocol: " + connectionData.getProtocol());
        return new DefaultRoute(
                connectionData.getAddress(),
                connectionData.getPort(),
                config.getLocalPort(),
                connectionData.getProtocol(),
                imsEntityType
        );
    }

    /*    private static Route createMsrpDefaultRoute(Configuration config) {
            return new DefaultRoute(
                    config.getProxyServer().getAddress(),
                    config.getProxyServer().getPort(),
                    config.getMsrpLocalPort(),
                    config.getConnectionType(),
                    IMSEntityType.MSRP
            );
        }
    */

    public Route getDefaultRoute(final IMSMessage msg) {
        return doGetDefaultRoute(msg.getEntityType());
    }


    public Route getDefaultRoute(final IMSEntityType entityType) {
        return doGetDefaultRoute(entityType);
    }

    private Route doGetDefaultRoute(final IMSEntityType entityType) {
        Logger.log(TAG, "doGetDefaultRoute: " + entityType);
        ConcurrentHashMap<IMSEntityType, Route> routeMap = (ConcurrentHashMap<IMSEntityType, Route>) defaultRouteMap;
        Route retValue;

        if (!routeMap.containsKey(entityType)) {
            Logger.log(TAG, "doGetDefaultRoute: !routeMap.containsKey(" + entityType + ")");
            routeMap.putIfAbsent(
                    entityType,
                    createDefaultRoute(
                            config,
                            connDataProvider.getConnectionData(),
                            entityType
                    )
            );
        }
        retValue = defaultRouteMap.get(entityType);
        Logger.log(TAG, "doGetDefaultRoute: retValue = " + retValue);

        return retValue;
    }
    
    void resetRoutes() throws IMSStackException {
        Logger.log(Logger.Tag.WARNING, "Refreshing the connection data in case useDNSLookup is set");

        //if(faultCountBeforeRefresh.getAndDecrement() == 0) {
            Logger.log(TAG, "*** IMSDefaultRouteResolver.resetRoutes#before - ConnectionDataProviderConfigVsDnsImpl.obtainSecurityInfo()");
            ((ConnectionDataProviderConfigVsDnsImpl) connDataProvider).refresh();
            Logger.log(TAG, "*** IMSDefaultRouteResolver.resetRoutes#after - ConnectionDataProviderConfigVsDnsImpl.obtainSecurityInfo()");
        /*    faultCountBeforeRefresh.set(FAULT_TRY_COUNT);
        } else {
            Logger.log(Logger.Tag.WARNING, "connDataProvider.refresh was skipped in order to have a change to establish connection with current connection data");
        }*/

        Logger.log(Logger.Tag.WARNING, "Clearing the route map");
        defaultRouteMap.clear();
    }
}
