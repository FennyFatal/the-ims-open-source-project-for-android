package javax.microedition.ims.android.presence;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.presence.IEvent;

interface IWatcherInfoSubscriberListener {
    void subscriptionStarted(); 
    void subscriptionFailed(in IReasonInfo reasonInfo);
    void subscriptionTerminated(in IEvent event);
    void watcherInfoReceived(in String watcherInfo); 
}   