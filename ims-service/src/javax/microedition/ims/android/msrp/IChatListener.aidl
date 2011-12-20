package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.IChat;
import javax.microedition.ims.android.msrp.IConference;

interface IChatListener
{

    void chatExtended(IChat chat, IConference conference);
    
    void chatExtensionFailed(IChat chat, in IReasonInfo reason);

}
