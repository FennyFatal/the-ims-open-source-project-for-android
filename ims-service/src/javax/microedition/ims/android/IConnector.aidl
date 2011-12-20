package javax.microedition.ims.android;

import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.core.ICoreService;
import javax.microedition.ims.android.xdm.IXDMService;
import javax.microedition.ims.android.msrp.IIMService;
import javax.microedition.ims.android.presence.IPresenceService;

interface IConnector
{
        // Other "open"-method forms are provided by client interface.
        // Those forms contained parameters that were mostly ignored
        ICoreService openCoreService(String name, out IExceptionHolder exceptionHolder);
        IXDMService openXDMService(String name);
        IIMService openIMService(String name);
        IPresenceService openPresenceService(String name);
    
}
