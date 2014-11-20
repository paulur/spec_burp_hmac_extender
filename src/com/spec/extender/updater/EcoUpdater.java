package com.spec.extender.updater;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.spec.extender.util.CryptUtil;
import com.spec.extender.util._debug;

public class EcoUpdater extends BaseUpdater {
	private String serviceBaseURL;
	private String clientID;
	private String hmacKey;
	private String signaturePlaceholder;
		
	public EcoUpdater(){};
	
	public EcoUpdater(String _serviceBaseURL, String _clientID, String _hmacKey, String _signaturePlaceholder){
		this.serviceBaseURL	= _serviceBaseURL;
		this.clientID		= _clientID;
		this.hmacKey		= _hmacKey;
		this.signaturePlaceholder
							= _signaturePlaceholder;
	}
	
	public static void main(String args[]){
		EcoUpdater eu			= new EcoUpdater();
		String requestHeader	= "POST /cisp/v1/update";//?sid=1001&ts=2014-07-30T20:42:07Z&sig=oPt7n9Jv0PWU5ucg54tswmJPRl4%3D HTTP/1.1";
		String[] mbq			= eu.getRequestMethodBaseUrlQueryString(requestHeader);
//		_debug.println(mbq[0] + "\n--" + mbq[1]  + "\n--" + mbq[2]);
		_debug.println(eu.generateEcoTimestamp());		
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
	protected List<String> updateHeaders(List<String> headers, String requestBody) {
		//---update timestamp header---
    	String timeStamp		= generateEcoTimestamp();
		String dateHeader		= generateEcoDateHeader(timeStamp);
		List<String> udpatedHeaders	
								= updateAnHeader(headers, "Date:", dateHeader);
		
		// update hmac header
    	String payload			= generateSALHMACPayload(headers.get(0), timeStamp);					    		    	
    	String hmacDigest		= null;
    	try {
			hmacDigest			= CryptUtil.generateSHA1Hmac(payload, getHmacKey());
		} catch (Exception e) {
			e.printStackTrace();
		} 
    	
    	System.out.println("EcoUpdater updating headers...");
    	udpatedHeaders 			= updateHMACHeader(udpatedHeaders, hmacDigest, this.signaturePlaceholder);
		
//    	_debug.println("updated headers: ");
//    	_debug.print(Util.listToString(udpatedHeaders));
		return udpatedHeaders;
	}
	
	public String generateSALHMACPayload(String methodUrlProtocolHeader, String timeStamp){
		String[] mbq = getRequestMethodBaseUrlQueryString(methodUrlProtocolHeader);
		
		StringBuffer payload	= new StringBuffer();
		payload.append(mbq[0] + "\n");
		payload.append(mbq[1] + "\n");
		payload.append(timeStamp + "\n");				
		
		return payload.toString();
	}
	
    /**     
     * Sample Date header: Date: Sat, 9 Mar 2013 21:01:27 CST
     * @param timeStamp
     * @param headers
     * @return
     */
    public String generateEcoDateHeader(String timeStamp){
    	StringBuffer sb = new StringBuffer("Date: " + timeStamp); 
    	
    	return sb.toString();
    }
	
    /**
     * 
     * @return in the format of "2014-11-13T16:17:47.741Z"
     */
    public String generateEcoTimestamp(){
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		String timeStamp = dateFormat.format(calendar.getTime());
		
		return timeStamp;
    }    
}
