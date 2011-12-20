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

package com.android.ims.presence;

import com.android.ims.util.CollectionsUtils;
import org.w3c.dom.Document;

import javax.microedition.ims.presence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default implementation {@link PresenceDocument}.
 * 
 * @author Andrei Khomushko
 * 
 */
public final class DefaultPresenceDocument implements PresenceDocument {
    // private static final String TAG = "DefaultPresenceDocument";
    // private static final String DEF_NAMESPACE =
    // "urn:ietf:params:xml:ns:resource-lists";

    private final AccessType accessType;
    private final Document document;

    private PersonInfo personInfo;

    private final List<DeviceInfo> devicesInfo = new ArrayList<DeviceInfo>();
    private final List<ServiceInfo> servicesInfo = new ArrayList<ServiceInfo>();
    private final List<DirectContent> directContents = new ArrayList<DirectContent>();

    public enum AccessType {
        READ, READ_WRITE
    }

    private DefaultPresenceDocument(final PresenceDocBuilder builder) {
        this.accessType = builder.accessType;
        this.document = builder.document;

        setPersonInfoInternally(builder.personInfo);

        for (DeviceInfo deviceInfo : builder.devicesInfo) {
            addDeviceInfoInternally(deviceInfo);
        }

        for (ServiceInfo serviceInfo : builder.servicesInfo) {
            addServiceInfoInternally(serviceInfo);
        }

        directContents.addAll(builder.directContents);
    }

    
    public void addDeviceInfo(final DeviceInfo newDeviceInfo) {
        if (newDeviceInfo == null) {
            throw new IllegalArgumentException(
                    "The deviceInfo argument is null");
        }

        if (accessType == AccessType.READ) {
            throw new IllegalStateException("The PresenceDocument is read only");
        }

        final DeviceInfo oldDeviceInfo = CollectionsUtils.find(devicesInfo,
                new CollectionsUtils.Predicate<DeviceInfo>() {
                    
                    public boolean evaluate(DeviceInfo deviceInfo) {
                        return deviceInfo.getIdentifier().equals(
                                newDeviceInfo.getIdentifier());
                    }
                });

        if (oldDeviceInfo == null) {
            addDeviceInfoInternally(newDeviceInfo);
        } else {
            replaceDeviceInfoInternally(newDeviceInfo, oldDeviceInfo);
        }
    }

    private void addDeviceInfoInternally(DeviceInfo deviceInfo) {
        devicesInfo.add(deviceInfo);
        deviceInfo.getAttachable().attach();
    }

    private void replaceDeviceInfoInternally(DeviceInfo deviceInfo,
            DeviceInfo oldDeviceInfo) {
        oldDeviceInfo.getAttachable().deattach();

        int idx = devicesInfo.indexOf(oldDeviceInfo);
        devicesInfo.set(idx, deviceInfo);

        deviceInfo.getAttachable().attach();
    }

    
    public void addDirectContent(final DirectContent directContent) {
        if (directContent == null) {
            throw new IllegalArgumentException(
                    "The directContent argument is null");
        }

        if (accessType == AccessType.READ) {
            throw new IllegalStateException("The PresenceDocument is read only");
        }

        final DirectContent contentToChange = CollectionsUtils.find(
                directContents,
                new CollectionsUtils.Predicate<DirectContent>() {
                    
                    public boolean evaluate(DirectContent content) {
                        return directContent.getCid().equals(content.getCid());
                    }
                });

        if (contentToChange == null) {
            addDirectContentInternally(directContent);
        } else {
            setDirectContentInternally(directContent, contentToChange);
        }
    }

    private void addDirectContentInternally(DirectContent directContent) {
        // directContent.attach();
        directContents.add(directContent);
    }

    private void setDirectContentInternally(DirectContent directContent,
            DirectContent directContentToRemove) {
        // directContentToRemove.deattach();

        int idx = directContents.indexOf(directContentToRemove);
        directContents.set(idx, directContent);

        // directContent.attach();
    }

    
    public void addServiceInfo(final ServiceInfo newServiceInfo) {
        if (newServiceInfo == null) {
            throw new IllegalArgumentException(
                    "The serviceInfo argument is null");
        }

        if (accessType == AccessType.READ) {
            throw new IllegalStateException("The PresenceDocument is read only");
        }

        final ServiceInfo serviceInfoToChange = CollectionsUtils.find(
                servicesInfo, new CollectionsUtils.Predicate<ServiceInfo>() {
                    
                    public boolean evaluate(ServiceInfo serviceInfo) {
                        return serviceInfo.getIdentifier().equals(
                                newServiceInfo.getIdentifier());
                    }
                });

        if (serviceInfoToChange == null) {
            addServiceInfoInternally(newServiceInfo);
        } else {
            setServiceInfoInternally(newServiceInfo, serviceInfoToChange);
        }
    }

    private void addServiceInfoInternally(final ServiceInfo serviceInfo) {
        servicesInfo.add(serviceInfo);

        serviceInfo.getAttachable().attach();
    }

    private void setServiceInfoInternally(final ServiceInfo serviceInfoToAdd,
            final ServiceInfo serviceInfoToRemove) {
        serviceInfoToRemove.getAttachable().deattach();

        int position = servicesInfo.indexOf(serviceInfoToRemove);
        servicesInfo.set(position, serviceInfoToAdd);

        serviceInfoToAdd.getAttachable().attach();
    }

    
    public Document getDOM() {
        return document;
    }

    
    public DeviceInfo[] getDeviceInfo() {
        return devicesInfo.toArray(new DeviceInfo[0]);
    }

    
    public DirectContent[] getDirectContent() {
        return directContents.toArray(new DirectContent[0]);
    }

    
    public PersonInfo getPersonInfo() {
        return personInfo;
    }

    
    public ServiceInfo[] getServiceInfo() {
        return servicesInfo.toArray(new ServiceInfo[0]);
    }

    
    public void removeDirectContent(final String cid) {
        if (cid == null) {
            throw new IllegalArgumentException("The cid argument is null");
        }

        if (accessType == AccessType.READ) {
            throw new IllegalStateException("The PresenceDocument is read only");
        }

        final DirectContent contentToRemove = CollectionsUtils.find(
                directContents,
                new CollectionsUtils.Predicate<DirectContent>() {
                    
                    public boolean evaluate(DirectContent content) {
                        return content.getCid().equals(cid);
                    }
                });

        if (contentToRemove != null) {
            removeDirectContentInternally(contentToRemove);
        } else {
            throw new IllegalArgumentException(
                    "The cid argument does not exist in the PresenceDocument");
        }
    }

    private void removeDirectContentInternally(DirectContent directContent) {
        // directContent.deattach();
        directContents.remove(directContent);
    }

    
    public void removeInfo(final String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException(
                    "The identifier argument is null");
        }

        if (accessType == AccessType.READ) {
            throw new IllegalStateException("The PresenceDocument is read only");
        }

        boolean deleted = false;

        if (personInfo != null && personInfo.getIdentifier().equals(identifier)) {
            deleted = removePersonInfoInternally();
        }

        final DeviceInfo deviceInfoToRemove = CollectionsUtils.find(
                devicesInfo, new CollectionsUtils.Predicate<DeviceInfo>() {
                    
                    public boolean evaluate(DeviceInfo deviceInfo) {
                        return deviceInfo.getIdentifier().equals(identifier);
                    }
                });

        if (deviceInfoToRemove != null) {
            deleted = removeDeviceInfoInternally(deviceInfoToRemove);
        }

        final ServiceInfo serviceInfoToRemove = CollectionsUtils.find(
                servicesInfo, new CollectionsUtils.Predicate<ServiceInfo>() {
                    
                    public boolean evaluate(ServiceInfo serviceInfo) {
                        return serviceInfo.getIdentifier().equals(identifier);
                    }
                });

        if (serviceInfoToRemove != null) {
            deleted = removeServiceInfoInternally(serviceInfoToRemove);
        }

        if (!deleted) {
            throw new IllegalArgumentException(
                    "The identifier argument does not exist in the PresenceDocument");
        }
    }

    private boolean removePersonInfoInternally() {
        personInfo.getAttachable().deattach();
        personInfo = null;
        return true;
    }

    private boolean removeServiceInfoInternally(ServiceInfo serviceInfoToRemove) {
        serviceInfoToRemove.getAttachable().deattach();
        return servicesInfo.remove(serviceInfoToRemove);
    }

    private boolean removeDeviceInfoInternally(DeviceInfo deviceInfoToRemove) {
        deviceInfoToRemove.getAttachable().deattach();
        return devicesInfo.remove(deviceInfoToRemove);
    }

    
    public void setPersonInfo(PersonInfo personInfo) {
        if (personInfo == null) {
            throw new IllegalArgumentException(
                    "The personInfo argument is null");
        }

        if (accessType == AccessType.READ) {
            throw new IllegalStateException("The PresenceDocument is read only");
        }

        setPersonInfoInternally(personInfo);
    }

    private void setPersonInfoInternally(PersonInfo newPersonInfo) {
        if (personInfo != null) {
            personInfo.getAttachable().deattach();
        }
        this.personInfo = newPersonInfo;
        if (newPersonInfo != null) {
            newPersonInfo.getAttachable().attach();
        }
    }

    public static class PresenceDocBuilder {
        private Document document;
        private AccessType accessType;
        private PersonInfo personInfo;

        private List<DeviceInfo> devicesInfo = new ArrayList<DeviceInfo>();
        private List<ServiceInfo> servicesInfo = new ArrayList<ServiceInfo>();
        private List<DirectContent> directContents = new ArrayList<DirectContent>();

        public PresenceDocBuilder buildDocument(Document document) {
            this.document = document;
            return this;
        }

        public PresenceDocBuilder buildAccessType(AccessType accessType) {
            this.accessType = accessType;
            return this;
        }

        public PresenceDocBuilder buildPersonalInfo(PersonInfo personInfo) {
            this.personInfo = personInfo;
            return this;
        }

        public PresenceDocBuilder buildDeviceInfo(
                Collection<DeviceInfo> devicesInfo) {
            this.devicesInfo.addAll(devicesInfo);
            return this;
        }
        
        public PresenceDocBuilder buildDeviceInfo(
                DeviceInfo deviceInfo) {
            this.devicesInfo.add(deviceInfo);
            return this;
        }


        public PresenceDocBuilder buildServicesInfo(
                Collection<ServiceInfo> servicesInfo) {
            this.servicesInfo.addAll(servicesInfo);
            return this;
        }

        public PresenceDocBuilder buildServicesInfo(
                ServiceInfo serviceInfo) {
            this.servicesInfo.add(serviceInfo);
            return this;
        }
        
        public PresenceDocBuilder buildDirectContents(
                Collection<DirectContent> directsContent) {
            this.directContents.addAll(directsContent);
            return this;
        }

        /**
         * Helper class for creating DefaultPresenceDocument.
         * 
         * @return - created object
         * 
         * @throws IllegalArgumentException
         *             - if pidfSource is null or empty
         * @throws IllegalArgumentException
         *             - if instance can't be instantiated
         */
        public DefaultPresenceDocument build() {

            if (accessType == null) {
                throw new IllegalArgumentException("The accesstype isn't set");
            }

            if (document == null) {
                throw new IllegalArgumentException(
                        "The document argument isn't set");
            }

            final DefaultPresenceDocument retValue;

            retValue = new DefaultPresenceDocument(this);

            return retValue;
        }
    }
    
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((devicesInfo == null) ? 0 : devicesInfo.hashCode());
        result = prime * result
                + ((directContents == null) ? 0 : directContents.hashCode());
        result = prime * result
                + ((personInfo == null) ? 0 : personInfo.hashCode());
        result = prime * result
                + ((servicesInfo == null) ? 0 : servicesInfo.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultPresenceDocument other = (DefaultPresenceDocument) obj;
        if (devicesInfo == null) {
            if (other.devicesInfo != null)
                return false;
        } else if (!devicesInfo.equals(other.devicesInfo))
            return false;
        if (directContents == null) {
            if (other.directContents != null)
                return false;
        } else if (!directContents.equals(other.directContents))
            return false;
        if (personInfo == null) {
            if (other.personInfo != null)
                return false;
        } else if (!personInfo.equals(other.personInfo))
            return false;
        if (servicesInfo == null) {
            if (other.servicesInfo != null)
                return false;
        } else if (!servicesInfo.equals(other.servicesInfo))
            return false;
        return true;
    }

    
    public String toString() {
        return "DefaultPresenceDocument [accessType=" + accessType
                + ", devicesInfo=" + devicesInfo + ", directContents="
                + directContents + ", personInfo=" + personInfo
                + ", servicesInfo=" + servicesInfo + "]";
    }
}
