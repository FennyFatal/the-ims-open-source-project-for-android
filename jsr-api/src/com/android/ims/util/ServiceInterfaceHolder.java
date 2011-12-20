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

package com.android.ims.util;

import com.android.ims.ServiceConnectionException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.IStackError;

/**
 * Helper class for managing connection to specified service.
 *
 * @author ext-akhomush
 * @param <T> - type of interfaces.
 */
public final class ServiceInterfaceHolder<T extends IInterface> {
    public static final int DEFAULT_TIMEOUT = 10000;
    private static final String TAG = "ServiceInterfaceHolder";

    private final BinderResolver<T> binderResolver;
    private final AtomicBoolean binded = new AtomicBoolean(false);

    private final AtomicReference<T> serviceInterface = new AtomicReference<T>();
    private final AtomicReference<IStackError> stackErrorHolder = new AtomicReference<IStackError>();
    private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

    /**
     * Cast an IBinder object into an YourInterfaceName interface.
     * Typical implementation:
     * YourInterfaceName.Stub.asInterface((IBinder)service) .
     *
     * @param <T> - YourInterfaceName
     */
    public interface BinderResolver<T extends IInterface> {
        T asInterface(IBinder service);
    }

    public ServiceInterfaceHolder(BinderResolver<T> binderResolver) {
        if (binderResolver == null) throw new AssertionError("BinderResolver is required");
        this.binderResolver = binderResolver;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Binded successfully to remote connector service: " + name.toShortString());

            IStackError stackError = extractErrorFromService(service);
            if(stackError == null) {
                serviceInterface.set(binderResolver.asInterface(service));
            } else {
                stackErrorHolder.set(stackError);
            }


            binded.set(true);
            connectionResponseReceived();
        }

        private IStackError extractErrorFromService(IBinder service) {
            IStackError stackError = null;
            try {
                String interfaceDescriptor = service.getInterfaceDescriptor();
                if(IStackError.class.getName().equals(interfaceDescriptor) ) {
                    stackError = IStackError.Stub.asInterface(service);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return stackError;
        }

        
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "ServiceDisconnected: " + name.toShortString());
            //Log.i(TAG, "Unbinded from remote connector service: " + name.toShortString());
            //serviceInterface.set(null);
            //binded.set(false);
            //connectionResponseReceived();
            stackErrorHolder.set(null);
        }

        private void connectionResponseReceived() {
            try {
                cyclicBarrier.await();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Connect to application service and establish connection.
     *
     * @param action   - Action that determinate service. This parameter used as action in intent filter.
     * @param context  - Object that encapsulates application environment. It need to bind to remote service.
     * @param timeouts - A flag to indicate that the caller wants timeout exceptions
     * @return service interface
     * @throws ServiceConnectionException - if connection is not establish successfully.
     */
    public T bindToService(String action, Context context, boolean timeouts)
            throws ServiceConnectionException, ImsException {
        synchronized (binded) {
            if (!binded.get()) {
                Log.i(TAG, "Start binding to remote connector service, action = " + action);

                cyclicBarrier.reset();

                Log.i(TAG, "Binding context: " + context.hashCode() + "," + context.toString() +
                        "; mConnection: " + mConnection.hashCode() + "," + mConnection.toString() + ";" + "Process.myPid():" + Process.myPid());
                boolean serviceBinded = context.bindService(new Intent(action),
                        mConnection, Context.BIND_AUTO_CREATE);

                if (!serviceBinded) {
                    throw new ServiceConnectionException("context.bindService == false. May be service has not been installed on device properly.");
                }
                waitConnectionResponse(timeouts);
            } else {
                Log.e(TAG, "Already binded to connection state service");
            }
        }



        T t = serviceInterface.get();
        if(t == null) {

            if(stackErrorHolder.get() != null) {
                final IStackError stackError = stackErrorHolder.get();
                final IError error = extractError(stackError);

                context.unbindService(mConnection);

                throw createImsException(error);
            } else {
                throw new ServiceConnectionException("Can't connect to service with action = " + action);
            }
        }

        return t;
    }

    private ImsException createImsException(IError iError) {
        return new ImsException(iError.getErrorCode(), iError.getMessage(), iError.getReasonData());
    }


    private IError extractError(IStackError stackError) {
        IError error = null;
        try {
            error = stackError.getError();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return error;
    }


    /**
     * Disconnect from service
     *
     * @param context  - Object that encapsulates application environment. It need to unbind from remote service.
     * @param timeouts - A flag to indicate that the caller wants timeout exceptions
     * @throws ServiceConnectionException - if connection is not establish successfully.
     */
    public void unbindFromService(Context context, boolean timeouts)
            throws ServiceConnectionException {
        if (binded.get()) {
            Log.i(TAG, "Start unbinding from connection state service");
            context.unbindService(mConnection);
        } else {
            Log.e(TAG, "Already unbinded from connection state service");
        }
    }

    private void waitConnectionResponse(boolean timeouts) throws ServiceConnectionException {
        try {
            if (timeouts) {
                cyclicBarrier.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            } else {
                cyclicBarrier.await();
            }
            Log.i(TAG, "remote connection state service is " + (serviceInterface == null ? "null" : "not null"));
        } catch (BrokenBarrierException e) {
            throw new ServiceConnectionException(String.format("BrokenBarrierException: %1$s", e.getMessage()), e);
        } catch (InterruptedException e) {
            throw new ServiceConnectionException(String.format("InterruptedException: %1$s", e.getMessage()), e);
        } catch (TimeoutException e) {
            throw new ServiceConnectionException(String.format("TimeoutException: %1$s", e.getMessage()), e);
        } finally {
            Log.i(TAG, "finally cyclicBarrier");
        }
    }
}
