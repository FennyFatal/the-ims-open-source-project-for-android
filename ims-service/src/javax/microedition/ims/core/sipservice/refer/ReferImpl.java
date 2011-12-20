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

package javax.microedition.ims.core.sipservice.refer;

import javax.microedition.ims.common.TimeoutUnit;
import javax.microedition.ims.core.dialog.Dialog;

/*
 * ReferImpl-To: sip:somebody@somewhere.com
 * 
 * ReferImpl-To: <sip:somebody@somewhere.com;method=SIP_SUBSCRIBE>
 * 
 * ReferImpl-To: http://www.somewhere.org
 * 
 * More examples in RFC 3515, 2.1
 */
public class ReferImpl implements Refer {

    private final String referTo;
    private final String referMethod;
    private final Dialog dialog;
    private final TimeoutUnit timeoutUnit;

    public ReferImpl(
            final Dialog dialog,
            final String referTo,
            final String referMethod,
            final TimeoutUnit timeoutUnit) {
        this.dialog = dialog;
        this.timeoutUnit = timeoutUnit;

        if (referTo == null) {
            throw new IllegalArgumentException("referTo parameter can not be null. Now it has value " + referTo);
        }

        if (referMethod == null) {
            throw new IllegalArgumentException("referMethod parameter can not be null. Now it has value " + referMethod);
        }

        this.referTo = referTo;
        this.referMethod = referMethod;
    }

    
    public String getReferTo() {
        return referTo;
    }

    
    public String getReferMethod() {
        return referMethod;
    }

    public Dialog getDialog() {
        return dialog;
    }

    
    public TimeoutUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReferImpl refer = (ReferImpl) o;

        if (dialog != null ? !dialog.equals(refer.dialog) : refer.dialog != null) {
            return false;
        }
        if (referMethod != null ? !referMethod.equals(refer.referMethod) : refer.referMethod != null) {
            return false;
        }
        return !(referTo != null ? !referTo.equals(refer.referTo) : refer.referTo != null);

    }

    
    public int hashCode() {
        int result = referTo != null ? referTo.hashCode() : 0;
        result = 31 * result + (referMethod != null ? referMethod.hashCode() : 0);
        result = 31 * result + (dialog != null ? dialog.hashCode() : 0);
        return result;
    }

    
    public String toString() {
        return "ReferImpl{" +
                "referTo='" + referTo + '\'' +
                ", referMethod='" + referMethod + '\'' +
                ", DIALOG=" + dialog +
                '}';
    }
}
