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

import javax.microedition.ims.common.DefaultTimeoutUnit;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.common.TimeoutUnit;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.transaction.Transaction;
import javax.microedition.ims.core.transaction.TransactionManager;
import javax.microedition.ims.core.transaction.TransactionResult;
import java.util.concurrent.TimeUnit;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 10-Dec-2009
 * Time: 15:41:24
 */
public abstract class AbstractService implements Service, Shutdownable {

    protected static final DefaultTimeoutUnit TRANSACTION_TIMEOUT = new DefaultTimeoutUnit(RepetitiousTaskManager.TRANSACTION_TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
    protected static final DefaultTimeoutUnit LONG_TRANSACTION_TIMEOUT = new DefaultTimeoutUnit(RepetitiousTaskManager.LONG_TRANSACTION_TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);

    //private static final List<Transaction> trnsInProcess = Collections.synchronizedList(new ArrayList<Transaction>(10));

    private final TransactionManager transactionManager;
    private final StackContext stackContext;

    public AbstractService(final StackContext stackContext, final TransactionManager transactionManager) {
        this.stackContext = stackContext;
        if (transactionManager == null) {
            throw new IllegalArgumentException(transactionManager + " TransactionManager not allowed.");
        }
        this.transactionManager = transactionManager;
    }

    protected TransactionManager getTransactionManager() {
        return transactionManager;
    }

    protected StackContext getStackContext() {
        return stackContext;
    }

    /*
    //TODO: move code to TransactionManagerImpl. Make it shutdownable. Make it can return value.
    public static <T, V> void runAsynchronously(final Transaction<T, V> transaction, final TimeoutUnit timeoutUnit) {
        synchronized (trnsInProcess) {
            if (!trnsInProcess.contains(transaction)) {

                if (transaction.isComplete()) {
                    throw new IllegalArgumentException("Trying to run transaction which ia already complete.");
                }

                trnsInProcess.add(transaction);
                TransactionUtils.getExecutorService().execute(
                        new Runnable() {

                            public void run() {
                                try {
                                    transaction.append(timeoutUnit);
                                }
                                finally {
                                    synchronized (trnsInProcess) {
                                        trnsInProcess.remove(transaction);
                                    }
                                }
                            }
                        }
                );
            }
        }
    }
    */

    public static <T, V> void runAsynchronously(final Transaction<T, V> transaction, final TimeoutUnit timeoutUnit) {
        transaction.appendNoBlock(timeoutUnit);
    }

    public static <T, V> void runAsynchronously(final Transaction<T, V> transaction) {
        transaction.appendNoBlock();
    }

    protected <T, V> TransactionResult<T> runSynchronously(Transaction<T, V> transaction, final TimeoutUnit timeoutUnit) {
        return transaction.append(timeoutUnit);
    }


}
