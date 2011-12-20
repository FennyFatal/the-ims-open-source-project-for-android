package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.ICapabilitiesListener;
import javax.microedition.ims.android.core.IServiceMethod;

interface ICapabilities {
    void queryCapabilities(boolean sdpInRequest);
    String[] getRemoteUserIdentities();
    int getState();
    boolean hasCapabilities(String connection);
    void addListener(in ICapabilitiesListener listener);
    void removeListener(in ICapabilitiesListener listener);
    IServiceMethod getServiceMethod();
}
