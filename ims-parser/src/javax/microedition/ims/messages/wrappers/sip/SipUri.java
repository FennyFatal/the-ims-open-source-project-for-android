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

package javax.microedition.ims.messages.wrappers.sip;

import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.ParamListDefaultImpl;
import javax.microedition.ims.messages.wrappers.common.Uri;

public class SipUri extends ParamHeader implements Uri {

    private final String displayName;
    private final String username;
    private final String password;
    private final String domain;
    private final String prefix;
    private final int port;
    private final ParamList headers;
    private final String stringValue;
    private final String shortURINoParams;
    private final String shortURI;
    private final String content;


    private SipUri(final SipUriBuilder builder) {
        super(new SipUriBuilder().value(builder.value).paramsList(builder.paramsList));

        this.displayName = builder.displayName;
        this.username = builder.username;
        this.password = builder.password;
        this.domain = builder.domain;
        this.prefix = builder.prefix;
        this.port = builder.port;
        this.headers = builder.headers == null ? null : ParamListDefaultImpl.unmodifableCopyOf(builder.headers);

        this.stringValue = doBuildStringValue();
        this.shortURINoParams = doBuildShortURINoParams();
        this.shortURI = doBuildShortUri();
        this.content = doBuildContent();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPassword() {
        return password;
    }


    public String getDomain() {
        return domain;
    }


    public String getPrefix() {
        return prefix;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }


    public ParamList getHeaders() {
        return headers;
    }


    public String buildContent() {
        return content;
    }

    private String doBuildContent() {
        StringBuilder retValue = new StringBuilder();
        if (displayName != null) {
            retValue.append("\"").append(displayName.trim()).append("\"").append(StringUtils.SPACE);
        }
        if (getShortURI() != null && getShortURI().length() > 0) {
            retValue.append("<").append(getShortURI());
            retValue.append(">");
        }

        if (headers != null) {
            retValue.append(headers.getContent(";"));
        }

        return retValue.toString();
    }

    public String getShortURI() {
        return shortURI;
    }

    private String doBuildShortUri() {
        StringBuilder retValue = new StringBuilder();

        if (prefix != null) {
            retValue.append(prefix).append(":");
        }
        retValue.append(doBuildSipAddress(true));

        return retValue.toString();
    }

    public String getShortURINoParams() {
        return shortURINoParams;
    }

    private String doBuildShortURINoParams() {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(":");
        sb.append(doBuildSipAddress(false));
        return sb.toString();
    }

    private String doBuildSipAddress(boolean params) {
        StringBuilder sb = new StringBuilder();
        if (username != null) {
            sb.append(username);
        }
        if (password != null) {
            sb.append(":").append(password);
        }
        if (username != null && domain != null) {
            sb.append("@");
        }
        if (domain != null) {
            sb.append(domain);
        }

        if (port > 0) {
            sb.append(":").append(port);
        }
        if (getParamsList() != null && params) {
            sb.append(getParamsList().getContent(";"));
        }
        return sb.toString();
    }


    public String toString() {
        return stringValue;
    }

    private String doBuildStringValue() {
        return "SipUri [displayName=" + displayName +
                " username=" + username +
                " password=" + password +
                " domain=" + domain +
                " prefix=" + prefix +
                " port=" + port +
                ", \ns()="
                + super.toString() + "]";
    }

    public static class SipUriBuilder extends ParamHeaderBuilder {
        String displayName;
        String username;
        String password;
        String domain;
        String prefix;
        Integer port = -1;
        ParamList headers;

        public SipUriBuilder() {
            ensureHeadersListExist();
        }

        public SipUriBuilder(final Uri uri) {
            this();
            this.displayName = uri.getDisplayName();
            this.username = uri.getUsername();
            this.password = uri.getPassword();
            this.domain = uri.getDomain();
            this.prefix = uri.getPrefix();
            this.port = uri.getPort();

            final ParamListDefaultImpl paramList = new ParamListDefaultImpl();
            paramList.merge(uri.getHeaders());
            this.headers = paramList;
        }

        public SipUriBuilder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public SipUriBuilder username(final String username) {
            this.username = username;
            return this;
        }

        public SipUriBuilder password(final String password) {
            this.password = password;
            return this;
        }

        public SipUriBuilder domain(final String domain) {
            this.domain = domain;
            return this;
        }

        public SipUriBuilder prefix(final String prefix) {
            this.prefix = prefix;
            return this;
        }

        public SipUriBuilder port(final int port) {
            this.port = port;
            return this;
        }

        public SipUriBuilder headers(final ParamList headers) {
            ensureHeadersListExist();

            this.headers.merge(headers);

            return this;
        }

        public ParamList getHeaders() {
            return headers;
        }

        private void ensureHeadersListExist() {
            if (this.headers == null) {
                this.headers = new ParamListDefaultImpl();
            }
        }

        public Uri buildUri() {
            return new SipUri(this);
        }
    }

}
