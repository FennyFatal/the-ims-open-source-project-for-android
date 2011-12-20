package javax.microedition.ims.engine.test;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.*;
import javax.microedition.ims.engine.test.util.ContactsHolder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Subscription281Activity extends ListActivity {
    private static final String TAG = "Subscription281Activity";
    
    private CoreService coreService;
    private Subscription subscription;
    
    private static final int ON_SUBSCR_TERMINATED = 1;
    private static final int ON_SUBSCR_STARTED = 2;
    private static final int ON_SUBSCR_START_FAILED = 3;
    private static final int ON_SUBSCR_NOTIFY = 4;
    private static final int ON_WRONG_PARAM = 5;
    
    
    private final Handler mHandler = new Handler() {
        
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case ON_SUBSCR_TERMINATED: {
                Toast.makeText(Subscription281Activity.this, "Subscription Terminated", Toast.LENGTH_SHORT).show();
                findViewById(R.id.subscr281_subscribe_btn).setEnabled(true);
                break;
            }
            case ON_SUBSCR_STARTED: {
                Toast.makeText(Subscription281Activity.this, "Subscription Started", Toast.LENGTH_SHORT).show();
                findViewById(R.id.subscr281_unsubscribe_btn).setEnabled(true);
                break;
            }
            case ON_SUBSCR_START_FAILED: {
                Toast.makeText(Subscription281Activity.this, "Subscription Start Failed", Toast.LENGTH_SHORT).show();
                findViewById(R.id.subscr281_subscribe_btn).setEnabled(true);
                break;
            }
            case ON_SUBSCR_NOTIFY: {
                Toast.makeText(Subscription281Activity.this, "Subscription Notify", Toast.LENGTH_SHORT).show();
                
                String xmlContent = (String)msg.obj;
                
                NotificationItem item = new NotificationItem(
                    "-----" + new Date().toString() + "-----",
                    xmlContent);
                ((NotificationListAdapter)getListAdapter()).addItem(item);
                refreshList();
                
                break;
            }
            case ON_WRONG_PARAM: {
                String msgContent = (String)msg.obj;
                Toast.makeText(Subscription281Activity.this, "Wrong input param: " + msgContent, Toast.LENGTH_SHORT).show();
                findViewById(R.id.subscr281_subscribe_btn).setEnabled(true);
                break;
            }
            
            default:
                super.handleMessage(msg);
            }
        }
    };
    

    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        LinearLayout linearLayout = new LinearLayout(this);
//        
//        TextView textView = new TextView(this);
//        
//        textView.setText("Hello World");
//        
//        ListView listView = new ListView(this);
//        listView.setId(android.R.id.list);
//        
//        linearLayout.addView(textView);
//        linearLayout.addView(listView);
        
        setContentView(R.layout.subscription281);

        
        Spinner identityControl = (Spinner) findViewById(R.id.subscr281_remote_users);
        
        String[] remoteIdentities = ContactsHolder.getContacts(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, remoteIdentities);
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        identityControl.setAdapter(adapter);
        
        
/*        final String appId = getResources().getText(R.id.def_app_id).toString();
        try {
            coreService = (CoreService) Connector.open("imscore://" + appId, Subscription281Activity.this);
        } catch (ImsException e) {
            Log.e(TAG, e.getMessage());
        }
*/        
        coreService = AppContext.instance.getConnection();
        
        findViewById(R.id.subscr281_subscribe_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    final String remoteUser = getRemoteParty();
                    
                    TextView eventTextView = (TextView)findViewById(R.id.subscr281_event);
                    String eventValue = eventTextView.getText().toString();
                    
                    if(eventValue.equalsIgnoreCase("reg") || eventValue.equalsIgnoreCase("presence")) {
                        findViewById(R.id.subscr281_subscribe_btn).setEnabled(false);
                        
                        SubscriptionStartTaskParams params = new SubscriptionStartTaskParams(remoteUser, eventValue);
                        new SubscriptionStartTask().execute(params);
                    } else {
                        Toast.makeText(Subscription281Activity.this, "Event can be 'reg' or 'presence'", Toast.LENGTH_SHORT).show();
                    }
                }

/*                private String buildRemoteUri(final String remoteUser) {
                    return String.format("%s;auid=resource-lists", remoteUser);
                }
*/            }
        );
        findViewById(R.id.subscr281_unsubscribe_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    findViewById(R.id.subscr281_unsubscribe_btn).setEnabled(false);
                    new SubscriptionStopTask().execute();
                }
            }
        );
        findViewById(R.id.subscr281_clear_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    ((NotificationListAdapter)getListAdapter()).clear();
                    
                    refreshList();
                }
            }
        );
        findViewById(R.id.subscr281_close_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(Subscription281Activity.this, InviteActivity.class));
                }
            }
        );
        
        
        //((TextView)findViewById(R.id.subscr281_event)).setText("presence");
        ((TextView)findViewById(R.id.subscr281_event)).setText("reg");
        
        findViewById(R.id.subscr281_unsubscribe_btn).setEnabled(false);
        
        setListAdapter(new NotificationListAdapter(this));
    }
    
    private void refreshList() {
        ((ListView)findViewById(android.R.id.list)).requestLayout();
    }
    
    private String getRemoteParty() {
        final String retValue;
        
        TextView remoteIdentity = (TextView)findViewById(R.id.subscr281_remote_user);
        String userInput =  remoteIdentity.getText().toString();
        if(!TextUtils.isEmpty(userInput)) {
            retValue = userInput;
        } else {
            Spinner remoteIdentities = (Spinner) findViewById(R.id.subscr281_remote_users);
            int pos = remoteIdentities.getSelectedItemPosition();
            retValue = ContactsHolder.getContacts(this)[pos];
        }
        
        return UriUtils.encodeUri(retValue);
    }

    
    private class NotificationItem {
        private String time;
        private String xml;
        
        public NotificationItem(String time, String xml) {
            this.time = time;
            this.xml = xml;
        }

        public String getTime() {
            return time;
        }

        public String getXml() {
            return xml;
        }
    }


    private class NotificationListAdapter extends BaseAdapter {
        
        private Context context;
        private List<NotificationItem> items = new ArrayList<NotificationItem>();
        
        public NotificationListAdapter(Context context) {
            this.context = context;
        }
        
        public void clear() {
            items.clear();
        }
        
        public void addItem(NotificationItem item) {
            items.add(item);
        }

        
        public int getCount() {
            return items.size();
        }

        
        public Object getItem(int arg0) {
            return items.get(arg0);
        }

        
        public long getItemId(int arg0) {
            return arg0;
        }

        
        public View getView(int position, View convertView, ViewGroup parent) {
            NotificationItem notificationItem = items.get(position);
            NotificationView notifView = null;
            if (convertView != null) {
                notifView = (NotificationView)convertView;
                notifView.setTime(notificationItem.getTime());
                notifView.setXml(notificationItem.getXml());
            } else {
                notifView = new NotificationView(context,
                                notificationItem.getTime(),
                                notificationItem.getXml());
            }
            return notifView;
        }
    }
    
    
    private class NotificationView extends LinearLayout {
        
        private TextView timeCtrl;
        private TextView xmlCtrl;
        
        public NotificationView(Context context, String time, String xml) {
            super(context);
            
            this.setOrientation(VERTICAL);
            
            timeCtrl = new TextView(context);
            timeCtrl.setText(time);
            addView(timeCtrl, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            
            xmlCtrl = new TextView(context);
            xmlCtrl.setText(xml);
            addView(xmlCtrl, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }
        
        public void setTime(String time) {
            timeCtrl.setText(time);
        }
        
        public void setXml(String xml) {
            xmlCtrl.setText(xml);
        }
    }
    
    
    private class SubscriptionStartTask extends AsyncTask<SubscriptionStartTaskParams, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(SubscriptionStartTaskParams... params) {
            Boolean res = true;
            
            String remoteUserIdentity = params[0].getRemoteUserIdentity();
            String eventValue = params[0].getEventValue();
            
            Log.i(TAG, "SubscriptionStartTask START");
            
            try {
                subscription = coreService.createSubscription(
                        null,
                        remoteUserIdentity,
                        eventValue/*"presence"*/
                );
                
                Log.i(TAG, "SubscriptionStartTask 2");
                
                subscription.setListener(subscriptionListener);
                
                Log.i(TAG, "SubscriptionStartTask 3");
                
                subscription.subscribe();
                
                Log.i(TAG, "SubscriptionStartTask 4");
                
            } catch (ImsException e) {
                Log.i(TAG, e.getMessage());
                res = false;
            } catch (ServiceClosedException e) {
                Log.i(TAG, e.getMessage());
                res = false;
            } catch (IllegalArgumentException e) {
                Log.i(TAG, e.getMessage());
                res = false;
                
                android.os.Message androidMsg = new android.os.Message();
                androidMsg.what = ON_WRONG_PARAM;
                androidMsg.obj = e.getMessage();
                mHandler.sendMessage(androidMsg);
            }
            
            Log.i(TAG, "SubscriptionStartTask FINISH");
            
            return res;
        }

        
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }
    
    
    private class SubscriptionStartTaskParams {
        private String remoteUserIdentity;
        private String eventValue;
        
        public SubscriptionStartTaskParams(String remoteUserIdentity, String eventValue) {
            this.remoteUserIdentity = remoteUserIdentity;
            this.eventValue = eventValue;
        }

        public String getRemoteUserIdentity() {
            return remoteUserIdentity;
        }

        public String getEventValue() {
            return eventValue;
        }
    }
    
    
    private class SubscriptionStopTask extends AsyncTask<String, Void, Boolean> {
        
        protected void onPreExecute() {
            super.onPreExecute();
        }

        
        protected Boolean doInBackground(String... arg0) {
            Boolean res = true;

            Log.i(TAG, "SubscriptionStopTask START");
            
            try {
                subscription.unsubscribe();
                
                Log.i(TAG, "SubscriptionStopTask 2");
                
            } catch (ServiceClosedException e) {
                Log.i(TAG, e.getMessage());
                res = false;
            }
            
            Log.i(TAG, "SubscriptionStopTask FINISH");

            return res;
        }

        
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }
    
    
    private SubscriptionListener subscriptionListener = new SubscriptionListener() {
        
        
        public void subscriptionTerminated(Subscription subscription) {
            mHandler.sendEmptyMessage(ON_SUBSCR_TERMINATED);
            Log.i(TAG, "subscriptionTerminated");
        }
        
        
        public void subscriptionStarted(Subscription subscription) {
            mHandler.sendEmptyMessage(ON_SUBSCR_STARTED);
            Log.i(TAG, "subscriptionStarted");
        }
        
        
        public void subscriptionStartFailed(Subscription subscription) {
            mHandler.sendEmptyMessage(ON_SUBSCR_START_FAILED);
            Log.i(TAG, "subscriptionStartFailed");
        }
        
        
        public void subscriptionNotify(Subscription subscription, Message notify) {
            Log.i(TAG, "subscriptionNotify");
            
            byte buf[] = new byte[100];
            StringBuilder out = new StringBuilder();
            
            MessageBodyPart[] bodyParts = notify.getBodyParts();
            
            if (bodyParts != null && bodyParts.length > 0) {
                MessageBodyPart messageBodyPart = bodyParts[0];
                InputStream openContentInputStream = messageBodyPart.openContentInputStream();
                
                try {
                    int read = 0;
                    while ((read = openContentInputStream.read(buf)) != -1) {
                        out.append( new String(buf, 0, read) );
                    }
                    
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            
            android.os.Message androidMsg = new android.os.Message();
            androidMsg.what = ON_SUBSCR_NOTIFY;
            androidMsg.obj = out.toString();
            mHandler.sendMessage(androidMsg);
            
            Log.i(TAG, "subscriptionNotify 2");
        }
    };

}
