package javax.microedition.ims.android.core;

interface IMessageBodyPart {
     String getHeader(String key);
     void setHeader(String key, String value);

     // openContent{Input,Output}Stream functions
     // are implemented at client library side
     void setContent(in byte[] content);
     byte[] getContent();
}
