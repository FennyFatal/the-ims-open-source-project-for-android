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

import java.util.Arrays;

/**
 * This class represents an identity or a group of identities which is used in a
 * Rule to represent which conditions apply to that specific Rule. An Identity
 * can be one of four different types according to [RFC4745].
 * 
 * @author Andrei Khomushko
 * 
 */
public final class Identity {
    public static final int IDENTITY_ALL = 2;
    public static final int IDENTITY_ALL_EXCEPT = 3;
    public static final int IDENTITY_DOMAIN_EXCEPT = 4;
    public static final int IDENTITY_SINGLE_USERS = 1;

    private final int type;
    private final String[] identityList;
    private final String domain;

    /**
     * This constructor sets the identity type to IDENTITY_ALL and represents
     * all authenticated users.
     */
    public Identity() {
        this(IDENTITY_ALL, null, null);
    }

    /**
     * This constructor sets the identity type to IDENTITY_DOMAIN_EXCEPT and
     * represents all authenticated users in a certain domain with the
     * exceptions set by the identityList parameter.
     * 
     * @param domain
     *            - a string containing a domain name
     * @param identityList
     *            - an array of user identities to exclude or null to indicate
     *            that no users identities will be excluded
     * @throws IllegalArgumentException
     *             - if the domain argument is null
     */
    public Identity(final String domain, final String[] identityList) {
        this(IDENTITY_DOMAIN_EXCEPT, identityList, domain);

        if (domain == null) {
            throw new IllegalArgumentException("the domain argument is null");
        }
    }

    /**
     * This constructor sets the identity type to IDENTITY_SINGLE_USERS or
     * IDENTITY_ALL_EXCEPT.
     * 
     * IDENTITY_SINGLE_USERS represents a list of authenticated users.
     * IDENTITY_ALL_EXCEPT represents all authenticated users with the
     * exceptions set by the identityList parameter. The identityList can
     * contain both user identities and domains.
     * 
     * @param type
     *            - IDENTITY_SINGLE_USERS or IDENTITY_ALL_EXCEPT
     * @param identityList
     *            - an array of user identities to allow if the type argument is
     *            IDENTITY_SINGLE_USERS or an array of user identities and/or
     *            domains to exclude if the type argument is IDENTITY_ALL_EXCEPT
     * @throws IllegalArgumentException
     *             - if the type argument is other than IDENTITY_SINGLE_USERS or
     *             IDENTITY_ALL_EXCEPT
     * @throws IllegalArgumentException
     *             - if the identityList argument is null or an empty array
     */
    public Identity(int type, String[] identityList) {
        this(type, identityList, null);

        if (type != IDENTITY_SINGLE_USERS && type != IDENTITY_ALL_EXCEPT) {
            throw new IllegalArgumentException(
                    "the type argument is other than IDENTITY_SINGLE_USERS or IDENTITY_ALL_EXCEPT");
        }

        if (identityList == null || identityList.length == 0) {
            throw new IllegalArgumentException(
                    "the identityList argument is null or an empty array");
        }
    }

    Identity(int type, String[] identityList, String domain) {
        this.type = type;
        this.identityList = identityList;
        this.domain = domain;
    }

    /**
     * Returns the type of this Identity.
     * 
     * @return the type of this Identity
     */
    public int getIdentityType() {
        return type;
    }

    /**
     * Returns the identityList. The content and meaning of the identityList
     * vary depending on the type of this Identity.
     * 
     * @return an array of allowed user identities if the type argument is
     *         IDENTITY_SINGLE_USERS, or an array of excluded user identities
     *         and domains if the type argument is IDENTITY_ALL_EXCEPT or
     *         IDENTITY_DOMAIN_EXCEPT, null otherwise
     */
    public String[] getIdentityList() {
        return identityList;
    }

    /**
     * Returns the allowed domain. This will only be set if the identity type is
     * IDENTITY_DOMAIN_EXCEPT.
     * 
     * @return the allowed domain if the identity type is
     *         IDENTITY_DOMAIN_EXCEPT, null otherwise
     */
    public String getAllowedDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return "Identity [type=" + type + ", identityList="
                + Arrays.toString(identityList) + ", domain=" + domain + "]";
    }

    public static void main(String[] args) {
        final Identity identity = new Identity(IDENTITY_SINGLE_USERS, new String[]{});
        System.out.println("");
    }
}
