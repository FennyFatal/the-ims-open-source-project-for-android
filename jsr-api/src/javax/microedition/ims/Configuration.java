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

package javax.microedition.ims;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ServiceConnectionException;
import com.android.ims.util.ServiceInterfaceHolder;
import com.android.ims.util.ServiceInterfaceHolder.BinderResolver;

import javax.microedition.ims.android.IConfiguration;
import javax.microedition.ims.android.IGsmLocationInfo;
import javax.microedition.ims.android.IRegistry;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * TODO: in JSR-281, some features, like INVITE (feature-tags) are pre-configurable by the
 * Configuration class, so that certain "media" is linked with set of feature-tags, and other headers, and
 * then with minimal set of function calls such INVITE-dialog is easy to set up. 
 * 
 * Similar approach could be feasible and to be considered for addition in later phases.
 */

/**
 * The <code>Configuration</code> class realizes dynamic installation of IMS
 * applications.
 */
public class Configuration {
    private static final String TAG = "Configuration";

    private static volatile Object mutex = new Object();

    private static volatile Configuration INSTANCE;
    private volatile Context context;
    private final ServiceInterfaceHolder<IConfiguration> interfaceHolder;
    private final IConfiguration configurationPeer;

    private final Set<String> localAppIds = new HashSet<String>();
    private final AtomicBoolean isDone = new AtomicBoolean(false);
    
    private Configuration(final Context context) throws ImsException, ServiceConnectionException {
        this.context = context;
        this.interfaceHolder = new ServiceInterfaceHolder<IConfiguration>(new BinderResolver<IConfiguration>() {
            public IConfiguration asInterface(IBinder service) {
                return IConfiguration.Stub.asInterface(service);
            }
        });

        configurationPeer = interfaceHolder.bindToService(IConfiguration.class.getName(), context, false);
    }

    /**
     * Returns a Configuration that enables dynamic installation of IMS
     * applications.
     *
     * @return a Configuration instance that enables dynamic installation of IMS
     *         applications.
     */
    public static Configuration getConfiguration(Context context) throws ImsException{
        if (INSTANCE == null) {
            synchronized (mutex) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new Configuration(context);
                    }
                    catch (ServiceConnectionException e) {
                        Log.e(TAG, "Cannot connect to configuration service");
                        throw new ImsException(e.getMessage(), e);
                    }
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Returns all AppIds for the local endpoint.
     *
     * @return a string array containing all AppIds for the local endpoint
     */
    public String[] getLocalAppIds() {
        return localAppIds.toArray(new String[0]);
    }

    /**
     * Returns the registry for an IMS application with the specified appId.
     *
     * @param appId - the application id
     * @return the registry for the IMS application specified by the appId
     *         argument
     * @throws IllegalArgumentException - if appId is not set in the registry
     */
    public Registry getRegistry(String appId) {
        if (appId == null) {
            throw new IllegalArgumentException("The appId argument is null");
        }

        Registry retValue = null;

        try {
            IRegistry iRegistry = configurationPeer.getRegistry(appId);
            if (iRegistry == null) {
                throw new IllegalArgumentException("The appId is not set in the registry");
            }
            if (!isDone.get()) {
                retValue = createRegistry(iRegistry);
            }
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return retValue;
    }

    private static Registry createRegistry(IRegistry iRegistry) {
        return new Registry(iRegistry.getProperties(), new String[0], iRegistry.getQosLevel());
    }

    private static IRegistry createIRegistry(String appId, String classname, Registry registry) {
        return new IRegistry(appId, classname, registry.getQosLevel(), registry.getProperties());
    }

    /**
     * Returns true if there is a registry for the IMS application with the
     * specified appId, else false.
     *
     * @param appId - the application id
     * @return true if there is a registry for the IMS application specified by
     *         the appId argument, else false
     * @throws IllegalArgumentException - if the appId argument is null
     */
    public boolean hasRegistry(String appId) {
        if (appId == null) {
            throw new IllegalArgumentException("The appId argument is null");
        }

        boolean retValue = false;

        try {
            retValue = configurationPeer.hasRegistry(appId);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return retValue;
    }

    /**
     * Removes the registry for the IMS application and deletes the binding to
     * the owning Java application. If this method is invoked, the local
     * endpoint will no longer be able to create new services with the
     * application identity specified by the appId argument, this does not
     * affect services that are already created.
     *
     * @param appId - the application id
     * @throws IllegalArgumentException - if appId is not set in the registry
     */
    public void removeRegistry(String appId) {
        if (!isDone.get()) {
            if (appId == null) {
                throw new IllegalArgumentException("The appId argument is null");
            }

            try {
                boolean isSuccees = configurationPeer.removeRegistry(appId);
                if (isSuccees) {
                    localAppIds.remove(appId);
                }
                else {
                    throw new IllegalArgumentException("The appId is not set in the registry");
                }
            }
            catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * Sets the registry for an IMS application and binds it to a parent Java
     * application.
     *
     * @param appId     - the application id
     * @param classname - the classname of the Java application that the IMS application is bound to
     * @param registry  - an array of arrays, specifying key and value(s)
     * @throws IllegalArgumentException - if the appId or classname argument is null
     * @throws IllegalArgumentException - if the registry argument is null or if it has invalid
     *                                  syntax
     */
    public void setRegistry(String appId, String classname,
                            Registry registry, RegistryListener listener) {
        if (!isDone.get()) {
            if (appId == null) {
                throw new IllegalArgumentException("The appId argument is null");
            }

            if (classname == null) {
                throw new IllegalArgumentException("The classname argument is null");
            }

            if (registry == null) {
                throw new IllegalArgumentException("The registry argument is null");
            }

            if (!registry.isValid()) {
                throw new IllegalArgumentException("The registry argument has invalid syntax");
            }

            IRegistry iRegistry = createIRegistry(appId, classname, registry);
            try {
                configurationPeer.setRegistry(iRegistry);
                localAppIds.add(appId);
            }
            catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void updateLocation(GsmLocationInfo locationInfo) {
        if (!isDone.get()) {
            if (locationInfo == null) {
                throw new IllegalArgumentException("The locationInfo argument is null");
            }
            
            String mccMnc = locationInfo.getMccMnc();
            if (mccMnc == null || mccMnc.trim().length() < 5) {
                throw new IllegalArgumentException("The locationInfo.mccMnc is incorrect");
            }
            
            int mcc = 0;
            try {
                String mccValue = mccMnc.substring(0, 3);
                mcc = Integer.parseInt(mccValue);
            } catch (NumberFormatException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            
            int mnc = 0;
            try {
                String mncValue = mccMnc.substring(3);
                mnc = Integer.parseInt(mncValue);
                
            } catch (NumberFormatException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            
            IGsmLocationInfo info = new IGsmLocationInfo(
                    locationInfo.getCid(), 
                    locationInfo.getLac(),
                    mcc, 
                    mnc,
                    locationInfo.getNetworkType());
            try {
                
                configurationPeer.updateLocation(info);
            }
            catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }
    
    public void close() {
        Log.e(TAG, "Configuration close. " + this.hashCode());

        if (isDone.compareAndSet(false, true)) {
            try {
                localAppIds.clear();
                interfaceHolder.unbindFromService(context, false);
            }
            catch (ServiceConnectionException e) {
                Log.e(TAG, "Cannot unbind from configuration service");
            }
            context = null;
            INSTANCE = null;
        }
    }
}