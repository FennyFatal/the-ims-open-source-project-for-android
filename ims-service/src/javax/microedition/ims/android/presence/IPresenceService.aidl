package javax.microedition.ims.android.presence;

import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.presence.IPresenceServiceListener;
import javax.microedition.ims.android.presence.IPresenceSource;
import javax.microedition.ims.android.presence.IWatcher;
import javax.microedition.ims.android.presence.IWatcherInfoSubscriber;

interface IPresenceService {
    String getAppId();
    String getUserId();
    String getSheme();
    void close();
    
    void addPresenceServiceListener(in IPresenceServiceListener listener);
    void removePresenceServiceListener(in IPresenceServiceListener listener);
    
    IPresenceSource createPresenceSource();
    IWatcher createWatcher(in String targetURI, out IExceptionHolder exceptionHolder);
    IWatcherInfoSubscriber createWatcherInfoSubscriber(); 
}
