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

package javax.microedition.ims.android.core;

import android.os.IBinder;
import android.os.RemoteException;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.history.MessageData;
import javax.microedition.ims.messages.history.MessageHistory;
import java.util.ArrayList;
import java.util.List;

/**
 * This class responsible for rpc communication
 *
 * @author ext-akhomush
 * @see IServiceMethod.aidl
 */
public abstract class ServiceMethodImpl extends IServiceMethod.Stub {
    protected final String TAG = getClass().getSimpleName();

    private final String remoteUserId;
    private final MessageHistory history;

    private final String remoteUserDisplayName;

    public ServiceMethodImpl(String remoteUserId, MessageHistory history) {
        this(remoteUserId, "", history);
    }

    public ServiceMethodImpl(String remoteUserId, String remoteUserDisplayName, MessageHistory history) {
        assert remoteUserId != null;

        this.remoteUserId = remoteUserId;
        this.remoteUserDisplayName = remoteUserDisplayName;

        assert history != null;
        this.history = history;
    }

    
    public IMessage getNextRequest() throws RemoteException {
        return getNextRequestInternally();
    }

    public MessageImpl getNextRequestInternally() {
        return new MessageImpl(history.nextRequestMessage());
    }

    
    public IMessage getNextResponse() throws RemoteException {
        return new MessageImpl(history.getNextResponseMessage());
    }

    
    public IMessage getPreviousRequest(int methodId) throws RemoteException {
        return getPreviousRequestInternally(methodId);
    }

    protected MessageImpl getPreviousRequestInternally(int methodId) {
        MessageData messageData = history.findPreviousRequestByMethod(getMethodById(methodId));
        return new MessageImpl(messageData);
    }

    public List<IBinder> getPreviousResponses(int methodId)
            throws RemoteException {
        MessageData[] responses = history.findPreviousResponsesByMethod(getMethodById(methodId));
        List<IBinder> result = new ArrayList<IBinder>(responses.length);
        for (MessageData messageData : responses) {
            result.add(new MessageImpl(messageData).asBinder());
        }
        return result;
    }

    public String[] getRemoteUserId() throws RemoteException {
        StringBuilder sb = new StringBuilder();

        sb.append(remoteUserDisplayName)
                .append(StringUtils.SPACE)
                .append(remoteUserId);
        Logger.log(Logger.Tag.WARNING, "Client's remote user id is " + sb.toString());

        return new String[]{sb.toString()};
    }

    protected abstract MessageType getMethodById(int methodId);
}
