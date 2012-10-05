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

import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.connection.ConnectionDataProvider;
import javax.microedition.ims.transport.IMSRouter;
import javax.microedition.ims.transport.impl.DefaultRouteResolver;
import javax.microedition.ims.transport.messagerouter.Route;
import javax.microedition.ims.transport.messagerouter.RouteDescriptor;
import javax.microedition.ims.transport.messagerouter.Router;
import javax.microedition.ims.transport.messagerouter.RouterListener;
import java.util.*;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 16.8.2010
 * Time: 17.48.06
 */
public class MessageRouterComposite implements Router<IMSMessage> {
    private final static String TAG = "MessageRouterComposite";
    private final static boolean DBG = false;
    private final DefaultRouteResolver defaultRouteResolver;
    private final Map<IMSEntityType, IMSRouter<IMSMessage>> routers;


    private MessageRouterComposite(final Builder builder) {
        Configuration config = builder.config;
        if (config == null) {
            throw new IllegalArgumentException("Config can not be null. Now it has value " + config);
        }

        ConnectionDataProvider connDataProvider = builder.connDataProvider;
        if (connDataProvider == null) {
            throw new IllegalArgumentException("dnsResolver can not be null. Now it has value " + connDataProvider);
        }

        this.defaultRouteResolver = new IMSDefaultRouteResolver(config, connDataProvider);

        routers = Collections.synchronizedMap(new HashMap<IMSEntityType, IMSRouter<IMSMessage>>(builder.routers));
    }

    public void addRouterListener(final RouterListener listener) {
        final Collection<IMSRouter<IMSMessage>> routerCollection;
        synchronized (routers) {
            routerCollection = routers.values();
        }

        for (IMSRouter<IMSMessage> imsRouter : routerCollection) {
            imsRouter.addRouterListener(listener);
        }
    }

    public void removeRouterListener(final RouterListener listener) {
        final Collection<IMSRouter<IMSMessage>> routerCollection;
        synchronized (routers) {
            routerCollection = routers.values();
        }

        for (IMSRouter<IMSMessage> imsRouter : routerCollection) {
            imsRouter.removeRouterListener(listener);
        }
    }

    public Route getRoute(final IMSMessage msg) {
        if (DBG) Logger.log(TAG, "getRoute: " + msg);

        Route retValue = null;

        final Router<IMSMessage> messageRouter = routers.get(msg.getEntityType());
        if (messageRouter != null) {
            if (DBG) Logger.log(TAG, "getRoute: messageRouter = " + messageRouter);
            retValue = messageRouter.getRoute(msg);
        }

        if (retValue == null) {
            if (DBG) Logger.log(TAG, "getRoute: getDefaultRoute");
            retValue = defaultRouteResolver.getDefaultRoute(msg);
        }

        if (DBG) Logger.log(TAG, "getRoute: retValue = " + retValue);
        return retValue;
    }

    public void addRoute(final Route route, final RouteDescriptor routeDescriptor) {
        Logger.log(TAG, "addRoute");
        final Router<IMSMessage> messageRouter = routers.get(route.getEntityType());
        if (messageRouter != null) {
            Logger.log(TAG, "addRoute: " + messageRouter);
            messageRouter.addRoute(route, routeDescriptor);
        }
    }

    public Route removeRoute(final RouteKey key) {
        Route retValue = null;

        final Collection<IMSRouter<IMSMessage>> routersCopy;
        synchronized (routers) {
            routersCopy = routers.values();
        }

        for (Router<IMSMessage> messageRouter : routersCopy) {
            retValue = messageRouter.removeRoute(key);
            if (retValue != null) {
                break;
            }
        }

        return retValue;
    }

    public Collection<Route> getActiveRoutes() {
        final Collection<Route> retValue = new ArrayList<Route>(10);

        final Collection<IMSRouter<IMSMessage>> routersCopy;
        synchronized (routers) {
            routersCopy = routers.values();
        }

        Collection<Route> activeRoutes;
        for (IMSRouter<IMSMessage> messageRouter : routersCopy) {
            activeRoutes = messageRouter.getActiveRoutes();
            if (activeRoutes != null && activeRoutes.size() > 0) {
                retValue.addAll(activeRoutes);
            }
            else {
                Route defaultRoute = defaultRouteResolver.getDefaultRoute(messageRouter.getEntityType());
                if(defaultRoute != null) {
                    retValue.add(defaultRoute);    
                }
            }
        }

        return retValue;
    }

    public void resetRoutes() throws IMSStackException {
        synchronized (routers) {
            for (IMSRouter<IMSMessage> messageRouter : routers.values()) {
                if (messageRouter instanceof MessageRouterBase) {
                    Logger.log(Logger.Tag.WARNING, "Resetting router " + messageRouter);
                    ((MessageRouterBase) messageRouter).resetRoutes();
                }
            }
        }
    }
    @Override
    public String toString() {
        return "MessageRouterComposite{" +
                "routeResolver=" + defaultRouteResolver +
                ", routers=" + routers +
                '}';
    }

    public static class Builder {
        private final static String TAG = "MessageRouterComposite.Builder";
        private final Configuration config;
        private final ConnectionDataProvider connDataProvider;
        private final Map<IMSEntityType, IMSRouter<IMSMessage>> routers =
                new HashMap<IMSEntityType, IMSRouter<IMSMessage>>(10);

        public Builder(final Configuration config,
                       final ConnectionDataProvider connDataProvider) {
            this.config = config;
            this.connDataProvider = connDataProvider;
        }

        public Builder addRouter(final IMSRouter<IMSMessage> router) {
            if (router == null) {
                throw new NullPointerException("Router is " + router);
            }
            Logger.log(TAG, "addRouter: " + router);

            if (routers.containsKey(router.getEntityType())) {
                throw new IllegalArgumentException("Router for " + router.getEntityType() + " already added.");
            }
            routers.put(router.getEntityType(), router);

            return this;
        }

        public Router<IMSMessage> build() {
            return new MessageRouterComposite(this);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "config=" + config +
                    ", routers=" + routers +
                    '}';
        }
    }
}
