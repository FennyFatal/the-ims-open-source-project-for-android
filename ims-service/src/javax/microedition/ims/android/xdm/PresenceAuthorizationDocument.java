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
import org.xml.sax.SAXException;

import javax.microedition.ims.android.IExceptionHolder;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.xdm.XCAPException;
import javax.microedition.ims.core.xdm.XDMRequest;
import javax.microedition.ims.core.xdm.XDMResponse;
import javax.microedition.ims.core.xdm.XDMService;
import java.io.IOException;

public class PresenceAuthorizationDocument extends IPresenceAuthorizationDocument.Stub {
    private static final String TAG = "Service - PresenceAuthorizationDocument";

    private final String etag;
    private final String documentSelector;
    private final String xmlContent;
    private final XDMService xdmServicePeer;

    public PresenceAuthorizationDocument(
            XDMService xdmServicePeer,
            String etag,
            String documentSelector,
            String xmlContent) {

        assert xdmServicePeer != null;
        this.xdmServicePeer = xdmServicePeer;

        assert documentSelector != null;
        this.documentSelector = documentSelector;

        this.etag = etag;
        this.xmlContent = xmlContent;
    }

    public void applyChanges(String xcapDiffDocument) throws RemoteException {
        // TODO Auto-generated method stub

    }

    
    public String syncDocumentChanges(IXCAPRequest request,
                                      IExceptionHolder exceptionHolder) throws RemoteException {
        String etag = null;

        XDMRequest xcapRequest = Utils.createXDMRequest(request);

        try {
            XDMResponse response = xdmServicePeer.sendXCAPRequest(xcapRequest);
            etag = response.getEtag();
            //retrieve actual xml and update cache
        }
        catch (XCAPException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
            exceptionHolder.setParcelableException(Utils.createIXCAPException(e));
        }
        catch (SAXException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();

            //suppress warning here bacause we just create wrapper around exception and pass it to upper level
            //noinspection ThrowableInstanceNeverThrown
            exceptionHolder.setParcelableException(Utils.createIXCAPException(new IOException(e.getMessage())));
        }

        return etag;
    }

    
    public String toString() {
        return "PresenceAuthorizationDocument [documentSelector=" + documentSelector
                + ", etag=" + etag + "]";
    }

}
