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

package javax.microedition.ims.common.streamutil;

import java.util.Arrays;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 13.5.2010
 * Time: 20.22.52
 */
public class ChunkDataContainer implements ChunkReceiver.ChunkData {
    private final int chunkNumber;
    private final byte[] chunkBytes;
    private final long firstByteNumber;
    private final long lastByteNumber;
    private final Type chunkType;

    public ChunkDataContainer(
            final int chunkNumber,
            final byte[] chunkBytes,
            final long firstByteNumber,
            final long lastByteNumber,
            final Type chunkType) {

        this.chunkNumber = chunkNumber;
        this.chunkBytes = chunkBytes;
        this.firstByteNumber = firstByteNumber;
        this.lastByteNumber = lastByteNumber;
        this.chunkType = chunkType;
    }

    private ChunkDataContainer(
            final ChunkReceiver.ChunkData data,
            final Type chunkType) {

        this(
                data.getChunkNumber(),
                data.getChunkBytes(),
                data.getFirstByteNumber(),
                data.getLastByteNumber(),
                chunkType
        );
    }

    
    public int getChunkNumber() {
        return chunkNumber;
    }

    
    public byte[] getChunkBytes() {
        return chunkBytes;
    }

    public long getFirstByteNumber() {
        return firstByteNumber;
    }

    public long getLastByteNumber() {
        return lastByteNumber;
    }

    
    public Type getChunkType() {
        return chunkType;
    }

    public static ChunkReceiver.ChunkData convertToType(final ChunkReceiver.ChunkData chunkData, final Type type) {
        return chunkData == null || chunkData.getChunkType() == type ?
                chunkData :
                new ChunkDataContainer(chunkData, type);
    }

    public static ChunkReceiver.ChunkData convertToCancelling(ChunkReceiver.ChunkData chunkData) {
        return chunkData == null ?
                chunkData :
                new ChunkDataContainer(
                        chunkData.getChunkNumber() + 1,
                        new byte[0],
                        chunkData.getFirstByteNumber(),
                        chunkData.getLastByteNumber(),
                        Type.CANCELLING
                );
    }

    
    public String toString() {
        return "ChunkDataContainer{" +
                "chunkNumber=" + chunkNumber +
                ", chunkBytes=" + Arrays.toString(chunkBytes) +
                ", firstByteNumber=" + firstByteNumber +
                ", lastByteNumber=" + lastByteNumber +
                ", chunkType=" + chunkType +
                '}';
    }
}
