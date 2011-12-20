/*package javax.microedition.ims.core.msrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.msrp.filetransfer.FileDescriptor;
import javax.microedition.ims.messages.parser.msrp.MsrpUriParser;
import javax.microedition.ims.messages.utils.MsrpUtils;
import javax.microedition.ims.messages.wrappers.msrp.MsrpUri;
import javax.microedition.ims.messages.wrappers.sdp.Attribute;
import javax.microedition.ims.messages.wrappers.sdp.ConnectionInfo;
import javax.microedition.ims.messages.wrappers.sdp.DirectionsType;
import javax.microedition.ims.messages.wrappers.sdp.Media;
import javax.microedition.ims.messages.wrappers.sdp.SdpMessage;

public class MsrpSessionUtils {

    private MsrpSessionUtils() {
    }

    public static void generateSdpForMsrpFiletransfer(Dialog dialog, MsrpUri fromPath, int port, String address, boolean send, FileDescriptor[] fileDescriptors) {  
        assert (fileDescriptors != null && fileDescriptors.length > 0) : "Cannot create SDP for empty file list";
        dialog.getOutgoingSdpMessage().setSessionAddress(address);
        dialog.getOutgoingSdpMessage().setConnectionInfo(new ConnectionInfo(address));
        ArrayList<Media> mediaList = new ArrayList<Media>();
        for(FileDescriptor descriptor: fileDescriptors) {
            Media media = new Media();
            media.setType("message");
            media.setPort(port);
            media.setFormats(Arrays.asList(new String[]{"*"}));
            media.setDirection(send ? DirectionsType.DirectionSendOnly : DirectionsType.DirectionReceiveOnly);
            media.setProtocol("TCP/MSRP");
            List<Attribute> attributes = new ArrayList<Attribute>();
            attributes.add(new Attribute("setup", "active"));
            attributes.add(new Attribute("accept-types", "*"));
            attributes.add(new Attribute("accept-wrapped-types", "*"));
            attributes.add(new Attribute("path", fromPath.buildContent()));
            attributes.add(new Attribute("file-selector", descriptor.buildContent()));
            attributes.add(new Attribute("file-disposition", "attachment")); //"render"
            if(send){
                attributes.add(new Attribute("file-transfer-id", descriptor.getFileId())); //The SDP answerer MUST copy the value of the 'file-transfer-id' attribute in the SDP answer.
            } else {
                for(Attribute a : dialog.getIncomingSdpMessage().getMedias().get(0).getAttributes()){
                    if("file-transfer-id".equals(a.getField())){
                        attributes.add(a);
                        break;
                    }
                }
            }
            if(descriptor.getCreationDate() != null) {
                attributes.add(new Attribute("file-date", descriptor.getCreationDate().toString()));
            }
            //attributes.add(new Attribute("file-range", descriptor.getFileRange()[0]+"-"+descriptor.getFileRange()[1]));            

            //dialog.getMsrpData().setLocalPath(fromPath);
            media.getAttributes().addAll(attributes);
            mediaList.add(media);
        }
        dialog.getOutgoingSdpMessage().addMedias(mediaList);

    }

    public static void generateSdpForMsrpSession(Dialog dialog, MsrpUri fromPath, int port, String address, boolean active) {

        dialog.getOutgoingSdpMessage().setSessionAddress(address);
        dialog.getOutgoingSdpMessage().setConnectionInfo(new ConnectionInfo(address));
        Media media = new Media();
        media.setType("message");
        media.setPort(port);
        media.setFormats(Arrays.asList(new String[]{"*"}));
        media.setProtocol("TCP/MSRP");
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("setup", active ? "active" : "passive"));
        attributes.add(new Attribute("accept-types", "*"));
        attributes.add(new Attribute("path", fromPath.buildContent()));

        //dialog.getMsrpData().setLocalPath(fromPath);
        media.getAttributes().addAll(attributes);

        ArrayList<Media> mediaList = new ArrayList<Media>();
        mediaList.add(media);
        dialog.getOutgoingSdpMessage().addMedias(mediaList);

    }

    public static MsrpUri generateLocalPath(int port, String address, StackContext context) {
        //return MsrpUtils.generateUri(address, port, context.getConfig().getRegistrationName().getName());
        return null;
    }

    public static MsrpUri processIncomingSdp(SdpMessage sdp, Dialog dialog) {
        MsrpUri remoteMsrpUri = null;
        if (sdp != null && sdp.getMedias() != null) {
            for (Media m : sdp.getMedias()) {
                if (m.getProtocol().equals(MsrpUtils.TCP_MSRP)) {
                    Logger.log("Incoming call or INVITE response in MSRP service!!!!!!!!!!!!!!!!!!!");

                    String msrpRemotePath = null;

                    for (Attribute s : m.getAttributes()) {
                        String value = s.getValue();
                        Logger.log("Attr:" + value);
                        if (value != null && value.startsWith("msrp://")) {

                            msrpRemotePath = value;
                            remoteMsrpUri = MsrpUriParser.parse(value);

                            //dialog.getMsrpData().setRemotePath(remoteMsrpUri);

                            //prepareTransport(remoteMsrpUri.getPort(), remoteMsrpUri.getDomain());
                            //sendEmptyMsrpMessage(dialog, remoteMsrpUri);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        return remoteMsrpUri;

    }

}
*/