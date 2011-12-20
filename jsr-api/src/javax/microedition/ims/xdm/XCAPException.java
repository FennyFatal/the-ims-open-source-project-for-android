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

package javax.microedition.ims.xdm;

/**
 * An <code>XCAPException</code> contains information about an error received
 * from an XCAP server. This exception is thrown when a request that is sent to
 * an XCAP server fails. All methods that makes an HTTP request to an XCAP
 * Server must throw this exception.
 * 
 * The <code>XCAPException</code> contains the HTTP status code and the reason
 * phrase with information about the failed request.
 * 
 * If the status code is 409 - Conflict Error, an <code>XCAPError</code> with
 * detailed information about that specific conflict can be obtained by calling
 * <code>getXCAPError</code>.
 * 
 * @author Andrei Khomushko
 * 
 */
public class XCAPException extends Exception {
    private static final long serialVersionUID = 1L;

    private int statusCode;
    private String reasonPhrase;
    private XCAPError xcapError;

    /**
     * 
     * @param statusCode
     *            - the HTTP status code
     * @param reasonPhrase
     *            - the reason phrase
     * @param xcapError
     *            - an XCAPError or null
     * 
     * @throws IllegalArgumentException
     *             - if the statusCode argument or the reasonPhrase argument is
     *             null
     */
    public XCAPException(int statusCode, final String reasonPhrase,
            final XCAPError xcapError) {
        if (statusCode == 0 || reasonPhrase == null) {
            throw new IllegalArgumentException(
                    "Illegeal statusCode or reasonPhrase arguments");
        }

        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.xcapError = xcapError;
    }

    /**
     * Returns the HTTP status code.
     * 
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the reason phrase according to the HTTP protocol.
     * 
     * @return the reason phrase
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Returns an XCAPError with detailed information of the error. This method
     * will only return a value if the status code was 409.
     * 
     * @return an XCAPError or null if the status code is not 409
     */
    public XCAPError getXcapError() {
        return xcapError;
    }

    
    public String toString() {
        return "XCAPException [reasonPhrase=" + reasonPhrase + ", statusCode="
                + statusCode + ", xcapError=" + xcapError + "]";
    }
}
