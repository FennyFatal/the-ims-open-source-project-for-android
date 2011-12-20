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

import javax.microedition.ims.presence.Event;
import javax.microedition.ims.presence.PresenceDocument;
import javax.microedition.ims.presence.Presentity;

/**
 * Default implementation {@link Presentity}
 * 
 * @author Andrei Khomushko
 *
 */
class DefaultPresentity implements Presentity, WatcherStateListener {
    private final String URI;
    private final String displayName;
    private final PresenceDocument presenceDocument;
    
    private int state = STATE_ACTIVE;
    private Event event;
    
    /**
     * Create Default presentity
     * 
     * @param uRI - the user identity of this Presentity
     * @param displayName - display name
     * @param presenceDocument - presence document
     * 
     * @throws IllegalArgumentException - The uri arg is null
     * @throws IllegalArgumentException - The presenceDocument arg is null
     */
    public DefaultPresentity(String uRI, String displayName,
            PresenceDocument presenceDocument) {
        if(uRI == null) {
            throw new IllegalArgumentException("The uri arg is null");
        }
        
        if(presenceDocument == null) {
            throw new IllegalArgumentException("The presenceDocument arg is null");
        }
        
        this.URI = uRI;
        this.displayName = displayName;
        this.presenceDocument = presenceDocument;
    }

    
    public String getDisplayName() {
        return displayName;
    }

    
    public Event getEvent() {
        return state == STATE_TERMINATED? event: null;
    }
    
    private void setEvent(Event event) {
        this.event = event;
    }

    
    public PresenceDocument getPresenceDocument() {
        return presenceDocument;
    }

    //TODO review
    
    public int getState() {
        return state;
    }
    
    private void setState(int state) {
        this.state = state;
    }

    
    public String getURI() {
        return URI;
    }
    
    
    public void subscriptionFailed() {
        setState(STATE_TERMINATED);
    }
    
    
    public void subscriptionStarted() {
        setState(STATE_ACTIVE);
    }
    
    
    public void subscriptionTerminated(Event event) {
        setEvent(event);
        setState(STATE_TERMINATED);
    }
    
    
    public String toString() {
        return "DefaultPresentity [URI=" + URI + ", displayName=" + displayName
                + ", event=" + event + ", presenceDocument=" + presenceDocument
                + ", state=" + state + "]";
    }
}
