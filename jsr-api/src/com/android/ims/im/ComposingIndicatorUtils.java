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

package com.android.ims.im;

import android.util.Log;
import com.android.ims.presence.BaseXMLParser;
import com.android.ims.util.XMLUtils;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;

public class ComposingIndicatorUtils extends BaseXMLParser {
    
    private static final String TAG = "ComposingIndicatorUtils";
    
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    
    public final static String COMPOSING_INDICATOR_CONTENT_TYPE = "application/im-iscomposing+xml";
    
    private final static String COMPOSING_CONTENT_TYPE = "text/plain";
    
    private final static String STATE_ACTIVE = "active";
    private final static String STATE_IDLE = "idle";
    
    
    public static class ComposingIndicatorInfo {
        private int timeout;

        public ComposingIndicatorInfo(int timeout) {
            this.timeout = timeout;
        }

        public int getTimeout() {
            return timeout;
        }
    }
    
    
    public static String createMessage(int timeout) {
        StringBuilder retValue = new StringBuilder();
        
        retValue.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        retValue.append(LINE_SEPARATOR)
            .append("<isComposing")
            .append(" xmlns=\"urn:ietf:params:xml:ns:im-iscomposing\"")
            .append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
            .append(" xsi:schemaLocation=\"urn:ietf:params:xml:ns:im-composingiscomposing.xsd\"")
            .append(">");
                
        retValue.append(LINE_SEPARATOR)
            .append("<state>");
        if (timeout > 0) {
            retValue.append(STATE_ACTIVE);
        } else {
            retValue.append(STATE_IDLE);
        }
        retValue.append("</state>");
        
        retValue.append(LINE_SEPARATOR)
            .append("<contenttype>")
            .append(COMPOSING_CONTENT_TYPE)
            .append("</contenttype>");
        
        if (timeout > 0) {
            retValue.append(LINE_SEPARATOR)
                .append("<refresh>")
                .append(timeout)
                .append("</refresh>");
        }
        
        retValue.append(LINE_SEPARATOR)
            .append("</isComposing>");

        return retValue.toString();
    }
    
    public static ComposingIndicatorInfo parseMessage(String messageBody) {
        Document document;
        final Node stateNode, refreshNode;
        int timeout = 0;
        
        try {
            document = XMLUtils.createDocument(messageBody, false);

            stateNode = (Node) applyXPAth(document, "/isComposing/state",
                XPathConstants.NODE);
            
            String stateValue = stateNode.getTextContent();
            
            if (STATE_ACTIVE.equalsIgnoreCase(stateValue)) {
                
                refreshNode = (Node) applyXPAth(document, "/isComposing/refresh",
                    XPathConstants.NODE);
                
                String refreshValue = refreshNode.getTextContent();
                
                timeout = Integer.parseInt(refreshValue);
            }
            
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (XPathExpressionException e) {
            Log.e(TAG, e.getMessage());
        }

        ComposingIndicatorInfo indicatorInfo = new ComposingIndicatorInfo(timeout);
        
        return indicatorInfo;
    }

}
