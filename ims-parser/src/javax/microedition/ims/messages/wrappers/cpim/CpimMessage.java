package javax.microedition.ims.messages.wrappers.cpim;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.wrappers.sip.Header;


public class CpimMessage {

    private ImUri from, to;
    private List<ImUri> cc;
    private Date timestamp;
    private String cachedDateView;
    private List<MessageSubject> subjects;
    private byte[] body;
    private ImUri nameSpace;


    private List<String> require;
    private String stringValue;


    private final static SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public CpimMessage() {
        subjects = Collections.synchronizedList(new ArrayList<MessageSubject>());
        cc = new ArrayList<ImUri>();
        require = new ArrayList<String>();
    }


    public ImUri getFrom() {
        return from;
    }

    public void setFrom(ImUri from) {
        this.from = from;
    }


    public ImUri getNameSpace() {
        return nameSpace;
    }


    public void setNameSpace(ImUri nameSpace) {
        this.nameSpace = nameSpace;
    }

    public ImUri getTo() {
        return to;
    }

    public void setTo(ImUri toUri) {
        this.to = toUri;
    }

     public void addCc(ImUri toUri) {
        this.cc.add(toUri);
    }

    public List<ImUri> getCcList() {
        return cc;
    }

    public void addRequire(String require) {
    this.require.add(require);
    }

    public List<String> getRequireList() {
        return require;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestampIso8601(String timestamp) {
        try {
            String date = timestamp.replaceAll("[-+]0([0-9]){1}\\:00", "+0$100");
            this.timestamp = ISO8601DATEFORMAT.parse(date);
            cachedDateView = timestamp;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<MessageSubject> getSubjects() {
        return subjects;
    }

    public void addSubject(String lang, String subject) {
        this.subjects.add(new MessageSubject(lang, subject));
    }

    public byte[] getContent() {
        return body;
    }
    public void setContent(byte[] content) {
        this.body = content;
    }


    public String getAsStringValue(){
        stringValue = buildMessageString();
        return stringValue;
    }

    private String buildMessageString(){
        StringBuilder sb = new StringBuilder(100);
        if(from != null){
            sb.append(Header.From).append(StringUtils.DOTS).append(from.getAsString()).append(StringUtils.SIP_TERMINATOR);
        }
        if(to != null){
            sb.append(Header.To).append(StringUtils.DOTS).append(to.getAsString()).append(StringUtils.SIP_TERMINATOR);
        }

        Iterator<ImUri> ccIter = cc.iterator();
        while(ccIter.hasNext()){
            sb.append("cc").append(StringUtils.DOTS).append(ccIter.next().getAsString()).append(StringUtils.SIP_TERMINATOR);
        }

        Iterator<MessageSubject> subjIter = subjects.iterator();
        while(subjIter.hasNext()){
            sb.append(Header.Subject).append(StringUtils.DOTS).append(subjIter.next().getAsString()).append(StringUtils.SIP_TERMINATOR);
        }

        if(require.size() >0) {
            Iterator<String> requireIter = require.iterator();
            sb.append(Header.Require).append(StringUtils.DOTS).append(requireIter.next());
            while(requireIter.hasNext()){
                sb.append(", ").append(requireIter.next()).append(StringUtils.SIP_TERMINATOR);
            }
        }

        if(timestamp != null){
            sb.append("DateTime").append(StringUtils.DOTS).append(cachedDateView).append(StringUtils.SIP_TERMINATOR);
        }

        sb.append(StringUtils.SIP_TERMINATOR);
        if(body != null){
            sb.append(new String(body)).append(StringUtils.SIP_TERMINATOR);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        stringValue = buildMessageString();
        return stringValue;
    }



}
