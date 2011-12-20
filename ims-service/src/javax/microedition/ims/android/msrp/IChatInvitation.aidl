package javax.microedition.ims.android.msrp;

interface IChatInvitation
{

    void accept();

    void reject();

    String getSessionId();

    String getSender();

    String getSubject();
    
    
    boolean isExpired();

}
