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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.sipservice.Acceptable;
import javax.microedition.ims.messages.utils.StatusCode;

public class ChatInvitationImpl extends IChatInvitation.Stub {
    private static final String TAG = "Service - ChatInvitationImpl";

    private final Dialog dialog;
    private final Acceptable acceptable;

    private final String sessionId;
    private final String sender;
    private final String subject;

    private boolean expired = false;


    public ChatInvitationImpl(Dialog dialog, Acceptable acceptable, String sessionId, String sender, String subject) {
        this.dialog = dialog;
        this.acceptable = acceptable;
        this.sessionId = sessionId;
        this.sender = sender;
        this.subject = subject;
    }

    
    public void accept() throws RemoteException {
        Logger.log(TAG, "accept#started");

        acceptable.accept(dialog);

        expire();

        Logger.log(TAG, "accept#finish");
    }

    
    public void reject() throws RemoteException {
        acceptable.reject(dialog, StatusCode.TEMPORARY_UNAVAILABLE, "TEMPORARY_UNAVAILABLE");

        expire();
    }

    
    public String getSender() throws RemoteException {
        return sender;
    }

    
    public String getSessionId() throws RemoteException {
        return sessionId;
    }

    
    public String getSubject() throws RemoteException {
        return subject;
    }

    
    public boolean isExpired() throws RemoteException {
        return expired;
    }

    void expire() {
        expired = true;
    }

}
