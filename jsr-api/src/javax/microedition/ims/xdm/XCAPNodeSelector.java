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

import com.android.ims.xdm.XCAPURLEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an XCAP node selector. An XCAP node selector is part of an XCAP
 * request, see the XCAPRequest class for details.
 * 
 * To use this class, some knowledge of XCAP node selectors as described in
 * [RFC4825] is required.
 * 
 * A node selector consists of a number of steps, see [RFC4825]. When an XCAP
 * server evaluates a node selector and matches it against an XML document, each
 * step of the selector will be examined in turn given the current document
 * context. When starting to evaluate the selector, the document context is the
 * root of the document. After each step of the selector, the document context
 * is updated, moving down in the document tree. This will traverse the XML
 * document from the root to exactly one of its nodes.
 * 
 * When building an XCAPNodeSelector a similar process is used. There is a
 * number of methods in this class that add a step to the node selector. The
 * steps are added one at a time in a process analogous to the server's
 * evaluation.
 * 
 * For example, the first step of the node selector should select the root
 * element of the document. If the root element is called watcherinfo, such a
 * step can be added to the selector using selectElementByName("watcherinfo").
 * Other select methods exist for selecting elements based on name, position,
 * and/or attribute.
 * 
 * @author Andrei Khomushko
 * 
 */
public class XCAPNodeSelector {
    private String nodeSelectorString;
    private final List<SelectorStep> selectorSteps = new ArrayList<SelectorStep>();
    
    enum SelectorType {
        ELEMENT, ATTRIBUTE, UNKNOWN
    }
    
    public XCAPNodeSelector() {
    }

    /**
     * Creates an XCAP node selector from a string.
     * 
     * The string should be a valid XCAP node selector. Note that the select
     * methods in this class can be used to add more steps to the node selector.
     * 
     * When using this constructor, the user is responsible for percent encoding
     * the nodeSelectorString argument if necessary, according to the rules in
     * [RFC4825].
     * 
     * @param nodeSelectorString
     *            - the node selector as a string
     * @throws IllegalArgumentException
     *             - if the nodeSelectorString is null
     */
    public XCAPNodeSelector(String nodeSelectorString) {
        if(nodeSelectorString == null) {
            throw new IllegalArgumentException("nodeSelectorString is null");
        }
        
        this.nodeSelectorString = nodeSelectorString;
    }

    /**
     * Adds a step to the selector that selects an element with a particular
     * name.
     * 
     * Note that each step can only select a single node. The request will fail
     * if multiple elements match the name.
     * 
     * @param elementName
     *            - the name of the element, or "*" to select any element
     * @return this XCAPNodeSelector with the new selector step added
     * @throws IllegalArgumentException
     *             - if the elementName argument is null
     */
    public XCAPNodeSelector selectElementByName(String elementName) {
        if (elementName == null) {
            throw new IllegalArgumentException(
                    "the elementName argument is null");
        }
        selectorSteps.add(new ElementByNameSelectorStep(elementName));
        return this;
    }

    /**
     * Adds a step to the selector that, given all elements with a particular
     * name, selects one of them based on position.
     * 
     * Note the order of selection: First, all elements whose name do not match
     * are excluded. Next, the element with a particular position among the
     * remaining elements is chosen.
     * 
     * Numbering follows the XCAP standard: The first element has position 1,
     * not 0.
     * 
     * @param elementName
     *            - the name of the element, "*" to select any element
     * @param position
     *            - the position of the element, where the first element has
     *            position 1
     * @return this XCAPNodeSelector with the new selector step added
     * @throws IllegalArgumentException
     *             - if the elementName argument is null
     * @throws IllegalArgumentException
     *             - if the position argument is less than one
     */
    public XCAPNodeSelector selectElementByPosition(String elementName,
            int position) {
        if (elementName == null) {
            throw new IllegalArgumentException("elementName argument is null");
        }

        if (position < 1) {
            throw new IllegalArgumentException(
                    "the position argument is less than one");
        }
        
        selectorSteps.add(new ElementByPositionSelectorStep(elementName, position));
        return this;
    }

    /**
     * Adds a step to the selector that selects an element with a particular
     * name and an attribute with a particular value.
     * 
     * Note that each step can only select a single node. The request will fail
     * if multiple elements match the name and attribute.
     * 
     * @param elementName
     *            - the name of the element, or "*" to select any element
     * @param attributeName
     *            - the name of the attribute, or "*" to select any attribute
     * @param value
     *            - the value of the attribute
     * @return this XCAPNodeSelector with the new selector step added
     * @throws IllegalArgumentException
     *             - if the elementName argument, the attributeName argument, or
     *             the value argument is null
     */
    public XCAPNodeSelector selectElementByAttribute(String elementName,
            String attributeName, String value) {
        if (elementName == null) {
            throw new IllegalArgumentException("elementName is null");
        }

        if (attributeName == null) {
            throw new IllegalArgumentException("attributeName is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        selectorSteps.add(new ElementByAttributeSelectorStep(elementName, attributeName, value));
        return this;
    }

    /**
     * Adds a step to the selector that, given all elements with a particular
     * name that has an attribute with a particular value, selects one of them
     * based on position.
     * 
     * Note the order of selection: First, all elements whose name do not match
     * are excluded. Next, all elements where the attribute does not match are
     * excluded. Finally, the element with a particular position among the
     * remaining elements is chosen.
     * 
     * Numbering follows the XCAP standard: The first element has position 1,
     * not 0.
     * 
     * @param elementName
     *            - the name of the element, or "*" to select any element
     * @param attributeName
     *            - the name of the attribute, or "*" to select any attribute
     * @param value
     *            - the value of the attribute
     * @param position
     *            - the position of the element, where the first element has
     *            position 1
     * @return this XCAPNodeSelector with the new selector step added
     * @throws IllegalArgumentException
     *             - if the elementName argument, the attributeName argument, or
     *             the value argument is null
     * @throws IllegalArgumentException
     *             - if the position argument is less than one
     */
    public XCAPNodeSelector selectElementByAttributeAndThenPosition(
            String elementName, String attributeName, String value, int position) {
        if (elementName == null) {
            throw new IllegalArgumentException("elementName is null");
        }

        if (attributeName == null) {
            throw new IllegalArgumentException("attributeName is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        if (position < 1) {
            throw new IllegalArgumentException(
                    "position argument is less than one");
        }
        selectorSteps.add(new ElementByAttributeAndThenPositionSelectorStep(elementName, attributeName, value, position));
        return this;
    }

    /**
     * Adds a step to the selector that selects the element with a particular
     * name and a particular position if and only if it has an attribute with a
     * particular value.
     * 
     * Note the order of selection: First, all elements whose name do not match
     * are excluded. Next, the element with a particular position among the
     * remaining elements is chosen. Finally, the value of the attribute is
     * checked, and if it does not match, there is no match.
     * 
     * Numbering follows the XCAP standard: The first element has position 1,
     * not 0.
     * 
     * @param elementName
     *            - the name of the element, or "*" to select any element
     * @param position
     *            - the position of the element, where the first element has
     *            position 1
     * @param attributeName
     *            - the name of the attribute, or "*" to select any attribute
     * @param value
     *            - the value of the attribute
     * @return this XCAPNodeSelector with the new selector step added
     * @throws IllegalArgumentException
     *             - if the elementName argument, the attributeName argument, or
     *             the value argument is null
     * @throws IllegalArgumentException
     *             - if the position argument is less than one
     */
    public XCAPNodeSelector selectElementByPositionAndThenAttribute(
            String elementName, int position, String attributeName, String value) {
        if (elementName == null) {
            throw new IllegalArgumentException("elementName is null");
        }

        if (attributeName == null) {
            throw new IllegalArgumentException("attributeName is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        if (position < 1) {
            throw new IllegalArgumentException(
                    "position argument is less than one");
        }

        selectorSteps.add(new ElementByPositionAndThenAttribute(elementName, position, attributeName, value));
        return this;
    }

    /**
     * Adds a step to the selector that selects an attribute based on its name.
     * 
     * The attribute selector must always be the last step in the node selector.
     * Adding more steps after this will result in an invalid node selector.
     * 
     * @param attributeName
     *            - the name of the attribute
     * @return this XCAPNodeSelector with the new selector step added
     * @throws IllegalArgumentException
     *             - if the attributeName argument is null
     */
    public XCAPNodeSelector selectAttribute(String attributeName) {
        if (attributeName == null) {
            throw new IllegalArgumentException("attributeName is null");
        }

        selectorSteps.add(new AttributeSelectorStep(attributeName));
        return this;
    }
    
    /**
     * Converts the node selector to a node selector string.
     * 
     * The node selector will be percent encoded where needed as described in
     * [RFC4825] so that it can be used in an XCAP request URI.
     * 
     * @return a node selector string
     */
    
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        if(nodeSelectorString != null) {
            retValue.append(nodeSelectorString);
            
            if(selectorSteps.size() > 0) {
                retValue.append("/");
            }
        }
        
        for(SelectorStep selectorStep: selectorSteps) {
            retValue.append(selectorStep.build()).append("/");    
        }
        
        if(selectorSteps.size() > 0) {
            retValue.deleteCharAt(retValue.length() - 1);
        }

        return retValue.toString();
    }

    interface SelectorStep {
        /** The node selector will be percent encoded where needed as described in [RFC4825] */
        String build();
    }
    
    class AttributeSelectorStep implements SelectorStep {
        private final String attributeName;
        
        public AttributeSelectorStep(String attributeName) {
            this.attributeName = attributeName;
        }

        
        public String build() {
            return XCAPURLEncoder.encode(String.format("@%s", attributeName));
        }
    }
    
    class ElementByAttributeSelectorStep implements SelectorStep {
        private final String elementName;
        private final String attributeName;
        private final String value;    
        
        public ElementByAttributeSelectorStep(String elementName,
                String attributeName, String value) {
            this.elementName = elementName;
            this.attributeName = attributeName;
            this.value = value;
        }

        
        public String build() {
            return XCAPURLEncoder.encode(String.format("%s[@%s=\"%s\"]", elementName, attributeName, value));
        }
    }
    
    class ElementByAttributeAndThenPositionSelectorStep extends ElementByAttributeSelectorStep {
        private int position;

        public ElementByAttributeAndThenPositionSelectorStep(
                String elementName, String attributeName, String value,
                int position) {
            super(elementName, attributeName, value);
            this.position = position;
        }

        
        public String build() {
            String retValue = super.build();
            retValue += XCAPURLEncoder.encode(String.format("[%s]", position));
            return retValue;
        }
    }
    
    class ElementByNameSelectorStep implements SelectorStep {
        private final String elementName;
        
        public ElementByNameSelectorStep(String elementName) {
            this.elementName = elementName;
        }

        
        public String build() {
            return elementName;
        }
    }
    
    class ElementByPositionSelectorStep extends ElementByNameSelectorStep {
        private  int position;
        
        public ElementByPositionSelectorStep(String elementName, int position) {
            super(elementName);
            this.position = position;
        }

        
        public String build() {
            String retValue = super.build();
            retValue += XCAPURLEncoder.encode(String.format("[%s]", position));
            return retValue;
        }
    }
    
    class ElementByPositionAndThenAttribute extends ElementByPositionSelectorStep {
        private String attributeName;
        private String value;
        
        public ElementByPositionAndThenAttribute(String elementName,
                int position, String attributeName, String value) {
            super(elementName, position);
            this.attributeName = attributeName;
            this.value = value;
        }

        
        public String build() {
            String retValue = super.build() ;
            retValue += XCAPURLEncoder.encode(String.format("[@%s=\"%s\"]", attributeName, value));
            return retValue;
        }
    }
    
    /**
     * Check this node selector selects attribute or element
     * @return true if this node selector selects attribute 
     */
    SelectorType retrieveSelectorType() {
        SelectorType retValue = SelectorType.UNKNOWN; 
        if(selectorSteps.size() == 0) {
            if(nodeSelectorString != null) {
                int idx = nodeSelectorString.lastIndexOf("/");
                if(idx > -1 && idx + 1 < nodeSelectorString.length()) {
                    if(nodeSelectorString.charAt(idx + 1) == '@') {
                        retValue = SelectorType.ATTRIBUTE;           
                    } else {
                        retValue = SelectorType.ELEMENT;        
                    }
                }
            }
                        
        } else {
            SelectorStep lastStep = selectorSteps.get(selectorSteps.size() - 1);
            if(lastStep instanceof AttributeSelectorStep) {
                retValue = SelectorType.ATTRIBUTE;
            } else {
                retValue = SelectorType.ELEMENT;
            }
        }
        return retValue;
    }
}
