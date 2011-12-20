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

import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.core.IMessageBodyPart;
import javax.microedition.ims.core.Message;
import javax.microedition.ims.core.MessageBodyPart;
import java.io.*;

/**
 * Default implementation for MessageBodyPart interface.
 *
 * @author ext-akhomush
 * @see MessageBodyPart
 */
public class MessageBodyPartImpl implements MessageBodyPart {
	private static final String TAG = "MessageBodyPartImpl";

	private final IMessageBodyPart mMessageBodyPart;
	private byte[] content;
	private  OutputStream os;
	private Message msg;

	public MessageBodyPartImpl(IMessageBodyPart messageBodyPart, Message msg) {
		assert messageBodyPart != null: "IMessageBodyPart cannot be null";
		this.mMessageBodyPart = messageBodyPart;
		assert msg != null : "Message cannot be null";
		this.msg = msg;
	}

	
	public String getHeader(String key) {
		if(key == null || !key.startsWith("Content-")){
			throw new IllegalArgumentException("Key coudn't be null and must starts with \"Content-\".");
		}
		String value = null;
		try {
			value = mMessageBodyPart.getHeader(key);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return value;
	}

	
	public InputStream openContentInputStream() {
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(mMessageBodyPart.getContent());
		} catch (RemoteException e) {
			Log.e(TAG, "Cannot open InputStream for content");
			e.printStackTrace();
		}
		return is;
	}

	
	public OutputStream openContentOutputStream() {
		if(msg.getState() != Message.STATE_UNSENT){
		    throw new IllegalStateException("Message is not in STATE_UNSENT");
		}
		if(os == null){
			os = new ByteArrayOutputStream() {
				public void close() throws IOException {
					super.flush();
					super.close();
					content = toByteArray();
				}
			};
		}
		return os;
	}

	
	public void setHeader(String key, String value) {
	    if(msg.getState() != Message.STATE_UNSENT){
            throw new IllegalStateException("Message is not in STATE_UNSENT");
        }
		if(key == null || !key.startsWith("Content-")){
			throw new IllegalArgumentException("Key coudn't be null and must starts with \"Content-\".");
		}
		if(value == null){
			throw new IllegalArgumentException("Value coudn't be null.");
		}
		try {
			mMessageBodyPart.setHeader(key, value);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	void flushData(){
		Log.d(TAG, "flushingaData, os: "+ (os != null));
		if(os != null){
			try {
				os.close();				
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				Log.e(TAG, "Cannot close output stream");
			}
			
			try {
				mMessageBodyPart.setContent(content);
			} catch (RemoteException e) {			
				Log.e(TAG, e.getMessage(), e);
				Log.e(TAG, "Cannot flush content");
			}
			
		}
	}
}
