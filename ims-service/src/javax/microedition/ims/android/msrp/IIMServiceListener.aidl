package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.IIMService;
import javax.microedition.ims.android.msrp.IMessage;
import javax.microedition.ims.android.msrp.IDeliveryReport;

interface IIMServiceListener
{

    void advertisementMessageReceived(IIMService service, in IMessage retValue);
    
    void deliveryReportsReceived(IIMService service, IDeliveryReport reports);
    
    void serviceClosed(IIMService service, in IReasonInfo reason);
    
    void systemMessageReceived(IIMService service, in IMessage retValue);

}
