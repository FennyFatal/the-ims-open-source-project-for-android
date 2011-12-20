package javax.microedition.ims.android.msrp;

import javax.microedition.ims.android.msrp.IConferenceManagerListener;

interface IConferenceManager
{

    String sendChatInvitation(String sender, String recipient, String subject);
    
    String sendConferenceInvitation(String sender, in String[] recipients, String subject);
    
    String joinPredefinedConference(String sender, String conferenceURI);
    
    void cancelInvitation(String sessionId);
    
    
    void addListener(IConferenceManagerListener listener);
    void removeListener(IConferenceManagerListener listener);

}
