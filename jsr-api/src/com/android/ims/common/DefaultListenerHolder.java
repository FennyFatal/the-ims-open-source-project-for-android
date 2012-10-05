
package com.android.ims.common;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Khomushko
 *
 * @param <T>
 */
public class DefaultListenerHolder<T> implements ListenerHolder<T>{

    private static final String TAG = "ListenerHolder";

    private final List<T> listeners = Collections.synchronizedList(new ArrayList<T>()) ;
    
    private final Class<?> clazz;

    private final T notifier;

    private class ProxyInvocationHandler<P> implements InvocationHandler {

        public Object invoke(final Object proxy, final Method method, final Object[] args)
                throws Throwable {
  
            method.setAccessible(true);
            Exception firstMetException = null;
            T firstListenerWithException = null;

            final List<T> listenersCopy;
            
            synchronized (listeners) {
                listenersCopy = new ArrayList<T>(listeners);
            }
            
            for (T listener : listenersCopy) {
                try {
                    method.invoke(listener, args);
                } catch (Exception localException) {
                    Log.e(TAG, localException.getMessage(), localException);
                    if (firstMetException == null) {
                        firstMetException = localException;
                        firstListenerWithException = listener;
                    }
                }
            }

            if (firstMetException != null) {
                firstMetException.printStackTrace();
                throw new NotificationException(firstListenerWithException, method, args,
                        firstMetException.getCause() == null ? firstMetException
                                : firstMetException.getCause());

            }

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public DefaultListenerHolder(final Class<? extends T> clazz) {
        this.clazz = clazz;

        final Object listenerProxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {
            clazz
        }, new ProxyInvocationHandler<T>());

        this.notifier = (T)listenerProxy;
    }

    @Override
    public void addListener(T listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(T listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private void removeAllListeners() {
        listeners.clear();
    }

    @Override
    public T getNotifier() {
        return notifier;
    }

    @Override
    public void shutdown() {
        removeAllListeners();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("ListenerHolder");
        sb.append("{listenerList=").append(listeners);
        sb.append(", clazz=").append(clazz);
        sb.append('}');
        return sb.toString();
    }
}
