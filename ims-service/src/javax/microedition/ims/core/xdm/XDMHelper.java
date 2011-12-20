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

package javax.microedition.ims.core.xdm;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.microedition.ims.core.xdm.data.AuthRuleIdentity;
import javax.microedition.ims.core.xdm.data.Identity;
import javax.microedition.ims.core.xdm.data.IdentityType;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 29.4.2010
 * Time: 15.40.56
 */
final class XDMHelper {

    static final String SERVICE_NODE_NAME = "service";
    static final String RULE_NODE_NAME = "rule";

    private XDMHelper() {
    }

    static PresenceListData handleServiceNode(Node node) {
        PresenceListData retValue;


        if (node != null) {

            if (!SERVICE_NODE_NAME.equalsIgnoreCase(node.getLocalName())) {
                throw new IllegalArgumentException("Must be '" + SERVICE_NODE_NAME + "' node" + ". Now it has value " + node.getLocalName());
            }

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                final Node rlsNode = element.getElementsByTagNameNS("*", "resource-list").item(0);

                final String uriReference = rlsNode == null ? null : rlsNode.getFirstChild().getNodeValue();
                final String serviceURI = element.getAttribute("uri");

                retValue = new PresenceListDataBean(serviceURI, uriReference);
            }
            else {
                throw new IllegalArgumentException("Node type is " + node.getNodeType() + ". " + Node.ELEMENT_NODE + " is only allowed type.");
            }
        }
        else {
            throw new NullPointerException("node value is " + node + ". Null is not allowed here.");
        }

        return retValue;
    }

    static PresenceAuthorizationRuleData handlePresenceAuthorizationRuleNode(Node node) {
        PresenceAuthorizationRuleData retValue;

        if (node != null) {

            if (!RULE_NODE_NAME.equalsIgnoreCase(node.getLocalName())) {
                throw new IllegalArgumentException("Must be '" + RULE_NODE_NAME + "' node" + ". Now it has value " + node.getLocalName());
            }

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                final String ruleId = element.getAttribute("id");

                String action = null;
                final Node actionsNode = element.getElementsByTagNameNS("*", "actions").item(0);
                if (actionsNode != null) {
                    final Node actionNode = ((Element) actionsNode).getElementsByTagNameNS("*", "sub-handling").item(0);
                    if (actionNode != null) {
                        action = actionNode.getFirstChild().getNodeValue();
                    }
                }


                String conditionURI = null;
                final Node conditionsNode = element.getElementsByTagNameNS("*", "conditions").item(0);
                if (conditionsNode != null) {
                    final Node conditionNode = ((Element) conditionsNode).getElementsByTagNameNS("*", "external-list").item(0);
                    if (conditionNode != null) {
                        conditionURI = ((Element) conditionNode).getAttribute("uri");
                    }
                }


                final NodeList identityNodes = element.getElementsByTagNameNS("*", "identity");
                final Collection<String> identityList = new ArrayList<String>(identityNodes.getLength() * 10);

                Node identityNode;
                for (int i = 0; i < identityNodes.getLength(); i++) {

                    identityNode = identityNodes.item(i);
                    if (identityNode != null) {
                        final NodeList oneNodes = ((Element) identityNode).getElementsByTagNameNS("*", "one");
                        if (oneNodes != null) {
                            for (int j = 0; j < oneNodes.getLength(); j++) {
                                identityList.addAll(collectAttributeValues(oneNodes.item(j), "id"));
                            }
                        }
                    }
                }
                Identity identity = new AuthRuleIdentity(null, identityList, IdentityType.IDENTITY_SINGLE_USERS);


                retValue = new PresenceAuthorizationRuleDataBean(ruleId, action, conditionURI, identity);
                //retValue = new PresenceAuthorizationRuleDataBean(int action, String uriListReference, boolean conditionAnonymousRequest, boolean conditionIdentity, boolean conditionOtherIdentity, boolean conditionURIList);
            }
            else {
                throw new IllegalArgumentException("Node type is " + node.getNodeType() + ". " + Node.ELEMENT_NODE + " is only allowed type.");
            }

        }
        else {
            throw new IllegalArgumentException("Node value is " + node + ". Null is not allowed here.");
        }

        return retValue;
    }

    private static Collection<String> collectAttributeValues(final Node node, final String attrNameToCollect) {

        final NamedNodeMap oneNodeAttrs = node.getAttributes();
        Collection<String> identities = new ArrayList<String>(oneNodeAttrs.getLength() * 2);

        Node attribute;
        String attrName;
        String attrValue;
        for (int j = 0; j < oneNodeAttrs.getLength(); j++) {
            attribute = oneNodeAttrs.item(j);
            attrName = attribute.getNodeName();
            if (attrNameToCollect.equals(attrName)) {
                attrValue = attribute.getNodeValue();
                if (attrValue != null && !attrValue.equals("")) {
                    identities.add(attrValue);
                }
            }
        }

        return identities;
    }

}
