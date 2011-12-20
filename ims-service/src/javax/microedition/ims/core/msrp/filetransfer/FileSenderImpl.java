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

import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.common.streamutil.ChunkReceiver;
import javax.microedition.ims.common.streamutil.FixedSizeSplitter;
import javax.microedition.ims.common.streamutil.Splitter;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.InitiateParty;
import javax.microedition.ims.core.msrp.MSRPSessionType;
import javax.microedition.ims.core.msrp.listener.MSRPFileSendingListener;
import javax.microedition.ims.core.msrp.listener.MSRPFileSendingProgressListener;
import javax.microedition.ims.core.msrp.listener.MSRPMessageStatusEvent;
import javax.microedition.ims.core.msrp.listener.MSRPMessageStatusListener;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 18.5.2010
 * Time: 12.28.04
 */
public class FileSenderImpl implements FileSender, Shutdownable, FileSenderListenerSupport {

    private final static String TAG = "FileSenderImpl";

    public static interface MessageSender {
        void sendMessage(MsrpMessage msrpMessage, boolean needProgress);
    }

    private final ListenerHolder<MSRPFileSendingListener> msrpFileSendingListenerHolder
            = new ListenerHolder<MSRPFileSendingListener>(MSRPFileSendingListener.class);

    private final ListenerHolder<MSRPFileSendingProgressListener> msrpFileTransferProgressListenerHolder
            = new ListenerHolder<MSRPFileSendingProgressListener>(MSRPFileSendingProgressListener.class);

    private final ListenerHolder<MSRPMessageStatusListener> msrpMessageStatusListenerHolder;

    private final Map<FileDescriptor, MSRPMessageStatusListener> listenerMap =
            Collections.synchronizedMap(new HashMap<FileDescriptor, MSRPMessageStatusListener>(10));

    private final MessageSender messageSender;

    private final Configuration iConfiguration;

    public FileSenderImpl(
            final MessageSender messageSender,
            final ListenerHolder<MSRPMessageStatusListener> msrpMessageStatusListenerHolder, Configuration iConfiguration) {
        this.messageSender = messageSender;
        this.msrpMessageStatusListenerHolder = msrpMessageStatusListenerHolder;
        this.iConfiguration = iConfiguration;
    }

    
    public void addMSRPFileSendingListener(MSRPFileSendingListener listener) {
        msrpFileSendingListenerHolder.addListener(listener);
    }

    
    public void removeMSRPFileSendingListener(MSRPFileSendingListener listener) {
        msrpFileSendingListenerHolder.removeListener(listener);
    }

    
    public void addMSRPFileSendingProgressListener(MSRPFileSendingProgressListener listener) {
        msrpFileTransferProgressListenerHolder.addListener(listener);
    }

    
    public void removeMSRPFileSendingProgressListener(MSRPFileSendingProgressListener listener) {
        msrpFileTransferProgressListenerHolder.removeListener(listener);
    }


    private class FileTransferListener implements MSRPMessageStatusListener {
        private final ChunkDataHolder chunkDataHolder;
        private final FileDescriptor fileDescriptor;

        public FileTransferListener(final ChunkDataHolder chunkDataHolder, final FileDescriptor fileDescriptor) {
            this.chunkDataHolder = chunkDataHolder;
            this.fileDescriptor = fileDescriptor;
        }

        
        public void messageDeliveredSuccessfully(final MSRPMessageStatusEvent event) {
            Logger.log(TAG, "FileTransferListener.messageDeliveredSuccessfully()");

            ChunkDataHolder.MessageChunkDataPair dataPair =
                    chunkDataHolder.remove(event.getMsrpMessage());

            if (dataPair != null) {
                long bytesTransferred = dataPair.getChunkData().getLastByteNumber();
                long bytesTotal = fileDescriptor.getFileSize();

                msrpFileTransferProgressListenerHolder.getNotifier().onBytesTransfered(
                        fileDescriptor, bytesTransferred, bytesTotal);

                if (ChunkReceiver.ChunkData.Type.LAST == dataPair.getChunkData().getChunkType()) {
                    msrpFileSendingListenerHolder.getNotifier().onFileSent(fileDescriptor);

                    //ensure this chunk is really last
                    assert chunkDataHolder.isEmpty();
                }
                else if (ChunkReceiver.ChunkData.Type.CANCELLING == dataPair.getChunkData().getChunkType()) {
                    //ensure this chunk is really last
                    assert chunkDataHolder.isEmpty();

                }
                else if (ChunkReceiver.ChunkData.Type.ORDINARY == dataPair.getChunkData().getChunkType()) {
                    ChunkDataHolder.MessageChunkDataPair nextPair =
                            chunkDataHolder.getFirstAvailable();
                    if (nextPair != null) {
                        messageSender.sendMessage(nextPair.getMsrpMessage(), false);
                    }
                }
            }
        }

        
        public void messageDeliveryFailed(MSRPMessageStatusEvent event) {
            Logger.log(TAG, "FileTransferListener.messageDeliveryFailed()");

            ChunkDataHolder.MessageChunkDataPair dataPair =
                    chunkDataHolder.remove(event.getMsrpMessage());

            if (dataPair != null) {
                if (!chunkDataHolder.isCancelled()) {
                    chunkDataHolder.cancel(InitiateParty.REMOTE);

                    // notify listener about file transfer cancelled.
                    int code = 0;
                    String reasonPhrase = "transaction failed without response message";
                    if (event.getTriggeringMessage() != null) {
                        code = event.getTriggeringMessage().getCode();
                        reasonPhrase = event.getTriggeringMessage().getReasonPhrase();
                    }
                    msrpFileSendingListenerHolder.getNotifier().onFileSendFailed(
                            fileDescriptor,
                            code,
                            reasonPhrase);
                }

                cleanUp(fileDescriptor);
            }
        }
    }


    
    public void sendFile(final FileDescriptor fileDescriptor) throws IOException {

        if (ChunkDataHolderImpl.find(fileDescriptor) != null) {
            throw new IllegalStateException("File sending is already in progress: " + fileDescriptor);
        }

        final ChunkDataHolder chunkDataHolder = new ChunkDataHolderImpl(
                new ChunkDataHolderImpl.MesageProvider() {
                    
                    public MsrpMessage obtain(ChunkReceiver.ChunkData chunkData) {
                        return FileSendHelper.createMessageForFileChunk(chunkData, fileDescriptor);
                    }
                }
        );

        synchronized (listenerMap) {
            ChunkDataHolderImpl.add(fileDescriptor, chunkDataHolder);
            //TODO: add file transfer type listener
            final FileTransferListener transferListener = new FileTransferListener(chunkDataHolder, fileDescriptor);
            msrpMessageStatusListenerHolder.addListener(transferListener, MSRPSessionType.FILE_OUT);
            listenerMap.put(fileDescriptor, transferListener);
        }

        Splitter splitter = new FixedSizeSplitter(iConfiguration.getMsrpChunkSize());

        splitter.split(fileDescriptor.getFilePath(),
                new ChunkReceiver() {

                    
                    public void onNextChunk(final ChunkData chunkData) {
                        if (!chunkDataHolder.isCancelled()) {
                            MsrpMessage msrpMessage = FileSendHelper.createMessageForFileChunk(chunkData, fileDescriptor);


                            if (chunkDataHolder.wasEmptyAndPut(msrpMessage, chunkData)) {
                                messageSender.sendMessage(
                                        chunkDataHolder.getFirstAvailable().getMsrpMessage(),
                                        false
                                );
                            }
                        }
                    }

                    public boolean needSleep() {
                        return chunkDataHolder.size() > 10;
                    }
                }
        );

    }

    
    public void cancelFileSending(final FileDescriptor fileDescriptor) {
        final ChunkDataHolder chunkDataHolder = ChunkDataHolderImpl.find(fileDescriptor);

        if (chunkDataHolder != null) {
            final ChunkDataHolder.MessageChunkDataPair dataPair = chunkDataHolder.cancel(InitiateParty.LOCAL);
            if (dataPair != null) {
                messageSender.sendMessage(
                        chunkDataHolder.getFirstAvailable().getMsrpMessage(),
                        false
                );
            }

        }

        msrpFileSendingListenerHolder.getNotifier().onFileSendFailed(
                fileDescriptor,
                0,
                "Cancelled by USER");

        cleanUp(fileDescriptor);
    }


    private void cleanUp(final FileDescriptor fileDescriptor) {

        synchronized (listenerMap) {
            ChunkDataHolderImpl.remove(fileDescriptor);
            final MSRPMessageStatusListener statusListener = listenerMap.remove(fileDescriptor);
            msrpMessageStatusListenerHolder.removeListener(statusListener);
        }
    }


    
    public void shutdown() {
        msrpFileTransferProgressListenerHolder.shutdown();
        msrpFileSendingListenerHolder.shutdown();

        Map<FileDescriptor, MSRPMessageStatusListener> listenerMapCopy;

        synchronized (listenerMap) {
            listenerMapCopy = new HashMap<FileDescriptor, MSRPMessageStatusListener>(listenerMap);
        }

        for (FileDescriptor fileDescriptor : listenerMapCopy.keySet()) {
            cleanUp(fileDescriptor);
        }
    }


}
