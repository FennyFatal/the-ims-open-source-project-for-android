package javax.microedition.ims.engine.test;

import android.util.Log;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.core.Session;
import javax.microedition.ims.core.media.Media;
import java.util.*;

public class SessionList {
    protected final static String TAG = "SessionList";
    
    public static enum SessonType {
        INCOMING,
        OUTGOING
    };
    
    public enum SessionState {
        IN_PROGRESS,//only one session can be in this state in one time
        IS_HOLDED   
    };
    
    Map<String, ExtSession> items = Collections.synchronizedMap(new HashMap<String, ExtSession>());
    
    
    public ExtSession addNewInProgressSession(Session session, SessonType sessonType) {
        String remoteIdentity = session.getRemoteUserId()[0];
        
        currentInProgressSessionOnHold();
        
        ExtSession res = new ExtSession(session, SessionState.IN_PROGRESS, sessonType, remoteIdentity);
        items.put(remoteIdentity, res);
        
        return res;
    }
    
    public void removeSessionOnTermination(Session session) {
        String remoteIdentity = session.getRemoteUserId()[0];
        items.remove(remoteIdentity);
    }
    
    public String[] getRemoteParties() {
        List<String> remoteParties = new ArrayList<String>();
        
        for (ExtSession ses : items.values()) {
            if (ses.getState() == SessionState.IN_PROGRESS) {
                remoteParties.add("A-" + ses.getRemoteIdentity());
            } else if (ses.getState() == SessionState.IS_HOLDED) {
                remoteParties.add("H-" + ses.getRemoteIdentity());
            }
        }
        
        return remoteParties.toArray(new String[0]);
    }
    
    public ExtSession getInProgressSession() {
        ExtSession res = null;
        for (ExtSession extSession : items.values()) {
            if(extSession.getState().equals(SessionState.IN_PROGRESS)) {
                res = extSession;
                break;
            }
        }
        return res;        
    }
    
    public void onHoldCurrentAndActivateAnotherSession(String remoteParty) {
        currentInProgressSessionOnHold();
        
        ExtSession ses = items.get(remoteParty);
        holdUnhold(ses, false);
        ses.setState(SessionState.IN_PROGRESS);
    }
    
    public String removeSessionAndActivateAnother(Session sessionToRemove) {
        String remoteIdentity = sessionToRemove.getRemoteUserId()[0];
        items.remove(remoteIdentity);
        
        if(items.values().size() > 0) {
            ExtSession ses = items.values().iterator().next();
            holdUnhold(ses, false);
            ses.setState(SessionState.IN_PROGRESS);
            
            return ses.getRemoteIdentity();
        }
        return null;
    }
    
    private void currentInProgressSessionOnHold() {
        ExtSession extSession = getInProgressSession();
        if (extSession != null) {
            
            holdUnhold(extSession, true);
            
            extSession.setState(SessionState.IS_HOLDED);
        }
    }
    
    private void holdUnhold(ExtSession extSession, boolean hold) {
        int direction = hold ? Media.DIRECTION_SEND : Media.DIRECTION_SEND_RECEIVE;
        
        Media[] medias = extSession.getSession().getMedia();
        if (medias != null) {
            try {
                
//                final BreakPoint breakPoint = new BreakPoint();
//                final AtomicReference<Boolean> holdResult = new AtomicReference<Boolean>();

                for(Media media : medias) {
                    media.setDirection(direction);
                }

//                extSession.getSession().setListener(new SessionAdapter() {
//                    
//                    public void sessionUpdated(Session session) {
//                        Log.i(TAG, "sessionUpdated#");
//                        holdResult.compareAndSet(null, true);
//                        breakPoint.notifySynchPoint();
//                    }
//
//                    
//                    public void sessionUpdateFailed(Session session) {
//                        Log.i(TAG, "sessionUpdateFailed#");
//                        holdResult.compareAndSet(null, false);
//                        breakPoint.notifySynchPoint();
//                    }
//                });

                extSession.getSession().update();
//                breakPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);
//
//                if (holdResult.get() == null || holdResult.get() == Boolean.FALSE) {
//                    throw new ImsException("Cannot stay onhold session = "
//                            + extSession.getSession());
//                }
                
            } catch (ImsException e) {
                Log.e(TAG, e.getMessage());
            } catch (IllegalStateException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
    
    
    public static class ExtSession {
        private Session session;
        private SessionState state;
        private SessonType type;
        private String remoteIdentity;
        
        public ExtSession(Session session, SessionState state, SessonType type,
                String remoteIdentity) {
            this.session = session;
            this.state = state;
            this.type = type;
            this.remoteIdentity = remoteIdentity;
        }

        public Session getSession() {
            return session;
        }
        
        public void setSession(Session session) {
            this.session = session;
        }
        
        public SessionState getState() {
            return state;
        }
        
        public void setState(SessionState state) {
            this.state = state;
        }

        public String getRemoteIdentity() {
            return remoteIdentity;
        }

        public void setRemoteIdentity(String remoteIdentity) {
            this.remoteIdentity = remoteIdentity;
        }
    }
    
}
