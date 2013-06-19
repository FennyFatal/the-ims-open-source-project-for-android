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

package javax.microedition.ims.core.auth;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.uicc.UiccController;
import com.orange.authentication.simcard.SimCardAuthenticationService;
import com.orange.authentication.simcard.ServiceAkaAuthenticationResult;
import android.util.Log;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.Logger.Tag;
import javax.microedition.ims.common.util.StringUtils;
import javax.microedition.ims.config.UserInfo;
import com.android.internal.telephony.CommandsInterface;
import android.telephony.TelephonyManager;
import android.os.ServiceManager;
import android.content.Context;
/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 30.6.2010
 * Time: 15.45.37
 */


public class AKAAuthProviderMockImpl implements AKAAuthProvider {
    private static final String LOG_TAG = "AKAAuthProviderAndroidImpl";
    private Context theContext;
    private ITelephony telephonyHdl = null;
    public AKAAuthProviderMockImpl(Context aContext)
	{
	    this.theContext = aContext;
	}

    @Override
    public AKAResponse calculateAkaResponse(byte[] rand, byte[] autn) {
	try
	{
	Log.d("AKAAuthProviderAndroidImpl", "Rand:"+rand);
	Log.d("AKAAuthProviderAndroidImpl", "autn:"+autn);
/*
	mPhoneFactory = Class.forName("com.android.internal.telephony.PhoneFactory");
	Method mMakeDefaultPhone = mPhoneFactory.getMethod("makeDefaultPhone", new Class[] {Context.class});
	mMakeDefaultPhone.invoke(null, theContext);
	Method mGetDefaultPhone = mPhoneFactory.getMethod("getDefaultPhone", null);
	mPhone = mGetDefaultPhone.invoke(null);
*/
	ServiceAkaAuthenticationResult result = new SimCardAuthenticationService(UiccController.getInstance().getCommandsInterface(),theContext).akaAuthentication(rand,autn);
	Log.d("AKAAuthProviderAndroidImpl", "Error:"+ result.getError().ordinal() + ":" + result.getError());
	Log.d("AKAAuthProviderAndroidImpl", "Ck:" + result.getCk());
	Log.d("AKAAuthProviderAndroidImpl", "Ik:" + result.getIk());
	Log.d("AKAAuthProviderAndroidImpl", "Res:" + result.getRes());
	Log.d("AKAAuthProviderAndroidImpl", "Auts:" + result.getAuts());
        return new AKAResponseImpl(
	//result.getCk(),
	result.getIk(),
	result.getRes(),
	result.getAuts(),
	result.getCk()
	);
	/*return new AKAResponseImpl(
                new byte[]{},
                new byte[]{},
                new byte[]{},
                StringUtils.hexStringToByteArray(DigestUtils.md5Hex("" + System.currentTimeMillis()).substring(0, 8))
        );*/
	}
	catch (Exception ex)
	{
	Log.d("AKAAuthProviderAndroidImpl", "error1:" + ex);
	Logger.log("AKAAuthProviderAndroidImpl", "error" + ex);
	return null;
	}
    }
/*
  private ITelephony getTelephonyHdl()
    throws Exception
  {
    if (this.telephonyHdl == null)
      this.telephonyHdl = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
    return this.telephonyHdl;
  }
*/
/*
    private AKAResponse doCalculateAkaResponse(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws AuthCalculationException
  {
    TelephonyManager manager = (TelephonyManager) theContext.getSystemService(Context.TELEPHONY_SERVICE);
    byte[] arrayOfByte1 = null;
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    try
    {
      int i = getTelephonyHdl().openIccLogicalChannel(manager.getIsimAid());
      if (i == -1)
      {
          return null;
      }
      byte[] arrayOfByte5 = hexStringToBytes(manager.calculateAkaResponse(paramArrayOfByte1, paramArrayOfByte2));
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
        throw new AuthCalculationException("Aka response can't be calculated");
        int n = arrayOfByte5[0];
        arrayOfByte2 = null;
        arrayOfByte3 = null;
        arrayOfByte4 = null;
        arrayOfByte1 = null;
        if (n != -36)
          continue;
        int i1 = arrayOfByte5[1];
        arrayOfByte2 = null;
        arrayOfByte3 = null;
        arrayOfByte4 = null;
        arrayOfByte1 = null;
        if (i1 <= 0)
          continue;
        arrayOfByte4 = new byte[i1];
        System.arraycopy(arrayOfByte5, 2, arrayOfByte4, 0, i1);
        arrayOfByte2 = null;
        arrayOfByte3 = null;
        arrayOfByte1 = null;
      }
    }
    catch (Exception localException)
    {
      while (true)
      {
        localException.printStackTrace();
      }
      if (arrayOfByte1 == null)
        return null;
    }
    return new AKAResponseImpl(arrayOfByte2, arrayOfByte3, arrayOfByte4, arrayOfByte1);
  }
*/    
    public UserInfo getImpi() {
TelephonyManager mTelephonyMgr = (TelephonyManager) theContext.getSystemService(Context.TELEPHONY_SERVICE); 
	String imsi = mTelephonyMgr.getSubscriberId();
        return new UserInfo("", imsi , "msg.pc.t-mobile.com");
        
    }

	    
    public UserInfo getImpu() {
        TelephonyManager mTelephonyMgr = (TelephonyManager) theContext.getSystemService(Context.TELEPHONY_SERVICE);
        String number = mTelephonyMgr.getLine1Number();
        return new UserInfo("sip", number , "msg.pc.t-mobile.com");
        //return null;
    }

    public String getHomeNetworkDomain() {
        return "msg.pc.t-mobile.com";
    }
    
    @Override
    public boolean isGbaUSupported() {
        return true;
    }
}
