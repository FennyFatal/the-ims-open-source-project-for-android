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

package javax.microedition.ims.core.dialog;

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.IMSEntity;
import javax.microedition.ims.core.InitiateParty;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.auth.AuthorizationData;
import javax.microedition.ims.core.sipservice.invite.SessionRefreshData;
import javax.microedition.ims.core.sipservice.refer.Refer;
import javax.microedition.ims.messages.MessageBuilderFactory;
import javax.microedition.ims.messages.history.MessageAddedListener;
import javax.microedition.ims.messages.history.MessageHistory;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Via;
import javax.microedition.ims.util.MessageUtilHolder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class stores all information about custom SESSION
 */
public class Dialog implements IMSEntity, Shutdownable {
    private static final String LOG_TAG = "Dialog";

    public static enum ParamKey {
        REGISTRATION_EXPIRES,
        SUBSCRIPTION_EXPIRES,
        PREV_REG_ADDRESS,
        USE_RETRY_AFTER,
        NOTIFY_INFO,
        OPTIONS_USE_BODY,
        PUBLISH_EXPIRES,
        PUBLISH_INFO,
        PUBLISH_TYPE,
        SUBSCRIBE_INFO,
        CHALLENGE_TYPE, PATH
    }

    public enum DialogState {
        EARLY, STATED
    }

    private final String callId, localTag, remoteParty;
    private final IMSID imsid;

    private AtomicReference<String> remotePartyDisplayName = new AtomicReference<String>("");

    //headers SIP_REFER
    private Refer referTo;

    private final AtomicReference<String> remoteTag = new AtomicReference<String>(null);
    private final ClientIdentity localParty;
    private final AtomicReference<DialogState> state = new AtomicReference<DialogState>(DialogState.EARLY);

    /*
    The CSeq header field contains a decimal number that increases for
    each request. Usually, it increases by 1 for each new request, with the exception
    of CANCEL and ACK requests, which use the CSeq number of the INVITE
    request to which it refers.
     */
    private final SubsequentNumberGenerator cSeqGenerator;
    private final SubsequentNumberGenerator rSeqGenerator;
    private volatile Map<ChallengeType, ? extends AuthorizationData> dialogAuthData;
    private volatile Map<ParamKey, Object> map = Collections.synchronizedMap(new HashMap<ParamKey, Object>(10));
    private MessageHistory messageHistory = new MessageHistory();
    private final AtomicReference<SdpMessage> outgoingSdpMessage = new AtomicReference<SdpMessage>(null);
    private final AtomicReference<SdpMessage> incomingSdpMessage = new AtomicReference<SdpMessage>(null);

    private final InitiateParty dialogType;
    private final AtomicReference<InitiateParty> reInviteInProgress = new AtomicReference<InitiateParty>(null);
    private final AtomicReference<InitiateParty> updateInProgress = new AtomicReference<InitiateParty>(null);

    //TODO
    private MessageBuilderFactory messageBuilderFactory;
    private Protocol prefferedProtocol;

    private SessionRefreshData sessionRefreshData;


    public SessionRefreshData getSessionRefreshData() {
        return sessionRefreshData;
    }

    public void setSessionRefreshData(SessionRefreshData sessionRefreshData) {
        this.sessionRefreshData = sessionRefreshData;
    }

    public Protocol getPrefferedProtocol() {
        return prefferedProtocol;
    }
    
    private final AtomicBoolean done = new AtomicBoolean(false);

/*    public void setPrefferedProtocol(Protocol prefferedProtocol) {
        this.prefferedProtocol = prefferedProtocol;
    }
*/
    /**
     * Constructor for client SESSION
     *
     * @param dialogType
     * @param localParty
     * @param remoteParty - remote party address
     */
    public Dialog(
            final InitiateParty dialogType,
            final ClientIdentity localParty,
            final String remoteParty,
            final StackContext stackContext) {

        this(dialogType, localParty, remoteParty, SIPUtil.newCallId(), SIPUtil.newTag(), stackContext);
    }

    /**
     * Constructor for server SESSION
     *
     * @param dialogType
     * @param localParty
     * @param remoteParty - remote party address
     * @param callId      - received callId
     */
    public Dialog(
            final InitiateParty dialogType,
            final ClientIdentity localParty,
            final String remoteParty,
            final String callId,
            final StackContext stackContext) {

        this(dialogType, localParty, remoteParty, callId, SIPUtil.newTag(), stackContext);
    }

    private Dialog(
            final InitiateParty dialogType,
            final ClientIdentity localParty,
            final String remotePartyURI,
            final String callId,
            final String localTag,
            final StackContext stackContext) {

        this.dialogType = dialogType;

        if (localParty == null) {
            throw new IllegalArgumentException("Client identity can not be null");
        }

        if (!MessageUtilHolder.isValidUri(stackContext.getConfig(), remotePartyURI)) {
            final String errMsg = "Remote party URI doesn't comply RFC 3261. Passed URI: '" + remotePartyURI + "'";
            throw new IllegalArgumentException(errMsg);
        }

        this.callId = callId;
        this.imsid = new IMSStringID(callId);

        this.localTag = localTag;
        this.remoteParty = remotePartyURI;
        this.localParty = localParty;
        this.cSeqGenerator = new DefaultSubsequentNumberGenerator(1);
        this.rSeqGenerator = new DefaultSubsequentNumberGenerator(1);
        //this.prefferedProtocol = stackContext.getConfig().getConnectionType();
        this.prefferedProtocol = stackContext.getConnectionType();
        this.messageHistory.setMessageAddedListener(new MessageAddedListener() {

            public void messageAdded(final BaseSipMessage msg) {
                List<Via> vias = new ArrayList<Via>(msg.getVias());

                if (vias != null && vias.size() > 0) {
                    Protocol prot = vias.get(0).getProtocol();
                    if (prefferedProtocol != prot) {
                        prefferedProtocol = prot;
                    }
                }

            }
        });
        this.outgoingSdpMessage.set(new SdpMessage());
        this.messageBuilderFactory = new MessageBuilderFactory(this, stackContext);

        //putCustomParameter(ParamKey.CHALLENGE_TYPE, ChallengeType.PROXY);
        //putCustomParameter(ParamKey.CHALLENGE_TYPE, Arrays.asList(ChallengeType.UAS));

        Logger.log(LOG_TAG, "DIALOG CREATED: " + this);
    }

    public IMSEntityType getEntityType() {
        return IMSEntityType.SIP;
    }

    public MessageBuilderFactory getMessageBuilderFactory() {
        if(done.get()) throw new IllegalStateException("Dialog was shutdown already.");
        return messageBuilderFactory;
    }

    public InitiateParty getInitiateParty() {
        return dialogType;
    }

    public String getRemoteTag() {
        return remoteTag.get();
    }

    public void setRemoteTag(String announcedRemoteTag) {
        remoteTag.compareAndSet(null, announcedRemoteTag);
    }

    public void dialogStated() {
        state.compareAndSet(DialogState.EARLY, DialogState.STATED);
        //if (this.remoteTag.compareAndSet(null, remoteTag)) {
        //    state.compareAndSet(DialogState.EARLY, DialogState.STATED);
        //}
    }

    public String getRemoteParty() {
        return remoteParty;
    }

    public DialogState getState() {
        return state.get();
    }

    public String getCallId() {
        return callId;
    }

    public IMSID getIMSEntityId() {
        /* Object ret = null;
        if(imsEntityType == IMSEntityType.SIP) {
            ret = getCallId();
        } else if(imsEntityType == IMSEntityType.MSRP) {
            ret = msrpData.getSessionId();
        } else{
            assert false : "Wrong imsEntityType!";
        }*/
        return imsid;
    }

    public Refer getReferTo() {
        return referTo;
    }

    public void setReferTo(Refer referTo) {
        this.referTo = referTo;
    }

    public String getLocalTag() {
        return localTag;
    }

    public ClientIdentity getLocalParty() {
        return localParty;
    }

    public int obtainCSeq() {
        return cSeqGenerator.next();
    }

    public int obtainRSeq() {
        return rSeqGenerator.next();
    }

    public Map<ChallengeType, ? extends AuthorizationData> getAuthorizationData() {
        return dialogAuthData;
    }

    public void setAuthorizationData(final Map<ChallengeType, ? extends AuthorizationData> authData) {
        this.dialogAuthData = Collections.unmodifiableMap(new HashMap<ChallengeType, AuthorizationData>(authData));
    }

    public void putCustomParameter(ParamKey key, Object value) {
        map.put(key, value);
    }

    public Object getCustomParameter(ParamKey key) {
        return map.get(key);
    }

    public void markReInviteInProgress(InitiateParty party) {
        reInviteInProgress.set(party);
    }

    public void unmarkReInviteInProgress() {
        reInviteInProgress.set(null);
    }

    public boolean isReInviteInProgress() {
        return reInviteInProgress.get() != null;
    }

    public InitiateParty getReInviteInitiateParty() {
        return reInviteInProgress.get();
    }

    public void markUpdateInProgress(InitiateParty party) {
        updateInProgress.set(party);
    }

    public void unmarkUpdateInProgress() {
        updateInProgress.set(null);
    }

    public boolean isUpdateInProgress() {
        return updateInProgress.get() != null;
    }

    public InitiateParty getUpdateInitiateParty() {
        return updateInProgress.get();
    }

    public MessageHistory getMessageHistory() {
        return messageHistory;
    }

    public SdpMessage getOutgoingSdpMessage() {
        return outgoingSdpMessage.get();
    }

    public SdpMessage getIncomingSdpMessage() {
        return incomingSdpMessage.get();
    }

    public void setIncomingSdpMessage(SdpMessage incomingSdpMessage) {
        this.incomingSdpMessage.set(incomingSdpMessage);
    }

    public String toString() {
        return "base = " + super.toString() + ", Dialog: " + callId;
    }

    @Override
    public void shutdown() {
        Logger.log(LOG_TAG, "shutdown#" + toString());
        if(done.compareAndSet(false, true)) {
            map.clear();
            messageHistory.shutdown();
            messageHistory = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.log(LOG_TAG, "finalize#" + this);
        super.finalize();
    }
    
    public String getRemotePartyDisplayName() {
        return remotePartyDisplayName.get();
    }

    public void setRemotePartyDisplayName(String remotePartyDisplayName) {
        this.remotePartyDisplayName.set(remotePartyDisplayName);
    }
}
