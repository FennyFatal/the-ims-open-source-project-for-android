package javax.microedition.ims.engine.test;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import org.w3c.dom.Document;

import javax.microedition.ims.Connector;
import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.presence.*;
import javax.microedition.ims.xdm.*;
import java.io.IOException;
import java.util.*;

public class ContListActivity extends Activity {
    private final static String TAG = "ContListActivity";

    private XDMService xdmService;
    private PresenceService presenceService;

    private XDMDataProvider dataProvider = new XDMDataProvider();

    private Watcher currentWatcher;

    private Map<String, ContListActivity.ContactItem> presenseData = new HashMap<String, ContListActivity.ContactItem>();

    private AsyncTask<String, Void, LoadListResult> loadListTask;
    private AsyncTask<String, Void, Exception> subscribeTask;
    private AsyncTask<Void, Void, IOException> unsubscribeTask;

    private static final int RELOAD_LIST = 1;
    private static final int SUBSCRIPTION_STARTED = 2;
    private static final int SUBSCRIPTION_TERMINATED = 3;
    private static final int SUBSCRIPTION_FAILED = 4;
    

    /*
     * interface Config { String getListForSubscription(); String
     * getContactListDocumentName(); String getContactListName(); }
     */
    /*
     * class ColibriaConfig implements Config {
     * 
     * @Override public String getListForSubscription() { return
     * "sip:movial11@dummy.com;pres-list=rcs"; }
     * 
     * @Override public String getContactListDocumentName() { return "index"; }
     * 
     * @Override public String getContactListName() { return "phbk"; } }
     * 
     * class TmobileConfig implements Config {
     * 
     * @Override public String getListForSubscription() { return
     * "list=phbk"; }
     * 
     * @Override public String getContactListDocumentName() { return "phbk"; }
     * 
     * @Override public String getContactListName() { return "phbk"; } }
     */

    private final Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case RELOAD_LIST: {
                Log.i(TAG, "handleMessage#RELOAD_LIST");
                reloadList();
                break;
            }

            case SUBSCRIPTION_STARTED: {
                ((TextView) findViewById(R.id.cont_list_status))
                        .setText("Status: subscription started");
                break;
            }

            case SUBSCRIPTION_TERMINATED: {
                ((TextView) findViewById(R.id.cont_list_status))
                        .setText("Status: subscription terminated");
                break;
            }

            case SUBSCRIPTION_FAILED: {
                ((TextView) findViewById(R.id.cont_list_status))
                        .setText("Status: subscription failed");
                break;
            }

            default:
                super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.cont_list);

        findViewById(R.id.cont_list_subscribe_btn).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        String targetUri = ((TextView) findViewById(R.id.cont_target_uri))
                                .getText().toString();
                        if (TextUtils.isEmpty(targetUri)) {
                            Toast.makeText(ContListActivity.this,
                                    "The targetUri is null", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            (subscribeTask = new SubscribeTask())
                                    .execute(targetUri);
                        }
                    }
                });
        findViewById(R.id.cont_list_unsubscribe_btn).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        (unsubscribeTask = new UnSubscribeTask()).execute();
                    }
                });

        findViewById(R.id.cont_list_load_btn).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        reloadList();
                    }
                });
        
        /*
        findViewById(R.id.cont_list_test_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    new TestTask().execute();
                }
            }
        );
        */


        /*
         * final String appId =
         * getResources().getText(R.id.def_app_id).toString(); try { xdmService
         * = (XDMService) Connector.open("imsxdm://" + appId,
         * ContListActivity.this); presenceService = (PresenceService)
         * Connector.open("imspresence://" + appId, ContListActivity.this);
         * reloadList(); } catch (ImsException e) { Log.e(TAG,
         * "e.getMessage(): " + e.getMessage()); }
         */}

    /*
     * private void reloadList() { List<ContactItem> listItems =
     * dataProvider.reloadContactList(); ListView lv = getContactListView();
     * lv.setAdapter(new ContactListAdapter(this, listItems)); }
     */

    private void reloadList() {
        Log.i(TAG, "reloadList#started");
        String docName = ((TextView) findViewById(R.id.cont_doc_name))
                .getText().toString();
        String listName = ((TextView) findViewById(R.id.cont_list_name))
                .getText().toString();

        if (TextUtils.isEmpty(docName)) {
            Toast.makeText(ContListActivity.this, "Document name is null",
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(listName)) {
            Toast.makeText(ContListActivity.this, "List name is null",
                    Toast.LENGTH_SHORT).show();
        } else {
            (loadListTask = new LoadListTask()).execute(docName, listName);
        }
        Log.i(TAG, "reloadList#finished");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (loadListTask != null) {
            loadListTask.cancel(true);
        }

        if (subscribeTask != null) {
            subscribeTask.cancel(true);
        }

        if (unsubscribeTask != null) {
            unsubscribeTask.cancel(true);
        }

        if (xdmService != null) {
            xdmService.close();
        }

        if (presenceService != null) {
            presenceService.close();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;

        switch (id) {

        // dialogs

        default: {
            dialog = super.onCreateDialog(id);
        }
        }

        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {

        // dialogs

        default: {
            super.onPrepareDialog(id, dialog);
        }
        }
    }

    private class ContactItem {
        private String name;
        private String uri;
        private String note;
        private String availability;

        public ContactItem(String name, String uri, String note,
                String availability) {
            this.name = name;
            this.uri = uri;
            this.note = note;
            this.availability = availability;
        }

        public String getName() {
            return name;
        }

        public String getUri() {
            return uri;
        }

        public String getNote() {
            return note;
        }

        public String getAvailability() {
            return availability;
        }

        @Override
        public String toString() {
            return "ContactItem [name=" + name + ", uri=" + uri + ", note="
                    + note + ", availability=" + availability + "]";
        }
        
        // public String getMood() {
        // return "";
        // }
    }

    private class XDMDataProvider {

        private URIListDocument lastUriListDocument = null;
        private String lastEtag = null;
        
        private class ReloadListResult {
            private List<ContactItem> list;
            private ImsException exception;
            public ReloadListResult(List<ContactItem> list, ImsException exception) {
                this.list = list;
                this.exception = exception;
            }
            public List<ContactItem> getList() {
                return list;
            }
            public ImsException getException() {
                return exception;
            }
        }

        public ReloadListResult reloadContactList(
                String contactListDocumentName, String contactListName) {
            
            Log.i(TAG, "reloadContactList()#started");
            List<ContactItem> resultList = new ArrayList<ContactItem>();
            ImsException resultException = null;

            URIListDocument uriListDocument = null;

            try {
                uriListDocument = URIListDocument.retrieveDocument(
                        obtainXDMService(), contactListDocumentName, null,
                        lastEtag);
            } catch (IOException e) {
                resultException = new ImsException(e.getMessage());
            } catch (XCAPException e) {
                if (e.getStatusCode() != 304/*!"Not modified".equalsIgnoreCase(e.getReasonPhrase())*/) {
                    resultException = new ImsException(e.toString());
                }
            } catch (ImsException e) {
                resultException = e;
            }

            if (uriListDocument == null && lastEtag != null) {
                uriListDocument = lastUriListDocument;
            } else {
                lastUriListDocument = uriListDocument;
            }
            
            if (uriListDocument != null) {
                lastEtag = uriListDocument.getEtag();
                Log.i(TAG, "reloadContactList()#1");
                URIList uriListPhbk = uriListDocument
                        .getURIList(contactListName);

                if (uriListPhbk != null) {
                    Log.i(TAG, "reloadContactList()#2");
                    ListEntry[] listEntries = uriListPhbk.getListEntries();
                    if (listEntries != null) {
                        Log.i(TAG, "reloadContactList()#3");
                        for (ListEntry listEntry : listEntries) {

                            String uri = listEntry.getUri();
                            String displayName = listEntry.getDisplayName();
                            String note = null;
                            String availability = null;

                            ContactItem presenceData = presenseData.get(uri);
                            if (presenceData != null) {
                                note = presenceData.getNote();
                                availability = presenceData.getAvailability();
                            }

                            ContactItem newContactItem = new ContactItem(
                                    displayName, uri, note, availability);
                            resultList.add(newContactItem);
                        }
                    }
                }
            } /*
               * else { Toast.makeText(ContListActivity.this,
               * "Contact list doesn't exist", Toast.LENGTH_SHORT).show(); }
               */

            Log.i(TAG, "reloadContactList()#finished");
            return new ReloadListResult(resultList, resultException);
        }

    }

    /*
     * private String dumpStringArray(String[] arr) { if (arr == null) { return
     * "null"; } StringBuilder buf = new StringBuilder(); boolean first = true;
     * buf.append("["); for (String i : arr) { if (first) { first = false; }
     * else { buf.append(","); } buf.append(i); } buf.append("]"); return
     * buf.toString(); }
     */

    private WatcherListener watcherListenerImpl = new WatcherListener() {
        public void subscriptionTerminated(Watcher watcher, Event event) {
            Log.i(TAG, "WatcherListener.subscriptionTerminated");
            mHandler.sendEmptyMessage(SUBSCRIPTION_TERMINATED);
        }
        public void subscriptionStarted(Watcher watcher) {
            Log.i(TAG, "WatcherListener.subscriptionStarted");
            mHandler.sendEmptyMessage(SUBSCRIPTION_STARTED);
        }
        public void subscriptionFailed(Watcher watcher, ReasonInfo reasonInfo) {
            Log.i(TAG, "WatcherListener.subscriptionFailed");
            mHandler.sendEmptyMessage(SUBSCRIPTION_FAILED);
        }

        public void presenceInfoReceived(Watcher watcher,
                Presentity[] presentities) {
            Log.i(TAG,
                    "WatcherListener.presenceInfoReceived presentities.length="
                            + presentities.length);
            for (Presentity pres : presentities) {

                String name = null, uri = null, note = null, availability = null;

                Log.i(TAG, "--------------------------------------------------");
                Log.i(TAG, "pres.getDisplayName()=" + pres.getDisplayName());
                Log.i(TAG, "pres.getState()=" + pres.getState());
                Log.i(TAG, "pres.getURI()=" + pres.getURI());

                uri = pres.getURI();

                PresenceDocument presenceDocument = pres.getPresenceDocument();
                Log.i(TAG, "presenceDocument=" + presenceDocument);

                if (presenceDocument != null) {
                    ServiceInfo[] serviceInfos = presenceDocument
                            .getServiceInfo();
                    Log.i(TAG, "serviceInfos=" + serviceInfos);

                    if (serviceInfos != null && serviceInfos.length > 0) {
                        Log.i(TAG, "serviceInfos[0].getFreeText()="
                                + serviceInfos[0].getFreeText());
                        availability = serviceInfos[0].getFreeText();
                    }

                    PersonInfo personInfo = presenceDocument.getPersonInfo();
                    Log.i(TAG, "personInfo=" + personInfo);

                    if (personInfo != null) {
                        Log.i(TAG,
                                "personInfo.getActivities()="
                                        + Arrays.toString(personInfo
                                                .getActivities()));
                        Log.i(TAG, "personInfo.getClassification()="
                                + personInfo.getClassification());
                        Log.i(TAG,
                                "personInfo.getFreeText()="
                                        + personInfo.getFreeText());
                        Log.i(TAG,
                                "personInfo.getIdentifier()="
                                        + personInfo.getIdentifier());
                        Log.i(TAG,
                                "personInfo.getMoods()="
                                        + Arrays.toString(personInfo.getMoods()));
                        Log.i(TAG,
                                "personInfo.getPlacetypes()="
                                        + Arrays.toString(personInfo
                                                .getPlacetypes()));
                        Log.i(TAG,
                                "personInfo.getTimestamp()="
                                        + personInfo.getTimestamp());

                        note = personInfo.getFreeText();
                        if (availability == null || availability.length() == 0) {
                            if (personInfo.getActivities() != null
                                    && personInfo.getActivities().length > 0) {
                                availability = personInfo.getActivities()[0];
                            }
                        }
                    }
                }
                Log.i(TAG, "--------------------------------------------------");

                ContactItem newItem = new ContactItem(name, uri, note,
                        availability);
                presenseData.put(uri, newItem);
                
                Log.i(TAG, "new ContactItem() === " + newItem);
            }

            mHandler.sendEmptyMessage(RELOAD_LIST);
        }
    };

    class SubscribeTask extends AsyncTask<String, Void, Exception> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            ((TextView) findViewById(R.id.cont_list_status))
                    .setText("Status: trying to subscribe");
        }

        @Override
        protected Exception doInBackground(String... args) {
            final Exception result;

            Exception exception = null;
            String targetURI = args[0];
            try {
                currentWatcher = obtainPresenceService().createListWatcher(
                        targetURI);
                currentWatcher.setListener(watcherListenerImpl);
                currentWatcher.subscribe();
            } catch (ImsException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            }

            result = exception;

            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
            setProgressBarIndeterminateVisibility(false);
            if (result != null) {
                Log.e(TAG, result.getMessage(), result);
                Toast.makeText(ContListActivity.this, result.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    class UnSubscribeTask extends AsyncTask<Void, Void, IOException> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            ((TextView) findViewById(R.id.cont_list_status))
                    .setText("Status: subscription terminated");
        }

        @Override
        protected IOException doInBackground(Void... arg0) {
            final IOException result;

            IOException exception = null;
            if (currentWatcher != null) {
                try {
                    currentWatcher.unsubscribe();
                } catch (IOException e) {
                    exception = e;
                }

                presenseData.clear();
            }
            result = exception;

            return result;
        }

        @Override
        protected void onPostExecute(IOException result) {
            setProgressBarIndeterminateVisibility(false);
            if (result == null) {
                reloadList();
            } else {
                Log.e(TAG, result.getMessage(), result);
                Toast.makeText(ContListActivity.this, result.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private XDMService obtainXDMService() throws ImsException {
        if (xdmService == null) {
            final String appId = getResources().getText(R.id.def_app_id)
                    .toString();
            xdmService = (XDMService) Connector.open("imsxdm://" + appId, this);
        }

        return xdmService;
    }

    private PresenceService obtainPresenceService() throws ImsException {
        if (presenceService == null) {
            final String appId = getResources().getText(R.id.def_app_id)
                    .toString();
            presenceService = (PresenceService) Connector.open("imspresence://"
                    + appId, this);
        }

        return presenceService;
    }

    private class LoadListResult {
        private List<ContactItem> items;
        private ImsException exception;
    }

    class LoadListTask extends AsyncTask<String, Void, LoadListResult> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            findViewById(R.id.cont_list_load_btn).setEnabled(false);
        }

        @Override
        protected LoadListResult doInBackground(String... args) {
            Log.i(TAG, "LoadListTask.doInBackground#started");
            final LoadListResult result = new LoadListResult();

            final String contactListDocumentName = args[0];
            final String contactListName = args[1];
            
            javax.microedition.ims.engine.test.ContListActivity.XDMDataProvider.ReloadListResult reloadContactListResult
                = dataProvider.reloadContactList(contactListDocumentName, contactListName);
            
            result.items = reloadContactListResult.getList();
            result.exception = reloadContactListResult.getException();

            Log.i(TAG, "LoadListTask.doInBackground#finished    result.items=" + (result.items == null ? "" : result.items));
            return result;
        }

        @Override
        protected void onPostExecute(LoadListResult result) {
            setProgressBarIndeterminateVisibility(false);
            findViewById(R.id.cont_list_load_btn).setEnabled(true);
            ;
            
            if (result.items != null) {
                getContactListView().setAdapter(
                        new ContactListAdapter(ContListActivity.this,
                                result.items));
            }

            if (result.exception != null) {
                Log.e(TAG, result.exception.getMessage(), result.exception);
                Toast.makeText(ContListActivity.this,
                        result.exception.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private ListView getContactListView() {
        return (ListView) findViewById(R.id.cont_list_contact_list);
    }

    private class ContactListAdapter extends BaseAdapter {
        private Context context;
        private List<ContactItem> items = new ArrayList<ContactItem>();

        public ContactListAdapter(Context context, List<ContactItem> items) {
            this.context = context;
            this.items = items;
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
            ContactItem contactItem = items.get(position);
            ContactView contactView = null;
            if (convertView != null) {
                contactView = (ContactView) convertView;
                contactView.setContactItem(contactItem);
            } else {
                contactView = new ContactView(context, contactItem);
            }
            return contactView;
        }
    }

    private class ContactView extends LinearLayout {
        private TextView nameCtrl;
        private TextView uriCtrl;
        private TextView availabilityCtrl;
        private TextView moodCtrl;
        private TextView noteCtrl;
        private ContactItem contactItem;

        public ContactView(Context context, ContactItem contactItem) {
            super(context);

            setOrientation(VERTICAL);

            this.contactItem = contactItem;
            this.nameCtrl = new TextView(context);
            setName(contactItem.getName());
            addView(nameCtrl, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            this.uriCtrl = new TextView(context);
            setUri(contactItem.getUri());
            addView(uriCtrl, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            // LinearLayout linearLayout = new LinearLayout(context);
            // linearLayout.setOrientation(HORIZONTAL);
            // linearLayout.setLayoutParams(new
            // LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
            // LayoutParams.WRAP_CONTENT));
            // //linearLayout.setBackgroundColor(Color.WHITE);
            // //addView(linearLayout, new
            // LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
            // LayoutParams.WRAP_CONTENT));
            // addView(linearLayout);

            this.availabilityCtrl = new TextView(context);
            setAvailability(contactItem.getAvailability());
            addView(availabilityCtrl, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // linearLayout.addView(availabilityCtrl);

            // moodCtrl = new TextView(context);
            // moodCtrl.setGravity(Gravity.RIGHT);
            // setMood(contactItem.getMood());
            // linearLayout.addView(moodCtrl);

            this.noteCtrl = new TextView(context);
            setNote(contactItem.getNote());
            addView(noteCtrl, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        public void update() {
            setName(contactItem.getName());
            setUri(contactItem.getUri());
        }

        private void setName(String name) {
            nameCtrl.setText("Name: " + name);
        }

        private void setUri(String uri) {
            uriCtrl.setText("Uri: " + uri);
        }

        private void setAvailability(String availability) {
            availabilityCtrl.setText("Status: "
                    + (availability == null ? "" : availability));
        }

        private void setMood(String mood) {
            moodCtrl.setText("Mood: " + (mood == null ? "" : mood));
        }

        private void setNote(String note) {
            noteCtrl.setText("Note: " + (note == null ? "" : note));
        }

        public void setContactItem(ContactItem contactItem) {
            this.contactItem = contactItem;
            update();
        }
    }
    
    
    private class TestTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            Log.i(TAG, "TestTask#started");

            watcherListenerImpl.presenceInfoReceived(
                null,
                new Presentity[]{
                    new Presentity() {
                        public String getDisplayName() {
                            return "";
                        }

                        public int getState() {
                            return Presentity.STATE_ACTIVE;
                        }

                        public String getURI() {
                            return "z111";
                        }

                        public PresenceDocument getPresenceDocument() {
                            return new PresenceDocument() {
                                public void setPersonInfo(PersonInfo personInfo) {
                                    //do nothing here
                                }
                                
                                public void removeInfo(String identifier) {
                                    //do nothing here
                                }
                                
                                public void removeDirectContent(String cid) {
                                    //do nothing here
                                }
                                
                                public ServiceInfo[] getServiceInfo() {
/*                                    ServiceInfo serviceInfo = new ServiceInfo("", "", "");
                                    serviceInfo.setFreeText("");
                                    return new ServiceInfo[] {serviceInfo};
*/
                                    return null;
                                }
                                
                                public PersonInfo getPersonInfo() {
                                    PersonInfo personInfo = new PersonInfo();
                                    personInfo.setFreeText("hard state text" + new Random().nextInt());
                                    return personInfo;
                                }
                                
                                public DirectContent[] getDirectContent() {
                                    return null;
                                }
                                
                                public DeviceInfo[] getDeviceInfo() {
                                    return null;
                                }
                                
                                public Document getDOM() {
                                    return null;
                                }
                                
                                public void addServiceInfo(ServiceInfo serviceInfo) {
                                    //do nothing here
                                }
                                
                                public void addDirectContent(DirectContent directContent) {
                                    //do nothing here
                                }
                                
                                public void addDeviceInfo(DeviceInfo deviceInfo) {
                                    //do nothing here
                                }
                            };
                        }

                        public Event getEvent() {
                            return null;
                        }
                    }       
                }
            );
            
            Log.i(TAG, "TestTask#finished");
            return null;
        } 
    }
    
}
