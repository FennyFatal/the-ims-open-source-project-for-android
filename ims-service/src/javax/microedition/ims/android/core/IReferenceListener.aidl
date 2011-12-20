package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.IReference;
import javax.microedition.ims.android.core.IMessage;

interface IReferenceListener 
{
    void referenceDelivered();
    void referenceDeliveryFailed();
    void referenceTerminated();
    void referenceNotify(IMessage notify); 
}

