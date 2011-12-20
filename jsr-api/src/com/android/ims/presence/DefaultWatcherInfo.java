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
import javax.microedition.ims.presence.WatcherInfo;

/**
 * Default implementation {@link}
 * 
 * @author Andrei Khomushko
 *
 */
public class DefaultWatcherInfo implements WatcherInfo{
    private final String id;
    private final String uRI;
    private final int state;
    private final Event event;
    private final String displayName;
    private final int duration;
    private final int expiration;
    
    private DefaultWatcherInfo(Builder builder) {
        this.id = builder.id;
        this.uRI = builder.uRI;
        this.state = builder.state;
        this.event = builder.event;
        this.displayName = builder.displayName;
        this.duration = builder.duration;
        this.expiration = builder.expiration;
    }
    
    public String getURI() {
        return uRI;
    }

    public int getState() {
        return state;
    }

    public Event getEvent() {
        return event;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDuration() {
        return duration;
    }

    public int getExpiration() {
        return expiration;
    }
    
    @Override
    public String toString() {
        return "DefaultWatcherInfo [id=" + id + ", uRI=" + uRI + ", state="
                + state + ", event=" + event + ", displayName=" + displayName
                + ", duration=" + duration + ", expiration=" + expiration + "]";
    }

    static class Builder {
        private String id;
        private String uRI;
        private int state;
        private Event event;
        private String displayName;
        private int duration;
        private int expiration;
        
        public Builder buildId(String id) {
            this.id = id;
            return this;
        }
        
        public Builder buildURI(String uRI) {
            this.uRI = uRI;
            return this;
        }
        
        public Builder buildState(int state) {
            this.state = state;
            return this;
        }
        
        public Builder buildEvent(Event event) {
            this.event = event;
            return this;
        }
        
        public Builder buildDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public Builder buildDuration(int duration) {
            this.duration = duration;
            return this;
        }
        
        public Builder buildExpiration(int expiration) {
            this.expiration = expiration;
            return this;
        }
        
        public DefaultWatcherInfo build() {
            return new DefaultWatcherInfo(this);
        }

        @Override
        public String toString() {
            return "Builder [id=" + id + ", uRI=" + uRI + ", state=" + state
                    + ", event=" + event + ", displayName=" + displayName
                    + ", duration=" + duration + ", expiration=" + expiration
                    + "]";
        }
    }
}
