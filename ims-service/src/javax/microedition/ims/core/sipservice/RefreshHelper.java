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
import javax.microedition.ims.common.ManagableScheduledFuture;
import javax.microedition.ims.common.NamedDaemonThreadFactory;
import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.Shutdownable;
import javax.microedition.ims.messages.wrappers.common.Uri;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.UriHeader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 15-Jan-2010
 * Time: 10:11:06
 */
public final class RefreshHelper {
    private static final int MINIMAL_REFRESH_DELAY = 5;
    private static final DateFormat LOG_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static interface Refresher {
        void refresh(long timeOutInMillis);
    }

    private static final String EXPIRES_URI_PARAMETER = "expires";

    private RefreshHelper() {
    }


    /**
     * return expire time in seconds
     *
     * @param initMessage
     * @param responseMessage
     * @return
     */
    public static long getRegistrationExpireTime(
            final BaseSipMessage initMessage,
            final BaseSipMessage responseMessage,
            final long expirationSeconds) {

        final List<UriHeader> uriHeaders = new ArrayList<UriHeader>(initMessage.getContacts().getContactsList());
        final Uri clientUri = uriHeaders.get(0).getUri();
        String clientName = clientUri.getUsername();
        String clientDomain = clientUri.getDomain();

        long expTime = responseMessage.getExpires();
        if (expTime < 0) {
            Collection<UriHeader> uriList = responseMessage.getContacts().getContactsList();

            for (UriHeader uriHeader : uriList) {
                final Uri contactUri = uriHeader.getUri();

                if (clientName.equalsIgnoreCase(contactUri.getUsername()) &&
                        clientDomain.equalsIgnoreCase(contactUri.getDomain()) &&
                        uriHeader.getParamsList().containsKey(EXPIRES_URI_PARAMETER)) {
                    try {
                        expTime = Long.parseLong(uriHeader.getParamsList().get(EXPIRES_URI_PARAMETER).getValue());
                        break;
                    }
                    catch (NumberFormatException e) {
                        //just do nothing.
                        Logger.log(e.getMessage() + " at getExpireTime");
                    }
                }
            }
        }
        return expTime < 0 ? expirationSeconds : expTime;
    }


    public static void scheduleRefresh(
            final RepetitiousTaskManager repetitiousTaskManager,
            final Object refreshIdentity,
            final long expTimeInMillis,
            final Refresher refresher) {

        long delay = expTimeInMillis - RepetitiousTaskManager.TRANSACTION_TIMEOUT_INTERVAL * 2;
        if (delay < 0) {
            delay = expTimeInMillis / 2;

            if (delay < MINIMAL_REFRESH_DELAY) {
                delay = MINIMAL_REFRESH_DELAY;
            }
        }
        Logger.log("scheduleRefresh: " + refreshIdentity + " " + delay + " " + refresher);

        repetitiousTaskManager.startDelayedTask(
                refreshIdentity,

                new RepetitiousTaskManager.Repeater<Object>() {

                    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
                            new NamedDaemonThreadFactory("Refresh timer")
                    );

                    @Override
                    public void onRepeat(final Object refreshIdentity, final Shutdownable task) {
                        Logger.log("onRepeat " + refreshIdentity);
                        executorService.submit(
                                new Runnable() {
                                    public void run() {
                                        Logger.log("new thread start .....");
                                        Thread.currentThread().setName("Refresh timer");
                                        refresher.refresh(RepetitiousTaskManager.TRANSACTION_TIMEOUT_INTERVAL);
                                        Logger.log("new thread end .....");
                                        executorService.shutdown();
                                        
                                        task.shutdown();
                                    }
                                }
                        );
                    }
                },

                delay
        );

        //Logger.log("scheduleRefresh: next run scheduled at " + DateFormat.format("hh:mm:ss", System.currentTimeMillis() + delay));
        Logger.log("scheduleRefresh: next run scheduled at " + LOG_DATE_FORMAT.format(new Date(System.currentTimeMillis() + delay)));

    }

    public static void cancelRefresh(final RepetitiousTaskManager repetitiousTaskManager, final Object refreshIdentity) {
        Logger.log("cancelRefresh: " + refreshIdentity);
        repetitiousTaskManager.cancelTask(refreshIdentity);
    }
}
