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

package javax.microedition.ims.core.sipservice.register;

import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Protocol;
import java.net.InetSocketAddress;
import java.util.EnumSet;

/**
 * * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 13.8.2010
 * Time: 16.50.47
 */
public class RedirectDataImpl implements RedirectData {
    private final static EnumSet<Protocol> allowedProtocols = EnumSet.of(Protocol.TCP, Protocol.UDP);

    private final InetSocketAddress address;
    private final Protocol protocol;
    private final IMSMessage causeMessage;
    private final Long expires;

    public RedirectDataImpl(
            final InetSocketAddress address,
            final Protocol protocol,
            final Long expires,
            final IMSMessage causeMessage) {

        if (!allowedProtocols.contains(protocol)) {
            final String errMsg = "The only allowed protocols are " + allowedProtocols + ". Now is " + protocol + ".";
            throw new IllegalArgumentException(errMsg);
        }

        this.address = address;
        this.protocol = protocol;
        this.expires = expires;
        this.causeMessage = causeMessage;
    }

    public InetSocketAddress getRedirectAddress() {
        return address;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public IMSMessage getCauseMessage() {
        return causeMessage;
    }

    public Long getExpires() {
        return expires;
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RedirectDataImpl aDefault = (RedirectDataImpl) o;

        return !(address != null ? !address.equals(aDefault.address) : aDefault.address != null) &&
                protocol == aDefault.protocol;

    }


    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        return result;
    }


    public String toString() {
        return "RedirectEventDefaultImpl{" +
                "address=" + address +
                ", protocol=" + protocol +
                ", causeMessage=" + causeMessage +
                '}';
    }
}
