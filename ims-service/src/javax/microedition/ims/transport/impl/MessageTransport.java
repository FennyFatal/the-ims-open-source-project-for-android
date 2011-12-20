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
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.messagerouter.MessageRouterBase;
import javax.microedition.ims.core.messagerouter.MessageRouterComposite;
import javax.microedition.ims.transport.*;
import javax.microedition.ims.transport.messagerouter.Route;
import javax.microedition.ims.transport.messagerouter.Router;
import javax.microedition.ims.transport.messagerouter.RouterEvent;
import javax.microedition.ims.transport.messagerouter.RouterListener;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageTransport<T extends IMSMessage> implements TransportIO<T>, Shutdownable {

    private final ListenerHolder<TransportListener> transportListenerHolder;
    private final ListenerHolder<UASListener> uasListenerHolder;
    private final ListenerHolder<ProducerListener> producerListenerHolder = new ListenerHolder<ProducerListener>(ProducerListener.class);

    private final Consumer<T> inQueue;
    private final Consumer<T> outQueue;

    private final ChannelListener<T> commonChannelListener;
    private final ChannelManager.Creator<T> channelCreator;
    private final ChannelManager<T> channelManager;

    private final AtomicBoolean done = new AtomicBoolean(false);
    private final Router<T> router;
    private final MessageContextRegistry<T> msgContextRegistry;

    private final Map<Route, UAS> routeUASMap = Collections.synchronizedMap(new HashMap<Route, UAS>(10));

    private class DefaultUAS extends CommonUAS {
        private final IMSEntityType entityType;

        public DefaultUAS(final int port, final Protocol protocol, final IMSEntityType entityType) {
            super(
                    new DefaultRoute(null, 0, port, protocol, entityType),
                    MessageTransport.this.uasListenerHolder
            );
            this.entityType = entityType;
        }

        protected ChannelWorker<T> createNewChannel(final Socket socket) throws ChannelIOException {

            final ChannelManager.Creator<T> creator = new ChannelManager.Creator<T>() {
                public ChannelWorker<T> create(final Route route) throws ChannelIOException {
                    ChannelWorker<T> channelWorker;
                    channelWorker = new ChannelWorker<T>(
                            socket,
                            inQueue,
                            msgContextRegistry.getMessageContext(route.getEntityType())
                    );
                    return channelWorker;
                }
            };

            ChannelWorker<T> retValue = channelManager.get(
                    socket,
                    creator,
                    entityType
            );

            retValue.addListener(
                    new ChannelListenerAdapter<T>() {
                        public void onFirstMessage(final ChannelEvent<T> event) {
                            transportListenerHolder.getNotifier().onIncomingConnection(
                                    event.getInitialRoute(),
                                    event.getFirstMessage()
                            );
                        }
                    }
            );

            return retValue;
        }
    }


    public MessageTransport(
            final Router<T> router,
            final MessageContextRegistry<T> msgContextRegistry,
            final ConnectionSecurityInfoProvider securityInfoProvider) throws IOException, QueueException {

        this.router = router;
        this.msgContextRegistry = msgContextRegistry;

        transportListenerHolder = new ListenerHolder<TransportListener>(TransportListener.class);
        uasListenerHolder = new ListenerHolder<UASListener>(UASListener.class);

        final ProducerListener<T> inMessagesProducer = new ProducerListener<T>() {
            public void onPop(T msg) {
                producerListenerHolder.getNotifier().onPop(msg);
            }
        };

        T dummyMessage = this.msgContextRegistry.getGlobalDummyMessage();

        inQueue = new QueueWorker<T>(inMessagesProducer, dummyMessage);

        final ProducerListener<T> outMessagesProducer = new ProducerListener<T>() {

            RobustPusher<T> robustPusher = new RobustPusher<T>(
                    new RobustPusher.RobustPusherCallback<T>() {
                        public void tryPushMessage(final T msg) throws ChannelIOException {
                            handleToInternetTransportLayer(msg);
                        }

                        public void cleanUpRoute(final Route route) {
                            doShutdownRoute(route);

                            Logger.log(Logger.Tag.WARNING, "Trying to recover the transport channel");
                            if (router instanceof MessageRouterComposite) {
                                try {
                                    Logger.log(Logger.Tag.WARNING, "Trying to reset routes ");
                                    ((MessageRouterComposite) router).resetRoutes();
                                } catch (IMSStackException e) {
                                    Logger.log(Logger.Tag.WARNING, e.toString());
                                }
                            }
                        }

                        public void tryEstablishRoute(final Route route) throws ChannelIOException {
                            doEstablishChannel(route);
                        }

                        public void onFailure(final ChannelIOException exception) {
                            onChannelError(exception.getRoute(), exception);
                        }
                    }
            );

            public void onPop(T msg) {
                robustPusher.push(msg);
            }
        };
        outQueue = new QueueWorker<T>(outMessagesProducer, dummyMessage);

        commonChannelListener = new ChannelListenerAdapter<T>() {
            public void onException(ChannelEvent<T> event) {
                onChannelError(event.getInitialRoute(), event.getException());
            }

            public void onChannelStart(final ChannelEvent<T> event) {
                super.onChannelStart(event);    //To change body of overridden methods use File | Settings | File Templates.
            }

            public void onChannelStop(final ChannelEvent<T> event) {
                doShutdownRoute(event.getInitialRoute());
            }
        };

        channelCreator = new ChannelManager.Creator<T>() {
            public ChannelWorker<T> create(final Route route) throws ChannelIOException {
                ChannelWorker<T> retValue;
                retValue = new ChannelWorker<T>(
                        route,
                        inQueue,
                        MessageTransport.this.msgContextRegistry.getMessageContext(route.getEntityType()),
                        securityInfoProvider
                );

                retValue.addListener(commonChannelListener);

                return retValue;
            }
        };

        channelManager = new ChannelManager<T>();

        router.addRouterListener(new RouterListener() {
            public void onRouteAdded(final RouterEvent event) {
                /*
                try {
                    doEstablishChannel(event.getRoute());
                }
                catch (ChannelIOException e) {
                    e.printStackTrace();
                }
                */
            }

            public void onRouteRemoved(final RouterEvent event) {
                doShutdownRoute(event.getRoute());
            }
        });
    }

    public void startUAS(final int port, final Protocol protocol, final IMSEntityType entityType) throws UASInstantiationException {
        Logger.log("MessageTransport", String.format("startUAS#port = %s, protocol = %s", port, protocol));
        //TODO: make thorough testing of this feature
        Route route = new DefaultRoute(null, 0, port, protocol, entityType);

        UAS uas;
        synchronized (routeUASMap) {
            uas = routeUASMap.get(route);
            if (uas == null) {
                routeUASMap.put(route, uas = new DefaultUAS(port, protocol, entityType));
            }
        }

        uas.startListen();
    }

    public void shutdownUAS(final int port, final Protocol protocol, final IMSEntityType entityType) {
        Route route = new DefaultRoute(null, 0, port, protocol, entityType);

        UAS uas = routeUASMap.get(route);
        if (uas instanceof Shutdownable) {
            ((Shutdownable) uas).shutdown();
        }
    }

    public void shutdownRoute(final Route route) {
        doShutdownRoute(route);
    }

    public void establishRoute(final Route route) throws ChannelIOException {
        doEstablishChannel(route);
    }

    private void onChannelError(final Route route, Exception e) {
        assert e != null;
        transportListenerHolder.getNotifier().onChannelError(route, e);
        doShutdownRoute(route);
        e.printStackTrace();

        String msg;
        String errMsg = e.getMessage() != null ? e.getMessage() : e.toString();
        msg = "Transport error detected " + errMsg;
        log(msg, "SIP_MESSAGE TRANSPORT");
    }

    private void doShutdownRoute(final Route route) {
        log("Shutdown transport for route: " + route, "SIP_MESSAGE TRANSPORT");
        ChannelWorker channelWorker = channelManager.remove(route);

        String msg = "Transport channel shutdown for route " + route + ": " + channelWorker;
        log(msg, "SIP_MESSAGE TRANSPORT");

        if (channelWorker != null) {
            channelWorker.shutdown();
            channelWorker.removeListener(commonChannelListener);
        }
    }
    
    public void shutdownRoutes() {
        log("Shutdown transport for all routes", "SIP_MESSAGE TRANSPORT");
        channelManager.shutdown();
    }

    private Shutdownable[] getShutdownableList() {
        return new Shutdownable[]{(Shutdownable) inQueue, (Shutdownable) outQueue, channelManager, transportListenerHolder};
    }

    public void addTransportListener(TransportListener<T> listener) {
        transportListenerHolder.addListener(listener);

    }

    public void removeTransportListener(TransportListener<T> listener) {
        transportListenerHolder.removeListener(listener);
    }

    public void addProducerListener(ProducerListener<T> listener) {
        producerListenerHolder.addListener(listener);

    }

    public void removeProducerListener(ProducerListener<T> listener) {
        producerListenerHolder.removeListener(listener);
    }

    public void addUASListener(UASListener listener) {
        uasListenerHolder.addListener(listener);

    }

    public void removeUASListener(UASListener listener) {
        uasListenerHolder.removeListener(listener);
    }

    public void push(T msg) {
        outQueue.push(msg);
    }

    private void handleToInternetTransportLayer(T msg) throws ChannelIOException {
        final ChannelWorker<T> chnlWrk = getChannel(msg);

        if (chnlWrk != null) {
            if(!msg.isExpired()) {
                chnlWrk.sendMessage(msg);    
            } else {
                Logger.log(Logger.Tag.WARNING, "message is expired: " + msg.shortDescription());
            }
        }
        else {
            assert false : "Can not create channel worker for messge " + msg.shortDescription();
        }
    }

    private ChannelWorker<T> getChannel(final T msg) throws ChannelIOException {
        return doEstablishChannel(router.getRoute(msg));
    }

    private ChannelWorker<T> doEstablishChannel(final Route route) throws ChannelIOException {
        Logger.log("establishing route: " + route.toString());
        return channelManager.get(route, channelCreator);
    }

    public void shutdown() {

        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "Shutdowning MessageTransport");

        if (done.compareAndSet(false, true)) {

            Map<Route, UAS> uasMapCopy;

            synchronized (routeUASMap) {
                uasMapCopy = new HashMap<Route, UAS>(routeUASMap);
                routeUASMap.clear();
            }

            for (Route route : uasMapCopy.keySet()) {
                UAS uas = uasMapCopy.get(route);
                if (uas instanceof Shutdownable) {
                    ((Shutdownable) uas).shutdown();
                }
            }

            Shutdownable[] list = getShutdownableList();
            for (Shutdownable aList : list) {
                aList.shutdown();
            }

            //executorService.shutdown();
        }

        Logger.log(getClass(), Logger.Tag.SHUTDOWN, "MessageTransport shutdown successfully");
    }

    static void log(String msg, String prefix) {
        Logger.log(prefix, msg);
    }
}
