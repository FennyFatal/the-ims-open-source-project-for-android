package javax.microedition.ims.android.env;

import android.content.Context;
import android.telephony.TelephonyManager;

import javax.microedition.ims.core.env.HardwareInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 12/13/10
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class HardwareInfoAndroidImpl implements HardwareInfo {

    //private final Context context;
    private final String deviceId;

    private final String deviceSoftwareVersion;

    public HardwareInfoAndroidImpl(Context context) {
        //this.context = context;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();

        //int cellid = loc.getCid();
        //int lac = loc.getLac();

        deviceId = tm.getDeviceId();
        deviceSoftwareVersion = tm.getDeviceSoftwareVersion();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceSoftwareVersion() {
        return deviceSoftwareVersion;
    }

    @Override
    public String toString() {
        return "HardwareInfoAndroidImpl [deviceId=" + deviceId + ", deviceSoftwareVersion="
                + deviceSoftwareVersion + "]";
    }
}
