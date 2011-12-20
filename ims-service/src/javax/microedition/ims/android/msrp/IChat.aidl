package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.msrp.IIMSession;
import javax.microedition.ims.android.msrp.IChatListener;

interface IChat
{

    void extendToConference(in String[] additionalParticipants);
    

    IIMSession getIMSession();

    void addListener(IChatListener listener);
    void removeListener(IChatListener listener);

}
