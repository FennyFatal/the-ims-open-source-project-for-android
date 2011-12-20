package javax.microedition.ims.android.xdm;

import javax.microedition.ims.android.IReasonInfo;

interface IXDMServiceListener {
    void serviceClosed(in IReasonInfo reasonInfo);
}   