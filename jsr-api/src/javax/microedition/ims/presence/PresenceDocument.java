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

package javax.microedition.ims.presence;

import org.w3c.dom.Document;

/**
 * The PresenceDocument can be used to publish the user's own presence
 * information or to access presence information from others.
 * 
 * </p><p>For detailed implementation guidelines and for complete API docs,
 * please refer to JSR-281 and JSR-235 documentation.
 * @author Andre Khomushko
 * 
 */
public interface PresenceDocument {
    /**
     * Returns the PersonInfo of this PresenceDocument . Changes made to the
     * PersonInfo object will not take effect on the PresenceDocument until
     * setPersonInfo(PersonInfo) is called.
     * 
     * Note: If multiple PersonInfo with the same identifier exist in the
     * PresenceDocument, only the PersonInfo with the latest time stamp will be
     * returned by this method.
     * 
     * @return the PersonInfo or null if a PersonInfo is not available
     */
    //<dm:person id="p1">
    PersonInfo getPersonInfo();

    /**
     * Returns all the ServiceInfo components of this PresenceDocument. Changes
     * made to any of the ServiceInfo objects will not take effect on the
     * PresenceDocument until addServiceInfo(ServiceInfo) is called.
     * 
     * Note: If multiple ServiceInfo with the same identifier exist in the
     * PresenceDocument, only the ServiceInfo with the latest time stamp will be
     * returned by this method.
     * 
     * @return an array of ServiceInfo or an empty array if no ServiceInfo is
     *         available
     */
    //<tuple id="sg89ae">
    ServiceInfo[] getServiceInfo();

    /**
     * Returns all the DeviceInfo components of this PresenceDocument. Changes
     * made to any of the DeviceInfo objects will not take effect on the
     * PresenceDocument until addDeviceInfo(DeviceInfo) is called.
     * 
     * Note: If multiple DeviceInfo with the same identifier exist in the
     * PresenceDocument, only the DeviceInfo with the latest time stamp will be
     * returned by this method.
     * 
     * @return an array of DeviceInfo or an empty array if no DeviceInfo is
     *         available
     */
    //<dm:device id="pc122">
    DeviceInfo[] getDeviceInfo();

    /**
     * Returns a Document Object Model (DOM) representation of this
     * PresenceDocument. If the PresenceDocument is read only it will still be
     * possible to modify the DOM.
     * 
     * @return a DOM structure of this PresenceDocument
     */
    Document getDOM();

    /**
     * Sets a PersonInfo for this PresenceDocument. This will replace any
     * existing PersonInfo.
     * 
     * @param personInfo
     *            - the PersonInfo to set
     * @throws IllegalStateException
     *             - if the PresenceDocument is read only
     * @throws IllegalArgumentException
     *             - if the personInfo argument is null
     */
    void setPersonInfo(PersonInfo personInfo);

    /**
     * Adds a ServiceInfo to this PresenceDocument. If this PresenceDocument
     * already contains a ServiceInfo with the same identifier, that ServiceInfo
     * will be updated.
     * 
     * @param serviceInfo
     *            - the ServiceInfo to add
     * @throws IllegalStateException
     *             - if the PresenceDocument is read only
     * @throws IllegalArgumentException
     *             - if the serviceInfo argument is null
     */
    void addServiceInfo(ServiceInfo serviceInfo);

    /**
     * Adds a DeviceInfo to this PresenceDocument. If this PresenceDocument
     * already contains a DeviceInfo with the same identifier, that DeviceInfo
     * will be updated.
     * 
     * @param deviceInfo
     *            - the DeviceInfo to add
     * @throws IllegalStateException
     *             - if the PresenceDocument is read only
     * @throws IllegalArgumentException
     *             - if the deviceInfo argument is null
     */
    void addDeviceInfo(DeviceInfo deviceInfo);

    /**
     * Removes the PersonInfo, ServiceInfo, or DeviceInfo with the given
     * identifier from this PresenceDocument.
     * 
     * @param identifier
     *            - the identifier of the component to remove
     * @throws IllegalArgumentException
     *             - if the identifier argument is null or if it does not exist
     *             in the PresenceDocument
     * @throws IllegalStateException
     *             - if the PresenceDocument is read only
     */
    void removeInfo(String identifier);

    /**
     * Adds a DirectContent to this PresenceDocument. If a DirectContent with
     * the same content identifier is already a part of this PresenceDocument,
     * it will be updated.
     * 
     * @param directContent
     *            - the content to add
     * @throws IllegalArgumentException
     *             - if the directContent argument is null
     * @throws IllegalStateException
     *             - if the PresenceDocument is read only
     */
    void addDirectContent(DirectContent directContent);

    /**
     * Removes the DirectContent with the specified content identifier.
     * 
     * @param cid
     *            - the content identifier of the DirectContent to remove
     * @throws IllegalArgumentException
     *             - if the cid argument is null or if it does not exist in the
     *             PresenceDocument
     * @throws IllegalStateException
     *             - if the PresenceDocument is read only
     */
    void removeDirectContent(String cid);

    /**
     * Returns all DirectContent of this PresenceDocument.
     * 
     * @return an array of DirectContent or an empty array if no DirectContent
     *         is available
     */
    DirectContent[] getDirectContent();
}
