package javax.microedition.ims.android.presence;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.presence.IEvent;
import javax.microedition.ims.android.presence.IPresentity;

interface IWatcherListener {
    void subscriptionStarted(); 
    void subscriptionFailed(in IReasonInfo reasonInfo);
    void subscriptionTerminated(in IEvent event);
    void presenceInfoReceived(in IPresentity[] presentities); 
}   