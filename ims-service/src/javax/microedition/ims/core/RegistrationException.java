package javax.microedition.ims.core;

public class RegistrationException extends Exception{
    private static final long serialVersionUID = 1L;

    private final int responseCode;
    private final String reasonPhrase;
    private final String reasonData;
    private final boolean byTimeout;

    public RegistrationException(int responseCode, String reasonPhrase, String reasonData, boolean byTimeout) {
        this.responseCode = responseCode;
        this.reasonPhrase = reasonPhrase;
        this.reasonData = reasonData;
        this.byTimeout = byTimeout;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public String getReasonData() {
        return reasonData;
    }

    public boolean isByTimeout() {
        return byTimeout;
    }

    @Override
    public String toString() {
        return "RegistrationException [responseCode=" + responseCode + ", reasonPhrase="
                + reasonPhrase + ", reasonData=" + reasonData + ", byTimeout=" + byTimeout + "]";
    }
}
