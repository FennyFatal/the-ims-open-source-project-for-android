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

package javax.microedition.ims.core.media;

import javax.microedition.ims.media.Player;
import java.io.IOException;

/**
 * The <code>StreamMedia</code> represents a standardized
 * application-independent streaming media that can be rendered in real-time.
 * The typical use is to stream audio or video content between endpoints in
 * real-time, in for example telephony applications. The IMS core manages the
 * media formats, codecs and protocols used in the transmission according to
 * relevant standard. It is possible to stream audio and video separately, as
 * well as combined audio and video.
 * </p><p>
 * By default, the <code>StreamMedia</code> object supports audio streaming,
 * which is mandatory for all devices claiming streaming support. The data is
 * sourced from a microphone of the device.
 * </p><p>
 * The originating endpoint may call <code>StreamMedia.setStreamType</code> to
 * explicitly set the media type for streaming, regardless of whether it is
 * going to send or receive media. This is not possible for the terminating
 * endpoint. For video (moving pictures), the camera is used to source video
 * content. Both peripherals are used for the combined audio video media. If the
 * terminal has several microphones, or cameras, the choice is
 * implementation-specific.
 * </p><p>
 * <p/>
 * It is possible for an originating endpoint to change the data source to
 * something else if needed. This is not available to the terminating endpoint,
 * thus limiting it to stream only from microphone and/or camera.
 * </p><p>
 * </p><h4>Players</h4>
 * A pair of players is used to control the data flow, and rendering. If
 * <code>canRead</code> returns <code>true</code>, the application may call
 * <code>Player.start</code> or <code>Player.prefetch</code> on the
 * receiving player, and then the IMS core starts receiving data and render.
 * If <code>canWrite</code> returns <code>true</code>, the application may
 * call <code>Player.start</code> or <code>Player.prefetch</code> for a
 * sending player, and then the IMS core starts sending data and render it. If
 * the methods return <code>false</code>, then <code>Player.start</code>
 * <p/>
 * and <code>Player.prefetch</code> throw <code>MediaException</code>.
 * <p>
 * If <code>canRead</code> and/or <code>canWrite</code> returns false while
 * a receiving and/or sending player is started, respectively, the IMS core
 * calls <code>Player.deallocate</code> on those players.
 * </p><p>
 * <p/>
 * It may be the case, depending on the underlying session negotiation process,
 * that the players got from the media are in an Unrealized or Realized state.
 * When <code>canRead</code> and <code>canWrite</code> eventually returns
 * true, the players are realized to enable <code>player.start</code>.
 * </p><p>
 * On a started player, the application can call <code>Player.stop</code> and
 * <code>Player.deallocate</code> even if the media mode properties (see
 * <code>Media</code> interface) are still true.
 * </p><p>
 * <p/>
 * A call to <code>Player.close</code> aborts the data flow transfer for that
 * player immediately. It is not possible to recover.
 * <p/>
 * </p><h4>Initializing the media</h4>
 * <code>StreamMedia</code> requires no further initialization before calling
 * <code>Session.start</code> or <code>Session.update</code>. However, the
 * developer should consider calling <code>setStreamType</code> or
 * <code>setSource</code>, and <code>setPreferredQuality</code>.
 * <p/>
 * <h2>Setting up streaming media session</h2>
 * <p/>
 * To setup the streaming media in a session, the originating endpoint goes
 * through the following steps:
 * <ul>
 * <li>Creates streaming media objects</li>
 * <li>Set direction</li>
 * <li>Set the streaming media type for the objects, or set the data source if
 * needed</li>
 * <li>start the session</li>
 * <li>get the players and start</li>
 * <p/>
 * </ul>
 * The terminating endpoint does the following in response to the invitation
 * <ul>
 * <li>Inspects the offered media components of the session</li>
 * <li>accept session</li>
 * <li>get the players and start</li>
 * </ul>
 * <p/>
 * <h2>Playback Examples</h2>
 * <p/>
 * <h4>Set up a bidirectional audio media</h4>
 * <p/>
 * <pre> try {
 * myMedia =
 * (StreamMedia)mySession.createMedia("StreamMedia",
 * Media.DIRECTION_SEND_RECEIVE);
 * myMedia.setStreamType(STREAM_TYPE_AUDIO);
 * myMedia.setSource("capture://audio");
 * mySession.start();
 * pIn = myMedia.getReceivingPlayer();
 * pOut = myMedia.getSendingPlayer();
 * } catch (ImsException ie) {
 * }
 * </pre>
 * <p/>
 * Wait until the media flow mode changes to allow read and write. The code
 * below can be placed in the <code>MediaListener.modeChanged</code> callback:
 * <p/>
 * <pre> if (myMedia.canRead()) {
 * try {
 * pIn.start();
 * } catch (MediaException me) {
 * }
 * }
 * <p/>
 * if (myMedia.canWrite()) {
 * try {
 * pOut.start();
 * } catch (MediaException me) {
 * }
 * }
 * <p/>
 * </pre>
 * <p/>
 * <h4>Stream from a multiplexed file</h4>
 * <p/>
 * <p/>
 * <pre> try {
 * myMedia =
 * (StreamMedia)mySession.createMedia("StreamMedia", Media.DIRECTION_SEND);
 * myMedia.setStreamType(STREAM_TYPE_AUDIO_VIDEO);
 * myMedia.setSource("file:///root/sample.mpg");
 * mySession.start();
 * pOut = myMedia.getSendingPlayer();
 * } catch (ImsException ie) {
 * }
 * </pre>
 * <p/>
 * As before, the player is started in the
 * <code>MediaListener.modeChanged</code> callback.
 * <p/>
 * <pre> if (myMedia.canWrite()) {
 * try {
 * pOut.start();
 * } catch (MediaException me) {
 * }
 * }
 * </pre>
 * <p/>
 * <h4>Putting a stream media on hold</h4>
 * RFC 3264 An Offer/Answer Model Session Description Protocol [RFC3264]
 * describes a procedure for putting media streams on hold, i.e. request the
 * remote to (temporarily) stop sending a media flow. A send receive media will
 * be given direction Send, and a Receive media gets the Inactive direction.
 * <p/>
 * <p/>
 * In the code example below, myMedia had direction Send Receive.
 * <p/>
 * <pre> try {
 * myMedia.setDirection(DIRECTION_SEND);
 * mySendingPlayer.deallocate();
 * mySession.update();
 * } catch (ImsException ie) {
 * }
 * </pre>
 * <p/>
 * <p/>
 * To resume myMedia, the application restores the original direction and then
 * start players in for example the <code>MediaListener.modeChanged</code>
 * callback:
 * <p/>
 * <pre> try {
 * if (myMedia.canRead()) {
 * myReceivingPlayer().start();
 * }
 * if (myMedia.canWrite()) {
 * mySendingPlayer().start();
 * }
 * } catch (MediaException me) {
 * }
 * </pre>
 */

public interface StreamMedia extends Media {
    /**
     * Enables the platform to choose a higher quality codec during the negotiation of the media.
     */
    static final int QUALITY_HIGH = 3;
    /**
     * Enables the platform to choose a lower quality codec during the negotiation of the media.
     */
    static final int QUALITY_LOW = 1;

    /**
     * Enables the platform to choose a medium quality codec during the negotiation of the media.
     */
    static final int QUALITY_MEDIUM = 2;

    /**
     * Indicates that the streaming will consist only of audio.
     */
    static final int STREAM_TYPE_AUDIO = 1;

    /**
     * Indicates that the streaming will consist of audio and video.
     */
    //static final int STREAM_TYPE_AUDIO_VIDEO = 3;

    /**
     * Indicates that the streaming will consist only of video.
     */
    static final int STREAM_TYPE_VIDEO = 2;

    /**
     * Returns a Player initiated to render the receiving part of this Media.
     * The returned Player is in the REALIZED state.
     *
     * @return
     * @throws IOException - if the exists property is false for the media
     */
    Player getReceivingPlayer() throws IOException;


    /**
     * Returns a Player initiated to source the sending part of this Media.
     * The returned Player is in the REALIZED state.
     *
     * @return
     * @throws IOException - if the exists property is false for the media
     */
    Player getSendingPlayer() throws IOException;

    /**
     * Returns the stream type for the Media.
     *
     * @return
     */
    int getStreamType();

    /**
     * Sets a preferred quality of the media stream.
     *
     * @param quality
     */
    void setPreferredQuality(int quality);

    /**
     * Sets the source to capture media from.
     * If this method is not called, then the audio is sourced
     * from a default microphone of the device,
     * and video is sourced from a default camera.
     * <p/>
     * Supported sources:
     * <p/>
     * capture://<device>
     * file://<path>
     *
     * @param source
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
     *                               IllegalArgumentException - if the syntax of the source argument is invalid or not supported
     */
    void setSource(String source);

    /**
     * Sets the stream type for the Media.
     *
     * @param type
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
     *                               IllegalArgumentException - if the type argument is not a valid identifier or not supported
     */
    void setStreamType(int type);

}
