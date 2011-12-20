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

import javax.microedition.ims.android.core.IMessage;
import javax.microedition.ims.android.core.IMessageBodyPart;
import javax.microedition.ims.core.Message;
import javax.microedition.ims.core.MessageBodyPart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation for Message interface.
 *
 * @author ext-akhomush
 * @see Message
 */
public class MessageImpl implements Message {
    private static final String TAG = "MessageImpl";

    private final IMessage mMessage;
    
    private List<MessageBodyPartImpl> parts = Collections.synchronizedList(new ArrayList<MessageBodyPartImpl>());

    public MessageImpl(IMessage message) {
        assert message != null;
        this.mMessage = message;
    }

    
    public void addHeader(String key, String value) {
    	if(key == null){
    		throw new IllegalArgumentException("Key coudn't be null");
    	}
    	if(value == null){
    		throw new IllegalArgumentException("Value coudn't be null.");
    	} 
    	if(getState() != STATE_UNSENT){
    		throw new IllegalArgumentException("Message is not in  STATE_UNSENT");
    	}
    	//TODO ImsException - if the key is not a defined header or a restricted header
        try {
            mMessage.addHeader(key, value);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public MessageBodyPart createBodyPart() {
    	/*if(getState() != STATE_UNSENT){
    		throw new IllegalArgumentException("Message is not in  STATE_UNSENT. Current state: "+getState());
    	}  */  	
    	
    	//TODO ImsException - if the body part could not be created
        MessageBodyPart bodyPart = null;
        try {
            IMessageBodyPart bodyPartRemote = mMessage.createBodyPart();
            bodyPart = new MessageBodyPartImpl(bodyPartRemote, this);
            parts.add((MessageBodyPartImpl) bodyPart);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);           
        }
        return bodyPart;
    }

    
    public MessageBodyPart[] getBodyParts() {
    	
    	//TODO Note: If the body part is a Session Description Protocol (SDP) 
    	//it is only returned if the Message is a response to a capability request.
        MessageBodyPart[] messageBodyParts;

        List<IBinder> remoteBodyParts = null;
        try {
            remoteBodyParts = mMessage.getBodyParts();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (remoteBodyParts != null) {
            List<MessageBodyPart> bodyParts = new ArrayList<MessageBodyPart>();
            for (IBinder binder : remoteBodyParts) {
                IMessageBodyPart messageBodyPart = IMessageBodyPart.Stub.asInterface(binder);
                bodyParts.add(new MessageBodyPartImpl(messageBodyPart, this));
            }
            messageBodyParts = bodyParts.toArray(new MessageBodyPartImpl[0]);
        } else {
            messageBodyParts = new MessageBodyPartImpl[0];
        }
        return messageBodyParts;
    }

    
    public String[] getHeaders(String key) {
    	//TODO ImsException - if the key is not a defined header or a restricted header
        String[] headers = null;
        try {
            headers = mMessage.getHeaders(key);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return headers;
    }

    
    public String getMethod() {
        String method = null;
        try {
            method = mMessage.getMethod();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return method;
    }

    
    public String getReasonPhrase() {
        String reasonPhrase = null;
        try {
            reasonPhrase = mMessage.getReasonPhrase();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return reasonPhrase;
    }

    
    public int getState() {
        int state = 0;
        try {
            state = mMessage.getState();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return state;
    }

    
    public int getStatusCode() {
        int statusCode = 0;
        try {
            statusCode = mMessage.getStatusCode();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return statusCode;
    }
    
    List<MessageBodyPartImpl> getCacheBodyParts(){
    	return parts;
    }
    
    
}
