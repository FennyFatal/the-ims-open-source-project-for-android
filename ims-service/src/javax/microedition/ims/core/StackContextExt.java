package javax.microedition.ims.core;

import javax.microedition.ims.common.Protocol;

public interface StackContextExt extends StackContext{
    void updateProtocol(Protocol protocol); 
}
