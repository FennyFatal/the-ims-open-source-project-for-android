package javax.microedition.ims.core.connection;

import javax.microedition.ims.transport.impl.ConnectionSecurityInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
* Created by IntelliJ IDEA.
* User: Labada
* Date: 3/17/11
* Time: 4:10 PM
* To change this template use File | Settings | File Templates.
*/
public class ConnectionSecurityInfoImplDefault implements ConnectionSecurityInfo {
    private final Collection<String> allowedRemoteAddressesUnmodifiableView;
    private final AddrCheckMode checkMode;


    public ConnectionSecurityInfoImplDefault(
            final Collection<String> allowedRemoteAddresses,
            final AddrCheckMode checkMode) {

        this.allowedRemoteAddressesUnmodifiableView =
                Collections.unmodifiableCollection(new ArrayList<String>(allowedRemoteAddresses));

        this.checkMode = checkMode;
    }

    public Collection<String> getAllowedRemoteAddresses() {
        return allowedRemoteAddressesUnmodifiableView;
    }


    public AddrCheckMode getAddrCheckMode() {
        return checkMode;
    }


    @Override
    public String toString() {
        return "ConnectionSecurityInfoImplDefault{" +
                "allowedRemoteAddressesUnmodifiableView=" + allowedRemoteAddressesUnmodifiableView +
                '}';
    }
}
