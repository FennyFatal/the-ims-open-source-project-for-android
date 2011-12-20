package javax.microedition.ims.core.connection;

import javax.microedition.ims.common.ConnectionData;
import javax.microedition.ims.common.ListenerSupport;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.dns.DNSResolver;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 3/4/11
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ConnectionDataProvider extends ListenerSupport<ConnectionDataListener>{
    ConnectionData getConnectionData();
    DNSResolver getDNSResolver();
    void refresh() throws IMSStackException;
}
