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

import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.ParamListDefaultImpl;

public class ParamHeader {
    private final String value;
    private final ParamList paramsList;
    private final String content;
    private final String stringValue;

    protected ParamHeader(final ParamHeaderBuilder builder) {
        this.value = builder.value;
        this.paramsList = builder.paramsList == null ? null : ParamListDefaultImpl.unmodifableCopyOf(builder.paramsList);
        this.content = doBuildContent();
        this.stringValue = doBuildStringValue();
    }

    public String getValue() {
        return this.value;
    }

    public ParamList getParamsList() {
        return paramsList;
    }

    public String buildContent() {
        return content;
    }

    private String doBuildContent() {
        StringBuilder retValue = new StringBuilder();
        retValue.append(value);
        if (paramsList != null) {
            String content = paramsList.buildContent();
            if (content != null && content.length() > 0) {
                retValue.append(content);
            }
        }
        return retValue.toString();
    }

    
    public String toString() {
        return stringValue;
    }

    private String doBuildStringValue() {
        return "ParamHeader [value=" + value
                + ", paramsList=" + paramsList + ", \ns()="
                + super.toString() + "]";
    }

    public static class ParamHeaderBuilder {
        protected String value;
        protected ParamList paramsList;

        public ParamHeaderBuilder() {
        }

        public ParamHeaderBuilder(String value) {
            this.value = value;
        }

        public ParamHeaderBuilder(ParamHeader paramHeader) {
            this.value = paramHeader.getValue();

            final ParamListDefaultImpl paramList = new ParamListDefaultImpl();
            paramList.merge(paramHeader.getParamsList());
            this.paramsList = paramList;
        }

        public ParamHeaderBuilder value(final String value) {
            this.value = value;
            return this;
        }

        public ParamHeaderBuilder paramsList(final ParamList paramsList) {
            ensureParamListExists();

            this.paramsList.merge(paramsList);

            return this;
        }

        public ParamList getParamsList() {
            return paramsList;
        }

        public ParamHeaderBuilder param(final String param) {
            ensureParamListExists();

            this.paramsList.set(param);

            return this;
        }

        public ParamHeaderBuilder param(final String paramKey, final String paramValue) {
            ensureParamListExists();

            this.paramsList.set(paramKey, paramValue);

            return this;
        }

        public ParamHeaderBuilder paramList(final ParamList paramList) {
            ensureParamListExist();

            this.paramsList.merge(paramList);

            return this;
        }


        private void ensureParamListExist() {
            if (this.paramsList == null) {
                this.paramsList = new ParamListDefaultImpl();
            }
        }

        public ParamHeader build() {
            return new ParamHeader(this);
        }

        private void ensureParamListExists() {
            if (this.paramsList == null) {
                this.paramsList = new ParamListDefaultImpl();
            }
        }

        
        public String toString() {
            return "Builder{" +
                    "value='" + value + '\'' +
                    ", paramsList=" + paramsList +
                    '}';
        }
    }

}
