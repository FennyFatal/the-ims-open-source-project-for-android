package javax.microedition.ims.android;

import android.os.RemoteException;

public class StackErrorBinder extends IStackError.Stub{
    private final IError iError;

    StackErrorBinder(IError iError) {
        assert iError != null;
        this.iError = iError;
    }


    public IError getError() throws RemoteException {
        return iError;
    }


    @Override
    public String toString() {
        return "StackErrorBinder [iError=" + iError + "]";
    }
}
