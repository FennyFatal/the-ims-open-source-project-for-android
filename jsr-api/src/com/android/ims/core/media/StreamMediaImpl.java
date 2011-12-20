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

package com.android.ims.core.media;

import android.content.Context;
import android.media.MediaRecorder;
import android.net.rtp.AudioCodec;
import android.util.Log;
import com.android.ims.configuration.AppConfiguration;
import com.android.ims.configuration.Codec;
import com.android.ims.core.DtmfPayload;
import com.android.ims.core.media.player.StagefrightPlayer;
import com.android.ims.core.media.player.AndroidVoipPlayer;
import com.android.ims.core.media.util.DirectionUtils;
import com.android.ims.core.media.util.StreamTypeUtils;
import com.android.ims.util.Utils;

import javax.microedition.ims.core.media.MediaException;
import javax.microedition.ims.core.media.StreamMedia;
import javax.microedition.ims.media.Player;
import javax.microedition.ims.media.PlayerExt;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * RTP-based reference implementation {@link StreamMedia}.
 *
 * @author ext-akhomush
 */
public class StreamMediaImpl extends MediaImpl implements StreamMedia {
    private final static String TAG = "StreamMediaImpl";
    public static final String PROTOCOL_RTP_AVP = "RTP/AVP";
    
    //TODO AK for test
    public static final String PROTOCOL_RTP_SAVP = "RTP/SAVP";
    //public static final String PROTOCOL_RTP_SAVP = "RTP/AVP";

    private int mStreamType = STREAM_TYPE_AUDIO;
    private int mPreferredQuality;
    private String mSource;
    //private String protocol = PROTOCOL;

    private Player sendingPlayer;
    private PlayerExt receivingPlayer;

    //private DatagramSocket rtpSocket;
    private final AppConfiguration configuration;
    private final /*static */Context mContext;
    private final DtmfPayload dtmfPayload;

    /**
     * Construct StreamMedia from local offer
     *
     * @param localAddress - local address
     * @param direction    - media direction
     */
    public StreamMediaImpl(int direction, final String localAddress,
            final AppConfiguration config, Context context, DtmfPayload dtmfPayload) {
    	super(config.isForceSrtp());
        this.configuration = config;
        this.mContext = context;
        this.dtmfPayload = dtmfPayload;
        setInitialDescriptors(direction, localAddress, config);
        setMediaInitiated(true);
    }

    /**
     * Construct StreamMedia from remote offer
     *
     * @param mediaDescriptor
     */
    public StreamMediaImpl(
            final MediaDescriptorImpl offeredDescriptor,
            final String localAddress,
            final AppConfiguration config,
            final Context context,
            DtmfPayload dtfmPayload) {

    	super(offeredDescriptor.isSecured());
        this.configuration = config;
        this.mContext = context;
        this.dtmfPayload = dtfmPayload;
        
        offeredDescriptor.setMedia(this);
        offeredDescriptor.setDirection(DirectionUtils.reverseDirection(offeredDescriptor.getDirection()));
        offeredDescriptor.setConnectionAddress(localAddress);
        offeredDescriptor.setPort(Utils.generateRandomPortNumber());
        
        mStreamType = StreamTypeUtils.convertToType(offeredDescriptor.getMediaType());
        if (mStreamType == STREAM_TYPE_AUDIO) {
            mSource = "capture://audio";
        } else if (mStreamType == STREAM_TYPE_VIDEO) {
            mSource = "capture://video";
        }

        setMediaDescriptors(new MediaDescriptorImpl[]{offeredDescriptor});
    }

	private void setInitialDescriptors(int direction, String localAddress,
            AppConfiguration configuration) {
        MediaDescriptorImpl[] mediaDescriptors = createMediaDescriptors(direction, localAddress, mStreamType, configuration);
        setMediaDescriptors(mediaDescriptors);
    }
    
    private MediaDescriptorImpl[] createMediaDescriptors(int direction, final String localAddress, 
            int mStreamType, AppConfiguration configuration) {
        List<MediaDescriptorImpl> descriptors = new ArrayList<MediaDescriptorImpl>();

        switch (mStreamType) {
        case STREAM_TYPE_AUDIO: {
            MediaDescriptorImpl audioDescriptor = createAudioDescriptor(direction, localAddress, configuration);
            descriptors.add(audioDescriptor); 
            break;
        } case STREAM_TYPE_VIDEO: {
            MediaDescriptorImpl videoDescriptor = createVideoDescriptor(direction, localAddress, configuration);
            descriptors.add(videoDescriptor); 
            break;
        } /*case STREAM_TYPE_AUDIO_VIDEO: {
            MediaDescriptorImpl audioDescriptor = createAudioDescriptor(direction, localAddress);
            MediaDescriptorImpl videoDescriptor = createVideoDescriptor(direction, localAddress);
            descriptors.add(audioDescriptor);
            descriptors.add(videoDescriptor); 
            break;
        } */
            default:
                Log.i(TAG, "createMediaDescriptors#unknown mStreamType = " + mStreamType);
                break;
        }

        return descriptors.toArray(new MediaDescriptorImpl[0]);
    }

    private MediaDescriptorImpl createAudioDescriptor(int direction,
            String localAddress, AppConfiguration config) {
        int localAudioPort = Utils.generateRandomPortNumber();

        MediaDescriptorImpl.Builder builder = new MediaDescriptorImpl.Builder()
                .media(this)
                .direction(direction)
                .mediaType(StreamTypeUtils.convertToString(STREAM_TYPE_AUDIO))
                .port(localAudioPort)
                .connectionAddress(localAddress)
                .isOwnAddress(true)
                .transport(obtainTransport(config));
        
        if(config.isForceSrtp()) {
        	CryptoParam cryptoParam = createCryptoParam(config);
			builder.cryptoParams(cryptoParam);
        }

        MediaDescriptorImpl mediaDescriptor = builder.build(); 

        Set<Codec> audioCodecs = new HashSet<Codec>(configuration.getAudioCodecs());
        if(dtmfPayload == DtmfPayload.OUTBAND) {
            audioCodecs.add(Codec.valueOf("106 telephone-event/8000"));
        }
        
        addRtpMapToDescriptor(mediaDescriptor, audioCodecs);
        
        return mediaDescriptor;
    }

    private MediaDescriptorImpl createVideoDescriptor(
            int direction, String localAddress, AppConfiguration conf) {
        int localVideoPort = Utils.generateRandomPortNumber();

        boolean forceSrtp = conf.isForceSrtp();
        MediaDescriptorImpl.Builder builder =  new MediaDescriptorImpl.Builder()
        	.media(this)
        	.direction(direction)
        	.mediaType(StreamTypeUtils.convertToString(STREAM_TYPE_VIDEO))
        	.port(localVideoPort)
        	.connectionAddress(localAddress)
        	.isOwnAddress(true)
        	.transport(obtainTransport(conf));

        if(forceSrtp) {
        	CryptoParam cryptoParam = createCryptoParam(conf);
        	builder.cryptoParams(cryptoParam);
        }
        
        final MediaDescriptorImpl mediaDescriptor = builder.build();
        
        addRtpMapToDescriptor(mediaDescriptor, configuration.getVideoCodecs());
        return mediaDescriptor;
    }

	private String obtainTransport(AppConfiguration conf) {
        return conf.isForceSrtp()? PROTOCOL_RTP_SAVP: PROTOCOL_RTP_AVP;
    }

    private CryptoParam createCryptoParam(AppConfiguration config) {
		final String localEncriptionKey = createAuthKey(config);
		CryptoParam cryptoParam = new CryptoParam(1, config.getSrtpCryptoAlgorithm(), localEncriptionKey);
		return cryptoParam;
	}

	private String createAuthKey(AppConfiguration configuration) {
		String localEncriptionKey  = null;
		String cryptoAlgorithm = configuration.getSrtpCryptoAlgorithm();
		try {
			localEncriptionKey = EncriptionKeyGenerator.generateEncriptionKey(cryptoAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		}
		return localEncriptionKey;
	}
    
    private void addRtpMapToDescriptor(final MediaDescriptorImpl mediaDescriptor, final Set<Codec> codecs) {
        StringBuilder mediaFormat = new StringBuilder("");
        
        if (codecs == null) {
            throw new IllegalStateException("No codecs!");
        }
        
        for (Codec codec : codecs) {
            mediaFormat.append(codec.getType()).append(" ");
            mediaDescriptor.addAttributeInternal(String.format("rtpmap:%s", codec.getContent()));
        }
        mediaDescriptor.setMediaFormat(mediaFormat.toString().trim());
    }

    /**
     * @see StreamMedia#getStreamType()
     */
    
    public int getStreamType() {
        return mStreamType;
    }

    /**
     * @see StreamMedia#setPreferredQuality(int)
     */
    
    public void setPreferredQuality(int quality) {
        if (getState() != STATE_INACTIVE) {
            throw new IllegalStateException("State must be STATE_INACTIVE, state: " + getState());
        }

        if (!isQualityValueValid(quality)) {
            throw new IllegalArgumentException("Not a valid quality: " + quality);
        }

        this.mPreferredQuality = quality;
    }

    private boolean isQualityValueValid(int quality) {
        return quality == QUALITY_HIGH || quality == QUALITY_LOW ||
                quality == QUALITY_MEDIUM;
    }

    /**
     * @see StreamMedia#setSource(String)
     */
    
    public void setSource(String source) {
        if (!isSourceSupported(source)) {
            throw new IllegalArgumentException("Invalid stream media source: " + source);
        }

        //TODO check if device supports video, audio

        if (getState() != STATE_INACTIVE) {
            throw new IllegalStateException("State must be STATE_INACTIVE, state: " + getState());
        }

        this.mSource = source;
    }

    private static boolean isSourceSupported(String source) {
        if (source == null) {
            return false;
        }

        boolean isSourceSupported = false;

        if (source.startsWith("file://")) {
            isSourceSupported = true;
        } else if (source.startsWith("capture://")) {
            String device = source.substring("capture://".length());
            if ("audio".equals(device) || "video".equals(device) ||
                    "audio_video".equals(device)) {
                isSourceSupported = true;
            }
        }
        return isSourceSupported;
    }

    /**
     * @see StreamMedia#setStreamType(int)
     */
    
    public void setStreamType(int type) {
        if (getState() != STATE_INACTIVE) {
            throw new IllegalStateException("State must be STATE_INACTIVE, state: " + getState());
        }

        if (!isStreamTypeValid(type)) {
            throw new IllegalArgumentException("Unknown stream type: " + type);
        }

        if (mStreamType != type) {
            mStreamType = type;
            String localAddress = getLocalMediaDescriptor(0).getConnectionAddress();
            setInitialDescriptors(getDirection(), localAddress, configuration);
        }

        this.mStreamType = type;
    }

    /**
     * @see MediaImpl#createMediaProposalBasedOnIncomingOffer(MediaDescriptorImpl)
     */
    
    protected MediaProposalImpl createMediaProposal() {
        return new StreamMediaProposalImpl(getLocalMediaDescriptors(),
                getStreamType());
    }

    private static boolean isStreamTypeValid(int streamType) {
        return streamType == STREAM_TYPE_AUDIO || /*streamType == STREAM_TYPE_AUDIO_VIDEO ||*/
                streamType == STREAM_TYPE_VIDEO;
    }

    /**
     * @see StreamMedia#getReceivingPlayer()
     */
    
    public Player getReceivingPlayer() throws IOException {
        if (!canRead()) {
            throw new IOException("there is no data flow for the media");
        }

        if (receivingPlayer == null) {
            MediaDescriptorImpl descriptor = getLocalMediaDescriptor(0);
            CryptoParam remoteCryptoParam = extractRemoteCryptoParam();
            String remoteCryptoKey = remoteCryptoParam != null? remoteCryptoParam.getKey(): null;
            receivingPlayer = createAndRealizeReceivingPlayer(descriptor, this, mStreamType, remoteCryptoKey);
        }

        return receivingPlayer;
    }

    private /*static */PlayerExt createAndRealizeReceivingPlayer(MediaDescriptorImpl localDescriptor,
    		final StreamMedia media, int type, String remoteCryptoKey) throws IOException {
        Log.i(TAG, "createAndRealizeReceivingPlayer#started");
        PlayerExt player;

        try {
            {
                if (type == STREAM_TYPE_AUDIO) {
                    player = new AndroidVoipPlayer(mContext, Player.PlayerType.PLAYER_RECEIVING,
                            media, localDescriptor, null, AudioCodec.PCMU, -1, remoteCryptoKey, -1);
                    //player = new StagefrightPlayer(mContext, Player.PlayerType.PLAYER_RECEIVING,
                    //        media, localDescriptor, null, AudioCodec.PCMU, -1, remoteCryptoKey, -1);
                } else {
                    player = new StagefrightPlayer(mContext, Player.PlayerType.PLAYER_RECEIVING,
                            media, localDescriptor, null, MediaRecorder.VideoEncoder.H263, -1, remoteCryptoKey);
                }
            }
        } catch (MediaException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }

        try {
            player.realize();
        } catch (MediaException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
        Log.i(TAG, "createAndRealizeReceivingPlayer#finished");
        return player;
    }

    /**
     * @see StreamMedia#getSendingPlayer()
     */
    
    public Player getSendingPlayer() throws IOException {
        if (!canWrite()) {
            throw new IOException("there is no data flow for the media");
        }

        if (sendingPlayer == null) {
            MediaDescriptorImpl remoteDescriptor = getRemoteMediaDescriptor(0);
            MediaDescriptorImpl localDescriptor = getLocalMediaDescriptor(0);
            final String authKey = extractCryptoKey(localDescriptor);
            sendingPlayer = createAndRealizeSendingPlayer(localDescriptor, remoteDescriptor,
                this, receivingPlayer != null? receivingPlayer.getChannel(): -1, mStreamType, authKey, dtmfPayload);
        }

        return sendingPlayer;
    }
    
    private String extractCryptoKey(MediaDescriptorImpl mediaDescriptor) {
    	CryptoParam cryptoParam = extractCryptoParam(mediaDescriptor);
    	return cryptoParam != null? cryptoParam.getKey(): null;
    }

	private CryptoParam extractCryptoParam(MediaDescriptorImpl mediaDescriptor) {
		CryptoParam[] cryptoParams = mediaDescriptor.getCryptoParams();
		return cryptoParams.length > 0? cryptoParams[0]: null;
	}

    private /*static */Player createAndRealizeSendingPlayer(MediaDescriptorImpl localDescriptor, MediaDescriptorImpl remoteDescriptor,
    		final StreamMedia media, int channel, int type, String authKey, DtmfPayload dtmfPayload) throws IOException {
        Log.i(TAG, "createAndRealizeSendingPlayer#started");
        Player player;

        try {
            int dtfmPayloadValue = (dtmfPayload == DtmfPayload.OUTBAND? 1: -1);
            {
                // TODO: Use StagefrightPlayer
                if (type == STREAM_TYPE_AUDIO) {
                    //player = new StagefrightPlayer(mContext, Player.PlayerType.PLAYER_SENDING,
                    //        media, localDescriptor, remoteDescriptor, AudioCodec.PCMU, -1, authKey, -1);
                    player = new AndroidVoipPlayer(mContext, Player.PlayerType.PLAYER_SENDING,
                            media, localDescriptor, remoteDescriptor, AudioCodec.PCMU, -1, authKey, -1);
                } else {
                    player = new StagefrightPlayer(mContext, Player.PlayerType.PLAYER_SENDING,
                            media, localDescriptor, remoteDescriptor, MediaRecorder.VideoEncoder.H263, -1, authKey);
                }
            }
        } catch (MediaException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }

        try {
            player.realize();
        } catch (MediaException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
        Log.i(TAG, "createAndRealizeSendingPlayer#ended");
        return player;
    }

/*   private DatagramSocket getRtpSocket() throws IOException {
        if (rtpSocket == null) {
            int localPort = getLocalMediaDescriptor(0).getPort();
            rtpSocket = prepareSocket(localPort);
        }
        return rtpSocket;
    }
*/
/*    private static DatagramSocket prepareSocket(int localPort) throws IOException {
        Log.i(TAG, "prepareRtpSocket#localPort" + localPort);
        DatagramSocket socket = new DatagramSocket(localPort);
        Log.i(TAG, "prepareRtpSocket#finished");
        return socket;
    }
*/
    /**
     * @see MediaImpl#handleSupportedCodec()
     */
    
    public boolean handleSupportedCodec() {
        boolean isSupported = false;

        MediaDescriptorImpl mediaDescriptor = getLocalMediaDescriptor(0);

        Set<Codec> supportedRtpMaps = new HashSet<Codec>();
        for (String attribute : mediaDescriptor.getAttributes()) {
            if (attribute.startsWith("rtpmap:")) {
                String codecValue = attribute.substring("rtpmap:".length());
                Codec codec = null;
                try {
                    codec = Codec.valueOf(codecValue);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                if(codec != null) {
                    if(isRtpMapSupported(codec, configuration)) {
                        supportedRtpMaps.add(codec);
                    }
                }
            }
        }
        isSupported = supportedRtpMaps.size() > 0;

        if (isSupported) {
            removeUnsupportedRtpMaps(mediaDescriptor, supportedRtpMaps);
        }

        return isSupported;
    }

    private void removeUnsupportedRtpMaps(final MediaDescriptorImpl mediaDescriptor, final Set<Codec> supportedRtpMaps) {
        //remove all attribute which started with rtpmap:
        for (String attribute : mediaDescriptor.getAttributes()) {
            if (attribute.startsWith("rtpmap:")) {
                mediaDescriptor.removeAttributeInternal(attribute);
            }
        }

        removeUnsupportedFmtParam(mediaDescriptor, supportedRtpMaps);

        addRtpMapToDescriptor(mediaDescriptor, supportedRtpMaps);
    }

    private void removeUnsupportedFmtParam(final MediaDescriptorImpl mediaDescriptor, final Set<Codec> supportedRtpMaps) {
        for (String attribute : mediaDescriptor.getAttributes()) {
            if (attribute.startsWith("fmtp:")) {
                boolean isFmtpValid = false;
                for (Codec codec : supportedRtpMaps) {
                    if (attribute.startsWith("fmtp:" + codec.getType())) {
                        isFmtpValid = true;
                        break;
                    }
                }
                if (!isFmtpValid) {
                    mediaDescriptor.removeAttributeInternal(attribute);
                }
            }
        }
    }

    private boolean isRtpMapSupported(final Codec codec, final AppConfiguration configuration) {
        boolean isValid = false;
        if(mStreamType == STREAM_TYPE_AUDIO) {
            isValid = configuration.isAudioCodecValid(codec);    
        } else if(mStreamType == STREAM_TYPE_VIDEO) {
            isValid = configuration.isVideoCodecValid(codec);
        }

        return isValid;
    }

    /**
     * @see MediaImpl#notifyPropertiesChanged()
     */
    
    protected void notifyPropertiesChanged() {
        Log.i(TAG, "notifyPropertiesChanged#");
        if (!getExists()) {
            try {
                closePlayers();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        //If canRead and/or canWrite returns false while a receiving and/or sending player is started, 
        //respectively, the IMS engine calls Player.deallocate on those players.
        boolean canRead = getCanRead();
        if (canRead) {
        	if(receivingPlayer != null && receivingPlayer.getAuthKey() == null) {
        		CryptoParam remoteCryptoParam = extractRemoteCryptoParam();
        		if(remoteCryptoParam != null) {
        			receivingPlayer.updateAuthKey(remoteCryptoParam.getKey());
        		}
        	}	
        } else {
            if (receivingPlayer != null &&
                    (receivingPlayer.getState() == Player.PREFETCHED || receivingPlayer.getState() == Player.STARTED)) {
                //receivingPlayer.deallocate();
            }
        }

        if (!getCanWrite()) {
            if (sendingPlayer != null &&
                    (sendingPlayer.getState() == Player.PREFETCHED || sendingPlayer.getState() == Player.STARTED)) {
                //sendingPlayer.deallocate();
            }
        }
    }

	private CryptoParam extractRemoteCryptoParam() {
		CryptoParam localCryptoParam = extractCryptoParamFromLocalDescriptor();
		CryptoParam remoteCryptoParam = localCryptoParam != null? extractCryptoParamByTagFromRemoteDescriptor(localCryptoParam.getTag()): null;
		return remoteCryptoParam;
	}

	private CryptoParam extractCryptoParamFromLocalDescriptor() {
		CryptoParam[] localCryptoParams = getLocalMediaDescriptor(0).getCryptoParams();
		return localCryptoParams.length > 0? localCryptoParams[0]: null;
	}

	private CryptoParam extractCryptoParamByTagFromRemoteDescriptor(final int acceptedCryptoTag) {
		CryptoParam remoteCryptoParam = null;
		MediaDescriptorImpl remoteMediaDescriptor = getRemoteMediaDescriptor(0);
		
		if(remoteMediaDescriptor != null) {
			for(CryptoParam cryptoParam: remoteMediaDescriptor.getCryptoParams()) {
				if(cryptoParam.getTag() == acceptedCryptoTag) {
					remoteCryptoParam = cryptoParam;
					break;
				}
			}
		}
		
		return remoteCryptoParam;
	}

    /**
     * @see MediaImpl#prepareMedia()
     */
    
    public void prepareMedia() throws IOException {
        Log.i(TAG, "prepareMedia#");
        setExists(true);
    }

    /**
     * @see MediaImpl#processMedia()
     */
    
    public void processMedia() throws IOException {
        Log.i(TAG, "processMedia#");
        setExists(true);
    }

    /**
     * @see MediaImpl#cleanMedia()
     */
    
    public void cleanMedia() {
        Log.i(TAG, "cleanMedia");
        setExists(false);
        try {
            closePlayers();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void closePlayers() throws IOException {
        if (sendingPlayer != null) {
            sendingPlayer.close();
        }
        
        if (receivingPlayer != null) {
            receivingPlayer.close();
        }
    }
}
