package javax.microedition.ims.core.msrp.filetransfer;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Shutdownable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 1/26/11
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
/*
  * chunk by chunk file writer
  */
class FileDataAccumulator implements Shutdownable {

    private static final String TAG = "Service - FileTransferManagerImpl";

    private final AtomicReference<OutputStream> outputStream = new AtomicReference<OutputStream>(null);
    private final String fileName;
    private final String pathname;

    private final AtomicBoolean done = new AtomicBoolean(false);

    public FileDataAccumulator(final String fileName, final String folderName) {
        this.fileName = fileName;

        //String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
        String folder = folderName;
        if (!folder.endsWith(File.separator)) {
            folder += File.separator;
        }
        /*boolean folderWasCreated = */new File(folder).mkdirs();

        this.pathname = folder + fileName;
    }

    public void processDataChunk(byte[] dataChunk) throws IOException {
        if (!done.get()) {
            Logger.log(TAG, "FileDataAccumulator.processDataChunk#started");
            obtainOutputStream().write(dataChunk);
            Logger.log(TAG, "FileDataAccumulator.processDataChunk#finished");
        }
    }

    private OutputStream obtainOutputStream() throws IOException {

        //try to check ref holder
        OutputStream retValue = outputStream.get();

        //if there no reference we have to create one
        if (retValue == null) {

            //ref holder for temp value
            OutputStream tempOutputStream;

            //there we create actual stream and check if it spurious
            if (outputStream.compareAndSet(null, tempOutputStream = createOutputStream())) {
                retValue = tempOutputStream;
            } else {
                //close spurious stream here
                try {
                    closeOutputStream(tempOutputStream);
                } catch (IOException e) {
                    Logger.log(Logger.Tag.WARNING, "Error during closing stream for " + pathname + ". " + e);
                }
            }

        }

        return retValue;
    }

    private OutputStream createOutputStream() throws IOException {

        OutputStream retValue;

        Logger.log(TAG, "FileDataAccumulator.prepareOutputStream#started");

        File file = new File(pathname);
        /*boolean fileWasCreated = */file.createNewFile();

        /*
        if (!fileWasCreated) {
            throw new FileAlreadyExistsException("File already exists " + pathname);
        }
        */

        retValue = new FileOutputStream(file);
        Logger.log(TAG, "FileDataAccumulator.prepareOutputStream#finished");

        return retValue;
    }

    private void closeOutputStream(final OutputStream stream) throws IOException {
        stream.flush();
        stream.close();
    }

    public void shutdown() {
        if (done.compareAndSet(false, true)) {

            Logger.log(TAG, "FileDataAccumulator.close#started");

            try {
                closeOutputStream(outputStream.get());
            } catch (IOException e) {
                Logger.log(Logger.Tag.WARNING, "Can not close stream for " + pathname + ". " + e);
            }

            Logger.log(TAG, "FileDataAccumulator.close#finished");
        }
    }

    public String getPathname() {
        return pathname;
    }

    @Override
    public String toString() {
        return "FileDataAccumulator{" +
                "fileName='" + fileName + '\'' +
                ", pathname='" + pathname + '\'' +
                '}';
    }
}
