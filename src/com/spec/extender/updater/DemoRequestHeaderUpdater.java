package com.spec.extender.updater;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import com.spec.extender.util.CryptUtil;


public class DemoRequestHeaderUpdater extends BaseUpdater {
	private String serviceBaseURL;
	private String clientID;
	private String hmacKey;
	private String signaturePlaceholder;
	
	public DemoRequestHeaderUpdater(String _serviceBaseURL, String _clientID, String _hmacKey, String _signaturePlaceholder){
		this.serviceBaseURL			= _serviceBaseURL;
		this.clientID				= _clientID;
		this.hmacKey				= _hmacKey;
		this.signaturePlaceholder	= _signaturePlaceholder;
	}
	
	public String getHmacKey() {
		return this.hmacKey;
	}	
	
	public String getSignaturePlaceholder() {
		return signaturePlaceholder;
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
	protected String updateBody(List<String> headers, String requestBody){
		return requestBody;
	}
	
	protected List<String> updateHeaders(List<String> headers, String requestBody) {
		//---update timestamp header---
    	String timeStamp		= generateDemoRequestHeaderTimestamp();
		String dateHeader		= generateDemoDateHeader(timeStamp);
		List<String> udpatedHeaders	
								= updateAnHeader(headers, "Date:", dateHeader);
		
		// update hmac header
    	String clearText		= generateSALHMACPayload(headers.get(0));					    		    	
    	String hmacDigest		= null;
    	try {
			hmacDigest			= CryptUtil.generateSHA1HmacWithTimestamp(clearText, timeStamp, getHmacKey());
		} catch (Exception e) {
			e.printStackTrace();
		} 
    	
    	System.out.println("SOSHeaderUpdater updating headers...");
    	udpatedHeaders 			= updateHMACHeader(udpatedHeaders, hmacDigest, this.signaturePlaceholder);
		
//    	_debug.println("updated headers: ");
//    	_debug.print(Util.listToString(udpatedHeaders));
		return udpatedHeaders;
	}
	
	public String generateSALHMACPayload(String methodUrlProtocolHeader){
		String[] methodUrlProtocol = new String[3];
		
		StringTokenizer tokenizer	= new StringTokenizer(methodUrlProtocolHeader);
		int i = 0;
		while (tokenizer.hasMoreTokens()){
			methodUrlProtocol[i++] = tokenizer.nextToken();
		}
				
		return methodUrlProtocol[0] + "\n" + methodUrlProtocol[1] + "\n";
	}
	
	
    /**     
     * Sample Date header: Date: Sat, 9 Mar 2013 21:01:27 CST
     * @param timeStamp
     * @param headers
     * @return
     */
    public String generateDemoDateHeader(String timeStamp){
    	StringBuffer sb = new StringBuffer("Date: " + timeStamp); 
    	
    	return sb.toString();
    }
	
    public static String generateDemoRequestHeaderTimestamp(){
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		String timeStamp = dateFormat.format(calendar.getTime());
//		timeStamp = "Tue, 26 Jun 2012 16:01:13 GMT";
		
		return timeStamp;
    }
    

}
