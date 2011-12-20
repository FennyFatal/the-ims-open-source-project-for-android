package javax.microedition.ims.media;

public interface IVideoCallback {
   public void updateVideoStats(int frameRateI, int bitRateI, int frameRateO, int bitRateO, int packetLoss);
}
