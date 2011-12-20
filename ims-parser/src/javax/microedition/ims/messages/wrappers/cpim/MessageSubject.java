package javax.microedition.ims.messages.wrappers.cpim;

import javax.microedition.ims.common.util.StringUtils;

public class MessageSubject {

    private String lang;
    private String subject;
    private String stringValue;


    public MessageSubject(String lang, String subject) {
        super();
        this.lang = lang;
        this.subject = subject;
        stringValue = buildStringValue();
    }

    public String getLang() {
        return lang;
    }


    public String getSubject() {
        return subject;
    }


    @Override
    public String toString() {
        return stringValue;
    }

    public Object getAsString() {
        return stringValue;
    }

    private String buildStringValue(){
        StringBuilder sb = new StringBuilder(40);
        if(lang != null && lang.length() > 0){
            sb.append(";lang=").append(lang).append(StringUtils.SPACE);
        }
        sb.append(subject);
        return sb.toString();
    }

}
