package javax.microedition.ims.core.env;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 12/10/10
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Environment {
    ConnectionManager getConnectionManager();
    GsmLocationService getGsmLocationService();
    HardwareInfo getHardwareInfo();
    File getExternalStorageDirectory();
}
