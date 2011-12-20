package javax.microedition.ims.engine.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.core.*;
import javax.microedition.ims.core.media.Media;
import javax.microedition.ims.engine.test.MediaRegistry.MediaBuilder;
import javax.microedition.ims.engine.test.MediaRegistry.MediaBuilderType;
import javax.microedition.ims.engine.test.util.BreakPoint;
import javax.microedition.ims.engine.test.util.ContactsHolder;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ConferenceActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    
    private static final int DIALOG_CHOOSE_CS = 0;
    private static final int DIALOG_CHOOSE_RPARTY1 = 1;
    private static final int DIALOG_CHOOSE_RPARTY2 = 2;

    private AsyncTask<String, Void, Object[]> connectTask;
    private AsyncTask<Session, Void, Boolean> disconnectTask;

    private Session conferenceSession;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTitle(String.format("%1$s, %2$s %3$s", getTitle(),
                getText(R.string.activity_pid), Process.myPid()));
        setContentView(R.layout.conference);

        findViewById(R.id.conf_server_choose_btn).setOnClickListener(
                chooseCSListener);
        findViewById(R.id.conf_party1_choose_btn).setOnClickListener(
                chooseRParty1Listener);
        findViewById(R.id.conf_party2_choose_btn).setOnClickListener(
                chooseRParty2Listener);

        findViewById(R.id.conf_start_stop_btn).setOnClickListener(
                startConferenceListener);
    }

    private View.OnClickListener startConferenceListener = new View.OnClickListener() {
        public void onClick(View view) {
            boolean requestToStartConference = ((ToggleButton) view)
                    .isChecked();
            if (requestToStartConference) {
                try {
                    connect(getConfServer(), getRParty1(), getRParty2());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage(), e);
                    Toast.makeText(ConferenceActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                disconnect();
            }
        };
    };

    private void connect(final String confServer, final String rParty1,
            final String rParty2) {
        if (TextUtils.isEmpty(confServer)) {
            throw new IllegalArgumentException(
                    "The confServer argument is null");
        }

        if (TextUtils.isEmpty(rParty1)) {
            throw new IllegalArgumentException("The rParty1 argument is null");
        }

        if (TextUtils.isEmpty(rParty2)) {
            throw new IllegalArgumentException("The rParty2 argument is null");
        }

        (connectTask = new StartConferenceTask()).execute(confServer, rParty1,
                rParty2);
    }

    private void disconnect() {
        if (conferenceSession != null) {
            (disconnectTask = new StopConferenceTask()).execute(conferenceSession);
        }
    }

    private class StartConferenceTask extends AsyncTask<String, Void, Object[]> {
        
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.conf_start_stop_btn).setEnabled(false);
        }

        protected Object[] doInBackground(String... params) {
            Session sessionACS = null;
            String errorMessage = null;
            try {
                sessionACS = createConference(params[0], params[1], params[2]);
            } catch (ServiceClosedException e) {
                Log.e(TAG, e.getMessage(), e);
                errorMessage = e.getMessage();

            } catch (ImsException e) {
                Log.e(TAG, e.getMessage(), e);
                errorMessage = e.getMessage();
            }

            return new Object[] { sessionACS, errorMessage };
        }

        private Session createConference(final String confServer,
                final String rParty1, final String rParty2)
                throws ImsException, ServiceClosedException {
            final Session retValue;

            // 1) create normal voice-call ( A ---INVITE-- B)
            final BreakPoint callABPoint = new BreakPoint();
            final AtomicBoolean callABResult = new AtomicBoolean(false);
            
            Log.i(TAG, "==> create normal voice-call  ( A ---INVITE-- B) ");
            Session sessionAB = instantiateCall(rParty1+";user=phone", new OperationListener() {
                
                public void operationSuccessed() {
                    callABResult.set(true);
                    callABPoint.notifySynchPoint();
                }
                
                public void operationFailed() {
                    callABResult.set(false);
                    callABPoint.notifySynchPoint();
                }
            });
            
            callABPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);
            
            if(!callABResult.get()) {
                throw new ImsException("==> can't create normal voice-call  ( A ---INVITE-- B) ");
            } else {
                Log.i(TAG, "==> normal voice-call created ( A ---INVITE-- B) ");
            }
            
            // 2) hold the call ( A --- RE-INVITE -- B)
            Log.i(TAG, "==> hold the call ( A --- RE-INVITE -- B)");

            final BreakPoint holdABPoint = new BreakPoint();
            final AtomicBoolean holdABResult = new AtomicBoolean(false);
            
            putCallOnHold(sessionAB, new OperationListener() {
                public void operationSuccessed() {
                    holdABResult.set(true);
                    holdABPoint.notifySynchPoint();
                }
                
                public void operationFailed() {
                    holdABResult.set(false);
                    holdABPoint.notifySynchPoint();
                }
            });
            
            holdABPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);
            
            if(!holdABResult.get()) {
                throw new ImsException("==> cann't hold the call ( A --- RE-INVITE -- B)");
            } else {
                Log.i(TAG, "==> the call holded ( A --- RE-INVITE -- B)");
            }
            

            // 3) another voice-call ( A ---- INVITE-- C )
            Log.i(TAG, "==> 3) another voice-call ( A ---- INVITE-- C )");
            
            final BreakPoint callACPoint = new BreakPoint();
            final AtomicBoolean callACResult = new AtomicBoolean(false);

            Session sessionAC = instantiateCall(rParty2 + ";user=phone", new OperationListener() {
                public void operationSuccessed() {
                    callACResult.set(true);
                    callACPoint.notifySynchPoint();

                }
                
                public void operationFailed() {
                    callACResult.set(false);
                    callACPoint.notifySynchPoint();
                }
            });
            
            callACPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);
            
            if(!callACResult.get()) {
                throw new ImsException("==> can't create normal voice-call  ( A ---INVITE-- C) ");
            } else {
                Log.i(TAG, "==> normal voice-call created ( A ---INVITE-- C) ");
            }


            // 4) and 5) are running in parallel
            //final CyclicBarrier confStartedBarrier = new CyclicBarrier(3);
            final CountDownLatch confStartedLatch = new CountDownLatch(2);
            //final BreakPoint holdACPoint = new BreakPoint();

            // 4) hold this call too ( A ---RE-INVITE--C )
            Log.i(TAG, "==> 4) hold this call too ( A ---RE-INVITE--C )");
            
            final AtomicBoolean holdACResult = new AtomicBoolean(false);
            putCallOnHold(sessionAC,  new OperationListener() {
                public void operationSuccessed() {
                    holdACResult.set(true);
                    //awaitBarrier(confStartedBarrier);
                    //TODO for test
/*                    try {
						Thread.sleep(5000l);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
*/                    confStartedLatch.countDown();
                        //holdACPoint.notifySynchPoint();
                }

                public void operationFailed() {
                    holdACResult.set(false);
                    //awaitBarrier(confStartedBarrier);
                    confStartedLatch.countDown();
                    //holdACPoint.notifySynchPoint();
                }
            });
            //holdACPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);

            // 5) call Conf-server ( A --- INVITE -- Conf)
            Log.i(TAG, "==> 5) call Conf-server ( A --- INVITE -- Conf)");
            final AtomicBoolean callConfResult = new AtomicBoolean(false);
            //final BreakPoint callConfPoint = new BreakPoint();

            Session sessionACS = instantiateCall(confServer + ";user=phone", new OperationListener() {
                public void operationSuccessed() {
                    callConfResult.set(true);
                    //awaitBarrier(confStartedBarrier);
                    confStartedLatch.countDown();
                    //callConfPoint.notifySynchPoint();
                };
                
                public void operationFailed() {
                    callConfResult.set(false);
                    //awaitBarrier(confStartedBarrier);
                    confStartedLatch.countDown();
                    //callConfPoint.notifySynchPoint();
                }
            });
            
            //awaitBarrier(confStartedBarrier);
            awaitCountDownLatch(confStartedLatch);
            //callConfPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);
            
            if(!holdACResult.get()) {
                throw new ImsException("==> 4) can't hold this call too ( A ---RE-INVITE--C )");
            } else {
                Log.i(TAG, "==> 4) this call too holded ( A ---RE-INVITE--C )");
            }
            
            if(!callConfResult.get()) {
                throw new ImsException("==> 5) can't call Conf-server ( A --- INVITE -- Conf)");
            } else {
                Log.i(TAG, "==> 5) Conf-server call created( A --- INVITE -- Conf)");
            }

            // 6), 7), 8) and 9) are running in parallel
            //final CyclicBarrier referBarrier = new CyclicBarrier(3);
            final CountDownLatch referLatch = new CountDownLatch(2);
            
            // 6) Refer to Conf-Server ( A--- REFER--B )
            // 7) receive NOTIFYs
            Log.i(TAG, "==> 6) Refer to Conf-Server ( A--- REFER--B )");
            final AtomicBoolean referABResult = new AtomicBoolean(false);
            referCallToConfServer(sessionAB, confServer, new OperationListener() {
                public void operationSuccessed() {
                    referABResult.set(true);
                    //awaitBarrier(referBarrier);
                    referLatch.countDown();
                }

                public void operationFailed() {
                    referABResult.set(false);
                    //awaitBarrier(referBarrier);
                    referLatch.countDown();
                }
            });

            // 8) Refer to Conf-server ( A ---REFER--C )
            // 9) receive NOTIFYs
            Log.i(TAG, "==> 8) Refer to Conf-server ( A ---REFER--C )");
            final AtomicBoolean referACResult = new AtomicBoolean(false);
            referCallToConfServer(sessionAC, confServer, new OperationListener() {
                public void operationSuccessed() {
                    referACResult.set(true);
                    //awaitBarrier(referBarrier);
                    referLatch.countDown();
                }
                
                public void operationFailed() {
                    referACResult.set(false);
                    //awaitBarrier(referBarrier);
                    referLatch.countDown();
                }
            });
            
            //awaitBarrier(referBarrier);
            awaitCountDownLatch(referLatch);
            
            if(!referABResult.get()) {
                throw new ImsException("==> 6) can't refer to Conf-Server ( A--- REFER--B )");
            } else {
                Log.i(TAG, "==> 6) B refered to Conf-Server ( A--- REFER--B )");
            }
            
            if(!referACResult.get()) {
                throw new ImsException("==> 8) can't refer to Conf-Server ( A--- REFER--C )");
            } else {
                Log.i(TAG, "==> 8) C refered to Conf-Server ( A--- REFER--C )");
            }

            // 10) BYE B and C from the original calls
            Log.i(TAG, "==> 10) BYE  B from the original calls");
            terminateCall(sessionAB);
            Log.i(TAG, "==> 11) BYE  C from the original calls");
            terminateCall(sessionAC);

            retValue = sessionACS;

            return retValue;
        }
        
        private void awaitBarrier(final CyclicBarrier referBarrier) {
            try {
                referBarrier.await();
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (BrokenBarrierException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        
        private void awaitCountDownLatch(final CountDownLatch countDownLatch) {
            try {
            	countDownLatch.await();
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage(), e);
            } 
        }
        

        private Session instantiateCall(final String remoteParty, final OperationListener operationListener)
                throws ImsException, ServiceClosedException {
            final Session retValue;

            //final BreakPoint breakPoint = new BreakPoint();
            //final AtomicReference<Boolean> callResult = new AtomicReference<Boolean>();

            CoreService coreService = AppContext.instance.getConnection();
            //DtfmPayload dtfmPayload = SettingsHelper.extractDtfmPayload(ConferenceActivity.this);
            final Session session = coreService
                    .createSession(null, remoteParty/*, dtfmPayload*/);
            session.setListener(new SessionAdapter() {
                
                public void sessionStarted(Session session) {
                    Log.i(TAG, "sessionStarted#     remoteParty=" + remoteParty);
                    //callResult.compareAndSet(null, true);
                    //breakPoint.notifySynchPoint();
                    operationListener.operationSuccessed();
                }

                
                public void sessionStartFailed(Session session) {
                    Log.i(TAG, "sessionStartFailed#     remoteParty=" + remoteParty);
                    //callResult.compareAndSet(null, false);
                    //breakPoint.notifySynchPoint();
                    operationListener.operationFailed();
                }
            });
            
            addInitialMedia(MediaBuilderType.StreamMediaAudio, session);
            
            session.start();
            
            //breakPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);

            //if (callResult.get() == null || callResult.get() == Boolean.FALSE) {
            //    throw new ImsException("Cann't instatntiate call to "
            //            + remoteParty);
            //}

            retValue = session;

            return retValue;
        }
        
        private void addInitialMedia(final MediaBuilderType builderType, final Session session) {
            MediaBuilder mediaBuilder = MediaRegistry.INSTANCE.findBuilder(builderType);
            mediaBuilder.addMedia(session);
        }

        private void putCallOnHold(final Session session, final OperationListener operationListener) throws ImsException {
            //final BreakPoint breakPoint = new BreakPoint();
            //final AtomicReference<Boolean> holdResult = new AtomicReference<Boolean>();

            for (Media media : session.getMedia()) {
                media.setDirection(Media.DIRECTION_SEND);
            }

            session.setListener(new SessionAdapter() {
                
                public void sessionUpdated(Session session) {
                    Log.i(TAG, "sessionUpdated#   toOnHold RemoteUserId=" + session.getRemoteUserId()[0]);
                    //holdResult.compareAndSet(null, true);
                    //breakPoint.notifySynchPoint();
                    operationListener.operationSuccessed();
                }

                
                public void sessionUpdateFailed(Session session) {
                    Log.i(TAG, "sessionUpdateFailed#   toOnHold RemoteUserId=" + session.getRemoteUserId()[0]);
                    //holdResult.compareAndSet(null, false);
                    //breakPoint.notifySynchPoint();
                    operationListener.operationFailed();
                }
            });

            session.update();
            
            //breakPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);      
            //if (holdResult.get() == null || holdResult.get() == Boolean.FALSE) {
            //    throw new ImsException("Can't place call on hold");  
            //} 
        }

        private Reference referCallToConfServer(final Session session,
                String confServer, final OperationListener operationListener) throws ImsException, ServiceClosedException {
            final Reference retValue;

            //final BreakPoint breakPoint = new BreakPoint();
            //final AtomicReference<Boolean> referResult = new AtomicReference<Boolean>();

            Reference reference = session.createReference(confServer, "invite");
            reference.setListener(new ReferenceAdapter() {
                
                public void referenceDelivered(Reference reference) {
                    Log.i(TAG, "referenceDelivered#   RemoteUserId=" + session.getRemoteUserId()[0]);
                }

                
                public void referenceDeliveryFailed(Reference reference) {
                    Log.i(TAG, "referenceDeliveryFailed#   RemoteUserId=" + session.getRemoteUserId()[0]);
                    operationListener.operationFailed();
                    //referResult.compareAndSet(null, false);
                    //breakPoint.notifySynchPoint();

                }

                
                public void referenceNotify(Reference reference, Message notify) {
                    Log.i(TAG, "referenceNotify#   RemoteUserId=" + session.getRemoteUserId()[0]);
                    operationListener.operationSuccessed();
                    //referResult.compareAndSet(null, true);
                    //breakPoint.notifySynchPoint();
                }
            });
            reference.refer(true);
            //breakPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);

            //if (referResult.get() == null || referResult.get() == Boolean.FALSE) {
            //    throw new ImsException("Cannot send REFER for session = "
            //            + session);
            //}

            retValue = reference;

            return retValue;
        }

        private void terminateCall(final Session session) throws ImsException,
                ServiceClosedException {
            final BreakPoint breakPoint = new BreakPoint();
            final AtomicReference<Boolean> terminateResult = new AtomicReference<Boolean>();

            session.setListener(new SessionAdapter() {
                
                public void sessionTerminated(Session session) {
                    Log.i(TAG, "sessionTerminated#   RemoteUserId=" + session.getRemoteUserId()[0]);
                    terminateResult.compareAndSet(null, true);
                    breakPoint.notifySynchPoint();
                }
            });

            session.terminate();

            breakPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);

            if (terminateResult.get() == null
                    || terminateResult.get() == Boolean.FALSE) {
                throw new ImsException("Cann't terminate call, session = "
                        + session);
            }
        }

        protected void onPostExecute(final Object[] results) {
            setProgressBarIndeterminateVisibility(false);
            ToggleButton confControl = (ToggleButton) findViewById(R.id.conf_start_stop_btn);
            confControl.setEnabled(true);
            
            Session confSession = (Session) results[0];
            String errorMessage = (String) results[1];
            
            confControl.setChecked(confSession != null && errorMessage == null);
            
            if (confSession != null) {
                setConferenceSession(confSession);
                
                Toast.makeText(ConferenceActivity.this,
                        "Session was created successfully", Toast.LENGTH_LONG)
                        .show();
                findViewById(R.id.conf_start_stop_btn).setEnabled(true);
            }

            if (errorMessage != null) {
                Toast.makeText(ConferenceActivity.this, errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    interface OperationListener {
        void operationSuccessed();
        void operationFailed();
    }


    private class StopConferenceTask extends AsyncTask<Session, Void, Boolean> {
        
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.conf_start_stop_btn).setEnabled(false);
        }

        
        protected Boolean doInBackground(Session... params) {
            Boolean result = Boolean.TRUE;
            try {
                terminateConference(params[0]);
            } catch (ImsException e) {
                result = Boolean.FALSE;
            }

            return result;
        }

        private void terminateConference(Session session) throws ImsException {
            final BreakPoint breakPoint = new BreakPoint();
            final AtomicReference<Boolean> terminateResult = new AtomicReference<Boolean>();

            session.setListener(new SessionAdapter() {
                
                public void sessionTerminated(Session session) {
                    Log.i(TAG, "sessionTerminated#");
                    terminateResult.compareAndSet(null, true);
                    breakPoint.notifySynchPoint();
                }
            });

            session.terminate();

            breakPoint.waitNotificationOrTimeout(60, TimeUnit.SECONDS);

            if (terminateResult.get() == null
                    || terminateResult.get() == Boolean.FALSE) {
                throw new ImsException("Cann't terminate call, session = "
                        + session);
            }
        }

        
        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            ToggleButton confControl = (ToggleButton) findViewById(R.id.conf_start_stop_btn);
            confControl.setEnabled(true);
            
            confControl.setChecked(result == Boolean.FALSE);
            
            Toast
                    .makeText(
                            ConferenceActivity.this,
                            result == Boolean.TRUE ? "Session was terminated successfully"
                                    : "Session was terminated unsuccessfully",
                            Toast.LENGTH_LONG).show();
        }
    }

    private View.OnClickListener chooseCSListener = new View.OnClickListener() {
        public void onClick(View view) {
            showDialog(DIALOG_CHOOSE_CS);
        };
    };

    private View.OnClickListener chooseRParty1Listener = new View.OnClickListener() {
        public void onClick(View view) {
            showDialog(DIALOG_CHOOSE_RPARTY1);
        };
    };

    private View.OnClickListener chooseRParty2Listener = new View.OnClickListener() {
        public void onClick(View view) {
            showDialog(DIALOG_CHOOSE_RPARTY2);
        };
    };

    private void setConfServer(String confServer) {
        TextView textView = (TextView) findViewById(R.id.conference_server_name);
        textView.setText(confServer);
    }

    private void setRParty1(String rParty1) {
        TextView textView = (TextView) findViewById(R.id.conference_party1);
        textView.setText(rParty1);
    }

    private void setRParty2(String rParty2) {
        TextView textView = (TextView) findViewById(R.id.conference_party2);
        textView.setText(rParty2);
    }

    private String getConfServer() {
        TextView textView = (TextView) findViewById(R.id.conference_server_name);
        return textView.getText().toString();
    }

    private String getRParty1() {
        TextView textView = (TextView) findViewById(R.id.conference_party1);
        return textView.getText().toString();
    }

    private String getRParty2() {
        TextView textView = (TextView) findViewById(R.id.conference_party2);
        return textView.getText().toString();
    }

    private void setConferenceSession(Session session) {
        this.conferenceSession = session;
    }

    
    protected Dialog onCreateDialog(int id) {
        final Dialog retValue;

        switch (id) {
        case DIALOG_CHOOSE_CS: {
            String[] elements = getResources().getStringArray(R.array.conference_server_list);
            retValue = createDialog(elements,
                    new ItemSelectListener() {
                        
                        public void itemSelected(String item) {
                            setConfServer(item);
                        }
                    });
            break;
        }
        case DIALOG_CHOOSE_RPARTY1: {
            String[] contacts = ContactsHolder.getContacts(this);
            retValue = createDialog(contacts,
                    new ItemSelectListener() {
                        
                        public void itemSelected(String item) {
                            setRParty1(item);
                        }
                    });
            break;

        }
        case DIALOG_CHOOSE_RPARTY2: {
            String[] contacts = ContactsHolder.getContacts(this);
            retValue = createDialog(contacts,
                    new ItemSelectListener() {
                        
                        public void itemSelected(String item) {
                            setRParty2(item);
                        }
                    });
            break;
        }
        default: {
            retValue = super.onCreateDialog(id);
        }
        }

        return retValue;
    }

    private Dialog createDialog(String[] elements, final ItemSelectListener listener) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, elements);

        return new AlertDialog.Builder(this).setTitle(
                R.string.conf_server_list_title).setAdapter(adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String dataItem = adapter.getItem(which);
                        // String[] items = getResources().getStringArray(
                        // R.array.remote_parties);
                        // setConferenceServer(items[which]);
                        listener.itemSelected(dataItem);
                    }
                }).create();
    }

    interface ItemSelectListener {
        void itemSelected(String item);
    }

    
    protected void onDestroy() {
        super.onDestroy();

        if (connectTask != null) {
            connectTask.cancel(true);
        }

        if (disconnectTask != null) {
            disconnectTask.cancel(true);
        }

        Log.i(TAG, "onDestroy");
    }
}
