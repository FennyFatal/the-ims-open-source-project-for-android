package javax.microedition.ims.android;

import javax.microedition.ims.android.IConnectionStateListener;

interface IConnectionState
{
        void addConnectionStateListener(IConnectionStateListener listener);
        void removeConnectionStateListener(IConnectionStateListener listener);
        boolean isConnected();
        boolean isSuspended();
        String[] getUserIdentities();
        boolean	isBehindNat();  
        String[] getRegisterURIs();
}
