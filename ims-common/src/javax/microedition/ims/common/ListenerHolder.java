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

package javax.microedition.ims.common;


import javax.microedition.ims.common.TimeObserver.TimeObserverCallback;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 09-Dec-2009
 * Time: 16:13:39
 */
public class ListenerHolder<T> implements ListenerSupportWithId<T>, Shutdownable {
    private static final String LOG_TAG = "ListenerHolder";
    private static final long METHOD_EXECUTION_TIME_LIMIT = 5000l;

    private static class EntityDescriptor {
        private String methodName;
        private Object listener;
        private Thread curThread;

        private EntityDescriptor(String methodName, Object listener, Thread curThread) {
            this.methodName = methodName;
            this.listener = listener;
            this.curThread = curThread;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((listener == null) ? 0 : listener.hashCode());
            result = prime * result
                    + ((methodName == null) ? 0 : methodName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            EntityDescriptor other = (EntityDescriptor) obj;
            if (listener == null) {
                if (other.listener != null) {
                    return false;
                }
            } else if (!listener.equals(other.listener)) {
                return false;
            }
            if (methodName == null) {
                if (other.methodName != null) {
                    return false;
                }
            } else if (!methodName.equals(other.methodName)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "EntityDescriptor [methodName=" + methodName + ", listener="
                    + listener + "]";
        }
    }

    private static final TimeObserverCallback<EntityDescriptor> timeObserverCallback = new TimeObserverCallback<EntityDescriptor>() {
        public void timeExeeceded(EntityDescriptor entity) {
            String entityDescription = String.format(
                    "%s.%s, instance = %s", entity.listener.getClass().getSimpleName(),
                    entity.methodName, entity.listener.toString()
            );
            Logger.log(Logger.Tag.WARNING,
                    String.format(
                            "Time limit for method execution has been exceed. Entity = [%s]",
                            entityDescription
                    )
            );

            for (StackTraceElement traceElement : entity.curThread.getStackTrace()) {
                Logger.log(Logger.Tag.WARNING, traceElement.toString());
            }
        }
    };

    private static final TimeObserver<EntityDescriptor> timeObserver = new TimeObserver<EntityDescriptor>(METHOD_EXECUTION_TIME_LIMIT, timeObserverCallback);

    private final Map<T, Collection<?>> listenerMap = Collections.synchronizedMap(new LinkedHashMap<T, Collection<?>>());
    private final Class<?> clazz;
    private final T notifier;

    private class ProxyInvocationHandler implements InvocationHandler {
        @SuppressWarnings("unchecked")
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

            T[] arrListenerCopy;
            Collection<?>[] arrListenerTypeCopy;
            synchronized (listenerMap) {
                final Set<T> keys = listenerMap.keySet();
                //this supressed because we know for shure exact type of array from previous step
                //noinspection unchecked,SuspiciousToArrayCall
                arrListenerCopy = (T[]) keys.toArray(new Object[keys.size()]);

                final Collection<Collection<?>> values = listenerMap.values();
                arrListenerTypeCopy = values.toArray(new Collection<?>[values.size()]);
            }

            method.setAccessible(true);
            Exception firstMetException = null;
            T firstListenerWithException = null;

            Set<Object> supportedListenerTypes = getSupportedListenerTypes(args);

            for (int i = 0, arrLength = arrListenerCopy.length; i < arrLength; i++) {
                T currListener = arrListenerCopy[i];
                Collection<?> currListenerTypes = arrListenerTypeCopy[i];


                final EntityDescriptor entity = TimeObserver.needProfiling ? new EntityDescriptor(method.getName(), currListener, Thread.currentThread()) : null;
                try {
                    if (currListenerTypes == null || currListenerTypes.size() == 0 ||
                            isListenerTypesSupported(supportedListenerTypes, currListenerTypes)) {

                        if (TimeObserver.needProfiling) {
                            timeObserver.addEntity(entity);
                        }

                        try {
                            method.invoke(currListener, args);
                        } finally {
                            if (TimeObserver.needProfiling) {
                                timeObserver.removeEntity(entity);
                            }
                        }
                    }
                } catch (Exception localException) {

                    Logger.log(Logger.Tag.WARNING, "Listener holder failed on invoking listener");
                    Logger.log(Logger.Tag.WARNING, "Method: " + method.getDeclaringClass().getName() + "." + method.getName());
                    Logger.log(Logger.Tag.WARNING, "Instance: " + currListener);
                    Logger.log(Logger.Tag.WARNING, "Arguments: " + (args == null ? "[]" : Arrays.asList(args)));
                    Logger.log(Logger.Tag.WARNING, "Failure description: " + localException.getCause());

                    if (localException.getCause() != null) {
                        localException.getCause().printStackTrace();
                    } else {
                        localException.printStackTrace();
                    }

                    if (firstMetException == null) {
                        firstMetException = localException;
                        firstListenerWithException = currListener;
                    }
                }
            }

            if (firstMetException != null) {
                firstMetException.printStackTrace();
                throw new NotificationException(
                        firstListenerWithException,
                        method,
                        args,
                        firstMetException.getCause() == null ?
                                firstMetException :
                                firstMetException.getCause()
                );
            }

            return null;
        }

        private boolean isListenerTypesSupported(Set<Object> supportedListenerTypes,
                Collection<?> currListenerTypes) {
            Logger.log(LOG_TAG, String.format("isListenerTypesSupported#supportedListenerTypes = %s, currListenerTypes = %s", supportedListenerTypes, currListenerTypes));
            return supportedListenerTypes.containsAll(currListenerTypes);
        }

        private Set<Object> getSupportedListenerTypes(Object[] args) {

            Set<Object> retValue = new HashSet<Object>();

            if (args != null && args.length > 0) {
                Object firstArgument = args[0];
                if (firstArgument instanceof ListenerSupportTypeHolder) {
                    ListenerSupportTypeHolder<?> listenerTypeHolder = (ListenerSupportTypeHolder<?>) firstArgument;
                    retValue.add(listenerTypeHolder.getType());
                }
                if (firstArgument instanceof ListenerSupportTypesHolder) {
                    ListenerSupportTypesHolder listenerTypesHolder = (ListenerSupportTypesHolder) firstArgument;
                    retValue.addAll(listenerTypesHolder.getTypes());
                }
            }
            return retValue;
        }
    }

    @SuppressWarnings("unchecked")
    public ListenerHolder(final Class<? extends T> clazz) {

        this.clazz = clazz;


        final Object listenerProxy = Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new ProxyInvocationHandler()
        );

        //here we know for shure that proxy will respond to T because of implemeted interface is Class<? extends T>
        //noinspection unchecked
        this.notifier = (T) listenerProxy;
    }

    public void addListener(T listener) {
        assert listener != null;
        assert !listenerMap.containsKey(listener);
        listenerMap.put(listener, null);
    }

    public void addListener(T listener, Object... listenerTypes) {
        assert
                !listenerMap.containsKey(listener) :
                "Listener for " + Arrays.toString(listenerTypes) + " is already added: " + listener;

        listenerMap.put(listener, Arrays.asList(listenerTypes));
    }

    public void removeListener(T listener) {
        listenerMap.remove(listener);
    }

    private void removeAllListeners() {
        synchronized (listenerMap) {
            listenerMap.clear();
        }
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
        sb.append("{listenerList=").append(listenerMap);
        sb.append(", clazz=").append(clazz);
        sb.append('}');
        return sb.toString();
    }

}
