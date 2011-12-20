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

package com.android.ims.xdm;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ServiceImpl;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.xdm.rpc.RemoteXDMServiceListener;
import org.w3c.dom.Element;

import javax.microedition.ims.ImsException;
import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.android.xdm.*;
import javax.microedition.ims.xdm.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation {@link XDMService}
 * 
 * @author Andrei Khomushko
 *
 */
public class XDMServiceImpl extends ServiceImpl implements XDMService, XDMServiceListener {
    private static final String TAG = "XDMServiceImpl";
    
    public static final String DEF_XUI_KEY = "currentUser";
    
    private final String xcapRoot;
    private final String defXui;
    private final boolean xdmSendFullDoc;
    private final IXDMService xdmServicePeer;
    private final RemoteXDMServiceListener remoteXDMServiceListener;
    
    private XDMServiceListener xdmServiceListener;
    
    public XDMServiceImpl(final IXDMService xdmService, final Context context,
            AppConfiguration configuration) throws ImsException {
        super(context, configuration);
        assert xdmService != null;
        this.xdmServicePeer = xdmService;
        
        this.remoteXDMServiceListener = new RemoteXDMServiceListener(this, this);
        try {
            xdmServicePeer.addXDMServiceListener(remoteXDMServiceListener);
            this.xcapRoot = xdmServicePeer.getXCAPRoot();
            this.defXui = xdmServicePeer.getXUI();
            this.xdmSendFullDoc = xdmServicePeer.isXdmSendFullDoc();
            
            System.setProperty(DEF_XUI_KEY, defXui);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException(e.getMessage(), e);
        }
        
    }

    
    public DocumentSubscriber createDocumentSubscriber(String[] urls)
            throws ServiceClosedException, ImsException {
        
        //TODO ImsException - if the urls argument contains documents or collections from more than one AUID and no subscription proxy is available
        
        if(urls == null || urls.length == 0) {
            throw new IllegalArgumentException("The urls argument is null or an empty array");
        }
        
        if(!isOpen()) {
            throw new ServiceClosedException("The Service is closed"); 
        }
        
        final DocumentSubscriber retValue;
        
        final IExceptionHolder exceptionHolder = new IExceptionHolder();
        
        final IDocumentSubscriber documentSubscriberPeer;
        try {
            documentSubscriberPeer = xdmServicePeer.createDocumentSubscriber(urls, exceptionHolder);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException("The DocumentSubscriber could not be created, message = " + e.getMessage(), e);
        }

        if(exceptionHolder.getParcelableException() != null) {
            //TODO handle exception properly
            throw new ImsException("Can't create document subscriber");
        } 
        
        final DefaultDocumentSubscriber documentSubscriber; 
        try {
            documentSubscriber = new DefaultDocumentSubscriber(documentSubscriberPeer);
            addServiceCloseListener(documentSubscriber);
        } catch (InstantiationException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ImsException(e.getMessage(), e);
        }
        
        retValue = documentSubscriber; 
        
        return retValue;
    }

    
    public DocumentEntry[] listDocuments(String auid)
            throws ServiceClosedException, IOException, XCAPException {
        if (!isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        List<DocumentEntry> retValue = new ArrayList<DocumentEntry>();

        IExceptionHolder exceptionHolder = new IExceptionHolder();
        IDocumentEntry[] documentEntries = null;
        try {
            documentEntries = xdmServicePeer.listDocuments(
                    auid, exceptionHolder);
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        
        if (exceptionHolder.getParcelableException() == null) {
            for (IDocumentEntry documentEntry : documentEntries) {
                retValue.add(new DocumentEntryImpl(documentEntry
                        .getDocumentSelector(), documentEntry.getEtag(),
                        documentEntry.getLastModified(), documentEntry
                                .getSize()));
            }
        } else {
            throw XDMUtils.createXCAPException((IXCAPException) exceptionHolder
                    .getParcelableException());
        }

        return retValue.toArray(new DocumentEntry[0]);
    }

    
    public Element performSearch(final Search search) throws ServiceClosedException,
            IOException, XCAPException {
        if (!isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        if (search == null) {
            throw new IllegalArgumentException("The search attribute is null");
        }
        
        // TODO Auto-generated method stub
        return null;
    }

    
    public XCAPResponse sendXCAPRequest(final XCAPRequest request)
            throws ServiceClosedException, IOException, XCAPException {
        if (!isOpen()) {
            throw new ServiceClosedException("Service already closed");
        }

        if (request == null) {
            throw new IllegalArgumentException("The request attribute is null");
        }
        
        XCAPResponse retValue = null;
        
        IXCAPRequest iRequest = XDMUtils.createIXCAPRequest(request, xcapRoot);
        IExceptionHolder exceptionHolder = new IExceptionHolder();
        IXCAPResponse iResponse = null;
        
        try {
            iResponse = xdmServicePeer.sendXCAPRequest(iRequest,
                    exceptionHolder);
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        
        if (exceptionHolder.getParcelableException() == null) {
            retValue = new XCAPResponseImpl(iResponse.getContent(),
                    iResponse.getEtag(), iResponse.getMimeType());
        } else {
            throw XDMUtils.createXCAPException((IXCAPException) exceptionHolder
                    .getParcelableException());
        }
        
        return retValue;
    }

    
    public void setListener(XDMServiceListener listener) {
        this.xdmServiceListener = listener;
    }

    
    public String getAppIdInternally() throws RemoteException {
        return xdmServicePeer.getAppId();
    }

    
    public String getSchemeInternally() throws RemoteException {
        return xdmServicePeer.getAppId();
    }

    
    protected void closeInternally() throws RemoteException {
        xdmServicePeer.removeXDMServiceListener(remoteXDMServiceListener);
        xdmServicePeer.close();
    }
    
    public IXDMService getXdmServicePeer()  {
        return xdmServicePeer;
    }

    
    public void serviceClosed(XDMService service, ReasonInfo reasonInfo) {
        notifyServiceClosed(reasonInfo);
    }
    
    private void notifyServiceClosed(ReasonInfo reasonInfo) {
        if(xdmServiceListener != null) {
            xdmServiceListener.serviceClosed(this, reasonInfo);
        }
    }

    public String getXcapRoot() {
        return xcapRoot;
    }
    
    public boolean isXdmSendFullDoc() {
        return xdmSendFullDoc;
    }

    public String getDefXui() {
        return defXui;
    }
    
    public String toString() {
        return "XDMServiceImpl [defXui=" + defXui + ", xcapRoot=" + xcapRoot
            + ", xdmSendFullDoc=" + xdmSendFullDoc + "]";
    }
}
