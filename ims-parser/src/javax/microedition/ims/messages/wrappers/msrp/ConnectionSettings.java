/*package javax.microedition.ims.messages.wrappers.msrp;

public class ConnectionSettings {

    enum ConnectionPolicy {
        cpUsePaths,
        cpUseConnectionInfo,
        cpUseP2P
    };    
     How many bytes to we send before considering chunking?
     * This is the 2048 value in the RFC
     * Valid is > 0
     
    private int chunkSize;

     Some servers do not support unlimited chunks (multiplexing server
     * and such). The maximum chunk size forces the MSRP connection to
     * chunk messages no larger than this size.
     * Valid is > 0 for active values, or 0 for no limit
     
    private int maximumChunkSize;

     The largest size message we are willing to receive. This is not
     * necessarily enforced, it is only indicated to the other side in the
     * SDP
     *     0 means no limit
     
    private long maximumSizeIn;

     If the other guy has indicated a maximum size, remember it here.
     * It means we shouldn't send anything bigger than this
     *     0 means no limit
     
    private long maximumSizeOut;


    private ConnectionPolicy  connectionPolicy;

    public ConnectionSettings() {
        chunkSize= 2048;
        connectionPolicy = ConnectionPolicy.cpUseConnectionInfo;
    }


    public ConnectionSettings( int cs, int ms ) {
        chunkSize = cs;
        maximumChunkSize = ms;
        connectionPolicy= ConnectionPolicy.cpUseConnectionInfo;
    }


    public int getChunkSize() {
        return chunkSize;
    }


    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }


    public int getMaximumChunkSize() {
        return maximumChunkSize;
    }


    public void setMaximumChunkSize(int maximumChunkSize) {
        this.maximumChunkSize = maximumChunkSize;
    }


    public long getMaximumSizeIn() {
        return maximumSizeIn;
    }


    public void setMaximumSizeIn(long maximumSizeIn) {
        this.maximumSizeIn = maximumSizeIn;
    }


    public long getMaximumSizeOut() {
        return maximumSizeOut;
    }


    public void setMaximumSizeOut(long maximumSizeOut) {
        this.maximumSizeOut = maximumSizeOut;
    }


    public ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }


    public void setConnectionPolicy(ConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
    }
}
*/