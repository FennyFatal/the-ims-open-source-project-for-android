package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.android.msrp.IIMSessionListener;
import javax.microedition.ims.android.msrp.IFileInfo;

interface IIMSession
{

    void sendMessage(in IMessage retValue, boolean deliveryReport);

    String getSessionId();
    
    void sendComposingIndicator(in IMessage retValue);
    
    void close();
    
    void cancelFileTransfer(String identifier);
    
    String sendFiles(in IFileInfo[] files, boolean deliveryReport);
    

    void addListener(IIMSessionListener listener);
    void removeListener(IIMSessionListener listener);

}
