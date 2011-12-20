package javax.microedition.ims.engine.test.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import javax.microedition.ims.engine.test.R;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactsHolder {
    private static final String TAG = "ContactsHelper"; 
    
    private static final int HARDCODED_CONTACTS = R.array.remote_parties;
    private static final String HARDCODED_CONTACTS_PATH; 
    
    private static ContactsHolder instance;
    
    private final List<String> contacts = new ArrayList<String>();
    
    static {
        HARDCODED_CONTACTS_PATH = Environment.getExternalStorageDirectory() + "/contact.ini";
    }
    
    private ContactsHolder(Context context) {
        readContacts(context);
    }
    
    private void readContacts(Context context) {
        String[] hardcodedContacts = readHardcodedContacts(context);
        contacts.addAll(Arrays.asList(hardcodedContacts));
        
        String[] importedContacts = readImportedContacts(context);
        contacts.addAll(Arrays.asList(importedContacts));
    }

    private String[] readHardcodedContacts(Context context) {
        return context.getResources().getStringArray(HARDCODED_CONTACTS);
    }
    
    private String[] readImportedContacts(Context context) {
        List<String> importedContacts = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream(HARDCODED_CONTACTS_PATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            
            String line;
            
            // read every line of the file into the line-variable, on line at the time
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if("".equals(line) || line.startsWith("#")) {
                    continue;
                }
                importedContacts.add(line);
            }
       
            reader.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Can't retrieve contacts: " + e.getMessage());
        } catch (IOException e) {
            Log.i(TAG, "Can't read contacts: " + e.getMessage());
        }
        return importedContacts.toArray(new String[0]);
    }

    private static ContactsHolder getInstance(Context context) {
        if(instance == null) {
            instance = new ContactsHolder(context);
        }
        
        return instance;
    }
    
    public static String[] getContacts(Context context) {
        return getInstance(context).contacts.toArray(new String[0]);
    }
}
