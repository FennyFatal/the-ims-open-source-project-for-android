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

import com.android.ims.presence.DefaultWatcherInfo.Builder;
import com.android.ims.util.XMLUtils;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.microedition.ims.presence.Event;
import javax.microedition.ims.presence.WatcherInfo;
import java.io.IOException;
import java.util.*;

/**
 * This class is responsible for parsing presence document.
 *
 * @author Andrei Khomushko
 */
final class WatcherInfoSubscriberParser extends BaseXMLParser {
    private static final String TAG = "WatcherInfoSubscriberParser";

    private final Document document;

    /**
     * @param pidfSource
     * @throws IOException              - if instance can't be instantiated
     * @throws IllegalArgumentException - if the pidfSource argument is null
     */
    WatcherInfoSubscriberParser(String watchersSource) throws IOException {
        /*if (TextUtils.isEmpty(watchersSource)) {
            throw new IllegalArgumentException(
                    "The watchersSource argument is null");
        }*/

        Document document = XMLUtils.createDocument(watchersSource, false);
        assert document != null;

        this.document = document;
    }

    private List<WatcherInfo> parseWatchersInfo() throws IOException {
        final List<WatcherInfo> watcherInfos = new ArrayList<WatcherInfo>();

        final NodeList watcherNodes;

        try {
            watcherNodes = (NodeList) applyXPAth(document,
                    "watcherinfo/watcher-list/watcher", XPathConstants.NODESET);
        }
        catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }

        for (int i = 0, count = watcherNodes.getLength(); i < count; i++) {
            Node node = watcherNodes.item(i);
            WatcherInfo deviceInfo = parseWatcherInfo(node);
            watcherInfos.add(deviceInfo);
        }

        return watcherInfos;
    }

    private WatcherInfo parseWatcherInfo(Node watcherNode) throws IOException {
        final WatcherInfo retValue;

        /*
         * <watcher display-name="movial20@dummy.com" duration-subscribed="1"
         * event="subscribe" expiration="598"
         * id="c5c6e35b-0a44-46ee-8fc9-6789dd8ac499_ybsipf0b40"
         * status="active">sip:movial20@dummy.com</watcher>
         */
        final String displayName, duration, event, expiration, id, status, identity;

        //try {
            NamedNodeMap attributes = watcherNode.getAttributes();
            //displayName = (String) applyXPAth(watcherNode, "@display-name", XPathConstants.STRING);
            
            Node displayNameNode = attributes.getNamedItem("display-name");
            displayName = displayNameNode != null? displayNameNode.getNodeValue(): null;
            
            //duration = (String) applyXPAth(watcherNode, "@duration-subscribed", XPathConstants.STRING);
            Node durationNode = attributes.getNamedItem("duration-subscribed");
            duration = durationNode != null? durationNode.getNodeValue(): null; 
            
            //event = (String) applyXPAth(watcherNode, "@event", XPathConstants.STRING);
            Node eventNode = attributes.getNamedItem("event");
            event = eventNode != null? eventNode.getNodeValue(): null; 
            
            //expiration = (String) applyXPAth(watcherNode, "@expiration", XPathConstants.STRING);
            Node expirationNode = attributes.getNamedItem("expiration");
            expiration = expirationNode != null? expirationNode.getNodeValue(): null; 
            
            //id = (String) applyXPAth(watcherNode, "@id", XPathConstants.STRING);
            Node idNode = attributes.getNamedItem("id");
            id = idNode != null? idNode.getNodeValue(): null; 
            
            //status = (String) applyXPAth(watcherNode, "@status", XPathConstants.STRING);
            Node statusNode = attributes.getNamedItem("status");
            status = statusNode != null? statusNode.getNodeValue(): null; 
            
            //identity = (String) applyXPAth(watcherNode, ".", XPathConstants.STRING);
            identity = watcherNode.getFirstChild().getNodeValue();
        //}
        /*catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }*/

        Builder builder = new DefaultWatcherInfo.Builder();
        builder.buildDisplayName(displayName).
                buildURI(identity).
                buildId(id);

        //if (!TextUtils.isEmpty(duration)) {
        if (duration != null && !"".equals(duration)) {
            int durTime = Integer.parseInt(duration);
            builder.buildDuration(durTime);
        }

        //if (!TextUtils.isEmpty(expiration)) {
        if (expiration != null && !"".equals(expiration)) {
            int expTime = Integer.parseInt(expiration);
            builder.buildExpiration(expTime);
        }

        if (event != null) {
            int eventType = EventHelper.getEventByName(event);
            builder.buildEvent(new EventImpl(eventType, -1));
        }

        if (status != null) {
            int state = StatusHelper.getStatusByName(status);
            builder.buildState(state);
        }

        retValue = builder.build();

        return retValue;
    }
    
    private enum EventHelper {
        SUBSCRIBE("subscribe", Event.EVENT_SUBSCRIBE),
        APPROVED("approved", Event.EVENT_APPROVED),
        DEACTIVATED("deactivated", Event.EVENT_DEACTIVATED),
        PROBATION("probation", Event.EVENT_PROBATION),
        REJECTED("rejected", Event.EVENT_REJECTED),
        TIMEOUT("timeout", Event.EVENT_TIMEOUT),
        GIVEUP("giveup", Event.EVENT_GIVEUP),
        NORESOURCE("noresource", Event.EVENT_NORESOURCE);
        
        private final String name;
        private final int value;
        
        private final static Map<String, Integer> mapping = new HashMap<String, Integer>();
        
        static {
            for(EventHelper status: values()) {
                mapping.put(status.name, status.value);
            }
        }
        
        private EventHelper(String name, int value) {
            this.value = value;
            this.name = name;
        } 
        
        private static int getEventByName(String name) {
            if (name == null) {
                return -1;
            }
            Integer status = mapping.get(name.toLowerCase());
            return status != null? status: -1;
        }
    }
    
    private enum StatusHelper {
        //"active" / "pending" / "terminated"
        ACTIVE("active", WatcherInfo.STATE_ACTIVE),
        PENDING("pending", WatcherInfo.STATE_PENDING),
        TERMINATED("terminated", WatcherInfo.STATE_TERMINATED);
        
        private final String name;
        private final int value;
        
        private final static Map<String, Integer> mapping = new HashMap<String, Integer>();
        
        static {
            for(StatusHelper status: values()) {
                mapping.put(status.name, status.value);
            }
        }
        
        private StatusHelper(String name, int value) {
            this.value = value;
            this.name = name;
        } 
        
        private static int getStatusByName(String name) {
            Integer status = mapping.get(name);
            return status != null? status: -1;
        }
    }

    /**
     * Parse xml into instances of WatcherInfo.
     *
     * @return - created instances of WatcherInfo.
     * @throws IOException - if document can't be parsed from xml
     */
    WatcherInfo[] parse() throws IOException {
        //Log.d(TAG, "parse#start parsing");
        List<WatcherInfo> watchers = parseWatchersInfo();
        //Log.d(TAG, "parse#end parsing");
        return watchers.toArray(new WatcherInfo[0]);
    }

    private static final String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<tns:watcherinfo xmlns:tns=\"urn:ietf:params:xml:ns:watcherinfo\" version=\"0\" state=\"full\">\n" +
            "   <tns:watcher-list resource=\"sip:79262948589@multifon.ru\" package=\"presence\">\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"+Hjx5Or8/Cuxgjuybv7PBg==\">sip:79255894279@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"FVvzEFdkiEIJoHqZat4tDA==\">sip:79255894279@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"6VT2ohedJZeBAsXtGy1gAQ==\">sip:79255894279@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"f/zY3N9EksIS7JZjX4HcpA==\">sip:79255894279@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"MtU4aJX4SPc8K9G+zKdHxg==\">sip:79255894279@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"pmvvzpK9xxL2dDgUQvZ/og==\">sip:79255894279@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"ZQjfzJ0roJAZOvdlZHQUTQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Ub8wjWkBGU6VbrWaPDDEDA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"nuUqtoTxB5hYbmN+8MXqig==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"O8bSN82e/nh+Tv3RRAOJ+g==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"1Gx+VuZnpfdw5AYHoM7LOw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"dVPe5wpcjc4A2KxgxVtf7g==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"N/3tQE+UpX7WG6FDq2YihA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Wzk3OAsk2hPWkH99+m0SYg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Z43/vL3HiJV+RmOQ8F30eQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"uQe3hzkGYqhB//hIK4LV3w==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Vp0r+Lf8xusY4pYF2M1KWQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"eRH9qVzSYqZH7PkwKGys9g==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"zGvrit8ePpPkem6K7lsOzQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"0LIe2ZaB1IHIgQobNInPPg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"kddsxfmvRO2Y6pc/P7GcNw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"rxc5a+IRoltbkEL5j283lg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"EqIY6Zuw68kh8osZTldlrg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"46HIuivUwApSnWCJ9tQo4Q==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"TwygaJJGqtQ06cj547sXEQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"PhumGlebxMx0j0IO86X3Ww==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"CkuiiCEWWmOF6J9DYiHuug==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"GlxRyVs3Nhy6pVRyXIpyhw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"a3zkMAy8LUbLaWnpiRUQcg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"TFgG/wKb7Bu3COFUeTHDUw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"4xZNp/mE/5LhW7aFLDf39g==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"wPQlVOEOt5lzUonHIY0foQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"W5yvvXTm6reLwhS+AIt59w==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"jIOdQFLGdWgqfeR/GzI4Tw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"vyqJ0xLAGww7n6Euv3n0fg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"+ACIwMK/yn5eE+UcnoLxxQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"gi8dGVDPzsak94FDHtcNww==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"3WzmeBKTn2xZirPukjKCdw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"LM43L45VrviisWCQCIGiEw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"UgtguiNdT47YNJHckcfbzw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"AGU5+YFzAhg0/AnhAmjmuQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"mrrufSJZKS80D2Y6N7gBrQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"2HU7co4SttGGUjO6F1keSA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"LEUeqH3AKxpGNA+6ioHcPg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"PcrZXDdv0x1JhLogy9Etzw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"YyBk/IjH79H8YiwaOEovOw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"II2ykgUxk0b1NmS9WrhiBA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"yd5cxEZnhW/+bszF3HjRTw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"CCczehL65geaQV7WkwmqkA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"ZDANMJrPCjXwmb/Gm4hY3A==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"QdqemywWRFKysBHHVBsfAQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"xSP5hBLNuuN1olgkvJdw6Q==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"sWPYpU9LvBjO0kqabyaZvg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"6NON8LuEX+bcBplskIbJZw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"gVkoDO62JsOfwWxqYaq1Yg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"qdkvyJTl2RhL/5oO0CDtaQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"rHHO34LMHMbRVssiT7a0Lg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Fo+XmQiUouHT7vm6o5Y4og==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"HTUZwdOJchtZ1RToIDP1Lg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"R/wA75bhN433zFo/GknAUQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"AZVdaSsJw2vwiN/Divv15g==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"awJRnMfCMdCfUfILKzGeew==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"dir7w9MQM9wi3eYi0+cN7w==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"t7pVZfU+y1QphDgjVuRBwg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"WtcCRGw1/qtMiOfBoMn19w==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"cDSn84ltjHI/bz6hmu9SwA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"qtrvxEAM54WqN1mFDjSnMw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"8gkZb9wL3tJwCPSmYiYYOg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"c1AxYpLctemE0K3ypSl0og==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"nHl5BObzi9RabiebGRTwUg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"aPb1HFKUoj0DZhp6oC1few==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"l+q9TMoJzKnkZs39yxjxAw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"iBP+6KvTe63H+/IJimWeRw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"sQfkPWe1iFzxR0zBc3hcjw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"cxQOlltAUgbLqt/CJ+c08w==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"IyP9giHIV2CXi4yU1pFT2w==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"88F8vLFtv6BsdIOvkC+9Pw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"04lThl3cYWL4kh+srZqfcg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"WmNNpL/8bUnCJA0IiylU/Q==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"DoJ8Da94BAI2whZS4myoQw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"euD5AK+7zYexoOCAGbzBhA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"ITK1UKNthk00vZPWCN6x2A==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"No3YGpsiAFQvdukyt8g4HA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"0/anZiZhWOvVjIenv3a9aA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"uq6JZW5gbnjRl5Mog11c4Q==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"iLfTRZwqXAokVEop1mLjmg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Qkfxw/3JAIzgG9m8Eq5s/A==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"xyIPEHAtciIQaLzATyzlCA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"gg8MDE/Y5iuXDhgaWhua4g==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"wCdcGKU72ATdH3IlGwpWug==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"4Id1hQ4aE4G5OV5XLiXybQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"HuuREyO1VbX6hC+6XBrVww==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"uI1BRtVeyl+3aJocANdClg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"aKpU5garMSxsVxf+nlej9A==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"zzGmVf0EbKkYfllH4IYK6g==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"nPuiEjNyNPDtya/ZUi1xqg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"ZjBxtFqd1SU/yobMXPaFiQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"lSw2sNk68dWMH2l2G7KTPA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"yaVJDbh5/oSguM69RvaE8Q==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"BdwhkE7ctosVZRFw1swUow==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Qi/uKf/u8tZnAP2FteHqcg==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"NvmVNhq9u62HoVVA3uvDsw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"tyylRWDPRI1Fu0B3YljIsQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"ctNo8fmnsYwqoH0LsUJicQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"vbOKW1dJhj/9S0xqFpVV+g==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"BwUuVlPrzTw2sXFGpquWxA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Cces9fn4JFclRHtbpQ+F4g==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"PQVSgg0HIteKTBoxuIszjA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"4KKHUt/LvjIyH8aSwkxOAg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"VoFEA6K4ffqUz7FGLiYdjA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"TwguGUBqv/VDZqjlBWBaXA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"P9GzgcGhRn88S8sVtdcDRQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"HTApoMoAXxabOIu3ZzSwJQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"SY8tkSUVWGgAP+NzE9QKpA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"nQECxB1D/LjtMXG5NVrhIw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"AS2U884J3M/I2+kI2F07MA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"qRL1s433fvob6Q/qPls/Tg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"U4GkCs8w9yK/Y5zZIU/K9Q==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"YCFEVDxDY7sOuWVYfYSL7A==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"+LejCU2h5VsrlXH7hHqw1g==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"0NioGBcq8VIQl+KV7A4QMw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"gsZFnHF2QYbanuLHcrfF6w==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"EKWA0CTCe3sr6wEMP215iw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"jyqS7Km9OxjylOZXHmlmeQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"zrd8/PASFbDJmdFKjy1ZnA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"2qFWtn6stsIqvaJthHINVg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"/xS5uZUqDfhAxq/SFngEog==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"xQKe6TgzZcVr67y9FHP3yw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"qvqczzadxGk0P9N/hu6taA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"vp3tYtVkcCdkmPYDf7EpUg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"bbS5SpDkEY10QY8VnhBokw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"1vM8G19W5zRz72bwMg0aDw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"rvZygGgCnzCNpDgyiMmTmA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"pH9UlPXc8GrXgYrMTGJAnQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"5dPf/Voha0G01QXGfGISuA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"NXjVybgRQbLncldmjgD2Dw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"UmM4SIy7a/indwMywD8GEQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"kFp+qIrtMHtj9FOVsHS2WQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"z+/jh5KTZXzjJLPzo6OVfg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"bXwO66ap8Zqh2hNGHGR04w==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"xx7hzKKxYMdenLmAb6Uv+A==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Zd9f+jX22+/MlPcRW2btGQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Og7fEiz319XsXFiWJyPACQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"2u3vSH/QO2bxtsrAbWOL8g==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"16AHCjtF+k0pdSNdIiy1/A==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"eULOicQWAFiUCTvv0B6tiQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"pK4oFxKWrdoqzP7jbxB9vA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"WOi1iGgvpmA6YwrI6tiA+Q==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"F8TrULWTDy2MOcy2gGG37w==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"7TfK8/NfYTZS41PRFOKepQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Kb7WOC5zKj/c5mTKbOU0Xg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"t6z7E98WdPDbO8ny+d6Qkw==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"46mSGxpIkx+KFCDIro8dUQ==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"mK4DwwSFezkJOPqXkGjucg==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"VHveW2Rj6tRayVIIcU68vQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"y4suLMqxDMSkIMU2RUgUzw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"fJDjoHlQRjr8NC3GaUoNXA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"AQ45d+2J7ObutO77bbhKng==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"/xPr+lP57jtTTZILKjaDJg==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"KQ54etQque2TKGPhd9uTUQ==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"HNsbSWilcuGTlwEWYePhyw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Kbj/oNNO0HfBstFdCjpIqw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"eOo6p1XRlDwvfvzY37yNxg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"1DSMib3H86jaR5l27R6/1Q==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"RtSafdcNVhBwA/mAFECBfw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"ctKcnuJXr/YO8hEizUueVA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"XRLMilJgMeXIPaw9lN+qbQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"KUjF1S6NDo2uLx/eFeDJ0A==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Wv/WDGfXcveJpfjelnWvfw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"11UgEDliSNQ2th8nTOZhig==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"rK+d8eKrFaN8pNwId1Rpag==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"t7whWqEfsJ0wOw5zp0G0Yg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"KWNYvNMeiUiOcbqWL2cxSg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"AX3G5JwFt1IbQJwFgnsTbQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"I9AwiE4zk6xEciifJrChbA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"mlS0fd3+z5majN215qlUNg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"fyNzxteDgRQhUpIBU5uCSQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"RtXv5zUsvxsT8ajmf8bovw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"arZahB0TkNgTYJ85qLdZWg==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"2rzO95y0296K55L4seWlaQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"2JvzuaiGrLzwrDo1tw1+sA==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"PKKrZf5nU2okO/19Kg5iiA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Dnq4kcDlU+8oneQgUsFvcA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"LIWMjJg6gO+1AhcG5xdNDg==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"B73B21/W7OuIRJfbjDge/Q==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"JMfNPOP2kLepoBQo+P8rmA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"YyXc34tsDwses8suyxWy1Q==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"80Ypc421KPOKBOv0DSG8JQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"FMNi0agQbXimqURc93Haqg==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"dsa6ueDsi/Y1Xp0HDduFiQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"D7kq3Zp59BXzJ//XlGimXA==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"7Oz++Mw2Un2dY112aRxJTw==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"5isUQV9OGRXY0LWR1PSe6g==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"e3HGq5P8JLIxwaVe/Pnttg==\">sip:savetikov@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"46I13G1l5r6bn3x37C59fw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"+fZzdRwWvh+tJyD2T15Eng==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"15zJlTf8w+ZYVbi36tQ9Lg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"k6O+hXg/KWzPOKjc3q6JVg==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"XvT5fJFBbYqgathmPeuwHA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"EkOlBAm4UKyzKvsJ+2w2lw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"a2Fec8DzYTGZnGw77sl8Hw==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"mys8xrOq9UjN9QfrM2JWig==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"2WNh3O8lILA+GpZC/oE9KA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"mfxVWJS0P0wcLDl43H7mig==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"2093yjt++Sul7gakAFNILg==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"qW0xHi3lQavqosaltkjkgQ==\">sip:79262948588@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"RAZh/J9QuOQ4XsanA3Fv9A==\">sip:79262948588@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"0ZXGz6ayXXvf5ORMftMizA==\">sip:79262948588@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"bqOlHciOnXMfFv+h1XtFRg==\">sip:79262948589@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"iYdEW8afrUdAnyNrzOMGtQ==\">sip:79262948588@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"zpe1Spdjx1WdMQi08VQGSg==\">sip:79262948588@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"0rK81Ks/A/0NJLUpXswLug==\">sip:79262948589@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"8emTRsvYF0KDIL3zIwpq8A==\">sip:79262948589@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"PjCywMIA+zXAcUGgU7mTBA==\">sip:79262948588@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"fIA8D90/zGk4n6cCnqBQBA==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"fj2OgItALidlSUPheqNMgQ==\">sip:79262948589@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"QMskOOJldjZOTxWBMcSTwA==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"xPAsISUTV9of3XOWs7BK3w==\">sip:79262948588@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"e+QrW9SNi+UXpltP16+NgA==\">sip:79262948589@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"pT/LnupdDOy+yYuUQBPXCQ==\">sip:79262948589@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"9BdRW8y1uZsfV9CNyi3hzg==\">sip:79262948589@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"YTnB97nyKvB4oPwxXABcIw==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"13uf4QQcQ4QeNTN/dCr0cw==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"iCANhgtUnlTLeYXptTEAhg==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"A5r71APPGilAsHu+tJo/ew==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"YnGxEc3ztWBFNUcTvcHMJg==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"1OegNNBOljQv8TPlu9YZxA==\">sip:79266285871@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"uZ/ebUFjV+1LijUblyXEuA==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"HuBtLXs0tDXAi5UHzPelhw==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"SRAYMyAN8lcP4zGu5NHxPQ==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"an6b8x6SsmlyYbol5PDCdw==\">sip:79262951057@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"ynv0pOzXQ7pU5lQG2ACnng==\">sip:79262948587@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"Tsb8EBu740UJDVRhFUDprg==\">sip:79262948588@multifon.ru</tns:watcher>\n" +
            "      <tns:watcher status=\"active\" event=\"approved\" id=\"b7gOnHLlUf4pKFQMFVWjyA==\">sip:79262948589@multifon.ru</tns:watcher>\n" +
            "   </tns:watcher-list>\n" +
            "</tns:watcherinfo>";

    public static void main(String[] args) throws IOException {
        final long startTime = System.currentTimeMillis();
        final long l1 = Runtime.getRuntime().freeMemory();
        System.out.println("Free memory before parsing "+l1);

        final WatcherInfoSubscriberParser watcherInfoSubscriberParser = new WatcherInfoSubscriberParser(test);
        final WatcherInfo[] watcherInfos = watcherInfoSubscriberParser.parse();
        System.out.println("watcherInfos.length = "+ watcherInfos.length);
        System.out.println(""+ Arrays.toString(watcherInfos));

        final long l2= Runtime.getRuntime().freeMemory();
        System.out.println("Free memory after parsing "+l2);

        System.out.println("Free memory diff after parsing "+(l2-l1));

        final long endTime = System.currentTimeMillis();
        System.out.println("Parse time: " + (endTime - startTime) + " millis");
    }
}
