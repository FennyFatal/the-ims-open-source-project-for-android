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

import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.Protocol;
import javax.microedition.ims.transport.messagerouter.Route;
import java.util.*;


/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 16-Dec-2009
 * Time: 18:56:30
 */

public class DefaultRoute implements Route {
    protected final String dstHost;
    protected final int dstPort;
    protected final int localPort;
    protected final Protocol transportType;
    protected final IMSEntityType entityType;
    protected final Collection<Protocol> simultaneousProtocols;

    public DefaultRoute(final String dstHost,
                        final int dstPort,
                        final int localPort,
                        final Protocol transportType,
                        final IMSEntityType entityType) {

        this(dstHost, dstPort, localPort, transportType, entityType, null);
    }

    public DefaultRoute(final String dstHost,
                        final int dstPort,
                        final int localPort,
                        final Protocol transportType,
                        final IMSEntityType entityType,
                        final Collection<Protocol> simultaneousProtocols) {

        this.dstHost = dstHost;
        this.dstPort = dstPort;
        this.localPort = localPort;
        this.transportType = transportType;
        this.entityType = entityType;

        final Set<Protocol> protocolSet = simultaneousProtocols == null || simultaneousProtocols.isEmpty() ?
                Collections.<Protocol>emptySet() :
                EnumSet.copyOf(simultaneousProtocols);
        protocolSet.remove(transportType);
        this.simultaneousProtocols = Collections.unmodifiableSet(protocolSet);
    }

    public static Route copyOf(final Route route, final Protocol protocol) {

        final HashSet<Protocol> protocols = new HashSet<Protocol>(route.getSimultaneousRoutes());
        protocols.remove(protocol);
        protocols.add(route.getTransportType());

        return new DefaultRoute(
                route.getDstHost(),
                route.getDstPort(),
                route.getLocalPort(),
                protocol,
                route.getEntityType(),
                protocols
        );
    }

    public static Route copyOf(final Route route, final Collection<Protocol> simultaneousProtocols) {
        return new DefaultRoute(
                route.getDstHost(),
                route.getDstPort(),
                route.getLocalPort(),
                route.getTransportType(),
                route.getEntityType(),
                simultaneousProtocols
        );
    }

    public String getDstHost() {
        return dstHost;
    }

    public int getDstPort() {
        return dstPort;
    }

    public int getLocalPort() {
        return localPort;
    }


    public Protocol getTransportType() {
        return transportType;
    }

    public IMSEntityType getEntityType() {
        return entityType;
    }

    public Collection<Protocol> getSimultaneousRoutes() {
        return simultaneousProtocols;
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultRoute that = (DefaultRoute) o;

        if (localPort != that.localPort) {
            return false;
        }
        if (dstHost != null ? !dstHost.equals(that.dstHost) : that.dstHost != null) {
            return false;
        }
        if (entityType != that.entityType) {
            return false;
        }
        if (transportType != that.transportType) {
            return false;
        }

        return true;
    }


    public int hashCode() {
        int result = dstHost != null ? dstHost.hashCode() : 0;
        result = 31 * result + localPort;
        result = 31 * result + (transportType != null ? transportType.hashCode() : 0);
        result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
        return result;
    }


    public String toString() {
        return "DefaultRoute{" +
                "dstHost='" + dstHost + '\'' +
                ", dstPort=" + dstPort +
                ", localPort=" + localPort +
                ", transportType=" + transportType +
                ", entityType=" + entityType +
                ", simultaneousProtocols=" + simultaneousProtocols +
                '}';
    }
}
