package javax.microedition.ims.transport.impl;

import javax.microedition.ims.core.connection.ConnectionDataListener;

/**
* Created by IntelliJ IDEA.
* User: Labada
* Date: 3/17/11
* Time: 11:36 AM
* To change this template use File | Settings | File Templates.
*/
public interface ConnectionSecurityInfoProvider extends ConnectionDataListener {
    ConnectionSecurityInfo obtainSecurityInfo();
}
