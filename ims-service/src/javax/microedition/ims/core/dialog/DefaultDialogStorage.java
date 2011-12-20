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

package javax.microedition.ims.core.dialog;

import javax.microedition.ims.StackHelper;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.ListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.InitiateParty;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * @author ext-plabada
 */
public class DefaultDialogStorage implements DialogStorage {
    private static final String LOG_TAG = "Dialog";
    
    private final ListenerHolder<DialogStorageListener> listenerHolder = new ListenerHolder<DialogStorageListener>(DialogStorageListener.class);

    private final Map<DialogKey, Dialog> keyToDialogMap = Collections.synchronizedMap(new HashMap<DialogKey, Dialog>(10));
    private final Map<String, Dialog> callIdToDialogMap = Collections.synchronizedMap(new HashMap<String, Dialog>(10));
    //private final Map<String, Dialog> sessionIdToDialogMap = Collections.synchronizedMap(new HashMap<String, Dialog>(10));
    private final StackContext stackContext;
    private final Object mutex = new Object();


    public DefaultDialogStorage(final StackContext stackContext) {
        this.stackContext = stackContext;
    }

    public void addDialogStorageListener(DialogStorageListener listener){
        listenerHolder.addListener(listener);
    }

    public void removeDialogStorageListener(DialogStorageListener listener){
        listenerHolder.removeListener(listener);
    }

    /*
     * Get DIALOG for client sessions
     */
    public Dialog getDialog(final ClientIdentity localParty, final String remoteParty, final String extraKey) {
        Dialog retValue;
        DialogKey key = new DialogKey(localParty, remoteParty, extraKey);

        synchronized (mutex) {
            if ((retValue = keyToDialogMap.get(key)) == null) {
                keyToDialogMap.put(
                        key,
                        retValue = new Dialog(
                                InitiateParty.LOCAL,
                                localParty,
                                remoteParty,
                                stackContext
                        )
                );

                assert !callIdToDialogMap.containsKey(retValue.getCallId());
                callIdToDialogMap.put(retValue.getCallId(), retValue);

                listenerHolder.getNotifier().onCreateDialog(new DialogStorageEventDefaultImpl(retValue));
            }
        }

        return retValue;

    }

    /*
     * Get DIALOG for client sessions
     */

    public Dialog getDialog(final ClientIdentity localParty, final String remoteParty, final DialogCallID dialogCallID) {
        Dialog retValue;
        DialogKey key = new DialogKey(localParty, remoteParty, dialogCallID.getId());

        synchronized (mutex) {
            if ((retValue = keyToDialogMap.get(key)) == null) {
                keyToDialogMap.put(
                        key,
                        retValue = new Dialog(
                                InitiateParty.LOCAL,
                                localParty,
                                remoteParty,
                                dialogCallID.getId(),
                                stackContext
                        )
                );
                assert !callIdToDialogMap.containsKey(retValue.getCallId());
                callIdToDialogMap.put(retValue.getCallId(), retValue);

                listenerHolder.getNotifier().onCreateDialog(new DialogStorageEventDefaultImpl(retValue));
            }
        }

        return retValue;

    }
    /*
     * Get DIALOG for server sessions
     */

    public Dialog getDialogForIncomingMessage(
            final ClientIdentity localParty,
            final BaseSipMessage incomingMessage) {

        Dialog retValue;
        //String remoteParty = msg.getFrom().getUriBuilder().getShortURI();
        final String remotePartyURI = StackHelper.getRemotePartyURIForIncomingMessage(incomingMessage);

        // obtain the display name from the request if there is any
        final String remoteDisplayName = StackHelper.getRemotePartyDisplayNameForIncomingMessage(incomingMessage);
        Logger.log(Logger.Tag.WARNING, "Remote party display name is " + remoteDisplayName);

        DialogKey key = new DialogKey(localParty, remotePartyURI, incomingMessage.getCallId());

        synchronized (mutex) {
            //TODO AK review it
            if ((retValue = callIdToDialogMap.get(incomingMessage.getCallId())) == null) {
                if ((retValue = keyToDialogMap.get(key)) == null) {
                    keyToDialogMap.put(
                            key,
                            retValue = new Dialog(
                                    InitiateParty.REMOTE,
                                    localParty,
                                    remotePartyURI,
                                    incomingMessage.getCallId(),
                                    /*incomingMessage.getFrom().getTag(),*/
                                    stackContext
                            )
                    );

                    // set the display name to the dialog instance
                    keyToDialogMap.get(key).setRemotePartyDisplayName(remoteDisplayName);
                    Logger.log(Logger.Tag.WARNING, "From display name for a dialog set successfully.");

                    assert !callIdToDialogMap.containsKey(retValue.getCallId());
                    callIdToDialogMap.put(retValue.getCallId(), retValue);

                    listenerHolder.getNotifier().onCreateDialog(new DialogStorageEventDefaultImpl(retValue));
                }
            }

        }

        return retValue;

    }

    public Dialog findDialogForMessage(final IMSMessage message) {
        synchronized (mutex) {
            return callIdToDialogMap.get(message.getIMSEntityId().stringValue());
        }
    }

    public Dialog findDialogByCallId(final String callId) {
        synchronized (mutex) {
            return callIdToDialogMap.get(callId);
        }
    }

    public void cleanUpDialog(final Dialog dialog) {
        Logger.log(LOG_TAG, "cleanUpDialog#dialog = " + dialog);
        
        DialogKey key = null;
        synchronized (mutex) {
            for (DialogKey dialogKey : keyToDialogMap.keySet()) {
                if (keyToDialogMap.get(dialogKey) == dialog) {
                    Logger.log(LOG_TAG, "cleanUpDialog#dialog found");
                    key = dialogKey;
                    break;
                }
            }
            if (key != null) {
                keyToDialogMap.remove(key);

                assert callIdToDialogMap.containsKey(dialog.getCallId());
                callIdToDialogMap.remove(dialog.getCallId());
                
                Logger.log(LOG_TAG, "cleanUpDialog#keyToDialogMap and callIdToDialogMap are cleaned");

                //sessionIdToDialogMap.remove(DIALOG.getMsrpData().getSessionId());
            }
        }

        if (key != null) {

            listenerHolder.getNotifier().onShutdownDialog(new DialogStorageEventDefaultImpl(dialog));
            dialog.shutdown();
        }

    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("DefaultDialogStorage");
        sb.append("{keyToDialogMap=").append(keyToDialogMap);
        sb.append('}');
        return sb.toString();
    }

}
