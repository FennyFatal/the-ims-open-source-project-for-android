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
