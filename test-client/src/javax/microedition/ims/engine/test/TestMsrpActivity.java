package javax.microedition.ims.engine.test;

import android.app.Activity;
import android.util.Log;

import javax.microedition.ims.Connector;
import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.im.*;


public class TestMsrpActivity extends Activity {
    
    private Chat curChat;

    protected void onStart() {        
        super.onStart();
        
        new Thread(new Runnable() {
            
            public void run() {
                
                Log.i("INFO_test", "START");
                
                final String appId = getResources().getText(R.id.def_app_id).toString();
                IMService imService = null;
                try {
                    imService = (IMService)Connector.open("imsim://" + appId, TestMsrpActivity.this);
                    Log.i("INFO_test", "START 2 " + imService);
                    imService.setListener(imServiceListener);
                    Log.i("INFO_test", "START 3");
                    
                    ConferenceManager conferenceManager = imService.getConferenceManager();
                    Log.i("INFO_test", "START 4 " + conferenceManager);
                    
                    conferenceManager.setListener(conferenceManagerListener);
                    Log.i("INFO_test", "START 5");
                    
                    String chatSessionId = conferenceManager.sendChatInvitation(
                            "sip:android1014@demo.movial.com",
                            "sip:android1003@demo.movial.com",
                            "Chat XXX");
                    Log.i("INFO_test", "START 6 - " + chatSessionId);
                    
                } catch (ImsException e) {
                    Log.i("INFO_test", e.getMessage());
                } catch (ServiceClosedException e) {
                    Log.i("INFO_test", e.getMessage());
                } 
                
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.i("INFO_test", e.getMessage());
                }
                
                
                Message message = new Message();
                ContentPart messageContent = new ContentPart("Do not tell the others...".getBytes(), "text/plain");
                message.addContentPart(messageContent);

                try {
                    curChat.sendMessage(message, false);
                    Log.i("INFO_test", "START 7");
                    
                } catch (ImsException e) {
                    Log.i("INFO_test", e.getMessage());
                }

                
                Log.i("INFO_test", "START 10");
                
                if(imService != null) {
                    imService.close();    
                }
                
                Log.i("INFO_test", "FINISH");
                
            }
        }).start();
        
    }
    
    
    
    
    private IMServiceListener imServiceListener = new IMServiceListener() {
        
        public void systemMessageReceived(IMService service, Message message) {
            Log.i("INFO_test", "IMServiceListener.systemMessageReceived");
        }
        
        
        public void serviceClosed(IMService service, ReasonInfo reason) {
            Log.i("INFO_test", "IMServiceListener.serviceClosed");
        }
        
        
        public void deliveryReportsReceived(IMService service, DeliveryReport[] reports) {
            Log.i("INFO_test", "IMServiceListener.deliveryReportsReceived");
        }
        
        
        public void advertisementMessageReceived(IMService service, Message message) {
            Log.i("INFO_test", "IMServiceListener.advertisementMessageReceived");
        }
    };
    
    
    private ConferenceManagerListener conferenceManagerListener = new ConferenceManagerListener() {
        
        public void conferenceStarted(Conference conference) {
            Log.i("INFO_test", "ConferenceManagerListener.conferenceStarted");
        }
        
        
        public void conferenceStartFailed(String sessionId, ReasonInfo reason) {
            Log.i("INFO_test", "ConferenceManagerListener.conferenceStartFailed");
        }
        
        
        public void conferenceInvitationReceived(ConferenceInvitation conferenceInvitation) {
            Log.i("INFO_test", "ConferenceManagerListener.conferenceInvitationReceived");
        }
        
        
        public void chatStarted(Chat chat) {
            curChat = chat;
            Log.i("INFO_test", "ConferenceManagerListener.chatStarted");
        }
        
        
        public void chatStartFailed(String sessionId, ReasonInfo reason) {
            Log.i("INFO_test", "ConferenceManagerListener.chatStartFailed");
        }
        
        
        public void chatInvitationReceived(ChatInvitation chatInvitation) {
            Log.i("INFO_test", "ConferenceManagerListener.chatInvitationReceived");
        }
    };
    

}
