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

import javax.microedition.ims.common.*;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.connection.ConnectionDataProvider;
import javax.microedition.ims.transport.impl.DefaultRoute;
import javax.microedition.ims.transport.messagerouter.Route;
import javax.microedition.ims.transport.messagerouter.RouteDescriptor;
import javax.microedition.ims.transport.messagerouter.RouterEventDefaultImpl;
import javax.microedition.ims.util.MessageUtilHolder;
import javax.microedition.ims.util.SipMessageUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 16.8.2010
 * Time: 17.48.06
 */
public class MessageRouterSIP extends MessageRouterBase<IMSMessage> {
    private final static String TAG = "MessageRouterSIP";
    private final static boolean DBG = true;

    private final static Object SIP_ROUTER_KEY = new Object();

    private final static EnumSet<Protocol> supportedSimulteneousProtocols = EnumSet.of(Protocol.UDP, Protocol.TCP);
    private final static EnumSet<Protocol> supportedSecuredProtocols = EnumSet.of(Protocol.TLS);


    private final AtomicReference<Route> currentRouteUDP = new AtomicReference<Route>(null);
    private final AtomicReference<Route> currentRouteTCP = new AtomicReference<Route>(null);
    private final AtomicReference<Route> currentRouteTLS = new AtomicReference<Route>(null);
    private final AtomicReference<Route> currentRoute = new AtomicReference<Route>(null);

    private final IMSDefaultRouteResolver routeResolver;
    private final Configuration config;

    private final RepetitiousTaskManager repetitiousTaskManager;

    public MessageRouterSIP(final Configuration config, final ConnectionDataProvider connDataProvider, final RepetitiousTaskManager repetitiousTaskManager){
        this.config = config;
        this.routeResolver = new IMSDefaultRouteResolver(config, connDataProvider);

        this.repetitiousTaskManager = repetitiousTaskManager;
    }

    public IMSEntityType getEntityType() {
        return IMSEntityType.SIP;
    }

   public Route getRoute(final IMSMessage msg) {
        if (DBG) Logger.log(TAG, "getRoute: msg = " + msg + ", currentRoute = " + currentRoute.get());

        final Route defaultRoute = routeResolver.getDefaultRoute(getEntityType());

        if (currentRoute.get() == null ||
                currentRoute.get().getDstHost().compareTo(defaultRoute.getDstHost()) != 0 ||
                currentRoute.get().getDstPort() != defaultRoute.getDstPort() ||
                currentRoute.get().getLocalPort() != defaultRoute.getLocalPort() ||
                currentRoute.get().getEntityType() != defaultRoute.getEntityType()) {

            if (DBG) Logger.log(TAG, "getRoute: defaultRoute = " + defaultRoute);

            if (currentRoute.compareAndSet(null, prepareRoute(defaultRoute, defaultRoute.getTransportType()))) {
                currentRouteTCP.set(prepareRoute(defaultRoute, Protocol.TCP));
                currentRouteUDP.set(prepareRoute(defaultRoute, Protocol.UDP));
                currentRouteTLS.set(prepareRoute(defaultRoute, Protocol.TLS));
            }
        }

        //Protocol transport = suggestProtocolForMessage(msg);
//	MessageUtilHolder sipMessageUtil = new MessageUtilHolder();
//        Protocol msgTransport = sipMessageUtil.getMessageTransport(msg);
        final Protocol transport = config.getConnectionType();
	if (DBG) Logger.log(TAG, "getRoute: transport = " + transport);
        checkTransport(transport);

        final Route route;
        Protocol transportType = currentRoute.get().getTransportType();
        boolean isCurrentRouteSecure = supportedSecuredProtocols.contains(transportType);
        if (isCurrentRouteSecure) {
            route = currentRoute.get();
        }
        else {
            route = Protocol.UDP == transport ? currentRouteUDP.get() : currentRouteTCP.get();
        }
        if (DBG) Logger.log(TAG, "getRoute: isCurrentRouteSecure = " + isCurrentRouteSecure + ", route = " + route);

        return route;
    }

    private Protocol suggestProtocolForMessage(final IMSMessage msg) {
        if (DBG) Logger.log(TAG, "suggestProtocolForMessage: msg = " + msg);
        Protocol transport;

        Protocol currentProtocol = retrieveCurrentProtocol();
        if (DBG) Logger.log(TAG, "suggestProtocolForMessage: currentProtocol = " + currentProtocol);
        boolean secured = supportedSecuredProtocols.contains(currentProtocol);
        boolean supportedSimulteneousProtocol = supportedSimulteneousProtocols.contains(currentProtocol);

        if (!secured && !supportedSimulteneousProtocol) {
            if (DBG) Logger.log(TAG, "suggestProtocolForMessage: secured = " + secured + ", supportedSimulteneousProtocol = " + supportedSimulteneousProtocol);
            throw new IllegalArgumentException("Unsup");//
        }

        if (secured) {
            transport = currentProtocol;
        }
        else {
            final SipMessageUtil<IMSMessage> sipMessageUtil = MessageUtilHolder.getSIPMessageUtil();
            transport = sipMessageUtil.getMessageTransport(msg);
            if (transport == null) {
                transport = currentProtocol;
            }
        }
        if (DBG) Logger.log(TAG, "suggestProtocolForMessage: transport = " + transport);

        return transport;
    }

    private Protocol retrieveCurrentProtocol() {
        if (DBG) Logger.log(TAG, "retrieveCurrentProtocol: currentProtocol = " + (currentRoute.get() == null ? "null" : currentRoute.get()));
        if (DBG) Logger.log(TAG, "retrieveCurrentProtocol: config.getConnectionType() = " + config.getConnectionType());
        return currentRoute.get() == null ? config.getConnectionType() : currentRoute.get().getTransportType();
    }

    public void addRoute(final Route route, final RouteDescriptor routeDescriptor) {
        if (DBG) Logger.log(TAG, "addRoute: Route: " + route + ", RouteDescriptor: " + routeDescriptor);

        final Protocol transport = route.getTransportType();

        if (checkTransport(transport)) {

            repetitiousTaskManager.cancelTask(SIP_ROUTER_KEY);
            doRemoveRoute();

            currentRoute.set(prepareRoute(route, route.getTransportType()));
            currentRouteTCP.set(prepareRoute(route, Protocol.TCP));
            currentRouteUDP.set(prepareRoute(route, Protocol.UDP));

            final Long expires = routeDescriptor.getExpires();
            if (expires != null && expires > 0) {
                repetitiousTaskManager.startDelayedTask(
                        SIP_ROUTER_KEY,
                        new RepetitiousTaskManager.Repeater<Object>() {
                            public void onRepeat(Object key, Shutdownable shutdownable) {
                                doRemoveRoute();
                                shutdownable.shutdown();
                            }
                        },
                        0
                );
            }

            getListenerHolder().getNotifier().onRouteAdded(new RouterEventDefaultImpl(route));
        }
    }

    private boolean checkTransport(final Protocol transport) throws IllegalArgumentException {
        final boolean supportedSimulteneousTransportDetected = supportedSimulteneousProtocols.contains(transport);
        final boolean securedTransport = supportedSecuredProtocols.contains(Protocol.TLS);

        if (!supportedSimulteneousTransportDetected && !securedTransport) {
            //TODO
            final String errMsg = "Illegal transport request detected '" + transport + "' only supported transports are "/* + supportedProtocols*/;
            throw new IllegalArgumentException(errMsg);
        }
        return true;
    }

    public Route removeRoute(final RouteKey key) {
        return doRemoveRoute();
    }

    private Route doRemoveRoute() {
        if (DBG) Logger.log(TAG, "doRemoveRoute");

        currentRouteTCP.set(null);
        currentRouteUDP.set(null);
        final Route retValue = currentRoute.getAndSet(null);

        getListenerHolder().getNotifier().onRouteAdded(new RouterEventDefaultImpl(retValue));

        return retValue;
    }

    private Route prepareRoute(final Route route, final Protocol transport) {

        Collection<Protocol> copy = new HashSet<Protocol>(supportedSimulteneousProtocols);

        boolean unsecuredProtocol = !supportedSecuredProtocols.contains(transport);

        if (unsecuredProtocol && config.useSimultaneousConnections()) {
            copy.remove(transport);
        }
        else {
            copy.clear();
        }

        return DefaultRoute.copyOf(DefaultRoute.copyOf(route, transport), copy);
    }

    public Collection<Route> getActiveRoutes() {
        Collection<Route> routes = new HashSet<Route>(7);

        Route route;

        route = currentRoute.get();
        if (route != null) {
            routes.add(route);
        }

        route = currentRouteTCP.get();
        if (route != null) {
            routes.add(route);
        }

        route = currentRouteUDP.get();
        if (route != null) {
            routes.add(route);
        }

        return routes.size() == 0 ? Collections.<Route>emptySet() : routes;
    }

    @Override
    public void resetRoutes() throws IMSStackException {
        routeResolver.resetRoutes();

        currentRoute.set(null);
    }
}
