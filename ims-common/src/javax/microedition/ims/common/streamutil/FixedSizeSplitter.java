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

import java.io.*;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 4.5.2010
 * Time: 18.40.26
 */
public class FixedSizeSplitter implements Splitter {

    private static final int DEFAULT_CHUNK_SIZE = 2048;
    private final int chunkSize;
    private final byte[] readBufferOne;
    private final byte[] readBufferTwo;


    public FixedSizeSplitter(int chunkSize) {

        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size can not be less than 1. Now it has size " + chunkSize);
        }

        this.chunkSize = chunkSize;
        this.readBufferOne = new byte[chunkSize];
        this.readBufferTwo = new byte[chunkSize];
    }

    public FixedSizeSplitter() {
        this(DEFAULT_CHUNK_SIZE);
    }

    
    public void split(InputStream streamToSplit, ChunkReceiver receiver) throws IOException {
        doSplit(streamToSplit, receiver);
    }

    
    public void split(File fileToSplit, ChunkReceiver receiver) throws IOException {
        doSplit(fileToSplit, receiver);
    }

    
    public void split(URI fileToSplit, ChunkReceiver receiver) throws IOException {
        doSplit(new File(fileToSplit), receiver);
    }

    
    public void split(String fileToSplit, ChunkReceiver receiver) throws IOException {
        doSplit(new File(fileToSplit), receiver);
    }

    private void doSplit(File fileToSplit, ChunkReceiver receiver) throws IOException {
        final InputStream inputStreamToSplit = new BufferedInputStream(
                new FileInputStream(fileToSplit),
                Math.max(chunkSize, DEFAULT_CHUNK_SIZE)
        );

        doSplit(inputStreamToSplit, receiver);
    }

    private void doSplit(InputStream streamToSplit, ChunkReceiver receiver) throws IOException {
        int bytesRedOne = 0;
        int bytesRedTwo = 0;

        int chunkNumber = 0;
        int firstByteNumber = 0;
        int lastByteNumber = 0;

        bytesRedOne = streamToSplit.read(readBufferOne);

        while ((bytesRedTwo = streamToSplit.read(readBufferTwo)) >= 0) {
            firstByteNumber = lastByteNumber + 1;
            lastByteNumber = firstByteNumber + bytesRedOne - 1;

            byte[] newChuck = new byte[bytesRedOne];
            System.arraycopy(readBufferOne, 0, newChuck, 0, bytesRedOne);

            receiver.onNextChunk(new ChunkDataContainer(
                    chunkNumber,
                    newChuck,
                    firstByteNumber,
                    lastByteNumber,
                    ChunkReceiver.ChunkData.Type.ORDINARY
            ));

            System.arraycopy(readBufferTwo, 0, readBufferOne, 0, bytesRedTwo);
            bytesRedOne = bytesRedTwo;

            chunkNumber++;

            while (receiver.needSleep()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            Thread.yield();
        }

        firstByteNumber = lastByteNumber + 1;
        lastByteNumber = firstByteNumber + bytesRedOne - 1;

        byte[] newChuck = new byte[bytesRedOne];
        System.arraycopy(readBufferOne, 0, newChuck, 0, bytesRedOne);

        receiver.onNextChunk(new ChunkDataContainer(
                chunkNumber,
                newChuck,
                firstByteNumber,
                lastByteNumber,
                ChunkReceiver.ChunkData.Type.LAST
        ));
    }

    //private static int comSize = 0;

    /*
    public static void main(String[] args) throws IOException {
        Splitter splitter = new FixedSizeSplitter(100);

        //final String fileToSplit = "c:\\boot.ini";
        final String fileToSplit = "c:\\AdobeTypeManager.rar";

        splitter.split(fileToSplit, new ChunkReceiver() {
            
            public void onNextChunk(int chunkNumber, byte[] chunk) {
                comSize += chunk.length;
                System.out.println("" + chunkNumber + ": " + new String(chunk));
            }
        });

        System.out.println("length: "+new File(fileToSplit).length()+" real leangth: "+comSize);
    }
    */
}
