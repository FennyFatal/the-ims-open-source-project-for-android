package javax.microedition.ims.core;

public enum ReasonCode {
    UNKNOWN("ER", "Unknown error"),
    CERT_NOT_VALID("ER01", "Server Certificate not valid"),
    NAPTR_LOOKUP_FAILS("ER02", "Unable to receive response to NAPTR query"),
    SRV_LOOKUP_FAILS("ER03", "Unable to receive response to SRV query"),
    A_LOOKUP_FAILS("ER04", "Unable to receive response to A query"),
    GBA_U_MISSIN("ER05", "Invalid SIM Card"),
    IMPU_MISSING("ER06", "IMPU missing � OTA did  not succeed"),
    CONNECTION_ERROR("Reg99", "Connection failed error");

    private final String errCodeString;
    private final String description;

    private ReasonCode(final String errCodeString, final String description) {
        this.errCodeString = errCodeString;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getErrCodeString() {
        return errCodeString;
    }

    @Override
    public String toString() {
        return "Code{" +
                "description='" + description + '\'' +
                '}';
    }
}
