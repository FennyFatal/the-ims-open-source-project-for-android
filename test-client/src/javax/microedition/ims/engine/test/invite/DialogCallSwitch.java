package javax.microedition.ims.engine.test.invite;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import javax.microedition.ims.engine.test.AppContext;
import javax.microedition.ims.engine.test.BaseActivity;
import javax.microedition.ims.engine.test.R;

public class DialogCallSwitch {

    protected final static String TAG = "DialogCallSwitch";
    
    public static final int DIALOG_ID = 30;
    //public static final int CALLS_DIALOG_ID = 31;
    
    
    public static Dialog createDialog(final BaseActivity baseActivity) {

        String[] elements = AppContext.instance.getSessionList().getRemoteParties();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(baseActivity,
                android.R.layout.simple_dropdown_item_1line, elements);

        AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
        builder.setTitle(R.string.call_switch_title);
        builder.setAdapter(
            adapter,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String dataItem = adapter.getItem(which);
                    if (dataItem.startsWith("A-")) {
                        //do nothing
                    } else if (dataItem.startsWith("H-")) {
                        String remoteParty = dataItem.substring(2);
                        AppContext.instance.getSessionList().onHoldCurrentAndActivateAnotherSession(remoteParty);
                    }
                }
            }
        );
        AlertDialog dialog = builder.create();
        
//        dialog.getListView().setAdapter(adapter);
        
        return dialog;
    }
    
    public static void onPrepare(final BaseActivity baseActivity, Dialog dialog) {
        
        String[] elements = AppContext.instance.getSessionList().getRemoteParties();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(baseActivity,
                android.R.layout.simple_dropdown_item_1line, elements);

        AlertDialog alertDialog = (AlertDialog)dialog;
        alertDialog.getListView().setAdapter(adapter);
        alertDialog.getListView().setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
                
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String dataItem = adapter.getItem(position);
                    String remoteParty = dataItem.substring(2);
                    if (dataItem.startsWith("A-")) {
                        //do nothing
                    } else if (dataItem.startsWith("H-")) {
                        AppContext.instance.getSessionList().onHoldCurrentAndActivateAnotherSession(remoteParty);
                        
                        DialogCallInProgress.hideDialog(baseActivity);
                        DialogCallInProgress.showDialog(baseActivity, remoteParty);
                    }
                    hideDialog(baseActivity);
                }
            }
        );
        
    }
    
    public static void showDialog(BaseActivity activity) {
        activity.showDialog(DIALOG_ID);
    }
    
    public static void hideDialog(BaseActivity activity) {
        activity.dismissDialog(DIALOG_ID);
    }
    
    
//    public static DialogCallSwitch createDialog2(final BaseActivity baseActivity) {
//        
//        final DialogCallSwitch dialog = new DialogCallSwitch(
//            baseActivity
//        );
//        
//        dialog.setCancelable(false);
//        dialog.setContentView(R.layout.dialog_call_switch);
//        dialog.setTitle(R.string.call_switch_title);
//        
//        dialog.findViewById(R.id.call_switch_btn_choose).setOnClickListener(new View.OnClickListener() {
//            
//            public void onClick(View v) {
//                baseActivity.showDialog(CALLS_DIALOG_ID);                
//            }
//        });
//
//        /*
//        dialog.findViewById(R.id.call_switch_btn_main).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) 
//               
//            }
//        });
//        */
//
//        dialog.findViewById(R.id.call_switch_btn_switch).setOnClickListener(new View.OnClickListener() {
//            
//            public void onClick(View v) {
//                baseActivity.dismissDialog(DIALOG_ID);
//               
//            }
//        });
//        
//        return dialog;
//    }
//    

}
