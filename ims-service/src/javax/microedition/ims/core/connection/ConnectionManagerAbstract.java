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

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.common.util.NetUtils;
import javax.microedition.ims.core.env.ConnectionManager;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 05-Feb-2010
 * Time: 13:34:03
 */
public abstract class ConnectionManagerAbstract<T> implements ConnectionManager, ConnectionManagerHandler<T>, Shutdownable {
    private final AtomicReference<ConnState> currentState = new AtomicReference<ConnState>(ConnState.UNKNOWN);
    private final AtomicReference<String> currentAddress = new AtomicReference<String>("localhost");
    private final AtomicReference<NetworkType> currentNetworkType = new AtomicReference<NetworkType>(NetworkType.UNKNOWN);

    private final ListenerHolder<ConnStateListener> connStateSupport = new ListenerHolder<ConnStateListener>(ConnStateListener.class);
    private final ListenerHolder<IPChangeListener> ipChangeSupport = new ListenerHolder<IPChangeListener>(IPChangeListener.class);
    private final ListenerHolder<NetTypeChangeListener> netTypeChangeSupport = new ListenerHolder<NetTypeChangeListener>(NetTypeChangeListener.class);
    private final ListenerHolder<NetworkInterfaceListener> networkInterfaceSupport = new ListenerHolder<NetworkInterfaceListener>(NetworkInterfaceListener.class);

    private final AtomicBoolean done = new AtomicBoolean(false);
    private final Object buildUpInfo;
    private final Object mutex = new Object();

    public ConnectionManagerAbstract(Object buildUpInfo) {
        this.buildUpInfo = buildUpInfo;
        log("instantiate " + getClass().getSimpleName());
    }

    public NetworkType getNetworkType() {
        return currentNetworkType.get();
    }

    public NetworkSubType getNetworkSubType() {
        final NetworkType networkType = currentNetworkType.get();
        NetworkSubType retValue;

        switch (networkType) {

            case WIFI:
                retValue = NetworkSubType.IEEE_802_11;
                break;
            case MOBILE:
                retValue = NetworkSubType.G3_TDD;
                break;
            case ETHERNET:
                retValue = NetworkSubType.G3_TDD;
                break;
            case NONE:
                retValue = null;
                break;
            case UNKNOWN:
                retValue = null;
                break;
            default:
                retValue = null;
                break;
        }

        return retValue;
    }

    protected Object getBuildUpInfo() {
        return buildUpInfo;
    }

    public void addConnStateListener(final ConnStateListener listener) {
        if (!done.get()) {
            connStateSupport.addListener(listener);
        }
    }

    public void removeConnStateListener(final ConnStateListener listener) {
        connStateSupport.removeListener(listener);
    }
    
    public void addNetworkInterfaceListener(final NetworkInterfaceListener listener) {
        if (!done.get()) {
            networkInterfaceSupport.addListener(listener);
        }
    }

    public void removeNetworkInterfaceListener(final NetworkInterfaceListener listener) {
        networkInterfaceSupport.removeListener(listener);
    }

    public void addIpChangeListener(final IPChangeListener listener) {
        if (!done.get()) {
            ipChangeSupport.addListener(listener);
        }
    }

    public void removeIpChangeListener(final IPChangeListener listener) {
        ipChangeSupport.removeListener(listener);
    }

    public void addNetTypeChangeListener(final NetTypeChangeListener listener) {
        if (!done.get()) {
            netTypeChangeSupport.addListener(listener);
        }
    }

    public void removeNetTypeChangeListener(final NetTypeChangeListener listener) {
        netTypeChangeSupport.removeListener(listener);
    }

    public ConnState getCurrentState() {
        return currentState.get();
    }

    public String getInetAddress() {
        return currentAddress.get();
    }

    public void onConnectivity() {
        handleConnectivityEvent(obtainDefaultEventInfo());
    }

    public void onConnectivity(T eventInfo) {
        handleConnectivityEvent(eventInfo);
    }

    private void handleConnectivityEvent(final T eventInfo) {
        final Map<String, String> allInterfaces = NetUtils.listNetInterfaces();
        log("handling connectivity event");
        log("All interfaces: " + allInterfaces);

        if (!done.get()) {

            IpChangeEvent ipChangeEvent;
            NetworkTypeChangeEvent networkTypeChangeEvent;
            ConnStateEvent connStateEvent;

            synchronized (mutex) {
                ipChangeEvent = handleIPChange(eventInfo);
                networkTypeChangeEvent = handleNetTypeChange(eventInfo);
                connStateEvent = handleStateChange(eventInfo);
            }

            if (ipChangeEvent != null) {
                networkInterfaceSupport.getNotifier().onInterfaceChanged(ipChangeEvent);
                ipChangeSupport.getNotifier().onIpChange(ipChangeEvent);
            }

            if (networkTypeChangeEvent != null) {
                netTypeChangeSupport.getNotifier().onNetworkTypeChange(networkTypeChangeEvent);
            }

            log("connStateEvent: " + connStateEvent);
            if (connStateEvent != null) {
                fireEvent(connStateEvent);
            }
        }
    }

    private IpChangeEvent handleIPChange(T eventInfo) {

        final String inetAddress = NetUtils.guessCurrentAddress();
        final NetworkType networkType = doResolveNetworkType(eventInfo);
        
        String prevAddress = currentAddress.get();
        log("previous address: " + prevAddress);
        log("new address: " + inetAddress);
        
        IpChangeEvent retValue = null;

        if (isNeedFireIpChangeEvent(inetAddress, networkType)) {
            currentAddress.set(inetAddress);
            retValue = new DefaultIpChangeEvent(prevAddress, inetAddress, currentNetworkType.get(), networkType);
        }

        return retValue;
    }

    private boolean isNeedFireIpChangeEvent(String inetAddress, NetworkType networkType) {
        return !currentAddress.get().equalsIgnoreCase(inetAddress)/* || currentNetworkType.get() != networkType*/;
    }

    private NetworkTypeChangeEvent handleNetTypeChange(T eventInfo) {

        final NetworkType networkType = doResolveNetworkType(eventInfo);

        NetworkType prevNetworkType = currentNetworkType.get();
        NetworkTypeChangeEvent retValue = null;

        log("previous network type: " + prevNetworkType);
        log("new network type: " + networkType);

        if (currentNetworkType.get() != networkType) {
            currentNetworkType.set(networkType);
            retValue = new DefaultNetworkTypeChangeEvent(networkType, prevNetworkType);
        }

        return retValue;
    }

    private NetworkType doResolveNetworkType(T eventInfo) {
        final NetworkType networkType = eventInfo == null ? resolveNetType() : resolveNetType(eventInfo);
        return networkType;
    }

    private ConnStateEvent handleStateChange(final T eventInfo) {
        log("handleStateChangeeventInfo" + eventInfo);
        final ConnState state = eventInfo == null ? resolveState() : resolveState(eventInfo);

        ConnState prevState = currentState.get();
        ConnStateEvent retValue = null;

        log("previous state: " + prevState);
        log("new state: " + state);

        if (currentState.get() != state) {
            currentState.set(state);
            retValue = new DefaultConnStateEvent(prevState, state);
        }

        return retValue;
    }

    protected abstract T obtainDefaultEventInfo();

    protected abstract ConnState resolveState();

    protected abstract ConnState resolveState(T eventInfo);

    protected abstract NetworkType resolveNetType();

    protected abstract NetworkType resolveNetType(T eventInfo);

    private void fireEvent(final ConnStateEvent event) {
        log("fireEvent# event: " + event);
        log("fireEvent# connStateSupport: " + connStateSupport);
        switch (event.getNewState()) {
            case CONNECTED: {
                connStateSupport.getNotifier().onConnected(event);
            }
            break;

            case CONNECTING: {
                connStateSupport.getNotifier().onConnecting(event);
            }
            break;

            case DISCONNECTED: {
                connStateSupport.getNotifier().onDisconnected(event);
            }
            break;

            case DISCONNECTING: {
                connStateSupport.getNotifier().onDisconnecting(event);
            }
            break;

            case UNKNOWN: {
                connStateSupport.getNotifier().onUnknown(event);
            }
            break;

            default: {
                assert false : "Never should be there";
            }
            break;

        }
    }

    public boolean isShutdown() {
        return done.get();
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {
            connStateSupport.shutdown();
        }
    }

    protected static void log(String msg) {
        Logger.log("CONN_MANAGER", msg);
    }
}
