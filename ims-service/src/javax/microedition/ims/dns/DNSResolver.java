package javax.microedition.ims.dns;

import javax.microedition.ims.common.ConnectionData;
import javax.microedition.ims.config.UserInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 2/16/11
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DNSResolver {
    //ConnectionData lookUp(String sipUri) throws DNSException;
    ConnectionData lookUp(UserInfo userInfo) throws DNSException;
    ConnectionData getLastConnectionData(UserInfo userInfo) throws DNSException;
}
