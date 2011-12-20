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

package javax.microedition.ims.core.sipservice.publish;

import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.ReasonInfo;
import javax.microedition.ims.core.sipservice.RefreshHelper;
import javax.microedition.ims.core.sipservice.publish.PublishServiceImpl.PublishRefresher;
import javax.microedition.ims.core.transaction.TransactionEvent;
import javax.microedition.ims.core.transaction.TransactionListenerAdapter;
import javax.microedition.ims.messages.utils.StatusCode;
import javax.microedition.ims.messages.wrappers.common.ResponseClass;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Response;

class PublishTransactionListener extends TransactionListenerAdapter<BaseSipMessage> {
    private final Dialog dialog;
    private final PublishRefresher refresher;
    private final long publishExpirationSeconds;
    private final PublishStateListener listener;
    private final String refreshIdentity = "PRESENCE_REFRESHER";

    private final RepetitiousTaskManager repetitiousTaskManager;

    public PublishTransactionListener(final RepetitiousTaskManager repetitiousTaskManager, final Dialog dialog,
                                      final PublishStateListener listener, final long publishExpirationSeconds, final PublishRefresher refresher) {
        this.dialog = dialog;
        this.listener = listener;
        this.publishExpirationSeconds = publishExpirationSeconds;
        this.refresher = refresher;

        this.repetitiousTaskManager = repetitiousTaskManager;
    }

    
    public void onTransactionInit(final TransactionEvent<BaseSipMessage> event) {
        dialog.putCustomParameter(Dialog.ParamKey.PUBLISH_EXPIRES, publishExpirationSeconds);
    }


    /*
     *
    If an EPA receives a 423 (Interval Too Brief) response to a PUBLISH
   request, it MAY retry the publication after changing the expiration
   interval in the Expires HEADER field to be equal to or greater than
   the expiration interval within the Min-Expires HEADER field of the
   423 (Interval Too Brief) response.
     */

    
    public void onIncomingMessage(final TransactionEvent<BaseSipMessage> event) {
        assert event.getLastInMessage() instanceof Response : "Wrong income message type";
        Response response = (Response) event.getLastInMessage();

        String etag = response.geteTag();
        ReasonInfo reasonInfo = new ReasonInfo(0, response.getReasonPhrase(), response.getStatusCode());
        if (response.getResponseClass() == ResponseClass.Success) {
            if (event.getInitialMessage().getExpires() == 0) {
                // response for unpublish
                RefreshHelper.cancelRefresh(repetitiousTaskManager, refreshIdentity);
                listener.publicationTerminated(new PublishStateEvent(dialog, etag, reasonInfo));
                /* } else  if(event.getInitialMessage().getIfMatch() != null){
             // response for publication refresh
             listener.publicationDelivered(new PublishStateEvent(dialog, etag, reasonInfo));*/
            }
            else {
                // response for initial publication           
                long expires = response.getExpires();
                refresher.updateETag(etag);
                RefreshHelper.scheduleRefresh(repetitiousTaskManager, refreshIdentity, expires * 1000, refresher);
                listener.publicationDelivered(new PublishStateEvent(dialog, etag, reasonInfo));
            }
        }
        else if (response.getStatusCode() == StatusCode.CONDITIONAL_REQUEST_FAILED) {
            RefreshHelper.cancelRefresh(repetitiousTaskManager, refreshIdentity);
            listener.publicationDeliveryFailed(new PublishStateEvent(dialog, etag, reasonInfo));
        }
        else if (response.getResponseClass().isErrorResponse()) {
            listener.publicationDeliveryFailed(new PublishStateEvent(dialog, etag, reasonInfo));
        }

    }

}
