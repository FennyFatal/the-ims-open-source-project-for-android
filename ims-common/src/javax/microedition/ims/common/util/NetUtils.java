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

package javax.microedition.ims.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 08-Jan-2010
 * Time: 10:56:48
 */
public final class NetUtils {
    private static final String LOCALHOST = "localhost";

    private NetUtils() {
    }
/*    public static String getUACIP() {
        String retValue;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            retValue = addr.getHostAddress();
        }
        catch (UnknownHostException e) {
            retValue = null;
        }

        return retValue;
    }
    private static String uacIp;
*/


    public static String getUACHostName() {
        String retValue;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            retValue = addr.getHostName();
        }
        catch (UnknownHostException e) {
            retValue = null;
        }

        return retValue;
    }

    public static Map<String, String> listNetInterfaces() {

        Map<String, String> retValue = new LinkedHashMap<String, String>(10);

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                retValue.put(intf.getName(), intf.getDisplayName());
            }
        }
        catch (SocketException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return retValue.size() == 0 ? Collections.<String, String>emptyMap() : retValue;
    }

    private static List<String> getNetInterfaceAddresses(final String name) {

        List<String> retValue = null;

        if (name != null) {
            try {
                NetworkInterface intrf = NetworkInterface.getByName(name);

                if (intrf != null) {

                    retValue = new ArrayList<String>(10);

                    Enumeration<InetAddress> enumIpAddr = intrf.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            retValue.add(inetAddress.getHostAddress());
                        }
                    }
                }
            }
            catch (SocketException e) {
                e.printStackTrace();
            }
        }

        if (retValue != null) {
            retValue = retValue.size() == 0 ? Collections.<String>emptyList() : retValue;
        }

        return retValue;
    }

    public static String guessCurrentAddress() {

        String retValue = LOCALHOST;

        final Map<String, String> runningInterfaces = listNetInterfaces();
        if (runningInterfaces.size() > 0) {

            //String randomInterfaceName = runningInterfaces.keySet().toArray(new String[1])[0];

            for (String interfaceName : runningInterfaces.keySet()) {
                String displayName = runningInterfaces.get(interfaceName);

                if (!displayName.matches(".*[Ll]oop[Bb]ack.*")) {
                    final List<String> ipList = getNetInterfaceAddresses(interfaceName);
                    if (ipList.size() > 0) {

                        boolean haveFound = false;
                        for (String ipItem : ipList) {
                            if (ipItem.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}")) {
                                retValue = ipItem;
                                haveFound = true;
                                break;
                            }
                        }
                        if (haveFound) {
                            break;
                        }
                    }
                }
            }
        }
        return retValue;
        //for testing emulates IP change
        //return new String []{"127.0.0.1","127.0.0.2","127.0.0.3","127.0.0.4"}[((int)(Math.random()*100))%4];
    }
    
    public static boolean isAddressLocal(String address) {
        return LOCALHOST.equals(address);
    }

/*    public static String convertToString(ServerAddress serverAddress) {

        String hostName = serverAddress.getHostName();
        if (hostName == null || "".equals(hostName.trim())) {
            hostName = serverAddress.getIpAddress();
        }

        return hostName;
    }
*/
}
