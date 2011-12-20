package javax.microedition.ims.core.env;

import javax.microedition.ims.StackHelper;
import javax.microedition.ims.common.ScheduledService;
import javax.microedition.ims.core.connection.ConnState;
import javax.microedition.ims.core.connection.GsmLocationInfo;
import javax.microedition.ims.core.connection.GsmLocationServiceDefaultImpl;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 12/10/10
 * Time: 4:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnvironmentDefaultImpl implements Environment {

    private final HardwareInfo hardwareInfo;
    private final GsmLocationService gsmLocationService;
    private final ConnectionManager connectionManager;
    private final File externalStorageDirectory;

    public EnvironmentDefaultImpl(final Builder builder) {
        this.connectionManager = builder.connectionManager;
        this.gsmLocationService = builder.gsmLocationService;
        this.hardwareInfo = builder.hardwareInfo;
        this.externalStorageDirectory = builder.externalStorageDirectory;

        checkInvariant();
    }

    private void checkInvariant() {
        if (this.connectionManager == null) {
            throw new NullPointerException("ConnectionManager is " + this.connectionManager);
        }

        if (this.gsmLocationService == null) {
            throw new NullPointerException("GsmLocationService is " + this.gsmLocationService);
        }

        if (this.hardwareInfo == null) {
            throw new NullPointerException("HardwareInfo is " + this.hardwareInfo);
        }

        if (this.externalStorageDirectory == null) {
            throw new NullPointerException("ExternalFolder is " + this.externalStorageDirectory);
        }
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public GsmLocationService getGsmLocationService() {
        return gsmLocationService;
    }

    public HardwareInfo getHardwareInfo() {
        return hardwareInfo;
    }

    public File getExternalStorageDirectory() {
        return externalStorageDirectory;
    }

    @Override
    public String toString() {
        return "EnvironmentDefaultImpl{" +
                "hardwareInfo=" + hardwareInfo +
                ", gsmLocationService=" + gsmLocationService +
                ", connectionManager=" + connectionManager +
                ", externalStorageDirectory=" + externalStorageDirectory +
                '}';
    }

    public static class Builder {

        private HardwareInfo hardwareInfo;
        private GsmLocationService gsmLocationService;
        private ConnectionManager connectionManager;
        public File externalStorageDirectory;

        public Builder connectionManager(final ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            return this;
        }


        public Builder gsmLocationService(final GsmLocationService gsmLocationService) {
            this.gsmLocationService = gsmLocationService;
            return this;
        }


        public Builder hardwareInfo(final HardwareInfo hardwareInfo) {
            this.hardwareInfo = hardwareInfo;
            return this;
        }

        public Builder externalStorageDirectory(final File externalFolder) {
            this.externalStorageDirectory = new File(externalFolder.toURI());
            return this;
        }

        public Environment build() {
            return new EnvironmentDefaultImpl(this);
        }

        public static Environment build(final ConnState connState) {
            ConnectionManager connManager = StackHelper.newMockConnectionManager(connState);
            GsmLocationServiceDefaultImpl locationService = new GsmLocationServiceDefaultImpl();
            locationService.updateLocationInfo(new GsmLocationInfo(111, 22, "12345", connManager.getNetworkSubType()));

            return new EnvironmentDefaultImpl.Builder()
                    .connectionManager(connManager)
                    .gsmLocationService(locationService)
                    .hardwareInfo(new HardwareInfoDefaultImpl())
                    .externalStorageDirectory(new File(System.getProperty(
                            "user.home") + File.separator +
                            "ims_stack" + File.separator +
                            "received_files" + File.separator)
                    )
                    .build();
        }
    }
}
