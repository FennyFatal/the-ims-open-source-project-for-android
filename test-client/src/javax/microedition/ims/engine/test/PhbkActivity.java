package javax.microedition.ims.engine.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.*;

import javax.microedition.ims.Connector;
import javax.microedition.ims.ImsException;
import javax.microedition.ims.xdm.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhbkActivity extends Activity {
    private static final String TAG = "PhbkActivity";
    
    private final int EDIT_ID = 1;
    private final int DELETE_ID = 2;
    
    //public static final String NAME_PHBK = "phbk";
    public static final String NAME_PHBK = "index";
    public static final String NAME_ICON_TAG = "allow_icontag";
    
    private XDMService xdmService;
    
    private static final String XUI = null;
    
    private XDMDataProvider dataProvider = new XDMDataProvider(NAME_PHBK, NAME_PHBK);
    
    public XDMDataProvider getDataProvider() {
        return dataProvider;
    }

    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.phbk);
        
        
        findViewById(R.id.phbk_add_contact_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    addContactButtonHandler();
                }
            }
        );
        findViewById(R.id.phbk_delete_doc_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    deleteDocument();
                }
            }
        );
        findViewById(R.id.phbk_reload_contacts_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    reloadList();
                }
            }
        );
        findViewById(R.id.phbk_select_document_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    DocTypeSelectDialog.showDialog(PhbkActivity.this);
                }
            }
        );
        
        
        final String appId = getResources().getText(R.id.def_app_id).toString();
        try {
            xdmService = (XDMService) Connector.open("imsxdm://" + appId, PhbkActivity.this);
        } catch (ImsException e) {
            Log.e(TAG, "e = " + e.getMessage());
        }

        List<ContactItem> listItems = dataProvider.loadOrCreateIfNotExist();
        
        ListView lv = getContactListView();
        lv.setAdapter(new ContactListAdapter(this, listItems));
//        ListAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"dddd", "fffff"});
//        lv.setAdapter(listAdapter);
        
        registerForContextMenu(lv);
        
    }
    
    public void reloadList() {
        List<ContactItem> listItems = dataProvider.loadOrCreateIfNotExist();
            
        ListView lv = getContactListView();
        lv.setAdapter(new ContactListAdapter(PhbkActivity.this, listItems));
    }
    
    public void deleteDocument() {
        dataProvider.deleteDocument();
        
        List<ContactItem> listItems = new ArrayList<ContactItem>();
        
        ListView lv = getContactListView();
        lv.setAdapter(new ContactListAdapter(PhbkActivity.this, listItems));
    }
    
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;

        switch (id) {
            case DocTypeSelectDialog.DIALOG_ID: {
                dialog = DocTypeSelectDialog.createDialog(this);
            }
            break;
    
            default: {
                dialog = super.onCreateDialog(id);
            }
        }

        return dialog;
    }

    
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DocTypeSelectDialog.DIALOG_ID: {
                DocTypeSelectDialog.onPrepare(this, dialog);
                break;
            }
    
            default: {
                super.onPrepareDialog(id, dialog);
            }
        }
    }

    private ListView getContactListView() {
        return (ListView)findViewById(R.id.phbk_contact_list);
    }
    
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        menu.add(0, EDIT_ID, 0, "Edit contact");
        menu.add(0, DELETE_ID, 0,  "Delete contact");
    }
    
    
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        
        ContactView targetContactView = (ContactView)info.targetView;
        
        switch (item.getItemId()) {
            case EDIT_ID:
                editContactMenuHandler(targetContactView);
                return true;
              
            case DELETE_ID:
                deleteContactMenuHandler(targetContactView);
                return true;
              
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showEditDialog(final ContactView targetContactView, final String name, final String uri) {
        
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_phbk_edit_contact, null);
        
        ((TextView)textEntryView.findViewById(R.id.phbk_edit_contact_name)).setText(name);
        ((TextView)textEntryView.findViewById(R.id.phbk_edit_contact_uri)).setText(uri);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon)
               .setTitle("Edit contact dialog")
               .setView(textEntryView)
               .setPositiveButton(
                   "Done",
                   new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int whichButton) {
                           
                           String nameValue = ((TextView)textEntryView.findViewById(R.id.phbk_edit_contact_name)).getText().toString();
                           String uriValue = ((TextView)textEntryView.findViewById(R.id.phbk_edit_contact_uri)).getText().toString();
                           
                           if (targetContactView != null) {
                               updateItem(targetContactView, nameValue, uriValue);
                           } else {
                               addItem(nameValue, uriValue);
                           }
                           
                       }
                   }
               )
               .setNegativeButton(
                   "Revert",
                   new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int whichButton) {
                           //do nothing
                       }
                   }
               )
               .create();
        
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void addContactButtonHandler() {
        showEditDialog(null, "", "");
    }
    
    private void editContactMenuHandler(ContactView targetContactView) {
        ContactItem contactItem = targetContactView.getContactItem();
        showEditDialog(targetContactView, contactItem.getName(), contactItem.getUri());
    }

    private void deleteContactMenuHandler(final ContactView targetContactView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete contact?")
               .setCancelable(false)
               .setPositiveButton(
                   "Yes",
                   new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           deleteItem(targetContactView);
                       }
                   }
               )
               .setNegativeButton(
                   "No",
                   new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                       }
                   }
               );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void addItem(String name, String uri) {
        dataProvider.add(name, uri);
        List<ContactItem> listItems = dataProvider.loadOrCreateIfNotExist();
        
        ListView lv = getContactListView();
        lv.setAdapter(new ContactListAdapter(this, listItems));
    }
    
    private void updateItem(ContactView targetContactView, String newName, String newUri) {
        ContactItem contactItem = targetContactView.getContactItem();
        
        dataProvider.remove(contactItem.getUri());
        dataProvider.add(newName, newUri);
        List<ContactItem> listItems = dataProvider.loadOrCreateIfNotExist();
        
        ListView lv = getContactListView();
        lv.setAdapter(new ContactListAdapter(this, listItems));
    }
    
    private void deleteItem(ContactView targetContactView) {
        String uri = targetContactView.getContactItem().getUri();
        dataProvider.remove(uri);
        List<ContactItem> listItems = dataProvider.loadOrCreateIfNotExist();
        
        ListView lv = getContactListView();
        lv.setAdapter(new ContactListAdapter(this, listItems));
    }



    private class ContactItem {
        private String name;
        private String uri;
        
        public ContactItem(String name, String uri) {
            this.name = name;
            this.uri = uri;
        }
        
        public String getName() {
            return name;
        }
        
        public String getUri() {
            return uri;
        }
        
        
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((uri == null) ? 0 : uri.hashCode());
            return result;
        }

        
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ContactItem other = (ContactItem) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (uri == null) {
                if (other.uri != null)
                    return false;
            } else if (!uri.equals(other.uri))
                return false;
            return true;
        }

        private PhbkActivity getOuterType() {
            return PhbkActivity.this;
        }
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
                contactView = (ContactView)convertView;
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
        private ContactItem contactItem;
        
        public ContactView(Context context, ContactItem contactItem) {
            super(context);
            
            this.setOrientation(VERTICAL);
            
            this.contactItem = contactItem;
            
            nameCtrl = new TextView(context);
            setName(contactItem.getName());
            addView(nameCtrl, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            
            uriCtrl = new TextView(context);
            setUri(contactItem.getUri());
            addView(uriCtrl, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
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

        public ContactItem getContactItem() {
            return contactItem;
        }

        public void setContactItem(ContactItem contactItem) {
            this.contactItem = contactItem;
            update();
        }
    }
    
    
    private class XDMDataProvider {
        
        private URIList lastUriList;
        private String currentDocumentName;
        private String currentUriListName;
        private URIListDocument lastUriListDocument = null;
        private String lastEtag = null;
        
        public XDMDataProvider(String currentDocumentName, String currentUriListName) {
            this.currentDocumentName = currentDocumentName;
            this.currentUriListName = currentUriListName;
        }
        
        public void setCurrentDocumentName(String currentDocumentName) {
            this.currentDocumentName = currentDocumentName;
        }

        public void setCurrentUriListName(String currentUriListName) {
            this.currentUriListName = currentUriListName;
        }

        //TODO AK: need execute this method in separate thread, use AsyncTask instead 
        public List<ContactItem> loadOrCreateIfNotExist() {
            List<ContactItem> result = new ArrayList<ContactItem>();
            
            URIListDocument uriListDocument = null;
            
            try {
                uriListDocument = URIListDocument.retrieveDocument(xdmService, currentDocumentName, XUI, lastEtag);
                
            } catch (IOException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (XCAPException e) {
                Toast.makeText(PhbkActivity.this, e.getReasonPhrase(), Toast.LENGTH_SHORT).show();
            }

            try {
                if (uriListDocument == null) {
                    if (lastEtag == null) {
                        uriListDocument = URIListDocument.createDocument(xdmService, currentDocumentName);
                    } else {
                        uriListDocument = lastUriListDocument;
                    }
                } else {
                    lastUriListDocument = uriListDocument;
                }
                lastEtag = uriListDocument.getEtag();
                
                
                URIList uriListPhbk = uriListDocument.getURIList(currentUriListName);
                if (uriListPhbk == null) {
                    uriListPhbk = uriListDocument.createURIList(currentUriListName, currentUriListName, new ListEntry[0]);
                }
                
                if (uriListPhbk != null) {
                    lastUriList = uriListPhbk;
                    ListEntry[] listEntries = uriListPhbk.getListEntries();
                    if (listEntries != null) {
                        for (ListEntry listEntry : listEntries) {
                            result.add(new ContactItem(listEntry.getDisplayName(), listEntry.getUri()));
                        }
                    }
                }
                
            } catch (IOException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (XCAPException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return result;
        }
        
        public void deleteDocument() {
            try {
                
                URIListDocument.deleteDocument(xdmService, currentDocumentName, null);
                
                lastUriList = null;
                lastUriListDocument = null;
                lastEtag = null;
                
            } catch (IOException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (XCAPException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        
        public void remove(String uri) {
            try {
                if (lastUriList != null) {
                    lastUriList.removeListEntry(uri);
                }
                
            } catch (IOException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (XCAPException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        
        public void add(String name, String uri) {
            
            ListEntry listEntry = new ListEntry(ListEntry.URI_ENTRY, uri);
            listEntry.setDisplayName(name);
            
            try {
                if (lastUriList == null) {
                    loadOrCreateIfNotExist();
                }
                
                if (lastUriList != null) {
                    lastUriList.addListEntry(listEntry);
                }
                
            } catch (IOException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (XCAPException e) {
                Toast.makeText(PhbkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        
    }
    
    
    
    private static class DocTypeSelectDialog {
        public static final int DIALOG_ID = 40;
        
        private static String[] elements = {NAME_PHBK, NAME_ICON_TAG};
        
        public static Dialog createDialog(final PhbkActivity phbkActivity) {
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(phbkActivity,
                    android.R.layout.simple_dropdown_item_1line, elements);

            AlertDialog.Builder builder = new AlertDialog.Builder(phbkActivity);
            builder.setTitle(R.string.call_switch_title);
            builder.setAdapter(
                adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String dataItem = adapter.getItem(which);
                        if (dataItem.equals(NAME_PHBK)) {
                            phbkActivity.getDataProvider().setCurrentDocumentName(NAME_PHBK);
                            phbkActivity.getDataProvider().setCurrentUriListName(NAME_PHBK);
                            phbkActivity.reloadList();
                            
                            ((TextView)phbkActivity.findViewById(R.id.phbk_current_document)).setText("Current document: " + NAME_PHBK);
                            
                        } else if (dataItem.equals(NAME_ICON_TAG)) {
                            phbkActivity.getDataProvider().setCurrentDocumentName(NAME_ICON_TAG);
                            phbkActivity.getDataProvider().setCurrentUriListName(NAME_ICON_TAG);
                            phbkActivity.reloadList();
                            
                            ((TextView)phbkActivity.findViewById(R.id.phbk_current_document)).setText("Current document: " + NAME_ICON_TAG);
                        }
                    }
                }
            );
            AlertDialog dialog = builder.create();
            return dialog;
        }
        
        public static void onPrepare(final PhbkActivity phbkActivity, Dialog dialog) {
            //do nothing
        }
        
        public static void showDialog(PhbkActivity activity) {
            activity.showDialog(DIALOG_ID);
        }
        
        public static void hideDialog(PhbkActivity activity) {
            activity.dismissDialog(DIALOG_ID);
        }
    }
    
}
