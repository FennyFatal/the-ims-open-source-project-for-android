package javax.microedition.ims.engine.test;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.ims.Connector;
import javax.microedition.ims.ImsException;
import javax.microedition.ims.xdm.*;
import java.io.IOException;
import java.util.Date;


public class XdmDocsActivity extends Activity {
    private static final String TAG = "XdmDocsActivity"; 
    
    public static final String NAME_RLS = "RLS-Doc";
    public static final String NAME_PRES_RULES = "Pres-Rules";
    
    private XDMService xdmService;
    
    private static final String XUI = null;
    
    private XDMDocumentProvider documentProvider;
    private XdmConfigRegistry currentXDMConfigRegistry;
    

    class XdmConfigRegistry {
        private final String rldDocPath;
        private final String rlsServiceUri;
        private final String rlsUriListReference;
        private final String presRulesDocPath;
        private final String presRulesUriListReference;

        private XdmConfigRegistry(XdmConfig config) {
            this.rldDocPath = config.rlsDocPath;
            this.presRulesDocPath = config.presRulesDocPath;
            
            this.rlsServiceUri = buildRlsServiceUri(config);
            this.rlsUriListReference = buildRlsUriListReference(config);
            this.presRulesUriListReference = buildPresRulesUriListReference(config); 
        }
        
        private String buildRlsServiceUri(XdmConfig config) {
            return String.format("%s;%s", config.xuiName, config.rlsServiceUriListParam);
        }

        private String buildRlsUriListReference(XdmConfig config) {
            return String.format("%s/%s/users/%s/%s/~~/%s/list%s", config.xcapRoot, URIListDocument.AUID, config.xuiName,
                    config.rlsDocPath, URIListDocument.AUID, "%5b@name=%22"+ config.rlsDocPath +"%22%5d");
        }

        private String buildPresRulesUriListReference(XdmConfig config) {
            return String.format("%s/%s/users/%s/%s/~~/%s/list%s", config.xcapRoot, URIListDocument.AUID, config.xuiName,
                    config.presRulesDocPath, URIListDocument.AUID, "%5b@name=%22" + config.presRulesDocPath + "%22%5d");
        } 

    } 
    
    class XdmConfig {
        private final String xcapRoot;
        private final String xuiName;
        private final String rlsDocPath;
        private final String rlsServiceUriListParam;
        private final String presRulesDocPath;
        
        private XdmConfig(String xcapRoot, String xuiName,
                String rlsDocPath, String rlsServiceUriListParam,
                String presRulesDocPath) {
            this.xcapRoot = xcapRoot;
            this.xuiName = xuiName;
            this.rlsDocPath = rlsDocPath;
            this.rlsServiceUriListParam = rlsServiceUriListParam;
            this.presRulesDocPath = presRulesDocPath;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.xdm_docs);
        
        
        findViewById(R.id.xdm_docs_select_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    XdmDocTypeSelectDialog.showDialog(XdmDocsActivity.this);
                }
            }
        );
        findViewById(R.id.xdm_docs_create_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    String resDescr = documentProvider.create();
                    Toast.makeText(XdmDocsActivity.this, resDescr, Toast.LENGTH_SHORT).show();
                    
                    writeLogMessage(resDescr);
                }
            }
        );
        findViewById(R.id.xdm_docs_retrieve_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    String resDescr = documentProvider.retrieve();
                    Toast.makeText(XdmDocsActivity.this, resDescr, Toast.LENGTH_SHORT).show();
                    
                    writeLogMessage(resDescr);
                }

            }
        );
        findViewById(R.id.xdm_docs_delete_btn).setOnClickListener(
            new OnClickListener() {
                public void onClick(View v) {
                    String resDescr = documentProvider.delete();
                    Toast.makeText(XdmDocsActivity.this, resDescr, Toast.LENGTH_SHORT).show();
                    
                    writeLogMessage(resDescr);
                }
            }
        );

        
        final String appId = getResources().getText(R.id.def_app_id).toString();
        try {
            xdmService = (XDMService) Connector.open("imsxdm://" + appId, XdmDocsActivity.this);
        } catch (ImsException e) {
            Log.e(TAG, e.getMessage());
        }
        
        String xcapRoot = xdmService.getXcapRoot();
        String defXui = xdmService.getDefXui();
        
        XdmConfig tMobileConfig = new XdmConfig(xcapRoot, defXui, "phbk", "list=phbk", "pres-rules");
        
        currentXDMConfigRegistry = new XdmConfigRegistry(tMobileConfig);
        documentProvider = new XDMRLSDocProvider(currentXDMConfigRegistry);
    }
    
    public void switchDocumentProvider(String docName) {
        if (docName.startsWith(NAME_RLS)) {
            this.documentProvider = new XDMRLSDocProvider(currentXDMConfigRegistry);
        } else if (docName.startsWith(NAME_PRES_RULES)) {
            this.documentProvider = new XDMPresRulesDocProvider(currentXDMConfigRegistry);
        }
    }
    
    private void writeLogMessage(String resDescr) {
        LinearLayout historyLayout = (LinearLayout) findViewById(R.id.xdm_docs_message_history_layout);
        
        Date time = new Date();
        
        TextView textView = new TextView(XdmDocsActivity.this);
        textView.setText(time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds() + " - " + resDescr);
        textView.setTextColor(Color.BLACK);
        
        historyLayout.addView(textView);
    }

    
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;

        switch (id) {
            case XdmDocTypeSelectDialog.DIALOG_ID: {
                dialog = XdmDocTypeSelectDialog.createDialog(this);
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
            case XdmDocTypeSelectDialog.DIALOG_ID: {
                XdmDocTypeSelectDialog.onPrepare(this, dialog);
                break;
            }
    
            default: {
                super.onPrepareDialog(id, dialog);
            }
        }
    }


    private abstract class XDMDocumentProvider {
        protected String lastEtag;
        protected XdmConfigRegistry xdmConfig;
        
        protected XDMDocumentProvider(XdmConfigRegistry xdmConfig) {
            this.xdmConfig = xdmConfig;
        }
        
        public abstract String getDocName();
        
        public abstract String retrieve();
        
        public abstract String create();
        
        public abstract String delete();       
        
    }
    
    
    private class XDMRLSDocProvider extends XDMDocumentProvider {
        
        public XDMRLSDocProvider(XdmConfigRegistry xdmConfig) {
            super(xdmConfig);
        }
        
        
        public String getDocName() {
            return NAME_RLS;
        }

        
        public String retrieve() {
            String res = null;
            
            try {
                PresenceListDocument retrievedDocument
                    = PresenceListDocument.retrieveDocument(xdmService, xdmConfig.rldDocPath, XUI, lastEtag);
                
                if (retrievedDocument == null) {
                    res = getDocName() + " doesn't exist";
                } else {
                    res = getDocName() + " exists. Etag=" + retrievedDocument.getEtag() + ". With "
                        + retrievedDocument.getPresenceLists().length + " presence lists.";
                    lastEtag = retrievedDocument.getEtag();
                }
                
            } catch (IOException e) {
                res = getDocName() + " retrieving error: " + e.getMessage();
            } catch (XCAPException e) {
                res = getDocName() + " retrieving error: " + e.getReasonPhrase();
            } 
            return res;
        }

        
        public String create() {
            String res = null;
            
            try {
                PresenceListDocument presenceListDocument
                    = PresenceListDocument.createDocument(xdmService, xdmConfig.rldDocPath);
                
                if (presenceListDocument != null) {
                    PresenceList presenceList = new PresenceList(xdmConfig.rlsServiceUri, xdmConfig.rlsUriListReference);
                    presenceListDocument.addPresenceList(presenceList);
                    
                    res = getDocName() + " was created. Etag=" + presenceListDocument.getEtag() + ". With "
                        + presenceListDocument.getPresenceLists().length + " presence lists.";
                } else {
                    res = getDocName() + " wasn't created";
                }
            
            } catch (IOException e) {
                res = getDocName() + " creation error: " + e.getMessage();
            } catch (XCAPException e) {
                res = getDocName() + " creation error: " + e.getReasonPhrase();
            } 
            return res;
        }

        
        public String delete() {
            String res = null;
            
            try {
                PresenceListDocument.deleteDocument(xdmService, xdmConfig.rldDocPath, null);
                
                res = getDocName() + " was deleted";
                
            } catch (IOException e) {
                res = getDocName() + " deleting error: " + e.getMessage();
            } catch (XCAPException e) {
                res = getDocName() + " deleting error: " + e.getReasonPhrase();
            } 
            return res;
        }
    }
    
    
    private class XDMPresRulesDocProvider extends XDMDocumentProvider {
        
        public XDMPresRulesDocProvider(XdmConfigRegistry xdmConfig) {
            super(xdmConfig);
        }

        
        public String getDocName() {
            return NAME_PRES_RULES;
        }

        
        public String retrieve() {
            String res = null;
            
            try {
                PresenceAuthorizationDocument retrievedDocument
                    = PresenceAuthorizationDocument.retrieveDocument(xdmService, xdmConfig.presRulesDocPath, XUI, lastEtag);
                
                if (retrievedDocument == null) {
                    res = getDocName() + " doesn't exist";
                } else {
                    res = getDocName() + " exists. Etag=" + retrievedDocument.getEtag() + ". With "
                        + retrievedDocument.getRules().length + " rules.";
                    lastEtag = retrievedDocument.getEtag();
                }
                
            } catch (IOException e) {
                res = getDocName() + " retrieving error: " + e.getMessage();
            } catch (XCAPException e) {
                res = getDocName() + " retrieving error: " + e.getReasonPhrase();
            } 
            return res;
        }

        
        public String create() {
            String res = null;
            
            try {
                PresenceAuthorizationDocument presenceAuthorizationDocument
                    = PresenceAuthorizationDocument.createDocument(xdmService, xdmConfig.presRulesDocPath);
                
                if (presenceAuthorizationDocument != null) {
                    
                    PresenceAuthorizationRule rule = new PresenceAuthorizationRule();
                    rule.setRuleId("allow_icontag");
                    rule.setConditionURIList(xdmConfig.presRulesUriListReference);
                    rule.setAction(PresenceAuthorizationRule.ACTION_ALLOW);
                    
                    PresenceContentFilter presenceContentFilter = new PresenceContentFilter();
                    presenceContentFilter.provideAllComponents(PresenceContentFilter.COMPONENT_PERSONS, true);
                    rule.setPresenceContentFilter(presenceContentFilter);
                    
                    presenceAuthorizationDocument.addRule(rule);
                    
                    res = getDocName() + " was created. Etag=" + presenceAuthorizationDocument.getEtag() + ". With "
                        + presenceAuthorizationDocument.getRules().length + " rules.";
                } else {
                    res = getDocName() + " wasn't created";
                }
            
            } catch (IOException e) {
                res = getDocName() + " creation error: " + e.getMessage();
            } catch (XCAPException e) {
                res = getDocName() + " creation error: " + e.getReasonPhrase();
            } 
            return res;
        }

        
        public String delete() {
            String res = null;
            
            try {
                PresenceAuthorizationDocument.deleteDocument(xdmService, xdmConfig.presRulesDocPath, null);
                
                res = getDocName() + " was deleted";
                
            } catch (IOException e) {
                res = getDocName() + " deleting error: " + e.getMessage();
            } catch (XCAPException e) {
                res = getDocName() + " deleting error: " + e.getReasonPhrase();
            } 
            return res;
        }
    }
    
    
    private static class XdmDocTypeSelectDialog {
        public static final int DIALOG_ID = 45;
        
        private static String[] elements = {NAME_RLS, NAME_PRES_RULES};
        
        public static Dialog createDialog(final XdmDocsActivity xdmDocsActivity) {
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(xdmDocsActivity,
                    android.R.layout.simple_dropdown_item_1line, elements);

            AlertDialog.Builder builder = new AlertDialog.Builder(xdmDocsActivity);
            builder.setTitle(R.string.xdm_docs_switch_title);
            builder.setAdapter(
                adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String dataItem = adapter.getItem(which);
                        if (dataItem.equals(NAME_RLS)) {
                            xdmDocsActivity.switchDocumentProvider(NAME_RLS);
                            
                            ((TextView)xdmDocsActivity.findViewById(R.id.xdm_docs_current_document)).setText("Current document: " + NAME_RLS);
                            
                        } else if (dataItem.equals(NAME_PRES_RULES)) {
                            xdmDocsActivity.switchDocumentProvider(NAME_PRES_RULES);
                            
                            ((TextView)xdmDocsActivity.findViewById(R.id.xdm_docs_current_document)).setText("Current document: " + NAME_PRES_RULES);
                        }
                    }
                }
            );
            AlertDialog dialog = builder.create();
            return dialog;
        }
        
        public static void onPrepare(final XdmDocsActivity xdmDocsActivity, Dialog dialog) {
            //do nothing
        }
        
        public static void showDialog(XdmDocsActivity activity) {
            activity.showDialog(DIALOG_ID);
        }
        
        public static void hideDialog(XdmDocsActivity activity) {
            activity.dismissDialog(DIALOG_ID);
        }
    }

    
    
}
