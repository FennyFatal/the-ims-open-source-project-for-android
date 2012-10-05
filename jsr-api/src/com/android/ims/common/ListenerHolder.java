package com.android.ims.common;

/**
 * 
 * @author Andrei Khomushko
 *
 * @param <T>
 */
public interface ListenerHolder<T> extends ListenerSupport<T>, Shutdownable{
    T getNotifier();
}
