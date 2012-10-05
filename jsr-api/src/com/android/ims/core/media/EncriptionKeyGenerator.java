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
package com.android.ims.core.media;

import android.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class EncriptionKeyGenerator {

	private EncriptionKeyGenerator() {
		assert false;
	}
	
	public static String generateEncriptionKey(String authAlgorithm) throws NoSuchAlgorithmException {
		final String encriptionKey;
		
		if("AES_CM_128_HMAC_SHA1_32".equalsIgnoreCase(authAlgorithm)){
			encriptionKey = generateEncriptionKey("AES", "SHA1PRNG");
		} else {
			throw new NoSuchAlgorithmException("Can't generate key fot algorithm \'" + authAlgorithm + "\'");
		}
		//
		return encriptionKey;
	}
	
	private static String generateEncriptionKey(String cipherAlgorithm, String prngAlgorithm) throws NoSuchAlgorithmException {
		final String encriptionKey;
        
        byte[] masterKey = generateMasterKey(cipherAlgorithm, prngAlgorithm);
        byte[] saltKey = generateSaltKey(prngAlgorithm);
        
        byte[] keyBuff = new byte[masterKey.length + saltKey.length];
        System.arraycopy(masterKey, 0, keyBuff, 0, masterKey.length);
        System.arraycopy(saltKey, 0, keyBuff, masterKey.length, saltKey.length);
        
        encriptionKey = Base64.encodeToString(keyBuff, Base64.NO_WRAP/*DEFAULT*/);
        
		return encriptionKey;
	}
	
	private static byte[] generateSaltKey(String prngAlgorithm) 
			throws NoSuchAlgorithmException{
		
		final byte[] slatKey = new byte[14];
		
		 //Returns a new instance of SecureRandom that utilizes the 
        //SHA1 algorithm.
        final SecureRandom secureRandom = SecureRandom.getInstance(prngAlgorithm);
        secureRandom.generateSeed(slatKey.length);
        
        secureRandom.nextBytes(slatKey);
        
        return slatKey;
	}
	
	private static byte[] generateMasterKey(String cipherAlgorithm, String prngAlgorithm) 
			throws NoSuchAlgorithmException{
		
		final byte[] masterKey;
		
		//API for generating symmetric cryptographic keys
		final KeyGenerator keygen = KeyGenerator.getInstance(cipherAlgorithm);

		 //Returns a new instance of SecureRandom that utilizes the 
        //SHA1 algorithm.
        final SecureRandom secrand = SecureRandom.getInstance(prngAlgorithm);
        
        //Reseeds this SecureRandom instance with the specified seed.
        secrand.generateSeed(16);
        
        //Initializes this KeyGenerator instance for the specified 
        //key size (in bits) using the specified randomness source.
        keygen.init(128, secrand);        
        
        //Generates a secret key.
        final SecretKey seckey = keygen.generateKey();
        //Returns the encoded form of the key.
        masterKey = seckey.getEncoded();
		
		return masterKey;
	}
}
