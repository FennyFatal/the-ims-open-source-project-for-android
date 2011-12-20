package javax.microedition.ims.engine.test.msrp;

import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.microedition.ims.engine.test.MsrpChatActivity;
import javax.microedition.ims.engine.test.R;
import java.io.File;

public class DialogMsrpFileList extends Dialog {
    
    private final MsrpChatActivity msrpChatActivity;
    private File selectedFile;
    
    public static final int DIALOG_ID = 21;

    public DialogMsrpFileList(MsrpChatActivity msrpChatActivity) {
        super(msrpChatActivity);
        this.msrpChatActivity = msrpChatActivity;
    }
    
    public static DialogMsrpFileList createDialog(MsrpChatActivity msrpChatActivity) {
        final DialogMsrpFileList dialog = new DialogMsrpFileList(msrpChatActivity);
        
        dialog.setContentView(R.layout.dialog_msrp_file_list);
        dialog.setTitle(R.string.msrp_file_list_title);
        
        dialog.findViewById(R.id.msrp_file_list_cancel_btn).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.onCancel();
                }
            }
        );
        
        dialog.findViewById(R.id.msrp_file_list_ok_btn).setOnClickListener(
            new View.OnClickListener() {
                
                public void onClick(View v) {
                    dialog.onOK();
                }
            }
        );
        
        return dialog;
    }
    
    public void onPrepare() {
        selectFile(null);
        findViewById(R.id.msrp_file_list_ok_btn).setEnabled(false);
        browseDir(new File("/sdcard"));
    }

    private void onCancel() {
        msrpChatActivity.dismissDialog(DIALOG_ID);
    }
    
    private void onOK() {
        msrpChatActivity.dismissDialog(DIALOG_ID);
        
        DialogMsrpFileProgress.showDialog(msrpChatActivity, selectedFile);
    }
    
    private void clearFileList() {
        LinearLayout msrpFileListLayout = (LinearLayout) findViewById(R.id.msrp_file_list_scroll_layout);
        msrpFileListLayout.removeAllViews();
    }
    
    private void addItemToFileList(final String itemName, final File file) {
        LinearLayout msrpFileListLayout = (LinearLayout) findViewById(R.id.msrp_file_list_scroll_layout);
        
        TextView textView = new TextView(msrpChatActivity);
        textView.setText(itemName);
        textView.setTextColor(Color.BLACK);
        
        if (file.isDirectory()) {
            textView.setOnClickListener(
                new View.OnClickListener() {
                    
                    public void onClick(View v) {
                        browseDir(file);
                    }
                }
            );
        } else {
            textView.setOnClickListener(
                new View.OnClickListener() {
                    
                    public void onClick(View v) {
                        selectFile(file);
                        findViewById(R.id.msrp_file_list_ok_btn).setEnabled(true);
                    }
                }
            );
        }
        
        msrpFileListLayout.addView(textView);
    }
    
    private void browseDir(File dir) {
        clearFileList();
        
        if (dir.isDirectory()) {
            File[] listFiles = dir.listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    addItemToFileList((file.isDirectory() ? "Dir " : "    ") + file.getPath(), file);
                }
            }
        } else {
            throw new IllegalStateException("This is not a dirrectory! " + dir.getPath());
        }
    }
    
    private void selectFile(File file) {
        selectedFile = file;
        ((TextView)findViewById(R.id.msrp_file_select_state)).setText("Selected: " + (file != null ? file.getPath() : ""));
    }

}
