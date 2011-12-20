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

package javax.microedition.ims.messages.wrappers.common;

import java.util.*;

public class ParamListDefaultImpl implements ParamList {

    public static ParamList unmodifableCopyOf(final ParamList paramList) {
        return new ParamListDefaultImpl(
                Collections.unmodifiableMap(
                        new HashMap<String, Param>(paramList.getParams())
                )
        );
    }

    private final Map<String, Param> params;

    public ParamListDefaultImpl() {
        this(new LinkedHashMap<String, Param>());
    }

    private ParamListDefaultImpl(final Map<String, Param> params) {
        this.params = params;
    }

    public void set(String aKey) {
        params.put(aKey, null);
    }

    public void set(String aKey, String aValue) {
        params.put(aKey, new Param(aKey, aValue));
    }

    public void set(String aKey, String aValue, boolean cs) {
        params.put(aKey, new Param(aKey, aValue, cs));
    }

    public void set(Param aParam) {
        params.put(aParam.getKey(), aParam);
    }

    public void unset(String aKey) {
        params.remove(aKey);
    }

    public boolean containsKey(Param aParam) {
        return containsKey(aParam.getKey());
    }

    public boolean containsKey(String aKey) {
        return params.containsKey(aKey);
    }

    public boolean containsFull(String key, String aValue) {
        return params.containsKey(key) && params.containsValue(new Param(key, aValue));
    }

    public boolean containsFull(Param aParam) {
        return params.containsValue(aParam);
    }

    public Param get(String aKey) {
        return params.get(aKey);
    }

    public void merge(ParamList paramList) {
        if (paramList != null && paramList.getParams() != null) {
            params.putAll(paramList.getParams());
        }

    }

    public Map<String, Param> getParams() {
        return params;
    }

    public String buildContent() {
        return getContent(";");
    }

    public String getContent(String separator) {
        Iterator<String> it = params.keySet().iterator();
        StringBuilder sb = new StringBuilder();
        String key = null;
        while (it.hasNext()) {
            key = it.next();
            sb.append(separator).append(key);
            if (params.get(key) != null && params.get(key).getValue() != null) {
                sb.append("=").append(params.get(key).getValue());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ParamListDefaultImpl [" + getContent(";");
    }

}
