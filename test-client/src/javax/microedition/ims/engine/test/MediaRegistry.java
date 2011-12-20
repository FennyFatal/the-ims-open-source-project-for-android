package javax.microedition.ims.engine.test;

import android.util.Log;
import com.android.ims.core.media.StreamMediaImpl;

import javax.microedition.ims.core.Session;
import javax.microedition.ims.core.media.*;
import javax.microedition.ims.media.Player;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class MediaRegistry {
    private static final String TAG = "MediaRegistry";

    public static final MediaRegistry INSTANCE = new MediaRegistry();

    private static Map<MediaBuilderType, MediaBuilder> mediaBuilders = new HashMap<MediaBuilderType, MediaBuilder>();

    static public enum MediaBuilderType {
        StreamMediaAudio, StreamMediaVideo/*, BasicRelaibleMedia*/
    }

    static abstract class MediaBuilder {
        abstract Media addMedia(Session session);

        void removeMedia(final Session session, final Media media) {
            session.removeMedia(media);
        }
    }

    public static class MediaListenerImpl implements MediaListener {
        private Player receivingPlayer, sendingPlayer;

        
        public void modeChanged(final Media media) {
            Log.i("MediaListenerImpl", "modeChanged#started");

            try {
                //get the players
                if(media.canRead()) {
                    receivingPlayer = ((StreamMediaImpl) media).getReceivingPlayer();
                }
                if(media.canWrite()) {
                    sendingPlayer = ((StreamMediaImpl) media).getSendingPlayer();
                }

                //special case 1: Audio and canRead=false and canWrite=true (means put on hold!)
                StreamMedia sm = (StreamMedia) media;
                if(!media.canRead() && media.canWrite() && sm.getStreamType() == StreamMedia.STREAM_TYPE_AUDIO) {
                    //put on hold:
                    // 1) stop receiving
                    if(receivingPlayer != null) receivingPlayer.putOnHold(true);
                    // 2) put sending on hold
                    if(sendingPlayer != null) sendingPlayer.putOnHold(true);
                } else {
                    //special case 2: Audio and canRead=true and canWrite=true and player was on hold (means resume call)
                    if(media.canRead() && media.canWrite() && 
                            sm.getStreamType() == StreamMedia.STREAM_TYPE_AUDIO && 
                            sendingPlayer != null && sendingPlayer.isOnHold()) {
                        //resume call:
                        // 1) start receiving
                        receivingPlayer.putOnHold(false);
                        // 2) resume sending
                        sendingPlayer.putOnHold(false);
                    } else {
                        //the normal start/stop cases (plus special case 3: video)
                        if (media.canRead()) {
                            //receivingPlayer = ((StreamMediaImpl) media).getReceivingPlayer();
                            receivingPlayer.start();
                        } else if(receivingPlayer != null){                    
                            receivingPlayer.stop();
                        }

                        /* SendOnly is a bit problematic: In case of audio media, we're
                           actually doing HOLD, so nothing is expected to be sent. However,
                           with video the one-way-video is a valid use case. So, the client
                           needs to decide what to do based on context */
                        if (media.canWrite()) {
                            //StreamMedia sm = (StreamMedia) media;

                            if (sm.canRead() || sm.getStreamType() == StreamMedia.STREAM_TYPE_VIDEO) {
                                //sendingPlayer = sm.getSendingPlayer();
                                int channel = (receivingPlayer != null? receivingPlayer.getChannel(): -1);
                                sendingPlayer.resetVideoCodec(codec, size, channel);
                                sendingPlayer.start();
                            }
                        } else  if(sendingPlayer != null) {                   
                            sendingPlayer.stop();
                        }
                    }
                } 
            } catch (MediaException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            Log.i("MediaListenerImpl", "modeChanged#finished");
        }
    }

    static {
        mediaBuilders.put(MediaBuilderType.StreamMediaAudio, new MediaBuilder() {
            
            public Media addMedia(final Session session) {
                StreamMedia media = (StreamMedia) session.createMedia(Media.MediaType.StreamMedia, Media.DIRECTION_SEND_RECEIVE);
                media.setStreamType(StreamMedia.STREAM_TYPE_AUDIO);
                MediaDescriptor audioDescriptor = media.getMediaDescriptors()[0];
                audioDescriptor.setMediaTitle("StreamMediaAudio");

                media.setMediaListener(new MediaListenerImpl());

                return media;
            }
        });

        mediaBuilders.put(MediaBuilderType.StreamMediaVideo, new MediaBuilder() {
            
            public Media addMedia(final Session session) {
                StreamMedia media = (StreamMedia) session.createMedia(Media.MediaType.StreamMedia, Media.DIRECTION_SEND_RECEIVE);
            	//StreamMedia media = (StreamMedia) session.createMedia("StreamMedia", Media.DIRECTION_SEND);
                media.setStreamType(StreamMedia.STREAM_TYPE_VIDEO);
                MediaDescriptor audioDescriptor = media.getMediaDescriptors()[0];
                audioDescriptor.setMediaTitle("StreamMediaVideo");

                media.setMediaListener(new MediaListenerImpl());

                return media;
            }
        });
    }


    private MediaRegistry() {
    }

    /**
     * Return media builder for specified type.
     *
     * @param type - specified type
     * @return media builder for specified type
     * @throws IllegalArgumentException - unknown media builder type
     */
    public MediaBuilder findBuilder(MediaBuilderType type) {
        MediaBuilder builder = mediaBuilders.get(type);
        if (builder == null) {
            throw new IllegalArgumentException("Unknown media builder type: " + type);
        }
        return builder;
    }

    public void subscribeLisnersToActiveMedia(Media[] medias) {
        Log.i(TAG, "subscribeLisnersToActiveMedia#");
        for (Media media : medias) {
            if (media instanceof StreamMediaImpl) {
                //AK: STATE_ACTIVE - for early media negotiation(resource reservation)
                if(media.getState() == Media.STATE_PENDING || media.getState() == Media.STATE_ACTIVE) {
                    Log.i(TAG, "subscribeLisnersToActiveMedia#listener added to media: " + media);
                    media.setMediaListener(new MediaListenerImpl());    
                }
            }
        }
    }

    private static int codec, size;
    public void setVideoCodec(int codec, int size) {
        this.codec = codec;
        this.size = size;
    }
}
