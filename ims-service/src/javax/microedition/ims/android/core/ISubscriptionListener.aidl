package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.IMessage;
import javax.microedition.ims.android.core.ISubscription;

interface ISubscriptionListener
{

    void subscriptionStarted();
    
    void subscriptionStartFailed();
    
    void subscriptionTerminated();
    
    void subscriptionNotify(IMessage notify);

}
