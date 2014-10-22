package com.spec.extender.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.spec.extender.CONST;
import com.spec.extender.exception.HeaderUpdaterException;

public class CryptUtil {
	public static String doSha256(String text)  {
	    try {
	        MessageDigest md	= MessageDigest.getInstance("SHA-256");
	        md.update(text.getBytes("UTF-8"));
	        byte[] digest		= md.digest();            
	        String hashStr		= BinaryUtil.toHex(digest);
	        
	        return hashStr;
	    } catch (Exception e) {
	        throw new HeaderUpdaterException("Unable to compute hash while signing request: " + e.getMessage());
	    }
	}

	/**
	 * This method is cloned from http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] doHmacSHA256(String data, byte[] key) throws Exception  {
		_debug.println("data to doHmacSHA256: " + data);
	     String algorithm="HmacSHA256";
	     Mac mac = Mac.getInstance(algorithm);
	     mac.init(new SecretKeySpec(key, algorithm));
	     
	     return mac.doFinal(data.getBytes("UTF8"));
	}
	
	public static String generateSHA1HmacWithTimestamp(String payload, String timeStamp, String secretKey) 
			throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException
	{	
	    String payloadForHash	= payload + timeStamp + "\n";
	    String hmacDigest		= CryptUtil.generateSHA1Hmac(payloadForHash, secretKey);
	
	    _debug.println("\n----------------------------------");
	    _debug.println("\nValues to be used in SAL Request Headers\n"
	    				+ "\npayload: [\n"+ payload + "\n]"
	    				+ "\ntimeStamp: [\n" + timeStamp + "\n]"
	    				);
	    
	    return hmacDigest;    	
	}

	public static String generateSHA1Hmac(String payload, String hmacKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException{
    	 byte[] data_bytes	= payload.getBytes("UTF8");
         byte[] key_bytes	= hmacKey.getBytes("UTF8");
         SecretKeySpec key_spec = new SecretKeySpec(key_bytes, com.spec.extender.CONST.HMAC_SHA1_ALGORITHM);
         Mac mac = Mac.getInstance(com.spec.extender.CONST.HMAC_SHA1_ALGORITHM);
         mac.init(key_spec);
         byte[] raw_hash = mac.doFinal(data_bytes);

         Base64 base64 = new Base64();
         String hmacDigest = new String(base64.encode(raw_hash));
         
//         _debug.println("\n----------------------------------");
//         _debug.println("\nValues to be used in SAL Request Headers\n");
//         _debug.println("\nClearText: " + payload);
//         _debug.println("\nHMAC: " + hmacDigest);
//         _debug.println("\n----------------------------------");
      
         return hmacDigest;
    }

}
