package javax.microedition.ims.core;

public class AkaException extends IMSStackException{
    private static final long serialVersionUID = 1L;

    private ReasonCode reason;

    public AkaException(ReasonCode reason) {
        this.reason = reason;
    }

    public ReasonCode getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "AkaException [reason=" + reason + "]";
    }
}
