package javax.microedition.ims.android.core;

import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.core.ICoreServiceListener;
import javax.microedition.ims.android.core.ISession;
import javax.microedition.ims.android.core.IReference;
import javax.microedition.ims.android.core.ISubscription;
import javax.microedition.ims.android.core.IPublication;
import javax.microedition.ims.android.core.IPageMessage;
import javax.microedition.ims.android.core.ICapabilities;

interface ICoreService
{
        String getLocalUserId();
        void addListener(ICoreServiceListener listener);
        void removeListener(ICoreServiceListener listener);
        
        // Methods for Connection and Service interfaces
        String getAppId();
        String getSheme();
        void close();
        boolean isForceSrtp();
        
        
        IReference createReference(String fromUserId, String toUserId, String referToUserId, String method,
                out IExceptionHolder exceptionHolder);
            
        ISubscription createSubscription(String from, String to, String event, 
                out IExceptionHolder exceptionHolder);
        
        IPublication createPublication(String from, String to, String event, 
                out IExceptionHolder exceptionHolder);
        
            
        ISession createSession(in String from, in String to, out IExceptionHolder excecptionHolder);
            
        IPageMessage createPageMessage(String from, String to, out IExceptionHolder excecptionHolder);
        
        ICapabilities createCapabilities(String from, String to, out IExceptionHolder excecptionHolder);
        
        String getDtmfPayload();
}
