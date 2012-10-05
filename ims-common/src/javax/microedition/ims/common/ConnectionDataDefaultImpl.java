package javax.microedition.ims.common;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 2/17/11
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionDataDefaultImpl implements ConnectionData {
    private final String address;
    private final int port;
    private final Protocol protocol;

    private ConnectionDataDefaultImpl(
            final String address,
            final int port,
            final Protocol protocol) {
        this.address = address;
        this.port = port;
        this.protocol = protocol;
    }

    private ConnectionDataDefaultImpl(final Builder builder) {
        this(
                builder.address,
                builder.port,
                builder.protocol
        );
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    public static class Builder {
        private String address;
        private int port;
        private Protocol protocol;

        public Builder() {
        }

        public Builder address(final String address){
            this.address = address;
            return this;
        }

        public Builder port(final int port){
            this.port = port;
            return this;
        }

        public Builder protocol(final Protocol protocol){
            this.protocol = protocol;
            return this;
        }

        public Protocol getProtocol() {
            return this.protocol;
        }

        public ConnectionData build() {
            return new ConnectionDataDefaultImpl(this);
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("[address='").append(address).append('\'')
                    .append(", port=").append(port)
                    .append(", protocol=").append(protocol).append(']')
                    .toString();
        }
    }

    @Override
    public String toString() {
        return "ConnectionDataDefaultImpl{" +
                "address='" + address + '\'' +
                ", port=" + port +
                ", protocol=" + protocol +
                '}';
    }
}
