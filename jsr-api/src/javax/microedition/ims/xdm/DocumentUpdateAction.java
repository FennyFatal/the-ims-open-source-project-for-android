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

import org.w3c.dom.*;

/**
 * Provides functionality to decide what action needs to be taken for a local
 * XDM document cache after the documentUpdateReceived method in the
 * DocumentSubscriberListener interface has been called due to an update of the
 * XDM document on the XDM server. For an example of how to use this class, see
 * DocumentSubscriber.
 * 
 * The XCAP diff document that is passed as an argument to the
 * documentUpdateReceived method can contain different amounts of information as
 * described in [XCAP-DIFF].
 * 
 * The lowest level of information is only an notification that the XDM document
 * on the XDM server has been updated, and in order to keep a local document
 * cache synchronized a new retrieval of the document is needed.
 * 
 * If the XCAP diff document provides information of the document updates this
 * could possibly be used to update the local XDM document cache without
 * retrieving the whole XDM document from the server. If all the updates and the
 * correct ETag are included in the XCAP diff document they can be applied to
 * the XDM document to save network resources.
 * 
 * The method documentUpdateReceived is also called when a subscription to an
 * XDM document has been established. If the local document cache is identical
 * to the XDM document on the server, the information in the XCAP diff document
 * indicates that no real update has been made and there is no need to take any
 * action.
 * 
 * {@link DocumentSubscriber}
 * 
 * @author Andrei Khomushko
 * 
 */
public final class DocumentUpdateAction {
    /**
     * Indicates that the local document is out of sync but the XCAP diff
     * document contains all necessary information to update the local document
     * to the new state.
     **/
    public static final int ACTION_APPLY_CHANGES = 1;

    /**
     * Indicates that the local document is up-to-date and no action is
     * required.
     * 
     * This usually means that the XCAP diff document was received in the
     * initial notification sent when the subscription starts, meaning that the
     * local document is already up-to-date.
     **/
    public static final int ACTION_DO_NOTHING = 2;

    /**
     * Indicates that the local document is out of sync and should be retrieved
     * in its entirety from the XDM server.
     * 
     * This usually means that the XCAP diff document contains information that
     * a document has changed, but no details of the change itself.
     **/
    public static final int ACTION_RETRIEVE_DOCUMENT = 0;

    private static String DOCUMENT = "document";
    private static String DOCUMENT_BODY_NOT_CHANGED = "body-not-changed";
    private static String DOCUMENT_ADD = "add";
    private static String DOCUMENT_REMOVE = "remove";
    private static String DOCUMENT_REPLACE = "replace";
    
    private static String DOCUMENT_PREVIOUS_ETAG = "previous-etag";
    private static String DOCUMENT_NEW_ETAG = "new-etag";
    //private static String DOCUMENT_SEL = "sel";

    private static String ELEMENT = "element";
    //private static String ELEMENT_SEL = "sel";
    //private static String ELEMENT_EXISTS = "exists";
    
    private static String ATTRIBUTE = "attribute";
    //private static String ATTRIBUTE_SEL = "sel";
    //private static String ATTRIBUTE_EXISTS = "exists";
    
    private DocumentUpdateAction() {}
    
    /**
     * Provides information on how to handle a received XCAP diff document.
     * 
     * This method is useful when keeping a local document cache synchronized
     * with the documents on an XDM server. When an XCAP diff document is
     * received, the method can be used to determine what action needs to be
     * taken for a particular document.
     * 
     * or an example of how to use this method, see DocumentSubscriber.
     * 
     * @param documentSelector
     *            - the document selector of the document
     * @param etag
     *            - the ETag of the cached document
     * @param xcapDiff
     *            - the XCAP diff document
     * @return one of the ACTION_ identifiers indicating the appropriate action
     *         to take
     * @throws IllegalArgumentException
     *             - if any of the arguments documentSelector, ETag or xcapDiff
     *             are null.
     */
    
    /*<?xml version="1.0" encoding="UTF-8"?>
    <d:xcap-diff xmlns:d="urn:ietf:params:xml:ns:xcap-diff"
               xcap-root="http://xcap.example.com/">

     <d:document previous-etag="7ahggs"
                 sel="tests/users/sip:joe@example.com/index"
                 new-etag="fgherhryt3">
       <d:add sel="*"
        ><foo>this is a new element</foo></d:add></d:document>

     <d:document previous-etag="fgherhryt3"
                 sel="tests/users/sip:joe@example.com/index"
                 new-etag="dgdgdfgrrr">
       <d:add sel="*"
        ><bar>this is a bar element
    </bar></d:add></d:document>

     <d:document previous-etag="dgdgdfgrrr"
                 sel="tests/users/sip:joe@example.com/index"
                 new-etag="63hjjsll">
       <d:add sel="*"
        ><foobar>this is a foobar element</foobar></d:add></d:document>

    </d:xcap-diff>
*/        

 /* +-----------+----------+-----------+----------+-------------------+
    | previous- | new-     | <add>     | <body-   | XCAP resource/    |
    | etag      | etag     | <replace> | not-     | metadata change   |
    |           |          | <remove>  | changed> |                   |
    +-----------+----------+-----------+----------+-------------------+
    | xxx       | yyy      | *         | -        | resource patched, |
    |           |          |           |          | patch included    |
    +-----------+----------+-----------+----------+-------------------+
    | xxx       | yyy      | -         | -        | resource patched, |
    |           |          |           |          | external document |
    |           |          |           |          | retrieval         |
    +-----------+----------+-----------+----------+-------------------+
    | xxx       | yyy      | -         | *        | only ETag changed |
    +-----------+----------+-----------+----------+-------------------+
    | -         | yyy      | -         | -        | resource created  |
    |           |          |           |          | or exists,        |
    |           |          |           |          | external document |
    |           |          |           |          | retrieval         |
    +-----------+----------+-----------+----------+-------------------+
    | xxx       | -        | -         | -        | resource removed  |
    +-----------+----------+-----------+----------+-------------------+
*/      
    public static int getAction(String documentSelector, String etag,
            Document xcapDiff) throws IllegalArgumentException {
        if(documentSelector == null) {
            throw new IllegalArgumentException("The documentSelector argument is null");
        }
        
        if(etag == null) {
            throw new IllegalArgumentException("The etag argument is null");
        }
        
        if(xcapDiff == null) {
            throw new IllegalArgumentException("The xcapDiff argument is null");
        }
        
        int retValue = ACTION_RETRIEVE_DOCUMENT;
        
      
        Element root = xcapDiff.getDocumentElement();
        NodeList documents = root.getElementsByTagName(DOCUMENT);
        
        final String previousETag = getPreviousETag(documents);
        final String newETag = getNewETag(documents);
        boolean isAddOrRemoveOrReplace = isAddOrRemoveOrReplace(documents);
        boolean isBodyNotChanged = isBodyNotChanged(documents);
        
        boolean isElementsExists = isElementsExists(root.getChildNodes());
        boolean isAttributeExists = isAttributeExists(root.getChildNodes());
        
        if(isElementsExists || isAttributeExists) {
            //resource patched, patch included
            retValue = ACTION_APPLY_CHANGES;
        } else if(previousETag != null && newETag != null && isAddOrRemoveOrReplace && !isBodyNotChanged) {
            //resource patched, patch included
            retValue = ACTION_APPLY_CHANGES;
        } else if(previousETag != null && newETag != null && !isAddOrRemoveOrReplace && !isBodyNotChanged) {
            //resource patched
            if(newETag.equals(etag)) {
                retValue = ACTION_DO_NOTHING;
            } else {
                //external document retrieval
                retValue = ACTION_RETRIEVE_DOCUMENT;    
            }
        } else if(previousETag != null && newETag != null && !isAddOrRemoveOrReplace && isBodyNotChanged) {
            //only ETag changed
            retValue = ACTION_APPLY_CHANGES;
        } else if(previousETag == null && newETag != null && !isAddOrRemoveOrReplace && !isBodyNotChanged) {
            //resource created or exists
            if(newETag.equals(etag)) {
                retValue = ACTION_DO_NOTHING;
            } else {
                //external document retrieval
                retValue = ACTION_RETRIEVE_DOCUMENT;
            }
        } else if(previousETag != null && newETag == null && !isAddOrRemoveOrReplace && !isBodyNotChanged) {
            //resource removed
            retValue = ACTION_DO_NOTHING;
        } else {
            retValue = ACTION_RETRIEVE_DOCUMENT;
        }
        
        return retValue;
    }

    private static boolean isAttributeExists(NodeList rootChields) {
        boolean isAttributeExists = false;
        
        for (int i = 0; i < rootChields.getLength(); i++){
            String chieldName = getNodeName(rootChields.item(i));
            if(ATTRIBUTE.equals(chieldName)) {
                isAttributeExists = true;
                break;
            }
        }
        
        return isAttributeExists;
    }

    private static boolean isElementsExists(NodeList rootChields) {
        boolean isElementsExists = false;
        
        for (int i = 0; i < rootChields.getLength(); i++){
            String chieldName = getNodeName(rootChields.item(i));
            if(ELEMENT.equals(chieldName)) {
                isElementsExists = true;
                break;
            }
        }
        
        return isElementsExists;
    }

    private static boolean isBodyNotChanged(NodeList documents) {
        boolean isBodyNotChanged = false;
        
        for (int i = 0; i < documents.getLength(); i++){
            Node document = documents.item(i);
            NodeList documentChields = document.getChildNodes();
            for (int j = 0; j < documentChields.getLength(); j++){
                String chieldName = getNodeName(documentChields.item(j));
                if(DOCUMENT_BODY_NOT_CHANGED.equals(chieldName)) {
                    isBodyNotChanged = true;
                    break;
                }
            }
        }
        
        return isBodyNotChanged;
    }
    
    private static final String getNodeName(Node node) {
        String retValue = null;
        String nodeName = node.getNodeName();
        int idx = nodeName.indexOf(":");
        if(idx > 0) {
            retValue = nodeName.substring(idx + 1);
        } else {
            retValue = nodeName;
        }
        
        return retValue;
    }

    private static boolean isAddOrRemoveOrReplace(NodeList documents) {
        boolean isAddOrRemoveOrReplaceExists = false;
        
        for (int i = 0; i < documents.getLength(); i++){
            Node document = documents.item(i);
            NodeList documentChields = document.getChildNodes();
            for (int j = 0; j < documentChields.getLength(); j++){
                String chieldName = getNodeName(documentChields.item(j));
                if(DOCUMENT_ADD.equals(chieldName) || DOCUMENT_REMOVE.equals(chieldName) ||
                        DOCUMENT_REPLACE.equals(chieldName)) {
                    isAddOrRemoveOrReplaceExists = true;
                    break;
                }
            }
        }
        
        return isAddOrRemoveOrReplaceExists;
    }

    private static String getPreviousETag(NodeList documents) {
        String previousETag = null;
        
        for (int i = 0; i < documents.getLength(); i++){
            Node document = documents.item(i);
            
            Node previousNodeTag = document.getAttributes().getNamedItem(DOCUMENT_PREVIOUS_ETAG);
            if(previousNodeTag != null) {
                previousETag = ((Attr)previousNodeTag).getValue();
                break;
            } 
        }
        
        return previousETag;
    }
    
    private static String getNewETag(NodeList documents) {
        String newETag = null;
        
        for (int i = 0; i < documents.getLength(); i++){
            Node document = documents.item(i);
            
            Node previousNodeTag = document.getAttributes().getNamedItem(DOCUMENT_NEW_ETAG);
            if(previousNodeTag != null) {
                newETag = ((Attr)previousNodeTag).getValue();
                break;
            } 
        }
        
        return newETag;
    }
}
