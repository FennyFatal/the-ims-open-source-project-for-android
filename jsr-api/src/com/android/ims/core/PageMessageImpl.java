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
import com.android.ims.rpc.RemotePageMessageListener;

import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.ServiceError;
import javax.microedition.ims.android.core.IPageMessage;
import javax.microedition.ims.android.core.IPageMessageListener;
import javax.microedition.ims.android.core.IServiceMethod;
import javax.microedition.ims.core.PageMessage;
import javax.microedition.ims.core.PageMessageListener;
import java.util.Arrays;

/**
 * Default implementation {@link PageMessage}
 */
public class PageMessageImpl extends ServiceMethodImpl implements PageMessage{
    private static final String TAG = "PageMessageImpl-JSR"; 
    
	private int state = STATE_UNSENT;
	private String contentType;
	private byte[] content;
	private RemotePageMessageListener currentListener;
	
	private final IPageMessage pageMessage;

	/**
	 * Constructor create page message based on local offer.
	 * @param serviceMethod
	 * @param service
	 * @param iPageMessage
	 */
	public PageMessageImpl(final IServiceMethod serviceMethod, 
	        final IPageMessage iPageMessage) {
	    this(serviceMethod, iPageMessage, null, null, STATE_UNSENT);
	}

	/**
	 * Constructor create page message based on remote offer.
	 * @param serviceMethod
	 * @param service
	 * @param iPageMessage
	 * @param content
	 * @param contentType
	 */
	public PageMessageImpl(final IServiceMethod serviceMethod, 
	        final IPageMessage iPageMessage, final byte[] content, 
	        final String contentType, int state) {
		super(serviceMethod);
		
		assert iPageMessage != null;
		this.pageMessage = iPageMessage;
		
		this.content = content;
		this.contentType = contentType;
		
		setState(state);
		
		Log.d(TAG, "init#");
	}
	
	/**
	 * @see PageMessage#getContent()
	 */
	
	public byte[] getContent() {			
		if(getState() != STATE_RECEIVED){
			throw new IllegalStateException("Message is not in state STATE_RECEIVED. No content available.");
		}
		return getContentInternally();
	}

	public byte[] getContentInternally() {
	    return content;
	}
    /**
     * @see PageMessage#getContentType()
     */
    
    public String getContentType() {
        if (getState() != STATE_RECEIVED) {
            throw new IllegalStateException("Message is not in state STATE_RECEIVED. No content type available.");
        }
        return getContentTypeInternally();
    }

    public String getContentTypeInternally() {
        return contentType;
    }
    
    /**
     * @see PageMessage#getState()
     */
    
    public int getState() {
        return state;
    }

    private void setState(int state) {
        this.state = state;
    }

    /**
     * @see PageMessage#send(byte[], String)
     */
	
	public void send(final byte[] content, final String contentType) 
	            throws ServiceClosedException{
	    if(!isServiceOpen()) {
	        throw new ServiceClosedException("Service must be open");
	    }
	    
	    if(getState() != STATE_UNSENT){
	        throw new IllegalStateException("Message is not in state STATE_UNSENT.");
	    }

	    
	    if (content == null && contentType != null) {
	        throw new IllegalArgumentException("Content is null and contentType is not null");
        }
	    

        Log.i(TAG, String.format("send#contentType = %s, content length = %s", contentType, content != null ? content.length : 0));
    	//setting added body to service side
    	/*MessageBodyPart part1 = getNextRequest().createBodyPart();
    	MessageBodyPart part2 = getNextRequest().createBodyPart();
    	MessageBodyPart part3 = getNextRequest().createBodyPart();
    	byte[] temp = new byte[10];
    	Arrays.fill(temp, (byte)'a');
    	try {
			part1.openContentOutputStream().write(temp);
			part1.setHeader("Content-Type", "test1");
			part2.openContentOutputStream().write(temp);
			part2.setHeader("Content-Type", "test2");	
			part3.openContentOutputStream().write(temp);
			part3.setHeader("Content-Type", "test3");
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
		
		//part1.setHeader("Content-Type", "multipart/application; boundary=\"34343434343434fff\"");
    	
		flushRequestBodies();
    	

        this.content = content;
        this.contentType = contentType;

        try {
            pageMessage.send(content, contentType);
            setState(STATE_SENT);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ServiceError(e.getMessage(), e);
        } 
	}

    /**
     * @see PageMessage#setListener(PageMessageListener)
     */
	
	public void setListener(final PageMessageListener listener) {
	    if(currentListener != null) {
	        removeListener(currentListener);
	        currentListener = null;
	    }
	    
	    if(listener != null) {
	        addListener(currentListener = new RemotePageMessageListener(this, listener));    
	    }
	}

	private void addListener(final IPageMessageListener listener) {
	    try {
	        pageMessage.addListener(listener);
	    } catch (RemoteException e) {
	        Log.e(TAG, e.getMessage(), e);
	    }
	}
	
	private void removeListener(final IPageMessageListener listener) {
	    try {
	        pageMessage.removeListener(listener);
	    } catch (RemoteException e) {
	        Log.e(TAG, e.getMessage(), e);
	    }
	}

	
    
    public String toString() {
        return "PageMessageImpl [content=" + Arrays.toString(content)
                + ", contentType=" + contentType + ", state=" + state + "]";
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize#"/* +  toString()*/);
        super.finalize();
    }
    
}
