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

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ReasonInfoImpl;
import com.android.ims.ServiceImpl;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.rpc.RemoteCoreServiceListener;
import com.android.ims.util.ValidatorUtil;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.IError;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.core.*;
import javax.microedition.ims.core.*;

/**
 * Default implementation CoreService interface.
 * 
 * @author ext-akhomush
 * @see CoreService
 */

public class CoreServiceImpl extends ServiceImpl implements CoreService {
    private ICoreServiceListener prevListener;
    private ICoreService coreServicePeer;
	private CoreServiceListener localListener;
	private DtmfPayload dtmfPayload;

    public CoreServiceImpl(ICoreService coreService, Context context,
            AppConfiguration configuration) {
        super(context, configuration);
        assert coreService != null;
        this.coreServicePeer = coreService;
        
        this.dtmfPayload = getDtmfPayload();
        Log.i(TAG, "dtmfPayload = " + dtmfPayload);
    }

    private DtmfPayload getDtmfPayload() {
        DtmfPayload dtmfPayload = null;
        try {
            String dtmfPayloadValue = coreServicePeer.getDtmfPayload();
            dtmfPayload = DtmfPayload.parse(dtmfPayloadValue);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return dtmfPayload;
    }

    /**
     * @see CoreService#createCapabilities(String, String)
     */
    
    public Capabilities createCapabilities(String from, String to)
            throws ServiceClosedException, ImsException {
        if(to == null) {
            throw new IllegalArgumentException("The 'to' argument is null.");
        }
        
        if (!isOpen()) {
            throw new ServiceClosedException("Service already closed.");
        }
        
        final CapabilitiesImpl capabilities;
        
        IExceptionHolder exceptionHolder = new IExceptionHolder();
        try {
            ICapabilities iCapabilities = coreServicePeer.createCapabilities(from, to, exceptionHolder);
            if(exceptionHolder.getParcelableException() == null) {
                if(iCapabilities != null) {
                    capabilities = new CapabilitiesImpl(iCapabilities.getServiceMethod(), iCapabilities);
                    addServiceCloseListener(capabilities);
                } else {
                    throw new ImsException("Capabilities cann't be created, remote peer is null");
                }
            } else {
                IError error = (IError)exceptionHolder.getParcelableException();
                throw new IllegalArgumentException(error.getMessage());
            }
        } catch (RemoteException e) {
            throw new ImsException("Capabilities cann't be created.", e);
        }
         
        return capabilities;
    }

    /**
     * @see CoreService#createSession(String, String)
     */
    
    public Session createSession(String from, String to) throws ImsException,
            ServiceClosedException {
        SessionImpl session = null;

        if (to == null) {
            throw new IllegalArgumentException("The to argument is null.");
        }
        
        if (isOpen()) {
            try {
                IExceptionHolder exceptionHolder = new IExceptionHolder();
                ISession iSession = coreServicePeer.createSession(from, to,
                        exceptionHolder);

                if (exceptionHolder.getParcelableException() != null) {
                    IError error = (IError)exceptionHolder.getParcelableException();
                    Log.e(TAG, "Cann't retrieve session from service, error = "
                            + error.toString());
                    if (error.getErrorCode() == IError.ERROR_WRONG_PARAMETERS) {
                        throw new IllegalArgumentException(
                                "Illegal argument to = " + to + ", message = "
                                        + error.getMessage());
                    } else {
                        throw new ImsException("Error when creating session, error code = " + error.getErrorCode());
                    }
                } else {
                    session = new SessionImpl(iSession,
                            iSession.getServiceMethod(), true, this, dtmfPayload);
                    addServiceCloseListener(session);
                }
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
                Log.e(TAG, "Error when creating session", e);
                throw new ImsException("Error when creating session", e);
            }
        } else {
            logInvalidState();
            throw new ServiceClosedException("Service already closed");
        }
        return session;
    }

    /**
     * @see CoreService#getLocalUserId()
     */
    
    public String getLocalUserId() {
        String localUserId = null;

        if (isOpen()) {
            try {
                localUserId = coreServicePeer.getLocalUserId();
            } catch (RemoteException e) {
                Log.e(TAG, "Error when getting local user id", e);
            }
        } else {
            logInvalidState();
        }
        return localUserId;
    }

    /**
     * @see CoreService#setListener(CoreServiceListener)
     */
    
    public void setListener(final CoreServiceListener listener) {
    	//TODO review this code
        if (isOpen()) {
            try {
            	
            	localListener = listener;
                if (prevListener != null) {
                    coreServicePeer.removeListener(prevListener);
                }

                if (listener != null) {
                    coreServicePeer
                            .addListener(prevListener = new RemoteCoreServiceListener(
                                    listener, this));
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error when set listener", e);
            }
        } else {
            logInvalidState();
        }
    }

    /**
     * @see CoreService#createReference(String, String, String, String)
     */
    
    public Reference createReference(String fromUserId, String toUserId,
            String referToUserId, String method) throws ServiceClosedException,
            ImsException {

        if (!isOpen()) {
            throw new ServiceClosedException("Service must be open");
        }
        if (toUserId == null) {
            throw new IllegalArgumentException("Invalid toUserId: " + toUserId);
        }
        if (referToUserId == null) {
            throw new IllegalArgumentException("Invalid referToUserId: "
                    + referToUserId);
        }
        if (!ValidatorUtil.isValidReferenceMethod(method)) {
            throw new IllegalArgumentException("Invalid reference method: "
                    + method);
        }

        ReferenceImpl reference = null;
        try {
            IExceptionHolder exceptionHolder = new IExceptionHolder();
            IReference iReference = coreServicePeer.createReference(fromUserId, toUserId, referToUserId, method, exceptionHolder);
            
            if (exceptionHolder.getParcelableException() != null) {
                IError error = (IError)exceptionHolder.getParcelableException();
                Log.e(TAG, "error = " + error.toString());
                if (error.getErrorCode() == IError.ERROR_WRONG_PARAMETERS) {
                    throw new IllegalArgumentException(error.getMessage());
                } else {
                    throw new ImsException("Error when creating reference, error code = " + error.getErrorCode());
                }
            } else {
                reference = new ReferenceImpl(iReference.getServiceMethod(), iReference);
                addServiceCloseListener(reference);
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("Reference could not be created", e);
        }

        return reference;
    }
    
    
    public Subscription createSubscription(String from, String to, String event)
            throws ServiceClosedException, ImsException {
        
        if (!isOpen()) {
            throw new ServiceClosedException("The Service is closed");
        }
        if (to == null) {
            throw new IllegalArgumentException("The syntax of the to argument is invalid."); 
        }
        if (event == null) {
            throw new IllegalArgumentException("The event argument is not defined.");
        }
        
        SubscriptionImpl subscription = null;
        try {
            IExceptionHolder exceptionHolder = new IExceptionHolder();
            ISubscription iSubscription = coreServicePeer.createSubscription(from, to, event, exceptionHolder);

            if (exceptionHolder.getParcelableException() != null) {
                IError error = (IError)exceptionHolder.getParcelableException();
                Log.e(TAG, "error = " + error.toString());
                if (error.getErrorCode() == IError.ERROR_WRONG_PARAMETERS) {
                    throw new IllegalArgumentException(error.getMessage());
                } else {
                    throw new ImsException("Error when creating subscription, error code = " + error.getErrorCode());
                }
            } else {
                subscription = new SubscriptionImpl(iSubscription.getServiceMethod(), iSubscription);
                addServiceCloseListener(subscription);
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The Subscription could not be created", e);
        }

        return subscription;
    }
    
    
    public Publication createPublication(String from, String to, String event)
            throws ServiceClosedException, ImsException {
        
        if (!isOpen()) {
            throw new ServiceClosedException("The Service is closed");
        }
        if (to == null) {
            throw new IllegalArgumentException("The syntax of the to argument is invalid."); 
        }
        if (event == null) {
            throw new IllegalArgumentException("The event argument is not defined.");
        }
        
        final PublicationImpl publication;
        try {
            IExceptionHolder exceptionHolder = new IExceptionHolder();
            IPublication iPublication = coreServicePeer.createPublication(from, to, event, exceptionHolder);

            if (exceptionHolder.getParcelableException() != null) {
                IError error = (IError)exceptionHolder.getParcelableException();
                Log.e(TAG, "error = " + error.toString());
                if (error.getErrorCode() == IError.ERROR_WRONG_PARAMETERS) {
                    throw new IllegalArgumentException(error.getMessage());
                } else {
                    throw new ImsException("Error when creating publication, error code = " + error.getErrorCode());
                }
            } else {
                publication = new PublicationImpl(iPublication.getServiceMethod(), iPublication);
                addServiceCloseListener(publication);
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The Publication could not be created", e);
        }
        
        return publication;
    }

    /**
     * @see CoreService#createPageMessage(String, String)
     */
    
    public PageMessage createPageMessage(String from, String to)
            throws ServiceClosedException, ImsException {
        if (!isOpen()) {
            throw new ServiceClosedException("Service must be open");
        }

        if (to == null) {
            throw new IllegalArgumentException("To parameter cann't be null");
        }

        PageMessageImpl pageMessage = null;
        try {
            IExceptionHolder exceptionHolder = new IExceptionHolder();
            final IPageMessage iPageMessage = coreServicePeer
                    .createPageMessage(from, to, exceptionHolder);
            
            if (exceptionHolder.getParcelableException() != null) {
                IError error = (IError)exceptionHolder.getParcelableException();
                throw new IllegalArgumentException(error.getMessage());
            } else {
                pageMessage = new PageMessageImpl(iPageMessage.getServiceMethod(), iPageMessage);
                addServiceCloseListener(new WeakServiceCloseAdapter(pageMessage));
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("PageMessage could not be created", e);
        }

        return pageMessage;
    }

    
    protected String getAppIdInternally() throws RemoteException {
        return coreServicePeer.getAppId();
    }

    
    protected String getSchemeInternally() throws RemoteException {
        return coreServicePeer.getSheme();
    }

    
    protected void closeInternally() throws RemoteException {
        setListener(null);
        prevListener = null;
        coreServicePeer.close();
    }
    
    @Override
    public void serviceClosed(){
    	if(localListener != null){
    		localListener.serviceClosed(this, new ReasonInfoImpl(ReasonInfo.REASONTYPE_SERVICE_CLOSED, "Connection with service lost", -1));
    	}
    }
}
