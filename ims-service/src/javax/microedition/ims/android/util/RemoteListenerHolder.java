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

package javax.microedition.ims.android.util;


import android.os.IInterface;
import android.os.RemoteCallbackList;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Shutdownable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author ext-akhomush
 * @param <T>
 */
public class RemoteListenerHolder<T extends IInterface> implements ListenerHolder<T>, Shutdownable {
    private static final String TAG = "RemoteListenerHolder";

    private final RemoteCallbackList<T> listeners = new RemoteCallbackList<T>();
    private final Class<?> clazz;
    private final T notifier;

    private class ProxyInvocationHandler<P> implements InvocationHandler {
        
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

            method.setAccessible(true);
            Exception firstMetException = null;

            final int N = listeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                T listener = listeners.getBroadcastItem(i);
                try {
                    method.invoke(listener, args);
                }
                catch (Exception localException) {
                    Logger.log(TAG, localException.getMessage());
                    localException.printStackTrace();
                    if (firstMetException == null) {
                        firstMetException = localException;
                    }
                }

            }
            listeners.finishBroadcast();

            if (firstMetException != null) {
                firstMetException.printStackTrace();
                throw new RemoteNotificationException(firstMetException.getMessage());
            }

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public RemoteListenerHolder(final Class<? extends T> clazz) {
        this.clazz = clazz;

        final Object listenerProxy = Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new ProxyInvocationHandler<T>()
        );

        this.notifier = (T) listenerProxy;
    }

    
    public void addListener(T listener) {
        if (listener != null) {
            listeners.register(listener);
        }
    }

    
    public void removeListener(T listener) {
        if (listener != null) {
            listeners.unregister(listener);
        }
    }

    private void removeAllListeners() {
        listeners.kill();
    }

    
    public T getNotifier() {
        return notifier;
    }

    
    public void shutdown() {
        removeAllListeners();
    }


    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("ListenerHolder");
        sb.append("{listenerList=").append(listeners);
        sb.append(", clazz=").append(clazz);
        sb.append('}');
        return sb.toString();
    }
}
