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

package javax.microedition.ims.messages.wrappers.sip;

import javax.microedition.ims.messages.wrappers.sip.headerbuilder.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 12.10.2010
 * Time: 11.36.39
 * To change this template use File | Settings | File Templates.
 */
final class HeaderBuilderFactory {

    private final static Map<Header, Class<? extends HeaderBuilder>> SUPPORTED_HEADERS =
            Collections.unmodifiableMap(prepareHeaderToBuilderClassMapping());

    private static Map<Header, Class<? extends HeaderBuilder>> prepareHeaderToBuilderClassMapping() {
        Map<Header, Class<? extends HeaderBuilder>> retValue =
                Collections.synchronizedMap(new HashMap<Header, Class<? extends HeaderBuilder>>());

        retValue.put(Header.Via, ViaHeaderBuilder.class);
        retValue.put(Header.Route, RouteHeaderBuilder.class);
        retValue.put(Header.RecordRoute, RecordRouteHeaderBuilder.class);
        retValue.put(Header.Max_Forwards, MaxForwardsHeaderBuilder.class);
        retValue.put(Header.From, FromHeaderBuilder.class);
        retValue.put(Header.To, ToHeaderBuilder.class);
        retValue.put(Header.Call_ID, CallidHeaderBuilder.class);
        retValue.put(Header.Referred_By, RefferedByHeaderBuilder.class);
        retValue.put(Header.CSeq, CSeqHeaderBuilder.class);
        retValue.put(Header.Subject, SubjectHeaderBuilder.class);
        retValue.put(Header.Server, ServerHeaderBuilder.class);
        retValue.put(Header.Expires, ExpiresHeaderBuilder.class);
        retValue.put(Header.Min_Expires, MinExpiresHeaderBuilder.class);
        retValue.put(Header.Contact, ContactsHeaderBuilder.class);
        retValue.put(Header.Allow, AllowHeaderBuilder.class);
        retValue.put(Header.Privacy, PrivacyHeaderBuilder.class);
        retValue.put(Header.Require, RequireHeaderBuilder.class);
        retValue.put(Header.Allow_Events, AllowEventsHeaderBuilder.class);
        retValue.put(Header.Supported, SupportedHeaderBuilder.class);
        retValue.put(Header.UserAgent, UserAgentHeaderBuilder.class);
        retValue.put(Header.PAssertedIdentities, PAssertedIdentitiesHeaderBuilder.class);
        retValue.put(Header.HistoryInfo, HistoryInfoHeaderBuilder.class);
        retValue.put(Header.ServiceRoutes, ServiceRoutesHeaderBuilder.class);
        retValue.put(Header.PAssociatedUris, PAssociatedUrisHeaderBuilder.class);
        retValue.put(Header.RefresherParam, RefresherParamHeaderBuilder.class);
        retValue.put(Header.Min_SE, MinSEHeaderBuilder.class);
        retValue.put(Header.Event, EventHeaderBuilder.class);
        retValue.put(Header.SubscriptionState, SubscriptionStateHeaderBuilder.class);
        retValue.put(Header.SIP_ETag, ETagHeaderBuilder.class);
        retValue.put(Header.SIP_If_Match, IfMatchHeaderBuilder.class);
        retValue.put(Header.AcceptContact, AcceptContactHeaderBuilder.class);
        retValue.put(Header.RejectContact, RejectContactHeaderBuilder.class);
        retValue.put(Header.Content_Type, ContentTypeHeaderBuilder.class);
        retValue.put(Header.Content_Length, ContentLengthHeaderBuilder.class);
        retValue.put(Header.Authorization, AuthorizationHeaderBuilder.class);
        retValue.put(Header.Proxy_Authorization, ProxyAuthorizationHeaderBuilder.class);
        retValue.put(Header.Accept, AcceptHeaderBuilder.class);

        return retValue;
    }

    private final Map<Header, AtomicReference<HeaderBuilder>> headerBuilders;
    private final AtomicReference<HeaderBuilder> customHeadersBuilderReference = new AtomicReference<HeaderBuilder>(null);

    HeaderBuilderFactory() {
        this.headerBuilders = Collections.unmodifiableMap(prepareBuildersMap());
    }

    private Map<Header, AtomicReference<HeaderBuilder>> prepareBuildersMap() {
        Map<Header, AtomicReference<HeaderBuilder>> retValue =
                new HashMap<Header, AtomicReference<HeaderBuilder>>(SUPPORTED_HEADERS.size() * 2);

        for (Header header : SUPPORTED_HEADERS.keySet()) {
            retValue.put(header, new AtomicReference<HeaderBuilder>(null));
        }
        return retValue;
    }

    HeaderBuilder obtainBuilder(final Header header) {
        AtomicReference<HeaderBuilder> headerBuilderReference = headerBuilders.get(header);

        if (headerBuilderReference == null) {
            throw new IllegalArgumentException("Unsupported header " + header + " " + headerBuilders);
        }

        final Class<? extends HeaderBuilder> builderClass = SUPPORTED_HEADERS.get(header);
        if (builderClass == null) {
            throw new IllegalArgumentException("Unsupported header " + header + " " + SUPPORTED_HEADERS);
        }

        return doObtainBuilder(headerBuilderReference, header, builderClass);
    }

    HeaderBuilder obtainCustomHeadersBuilder() {
        return doObtainBuilder(customHeadersBuilderReference, null, CustomHeaderBuilder.class);
    }


    private HeaderBuilder doObtainBuilder(
            final AtomicReference<HeaderBuilder> headerBuilderReference,
            final Header header,
            final Class<? extends HeaderBuilder> builderClass) {

        HeaderBuilder retValue = null;
        try {
            retValue = headerBuilderReference.get();
            if (retValue == null && headerBuilderReference.compareAndSet(null, instantiate(header, builderClass))) {
                retValue = headerBuilderReference.get();
            }
        }
        catch (Exception e) {
            final String errMsg = "Can't instantiate header builder of class " +
                    builderClass + ". Got exception during instantiation " + e.toString();

            throw new IllegalArgumentException(errMsg);
        }

        return retValue;
    }

    private HeaderBuilder instantiate(final Header header, final Class<? extends HeaderBuilder> builderClass)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        return ((Constructor<? extends HeaderBuilder>) builderClass.getConstructor(Header.class)).newInstance(header);
    }

    
    public String toString() {
        return "HeaderBuilderFactory{" +
                "headerBuilders=" + headerBuilders +
                '}';
    }
}
