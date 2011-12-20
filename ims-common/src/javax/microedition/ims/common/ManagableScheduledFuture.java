package javax.microedition.ims.common;

import java.util.concurrent.ScheduledFuture;

/**
 * 
 * @author Khomushko
 *
 * @param <T>
 */
public interface ManagableScheduledFuture<T> extends ScheduledFuture<T>, Shutdownable{
}
