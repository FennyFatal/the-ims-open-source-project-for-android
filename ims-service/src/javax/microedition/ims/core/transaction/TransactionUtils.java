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

package javax.microedition.ims.core.transaction;

import javax.microedition.ims.StackOuterErrorDefaultImpl;
import javax.microedition.ims.common.ErrorHandler;
import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.NamedDaemonThreadFactory;
import javax.microedition.ims.core.StackOuterError;
import javax.microedition.ims.core.StackOuterErrorType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 29-Jan-2010
 * Time: 11:29:06
 */
public final class TransactionUtils {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(
            10,
            new NamedDaemonThreadFactory("TransactionUtils pool")
    );

    static {
        ((ThreadPoolExecutor) executorService).setRejectedExecutionHandler(
                new RejectedExecutionHandler() {
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        assert false : "Thread pool is exhausted";
                    }
                }
        );
    }

    private static final ExecutorService transactionExecutor = Executors.newSingleThreadExecutor(
            new NamedDaemonThreadFactory("Transaction Thread")
    );
    private static final Thread transactionExecutorThread;
    private static final AtomicReference<Future<?>> currentTask = new AtomicReference<Future<?>>(null);

    private static final Queue<ElementContainer<Callable<?>>> executionQueue =
            new PriorityBlockingQueue<ElementContainer<Callable<?>>>(100);

    private static final Map<Callable<?>, FutureTask<?>> executionMap =
            Collections.synchronizedMap(new LinkedHashMap<Callable<?>, FutureTask<?>>(100));

    // private static final AtomicReference<ErrorHandler> commonExceptionHandler = new AtomicReference<ErrorHandler>(null);
    private static final ListenerHolder<ErrorHandler> errorHandlerSupport = new ListenerHolder<ErrorHandler>(ErrorHandler.class);

    private static class DefaultErrorHandler implements ErrorHandler<Throwable> {
        public void handleError(final Throwable e) {


            StackOuterError stackOuterError = new StackOuterErrorDefaultImpl(
                    StackOuterErrorType.EXCEPTION,
                    e.getMessage(),
                    null,
                    e
            );

            errorHandlerSupport.getNotifier().handleError(stackOuterError);
        }
    }

    static {
        Future<Thread> threadFuture = transactionExecutor.submit(
                new Callable<Thread>() {
                    public Thread call() throws Exception {
                        return Thread.currentThread();
                    }
                }
        );
        try {
            transactionExecutorThread = threadFuture.get();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can not instantiate transaction manager. " + e);
        }
    }

    private static class CallableWrapper implements Callable<Object> {
        private final Callable<Object> hostCallable;
        private final ErrorHandler<Throwable> exceptionHandler;
        private final Runnable hostRunnable;
        private final Object result;

        private final AtomicBoolean started = new AtomicBoolean(false);

        public CallableWrapper(final Callable<Object> hostCallable, final ErrorHandler<Throwable> exceptionHandler) {
            this.hostCallable = hostCallable;
            this.exceptionHandler = exceptionHandler;
            this.hostRunnable = null;
            this.result = null;
        }

        public CallableWrapper(final Runnable hostRunnable, final Object result, ErrorHandler exceptionHandler) {
            this.hostRunnable = hostRunnable;
            this.result = result;
            this.exceptionHandler = exceptionHandler;

            this.hostCallable = null;
        }

        public Object call() throws Exception {

            started.compareAndSet(false, true);

            log("RUN " + this);

            assert hostCallable == null || hostRunnable == null;

            Object retValue = null;

            try {
                Thread.yield();

                if (hostCallable != null) {
                    retValue = hostCallable.call();
                }
                else {
                    retValue = result;
                    hostRunnable.run();
                }

            }
            catch (Exception e) {
                e.printStackTrace();
                Logger.log("TranscationUtils", (e.getMessage() != null ? e.getMessage() : "Message not availabe"));
                handleError(e);
                throw e;
            }
            catch (Error error) {
                handleError(error);
                throw error;
            }
            finally {
                synchronized (currentTask) {
                    if (executionMap.containsKey(this)) {
                        executionMap.remove(this);
                        final ElementContainer<Callable<?>> headElement = executionQueue.poll();

                        assert headElement.getElement() == this :
                                "Inconsistent state. Head of the task queue doesn't contain current task. Expected task: "
                                        + this + " actual task: " + headElement;
                    }


                    final FutureTask<?> nextTask;
                    if (!executionQueue.isEmpty()) {

                        //here we get topmost element, then remove it, change it Vip status and then add again to queue
                        final PrioritizedData<Callable<?>> prioritizedData = (PrioritizedData<Callable<?>>) executionQueue.peek();
                        executionQueue.remove(prioritizedData);
                        prioritizedData.markVip(PrioritizedData.VipStatus.VIP);
                        executionQueue.add(prioritizedData);
                        assert executionQueue.peek() == prioritizedData : "Queue is broken";

                        final Callable<?> callable = prioritizedData.getElement();
                        nextTask = executionMap.get(callable);
                    }
                    else {
                        nextTask = null;
                    }

                    currentTask.set(nextTask);
                    if (nextTask != null) {
                        transactionExecutor.execute(nextTask);
                    }

                    assert executionQueue.size() == executionMap.size() :
                            "Inconsistent behaviour. " +
                                    "executionQueue size = " + executionQueue.size() +
                                    " executionMap size = " + executionMap.size() + " but must be identical.";
                }
                log("END " + this);
            }
            return retValue;
        }

        private void handleError(final Throwable error) {
            if (exceptionHandler == null) {
                error.printStackTrace();
            }
            else {
                try {
                    exceptionHandler.handleError(error);
                }
                catch (Throwable innerError) {
                    innerError.printStackTrace();
                    error.printStackTrace();
                }
            }
        }


        public String toString() {
            Object host = hostCallable != null ? hostCallable : hostRunnable;
            final String wholeId = super.toString();
            final String[] idParts = wholeId.split("@");
            final String id = idParts == null || idParts.length == 0 ? wholeId : idParts[idParts.length - 1];
            //return separator + "<" + host.toString() + " started:" + started.get() + " id:" + id + ">";
            return "<id:" + id + " " + host.toString() + " started:" + started.get() + ">";
        }
    }


    private static Object invoke(
            final Callable<Object> callable,
            final boolean waitResult,
            final boolean forceQueueing,
            final Priority priority,
            final ErrorHandler<Throwable> exceptionHandler) throws Exception {

        Callable<Object> wrapper = new CallableWrapper(callable, exceptionHandler);

        return doInvoke(wrapper, waitResult, forceQueueing, priority);
    }

    private static Object invoke(
            final Runnable runnable,
            final Object result,
            final boolean waitResult,
            final boolean forceQueueing,
            final ErrorHandler exceptionHandler) throws Exception {


        final PriorityCall priorityAnnotation = runnable.getClass().getMethod("run").getAnnotation(PriorityCall.class);
        Priority priority = priorityAnnotation == null ? Priority.NORMAL : priorityAnnotation.priority();

        Callable<Object> wrapper = new CallableWrapper(runnable, result, exceptionHandler);
        return doInvoke(wrapper, waitResult, forceQueueing, priority);
    }

    private static Object doInvoke(
            final Callable<Object> wrapper,
            final boolean waitResult,
            final boolean forceQueueing,
            final Priority priority) throws Exception {

        Object retValue = null;

        if (forceQueueing || !isTransactionExecutionThread()) {
            final FutureTask<Object> result = new FutureTask<Object>(wrapper);
            final List<ElementContainer<Callable<?>>> executionQueueCopy;
            final PrioritizedData<Callable<?>> prioritizedData = new PrioritizedData<Callable<?>>(priority, wrapper);

            log("ADD : " + wrapper);
            //assert !isTransactionExecutionThread() : "may be you don't want queueing from transaction thread";
            synchronized (currentTask) {
                if (currentTask.compareAndSet(null, result)) {
                    transactionExecutor.execute(result);
                    prioritizedData.markVip(PrioritizedData.VipStatus.VIP);
                    assert executionQueue.size() == 0 : "queue is broken";
                    //result = transactionExecutor.submit(wrapper);
                }
                executionMap.put(wrapper, result);

                /*
                if (priority != Priority.HIGH || executionQueue.size() == 0) {
                    executionQueue.add(wrapper);
                } else {
                    executionQueue.add(1, wrapper);
                }
                */
                executionQueue.add(prioritizedData);


                //executionQueueCopy = new ArrayList<Callable<?>>(executionMap.keySet());
                executionQueueCopy = copyQueue(executionQueue);


                assert executionQueue.size() == executionMap.size() :
                        "Inconsistent behaviour. " +
                                "executionQueue size = " + executionQueue.size() +
                                " executionMap size = " + executionMap.size() + " but must be identical.";
            }

            log(buildQueueLogMsg(executionQueueCopy));

            if (waitResult) {
                assert !isTransactionExecutionThread() : "Deadlock call detected for " + wrapper;
                retValue = result.get();
            }
        }
        else {
            retValue = wrapper.call();
        }

        return retValue;
    }

    private static String buildQueueLogMsg(List<?> executionQueueCopy) {
        final StringBuilder logMsgBuilder = new StringBuilder();
        logMsgBuilder.append("\n");
        logMsgBuilder.append("QUEUE:[\n");
        for (Object elementContainer : executionQueueCopy) {
            logMsgBuilder.append(elementContainer).append("\n");
        }
        logMsgBuilder.append("QUEUE:]\n");
        return logMsgBuilder.toString();
    }

    private static <T> List<T> copyQueue(final Queue<T> queueToCopy) {
        List<T> executionQueueCopy;
        final T[] containers = (T[]) queueToCopy.toArray(new Object[queueToCopy.size()]);
        Arrays.sort(containers);
        executionQueueCopy = Arrays.asList(containers);
        return executionQueueCopy;
    }


    private static class TransactionProxyCallable implements Callable<Object> {
        private final Method method;
        private final Object hostObject;
        private final Object[] args;

        public TransactionProxyCallable(final Method method, final Object hostObject, final Object[] args) {
            this.method = method;
            this.hostObject = hostObject;
            this.args = args;
        }

        public Object call() throws Exception {
            return method.invoke(hostObject, args);
        }


        public String toString() {

            final String wholeName = hostObject.getClass().getName();
            String[] nameParts = wholeName.split("\\.");
            String shortName = nameParts == null || nameParts.length == 0 ? wholeName : nameParts[nameParts.length - 1];

            final StringBuffer sb = new StringBuffer();
            sb.append("ProxyCall:");
            sb.append(shortName).append(".");
            sb.append(method.getName());
            sb.append(args == null ? "[]" : Arrays.asList(args).toString()).append(" ").append(hostObject);
            return sb.toString();
        }
    }

    private static class TransactionInvocationHandler implements InvocationHandler {
        private final Object hostObject;
        private final Class<?> proxyInterface;
        private final Method priorityResolver;

        public TransactionInvocationHandler(final Object hostObject, final Class<?> proxyInterface) {
            this.hostObject = hostObject;
            this.proxyInterface = proxyInterface;
            this.priorityResolver = obtainPriorityResolver(hostObject);
        }

        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final Object retValue;

            final boolean proxyCallDetected = method.getDeclaringClass().equals(proxyInterface);
            final boolean directCallDetected = method.getAnnotation(DirectCall.class) != null;

            if (proxyCallDetected && !directCallDetected) {

                final boolean smartCallDetected = method.getAnnotation(SmartCall.class) != null;
                final Priority priority = obtainTaskPriority(method);

                final TransactionProxyCallable callable = new TransactionProxyCallable(method, hostObject, args);
                log(callable + " " + hostObject.toString());
                final boolean waitResult = method.getReturnType() != void.class;
                retValue = TransactionUtils.invoke(callable, waitResult, !smartCallDetected, priority, new ErrorHandler<Throwable>() {
                    public void handleError(final Throwable e) {
                        final String errMsg = "(EXCEPTION) " + callable + ": EXCEPTION detected" + e.toString();
                        log(errMsg);

                        StackOuterError stackOuterError = new StackOuterErrorDefaultImpl(
                                StackOuterErrorType.EXCEPTION,
                                errMsg,
                                null,
                                e
                        );
                        errorHandlerSupport.getNotifier().handleError(stackOuterError);
                    }
                });
            }
            else {
                retValue = method.invoke(hostObject, args);
            }

            return retValue;
        }

        private Priority obtainTaskPriority(Method method) throws IllegalAccessException, InvocationTargetException {

            //set default value
            Priority priority = Priority.NORMAL;
            final PriorityCall priorityCallAnnotation = method.getAnnotation(PriorityCall.class);
            boolean priorityOverrideAllowed = priorityCallAnnotation == null || priorityCallAnnotation.override() == PriorityCall.Override.YES;
            Priority priorityOverriden = null;

            //try to get priority using reflection @PriorityResolver method
            if (priorityResolver != null && priorityOverrideAllowed) {
                priorityOverriden = (Priority) priorityResolver.invoke(hostObject, method);
            }

            //if we were unable to get priority through special method we will try to find special annotation
            if (priorityOverriden != null) {
                priority = priorityOverriden;
            }
            else {
                if (priorityCallAnnotation != null) {
                    priority = priorityCallAnnotation.priority();
                }
            }


            return priority;
        }

        private Method obtainPriorityResolver(Object hostObject) {
            Class<?> hostClass = hostObject.getClass();
            Method priorityResolver = null;

            final Method[] methods = hostClass.getMethods();
            for (Method method : methods) {
                final PriorityResolver priorityResolverAnnotation = method.getAnnotation(PriorityResolver.class);
                if (priorityResolverAnnotation != null &&
                        method.getReturnType() == Priority.class &&
                        method.getParameterTypes().length == 1 &&
                        method.getParameterTypes()[0] == Method.class) {
                    priorityResolver = method;
                    break;
                }
            }


            return priorityResolver;
        }


        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append("TransactionInvocationHandler");
            sb.append("{hostObject=").append(hostObject);
            sb.append(", proxyInterface=").append(proxyInterface);
            sb.append('}');
            return sb.toString();
        }
    }


    private TransactionUtils() {
    }


    /*public static void registerExceptionHandler(ErrorHandler exceptionHandler) {
        commonExceptionHandler.compareAndSet(null, exceptionHandler);
    }*/

    //TODO: make only one handler for each StackContext

    public static void addExceptionHandler(final ErrorHandler<StackOuterError> exceptionHandler) {
        errorHandlerSupport.addListener(exceptionHandler);
    }

    public static void removeExceptionHandler(final ErrorHandler<StackOuterError> exceptionHandler) {
        errorHandlerSupport.removeListener(exceptionHandler);
    }


    public static boolean isTransactionExecutionThread() {
        return Thread.currentThread() == transactionExecutorThread;
    }

    //will be placed at the end of queue and return immediately

    public static void invokeLater(TransactionRunnable runnable) {
        try {
            Object result = null;
            final boolean waitResult = false;
            final boolean forceQueueing = true;

            runnable.setType("IL(R:" + result + " W:" + waitResult + " F:" + forceQueueing + ")");
            invoke(runnable, null, waitResult, forceQueueing, new DefaultErrorHandler());
        }
        catch (Exception e) {
            log("invocation error for: " + runnable);
            e.printStackTrace();
        }
    }

    public static void invokeLaterSmart(TransactionRunnable runnable) {
        try {
            Object result = null;
            final boolean waitResult = false;
            final boolean forceQueueing = false;

            runnable.setType("IL(R:" + result + " W:" + waitResult + " F:" + forceQueueing + ")");
            invoke(runnable, null, waitResult, forceQueueing, new DefaultErrorHandler());
        }
        catch (Exception e) {
            log("invocation error for: " + runnable);
            e.printStackTrace();
        }
    }

    //will be placed at the end of queue and return after runnable is finished

    public static void invokeLastAndWait(TransactionRunnable runnable) {
        try {
            Object result = null;
            final boolean waitResult = true;
            final boolean forceQueueing = true;

            runnable.setType("IW(R:" + result + " W:" + waitResult + " F:" + forceQueueing + ")");
            invoke(runnable, result, waitResult, forceQueueing, new DefaultErrorHandler());
        }
        catch (Exception e) {
            log("invocation error for: " + runnable);
            e.printStackTrace();
        }
    }

    public static void invokeAndWait(TransactionRunnable runnable) {
        try {
            Object result = null;
            final boolean waitResult = true;
            final boolean forceQueueing = false;

            runnable.setType("IW(R:" + result + " W:" + waitResult + " F:" + forceQueueing + ")");
            invoke(runnable, result, waitResult, forceQueueing, new DefaultErrorHandler());
        }
        catch (Exception e) {
            log("invocation error for: " + runnable);
            e.printStackTrace();
        }
    }


    public static <T, V> T wrap(
            final V hostObject,
            final Class<T> mainInterfaceToImplement,
            final Class<?>... interfacesToImplement) {

        T retValue;

        assert !(hostObject instanceof Proxy) : "Attempt to wrap already wrapped object.";

        if (mainInterfaceToImplement != null) {

            Object wrappedHost = Proxy.newProxyInstance(
                    mainInterfaceToImplement.getClassLoader(),
                    mergeClasses(mainInterfaceToImplement, interfacesToImplement),
                    new TransactionInvocationHandler(hostObject, mainInterfaceToImplement)

            );

            retValue = (T) wrappedHost;
        }
        else {
            retValue = (T) hostObject;
        }

        return retValue;
    }

    private static Class<?>[] mergeClasses(Class<?> mainInterfaceToImplement, Class<?>[] interfacesToImplement) {

        int arrSize = interfacesToImplement != null && interfacesToImplement.length > 0 ?
                interfacesToImplement.length + 1 : 1;

        Class<?>[] retValue = new Class<?>[arrSize];
        retValue[0] = mainInterfaceToImplement;

        if (interfacesToImplement != null && interfacesToImplement.length > 0) {
            System.arraycopy(interfacesToImplement, 0, retValue, 1, interfacesToImplement.length);
        }

        return retValue;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    private static void log(String msg) {
        Logger.log(Logger.Tag.TRANSACTION, "TRNS_UTILS", msg);
    }

    public static void reset() {
        synchronized (currentTask) {
            List<Callable<?>> keys = new ArrayList<Callable<?>>(executionMap.keySet());

            for (Callable<?> callable : keys) {
                Future<?> future = executionMap.remove(callable);
                future.cancel(true);
            }

            executionQueue.clear();
            currentTask.set(null);
        }
    }

    static interface A {
        @SmartCall
        void a();
    }

    static interface B {
        void a();
    }

    public static void main(final String[] args) {
        Collection<? extends ElementContainer<Integer>> tasks = Arrays.asList(
                new PrioritizedData<Integer>(Priority.NORMAL, 1),
                new PrioritizedData<Integer>(Priority.NORMAL, 2),
                new PrioritizedData<Integer>(Priority.LOW, 3),
                new PrioritizedData<Integer>(Priority.NORMAL, 4),
                new PrioritizedData<Integer>(Priority.NORMAL, 5),
                new PrioritizedData<Integer>(Priority.LOW, 6, PrioritizedData.VipStatus.VIP),
                new PrioritizedData<Integer>(Priority.NORMAL, 7),
                new PrioritizedData<Integer>(Priority.HIGH, 8),
                new PrioritizedData<Integer>(Priority.NORMAL, 9)
        );

        PriorityQueue<ElementContainer<Integer>> priorityQueue = new PriorityQueue<ElementContainer<Integer>>(tasks);
        priorityQueue.add(new PrioritizedData<Integer>(Priority.HIGH, 2));

        List<ElementContainer<Integer>> queueCopy = copyQueue(priorityQueue);
        log(buildQueueLogMsg(queueCopy));
    }
}
