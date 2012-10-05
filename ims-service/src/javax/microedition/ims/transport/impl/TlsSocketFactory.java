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

package javax.microedition.ims.transport.impl;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.transport.messagerouter.Route;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class TlsSocketFactory implements SocketFactory {
    private static final String TAG = "TlsSocketFactory";
    private final ConnectionSecurityInfoProvider securityInfoProvider;

    public TlsSocketFactory(final ConnectionSecurityInfoProvider securityInfoProvider) {
        if (securityInfoProvider == null) {
            throw new NullPointerException("ConnectionSecurityInfoProvider is " + securityInfoProvider);
        }
        this.securityInfoProvider = securityInfoProvider;
    }

    public Socket createSocket(Route route) throws IOException {
        final Socket retValue;

        SSLSocket tcpSocket;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = new TrustManager[]{new TestTrustManager()};

            sslContext.init(null, trustManagers, null);
            Logger.log(TAG, "Context inited");

            tcpSocket = (SSLSocket) sslContext.getSocketFactory().createSocket();
            Logger.log(TAG, "Socket created");

            tcpSocket.connect(new InetSocketAddress(route.getDstHost(), route.getDstPort()), CONNECT_TIMEOUT);
            Logger.log(TAG, "Socket connected");

            tcpSocket.startHandshake();
            Logger.log(TAG, "Socket handshaked");
        } catch (NoSuchAlgorithmException e) {
            Logger.log(TAG, "createSocket#" + e.getMessage());
            throw new SSLException(e.getMessage());
        } catch (KeyManagementException e) {
            Logger.log(TAG, "createSocket#" + e.getMessage());
            throw new SSLException(e.getMessage());
        }/*catch (IOException e){
            Logger.log(TAG, "createSocket#" + e.getMessage());
            throw new SSLException(e.getMessage());
        }*/

        retValue = tcpSocket;

        return retValue;
    }

    class TestTrustManager implements X509TrustManager {
        private static final String STRING_TO_CHECK = "T-Mobile";
        private X509Certificate[] chain;

        private String authType;

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            this.chain = chain;
            this.authType = authType;
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.chain = chain;
            this.authType = authType;

            //Logger.log(Logger.Tag.WARNING, "THROW EXCEPTION");
            
            //throw new CertificateException(certMessage);
            
            if (chain == null || chain.length == 0) {
                throw new CertificateException("Empty certificate chain " + chain);
            } else {

                Logger.log(TAG, "*** TestTrustManager.checkServerTrusted#before - ConnectionSecurityInfoProviderImplDefault.obtainSecurityInfo()");
                ConnectionSecurityInfo connectionSecurityInfo = securityInfoProvider.obtainSecurityInfo();
                Logger.log(TAG,
                        String.format("*** TestTrustManager.checkServerTrusted#after - " +
                                "ConnectionSecurityInfoProviderImplDefault.obtainSecurityInfo(), connectionSecurityInfo = " + connectionSecurityInfo));

                if (ConnectionSecurityInfo.AddrCheckMode.CHECK_EVERY_ADDRESS == connectionSecurityInfo.getAddrCheckMode()) {
                    Collection<String> allowedRemoteAddresses = connectionSecurityInfo.getAllowedRemoteAddresses();

                    X509Certificate x509Certificate = chain[0];
                    String name = x509Certificate.getSubjectDN().getName();
                    String address = name.replaceFirst("CN=", "").replaceFirst(",.*", "");

                    if (/*!name.contains(STRING_TO_CHECK)||*/
                            !allowedRemoteAddresses.contains(address)) {

                        String errMsg = "Wrong certificate subject " + name;
                        Logger.log(Logger.Tag.WARNING, errMsg);
                        throw new CertificateException(errMsg);
                    }
                }
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public X509Certificate[] getChain() {
            return chain;
        }

        public String getAuthType() {
            return authType;
        }
    }

}
