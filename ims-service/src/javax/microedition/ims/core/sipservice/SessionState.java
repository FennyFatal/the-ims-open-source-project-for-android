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
import javax.microedition.ims.core.transaction.TransactionType;
import javax.microedition.ims.util.MessageUtilHolder;
import javax.microedition.ims.util.SipMessageUtil;

public enum SessionState {
    SESSION_NEGOTIATING,
    SESSION_ALERTING,
    SESSION_STARTED,
    SESSION_START_FAILED,
    SESSION_TERMINATED,
    SESSION_UPDATED,
    SESSION_UPDATE_FAILED,
    SESSION_UPDATE_RECEIVED,

    MESSAGE_DELIVERED,
    MESSAGE_DELIVERY_FAILED,
    MESSAGE_RECEIVED,

    UNKNOWN;

    public static <T> SessionState toSessionState(final TransactionStateChangeEvent<T> event) {
        SessionState retValue = UNKNOWN;

        final TransactionType.Name transactionName = event.getTransaction().getTransactionType().getName();
        final State stateName = event.getState();
        final StateChangeReason reason = event.getStateChangeReason();
        final T message = event.getTriggeringMessage();
        final SipMessageUtil<T> sipMessageUtil = MessageUtilHolder.getSIPMessageUtil();

        Logger.log("SessionState.toSessionState()", "+++++ transactionName : " + transactionName + "   stateName: " + stateName.name() + "   reason: " + reason);


        switch (transactionName) {
            case SIP_INVITE_CLIENT: {
                if (State.CALLING == stateName || State.PROCEEDING == stateName) {
                    if (reason == StateChangeReason.INCOMING_UPDATE) {
                        retValue = SESSION_UPDATE_RECEIVED;
                    }
                    else if (reason == StateChangeReason.CLIENT_UPDATE_SUCCESS) {
                        retValue = SESSION_UPDATED;
                    }
                    else if (reason == StateChangeReason.CLIENT_UPDATE_FAILED) {
                        retValue = SESSION_UPDATE_FAILED;
                    }
                    else {
                        retValue = SESSION_ALERTING;
                    }

                }
                else if (State.TERMINATED == stateName &&
                        StateChangeReason.INCOMING_MESSAGE == reason &&
                        sipMessageUtil.isSuccessResponse(message)) {
                    retValue = SESSION_STARTED;
                }
                else {
                    retValue = SESSION_START_FAILED;
                }
            }
            break;

            case SIP_INVITE_SERVER: {
                if (State.PROCEEDING == stateName) {
                    //event.getTransaction().getStackContext().getConfig();            
                    retValue = SESSION_NEGOTIATING;
                }
                else if (State.PROCEEDING_RR == stateName) {
                    if (reason == StateChangeReason.INITIALIZATION) {
                        retValue = SESSION_NEGOTIATING;
                    }
                    else if (reason == StateChangeReason.CLIENT_PREACCEPT) {
                        retValue = SESSION_NEGOTIATING;
                    }
                    else if (reason == StateChangeReason.CLIENT_PREACCEPT_DELIVERED) {
                        retValue = SESSION_ALERTING;
                    }
                    else if (reason == StateChangeReason.INCOMING_UPDATE) {
                        retValue = SESSION_UPDATE_RECEIVED;
                    }
                    else if (reason == StateChangeReason.CLIENT_UPDATE_SUCCESS) {
                        retValue = SESSION_UPDATED;
                    }
                    else if (reason == StateChangeReason.CLIENT_UPDATE_FAILED) {
                        retValue = SESSION_UPDATE_FAILED;
                    }
                    else {
                        assert false : "unhandled REASON = " + reason;
                    }
                }
                else if (State.COMPLETED == stateName &&
                        StateChangeReason.CLIENT_ACCEPT == reason) {
                    retValue = SESSION_ALERTING;
                }
                else if (State.CONFIRMED == stateName &&
                        StateChangeReason.CLIENT_ACCEPT_DELIVERED == reason) {
                    retValue = SESSION_STARTED;
                }
                else if (State.TERMINATED == stateName &&
                        StateChangeReason.CLIENT_ACCEPT_DELIVERED == reason) {
                    retValue = SESSION_STARTED;
                }

                else {
                    retValue = SESSION_START_FAILED;
                }
            }
            break;

            case SIP_REINVITE_CLIENT: {
                if (State.CALLING == stateName || State.PROCEEDING == stateName) {
                    retValue = null;
                }
                else if (State.TERMINATED == stateName &&
                        StateChangeReason.INCOMING_MESSAGE == reason &&
                        sipMessageUtil.isSuccessResponse(message)) {
                    retValue = SESSION_UPDATED;
                }
                else {
                    retValue = SESSION_UPDATE_FAILED;
                }
            }
            break;

            case SIP_REINVITE_SERVER: {
                if (State.PROCEEDING == stateName) {

                    if (reason == StateChangeReason.INITIALIZATION) {
                        retValue = SESSION_UPDATE_RECEIVED;
                    }

                }
                else if (State.COMPLETED == stateName) {

                    if (reason == StateChangeReason.CLIENT_ACCEPT) {
                        retValue = SESSION_UPDATED; //for server side
                    }

                    //waiting ack
                }
                else if (State.CONFIRMED == stateName) {
                    if (StateChangeReason.CLIENT_ACCEPT == reason) {
                        retValue = SESSION_UPDATED;
                    }
                    else if (StateChangeReason.CLIENT_REJECT == reason || StateChangeReason.TIMER_TIMEOUT == reason) {
                        retValue = SESSION_UPDATE_FAILED;
                    }
                }
                else if (State.TERMINATED == stateName &&
                        StateChangeReason.TIMER_TIMEOUT == reason) {
                    retValue = SESSION_UPDATE_FAILED;
                }
            }
            break;


            case SIP_BYE_CLIENT: {
                retValue = SESSION_TERMINATED;
            }
            break;

            case SIP_BYE_SERVER: {
                if (State.COMPLETED == stateName || State.TERMINATED == stateName) {
                    retValue = SESSION_TERMINATED;
                }
                else {
                    retValue = null;
                }
            }
            break;

            case SIP_MESSAGE_CLIENT: {
                if (State.COMPLETED == stateName &&
                        StateChangeReason.INCOMING_MESSAGE == reason) {
                    if (sipMessageUtil.isSuccessResponse(message)) {
                        retValue = MESSAGE_DELIVERED;
                    }
                    else {
                        retValue = MESSAGE_DELIVERY_FAILED;
                    }
                } else if(State.TRYING == stateName &&
                        StateChangeReason.TRANSACTION_SHUTDOWN == reason) {
                    retValue = MESSAGE_DELIVERY_FAILED;
                } else {
                    retValue = null;
                }
                
                //08-23 13:40:31.657: INFO/SessionState.toSessionState()(5599): +++++ transactionName : SIP_MESSAGE_CLIENT   stateName: TRYING   reason: TRANSACTION_SHUTDOWN

            }
            break;
            
            case SIP_MESSAGE_SERVER: {
                if (State.COMPLETED == stateName || State.TERMINATED == stateName) {
                    retValue = MESSAGE_RECEIVED;
                }
                else {
                    retValue = null;
                }
            }
            break;
        }

        Logger.log("SessionState", "retValue: " + retValue);
        return retValue;
    }

}
