/*
 * This software code is (c) 2010 T-Mobile USA, Inc. All Rights Reserved.
 *
 * Unauthorized redistribution or further use of this material is
 * prohibited without the express permission of T-Mobile USA, Inc. and
 * will be prosecuted to the fullest extent of the law.
 *
 * Removal or modification of these Terms and Conditions from the source
 * or binary code of this software is prohibited.  In the event that
 * redistribution of the source or binary code for this software is
 * approved by T-Mobile USA, Inc., these Terms and Conditions and the
 * above copyright notice must be reproduced in their entirety and in all
 * circumstances.
 *
 * No name or trademarks of T-Mobile USA, Inc., or of its parent company,
 * Deutsche Telekom AG or any Deutsche Telekom or T-Mobile entity, may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" AND "WITH ALL FAULTS" BASIS
 * AND WITHOUT WARRANTIES OF ANY KIND.  ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS OR WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT CONCERNING THIS SOFTWARE, ITS SOURCE OR BINARY CODE
 * OR ANY DERIVATIVES THEREOF ARE HEREBY EXCLUDED.  T-MOBILE USA, INC.
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE
 * OR ITS DERIVATIVES.  IN NO EVENT WILL T-MOBILE USA, INC. OR ITS
 * LICENSORS BE LIABLE FOR LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF T-MOBILE USA,
 * INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * THESE TERMS AND CONDITIONS APPLY SOLELY AND EXCLUSIVELY TO THE USE,
 * MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE, ITS SOURCE OR BINARY
 * CODE OR ANY DERIVATIVES THEREOF, AND ARE SEPARATE FROM ANY WRITTEN
 * WARRANTY THAT MAY BE PROVIDED WITH A DEVICE YOU PURCHASE FROM T-MOBILE
 * USA, INC., AND TO THE EXTENT PERMITTED BY LAW.
 */
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
