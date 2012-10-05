
package com.android.ims.common;

/**
 * 
 * @author Khomushko
 *
 * @param <T>
 */
public interface ListenerSupport<T> {

    void addListener(T listener);

    void removeListener(T listener);

}
