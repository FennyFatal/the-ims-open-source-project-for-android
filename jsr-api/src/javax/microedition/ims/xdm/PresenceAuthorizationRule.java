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

/**
 * The PresenceAuthorizationRule class represents a rule that is used to
 * authorize access for a watchers.
 *
 *
 * A list entry consists of either a single user URI or a reference to an
 * already existing URI list. Each entry can provide an optional display name.
 * </p><p>For detailed implementation guidelines and for complete API docs, 
 * please refer to JSR-281 and JSR-235 documentation.
 *
 * @author Andrei Khomushko
 * 
 */
public class PresenceAuthorizationRule extends Rule {
    /** This action gives the watcher access to the presence document. */
    public static final int ACTION_ALLOW = 1;

    /**
     * This action blocks the watcher from accessing the presence document. This
     * is the default action in a rule.
     */
    public static final int ACTION_BLOCK = 3;

    /**
     * This action gives the watcher access to the presence document if the
     * presentity confirms it.
     */
    public static final int ACTION_CONFIRM = 2;

    /**
     * This action blocks the watcher from accessing presence documents but
     * indicates that the presentity is unavailable.
     */
    public static final int ACTION_POLITE_BLOCK = 4;

    
    private enum MutExclCondition {
        ANONYMOUS_REQUEST,
        URI_LIST,
        IDENTITY,
        OTHER_IDENTITY
    };
    
    
    private int action = ACTION_BLOCK;
    private Identity identity;
    private PresenceContentFilter presenceContentFilter;
    private String uriListReference;
    private MutExclCondition mutExclCondition = MutExclCondition.OTHER_IDENTITY;
    

    public PresenceAuthorizationRule() {
    }
    
    public void setConditionAnonymousRequest() {
        mutExclCondition = MutExclCondition.ANONYMOUS_REQUEST;
    }
    
    public boolean isConditionAnonymousRequest() {
        return mutExclCondition == MutExclCondition.ANONYMOUS_REQUEST;
    }
    
    public void setAction(int action) {
        this.action = action;
    }
    
    public int getAction() {
        return action;
    }
    
    public void setPresenceContentFilter(PresenceContentFilter presenceContentFilter) {
        this.presenceContentFilter = presenceContentFilter;
    }
    
    public PresenceContentFilter getPresenceContentFilter() {
        return presenceContentFilter;
    }
    
    
    public Identity getIdentity() {
        return identity;
    }
    
    
    public String getURIListReference() {
        if (mutExclCondition == MutExclCondition.URI_LIST) {
            return uriListReference;
        }
        return null;
    }
    
    
    public boolean isConditionIdentity() {
        return mutExclCondition == MutExclCondition.IDENTITY;
    }
    
    
    public boolean isConditionOtherIdentity() {
        return mutExclCondition == MutExclCondition.OTHER_IDENTITY;
    }
    
    
    public boolean isConditionURIList() {
        return mutExclCondition == MutExclCondition.URI_LIST;
    }
    
    
    public void setConditionIdentity(Identity identity) {
        this.identity = identity;
        this.mutExclCondition = MutExclCondition.IDENTITY;
    }
    
    
    public void setConditionOtherIdentity() {
        this.mutExclCondition = MutExclCondition.OTHER_IDENTITY;
    }
    
    
    public void setConditionURIList(String uriListReference) {
        this.uriListReference = uriListReference;
        this.mutExclCondition = MutExclCondition.URI_LIST;
    }

    @Override
    public String toString() {
        return "PresenceAuthorizationRule [action=" + action + ", identity="
                + identity + ", presenceContentFilter=" + presenceContentFilter
                + ", uriListReference=" + uriListReference
                + ", mutExclCondition=" + mutExclCondition + "]";
    }
}
