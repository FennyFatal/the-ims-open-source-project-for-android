package javax.microedition.ims.engine.test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Settings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "Settings";

    private ListPreference mCodec;
    private ListPreference mCodecSize;
    //private ListPreference mDtfmPayload;
    
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCache();
    }

    public boolean onPreferenceChange(Preference preference, Object object) {
        if (preference == mCodec) {
            editor.putInt(getResources().getString(R.string.codec_preferences_category), mCodec.findIndexOfValue((String) object)).commit(); 
        } else if (preference == mCodecSize) {
            editor.putInt(getResources().getString(R.string.codecsize_preferences_category), mCodecSize.findIndexOfValue((String) object)).commit(); 
        } /*else if (preference == mDtfmPayload) {
            String key = getResources().getString(R.string.dtfmpayload_preferences_category);
            editor.putString(key, (String)object).commit();
            loadDtfmCache();
        }*/
        return false;
    }
    
    private void initSettings() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        mCodec = new ListPreference(this);
        mCodec.setTitle(R.string.codec_preferences_category);
        mCodec.setEntries(R.array.codec_preferences_entries);
        mCodec.setEntryValues(R.array.codec_preferences_values);
        mCodec.setOnPreferenceChangeListener(this);

        mCodecSize = new ListPreference(this);
        mCodecSize.setTitle(R.string.codecsize_preferences_category);
        mCodecSize.setEntries(R.array.codecsize_preferences_entries);
        mCodecSize.setEntryValues(R.array.codecsize_preferences_values);
        mCodecSize.setOnPreferenceChangeListener(this);

        //initDtfmPayload();

        root.addPreference(mCodec);
        root.addPreference(mCodecSize);
        //root.addPreference(mDtfmPayload);

        setPreferenceScreen(root);

        editor = root.getSharedPreferences().edit();
    }

/*    private void initDtfmPayload() {
        mDtfmPayload = new ListPreference(this);
        mDtfmPayload.setTitle(R.string.dtfmpayload_preferences_category);
        mDtfmPayload.setEntries(R.array.dtfmpayload_preferences_entries);
        mDtfmPayload.setEntryValues(R.array.dtfmpayload_preferences_values);
        mDtfmPayload.setOnPreferenceChangeListener(this);
    }
*/
    private void loadCache() {
        int index = getPreferenceScreen().getSharedPreferences().getInt(getResources().getString(R.string.codec_preferences_category), 0);
        mCodec.setValueIndex(index);

        index = getPreferenceScreen().getSharedPreferences().getInt(getResources().getString(R.string.codecsize_preferences_category), 0);
        mCodecSize.setValueIndex(index);
        
        //loadDtfmCache();
    }

/*    private void loadDtfmCache() {
        String dtfmPayloadValue = getDtfmPayload();
        mDtfmPayload.setSummary(dtfmPayloadValue);
        mDtfmPayload.setValue(dtfmPayloadValue);
    }
*/
/*    private String getDtfmPayload() {
        String defValue = getResources().getString(R.string.def_dtfmpayload_preferences_category);
        String dtfmPayloadKey = getResources().getString(R.string.dtfmpayload_preferences_category);
        String dtfmPayloadValue = getPreferenceScreen().getSharedPreferences().getString(dtfmPayloadKey, defValue);
        return dtfmPayloadValue;
    }
*/}
