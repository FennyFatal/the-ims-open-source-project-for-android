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
 * The Rule class is an abstract class that all current and future rules must
 * extend. This top-level class covers the common conditions on which a rule
 * will apply. The implementation of this abstract rule supplies three
 * conditions:
 * 
 * <ul>
 * <li>
 * <h4>Identity condition</h4>: If this condition is set to true, the rule will
 * apply to requests from all identities matching the defined Identity, as
 * described in [RFC4745].</li>
 * <li>
 * <h4>URI list condition</h4>:If this condition is set to true, the rule will
 * apply to requests from all identities declared in the defined URI list.</li>
 * <li>
 * <h4>Other Identity condition</h4>:If this condition is set to true, the rule
 * will apply to requests from all identities that are not referenced by any
 * other rule in the document where the rule resides. This can be used to define
 * a default condition.</li>
 * </ul>
 * 
 * These conditions are all mutually exclusive. Only one condition at a time can
 * apply to a rule. All conditions will be set to false by default.
 * 
 * All rules must have a rule identifier. If no rule identifier is set, the
 * implementation of this class will assign one when the rule is added to a
 * document. If the document already contains a rule with the same rule
 * identifier, that rule will be updated.
 * 
 * One common usage of rule is the presence authorization rule which is used for
 * watcher authorization in a Presence service.
 * 
 * @author Andrei Khomushko
 * 
 */
public abstract class Rule {
    private String ruleId;

    Rule() {

    }

    /**
     * 
     * @return
     */
    abstract Identity getIdentity();

    /**
     * Returns the rule identifier of this rule.
     * 
     * @return the rule identifier of this rule.
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * Returns the URI list reference for this rule.
     * 
     * @return the URI list reference for this rule.
     */
    abstract String getURIListReference();

    /**
     * Returns the status of the Identity condition.
     * 
     * @return the status of the Identity condition.
     */
    abstract boolean isConditionIdentity();

    /**
     * Returns the status of the Other Identity condition.
     * 
     * @return the status of the Other Identity condition.
     */
    abstract boolean isConditionOtherIdentity();

    /**
     * Returns the status of the URI list condition.
     * 
     * @return the status of the URI list condition.
     */
    abstract boolean isConditionURIList();

    /**
     * Sets the identity of this rule.
     * 
     * @param identity
     *            - the identity of this rule.
     */
    abstract void setConditionIdentity(Identity identity);

    /**
     * Sets the mutually exclusive condition to the Other Identity condition.
     * When this condition is set all other mutually exclusive conditions
     * defined for this rule will be false.
     */
    abstract void setConditionOtherIdentity();

    /**
     * Sets an URI list, with a list reference defined by parameter
     * uriListReference, for this rule.
     * 
     * @param uriListReference
     */
    abstract void setConditionURIList(String uriListReference);

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
}
