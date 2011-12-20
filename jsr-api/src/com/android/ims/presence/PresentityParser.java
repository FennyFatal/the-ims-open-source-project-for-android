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

import android.text.TextUtils;
import com.android.ims.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.microedition.ims.presence.PresenceDocument;
import javax.microedition.ims.presence.Presentity;
import java.io.IOException;

/**
 * This class responsible for parsing Presentity. 
 * 
 * @author Andrei Khomushko
 *
 */
final class PresentityParser extends BaseXMLParser {
    
    private final Document document;
    
    /**
     *                                             Autn
     * @param document
     * @throws IllegalArgumentException - if the document argument is null
     */    
    PresentityParser(Document document) {
        if (document == null) {
            throw new IllegalArgumentException(
                    "The document argument is null");
        }
        this.document = document;
    }

    /**
     * 
     * @param pidfSource
     * @throws IOException - if instance can't be instantiated
     * @throws IllegalArgumentException - if the pidfSource argument is null 
     */
    PresentityParser(String pidfSource) throws IOException{
        if (TextUtils.isEmpty(pidfSource)) {
            throw new IllegalArgumentException(
                    "The pidfSource argument is null");
        }

        //backward capability
        this.document = XMLUtils.createDocument(pidfSource, true);
    }

    Presentity parse() throws IOException {
        final Presentity presentity;
        
        final Element presenceNode;
        //try {
        //    presenceNode = (Node) applyXPAth(document, "presence", XPathConstants.NODE);
        //} catch (XPathExpressionException e1) {
        //    throw new IOException("Can't find retrieve node");
        //} 
        
        NodeList presenceNodeList = document.getElementsByTagNameNS("*", "presence");
        presenceNode = (Element)presenceNodeList.item(0);
        
        if(presenceNode == null) {
            throw new IOException("Can't find presence node");
        }
        
        final String uRI, displayName; 
/*        try{
            uRI = (String) applyXPAth(presenceNode, "@entity", XPathConstants.STRING);
            displayName = (String) applyXPAth(presenceNode, "@display-name", XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new IOException(e.getMessage());
        }
*/        
        uRI = presenceNode.getAttribute("entity");
        displayName = presenceNode.getAttribute("display-name");
        
        PresenceDocumentParser presenceDocumentParser = new PresenceDocumentParser(document);
        PresenceDocument presenceDocument =  presenceDocumentParser.parse();
        
        presentity = new DefaultPresentity(uRI, displayName, presenceDocument);
        
        return presentity;
    }
}
