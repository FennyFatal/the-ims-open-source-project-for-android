package javax.microedition.ims.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.ims.core.IMSStackException;

/**
 *
 * @author Khomushko
 *
 * @param <T>
 */
public class Memoizer<T> {
    private final Computable<T> computable;
    private final AtomicReference<Future<T>> cache = new AtomicReference<Future<T>>();

    public Memoizer(Computable<T> computable) {
        this.computable = computable;
    }

    public T computeOrExeption() throws Throwable{
        T computeValue = null;
        try {
            computeValue = compute();
        } catch (IllegalStateException e) {
            throw e.getCause();
        }
        return computeValue;
    }

    private T compute() {
        while (true) {
            Future<T> future = cache.get();
            if (future == null) {
                Callable<T> callable = new Callable<T>() {
                    public T call() throws Exception {
                        return computable.compute();
                    }
                };

                FutureTask<T> futureTask = new FutureTask<T>(callable);
                if (cache.compareAndSet(null, futureTask)) {
                    future = futureTask;
                    futureTask.run();
                } else {
                    future = cache.get();
                }
            }

            try {
                return future.get();
            } catch (InterruptedException e) {
                cache.set(null);
            } catch (ExecutionException e) {
                throw launderThrowable(e.getCause());
            }
        }
    }

    private static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new IllegalStateException("Nit runtime exception", t);
        }

    }

    public interface Computable<V> {
        V compute() throws Exception;
    }

    public static void main(String[] args) {
        String computedValue = null;


        final Memoizer<String> memoizer = new Memoizer<String>(new Computable<String>() {
            public String compute() throws Exception {
                return "aaa";
            }
        });

        try {
            computedValue = memoizer.computeOrExeption();
        } catch (IMSStackException e) {
            System.out.println("IMSStackException");
        } catch (Throwable e) {
            if(e.getCause() instanceof IMSStackException) {
                System.out.println("IMSStackException");
            }
        }

        try {
            computedValue = memoizer.computeOrExeption();
        } catch (IMSStackException e) {
            System.out.println("IMSStackException");
        } catch (Throwable e) {
            if(e.getCause() instanceof IMSStackException) {
                System.out.println("IMSStackException");
            }
        }

        System.out.println("computedValue = " + computedValue);
    }
}
