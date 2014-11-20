package com.spec.extender.updater;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
		EcoUpdater eu			= (EcoUpdater)new UpdaterFactory().create("https://fourdos304p.dev.ch3.s.com:2043/efpipe/eventsqueue/1/efevents/v1");
		String requestHeader	= "GET /efpipe/eventsqueue/0/efevents/v1?batchId=234w-23s-234s22&batchSize=0";
									//"POST /efpipe/eventsqueue/0/resetBatch";//?sid=1001&ts=2014-07-30T20:42:07Z&sig=oPt7n9Jv0PWU5ucg54tswmJPRl4%3D HTTP/1.1";
		String host				= "HOST: fourdos304p.dev.ch3.s.com:2080";
		String timeStamp		= "2014-11-20T18:16:57.688Z";
									//"2014-11-20T16:12:47.298Z";
		String requestBody		= "";//"<abod ?>";
		
		List<String> headers	= new ArrayList<String>();
		headers.add(requestHeader);
		headers.add(host);
//		String[] mbq			= eu.getRequestMethodBaseUrlQueryString(requestHeader);
//		_debug.println(mbq[0] + "\n--" + mbq[1]  + "\n--" + mbq[2]);
//		_debug.println(eu.generateEcoTimestamp());
		String hmacPayload		= eu.generateHMACPayload(headers, requestBody, timeStamp);
		String hmacDigest		= eu.generateHMACdigest(hmacPayload);
		_debug.println("payload:\n" + hmacPayload);
		_debug.println("eco hamc:\n" + hmacDigest);
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
    	String payload			= generateHMACPayload(headers, requestBody, timeStamp);	
    	_debug.println("hmac payload: " + payload);
    	
    	String hmacDigest		= this.generateHMACdigest(payload);
    	
    	System.out.println("EcoUpdater updating headers...");
    	udpatedHeaders 			= updateHMACHeader(udpatedHeaders, hmacDigest, this.signaturePlaceholder);
		
//    	_debug.println("updated headers: ");
//    	_debug.print(Util.listToString(udpatedHeaders));
		return udpatedHeaders;
	}
	
	private String generateHMACdigest(String payload){
		String hmacDigest		= null;
    	try {
			hmacDigest			= CryptUtil.generateSHA256Hamc(payload, getHmacKey());
		} catch (Exception e) {
			e.printStackTrace();
		} 
    	
    	return hmacDigest;
	}
	
	private String generateHMACPayload(List<String> headers, String requestBody, String timeStamp){
		String methodUrlProtocolHeader	= headers.get(0);
		String[] mbq		= getRequestMethodUrlProtocol(methodUrlProtocolHeader);
		String host			= retrieveCaseInsensitiveHeader(headers, "HOST");
		String transmission	= this.serviceBaseURL.substring(0, this.serviceBaseURL.indexOf(':'));
		//TODO only in debug
		//transmission		= "http";
		_debug.println("mbq array: |" + mbq[0] + "|" + mbq[1] + "|" + mbq[2] + "|");
		StringBuffer payload	= new StringBuffer();
		payload.append(mbq[0] + "\n");
		payload.append(transmission + "://" + host + mbq[1] + "\n");
		payload.append(timeStamp + "\n");				
		
		String method	= mbq[0];
		if (method.equals("POST")){
			if (requestBody.length() == 0) payload.append("{}\n");
			else payload.append(requestBody.trim() + "\n");
		}
		
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
