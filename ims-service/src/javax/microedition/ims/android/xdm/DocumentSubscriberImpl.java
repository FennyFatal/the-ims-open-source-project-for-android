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

package javax.microedition.ims.android.xdm;

import android.os.RemoteException;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.util.ListenerHolder;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.EventPackage;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.dialog.IncomingNotifyListener;
import javax.microedition.ims.core.sipservice.subscribe.SubscribeService;
import javax.microedition.ims.core.sipservice.subscribe.Subscription;
import javax.microedition.ims.core.sipservice.subscribe.listener.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation {@link IDocumentSubscriber.Stub}
 *
 * @author Khomushko
 */
public class DocumentSubscriberImpl extends IDocumentSubscriber.Stub implements
        SubscriptionStateListener, IncomingNotifyListener {
    private static final String TAG = "DocumentSubscriberImpl";

    private final String[] urls;
    private final SubscribeService subscribeService;
    private final ClientIdentity localParty;
    private final String remoteParty;
    private final AtomicReference<Subscription> subscriptionHolder = new AtomicReference<Subscription>();

    private final ListenerHolder<IDocumentSubscriberListener> listenerHolder = new RemoteListenerHolder<IDocumentSubscriberListener>(
            IDocumentSubscriberListener.class);

    //private XCAPDifDocDescriptor parseXcapDiffDoc;

    public DocumentSubscriberImpl(final String[] urls,
                                  final SubscribeService subscribeService,
                                  final ClientIdentity localParty,
                                  final String remoteParty) {
        this.urls = urls;
        this.subscribeService = subscribeService;
        this.localParty = localParty;
        this.remoteParty = remoteParty;
    }


    public void addListener(IDocumentSubscriberListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }

    
    public String[] getURLs() throws RemoteException {
        return urls;
    }

    
    public void removeListener(IDocumentSubscriberListener listener)
            throws RemoteException {
        listenerHolder.addListener(listener);
    }


    public void subscribe() throws RemoteException {
        // TODO Auto-generated method stub
        Subscription subscription = subscribeService.lookUpDocumentChangesSubscription(
                localParty,
                remoteParty,
                Arrays.asList(urls)
        );

        subscription.addSubscriptionStateListener(this);
        subscription.addSubscriptionNotifyListener(this);
        subscription.subscribe();

        subscriptionHolder.set(subscription);
    }

    
    public void unsubscribe() throws RemoteException {
        // TODO Auto-generated method stub
        Subscription subscription = subscriptionHolder.get();
        if (subscription != null) {
            subscription.removeSubscriptionStateListener(this);
            subscription.removeSubscriptionNotifyListener(this);
        }
        else {
            Log.i(TAG, "The subscription isn't set");
        }
    }

    
    public void onSubscriptionRefreshFailed(SubscriptionFailedEvent event) {
        Log.i(TAG, "onSubscriptionRefreshFailed#event = " + event);
    }

    
    public void onSubscriptionRefreshed(SubscriptionStateEvent event) {
        Log.i(TAG, "onSubscriptionRefreshed#event = " + event);
    }

    
    public void onSubscriptionStartFailed(SubscriptionFailedEvent event) {
        Log.i(TAG, "onSubscriptionStartFailed#event = " + event);

        IReasonInfo reason = new IReasonInfo(event.getReasonPhrase(), -1, event.getStatusCode());
        try {
            listenerHolder.getNotifier().subscriptionFailed(reason);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public void onSubscriptionStarted(SubscriptionStateEvent event) {
        Log.i(TAG, "onSubscriptionStarted#event = " + event);
        try {
            listenerHolder.getNotifier().subscriptionStarted();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public void onSubscriptionTerminated(SubscriptionTerminatedEvent event) {
        Log.i(TAG, "onSubscriptionTerminated#event = " + event);
        try {
            listenerHolder.getNotifier().subscriptionTerminated();
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public void notificationReceived(NotifyEvent event) {
        Log.i(TAG, "notificationReceived#event = " + event);

        String[] xcapDiffs = event.getNotifyInfo().getNotifyBodyMessages();
        String xcapDiff = (xcapDiffs != null && xcapDiffs.length > 0 ? xcapDiffs[0] : null);

        try {
            XCAPDifDocDescriptor xcapDiffDoc = parseXcapDiffDoc(xcapDiff);
            if (xcapDiffDoc.newTag != null) {
                documentUpdateReceived(xcapDiffDoc.docSelector, xcapDiff);
            }
            else {
                documentDeleted(xcapDiffDoc.docSelector, xcapDiff);
            }
        }
        catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }


        String documentSelector = null;


        if (event.getNotifyInfo().getEventPackage() == EventPackage.XCAP_DIFF) {
            documentUpdateReceived(documentSelector, xcapDiff);
        }
        else {
            documentDeleted(documentSelector, xcapDiff);
        }
    }

    class XCAPDifDocDescriptor {
        private String docSelector;
        private String newTag;

        private XCAPDifDocDescriptor(String docSelector, String newTag) {
            this.docSelector = docSelector;
            this.newTag = newTag;
        }
    }

    private XCAPDifDocDescriptor parseXcapDiffDoc(String source) throws IOException {
        final XCAPDifDocDescriptor retValue;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            InputSource inStream = new org.xml.sax.InputSource();
            inStream.setCharacterStream(new StringReader(source));
            DocumentBuilder db = factory.newDocumentBuilder();
            Document doc = db.parse(inStream);
            NodeList nodeList = doc.getElementsByTagName("document");
            Node docNode = nodeList.item(0);

            String newETag = null;
            String selector = null;

            Node newEtagItem = docNode.getAttributes().getNamedItem("new-etag");
            if (newEtagItem != null) {
                newETag = newEtagItem.getNodeValue();
            }
            selector = docNode.getAttributes().getNamedItem("sel").getNodeValue();

            retValue = new XCAPDifDocDescriptor(selector, newETag);
        }
        catch (ParserConfigurationException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
        catch (SAXException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }

        return retValue;
    }

    private void documentDeleted(String documentSelector, String xcapDiff) {
        Log.i(TAG, "documentDeleted#documentSelector = " + documentSelector);
        try {
            listenerHolder.getNotifier().documentDeleted(documentSelector, xcapDiff);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void documentUpdateReceived(String documentSelector, String xcapDiff) {
        Log.i(TAG, "documentUpdateReceived#documentSelector = " + documentSelector);
        try {
            listenerHolder.getNotifier().documentUpdateReceived(documentSelector, xcapDiff);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    
    public String toString() {
        return "DocumentSubscriberImpl [urls=" + Arrays.toString(urls) + "]";
    }
}
