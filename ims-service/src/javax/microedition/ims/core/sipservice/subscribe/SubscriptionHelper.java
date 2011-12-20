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

package javax.microedition.ims.core.sipservice.subscribe;

import javax.microedition.ims.common.EventPackage;
import java.util.List;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 21.6.2010
 * Time: 17.40.23
 */
final class SubscriptionHelper {
    private static final int DOCUMENT_CHANGES_SUBSCRIPTION_EXPIRATION_SECONDS = 3660;

    private static final String ENTRY_TERMINATOR = "\r\n";
    private static final String URI_PLACE_HOLDER = "#uri";
    private static final String URI_LIST_PLACEHOLDER = "#uriList";

    private static final String ENTRY_TEMPLATE = "  <entry uri=\"" + URI_PLACE_HOLDER + "\"/>";
    private static final String BODY_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<resource-list xmlns=\"urn:ietf:params:xml:ns:resource-lists\">\n" +
            "" + URI_LIST_PLACEHOLDER + "\n" +
            "</resource-list>";

    private SubscriptionHelper() {
    }

    private static String createDocumentChangeSubscriptionBody(final List<String> documentUris) {

        final StringBuilder uriListBuilder = new StringBuilder("");

        for (String documentUri : documentUris) {
            uriListBuilder.append(ENTRY_TEMPLATE.replaceAll(URI_PLACE_HOLDER, documentUri)).append(ENTRY_TERMINATOR);
        }

        return BODY_TEMPLATE.replaceAll(
                URI_LIST_PLACEHOLDER,
                uriListBuilder.toString().replaceAll(ENTRY_TERMINATOR + "$", "")
        );
    }

    static SubscriptionInfo createDocumentChangeSubscriptionInfo(List<String> documentUris) {
        return new SubscriptionInfoImpl(
                EventPackage.XCAP_DIFF,
                DOCUMENT_CHANGES_SUBSCRIPTION_EXPIRATION_SECONDS,
                createDocumentChangeSubscriptionBody(documentUris)
        );
    }
}
