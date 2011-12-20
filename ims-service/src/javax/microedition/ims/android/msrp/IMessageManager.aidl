package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.android.msrp.IMessageManagerListener;

interface IMessageManager
{

    void sendMessage(in IMessage retValue, boolean deliveryReport);
    
    void sendLargeMessage(in IMessage retValue, boolean deliveryReport);
    
    void cancel(String messageId);
    
    void addListener(IMessageManagerListener listener);
    void removeListener(IMessageManagerListener listener);

}
