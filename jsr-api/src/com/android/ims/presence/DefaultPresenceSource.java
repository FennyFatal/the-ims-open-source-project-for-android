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

package com.android.ims.presence;

import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ManagableConnection;
import com.android.ims.ServiceCloseListener;
import com.android.ims.presence.DefaultPresenceDocument.AccessType;
import com.android.ims.presence.rpc.RemotePresenceSourceListener;
import com.android.ims.util.XMLUtils;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

import javax.microedition.ims.ReasonInfo;
import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.android.presence.IPresenceSource;
import javax.microedition.ims.presence.PresenceDocument;
import javax.microedition.ims.presence.PresenceSource;
import javax.microedition.ims.presence.PresenceSourceListener;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation {@link PresenceSource}
 * 
 * @author Andrei Khomushko
 * 
 */
final class DefaultPresenceSource implements PresenceSource,
        PresenceSourceListener, ServiceCloseListener {
    private static final String TAG = "DefaultPresenceSource";

    private final IPresenceSource presenceSourcePeer;
    private final PresenceDocument document;
    private final String userIdentity;
    private final RemotePresenceSourceListener remotePresenceSourceListener;
    private final AtomicBoolean done = new AtomicBoolean(false);

    private PresenceSourceListener listener;

    /**
     * Create DefaultPresenceSource.
     * 
     * @param presenceSourcePeer
     *            - remote object for communication
     * 
     * @throws InstantiationException
     *             - if DefaultPresenceSource cannn't be created.
     * @throws IllegalArgumentException
     *             - if presenceSourcePeer is null
     */
    public DefaultPresenceSource(final IPresenceSource presenceSourcePeer)
            throws InstantiationException {
        if (presenceSourcePeer == null) {
            throw new IllegalArgumentException(
                    "The presenceSourcePeer argument is null");
        }
        this.presenceSourcePeer = presenceSourcePeer;

        final String userIdentity;
        try {
            userIdentity = presenceSourcePeer.getUserIdentity();
        } catch (RemoteException e) {
            throw new InstantiationException(
                    "Cann't retrieve userIdentity from presenceSourcePeer");
        }

        if (userIdentity == null) {
            throw new InstantiationException(
                    "presenceSourcePeer#getUserIdentity returns null");
        }
        this.userIdentity = userIdentity;

        final PresenceDocument presenceDocument;
        try {
            presenceDocument = new DefaultPresenceDocument.PresenceDocBuilder()
                    .buildAccessType(AccessType.READ_WRITE)
                    .buildDocument(XMLUtils.createNewDocument()).build();
        } catch (IOException e) {
            throw new InstantiationException("Can't create new document model.");
        }

        this.document = presenceDocument;

        this.remotePresenceSourceListener = new RemotePresenceSourceListener(
                this, this);
        try {
            presenceSourcePeer.addListener(remotePresenceSourceListener);
        } catch (RemoteException e) {
            throw new InstantiationException(
                    "Cann't communicate with presenceSourcePeer");
        }
    }

    
    public PresenceDocument getPresenceDocument() {
        return document;
    }

    
    public int getState() {
        int retValue = -1;

        try {
            retValue = presenceSourcePeer.getState();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return retValue;
    }

    
    public void publish() throws ServiceClosedException, IOException {
        Log.i(TAG, "publish# started");
        if (done.get()) {
            throw new ServiceClosedException("Service already closed");
        }

        int state = getState();
        if (state == STATE_PENDING_PUBLISH || state == STATE_PENDING_UNPUBLISH) {
            throw new IllegalStateException(
                    "the PresenceSource is in STATE_PENDING_PUBLISH or STATE_PENDING_UNPUBLISH");
        }

        final String pidf = documentToString();

        try {
            presenceSourcePeer.publish(pidf);
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        Log.i(TAG, "publish# finished");
    }

    private String documentToString() throws IOException {
        final String pidf;
        if (isObjectModified(document)) {
            PresenceDocumentWriter writer = new PresenceDocumentWriter(
                    userIdentity, document);
            pidf = writer.toXML();
        } else {
            Document dom = document.getDOM();
            try {
                pidf = serializeToString(dom);
            } catch (TransformerException e) {
                throw new IOException(e.getMessage());
            }
        }
        return pidf;
    }

    private boolean isObjectModified(PresenceDocument document) {
        return document.getPersonInfo() != null
                || document.getDeviceInfo().length > 0
                || document.getServiceInfo().length > 0
                || document.getDirectContent().length > 0;
    }

    private String serializeToString(Document document)
            throws TransformerException {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    
    public void unpublish() throws IOException {
        Log.i(TAG, "unpublish# started");
        if (done.get()) {
            throw new ServiceClosedException("Service already closed");
        }

        int state = getState();
        if (state != STATE_ACTIVE) {
            throw new IllegalStateException(
                    "the PresenceSource isn't in STATE_ACTIVE");
        }

        try {
            presenceSourcePeer.unpublish();
        } catch (RemoteException e) {
            throw new IOException(e.getMessage());
        }
        Log.i(TAG, "unpublish# finished");
    }

    
    public void setListener(PresenceSourceListener listener) {
        this.listener = listener;
    }

    
    public void publicationDelivered(PresenceSource presenceSource) {
        Log.i(TAG, "publicationDelivered#");
        if (listener != null) {
            listener.publicationDelivered(presenceSource);
        }
    }

    
    public void publicationFailed(PresenceSource presenceSource,
            ReasonInfo reason) {
        Log.i(TAG, "publicationFailed#reason = " + reason);
        if (listener != null) {
            listener.publicationFailed(presenceSource, reason);
        }
    }

    
    public void publicationTerminated(PresenceSource presenceSource,
            ReasonInfo reason) {
        Log.i(TAG, "publicationTerminated#reason = " + reason);
        if (listener != null) {
            listener.publicationTerminated(presenceSource, reason);
        }
    }

    
    public void serviceClosed(ManagableConnection connection) {
        Log.i(TAG, "serviceClosed#");
        removeRemoteListener();
        connection.removeServiceCloseListener(this);
        done.set(true);
    }

    private void removeRemoteListener() {
        try {
            presenceSourcePeer.removeListener(remotePresenceSourceListener);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public String toString() {
        return "DefaultPresenceSource [document=" + document + ", done=" + done
                + ", listener=" + listener + "]";
    }
}
