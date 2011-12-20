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

import javax.microedition.ims.android.core.ISessionDescriptor;
import javax.microedition.ims.core.SessionDescriptor;

/**
 * This class responsible for retrieving SDP related information.
 * For more details see @SessionDescriptor class.
 *
 * @author ext-ahomushko
 */
public class SessionDescriptorImpl implements SessionDescriptor {
    private static final String TAG = "SessionDescriptorImpl";

    private final ISessionDescriptor mSessionDesciption;

    public SessionDescriptorImpl(final ISessionDescriptor iSessionDesciption) {
        assert iSessionDesciption != null;
        this.mSessionDesciption = iSessionDesciption;
    }


    /**
     * @see SessionDescriptor#addAttribute(String)
     */
    
    public void addAttribute(String attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute argument is null");
        }
        //TODO @throws IllegalArgumentException - if the attribute argument is null or if the syntax of the attribute argument is invalid
        // @throws IllegalArgumentException - if the attribute already exists in the Session 
        // @throws IllegalArgumentException - if the attribute is reserved for the IMS engine 
        // @throws IllegalStateException - if the Session is not in STATE_INITIATED or STATE_ESTABLISHED state  
        try {
            mSessionDesciption.addAttribute(attribute);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see SessionDescriptor#getAttributes()
     */
    
    public String[] getAttributes() {
        String[] attributes = null;
        try {
            attributes = mSessionDesciption.getAttributes();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return attributes;
    }

    /**
     * @see SessionDescriptor#getProtocolVersion()
     */
    
    public String getProtocolVersion() {
        String protocolVersion = null;
        try {
            protocolVersion = mSessionDesciption.getProtocolVersion();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return protocolVersion;
    }

    /**
     * @see SessionDescriptor#getSessionId()
     */
    
    public String getSessionId() {
        String sessionId = null;
        try {
            sessionId = mSessionDesciption.getSessionId();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return sessionId;
    }

    /**
     * @see SessionDescriptor#getSessionInfo()
     */
    
    public String getSessionInfo() {
        String sessionInfo = null;
        try {
            sessionInfo = mSessionDesciption.getSessionInfo();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return sessionInfo;
    }

    /**
     * @see SessionDescriptor#getSessionName()
     */
    
    public String getSessionName() {
        String sessionName = null;
        try {
            sessionName = mSessionDesciption.getSessionName();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return sessionName;
    }

    /**
     * @see SessionDescriptor#removeAttribute(String)
     */
    
    public void removeAttribute(String attribute) {
        //TODO @throw IllegalArgumentException - if the attribute argument is null
        //TODO @throw IllegalArgumentException - if the attribute does not exist in the Session 
        //TODO @throw IllegalArgumentException - if the attribute is reserved for the IMS engine 
        //TODO @throw IllegalStateException - if the Session is not in STATE_INITIATED or STATE_ESTABLISHED state 

        try {
            mSessionDesciption.removeAttribute(attribute);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see SessionDescriptor#setSessionInfo(String)
     */
    
    public void setSessionInfo(String info) {
        //TODO @throw IllegalStateException - if the Session is not in STATE_INITIATED
        //TODO @throw IllegalArgumentException - if the info argument is null  

        try {
            mSessionDesciption.setSessionInfo(info);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see SessionDescriptor#getSessionName()
     */
    
    public void setSessionName(String name) {
        //TODO IllegalStateException - if the Session is not in STATE_INITIATED  
        if (name == null) {
            throw new IllegalArgumentException("Session name cannot be null");
        }
        try {
            mSessionDesciption.setSessionName(name);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
