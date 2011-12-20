package javax.microedition.ims.android.presence;

import javax.microedition.ims.android.IReasonInfo; 

interface IPresenceSourceListener {
    void publicationDelivered();
    void publicationFailed(in IReasonInfo reason);
    void publicationTerminated(in IReasonInfo reason);
}