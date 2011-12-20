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

import java.util.ArrayList;
import java.util.List;

/**
 * A WatcherFilter enables the possibility to limit the data received in the
 * presenceInfoReceived event of the WatcherListener interface. The filtering
 * mechanism is defined in [OMA_PRES_SPEC], chapter 5.2.5.
 * 
 * There are three available types of filters:
 * <ul>
 * <li>Generic - this creates a generic filter that applies to all presentities
 * of the Watcher.</li>
 * <li>URI-specific - this creates a URI-specific filter that only applies to
 * the presentity that is identified by the user identity.</li>
 * <li>Domain-specific - this creates a domain-specific filter that applies to
 * all presentities that can be identified by the domain.</li>
 * </ul>
 * 
 * If the filter is to be used in a watcher that subscribes to a single
 * presentity it is recommended that only one generic filter should be used. A
 * watcher that subscribes to a presence list can have any number of filters but
 * only one generic filter is recommended to ensure consistency.
 * 
 * An URI specific filter overrides a domain specific filter and both the URI
 * and domain filters override the generic filter, according to [RFC4660].
 * 
 * A WatcherFilter is created and added to a WatcherFilterSet. See
 * WatcherFilterSet for examples.
 * 
 * @author Andrei Khomushko
 * 
 */
public class WatcherFilter {

    /** Identifier for the XML element <device>. */
    public static final int COMPONENT_DEVICE = 3;

    /** Identifier for the XML element <person>. */
    public static final int COMPONENT_PERSON = 1;

    /** Identifier for the XML element <tuple>, which represents a service.. */
    public static final int COMPONENT_SERVICE = 2;

    public static final int FILTER_TYPE_DOMAIN = 3;
    public static final int FILTER_TYPE_GENERIC = 1;
    public static final int FILTER_TYPE_URI = 2;

    private final int filterType;
    private final String uri;
    private boolean isPersonInfoIncluded;
    private final List<String> includedServicesInfo = new ArrayList<String>();
    private final List<String> includedDevicesInfo = new ArrayList<String>();
    private boolean isOverridingWillingnessIncluded;
    private boolean isActivitiesincluded;
    private boolean isPlaceTypeIncluded;
    private boolean isTimeOffsetIncluded;
    private boolean isMoodsIncluded;
    private boolean isWillingnessIncluded;
    private boolean isPersonStatusIconIncluded;
    private boolean isServiceStatusIconIncluded;

    /**
     * Creates a new WatcherFilter. If the filterType argument is
     * FILTER_TYPE_GENERIC, a generic filter will be created. If the filter is
     * specific, meaning that the filterType argument is either
     * FILTER_TYPE_DOMAIN or FILTER_TYPE_URI, a domain-specific or a
     * URI-specific filter will be created respectively.
     * 
     * The filter will be empty and must be initialized by calling the include
     * methods.
     * 
     * @param filterType
     *            - the type of filter, one of FILTER_TYPE_URI,
     *            FILTER_TYPE_DOMAIN , or FILTER_TYPE_GENERIC
     * @param uri
     *            - the user identity for URI-specific filters, the domain for
     *            domain-specific filters, or null for generic filters
     * 
     * @throws IllegalArgumentException
     *             - if the filterType argument is not a valid filter type
     *             identifier
     * @throws IllegalArgumentException
     *             - if the filterType argument is FILTER_TYPE_URI and the uri
     *             argument is null or an invalid user identity
     * @throws IllegalArgumentException
     *             - if the filterType argument is FILTER_TYPE_DOMAIN and the
     *             uri argument is null or an empty string
     */
    public WatcherFilter(int filterType, String uri) {
        if (!isFilterTypeValid(filterType)) {
            throw new IllegalArgumentException(
                    "The filterType argument is not a valid filter type identifier");
        }

        if (filterType == FILTER_TYPE_URI && uri == null) {
            throw new IllegalArgumentException(
                    "The filterType argument is FILTER_TYPE_URI and the uri argument is null or an invalid user identity");
        }

        if (filterType == FILTER_TYPE_DOMAIN
                && (uri == null || "".equals(uri.trim()))) {
            throw new IllegalArgumentException(
                    "The filterType argument is FILTER_TYPE_DOMAIN and the uri argument is null or an empty string");
        }

        this.filterType = filterType;
        this.uri = uri;
    }

    private static boolean isFilterTypeValid(int filterType) {
        return filterType == FILTER_TYPE_GENERIC
                || filterType == FILTER_TYPE_DOMAIN
                || filterType == FILTER_TYPE_URI;
    }

    public int getFilterType() {
        return filterType;
    }

    /**
     * Returns the user identity for URI-specific filters, the domain for
     * domain-specific filters, or null for generic filters.
     * 
     * @return the user identity, domain, or null depending on the filter type
     */
    public String getURI() {
        return uri;
    }

    /**
     * Includes a rule to filter on the PersonInfo component, meaning that the
     * component will be included in the presence information.
     */
    public void includePersonInfo() {
        this.isPersonInfoIncluded = true;
    }

    /**
     * Indicates whether the PersonInfo component is included in this
     * WatcherFilter.
     * 
     * @return true if the PersonInfo component is included, false otherwise
     */
    public boolean isPersonInfoIncluded() {
        return isPersonInfoIncluded;
    }

    /**
     * Includes a rule to filter on the ServiceInfo component, meaning that the
     * component will be included in the presence information.
     * 
     * @param serviceId
     *            - the serviceId of a specific ServiceInfo to filter
     * 
     * @throws IllegalArgumentException
     *             - if the serviceId argument is null
     */
    public void includeServiceInfo(String serviceId) {
        if (serviceId == null) {
            throw new IllegalArgumentException("The serviceId argument is null");
        }

        includedServicesInfo.add(serviceId);
    }

    /**
     * Returns the serviceIds of the ServiceInfo components that are included in
     * this WatcherFilter.
     * 
     * @return an array of serviceIds or an empty array if no ServiceInfo
     *         components are included
     */
    public String[] getIncludedServiceInfo() {
        return includedServicesInfo.toArray(new String[0]);
    }

    /**
     * Includes a rule to filter on the DeviceInfo component, meaning that the
     * component will be included in the presence information.
     * 
     * @param deviceId
     *            - the deviceId of a specific DeviceInfo to filter
     * 
     * @throws IllegalArgumentException
     *             - if the deviceId argument is null
     */
    public void includeDeviceInfo(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("The deviceId argument is null");
        }

        includedDevicesInfo.add(deviceId);
    }

    /**
     * Returns the deviceIds of the DeviceInfo components that are included in
     * this WatcherFilter.
     * 
     * @return an array of deviceIds or an empty array if no DeviceInfo
     *         components are included
     */
    public String[] getIncludedDeviceInfo() {
        return includedDevicesInfo.toArray(new String[0]);
    }

    /**
     * Include a rule to filter on the overriding-willingness element, meaning
     * that the element will be included in the presence information.
     */
    public void includeOverridingWillingness() {
        this.isOverridingWillingnessIncluded = true;
    }

    /**
     * Indicates whether the overriding-willingness element is included in this
     * WatcherFilter.
     * 
     * @return true if the overriding-willingness element is included, false
     *         otherwise
     */
    public boolean isOverridingWillingnessIncluded() {
        return isOverridingWillingnessIncluded;
    }

    /**
     * Include a rule to filter on the activities element, meaning that the
     * element will be included in the presence information.
     */
    public void includeActivities() {
        this.isActivitiesincluded = true;
    }

    /**
     * Indicates whether the activities element is included in this
     * WatcherFilter.
     * 
     * @return true if the activities element is included, false otherwise
     */
    public boolean isActivitiesIncluded() {
        return isActivitiesincluded;
    }

    /**
     * Include a rule to filter on the place-type element, meaning that the
     * element will be included in the presence information.
     */
    public void includePlaceType() {
        this.isPlaceTypeIncluded = true;
    }

    /**
     * Indicates whether the place-type element is included in this
     * WatcherFilter.
     * 
     * @return true if the place-type element is included, false otherwise
     */
    public boolean isPlaceTypeIncluded() {
        return isPlaceTypeIncluded;
    }

    /**
     * Include a rule to filter on the time-offset element, meaning that the
     * element will be included in the presence information.
     */
    public void includeTimeOffset() {
        this.isTimeOffsetIncluded = true;
    }

    /**
     * Indicates whether the time-offset element is included in this
     * WatcherFilter.
     * 
     * @return true if the time-offset element is included, false otherwise
     */
    public boolean isTimeOffsetIncluded() {
        return isTimeOffsetIncluded;
    }

    /**
     * Include a rule to filter on the mood element, meaning that the element
     * will be included in the presence information.
     */
    public void includeMoods() {
        this.isMoodsIncluded = true;
    }

    /**
     * Indicates whether the mood element is included in this WatcherFilter.
     * 
     * @return true if the mood element is included, false otherwise
     */
    public boolean isMoodsIncluded() {
        return isMoodsIncluded;
    }

    /**
     * Include a rule to filter on the willingness element, meaning that the
     * element will be included in the presence information.
     */
    public void includeWillingness() {
        this.isWillingnessIncluded = true;
    }

    /**
     * Indicates whether the willingness element is included in this
     * WatcherFilter.
     * 
     * @return true if the willingness element is included, false otherwise
     */
    public boolean isWillingnessIncluded() {
        return isWillingnessIncluded;
    }

    /**
     * Iclude a rule to filter on the status-icon element, meaning that the
     * element will be included in the presence information.
     * 
     * @param component
     *            - COMPONENT_PERSON or COMPONENT_SERVICE
     * 
     * @throw IllegalArgumentException - if the component argument is not a
     *        valid component identifier
     */
    public void includeStatusIcon(int component) {
        if(!(component == COMPONENT_PERSON || component == COMPONENT_SERVICE)) {
            throw new IllegalArgumentException(
                    "The component argument is not a valid component identifier, component = "
                            + component);
        }
        
        if (component == COMPONENT_PERSON) {
            this.isPersonStatusIconIncluded = true;
        } else if (component == COMPONENT_SERVICE) {
            this.isServiceStatusIconIncluded = true;
        }
    }

    /**
     * Indicates whether the status-icon element is included in this
     * WatcherFilter for a specific component.
     * 
     * @param component - COMPONENT_PERSON or COMPONENT_SERVICE
     * @return true if the status-icon element is included for the component, false otherwise
     * @throws IllegalArgumentException - if the component argument is not a valid component identifier
     */
    public boolean isStatusIconIncluded(int component) {
        if(!(component == COMPONENT_PERSON || component == COMPONENT_SERVICE)) {
            throw new IllegalArgumentException(
                    "The component argument is not a valid component identifier, component = "
                            + component);
        }
        
        boolean isStatusIconIncluded = false;
        
        if (component == COMPONENT_PERSON) {
            isStatusIconIncluded = isPersonStatusIconIncluded;
        } else if (component == COMPONENT_SERVICE) {
            isStatusIconIncluded = isServiceStatusIconIncluded;
        } 
        
        return isStatusIconIncluded;
    }
    
    
}
