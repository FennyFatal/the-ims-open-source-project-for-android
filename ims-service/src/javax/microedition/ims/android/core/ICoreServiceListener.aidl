package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.ICoreService;
import javax.microedition.ims.android.core.ISession;
import javax.microedition.ims.android.core.IReference;
import javax.microedition.ims.android.core.IPageMessage;

interface ICoreServiceListener 
{
	void pageMessageReceived(ICoreService service, IPageMessage retValue,
	           in byte[] content, String contentType);
    void sessionInvitationReceived(ICoreService service, ISession session);
    void referenceReceived(ICoreService service, IReference reference);
}
