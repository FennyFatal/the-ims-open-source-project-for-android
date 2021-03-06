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

package javax.microedition.ims.core.media;

/**
 * <code>MediaDescriptor</code> is an interface towards the media parts of the
 * SDP.
 *
 *
 *
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation
 *
 * @see javax.microedition.ims.core.SessionDescriptor
 */

public interface MediaDescriptor {
    /**
     * Adds an attribute (a=) to the Media.
     * <p/>
     * Adding attributes that the IMS engine has reserved, such as
     * "sendonly", "recvonly", "sendrecv", leads to IllegalArgumentException.
     * <p/>
     * Syntax:
     * a=<attribute>
     * a=<attribute>:<value>
     * <p/>
     * The example below adds two attributes for a BasicReliableMediaImpl.
     * MediaDescriptor[] desc = basicReliableMedia.getMediaDescriptors();
     * desc[0].addAttribute("max-size:2048000");
     * desc[0].addAttribute("synced");
     * <p/>
     * The resulting attribute lines will be:
     * a=max-size:2048000
     * a=synced
     * <p/>
     * Note: If the Media is in STATE_ACTIVE the attribute will be set
     * on the proposal media until the Session has been updated.
     * The proposal media can be retrieved with the getProposal method
     * on the Media interface.
     *
     * @param attribute - the attribute to add
     * @throws IllegalArgumentException - if the attribute argument is null or if the syntax of the attribute argument is invalid
     *                                  IllegalArgumentException - if the attribute already exist in the Media
     *                                  IllegalArgumentException - if the attribute could not be added
     *                                  IllegalStateException - if the Media is not in STATE_INACTIVE or STATE_ACTIVE
     */
    void addAttribute(String attribute);

    /**
     * Returns all attributes (a=) for the Media.
     * If there are no attributes, an empty string array will be returned.
     *
     * @return a string array containing the attributes
     */
    String[] getAttributes();

    /**
     * Returns the proposed bandwidth (b=) to be used by the media.
     *
     * @return bandwidth information
     */
    String[] getBandwidthInfo();

    /**
     * Returns the contents of the media description field (m=) of the current server SDP for this media.
     * <p/>
     * Example of an SDP m-line that may be returned for an audio stream media:
     * audio 4000 RTP/AVP 97 101
     *
     * @return the media description
     * @throw IllegalStateException - if the Media is in STATE_INACTIVE
     */
    String getMediaDescription();

    /**
     * Returns the title (i=) of the Media.
     * See [RFC4566], chapter 5.4. for more information.
     *
     * @return the Media title or null if the title has not been set
     */
    String getMediaTitle();

    /**
     * Removes an attribute (a=) from the Media.
     * <p/>
     * Note: If the Media is in STATE_ACTIVE the attribute
     * will be removed on the proposal media until the Session has been updated.
     * The proposal media can be retrieved with the getProposal method on the Media interface.
     *
     * @param attribute - the attribute to remove
     * @throws IllegalArgumentException - if the attribute argument is null
     *                                  IllegalArgumentException - if the attribute does not exist in the Media
     *                                  IllegalArgumentException - if the attribute could not be removed
     *                                  IllegalStateException   - if the Media is not in STATE_INACTIVE or STATE_ACTIVE
     */
    void removeAttribute(String attribute);

    /**
     * Sets the proposed bandwidth (b=) to be used by the media.
     * <p/>
     * b=<modifier>:<bandwidth-value>
     * <modifier> is a single alphanumeric word giving the meaning of the bandwidth figure.
     * <bandwidth-value> - is in kilobits per second
     * <p/>
     * Example:
     * MediaDescriptor desc;
     * desc.setBandwidthInfo(new String[]{ "AS:128" });
     *
     * @param info - the bandwidth info to set
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE or STATE_ACTIVE
     *                               IllegalArgumentException - if the info argument is null or if the syntax is invalid
     */
    void setBandwidthInfo(String[] info);

    /**
     * Sets a title (i=) to the Media.
     *
     * @param title - the Media title to set
     * @throws: IllegalStateException - if the Media is not in STATE_INACTIVE
     * IllegalArgumentException - if the title argument is null
     */
	void setMediaTitle(String title);
    
}
