package javax.microedition.ims.common;

/**
* Created by IntelliJ IDEA.
* User: Labada
* Date: 3/4/11
* Time: 11:28 AM
* To change this template use File | Settings | File Templates.
*/
public interface ConnectionData {
    String getAddress();
    int getPort();
    Protocol getProtocol();
}
