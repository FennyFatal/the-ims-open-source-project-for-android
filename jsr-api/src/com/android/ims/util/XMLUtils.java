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

package com.android.ims.util;

import android.util.Log;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Helper class for xml stuff.
 *
 * @author Andrei Khomushko
 */
public final class XMLUtils {
    private final static char[] unescape_symbols = {'@'};

    private XMLUtils() {
        assert false;
    }

    private static DocumentBuilder createDocumentBuilder(boolean isNamespaceAware) throws IOException {
        final DocumentBuilder builder;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(isNamespaceAware);
        
        try {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        }

        return builder;
    }

    public static Document createNewDocument() throws IOException {
        final Document document;

        DocumentBuilder builder = createDocumentBuilder(false);
        document = builder.newDocument();

        return document;
    }

    public static Document createDocument(final String source, boolean isNamespaceAware) throws IOException {
        final Document document;

        DocumentBuilder builder = createDocumentBuilder(isNamespaceAware);

        try {
            document = builder.parse(new ByteArrayInputStream(source.getBytes()));
        }
        catch (SAXException e) {
            String errMsg = "Failed to parse XML source(" + e.getMessage() + ") [" + source + "]";
            logAsSplittedStrings("XMLUtils", errMsg);
            throw new IOException(e.getMessage());
        }
        catch (DOMException e) {
            String errMsg = "Failed to parse XML source(" + e.getMessage() + ") [" + source + "]";
            logAsSplittedStrings("XMLUtils", errMsg);
            throw new IOException(e.getMessage());
        }

        return document;
    }

    public static String obtainTimeStamp(Node node, String timestampTag) throws XPathExpressionException {
        return (String) XPathEvaluator.getEvaluator().eval(node, timestampTag, XPathConstants.STRING);
        //return (String) applyXPAth(node, timestampTag, XPathConstants.STRING);
    }

    public static Node obtainLatestNode(
            final Document document,
            final String expression,
            final String timestampTag) throws IOException {

        Node retValue = null;

        try {
            long latestUpdateMillis = -1;

            final NodeList nodeList = (NodeList) XPathEvaluator.getEvaluator().
                    eval(document, expression, XPathConstants.NODESET);

            final int length = nodeList.getLength();

            for (int i = 0; i < length; i++) {

                final Node tempNode = nodeList.item(i);
                final String personTimeStamp = obtainTimeStamp(tempNode, timestampTag);
                Long time = null;

                if (personTimeStamp != null && !personTimeStamp.equals("")) {
                    time = Utils.convertInetTimeFormatToJavaTime(personTimeStamp).getTime();
                }

                if (time != null) {
                    if (time > latestUpdateMillis) {
                        latestUpdateMillis = time;
                        retValue = tempNode;
                    }
                }
                else if (latestUpdateMillis == -1) {
                    retValue = tempNode;
                }
            }
        }
        catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static String unescape(String source) {
        for (char unescape_symbol : unescape_symbols) {
            source = source.replaceAll(String.format("&#%s;", (int) unescape_symbol), unescape_symbol + "");
        }
        return source;
    }

    public static void logAsSplittedStrings(final String prefix, final String msg) {
        final String[] strings = msg.split("\r\n|\r|\n");
        for (String msgString : strings) {
            Log.i(prefix, msgString);
        }
    }
}
