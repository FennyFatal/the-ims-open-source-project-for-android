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

import android.util.Log;
import com.android.ims.presence.DefaultPresenceDocument.AccessType;
import com.android.ims.presence.DefaultPresenceDocument.PresenceDocBuilder;
import com.android.ims.util.Utils;
import com.android.ims.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.microedition.ims.presence.*;
import javax.microedition.ims.presence.DeviceInfo.DeviceInfoBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.android.ims.presence.PresenceDocumentShemas.*;

/**
 * This class is responsible for parsing presence document.
 *
 * @author Andrei Khomushko
 */
final class PresenceDocumentParser extends BaseXMLParser {
    //private static final String NAMESPACE_GML_PREFIX = "gml";

    //TODO: for debugging
    private static final String TEST_NOTIFY = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<pr:presence xmlns:pr=\"urn:ietf:params:xml:ns:pidf\" entity=\"sip:79258450001@multifon.ru\">\n" +
            "    <pr:tuple id=\"a068c587786b2\">\n" +
            "      <pr:status>\n" +
            "        <pr:basic>open</pr:basic>\n" +
            "      </pr:status>\n" +
            "      <pdm:note xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\">1234567890</pdm:note>\n" +
            "      <op:service-description xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\">\n" +
            "        <op:service-id>NeuStar.IM</op:service-id>\n" +
            "        <op:version>1.000</op:version>\n" +
            "      </op:service-description>\n" +
            "      <pdm:deviceID xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\">urn:uuid:d7295e62-e9af-4aa5-afa5-a93f6e8d844c</pdm:deviceID>\n" +
            "      <pr:timestamp>2010-07-29T13:38:37Z</pr:timestamp>\n" +
            "    </pr:tuple>\n" +
            "    <pr:tuple id=\"a46a836a45093\">\n" +
            "      <pr:status>\n" +
            "        <pr:basic>open</pr:basic>\n" +
            "      </pr:status>\n" +
            "      <op:service-description xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\">\n" +
            "        <op:service-id>NeuStar.IM</op:service-id>\n" +
            "        <op:version>1.000</op:version>\n" +
            "      </op:service-description>\n" +
            "      <pdm:deviceID xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\">urn:uuid:b6345ed7-129e-4af4-bae4-3fdb7e63e5ee</pdm:deviceID>\n" +
            "      <pr:note/>\n" +
            "      <pr:timestamp>2010-09-05T13:18:42Z</pr:timestamp>\n" +
            "    </pr:tuple>\n" +
            "    <pdm:person xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\" id=\"a29c60f57b54f\">\n" +
            "      <rpid:user-input xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\">active</rpid:user-input>\n" +
            "      <ci:display-name xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"/>\n" +
            "      <ci:homepage xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"/>\n" +
            "      <ci:icon xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\">https://im.srv.com/content/presence/sip:79258450001@multifon.ru/avatar/my_avatar.jpg</ci:icon>\n" +
            "      <op:overriding-willingness xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\">\n" +
            "        <op:basic>open</op:basic>\n" +
            "      </op:overriding-willingness>\n" +
            "      <rpid:activities xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\">\n" +
            "        <rpid:unknown/>\n" +
            "      </rpid:activities>\n" +
            "      <pdm:timestamp>2010-07-29T13:38:37Z</pdm:timestamp>\n" +
            "    </pdm:person>\n" +
            "    <pdm:person xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\" id=\"adbaca7f5bc91\">\n" +
            "      <rpid:user-input xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\">active</rpid:user-input>\n" +
            "      <ci:display-name xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"/>\n" +
            "      <ci:homepage xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"/>\n" +
            "      <ci:icon xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\">https://im.srv.com/content/presence/sip:79258450001@multifon.ru/avatar/my_avatar.jpg</ci:icon>\n" +
            "      <op:overriding-willingness xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\">\n" +
            "        <op:basic>open</op:basic>\n" +
            "      </op:overriding-willingness>\n" +
            "      <rpid:activities xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\">\n" +
            "        <rpid:do-not-disturb/>\n" +
            "      </rpid:activities>\n" +
            "      <pdm:note/>\n" +
            "      <pdm:timestamp>2010-09-05T13:18:42Z</pdm:timestamp>\n" +
            "    </pdm:person>\n" +
            "    <pdm:device xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\" id=\"abe02b4fea2d5\">\n" +
            "      <pdm:deviceID>d7295e62-e9af-4aa5-afa5-a93f6e8d844c</pdm:deviceID>\n" +
            "      <pdm:note>7777777777777777777755555555555555555555333333333333333333331111111111111111111199999999999999999999</pdm:note>\n" +
            "      <pdm:timestamp>2010-07-29T13:38:37Z</pdm:timestamp>\n" +
            "    </pdm:device>\n" +
            "    <pdm:device xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\" id=\"a713d1d54fac9\">\n" +
            "      <pr:note>D???D?????D?D?eD?D?</pr:note>\n" +
            "      <pdm:deviceID>b6345ed7-129e-4af4-bae4-3fdb7e63e5ee</pdm:deviceID>\n" +
            "      <pdm:timestamp>2010-09-05T13:18:42Z</pdm:timestamp>\n" +
            "    </pdm:device>\n" +
            "</pr:presence>";

    private static final String TAG = "PresenceDocumentParser";
    private static final String TIMESTAMP_TAG = "timestamp";

    private final Document document;

    /**
     * @param document
     * @throws IllegalArgumentException - if the document argument is null
     */
    PresenceDocumentParser(Document document) {
        if (document == null) {
            throw new IllegalArgumentException(
                    "The document argument is null");
        }
        this.document = document;
    }

    /**
     * @param pidfSource
     * @throws IOException              - if instance can't be instantiated
     * @throws IllegalArgumentException - if the pidfSource argument is null
     */
    PresenceDocumentParser(String pidfSource) throws IOException {
        if (pidfSource == null|| "".equals(pidfSource.trim())) {
            throw new IllegalArgumentException(
                    "The pidfSource argument is null");
        }

        this.document = XMLUtils.createDocument(pidfSource, true);
    }

    private PersonInfo parsePersonInfo(Element presenceNode) throws IOException {
        /*
         * "<dm:person id=\"p1\">" + "<rpid:status-icon>%s</rpid:status-icon>" +
         * "<rpid:note>%s</rpid:note>" + "</dm:person>"
         */
        final PersonInfo retValue;

        //Node latestPersonNode = XMLUtils.obtainLatestNode(document, "/presence/person", TIMESTAMP_TAG);
        NodeList personNodeList = presenceNode.getElementsByTagNameNS(NAMESPACE_DATA_MODEL, "person");

        PersonInfo personInfo = null;

        if (personNodeList.getLength() > 0) {
            final Element latestPersonNode = (Element)personNodeList.item(personNodeList.getLength() - 1); 
            
            final String id, note, classification;
            final StatusIcon statusIcon;
            ArrayList<String> moods = new ArrayList<String>();
            ArrayList<String> activities = new ArrayList<String>();
            ArrayList<String> placetypes = new ArrayList<String>();
            int timeOffset = PersonInfo.TIME_OFFSET_UNSET;
            PresenceState overridingWillingness = PresenceState.UNSET;
            Date timestamp = null;
            final GeographicalLocationInfo locationInfo;

            //try {
                //id = (String) applyXPAth(latestPersonNode, "@id",
                //        XPathConstants.STRING);
                id = latestPersonNode.getAttribute("id");
                
                //statusIcon = (String) applyXPAth(latestPersonNode, "status-icon",
                //        XPathConstants.STRING);
                NodeList statusIconNodeList = latestPersonNode.getElementsByTagNameNS(NAMESPACE_RPID, "status-icon");
                String statusIconUrl = extractTextContent(statusIconNodeList); 
                statusIcon = statusIconUrl != null? new StatusIcon(statusIconUrl, null, 0, null, null): null;
                
                //note = (String) applyXPAth(latestPersonNode, "note",
                //        XPathConstants.STRING);
                NodeList noteNodeList = latestPersonNode.getElementsByTagNameNS(NAMESPACE_DATA_MODEL, "note");
                String noteContent = extractTextContent(noteNodeList);
                if(noteContent == null) {
                    noteNodeList = latestPersonNode.getElementsByTagNameNS(NAMESPACE_RPID, "note");
                    noteContent = extractTextContent(noteNodeList);
                }
                note = noteContent;

                //parsing <mood> tag
                parsePersonInfoMoodNode(latestPersonNode, moods);

                //parsing <activities> tag
                parsePersonInfoActivitiesNode(latestPersonNode, activities);

                //parsing <place-type> tag
                parsePersonInfoPlaceTypeNode(latestPersonNode, placetypes);

                //parsing <class> tag
                //classification = (String) applyXPAth(latestPersonNode, "class",
                //        XPathConstants.STRING);
                NodeList classNodeList = latestPersonNode.getElementsByTagNameNS(NAMESPACE_RPID, "class");
                classification = extractTextContent(classNodeList);

                //parsing <time-offset> tag
                //String timeOffsetStr = (String) applyXPAth(latestPersonNode, "time-offset",
                //        XPathConstants.STRING);
                NodeList timeOffsetNodeList = latestPersonNode.getElementsByTagNameNS(NAMESPACE_RPID, "time-offset");
                if(timeOffsetNodeList.getLength() > 0) {
                    String timeOffsetStr = timeOffsetNodeList.item(0).getTextContent();
                    if (timeOffsetStr != null && timeOffsetStr.length() > 0) {
                        timeOffset = Integer.valueOf(timeOffsetStr);
                    }
                }

                //parsing <overriding-willingness><basic>open</basic></overriding-willingness>
                //String overridingWillingnessStr = (String) applyXPAth(latestPersonNode, "overriding-willingness/basic",
                //        XPathConstants.STRING);
                NodeList willingnessNodeList = latestPersonNode.getElementsByTagNameNS(NAMESPACE_OMA_PRES, "overriding-willingness");
                Element willingnessNode = willingnessNodeList.getLength() > 0? (Element)willingnessNodeList.item(0): null;
                if(willingnessNode != null) {
                    NodeList basicNodeList = willingnessNode.getElementsByTagNameNS(NAMESPACE_OMA_PRES, "basic");
                    String overridingWillingnessStr = extractTextContent(basicNodeList);
                    if (overridingWillingnessStr != null && overridingWillingnessStr.length() > 0) {
                        overridingWillingness = PresenceState.valueOf(overridingWillingnessStr);
                    }
                }

                //parsing <timestamp> tag
                //String timestampStr = XMLUtils.obtainTimeStamp(latestPersonNode, TIMESTAMP_TAG);
                NodeList timestampNodeList = latestPersonNode.getElementsByTagNameNS(NAMESPACE_DATA_MODEL, TIMESTAMP_TAG);
                String timestampStr = extractTextContent(timestampNodeList);
                if (timestampStr != null && timestampStr.length() > 0) {
//                    try {
//                        timestamp = new Date(timestampStr);
//                    } catch (IllegalArgumentException e) {
//                    }

                    if (timestamp == null) {
                        // parse "2010-07-29T09:41:32Z"
                        timestamp = Utils.convertInetTimeFormatToJavaTime(timestampStr);
                    }
                }

                //Node geoprivNode = (Node) applyXPAth(latestPersonNode, "geopriv",
                //        XPathConstants.NODE);
                NodeList geoprivNodeList = latestPersonNode.getElementsByTagNameNS(NAMESPACE_GP, "geopriv");
                locationInfo = geoprivNodeList.getLength() > 0? parseGeoprivNode((Element)geoprivNodeList.item(0)): null;
            //}
            //catch (XPathExpressionException e) {
            //    throw new IOException(e.getMessage());
            //}

            try {
                personInfo = new PersonInfo.PersonInfoBuilder()
                        .buildId(id)
                        .buildStatusIcon(statusIcon)
                        .buildNote(note)
                        .buildMoods(moods)
                        .buildActivities(activities)
                        .buildPlacetypes(placetypes)
                        .buildClassification(classification)
                        .buildTimeOffset(timeOffset)
                        .buildOverridingWillingness(overridingWillingness)
                        .buildTimestamp(timestamp)
                        .buildGeographicalLocationInfo(locationInfo)
                        .build();
            }
            catch (IllegalArgumentException e) {
                throw new IOException(e.getMessage());
            }
        }

        retValue = personInfo;

        return retValue;
    }

    private String extractTextContent(NodeList nodeList) {
        return nodeList.getLength() > 0? nodeList.item(0).getTextContent(): null;
    }

    private void parsePersonInfoPlaceTypeNode(final Element personNode,
                                              ArrayList<String> placetypes) /*throws XPathExpressionException*/ {
        //Node placetypeNode = (Node) applyXPAth(personNode, "place-type",
        //        XPathConstants.NODE);
        NodeList placeTypeNodeList = personNode.getElementsByTagNameNS(NAMESPACE_RPID, "place-type");
        
        if (placeTypeNodeList.getLength() > 0) {
            Node placetypeNode = placeTypeNodeList.item(0);
            
            NodeList placetypeNodeChildList = placetypeNode.getChildNodes();
            if (placetypeNodeChildList != null) {
                for (int i = 0; i < placetypeNodeChildList.getLength(); i++) {
                    Node placetypeItemNode = placetypeNodeChildList.item(i);
                    if(placetypeItemNode.getNodeType() == Node.ELEMENT_NODE) {
                        String placetypeName = placetypeItemNode.getLocalName();
                        placetypes.add(placetypeName);
                    }
                }
            }
        }
    }

    private void parsePersonInfoActivitiesNode(final Element personNode,
                                               ArrayList<String> activities) /*throws XPathExpressionException */{
        //Node activitiesNode = (Node) applyXPAth(personNode, "activities",
        //        XPathConstants.NODE);
        NodeList activitiesNodeList = personNode.getElementsByTagNameNS(NAMESPACE_RPID, "activities");
        
        if (activitiesNodeList.getLength() > 0) {
            final Node activitiesNode = activitiesNodeList.item(0);
            
            NodeList activitiesNodeChildList = activitiesNode.getChildNodes();
            //if (activitiesNodeChildList != null) {
                for (int i = 0; i < activitiesNodeChildList.getLength(); i++) {
                    Node activityItemNode = activitiesNodeChildList.item(i);
                    if(activityItemNode.getNodeType() == Node.ELEMENT_NODE) {
                        String activityName = activityItemNode.getLocalName();
                        activities.add(activityName);
                    }
                }
            //}
        }
    }

    private void parsePersonInfoMoodNode(final Element personNode,
                                         ArrayList<String> moods) /*throws XPathExpressionException*/ {
        //Node moodNode = (Node) applyXPAth(personNode, "mood",
        //        XPathConstants.NODE);
        NodeList moodNodeList = personNode.getElementsByTagNameNS(NAMESPACE_RPID, "mood");
        if (moodNodeList.getLength() > 0) {
            final Node moodNode = moodNodeList.item(0);
            
            NodeList moodNodeChildList = moodNode.getChildNodes();
            //if (moodNodeChildList != null) {
                for (int i = 0; i < moodNodeChildList.getLength(); i++) {
                    Node moodItemNode = moodNodeChildList.item(i);
                    if(moodItemNode.getNodeType() == Node.ELEMENT_NODE) {
                        String nodeName = moodItemNode.getLocalName();
                        moods.add(nodeName);
                    } 
                }
            //}
        }
    }

    private Collection<DeviceInfo> parseDevicesInfo(Element presenceNode)
            throws IOException {
        final List<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();

/*        final NodeList tupleNodes;
        try {
            tupleNodes = (NodeList) applyXPAth(document, "/presence/device",
                    XPathConstants.NODESET);
        }
        catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }
*/        
        if(presenceNode.getNodeType() == Node.ELEMENT_NODE) {
            NodeList tupleNodes = presenceNode.getElementsByTagNameNS(NAMESPACE_DATA_MODEL, "device");
            
            for (int i = 0, count = tupleNodes.getLength(); i < count; i++) {
                Element node = (Element)tupleNodes.item(i);
                DeviceInfo deviceInfo = parseDeviceInfo(node);
                deviceInfos.add(deviceInfo);
            }
        }

        return deviceInfos;
    }

    private DeviceInfo parseDeviceInfo(Element deviceNode) throws IOException {
        final DeviceInfo retValue;

        
        /*
         * "<device id=\"#deviceId\" xmlns="urn:ietf:params:xml:ns:pidf:data-model" + 
         *    "<deviceID>Mobile</dm:deviceID>" + 
         *    "<note>Mobile</dm:note>" +
         * "</device>"
         */
        final String id, deviceID, note;
        final GeographicalLocationInfo locationInfo;

        //try {
            //id = (String) applyXPAth(deviceNode, "@id", XPathConstants.STRING);
            //id = deviceNode.getAttributes().getNamedItem("id").getNodeValue();
            id = deviceNode.getAttribute("id");
            
            //deviceID = (String) applyXPAth(deviceNode, "deviceID",
            //        XPathConstants.STRING);
            NodeList deviceIdNode = deviceNode.getElementsByTagNameNS(NAMESPACE_DATA_MODEL, "deviceID");
            assert deviceIdNode.getLength() == 1; 
            deviceID = deviceIdNode.item(0).getTextContent();
                
            //note = (String) applyXPAth(deviceNode, "note",
            //        XPathConstants.STRING);
            NodeList deviceNoteNode = deviceNode.getElementsByTagNameNS(NAMESPACE_DATA_MODEL, "note");
            note = deviceNoteNode.getLength() > 0? deviceNoteNode.item(0).getTextContent(): null;    
             

            //Node geoprivNode = (Node) applyXPAth(deviceNode, "geopriv",
            //        XPathConstants.NODE);
            NodeList geoprivNode = deviceNode.getElementsByTagNameNS(NAMESPACE_GP, "geopriv");
            locationInfo = geoprivNode.getLength() > 0? parseGeoprivNode((Element)geoprivNode.item(0)): null;
        /*}
        catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }*/
        
        DeviceInfoBuilder deviceInfoBuilder = new DeviceInfoBuilder()
            .buildId(id)
            .buildDeveiceId(deviceID)
            .buildFreeText(note)
            .buildGeographicalLocationInfo(locationInfo)
                // TODO note?(FreeText was added to DeviceInfo)
        ;

        final DeviceInfo deviceInfo;
        try {
            deviceInfo = deviceInfoBuilder.build();
        }
        catch (IllegalArgumentException e) {
            throw new IOException("Can't create DeviceInfo: " + e.getMessage());
        }

        retValue = deviceInfo;

        return retValue;
    }

    private Collection<ServiceInfo> parseServicesInfo(Element presenceNode)
            throws IOException {
        final List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();

/*        final NodeList servicesNodes;
        try {
            servicesNodes = (NodeList) applyXPAth(document, "/presence/tuple",
                    XPathConstants.NODESET);
        }
        catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }
*/        
        NodeList servicesNodes = presenceNode.getElementsByTagNameNS("*", "tuple");

        for (int i = 0, count = servicesNodes.getLength(); i < count; i++) {
            Element node = (Element)servicesNodes.item(i);
            ServiceInfo serviceInfo = parseServiceInfo(node);
            serviceInfos.add(serviceInfo);
        }

        return serviceInfos;
    }

    private ServiceInfo parseServiceInfo(Element serviceNode) throws IOException {
        final ServiceInfo retValue;

        /*
         * "<tuple id=\"e750389c-4b8a-4dd5-8e01-c26869a168c1\">" +
         *   "<status><basic>open</basic></status>" +
         *   "<note xmlns=\"urn:ietf:params:xml:ns:pidf:rpid\">#serviceNote</note>"
         * "<service-description xmlns=\"urn:oma:xml:prs:pidf:oma-pres\">" +
         * "<service-id>presence</service-id>" "<version>1.0</version>" +
         * "</service-description>" +
         * "<deviceID xmlns=\"urn:ietf:params:xml:ns:pidf:data-model\">Mobile</deviceID>"
         * "<contact priority=\"1.0\">#presenceEntity</contact>" "</tuple>"
         */
        final String id, note;
        String statusBasic = null;

        //id = (String) applyXPAth(serviceNode, "@id", XPathConstants.STRING);
        id = serviceNode.getAttribute("id");
        
/*        try {
            statusBasic = (String) applyXPAth(serviceNode, "status/basic",
                    XPathConstants.STRING);
        }
        catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }
*/      
        NodeList statusNodeList = serviceNode.getElementsByTagNameNS("*", "status");
        Element statusNode = statusNodeList.getLength() > 0? (Element)statusNodeList.item(0): null;
        if(statusNode != null) {
            NodeList basicNodeList = statusNode.getElementsByTagNameNS("*", "basic");
            statusBasic = extractTextContent(basicNodeList);
        }
        
        //note = (String) applyXPAth(serviceNode, "note",
        //        XPathConstants.STRING);
        NodeList noteNodeList = serviceNode.getElementsByTagNameNS(NAMESPACE_DATA_MODEL, "note");
        note = extractTextContent(noteNodeList);

/*        final Node serviceDescNode;
        try {
            serviceDescNode = (Node) applyXPAth(serviceNode,
                    "service-description", XPathConstants.NODE);
        }
        catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }
*/
        NodeList serviceDescriptionNodeList = serviceNode.getElementsByTagNameNS(NAMESPACE_OMA_PRES, "service-description");
        final Element serviceDescNode = serviceDescriptionNodeList.getLength() > 0? (Element)serviceDescriptionNodeList.item(0): null; 

        // description
        String serviceDescId = null, serviceDescVersion = null, serviceDescDescription = null;
        if (serviceDescNode != null) {
            //throw new IOException("ServiceDesc node is not found");

/*            try {
*/                //serviceDescId = (String) applyXPAth(serviceDescNode, "service-id",
                //        XPathConstants.STRING);
                 NodeList serviceIdNodeList = serviceDescNode.getElementsByTagNameNS(NAMESPACE_OMA_PRES, "service-id");
                 serviceDescId = extractTextContent(serviceIdNodeList);
                
                //serviceDescVersion = (String) applyXPAth(serviceDescNode,
                //        "version", XPathConstants.STRING);
                NodeList versionNodeList = serviceDescNode.getElementsByTagNameNS(NAMESPACE_OMA_PRES, "version");
                serviceDescVersion = extractTextContent(versionNodeList); 
                
                //serviceDescDescription = (String) applyXPAth(serviceDescNode,
                //        "description", XPathConstants.STRING);
                NodeList descriptionNodeList = serviceDescNode.getElementsByTagNameNS(NAMESPACE_OMA_PRES, "description");
                serviceDescDescription = extractTextContent(descriptionNodeList); 

/*            }
            catch (XPathExpressionException e) {
                throw new IOException(e.getMessage());
            }
*/        }

        final String deviceId, contact;
        //try {
            //deviceId = (String) applyXPAth(serviceNode, "deviceID",
            //        XPathConstants.STRING);
            
            NodeList deviceIdNodeList = serviceNode.getElementsByTagNameNS(NAMESPACE_DATA_MODEL, "deviceID");
            deviceId = extractTextContent(deviceIdNodeList); 
            
            //contact = (String) applyXPAth(serviceNode, "contact",
            //        XPathConstants.STRING);
            NodeList contactNodeList = serviceNode.getElementsByTagNameNS("*", "contact");
            contact = extractTextContent(contactNodeList); 
/*        }
        catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }
*/
        final ServiceInfo serviceInfo;
        try {
            serviceInfo = new ServiceInfo.ServiceInfoBuilder().buildId(id)
                    .buildStatus(PresenceState.valueOf(statusBasic))
                    .buildFreeText(note).buildServiceId(serviceDescId)
                    .buildServiceVersion(serviceDescVersion)
                    .buildServiceDescription(serviceDescDescription)
                    .buildDeviceId(deviceId).buildContact(contact).build();
        }
        catch (IllegalArgumentException e) {
            throw new IOException("ServiceInfoBuilder#" + e.getMessage());
        }

        retValue = serviceInfo;

        return retValue;
    }

    private Collection<DirectContent> parseDirectContents()
            throws IOException {
        final List<DirectContent> directContents = new ArrayList<DirectContent>();
        // TODO not implemented yet
        return directContents;
    }
    
    private GeographicalLocationInfo parseGeoprivNode(final Element geoprivNode) /*throws XPathExpressionException */{
        final GeographicalLocationInfo retValue = new GeographicalLocationInfo();
        
/*        <gp:geopriv xmlns:gp="urn:ietf:params:xml:ns:pidf:geopriv10">
            <gp:location-info>
                <gml:location>
                    <gml:Point gml:id="point1" srsName="epsg:4326">
                        <gml:coordinates>37:46:30N 122:25:10W</gml:coordinates>
                    </gml:Point>
                </gml:location>
                <cl:civicAddress xmlns:cl="urn:ietf:params:xml:ns:pidf:geopriv10:civicLoc">
                    <cl:LOC>Room 543</cl:LOC>
                    <cl:A3>New York</cl:A3>
                    <cl:A4>Manhattan</cl:A4>
                    <cl:country>US</cl:country>
                    <cl:A2>King's County</cl:A2>
                    <cl:FLR>5</cl:FLR>
                    <cl:HNO>123</cl:HNO>
                    <cl:HNS>A, 1/2</cl:HNS>
                    <cl:LMK>Low Library</cl:LMK>
                    <cl:PRD>N, W</cl:PRD>
                    <cl:A1>New York</cl:A1>
                    <cl:A5>Morningside Heights</cl:A5>
                    <cl:PC>10027-0401</cl:PC>
                    <cl:NAM>Joe's Barbershop</cl:NAM>
                    <cl:A6>Broadway</cl:A6>
                    <cl:STS>Avenue, Platz, Street</cl:STS>
                    <cl:POD>SW</cl:POD>
                </cl:civicAddress>
            </gp:location-info>
            <gp:usage-rules>
                <gp:retransmission-allowed>no</gp:retransmission-allowed>
                <gp:retention-expiry>18 Oct 2010 10:07:29 GMT</gp:retention-expiry>
            </gp:usage-rules>
        </gp:geopriv>*/

        
        //Node locationInfoNode = (Node) applyXPAth(geoprivNode, "location-info",
        //        XPathConstants.NODE);
        NodeList locationInfoNodeList = geoprivNode.getElementsByTagNameNS(NAMESPACE_GP, "location-info");
        
        if (locationInfoNodeList.getLength() > 0) {
            final Element locationInfoNode = (Element)locationInfoNodeList.item(0);
            
            //Node locationNode = (Node) applyXPAth(locationInfoNode, "location", XPathConstants.NODE);
            NodeList locationNodeList = locationInfoNode.getElementsByTagNameNS(NAMESPACE_GML, "location");
            
            if(locationNodeList.getLength() > 0) {
                final Element locationNode = (Element)locationNodeList.item(0);
                
                //Node pointNode = (Node) applyXPAth(locationNode, "Point", XPathConstants.NODE);
                NodeList pointNodeList = locationNode.getElementsByTagNameNS(NAMESPACE_GML, "Point");
                if(pointNodeList.getLength() > 0) {
                    final Element pointNode = (Element)pointNodeList.item(0);
                    Point geoPoint = parseGeoPoint(pointNode);
                    retValue.setPoint(geoPoint);
                }

                NodeList circleNodeList = locationNode.getElementsByTagNameNS(NAMESPACE_GS, "Circle");
                if(circleNodeList.getLength() > 0) {
                    final Element circleNode = (Element)circleNodeList.item(0);
                    Circle getCircle = parseGeoCircle(circleNode);
                    retValue.setCircle(getCircle);
                }
            }
            
            //Node civicAddressesNode = (Node) applyXPAth(locationInfoNode, "civicAddress", XPathConstants.NODE);
            NodeList civicAddressNodeList = locationInfoNode.getElementsByTagNameNS(NAMESPACE_CL, "civicAddress");
            if(civicAddressNodeList.getLength() > 0) {
                final Element civicAddressesNode = (Element)civicAddressNodeList.item(0);
                
                NodeList childNodes = civicAddressesNode.getChildNodes();
                for(int i = 0, count = childNodes.getLength(); i < count; i++) {
                    Node civicAddressNode = childNodes.item(i);
                    if(civicAddressNode.getNodeType() == Node.ELEMENT_NODE) {
                        String addressIdentifierValue = civicAddressNode.getLocalName();
                        CivicAddressType addressIdentifier = CivicAddressType.createByTagName(addressIdentifierValue);
                        assert addressIdentifier != null;
                        
                        String addressValue = civicAddressNode.getTextContent();
                        retValue.addCivicAddress(addressIdentifier.getCode(), addressValue);
                    }
                }
            }
        }
        
        //Node usageRulesNode = (Node) applyXPAth(geoprivNode, "usage-rules",
        //        XPathConstants.NODE);
        NodeList usageRulesNodeList = geoprivNode.getElementsByTagNameNS(NAMESPACE_GP, "usage-rules");
        
        if(usageRulesNodeList.getLength() > 0) {
            final Element usageRulesNode = (Element) usageRulesNodeList.item(0);
            
            //Node retransmissionAllowedNode = (Node) applyXPAth(geoprivNode, "retransmission-allowed",
            //        XPathConstants.NODE);
            NodeList retransmissionAllowedNodeList = usageRulesNode.getElementsByTagNameNS(NAMESPACE_GP, "retransmission-allowed");
            
            if(retransmissionAllowedNodeList.getLength() > 0) {
                final Node retransmissionAllowedNode = retransmissionAllowedNodeList.item(0);
                
                String retransmissionAllowedValue = retransmissionAllowedNode.getTextContent();
                boolean isRetransmissionAllowed = "yes".equalsIgnoreCase(retransmissionAllowedValue);
                retValue.setRetransmissionAllowed(isRetransmissionAllowed);
            }
            
            //Node retentionExpiryNode = (Node) applyXPAth(geoprivNode, "retention-expiry",
            //        XPathConstants.NODE);
            NodeList retentionExpiryNodeList = usageRulesNode.getElementsByTagNameNS(NAMESPACE_GP, "retention-expiry");
            if(retentionExpiryNodeList.getLength() > 0) {
                final Node retentionExpiryNode = retentionExpiryNodeList.item(0);
                String retentionExpiryValue = retentionExpiryNode.getTextContent();
                Date retentionExpiryDate = Utils.convertInetTimeFormatToJavaTime(retentionExpiryValue);
                retValue.setRetentionExpires(retentionExpiryDate);
            }
        }
        
        //<gp:method>Wiremap</gp:method>
        NodeList methodNodeList = geoprivNode.getElementsByTagNameNS(NAMESPACE_GP, "method");
        if(methodNodeList.getLength() > 0) {
            final Element methodNode = (Element) methodNodeList.item(0);
            String method = methodNode.getTextContent();
            retValue.setMethod(method);
        }    
        
        return retValue;
    }
    
    private Point parseGeoPoint(Element pointNode) {
        //NamedNodeMap attributes = pointNode.getAttributes();
        
        //Node gmlIdItem = attributes.getNamedItemNS(NAMESPACE_GML, "id");
        //String gmlId = gmlIdItem.getNodeValue();
        String gmlId = pointNode.getAttributeNS(NAMESPACE_GML, "id");
        
        //String srsName = attributes.getNamedItem("srsName").getNodeValue();
        String srsName = pointNode.getAttribute("srsName");
        
        
        String coordinates = null;
/*                    NodeList childNodes = pointNode.getChildNodes();
        for(int i = 0, count = childNodes.getLength(); i < count; i++) {
            Node coordinatesNode = childNodes.item(i);
            String attrName = NAMESPACE_GML_PREFIX + ":coordinates";
            if(attrName.equals(coordinatesNode.getNodeName())) {
                coordinates =  coordinatesNode.getTextContent();
            }
        }
*/                    
        NodeList coordinatesNodeList = pointNode.getElementsByTagNameNS(NAMESPACE_GML, "coordinates");
        if(coordinatesNodeList.getLength() > 0) {
            final Node coordinateNode = coordinatesNodeList.item(0);
            coordinates = coordinateNode.getTextContent();
        }

        return new Point(gmlId, srsName, coordinates);
    }
    
    private Circle parseGeoCircle(Element circleNode) {
        /* xmlns:gs="http://www.opengis.net/pidflo/1.0"
        <gs:Circle srsName="urn:ogc:def:crs:EPSG::4326">
          <gml:pos>42.5463 -73.2512</gml:pos>
          <gs:radius uom="urn:ogc:def:uom:EPSG::9001">
            850.24
          </gs:radius>
        </gs:Circle>
         */
        
        String srsName = circleNode.getAttribute("srsName");
        
        String coordinates = null;
        NodeList coordinatesNodeList = circleNode.getElementsByTagNameNS(NAMESPACE_GML, "pos");
        if(coordinatesNodeList.getLength() > 0) {
            final Node coordinateNode = coordinatesNodeList.item(0);
            coordinates = coordinateNode.getTextContent();
        }
        
        String radius = null;
        NodeList radiusNodeList = circleNode.getElementsByTagNameNS(NAMESPACE_GS, "radius");
        if(radiusNodeList.getLength() > 0) {
            final Node radiusNode = radiusNodeList.item(0);
            radius = radiusNode.getTextContent();
        }

        return new Circle(srsName, coordinates, radius);
    }

    /**
     * Parse xml into instance of DefaultPresenceDocument.
     *
     * @return - created instance of DefaultPresenceDocument.
     * @throws IOException - if document can't be parsed from xml
     */
    DefaultPresenceDocument parse() throws IOException {
        Log.d(TAG, "parse#start parsing");
        final DefaultPresenceDocument retValue;

        final PresenceDocBuilder docBuilder = new DefaultPresenceDocument.PresenceDocBuilder();

        docBuilder.buildAccessType(AccessType.READ).buildDocument(document);

        NodeList presenceNodeList = document.getElementsByTagNameNS("*", "presence");
        assert presenceNodeList.getLength() == 1;
        Element presenceNode = (Element)presenceNodeList.item(0);
        
        docBuilder.buildPersonalInfo(parsePersonInfo(presenceNode))
                .buildServicesInfo(parseServicesInfo(presenceNode))
                .buildDeviceInfo(parseDevicesInfo(presenceNode))
                .buildDirectContents(parseDirectContents());

        try {
            retValue = docBuilder.build();
        }
        catch (IllegalArgumentException e) {
            throw new IOException("PresenceDocBuilder#" + e.getMessage());
        }

        Log.d(TAG, "parse#retValue = " + retValue);
        Log.d(TAG, "parse#end parsing");
        return retValue;
    }

    //TODO: debug routine

/*    public static void main(String[] args) {
        try {
            final PresenceDocumentParser documentParser = new PresenceDocumentParser(TEST_NOTIFY);
            Element presenceNode = (Element)documentParser.document.getElementsByTagNameNS("*","presence").item(0);
            final PersonInfo personInfo = documentParser.parsePersonInfo(presenceNode);
            System.out.println("" + personInfo);
            
            Collection<DeviceInfo> parseDevicesInfo = documentParser.parseDevicesInfo(presenceNode);
            System.out.println(parseDevicesInfo);
            
            Collection<ServiceInfo> parseServicesInfo = documentParser.parseServicesInfo(presenceNode);
            System.out.println(parseServicesInfo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
