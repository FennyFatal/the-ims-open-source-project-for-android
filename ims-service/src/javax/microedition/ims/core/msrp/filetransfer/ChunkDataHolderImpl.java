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

import javax.microedition.ims.common.streamutil.ChunkDataContainer;
import javax.microedition.ims.common.streamutil.ChunkReceiver;
import javax.microedition.ims.core.InitiateParty;
import javax.microedition.ims.messages.wrappers.msrp.ChunkTerminator;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 13.5.2010
 * Time: 20.59.32
 */
public class ChunkDataHolderImpl implements ChunkDataHolder {

    public static interface MesageProvider {
        MsrpMessage obtain(ChunkReceiver.ChunkData chunkData);
    }

    private static final Map<FileDescriptor, ChunkDataHolder> DATA_HOLDER_MAP
            = Collections.synchronizedMap(new HashMap<FileDescriptor, ChunkDataHolder>(10));

    public static void add(final FileDescriptor fileDescriptor, final ChunkDataHolder dataHolder) {
        DATA_HOLDER_MAP.put(fileDescriptor, dataHolder);
    }

    public static ChunkDataHolder remove(final FileDescriptor fileDescriptor) {
        return DATA_HOLDER_MAP.remove(fileDescriptor);
    }

    public static ChunkDataHolder find(final FileDescriptor fileDescriptor) {
        return DATA_HOLDER_MAP.get(fileDescriptor);
    }

    private static class MessageChunkDataPairContainer implements MessageChunkDataPair {
        private final MsrpMessage msrpMessage;
        private final ChunkReceiver.ChunkData chunkData;

        private MessageChunkDataPairContainer(final MsrpMessage msrpMessage, final ChunkReceiver.ChunkData chunkData) {
            this.chunkData = chunkData;
            this.msrpMessage = msrpMessage;
        }

        
        public MsrpMessage getMsrpMessage() {
            return msrpMessage;
        }

        
        public ChunkReceiver.ChunkData getChunkData() {
            return chunkData;
        }

        
        public String toString() {
            return "MessageChunkDataPairContainer{" +
                    "chunkData=" + chunkData +
                    ", msrpMessage=" + msrpMessage +
                    '}';
        }
    }

    private final Map<String, MessageChunkDataPair> dataMap;
    private final MesageProvider mesageProvider;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicReference<MessageChunkDataPair> lastUsedChunk = new AtomicReference<MessageChunkDataPair>(null);


    public ChunkDataHolderImpl(final MesageProvider mesageProvider) {
        this.mesageProvider = mesageProvider;
        dataMap = Collections.synchronizedMap(new LinkedHashMap<String, MessageChunkDataPair>(100));
    }

    
    public MessageChunkDataPair cancel(InitiateParty initiateParty) {

        MessageChunkDataPair retValue = null;

        synchronized (dataMap) {
            final MessageChunkDataPair firstAvailable = doGetFirstAvailable();
            dataMap.clear();

            if (initiateParty == InitiateParty.LOCAL) {
                if (firstAvailable != null) {
                    final ChunkReceiver.ChunkData oldData = firstAvailable.getChunkData();
                    final ChunkReceiver.ChunkData newData = ChunkDataContainer.convertToType(
                            oldData,
                            ChunkReceiver.ChunkData.Type.CANCELLING
                    );
                    final MsrpMessage msrpMsg = firstAvailable.getMsrpMessage();
                    FileSendHelper.updateTerminator(msrpMsg, ChunkTerminator.ABORTED);

                    dataMap.put(msrpMsg.getMessageId(), new MessageChunkDataPairContainer(msrpMsg, newData));
                }
                else {
                    final MessageChunkDataPair lstUsedPair = lastUsedChunk.get();
                    final ChunkReceiver.ChunkData newData = ChunkDataContainer.convertToCancelling(
                            lstUsedPair.getChunkData()
                    );

                    final MsrpMessage msrpMsg = mesageProvider.obtain(newData);
                    dataMap.put(msrpMsg.getMessageId(), retValue = new MessageChunkDataPairContainer(msrpMsg, newData));
                }
            }

            cancelled.set(true);
        }

        return retValue;
    }

    
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    public int size() {
        return dataMap.size();
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    
    public MessageChunkDataPair getFirstAvailable() {
        return doGetFirstAvailable();
    }

    private MessageChunkDataPair doGetFirstAvailable() {
        synchronized (dataMap) {
            final MessageChunkDataPair[] chunkDataPairs = dataMap.values().toArray(new MessageChunkDataPair[dataMap.size()]);
            final MessageChunkDataPair retValue = chunkDataPairs == null || chunkDataPairs.length == 0 ? null : chunkDataPairs[0];

            if (retValue != null) {
                lastUsedChunk.set(retValue);
            }

            return retValue;
        }
    }

    
    public MessageChunkDataPair remove(final MsrpMessage msrpMessage) {
        return dataMap.remove(msrpMessage.getTransactionId());
    }

    
    public boolean wasEmptyAndPut(final MsrpMessage msrpMessage, final ChunkReceiver.ChunkData chunkData) {

        synchronized (dataMap) {
            boolean retValue = false;

            if (!cancelled.get()) {

                retValue = dataMap.isEmpty();

                dataMap.put(
                        msrpMessage.getTransactionId(),
                        new MessageChunkDataPairContainer(msrpMessage, chunkData)
                );
            }

            return retValue;
        }
    }
}
