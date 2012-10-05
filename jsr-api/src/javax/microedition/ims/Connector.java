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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ManagableConnection;
import com.android.ims.ServiceCloseListener;
import com.android.ims.ServiceConnectionException;
import com.android.ims.ServiceImpl;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.configuration.DefaultEnvironment;
import com.android.ims.configuration.Environment;
import com.android.ims.core.CoreServiceImpl;
import com.android.ims.im.IMServiceImpl;
import com.android.ims.presence.DefaultPresenceService;
import com.android.ims.xdm.XDMServiceImpl;

import javax.microedition.ims.android.IConnector;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.IStackError;
import javax.microedition.ims.android.core.ICoreService;
import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.msrp.IIMService;
import javax.microedition.ims.android.presence.IPresenceService;
import javax.microedition.ims.android.xdm.IXDMService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class provides a minimal support for generic connections framework found
 * from J2ME. This means that one can open IMS-connections using J2ME-like
 * syntax, but there are still some Android specific differences (like context
 * parameters).
 */
public class Connector {
    private static final String TAG = "Connector";

    private enum IMSShema {
        CORE("imscore"), XDM("imsxdm"), IM("imsim"), PRESENCE("imspresence");

        private String name;

        private IMSShema(String name) {
            this.name = name;
        }

        public static IMSShema parse(String name) {
            IMSShema retValue = null;
            if (CORE.name.equals(name)) {
                retValue = CORE;
            } else if (XDM.name.equals(name)) {
                retValue = XDM;
            } else if (IM.name.equals(name)) {
                retValue = IM;
            } else if (PRESENCE.name.equals(name)) {
                retValue = PRESENCE;
            }
            return retValue;
        }
    }

    /**
     * Access mode READ.
     */
    public static final int READ = 1;

    /**
     * Access mode READ_WRITE.
     */
    public static final int READ_WRITE = 3;

    /**
     * Access mode WRITE.
     */
    public static final int WRITE = 2;

    public static final int DEFAULT_TIMEOUT = 10000;

    private static final Connector instance = new Connector();

    private final AtomicReference<IConnector> connector = new AtomicReference<IConnector>();
    private final AtomicReference<IStackError> stackErrorHolder = new AtomicReference<IStackError>();

    private volatile CyclicBarrier cyclicBarrier;
    private final Counter clientCounter = new Counter(0);

    private Connector() {
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Connected successfully to remote connector service: "
                    + name.toShortString());

            try {
                String interfaceDescriptor = service.getInterfaceDescriptor();

                if(IStackError.class.getName().equals(interfaceDescriptor)) {
                    stackErrorHolder.compareAndSet(null, IStackError.Stub.asInterface(service));
                } else {
                    connector.compareAndSet(null, IConnector.Stub.asInterface(service));
                }
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }

            try {
                cyclicBarrier.await();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "onServiceConnected NULL");;
            }
        }

        
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Disconnected from remote connector service: "
                    + name.toShortString());
            connector.set(null);
            stackErrorHolder.set(null);

            notifyErrorListeners();
            errorListeners.clear();
        }

		private void notifyErrorListeners() {
			for(ErrorListener errorListener: errorListeners){
            	errorListener.serviceClosed();
            }
		}
    };
    
    public interface ErrorListener{
    	void serviceClosed();
    }
    
    List<ErrorListener> errorListeners = new ArrayList<ErrorListener>();

    static class ConfigurationHolder {
        private static AppConfiguration configuration;

        private static final AppConfiguration loadConfiguration(String appId, Context context)
            throws ImsException{
            if (configuration == null || isConfigurationChanged()) {
                configuration = createConfiguration(appId, context);
            }
            return configuration;
        }

        private static boolean isConfigurationChanged() {
            Environment environment = (Environment) System.getProperties().get(
                    Environment.class.getName());
            return environment != null
                    && !configuration.getEnvironment().equals(environment);
        }

        private static AppConfiguration createConfiguration(String appId, Context context)
            throws ImsException{
            final AppConfiguration retValue;
            
            Environment environment = (Environment) System.getProperties().get(
                    Environment.class.getName());
            
            if (environment == null) {
                environment = createDefaultEnvironment(appId, context);
            }
            
            retValue = new AppConfiguration(environment);
            
            return retValue;
        }
        
        private static Environment createDefaultEnvironment(String appId, Context context)
            throws ImsException{
            final Environment retValue;
            
            Configuration configuration = Configuration.getConfiguration(context);
            Registry registry = configuration.getRegistry(appId);
            retValue = new DefaultEnvironment(registry, "AES_CM_128_HMAC_SHA1_32");
            
            return retValue;
        }
    }

    /**
     * Connect to application service and establish connection.
     * 
     * @param context
     *            - Object that encapsulates application environment. It need to
     *            bind to remote service.
     * @param timeouts
     *            - A flag to indicate that the caller wants timeout exceptions
     * 
     * @throws ServiceConnectionException
     *             - if connection is not establish successfully.
     */
    private void bindToConnectorService(Context context, boolean timeouts)
            throws ServiceConnectionException, ImsException {
        Log.i(TAG, "Start binding to remote connector service.");
        cyclicBarrier = new CyclicBarrier(2);
        boolean serviceBinded = context.bindService(new Intent(IConnector.class
                .getName()), mConnection, Context.BIND_AUTO_CREATE);

        if (!serviceBinded) {
            throw new ServiceConnectionException(
                    "context.bindService == false. May be service has not been installed on device properly.");
        }

        try {
            if (timeouts) {
                cyclicBarrier.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            } else {
                cyclicBarrier.await();
            }
            Log.i(TAG, "remote connector service is "
                    + (connector.get() == null ? "null" : "not null"));
        } catch (BrokenBarrierException e) {
            throw new ServiceConnectionException(String.format(
                    "BrokenBarrierException: %1$s", e.getMessage()));
        } catch (InterruptedException e) {
            throw new ServiceConnectionException(String.format(
                    "InterruptedException: %1$s", e.getMessage()));
        } catch (TimeoutException e) {
            throw new ServiceConnectionException(String.format(
                    "TimeoutException: %1$s", e.getMessage()));
        } finally {
            Log.i(TAG, "finally cyclicBarrier");
            cyclicBarrier = null;
            // cyclicBarrier.reset();
        }

        if (connector.get() == null) {
            if(stackErrorHolder.get() != null) {
                IStackError stackError = stackErrorHolder.get();
                IError error = extractError(stackError);

                context.unbindService(mConnection);

                throw createImsException(error);
            } else {
                throw new ServiceConnectionException(
                "Cann't bind to connector service");
            }
        }

    }

    private IError extractError(IStackError stackError) {
        IError error = null;
        try {
            error = stackError.getError();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return error;
    }

    public static synchronized void close(final Context context) {
        if (context != null) {
            instance.unbindFromConnectorService(context);
        } else {
            Log.e(TAG, "close#context is null");
        }

    }

    private void unbindFromConnectorService(final Context context) {
        if (connector.get() != null) {
            Log.i(TAG, "unbindFromConnectorService#start");
            try {
                context.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "unbindFromConnectorService# error: " + e);
            }
            connector.set(null);
            Log.i(TAG, "unbindFromConnectorService#end");
        } else {
            Log.i(TAG, "unbindFromConnectorService#not yet connected");
        }
    }

    /**
     * Create and open a Connection. This function is a blocking call as defined
     * by JSR281. You may not call this function from the main thread of your
     * Android application. This is because the function uses a blocking wait to
     * detect when the remote IMS service is usable. Calling this from main
     * thread would cause a deadlock, since service connections are notified
     * when the application returns to main loop.
     * 
     * @param name
     *            Defines the application ID and possible parameters.
     * @param context
     *            Android context to use for binding the IMS service.
     * 
     * @throws IllegalArgumentException
     *             - if arguments is invalid
     * @throws IllegalArgumentException
     *             - if the requested protocol type is not supported
     * @throws ImsException - if connection can't be open
     *             
     */
    public static synchronized Connection open(String name, Context context) 
        throws ImsException{
        return open(name, context, READ_WRITE);
    }

    /**
     * Create and open a Connection. The value for mode is ignored for IMS
     * purposes, but it's there for compatibility with J2ME generic connections
     * framework.
     * 
     * @param name
     *            Defines the application ID and possible parameters
     * @param context
     *            Android context to use for binding the IMS service.
     * @param mode
     *            Ignored for now. Exists for compatibility reasons.
     * 
     * @throws IllegalArgumentException
     *             - if arguments is invalid
     * @throws IllegalArgumentException
     *             - if the requested protocol type is not supported
     * @throws ImsException - if connection can't be open            
     */
    public static synchronized Connection open(String name, Context context,
            int mode) throws ImsException{
        return open(name, context, mode, false);
    }

    /**
     * Create and open a Connection.
     * 
     * @param name
     *            - Defines the application ID and possible parameters
     * @param context
     *            - Android context to use for binding the IMS service.
     * @param mode
     *            - Ignored for now. Exists for compatibility reasons.
     * @param timeouts
     *            - Whether the stack should report timeouts
     * 
     * @throws IllegalArgumentException
     *             - if arguments is invalid
     * @throws IllegalArgumentException
     *             - if the requested protocol type is not supported
     * 
     *             Example: imscore://myAppId;ServiceID=
     * @throws ImsException - if connection can't be open            
     */
    public static synchronized Connection open(String name, Context context,
            int mode, boolean timeouts) throws ImsException{
        return instance.openConnection(name, context, mode, timeouts);
    }

    /**
     * Create and open a Connection.
     * 
     * @param name
     *            - Defines the application ID and possible parameters
     * @param context
     *            - Android context to use for binding the IMS service.
     * @param mode
     *            - Ignored for now. Exists for compatibility reasons.
     * @param timeouts
     *            - Whether the stack should report timeouts
     * 
     * @return Connection
     * 
     * @throws IllegalArgumentException
     *             - if arguments is invalid
     * @throws IllegalArgumentException
     *             - if the requested protocol type is not supported
     * @throws ImsException - if connection can't be open            
     */
    private Connection openConnection(final String name, Context context,
            int mode, boolean timeouts) throws ImsException{
        Log.i(TAG, "openConnection#name = " + name);

        Connection retValue = null;

        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("The name argument is invalid");
        }

        IMSShema shema = parseSchema(name);
        if (shema == null) {
            throw new IllegalArgumentException("The name argument is invalid");
        }

        try {
            retValue = doOpenConnection(shema, name, context, mode, timeouts);
        } catch (ImsException e) {
            Log.e(TAG, e.getMessage(), e);
            throw e;
        } catch (ServiceConnectionException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException(e.getMessage(), e);
        }

        return retValue;
    }

    private Connection doOpenConnection(IMSShema shema, final String name, 
            Context context, int mode, boolean timeouts)
            throws ServiceConnectionException, ImsException {

        final ServiceImpl retValue;

        Log.i(TAG, "Start openning connection, clientId = " + name);
        if (connector.get() == null) {
            bindToConnectorService(context, timeouts);
        }

        if (connector.get() == null) {
            throw new ServiceConnectionException(
                    "Can't connect to remote service service, connector is null");
        }

        ServiceImpl connection = createConnection(shema, name, context);
        connection.addServiceCloseListener(serviceCloseListener);
        errorListeners.add(connection);
        clientCounter.increment();

        retValue = connection;

        return retValue;
    }

    private ServiceImpl createConnection(final IMSShema shema, String name,
            Context context) throws ImsException, ServiceConnectionException{
        Log.i(TAG, String.format("createConnection#shema = %s , name = %s", shema, name));

        final ServiceImpl retValue;

        final String appId = parseAppId(name);
        
        try {
            switch (shema) {
            case CORE: {
                final IExceptionHolder exceptionHolder = new IExceptionHolder();
                IBinder iBinder = new Binder();
                Log.i(TAG, "CORE: iBinder = "+iBinder);
                ICoreService coreService = connector.get()
                        .openCoreService(name, iBinder, exceptionHolder);
                
                IError iError = (IError)exceptionHolder.getParcelableException();
                if (iError != null) {
                    throw createImsException(iError);
                }
                
                AppConfiguration configuration = ConfigurationHolder.loadConfiguration(appId, context);
                configuration.setForceSrtp(coreService.isForceSrtp());
                retValue = new CoreServiceImpl(coreService, context, configuration);
                break;
            }
            case XDM: {
                IXDMService xdmService = connector.get().openXDMService(name);
                //if (xdmService == null) {
                //    throw new ImsException("The xdmService is null");
                //}
                
                retValue = new XDMServiceImpl(xdmService, context,
                        ConfigurationHolder.loadConfiguration(appId, context));
                break;
            }
            case IM: {
                IIMService imService = connector.get().openIMService(name);
                //if (imService == null) {
                //    throw new ImsException("The imService is null");
                //}
                retValue = new IMServiceImpl(imService, context,
                        ConfigurationHolder.loadConfiguration(appId, context));
                break;
            }
            case PRESENCE: {
                IPresenceService presenceService = connector.get()
                        .openPresenceService(name);
                //if (presenceService == null) {
                //    throw new ImsException(
                //            "The presenceService is null");
                //}

                retValue = new DefaultPresenceService(presenceService, context,
                        ConfigurationHolder.loadConfiguration(appId, context));
                break;
            }
            default: {
                Log.w(TAG, "Unknown shema = " + shema);
                retValue = null;
                //throw new ImsException("Unknown shema: " + shema);
            }
        }
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we can count on soon being
            // disconnected (and then reconnected if it can be restarted)
            // so there is no need to do anything here.
            Log.e(TAG, "Connection not opened, failed to communicate with ims stack.", e);
            throw new ServiceConnectionException(e.getMessage(), e);
        }

        Log.i(TAG, "createConnection#connection opened");

        return retValue;
    }

    private ImsException createImsException(IError iError) {
        return new ImsException(iError.getErrorCode(), iError.getMessage(), iError.getReasonData());
    }
    
    private static String parseAppId(String name) {
        final String retValue;
        
        String[] exprs = name.split("://", 2);
        exprs = exprs[1].split(";", 2);
        retValue = exprs[0];
        
        return retValue;
    }

    private ServiceCloseListener serviceCloseListener = new ServiceCloseListener() {
        
        public void serviceClosed(final ManagableConnection connection) {
            Log.i(TAG, "serviceClosed#start");
            clientCounter.decrement();
            if (clientCounter.test(0)) {            	
                unbindFromConnectorService(connection.getContext());
            }
            connection.removeServiceCloseListener(this);
            errorListeners.remove(connection);
            Log.i(TAG, "serviceClosed#end");
        }
    };

    class Counter {
        private final AtomicReference<Integer> value = new AtomicReference<Integer>();

        private Counter(Integer initialValue) {
            value.set(initialValue);
        }

        private void increment() {
            value.set(value.get() + 1);
        }

        private void decrement() {
            value.set(value.get() - 1);
        }

        private boolean test(Integer testValue) {
            return testValue.equals(value.get());
        }
    }

    private static IMSShema parseSchema(String uri) {
        final IMSShema retValue;

        int schemaIdx = uri.indexOf("://");
        if (schemaIdx > -1) {
            String shemeName = uri.substring(0, schemaIdx);
            retValue = IMSShema.parse(shemeName);
        } else {
            retValue = null;
        }

        return retValue;
    }
}
