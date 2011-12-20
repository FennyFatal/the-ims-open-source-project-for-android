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

import android.text.TextUtils;
import android.util.Xml;
import com.android.ims.util.Utils;
import com.android.ims.util.XMLUtils;
import org.xmlpull.v1.XmlSerializer;

import javax.microedition.ims.presence.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import static com.android.ims.presence.PresenceDocumentShemas.*;

/**
 * Utility class for serialization PresenceDocument to xml.
 * 
 * @author Andrei Khomushko
 * 
 */
public final class PresenceDocumentWriter {
    private static final String FEATURES_INDENT_OUTPUT = "http://xmlpull.org/v1/doc/features.html#indent-output";

    private final String userIdentity;
    private final PresenceDocument document;

    public PresenceDocumentWriter(final String userIdentity,
            final PresenceDocument document) {
        if (userIdentity == null) {
            throw new IllegalArgumentException(
                    "The userIdentity argument is null");
        }

        if (document == null) {
            throw new IllegalArgumentException("The document argument is null");
        }

        this.userIdentity = userIdentity;
        this.document = document;
    }

    private void writePersonInfo(final XmlSerializer serializer)
            throws IOException {
        /*
         * <pdm:person id=\"p1\"> <rpid:status-icon>%s</rpid:status-icon>
         * <rpid:note>%s</rpid:note>"
         * 
         * <rpid:activities from="2005-05-30T12:00:00+05:00"
         * until="2005-05-30T17:00:00+05:00"> <rpid:note>Far away</rpid:note>
         * <rpid:away/> </rpid:activities> </pdm:person>
         */

        final PersonInfo personInfo = document.getPersonInfo();

        if (personInfo != null) {

            String identifier = personInfo.getIdentifier();
            if (identifier == null) {
                throw new IllegalArgumentException("Can't retrieve id for person info");
            }

            serializer.startTag(NAMESPACE_DATA_MODEL, "person");
            serializer.attribute("", "id", identifier);

            String statusIcon = (personInfo.getStatusIcon() == null ? null
                    : personInfo.getStatusIcon().getUri());
            if (!TextUtils.isEmpty(statusIcon)) {
                serializer.startTag(NAMESPACE_RPID, "status-icon");
                serializer.text(statusIcon);
                serializer.endTag(NAMESPACE_RPID, "status-icon");
            }
            
            
            if (personInfo.getOverridingWillingness() != null && personInfo.getOverridingWillingness() != PresenceState.UNSET) {
                serializer.startTag(NAMESPACE_OMA_PRES, "overriding-willingness");
                serializer.startTag(NAMESPACE_OMA_PRES, "basic");
                serializer.text(personInfo.getOverridingWillingness().toString());
                serializer.endTag(NAMESPACE_OMA_PRES, "basic");
                serializer.endTag(NAMESPACE_OMA_PRES, "overriding-willingness");
            }
            

            //activities
            final String[] activities = personInfo.getActivities();
            
            serializer.startTag(NAMESPACE_RPID, "activities");
                
            for(String activity: activities) {
                if(ActivitiesValues.isPredefined(activity)) {
                    serializer.startTag(NAMESPACE_RPID, activity);
                    serializer.endTag(NAMESPACE_RPID, activity);
                } else {
                    serializer.startTag(NAMESPACE_RPID, "other");
                    serializer.text(activity);
                    serializer.endTag(NAMESPACE_RPID, "other");
                }
            }
                
            serializer.endTag(NAMESPACE_RPID, "activities");
            
            
            //mood
            String[] moods = personInfo.getMoods();
            
            serializer.startTag(NAMESPACE_RPID, "mood");
            
            for(String mood: moods) {
                if(MoodsValues.isPredefined(mood)) {
                    serializer.startTag(NAMESPACE_RPID, mood);
                    serializer.endTag(NAMESPACE_RPID, mood);
                } else {
                    serializer.startTag(NAMESPACE_RPID, "other");
                    serializer.text(mood);
                    serializer.endTag(NAMESPACE_RPID, "other");
                }
            }
            
            serializer.endTag(NAMESPACE_RPID, "mood");
            
            
            serializer.startTag(NAMESPACE_RPID, "user-input");
            serializer.text("active");
            serializer.endTag(NAMESPACE_RPID, "user-input");
            

            String freeText = personInfo.getFreeText();
            if (!TextUtils.isEmpty(freeText)) {
                serializer.startTag(NAMESPACE_DATA_MODEL, "note");
                serializer.text(freeText);
                serializer.endTag(NAMESPACE_DATA_MODEL, "note");
            }
            
            GeographicalLocationInfo locationInfo = personInfo.getGeographicalLocationInfo();
            if(locationInfo != null) {
                writeGeographicalLocationInfo(locationInfo, serializer);    
            }

            serializer.endTag(NAMESPACE_DATA_MODEL, "person");
        }
    }

    private void writeDevicesInfo(final XmlSerializer serializer)
            throws IOException {
        for (DeviceInfo deviceInfo : document.getDeviceInfo()) {
            writeDeviceInfo(deviceInfo, serializer);
        }
    }

    private void writeDeviceInfo(final DeviceInfo deviceInfo,
            final XmlSerializer serializer) throws IOException {
        /*
         * "<device id=\"#deviceId\" xmlns="urn:ietf:params:xml:ns:pidf:data-model" + 
         *    "<deviceID>Mobile</pdm:deviceID>" + 
         *    "<note>Mobile</pdm:note>" +
         * "</device>"
         */
        
        //serializer.setPrefix("", NAMESPACE_DATA_MODEL);
        
        String identifier = deviceInfo.getIdentifier();
        if (identifier == null) {
            throw new IllegalArgumentException("Can't retrieve id for device info");
        }
        serializer.startTag(NAMESPACE_DATA_MODEL, "device");
        serializer.attribute("", "id", identifier);

        String deviceId = deviceInfo.getDeviceId();
        if (!TextUtils.isEmpty(deviceId)) {
            serializer.startTag(NAMESPACE_DATA_MODEL, "deviceID");
            serializer.text(deviceId);
            serializer.endTag(NAMESPACE_DATA_MODEL, "deviceID");
        }

        String freeText = deviceInfo.getFreeText();
        if (!TextUtils.isEmpty(freeText)) {
            serializer.startTag(NAMESPACE_DATA_MODEL, "note");
            serializer.text(freeText);
            serializer.endTag(NAMESPACE_DATA_MODEL, "note");
        }
        
        GeographicalLocationInfo locationInfo = deviceInfo.getGeographicalLocationInfo();
        if(locationInfo != null) {
            writeGeographicalLocationInfo(locationInfo, serializer);    
        }

        serializer.endTag(NAMESPACE_DATA_MODEL, "device");
    }

    private void writeServicesInfo(final XmlSerializer serializer) 
            throws IOException{
        for (ServiceInfo serviceInfo : document.getServiceInfo()) {
            writeServiceInfo(serviceInfo, serializer);
        }
    }
    
    private void writeGeographicalLocationInfo(GeographicalLocationInfo locationInfo,
            final XmlSerializer serializer) throws IOException{
/*        <?xml version="1.0" encoding="UTF-8"?>
        <presence xmlns="urn:ietf:params:xml:ns:pidf"
           xmlns:gp="urn:ietf:params:xml:ns:pidf:geopriv10"
           xmlns:gml="urn:opengis:specification:gml:schema-xsd:feature:v3.0"
           entity="pres:geotarget@example.com">
         <tuple id="sg89ae">
          <status>
          
           <gp:geopriv>
             <gp:location-info>
                <gml:location>
                 <gml:Point gml:id="point1" srsName="epsg:4326">
                   <gml:coordinates>37:46:30N 122:25:10W</gml:coordinates>
                 </gml:Point>
                </gml:location>
                
                <cl:civicAddress>
                  <cl:country>US</cl:country>
                  <cl:A1>New York</cl:A1>
                  <cl:A3>New York</cl:A3>
                  <cl:A6>Broadway</cl:A6>
                  <cl:HNO>123</cl:HNO>
                  <cl:LOC>Suite 75</cl:LOC>
                  <cl:PC>10027-0401</cl:PC>
                </cl:civicAddress>
             </gp:location-info>
             <gp:usage-rules>
               <gp:retransmission-allowed>no</gp:retransmission-allowed>
               <gp:retention-expiry>2003-06-23T04:57:29Z</gp:retention-expiry>
             </gp:usage-rules>
           </gp:geopriv>
           
          </status>
          ...
         </tuple>
        </presence>*/
        
        serializer.startTag(NAMESPACE_GP, "geopriv");
        
        //<gp:location-info> handling
        Point point = locationInfo.getPoint();
        Circle circle = locationInfo.getCircle();
        boolean civicAddressesEmpty = locationInfo.isCivicAddressesEmpty();
        
        if(point != null || circle != null || !civicAddressesEmpty) {
            serializer.startTag(NAMESPACE_GP, "location-info");
            
            if(point != null || circle != null) {
                
                //<gml:location> handling
                
                serializer.startTag(NAMESPACE_GML, "location");
                
                if(point != null) {
                    writeGeographicalPoint(serializer, point);
                }
                
                if(circle != null) {
                    writeGeographicalCircle(serializer, circle);    
                }
                
                //</gml:location> handling
                serializer.endTag(NAMESPACE_GML, "location");
            }
            
            if(!civicAddressesEmpty) {
/*             <cl:civicAddress>
                <cl:country>US</cl:country>
                <cl:A1>New York</cl:A1>
                <cl:A3>New York</cl:A3>
                <cl:A6>Broadway</cl:A6>
                <cl:HNO>123</cl:HNO>
                <cl:LOC>Suite 75</cl:LOC>
                <cl:PC>10027-0401</cl:PC>
              </cl:civicAddress>
*/
                serializer.startTag(NAMESPACE_CL, "civicAddress");
                
                for(CivicAddressType civicAddressType: CivicAddressType.values()) {
                    //civicAddressType
                    String civicAddress = locationInfo.getCivicAddress(civicAddressType.getCode());
                    if(civicAddress != null) {
                        String civicTag = civicAddressType.getTag();
                        serializer.startTag(NAMESPACE_CL, civicTag);
                        serializer.text(civicAddress);
                        serializer.endTag(NAMESPACE_CL, civicTag);
                    }
                }
                
                serializer.endTag(NAMESPACE_CL, "civicAddress");
            }
            
            
            serializer.endTag(NAMESPACE_GP, "location-info");
        }
        
/*        <gp:usage-rules>
            <gp:retransmission-allowed>no</gp:retransmission-allowed>
            <gp:retention-expiry>2003-06-23T04:57:29Z</gp:retention-expiry>
          </gp:usage-rules>
*/
        serializer.startTag(NAMESPACE_GP, "usage-rules");
        
        serializer.startTag(NAMESPACE_GP, "retransmission-allowed");
        serializer.text(locationInfo.isRetransmissionAllowed()? "yes": "no");
        serializer.endTag(NAMESPACE_GP, "retransmission-allowed");
        
        Date retentionExpires = locationInfo.getRetentionExpires();
        if(retentionExpires != null) {
            serializer.startTag(NAMESPACE_GP, "retention-expiry");
            String inetRetentionExpires = Utils.convertJavaTimeToInetTimeFormat(retentionExpires);
            serializer.text(inetRetentionExpires);
            serializer.endTag(NAMESPACE_GP, "retention-expiry");
        }
        
        serializer.endTag(NAMESPACE_GP, "usage-rules");
        
        //<gp:method>Wiremap</gp:method>
        String method = locationInfo.getMethod();
        if(method != null) {
            serializer.startTag(NAMESPACE_GP, "method");
            serializer.text(method);
            serializer.endTag(NAMESPACE_GP, "method");
        }
        
        serializer.endTag(NAMESPACE_GP, "geopriv");
        
    }

    private void writeGeographicalCircle(final XmlSerializer serializer, Circle circle) throws IOException {
        /* xmlns:gs="http://www.opengis.net/pidflo/1.0"
        <gs:Circle srsName="urn:ogc:def:crs:EPSG::4326">
          <gml:pos>42.5463 -73.2512</gml:pos>
          <gs:radius uom="urn:ogc:def:uom:EPSG::9001">
            850.24
          </gs:radius>
        </gs:Circle>
         */
        
        serializer.setPrefix(NAMESPACE_GS_PREFIX, NAMESPACE_GS);
        
        String srsName = circle.getSrsName();
        String coordinates = circle.getCoordinates();
        String radius = circle.getRadius();
        
        serializer.startTag(NAMESPACE_GS, "Circle");
        serializer.attribute("", "srsName", srsName);
        
        serializer.startTag(NAMESPACE_GML, "pos");
        serializer.text(coordinates);
        serializer.endTag(NAMESPACE_GML, "pos");
        
        serializer.startTag(NAMESPACE_GS, "radius");
        serializer.attribute("", "uom", "urn:ogc:def:uom:EPSG::9001");
        serializer.text(radius);
        serializer.endTag(NAMESPACE_GS, "radius");
        
        serializer.endTag(NAMESPACE_GS, "Circle");
    }

    private void writeGeographicalPoint(final XmlSerializer serializer, Point point) throws IOException {
        /*
        <gml:Point gml:id="point1" srsName="epsg:4326">
          <gml:coordinates>37:46:30N 122:25:10W</gml:coordinates>
        </gml:Point>
         */
            
        String gmlId = point.getGmlId();
        String srsName = point.getSrsName();
        String coordinates = point.getCoordinates();
            
        serializer.startTag(NAMESPACE_GML, "Point");
            
        serializer.attribute(NAMESPACE_GML, "id", gmlId);
        serializer.attribute("", "srsName", srsName);
            
        serializer.startTag(NAMESPACE_GML, "coordinates");
        serializer.text(coordinates);
        serializer.endTag(NAMESPACE_GML, "coordinates");
            
        serializer.endTag(NAMESPACE_GML, "Point");
    }

    private void writeServiceInfo(final ServiceInfo serviceInfo,
            final XmlSerializer serializer) throws IOException{
        /*
         * "<tuple id=\"e750389c-4b8a-4dd5-8e01-c26869a168c1\">" +
         * "<status><basic>open</basic></status>" +
         * "<note xmlns=\"urn:ietf:params:xml:ns:pidf:rpid\">#serviceNote</note>"
         * "<service-description xmlns=\"urn:oma:xml:prs:pidf:oma-pres\">" +
         * "  <service-id>presence</service-id>" 
         *    "<version>1.0</version>" +
         * "</service-description>" +
         * "<deviceID xmlns=\"urn:ietf:params:xml:ns:pidf:data-model\">Mobile</deviceID>"
         * "<contact priority=\"1.0\">#presenceEntity</contact>" "</tuple>"
         */
        
        String identifier = serviceInfo.getIdentifier();
        if (identifier == null) {
            throw new IllegalArgumentException("Can't retrieve id for service info");
        }
        
        serializer.startTag(NAMESPACE_PIDF, "tuple");
        serializer.attribute("", "id", identifier);
        
        
        PresenceState status = serviceInfo.getStatus();
        if(status != null && status != PresenceState.UNSET) {
            serializer.startTag(NAMESPACE_PIDF, "status");
            serializer.startTag(NAMESPACE_PIDF, "basic");
            serializer.text(status.toString());
            serializer.endTag(NAMESPACE_PIDF, "basic");
            serializer.endTag(NAMESPACE_PIDF, "status");
        }
        
        
        String serviceDescription = serviceInfo.getServiceDescription();
        String serviceId = serviceInfo.getServiceId();
        String serviceVersion = serviceInfo.getServiceVersion();
        
        if(!TextUtils.isEmpty(serviceDescription) || !TextUtils.isEmpty(serviceId) 
                || !TextUtils.isEmpty(serviceVersion)) {
            //serializer.setPrefix("", NAMESPACE_OMA_PRES);
            serializer.startTag(NAMESPACE_OMA_PRES, "service-description");
            
            if(!TextUtils.isEmpty(serviceId)) {
                serializer.startTag(NAMESPACE_OMA_PRES, "service-id");
                serializer.text(serviceId);
                serializer.endTag(NAMESPACE_OMA_PRES, "service-id");
            }
            
            if(!TextUtils.isEmpty(serviceVersion)) {
                serializer.startTag(NAMESPACE_OMA_PRES, "version");
                serializer.text(serviceVersion);
                serializer.endTag(NAMESPACE_OMA_PRES, "version");
            }
            
            if(!TextUtils.isEmpty(serviceDescription)) {
                serializer.startTag(NAMESPACE_OMA_PRES, "description");
                serializer.text(serviceDescription);
                serializer.endTag(NAMESPACE_OMA_PRES, "description");
            }
            
            serializer.endTag(NAMESPACE_OMA_PRES, "service-description");
        }
        
        String deviceId = serviceInfo.getDeviceId();
        if(!TextUtils.isEmpty(deviceId)) {
            //serializer.setPrefix("", NAMESPACE_DATA_MODEL);
            serializer.startTag(NAMESPACE_DATA_MODEL, "deviceID");
            serializer.text(deviceId);
            serializer.endTag(NAMESPACE_DATA_MODEL, "deviceID");
        }
        
        String contact = serviceInfo.getContact();
        if(!TextUtils.isEmpty(contact)) {
            serializer.startTag(NAMESPACE_PIDF, "contact");
            serializer.text(contact);
            serializer.endTag(NAMESPACE_PIDF, "contact");
        }
        
        
        String freeText = serviceInfo.getFreeText();
        if(!TextUtils.isEmpty(freeText)) {
//            serializer.setPrefix("", NAMESPACE_RPID);
            serializer.startTag(NAMESPACE_DATA_MODEL, "note");
            serializer.text(freeText);
            serializer.endTag(NAMESPACE_DATA_MODEL, "note");
        }
        
        
        serializer.endTag(NAMESPACE_PIDF, "tuple");
    }

    private void writeDirectContents(final XmlSerializer serializer) {
        for (DirectContent directContent : document.getDirectContent()) {
            writeDirectContent(directContent, serializer);
        }
    }

    private void writeDirectContent(final DirectContent content,
            final XmlSerializer serializer) {
        // TODO not yet implemented
    }

    public String toXML() throws IOException {
        /*
         * "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" "
            + "xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\" "
            + "xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\" "
            + "entity=\"#presenceEntity\">"
            + ...
            +</presence>
         */
        
        final String retValue;

        final XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        serializer.setOutput(writer);

        serializer.startDocument("utf-8", null);
        serializer.setFeature(FEATURES_INDENT_OUTPUT, true);

        serializer.setPrefix("", NAMESPACE_PIDF);
        serializer.setPrefix(NAMESPACE_DATA_MODEL_PREFIX, NAMESPACE_DATA_MODEL);
        serializer.setPrefix(NAMESPACE_RPID_PREFIX, NAMESPACE_RPID);
        
        serializer.setPrefix(NAMESPACE_OMA_PRES_PREFIX, NAMESPACE_OMA_PRES);
        serializer.setPrefix("c", "urn:ietf:params:xml:ns:pidf:cipid");//R
        serializer.setPrefix("caps", "urn:ietf:params:xml:ns:pidf:caps");//R
        
        serializer.setPrefix(NAMESPACE_GML_PREFIX, NAMESPACE_GML);
        serializer.setPrefix(NAMESPACE_GP_PREFIX, NAMESPACE_GP);
        serializer.setPrefix(NAMESPACE_CL_PREFIX, NAMESPACE_CL);

        serializer.startTag(NAMESPACE_PIDF, "presence");
        serializer.attribute("", "entity", userIdentity);

        writeServicesInfo(serializer);
        writePersonInfo(serializer);
        writeDevicesInfo(serializer);
        writeDirectContents(serializer);

        serializer.endTag(NAMESPACE_PIDF, "presence");

        serializer.endDocument();

        retValue = XMLUtils.unescape(writer.toString());

        return retValue;

    }
}
