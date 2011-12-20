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

package javax.microedition.ims.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class responsible for description USER identity.
 *
 * @author Andrei Khomushko
 */
public final class UserInfo {
    private static final String ALLOWED_INPUT_FORMAT = "<schema>:<name>@<domain>";
    private static final String ERROR_MESSAGE_FORMAT_STRING = "Can not parse input '%s'.  Input must be in format %s";
    private static final String FORBIDDEN_SYMBOLS = "\\s,@,:";
    private static final String ALLOWED_SYMBOL_CLASS = createAllowedSymbolClass(FORBIDDEN_SYMBOLS);

    private static String createAllowedSymbolClass(final String forbiddenSymbols) {
        return "[^" + forbiddenSymbols.replaceAll(",", "") + "]";
    }

    //Matches <schema>:<name>@<domain>
    private static final String FULL_URI_REGEXP = "(^\\w+:" + ALLOWED_SYMBOL_CLASS + "+@" + ALLOWED_SYMBOL_CLASS + "+)$";
    //Matches <name>@<domain>
    private static final String NO_SCHEMA_URI_REGEXP = "^(" + ALLOWED_SYMBOL_CLASS + "+@" + ALLOWED_SYMBOL_CLASS + "+)$";
    //Matches <name>
    private static final String NAME_ONLY_URI_REGEXP = "^(" + ALLOWED_SYMBOL_CLASS + "+)$";


    private final String schema;
    private final String name;
    private final String domain;
    private static final Pattern INPUT_PATTERN = Pattern.compile(FULL_URI_REGEXP + "|" + NO_SCHEMA_URI_REGEXP + "|" + NAME_ONLY_URI_REGEXP);


    public UserInfo(String schema, String name, String domain) {

        if (name == null && domain == null) {
            throw new IllegalArgumentException("Either name or domain MUST NOT be null");
        }

        this.schema = schema;
        this.name = name;
        this.domain = domain;
    }

    /**
     * Format:
     * shema:name@domain
     *
     * @param string
     * @return
     */
    public static UserInfo valueOf(final String input) {
        String schema;
        String name;
        String domain;

        Matcher inputMatcher = INPUT_PATTERN.matcher(input);

        final String errorMessage = String.format(ERROR_MESSAGE_FORMAT_STRING, input, ALLOWED_INPUT_FORMAT);

        if (!inputMatcher.matches()) {
            throw new IllegalArgumentException(errorMessage);
        }
        //schema
        String[] exprs = input.split(":", 2);
        String fullName;
        if (exprs.length > 1) {
            schema = exprs[0].trim().equalsIgnoreCase("") ? null : exprs[0];
            fullName = exprs[1].trim().equalsIgnoreCase("") ? null : exprs[1];
        }
        else {
            schema = null;
            fullName = input;
        }

        if (fullName == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        exprs = fullName.split("@", 2);
        if (exprs.length > 1) {
            name = exprs[0].trim().equalsIgnoreCase("") ? null : exprs[0];
            domain = exprs[1].trim().equalsIgnoreCase("") ? null : exprs[1];
        }
        else {
            domain = null;
            name = fullName;
        }


        if (name == null && domain == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        return new UserInfo(schema, name, domain);
    }

    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    /**
     * @return <name>@<domain>
     */
    public String toFullName() {
        return calcFullName();
    }

    /**
     * @return <schema>:<name>@<domain>
     */
    public String toUri() {
        final String fullName = calcFullName();
        if (isEmpty(schema)) {
            return fullName;
        }
        return String.format("%s:%s", schema, fullName);
    }

    /**
     * @return <schema>:<domain>
     */
    public String toDomainUri() {

        if (domain == null) {
            throw new IllegalStateException("domain cannot be null");
        }

        final String fullName = domain;
        return schema == null ? fullName : String.format("%s:%s", schema, fullName);
    }

    private String calcFullName() {
        final String retValue;
        if (!isEmpty(name) && !isEmpty(domain)) {
            retValue = String.format("%s@%s", name, domain);
        }
        else {
            retValue = isEmpty(name) ? domain : name;
        }
        return retValue.replaceAll("@$", "");
    }

    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }


    
    public String toString() {
        return "UserInfo [domain=" + domain + ", name=" + name
                + ", prefix=" + schema + "]";
    }

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserInfo other = (UserInfo) obj;
        if (domain == null) {
            if (other.domain != null) {
                return false;
            }
        }
        else if (!domain.equals(other.domain)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        }
        else if (!schema.equals(other.schema)) {
            return false;
        }
        return true;
    }

    /*  public static void main(String[] args) {
        Logger.log(UserInfo.valueOf(":").toString());

        String st1 = "tel:+qa1005@demo.movial.com";
        String st2 = "a";
        String st3 = "sip:qa1008*#@demo.movial.com";


    }*/
}
