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

package com.android.ims.rpc;

import android.os.RemoteException;
import android.util.Log;
import com.android.ims.core.CoreServiceImpl;
import com.android.ims.core.PageMessageImpl;
import com.android.ims.core.ReferenceImpl;
import com.android.ims.core.SessionImpl;

import javax.microedition.ims.android.core.*;
import javax.microedition.ims.core.CoreServiceListener;
import javax.microedition.ims.core.PageMessage;
import com.android.ims.core.WeakServiceCloseAdapter;

/**
 * This class responsible for wrapping and forwarding rpc events to @CoreServiceListener object.
 *
 * @author ext-akhomush
 */
public class RemoteCoreServiceListener extends ICoreServiceListener.Stub {
    private final String TAG = "RemoteCoreServiceListener";

    private final CoreServiceListener mDestination;
    private final CoreServiceImpl mCoreService;

    public RemoteCoreServiceListener(CoreServiceListener destination, CoreServiceImpl coreService) {
        assert destination != null;
        assert coreService != null;
        this.mDestination = destination;
        this.mCoreService = coreService;
    }

    
    public void sessionInvitationReceived(final ICoreService coreService, final ISession iSession)
            throws RemoteException {
        //Dtfm payload doesn't supported for incoming sessions
        final SessionImpl session = new SessionImpl(iSession,
                iSession.getServiceMethod(), false, mCoreService, null);
        mCoreService.addServiceCloseListener(session);
        Log.i(TAG, "sessionInvitationReceived#");
        
        boolean handled = session.handlingIncommingInvite();
        if (handled) {
            session.handleIncomingCall(new SessionImpl.SessionPrepareListener() {
                
                public void sessionPrepared() {
                    mDestination.sessionInvitationReceived(mCoreService, session);
                }
            });
        } else {
            session.reject(SessionImpl.REJECT_CODE_NOT_ACCEPTABLE);
            Log.e(TAG, "sessionInvitationReceived# medias cannnot be handled, there are'nt supported medias, send reject(488)");
        }
        
    }
    
    
    public void referenceReceived(final ICoreService service, final IReference iReference)
            throws RemoteException {
        Log.i(TAG, "referenceReceived#");
        ReferenceImpl reference = new ReferenceImpl(iReference.getServiceMethod(), iReference);
        mCoreService.addServiceCloseListener(reference);
        mDestination.referenceReceived(mCoreService, reference);
    }

    
    public void pageMessageReceived(final ICoreService iCoreService,
                                    final IPageMessage iPageMessage, final byte[] content,
                                    final String contentType) throws RemoteException {
        Log.i(TAG, String.format("pageMessageReceived#content.length = %s, contentType = %s", content.length, contentType));

        IServiceMethod serviceMethod = iPageMessage.getServiceMethod();
        PageMessageImpl pageMessage = new PageMessageImpl(serviceMethod, iPageMessage, content, contentType, PageMessage.STATE_RECEIVED);
        mCoreService.addServiceCloseListener(new WeakServiceCloseAdapter(pageMessage));

        mDestination.pageMessageReceived(mCoreService, pageMessage);
    }
}
