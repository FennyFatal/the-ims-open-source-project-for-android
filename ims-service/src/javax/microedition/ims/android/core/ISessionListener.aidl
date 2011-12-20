package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.ISession;
import javax.microedition.ims.android.core.IReference;

interface ISessionListener
{
    void sessionAlerting(ISession session);
    void sessionStarted(ISession session);
    void sessionStartFailed(ISession session);
    void sessionTerminated(ISession session);
    void sessionUpdated(ISession session);
    void sessionUpdateFailed(ISession session);  
    void sessionUpdateReceived(ISession session);
    void sessionReferenceReceived(ISession session, IReference reference);  
}
