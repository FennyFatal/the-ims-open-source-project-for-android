package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.msrp.IConferenceManager;
import javax.microedition.ims.android.msrp.IDeferredMessageManager;
import javax.microedition.ims.android.msrp.IFileTransferManager;
import javax.microedition.ims.android.msrp.IHistoryManager;
import javax.microedition.ims.android.msrp.IMessageManager;
import javax.microedition.ims.android.msrp.IIMServiceListener;

interface IIMService
{

    IConferenceManager getConferenceManager();

    IDeferredMessageManager getDeferredMessageManager();

    IFileTransferManager getFileTransferManager();

    IHistoryManager getHistoryManager();

    IMessageManager getMessageManager();


    String getLocalUserId();
    String getAppId();
    String getSheme();
    void close();

    void addListener(IIMServiceListener listener);
    void removeListener(IIMServiceListener listener);

}
