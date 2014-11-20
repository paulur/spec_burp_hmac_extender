package com.spec.extender;

public interface CONST {
	String CONFIG_FILE_NAME		= "updater_config.xml";
	
	String SIGNATURE_PLACEHOLDER_CHAR
								= "$";
	
	//burp UI constants
	String COL_NAME[]	= {
								"Req#",
								"Tool", 
								"URL", 
	//							"Method", 
								"Status", 
								"Length", 
								"Time", 
	//							"IP"
							};
	int NUM_COL 		= COL_NAME.length;
	
	/** encryption algorithm. */
	String HMAC_SHA1_ALGORITHM 		= "HmacSHA1";
	String HMAC_SHA256_ALGORITHM	= "HmacSHA256";
	
	
	/**
	 * Header Names
	 */
	String HEADER_DATE	= "Date";
	String HEADER_AUTHZ	= "Authorization";

	final static public String AuthHeaderName	= "Authorization";

}
