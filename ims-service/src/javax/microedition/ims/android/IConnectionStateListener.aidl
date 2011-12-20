package javax.microedition.ims.android;


interface IConnectionStateListener {
    void connectionResumed();
    void connectionSuspended();
    void imsConnected(String connectionInfo);
    void imsDisconnected();
}
