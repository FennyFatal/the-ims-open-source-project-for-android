package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.IServiceMethod;
import javax.microedition.ims.android.core.ISession;
import javax.microedition.ims.android.core.IReferenceListener;

interface IReference {
    int getState();
    IServiceMethod getServiceMethod();
    void accept();
    void reject();
    void refer();
    void connectReferSession(ISession session);
    void setListener(IReferenceListener listener);
    String getReferMethod();
    String getReferToUserId();
}
