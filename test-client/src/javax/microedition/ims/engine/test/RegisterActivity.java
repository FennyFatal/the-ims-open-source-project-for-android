package javax.microedition.ims.engine.test;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.*;
import javax.microedition.ims.core.CoreService;
import javax.microedition.ims.core.media.Media;

public class RegisterActivity extends BaseActivity {
    private static final String CLIENT_NAME = "clientName";
    private static final String CLIENT_IDENTITY = "clientIdentity";

    private final String TAG = getClass().getSimpleName();
    private AsyncTask<String, Void, Object[]> connectTask;
    private AsyncTask<Object, Void, Void> disconnectTask;

    /**
     * Called when the activity is first created.
     */
    
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTitle(String.format("%1$s, %2$s %3$s", getTitle(),
                getText(R.string.activity_pid), Process.myPid()));
        setContentView(R.layout.register);

        // restore previously stored data
        String clientName = (savedInstanceState == null ? null
                : savedInstanceState.getString(CLIENT_NAME));
        String clientIdentity = (savedInstanceState == null ? null
                : savedInstanceState.getString(CLIENT_IDENTITY));

/*
        TextView clientNameEdit = (TextView) findViewById(R.id.client_app_name);
        clientNameEdit.setText(clientName == null ? getResources().getText(
                R.id.def_app_id) : clientName);

        TextView clientIdentityEdit = (TextView) findViewById(R.id.client_identity);
        clientIdentityEdit.setText(clientIdentity == null ? getResources()
                .getText(R.id.def_registration_name) : clientIdentity);
*/

        findViewById(R.id.connect_btn).setOnClickListener(connectListener);
        findViewById(R.id.go_invite_btn).setOnClickListener(goToInviteListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        //findViewById(R.id.client_app_name).setVisibility(View.GONE);
    }

    
    protected void onStart() {
        super.onStart();
        updateConnectionState(AppContext.instance.getConnectionState());
    }

    
    protected void onDestroy() {
        super.onDestroy();

        if (connectTask != null) {
            connectTask.cancel(true);
        }

        if (disconnectTask != null) {
            disconnectTask.cancel(true);
        }

        AppContext.instance.free(this);

        Log.i(TAG, "onDestroy");
        // System.exit(0);
    }

    private OnClickListener connectListener = new OnClickListener() {
        public void onClick(View v) {
            if (AppContext.instance.getConnection() == null) {
                /*
                String clientName = ((TextView) findViewById(R.id.client_app_name))
                        .getText().toString();
                String clientIdentity = ((TextView) findViewById(R.id.client_identity))
                        .getText().toString();
                */
                String clientName = getResources().getText(R.id.def_app_id).toString();
                String clientIdentity = getResources().getText(R.id.def_registration_name).toString();
                connect(clientName, clientIdentity);
            } else {
                disconnect();
            }
        }
    };

    private OnClickListener goToInviteListener = new OnClickListener() {
        public void onClick(View v) {
            goToInviteScreen();
        }
    };

    private void goToInviteScreen() {
        startActivity(new Intent(this, InviteActivity.class));
    }

    private void connect(final String clientName, final String clientIdentity) {
        if (!TextUtils.isEmpty(clientName)
                && !TextUtils.isEmpty(clientIdentity)) {
            (connectTask = new ConnectTask()).execute(clientName,
                    clientIdentity);
        } else {
            Toast.makeText(this, R.string.wrong_argument_msg,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void disconnect() {
        if (AppContext.instance.getConnection() != null) {
            (disconnectTask = new DisconnectTask()).execute(
                    AppContext.instance.getConnection(),
                    AppContext.instance.getConfiguration(),
                    AppContext.instance.getConnectionState());
        }
    }

    
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        /*
        TextView clientNameEdit = (TextView) findViewById(R.id.client_app_name);
        TextView clientIdentityEdit = (TextView) findViewById(R.id.client_identity);

        outState.putString(CLIENT_NAME, clientNameEdit.getText().toString());
        outState.putString(CLIENT_IDENTITY, clientIdentityEdit.getText()
                .toString());
        */
        super.onSaveInstanceState(outState);
    }

    private class ConnectTask extends AsyncTask<String, Void, Object[]> {
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.connect_btn).setEnabled(false);
        }

        protected Object[] doInBackground(String... params) {

            Configuration configuration = null;
            ConnectionState mConnectionState = null;
            CoreService mConnection = null;
            String errorMessage = null;
            String reasonData = null;

            Integer errorCode = ImsException.UNSPECIFIED_ERROR;
            try {
                configuration = openConfiguration();

                mConnectionState = openConnectionState();
                AppContext.instance.setConnectionState(mConnectionState);

                mConnection = openCoreService(params[0], params[1]);
                Log.i(TAG, "Core service opened");
            } catch (ImsException e) {
                Log.e(TAG,
                        "Cann't resolve core service, message: "
                                + e.getMessage());
                errorCode = e.getCode();
                errorMessage = e.getMessage();
                reasonData = e.getReasonData();
            }


            return new Object[] {configuration, mConnection, errorMessage,
                    errorCode, reasonData, mConnectionState };
        }

        private Configuration openConfiguration() throws ImsException{
            final Configuration configuration;

            final Registry registry = new Registry(AppContext.REGISTRY,
                    new String[0], Registry.QOS_LEVEL_STREAMING);
            final String appId = RegisterActivity.this.getResources()
                    .getText(R.id.def_app_id).toString();

            configuration = Configuration
                    .getConfiguration(RegisterActivity.this);

            configuration.setRegistry(appId, "", registry, null);

            return configuration;
        }

        private CoreService openCoreService(final String clientName,
                final String clientIdentity) throws ImsException {
            Log.i(TAG, "Getting core service");
            String name = String.format("%1$s://%2$s;%3$s", "imscore",
                    clientName, clientIdentity);
            CoreService coreService = (CoreService) Connector.open(name,
                    RegisterActivity.this);

            return coreService;
        }

        private ConnectionState openConnectionState() throws ImsException{
            Log.i(TAG, "Getting connection state manager");
            ConnectionState connectionState = ConnectionState
                    .getConnectionState(RegisterActivity.this);
            return connectionState;
        }

        protected void onPostExecute(final Object[] results) {
            setProgressBarIndeterminateVisibility(false);

            Button connectBtn = (Button) findViewById(R.id.connect_btn);
            connectBtn.setEnabled(true);

            final Configuration configuration = (Configuration) results[0];
            final CoreService coreService = (CoreService) results[1];
            final String errorMessage = (String) results[2];
            final int errorCode = (Integer) results[3];
            final String reasonData = (String) results[4];
            final ConnectionState connectionState = (ConnectionState) results[5];

            //handle case when errorCode == ImsException.DNS_LOOKUP_FAILURE_ERROR 
            
            try {
                updateConfiguration(configuration);
                updateConnectionState(connectionState);
                updateCoreService(coreService, errorMessage, errorCode, reasonData);

                connectBtn.setText(R.string.reg_disconnect_btn);
                findViewById(R.id.go_invite_btn).setVisibility(View.VISIBLE);

                goToInviteScreen();
            } catch (IllegalStateException e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

        private void updateConfiguration(final Configuration conf) {
/*            if (conf == null) {
                throw new IllegalStateException(
                        "Cann't pass client configuration to stack.");
            }
*/
            AppContext.instance.setConfiguration(conf);
        }

        private void updateConnectionState(ConnectionState connectionState) {
/*            if (connectionState == null) {
                throw new IllegalStateException(
                        "Cann't connect to connection manager");
            }
*/
            RegisterActivity.this.updateConnectionState(connectionState);
        }

        private void updateCoreService(final CoreService coreService,
                String errorMessage, int errorCode, String reasonData) {
            if (coreService == null) {
                throw new IllegalStateException(
                        "Cann't open core service, code = " + errorCode + ", message = " + errorMessage + ", reasonData = " + reasonData);
            }

            AppContext.instance.setConnection(coreService);
        }
    }

    private class DisconnectTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.connect_btn).setEnabled(false);
        }

        
        protected Void doInBackground(Object... params) {
            closeCoreService((CoreService) params[0]);
            closeConfiguration((Configuration) params[1]);
            closeConnectionState((ConnectionState) params[2]);
            return null;
        }

        private void closeCoreService(final CoreService coreService) {
            Log.i(TAG, "Closing core service");
            assert coreService != null;
            coreService.close();
            Log.i(TAG, "Core service closed");
        }
        
        private void closeConfiguration(final Configuration configuration) {
            Log.i(TAG, "Closing configuration");
            assert configuration != null;
            configuration.close();
            Log.i(TAG, "Configuration closed");
        }

        private void closeConnectionState(final ConnectionState connectionState) {
            Log.i(TAG, "Closing connection state service");
            assert connectionState != null;
            connectionState.close();
            Log.i(TAG, "Connection state service closed");
        }

        protected void onPostExecute(Void result) {
            setProgressBarIndeterminateVisibility(false);

            Button connectBtn = (Button) findViewById(R.id.connect_btn);
            connectBtn.setEnabled(true);
            
            connectBtn.setText(R.string.reg_connect_btn);
            findViewById(R.id.go_invite_btn).setVisibility(View.INVISIBLE);

            AppContext.instance.setConnection(null);
            AppContext.instance.setConfiguration(null);
            AppContext.instance.setConnectionState(null);
            
            updateConnectionState(null);
        }
    }

    
    public void mediaUpdated(Media[] medias) {
        Log.e(TAG, "mediaUpdated#unhandled");
    }
}
