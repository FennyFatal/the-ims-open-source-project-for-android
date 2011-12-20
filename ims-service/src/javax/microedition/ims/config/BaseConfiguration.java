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

package javax.microedition.ims.config;

import javax.microedition.ims.FeatureMapper;
import javax.microedition.ims.common.*;
import javax.microedition.ims.core.sipservice.PrivacyInfo;
import javax.microedition.ims.core.xdm.XDMConfig;
import javax.microedition.ims.messages.wrappers.sip.Refresher;
import java.util.*;

/**
 * This class responsible for representation Configuration object.
 *
 * @author Andrei Khomushko
 */
public final class BaseConfiguration implements Configuration {
    private static final List<String> DEFAULT_SPECIAL_URI_LIST = Arrays.asList("urn:service:sos");

    private final int localPort;
    private final UserInfo preferredIdentity;
    private final ServerAddress proxyServer;
    private final ServerAddress registrarServer;
    private final UserInfo authUserName;
    private final UserInfo registrationName;
    private final UserPassword userPassword;
    private final String realm;
    private final String userAgent;
    private final Protocol connectionType;
    private final int maxForwards;
    private final boolean useRPort;
    private final boolean forceSrtp;
    private final boolean useDnsLookup;
    private final boolean useResourceReservation;
    private final long registrationExpirationSeconds;
    //private final boolean forceAuthorization;
    private final boolean useSimultaneousAuth;
    private final Collection<ChallengeType> authForceTypes = new HashSet<ChallengeType>();

    //private final String[] requiredFeatures;
    private final Set<OptionFeature> supportedFeatures;

    private final XDMConfig xdmConfig;
    private final PrivacyInfo privacyInfo;
    private final boolean globalIpDiscovery;
    private final int msrpChunkSize;
    private final int msrpLocalPort;

    private long sessionExpires, minSessionExpires;
    private Refresher refresher;
    private boolean useInviteRefresh;
    private boolean useSimultaneousConnection;
    private FeatureMapper featureMapping;
    private ArrayList<String> specialUris;
    private final DtmfPayloadType dtmfPayload;

    private BaseConfiguration(ConfigurationBuilder builder) {

        this.localPort = builder.localPort;

        if (builder.proxyServer == null) {
            throw new IllegalArgumentException(
                    "The proxyServer parameter is null");
        }
        this.proxyServer = builder.proxyServer;

        if (builder.registrarServer == null) {
            throw new IllegalArgumentException(
                    "The registrarServer parameter is null");
        }
        this.registrarServer = builder.registrarServer;

        if (builder.authUserName == null) {
            throw new IllegalArgumentException(
                    "The authUserName parameter is null");
        }
        this.authUserName = builder.authUserName;

        if (builder.registrationName == null) {
            throw new IllegalArgumentException(
                    "The registrationName parameter is null");
        }
        this.registrationName = builder.registrationName;

        if (builder.userPassword == null) {
            throw new IllegalArgumentException(
                    "The userPassword parameter is null");
        }
        this.userPassword = builder.userPassword;

        if (builder.realm == null) {
            throw new IllegalArgumentException("The realm parameter is null");
        }
        this.realm = builder.realm;

        if (builder.userAgent == null) {
            throw new IllegalArgumentException(
                    "The userAgent parameter is null");
        }
        this.userAgent = builder.userAgent;

        if (builder.connectionType == null) {
            throw new IllegalArgumentException(
                    "The connectionType parameter is null");
        }
        this.connectionType = builder.connectionType;

        if (builder.maxForwards < 0) {
            throw new IllegalArgumentException(
                    "The maxForwards parameter is negative");
        }
        this.maxForwards = builder.maxForwards;

        this.useRPort = builder.useRPort;
        this.forceSrtp = builder.forceSrtp;
        this.useDnsLookup = builder.useDNSLookup;
        this.useResourceReservation = builder.useResourceReservation;
        this.globalIpDiscovery = builder.globalIpDiscovery;

        if (builder.registrationExpirationSeconds < 0) {
            throw new IllegalArgumentException(
                    "The registrationExpirationSeconds parameter is negative");
        }
        this.registrationExpirationSeconds = builder.registrationExpirationSeconds;

        //this.forceAuthorization = builder.forceAuthorization;
        this.useSimultaneousAuth = builder.useSimultaneousAuth;
        this.authForceTypes.addAll(builder.authForceTypes);

        this.supportedFeatures = builder.supportedFeatures;
        //this.requiredFeatures = builder.requiredFeatures.toArray(new String[0]);

        if (builder.xdmConfig == null) {
            throw new IllegalArgumentException(
                    "The xdmConfig parameter is null");
        }
        this.xdmConfig = builder.xdmConfig;

        if (builder.privacyInfo == null) {
            throw new IllegalArgumentException(
                    "The privacyInfo parameter is null");
        }
        this.privacyInfo = builder.privacyInfo;

        // we allow null values as if user doesn't have preferred identity. But
        // if he has one it must comply with RFC3261.
        // SipUriParsers check this.
        // if (builder.preferredIdentity != null &&
        // SipUriParser.parseUri(builder.preferredIdentity) == null) {
        // final String errMsg =
        // "preferredIdentity doesn't comply with RFC 3261. Now it has value '"
        // + builder.preferredIdentity + "'";
        // throw new IllegalArgumentException(errMsg);
        // }
        this.preferredIdentity = builder.preferredIdentity;

        if (builder.msrpChunkSize < 0) {
            throw new IllegalArgumentException(
                    "The msrpChunkSize parameter is negative");
        }
        this.msrpChunkSize = builder.msrpChunkSize;

        if (builder.msrpLocalPort < 0 || builder.msrpLocalPort > 65536) {
            throw new IllegalArgumentException(
                    "The msrpLocalPort < 0 || msrpLocalPort > 65536");
        }
        this.msrpLocalPort = builder.msrpLocalPort;
        this.refresher = builder.refresher;
        this.sessionExpires = builder.sessionExpires;
        this.minSessionExpires = builder.minSessionExpires;
        this.useInviteRefresh = builder.useInviteRefresh;
        this.useSimultaneousConnection = builder.useSimultaneousConnection;

        if (builder.featureMapping == null) {
            throw new IllegalArgumentException(
                    "The featureMapping parameter is null");
        }
        this.featureMapping = builder.featureMapping;

        this.specialUris = builder.specialUris == null ? null : new ArrayList<String>(builder.specialUris);

        this.dtmfPayload = builder.dtmfPayload;
    }

    public static class ConfigurationBuilder {
        private int localPort;
        private ServerAddress proxyServer;
        private ServerAddress registrarServer;
        private UserInfo authUserName;
        private UserInfo registrationName;
        private UserPassword userPassword;
        private String realm;
        private String userAgent;
        private Protocol connectionType;
        private int maxForwards;
        private boolean useRPort;
        private boolean forceSrtp;
        private boolean useDNSLookup;
        private boolean useResourceReservation;
        private long registrationExpirationSeconds;
        private boolean useSimultaneousAuth;
        private final Collection<ChallengeType> authForceTypes = new HashSet<ChallengeType>();
        // private boolean isPrackSupported = true;

        //private final Set<String> requiredFeatures = new HashSet<String>();
        private final Set<OptionFeature> supportedFeatures = new HashSet<OptionFeature>();

        private XDMConfig xdmConfig;
        private PrivacyInfo privacyInfo;
        private UserInfo preferredIdentity;
        private boolean globalIpDiscovery;
        private int msrpChunkSize, msrpLocalPort;
        private long sessionExpires, minSessionExpires;
        private Refresher refresher;
        private boolean useInviteRefresh;
        private boolean useSimultaneousConnection;
        private FeatureMapper featureMapping;
        private Collection<String> specialUris;
        private DtmfPayloadType dtmfPayload = DtmfPayloadType.INBAND;

        public ConfigurationBuilder() {
            specialUris = new ArrayList<String>(DEFAULT_SPECIAL_URI_LIST);
        }

        public ConfigurationBuilder(Configuration configuration)
                throws IllegalArgumentException {
            buildLocalPort(configuration.getLocalPort());
            buildProxyServer(configuration.getProxyServer());
            buildRegistrarServer(configuration.getRegistrarServer());
            buildAuthUserName(configuration.getAuthUserName());
            buildRegistrationName(configuration.getRegistrationName());
            buildUserPassword(configuration.getUserPassword());
            buildRealm(configuration.getRealm());
            buildUserAgent(configuration.getUserAgent());
            buildConnectionType(configuration.getConnectionType());
            buildMaxForwards(configuration.getMaxForwards());
            buildUseRPort(configuration.useRPort());
            //buildForceSrtp(configuration.forceSrtp());
            buildUseDNSLookup(configuration.useDNSLookup());
            buildUseResourceReservation(configuration.useResourceReservation());
            buildRegistrationExpirationSeconds(configuration
                    .getRegistrationExpirationSeconds());
            //buildForceAuthorization(configuration.forceAuthorization());
            buildUseSimultaneousAuth(configuration.useSimultaneousAuth());
            buildAuthForceTypes(configuration.getAuthForceTypes().toArray(new ChallengeType[0]));
            buildSupportedFeature(configuration.getSupportedFeatures());
            //buildRequiredFeature(configuration.getRequiredFeatures());
            buildXdmConfig(configuration.getXDMConfig());
            buildPrivacyInfo(configuration.getPrivacyInfo());
            buildPreferredIdentity(configuration.getPreferredIdentity());
            buildGlobalIpDiscovery(configuration.globalIpDiscovery());
            buildMsrpLocalPort(configuration.getMsrpLocalPort());
            buildMsrpChunkSize(configuration.getMsrpChunkSize());
            buildSessionExpires(configuration.getSessionExpiresTime());
            buildMinSessionExpires(configuration.getMinSessionExpiresTime());
            buildRefresher(configuration.getRefresher());
            buildUseInviteRefresh(configuration.useInviteRefresh());
            buildUseSimultaneousConnection(configuration.useSimultaneousConnections());
            buildFeatureMapping(configuration.getFeatureMapper());
            buildSpecialUris(configuration.getSpecialUris());
            buildDtmfPayload(configuration.getDtmfPayload());
        }

        public ConfigurationBuilder buildDtmfPayload(DtmfPayloadType dtmfPayload) {
            this.dtmfPayload = dtmfPayload;
            return this;
        }
        
        public ConfigurationBuilder buildLocalPort(int localPort) {
            this.localPort = localPort;
            return this;
        }

        public ConfigurationBuilder buildProxyServer(ServerAddress proxyServer) {
            this.proxyServer = proxyServer;
            return this;
        }

        public ConfigurationBuilder buildRegistrarServer(
                ServerAddress registrarServer) {
            this.registrarServer = registrarServer;
            return this;
        }

        public ConfigurationBuilder buildAuthUserName(UserInfo authUserName) {
            this.authUserName = authUserName;
            return this;
        }

        public ConfigurationBuilder buildRegistrationName(
                UserInfo registrationName) {
            this.registrationName = registrationName;
            return this;
        }

        public ConfigurationBuilder buildUserPassword(UserPassword userPassword) {
            this.userPassword = userPassword;
            return this;
        }

        public ConfigurationBuilder buildRealm(String realm) {
            this.realm = realm;
            return this;
        }

        public ConfigurationBuilder buildUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public ConfigurationBuilder buildConnectionType(Protocol connectionType) {
            this.connectionType = connectionType;
            return this;
        }

        public ConfigurationBuilder buildMaxForwards(int maxForwards) {
            this.maxForwards = maxForwards;
            return this;
        }

        public ConfigurationBuilder buildUseRPort(boolean useRPort) {
            this.useRPort = useRPort;
            return this;
        }

        public ConfigurationBuilder buildForceSrtp(boolean forceSrtp) {
            this.forceSrtp = forceSrtp;
            return this;
        }

        public ConfigurationBuilder buildUseDNSLookup(final boolean useDNSLookup) {
            this.useDNSLookup = useDNSLookup;
            return this;
        }
        
        public ConfigurationBuilder buildUseResourceReservation(
                boolean useResourceReservation) {
            this.useResourceReservation = useResourceReservation;
            return this;
        }

        public ConfigurationBuilder buildGlobalIpDiscovery(
                boolean globalIpDiscovery) {
            this.globalIpDiscovery = globalIpDiscovery;
            return this;
        }

        public ConfigurationBuilder buildRegistrationExpirationSeconds(
                long registrationExpirationSeconds) {
            this.registrationExpirationSeconds = registrationExpirationSeconds;
            return this;
        }

/*        public ConfigurationBuilder buildForceAuthorization(
                boolean forceAuthorization) {
            this.forceAuthorization = forceAuthorization;
            return this;
        }
*/

        public ConfigurationBuilder buildUseSimultaneousAuth(boolean useSimultaneousAuth) {
            this.useSimultaneousAuth = useSimultaneousAuth;
            return this;
        }


        public ConfigurationBuilder buildAuthForceTypes(
                ChallengeType... challengeTypes) {
            this.authForceTypes.addAll(Arrays.asList(challengeTypes));
            return this;
        }

        /*
        * public ConfigurationBuilder buildPrackSupported(boolean
        * isPrackSupported) { this.isPrackSupported = isPrackSupported; return
        * this; }
        */

        public ConfigurationBuilder buildSupportedFeature(
                OptionFeature... supportedFeatures) {
            this.supportedFeatures.addAll(Arrays.asList(supportedFeatures));
            return this;
        }

/*        public ConfigurationBuilder buildRequiredFeature(
                String... requiredFeatures) {
            this.requiredFeatures.addAll(Arrays.asList(requiredFeatures));
            return this;
        }
*/

        public ConfigurationBuilder buildXdmConfig(XDMConfig xdmConfig) {
            this.xdmConfig = xdmConfig;
            return this;
        }

        public ConfigurationBuilder buildPrivacyInfo(PrivacyInfo privacyInfo) {
            this.privacyInfo = privacyInfo;
            return this;
        }

        public ConfigurationBuilder buildPreferredIdentity(
                final UserInfo preferredIdentity) {
            this.preferredIdentity = preferredIdentity;
            return this;
        }

        public ConfigurationBuilder buildMsrpChunkSize(int msrpChunkSize) {
            this.msrpChunkSize = msrpChunkSize;
            return this;
        }

        public ConfigurationBuilder buildMsrpLocalPort(int msrpLocalPort) {
            this.msrpLocalPort = msrpLocalPort;
            return this;
        }

        public ConfigurationBuilder buildRefresher(Refresher refresher) {
            this.refresher = refresher;
            return this;
        }

        public ConfigurationBuilder buildSessionExpires(long sessionExpires) {
            this.sessionExpires = sessionExpires;
            return this;
        }

        public ConfigurationBuilder buildMinSessionExpires(long minSessionExpires) {
            this.minSessionExpires = minSessionExpires;
            return this;
        }

        public ConfigurationBuilder buildUseInviteRefresh(boolean useInviteRefresh) {
            this.useInviteRefresh = useInviteRefresh;
            return this;
        }

        public ConfigurationBuilder buildUseSimultaneousConnection(boolean useSimultaneousConnection) {
            this.useSimultaneousConnection = useSimultaneousConnection;
            return this;
        }

        public ConfigurationBuilder buildFeatureMapping(FeatureMapper featureMapping) {
            this.featureMapping = featureMapping;
            return this;
        }

        public ConfigurationBuilder buildSpecialUris(final Collection<String> specialUris) {
            this.specialUris = specialUris;
            return this;
        }

        /**
         * Build configuration using builder parameters
         *
         * @return configuration
         */
        public Configuration build() {
            return new BaseConfiguration(this);
        }
    }

    public int getLocalPort() {
        return localPort;
    }

    public ServerAddress getProxyServer() {
        return proxyServer;
    }

    public String getRealm() {
        return realm;
    }

    public ServerAddress getRegistrarServer() {
        return registrarServer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public UserInfo getAuthUserName() {
        return authUserName;
    }

    public UserPassword getUserPassword() {
        return userPassword;
    }

    public Protocol getConnectionType() {
        return connectionType;
    }

    public int getMaxForwards() {
        return maxForwards;
    }

    public boolean useRPort() {
        return useRPort;
    }

/*
    public boolean forceAuthorization() {
        return forceAuthorization;
    }
*/
    public boolean useSimultaneousAuth() {
        return useSimultaneousAuth;
    }

    public Collection<ChallengeType> getAuthForceTypes() {
        return authForceTypes;
    }

    public long getRegistrationExpirationSeconds() {
        return registrationExpirationSeconds;
    }

    public UserInfo getRegistrationName() {
        return registrationName;
    }

    /*
     *  public boolean isPrackSupported() { return isPrackSupported; }
     */

/*
    public String[] getRequiredFeatures() {
        return requiredFeatures;
    }
*/
    public OptionFeature[] getSupportedFeatures() {
        return supportedFeatures.toArray(new OptionFeature[supportedFeatures.size()]);
    }

    public XDMConfig getXDMConfig() {
        return xdmConfig;
    }

    public boolean useResourceReservation() {
        return useResourceReservation;
    }

    public PrivacyInfo getPrivacyInfo() {
        return privacyInfo;
    }

    public UserInfo getPreferredIdentity() {
        return preferredIdentity;
    }

    public boolean globalIpDiscovery() {
        return globalIpDiscovery;
    }

    public int getMsrpChunkSize() {
        return msrpChunkSize;
    }

    public int getMsrpLocalPort() {
        return msrpLocalPort;
    }

    public long getSessionExpiresTime() {
        return sessionExpires;
    }

    public long getMinSessionExpiresTime() {
        return minSessionExpires;
    }

    public Refresher getRefresher() {
        return refresher;
    }

    public boolean useInviteRefresh() {
        return useInviteRefresh;
    }

    public boolean useSimultaneousConnections() {
        return useSimultaneousConnection;
    }

    public FeatureMapper getFeatureMapper() {
        return featureMapping;
    }

    public boolean isFeatureSupported(OptionFeature feature) {
        return supportedFeatures.contains(feature);
    }

    public Collection<String> getSpecialUris() {
        return specialUris;
    }
    
    public boolean forceSrtp() {
        return forceSrtp;
    }

    @Override
    public boolean useDNSLookup() {
        return useDnsLookup;
    }

    public DtmfPayloadType getDtmfPayload() {
        return dtmfPayload;
    }

    @Override
    public String toString() {
        return "BaseConfiguration [localPort=" + localPort + ", preferredIdentity="
                + preferredIdentity + ", proxyServer=" + proxyServer + ", registrarServer="
                + registrarServer + ", authUserName=" + authUserName + ", registrationName="
                + registrationName + ", userPassword=" + userPassword + ", realm=" + realm
                + ", userAgent=" + userAgent + ", connectionType=" + connectionType
                + ", maxForwards=" + maxForwards + ", useRPort=" + useRPort + ", forceSrtp="
                + forceSrtp + ", useResourceReservation=" + useResourceReservation
                + ", useDNSLookup="+useDnsLookup
                + ", registrationExpirationSeconds=" + registrationExpirationSeconds
                + ", useSimultaneousAuth=" + useSimultaneousAuth + ", authForceTypes="
                + authForceTypes + ", supportedFeatures=" + supportedFeatures + ", xdmConfig="
                + xdmConfig + ", privacyInfo=" + privacyInfo + ", globalIpDiscovery="
                + globalIpDiscovery + ", msrpChunkSize=" + msrpChunkSize + ", msrpLocalPort="
                + msrpLocalPort + ", sessionExpires=" + sessionExpires + ", minSessionExpires="
                + minSessionExpires + ", refresher=" + refresher + ", useInviteRefresh="
                + useInviteRefresh + ", useSimultaneousConnection=" + useSimultaneousConnection
                + ", featureMapping=" + featureMapping 
                + ", specialUris=" + specialUris + ", dtmfPayload=" + dtmfPayload + "]";
    }
}
