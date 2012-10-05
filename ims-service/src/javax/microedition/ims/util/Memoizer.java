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
