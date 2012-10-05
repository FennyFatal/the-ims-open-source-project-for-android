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

package javax.microedition.ims.core.registry;

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.registry.property.AuthenticationProperty;
import javax.microedition.ims.core.registry.property.CapabilityProperty;
import javax.microedition.ims.core.registry.property.MprofProperty;
import javax.microedition.ims.core.registry.property.RegisterProperty;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.text.TextUtils;
/**
 * Default implementation {@link StackRegistry}
 *
 * @author Andrei Khomushko
 */
public class DefaultStackRegistry implements StackRegistryEditor {
    private static final String TAG = "StackRegistryImpl";

    private CommonRegistry commonRegistry;

    private final Set<ClientRegistry> clientRegistries = new HashSet<ClientRegistry>();
    private final ArrayList<CommonRegistry> commonRegistries = new ArrayList<CommonRegistry>();

    private final ListenerHolder<RegistryChangeListener> changeRegistryHolder = new ListenerHolder<RegistryChangeListener>(
            RegistryChangeListener.class);

    public DefaultStackRegistry(CommonRegistry commonRegistry) {
        this.commonRegistry = commonRegistry;
    }

    public void addRegistryChangeListener(RegistryChangeListener listener) {
        changeRegistryHolder.addListener(listener);
    }

    
    public void addRegistryChangeListener(String appId,
                                          RegistryChangeListener listener) {
        changeRegistryHolder.addListener(listener, appId);
    }

    
    public void removeRegistryChangeListener(RegistryChangeListener listener) {
        changeRegistryHolder.removeListener(listener);
    }

    
    public ClientRegistry getClientRegistry(final String appId) {
        return CollectionsUtils.find(clientRegistries, new CollectionsUtils.Predicate<ClientRegistry>() {
            
            public boolean evaluate(ClientRegistry clientRegistry) {
                return clientRegistry.getAppId().equals(appId);
            }
        });
    }

    public ClientRegistry[] getClientRegistries() {
        return clientRegistries.toArray(new ClientRegistry[clientRegistries.size()]);
    }

    
    public CommonRegistry getCommonRegistry() {
        return commonRegistry;
    }

    public CommonRegistry getCommonRegistry(final String appId) {
        for (int i=0;i<commonRegistries.size();i++) {
            CommonRegistry retValue = commonRegistries.get(i);
            if (!TextUtils.isEmpty(appId) && appId.equals(retValue.getAppId()))
                return retValue;
        }
        return null;
    }
    
    public void applyChanges(ClientRegistry clientRegistry,
                             CommonRegistry commonRegistry) {

        updateCommonRegistry(commonRegistry);

        updateClientRegistry(clientRegistry);
    }

    
    public boolean dropClientData(String appId) {
        return removeClientRegistry(appId);
    }

    public boolean dropCommonData(String appId) {
        return removeCommonRegistry(appId);
    }

    protected void updateCommonRegistry(CommonRegistry commonRegistryUpdate) {
        final String appId = commonRegistryUpdate.getAppId();
        CommonRegistry oldRegistry = getCommonRegistry(appId);

        if (!commonRegistryUpdate.equals(oldRegistry)) {
            commonRegistries.remove(oldRegistry);
            commonRegistries.add(commonRegistryUpdate);
        }

        if (isHasUpdate(commonRegistryUpdate)) {
            CommonRegistry oldCommonRegistry = commonRegistry;

            CommonRegistry newCommonRegistry;
            if (commonRegistry == null) {
                newCommonRegistry = commonRegistryUpdate;
            }
            else {
                newCommonRegistry = mergeAndCreateSnapshot(commonRegistryUpdate,
                        commonRegistry);
            }

            this.commonRegistry = newCommonRegistry;

            if (changeRegistryHolder != null && changeRegistryHolder.getNotifier() != null)
                changeRegistryHolder.getNotifier().commonRegistryChanged(
                        new RegistryChangeEventImpl<CommonRegistry>(null,
                                newCommonRegistry, oldCommonRegistry));
        }
    }

    private boolean isHasUpdate(CommonRegistry commonRegistry) {
        return commonRegistry != null
                && (commonRegistry.getRegisterProperties().length != 0
                || commonRegistry.getCapabilityProperties().length != 0
                || commonRegistry.getMprofProperties().length != 0
                || commonRegistry.getConnectionsValues().length != 0
                || commonRegistry.getWriteHeaders().length != 0
                || commonRegistry.getReadHeaders().length != 0
                || commonRegistry.getAuthenticationProperty() != null);
    }

    private void updateClientRegistry(ClientRegistry newClientRegistry) {
        final String appId = newClientRegistry.getAppId();

        ClientRegistry oldClientRegistry = getClientRegistry(appId);

        if (!newClientRegistry.equals(oldClientRegistry)) {
            clientRegistries.remove(oldClientRegistry);
            clientRegistries.add(newClientRegistry);
            if (changeRegistryHolder != null && changeRegistryHolder.getNotifier() != null)
                changeRegistryHolder.getNotifier().clientRegistryChanged(
                        new RegistryChangeEventImpl<ClientRegistry>(appId,
                                newClientRegistry, oldClientRegistry));
        }
        else {
            Logger.log(TAG, "New regestry is the same as old");
        }
    }

    private boolean removeClientRegistry(String appId) {
        final boolean retValue;

        ClientRegistry oldClientRegistry = getClientRegistry(appId);
        if (oldClientRegistry != null) {
            clientRegistries.remove(oldClientRegistry);
            if (changeRegistryHolder != null && changeRegistryHolder.getNotifier() != null)
                changeRegistryHolder.getNotifier().clientRegistryChanged(
                        new RegistryChangeEventImpl<ClientRegistry>(appId, null,
                                oldClientRegistry));
        }
        retValue = oldClientRegistry != null;

        return retValue;
    }

    private static CommonRegistry mergeAndCreateSnapshot(CommonRegistry newRegistry,
                                                         CommonRegistry oldRegistry) {
        final CommonRegistry retValue;

        final Set<RegisterProperty> registerProperties = new HashSet<RegisterProperty>(
                Arrays.asList(oldRegistry.getRegisterProperties())
        );
        registerProperties.addAll(Arrays.asList(newRegistry.getRegisterProperties()));

        final Set<CapabilityProperty> capabilityProperties = new HashSet<CapabilityProperty>(
                Arrays.asList(oldRegistry.getCapabilityProperties())
        );
        capabilityProperties.addAll(Arrays.asList(newRegistry.getCapabilityProperties())
        );

        final Set<MprofProperty> mprofProperties = new HashSet<MprofProperty>(
                Arrays.asList(oldRegistry.getMprofProperties()));
        mprofProperties.addAll(Arrays.asList(newRegistry.getMprofProperties()));

        final Set<String> connectionValues = new HashSet<String>(Arrays
                .asList(oldRegistry.getConnectionsValues()));
        connectionValues.addAll(Arrays.asList(newRegistry
                .getConnectionsValues()));

        final Set<String> writeHeaders = new HashSet<String>(Arrays
                .asList(oldRegistry.getWriteHeaders()));
        writeHeaders.addAll(Arrays.asList(newRegistry.getWriteHeaders()));

        final Set<String> readHeaders = new HashSet<String>(Arrays
                .asList(oldRegistry.getReadHeaders()));
        readHeaders.addAll(Arrays.asList(newRegistry.getReadHeaders()));

        final AuthenticationProperty authProperty = newRegistry.getAuthenticationProperty();
        final AuthenticationProperty xdmAuthProperty = newRegistry.getXdmAuthenticationProperty();

        retValue = new DefaultCommonRegistry(registerProperties,
                capabilityProperties, mprofProperties, connectionValues,
                writeHeaders, readHeaders, authProperty, xdmAuthProperty);

        return retValue;
    }

    private boolean removeCommonRegistry(String appId) {
        boolean retValue = false;

        CommonRegistry oldCommonRegistry = getCommonRegistry(appId);
        if (oldCommonRegistry != null)
            commonRegistries.remove(oldCommonRegistry);

        CommonRegistry newCommonRegistry = null, commonRegistryUpdate;

        for(int i=0;i<commonRegistries.size();i++) {
            commonRegistryUpdate = commonRegistries.get(i);
            if (isHasUpdate(commonRegistryUpdate)) {
                if (newCommonRegistry == null)
                    newCommonRegistry = commonRegistryUpdate;
                else
                    newCommonRegistry = mergeAndCreateSnapshot(commonRegistryUpdate, newCommonRegistry);
            }
        }

        if (!commonRegistry.equals(newCommonRegistry)) {
            retValue = true;
            if (changeRegistryHolder != null && changeRegistryHolder.getNotifier() != null)
                changeRegistryHolder.getNotifier().commonRegistryChanged(
                        new RegistryChangeEventImpl<CommonRegistry>(null,
                                newCommonRegistry, commonRegistry));
            this.commonRegistry = newCommonRegistry;
        }
        return retValue;
    }

    public String toString() {
        return "StackRegistryImpl [changeRegistryHolder="
                + changeRegistryHolder + ", clientRegistries="
                + clientRegistries + ", commonRegistry=" + commonRegistry + "]";
    }
}
