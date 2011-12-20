package javax.microedition.ims.android;

import javax.microedition.ims.core.ReasonCode;

public final class ErrorsUtils {
    private ErrorsUtils() {
        assert false;
    }
    
    public static int toIErrorCode(final ReasonCode code) {
        final int retValue;

        switch (code) {
            case CERT_NOT_VALID:
                retValue = IError.ERROR_UNTRUSTED_SERVER_CERTIFICATE;
                break;

            case NAPTR_LOOKUP_FAILS:
                retValue = IError.ERROR_NAPTR_QUERY_FAILED;
                break;

            case SRV_LOOKUP_FAILS:
                retValue = IError.ERROR_SRV_QUERY_FAILED;
                break;

            case A_LOOKUP_FAILS:
                retValue = IError.ERROR_ARECORD_QUERY_FAILED;
                break;

            case IMPU_MISSING:
                retValue = IError.IMPU_MISSING_OTA_FAILED;
                break;
                
            case GBA_U_MISSIN:
                retValue = IError.GBA_U_FAILED;
                break;
                
            case CONNECTION_ERROR:
                retValue = IError.ERROR_CONNECTIVITY;
                break;
                
            case UNKNOWN:
                retValue = IError.ERROR_UNKNOWN;
                break;
                
            default:
                retValue = IError.ERROR_UNKNOWN;
                break;
        }

        return retValue;
    }
}
