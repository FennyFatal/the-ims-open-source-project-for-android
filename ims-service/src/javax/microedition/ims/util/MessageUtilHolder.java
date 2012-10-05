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

package javax.microedition.ims.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.core.InitiateParty;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.registry.CommonRegistry;
import javax.microedition.ims.core.sipservice.subscribe.*;
import javax.microedition.ims.messages.parser.body.BodyParser;
import javax.microedition.ims.messages.parser.message.MessageParser;
import javax.microedition.ims.messages.parser.message.SipUriParser;
import javax.microedition.ims.messages.utils.SipMessageUtils;
import javax.microedition.ims.messages.wrappers.body.BodyHeader;
import javax.microedition.ims.messages.wrappers.body.BodyPart;
import javax.microedition.ims.messages.wrappers.common.Param;
import javax.microedition.ims.messages.wrappers.common.ParamList;
import javax.microedition.ims.messages.wrappers.common.ResponseClass;
import javax.microedition.ims.messages.wrappers.msrp.MsrpMessage;
import javax.microedition.ims.messages.wrappers.sip.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 03-Mar-2010
 * Time: 12:12:29
 */
public final class MessageUtilHolder {

    private static final String BRANCH_PARAM = "branch";
    public static final Set<MessageType> SUPPORTED_REQUESTS = Collections.synchronizedSet(EnumSet.of(
            MessageType.SIP_INVITE, MessageType.SIP_ACK, MessageType.SIP_PRACK, MessageType.SIP_BYE,
            MessageType.SIP_CANCEL, MessageType.SIP_REFER, MessageType.SIP_NOTIFY,
            MessageType.SIP_MESSAGE, MessageType.SIP_OPTIONS, MessageType.SIP_UPDATE
    ));

    private static MessageUtil messageUtil = new MessageUtil<IMSMessage>() {
        public Protocol getPrefferedProtocol(IMSMessage msg) {
            Protocol ret = null;
            if (msg instanceof MsrpMessage) {
                ret = Protocol.TCP;
            }
            else if (msg instanceof BaseSipMessage) {
                List<Via> vias = ((BaseSipMessage) msg).getVias();

                if (vias != null && vias.size() > 0) {
                    ret = vias.get(0).getProtocol();
                }
            }
            return ret;
        }
    };

    private static MsrpMessageUtil msrpMessageUtil = new MsrpMessageUtil<MsrpMessage>() {

        public String getFromPathID(MsrpMessage msg) {
            return msg.getFromPath().getId();
        }

        public String getToPathID(MsrpMessage msg) {
            return msg.getToPath().getId();
        }
    };

    public static void main1(String[] args) throws IOException {

        String messageBody = "--+++\r\n" +
                "Content-Transfer-Encoding: binary\r\n" +
                "Content-Type: application/rlmi+xml\r\n" +
                "Content-ID: \r\n" +
                "\r\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "--+++\r\n" +
                "Content-Transfer-Encoding: binary\r\n" +
                "Content-Type: application/pidf+xml\r\n" +
                "\r\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "\r\n" +
                "\r\n" +
                "--+++--\r\n";

        String boundary = "--+++";

        String boundaryPattern = Pattern.quote(boundary) + "(\r\n)?";
        messageBody = messageBody.replaceFirst("--$", "").replaceFirst(boundaryPattern, "");
        String[] bodyPartsMessages = messageBody.split(boundaryPattern);
        System.out.println(bodyPartsMessages.length);
    }

    public static void main(String[] args) throws IOException {
        String source = "NOTIFY sip:79262948588@10.0.2.15:5061;transport=TCP SIP/2.0\r\n" +
                "Via: SIP/2.0/TCP 193.201.229.35:5060;branch=z9hG4bK03r26s102o3gle095081.1\r\n" +
                "Max-Forwards: 19\r\n" +
                "From: <sip:79262948588_all@multifon.ru>;tag=20A9324631353641A783DB0A\r\n" +
                "To: <sip:79262948588@multifon.ru>;tag=494669e9-8bf2-4e7e-9a2b-fe0553f06e2f\r\n" +
                "Call-ID: d11bc7bf-670a-4ffc-8471-4031b866681b@10.0.2.15\r\n" +
                "CSeq: 1 NOTIFY\r\n" +
                "Contact: \"79262948588_all\"<sip:79262948588_all@193.201.229.35:5060;transport=tcp>\r\n" +
                "Require: eventlist\r\n" +
                "Event: presence\r\n" +
                "Subscription-State: active\r\n" +
                "Content-Type: multipart/related;boundary=boundary1;type=application\r\n" + /**application/rlmi+xml*/
                "Content-Length: 3780\r\n" +
                "\r\n" +
                "--boundary1\r\n" +
                "Content-Type:application/rlmi+xml;charset=\"UTF-8\"\r\n" +
                "\r\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<list xmlns=\"urn:ietf:params:xml:ns:rlmi\" uri=\"sip:79262948588_all@multifon.ru\" version=\"0\" fullState=\"true\">\r\n" +
                "   <resource uri=\"tel:+79262948589\">\r\n" +
                "      <instance id=\"9fbd34aa978f\" state=\"terminated\" reason=\"noresource\" cid=\"tel:+79262948589\"></instance>\r\n" +
                "   </resource>\r\n" +
                "   <resource uri=\"sip:79262056390@multifon.ru\">\r\n" +
                "      <instance id=\"5d293351d130\" state=\"active\" cid=\"sip:79262056390@multifon.ru\"></instance>\r\n" +
                "   </resource>\r\n" +
                "   <resource uri=\"sip:79255894279@multifon.ru\">\r\n" +
                "      <instance id=\"21ae25d26a0c\" state=\"active\" cid=\"sip:79255894279@multifon.ru\"></instance>\r\n" +
                "   </resource>\r\n" +
                "   <resource uri=\"sip:79262948589@multifon.ru\">\r\n" +
                "      <instance id=\"bad88dfbf4df\" state=\"active\" cid=\"sip:79262948589@multifon.ru\"></instance>\r\n" +
                "   </resource>\r\n" +
                "</list>\r\n" +
                "\r\n" +
                "--boundary1\r\n" +
                "Content-Type:application/pidf+xml\r\n" +
                "Content-ID: <sip:79262056390@multifon.ru>\r\n" +
                "\r\n" +
                "<?xml version=\"1.0\"encoding=\"UTF-8\"?>\r\n" +
                "<pr:presence xmlns:pr=\"urn:ietf:params:xml:ns:pidf\"entity=\"sip:79262056390@multifon.ru\">\r\n" +
                "  <pr:tuple id=\"a01db172-e8ed-46b7-af81-dbd9b6dbe5c6\">\r\n" +
                "    <pr:status>\r\n" +
                "      <pr:basic>open</pr:basic>\r\n" +
                "    </pr:status>\r\n" +
                "    <op:service-description xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\">\r\n" +
                "      <op:service-id>NeuStar.IM</op:service-id>\r\n" +
                "      <op:version>1.000</op:version>\r\n" +
                "    </op:service-description>\r\n" +
                "    <pdm:deviceID xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\">urn:uuid:b0f4fb7e-81a0-4d61-8197-5ac0b473d87b</pdm:deviceID>\r\n" +
                "    <pr:note/>\r\n" +
                "    <pr:timestamp>2010-08-19T05:10:42Z</pr:timestamp>\r\n" +
                "  </pr:tuple>\r\n" +
                "  <pdm:person xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\"id=\"d7f909bf-e40a-4a3a-ab88-583c7b50fb03\">\r\n" +
                "    <rpid:user-input xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\">active</rpid:user-input>\r\n" +
                "    <ci:display-name xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"/>\r\n" +
                "    <ci:homepage xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"/>\r\n" +
                "    <ci:icon xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\">https://sers/content/presence/sip:79262056390@multifon.ru/avatar/my_avatar.jpg</ci:icon>\r\n" +
                "    <op:overriding-willingness xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\">\r\n" +
                "      <op:basic>open</op:basic>\r\n" +
                "    </op:overriding-willingness>\r\n" +
                "    <rpid:activities xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\">\r\n" +
                "      <rpid:unknown/>\r\n" +
                "    </rpid:activities>\r\n" +
                "    <rpid:mood xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\">\r\n" +
                "      <rpid:ashamed/>\r\n" +
                "    </rpid:mood>\r\n" +
                "    <pdm:note/>\r\n" +
                "    <pdm:timestamp>2010-08-19T05:10:42Z</pdm:timestamp>\r\n" +
                "  </pdm:person>\r\n" +
                "  <pdm:device xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\"id=\"f3545397-68f3-4984-a543-a7261b196df9\">\r\n" +
                "    <pr:note>D???D?????D?D°ËD?D?</pr:note>\r\n" +
                "    <pdm:deviceID>b0f4fb7e-81a0-4d61-8197-5ac0b473d87b</pdm:deviceID>\r\n" +
                "    <pdm:timestamp>2010-08-19T05:10:42Z</pdm:timestamp>\r\n" +
                "  </pdm:device>\r\n" +
                "</pr:presence>\r\n" +
                "\r\n" +
                "\r\n" +
                "--boundary1\r\n" +
                "Content-Type:application/pidf+xml\r\n" +
                "Content-ID: <sip:79255894279@multifon.ru>\r\n" +
                "\r\n" +
                "<?xml version=\"1.0\"encoding=\"UTF-8\"?>\r\n" +
                "<pr:presence xmlns:pr=\"urn:ietf:params:xml:ns:pidf\" entity=\"sip:79255894279@multifon.ru\">\r\n" +
                "  <pr:tuple id=\"1234\">\r\n" +
                "    <pr:status>\r\n" +
                "      <pr:basic>closed</pr:basic>\r\n" +
                "    </pr:status>\r\n" +
                "    <op:willingness xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\">\r\n" +
                "      <op:basic>closed</op:basic>\r\n" +
                "    </op:willingness>\r\n" +
                "  </pr:tuple>\r\n" +
                "</pr:presence>\r\n\n\n" +
                "--boundary1\r\n" +
                "Content-Type:application/pidf+xml\r\n" +
                "Content-ID:";

        BaseSipMessage message = MessageParser.parse(source);
        System.out.println(message.buildContent());
        List retrievePidfDocsBodies = sipMessageUtil.retrievePidfDocsBodies(message);
        System.out.println(retrievePidfDocsBodies);
    }


    private static SipMessageUtil sipMessageUtil = new SipMessageUtil<BaseSipMessage>() {
        
        public int getStatusCode(final BaseSipMessage msg) {
            Response baseSipMessage = (Response) msg;
            return baseSipMessage.getStatusCode();
        }

        
        public boolean isClientClassResponse(final BaseSipMessage msg) {
            Response responseMsg = (Response) msg;
            return responseMsg.getResponseClass() == ResponseClass.Client;
        }

        
        public boolean isResponse(final BaseSipMessage msg) {
            return msg instanceof Response;
        }

        
        public boolean isMessagesEqual(final BaseSipMessage firstMsg, final BaseSipMessage secondMsg) {
            boolean retValue = false;

            if (firstMsg instanceof Request) {
                retValue = ((Request) firstMsg).isEqualTo((Request) secondMsg);
            }
            else if (firstMsg instanceof Response) {
                retValue = ((Response) firstMsg).isEqualTo((Response) secondMsg);
            }

            return retValue;
        }

        
        public boolean isDuplicateDetected(final StackContext stackContext, final BaseSipMessage msg) {
            Dialog dialog = stackContext.getDialogStorage().findDialogForMessage(msg);
            return dialog != null && dialog.getMessageHistory().hasDuplicateMessage(msg);
        }

        
        public String messageShortDescription(final BaseSipMessage msg) {
            return msg.shortDescription();
        }

        
        public BaseSipMessage buildResponse(final Dialog dialog, final BaseSipMessage msg, final int statusCode, final String message) {
            Request request = (Request) msg;
            return dialog.getMessageBuilderFactory().getResponseBuilder().buildMessage(request, statusCode, message);
        }

        
        public BaseSipMessage buildStatelessResponse(final StackContext context, final BaseSipMessage msg, final int statusCode, final String message) {

            Dialog newDlg = new Dialog(
                    InitiateParty.REMOTE,
                    context.getRegistrationIdentity(),
                    msg.getFrom().getUri().getShortURI(),
                    msg.getCallId(),
                    context
            );

            return newDlg.getMessageBuilderFactory().getResponseBuilder().buildMessage(msg, statusCode, message);
        }

        
        public String getCallId(final BaseSipMessage msg) {
            return msg.getCallId();
        }


        public Protocol getMessageTransport(final BaseSipMessage msg) {

            Protocol retValue = null;

            final List<Via> vias = msg.getVias();
            if (vias != null && vias.size() > 0 && vias.get(0) != null) {
                final Via via = vias.get(0);
                retValue = via.getProtocol();
            }
            else {
                assert false : "Message doesn't contain Transport info in it's via header " + msg.buildContent();
            }

            return retValue;
        }

        public boolean isMessageRFC3261Compilant(final BaseSipMessage msg) {

            boolean retValue = false;

            List<Via> viaList = msg.getVias();
            if (viaList != null && viaList.size() > 0) {
                Via topMostVia = viaList.get(0);
                Param branch = topMostVia.getParamsList().get(BRANCH_PARAM);

                if (branch == null) {
                    branch = topMostVia.getUri().getHeaders().get(BRANCH_PARAM);
                }

                if (branch != null) {
                    String branchValue = branch.getValue();
                    //TODO AK: Sometimes sip.movial.com adds Via with fake branch(branch="0").
                    //Issue has been forwarded to to the haloya-server guys.
                    retValue = branchValue.equals("0") || branchValue.startsWith("z9hG4bK");
                }
            }
            else {
                retValue = true;
            }

            return retValue;
        }

        
        public int calcMessageHash(final BaseSipMessage msg) {
            return msg.calcHash();
        }

        
        public boolean isRequest(final BaseSipMessage msg) {
            return msg instanceof Request;
        }

        public boolean isSuccessResponse(final BaseSipMessage msg) {
            return msg instanceof Response && ((Response) msg).getResponseClass() == ResponseClass.Success;
        }

        
        public String getMessageBranch(final BaseSipMessage msg) throws NullPointerException {
            if (msg == null) {
                throw new NullPointerException("Cannot handle null value. Now passed " + msg);
            }

            final List<Via> viaList = msg.getVias();

            String retValue = null;
            if (viaList != null && viaList.size() > 0) {
                final Via via = viaList.get(0);

                if (via != null) {
                    final String paramToLookUp = "branch";
                    retValue = getParam(paramToLookUp, via.getParamsList());

                    if (retValue == null) {
                        retValue = getParam(paramToLookUp, via.getUri().getHeaders());
                    }
                }
            }

            if (retValue != null) {
                retValue = retValue.replaceAll(";.*", "");
            }

            return retValue;
        }

        
        public NotifyInfo getNotifyInfo(BaseSipMessage msg) {
            String notifyEvent = msg.getEvent().getValue();
            String notifySubscriptionState = msg.getSubscriptionState().getValue();

            final RemoteStateDefault.RemoteStateBuilder builder = new RemoteStateDefault.RemoteStateBuilder();
            builder.value(notifySubscriptionState);

            final ParamList paramList = msg.getSubscriptionState().getParamsList();
            if (paramList != null) {
                for (RemoteStateParam stateParam : RemoteStateParam.values()) {
                    Param param = paramList.get(stateParam.stringValue());
                    if (param != null) {
                        final String value = param.getValue();
                        builder.param(stateParam, value);
                    }
                }
            }

            final RemoteState remoteState = builder.build();

            final byte[] body = msg.getBody();


            final List<String> notifyBodyMessages = new ArrayList<String>();
            if (checkIsMultipart(msg.getContentType())) {
                List<String> retrievePidfDocsBodies;
                try {
                    retrievePidfDocsBodies = retrievePidfDocsBodies(msg);
                }
                catch (IOException e) {
                    retrievePidfDocsBodies = new ArrayList<String>();
                }
                notifyBodyMessages.addAll(retrievePidfDocsBodies);
            }
            else {
                //String notifyBodyMessage = body == null ? "": ;
                if (body != null) {
                    notifyBodyMessages.add(new String(body));
                }
            }

            return new DefaultNotifyInfo(
                    EventPackage.parse(notifyEvent),
                    remoteState,
                    notifyBodyMessages.toArray(new String[0])
            );
        }

        public List<String> retrievePidfDocsBodies(BaseSipMessage message) throws IOException {
            final List<String> pidfDocsBodies;

            final String boundary = createBoundary(message.getContentType());

            final List<BodyPart> bodyParts = splitToBodyParts(message.getBody(), boundary);

            final BodyPart rlmiBodyPart = extractRlmiPart(bodyParts);
            final Map<String, BodyPart> pifdParts = extractPifdParts(bodyParts);

            List<String> pidfCids;
            try {
                pidfCids = extractPidfCids(rlmiBodyPart.getContent());
            }
            catch (SAXException e) {
                throw new IOException(e.getMessage());
            }
            catch (IOException e) {
                throw new IOException(e.getMessage());
            }
            catch (ParserConfigurationException e) {
                throw new IOException(e.getMessage());
            }

            pidfDocsBodies = extractPidfBodies(pidfCids, pifdParts);

            return pidfDocsBodies;
        }

        private List<String> extractPidfBodies(List<String> pidfCids, Map<String, BodyPart> pidfParts) {
            final List<String> pidfBodies = new ArrayList<String>();

            for (String pidfCid : pidfCids) {
                BodyPart bodyPart = pidfParts.get(pidfCid);
                if (bodyPart != null) {
                    pidfBodies.add(new String(bodyPart.getContent()));
                }
                else {
                    Logger.log("MessageUtilHolder", "extractPidfBodies#can't find body for pidfCid = " + pidfCid);
                }
            }

            return pidfBodies;
        }

        private BodyPart extractRlmiPart(final List<BodyPart> bodyParts) {
            final BodyPart rlmiBodyPart = CollectionsUtils.find(bodyParts, new CollectionsUtils.Predicate<BodyPart>() {
                //Content-Type: application/rlmi+xml
                private static final String CONTENT_TYPE_RLMI = "application/rlmi+xml";
                public boolean evaluate(BodyPart part) {
                    final BodyHeader contentTypeHeader = CollectionsUtils.find(part.getHeaders(), new CollectionsUtils.Predicate<BodyHeader>() {
                        
                        public boolean evaluate(BodyHeader header) {
                            return header.getName().equalsIgnoreCase(Header.Content_Type.stringValue());
                        }
                    });

                    return contentTypeHeader != null && CONTENT_TYPE_RLMI.equalsIgnoreCase(contentTypeHeader.getValue());
                }
            });

            assert rlmiBodyPart != null;

            return rlmiBodyPart;
        }

        private Map<String, BodyPart> extractPifdParts(final List<BodyPart> bodyParts) {
            final Map<String, BodyPart> pifdParts = new HashMap<String, BodyPart>();

            final String CONTENT_TYPE_PIFD = "application/pidf+xml";

            for (BodyPart bodyPart : bodyParts) {

                boolean isPidfContentType = false;
                String contentIdValue = null;

                for (BodyHeader bodyHeader : bodyPart.getHeaders()) {
                    if (bodyHeader.getName().equalsIgnoreCase(Header.Content_Type.stringValue())) {
                        isPidfContentType = CONTENT_TYPE_PIFD.equalsIgnoreCase(bodyHeader.getValue());
                    }
                    if (bodyHeader.getName().equalsIgnoreCase(Header.Content_ID.stringValue())) {
                        contentIdValue = bodyHeader.getValue();
                        contentIdValue = contentIdValue.replace("<", "").replace(">", "");
                    }
                }

                if (isPidfContentType && contentIdValue != null) {
                    pifdParts.put(contentIdValue, bodyPart);
                }
            }

            assert pifdParts.size() > 0;

            return pifdParts;
        }


        private List<BodyPart> splitToBodyParts(byte[] body,
                                                final String boundary) {
            String messageBody = new String(body);


            String boundaryPattern = Pattern.quote(boundary) + "(\r\n)?";
            messageBody = messageBody.replaceFirst("--$", "").replaceFirst(boundaryPattern, "");
            String[] bodyPartsMessages = messageBody.split(boundaryPattern);
/*            System.out.println(bodyPartsMessages.length);

            
            messageBody = messageBody.replaceAll(boundary + "\r\n", boundary);
            messageBody = messageBody.replaceFirst(boundary, "");
            messageBody = messageBody.replaceFirst("--$", "");
            String[] bodyPartsMessages = messageBody.split(boundary);
*/
            //bodyPartsMessages
            List<BodyPart> bodyParts = new ArrayList<BodyPart>();
            for (String bodyPartMessage : bodyPartsMessages) {

                byte[] bytes = bodyPartMessage.getBytes();
                //if(bytes.length > 0 && !Arrays.equals("--".getBytes(), bytes)) {
                //if (bytes.length > 0 && !(bodyPartMessage.startsWith("--") && bodyPartMessage.length() <= 4)) {
                BodyPart bodyPart = BodyParser.parse(bytes);
                assert bodyPart != null;
                if (bodyPart != null) {
                    bodyParts.add(bodyPart);
                }
                else {
                    Logger.log("MessageUtilHolder", "splitToBodyParts#parser can't parse body part = " + new String(bytes));
                }
                //}
            }
            return bodyParts;
        }

        private String createBoundary(ParamHeader contentType) {
            Param boundaryParam = contentType.getParamsList().get("boundary");
            assert boundaryParam != null;
            String boundary = "--" + boundaryParam.getValue();
            return boundary;
        }

        private List<String> extractPidfCids(byte[] rlmiContent)
                throws SAXException, IOException, ParserConfigurationException {
            final List<String> pidfCids = new ArrayList<String>();

            Document doc;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder db = factory.newDocumentBuilder();

            InputSource inStream = new InputSource();

            inStream.setCharacterStream(new StringReader(new String(rlmiContent)));
            doc = db.parse(inStream);

            NodeList nodeList = doc.getElementsByTagName("list");

            for (int listIndex = 0; listIndex < nodeList.getLength(); listIndex++) {
                Node listNode = nodeList.item(listIndex);
                if (listNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element listElement = (Element) listNode;

                    NodeList resourceNodeList = listElement.getElementsByTagName("resource");

                    for (int entrIndex = 0; entrIndex < resourceNodeList.getLength(); entrIndex++) {
                        Node resourceNode = resourceNodeList.item(entrIndex);

                        if (resourceNode.getNodeType() == Node.ELEMENT_NODE) {
                            String pidfCid = extractPidfCidFromResourceNode((Element) resourceNode);
                            if (pidfCid != null) {
                                pidfCids.add(pidfCid);
                            }
                        }
                    }
                }

            }

            return pidfCids;
        }

        private String extractPidfCidFromResourceNode(Element resourceNode) {
            String pidfCid = null;

            NodeList instanceListNode = resourceNode.getElementsByTagName("instance");
            if (instanceListNode != null && instanceListNode.getLength() > 0) {
                Node instanceNode = instanceListNode.item(0);

                if (instanceNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element instanceElement = (Element) instanceNode;
                    if (instanceNode != null) {
                        pidfCid = instanceElement.getAttribute("cid");
                    }
                }
            }

            return pidfCid;
        }

        private boolean checkIsMultipart(ParamHeader contentType) {
            return SipMessageUtils.checkIsMultipart(contentType);
        }

        
        public String getResponseLine(final BaseSipMessage msg) {
            String retValue = null;

            if (msg != null && msg instanceof Response) {
                Response response = (Response) msg;
                retValue = response.getResponseLine();
            }

            assert retValue != null : "Cannot get response line from " + msg == null ? null : msg.shortDescription();

            return retValue;
        }

        
        public boolean isWellFormedRequest(BaseSipMessage msg) {
            boolean retValue = true;
            MessageType messageType = MessageType.parse(msg.getMethod());

            if (messageType == MessageType.SIP_REFER) {
                final List<String> referToHeader = msg.getCustomHeader(Header.ReferTo);
                if (!referToHeader.isEmpty()) {
                    final UriHeader referToUri = SipUriParser.parseUri(referToHeader.get(0));

                    if (referToUri == null) {
                        retValue = false;
                    }
                }

            }

            return retValue;
        }

        private String getParam(String paramName, ParamList paramList) {
            String retValue = null;

            if (paramList != null) {
                final Param param = paramList.getParams().get(paramName);
                if (param != null) {
                    retValue = param.getValue();
                }
            }

            return retValue;
        }

        public MessageType[] getAllowedMessages(final CommonRegistry commonRegistry, final Configuration config) {
            List<MessageType> supportedRequests = new ArrayList<MessageType>();
            if (commonRegistry != null && commonRegistry.getMethods() != null)
                supportedRequests.addAll(Arrays.asList(commonRegistry.getMethods()));

            //final List<OptionFeature> supportedFeatures = Arrays.asList(getSupportedFeatures());

            boolean prackSupported = config.isFeatureSupported(OptionFeature._100REL);
            if (!prackSupported) {
                supportedRequests.remove(MessageType.SIP_PRACK);
            }

            return supportedRequests.toArray(new MessageType[supportedRequests.size()]);
        }

        public BaseSipMessage buildDummyMessage() {
            return new BaseSipMessage.Builder(BaseSipMessage.Builder.Type.REQUEST)
                    .requestUri(new SipUri.SipUriBuilder().domain("test.com").buildUri())
                    .callId(SIPUtil.newCallId())
                    .build();
        }
    };

    public static boolean isValidUri(final Configuration configuration, final String uriToTest){
        Collection<String> specialUris = configuration.getSpecialUris();
        return specialUris.contains(uriToTest) || SipUriParser.parseUri(uriToTest) != null;
    }

    private MessageUtilHolder() {
    }

    public static <T> SipMessageUtil<T> getSIPMessageUtil() {
        return sipMessageUtil;
    }

    public static <T> MessageUtil<T> getMessageUtil() {
        return messageUtil;
    }

    public static <T> MsrpMessageUtil<T> getMSRPMessageUtil() {
        return msrpMessageUtil;
    }
}
