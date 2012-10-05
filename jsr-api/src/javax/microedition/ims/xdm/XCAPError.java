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
 * An <code>XCAPError</code> contains detailed information of an XCAP error that
 * is received from an XCAP server. 
 * 
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 * @author Andrei Khomushko
 * 
 */
public interface XCAPError {
    /**
     * Indicates that the requested DELETE operation could not be performed
     * because it would not be idempotent.
     */
    int XCAP_ERROR_CANNOT_DELETE = 7;

    /**
     * Indicates that the requested PUT operation could not be performed because
     * a GET of that resource after the PUT would not yield the content of the
     * PUT request.
     */
    int XCAP_ERROR_CANNOT_INSERT = 6;

    /**
     * Indicates that the requested operation would result in a document that
     * failed a data constraint defined by the application usage, but not
     * enforced by the schema or a uniqueness constraint.
     */
    int XCAP_ERROR_CONSTRAINT_FAILURE = 9;

    /** Indicates an error condition that is defined by an extension to XCAP. */
    int XCAP_ERROR_EXTENSION = 10;

    /**
     * Indicates that an attempt to insert a document, element, or attribute
     * failed because the directory, document, or element into which the
     * insertion was supposed to occur did not exist.
     */
    int XCAP_ERROR_NO_PARENT = 3;

    /**
     * Indicates that the request could not be completed because it would have
     * produced a document not encoded in UTF-8.
     */
    int XCAP_ERROR_NOT_UTF_8 = 11;

    /**
     * Indicates that the body of the request was not a well-formed XML
     * document.
     */
    int XCAP_ERROR_NOT_WELL_FORMED = 1;

    /**
     * Indicates that the request was supposed to contain a valid XML attribute
     * value, but did not.
     */
    int XCAP_ERROR_NOT_XML_ATT_VALUE = 5;

    /**
     * Indicates that the request was supposed to contain a valid XML fragment
     * body, but did not.
     */
    int XCAP_ERROR_NOT_XML_FRAG = 2;

    /**
     * Indicates that the document was not compliant to the schema after the
     * requested operation was performed.
     */
    int XCAP_ERROR_SCHEMA_VALIDATION_ERROR = 4;

    /**
     * Indicates that the requested operation would result in a document that
     * did not meet a uniqueness constraint defined by the application usage.
     */
    int XCAP_ERROR_UNIQUENESS_FAILURE = 8;

    /**
     * Returns the closest ancestor if returned by the XCAP server as a result
     * of an XCAP_ERROR_NO_PARENT error.
     * 
     * @return the closest ancestor or null if the error type is not
     *         XCAP_ERROR_NO_PARENT
     */
    public String getClosestAncestor();

    /**
     * Returns the content of the extension element if returned by the XCAP
     * server as a result of an XCAP_ERROR_EXTENSION error.
     * 
     * @return the content or null if the error type is not XCAP_ERROR_EXTENSION
     */
    public String getExtensionContent();

    /**
     * Returns descriptions of the uniqueness errors returned by the XCAP server
     * as a result of an XCAP_ERROR_UNIQUENESS_FAILURE error.
     * 
     * @return an array of uniqueness errors or an empty array if the error type
     *         is not XCAP_ERROR_UNIQUENESS_FAILURE
     */
    public UniquenessError[] getUniquenessErrors();

    /**
     * Returns the XCAP error phrase of this XCAPError.
     * 
     * @return the XCAP error phrase or null if there is no XCAP error phrase
     */
    public String getXCAPErrorPhrase();

    /**
     * Returns the XCAP error type of this XCAPError.
     * 
     * @return the XCAP error type
     */
    public int getXCAPErrorType();
}
