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
package javax.microedition.ims.dns;

import org.xbill.DNS.*;

import javax.microedition.ims.common.ConnectionData;
import javax.microedition.ims.common.ConnectionDataDefaultImpl;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Logger.Tag;
import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.ReasonCode;
import javax.microedition.ims.core.connection.NetworkType;
import javax.microedition.ims.core.env.ConnectionManager;

import java.net.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 2/16/11
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */

//more info in article 'Tutorial: SIP using NAPTR and SRV DNS Records'
//at http://anders.com/cms/264/SIP/DNS/NAPTR/SRV/dnscache/tinydns
public final class DNSResolverDNSJavaImpl implements DNSResolver {

    //private static final int ICMP_TIMEOUT = 32000;
    private static final String DEFAULT_DATA_CONNECTION_PROXY_SERVER = "pcf.sipgeo.t-mobile.com.";
    private static final String DEFAULT_WIFI_PROXY_SERVER = "sba.sipgeo.t-mobile.com.";
    private static final int DEFAULT_TLS_PORT = 5061;
    private static final int DEFAULT_NON_TLS_PORT = 5060;

    private static final String SERVICE_NAME_SIP = "SIP";
    private static final String SERVICE_NAME_SIPS = "SIPS";

    private static final String TCP_PROTOCOL_MARKER = "T";
    private static final String UDP_PROTOCOL_MARKER = "U";

    private static final String LOG_PREFIX = "DNS";

    private final Configuration mConfiguration;
    private final ConnectionManager mConnectionManager;
    // the field to store the connection data object
    // the uniqueness guarantees that we shall use the same A-record (DNS lookup) all the time
    // even in case the lookup might return multiple A-records
    //TODO we can't cache dns lookup result due the reason that network interface can be changed and cached value becames invalid
    private AtomicReference<ConnectionData> connectionData = new AtomicReference<ConnectionData>(null);
    private AtomicReference<NetworkType> mNetworkType = new AtomicReference<NetworkType>(NetworkType.WIFI);

    //Comparator to compare records like this
    //example.com NAPTR 10 100 "S" "SIP+D2U" "" _sip._udp.example.com.
    //example.com NAPTR 20 100 "S" "SIP+D2T" "" _sip._tcp.example.com.
    //The 10 refers to the preference for the record. The lower number is always tried first.
    //100 is the order and is only important if the preference numbers are the same.
    //
    //lower Preference has bigger sorting weight. The same stay true for Order if Preference is equal
    private static class NAPTRRecordComparator implements Comparator<NAPTRRecord> {
        @Override
        public int compare(NAPTRRecord record1, NAPTRRecord record2) {
            int retValue = record1.getOrder() - record2.getOrder();

            if (retValue == 0) {
                retValue = record1.getPreference() - record2.getPreference();
            }

            return retValue;
        }
    }

    //Comparator to compare records like this
    //_sip._udp.example.com SRV 5 100 5060 sip-udp01.example.com.
    //_sip._udp.example.com SRV 10 100 5060 sip-udp02.example.com.
    //Priority 5 before priority 10. 100 is the weight which is used to differentiate between records of the same priority
    //
    //lower Priority has bigger sorting weight. The greater Weight parameter has greater sorting weight if Priorities is equal.
    private static class SRVRecordComparator implements Comparator<SRVRecord> {
        @Override
        public int compare(SRVRecord record1, SRVRecord record2) {
            return (record1.getPriority() - record2.getPriority());
        }
    }

    private final Comparator<NAPTRRecord> naptrComparatorImportantFirst = new NAPTRRecordComparator();
    private final Comparator<SRVRecord> srvComparatorImportantFirst = new SRVRecordComparator();

    public DNSResolverDNSJavaImpl(final Configuration configuration) {
        mConfiguration = configuration;
        mConnectionManager = null;
    }

    public DNSResolverDNSJavaImpl(final Configuration configuration, final ConnectionManager connectionManager) {
        mConfiguration = configuration;
        mConnectionManager = connectionManager;
    }

/*    @Override
    public ConnectionData lookUp(final String sipUri) throws DNSException {
        UserInfo userInfo = UserInfo.valueOf(sipUri);

        if (userInfo == null) {
            throw new IllegalArgumentException(sipUri + " can not be parsed");
        }

        return doLookUp(userInfo);
    }
*/
    @Override
    public ConnectionData lookUp(final UserInfo userInfo) throws DNSException {
        return doLookUp(userInfo);
    }

    private ConnectionData doLookUp(final UserInfo userInfo) throws DNSException {
        Logger.log(Tag.COMMON, "doLookUp#userInfo = " + userInfo);
        
        ConnectionData retValue = null;
/*
        if (connectionData.get() == null) {
            Logger.log(Tag.COMMON, "doLookUp#connectionData is null");
            try {
                connectionData.set(retValue = doActualLookUp(userInfo));
            } catch (TextParseException e) {
                Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Can't fulfill lookup for " + userInfo);
                Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "" + e);
            }
        } else {
            Logger.log(Tag.COMMON, "doLookUp#connectionData is cached");
            retValue = connectionData.get();
        }
*/
        if (mConnectionManager != null) {
            mNetworkType.set(mConnectionManager.obtainNetworkTypeDirectly());
        }

        try {
            connectionData.set(retValue = doActualLookUp(userInfo));
        } catch (TextParseException e) {
            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Can't fulfill lookup for " + userInfo);
            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "" + e);
        }
        
        Logger.log(Tag.COMMON, "doLookUp#retValue = " + retValue);

        return retValue;
    }

    @Override
    public ConnectionData getLastConnectionData(UserInfo userInfo) throws DNSException {
        return connectionData.get();
    }

    private ConnectionData doActualLookUp(final UserInfo userInfo) throws TextParseException, DNSException {
        String serverName = userInfo.getDomain();

        if (mNetworkType.get() == NetworkType.WIFI) {
            serverName = "wifi." + serverName;
        }
        // lookup for records like this
        // example.com NAPTR 10 100 "S" "SIP+D2U" "" _sip._udp.example.com.
        Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "Lookup: " + serverName);

        Lookup.refreshDefault();
        Lookup naptrLookup = new Lookup(serverName, Type.NAPTR);
        //naptrLookup.setCache(cache)
        Record[] records = naptrLookup.run();
        Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "Number of NAPTR records = " +
                ((records != null) ? records.length : "0"));

        ConnectionData retValue = null;
        if (Lookup.SUCCESSFUL == naptrLookup.getResult() && records != null) {
            // here we know for sure exact type of array elements by passing Type.
            // NAPTR to Lookup constructor. Then cast is safe.
            @SuppressWarnings({"SuspiciousToArrayCall"})
            NAPTRRecord[] sortedNaptrRecords = sortRecords(
                    Arrays.asList(records).toArray(new NAPTRRecord[records.length]),
                    naptrComparatorImportantFirst
            );

            for (NAPTRRecord naptrRecord : sortedNaptrRecords) {
                Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "NAPTR Record: " + naptrRecord);
            }
            //process all records from most important to least one
            for (NAPTRRecord naptrRecord : sortedNaptrRecords) {
                retValue = processNAPTRRecord(naptrRecord);

                if (retValue != null){
                    break;
                }
            }
        } else {
            int result = naptrLookup.getResult();
            String errorString = naptrLookup.getErrorString();

            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Can't do 'NAPTR' lookup for " + serverName + " " + userInfo);
            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "'NAPTR' Lookup result = " + result + " " + errorString);

            try {
                ConnectionDataDefaultImpl.Builder builder = new ConnectionDataDefaultImpl.Builder();
                Name target = null;

                if (mNetworkType.get() == NetworkType.MOBILE) {
                    target = new Name(DEFAULT_DATA_CONNECTION_PROXY_SERVER);
                    Protocol protocol = mConfiguration.getConnectionType();

                    if (protocol == Protocol.TLS) {
                        Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Forcibly set protocol from TLS to TCP...");
                        protocol = Protocol.TCP;
                    }
                    Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Use protocol " + protocol + ", port = " + DEFAULT_NON_TLS_PORT);

                    builder.protocol(protocol);
                    builder.port(DEFAULT_NON_TLS_PORT);
                } else if (mNetworkType.get() == NetworkType.WIFI) {
                    Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Use default A Record...");
                    target = new Name(DEFAULT_WIFI_PROXY_SERVER);

                    Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Use protocol TLS, port = " + DEFAULT_TLS_PORT);
                    builder.protocol(Protocol.TLS);
                    builder.port(DEFAULT_TLS_PORT);
                }

                retValue = doProcessRecordAsTypeA(target, builder);
            } catch (TextParseException e) {
                Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Can't fulfill lookup for " +
                        (mNetworkType.get() == NetworkType.MOBILE ?
                        DEFAULT_DATA_CONNECTION_PROXY_SERVER : DEFAULT_WIFI_PROXY_SERVER));
                Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "" + e);
                throw new DNSException(ReasonCode.NAPTR_LOOKUP_FAILS, e.toString());
            }
        }

        return retValue;
    }

    private ConnectionData processNAPTRRecord(final NAPTRRecord naptrRecord) throws DNSException {
        ConnectionData retValue = null;

        // NAPTR record looks like this
        // example.com NAPTR 10 100 "S" "SIP+D2U" "" _sip._udp.example.com.
        Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "");
        Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "processing 'NAPTR' record: " + naptrRecord);

        // Service is a "SIP+D2U" part of NAPTR record
        // Currently we can only process two parted string with '+' in the middle
        // "Services" field may looks like "SIP+D2U", "SIP+D2T", "E2U+email" or something like that.
        // "SIP+D2U" is SIP over UDP, "SIP+D2T" is SIP over TCP and (you guessed it) "E2U+email" stands for email.
        // This is the application specific service optios we have to reach example.com.
        String[] serviceInfoParts = naptrRecord.getService().trim().split("\\+");

        if (serviceInfoParts != null && serviceInfoParts.length >= 2) {
            String serviceName = serviceInfoParts[0];
            String protocolInfo = serviceInfoParts[1];

            // the only supported service types are SIP and SIPS. So service name must begin with 'SIP' string.
            if (serviceName.toUpperCase().startsWith(SERVICE_NAME_SIP)) {
                // guess protocol by service name here
                Protocol protocol = determineProtocol(serviceName, protocolInfo);

                if (protocol != null) {
                    // Regular expressions and replacements are mutually exclusive.
                    // If you have one, you shouldn't have the other.
                    // The replacement is used as the "result" of the NAPTR lookup
                    // instead of mutating the original request as the regular expression.
                    // The regular expression is used to mutate the original request into something new.
                    // We're not using it here but you could use this
                    // to substitute the entire name or parts of the name used in the original query.
                    // (NOTE: These are NOT cumulative. You would never use a regular expression
                    // on the output of a NAPTR lookup, only on the original query.)

                    if (mNetworkType.get() == NetworkType.WIFI && protocol != Protocol.TLS) {
                        try {
                            ConnectionDataDefaultImpl.Builder builder = new ConnectionDataDefaultImpl.Builder();
                            Name target = new Name(DEFAULT_WIFI_PROXY_SERVER);

                            Logger.log(Logger.Tag.WARNING, LOG_PREFIX,
                                    "Use TLS forcibly because server returns non-secure SIP under WiFi connection!");
                            builder.protocol(Protocol.TLS);
                            builder.port(DEFAULT_TLS_PORT);

                            retValue = doProcessRecordAsTypeA(target, builder);
                        } catch (TextParseException e) {
                            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Can't fulfill lookup for " + DEFAULT_WIFI_PROXY_SERVER);
                            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "" + e);
                            throw new DNSException(ReasonCode.NAPTR_LOOKUP_FAILS, e.toString());
                        }
                    } else {
                        Name replacement = naptrRecord.getReplacement();
                        String replacementAsString = replacement == null ? null : replacement.toString();

                        if (replacementAsString != null && !replacementAsString.trim().equals("")) {
                            retValue = doProcessNAPTRRecord(naptrRecord, new ConnectionDataDefaultImpl.Builder().protocol(protocol));
                        } else {
                            String errMsg = "Record skipped. Unsupported replacement: " + replacement + " for record: " + naptrRecord;
                            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, errMsg);
                        }
                    }
                } else {
                    String errMsg = "Record skipped. Unsupported protocol: " + protocolInfo + " for record: " + naptrRecord;
                    Logger.log(Logger.Tag.WARNING, LOG_PREFIX, errMsg);
                }
            } else {
                String errMsg = "Record skipped. Unsupported service: " + serviceName + " for record: " + naptrRecord;
                Logger.log(Logger.Tag.WARNING, LOG_PREFIX, errMsg);
            }
        }
        return retValue;
    }

    private ConnectionData doProcessNAPTRRecord(
            final NAPTRRecord naptrRecord,
            final ConnectionDataDefaultImpl.Builder builder) throws DNSException {
        ConnectionData retValue;
        //NAPTR record looks like this
        //example.com NAPTR 10 100 "S" "SIP+D2U" "" _sip._udp.example.com.
        //
        //There are currently four possible flags:
        //"S" which denotes that an SRV lookup is to be performed on the output of this NAPTR record.
        //"A" means the result should be lookedup as an "A", "AAAA" or "A6" record.
        //A "U" means that the NAPTR result is an absolute URI that the application should process.
        //A "P" would signify a "non-terminal" rule where additional NAPTR lookups would be necessary.
        // It is application specific and can be mutated by regular expressions.
        String flags = naptrRecord.getFlags().toUpperCase();

        if (flags.contains("U")) {
            //A "U" means that the NAPTR result is an absolute URI that the application should process
            retValue = pocessRecordAsTypeU(naptrRecord, builder);
        } else if (flags.contains("A")) {
            //"A" means the result should be lookedup as an "A", "AAAA" or "A6" record
            retValue = processRecordAsTypeA(naptrRecord, builder);
        } else if (flags.contains("S")) {
            //"S" denotes that an SRV lookup is to be performed on the output of this NAPTR record
            retValue = processRecordAsTypeSRV(naptrRecord, builder);
        } else if (flags.contains("P")) {
            //A "P" would signify a "non-terminal" rule where additional NAPTR lookups would be necessary.
            //It is application specific and can be mutated by regular expressions
            //no processing algorithm for "P" at this time
            retValue = null;
        } else {
            String errMsg = flags + " is unknown DNS flags for processing. Record = " + naptrRecord + " " + builder;
            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, errMsg);
            assert false : errMsg;

            retValue = null;
        }

        return retValue;
    }

    private ConnectionData processRecordAsTypeSRV(
            final NAPTRRecord naptrRecord,
            final ConnectionDataDefaultImpl.Builder builder) throws DNSException {
        // SRV record looks like this
        // _sip._udp.example.com SRV 5 100 5060 sip-udp01.example.com.
        Lookup srvLookup = new Lookup(naptrRecord.getReplacement(), Type.SRV);
        Record[] records = srvLookup.run();
        ConnectionData retValue = null;

        Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "Number of SRV records = " +
                ((records != null) ? records.length : "0"));

        if (Lookup.SUCCESSFUL == srvLookup.getResult() && records != null) {
            //here we know for sure exact type of array elements by passing Type.SRV to Lookup constructor. Then cast is safe.
            @SuppressWarnings({"SuspiciousToArrayCall"})
            SRVRecord[] srvRecords = sortRecords(
                    Arrays.asList(records).toArray(new SRVRecord[records.length]),
                    srvComparatorImportantFirst
            );

            int priorityIndex = 0, weightSum = 0, randNum = 0;
            SRVRecord srvRecord = srvRecords[0];

            if (srvRecords.length > 1) {
                // Fine proper DNS server by SRV records...
                Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "Finding proper DNS server...");
                Random rand = new Random();

                for (int i = 0; i < srvRecords.length; i++) {
                    Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "SRV Record: " + srvRecords[i]);

                    if (srvRecords[0].getPriority() == srvRecords[i].getPriority()) {
                        weightSum += srvRecords[i].getWeight();
                    } else if (srvRecords[0].getPriority() != srvRecords[i].getPriority()) {
                        priorityIndex = i;
                        break;
                    }
                }

                randNum = rand.nextInt(weightSum);
                Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "priorityIndex = " + priorityIndex +
                        ", weightSum = " + weightSum + ", randNum = " + randNum);
                weightSum = 0;
                for (int i = 0; i < priorityIndex; i++) {
                    weightSum += srvRecords[i].getWeight();

                    if (randNum <= weightSum) {
                        srvRecord = srvRecords[i];
                        Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "Select SRV record: " + srvRecord);
                        break;
                    }
                }
            }

            Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "processing 'SRV' record: " + srvRecord);
            retValue = processRecordAsTypeA(srvRecord, builder.port(srvRecord.getPort()));
        } else {
            int result = srvLookup.getResult();
            String errorString = srvLookup.getErrorString();

            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Can't do 'SRV' lookup for " + naptrRecord.getReplacement() + " " + builder);
            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "'SRV' Lookup result = " + result + " " + errorString);

            try {
                Name target = null;

                if (mNetworkType.get() == NetworkType.MOBILE) {
                    target = new Name(DEFAULT_DATA_CONNECTION_PROXY_SERVER);
                    Protocol protocol = mConfiguration.getConnectionType();

                    if (protocol == Protocol.TLS) {
                        Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Forcibly set protocol from TLS to TCP...");
                        protocol = Protocol.TCP;
                    }
                    Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Use protocol " + protocol + ", port = " + DEFAULT_NON_TLS_PORT);

                    builder.protocol(protocol);
                    builder.port(DEFAULT_NON_TLS_PORT);
                } else if (mNetworkType.get() == NetworkType.WIFI) {
                    Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Use default A Record...");
                    target = new Name(DEFAULT_WIFI_PROXY_SERVER);

                    Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Use protocol TLS, port = " + DEFAULT_TLS_PORT);
                    builder.protocol(Protocol.TLS);
                    builder.port(DEFAULT_TLS_PORT);
                }

                retValue = doProcessRecordAsTypeA(target, builder);
            } catch (TextParseException e) {
                Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Can't fulfill lookup for " +
                        (mNetworkType.get() == NetworkType.MOBILE ?
                        DEFAULT_DATA_CONNECTION_PROXY_SERVER : DEFAULT_WIFI_PROXY_SERVER));
                Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "" + e);
                throw new DNSException(ReasonCode.NAPTR_LOOKUP_FAILS, e.toString());
            }
        }

        return retValue;
    }

    private ConnectionData processRecordAsTypeA(
            final SRVRecord srvRecord,
            final ConnectionDataDefaultImpl.Builder builder) throws DNSException {

        return doProcessRecordAsTypeA(srvRecord.getTarget(), builder);
    }

    private ConnectionData processRecordAsTypeA(final NAPTRRecord naptrRecord, final ConnectionDataDefaultImpl.Builder builder) throws DNSException {
        return doProcessRecordAsTypeA(naptrRecord.getReplacement(), builder);
    }

    private ConnectionData doProcessRecordAsTypeA(final Name target, final ConnectionDataDefaultImpl.Builder builder) throws DNSException {
        //'A' record looks like this
        //sip-udp01.example.com has address 11.22.33.44

        Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "'A' target = " + (target == null ? null : target.toString()));
        Lookup aLookup = new Lookup(target, Type.A);
        Record[] records = aLookup.run();
        Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "'A' lookup records = " + (records == null ? null : Arrays.asList(records)));

        if (Lookup.SUCCESSFUL == aLookup.getResult() && records != null) {
            @SuppressWarnings({"SuspiciousToArrayCall"})
            ARecord[] aRecords = Arrays.asList(records).toArray(new ARecord[records.length]);

            for (ARecord aRecord : aRecords) {
                Logger.log(Logger.Tag.COMMON, LOG_PREFIX, "processing 'A' record: " + aRecord);
                InetAddress address = aRecord.getAddress();

                //here we test address for liveness. If address doesn't respond we would rather choose next one to try.
                if (isReachable(address)) {
                    builder.address(address.getHostAddress());
                    break;
                }
            }
        } else {
            int result = aLookup.getResult();
            String errorString = aLookup.getErrorString();
            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "Can't do 'A' lookup for " + target + " " + builder);
            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, "'A' Lookup result = " + result + " " + errorString);

            throw new DNSException(ReasonCode.A_LOOKUP_FAILS, errorString);
        }

        return builder.build();
    }

    private ConnectionData pocessRecordAsTypeU(
            final NAPTRRecord naptrRecord,
            final ConnectionDataDefaultImpl.Builder builder) {

        //A "U" means that the NAPTR result is an absolute URI that the application should process
        //The "U" flag means that the next step
        //is not a DNS lookup but that the output of the Regexp field is an
        //URI that adheres to the 'absoluteURI' production found in the
        //ABNF of RFC 2396 [9].  Since there may be applications that use
        //NAPTR to also lookup aspects of URIs, implementors should be
        //aware that this may cause loop conditions and should act
        //accordingly.

        //Name replacement = naptrRecord.getReplacement();
        //String address = replacement.toString();//TODO: extract address from replacement
        //int port = 5060;//TODO: extract port from replacement

        //return builder.address(address).port(port).build();
        return null;
    }

    private static <T> T[] sortRecords(final T[] records, final Comparator<T> comparator) {
        Arrays.sort(records, comparator);
        return records;
    }

    private static Protocol determineProtocol(final String serviceName, final String protocolInfo) {
        Protocol retValue = null;
        //"Services" field may looks like "SIP+D2U", "SIP+D2T", "E2U+email" or something like that.
        //"SIP+D2U" is SIP over UDP, "SIP+D2T" is SIP over TCP and (you guessed it) "E2U+email" stands for email

        //SIPS means SIP Secure. The only secure protocol currently supported is TLS
        if (serviceName.toUpperCase().startsWith(SERVICE_NAME_SIPS)) {

            retValue = Protocol.TLS;
        }
        //if the service name is SIP then additional analysis required to determine actual protocol UDP or TCP
        else if (serviceName.toUpperCase().startsWith(SERVICE_NAME_SIP)) {
            if (protocolInfo.toUpperCase().endsWith(TCP_PROTOCOL_MARKER)) {
                retValue = Protocol.TCP;
            } else if (protocolInfo.toUpperCase().endsWith(UDP_PROTOCOL_MARKER)) {
                retValue = Protocol.UDP;
            }
        }


        return retValue;
    }

    private static boolean isReachable(final InetAddress address) {

        boolean retValue = false;

        /*try {
            retValue = address.isReachable(ICMP_TIMEOUT);
        } catch (IOException e) {
            String errMsg = "Error during DNS lookup testing remote host " + address + " " + e;
            Logger.log(Logger.Tag.WARNING, LOG_PREFIX, errMsg);
        }*/

        //try establish socket connection here
        if (retValue == false) {
        }

        //return retValue;
        return true;
    }

/*    public static void main(String[] args) throws DNSException {
        DNSResolver dnsResolver = new DNSResolverDNSJavaImpl();
    }
*/}
