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

package javax.microedition.ims;

/**
 * An ImsException indicates an unexpected error condition in a method.
 */
public class ImsException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public static final int UNSPECIFIED_ERROR = -1;

    /** Common error codes */
    public static final int ERROR_WRONG_PARAMETERS = 1;
    public static final int ERROR_DNS_LOOKUP = 2;
    public static final int ERROR_STACK_CONFIGURATION = 3;

    /** Device side error codes */
    public static final int ERROR_TIMEOUT = 11;
    public static final int ERROR_CONNECTIVITY = 12;
    public static final int ERROR_NON_T_MOBILE_SIM = 13;
    public static final int ERROR_NOT_SUPPORTED_SIM = 14;
    public static final int ERROR_SIM_INFO_MISSING = 15;

    /** DNS Query failures  */
    public static final int ERROR_NAPTR_QUERY_FAILED = 21;
    public static final int ERROR_SRV_QUERY_FAILED = 22;
    public static final int ERROR_ARECORD_QUERY_FAILED = 23;

    public static final int IMPU_MISSING_OTA_FAILED = 24;
    public static final int GBA_U_FAILED = 25;
    

    /** TLS failures */
    public static final int ERROR_UNTRUSTED_SERVER_CERTIFICATE = 30;

    private final int code;
    private final String reasonData;

    /**
     * Constructs an ImsException with null as its error detail message.
     */
/*    public ImsException() {
        
    }
*/
    public ImsException(String message) {
        this(message, null);
    }

    /**
     * Constructs an ImsException with the specified detail message.
     *
     * @param reason
     */
    public ImsException(String message, Throwable throwable) {
        this(UNSPECIFIED_ERROR, message, null, throwable);
    }
    
    public ImsException(int code, String message, String reasonData) {
        this(code, message, reasonData, null);
    }

    private ImsException(int code, String message, String reasonData, Throwable throwable) {
        super(message, throwable);
        this.code = code;
        this.reasonData = reasonData;
    }
    
    public int getCode() {
        return code;
    }

    public String getReasonData() {
        return reasonData;
    }

    @Override
    public String toString() {
        return "ImsException [code=" + code + ", reasonData=" + reasonData + ", toString()="
                + super.toString() + "]";
    }
}
