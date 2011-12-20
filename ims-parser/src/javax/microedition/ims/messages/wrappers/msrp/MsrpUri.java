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

package javax.microedition.ims.messages.wrappers.msrp;

import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.ParamListDefaultImpl;
import javax.microedition.ims.messages.wrappers.common.Uri;

public class MsrpUri implements Uri {
    private String prefix;
    private String username;
    private String domain;
    private String sessionId;
    private Protocol transport;
    private int port;
    private ParamList params = new ParamListDefaultImpl();


    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String scheme) {
        this.prefix = scheme;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getId() {
        return sessionId;
    }

    public void setId(String id) {
        this.sessionId = id;
    }

    public Protocol getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        if (transport == null) {
            throw new IllegalArgumentException("Transport cannot be null");
        }

        if (Protocol.TCP.name().toLowerCase().equals(transport.toLowerCase())) {
            this.transport = Protocol.TCP;
        }
        else if (Protocol.UDP.name().toLowerCase().equals(transport.toLowerCase())) {
            this.transport = Protocol.UDP;
        }
        else if (Protocol.TLS.name().toLowerCase().equals(transport.toLowerCase())) {
            this.transport = Protocol.TLS;
        }
    }

    public void setTransport(Protocol transport) {
        this.transport = transport;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ParamList getParams() {
        return params;
    }

    public void setParams(ParamList params) {
        this.params = params;
    }

    public ParamList getParamsList() {
        return params;
    }

    public String getShortURI() {
        throw new UnsupportedOperationException();
    }

    public ParamList getHeaders() {
        throw new UnsupportedOperationException();
    }

    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    public String getPassword() {
        throw new UnsupportedOperationException();
    }

    public String getShortURINoParams() {
        throw new UnsupportedOperationException();
    }

    public String buildContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("://");
        if (username != null && username.length() > 0) {
            sb.append(username).append("@");
        }
        sb.append(domain);
        if (port > 0) {
            sb.append(":").append(port);
        }
        sb.append("/").append(sessionId).append(";").append(transport.name().toLowerCase());
        sb.append(params.buildContent());
        return sb.toString();
    }


}

