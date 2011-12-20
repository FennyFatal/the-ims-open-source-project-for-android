package javax.microedition.ims.android.presence;

import javax.microedition.ims.android.presence.IPresenceSourceListener; 

interface IPresenceSource {
    String getUserIdentity();
    int getState();
    void publish(String source);
    void unpublish();
    void addListener(IPresenceSourceListener listener);
    void removeListener(IPresenceSourceListener listener);
    
}
