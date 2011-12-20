package javax.microedition.ims.android.core;

import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.core.IPublicationListener;
import javax.microedition.ims.android.core.IServiceMethod;

interface IPublication
{

    void publish(in byte[] state, String contentType,
            out IExceptionHolder exceptionHolder);
    
    void unpublish();
    
    String getEvent();
    
    int getState();
    
        
    void addListener(IPublicationListener listener);
    void removeListener(IPublicationListener listener);
    
    IServiceMethod getServiceMethod();

}
