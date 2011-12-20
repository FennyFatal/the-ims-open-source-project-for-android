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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The PresenceContentFilter class holds the presence content filter for a
 * PresenceAuthorizationRule [OMA_PRES_XDM]. A presence content filter defines
 * what information a watcher has access to in a specific presence document,
 * given that the watcher has been allowed to access the presence document.
 * 
 * @author Andrei Khomushko
 * 
 */
public class PresenceContentFilter {
    public static final int COMPONENT_DEVICES = 2;
    public static final int COMPONENT_PERMISSION_CLASSIFICATION = 18;
    public static final int COMPONENT_PERMISSION_DEVICE_ID = 19;
    public static final int COMPONENT_PERMISSION_SERVICE_ID = 22;
    public static final int COMPONENT_PERMISSION_SERVICE_URI = 20;
    public static final int COMPONENT_PERMISSION_SERVICE_URI_SCHEME = 21;
    public static final int COMPONENT_PERSONS = 1;
    public static final int COMPONENT_SERVICES = 3;
    public static final int ELEMENT_ACTIVITIES = 9;
    public static final int ELEMENT_BARRING_STATE = 16;
    public static final int ELEMENT_CLASSIFICATION = 10;
    public static final int ELEMENT_FREE_TEXT = 17;
    public static final int ELEMENT_GEOGRAPHICAL_LOCATION_INFO = 5;
    public static final int ELEMENT_MOOD = 11;
    public static final int ELEMENT_NETWORK_AVAILABILITY = 7;
    public static final int ELEMENT_PLACE_TYPES = 12;
    public static final int ELEMENT_REGISTRATION_STATE = 15;
    public static final int ELEMENT_SESSION_PARTICIPATION = 8;
    public static final int ELEMENT_STATUS_ICON = 13;
    public static final int ELEMENT_TIME_OFFSET = 14;
    public static final int ELEMENT_WILLINGNESS = 6;


    private List<UnknownElement> unknownElements = new ArrayList<UnknownElement>();
    
    private boolean allElementsProvided = false;
    private Map<Integer, Boolean> providedElements = new HashMap<Integer, Boolean>();
    
    private boolean allPersonComponentsProvided = false;
    private List<String> providedPersons = new ArrayList<String>();
    
    private boolean allDeviceComponentsProvided = false;
    private Map<Integer, List<String>> provideDevices = new HashMap<Integer, List<String>>();
    
    private boolean allServiceComponentsProvided = false;
    private Map<Integer, List<String>> providedServices = new HashMap<Integer, List<String>>();

    
    public PresenceContentFilter() {
    }

    /**
     * Returns all provided devices previously set by the provideDeviceComponent
     * method.
     * 
     * @param element
     *            - the element identifier
     * @return an array of devices identified by classification or device ID,
     *         depending on the value of the element argument, or an empty array
     *         if there are no permissions set for device components based on
     *         the value of the element argument
     * @throws IllegalArgumentException
     *             - if the element argument is not
     *             COMPONENT_PERMISSION_CLASSIFICATION or
     *             COMPONENT_PERMISSION_DEVICE_ID
     */
    public String[] getProvidedDevices(int element) {
        if (!isProvidedDeviceValid(element)) {
            throw new IllegalArgumentException(
                    "the element argument is not COMPONENT_PERMISSION_CLASSIFICATION or COMPONENT_PERMISSION_DEVICE_ID");
        }

        List<String> devices = provideDevices.get(element);
        return devices == null ? new String[0] : devices.toArray(new String[0]);
    }

    private boolean isProvidedDeviceValid(int element) {
        return element == COMPONENT_PERMISSION_CLASSIFICATION
                || element == COMPONENT_PERMISSION_DEVICE_ID;
    }

    /**
     * Provides access to a device component in a presence document, given a
     * certain value of the classification or device ID element.
     * 
     * @param element
     *            - the element identifier
     * @param value
     *            - the value of the element to match in the device component(s)
     * @throws IllegalArgumentException
     *             - if the element argument is not
     *             COMPONENT_PERMISSION_CLASSIFICATION or
     *             COMPONENT_PERMISSION_DEVICE_ID
     * @throws IllegalArgumentException
     *             - if the value argument is null
     */
    public void provideDeviceComponent(int element, String value) {
        if (value == null) {
            throw new IllegalArgumentException("value argument is null");
        }

        if (!isProvidedDeviceValid(element)) {
            throw new IllegalArgumentException(
                    "the element argument is not COMPONENT_PERMISSION_CLASSIFICATION or COMPONENT_PERMISSION_DEVICE_ID");
        }

        List<String> values = provideDevices.get(element);
        if (values == null) {
            values = new ArrayList<String>();
        }
        values.add(value);
        provideDevices.put(element, values);
    }

    /**
     * Returns all provided persons previously set by the providePersonComponent
     * method.
     * 
     * @return an array of person classifications or an empty array if there are
     *         no permissions set for person components based on classification.
     */
    public String[] getProvidedPersons() {
        return providedPersons.toArray(new String[0]);
    }

    /**
     * Provides access to a person component in a presence document, given a
     * certain value of the classification element.
     * 
     * @param value
     *            - he value of the classification to match in the person
     *            component(s)
     * @throws IllegalArgumentException
     *             - if the value argument is null
     */
    public void providePersonComponent(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value argument is null");
        }
        providedPersons.add(value);
    }

    /**
     * Returns all provided services previously set by the
     * provideServiceComponent method.
     * 
     * @param element
     *            - the element identifier
     * @return an array of services identified by classification, service ID,
     *         URI, or URI scheme, depending on the value of the element
     *         argument, or an empty array if there are no permissions set for
     *         service components based on the value of the element argument
     * @throws IllegalArgumentException
     *             - if the element argument is not
     *             COMPONENT_PERMISSION_CLASSIFICATION,
     *             COMPONENT_PERMISSION_SERVICE_ID,
     *             COMPONENT_PERMISSION_SERVICE_URI, or
     *             COMPONENT_PERMISSION_SERVICE_URI_SCHEME
     */
    public String[] getProvidedServices(int element) {
        if (!isProvidedServiceValid(element)) {
            throw new IllegalArgumentException("element argument is not valid");
        }
        
        List<String> services = providedServices.get(element);
        return services == null ? new String[0] : services.toArray(new String[0]);
    }

    private boolean isProvidedServiceValid(int element) {
        return element == COMPONENT_PERMISSION_CLASSIFICATION
            || element == COMPONENT_PERMISSION_SERVICE_ID
            || element == COMPONENT_PERMISSION_SERVICE_URI
            || element == COMPONENT_PERMISSION_SERVICE_URI_SCHEME;
    }

    /**
     * Provides access to a service component in a presence document, given a
     * certain value of the classification, service URI, or service URI scheme
     * element.
     * 
     * @param element
     *            - the element identifier
     * @param value
     *            - the value of the element to match in the service
     *            components(s)
     * @throws IllegalArgumentException
     *             - if the element argument is not
     *             COMPONENT_PERMISSION_CLASSIFICATION
     *             COMPONENT_PERMISSION_SERVICE_ID,
     *             COMPONENT_PERMISSION_SERVICE_URI, or
     *             COMPONENT_PERMISSION_SERVICE_URI_SCHEME
     * @throws IllegalArgumentException
     *             - if the value argument is null
     */
    public void provideServiceComponent(int element, String value) {
        if (value == null) {
            throw new IllegalArgumentException("value argument is null");
        }

        if (!isProvidedServiceValid(element)) {
            throw new IllegalArgumentException(
                    "the element argument is not valid");
        }
        
        List<String> values = providedServices.get(element);
        if (values == null) {
            values = new ArrayList<String>();
        }
        values.add(value);
        providedServices.put(element, values);
    }

    /**
     * Returns all unknown elements in this PresenceContentFilter.
     * 
     * @return an array of UnknownElement or an empty array if no unknown
     *         elements are available
     */
    public UnknownElement[] getUnknownElements() {
        return unknownElements.toArray(new UnknownElement[0]);
    }

    /**
     * Returns the setting made by the last call to the provideAllElements
     * method. If the method has not been called, the default value of false
     * will be returned.
     * 
     * @return true if access for all elements is provided, false otherwise
     */
    public boolean isAllElementsProvided() {
        return allElementsProvided;
    }

    /**
     * Returns the permission status of all person components.
     * 
     * @return true if access for all person components is provided, false
     *         otherwise
     */
    public boolean isAllPersonComponentsProvided() {
        return allPersonComponentsProvided;
    }

    /**
     * Returns the permission status of all service components.
     * 
     * @return true if access for all service components is provided, false
     *         otherwise
     */
    public boolean isAllServiceComponentsProvided() {
        return allServiceComponentsProvided;
    }
    
    /**
     * Returns the permission status of all device components.
     * 
     * @return true if access for all device components is provided, false
     *         otherwise
     */
    public boolean isAllDeviceComponentsProvided() {
        return allDeviceComponentsProvided;
    }

    /**
     * Returns the permission status of the specified element. If
     * isAllElementsProvided returns true, this method will always return true.
     * 
     * @param element
     *            - the element identifier
     * @return true if access for the element is provided, false otherwise
     * @throws IllegalArgumentException
     *             - if the element argument is not one of the ELEMENT_
     *             identifiers
     */
    public boolean isElementProvided(int element) {
        if (!isElementValid(element)) {
            throw new IllegalArgumentException(
                    "the element argument is not one of the ELEMENT_ identifiers");
        }
        
        if (isAllElementsProvided()) {
            return true;
        }
        
        if (providedElements.containsKey(element)) {
            return providedElements.get(element);
        }

        return false;
    }

    private boolean isElementValid(int element) {
        return element == ELEMENT_ACTIVITIES
            || element == ELEMENT_BARRING_STATE
            || element == ELEMENT_CLASSIFICATION
            || element == ELEMENT_FREE_TEXT
            || element == ELEMENT_GEOGRAPHICAL_LOCATION_INFO
            || element == ELEMENT_MOOD
            || element == ELEMENT_NETWORK_AVAILABILITY
            || element == ELEMENT_PLACE_TYPES
            || element == ELEMENT_REGISTRATION_STATE
            || element == ELEMENT_SESSION_PARTICIPATION
            || element == ELEMENT_STATUS_ICON
            || element == ELEMENT_TIME_OFFSET
            || element == ELEMENT_WILLINGNESS;
    }

    /**
     * Provides permissions for all presence components of a certain type.
     * 
     * @param component
     *            - the component identifier representing which component to
     *            change setting for
     * @param value
     *            - true provides access to all components, false restores
     *            previous settings set with providePersonComponent,
     *            provideServiceComponent, or provideDeviceComponent.
     * @throws IllegalArgumentException
     *             - if the component argument is not COMPONENT_PERSONS,
     *             COMPONENT_DEVICES, or COMPONENT_SERVICES
     */
    public void provideAllComponents(int component, boolean value) {
        if (component != COMPONENT_PERSONS && component != COMPONENT_DEVICES && component != COMPONENT_SERVICES) {
            throw new IllegalArgumentException(
                    "the component argument is not COMPONENT_PERSONS, COMPONENT_DEVICES, or COMPONENT_SERVICES");
        }
        
        if (component == COMPONENT_PERSONS) {
            allPersonComponentsProvided = value;
        } else if (component == COMPONENT_DEVICES) {
            allDeviceComponentsProvided = value;
        } else if (component == COMPONENT_SERVICES) {
            allServiceComponentsProvided = value;
        }
    }

    /**
     * Provides permissions for all elements. If set to true the presence
     * content filter will allow access to all elements in the presence
     * document, if the components in which the elements resides are visible to
     * the watcher.
     * 
     * If set to true, all calls to isElementProvided will return true. If set
     * to false, all calls to isElementProvided will behave according to the
     * previous settings made by the provideElement method for the specific
     * element.
     * 
     * @param value
     *            - true provides access to all elements, false restores
     *            previous settings
     */
    public void provideAllElements(boolean value) {
        this.allElementsProvided = value;
    }

    /**
     * Provides permissions for an element. If set to true the presence content
     * filter will allow access to the element in the presence document, if the
     * components in which the element resides are visible to the watcher. All
     * element permissions are set to false by default.
     * 
     * @param element
     *            - the element identifier
     * @param value
     *            - true grants access to the element, false removes access
     * @throws IllegalArgumentException
     *             - if the element argument is not one of the ELEMENT_
     *             identifiers
     */
    public void provideElement(int element, boolean value) {
        if (!isElementValid(element)) {
            throw new IllegalArgumentException(
                    "the element argument is not one of the ELEMENT_ identifiers");
        }
        
        providedElements.put(element, value);
    }

    /**
     * Provides permissions for an unknown element not defined by
     * [OMA_PRES_DDS]. If an unknown element with the same name and namespace
     * already exists in this PresenceContentFilter, it will be replaced by the
     * one defined by this method.
     * 
     * @param unknownElement
     *            - an UnknownElement
     * @throws IllegalArgumentException
     *             - if the unknownElement argument is null
     */
    public void provideUnknownElement(UnknownElement unknownElement) {
        if (unknownElement == null) {
            throw new IllegalArgumentException(
                    "the unknownElement argument is null");
        }
        
        unknownElements.add(unknownElement);
    }
    
}
