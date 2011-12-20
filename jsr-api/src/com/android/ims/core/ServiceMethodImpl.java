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

package com.android.ims.core;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ManagableConnection;
import com.android.ims.ServiceCloseListener;

import javax.microedition.ims.android.core.IMessage;
import javax.microedition.ims.android.core.IServiceMethod;
import javax.microedition.ims.core.Message;
import javax.microedition.ims.core.ServiceMethod;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of the {@link ServiceMethod} interface.
 *
 * @author ext-akhomush
 */
public abstract class ServiceMethodImpl implements ServiceMethod, ServiceCloseListener {
	protected final String TAG = getClass().getSimpleName() + "-JSR";

	private final IServiceMethod serviceMethod;
	private final AtomicReference<Boolean> done = new AtomicReference<Boolean>(false);
	//private final ServiceImpl service;
	private MessageImpl nextRequest, nextResponce;

	protected ServiceMethodImpl(IServiceMethod serviceMethod/*, final ServiceImpl service*/) {
		assert serviceMethod != null;
		this.serviceMethod = serviceMethod;

		//assert service != null;
		//this.service = service;
	}

	/**
	 * @see ServiceMethod#getNextRequest()
	 */
	
	public Message getNextRequest() {
		if(nextRequest == null) {
			try {
				IMessage messageRemote = serviceMethod.getNextRequest();
				nextRequest = new MessageImpl(messageRemote);
			} catch (RemoteException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return nextRequest;
	}

	/**
	 * @see ServiceMethod#getNextResponse()
	 */
	
	public Message getNextResponse() {
		if(nextResponce == null) {
			try {
				IMessage messageRemote = serviceMethod.getNextResponse();
				nextResponce = new MessageImpl(messageRemote);
			} catch (RemoteException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return nextResponce;
	}

	/**
	 * @see ServiceMethod#getPreviousRequest(int)
	 */
	
	public Message getPreviousRequest(int method) {
		if (!isValidApiMethod(method)) {
			throw new IllegalArgumentException("Not a valid method: " + method);
		}

		Message message = null;
		try {
			IMessage messageRemote = serviceMethod.getPreviousRequest(method);
			message = new MessageImpl(messageRemote);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return message;
	}

	/**
	 * @see ServiceMethod#getPreviousResponses(int)
	 */
	
	public Message[] getPreviousResponses(int method) {
		if (!isValidApiMethod(method)) {
			throw new IllegalArgumentException("Not a valid method: " + method);
		}

		Message[] messages = null;
		try {
			List<IBinder> messagesRemote = serviceMethod.getPreviousResponses(method);

			messages = new Message[messagesRemote.size()];
			for (int i = 0, count = messagesRemote.size(); i < count; i++) {
				IMessage messageRemote = IMessage.Stub.asInterface(messagesRemote.get(i));
				messages[i] = new MessageImpl(messageRemote);
			}

		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
			messages = new Message[0];
		}

		return messages;
	}

    int getSessionStatus() {
        final Message[] responses = getPreviousResponses(Message.SESSION_START);
        final Message lastResponse = responses.length > 0 ? responses[responses.length - 1] : null;

        return lastResponse != null ? lastResponse.getStatusCode() : -1;
    }

	private boolean isValidApiMethod(int method) {
		return method == Message.CAPABILITIES_QUERY ||
		method == Message.PAGEMESSAGE_SEND ||
		method == Message.PUBLICATION_PUBLISH ||
		method == Message.PUBLICATION_UNPUBLISH ||
		method == Message.REFERENCE_REFER ||
		method == Message.SESSION_START ||
		method == Message.SESSION_UPDATE ||
		method == Message.SESSION_TERMINATE ||
		method == Message.SUBSCRIPTION_SUBSCRIBE ||
		method == Message.SUBSCRIPTION_UNSUBSCRIBE;
	}

	/**
	 * @see ServiceMethod#getRemoteUserId()
	 */
	
	public String[] getRemoteUserId() {
		String[] userIdenties = null;
		try {
			userIdenties = serviceMethod.getRemoteUserId();
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return userIdenties;
	}

/*	public ServiceImpl getService() {
		return service;
	}
*/
	protected boolean isServiceOpen() {
		//return service.isOpen();
	    return !done.get();
	}

	
	public void serviceClosed(ManagableConnection connection) {
	    connection.removeServiceCloseListener(this);
	    done.set(true);
	}

	protected void flushRequestBodies(){
		if(nextRequest != null){
			for(MessageBodyPartImpl bodyPart : nextRequest.getCacheBodyParts()){
				bodyPart.flushData();
			}
			nextRequest = null;
		}
	}

	protected void flushResponceBodies(){
		if(nextResponce != null){
			for(MessageBodyPartImpl bodyPart : nextResponce.getCacheBodyParts()){
				bodyPart.flushData();
			}
			nextResponce = null;
		}
	}
}
