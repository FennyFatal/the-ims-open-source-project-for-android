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

package javax.microedition.ims.android;

import android.os.RemoteException;
import android.telephony.TelephonyManager;

import javax.microedition.ims.DefaultStackContext;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.connection.GsmLocationServiceDefaultImpl;
import javax.microedition.ims.core.connection.NetworkSubType;
import javax.microedition.ims.core.env.GsmLocationService;
import javax.microedition.ims.core.registry.ClientRegistry;
import javax.microedition.ims.core.registry.CommonRegistry;
import javax.microedition.ims.core.registry.DefaultClientRegistry.ClientRegistryBuilder;
import javax.microedition.ims.core.registry.DefaultCommonRegistry.CommonRegistryBuilder;
import javax.microedition.ims.core.registry.StackRegistryEditor;
import javax.microedition.ims.registry.ClientRegistryContentHandler;
import javax.microedition.ims.registry.CommonRegistryContentHandler;
import javax.microedition.ims.registry.RegistryParser;
import javax.microedition.ims.registry.RegistrySerializer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Khomushko
 */
public class ConfigurationBinder extends IConfiguration.Stub {
    private static final Map<Integer, NetworkSubType> androidToStackMapping = createMapping();

    private final StackRegistryEditor stackAppRegistry;
    private StackContext stackContext;

    public ConfigurationBinder(StackRegistryEditor stackAppRegistry, StackContext stackContext) {
        if (stackAppRegistry == null) {
            throw new IllegalArgumentException(
                    "The stackAppRegistry argument is null");
        }
        this.stackAppRegistry = stackAppRegistry;
        this.stackContext = stackContext;
    }


    public IRegistry getRegistry(String appId) throws RemoteException {
        final IRegistry retValue;

        CommonRegistry commonRegistry = stackAppRegistry.getCommonRegistry(appId);
        String[][] commonProps = RegistrySerializer
                .serializeCommonRegistry(commonRegistry);

        ClientRegistry clientRegistry = stackAppRegistry
                .getClientRegistry(appId);
        String[][] clientProps = RegistrySerializer
                .serializeClientRegistry(clientRegistry);

        String[][] props = new String[clientProps.length + commonProps.length][];
        System.arraycopy(clientProps, 0, props, 0, clientProps.length);
        System.arraycopy(commonProps, 0, props, clientProps.length,
                commonProps.length);

        retValue = new IRegistry(appId, "", 1, props);

        return retValue;
    }


    public boolean hasRegistry(String appId) throws RemoteException {
        return stackAppRegistry.getClientRegistry(appId) != null;
    }


    public boolean removeRegistry(String appId) throws RemoteException {
        final boolean retValue;

        retValue = stackAppRegistry.dropClientData(appId) && stackAppRegistry.dropCommonData(appId);

        return retValue;
    }


    public void setRegistry(IRegistry iRegistry) throws RemoteException {
        final String appId = iRegistry.getAppId();
        final String[][] properties = iRegistry.getProperties();

        final RegistryParser registryParser = new RegistryParser();

        final ClientRegistryBuilder clientRegistryBuilder = new ClientRegistryBuilder(appId);
        final CommonRegistryBuilder commonRegistryBuilder = new CommonRegistryBuilder(appId);

        registryParser.addRegistryContentHandler(new ClientRegistryContentHandler(clientRegistryBuilder));
        registryParser.addRegistryContentHandler(new CommonRegistryContentHandler(commonRegistryBuilder));

        registryParser.parse(properties);
        registryParser.close();

        final ClientRegistry clientRegistry = clientRegistryBuilder.build();
        final CommonRegistry commonRegistry = commonRegistryBuilder.build();

        stackAppRegistry.applyChanges(clientRegistry,
                commonRegistry);
    }

    public void updateLocation(IGsmLocationInfo locationInfo)
            throws RemoteException {
        if (locationInfo == null) {
            throw new IllegalArgumentException("locationInfo cannot be null");
        }

        if (stackContext instanceof DefaultStackContext) {
            DefaultStackContext defaultStackContext = (DefaultStackContext) stackContext;
            final GsmLocationService service = defaultStackContext.getEnvironment().getGsmLocationService();

            if (service instanceof GsmLocationServiceDefaultImpl) {
                GsmLocationServiceDefaultImpl serviceDefault = (GsmLocationServiceDefaultImpl) service;

                String mccMnc = locationInfo.getMcc() + "" + locationInfo.getMnc();
                NetworkSubType networkSubType = androidToStackMapping.get(locationInfo.getNetworkType());

                serviceDefault.updateLocationInfo(
                        new GsmLocationInfo(locationInfo.getCid(), locationInfo.getLac(), mccMnc, networkSubType)
                );
            }
        }
    }

    public void removeLocation()
            throws RemoteException {
        if (stackContext instanceof DefaultStackContext) {
            DefaultStackContext defaultStackContext = (DefaultStackContext) stackContext;
            final GsmLocationService service = defaultStackContext.getEnvironment().getGsmLocationService();

            if (service instanceof GsmLocationServiceDefaultImpl) {
                ((GsmLocationServiceDefaultImpl) service).updateLocationInfo(null);
            }
        }
    }

    private static Map<Integer, NetworkSubType> createMapping() {
        Map<Integer, NetworkSubType> retValue = new HashMap<Integer, NetworkSubType>();

        retValue.put(TelephonyManager.NETWORK_TYPE_GPRS, NetworkSubType.GSM);
        retValue.put(TelephonyManager.NETWORK_TYPE_UMTS, NetworkSubType.G3_TDD);
        retValue.put(TelephonyManager.NETWORK_TYPE_HSDPA, NetworkSubType.G3_TDD);
        retValue.put(TelephonyManager.NETWORK_TYPE_HSPA, NetworkSubType.G3_TDD);
        retValue.put(TelephonyManager.NETWORK_TYPE_HSUPA, NetworkSubType.G3_TDD);
        retValue.put(TelephonyManager.NETWORK_TYPE_EDGE, NetworkSubType.GSM);
        retValue.put(TelephonyManager.NETWORK_TYPE_CDMA, NetworkSubType.G3_TDD);
        retValue.put(TelephonyManager.NETWORK_TYPE_EVDO_0, NetworkSubType.G3_TDD);
        retValue.put(TelephonyManager.NETWORK_TYPE_EVDO_A, NetworkSubType.G3_TDD);
        //retValue.put(TelephonyManager.NETWORK_TYPE_EVDO_B, NetworkSubType.EVDO_B);
        //retValue.put(TelephonyManager.NETWORK_TYPE_IDEN, NetworkSubType.IDEN);
        //retValue.put(TelephonyManager.NETWORK_TYPE_RTT, NetworkSubType.RTT);

        return retValue;
    }
}

