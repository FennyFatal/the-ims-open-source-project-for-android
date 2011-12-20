package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.IPageMessageListener;
import javax.microedition.ims.android.core.IServiceMethod;

interface IPageMessage {
    void send(in byte[] content, String contentType);
    void addListener(in IPageMessageListener listener);
    void removeListener(in IPageMessageListener listener);
    IServiceMethod getServiceMethod();
}
