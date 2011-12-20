package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.IMessage;

interface IServiceMethod
{
    // ServiceMethod interface unctions
    IMessage getNextRequest();
    IMessage getNextResponse();
    IMessage getPreviousRequest(int method);
    
    // AIDL doesn't seem to allow arraw of AIDL types. We use list instead...
    List<IBinder> getPreviousResponses(int method);
    String[] getRemoteUserId(); 
}
