package javax.microedition.ims.core.msrp.filetransfer;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 1/26/11
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileAlreadyExistsException extends IOException{
    private static final long serialVersionUID = 1L;

    public FileAlreadyExistsException() {
    }

    public FileAlreadyExistsException(String message) {
        super(message);
    }
}
