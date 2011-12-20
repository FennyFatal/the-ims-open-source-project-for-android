package javax.microedition.ims.transport.impl;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 3/17/11
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ConnectionSecurityInfo {
    enum AddrCheckMode {
        CHECK_EVERY_ADDRESS,
        ALLOW_ALL
    }

    Collection<String> getAllowedRemoteAddresses();
    AddrCheckMode getAddrCheckMode();
}
