package javax.microedition.ims.engine.test.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: ext-plabada
 * Date: 04-Mar-2010
 * Time: 13:37:07
 * To change this template use File | Settings | File Templates.
 */
public class BreakPoint {

    private final Object mutex = new Object();
    private final AtomicBoolean waitCondition = new AtomicBoolean(true);

    public void notifySynchPoint() {
        waitCondition.set(false);

        synchronized (mutex) {
            mutex.notifyAll();
        }
    }

    /**
     * @param time
     * @param timeUnit
     * @return true if returns by timeout
     */
    public boolean waitNotificationOrTimeout(final long time, final TimeUnit timeUnit) {

        final long waitStarted = System.currentTimeMillis();
        final long needWaitMillis = timeUnit.toMillis(time);

        while (waitCondition.get()) {
            synchronized (mutex) {
                try {
                    long currentWaitPeriod = (waitStarted + needWaitMillis) - System.currentTimeMillis();
                    if (currentWaitPeriod > 0) {
                        mutex.wait(currentWaitPeriod);
                    } else {
                        break;
                    }
                }
                catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }

        return waitCondition.get();
    }
}
