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


public class Param {
    private String key;
    private String value;
    private boolean caseSensetiveValue; //TODO introduce this into code!! toLowerCase avoidance

    public Param(String aKey) {
        this(aKey, null);
    }

    public Param(String aKey, String aValue) {
        this.key = aKey;
        this.value = aValue;
    }

    public Param(String aKey, String aValue, boolean cs) {
        this(aKey, aValue);
        this.caseSensetiveValue = cs;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public boolean keyEquals(String aKey) {
        if (aKey.equals(this.key)) {
            return true;
        }
        return false;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Param)) {
            return false;
        }
        else if (!key.equals(((Param) other).getKey())) {
            return false;
        }
        else if (!value.equals(((Param) other).getValue())) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean keyEquals(Param aParam) {
        if (aParam.getKey().equals(this.key)) {
            return true;
        }
        return false;
    }

    public boolean isCaseSensetiveValue() {
        return caseSensetiveValue;
    }

    public void setCaseSensetiveValue(boolean caseSensetiveValue) {
        this.caseSensetiveValue = caseSensetiveValue;
    }

    
    public String toString() {
        return "Param [key=" + key
                + ", value=" + value + ", caseSensetiveValue=" + caseSensetiveValue + ", \ns()="
                + super.toString() + "]";
    }

    public void setValue(String value) {
        this.value = value;
    }

}
