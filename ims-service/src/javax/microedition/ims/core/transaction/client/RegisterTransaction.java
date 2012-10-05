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

package javax.microedition.ims.core.transaction.client;

import javax.microedition.ims.common.RepetitiousTaskManager;
import javax.microedition.ims.common.RepetitiousTaskManager.RepetitiousTimeStrategy;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.transaction.TransactionDescription;
import javax.microedition.ims.core.transaction.TransactionUtils;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Response;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 18-Dec-2009
 * Time: 13:42:44
 */
public abstract class RegisterTransaction extends ClientTransaction {

    RegisterTransaction(
            final StackContext stackContext,
            final Dialog dlg,
            final TransactionDescription description) {

        super(stackContext, dlg, description);
    }


    //return null if transaction is not complete otherwise result of transaction
    /*
   public Object onIncomingMessage(BaseSipMessage initialMessage, Response responseMessage) {

       Boolean retValue = null;

       boolean sameCallId = responseMessage.getCallId().equals(initialMessage.getCallId());
       boolean sameCallCSec = responseMessage.getcSeq() == initialMessage.getcSeq();

       final boolean haveRightResponse = sameCallId && sameCallCSec;

       if (haveRightResponse) {
           retValue = Response.ResponseClass.Success == responseMessage.getResponseClass();
       }

       return retValue;
   }
    */


    public Boolean onMessage(final BaseSipMessage initialMessage, final BaseSipMessage lastMessage) {
        assert TransactionUtils.isTransactionExecutionThread() : "Code run in wrong thread. Must be run in TransactionThread. Now in " + Thread.currentThread();

        Boolean retValue = super.onMessage(initialMessage, lastMessage);

        if (retValue != null) {
            //TODO: don't forget to check call id and CSEq here
            switch (((Response) lastMessage).getResponseClass()) {
                case Success: {
                    retValue = Boolean.TRUE;
                }
                break;

                case Redirection:
                case Global:
                case Server:
                case Client: {
                    retValue = Boolean.FALSE;
                }
                break;

                case Informational:
                case Unknown:
                default: {
                    retValue = null;
                }
                break;
            }
        }

        return retValue;
    }


    protected RepetitiousTimeStrategy getResendRequestInterval() {
        return new RepetitiousTaskManager.FixedRepetitiousTimeStrategy(RepetitiousTaskManager.T4);
    }

}
