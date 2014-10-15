package com.shc.ecom.cisp.core.security;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class DigitalSignature {
    public static void main(String[] args) throws Exception{
        //1. sid/key for UP:
    	//String sid = "1001"; 						// site identifier created to uniquely identify site
        //String authKey = "fdprL5qe2/St1AkzF7jUw3DC00A="; // secret key, retrieved from a key store
        
    	//2. sid/key for SAL
    	//String sid = "1101"; 						
        //String authKey = "NtllUiS649iDq7QsmSNzD4Np29M"; 
        
    	//3. sid/key for WCSCO
    	//String sid = "1102"; 						
        //String authKey = "NyMlEeLaYkQeIIQVc_lr8DVGAck"; 
	
		// 4. sid/key for Security
    	String sid= "2";
    	String authKey = "NyMlEeLaYkQeIIQVc_lr8DVGAck";
    	
        //2. path:
        String path="/v1/users/payments/create"; //depends on what API is called
        //String path="/v1/users/payments/get";
        //String path="/v1/users/payments/update";
        //String path ="/v1/users/payments/delete";
        //String path = "/v1/users/payments/setPreferred";
        //3. sig
        DigitalSignature ds = new DigitalSignature();
        String ts=ds.getCurrentTimeStamp();
   //     System.out.println("ts=" + ts);
        String sig= ds.generateSig(sid, authKey, ts, path);
   //     System.out.println("sig=" + sig);
        
        String url = "https://ssapp301p.dev.ch3.s.com:9643/cisp";
        //String url = "https://cispvip.qa.ch3.s.com/cisp";
        //String url = "https://ciscp.prod.global.s.com/cisp";
        System.out.println(url + path + "?sid=" + sid + "&ts=" + ts + "&sig=" + sig);
   }
    
    protected String generateSig(String sid, String authKey, String ts, String path) throws Exception {
    	String sig = null;
    	try {
    		String data = "sid=" + sid + "ts=" + ts+ "path="+path;
			SecretKeySpec signKey = new SecretKeySpec(authKey.getBytes(), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signKey);
			byte[] rawData = mac.doFinal(data.getBytes());
			
			String base64Data = Base64.encodeBase64String(rawData);
			sig = URLEncoder.encode(base64Data, "UTF-8");			
    	} catch (Exception ex) {
    		System.err.println(ex);
    	}
	   	return sig;
    } 
    
   protected String getCurrentTimeStamp() throws Exception{
    	Date now = Calendar.getInstance().getTime();
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    return formatter.format(now); 
	}
}