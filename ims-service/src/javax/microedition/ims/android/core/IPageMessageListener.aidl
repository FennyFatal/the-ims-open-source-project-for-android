package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.IPageMessage;

interface IPageMessageListener {
	void pageMessageDelivered(IPageMessage pageMessage);
	void pageMessageDeliveryFailed(IPageMessage pageMessage);
}