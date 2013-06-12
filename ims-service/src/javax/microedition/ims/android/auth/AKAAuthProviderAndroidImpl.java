package javax.microedition.ims.android.auth;
/*
This is the actual code in the smali files. 
However, certain interfaces are only available during OS compile time.
So, we are using the smali version of the code (compiled once) to greatly reduce build time.
*/
import android.content.Context;
import android.os.RemoteException;
//import android.os.ServiceManager;
//import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
//import com.android.internal.telephony.ITelephony.Stub;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Logger.Tag;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.core.auth.AKAAuthProvider;
import javax.microedition.ims.core.auth.AKAResponse;
import javax.microedition.ims.core.auth.AKAResponseImpl;
import javax.microedition.ims.core.auth.AuthCalculationException;

public class AKAAuthProviderAndroidImpl
  implements AKAAuthProvider
{
  private static final String LOG_TAG = "AKAAuthProviderAndroidImpl";
//  private static final Map<AkaResponseKey, AKAResponse> akaResponseCache = new HashMap();
//  private final TelephonyManager manager;
 // private ITelephony telephonyHdl = null;

  public AKAAuthProviderAndroidImpl(Context paramContext)
  {
/*
    this.manager = ((TelephonyManager)paramContext.getSystemService("phone"));
    if (this.manager == null)
      throw new IllegalArgumentException("Invalid Android context. Can not obtain telephony manager. Context = " + paramContext);
*/
  }

  private AKAResponse doCalculateAkaResponse(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws AuthCalculationException
  {
/*
    Log.d("AKAAuthProviderAndroidImpl", "IsimAid() " + this.manager.getIsimAid());
    byte[] arrayOfByte1 = null;
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    try
    {
      int i = getTelephonyHdl().openIccLogicalChannel(this.manager.getIsimAid());
      if (i == -1)
      {
        Log.e("AKAAuthProviderAndroidImpl", "unable to open logical channel");
        return null;
      }
      Log.d("AKAAuthProviderAndroidImpl", "iChannelId() " + i);
      byte[] arrayOfByte5 = hexStringToBytes(this.manager.calculateAkaResponse(paramArrayOfByte1, paramArrayOfByte2));
      Log.d("AKAAuthProviderAndroidImpl", "calculateAkaResponse() " + arrayOfByte5);
      getTelephonyHdl().closeIccLogicalChannel(i);
      if ((arrayOfByte5[0] == -37) || (arrayOfByte5[0] == 0))
      {
        int j = arrayOfByte5[1];
        arrayOfByte2 = null;
        arrayOfByte3 = null;
        arrayOfByte4 = null;
        arrayOfByte1 = null;
        if (j > 0)
        {
          arrayOfByte1 = new byte[j];
          System.arraycopy(arrayOfByte5, 2, arrayOfByte1, 0, j);
        }
        int k = arrayOfByte5[(j + 2)];
        arrayOfByte2 = null;
        arrayOfByte3 = null;
        arrayOfByte4 = null;
        if (k > 0)
        {
          arrayOfByte2 = new byte[k];
          System.arraycopy(arrayOfByte5, j + 3, arrayOfByte2, 0, k);
        }
        int m = arrayOfByte5[(k + (j + 3))];
        arrayOfByte3 = null;
        arrayOfByte4 = null;
        if (m > 0)
        {
          arrayOfByte3 = new byte[m];
          System.arraycopy(arrayOfByte5, k + (j + 4), arrayOfByte3, 0, m);
        }
      }
      while (arrayOfByte1 == null)
      {
        Logger.log("AKAAuthProviderAndroidImpl", "telephonyManager = " + this.manager);
        throw new AuthCalculationException("Aka response can't be calculated");
        int n = arrayOfByte5[0];
        arrayOfByte2 = null;
        arrayOfByte3 = null;
        arrayOfByte4 = null;
        arrayOfByte1 = null;
        if (n == -36)
        {
          int i1 = arrayOfByte5[1];
          arrayOfByte2 = null;
          arrayOfByte3 = null;
          arrayOfByte4 = null;
          arrayOfByte1 = null;
          if (i1 > 0)
          {
            arrayOfByte4 = new byte[i1];
            System.arraycopy(arrayOfByte5, 2, arrayOfByte4, 0, i1);
            arrayOfByte2 = null;
            arrayOfByte3 = null;
            arrayOfByte1 = null;
          }
        }
      }
    }
    catch (RemoteException localRemoteException)
    {
      while (true)
      {
        localRemoteException.printStackTrace();
        Log.d("AKAAuthProviderAndroidImpl", "error1:" + localRemoteException);
      }
    }
    catch (Exception localException)
    {
      while (true)
      {
        localException.printStackTrace();
        Log.d("AKAAuthProviderAndroidImpl", "error2:" + localException);
      }
      if (arrayOfByte1 == null)
        return null;
    }
    return new AKAResponseImpl(arrayOfByte2, arrayOfByte3, arrayOfByte4, arrayOfByte1);
*/
return null;
  }

  private ITelephony getTelephonyHdl()
    throws Exception
  {
/*
    if (this.telephonyHdl == null)
      this.telephonyHdl = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
    return this.telephonyHdl;
*/
return null;
  }

  private int hexCharToInt(char paramChar)
  {
   /* if ((paramChar >= '0') && (paramChar <= '9'))
      return paramChar - '0';
    if ((paramChar >= 'A') && (paramChar <= 'F'))
      return 10 + (paramChar - 'A');
    if ((paramChar >= 'a') && (paramChar <= 'f'))
      return 10 + (paramChar - 'a');
    throw new RuntimeException("invalid hex char '" + paramChar + "'");
*/
return 0;
  }

  private byte[] hexStringToBytes(String paramString)
  {
/*
    byte[] arrayOfByte;
    if (paramString == null)
      arrayOfByte = null;
    while (true)
    {
      return arrayOfByte;
      int i = paramString.length();
      arrayOfByte = new byte[i / 2];
      for (int j = 0; j < i; j += 2)
        arrayOfByte[(j / 2)] = ((byte)(hexCharToInt(paramString.charAt(j)) << 4 | hexCharToInt(paramString.charAt(j + 1))));
    }
*/
return null;
  }

  public AKAResponse calculateAkaResponse(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws AuthCalculationException
  {
/*
    AkaResponseKey localAkaResponseKey = new AkaResponseKey(paramArrayOfByte1, paramArrayOfByte2);
    AKAResponse localAKAResponse;
    if (akaResponseCache.containsKey(localAkaResponseKey))
    {
      Logger.log("AKAAuthProviderAndroidImpl", "calculateAkaResponse#aka response has been calculated, retrieved from cache");
      localAKAResponse = (AKAResponse)akaResponseCache.get(localAkaResponseKey);
    }
    while (true)
    {
      Logger.log(Logger.Tag.COMMON, "androidAKAResponse = " + localAKAResponse);
      return localAKAResponse;
      Logger.log("AKAAuthProviderAndroidImpl", "calculateAkaResponse#aka response hasn't been calculated yet, retrieved from telephony manager");
      localAKAResponse = doCalculateAkaResponse(paramArrayOfByte1, paramArrayOfByte2);
      akaResponseCache.put(localAkaResponseKey, localAKAResponse);
    }
*/
return null;
  }

  public String getHomeNetworkDomain()
  {
/*
    return this.manager.getIsimDomain();
*/ return null;
  }

  public UserInfo getImpi()
  {
/*
    Logger.log(Logger.Tag.COMMON, "Device impi: " + this.manager.getIsimImpi());
    return UserInfo.valueOf(this.manager.getIsimImpi());
*/ return null;
  }

  public UserInfo getImpu()
  {
/*
    String[] arrayOfString = this.manager.getIsimImpu();
    Logger.log(Logger.Tag.COMMON, "getImpu#Device impu: " + Arrays.toString(arrayOfString));
    if ((arrayOfString != null) && (arrayOfString.length > 0));
    for (UserInfo localUserInfo = UserInfo.valueOf(arrayOfString[0]); ; localUserInfo = null)
    {
      Logger.log(Logger.Tag.COMMON, "getImpu#retValue: " + localUserInfo);
      return localUserInfo;
    }
*/
return null;
  }

  public boolean isGbaUSupported()
  {
/*
    Logger.log(Logger.Tag.COMMON, "isGbaUSupported: manager.hasIccCard():" + this.manager.hasIccCard());
    Logger.log(Logger.Tag.COMMON, "isGbaUSupported: manager.hasIsim():" + this.manager.hasIsim());
    Logger.log(Logger.Tag.COMMON, "isGbaUSupported: manager.isGbaSupported():" + this.manager.isGbaSupported());
    return (this.manager.hasIsim()) && (this.manager.isGbaSupported());
*/
return false;
  }
/*
  private final class AkaResponseKey
  {
    private final byte[] autn;
    private final byte[] rand;

    public AkaResponseKey(byte[] paramArrayOfByte1, byte[] arg3)
    {
      this.rand = paramArrayOfByte1;
      Object localObject;
      this.autn = localObject;
    }

    private AKAAuthProviderAndroidImpl getOuterType()
    {
      return AKAAuthProviderAndroidImpl.this;
    }

    public boolean equals(Object paramObject)
    {
      if (this == paramObject);
      AkaResponseKey localAkaResponseKey;
      do
      {
        return true;
        if (paramObject == null)
          return false;
        if (getClass() != paramObject.getClass())
          return false;
        localAkaResponseKey = (AkaResponseKey)paramObject;
        if (!getOuterType().equals(localAkaResponseKey.getOuterType()))
          return false;
        if (!Arrays.equals(this.autn, localAkaResponseKey.autn))
          return false;
      }
      while (Arrays.equals(this.rand, localAkaResponseKey.rand));
      return false;
    }

    public int hashCode()
    {
      return 31 * (31 * (31 + getOuterType().hashCode()) + Arrays.hashCode(this.autn)) + Arrays.hashCode(this.rand);
    }

    public String toString()
    {
      return "AkaResponseKey [rand=" + Arrays.toString(this.rand) + ", autn=" + Arrays.toString(this.autn) + "]";
    }
  }
*/
}
