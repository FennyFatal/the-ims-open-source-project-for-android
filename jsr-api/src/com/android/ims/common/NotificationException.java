
package com.android.ims.common;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 
 * @author Andrei Khomushko
 *
 */
public class NotificationException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Object firstListenerWithException;

    private final Method method;

    private final Object[] args;

    private final Throwable throwable;

    public NotificationException(final Object firstListenerWithException, final Method method,
            final Object[] args, final Throwable throwable) {

        super("Failed to call " + method.getDeclaringClass().getName() + "." + method.getName()
                + " " + Arrays.asList(args) + " for " + firstListenerWithException + " due to "
                + throwable);
        this.firstListenerWithException = firstListenerWithException;
        this.method = method;
        this.args = args;
        this.throwable = throwable;
    }

    public Object getFirstListenerWithException() {
        return firstListenerWithException;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
