package javax.microedition.ims.engine.test;

import android.content.Context;
import android.util.Log;

import javax.microedition.ims.*;
import javax.microedition.ims.core.*;
import javax.microedition.ims.engine.test.invite.DialogCallInProgress;
import javax.microedition.ims.engine.test.invite.DialogIncomingCall;
import javax.microedition.ims.engine.test.invite.DialogOutgoingCall;
import javax.microedition.ims.engine.test.invite.DialogVideoCall;
import javax.microedition.ims.engine.test.refer.DialogIncomingRefer;
import javax.microedition.ims.engine.test.refer.DialogSendRefer;
import javax.microedition.ims.engine.test.refer.DialogSendReferProgress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Application context.
 *
 * @author ext-akhomush
 */
public final class AppContext implements CoreServiceListener, ConnectionStateListener {
    public static final String TAG = "AppContext";
    public static final AppContext instance = new AppContext();
    
/*    public static final String[][] REGISTRY =
        new String[][] {
            {"Stream", "Audio Video"},
            {"Basic", "application/test"},
            {"Event", "presence refer"},
            {
                "CoreService",
                "test",
                "urn:IMSAPI:javax.microedition.ims.engine.test",
                "urn:3gpp:org.3gpp.icsi;require urn:3gpp:3gpp-service.ims.icsi.mmtel",
                ""},
            {"Cap", "StreamAudio", "Req_Resp", "a=rtpmap:0 PCMU/8000"},
            {"Cap", "StreamVideo", "Req_Resp", "a=rtpmap:31 H261/90000", "a=rtpmap:32 MPV/90000"}
            };
*/
    
    //+g.oma.sip-im +g.3gpp.smsip
    //{"Reg", "javax.microedition.ims.engine.test", "Path"}
    
    //IARI, ICSIs, and Feature Tags
/*    public static final String[][] REGISTRY =
        new String[][] {
            {
                "CoreService",
                "test",
                "",
                "urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel",
                ""},
            {"Cap", "StreamAudio", "Req_Resp", "a=rtpmap:0 PCMU/8000"},
            {"Cap", "StreamVideo", "Req_Resp", "a=rtpmap:34 H263/90000", "a=rtpmap:31 H261/90000", "a=rtpmap:32 MPV/90000"},
            {"Pager", "text/plain application/vnd.3gpp.sms"},
            {"Reg", "", "14fa394d-c455-84cd-2c97-22b69af15f43"},
            {"XdmAuth", "12065748784", "TMO-VOIP-TRIAL"}
            };
*/    //Property for authentication
    //{"Auth", "12065748784", "TMO-VOIP-TRIAL"}
    //Property for xdm authentication
    //{"Xdm-auth", "12065748784", "TMO-VOIP-TRIAL"}

    //TODO for testing with alternative server
    public static final String[][] REGISTRY =
        new String[][] {
            {
                "CoreService",
                "test",
                "",
                "urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel",
                ""},
            {"Cap", "StreamAudio", "Req_Resp", "a=rtpmap:0 PCMU/8000"},
            {"Cap", "StreamVideo", "Req_Resp", "a=rtpmap:34 H263/90000", "a=rtpmap:31 H261/90000", "a=rtpmap:32 MPV/90000"},
            {"Pager", "text/plain application/vnd.3gpp.sms"},
            {"Reg", "", "14fa394d-c455-84cd-2c97-22b69af15f43"}
            };

    
    private final List<ContextListener> coreServiceListeners = Collections.synchronizedList(new ArrayList<ContextListener>());

    private DialogSendReferProgress.DlgParams dialogSendReferProgressParams;
    private DialogSendRefer.DlgParams dialogSendReferParams;
    private DialogIncomingRefer.DlgParams dialogIncomingReferParams;
    private DialogOutgoingCall.DlgParams dialogOutgoingCallParams;
    private DialogVideoCall.DlgParams dialogVideoCallParams;
    private DialogCallInProgress.DlgParams dialogCallInProgressParams;
    private DialogIncomingCall.DlgParams dialogIncomingCallParams;
    private DialogSendRefer dialogSendRefer;
    
    private Configuration configuration;
    
    public enum IncomingSessionUpdateAction {
        ACCEPT, REJECT
    };
    
    private IncomingSessionUpdateAction actionOnSessionUpdate = IncomingSessionUpdateAction.ACCEPT;


    /**
     * Internal application listener.
     *
     * @author Khomushko
     */
    interface ContextListener {
        void incomeCallReceved(final String remoteParty, final Session session);

        void connectionStateChanged(final ImsStateType imsState, final ConnectionStateType connectionState);

        void pageMessageReceived(final PageMessage pageMessage);
        //void pageMessageReceived(CoreService service,PageMessage message);

        void referenceReceived(CoreService service, Reference reference);
        
        void coreServiceClosed(CoreService service, ReasonInfo reason);
    }

    enum ConnectionStateType {
        CONNECTION_RESUMED, CONNECTION_SUSPENDED;

        public static ConnectionStateType valueOf(boolean isSuspended) {
            return isSuspended ? ConnectionStateType.CONNECTION_SUSPENDED : ConnectionStateType.CONNECTION_RESUMED;
        }
    }

    enum ImsStateType {
        IMS_CONNECTED, IMS_DISCONNECTED;

        public static ImsStateType valueOf(boolean isConnected) {
            return isConnected ? ImsStateType.IMS_CONNECTED : ImsStateType.IMS_DISCONNECTED;
        }
    }
    
    private SessionList sessionList = new SessionList();

    private CoreService mConnection;
//    private Session session;
//    private Session sessionByRefer;
    private ConnectionState connectionState;

    private AppContext() {
    }

    public CoreService getConnection() {
        return mConnection;
    }

    public void setConnection(CoreService connection) {
        if (mConnection != null) {
            mConnection.close();
        }
        
        this.mConnection = connection;

        if (mConnection != null) {
            mConnection.setListener(this);
        }
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * {@link CoreServiceListener#sessionInvitationReceived(CoreService, Session)}
     */
    
    public void sessionInvitationReceived(CoreService service, Session session) {
        Log.d(TAG, "sessionInvitationReceived#starting");
//        setSession(session);
        for (ContextListener listener : coreServiceListeners) {
            Log.d(TAG, "listener:" + listener);
            listener.incomeCallReceved(session.getRemoteUserId()[0], session);
        }
        Log.d(TAG, "sessionInvitationReceived#finished");
    }

    /**
     * {@link CoreServiceListener#referenceReceived(CoreService, Reference)}
     */
    
    public void referenceReceived(CoreService coreService, Reference reference) {
        Log.d(TAG, "referenceReceived#starting");

        for (ContextListener listener : coreServiceListeners) {
            Log.d(TAG, "listener:" + listener);
            listener.referenceReceived(coreService, reference);
        }

        Log.d(TAG, "referenceReceived#finished");
    }

    public void removeCoreServiceListener(ContextListener listener) {
        coreServiceListeners.remove(listener);
    }

    public void addCoreServiceListener(ContextListener listener) {
        coreServiceListeners.add(listener);
    }

    public SessionList getSessionList() {
        return sessionList;
    }

    public void free(Context context) {
        if (mConnection != null) {
            //mConnection.setListener(null);
            mConnection.close();
            mConnection = null;
        } 

        if (connectionState != null) {
            Log.i(TAG, "Starting destroying connection state manager");
            //connectionState.setListener(null);
            connectionState.close();
            connectionState = null;
            Log.i(TAG, "Finished destroying connection state manager");
        }
        
        if(configuration != null) {
            Log.i(TAG, "Starting destroying configuration manager");
            configuration.close();
            configuration = null;
            Log.i(TAG, "Finished destroying configuration manager");
        }

        coreServiceListeners.clear();
        
        Connector.close(context);
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
        if (connectionState != null) {
            connectionState.setListener(this);
        }
    }

    public void connectionResumed() {
        Log.i(TAG, "connectionResumed#");
        ImsStateType imsState = ImsStateType.valueOf(connectionState.isConnected());
        connectionStateChanged(imsState, ConnectionStateType.CONNECTION_RESUMED);
    }

    public void connectionSuspended() {
        Log.i(TAG, "connectionSuspended#");
        ImsStateType imsState = ImsStateType.valueOf(connectionState.isConnected());
        connectionStateChanged(imsState, ConnectionStateType.CONNECTION_SUSPENDED);
    }

    public void imsConnected(String connectionInfo) {
        Log.i(TAG, "imsConnected-info#"+connectionInfo);
        ConnectionStateType connectionStateType = ConnectionStateType.valueOf(connectionState.isSuspended());
        connectionStateChanged(ImsStateType.IMS_CONNECTED, connectionStateType);
    }

    public void imsDisconnected() {
        Log.i(TAG, "imsDisconnected#");
        ConnectionStateType connectionStateType = ConnectionStateType.valueOf(connectionState.isSuspended());
        connectionStateChanged(ImsStateType.IMS_DISCONNECTED, connectionStateType);
    }

    private void connectionStateChanged(ImsStateType imsState, ConnectionStateType connectionState) {
        for (ContextListener listener : coreServiceListeners) {
            listener.connectionStateChanged(imsState, connectionState);
        }
    }

    
    public void pageMessageReceived(CoreService service, PageMessage message) {
        Log.d(TAG, "pageMessageReceived#message = " + message);
        for (ContextListener listener : coreServiceListeners) {
            listener.pageMessageReceived(message);
        }
	}

    public DialogSendReferProgress.DlgParams getDialogSendReferProgressParams() {
        return dialogSendReferProgressParams;
    }

    public void setDialogSendReferProgressParams(
            DialogSendReferProgress.DlgParams dialogSendReferProgressParams) {
        this.dialogSendReferProgressParams = dialogSendReferProgressParams;
    }

    public DialogSendRefer.DlgParams getDialogSendReferParams() {
        return dialogSendReferParams;
    }

    public void setDialogSendReferParams(
            DialogSendRefer.DlgParams dialogSendReferParams) {
        this.dialogSendReferParams = dialogSendReferParams;
    }

    public DialogIncomingRefer.DlgParams getDialogIncomingReferParams() {
        return dialogIncomingReferParams;
    }

    public void setDialogIncomingReferParams(
            DialogIncomingRefer.DlgParams dialogIncomingReferParams) {
        this.dialogIncomingReferParams = dialogIncomingReferParams;
    }

    public DialogOutgoingCall.DlgParams getDialogOutgoingCallParams() {
        return dialogOutgoingCallParams;
    }

    public void setDialogOutgoingCallParams(DialogOutgoingCall.DlgParams dialogOutgoingCallParams) {
        this.dialogOutgoingCallParams = dialogOutgoingCallParams;
    }

    public DialogCallInProgress.DlgParams getDialogCallInProgressParams() {
        return dialogCallInProgressParams;
    }

    public void setDialogCallInProgressParams(DialogCallInProgress.DlgParams dialogCallInProgressParams) {
        this.dialogCallInProgressParams = dialogCallInProgressParams;
    }

    public DialogVideoCall.DlgParams getDialogVideoCallParams() {
        return dialogVideoCallParams;
    }

    public void setDialogVideoCallParams(DialogVideoCall.DlgParams dialogVideoCallParams) {
        this.dialogVideoCallParams = dialogVideoCallParams;
    }

    public DialogSendRefer getDialogSendRefer() {
        return dialogSendRefer;
    }

    public void setDialogSendRefer(DialogSendRefer dialogSendRefer) {
        this.dialogSendRefer = dialogSendRefer;
    }

    public DialogIncomingCall.DlgParams getDialogIncomingCallParams() {
        return dialogIncomingCallParams;
    }

    public void setDialogIncomingCallParams(
            DialogIncomingCall.DlgParams dialogIncomingCallParams) {
        this.dialogIncomingCallParams = dialogIncomingCallParams;
    }

    public IncomingSessionUpdateAction getActionOnSessionUpdate() {
        return actionOnSessionUpdate;
    }

    public void setActionOnSessionUpdate(
            IncomingSessionUpdateAction actionOnSessionUpdate) {
        this.actionOnSessionUpdate = actionOnSessionUpdate;
    }

	public void serviceClosed(CoreService service, ReasonInfo reason) {
		 Log.d(TAG, "pageMessageReceived#message = " + reason);
	        for (ContextListener listener : coreServiceListeners) {
	            listener.coreServiceClosed(service, reason);
	        }
	}

}
