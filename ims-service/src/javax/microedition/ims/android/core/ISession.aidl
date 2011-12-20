package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.ISessionListener;
import javax.microedition.ims.android.core.IServiceMethod;
import javax.microedition.ims.android.core.ISessionDescriptor;
import javax.microedition.ims.android.core.IReference;
import javax.microedition.ims.android.core.media.IMedia;
import javax.microedition.ims.android.core.ICapabilities;

interface ISession
{
    // Adding/removing/rolling back/pending updates to medias
    // is handled at the client side library
    void accept(in List<IMedia> medias);
    void reject(int statusCode);
    void rejectWithDiversion(String alternativeUserAddress);
    void preaccept(in List<IMedia> medias);
    void start(in List<IMedia> medias);
    void terminate();
    void update(in List<IMedia> medias);
    List<IMedia> getMedias();
    int getState();
    boolean useResourceReservation();
        
    void addListener(ISessionListener listener);
    void removeListener(ISessionListener listener);
        
    ISessionDescriptor getSessionDescriptor();
    IServiceMethod getServiceMethod();
    IReference createReference(String referToUserId, String method);
    ICapabilities createCapabilities();    
}
