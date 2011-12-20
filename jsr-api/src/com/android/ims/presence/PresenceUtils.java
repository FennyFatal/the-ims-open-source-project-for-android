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

import javax.microedition.ims.android.presence.IEvent;
import javax.microedition.ims.presence.Event;
import javax.microedition.ims.presence.ServiceInfo;
import javax.microedition.ims.presence.WatcherFilterSet;
import java.util.Date;
import java.util.Random;

/**
 * Presence utility class.
 * 
 * @author Andrei Khomushko
 *
 */
public final class PresenceUtils {
    private static final String TAG = "PresenceUtils";
    
    private PresenceUtils() {
        assert false;
    }
    
    private final static String XML_R= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" " +
    "xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\" " +
    "xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\" " +
    "xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\" " +
    "xmlns:c=\"urn:ietf:params:xml:ns:pidf:cipid\" " +
    "xmlns:caps=\"urn:ietf:params:xml:ns:pidf:caps\" " +
    "xmlns:gml=\"urn:opengis:specification:gml:schema-xsd:feature:v3.0\" " +
    "entity=\"#presenceEntity\">" +
    
    "<tuple id=\"e750389c-4b8a-4dd5-8e01-c26869a168c1\">" +
    "<status>" +
    "<basic>open</basic></status>" +
    "<note xmlns=\"urn:ietf:params:xml:ns:pidf:rpid\">#serviceNote</note>"+
    "<service-description xmlns=\"urn:oma:xml:prs:pidf:oma-pres\">" +
    "<service-id>presence</service-id>" +
    "<version>1.0</version>" +
    "</service-description>" +
    "<deviceID xmlns=\"urn:ietf:params:xml:ns:pidf:data-model\">Mobile</deviceID>" +
    "<contact priority=\"1.0\">#presenceEntity</contact>" +
    "</tuple>" +
    
    "<device xmlns=\"urn:ietf:params:xml:ns:pidf:data-model\" id=\"#deviceId\">" +
    "<deviceID>Mobile</deviceID>" +
    "<note>Mobile</note></device>" +
    
    "</presence>";

    public static String convertWatcherFilterToString(WatcherFilterSet filterSet) {
        //TODO unimplemented yet
        return null;
    }
    
    public static String convertDocumentDOMToString(String presenceEntity, DefaultPresenceDocument presenceDocument) {
        //String presenceEntity, String serviceNote, String contact
        //TODO unimplemented yet
        ServiceInfo serviceInfo = presenceDocument.getServiceInfo()[0];
        return convertDOMToString(presenceEntity, 
                serviceInfo.getFreeText(), 
                presenceEntity,
                serviceInfo.getAttachable().getTimestamp());
    }
    private static Random random = new Random();
    
    private static String convertDOMToString(String presenceEntity, String serviceNote, String contact, Date timestamp) {
        return XML_R.replaceAll("#presenceEntity", presenceEntity).
            replaceAll("#serviceNote", serviceNote).
            replaceAll("#contact", contact).
            replaceAll("#deviceId", "af22dfb4-1565-4748-a734-3d02caca7" + random.nextInt(100)).
            replaceAll("#timestamp", timestamp.toGMTString());
    }     
    
    public static int convertToEventType(IEvent.EventType eventType) {
        final int retValue; 
        switch (eventType) {
        case EVENT_APPROVED:
            retValue = Event.EVENT_APPROVED;
            break;
        case EVENT_DEACTIVATED:
            retValue = Event.EVENT_DEACTIVATED;
            break;
        case EVENT_GIVEUP:
            retValue = Event.EVENT_GIVEUP;
            break;
        case EVENT_NORESOURCE:
            retValue = Event.EVENT_NORESOURCE;
            break;
        case EVENT_PROBATION:
            retValue = Event.EVENT_PROBATION;
            break;
        case EVENT_REJECTED:
            retValue = Event.EVENT_REJECTED;
            break;
        case EVENT_SUBSCRIBE:
            retValue = Event.EVENT_SUBSCRIBE;
            break;
        case EVENT_TIMEOUT:
            retValue = Event.EVENT_TIMEOUT;
            break;
        default:
            retValue = -1;
            Log.i(TAG, "Unknown errorType = " + eventType);
            break;
        }
        return retValue;
    }
    
    public static Event createEvent(IEvent event) {
        return new EventImpl(PresenceUtils.convertToEventType(event
                .getEventType()), event.getRetryAfter());
    }
    
    public static void main(String[] args) {
        String res = convertDOMToString("sip:12345678@dummy.com", "Online", "sip:12345678@dummy.com", new Date());
        System.out.println(res);
    }
}
