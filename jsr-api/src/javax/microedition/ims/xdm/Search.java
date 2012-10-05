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

import org.w3c.dom.Document;

/**
 * Represents a search for data in XML documents on the server.
 * 
 * A list entry consists of either a single user URI or a reference to an
 * already existing URI list. Each entry can provide an optional display name.
 *
 * </p><p>For detailed implementation guidelines and for complete API docs, 
 * please refer to JSR-281 and JSR-235 documentation.
 *
 * @author Andrei Khomushko
 * 
 */
public class Search {
    /**
     * String used to indicate that a search should be performed in all domains.
     */
    public static final String DOMAIN_ALL = "all";

    /**
     * String used to indicate that a search should be performed in a user's
     * home domain.
     */
    public static final String DOMAIN_HOME = "home";

    /**
     * Creates a search based on a search target and an XML representation of a
     * search document.
     * 
     * @param searchDocument
     *            - the search document
     * @param target
     *            - the target of the search, see above
     * @throws IllegalArgumentException
     *             - if the searchDocument argument is null
     * @throws IllegalArgumentException
     *             - if the target argument is null
     */
    public Search(Document searchDocument, String target) {
        // TODO not implemented yet
    }

    /**
     * Creates a search based on a search target and an XQuery query.
     * 
     * @param query
     *            - the XQuery query
     * @param target
     *            - the target of the search, see above
     * @throws IllegalArgumentException
     *             - if the query argument is null
     * @throws IllegalArgumentException
     *             - if the target argument is null
     */
    public Search(String query, String target) {
        // TODO not implemented yet
    }

    /**
     * Given a number of XQuery conditions, creates a new condition that is met
     * if all of the supplied conditions are met.
     * 
     * @param conditions
     *            - an array of conditions
     * @return a condition that is met if all the conditions are met
     * @throws IllegalArgumentException
     *             - if the conditions argument is null, an empty array, or
     *             contains one or more null elements
     */
    public static String createConditionAnd(String[] conditions) {
        if (conditions == null || conditions.length == 0) {
            throw new IllegalArgumentException(
                    "the conditions argument is null, an empty array");
        }

        for (String condition : conditions) {
            if (condition == null) {
                throw new IllegalArgumentException(
                        "conditions contains one or more null elements");
            }
        }

        // TODO not implemented yet
        return null;
    }

    /**
     * Given two XQuery conditions, creates a new condition that is met if both
     * of the supplied conditions are met. 
     * 
     * @param condition1
     * @param condition2
     * @return a condition that is met if both condition1 and condition2 are met
     * @throws IllegalArgumentException
     *             - if the condition1 argument or the condition2 argument is
     *             null
     */
    public static String createConditionAnd(String condition1, String condition2) {
        if (condition1 == null || condition2 == null) {
            throw new IllegalArgumentException(
                    "condition1 argument or the condition2 argument is null");
        }
        // TODO not implemented yet
        return null;
    }

    /**
     * Given an XQuery condition, creates a new condition that is the negation
     * of the condition.
     * 
     * @param condition
     *            - the condition to negate
     * @return a negation of the condition
     * @throws IllegalArgumentException
     *             - if the condition argument is null
     */
    public static String createConditionNot(String condition) {
        if (condition == null) {
            throw new IllegalArgumentException("condition is null");
        }

        // TODO not implemented yet
        return null;
    }

    /**
     * Given a number of XQuery conditions, creates a new condition that is met
     * if any of the supplied conditions are met.
     * 
     * @param conditions
     *            - an array of conditions
     * @return a condition that is met if any of the conditions are met
     * @throws IllegalArgumentException
     *             - if the conditions argument is null, an empty array, or
     *             contains one or more null elements
     */
    public static String createConditionOr(String[] conditions) {
        if (conditions == null || conditions.length == 0) {
            throw new IllegalArgumentException(
                    "the conditions argument is null, an empty array");
        }

        for (String condition : conditions) {
            if (condition == null) {
                throw new IllegalArgumentException(
                        "conditions contains one or more null elements");
            }
        }
        // TODO not implemented yet
        return null;
    }

    /**
     * Given two XQuery conditions, creates a new condition that is met if any
     * of the supplied conditions are met. This results in a XQuery condition on
     * the form (condition1) or (condition2).
     * 
     * 
     * @param condition1
     * @param condition2
     * @return a condition that is met if either or both of condition1 and
     *         condition2 are met
     * @throws IllegalArgumentException
     *             - if the condition1 argument or the condition2 argument is
     *             null
     */
    public static String createConditionOr(String condition1, String condition2) {
        if (condition1 == null || condition2 == null) {
            throw new IllegalArgumentException(
                    "condition1 argument or the condition2 argument is null");
        }

        // TODO not implemented yet
        return null;
    }

    /**
     * Returns the DOM representation of the search document.
     * 
     * @return the DOM of the search document
     */
    public Document getDOM() {
        // TODO not implemented yet
        return null;
    }

    /**
     * Returns the domain or domains to search in.
     *
     * @return the domain or domains to search in
     */
    public String getDomain() {
        // TODO not implemented yet
        return null;
    }

    /**
     * Returns the maximum number of search results that should be returned by
     * the server.
	 *
     * @return the maximum number of search results, or -1 if there is no upper
     *         limit
     */
    public int getMaxResults() {
        // TODO not implemented yet
        return 0;
    }

    /**
     * Returns the target of the search.
     * 
     * @return the target of the search
     */
    public String getTarget() {
        // TODO not implemented yet
        return null;
    }

    /**
     * Sets the domain or domains to search in.
     * 
     * @param domain
     *            - the domain or domains to search in
     * @throws IllegalArgumentException
     *             - if the domain argument is not valid according to the rules
     *             above
     */
    public void setDomain(String domain) {
        // TODO not implemented yet
    }

    /**
     * Sets the maximum number of search results that should be returned by the
     * server. A negative number removes any existing value.
     * 
     * @param maxResults
     *            - the maximum number of search results, or a negative number
     *            if there should be no upper limit on the number of search
     *            results
     */
    public void setMaxResults(int maxResults) {
        // TODO not implemented yet
    }
}
