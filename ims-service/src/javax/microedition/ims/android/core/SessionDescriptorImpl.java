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

package javax.microedition.ims.android.core;

import android.os.RemoteException;

import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ext-akhomush
 * @see ISessionDescriptor
 */
public class SessionDescriptorImpl extends ISessionDescriptor.Stub {
    //server SDP + client SDP
    //getters read from the last server SDP
    //setters apply to all client SDPs in the
    //current SESSION, beginning with the one to be sent next

    private static final List<String> RESERVED_ATTRIBUTES = Collections.synchronizedList(Arrays.asList(new String[]{"charset", "charset:iso8895-1", "group", "maxprate", "ice-lite", "ice-mismatch", "ice-options", "ice-pwd", "ice-ufrag", "inactive", "sendonly", "recvonly", "sendrecv", "csup", "creq", "acap", "tcap"}));

    private SdpMessage outgoingSdpMessage, incomingSdpMessage;

    public SessionDescriptorImpl(SdpMessage outgoingSdpMessage, SdpMessage incomingSdpMessage) {
        this.outgoingSdpMessage = outgoingSdpMessage;
        this.incomingSdpMessage = incomingSdpMessage;
    }

    
    public void addAttribute(String attribute) throws RemoteException {
        if (!RESERVED_ATTRIBUTES.contains(attribute)) {
            outgoingSdpMessage.getAttributes().add(new Attribute(attribute, null));
        }
        else {
            //throw new IllegalArgumentException
        }
    }

    
    public String[] getAttributes() throws RemoteException {
        String[] ret = new String[incomingSdpMessage.getAttributes().size()];
        int i = 0;
        for (Attribute a : incomingSdpMessage.getAttributes()) {
            ret[i++] = a.getContent();
        }
        return ret;
    }

    public String getProtocolVersion() throws RemoteException {
        return String.valueOf(incomingSdpMessage.getVersion());
    }

    
    public String getSessionId() throws RemoteException {
        return incomingSdpMessage.getSessionOrigin();
    }

    public String getSessionInfo() throws RemoteException {
        return incomingSdpMessage.getSessionInformation();
    }

    
    public String getSessionName() throws RemoteException {
        return incomingSdpMessage.getSessionName();
    }

    
    public void removeAttribute(String attribute) throws RemoteException {
        if (!RESERVED_ATTRIBUTES.contains(attribute)) {
            outgoingSdpMessage.getAttributes().remove(new Attribute(attribute, null));
        }
        else {
            //throw new IllegalArgumentException
        }
    }

    
    public void setSessionInfo(String info) throws RemoteException {
        outgoingSdpMessage.setSessionInformation(info);
    }

    
    public void setSessionName(String name) throws RemoteException {
        outgoingSdpMessage.setSessionName(name);
    }
}
