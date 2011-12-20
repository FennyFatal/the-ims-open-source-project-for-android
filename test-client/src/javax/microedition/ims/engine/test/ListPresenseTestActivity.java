package javax.microedition.ims.engine.test;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.*;
import java.io.IOException;
import java.io.InputStream;

public class ListPresenseTestActivity extends BaseActivity{

	//private static final int remoteIdentitiesItems = R.array.remote_parties;

	protected SubscribeStatusTask publishStatusTask;
	protected SubscribeStatusTask subscribeStatusTask;

	private static final int ON_SUBSCR_TERMINATED = 1;
	private static final int ON_SUBSCR_STARTED = 2;
	private static final int ON_SUBSCR_START_FAILED = 3;
	private static final int ON_SUBSCR_NOTIFY = 4;


	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);		

		setContentView(R.layout.list_subscribe_test);
		

		findViewById(R.id.btn_status_subscribe).setOnClickListener(subscribeListener);  
		findViewById(R.id.btn_status_unsubscribe).setOnClickListener(unsubscribeListener); 
		findViewById(R.id.btn_back).setOnClickListener(backListener);
	}

	
	protected void onStart() {
		super.onStart();
		updateConnectionState(AppContext.instance.getConnectionState());
	}

	private OnClickListener subscribeListener = new OnClickListener() {
		public void onClick(View v) {
			String uri = getRemoteParty();
			if(uri != null & uri.length() > 0){
				(subscribeStatusTask = new SubscribeStatusTask()).execute(uri);
			} else {
				Toast.makeText(ListPresenseTestActivity.this, "Wrong uri to subscribe", Toast.LENGTH_SHORT).show();
			}
		}
	};

	protected UnSubscribeStatusTask unsubscribeStatusTask;

	private OnClickListener unsubscribeListener = new OnClickListener() {
		public void onClick(View v) {
			(unsubscribeStatusTask = new UnSubscribeStatusTask()).execute();
		}
	};

	private OnClickListener backListener = new OnClickListener() {
		public void onClick(View v) {
			startActivity(new Intent(ListPresenseTestActivity.this, InviteActivity.class));
		}
	};

	public Subscription sub;

	private class SubscribeStatusTask extends AsyncTask<String, Void, Boolean>{

		
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);           
		}

		
		protected Boolean doInBackground(String... params) {
			final String uri = params[0];

			boolean retValue = true;
			CoreService coreService = AppContext.instance.getConnection();
			try {
				sub = coreService.createSubscription(AppContext.instance.getConnection().getLocalUserId(), uri, "presence");
				sub.setListener(subscriptionListener); 
				sub.subscribe();
			} catch (ServiceClosedException e) {
				retValue = false;
				e.printStackTrace();
			} catch (ImsException e) {
				retValue = false;
				e.printStackTrace();
			}
			return retValue;
		}

		/*
		protected void onPostExecute(Boolean result) {
			setProgressBarIndeterminateVisibility(false);
			findViewById(R.id.btn_status_publish).setEnabled(true);

			String message = result? "Presence status sent": "Presence status didn't sent";
			Toast.makeText(ContactPresenseTestActivity.this, message, Toast.LENGTH_LONG).show();
		}*/
	}

	private class UnSubscribeStatusTask extends AsyncTask<String, Void, Boolean>{

		
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);			
		}

		
		protected Boolean doInBackground(String... params) {			

			boolean retValue = true;

			if(sub != null){
				try {
					sub.unsubscribe();
				} catch (ServiceClosedException e) {
					retValue = false;
					e.printStackTrace();
				}
			}

			return retValue;
		}

		/*
		protected void onPostExecute(Boolean result) {
			setProgressBarIndeterminateVisibility(false);
			findViewById(R.id.btn_status_publish).setEnabled(true);

			String message = result? "Presence status sent": "Presence status didn't sent";
			Toast.makeText(ContactPresenseTestActivity.this, message, Toast.LENGTH_LONG).show();
		}*/
	}

	private String getRemoteParty() {
		final String retValue;

		TextView remoteIdentity = (TextView)findViewById(R.id.remote_identity);
		String userInput =  remoteIdentity.getText().toString();
		if(!TextUtils.isEmpty(userInput)) {
			retValue = userInput;
		} else {
			retValue = null;
		}

		return retValue;
	}

	private final Handler mHandler = new Handler() {
		
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ON_SUBSCR_TERMINATED: {
				Toast.makeText(ListPresenseTestActivity.this, "Subscription Terminated", Toast.LENGTH_SHORT).show();
				break;
			}
			case ON_SUBSCR_STARTED: {
				Toast.makeText(ListPresenseTestActivity.this, "Subscription Started", Toast.LENGTH_SHORT).show();
				break;
			}
			case ON_SUBSCR_START_FAILED: {
				Toast.makeText(ListPresenseTestActivity.this, "Subscription Start Failed", Toast.LENGTH_SHORT).show();
				break;
			}
			case ON_SUBSCR_NOTIFY: {
				Toast.makeText(ListPresenseTestActivity.this, "Subscription Notify", Toast.LENGTH_SHORT).show();	                	              	                
				break;
			}

			default:
				super.handleMessage(msg);
			}
		}
	};

	private SubscriptionListener subscriptionListener = new SubscriptionListener() {

		
		public void subscriptionTerminated(Subscription subscription) {
			mHandler.sendEmptyMessage(ON_SUBSCR_TERMINATED);
		}

		
		public void subscriptionStarted(Subscription subscription) {
			mHandler.sendEmptyMessage(ON_SUBSCR_STARTED);
		}

		
		public void subscriptionStartFailed(Subscription subscription) {
			mHandler.sendEmptyMessage(ON_SUBSCR_START_FAILED);
		}

		
		public void subscriptionNotify(Subscription subscription, Message notify) {
			Log.i("INFO_test", "subscriptionNotify");

			byte buf[] = new byte[100];

			if(notify.getBodyParts().length > 0){
				MessageBodyPart messageBodyPart = notify.getBodyParts()[0];
				InputStream openContentInputStream = messageBodyPart.openContentInputStream();
				StringBuilder out = new StringBuilder();

				try {
					int read = 0;
					while ((read = openContentInputStream.read(buf)) != -1) {
						out.append( new String(buf, 0, read) );
					}

				} catch (IOException e) {
					Log.e("INFO_test", e.getMessage(), e);
				}

				android.os.Message androidMsg = new android.os.Message();
				androidMsg.what = ON_SUBSCR_NOTIFY;
				androidMsg.obj = out.toString();
				mHandler.sendMessage(androidMsg);


				Log.i("INFO_test", "subscriptionNotify 2");
			}
		}
	};

}
