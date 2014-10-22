package com.spec.extender.hmac.updater;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.spec.extender.util._debug;

public class DemoURLUpdater extends BaseUpdater {
	private String serviceBaseURL;
	private String clientID;
	private String hmacKey;
	private String signaturePlaceholder;
		
	public DemoURLUpdater(String _serviceBaseURL, String _clientID, String _hmacKey, String _signaturePlaceholder){
		this.serviceBaseURL	= _serviceBaseURL;
		this.clientID		= _clientID;
		this.hmacKey		= _hmacKey;
		this.signaturePlaceholder
							= _signaturePlaceholder;
	}
	
	public static void main(String args[]){
	
	}

	@Override
	public String getHmacKey() {
		return this.hmacKey;
	}

	@Override
	public String getSignaturePlaceholder() {
		return this.signaturePlaceholder;
	}	
	
	@Override
	public String getServiceBaseURL() {
		return this.serviceBaseURL;
	}

	@Override
	public String getClientID() {
		return this.clientID;
	}
		
	/**
	 * request body is not changed
	 */
	@Override
	protected String updateBody(List<String> headers, String requestBody){
		return requestBody;
	}
	
	/**
	 * @headers	Incoming request header looks like the following:
				POST /cisp/v1/update?sid=1001&ts=2014-07-30T20:42:07Z&sig=oPt7n9Jv0PWU5ucg54tswmJPRl4%3D HTTP/1.1,
				Host: google.com,
				Accept-Encoding: identity,
	 * This method replace the query string 
	 */
	@Override
	protected List<String> updateHeaders(List<String> headers, String requestBody) {
		String requestMethodURLProtocol 
							= headers.get(0);
		String updatedMethodURLProtocol
							= this.updateMethodURLProtocol(requestMethodURLProtocol);
		
		return updateAnHeader(headers, requestMethodURLProtocol, updatedMethodURLProtocol);
	}
	
	private String updateMethodURLProtocol(String originalMethodURLProtocol){
		_debug.println("originalMethodURLProtocol: " + originalMethodURLProtocol);
		String[] mup	 	= getRequestMethodUrlProtocol(originalMethodURLProtocol);
		
		_debug.println(mup[2]);
		
		String path			= mup[1];
		int queryIndex		= path.indexOf('?');
		if (queryIndex > 0)
			path	= path.substring(0, queryIndex);

		String updatedRequestMethodURLProtocol	= null;
		try {
			String newQuery		= updateQuerySig(this.clientID, this.hmacKey, path);			
			updatedRequestMethodURLProtocol
								= new StringBuffer(mup[0]).append(" " + newQuery).append(" " + mup[2]).toString();
			_debug.println();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return updatedRequestMethodURLProtocol;
	}
	
	   public String updateQuerySig(String sid, String authKey, String path) throws Exception{
	    	String ts	= getCurrentTimeStamp();	
//	    				= "2014-10-09T13:07:10Z";
			
	    	String pathTosign	= path;
	    	int indexOfSignedPath	= path.indexOf("/v1/");
	    	if (indexOfSignedPath > 0)
	    		pathTosign = pathTosign.substring(indexOfSignedPath); //remove "/csip" from the path

			_debug.println("path to sign: " + pathTosign);
	    	String sig	= this.generateSig(sid, authKey, ts, pathTosign);
	    	StringBuffer queryBuffer 
	    				=  new StringBuffer(path + "?sid=" + sid + "&ts=" + ts + "&sig=" + sig);
	    	
	    	return queryBuffer.toString();
	    }
	        
	    
	    /**
	     * 
	     * @param sid		Client ID
	     * @param authKey	HMAC key
	     * @param ts		Timestamp
	     * @param path		Relative Path
	     * @return
	     * @throws Exception
	     */
	    private String generateSig(String sid, String authKey, String ts, String path) throws Exception {
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
