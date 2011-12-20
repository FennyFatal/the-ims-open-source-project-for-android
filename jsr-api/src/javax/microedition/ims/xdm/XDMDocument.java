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

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The <code>XDMDocument</code> interface is a representation of an XML document
 * that is stored on an XDM server. The XDMDocument has an ETag associated with
 * the content to indicate the version of the document. The XML document on the
 * server might be changed from several devices and each application is
 * responsible for making sure that the local copy is up-to-date. The
 * application can subscribe to changes through the XDMService interface and
 * receive notifications of document changes through the
 * DocumentSubscriberListener interface.
 * 
 * <p>
 * <h4>Retrieving a document</h4>
 * </p>
 * An XDMDocument can be obtained in two ways. The application can retrieve a
 * document from the server by using the static <code>retrieveDocument</code>
 * method on the corresponding subclass for the specific document. When using
 * this method the complete document will be fetched and the implementation will
 * store an internal representation of the document that is passed to the
 * application. </br> The other way is to load the document from a copy cached
 * by the application with the static <code>loadDocument</code> method. This
 * method is useful if the document has not been changed on the server and the
 * application wants to obtain the document without fetching it from the server.
 * See example below. </br> XML that is not supported will be discarded, meaning
 * that extensions or unsupported XML might be lost when modifying the document
 * on the server.
 * 
 * <p>
 * <h4>Modifying a document</h4>
 * </p>
 * When the XDMDocument has been obtained the application can do partial changes
 * to the document. All changes done to the document will be sent to the server
 * as XCAP requests. The ETag on the XDMDocument will be updated if the request
 * is successful.
 * 
 * <p>
 * <h4>Saving a document</h4>
 * </p>
 * The XDMDocument can be saved locally by using the saveDocument method. The
 * saved copy might differ from the document on the XDM server, since XML that
 * is not supported will be discarded when the document is retrieved.
 * 
 * <p>
 * <h4>Conditional operations</h4>
 * </p>
 * It is possible to make conditional operations towards an XDM server. This is
 * because it is anticipated that an application using this API might want to
 * cache a retrieved document and still ensure that the version of the document
 * residing on the server is the same as the cached copy. </br> For example,
 * when calling retrieveDocument an optional ETag can be supplied by using the
 * ETag parameter. If this is done, the ETag will be compared to the ETag that
 * the server holds for that document. If the server's and the client's ETags
 * match, the requested operation will not be performed. In the case of
 * <code>retrieveDocument</code>, an <code>XCAPException</code> will be thrown
 * with the status code 412 Precondition Failed, thus giving the user the
 * opportunity to load its cached copy with <code>loadDocument</code> instead.
 * </br> The ETag will be automatically updated when any a call to
 * <code>createDocument</code> is made, usage of ETag is not optional. The ETag
 * for the document will always be set based on server response and therefore
 * all operations from then on will be conditional. To make unconditional
 * operations on the newly created document a call to retrieveDocument without
 * supplying the ETag must be made.
 * 
 * @author Andrei Khomushko
 */
public interface XDMDocument {

    /**
     * Modifies the document according to an XCAP diff XML document received
     * from the server. </br> This method can be used to keep the local copy of
     * the document synchronized with the document on the server. The XCAP diff
     * XML document is received when the client subscribes to changes in the
     * document. See DocumentSubscriber for details on how subscription works,
     * and when and how this method should be called.
     * 
     * @param xcapDiff
     *            - the XCAP diff document as received from the server
     * @throws IllegalArgumentException
     *             - if the xcapDiff argument is null
     */
    void applyChanges(Document xcapDiff);

    /**
     * Returns the document selector of this document.
     * 
     * @return the document selector
     */
    String getDocumentSelector();

    /**
     * Returns the ETag of this document.
     * 
     * @return the ETag or null if the ETag is not available
     */
    String getEtag();

    /**
     * Saves the document to an OutputStream. The saved copy might differ from
     * the document on the XDM server, since XML that is not supported will be
     * discarded.
     * 
     * @param os
     *            - the output stream to write the content to
     * 
     * @throws IOException
     *             - if an I/O error occurs 
     * @throws IllegalArgumentException - if the os argument is null
     */
    void saveDocument(OutputStream os) throws IOException;
}
