package javax.microedition.ims.android.presence;

import javax.microedition.ims.android.IReasonInfo;

interface IPresenceServiceListener {
    void serviceClosed(in IReasonInfo reasonInfo);
}   