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

package javax.microedition.ims.android.presence;

import android.os.RemoteException;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogCallIDImpl;
import javax.microedition.ims.core.dialog.DialogStorage;
import javax.microedition.ims.core.sipservice.publish.PublishService;
import javax.microedition.ims.core.sipservice.subscribe.SubscribeService;
import javax.microedition.ims.util.MessageUtilHolder;

/**
 * Presence Service default implementation.
 *
 * @author Andrei Khomushko
 */
public class PresenceServiceImpl extends IPresenceService.Stub {
    //private static final String TAG = "IPresenceServiceImpl";

    private final ClientIdentity localParty;
    private final DialogStorage dialogStorage;
    private final PublishService publishServicePeer;
    private final SubscribeService subscribeServicePeer;

    private final RemoteListenerHolder<IPresenceServiceListener> listenerHolder = new RemoteListenerHolder<IPresenceServiceListener>(
            IPresenceServiceListener.class);
    private final StackContext context;

    private List<IPresenceSource> mPresenceSourceList = new ArrayList<IPresenceSource>();
    private List<IWatcher> mWatcherList = new ArrayList<IWatcher>();
    private List<IWatcherInfoSubscriber> mWatcherInfoList = new ArrayList<IWatcherInfoSubscriber>();

    /**
     * Create presenceServiceImpl
     *
     * @param callingParty
     * @param presenceService
     * @param imsStack
     * @throws IllegalArgumentException - if any parameter is null
     */
    public PresenceServiceImpl(final ClientIdentity callingParty,
                               final DialogStorage dialogStorage,
                               final PublishService publishService,
                               final SubscribeService subscribeService,
                               final StackContext context) {
        if (callingParty == null) {
            throw new IllegalArgumentException(
                    "The callingParty argument is null");
        }

        if (dialogStorage == null) {
            throw new IllegalArgumentException(
                    "The dialogStorage argument is null");
        }

        if (publishService == null) {
            throw new IllegalArgumentException(
                    "The publishService argument is null");
        }

        if (subscribeService == null) {
            throw new IllegalArgumentException(
                    "The subscribeService argument is null");
        }

        this.localParty = callingParty;
        this.dialogStorage = dialogStorage;
        this.publishServicePeer = publishService;
        this.subscribeServicePeer = subscribeService;
        this.context = context;
    }

    @Override
    public void close() throws RemoteException {
        Collections.reverse(mPresenceSourceList);
        for (IPresenceSource item : mPresenceSourceList) {
            if (item.getState() == 4 /*PresenceSourceImpl.StateCode.STATE_ACTIVE*/) {
                item.unpublish();
            }
        }
        Collections.reverse(mWatcherList);
        for (IWatcher item : mWatcherList) {
            if (item.getState() == 3 /*SubscriptionState.STATE_ACTIVE*/) {
                item.unsubscribe();
            }
        }
        Collections.reverse(mWatcherInfoList);
        for (IWatcherInfoSubscriber item : mWatcherInfoList) {
            if (item.getState() == 3 /*SubscriptionState.STATE_ACTIVE*/) {
                item.unsubscribe();
            }
        }

        listenerHolder.shutdown();
    }

    @Override
    public String getAppId() throws RemoteException {
        return localParty.getAppID();
    }

    @Override
    public String getUserId() throws RemoteException {
        return localParty.getUserInfo().toUri();
    }

    @Override
    public String getSheme() throws RemoteException {
        return localParty.getSchema();
    }

    @Override
    public IPresenceSource createPresenceSource() throws RemoteException {
        Dialog dialog = dialogStorage.getDialog(localParty, localParty
                .getUserInfo().toUri(), new DialogCallIDImpl(SIPUtil
                .newCallId()));
        IPresenceSource presenceSource = new PresenceSourceImpl(dialog, publishServicePeer);
        mPresenceSourceList.add(presenceSource);
        return presenceSource;
    }

/*    private void notifyServiceClosed(IReasonInfo reasonInfo) {
        try {
            listenerHolder.getNotifier().serviceClosed(reasonInfo);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage(), e);
        }
    }
*/
    @Override
    public void addPresenceServiceListener(IPresenceServiceListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }

    @Override
    public void removePresenceServiceListener(IPresenceServiceListener listener)
            throws RemoteException {
        listenerHolder.removeListener(listener);
    }

    @Override
    public IWatcher createWatcher(String targetURI,
                                  IExceptionHolder exceptionHolder) throws RemoteException {
        IWatcher retValue = null;

        try {
            retValue = doCreateWatcher(targetURI);
            mWatcherList.add(retValue);
        } catch (IllegalArgumentException e) {
            exceptionHolder.setParcelableException(new IError(
                    IError.ERROR_WRONG_PARAMETERS, e.getMessage()));
        }

        return retValue;
    }

    private IWatcher doCreateWatcher(final String remoteParty) throws IllegalArgumentException {

        if (!MessageUtilHolder.isValidUri(context.getConfig(), remoteParty)) {
            throw new IllegalArgumentException("The targetURI argument is invalid");
        }

        return new WatcherImpl(localParty, remoteParty, subscribeServicePeer);
    }

    @Override
    public IWatcherInfoSubscriber createWatcherInfoSubscriber() {
        final IWatcherInfoSubscriber infoSubscriber;

        infoSubscriber = new WatcherInfoSubscriberImpl(
                localParty,
                localParty.getUserInfo().toUri(),
                subscribeServicePeer
        );
        mWatcherInfoList.add(infoSubscriber);

        return infoSubscriber;
    }
}
