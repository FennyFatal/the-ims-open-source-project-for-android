package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.msrp.IChat;
import javax.microedition.ims.android.msrp.IChatInvitation;
import javax.microedition.ims.android.msrp.IConference;
import javax.microedition.ims.android.msrp.IConferenceInvitation;

interface IConferenceManagerListener
{

    void chatInvitationReceived(IChatInvitation chatInvitation);
    
    void chatStarted(IChat chat);
    
    void chatStartFailed(String sessionId, in IReasonInfo reason);


    void conferenceInvitationReceived(IConferenceInvitation conferenceInvitation);
    
    void conferenceStarted(IConference conference);
    
    void conferenceStartFailed(String sessionId, in IReasonInfo reason);

}
