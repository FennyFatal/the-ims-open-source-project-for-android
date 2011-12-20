/*
 * This software code is (c) 2010 T-Mobile USA, Inc. All Rights Reserved.
 *
 * Unauthorized redistribution or further use of this material is
 * prohibited without the express permission of T-Mobile USA, Inc. and
 * will be prosecuted to the fullest extent of the law.
 *
 * Removal or modification of these Terms and Conditions from the source
 * or binary code of this software is prohibited.  In the event that
 * redistribution of the source or binary code for this software is
 * approved by T-Mobile USA, Inc., these Terms and Conditions and the
 * above copyright notice must be reproduced in their entirety and in all
 * circumstances.
 *
 * No name or trademarks of T-Mobile USA, Inc., or of its parent company,
 * Deutsche Telekom AG or any Deutsche Telekom or T-Mobile entity, may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" AND "WITH ALL FAULTS" BASIS
 * AND WITHOUT WARRANTIES OF ANY KIND.  ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS OR WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT CONCERNING THIS SOFTWARE, ITS SOURCE OR BINARY CODE
 * OR ANY DERIVATIVES THEREOF ARE HEREBY EXCLUDED.  T-MOBILE USA, INC.
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE
 * OR ITS DERIVATIVES.  IN NO EVENT WILL T-MOBILE USA, INC. OR ITS
 * LICENSORS BE LIABLE FOR LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF T-MOBILE USA,
 * INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * THESE TERMS AND CONDITIONS APPLY SOLELY AND EXCLUSIVELY TO THE USE,
 * MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE, ITS SOURCE OR BINARY
 * CODE OR ANY DERIVATIVES THEREOF, AND ARE SEPARATE FROM ANY WRITTEN
 * WARRANTY THAT MAY BE PROVIDED WITH A DEVICE YOU PURCHASE FROM T-MOBILE
 * USA, INC., AND TO THE EXTENT PERMITTED BY LAW.
 */

package javax.microedition.ims.core.msrp.filetransfer;

import javax.microedition.ims.common.*;
import javax.microedition.ims.core.env.Environment;
import javax.microedition.ims.core.msrp.listener.IncomingMSRPMessageEvent;
import javax.microedition.ims.core.msrp.listener.IncomingMSRPMessageListener;
import javax.microedition.ims.core.msrp.listener.MSRPFileReceivingListener;
import javax.microedition.ims.core.msrp.listener.MSRPFileReceivingProgressListener;
import javax.microedition.ims.messages.wrappers.msrp.ChunkTerminator;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 19.5.2010
 * Time: 15.40.04
 */
public class FileReceiverImpl implements IncomingMSRPMessageListener, FileReceiverListenerSupport, Shutdownable {

    private final ListenerHolder<MSRPFileReceivingListener> msrpFileReceivingListenerHolder
            = new ListenerHolder<MSRPFileReceivingListener>(MSRPFileReceivingListener.class);

    private final ListenerHolder<MSRPFileReceivingProgressListener> msrpFileReceivingProgressListenerHolder
            = new ListenerHolder<MSRPFileReceivingProgressListener>(MSRPFileReceivingProgressListener.class);

    private Map<String, FileDataAccumulator> dataAccumulators = new ConcurrentHashMap<String, FileDataAccumulator>();

    private final Object mutex = new Object();
    //private final List<FileDescriptor> fileDescriptors;
    private final List<FileDescriptor> fileDescriptorsToAssign;
    private final Map<String, FileDescriptor> msgIdToFileDescriptorMap = new HashMap<String, FileDescriptor>();
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final Environment environment;
    private final SubsequentNumberGenerator numberGenerator = new DefaultSubsequentNumberGenerator(0);


    public FileReceiverImpl(
            final List<FileDescriptor> fileDescriptors,
            final Environment environment
    ) {
        this.environment = environment;
        //this.fileDescriptors = new ArrayList<FileDescriptor>(fileDescriptors);
        this.fileDescriptorsToAssign = new ArrayList<FileDescriptor>(fileDescriptors);
    }


    public void addMSRPFileReceivingListener(MSRPFileReceivingListener listener) {
        if (!done.get()) {
            msrpFileReceivingListenerHolder.addListener(listener);
        }
    }


    public void removeMSRPFileReceivingListener(MSRPFileReceivingListener listener) {
        msrpFileReceivingListenerHolder.removeListener(listener);
    }


    public void addMSRPFileReceivingProgressListener(MSRPFileReceivingProgressListener listener) {
        if (!done.get()) {
            msrpFileReceivingProgressListenerHolder.addListener(listener);
        }
    }


    public void removeMSRPFileReceivingProgressListener(MSRPFileReceivingProgressListener listener) {
        msrpFileReceivingProgressListenerHolder.removeListener(listener);
    }


    public void onMessageReceived(final IncomingMSRPMessageEvent event) {
        if (!done.get()) {
            try {
                handleOnMessageReceived(event);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    public void onFileMessageReceived(IncomingMSRPMessageEvent event) {
        if (!done.get()) {
            try {
                handleOnFileMessageReceived(event);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void onComposingIndicatorReceived(IncomingMSRPMessageEvent event) {
        //do nothing
    }

    private void handleOnMessageReceived(IncomingMSRPMessageEvent event) throws IOException {
        final MsrpMessage message = event.getMsg();
        final String msgId = message.getMessageId();

        if (msgId != null) {

            if (message.getTotalSize() > 0) {
                final ChunkTerminator terminator = message.getTerminator();

                FileDescriptor fileDescriptor = obtainDescriptorForMessage(msgId);
                if (fileDescriptor != null) {
                    if (terminator == ChunkTerminator.FINISHED || terminator == ChunkTerminator.ABORTED) {
                        msgIdToFileDescriptorMap.put(msgId, null);
                    }

                    FileDataAccumulator fileDataAccumulator = processMessage(message, terminator, fileDescriptor);
                    notifyListeners(message, terminator, fileDescriptor, fileDataAccumulator.getPathname());

                } else {
                    assert !msgIdToFileDescriptorMap.containsKey(msgId) : "Messages with id = " + msgId + " were finished or aborted earlier.";
                    assert false : "some unknown problem with file receiving. Message id " + msgId + " for unknown fileDescriptor.";
                }
            }

        } else {
            assert false : "Every message must contain messageId. This message messageId is " +
                    msgId + " message: " + message;
        }
    }

    private void handleOnFileMessageReceived(IncomingMSRPMessageEvent event) throws IOException {
        final MsrpMessage message = event.getMsg();
        final String msgId = message.getMessageId();

        if (msgId != null) {

            if (message.getTotalSize() > 0) {
                final ChunkTerminator terminator = message.getTerminator();

                String contentType = message.getContentType();
                MimeType mimeType = MimeType.parse(contentType);

                if (mimeType != null) {
                    String fileId = "" + System.currentTimeMillis() + "_" + numberGenerator.next();
                    final String fileName = mimeType.getMimeTypeClass().stringValue() + "_" + fileId + ".jpg";

                    final Date today = new Date();

                    final FileDescriptor fileDescriptor = new FileDescriptor.FileDescriptorBuilder()
                            .fileName(fileName)
                            .fileSize(message.getBody() == null ? 0 : message.getBody().length)
                            .contentType(message.getContentType())
                            .hash("" + fileId.hashCode())
                            .fileId(fileId)
                            .creationDate(today)
                            .modificationDate(today)
                            .build();

                    FileDataAccumulator fileDataAccumulator = processMessage(message, terminator, fileDescriptor);
                    notifyListeners(message, terminator, fileDescriptor, fileDataAccumulator.getPathname());
                } else {
                    Logger.log(Logger.Tag.WARNING, "Unsupported content type for chat session file transfer: " + contentType);
                }
            }

        } else {
            assert false : "Every message must contain messageId. This message messageId is " +
                    msgId + " message: " + message;
        }
    }

    public void cancelFileReceiving(FileDescriptor fileDescriptor) {
        msrpFileReceivingListenerHolder.getNotifier().onFileReceiveFailed(
                fileDescriptor,
                0,
                "Cancelled by Receiving USER", true);
    }


    private FileDataAccumulator processMessage(
            final MsrpMessage message,
            final ChunkTerminator terminator,
            final FileDescriptor fileDescriptor) throws IOException {

        FileDataAccumulator retValue;

        retValue = dataAccumulators.get(fileDescriptor.getFileId());
        if (retValue == null) {
            retValue = new FileDataAccumulator(
                    fileDescriptor.getFileName(),
                    environment.getExternalStorageDirectory().getPath()
            );

            dataAccumulators.put(fileDescriptor.getFileId(), retValue);
        }
        retValue.processDataChunk(message.getBody());

        if (terminator == ChunkTerminator.FINISHED) {
            FileDataAccumulator fileDataAccumulator = dataAccumulators.remove(fileDescriptor.getFileId());
            if (fileDataAccumulator != null) {
                fileDataAccumulator.shutdown();
            }
        } else if (terminator == ChunkTerminator.ABORTED) {
            FileDataAccumulator fileDataAccumulator = dataAccumulators.remove(fileDescriptor.getFileId());
            if (fileDataAccumulator != null) {
                fileDataAccumulator.shutdown();
            }
        }

        return retValue;
    }


    private void notifyListeners(
            final MsrpMessage message,
            final ChunkTerminator terminator,
            final FileDescriptor fileDescriptor,
            final String pathName) {

        msrpFileReceivingProgressListenerHolder.getNotifier().onBytesReceived(
                fileDescriptor,
                message.getPrevProgress() + message.getBody().length - 1,
                (int) message.getTotalSize(),
                message.getBody()
        );

        if (terminator == ChunkTerminator.FINISHED) {
            msrpFileReceivingListenerHolder.getNotifier().onFileReceived(fileDescriptor, pathName);

        } else if (terminator == ChunkTerminator.ABORTED) {
            msrpFileReceivingListenerHolder.getNotifier().onFileReceiveFailed(
                    fileDescriptor,
                    0,
                    "",
                    false
            );
        }
    }

    private FileDescriptor obtainDescriptorForMessage(String msgId) {
        if (!msgIdToFileDescriptorMap.containsKey(msgId)) {
            if (fileDescriptorsToAssign.size() > 0) {
                final FileDescriptor nextFileDescriptor = fileDescriptorsToAssign.remove(0);
                msgIdToFileDescriptorMap.put(msgId, nextFileDescriptor);
            } else {
//                assert false : "Unknown message Id(" + msgId + ") appeared. There are " + fileDescriptors.size() +
//                        " files to be received in this SESSION and " + msgIdToFileDescriptorMap.size() +
//                        " message id's already received. No free file descriptors available to associate with this message Id.";
                return null;
            }
        }

        return msgIdToFileDescriptorMap.get(msgId);
    }


    public void shutdown() {

        if (done.compareAndSet(false, true)) {

            synchronized (mutex) {
                fileDescriptorsToAssign.clear();
                msgIdToFileDescriptorMap.clear();
            }

            Map<String, FileDataAccumulator> copyMap = new HashMap<String, FileDataAccumulator>(dataAccumulators);
            dataAccumulators.clear();

            for (String next : copyMap.keySet()) {
                FileDataAccumulator fileDataAccumulator = copyMap.get(next);
                fileDataAccumulator.shutdown();
            }

            msrpFileReceivingListenerHolder.shutdown();
            msrpFileReceivingProgressListenerHolder.shutdown();
        }
    }

}
