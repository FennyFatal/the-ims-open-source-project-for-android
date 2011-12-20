package javax.microedition.ims.messages.wrappers.cpim;

import javax.microedition.ims.common.util.StringUtils;

public class ImUri {
    private String displayName, uri;
    private String stringValue;

    public ImUri(String displayName, String uri) {
        super();
        this.displayName = displayName;
        this.uri = uri;
        stringValue = buildUriString();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUri() {
        return uri;
    }

    public String getAsString(){
        return stringValue;
    }

    private String buildUriString(){
        StringBuilder sb = new StringBuilder(50);
        if(displayName != null && displayName.length() > 0){
            sb.append(displayName).append(StringUtils.SPACE).append("<").append(uri).append(">");
        } else {
            sb.append("<").append(uri).append(">");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return stringValue;
    }




}
