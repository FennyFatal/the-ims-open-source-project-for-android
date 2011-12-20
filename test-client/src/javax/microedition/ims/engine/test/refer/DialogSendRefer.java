package javax.microedition.ims.engine.test.refer;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.*;
import javax.microedition.ims.engine.test.AppContext;
import javax.microedition.ims.engine.test.InviteActivity;
import javax.microedition.ims.engine.test.R;
import javax.microedition.ims.engine.test.util.ContactsHolder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DialogSendRefer extends Dialog {
    protected final static String TAG = "DialogSendRefer";

    public static final int DIALOG_ID = 10;


    private static final int MSG_REFERENCE_TERMINATED = 1;
    private static final int MSG_REFERENCE_NOTIFY = 2;
    private static final int MSG_REFERENCE_DELIVERY_FAILED = 3;
    private static final int MSG_REFERENCE_DELIVERED = 4;

    private static final int MSG_SESSION_UPDATED_TO_ON_HOLD = 5;


    private boolean toOnHold = false;

    private static final String REFER_TO_IDENTITY = "referToIdentity";

    private InviteActivity inviteActivity;

    private final Handler mHandler = new Handler() {
        
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MSG_REFERENCE_TERMINATED: {
                DlgParams dlgParams = AppContext.instance.getDialogSendReferParams();

                Log.i("YYY", "MSG_REFERENCE_TERMINATED");
                Toast.makeText(inviteActivity, "MSG_REFERENCE_TERMINATED", Toast.LENGTH_SHORT).show();

                if (dlgParams.getCurrentStartedSession() != null) {
                    inviteActivity.terminateCall(true, true, dlgParams.getCurrentStartedSession());
                }

                break;
            }
            case MSG_REFERENCE_NOTIFY: {
                Message notify = (Message)msg.obj;
                String body = "";
                try {
                    InputStream inputStream = notify.getBodyParts()[0].openContentInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    char[] buf = new char[100];
                    int size = inputStreamReader.read(buf);
                    size = size < 99 ? size : 99;
                    buf[size]='\n';
                    body = new StringBuffer().append(buf, 0, size).toString();
                } catch (IOException e) {
                    Log.i("INFO_test", e.getMessage());
                }
                Toast.makeText(inviteActivity, "Notify : " + body, Toast.LENGTH_SHORT).show();
                Log.i("YYY", "Notify : " + body);

                //TODO check this
                //                    if (!body.contains("100") && !body.contains("180")) {
                //                        inviteActivity.terminateCall(true);
                //                    }

                break;
            }
            case MSG_REFERENCE_DELIVERY_FAILED: {
                Log.i("YYY", "MSG_REFERENCE_DELIVERY_FAILED");
                inviteActivity.dismissDialog(DialogSendReferProgress.DIALOG_ID);
                Toast.makeText(inviteActivity, "Reference delivery FAILED!", Toast.LENGTH_SHORT).show();
                break;
            }
            case MSG_REFERENCE_DELIVERED: {
                Log.i("YYY", "MSG_REFERENCE_DELIVERED");
                inviteActivity.dismissDialog(DialogSendReferProgress.DIALOG_ID);
                Toast.makeText(inviteActivity, "Reference delivered", Toast.LENGTH_SHORT).show();
                break;
            }

            case MSG_SESSION_UPDATED_TO_ON_HOLD: {

                Toast.makeText(inviteActivity, "Session updated to On Hold", Toast.LENGTH_SHORT).show();

                switchDialogsAndRunCallTransferTask();

                break;
            }

            default: {
                super.handleMessage(msg);
                break;
            }
            }
        }
    };

    private DialogSendRefer(InviteActivity inviteActivity) {
        super(inviteActivity);
        this.inviteActivity = inviteActivity;
    }

    public static void showDialog(DlgParams dlgParams, Activity activity) {
        AppContext.instance.setDialogSendReferParams(dlgParams);
        activity.showDialog(DialogSendRefer.DIALOG_ID);
    }

    public static DialogSendRefer createDialog(final InviteActivity inviteActivity) {

        final DialogSendRefer dialog = new DialogSendRefer(
                inviteActivity
        );

        AppContext.instance.setDialogSendRefer(dialog);

        dialog.setContentView(R.layout.dialog_refer_send);
        dialog.setTitle(R.string.send_refer_title);
        
        
        Spinner identityControl = (Spinner) dialog.findViewById(R.id.send_refer_remote_users);
/*        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                inviteActivity, R.array.remote_parties, android.R.layout.simple_spinner_item);
*/
        String[] remoteIdentities = ContactsHolder.getContacts(inviteActivity);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(inviteActivity, android.R.layout.simple_spinner_item, remoteIdentities);

        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        identityControl.setAdapter(adapter);
        

        dialog.findViewById(R.id.btn_close).setOnClickListener(
                new View.OnClickListener() {
                    
                    public void onClick(View v) {
                        inviteActivity.dismissDialog(DIALOG_ID);
                    }
                }
        );

        dialog.findViewById(R.id.btn_send_refer).setOnClickListener(
                new View.OnClickListener() {
                    
                    public void onClick(View v) {
                        dialog.onCallTransfer();
                    }
                }
        );

        return dialog;
    }

    private void onCallTransfer() {
//        DlgParams dlgParams = AppContext.instance.getDialogSendReferParams();
//        
//        if(dlgParams.isReferSendingFromSession()) {
//            toOnHold = true;
//            enableDisableButtons(false);
//            currentSessionOnHold();
//        } else {
//            switchDialogsAndRunCallTransferTask();
//        }
        switchDialogsAndRunCallTransferTask();
    }

    private void switchDialogsAndRunCallTransferTask() {
        DlgParams dlgParams = AppContext.instance.getDialogSendReferParams();

        inviteActivity.dismissDialog(DIALOG_ID);
        
        enableDisableButtons(true);

        DialogSendReferProgress.showDialog(
                new DialogSendReferProgress.DlgParams(
                        dlgParams.getTo(),
                        getReferToParty()
                ),
                inviteActivity
        );

        CallTransferExecutionContext context = new CallTransferExecutionContext(
                dlgParams.getCurrentStartedSession(), //currentStartedSession
                AppContext.instance.getConnection(), //coreService,
                dlgParams.getTo(), //to
                getReferToParty(), //referToParty,
                "INVITE" //referMethod
        );
        new CallTransferTask().execute(context);
    }

    private void enableDisableButtons(boolean enableButtons) {
        findViewById(R.id.btn_close).setEnabled(enableButtons);
        findViewById(R.id.btn_send_refer).setEnabled(enableButtons);
    }

    private TextView getReferToEdit() {
        return (TextView) findViewById(R.id.edit_remote_user);
    }

    private String getReferToParty() {
        final String retValue;
        
        String referToEditValue = getReferToEdit().getText().toString();
        if(!TextUtils.isEmpty(referToEditValue)) {
            retValue = referToEditValue;
        } else {
            Spinner remoteIdentities = (Spinner) findViewById(R.id.send_refer_remote_users);
            int pos = remoteIdentities.getSelectedItemPosition();
            //retValue = inviteActivity.getResources().getStringArray(R.array.remote_parties)[pos];
            retValue = ContactsHolder.getContacts(inviteActivity)[pos];
        }
        
        return retValue;
    }

    
    public Bundle onSaveInstanceState() {
        Bundle bundle = super.onSaveInstanceState();

        bundle.putString(REFER_TO_IDENTITY, getReferToParty());

        return bundle;
    }

    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //restore previously stored data
//        String referToIdentity = savedInstanceState == null ? null : savedInstanceState.getString(REFER_TO_IDENTITY);
//
//        TextView referToEdit = getReferToEdit();
//        referToEdit.setText(referToIdentity == null ? inviteActivity.getResources().getText(R.id.def_refer_to_party_name) : referToIdentity);
    }

    public void onSessionUpdated() {
        mHandler.sendEmptyMessage(MSG_SESSION_UPDATED_TO_ON_HOLD);
        toOnHold = false;
    }


    public static class DlgParams {
        private Session currentStartedSession;
        private String to;

        public DlgParams(Session currentStartedSession, String to) {
            this.currentStartedSession = currentStartedSession;
            this.to = to;
        }

        public Session getCurrentStartedSession() {
            return currentStartedSession;
        }

        public void setCurrentStartedSession(Session currentStartedSession) {
            this.currentStartedSession = currentStartedSession;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }


    private class CallTransferTask extends AsyncTask<CallTransferExecutionContext, Void, Boolean> {
        
        protected void onPreExecute() {

            super.onPreExecute();
        }

        
        protected Boolean doInBackground(CallTransferExecutionContext... params) {
            CallTransferExecutionContext context = params[0];

            Boolean result = Boolean.FALSE;
            try {
                Reference reference = null;
                if (context.getCurrentStartedSession() != null) {
                    Log.i(TAG, "!!!!!!! context.getSession().getState() : " + context.getCurrentStartedSession().getState());
                    reference = context.getCurrentStartedSession().createReference(context.getReferToParty(), context.getReferMethod());
                } else {
                    reference = context.getCoreService().createReference("from", context.getTo(), context.getReferToParty(), context.getReferMethod());
                }

                reference.setListener(new ReferenceListener() {
                    
                    public void referenceTerminated(Reference reference) {
                        mHandler.sendEmptyMessage(MSG_REFERENCE_TERMINATED);
                    }

                    
                    public void referenceNotify(Reference reference, Message notify) {
                        android.os.Message message = new android.os.Message();
                        message.what = MSG_REFERENCE_NOTIFY;
                        message.obj = notify;
                        mHandler.sendMessage(message);
                    }

                    
                    public void referenceDeliveryFailed(Reference reference) {
                        mHandler.sendEmptyMessage(MSG_REFERENCE_DELIVERY_FAILED);
                    }

                    
                    public void referenceDelivered(Reference reference) {
                        mHandler.sendEmptyMessage(MSG_REFERENCE_DELIVERED);
                    }
                });

                reference.refer(true);

                result = Boolean.TRUE;

            } catch (ImsException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (ServiceClosedException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return result;
        }

        
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);
        }
    }


    private class CallTransferExecutionContext {
        private Session currentStartedSession;
        private CoreService coreService;
        private String to;
        private String referToParty;
        private String referMethod;

        public CallTransferExecutionContext(Session currentStartedSession,
                CoreService coreService, String to,
                String referToParty, String referMethod) {
            this.currentStartedSession = currentStartedSession;
            this.coreService = coreService;
            this.to = to;
            this.referToParty = referToParty;
            this.referMethod = referMethod;
        }
        
        public Session getCurrentStartedSession() {
            return currentStartedSession;
        }

        public CoreService getCoreService() {
            return coreService;
        }

        public String getReferToParty() {
            return referToParty;
        }

        public String getReferMethod() {
            return referMethod;
        }

        public String getTo() {
            return to;
        }
    };

}
