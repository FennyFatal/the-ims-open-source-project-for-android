package javax.microedition.ims.android.presence;

import javax.microedition.ims.android.presence.IWatcherInfoSubscriberListener; 

interface IWatcherInfoSubscriber {
    void subscribe(); 
    void unsubscribe();
    int getState();
    void addListener(IWatcherInfoSubscriberListener listener);
    void removeListener(IWatcherInfoSubscriberListener listener);
}