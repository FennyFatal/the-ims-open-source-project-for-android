package javax.microedition.ims.dns;

import javax.microedition.ims.core.ReasonCode;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 2/21/11
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class DNSException extends Exception{
    private static final long serialVersionUID = 1L;

    private final ReasonCode code;
    private final String description;

    public DNSException(final ReasonCode code, final String description) {
        this.code = code;
        this.description = description;
    }

    public DNSException(final ReasonCode code) {
        this(code, code.getDescription());
    }

    public DNSException(final String description) {
        this(ReasonCode.UNKNOWN, description);
    }

    public DNSException() {
        this(ReasonCode.UNKNOWN);
    }

    public ReasonCode getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DNSException{" +
                "reasonCode=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
