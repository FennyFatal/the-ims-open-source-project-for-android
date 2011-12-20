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

package javax.microedition.ims.core.sipservice;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.transaction.Transaction;
import javax.microedition.ims.core.transaction.TransactionEvent;
import javax.microedition.ims.core.transaction.TransactionListenerAdapter;
import javax.microedition.ims.core.transaction.TransactionResult;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.concurrent.TimeUnit;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 17.01.2010
 * Time: 18:06:01
 */
public class RefreshListener extends TransactionListenerAdapter<BaseSipMessage> {
    private final String operationName;
    private final Dialog.ParamKey paramKey;
    private final Dialog dialog;
    private final RefreshHelper.Refresher refresher;
    private final long expirationSeconds;

    private final RepetitiousTaskManager repetitiousTaskManager;

    public RefreshListener(
            final RepetitiousTaskManager repetitiousTaskManager,
            final String operationName,
            final Dialog.ParamKey paramKey,
            final Dialog dialog,
            final RefreshHelper.Refresher refresher,
            long expirationSeconds) {

        this.operationName = operationName;
        this.paramKey = paramKey;

        this.dialog = dialog;
        this.refresher = refresher;
        this.expirationSeconds = expirationSeconds;

        this.repetitiousTaskManager = repetitiousTaskManager;
    }

    
    public void onTransactionInit(final TransactionEvent<BaseSipMessage> event) {
        dialog.putCustomParameter(paramKey, expirationSeconds);
    }

    public void onTransactionComplete(TransactionEvent<BaseSipMessage> event, final TransactionResult.Reason reason) {
        final Transaction<Boolean, BaseSipMessage> transaction = event.getTransaction();
        final boolean registrationSuccessful = transaction.isComplete() && transaction.getTransactionValue().getValue();

        if (registrationSuccessful) {
            long expTimeInSeconds = RefreshHelper.getRegistrationExpireTime(
                    event.getInitialMessage(),
                    event.getLastInMessage(),
                    expirationSeconds
            );

            final String logMsg = operationName + " completed successfully. " + operationName +
                    " refresh will be held in " + expTimeInSeconds + " seconds";

            Logger.log(Logger.Tag.COMMON, logMsg);

            final long expTimeInMillis = TimeUnit.SECONDS.toMillis(expTimeInSeconds);
            RefreshHelper.scheduleRefresh(repetitiousTaskManager, operationName, expTimeInMillis, refresher);
        }
        else {
            final String logMsg = operationName + " failed. " + operationName + " refresh will not be scheduled";
            Logger.log(Logger.Tag.COMMON, logMsg);
        }
    }

    
    public String toString() {
        return "RefreshListener{" +
                "operationName='" + operationName + '\'' +
                ", paramKey=" + paramKey +
                ", DIALOG=" + dialog +
                ", expirationSeconds=" + expirationSeconds +
                '}';
    }
}
