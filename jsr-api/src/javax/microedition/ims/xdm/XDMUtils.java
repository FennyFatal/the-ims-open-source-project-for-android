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

import com.android.ims.xdm.URIListImpl;
import com.android.ims.xdm.XCAPErrorImpl;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.microedition.ims.android.xdm.IUniquenessError;
import javax.microedition.ims.android.xdm.IXCAPError;
import javax.microedition.ims.android.xdm.IXCAPException;
import javax.microedition.ims.android.xdm.IXCAPRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public final class XDMUtils {
    // TODO retrieve xcapRoot
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private XDMUtils() {
        assert false;
    }
    
    public static IXCAPRequest createIXCAPRequest(XCAPRequest request, String xcapRoot) {
        request.prepare();
        return new IXCAPRequest(request.getHttpMethod(), request
                .createRequestURI(xcapRoot), request.getMessageBody(), request
                .getHeaders());
    }

    public static XCAPException createXCAPException(IXCAPException iException) {
        XCAPError xcapError = null;

        if (iException.getXcapError() != null) {
            xcapError = createXCAPError(iException.getXcapError());
        }

        return new XCAPException(iException.getStatusCode(),
                iException.getReasonPhrase(), xcapError);
    }

    public static XCAPError createXCAPError(IXCAPError iXCAPError) {

        UniquenessError[] uniquenessErrors = null;
        if (iXCAPError.getUniquenessErrors() != null) {
            int length = iXCAPError.getUniquenessErrors().length;
            uniquenessErrors = new UniquenessError[length];
            for (int i = 0; i < length; i++) {
                IUniquenessError iError = iXCAPError.getUniquenessErrors()[i];
                uniquenessErrors[i] = new UniquenessError(iError
                        .getAlternativeValues(), iError.getField());
            }
        }

        return new XCAPErrorImpl(iXCAPError.getClosestAncestor(), iXCAPError
                .getExtensionContent(), uniquenessErrors, iXCAPError
                .getxCAPErrorPhrase(), iXCAPError.getxCAPErrorType());
    }
    
    public static String readToString(InputStream is) throws IOException{
        StringBuffer stringBuffer = new StringBuffer();
        
        byte[] buffer = new byte[1024];
        int readed = -1;
        while((readed = is.read(buffer)) > -1) {
            stringBuffer.append(new String(buffer, 0, readed));
        }
        
        is.close();
        
        return stringBuffer.toString();
    }
    
    public static void writeXmlToStream(final String xml, final OutputStream os) throws IOException{
        os.write(xml.getBytes());
    }
    
    public static String getXml(Node node) { 
        if (node == null) { 
            return ""; 
        }
        
        StringBuilder buffer = new StringBuilder(); 
        if (node instanceof Document) { 
            //buffer.append(getXml(((CustomDocument)node).getFirstChild())); 
            buffer.append(getXml(((Document)node).getFirstChild()));
        } else if (node instanceof Element) {
            Element element = (Element)node; 
            buffer.append("<"); 
            buffer.append(element.getNodeName());
            
            if (element.hasAttributes()) { 
                NamedNodeMap map = element.getAttributes(); 
                for (int i = 0; i < map.getLength(); i++) { 
                    Node attr = map.item(i);
                    buffer.append(" ");
                    buffer.append(attr.getNodeName()); 
                    buffer.append("=\""); 
                    buffer.append(attr.getNodeValue()); 
                    buffer.append("\""); 
                } 
            } 
            buffer.append(">");
            
            NodeList children = element.getChildNodes(); 
            for (int i = 0; i < children.getLength(); i++) { 
                buffer.append(getXml(children.item(i))); 
            } 
            buffer.append("</"); 
            buffer.append(element.getNodeName()); 
            buffer.append(">"); 
        } else if (node != null && node.getNodeValue() != null) { 
             buffer.append(node.getNodeValue()); 
        } 
        return buffer.toString(); 
    }
    
    public static String getStringFromNode(Node root) throws IOException {
        StringBuilder result = new StringBuilder();
        if(root.getNodeType() == 3)
            result.append(root.getNodeValue());
        else{
            if (root.getNodeType() != 9){
                result.append("<").append(root.getNodeName()).append(">");
            }else{
                result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            }
            NodeList nodes = root.getChildNodes();
            for(int i=0,j=nodes.getLength();i<j;i++){
                Node node = nodes.item(i);
                result.append(getStringFromNode(node));
            }
            if (root.getNodeType() != 9){
                result.append("</").append(root.getNodeName()).append(">");
            }
        }
        return result.toString();
    }
    
    public static final void main(String[] args) throws ParserConfigurationException, IOException, SAXException{
        final String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <presence xmlns=\"urn:ietf:params:xml:ns:pidf\" xmlns:im=\"urn:ietf:params:xml:ns:pidf:im\" entity=\"pres:someone@example.com\"><tuple id=\"bs35r9\"><status><im:ddd>busy</im:ddd></status></tuple></presence>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document document = builder.parse(new ByteArrayInputStream(testXml.getBytes()));
        String result = getXml(document);
        //String result = getStringFromNode(document);
        System.out.println(result);
    }
    

    public static class RlsServicesCreator {
        
        public static String presenceDocToXml(List<PresenceList> presenceLists, final String nameSpace) {
            StringBuilder retValue = new StringBuilder();
            
            retValue.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            retValue.append(LINE_SEPARATOR).append("<rs:rls-services xmlns:rs=\"" + nameSpace + "\">");
            
            if (presenceLists != null && !presenceLists.isEmpty()) {
                for (PresenceList presenceList : presenceLists) {
                    String presenceListRepresentation = presenceListToXml(presenceList, null);
                    retValue.append(LINE_SEPARATOR).
                        append(presenceListRepresentation);
                }
            }
            
            retValue.append(LINE_SEPARATOR).append("</rs:rls-services>");
            
            return retValue.toString();
        }
        
        /**
         *  <service uri="sip:movial11@dummy.com;pres-list=rcs">
         *  <resource-list>http://siptest.dummy.com:8080/services/resource-lists/users/sip:movial11@dummy.com/index/~~/resource-lists/list%5b@name=%22rcs%22%5d</resource-list>
         *  </service>  
         * @param presenceList
         * @return
         */
        public static String presenceListToXml(final PresenceList presenceList, final String nameSpace) {
            StringBuilder retValue = new StringBuilder();
            
            if (nameSpace != null) {
                retValue.append(String.format("<rs:service xmlns:rs=\"" + nameSpace + "\" uri=\"%s\">", presenceList.getServiceURI()));
            } else {
                retValue.append(String.format("<rs:service uri=\"%s\">", presenceList.getServiceURI()));
            }

            retValue.append(LINE_SEPARATOR).
                append(String.format("<rs:resource-list>%s</rs:resource-list>", presenceList.getURIListReference()));
                
            retValue.append(LINE_SEPARATOR).append("<rs:packages>");
            retValue.append(LINE_SEPARATOR).append(String.format("<rs:package>%s</rs:package>", "presence"));
            retValue.append(LINE_SEPARATOR).append("</rs:packages>");
            
            retValue.append(LINE_SEPARATOR).append("</rs:service>");
            
            return retValue.toString();
        }

    }
    
    
    public static class RuleSetCreator {
        
        public static String presenceAuthorizationDocToXml(final List<PresenceAuthorizationRule> presenceAuthorizationRules,
                final String nameSpaceCr, final String nameSpaceOp, final String nameSpacePr, final String nameSpaceOcp) {
            StringBuilder retValue = new StringBuilder();
            
            retValue.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            retValue.append(LINE_SEPARATOR).
                append("<cr:ruleset xmlns=\"" + nameSpaceCr + "\" xmlns:cr=\"" + nameSpaceCr
                    + "\" xmlns:op=\"" + nameSpaceOp + "\" xmlns:pr=\"" + nameSpacePr + "\" xmlns:ocp=\"" + nameSpaceOcp + "\">");
            
            if (presenceAuthorizationRules != null && !presenceAuthorizationRules.isEmpty()) {
                for (PresenceAuthorizationRule presenceAuthorizationRule : presenceAuthorizationRules) {
                    String presenceAuthorizationRuleRepresentation
                        = presenceAuthorizationRuleToXml(presenceAuthorizationRule, null, null, null, null);
                    retValue.append(LINE_SEPARATOR).
                        append(presenceAuthorizationRuleRepresentation);
                }
            }
            
            retValue.append(LINE_SEPARATOR).append("</cr:ruleset>");

            return retValue.toString();
        }
        
        public static String presenceAuthorizationRuleToXml(PresenceAuthorizationRule rule,
                final String nameSpaceCr, final String nameSpaceOp, final String nameSpacePr, final String nameSpaceOcp) {
            
            StringBuilder retValue = new StringBuilder();
            
            retValue.append(String.format("<cr:rule id=\"%s\" ", rule.getRuleId()));
            if (nameSpaceCr != null) {
                retValue.append("xmlns:cr=\"" + nameSpaceCr + "\" ");
            }
            if (nameSpaceOp != null) {
                retValue.append("xmlns:op=\"" + nameSpaceOp + "\" ");
            }
            if (nameSpacePr != null) {
                retValue.append("xmlns:pr=\"" + nameSpacePr + "\" ");
            }
            if (nameSpaceOcp != null) {
                retValue.append("xmlns:ocp=\"" + nameSpaceOcp + "\" ");
            }
            retValue.append(">");
            
            /*
             * conditions section
             */
            retValue.append(LINE_SEPARATOR).append("<cr:conditions>");
            if (rule.isConditionURIList()) {
                retValue.append(LINE_SEPARATOR).append(String.format("<op:external-list uri=\"%s\" />", rule.getURIListReference()));
                
            } else if (rule.isConditionOtherIdentity()) {
                retValue.append(LINE_SEPARATOR).append("<ocp:other-identity/>");
                
            } else if (rule.isConditionIdentity()) {
                retValue.append(LINE_SEPARATOR).append("<cr:identity>");
                Identity identity = rule.getIdentity();
                switch (identity.getIdentityType()) {
                    case Identity.IDENTITY_SINGLE_USERS:
                        for (String identityId : identity.getIdentityList()) {
                            retValue.append(LINE_SEPARATOR)
                                .append(String.format("<cr:one id=\"%s\" />", identityId));
                        }
                        break;
                    case Identity.IDENTITY_ALL:
                        retValue.append(LINE_SEPARATOR).append("<cr:many/>");
                        break;
                    case Identity.IDENTITY_ALL_EXCEPT:
                        retValue.append(LINE_SEPARATOR).append("<cr:many>");
                        for (String identityId : identity.getIdentityList()) {
                            retValue.append(LINE_SEPARATOR)
                                .append(String.format("<cr:except id=\"%s\" />", identityId));
                        }
                        retValue.append(LINE_SEPARATOR).append("</cr:many>");
                        break;
                    case Identity.IDENTITY_DOMAIN_EXCEPT:
                        retValue.append(LINE_SEPARATOR).append(String.format("<cr:many domain=\"%s\" />", identity.getAllowedDomain()));
                        for (String identityId : identity.getIdentityList()) {
                            retValue.append(LINE_SEPARATOR)
                                .append(String.format("<cr:except id=\"%s\" />", identityId));
                        }
                        retValue.append(LINE_SEPARATOR).append("</cr:many>");
                        break;
                }
                retValue.append(LINE_SEPARATOR).append("</cr:identity>");
                
            } else if (rule.isConditionAnonymousRequest()) {
                retValue.append(LINE_SEPARATOR).append("<ocp:anonymous-request/>");
            }
            retValue.append(LINE_SEPARATOR).append("</cr:conditions>");

            /*
             * actions section
             */
            retValue.append(LINE_SEPARATOR).append("<cr:actions>");
            retValue.append(LINE_SEPARATOR).append(String.format("<pr:sub-handling>%s</pr:sub-handling>", getRuleActionStr(rule)));
            retValue.append(LINE_SEPARATOR).append("</cr:actions>");
            
            PresenceContentFilter presenceContentFilter = rule.getPresenceContentFilter();
            if(presenceContentFilter != null) {
                /*
                 * transformations section
                 */
                retValue.append(LINE_SEPARATOR).append("<cr:transformations>");
                
                
                if (presenceContentFilter.isAllPersonComponentsProvided()) {
                    retValue.append(LINE_SEPARATOR).append("<pr:provide-persons>");
                    retValue.append(LINE_SEPARATOR).append("<pr:all-persons/>");
                    retValue.append(LINE_SEPARATOR).append("</pr:provide-persons>");
                } else {
                    String[] providedPersons = presenceContentFilter.getProvidedPersons();
                    if (providedPersons != null && providedPersons.length > 0) {
                        retValue.append(LINE_SEPARATOR).append("<pr:provide-persons>");
                        for (String person : providedPersons) {
                            retValue.append(LINE_SEPARATOR).
                                append(String.format("<pr:class>%s</pr:class>", person));
                        }
                        retValue.append(LINE_SEPARATOR).append("</pr:provide-persons>");
                    }
                }
                
                if (presenceContentFilter.isAllServiceComponentsProvided()) {
                    retValue.append(LINE_SEPARATOR).append("<pr:provide-services>");
                    retValue.append(LINE_SEPARATOR).append("<pr:all-services/>");
                    retValue.append(LINE_SEPARATOR).append("</pr:provide-services>");
                } else {
                    retValue.append(LINE_SEPARATOR).append("<pr:provide-services>");
                    
                    String[] providedServicesClasses
                        = presenceContentFilter.getProvidedServices(PresenceContentFilter.COMPONENT_PERMISSION_CLASSIFICATION);
                    if (providedServicesClasses != null && providedServicesClasses.length > 0) {
                        for (String psClass : providedServicesClasses) {
                            retValue.append(LINE_SEPARATOR).
                                append(String.format("<pr:class>%s</pr:class>", psClass));
                        }
                    }
                    
                    String[] providedServicesIds
                        = presenceContentFilter.getProvidedServices(PresenceContentFilter.COMPONENT_PERMISSION_SERVICE_ID);
                    if (providedServicesIds != null && providedServicesIds.length > 0) {
                        for (String psId : providedServicesIds) {
                            retValue.append(LINE_SEPARATOR).
                                append(String.format("<pr:occurrence-id>%s</pr:occurrence-id>", psId));
                        }
                    }
                    
                    String[] providedServicesUris
                        = presenceContentFilter.getProvidedServices(PresenceContentFilter.COMPONENT_PERMISSION_SERVICE_URI);
                    if (providedServicesUris != null && providedServicesUris.length > 0) {
                        for (String psUri : providedServicesUris) {
                            retValue.append(LINE_SEPARATOR).
                                append(String.format("<pr:service-uri>%s</pr:service-uri>", psUri));
                        }
                    }
                            
                    String[] providedServicesSchemes
                        = presenceContentFilter.getProvidedServices(PresenceContentFilter.COMPONENT_PERMISSION_SERVICE_URI_SCHEME);
                    if (providedServicesSchemes != null && providedServicesSchemes.length > 0) {
                        for (String psScheme : providedServicesUris) {
                            retValue.append(LINE_SEPARATOR).
                                append(String.format("<pr:service-uri-scheme>%s</pr:service-uri-scheme>", psScheme));
                        }
                    }
                    
                    retValue.append(LINE_SEPARATOR).append("</pr:provide-services>");
                }
                
                if (presenceContentFilter.isAllDeviceComponentsProvided()) {
                    retValue.append(LINE_SEPARATOR).append("<pr:provide-devices>");
                    retValue.append(LINE_SEPARATOR).append("<pr:all-devices/>");
                    retValue.append(LINE_SEPARATOR).append("</pr:provide-devices>");
                } else {
                    retValue.append(LINE_SEPARATOR).append("<pr:provide-devices>");
                    
                    String[] providedDevicesClasses
                        = presenceContentFilter.getProvidedDevices(PresenceContentFilter.COMPONENT_PERMISSION_CLASSIFICATION);
                    if (providedDevicesClasses != null && providedDevicesClasses.length > 0) {
                        for (String pdClass : providedDevicesClasses) {
                            retValue.append(LINE_SEPARATOR).
                                append(String.format("<pr:class>%s</pr:class>", pdClass));
                        }
                    }
                    
                    String[] providedDevicesIds
                        = presenceContentFilter.getProvidedDevices(PresenceContentFilter.COMPONENT_PERMISSION_DEVICE_ID);
                    if (providedDevicesIds != null && providedDevicesIds.length > 0) {
                        for (String pdId : providedDevicesIds) {
                            retValue.append(LINE_SEPARATOR).
                                append(String.format("<pr:deviceID>%s</pr:deviceID>", pdId));
                        }
                    }

                    retValue.append(LINE_SEPARATOR).append("</pr:provide-devices>");
                }

                if (presenceContentFilter.isAllElementsProvided()) {
                    retValue.append(LINE_SEPARATOR).append("<pr:provide-all-attributes/>");
                } else {
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_ACTIVITIES)) {
                        retValue.append(LINE_SEPARATOR).append("<pr:provide-activities>true</pr:provide-activities>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_BARRING_STATE)) {
                        retValue.append(LINE_SEPARATOR).append("<op:provide-barring-state>true</op:provide-barring-state>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_CLASSIFICATION)) {
                        retValue.append(LINE_SEPARATOR).append("<pr:provide-class>true</pr:provide-class>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_FREE_TEXT)) {
                        retValue.append(LINE_SEPARATOR).append("<pr:provide-note>true</pr:provide-note>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_GEOGRAPHICAL_LOCATION_INFO)) {
                        retValue.append(LINE_SEPARATOR).append("<op:provide-geopriv>full</op:provide-geopriv>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_MOOD)) {
                        retValue.append(LINE_SEPARATOR).append("<pr:provide-mood>true</pr:provide-mood>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_NETWORK_AVAILABILITY)) {
                        retValue.append(LINE_SEPARATOR).append("<op:provide-network-availability>true</op:provide-network-availability>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_PLACE_TYPES)) {
                        retValue.append(LINE_SEPARATOR).append("<pr:provide-place-type>true</pr:provide-place-type>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_REGISTRATION_STATE)) {
                        retValue.append(LINE_SEPARATOR).append("<op:provide-registration-state>true</op:provide-registration-state>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_SESSION_PARTICIPATION)) {
                        retValue.append(LINE_SEPARATOR).append("<op:provide-session-participation>true</op:provide-session-participation>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_STATUS_ICON)) {
                        retValue.append(LINE_SEPARATOR).append("<pr:provide-status-icon>true</pr:provide-status-icon>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_TIME_OFFSET)) {
                        retValue.append(LINE_SEPARATOR).append("<pr:provide-time-offset>true</pr:provide-time-offset>");
                    }
                    if (presenceContentFilter.isElementProvided(PresenceContentFilter.ELEMENT_WILLINGNESS)) {
                        retValue.append(LINE_SEPARATOR).append("<op:provide-willingness>true</op:provide-willingness>");
                    }
                }
                
                retValue.append(LINE_SEPARATOR).append("</cr:transformations>");                
            }
            
            retValue.append(LINE_SEPARATOR).append("</cr:rule>");

            return retValue.toString();
        }
          
        private static String getRuleActionStr(PresenceAuthorizationRule rule) {
            switch (rule.getAction()) {
                case PresenceAuthorizationRule.ACTION_ALLOW:
                    return "allow";
                case PresenceAuthorizationRule.ACTION_BLOCK:
                    return "block";
                case PresenceAuthorizationRule.ACTION_CONFIRM:
                    return "confirm";
                case PresenceAuthorizationRule.ACTION_POLITE_BLOCK:
                    return "polite-block";
            }
            return null;
        }

    }
    
    
    public static class ResourceListsCreator {
        
        public static String uriDocToXml(final List<URIListImpl> uriLists, final String nameSpace) {
            StringBuilder retValue = new StringBuilder();
            
            retValue.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            retValue.append(LINE_SEPARATOR).append("<rl:resource-lists xmlns:rl=\""+ nameSpace + "\">");
            
            if (uriLists != null && !uriLists.isEmpty()) {
                for (URIList uriList : uriLists) {
                    String uriListRepresentation
                        = uriListToXml(uriList.getListName(), uriList.getDisplayName(), uriList.getListEntries(), null);
                    retValue.append(LINE_SEPARATOR).
                        append(uriListRepresentation);
                }
            }
            
            retValue.append(LINE_SEPARATOR).append("</rl:resource-lists>");

            return retValue.toString();
        }
        
        /**
         * <list name="phbk">
         *      <display-name>phbk</display-name>
         *      <gid xmlns="voxmobili.xcap.dblink">119851</gid>
         *       <entry uri="tel:19728881041">
         *         <display-name>T 1041</display-name>
         *       </entry>
         * </list>
         */
        public static String uriListToXml(String name, String displayName,
                ListEntry[] listEntries, final String nameSpace) {
            StringBuilder retValue = new StringBuilder();
            
            if (nameSpace != null) {
                retValue.append(String.format("<rl:list xmlns:rl=\""+ nameSpace + "\" name=\"%s\">", name));
            } else {
                retValue.append(String.format("<rl:list name=\"%s\">", name));
            }
            
            if(displayName != null) {
                retValue.append(LINE_SEPARATOR).
                    append(String.format("<rl:display-name>%s</rl:display-name>", displayName));
            } 
                
            if (listEntries != null && listEntries.length > 0) {
                for(ListEntry listEntry : listEntries) {
                    String listEntryRepresentation = listEntryToXml(listEntry, null);
                    retValue.append(LINE_SEPARATOR).
                        append(listEntryRepresentation);
                }
            }
            
            retValue.append(LINE_SEPARATOR).append("</rl:list>");
            
            return retValue.toString();
        }
        
        /**
         * <entry uri="tel:19728881041">
         *     <display-name>T 1041</display-name>
         * </entry>
         * 
         * or
         * 
         * <external anchor="http://xdms.ims.dummy.com:8080/services/resource-lists/users/sip:movial5@dummy.com/index/~~/resource-lists/list%5b@name=%22rcs%22%5d" >
         *  <display-name>T 1041</display-name>
         * </external> 
         * 
         */
        public static String listEntryToXml(final ListEntry listEntry, final String nameSpace) {
            StringBuilder retValue = new StringBuilder();
            
            if(listEntry.getType() == ListEntry.URI_ENTRY) {
                if (nameSpace != null) {
                    retValue.append(String.format("<rl:entry xmlns:rl=\""+ nameSpace + "\" uri=\"%s\">", listEntry.getUri()));    
                } else {
                    retValue.append(String.format("<rl:entry uri=\"%s\">", listEntry.getUri()));
                }
            } else if(listEntry.getType() == ListEntry.URI_LIST_ENTRY){
                if (nameSpace != null) {
                    retValue.append(String.format("<rl:external xmlns:rl=\""+ nameSpace + "\" anchor=\"%s\">", listEntry.getUri()));
                } else {
                    retValue.append(String.format("<rl:external anchor=\"%s\">", listEntry.getUri()));
                }
            }
            
            
            if(listEntry.getDisplayName() != null) {
                retValue.append(LINE_SEPARATOR).
                    append(String.format("<rl:display-name>%s</rl:display-name>", listEntry.getDisplayName()));
            }
            
            if(listEntry.getType() == ListEntry.URI_ENTRY) {
                retValue.append(LINE_SEPARATOR).append("</rl:entry>");    
            } else if(listEntry.getType() == ListEntry.URI_LIST_ENTRY){
                retValue.append(LINE_SEPARATOR).append("</rl:external>");
            }
            
            return retValue.toString();
        }
    }
    
}
