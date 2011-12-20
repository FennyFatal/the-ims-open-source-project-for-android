package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.ISubscriptionListener;
import javax.microedition.ims.android.core.IServiceMethod;

interface ISubscription
{

    void subscribe();
    
    void poll();
    
    void unsubscribe();
    
    String getEvent();
    
    int getState();
    
        
    void addListener(ISubscriptionListener listener);
    void removeListener(ISubscriptionListener listener);
    
    IServiceMethod getServiceMethod();

}
