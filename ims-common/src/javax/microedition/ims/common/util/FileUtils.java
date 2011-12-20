package javax.microedition.ims.common.util;

import javax.microedition.ims.common.Logger;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 1/27/11
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public final class FileUtils {
    private FileUtils() {
    }

    public static byte[] readAll(final File file) {
        byte[] retValue = null;

        if (file != null && file.exists() && file.isFile()) {

            long longLength = file.length();
            int length = longLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)longLength;

            retValue = new byte[length];

            InputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file), retValue.length);
                int redBytesNumber = in.read(retValue);

                assert redBytesNumber == length : "Error during file reading";

            } catch (IOException e) {
                retValue = null;
                Logger.log(Logger.Tag.WARNING, "Exception occurred during file reading. " + file + " " + e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Logger.log(Logger.Tag.WARNING, "Can't close stream for " + file);
                    }
                }
            }
        }

        return retValue;
    }
}
