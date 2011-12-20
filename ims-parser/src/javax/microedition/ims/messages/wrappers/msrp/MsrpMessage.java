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

package javax.microedition.ims.messages.wrappers.msrp;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.IMSID;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.IMSStringID;
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.messages.parser.msrp.MsrpUriParser;
import javax.microedition.ims.messages.utils.MsrpUtils;
import javax.microedition.ims.messages.wrappers.common.ResponseClass;

public class MsrpMessage implements IMSMessage {
    private String reasonPhrase, messageId, transactionId, currentProgress, contentType, subject;
    private FailureReport failureReport = FailureReport.NotSet;
    private SuccessReport successReport = SuccessReport.NotSet;
    private MsrpMessageType type;
    private MsrpUri fromPath, toPath;
    private int code;
    private long prevProgress;
    private MessageState state = MessageState.Idle;
    private long totalSize;
    private byte[] body;
    private ChunkTerminator terminator = ChunkTerminator.NOT_SET;
    private ResponseClass responseClass;
    private final AtomicBoolean expired = new AtomicBoolean(false); 

    private final IMSID imsid;

    public MsrpMessage(String transactionId) {
        this.transactionId = transactionId;
        this.imsid = new IMSStringID(transactionId);
    }

    public MsrpMessage() {
        this(MsrpUtils.generateTransactionOrMessageId());
        messageId = MsrpUtils.generateTransactionOrMessageId();
    }

    public MsrpMessage(IMsrpMessageBuilder iMsrpMessageBuilder) {
        this(MsrpUtils.generateTransactionOrMessageId());

        this.fromPath = MsrpUriParser.parse(iMsrpMessageBuilder.sender);
        this.toPath = MsrpUriParser.parse(iMsrpMessageBuilder.recipient);
        this.subject = iMsrpMessageBuilder.subject;
        this.messageId = iMsrpMessageBuilder.messageId;
        this.body = iMsrpMessageBuilder.body;
        this.contentType = iMsrpMessageBuilder.contentType;
    }


    public void chunkReceived(String id) {
    }

    public MessageState getState() {
        return state;
    }

    public void setState(MessageState state) {
        this.state = state;
    }

    public ResponseClass getResponseClass() {
        return responseClass;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getPrevProgress() {
        return prevProgress;
    }

    public byte[] getBody() {
        return body;
    }


    public void setContent(int ofs, byte[] content) {
        this.body = content;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
        responseClass = ResponseClass.createByCode(code);
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public FailureReport getFailureReport() {
        return failureReport;
    }

    public void setFailureReport(FailureReport failureReport) {
        this.failureReport = failureReport;
    }

    public SuccessReport getSuccessReport() {
        return successReport;
    }

    public void setSuccessReport(SuccessReport successReport) {
        this.successReport = successReport;
    }

    public MsrpUri getFromPath() {
        return fromPath;
    }

    public void setFromPath(MsrpUri fromPath) {
        this.fromPath = fromPath;
    }

    public MsrpUri getToPath() {
        return toPath;
    }

    public void setToPath(MsrpUri toPath) {
        this.toPath = toPath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public ChunkTerminator getTerminator() {
        return terminator;
    }

    public void setTerminator(ChunkTerminator terminator) {
        this.terminator = terminator;
    }

    public String getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(String currentProgress) {
        this.currentProgress = currentProgress;
    }

    public void setCurrentProgress(long currentProgress) {
        this.currentProgress = "" + currentProgress;
    }

    public void setPrevProgress(long prevProgress) {
        this.prevProgress = prevProgress;
    }

    public MsrpMessageType getType() {
        return type;
    }

    public void setType(MsrpMessageType type) {
        this.type = type;
    }

    public String buildContent() {
        return new String(buildByteContent());
    }


    public byte[] buildByteContent() {
        StringBuilder sb = new StringBuilder();

        if (type != MsrpMessageType.STATUS) {
            sb.append(MsrpUtils.MSRP).append(" ").append(transactionId).append(" ").append(type).append(StringUtils.SIP_TERMINATOR);
        }
        else {
            sb.append(MsrpUtils.MSRP).append(" ").append(transactionId).append(" ").append(code).append(" ").append(reasonPhrase).append(StringUtils.SIP_TERMINATOR);
        }
        if (toPath != null) {
            sb.append(MsrpHeaders.To_Path.stringValue()).append(": ").append(toPath.buildContent()).append(StringUtils.SIP_TERMINATOR);
        }
        if (fromPath != null) {
            sb.append(MsrpHeaders.From_Path.stringValue()).append(": ").append(fromPath.buildContent()).append(StringUtils.SIP_TERMINATOR);
        }

        if (messageId != null && messageId.length() > 0) {
            sb.append(MsrpHeaders.Message_ID.stringValue()).append(": ").append(messageId).append(StringUtils.SIP_TERMINATOR);
        }

        if (failureReport != FailureReport.NotSet) {
            sb.append(MsrpHeaders.Failure_Report.stringValue()).append(": ").append(failureReport.stringValue()).append(StringUtils.SIP_TERMINATOR);
        }

        if (successReport != SuccessReport.NotSet) {
            sb.append(MsrpHeaders.Success_Report.stringValue()).append(": ").append(successReport.stringValue()).append(StringUtils.SIP_TERMINATOR);
        }

        if (subject != null && subject.length() > 0) {
            sb.append(MsrpHeaders.Subject.stringValue()).append(": ").append(subject).append(StringUtils.SIP_TERMINATOR);
        }

        if (type != MsrpMessageType.STATUS) {
            sb.append(MsrpHeaders.Byte_Range.stringValue()).append(": ").append(prevProgress).append("-").append(currentProgress);
            sb.append("/").append(totalSize).append(StringUtils.SIP_TERMINATOR);
        }

        if (type == MsrpMessageType.REPORT) {
            sb.append(MsrpHeaders.Status.stringValue()).append(": 000 ").append(code).append(" ").append(reasonPhrase).append(StringUtils.SIP_TERMINATOR);
        }

        if (body != null && contentType != null && contentType.length() > 0) {
            sb.append(MsrpHeaders.Content_Type.stringValue()).append(": ").append(contentType).append(StringUtils.SIP_TERMINATOR);
        }
        assert getTerminator() != ChunkTerminator.NOT_SET : "Wrong chunk terminator";
        String lineEnding = new StringBuilder().append("-------").append(transactionId).append(getTerminator().getValue()).append(StringUtils.SIP_TERMINATOR).toString();
        byte[] headers = sb.toString().getBytes();
        int headersLength = headers.length, bodyLength = 0;
        int terminatorLen = StringUtils.SIP_TERMINATOR.getBytes().length;
        byte[] ret = null;
        if (body != null && body.length > 0) {
            bodyLength = body.length + 2 * terminatorLen;
            ret = new byte[headersLength + bodyLength + lineEnding.length()];
        }
        else {
            ret = new byte[headersLength + lineEnding.length()];
        }

        assert headersLength + bodyLength + lineEnding.length() == ret.length : "Wring MSRP message length";

        System.arraycopy(headers, 0, ret, 0, headersLength);

        if (body != null && body.length > 0) {

            System.arraycopy(StringUtils.SIP_TERMINATOR.getBytes(), 0, ret, headersLength, terminatorLen);
            System.arraycopy(body, 0, ret, headersLength + terminatorLen, body.length);
            System.arraycopy(StringUtils.SIP_TERMINATOR.getBytes(), 0, ret, headersLength + body.length + terminatorLen, terminatorLen);
        }
        System.arraycopy(lineEnding.getBytes(), 0, ret, headersLength + bodyLength, lineEnding.length());


        return ret;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String toString() {
        return "MsrpMessage{" +
                " totalSize=" + totalSize +
                ", responseClass=" + responseClass +
                ", imsid=" + imsid +
                ", msg=" + buildContent() +
                '}';
    }

    public static class IMsrpMessageBuilder {
        private String contentType;
        private String sender;
        private String recipient;
        private String subject;

        private String messageId;

        private byte[] body;

        public IMsrpMessageBuilder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public IMsrpMessageBuilder recipients(String recipients) {
            this.recipient = recipients;
            return this;
        }

        public IMsrpMessageBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public IMsrpMessageBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public IMsrpMessageBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public IMsrpMessageBuilder contentParts(byte[] content) {
            this.body = content;
            return this;
        }

        public MsrpMessage build() {
            return new MsrpMessage(this);
        }
    }

    
    public IMSEntityType getEntityType() {
        return IMSEntityType.MSRP;
    }

    
    public IMSID getIMSEntityId() {
        /* Object ret = null;
        if(type == MsrpMessageType.STATUS){
            ret = getFromPath().getId();
        } else{
            ret = getFromPath().getId();
        }*/

        //return transactionId;
        return new IMSStringID(transactionId);
    }

    
    public String shortDescription() {
        StringBuilder sb = new StringBuilder();

        if (type != MsrpMessageType.STATUS) {
            sb.append(MsrpUtils.MSRP).append(" ").append(transactionId).append(" ").append(type).append(StringUtils.SIP_TERMINATOR);
        }
        else {
            sb.append(MsrpUtils.MSRP).append(" ").append(transactionId).append(" ").append(code).append(" ").append(reasonPhrase).append(StringUtils.SIP_TERMINATOR);
        }
        return sb.toString();
    }

    public int calcHash() {
        int result = transactionId.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);

        final String fromId = getFromPath().getId();
        result = 31 * result + (fromId != null ? fromId.hashCode() : 0);

        final String toId = getToPath().getId();
        result = 31 * result + (toId != null ? toId.hashCode() : 0);

        return result;
    }

    @Override
    public boolean isExpired() {
        return expired.get();
    }
    
    @Override
    public void expire() {
        expired.compareAndSet(false, true);
    }
}
