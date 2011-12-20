package javax.microedition.ims.core.env;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 12/10/10
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class HardwareInfoDefaultImpl implements HardwareInfo {
    private static final String TEST_IMEI = "111111112222223";
    private static final String TEST_DEVICE_SOFTWARE_VERSION = "1111111122222242";

    public HardwareInfoDefaultImpl() {
    }

    public String getDeviceId() {
        return TEST_IMEI;
    }

    public String getDeviceSoftwareVersion() {
        return TEST_DEVICE_SOFTWARE_VERSION;
    }
}
