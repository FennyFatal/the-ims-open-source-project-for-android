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

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.MultiMap;
import javax.microedition.ims.common.util.MultiValueMap;
import javax.microedition.ims.messages.parser.message.ChallengeParser;
import javax.microedition.ims.messages.parser.message.MessageParser;
import javax.microedition.ims.messages.wrappers.common.Param;
import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.ParamListDefaultImpl;
import javax.microedition.ims.messages.wrappers.common.Uri;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseSipMessage implements IMSMessage {
    private final AtomicBoolean expired = new AtomicBoolean(false);

    private static final HeaderBuilderFactory HEADER_BUILDER_FACTORY = new HeaderBuilderFactory();
    static final Collection<HeaderBuilder> HEADERS_ORDER = Arrays.asList(

            HEADER_BUILDER_FACTORY.obtainBuilder(Header.To),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Max_Forwards),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Supported),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.UserAgent),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Expires),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Accept),
            HEADER_BUILDER_FACTORY.obtainCustomHeadersBuilder(),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Call_ID),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.From),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.CSeq),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Content_Length),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Via),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Contact),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Authorization),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Proxy_Authorization),

            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Route),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.RecordRoute),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Referred_By),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Subject),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Server),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Min_Expires),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Allow),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Privacy),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Require),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Allow_Events),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.PAssertedIdentities),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.HistoryInfo),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.ServiceRoutes),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.PAssociatedUris),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.RefresherParam),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Min_SE),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.SubscriptionState),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.SIP_ETag),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.SIP_If_Match),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.AcceptContact),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.RejectContact),
            HEADER_BUILDER_FACTORY.obtainBuilder(Header.Content_Type)
    );
    private static final Collection<String> DIRECTLY_HANDLED_HEADERS =
            Collections.unmodifiableCollection(obtainDirectlyHandledHeaders());


    private final int cSeq;
    private final int maxForwards;

    private final String callId;
    private final String eTag;
    private final String ifMatch;
    private final String subject;
    private final String method;
    private final String userAgent;
    private final String server;

    private final SessionExpiresHeader sessionExpires;
    private final UriHeader from;
    private final UriHeader to;
    private final UriHeader referredBy;

    private final byte[] body;
    private final long expires;
    private final long minExpires;
    private final ParamHeader subscriptionState;
    private final ParamHeader event;
    private final ParamHeader contentType;

    //private final ParamHeader accept;   
    private final MultiMap<String, String> customHeaders;
    private final ParamList acceptContact;
    private final ParamList rejectContact;

    private final Collection<String> allowEvents;
    private final Collection<String> privacy;
    private final Collection<String> allow;
    private final Collection<String> require;
    private final Collection<String> supported;

    private final Collection<UriHeader> routes;
    private final Collection<UriHeader> pAssociatedUris;
    private final Collection<UriHeader> pAssertedIdentities;
    private final Collection<UriHeader> recordRoutes;
    private final Collection<UriHeader> historyInfo;
    private final Collection<UriHeader> serviceRoutes;

    private final List<Via> vias;
    private final ContactsList contacts;

    private final Map<ChallengeType, AuthChallenge> challenges;

    private final int contentLength;

    private final IMSID imsid;
    private final String stringValue;
    private final byte[] byteContent;
    private final String content;
    private final String shortDescription;
    private final int preComputedHash;


    public BaseSipMessage(final Builder builder) {

        this.method = builder.method;

        this.callId = builder.callId;
        this.imsid = new IMSStringID(callId);

        this.cSeq = builder.cSeq;

        this.maxForwards = builder.maxForwards;

        this.userAgent = builder.userAgent;

        //sessionExpires = new SessionExpiresHeader(null, -1L, -1L);
        this.sessionExpires = builder.sessionExpires.build();
        this.expires = builder.expires;
        this.minExpires = builder.minExpires;

        //from = new UriHeader(new SipUri.Builder().buildUri());
        this.from = builder.fromHeader == null ? null : builder.fromHeader.build();

        //to = new UriHeader(new SipUri.Builder().buildUri());
        this.to = builder.toHeader == null ? null : builder.toHeader.build();

        //referredBy = new UriHeader(new SipUri.Builder().buildUri());
        this.referredBy = builder.referredByHeader == null ? null : builder.referredByHeader.build();

        //subscriptionState = new ParamHeader(new ParamHeaderBuilder().value(null).paramsList(new ParamListDefaultImpl()));
        this.subscriptionState = builder.subscriptionState == null ? null : builder.subscriptionState.build();

        //event = new ParamHeader(new ParamHeaderBuilder().value(null).paramsList(new ParamListDefaultImpl()));
        this.event = builder.event == null ? null : builder.event.build();

        //contentType = new ParamHeader(new ParamHeaderBuilder().value(null).paramsList(new ParamListDefaultImpl()));
        this.contentType = builder.contentType == null ? null : builder.contentType.build();
        //accept = new ParamHeader();

        this.contentLength = builder.contentLength;

        this.server = builder.server;
        this.eTag = builder.eTag;
        this.ifMatch = builder.ifMatch;
        this.subject = builder.subject;

        this.acceptContact = builder.acceptContact;
        this.rejectContact = builder.rejectContact;

        //allowEvents = new ArrayList<String>();
        this.allowEvents = Collections.unmodifiableCollection(new ArrayList<String>(builder.allowEvents));

        //privacy = new ArrayList<String>();
        this.privacy = builder.privacies == null ? null : Collections.unmodifiableCollection(new ArrayList<String>(builder.privacies));

        //allow = new ArrayList<String>();
        this.allow = Collections.unmodifiableCollection(new ArrayList<String>(builder.allow));

        //require = new ArrayList<String>();
        this.require = Collections.unmodifiableCollection(new ArrayList<String>(builder.require));

        //supported = new ArrayList<String>();
        this.supported = Collections.unmodifiableCollection(new ArrayList<String>(builder.supported));

        //routes = new ArrayList<UriHeader>();
        this.routes = Collections.unmodifiableCollection(new ArrayList<UriHeader>(builder.routes));

        //pAssociatedUris = new ArrayList<UriHeader>();
        this.pAssociatedUris = Collections.unmodifiableCollection(new ArrayList<UriHeader>(builder.pAssociatedUris));

        //pAssertedIdentities = new ArrayList<UriHeader>();
        this.pAssertedIdentities = Collections.unmodifiableCollection(new ArrayList<UriHeader>(builder.assertedIdentities));

        //recordRoutes = new ArrayList<UriHeader>();
        this.recordRoutes = Collections.unmodifiableCollection(new ArrayList<UriHeader>(builder.recordRoutes));

        //historyInfo = new ArrayList<UriHeader>();
        this.historyInfo = Collections.unmodifiableCollection(new ArrayList<UriHeader>(builder.historyInfo));

        //serviceRoutes = new ArrayList<UriHeader>();
        this.serviceRoutes = Collections.unmodifiableCollection(new ArrayList<UriHeader>(builder.serverRoutes));

        //customHeaders = new MultiValueMap<String, String>();
        this.customHeaders = new MultiValueMap<String, String>(builder.customHeaders);

        //vias = new ArrayList<Via>();
        this.vias = builder.vias == null ? Collections.<Via>emptyList() : buildVias(builder.vias);

        //contacts = new ContactsList();
        this.contacts = builder.contacts == null ? null : builder.contacts.build();

        final Map<ChallengeType, AuthChallenge> chMap = builder.challengeMap;
        this.challenges =
                Collections.unmodifiableMap(
                        chMap == null ?
                                Collections.<ChallengeType, AuthChallenge>emptyMap() :
                                new HashMap<ChallengeType, AuthChallenge>(chMap)
                );

        this.body = builder.body == null ? null : builder.body.clone();

        this.stringValue = doBuildStringValue();
        this.byteContent = doBuildByteContent();
        this.content = new String(byteContent);
        this.shortDescription = doBuildShortDescription();
        this.preComputedHash = doComputeHash();
    }

    public Builder getBuilder() {
        final Builder retValue = new Builder(this instanceof Request ? Builder.Type.REQUEST : Builder.Type.RESPONSE);

        retValue.body = this.body;

        retValue.cSeq = this.getcSeq();
        retValue.contentLength = this.contentLength;
        retValue.maxForwards = this.maxForwards;

        retValue.expires = this.expires;
        retValue.minExpires = this.minExpires;

        retValue.callId = this.callId;

        retValue.eTag = this.eTag;

        retValue.ifMatch = this.ifMatch;
        retValue.server = this.server;
        retValue.userAgent = this.userAgent;
        retValue.subject = this.subject;
        retValue.method = this.method;

        retValue.allow = this.allow;
        retValue.allowEvents = this.allowEvents;
        retValue.require = this.require;
        retValue.supported = this.supported;
        retValue.privacies = this.privacy;

        retValue.toHeader = new UriHeader.UriHeaderBuilder(this.to);
        retValue.fromHeader = new UriHeader.UriHeaderBuilder(this.from);
        retValue.referredByHeader = this.referredBy == null ? null : new UriHeader.UriHeaderBuilder(this.referredBy);
        retValue.routes = this.routes;
        retValue.pAssociatedUris = this.pAssociatedUris;
        retValue.assertedIdentities = this.pAssertedIdentities;
        retValue.serverRoutes = this.serviceRoutes;
        retValue.recordRoutes = this.recordRoutes;
        retValue.historyInfo = this.historyInfo;

        retValue.subscriptionState = this.subscriptionState == null ?
                null :
                new ParamHeader.ParamHeaderBuilder(this.subscriptionState);

        retValue.event = this.event == null ?
                null :
                new ParamHeader.ParamHeaderBuilder(this.event);

        retValue.contentType = this.contentType == null ?
                null :
                new ParamHeader.ParamHeaderBuilder(this.contentType);

        retValue.contacts = this.contacts == null ? null : new ContactsList.Builder(this.contacts);

        retValue.sessionExpires = this.sessionExpires == null ?
                null :
                new SessionExpiresHeader.Builder(this.sessionExpires);

        retValue.customHeaders = this.customHeaders;

        retValue.acceptContact = this.acceptContact;
        retValue.rejectContact = this.rejectContact;

        retValue.vias = buildViaBuilders(vias);

        retValue.challengeMap = new HashMap<ChallengeType, AuthChallenge>(this.challenges);

        return retValue;
    }

    private List<Via> buildVias(final Collection<Via.Builder> vias) {

        final List<Via> retValue;

        if (vias != null) {
            retValue = new ArrayList<Via>(vias.size());
            for (Via.Builder viaBuilder : vias) {
                retValue.add(viaBuilder.build());
            }
        }
        else {
            retValue = Collections.emptyList();
        }

        return retValue;
    }

    private List<Via.Builder> buildViaBuilders(final Collection<Via> vias) {

        final List<Via.Builder> retValue;

        if (vias != null) {
            retValue = new ArrayList<Via.Builder>(vias.size());

            for (Via via : vias) {
                retValue.add(new Via.Builder(via));
            }
        }
        else {
            retValue = Collections.emptyList();
        }

        return retValue;
    }

    private static Collection<String> obtainDirectlyHandledHeaders() {
        Collection<String> directlyHandledHeaders = new ArrayList<String>();
        for (HeaderBuilder builder : HEADERS_ORDER) {
            final Header header = builder.getHeader();
            if (header != null) {
                directlyHandledHeaders.add(header.stringValue());
            }
        }
        return directlyHandledHeaders;
    }

    public String getMethod() {
        return method;
    }

    public int getcSeq() {
        return cSeq;
    }

    public int getMaxForwards() {
        return maxForwards;
    }

    public String getCallId() {
        return callId;
    }

    public long getExpires() {
        return expires;
    }

    public long getMinExpires() {
        return minExpires;
    }

    public SessionExpiresHeader getSessionExpires() {
        return sessionExpires;
    }

    public String geteTag() {
        return eTag;
    }

    public UriHeader getFrom() {
        return from;
    }

    public UriHeader getTo() {
        return to;
    }

    public String getIfMatch() {
        return ifMatch;
    }

    public String getSubject() {
        return subject;
    }

    public byte[] getBody() {
        return body;
    }

    public UriHeader getReferredBy() {
        return referredBy;
    }

    public ParamHeader getEvent() {
        return event;
    }

    public ParamHeader getContentType() {
        return contentType;
    }

    public ParamList getAcceptContact() {
        return acceptContact;
    }

    public ParamList getRejectContact() {
        return rejectContact;
    }

    public Collection<String> getAllowEvents() {
        return allowEvents;
    }

    public Collection<String> getPrivacy() {
        return privacy;
    }

    public Collection<String> getAllow() {
        return allow;
    }

    public Collection<String> getRequire() {
        return require;
    }

    public void addRequire(String item) {
        if (!require.contains(item)) {
            require.add(item);
        }
    }

    public Collection<String> getSupported() {
        return supported;
    }

    public void addSupported(String item) {
        if (!supported.contains(item)) {
            supported.add(item);
        }
    }

    public Collection<UriHeader> getRoutes() {
        return routes;
    }

    public void addRoute(UriHeader item) {
        routes.add(item);
    }

    public Collection<UriHeader> getpAssociatedUris() {
        return pAssociatedUris;
    }

    public Collection<UriHeader> getpAssertedIdentities() {
        return pAssertedIdentities;
    }

    public Collection<UriHeader> getRecordRoutes() {
        return recordRoutes;
    }

    public void addRecordRoute(UriHeader item) {
        recordRoutes.add(item);
    }

    public Collection<UriHeader> getHistoryInfo() {
        return historyInfo;
    }

    public Collection<UriHeader> getServiceRoutes() {
        return serviceRoutes;
    }

    public List<Via> getVias() {
        return vias;
    }

    public ContactsList getContacts() {
        return contacts;
    }

    public ParamHeader getSubscriptionState() {
        return subscriptionState;
    }

    public List<String> getCustomHeader(String header) {
        Collection<String> collection = customHeaders.get(header);
        return customHeaders.get(header) != null ? new ArrayList<String>(collection) : new ArrayList<String>();
    }

    /**
     * Return first value for specified custom header
     * @param header
     * @return
     */
    public String getCustomHeaderValue(Header header) {
        List<String> customHeaderValues = getCustomHeader(header);
        return customHeaderValues.size() > 0? customHeaderValues.get(0): null;
    }

    public List<String> getCustomHeader(Header header) {
        return getCustomHeader(header.stringValue());
    }
    
    public String toString() {
        return stringValue;
    }

    private String doBuildStringValue() {
        return super.toString() + "" + getMethod();
    }
    
    public String buildContent() {
        return content;
    }

    public byte[] buildByteContent() {
        return byteContent;
    }

    private byte[] doBuildByteContent() {
        StringBuilder retValue = new StringBuilder(1000);

        for (HeaderBuilder headerBuilder : HEADERS_ORDER) {
            headerBuilder.build(this, retValue, DIRECTLY_HANDLED_HEADERS);
        }

        return retValue.toString().getBytes();
    }

    public Map<ChallengeType, AuthChallenge> getAuthenticationChallenges() {
        return challenges;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String shortDescription() {
        return shortDescription;
    }

    private String doBuildShortDescription() {
        return getMethod() + ":" + getCallId();
    }

    public MultiMap<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public boolean isEqualTo(final BaseSipMessage msg) {
        boolean retValue = false;

        if (msg != null) {
            boolean sameMethod = MessageType.parse(getMethod()) == MessageType.parse(msg.getMethod());
            boolean sameCallId = getCallId().equals(msg.getCallId());
            boolean sameCSeq = getcSeq() == msg.getcSeq();

            final String firstFromTag = getFrom().getTag();
            final String secondsFromTag = msg.getFrom().getTag();
            boolean sameFromTag = firstFromTag != null ? firstFromTag.equals(secondsFromTag) : secondsFromTag == null;

            final String firstToTag = getTo().getTag();
            final String secondToTag = msg.getTo().getTag();
            boolean sameToTag = firstToTag != null ? firstToTag.equals(secondToTag) : secondToTag == null;

            final String firstBranch = getMessageBranch(this);
            final String secondBranch = getMessageBranch(msg);
            boolean sameBranch = firstBranch != null ? firstBranch.equals(secondBranch) : secondBranch == null;

            final String firstRSeq = getCustomHeaderValue(Header.RSeq);
            final String secondRSeq = msg.getCustomHeaderValue(Header.RSeq);
            boolean sameRSeq = firstRSeq != null ? firstRSeq.equals(secondRSeq) : secondRSeq == null;

            if (sameMethod && sameCallId && sameCSeq && sameFromTag && sameToTag && sameBranch && sameRSeq) {
                retValue = true;
            }
        }

        return retValue;
    }

    private static String getMessageBranch(final BaseSipMessage msg) throws NullPointerException {
        if (msg == null) {
            throw new NullPointerException("Cannot handle null value. Now passed " + msg);
        }

        final List<Via> viaList = msg.getVias();

        String retValue = null;
        if (viaList != null && viaList.size() > 0) {
            final Via via = viaList.get(0);

            if (via != null) {
                final String paramToLookUp = "branch";
                retValue = getParam(paramToLookUp, via.getParamsList());

                if (retValue == null) {
                    retValue = getParam(paramToLookUp, via.getUri().getHeaders());
                    if (retValue == null) {
                        retValue = getParam(paramToLookUp, via.getUri().getParamsList());
                    }
                }
            }
        }

        assert retValue != null : "Branch not found for message: " + msg.buildContent();
        retValue = retValue.replaceAll(";.*", "");

        return retValue;
    }

    private static String getParam(String paramName, ParamList paramList) {
        String retValue = null;

        if (paramList != null) {
            final Param param = paramList.getParams().get(paramName);
            if (param != null) {
                retValue = param.getValue();
            }
        }

        return retValue;
    }

    public int calcHash() {
        return preComputedHash;
    }

    private int doComputeHash() {
        int result = cSeq;
        result = 31 * result + (callId != null ? callId.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);

        final String fromTag = getFrom() == null ? null : getFrom().getTag();
        result = 31 * result + (fromTag != null ? fromTag.hashCode() : 0);

        final String toTag = getTo() == null ? null : getTo().getTag();
        result = 31 * result + (toTag != null ? toTag.hashCode() : 0);

        return result;
    }

    
    public IMSID getIMSEntityId() {
        return imsid;
    }

    
    public IMSEntityType getEntityType() {
        return IMSEntityType.SIP;
    }

    public String getServer() {
        return server;
    }

    public static class Builder {

        public static enum Type {
            REQUEST, RESPONSE
        }

        Type type;

        byte[] body;

        int cSeq;
        int contentLength;
        int maxForwards;
        int statusCode;
        long expires = -1;
        long minExpires = -1;

        String callId;
        String eTag;
        String ifMatch;
        String reasonPhrase;
        String server;
        String userAgent;
        String subject;
        String method;

        Collection<String> allow = new ArrayList<String>();
        Collection<String> allowEvents = new ArrayList<String>();
        Collection<String> require = new ArrayList<String>();
        Collection<String> supported = new ArrayList<String>();
        Collection<String> privacies = null;

        Uri requestUri;

        UriHeader.UriHeaderBuilder toHeader;
        UriHeader.UriHeaderBuilder fromHeader;
        UriHeader.UriHeaderBuilder referredByHeader;


        Collection<UriHeader> routes = new ArrayList<UriHeader>();
        Collection<UriHeader> pAssociatedUris = new ArrayList<UriHeader>();
        Collection<UriHeader> assertedIdentities = new ArrayList<UriHeader>();
        Collection<UriHeader> serverRoutes = new ArrayList<UriHeader>();
        Collection<UriHeader> recordRoutes = new ArrayList<UriHeader>();
        Collection<UriHeader> historyInfo = new ArrayList<UriHeader>();

        ParamHeader.ParamHeaderBuilder subscriptionState;
        ParamHeader.ParamHeaderBuilder event;
        ParamHeader.ParamHeaderBuilder contentType = new ParamHeader.ParamHeaderBuilder();

        ContactsList.Builder contacts = new ContactsList.Builder();
        SessionExpiresHeader.Builder sessionExpires = new SessionExpiresHeader.Builder(null, -1L, -1L);

        MultiMap<String, String> customHeaders = new MultiValueMap<String, String>();

        ParamList acceptContact = new ParamListDefaultImpl();
        ParamList rejectContact = new ParamListDefaultImpl();

        Collection<Via.Builder> vias = new ArrayList<Via.Builder>();
        Map<ChallengeType, AuthChallenge> challengeMap;

        public Builder() {
        }

        public Builder(Type type) {
            this.type = type;
        }

        public Builder msgType(final Type type) {
            this.type = type;
            return this;
        }

        public Type getType() {
            return type;
        }

        public Builder body(final byte[] body) {
            this.body = body;
            return this;
        }

        public byte[] getBody() {
            return body;
        }

        public Builder cSeq(final int i) {
            this.cSeq = i;
            return this;
        }

        public Builder requestUri(final Uri uri) {
            this.requestUri = uri;
            return this;
        }

        public Builder to(final UriHeader.UriHeaderBuilder toHeader) {
            this.toHeader = toHeader;
            return this;
        }

        public Builder from(final UriHeader.UriHeaderBuilder fromHeader) {
            this.fromHeader = fromHeader;
            return this;
        }

        public Builder route(final UriHeader routeUri) {
            if (this.routes == null) {
                this.routes = createUriContainer();
            }
            routes.add(routeUri);

            return this;
        }

        public Builder pAssociatedUri(final UriHeader pAssociatedUri) {
            if (this.pAssociatedUris == null) {
                this.pAssociatedUris = createUriContainer();
            }
            this.pAssociatedUris.add(pAssociatedUri);

            return this;
        }

        public Builder assertedIdentity(final UriHeader assertedIdentityUri) {

            if (this.assertedIdentities == null) {
                this.assertedIdentities = createUriContainer();
            }
            this.assertedIdentities.add(assertedIdentityUri);

            return this;
        }

        public Builder serviceRoute(final UriHeader serviceRouteUri) {

            if (this.serverRoutes == null) {
                this.serverRoutes = createUriContainer();
            }
            serverRoutes.add(serviceRouteUri);

            return this;
        }

        public Builder recordRoute(final UriHeader recordRouteUri) {

            if (this.recordRoutes == null) {
                this.recordRoutes = createUriContainer();
            }
            this.recordRoutes.add(recordRouteUri);

            return this;
        }

        public Builder referredBy(final UriHeader.UriHeaderBuilder referredByHeader) {
            this.referredByHeader = referredByHeader;
            return this;
        }

        public Builder historyInfo(final UriHeader historyInfo) {
            if (this.historyInfo == null) {
                this.historyInfo = createUriContainer();
            }

            this.historyInfo.add(historyInfo);

            return this;
        }

        public Builder contentLength(final int contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder maxForwards(final int maxForwards) {
            this.maxForwards = maxForwards;
            return this;
        }

        public Builder callId(final String callId) {
            this.callId = callId;
            return this;
        }

        public Builder allow(final String allow) {

            if (this.allow == null) {
                this.allow = createStringContainer();
            }

            this.allow.add(allow);

            return this;
        }

        public Builder allowEvents(final String allowEvent) {

            if (this.allowEvents == null) {
                this.allowEvents = createStringContainer();
            }

            this.allowEvents.add(allowEvent);

            return this;
        }

        public Builder require(final String require) {

            if (this.require == null) {
                this.require = createStringContainer();
            }

            this.require.add(require);
            return this;
        }

        public Builder supported(final String supported) {

            if (this.supported == null) {
                this.supported = createStringContainer();
            }
            this.supported.add(supported);

            return this;
        }

        public Builder privacy(final String privacy) {

            if (this.privacies == null) {
                this.privacies = createStringContainer();
            }
            this.privacies.add(privacy);

            return this;
        }

        public Builder eTag(final String eTag) {
            this.eTag = eTag;
            return this;
        }

        public Builder ifMatch(final String ifMatch) {
            this.ifMatch = ifMatch;
            return this;
        }

        private Collection<String> createStringContainer() {
            return new ArrayList<String>(10);
        }

        private Collection<UriHeader> createUriContainer() {
            return new ArrayList<UriHeader>(10);
        }

        public Builder statusCode(final int code) {
            this.statusCode = code;
            return this;
        }

        public Builder expires(final long expires) {
            this.expires = expires;
            return this;
        }

        public Builder minExpires(final int minExpires) {
            this.minExpires = minExpires;
            return this;
        }

        public Builder reasonPhrase(final String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
            return this;
        }

        public Builder server(final String server) {
            this.server = server;
            return this;
        }

        public Builder subscriptionState(final String subscriptionState) {
            if (this.subscriptionState == null) {
                this.subscriptionState = new ParamHeader.ParamHeaderBuilder();
                this.subscriptionState.paramsList(new ParamListDefaultImpl());
            }
            this.subscriptionState.value(subscriptionState);

            return this;
        }

        public ParamHeader.ParamHeaderBuilder getSubscriptionStateBuilder() {
            return subscriptionState;
        }

        public Builder contentType(final String contentType) {
            ensureContentTypeExists();

            this.contentType.value(contentType);

            return this;
        }

        public ParamHeader.ParamHeaderBuilder getContentTypeBuilder() {

            ensureContentTypeExists();

            return contentType;
        }

        private void ensureContentTypeExists() {
            if (this.contentType == null) {
                this.contentType = new ParamHeader.ParamHeaderBuilder();
                this.contentType.paramsList(new ParamListDefaultImpl());
            }
        }

        public Builder event(final String event) {
            if (this.event == null) {
                this.event = new ParamHeader.ParamHeaderBuilder();
                this.event.paramsList(new ParamListDefaultImpl());
            }
            this.event.value(event);

            return this;
        }

        public ParamHeader.ParamHeaderBuilder getEventBuilder() {
            return event;
        }

        public Builder userAgent(final String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        public ContactsList.Builder getContactsBuilder() {
            if (this.contacts == null) {
                this.contacts = new ContactsList.Builder();
            }
            return this.contacts;
        }

        public SessionExpiresHeader.Builder getSessionExpiresBuilder() {

            if (this.sessionExpires == null) {
                this.sessionExpires = new SessionExpiresHeader.Builder();
            }

            return sessionExpires;
        }

        public Builder method(final String method) {
            this.method = method;
            return this;
        }

        public String getMethod() {
            return method;
        }

        public Builder customHeader(final Header customHeaderName, final String value) {
            return customHeader(customHeaderName.stringValue(), value);
        }

        public Builder customHeader(final String customHeaderName, final String value) {
            if (this.customHeaders == null) {
                this.customHeaders = new MultiValueMap<String, String>();
            }

            this.customHeaders.put(customHeaderName, value);

            String trimmedValue = value.trim();
            if ((isAuthHeader(customHeaderName)) &&
                    trimmedValue != null) {

                //Logger.log(Logger.Tag.PARSER,"Get auth info: "+trimmedValue);
                AuthenticationChallenge challenge = ((AuthenticationChallenge) ChallengeParser.consume(trimmedValue));
                AuthChallenge finalChallenge;

                if (customHeaderName.equals(Header.WWW_Authenticate.stringValue())) {
                    finalChallenge = challenge.copyOf(ChallengeType.UAS);
                }
                else {
                    finalChallenge = challenge.copyOf(ChallengeType.PROXY);
                }

                ensureChallengeMapExists();
                challengeMap.put(finalChallenge.getChallengeType(), finalChallenge);
            }

            return this;
        }

        private boolean isAuthHeader(final String headerName) {
            return Header.WWW_Authenticate.testAgainst(headerName) ||
                Header.Proxy_Authenticate.testAgainst(headerName);
        }

        public Builder via(final Via.Builder via) {

            if (vias == null) {
                vias = new ArrayList<Via.Builder>();
            }

            vias.add(via);

            return this;
        }

        public Builder via(final Via via) {

            if (vias == null) {
                vias = new ArrayList<Via.Builder>();
            }

            vias.add(new Via.Builder(via));

            return this;
        }

        public Builder resetVia() {

            if (vias == null) {
                vias = new ArrayList<Via.Builder>();
            }

            vias.clear();

            return this;
        }

        public Collection<Via.Builder> getVias() {
            return vias;
        }

        public Builder rejectContact(final ParamList paramsList) {
            if (this.rejectContact == null) {
                this.rejectContact = new ParamListDefaultImpl();
            }
            this.rejectContact.merge(paramsList);

            return this;
        }

        public Builder acceptContact(final String key, final String value) {
            if (this.acceptContact == null) {
                this.acceptContact = new ParamListDefaultImpl();
            }
            this.acceptContact.set(new Param(key, value));

            return this;
        }

        public BaseSipMessage build() {
            if (type == null) {
                throw new IllegalStateException("Message type doesn't set.");
            }
            return type == Type.RESPONSE ? new Response(this) : new Request(this);
        }

        public void authenticationChallenge(final Map<ChallengeType, AuthChallenge> challengeMap) {
            ensureChallengeMapExists();

            this.challengeMap = challengeMap;
        }

        private void ensureChallengeMapExists() {
            if (this.challengeMap == null) {
                this.challengeMap = new HashMap<ChallengeType, AuthChallenge>();
            }
        }

        public String shortDescription() {
            return null;
        }
    }

    public static void main(String[] args) {
        String msg = "OPTIONS sip:12345678@10.0.2.15:5061;transport=UDP SIP/2.0\r\n" +
                "To: <sip:12345678@dummy.com>\r\n" +
                "Max-Forwards: 69\r\n" +
                "Supported: 100rel\r\n" +
                "User-Agent: Movial\r\n" +
                "P-Access-Network-Info:  3GPP-GERAN\r\n" +
                "Call-ID: 8f1b9dbf-fd95-4345-bb55-2ca5d67fc802@10.0.2.15\r\n" +
                "From: <sip:123454678@dummy.com>;tag=29f1\r\n" +
                "CSeq: 2 OPTIONS\r\n" +
                "Content-Length: 0\r\n" +
                "Via: SIP/2.0/UDP 66.94.28.100:5060;branch=z9hG4bKmcacja30e8chbjoig1o0\r\n" +
                "Contact: <sip:66.94.28.100:5060;transport=udp>\r\n" +
                "P-Asserted-Identity: <sip:12345678@dummy.com>\r\n\r\n";

        final BaseSipMessage sipMessage = MessageParser.parse(msg);
        System.out.println(""+getMessageBranch(sipMessage));
    }

    public int getContentLength() {
        return contentLength;
    }
    
    @Override
    public boolean isExpired() {
        return expired.get();
    }
    
    @Override
    public void expire() {
        expired.compareAndSet(false, true);
    }
}
