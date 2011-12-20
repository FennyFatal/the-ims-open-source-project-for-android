package com.android.ims.core;

import java.util.HashMap;
import java.util.Map;

public enum DtmfPayload {
    INBAND("inband"), OUTBAND("outband");
    
    private String value;
    
    private static Map<String, DtmfPayload> mapping = new HashMap<String, DtmfPayload>();
    
    static {
        for(DtmfPayload dtfmPayload: values()) {
            mapping.put(dtfmPayload.value, dtfmPayload);
        }
    }
    
    private DtmfPayload(String value) {
        this.value = value;
    }
    
    public static DtmfPayload parse(String value) {
        return value != null? mapping.get(value.toLowerCase()): null;
    }
}
