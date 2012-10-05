package javax.microedition.ims.media;

public interface PlayerExt extends Player {
	void updateAuthKey(String authKey);
	String getAuthKey();
    String getUri();
    int getCodec();
    int getRtp();
}
