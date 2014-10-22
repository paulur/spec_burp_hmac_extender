package com.spec.extender.hmac.updater;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;

import com.spec.extender.exception.HeaderUpdaterException;
import com.spec.extender.exception.UnimplementedException;
import com.spec.extender.util.CryptUtil;
import com.spec.extender.util._debug;

public class AWSUpdater extends BaseUpdater {	
	final static String REGION		= "us-east-1";
	final static String SERVICE		= "AWSCognitoIdentityService";
	final static String X_TARGET	= "X-Amz-Target";
	static final String ALGORITHM	= "AWS4-HMAC-SHA256";
	static final String TERMINATOR	= "aws4_request";
	
	private String serviceBaseURL;
	private String clientID;
	private String hmacKey;
	private String signaturePlaceholder;
	
	public AWSUpdater(String _serviceBaseURL, String _clientID, String _hmacKey, String _signaturePlaceholder){
		this.serviceBaseURL	= _serviceBaseURL;
		this.hmacKey		= _hmacKey;
		this.clientID		= _clientID;
		this.signaturePlaceholder
							= _signaturePlaceholder;
	}

	public static void main(String args[]){
//		AWSHeaderUpdater ahu 			= new AWSHeaderUpdater();
//		ArrayList<String> awsHeaders	= ahu.createAWSHeader();
//		ahu.retrieveSignedHeaders(awsHeaders);
	}
	
	public String getClientID(){
		return this.clientID;
	}

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
	

	/**
	 * 	Sample request:
	 	POST / HTTP/1.1
		Host: cib.us-east-1.amazonaws.com
		Accept-Encoding: identity
		Content-Length: 217
		Content-Encoding: amz-1.0
		User-Agent: aws-cli/1.4.4 Python/2.7.8 Linux/3.2.6
		X-Amz-Date: 20140927T182907Z
		X-Amz-Target: AWSCognitoIdentityService.CreateIdentityPool
		Content-Type: application/x-amz-json-1.1
		Authorization: AWS4-HMAC-SHA256 Credential=abc/20140927/us-east-1/cognito-identity/aws4_request, SignedHeaders=content-encoding;content-type;host;user-agent;x-amz-date;x-amz-target, Signature=f5d9219160b2bd57fbe48e2972e2ee306dbbbca99322c1a948b1f425cf5da32e
		
		{"IdentityPoolName": "paul_\"", "DeveloperProviderName": "provider", "SupportedLoginProviders": {"name": "paul.com", "value": "1234"}, "AllowUnauthenticatedIdentities": true}
	
		http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
		http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-python
	 * @throws Exception 
	 */
	protected List<String> updateHeaders(List<String> headers, String requestBody){
		throw new UnimplementedException(this.getClass().getName() + ": method: updateHeaders");
	}
	

	protected String updateBody(List<String> headers, String requestBody){
		throw new UnimplementedException(this.getClass().getName() + ": method: requestBody");		
	}

	/**
	 * canonical_request = method + '\n' + canonical_uri + '\n' + canonical_querystring + '\n' + canonical_headers + '\n' + signed_headers + '\n' + payload_hash
	 * @throws UnsupportedEncodingException 
	 * 	
	 */
	public String buildCanonicalRequest(List<String> headers, String requestBody) throws UnsupportedEncodingException{
		String[] methodCanonicalUQ		= buildMethodCanonicalURIQuery(headers.get(0));
		String method					= methodCanonicalUQ[0].trim();
		String canonicalURI				= methodCanonicalUQ[1].trim();
		String canonicalQuery			= methodCanonicalUQ[2].trim();
		String signedHeaders			= retrieveSignedHeaders(getAuthHeader(headers));
		String canonicalHeaders			= buildCanonicalHeaders(signedHeaders, headers);
		String payloadHash				= CryptUtil.doSha256(requestBody.trim());
		
		StringBuffer canonicalRequest	= new StringBuffer();
		canonicalRequest.append(method 	+ "\n" 
										+ canonicalURI + "\n" 
										+ canonicalQuery + "\n" 
										+ canonicalHeaders + "\n" 
										+ signedHeaders + "\n" 
										+ payloadHash);
		
		return canonicalRequest.toString();
	}

	public String buildCanonicalHeaders(String signedHeaders, List<String> headers){
		ArrayList<String> signedHeaderList	= AWSUpdater.retrieveSignedHeaderList(signedHeaders);
		StringBuffer canonicalHeaders		= new StringBuffer();
		
		for (String signedHeader : signedHeaderList){
			canonicalHeaders.append(signedHeader + ":" + getAnInsensitiveHeader(headers, signedHeader) + "\n");
		}
		
		return canonicalHeaders.toString();
	}

	public String[] buildMethodCanonicalURIQuery(String requestMUP){
		String[] mup		= getRequestMethodUrlProtocol(requestMUP);
		String method		= mup[0].trim();
		String URI			= mup[1].trim();
		String canonicalURI	= URI;
		String canonicalQueryString
							= "";
		
		int queryIndex		= URI.indexOf('?');
		if (queryIndex > 0){
			canonicalURI	= URI.substring(0, queryIndex);
			canonicalQueryString
							= URI.substring(queryIndex, URI.length());
		}
		
		String[] methodCanonicalUQ 
							= new String[3];
		methodCanonicalUQ[0]= method;
		methodCanonicalUQ[1]= canonicalURI;
		methodCanonicalUQ[2]= canonicalQueryString;
		
		return methodCanonicalUQ;
	}
	
	public String doSig(List<String> headers, String requestBody, String hmacKey) throws Exception{		
		String sigPayload	= buildCanonicalRequest(headers, requestBody.trim());
		
		Date sigDateStamp	= new Date();
		String regionName	= retriveRegion(headers);
		String serviceName	= retriveService(headers);		
		byte[] sigKey		= getSignatureKey(hmacKey, sigDateStamp, regionName, serviceName);

		byte[] sig			= CryptUtil.doHmacSHA256(sigPayload, sigKey);		
		String sigStr		= new String(sig, "UTF-8");
		
		return sigStr;
	}
	
	/**
	 * This method is cloned from http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
	 * @param sigKey: AWS acces key
	 * @param dateStamp
	 * @param regionName
	 * @param serviceName
	 * @return
	 * @throws Exception
	 */
	private byte[] getSignatureKey(String sigKey, Date dateStamp, String regionName, String serviceName) throws Exception  {
	     byte[] kSecret = ("AWS4" + sigKey).getBytes("UTF8");
	     byte[] kDate    = CryptUtil.doHmacSHA256(getDateTimeStamp(dateStamp), kSecret);
	     byte[] kRegion  = CryptUtil.doHmacSHA256(regionName, kDate);
	     byte[] kService = CryptUtil.doHmacSHA256(serviceName, kRegion);
	     byte[] kSigning = CryptUtil.doHmacSHA256("aws4_request", kService);
	     
	     return kSigning;
	}
	
	
	/**
	 * This method is cloned from AWS4Signer.java
	 * @param date
	 * @return
	 */
	private String getDateTimeStamp(Date date) {
        return dateTimeFormat.get().format(date);
    }
	
	/**
	 * This method is cloned from AWS4Signer.java
	 * @param date
	 * @return
	 */
	private ThreadLocal<SimpleDateFormat> dateTimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
            return dateTimeFormat;
        }
    };

    
    /**
     * 
     * @param timeOffset
     * @return
     */
    protected Date getSignatureDate(int timeOffset) {
        Date dateValue = new Date();
        if (timeOffset != 0) {
            long epochMillis = dateValue.getTime();
            epochMillis -= timeOffset*1000;
            dateValue = new Date(epochMillis);   
        }
        return dateValue;
    }

	//dynamodb.us-west-2.amazonaws.com
	public static String retriveRegion(List<String> headers){
		return REGION;
	}

	// X-Amz-Target: AWSCognitoIdentityService.CreateIdentityPool
	public String retriveService(List<String> headers){
		String xTargetHeader	= getAnHeader(headers, X_TARGET);
		String service			= xTargetHeader.substring(0, xTargetHeader.indexOf('.'));
		
		return service;
	}

	public static String retrieveSignedHeaders(String authHeader){
			_debug.println("authHeader for retrieve signed headers: \n" + authHeader);
			
			String signedHeaderString = null;
			
			StringTokenizer st = new StringTokenizer(authHeader, ",");
			while (st.hasMoreTokens()){
				String auth = st.nextToken().trim();
	//			_debug.print("SignedHeader: \n\t" + authHeader);
				if (auth.startsWith("SignedHeaders=")){
	//				_debug.println("SignedHeaders: " + auth);
					signedHeaderString	= auth.substring("SignedHeaders=".length());
					_debug.println("signedHeaderString: " + signedHeaderString);
					return signedHeaderString;	 
				}
			}
			
			throw new HeaderUpdaterException("no signed headers are reretrieved from auth header!");		
		}

	/**
		 * 
		 AWS4-HMAC-SHA256 Credential=AKIAJB4CDXCM47C3NGHQ/20140927/us-east-1/cognito-identity/aws4_request, SignedHeaders=content-encoding;content-type;host;user-agent;x-amz-date;x-amz-target, Signature=f5d9219160b2bd57fbe48e2972e2ee306dbbbca99322c1a948b1f425cf5da32e
		 * @param headers
		 * @return
		 */
		public static ArrayList<String> retrieveSignedHeaderList(String signedHeaders){		
	//		_debug.debug("authNHeader: " + authNHeader);
			
			ArrayList<String> signedHeaderList = new ArrayList<String>();			
			StringTokenizer _st = new StringTokenizer(signedHeaders, ";");
			while (_st.hasMoreElements()){
				signedHeaderList.add(_st.nextToken().trim());				
			}				
			
			_debug.printlnStringCollection(signedHeaderList);
			
			return signedHeaderList;
		}
}
