package javax.microedition.ims.engine.test;

import android.app.Activity;
import android.util.Log;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.*;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.core.media.MediaDescriptor;
import javax.microedition.ims.core.media.StreamMedia;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//Reference ref = coreService.createReference(
//"sip:android1003@demo.movial.com",
//"sip:android1002@demo.movial.com",
//"sip:android1003@demo.movial.com",
//"INVITE");

public class TestReferActivity extends Activity {
    
    private final static String SIP_SESSION_FROM = "sip:android1003@demo.movial.com";
    private final static String SIP_SESSION_TO = "sip:android1002@demo.movial.com";
//    private final static String SIP_REFER_INVITE_TARGET = "sip:android1005@demo.movial.com";
    private final static String SIP_REFER_INVITE_TARGET = "sip:android1014@demo.movial.com";
    
    private CoreService coreServiceRef;

    protected void onStart() {        
        super.onStart();

        new Thread(new Runnable() {
            
            public void run() {

                Log.i("INFO_test", "START");
                
                try {
                    //final String appId = getResources().getText(R.id.def_app_id).toString();
                    //CoreService coreService = (CoreService) Connector.open("imscore://" + appId, TestReferActivity.this);
                    CoreService coreService = AppContext.instance.getConnection();
                    setCoreServiceRef(coreService);
                    
                    Log.i("INFO_test", "START 2 " + coreService);
                    coreService.setListener(coreServiceListener);
                    Log.i("INFO_test", "START 3");
                    
                    Log.i("INFO_test", "START 4");
                    
                    doSessionStartWork(coreService);
                    
                    Log.i("INFO_test", "START 5");
                    
                } catch (ImsException e) {
                    Log.i("INFO_test", e.getMessage());
                } catch (ServiceClosedException e) {
                    Log.i("INFO_test", e.getMessage());
                }
                
                Log.i("INFO_test", "FINISH");
                
                try {
                    
                    Log.i("INFO_test", "WAITING...");
                    Thread.sleep(50000);
                    
                } catch (Exception e) {
                    Log.i("INFO_test", e.getMessage());
                    // TODO: handle exception
                }
                
            }
        }).start();
        
    }
    

    public void doSessionStartWork(CoreService coreService) throws ImsException, ServiceClosedException {
        Log.i("INFO_test", "START - doSessionStartWork()");

        //DtfmPayload dtfmPayload = SettingsHelper.extractDtfmPayload(TestReferActivity.this);
        Session session = coreService.createSession(SIP_SESSION_FROM, SIP_SESSION_TO/*, dtfmPayload*/);
        
        session.setListener(sessionListener);
        
        StreamMedia media = (StreamMedia) session.createMedia(Media.MediaType.StreamMedia, Media.DIRECTION_SEND_RECEIVE);
        media.setStreamType(StreamMedia.STREAM_TYPE_AUDIO);
        MediaDescriptor audioDescriptor = media.getMediaDescriptors()[0];
        audioDescriptor.setMediaTitle("StreamMediaAudio");                 
        
        session.start();
        
        Log.i("INFO_test", "FINISH - doSessionStartWork()");
    }
    
    
    private void doReferSendWork(Session session) {
        Log.i("INFO_test", "START - doReferSendWork()");
        
        try {
            Reference ref = session.createReference(SIP_REFER_INVITE_TARGET, "INVITE");
            
            ref.setListener(referenceListener);

            ref.refer(true);
            
        } catch (ImsException e) {
            Log.i("INFO_test", e.getMessage());
        } catch (ServiceClosedException e) {
            Log.i("INFO_test", e.getMessage());
        }
        
        Log.i("INFO_test", "FINISH - doReferSendWork()");
    }
    
    
    private void doReferenceReceived(CoreService service, Reference reference) {
        Log.i("INFO_test", "START - doReferenceReceived()");
        
        try {
            String referToUserId = reference.getReferToUserId();
            String referMethod = reference.getReferMethod();

            // notify the application of the reference
            //...

            if ("INVITE".compareToIgnoreCase(referMethod) == 0) {

                reference.accept();
                // assume referMethod == "INVITE"

                //DtfmPayload dtfmPayload = SettingsHelper.extractDtfmPayload(TestReferActivity.this);
                Session mySession = service.createSession(null, referToUserId/*, dtfmPayload*/);

                // connect the reference with the IMS engine 
                reference.connectReferMethod((ServiceMethod) mySession);

                // start the reference with the third party 
                mySession.start();
            }

        } catch (ImsException e) {
            Log.i("INFO_test", e.getMessage());
        } catch (ServiceClosedException e) {
            Log.i("INFO_test", e.getMessage());
        }
        
        Log.i("INFO_test", "FINISH - doReferenceReceived()");
    }


    private CoreServiceListener coreServiceListener = new CoreServiceListener() {
        
        public void referenceReceived(CoreService coreService, Reference reference) {
            Log.i("INFO_test", "START - CoreServiceListener.referenceReceived(CoreService service, Reference reference)");
            
            doReferenceReceived(coreService, reference);

            Log.i("INFO_test", "FINISH - CoreServiceListener.referenceReceived(CoreService service, Reference reference)");
        }

        
        public void sessionInvitationReceived(CoreService service, Session session) {
        }

        
        public void pageMessageReceived(CoreService service, PageMessage message) {
        }

		public void serviceClosed(CoreService service, ReasonInfo reason) {
			
		}
    };


    private SessionListener sessionListener = new SessionListener() {
        
        public void sessionReferenceReceived(Session session, Reference reference) {
            Log.i("INFO_test", "START - SessionListener.sessionReferenceReceived(Session session, Reference reference)");
            
            doReferenceReceived(getCoreServiceRef(), reference);
            
            Log.i("INFO_test", "FINISH - SessionListener.sessionReferenceReceived(Session session, Reference reference)");
        }

        
        public void sessionAlerting(Session session) {
        }

        
        public void sessionStarted(Session session) {
            Log.i("INFO_test", "START - SessionListener.sessionStarted(Session session)");
            
            doReferSendWork(session);
            
            Log.i("INFO_test", "FINISH - SessionListener.sessionStarted(Session session)");
        }

        
        public void sessionStartFailed(Session session) {
        }

        
        public void sessionTerminated(Session session) {
        }

        
        public void sessionUpdated(Session session) {
        }

        
        public void sessionUpdateFailed(Session session) {
        }

        
        public void sessionUpdateReceived(Session session) {
        }
    };

    private ReferenceListener referenceListener = new ReferenceListener() {
        
        public void referenceTerminated(Reference reference) {            
            Log.i("INFO_test", "ReferenceListener.referenceTerminated(Reference reference)");
        }

        
        public void referenceNotify(Reference reference, Message notify) {           
            Log.i("INFO_test", "ReferenceListener.referenceNotify(Reference reference, Message notify)");
            Log.i("INFO_test", "notify.getMethod()           : " + notify.getMethod());
            Log.i("INFO_test", "notify.getReasonPhrase()     : " + notify.getReasonPhrase());
            Log.i("INFO_test", "notify.getState()            : " + notify.getState());
            Log.i("INFO_test", "notify.getStatusCode()       : " + notify.getStatusCode());
            Log.i("INFO_test", "notify.getBodyParts().length : " + notify.getBodyParts().length);
            
            try {
                InputStream inputStream = notify.getBodyParts()[0].openContentInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                char[] buf = new char[100];
                int size = inputStreamReader.read(buf);
                size = size < 99 ? size : 99;
                buf[size]='\n';
                Log.i("INFO_test", "BODY : " + new StringBuffer().append(buf, 0, size).toString());
            } catch (IOException e) {
                Log.i("INFO_test", e.getMessage());
            }
            
        }

        
        public void referenceDeliveryFailed(Reference reference) {
            Log.i("INFO_test", "ReferenceListener.referenceDeliveryFailed(Reference reference)");
        }

        
        public void referenceDelivered(Reference reference) {
            Log.i("INFO_test", "ReferenceListener.referenceDelivered(Reference reference)");
        }
    };

    public CoreService getCoreServiceRef() {
        return coreServiceRef;
    }


    public void setCoreServiceRef(CoreService coreServiceRef) {
        this.coreServiceRef = coreServiceRef;
    }

}
