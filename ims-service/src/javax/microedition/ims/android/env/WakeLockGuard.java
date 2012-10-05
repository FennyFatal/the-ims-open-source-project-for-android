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

/**
 * 
 * @author Andrei Khomushko
 *
 */
package javax.microedition.ims.android.env;

import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.util.concurrent.atomic.AtomicLong;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Shutdownable;

public class WakeLockGuard implements Shutdownable{
    private static final String LOG_TAG = "WakeLockGuard"; 
    
    private final WakeLock wakeLock;
    private final AtomicLong refCounter = new AtomicLong(0);
    
    WakeLockGuard(WakeLock wakeLock) {
        this.wakeLock = wakeLock;
        
    }

    public void acquire() {
        Logger.log(LOG_TAG, "acquire#wake lock acquire request was sent");
        wakeLock.acquire();
        Logger.log(LOG_TAG, "acquire#refCount = " + refCounter.incrementAndGet());
    }
    
    public void release() {
        Logger.log(LOG_TAG, "release#wake lock release request was sent");
        wakeLock.release();
        Logger.log(LOG_TAG, "release#refCount = " + refCounter.decrementAndGet());
    }
    
    @Override
    public void shutdown() {
        long refCount = refCounter.get();
        Logger.log(LOG_TAG, "shutdown#refCount = " + refCount);
        
        if(refCount > 0) {
            Logger.log(LOG_TAG, String.format("shutdown#there are a %s references to wake lock, power leak(battery life) is possible", refCount));    
        }
        
        
        //safety net for releasing wake log 
        for(int i = 0; i < refCounter.get(); i++) {
            wakeLock.release();
        }
    }

    @Override
    public String toString() {
        return "WakeLockGuard [wakeLock=" + wakeLock + ", refCounter=" + refCounter + "]";
    }
}
