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

package javax.microedition.ims.transport.impl;

import javax.microedition.ims.common.*;
import javax.microedition.ims.transport.ChannelIOException;
import javax.microedition.ims.transport.messagerouter.Route;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 16.01.2010
 * Time: 14:20:08
 */
//TODO: make thorough testing of this feature
final class ChannelManager<T extends IMSMessage> implements Shutdownable {

    static interface Creator<T extends IMSMessage> {
        ChannelWorker<T> create(final Route route) throws ChannelIOException;
    }

    private final Object mutex = new Object();
    private final Map<Route, ChannelWorker<T>> channelMap = Collections.synchronizedMap(new HashMap<Route, ChannelWorker<T>>());

    ChannelManager() {
    }

    ChannelWorker<T> get(Route key, Creator<T> creator) throws ChannelIOException {
        return doGet(key, creator);
    }

    ChannelWorker<T> get(Socket socket, Creator<T> creator, IMSEntityType entityType) throws ChannelIOException {
        Route key = new DefaultRoute(
                socket.getInetAddress().getHostAddress(),
                socket.getPort(),
                socket.getLocalPort(),
                Protocol.TCP,
                entityType
        );

        return doGet(key, creator);
    }

    private ChannelWorker<T> doGet(final Route key, final Creator<T> creator) throws ChannelIOException {
        ChannelWorker<T> retValue = null;
        Logger.log("ChannelManager<IMSMessage>", "getting ChannelWorker for route:" + key);
        synchronized (mutex) {
            try {
                retValue = channelMap.get(key);
                if (retValue == null) {
                    retValue = createChannelWorker(key, creator);

                    if (key.getSimultaneousRoutes() != null) {
                        final Collection<Protocol> collection = key.getSimultaneousRoutes();
                        for (Protocol protocol : collection) {
                            final Route route = DefaultRoute.copyOf(key, protocol);
                            if (!channelMap.containsKey(route)) {
                                createChannelWorker(route, creator);
                            }
                        }
                    }
                }
            }
            catch (ChannelIOException e) {
                doRemove(key);
                throw e;
            }
        }

        Logger.log("ChannelManager<IMSMessage>", "ChannelWorker is : " + retValue);

        return retValue;
    }

    private ChannelWorker<T> createChannelWorker(final Route route, Creator<T> creator) throws ChannelIOException {

        assert Thread.holdsLock(mutex) : "Method run without proper synchronization";

        ChannelWorker<T> retValue;
        try {

            //TOD0: debug code
            /*
            if(Protocol.TCP == route.getTransportType()){
                throw new ChannelIOException(route, ChannelIOException.Reason.UNKNOWN_ERROR, "Debug. 'TCP is forbidden' simulation.");
            }
            */
            //TOD0: end of debug code
            Logger.log("ChannelManager<IMSMessage>", "createChannelWorker for:" + route);

            retValue = creator.create(route);
            retValue.addListener(createChannelListener());
            MessageTransport.log("new channel created for route " + route + ": " + retValue, "MESSAGE TRANSPORT");
            channelMap.put(route, retValue);
        }
        catch (ChannelIOException e) {
            throw new ChannelIOException(
                    e.getRoute(),
                    e.getReason(),
                    e.getMessage() + ". Current routes = " + channelMap.keySet(),
                    e.getThrowableCause()
            );
        }

        return retValue;
    }

    private ChannelListenerAdapter<T> createChannelListener() {
        return new ChannelListenerAdapter<T>() {

            public void onChannelStop(final ChannelEvent event) {
                synchronized (mutex) {
                    doRemove(event.getInitialRoute());
                    doRemove(event.getRealRoute());
                }
            }
        };
    }

    ChannelWorker remove(final Route key) {
        return doRemove(key);
    }

    private ChannelWorker doRemove(final Route key) {

        assert key != null : "Can't operate with null route. Now route is " + key;

        ChannelWorker<T> retValue = null;

        if (key != null) {
            synchronized (mutex) {
                retValue = channelMap.remove(key);
                if (retValue != null) {
                    retValue.shutdown();
                }

                if (key.getSimultaneousRoutes() != null) {
                    final Collection<Protocol> collection = key.getSimultaneousRoutes();

                    for (Protocol protocol : collection) {
                        final ChannelWorker<T> simultaneousChannelWorker =
                                channelMap.remove(DefaultRoute.copyOf(key, protocol));

                        if (simultaneousChannelWorker != null) {
                            simultaneousChannelWorker.shutdown();
                        }
                    }
                }
            }
        }

        return retValue;
    }

    
    public void shutdown() {
        Map<Route, ChannelWorker<T>> copy;

        synchronized (mutex) {
            copy = new HashMap<Route, ChannelWorker<T>>(channelMap);
            channelMap.clear();
        }

        for (Route route : copy.keySet()) {
            copy.get(route).shutdown();
        }
    }
}
