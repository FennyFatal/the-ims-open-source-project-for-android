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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.PowerManager.WakeLock;
import android.os.StrictMode;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;

import javax.microedition.ims.DefaultStackContext;
import javax.microedition.ims.StackHelper;
import javax.microedition.ims.core.auth.AKAAuthProviderMockImpl;
import javax.microedition.ims.android.auth.AKAAuthProviderAndroidImpl;
import javax.microedition.ims.android.config.AndroidConfiguration;
import javax.microedition.ims.android.env.AlarmScheduledService;
import javax.microedition.ims.android.env.HardwareInfoAndroidImpl;
import javax.microedition.ims.common.DefaultTimeoutUnit;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.ScheduledService;
import javax.microedition.ims.config.BaseConfiguration;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.AkaException;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.ReasonCode;
import javax.microedition.ims.core.connection.ConnectionDataProvider;
import javax.microedition.ims.core.connection.ConnectionDataProviderConfigVsDnsImpl;
import javax.microedition.ims.core.connection.ConnectionManagerHandler;
import javax.microedition.ims.core.connection.ConnectionSecurityInfoProviderImplDefault;
import javax.microedition.ims.core.connection.GsmLocationServiceDefaultImpl;
import javax.microedition.ims.core.env.ConnectionManager;
import javax.microedition.ims.core.env.DefaultScheduledService;
import javax.microedition.ims.core.env.Environment;
import javax.microedition.ims.core.env.EnvironmentDefaultImpl;
import javax.microedition.ims.core.messagerouter.MessageRouterComposite;
import javax.microedition.ims.core.messagerouter.MessageRouterMSRP;
import javax.microedition.ims.core.messagerouter.MessageRouterSIP;
import javax.microedition.ims.core.registry.DefaultCommonRegistry;
import javax.microedition.ims.core.registry.DefaultStackRegistry;
import javax.microedition.ims.core.registry.StackRegistry;
import javax.microedition.ims.core.registry.StackRegistryEditor;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.dns.DNSResolverDNSJavaImpl;
import javax.microedition.ims.transport.messagerouter.Router;

/**
 * This service runs in a different process than the application.
 * Because client must use IPC to interact with it.
 *
 * @author Andrei Khomushko
 */
public class ConnectorService extends Service implements UncaughtExceptionHandler{
    //private static final int FINALIZATION_TIMEOUT = 32;
    //private static final int FINALIZATION_TIMEOUT = 20;
    private static final int FINALIZATION_TIMEOUT = 10;

    private static final String TAG = "ConnectorService";

    private static final String LOG_FILE_PATH = "/sdcard/log.txt";

    private final AtomicReference<IMSStack<IMSMessage>> imsStackHolder = new AtomicReference<IMSStack<IMSMessage>>();

    private final AtomicReference<BroadcastReceiver> connectionReceiverHolder = new AtomicReference<BroadcastReceiver>();
    private final AtomicReference<BroadcastReceiver> alarmReceiverHolder = new AtomicReference<BroadcastReceiver>();

    private final AtomicReference<ConnectorBinder> connectorHolder = new AtomicReference<ConnectorBinder>();
    private final AtomicReference<ConnectionStateBinder> connectionStateHolder = new AtomicReference<ConnectionStateBinder>();
    private final AtomicReference<ConfigurationBinder> configurationHolder = new AtomicReference<ConfigurationBinder>();
    private final AtomicReference<StackErrorBinder> stackCreationErrorHolder = new AtomicReference<StackErrorBinder>();

    private final AtomicReference<WifiLock> wifiLockHolder = new AtomicReference<WifiManager.WifiLock>();
    //private final AtomicReference<WakeLock> wakeLockHolder = new AtomicReference<WakeLock>();

    private CountDownLatch releaseLatch = new CountDownLatch(1);

    private boolean isFileOutput;

    //private StackFinalizationHandler stackFinalizationHandler;

    public void onCreate() {
        Logger.log(TAG, "Service created, pid: " + Process.myPid());

        enableLogging();

		  Thread.setDefaultUncaughtExceptionHandler(this);

        {
            WifiLock wifiLock = retrieveWifiLock();
            if(!wifiLock.isHeld()){
                wifiLock.acquire();
            }
            wifiLockHolder.compareAndSet(null, wifiLock);
        }

/*        {
            WakeLock wakeLock = retrieveWakeLock();
            if(!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            wakeLockHolder.compareAndSet(null, wakeLock);
        }
*/
        
/*        {
            HandlerThread thread = new HandlerThread("StackFinalizationHandler",
                    Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            
            Looper mServiceLooper = thread.getLooper();
            stackFinalizationHandler = new StackFinalizationHandler(mServiceLooper);
        }
*/        

        IMSStack<IMSMessage> imsStack;
        try {
            final Configuration configuration = new AndroidConfiguration(ConnectorService.this);
            final ConnectionManager connectionManager = StackHelper.newAndroidConnectionManager(this);
            final ConnectionDataProviderConfigVsDnsImpl connDataProvider = new ConnectionDataProviderConfigVsDnsImpl(
                    configuration,
                    new DNSResolverDNSJavaImpl(configuration, connectionManager)
            );
                        
            final ScheduledService scheduledService;
            {
                AlarmScheduledService alarmScheduledService = new AlarmScheduledService(this);

                IntentFilter filter = new IntentFilter();
                filter.addAction(AlarmScheduledService.ACTION_IMS_ALARM_SCHEDULE_SERVICE);

                registerReceiver(alarmScheduledService, filter);

                alarmReceiverHolder.set(alarmScheduledService);

                scheduledService = alarmScheduledService;
            }


            //final ScheduledService scheduledService = new DefaultScheduledService();

            imsStack = createImsStack(configuration, connectionManager, connDataProvider, scheduledService);

            imsStackHolder.compareAndSet(null, imsStack);

            final ConnectionReceiver connectionReceiver = createConnectionReceiver(imsStack);
            connectionReceiverHolder.compareAndSet(null, connectionReceiver);

            //TODO review
            final ConnectorBinder connector = new ConnectorBinder(imsStack, connDataProvider, releaseLatch);
            connectorHolder.compareAndSet(null, connector);

            final ConnectionStateBinder connectionState = instantiateConnectionStateBinder(imsStack);
            imsStack.getRegisterService().addRegistrationListener(connectionState);
            imsStack.getContext().getEnvironment().getConnectionManager().addConnStateListener(connectionState);
            connectionStateHolder.compareAndSet(null, connectionState);

            ConfigurationBinder configurationBinder = createConfigurationBinder(imsStack);
            configurationHolder.compareAndSet(null, configurationBinder);

            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .permitNetwork()
                    .build());

        } catch (AkaException e) {
            ReasonCode reason = e.getReason();
            Logger.log(TAG, reason.toString());
            e.printStackTrace();

            int errorCode = ErrorsUtils.toIErrorCode(reason);
            String reasonData = reason.getErrCodeString();
            String message = reason.getDescription();

            publishError(new IError(errorCode, message, reasonData));

        } catch (IMSStackException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();

            publishError(new IError(IError.ERROR_UNKNOWN, e.getMessage()));
        }
    }

    private WifiLock retrieveWifiLock() {
        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiLock wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
        return wifiLock;
    }

/*    private WakeLock retrieveWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        return wl;
    }
*/

    private void publishError(IError iError) {
        StackErrorBinder errorDescriptor = new StackErrorBinder(iError);
        stackCreationErrorHolder.compareAndSet(null, errorDescriptor);
    }

    private ConfigurationBinder createConfigurationBinder(final IMSStack<IMSMessage> imsStack) {
        final ConfigurationBinder configuration;

        StackRegistryEditor stackRegistry = (StackRegistryEditor)imsStack.getContext().getStackRegistry();
        configuration = new ConfigurationBinder(stackRegistry, imsStack.getContext());

        return configuration;
    }

    private ConnectionReceiver createConnectionReceiver(final IMSStack<IMSMessage> imsStack) {
        final ConnectionReceiver connectionReceiver;

        final ConnectionManagerHandler<?> connectionManager = (ConnectionManagerHandler<?>)imsStack.getContext().getEnvironment().getConnectionManager();

        connectionReceiver = instantiateAndRegistryConnectionReceiver(connectionManager);

        return connectionReceiver;
    }

    private IMSStack<IMSMessage> createImsStack(Configuration configuration, ConnectionManager connectionManager,
            ConnectionDataProvider connDataProvider, ScheduledService scheduledService)
            throws IMSStackException, AkaException {
        final IMSStack<IMSMessage> imsStackInstance;

        final Configuration configSnapshot = new BaseConfiguration.ConfigurationBuilder(configuration).build();

        final DefaultStackRegistry stackRegistry = new DefaultStackRegistry(new DefaultCommonRegistry.CommonRegistryBuilder().build());
	
        imsStackInstance = instantiateStack(getApplicationContext(),
                configSnapshot,
                connectionManager,
                stackRegistry,
                connDataProvider,
                scheduledService);

        return imsStackInstance;
    }

    private void enableLogging() {
        if (isFileOutput) {
            try {
                enableFileLogging(LOG_FILE_PATH);
            } catch (IOException e) {
                Logger.log(TAG, "Error in enabling file logging, message = " + e.getMessage());
            }
        }
    }

    /**
     * redirect sys.out stream to specified file
     */
    private void enableFileLogging(String path) throws IOException {
        final File logFile = new File(path);

        logFile.deleteOnExit();
        logFile.createNewFile();
        System.setOut(new PrintStream(logFile));
        Logger.log(TAG, "Sys.out redirected to" + path);
    }

    private IMSStack<IMSMessage> instantiateStack(
            final Context androidContext,
            final Configuration configuration,
            final ConnectionManager connectionManager,
            final StackRegistry stackRegistry
            /*OptionsRegistry optionsRegistry*/,
            final ConnectionDataProvider connDataProvider,
            final ScheduledService scheduledService) 
            throws IMSStackException, AkaException {
        final IMSStack<IMSMessage> retValue;

        final RepetitiousTaskManager repetitiousTaskManager = new RepetitiousTaskManager(scheduledService);

            final Router<IMSMessage> messageRouter =
                    new MessageRouterComposite.Builder(configuration, connDataProvider).
                            addRouter(new MessageRouterMSRP()).
                            addRouter(new MessageRouterSIP(configuration, connDataProvider, repetitiousTaskManager)).
                            build();

            Environment env = new EnvironmentDefaultImpl.Builder()
                    .connectionManager(connectionManager)
                    .gsmLocationService(new GsmLocationServiceDefaultImpl())
                    .hardwareInfo(new HardwareInfoAndroidImpl(androidContext))
                    .externalStorageDirectory(android.os.Environment.getExternalStorageDirectory())
                    .build();
            Logger.log(TAG, "instantiateStack#env = " + env);

            retValue = StackHelper.newIMSSipStack(
                    new DefaultStackContext.Builder().
                            configuration(configuration).
                            router(messageRouter).
                            environment(env).
                            stackRegistry(stackRegistry).
                          //akaAuthProvider(new AKAAuthProviderAndroidImpl(androidContext)).
                            akaAuthProvider(new AKAAuthProviderMockImpl(androidContext)).
                            connectionSecurityInfoProvider(
                                    new ConnectionSecurityInfoProviderImplDefault(
                                            connDataProvider.getDNSResolver(),
                                            configuration.getRegistrationName(),
                                            configuration
                                    )).
                            connectionDataProvider(connDataProvider).
                            repetitiousTaskManager(repetitiousTaskManager).
                            build()
            );

            Logger.log(TAG, "IMS stack initialized");

        return retValue;
    }

    private ConnectionStateBinder instantiateConnectionStateBinder(final IMSStack<IMSMessage> imsStack) {
        final ConnectionStateBinder connectionState;

        final ConnectionManager connectionManager = imsStack.getContext().getEnvironment().getConnectionManager();

        connectionState = new ConnectionStateBinder(imsStack.getRegisterService(), connectionManager);

        return connectionState;
    }

    private ConnectionReceiver instantiateAndRegistryConnectionReceiver(final ConnectionManagerHandler<?> connectionManagerHandler) {
        final ConnectionReceiver connectionReceiver = new ConnectionReceiver(connectionManagerHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED");

        registerReceiver(connectionReceiver, filter);

        return connectionReceiver;
    }


    enum BinderType {

        CONNECTOR(IConnector.class.getName()),
        CONNECTION_STATE(IConnectionState.class.getName()),
        CONFIGURATION(IConfiguration.class.getName()),
        STACK_ERROR(IStackError.class.getName());

        private final String action;

        private static Map<String, BinderType> mapping = new HashMap<String, BinderType>();

        static {
            for(BinderType binderType: values()) {
                mapping.put(binderType.action, binderType);
            }
        }

        private BinderType(final String action) {
            this.action = action;
        }

        private static BinderType parse(String action) {
            return action != null? mapping.get(action): null;
        }

        @Override
        public String toString() {
            return "BinderType[action = " + action + "]";
        }
    }

    public IBinder onBind(Intent intent) {
        Logger.log(TAG, "onBind#intent = " + intent);

        final BinderType binderType = BinderType.parse(intent.getAction());

        if(binderType == null) {
            Logger.log(TAG, "Unknow binder type: intent = " + intent);
            return null;
        }

        StackErrorBinder stackErrorBinder = stackCreationErrorHolder.get();
        if(stackErrorBinder != null) {
            Logger.log(TAG, "onBind#error: " + stackErrorBinder.toString());
            return stackErrorBinder;
        }

        return findBinder(binderType);
    }

    private Binder findBinder(final BinderType binderType) {
        final Binder mBinder;
        switch (binderType) {
            case CONNECTOR:
                mBinder = connectorHolder.get();
                break;
            case CONNECTION_STATE:
                mBinder = connectionStateHolder.get();
                break;
            case CONFIGURATION:
                mBinder = configurationHolder.get();
                break;
            default:
                mBinder = null;
        }
        return mBinder;
    }


    public boolean onUnbind(Intent intent) {
        boolean res = super.onUnbind(intent);
        Logger.log(TAG, "Service unbinded#intent = " + intent);
        BinderType binderType = BinderType.parse(intent.getAction());
        try {
            // wait ConnectorBinder to finish presenceService and coreService closed
            if (binderType==BinderType.CONNECTOR) {
                releaseLatch.await(10, TimeUnit.SECONDS);
                Logger.log(TAG, "CONNECTOR CountDownLatch await finish");
            }
        } catch (InterruptedException e) {
            Logger.log(TAG, "CONNECTOR CountDownLatch Exception: "+e);
        }
        return res;
    }

    /**
     * The IConnector is defined through IDL
     */
    public void onDestroy() {
        Logger.log(TAG, "onDestroy");
        
        if (connectionReceiverHolder.get() != null) unregisterReceiver(connectionReceiverHolder.get());

        //stackFinalizationHandler.sendEmptyMessage(0);
        doFinalize();
        
        if (alarmReceiverHolder.get() != null) unregisterReceiver(alarmReceiverHolder.get());
        

        Logger.log(TAG, "finalization request is sent");
        //Debug.stopMethodTracing();
        
        Thread.setDefaultUncaughtExceptionHandler(null);
    }
    
    private final class StackFinalizationHandler extends Handler {
        public StackFinalizationHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
            doFinalize();
        }
    }

    private void doFinalize() {
        Logger.log(TAG, "stack finalization start");
        
        try {
            final IMSStack<IMSMessage> imsStack = imsStackHolder.get();
            if(imsStack != null) {
                finalizeStack(imsStack);

                final ConnectionManager connectionManager = imsStack.getContext().getEnvironment().getConnectionManager();
                connectionManager.removeConnStateListener(connectionStateHolder.get());

                connectionStateHolder.set(null);
                imsStackHolder.set(null);

                //unregisterReceiver(connectionReceiverHolder.get());
                connectionReceiverHolder.set(null);

                Logger.log(TAG, "Service destroyed");
            }

            {
                WifiLock wifiLock = wifiLockHolder.get();
                if(wifiLock != null && wifiLock.isHeld()) {
                    wifiLock.release();
                }
                wifiLockHolder.set(null);
            }

/*            {
                WakeLock wakeLock = wakeLockHolder.get();
                if(wakeLock != null && wakeLock.isHeld()) {
                    wakeLock.release();
                }
                wakeLockHolder.set(null);
            }
*/        } finally {
            Logger.log(TAG, "stack finalization end");
        }
    }

    private void finalizeStack(final IMSStack<IMSMessage> imsStack) {
        if (imsStack != null) {

            //imsStack.getRegisterService().unregister();
            //Wait max 32 seconds when unregistering to avoid ANR
            imsStack.getRegisterService().unregister(new DefaultTimeoutUnit(FINALIZATION_TIMEOUT, TimeUnit.SECONDS));
            
            imsStack.getRegisterService().removeRegistrationListener(connectionStateHolder.get());

            //imsStack.getContext().getConnectionManager().removeConnStateListener(connectionState.get());
            //imsStack.getRegisterService().removeRegistrationListener(connectionState.get());

            //StackHelper.shutdownStack(imsStack, 200);
            StackHelper.shutdownStack(imsStack);
            TransactionUtils.reset();
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Logger.log("UncaughtExceptionHolder", "thread = " + thread.getName());
        ex.printStackTrace();
    }
}
