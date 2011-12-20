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

package javax.microedition.ims.core;

import javax.microedition.ims.config.UserInfo;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com), ext-akhomush
 * Date: 14-Dec-2009
 * Time: 18:12:54
 */
public final class ClientIdentityImpl implements ClientIdentity {
    private final static String DEFAULT_SCHEMA = "imscore";

    private final String schema;
    private final String appId;
    private final UserInfo userInfo;

    public static class Creator {
        /**
         * Template:
         * {shema}://{appId};{shema}:{name}@{domain}
         *
         * @param uri
         * @param user
         * @return
         */
        public static ClientIdentityImpl createFromUriAndUser(final String uri, final UserInfo user) {
            String schema = null;
            String appId = null;
            UserInfo userInfo = user;

            if (uri != null) {
                String[] exprs = uri.split("://", 2);
                if (exprs.length > 0) {
                    schema = exprs[0].trim();
                }

                if (exprs.length == 2) {
                    exprs = exprs[1].split(";");
                    if (exprs.length > 0) {
                        appId = exprs[0].trim();
                    }
                    else {
                        appId = exprs[1].trim();
                    }

                    if (userInfo == null && exprs.length == 2) {
                        String clientName = exprs[1].trim();
                        userInfo = UserInfo.valueOf(clientName);
                    }
                }
            }
            return new ClientIdentityImpl(schema, appId, userInfo);
        }

        public static ClientIdentityImpl createFromUri(final String uri) {
            return createFromUriAndUser(uri, null);
        }

        public static ClientIdentityImpl createFromUserInfo(final String appId, final UserInfo userInfo) {
            return new ClientIdentityImpl(DEFAULT_SCHEMA, appId, userInfo);
        }
    }

    private ClientIdentityImpl(final String schema, final String appId, final UserInfo userInfo) {
        this.schema = schema;
        this.appId = appId;
        this.userInfo = userInfo;
    }

    public String getAppID() {
        return appId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getSchema() {
        return schema;
    }

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appId == null) ? 0 : appId.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        result = prime * result
                + ((userInfo == null) ? 0 : userInfo.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ClientIdentityImpl other = (ClientIdentityImpl) obj;
        if (appId == null) {
            if (other.appId != null) {
                return false;
            }
        }
        else if (!appId.equals(other.appId)) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        }
        else if (!schema.equals(other.schema)) {
            return false;
        }
        if (userInfo == null) {
            if (other.userInfo != null) {
                return false;
            }
        }
        else if (!userInfo.equals(other.userInfo)) {
            return false;
        }
        return true;
    }

    
    public String toString() {
        return "ClientIdentityImpl [appId=" + appId + ", schema=" + schema
                + ", userInfo=" + userInfo + "]";
    }
}
