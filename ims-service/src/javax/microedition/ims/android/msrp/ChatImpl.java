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

package javax.microedition.ims.android.msrp;

import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.msrp.listener.ChatExtensionListener;

public class ChatImpl extends IChat.Stub {
    private static final String TAG = "Service - ChatImpl";

    private final IMSessionImpl imSessionImpl;

    private final RemoteListenerHolder<IChatListener> listenerHolder = new RemoteListenerHolder<IChatListener>(IChatListener.class);


    private ChatExtensionListener chatExtensionListener = new ChatExtensionListener() {
        
        public void onChatExtended() {
            notifyChatExtended();
        }

        
        public void onChatExtensionFailed(String reasonPhrase, int reasonType, int statusCode) {
            notifyChatExtensionFailed(reasonPhrase, reasonType, statusCode);
        }
    };


    public ChatImpl(IMSessionImpl imSessionImpl) {
        this.imSessionImpl = imSessionImpl;

        //msrpService.addChatExtensionListener(imSessionImpl.getMsrpDialog(), chatExtensionListener);
        imSessionImpl.getMsrpService().addChatExtensionListener(chatExtensionListener);
    }

    
    public IIMSession getIMSession() {
        return imSessionImpl;
    }

    
    public void extendToConference(String[] additionalParticipants) throws RemoteException {

        imSessionImpl.getMsrpService().extendToConference(additionalParticipants);

    }

    
    public void addListener(IChatListener listener) throws RemoteException {
        if (listener != null) {
            listenerHolder.addListener(listener);
        }
    }

    
    public void removeListener(IChatListener listener) throws RemoteException {
        if (listener != null) {
            listenerHolder.removeListener(listener);
        }
    }

    private void notifyChatExtended() {
        ConferenceImpl conferenceImpl = new ConferenceImpl(imSessionImpl);
        try {
            listenerHolder.getNotifier().chatExtended(ChatImpl.this, conferenceImpl);
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
    }

    private void notifyChatExtensionFailed(String reasonPhrase,
                                           int reasonType, int statusCode) {
        IReasonInfo reason = new IReasonInfo(reasonPhrase, reasonType, statusCode);
        try {
            listenerHolder.getNotifier().chatExtensionFailed(ChatImpl.this, reason);
        }
        catch (RemoteException e) {
            e.printStackTrace();
            Logger.log(TAG, e.getMessage());
        }
    }
}
