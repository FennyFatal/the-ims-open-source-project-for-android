package javax.microedition.ims.common;

import java.util.HashMap;
import java.util.Map;

public enum DtmfPayloadType {
    INBAND("inband"), OUTBAND("outband");
    
    private String value;
    
    private static Map<String, DtmfPayloadType> mapping = new HashMap<String, DtmfPayloadType>();
    
    static {
        for(DtmfPayloadType dtfmPayload: values()) {
            mapping.put(dtfmPayload.value, dtfmPayload);
        }
    }
    
    private DtmfPayloadType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static DtmfPayloadType parse(String value) {
        return mapping.get(value.toLowerCase());
    }
}
