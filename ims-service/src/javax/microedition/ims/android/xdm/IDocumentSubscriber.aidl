package javax.microedition.ims.android.xdm;

import javax.microedition.ims.android.xdm.IDocumentSubscriberListener;

interface IDocumentSubscriber {
    String[] getURLs();
    void addListener(IDocumentSubscriberListener listener);
    void removeListener(IDocumentSubscriberListener listener);
    void subscribe();
    void unsubscribe();
}