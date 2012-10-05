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

import javax.microedition.ims.ServiceClosedException;
import java.io.IOException;

/**
 * A <code>URIList</code> contains a number of <code>ListEntry</code> objects.
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 * 
 * @see URIListDocument, ListEntry
 * 
 * @author Andrei Khomushko
 * 
 */
public interface URIList {
    /**
     * Adds a ListEntry to this URIList and submits the changes to the document
     * on the XDM server. This method is synchronous and will block until the
     * server responds.
     * 
     * If the ListEntry does not exist in the URI list it will be created. If
     * the ListEntry already exists it will be overwritten.
     * 
     * @param listEntry
     *            - the ListEntry to add
     * 
     * @throws IllegalArgumentException
     *             - if the listEntry argument is null
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    void addListEntry(ListEntry listEntry) throws ServiceClosedException,
            XCAPException, IOException;

    /**
     * Returns the display name for this URIList.
     * 
     * @return the display name or null if the display name is not available
     */
    String getDisplayName();

    /**
     * Returns the ListEntry identified by the uri argument. Note: Changes made
     * to a ListEntry will not take effect on the URIList until addListEntry is
     * called.
     * 
     * @param uri
     *            - the URI that identifies the ListEntry
     * @return the ListEntry or null if the ListEntry does not exist
     */
    ListEntry getListEntry(String uri);

    /**
     * Returns all ListEntry objects in this URIList. Note: Changes made to a
     * ListEntry will not take effect on the URIList until addListEntry is
     * called.
     * 
     * @return an array of ListEntry or an empty array if no ListEntry are
     *         available
     */
    ListEntry[] getListEntries();

    /**
     * Returns the name of this URIList.
     * 
     * @return the list name
     */
    String getListName();

    /**
     * Removes the ListEntry identified by the uri argument from this URIList
     * and submits the changes to the document on the XDM server. This method is
     * synchronous and will block until the server responds.
     * 
     * @param uri
     *            - the URI that identifies the ListEntry
     * 
     * @throws IllegalArgumentException
     *             - if the ListEntry identified by the URI does not exist in
     *             this URIList
     * @throws ServiceClosedException
     *             - if the ListEntry identified by the URI does not exist in
     *             this URIList
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    void removeListEntry(String uri) throws ServiceClosedException,
            XCAPException, IOException;

    /**
     * Sets the display name for this URIList and submits the changes to the
     * document on the XDM server. This method is synchronous and will block
     * until the server responds.
     * 
     * The display name is a human-readable string that describes the URI list.
     * A null value removes any existing display name.
     * 
     * @param name
     *            - the display name or null
     * @throws ServiceClosedException
     *             - if the service is closed
     * @throws XCAPException
     *             - if the HTTP response from the XDM server has a status code
     *             other than 2xx Success
     * @throws IOException
     *             - if an I/O error occurs
     */
    void setDisplayName(String name) throws ServiceClosedException,
            XCAPException, IOException;
}
