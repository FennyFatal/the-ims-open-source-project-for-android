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

package javax.microedition.ims.android.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import javax.microedition.ims.core.connection.ConnState;
import javax.microedition.ims.core.connection.ConnectionManagerAbstract;
import javax.microedition.ims.core.connection.NetworkType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 05-Feb-2010
 * Time: 11:47:16
 */
public class AndroidConnectionManager extends ConnectionManagerAbstract<NetworkInfo> {
    private final Map<NetworkInfo.State, ConnState> stateMap;
    private final ConnectivityManager connectivityManager;
    private Context context;


    public AndroidConnectionManager(Object buildUpInfo) {
        super(buildUpInfo);
        this.context = (Context) buildUpInfo;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        stateMap = Collections.synchronizedMap(Collections.unmodifiableMap(buildStateMap()));

        onConnectivity();
    }

    private Map<NetworkInfo.State, ConnState> buildStateMap() {
        Map<NetworkInfo.State, ConnState> retValue = new HashMap<NetworkInfo.State, ConnState>(10);
        retValue.put(NetworkInfo.State.CONNECTED, ConnState.CONNECTED);
        retValue.put(NetworkInfo.State.CONNECTING, ConnState.CONNECTING);
        retValue.put(NetworkInfo.State.DISCONNECTED, ConnState.DISCONNECTED);
        retValue.put(NetworkInfo.State.DISCONNECTING, ConnState.DISCONNECTING);
        retValue.put(NetworkInfo.State.SUSPENDED, ConnState.DISCONNECTED);
        retValue.put(NetworkInfo.State.UNKNOWN, ConnState.UNKNOWN);

        return retValue;
    }

    @Override
    protected NetworkInfo obtainDefaultEventInfo() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        log("info: " + info);
        return info;
    }

    @Override
    protected NetworkType resolveNetType() {
        return doResolveNetType(connectivityManager.getActiveNetworkInfo());
    }

    @Override
    protected NetworkType resolveNetType(final NetworkInfo eventInfo) {
        return doResolveNetType(eventInfo);
    }

    private NetworkType doResolveNetType(final NetworkInfo eventInfo) {

        NetworkType retValue = NetworkType.UNKNOWN;

        if (eventInfo != null) {
            int networkType = eventInfo.getType();

            if (networkType == ConnectivityManager.TYPE_MOBILE) {
                retValue = NetworkType.MOBILE;
            }
            else if (networkType == ConnectivityManager.TYPE_WIFI) {
                retValue = NetworkType.WIFI;
            }
        }
        else {
            retValue = NetworkType.NONE;
        }

        return retValue;
    }

    public String getAccessPointMAC() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String retValue = null;

        if (wifiManager != null) {
            final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                //Return the basic service set identifier (BSSID) of the current access point.
                // The BSSID may be null if there is no network currently connected.
                //the BSSID, in the form of a six-byte MAC address: XX:XX:XX:XX:XX:XX
                retValue = wifiInfo.getBSSID();
            }
        }

        return retValue;
    }

    public ConnState obtainConnStateDirectly() {
        final ConnState currConnState = resolveState();
        final ConnState lastKnownConnState = getCurrentState();

        if (currConnState != lastKnownConnState) {
            onConnectivity(connectivityManager.getActiveNetworkInfo());
            assert false : "Connection manager and Android device became unsynchronized. " +
                    "Last known state connState = " + lastKnownConnState +
                    " actual current connState = " + currConnState;
        }

        return currConnState;
    }


    public NetworkType obtainNetworkTypeDirectly() {
        final NetworkType currNetworkType = resolveNetType();
        final NetworkType lastKnownNetworkType = getNetworkType();

        if (currNetworkType != lastKnownNetworkType) {
            onConnectivity(connectivityManager.getActiveNetworkInfo());
            assert false : "Connection manager and Android device became unsynchronized. " +
                    "Last known state connState = " + lastKnownNetworkType +
                    " actual current connState = " + currNetworkType;
        }

        return lastKnownNetworkType;
    }

    @Override
    protected ConnState resolveState() {
        return doResolveState(connectivityManager.getActiveNetworkInfo());
    }

    @Override
    protected ConnState resolveState(final NetworkInfo eventInfo) {
        return doResolveState(eventInfo);
    }

    private ConnState doResolveState(final NetworkInfo eventInfo) {
        return eventInfo == null ? ConnState.DISCONNECTED : stateMap.get(eventInfo.getState());
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
