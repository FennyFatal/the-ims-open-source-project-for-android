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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntryPointTestXmlParser {


    private static final String LIST_NODE_NAME = "list";

    /**
     * @param args
     */
    public static void main(String[] args) {

        String source =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<rl:resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\" xmlns:rl=\"urn:ietf:params:xml:ns:resource-lists\" >"
                        + "  <rl:list name=\"phbk\">"
                        + "    <rl:display-name>phbk</rl:display-name>"
                        + "  </rl:list>"
                        + "</rl:resource-lists>";

        javax.xml.parsers.DocumentBuilder db = null;
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            db = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


        final List<URIListData> retValue = new ArrayList<URIListData>();

        Document doc = null;
        InputSource inStream = new InputSource();

        inStream.setCharacterStream(new java.io.StringReader(source));

        try {
            doc = db.parse(inStream);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (SAXException e) {
            e.printStackTrace();
        }

        //NodeList nodeList = doc.getElementsByTagName(LIST_NODE_NAME);
        //int length = nodeList.getLength();

        NodeList nodeList1 = doc.getElementsByTagName("resource-lists");
        int length1 = nodeList1.getLength();

        NodeList nodeList2 = doc.getElementsByTagName("list");
        int length2 = nodeList2.getLength();

        NodeList nodeList3 = doc.getElementsByTagName("rl:resource-lists");
        int length3 = nodeList3.getLength();

        NodeList nodeList4 = doc.getElementsByTagName("rl:list");
        int length4 = nodeList4.getLength();


        NodeList nodeList5 = doc.getElementsByTagNameNS("*", "resource-lists");
        int length5 = nodeList5.getLength();

        NodeList nodeList6 = doc.getElementsByTagNameNS("*", "list");
        int length6 = nodeList6.getLength();

        NodeList nodeList7 = doc.getElementsByTagNameNS("rl", "resource-lists");
        int length7 = nodeList7.getLength();

        NodeList nodeList8 = doc.getElementsByTagNameNS("rl", "list");
        int length8 = nodeList8.getLength();


//        NodeList childNodes = nodeList.item(0).getChildNodes();
//        int length1 = childNodes.getLength();

        System.out.println(length1);

    }

}
