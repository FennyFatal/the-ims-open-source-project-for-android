package javax.microedition.ims.engine.test;


/**
 * Utility class.
 * 
 * @author Andrei Khomushko
 *
 */
public final class UriUtils {
    private static final String TAG = "UriUtils";
    
    private static final String SHEMA_RULES = "\\w+:";
    private static final String USERPART_RULES = "((\\*|#){1}\\d{2,3}(\\*|#){1}(\\d*#)?)|(##\\d{2}#)";
    private static final String DOMAIN_RULES = "@(\\w|\\.|-)+";
    private static final String SIP_ADDRESS_RULES = String.format("<?(%s)(%s)(%s)>?", SHEMA_RULES, USERPART_RULES, DOMAIN_RULES);

    private UriUtils() {
        assert false;
    }

    /**
     * Check is userPart is star-code.
     *
     * Rules for user_party:
     * 1) there is * and two or three numbers and * or # in the beginning
     * 2) there is # and three numbers and # in the beginning  
     *  
     * @param userPart 
     * @return is userPart address is star-code
     */
    private static boolean isUserPartStarCode(String userPart) {
        return userPart.matches(USERPART_RULES);
    }
    
    /**
     * Check is calledParty address is star-code.
     * 
     * Called party format: {shema}:{user_party}@{domain}
     * 
     * Rules for {user_party} see in {@link UriUtils#isUserPartStarCode(String)}
     *  
     * @param calledParty
     * @return is calledParty address is star-code
     */
    public static boolean isUriStarCode(String calledParty) {
        return calledParty.matches(SIP_ADDRESS_RULES);
    }
    
    public static String encodeUri(String uri){          
        StringBuilder ret = new StringBuilder();
        if(!isEmpty(uri)){
            for (int i = 0; i < uri.length(); i++) {
                char c = uri.charAt(i);
                /*                if (c == '#') {                   
                    ret.append('%').append("23");
                } else {
                    ret.append(c);
                }*/

                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                    ret.append(c);
                } else {

                    // also these switched characters are fine, rest we escape.
                    switch (c) {
                    case '@':
                    case '[':
                    case ']':
                    case '/':
                    case ':':
                    case '&':
                    case '+':
                    case '$':
                    case '-':
                    case '_':
                    case '.':
                    case '!':
                    case '~':
                    case '*':
                    case '\'':
                    case '(':
                    case ')':
                        ret.append(c);
                        break;
                    default:
                        if(c == '%' && toNumber(uri.substring(i+1, i+3) , -1, 16) > 0){
                            ret.append('%').append(uri.substring(i+1, i+3));
                            i+=2;
                        }else{
                            ret.append('%').append(Integer.toString((int)c,16));
                        }
                    }
                }
            }
        }
        return ret.toString();
    }
    
    public static String decodeUri(String uri){          
        StringBuilder ret = new StringBuilder();
        if(!isEmpty(uri)){
            for (int i = 0; i < uri.length(); i++) {
                char c = uri.charAt(i);
                /*                if (c == '#') {                   
                    ret.append('%').append("23");
                } else {
                    ret.append(c);
                }*/

                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                    ret.append(c);
                } else {

                    // also these switched characters are fine, rest we escape.
                    switch (c) {
                    case '@':
                    case '[':
                    case ']':
                    case '/':
                    case ':':
                    case '&':
                    case '+':
                    case '$':
                    case '-':
                    case '_':
                    case '.':
                    case '!':
                    case '~':
                    case '*':
                    case '\'':
                    case '(':
                    case ')':
                        ret.append(c);
                        break;
                    default:
                        if(c == '%'){
                            int charCode = toNumber(uri.substring(i+1, i+3) , -1, 16);
                            if(charCode > 0) {
                                ret.append((char)charCode);
                                i+=2;
                            } else {
                                ret.append('%').append(uri.substring(i+1, i+3));    
                            }
                        }else{
                            ret.append(c);
                        }
                    }
                }
            }
        }
        return ret.toString();
    }

    private static boolean isEmpty(String uri) {
        return uri == null || uri.trim().equals("");
    }

    private static int toNumber(String parseMe, int defValue, int radix) {
        int ret = defValue;
        try {
            ret = Integer.parseInt(parseMe, radix);
        } catch (NumberFormatException e) {
            //Logger.i(TAG, "Error parsing int: " + parseMe);
            System.out.println("Error parsing int: " + parseMe);
        }
        return ret;
    }
    
/*    public static void main(String[] args) {
        testStarCodes();
    }
    
    private static void testStarCodes() {
        String[] exprs = new String[] {
            System.out.println(isCalledPartyStarCode(expr));
        }
    }
*/    
    
    
    public static void main(String[] args) {
        
        String url = "<urn:uuid:12345678>";
        String encodeUri = encodeUri(url);
        System.out.println("encodeUri = " + encodeUri);

        String s = "*21*12345678#@dummy.com";
        System.out.println(s);
        System.out.println(isUriStarCode(s));
        System.out.println(encodeUri(s));

    }
}
