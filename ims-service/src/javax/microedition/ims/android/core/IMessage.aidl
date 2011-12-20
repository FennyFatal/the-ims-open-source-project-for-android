package javax.microedition.ims.android.core;

import javax.microedition.ims.android.core.IMessageBodyPart;

interface IMessage {
        void addHeader(String key, String value);
        IMessageBodyPart createBodyPart();
        List<IBinder> getBodyParts();
        String[] getHeaders(String key);
        String getMethod();
        String getReasonPhrase();
        int getState();
        int getStatusCode();
}
