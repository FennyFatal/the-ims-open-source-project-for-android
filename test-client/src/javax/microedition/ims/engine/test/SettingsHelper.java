package javax.microedition.ims.engine.test;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsHelper {
    private static String SHARED_PREFERENCES_NAME = "javax.microedition.ims.engine.test_preferences";
    
    private SettingsHelper() {
        assert false;
    }
    
/*    public static DtfmPayload extractDtfmPayload(Context context) {
        SharedPreferences mSharedPreferences = extractPreferences(context);
        
        String dtfmPayloadKey = context.getResources().getString(R.string.dtfmpayload_preferences_category);
        String defValue = context.getResources().getString(R.string.def_dtfmpayload_preferences_category);

        String dtfmPlayloadValue = mSharedPreferences.getString(dtfmPayloadKey, defValue);
        DtfmPayload dtfmPayload = DtfmPayload.parse(dtfmPlayloadValue);        
        return dtfmPayload;
    }
*/
    public static SharedPreferences extractPreferences(Context context) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
        return mSharedPreferences;
    }

}
