package javax.microedition.ims.android.presence;

import javax.microedition.ims.android.presence.IWatcherListener; 

interface IWatcher {
    int getState();
    String getTargetURI();
    void poll();  
    
    void subscribe(); 
          
    void unsubscribe();
         
    void addListener(IWatcherListener listener);
    void removeListener(IWatcherListener listener);
    
}
