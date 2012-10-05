package javax.microedition.ims.android;

import javax.microedition.ims.android.IRegistry;
import javax.microedition.ims.android.IGsmLocationInfo;

interface IConfiguration
{
        IRegistry getRegistry(String appId);
        boolean hasRegistry(String appId);
        boolean removeRegistry(String appId);
        void setRegistry(in IRegistry registry);
        void updateLocation(in IGsmLocationInfo locationInfo);
        void removeLocation();
}
