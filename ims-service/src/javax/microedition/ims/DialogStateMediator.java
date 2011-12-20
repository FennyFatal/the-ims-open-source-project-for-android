/*package javax.microedition.ims;

/*public class DialogStateMediator extends DialogStateListenerAdapter  implements IncomingCallListener{
    public static final DialogStateMediator INSTANCE = new DialogStateMediator();

    public interface CoreServiceListener {
        void incomingCallRecieved(final Dialog dialog);
        void referenceRecieved(final Dialog dialog, final String referToUserId, final String method);
    }
    
    public interface SessionStateListener {
        void stateChanged(final SessionState state, final Dialog dialog);
        void referenceRecieved(final Dialog dialog, final String referToUserId, final String method);
    }
    
    private final Map<ClientIdentity, CoreServiceListener> coreServiceListeners = Collections.synchronizedMap(new HashMap<ClientIdentity, CoreServiceListener>());
    
    private final Map<DialogKey, SessionStateListener> sessionStateListeners = Collections.synchronizedMap(new HashMap<DialogKey, SessionStateListener>());

    private DialogStateMediator() {
    }

    public void registerSessionStateListener(ClientIdentity identity, String remoteParty, SessionStateListener listener) {
        sessionStateListeners.put(new DialogKey(identity, remoteParty, null), listener);
    }

    public void untegisterSessionStateListener(ClientIdentity identity, String remoteParty) {
        sessionStateListeners.remove(new DialogKey(identity, remoteParty, null));
    }

    public void registerCoreServiceListener(ClientIdentity identity, CoreServiceListener listener) {
        coreServiceListeners.put(identity, listener);
    }

    public void untegisterCoreServiceListener(ClientIdentity identity) {
        coreServiceListeners.remove(identity);
    }

    public void removeStateListeners() {
        synchronized (sessionStateListeners) {
            sessionStateListeners.clear();
        }
    }

    
    public void onSessionEvent(final DialogStateEvent event) {
        onDialogStateChanged(event.getDialog(), event.getSessionState());
    }
    
    private void onDialogStateChanged(final Dialog dialog, SessionState sessionState) {
        final DialogKey dialogKey = new DialogKey(dialog.getLocalParty(), dialog.getRemoteParty(), null);
        SessionStateListener stateListener = sessionStateListeners.get(dialogKey);
        if (stateListener != null) {
            Logger.log("DialogStateMediator#onDialogStateChanged"
                    + sessionState.name());
            stateListener.stateChanged(sessionState, dialog);
        } else {
            Logger.log("DialogStateMediator", "onDialogStateChanged# cannot find listeners for:" + dialogKey);
        }
    }

    public ClientIdentity getClientIdentityByName(String clientShortUri) {
        //TODO temporary commented, should be resolved later 
*//*
         ClientIdentity identity = null;
         for (ClientIdentity key : coreServiceListeners.keySet()) {
            if (clientShortUri.contains(key.getUserInfo().toUri())) {
                identity = key;
                break;
            }
        }
*//*
        Iterator<ClientIdentity> listenerIterator = coreServiceListeners.keySet().iterator();
        return listenerIterator.hasNext()? listenerIterator.next(): null;
    }

    
    public void onIncomingCall(final IncomingOperationEvent event) {
        Logger.log("onIncomingCall#localParty=" + event.getMsrpDialog().getLocalParty()
                + " remoteParty=" + event.getMsrpDialog().getRemoteParty());

        //String localPartyURI = localParty.getShortURI();
        //String localPartyURI = DIALOG.getLocalParty().getUserInfo().toUri();
        for (ClientIdentity identity : coreServiceListeners.keySet()) {
            //TODO need to resolve this issue
            //if (localPartyURI.contains(identity.getClientName())) {
                coreServiceListeners.get(identity).incomingCallRecieved(event.getMsrpDialog());
            //}
        }
    }

    
    public void onIncomingMediaUpdate(final IncomingOperationEvent event) {
        onDialogStateChanged(event.getMsrpDialog(), SessionState.SESSION_UPDATE_RECEIVED);
    }
}
*/
