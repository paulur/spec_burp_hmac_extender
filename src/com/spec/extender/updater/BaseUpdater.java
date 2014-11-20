package com.spec.extender.updater;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.spec.extender.CONST;
import com.spec.extender.exception.ExtenderException;
import com.spec.extender.util._debug;

public abstract class BaseUpdater implements Updater {
	protected abstract List<String> updateHeaders(List<String> headers, String requestBody);
	protected abstract String updateBody(List<String> headers, String requestBody);
	
	@Override
	public UpdaterPayload doUpdate(UpdaterPayload updaterPayload){
		List<String> headers		= updaterPayload.getHeaders();
		String requestBody			= updaterPayload.getRequestBody();
		
		List<String> updatedHeaders	= updateHeaders(headers, requestBody);
		String updatedBody			= updateBody(headers, requestBody);
		
		return new UpdaterPayload(updatedHeaders, updatedBody);
		
	}
	
	@Override
	public String toString() {
		return "\n\tclass name: " + getClass().getName()
				+ "\n\tservice base url: " + getServiceBaseURL()
				+ "; \n\tclient id: " + getClientID() + "; \n\thmac key: "
				+ getHmacKey() + "; \n\tsignature place holder: "
				+ getSignaturePlaceholder();
	}

	String replaceSigUsingPlaceholder(String signaturePlaceholder, String hmacDigest) {
		int placeholderIndex = signaturePlaceholder
				.indexOf(CONST.SIGNATURE_PLACEHOLDER_CHAR);
		String subStrInfront = signaturePlaceholder.substring(0,
				placeholderIndex);
		StringBuffer udpatedAuthHeaderBuffer = new StringBuffer(subStrInfront
				+ hmacDigest);
		// _debug.println("signaturePlaceholder length: " +
		// signaturePlaceholder.length());
		// _debug.println("placeholderIndex index: " + placeholderIndex);
		if (placeholderIndex < signaturePlaceholder.length() - 1) {
			String subStrAfter = signaturePlaceholder.substring(
					placeholderIndex + 1, signaturePlaceholder.length());
			udpatedAuthHeaderBuffer.append(subStrAfter);
		}

		return udpatedAuthHeaderBuffer.toString();
	}

	/**
	 * If original value doens't exit, headers list doesn't change.
	 * 
	 * @param headers
	 * @param originalValue
	 * @param updatedValue
	 * @return
	 */
	protected List<String> updateAnHeader(List<String> headers,
			String originalValue, String updatedValue) {
		Iterator<String> headerIter = headers.iterator();
		String[] headerArray = new String[headers.size()];

		int i = 0;
		while (headerIter.hasNext()) {
			String nextHeader = headerIter.next();
			if (nextHeader.startsWith(originalValue)) {
				nextHeader = updatedValue;
			}
			headerArray[i++] = nextHeader;
		}

		return Arrays.asList(headerArray);
	}

	/**
	 * Sample AuthZ header: Authorization: hmac-v1
	 * client:UOhSQGFQNF1zxPP/MJNwc+Kk6kY=
	 * 
	 * @param hmacSignature
	 * @param headers
	 * @return
	 */
	protected List<String> updateHMACHeader(List<String> headers,
			String hmacSignature, String signaturePlaceholder) {
		String udpatedAuthHeader = replaceSigUsingPlaceholder(signaturePlaceholder,
				hmacSignature);
		String signatureHeader = signaturePlaceholder.substring(0,
				signaturePlaceholder.indexOf(":"));

		// _debug.println("remove hardcoded signagure header name.");
		return updateAnHeader(headers, signatureHeader, udpatedAuthHeader);
	}

	/**
	 * Burp request URL contains protocol (i.e., http/https). This method remove
	 * the protocol and get the clean url.
	 * 
	 * @param requestURL
	 * @return
	
	private String retrieveURLFromBurpRequestURL(String requestURL) {
		if (requestURL.startsWith("https://"))
			return requestURL.substring(8);
		if (requestURL.startsWith("http://"))
			return requestURL.substring(7);
	
		throw new ExtenderException("Unknown protocol name.");
	}
	 */

	protected String retrieveHeader(List<String> headers, String HeaderName) {
		return getHeaderMap(headers).get(HeaderName);
	}

	protected String retrieveCaseInsensitiveHeader(List<String> headers, String HeaderName) {
		return retrieveCaseInsensitiveHeaderMap(headers).get(HeaderName.toLowerCase());
	}

	protected String retrieveAuthHeader(List<String> headers) {
		return retrieveHeader(headers, CONST.AuthHeaderName);
	}

	/**
	 * All keys are in lower case.
	 * 
	 * @param headers
	 * @return
	 */
	protected HashMap<String, String> retrieveCaseInsensitiveHeaderMap(
			List<String> headers) {
		HashMap<String, String> headerMap = new HashMap<String, String>();

		for (String header : headers) {
			int sepIndex = header.indexOf(":");
			if (sepIndex < 0) {
				headerMap.put(header.toLowerCase(), header.toLowerCase());
			} else {
				// debug("sepIndex: " + sepIndex);
				String headerName = header.substring(0, sepIndex).trim();
				String headerValue = header.substring(sepIndex + 1,
						header.length()).trim();
				// debug(headerName + "|" + headerValue + "\n");
				headerMap.put(headerName.toLowerCase(), headerValue);
			}
		}

		return headerMap;
	}

	protected HashMap<String, String> getHeaderMap(List<String> headers) {
		HashMap<String, String> headerMap = new HashMap<String, String>();
		Iterator<String> headerIter = headers.iterator();

		while (headerIter.hasNext()) {
			String header = headerIter.next();
			// debug(header);
			int sepIndex = header.indexOf(":");
			if (sepIndex < 0) {
				headerMap.put(header, "");
			} else {
				// debug("sepIndex: " + sepIndex);
				String headerName = header.substring(0, sepIndex).trim();
				String headerValue = header.substring(sepIndex + 1,
						header.length()).trim();
				// debug(headerName + "|" + headerValue + "\n");
				headerMap.put(headerName, headerValue);
			}
		}

		return headerMap;
	}

	protected String[] getRequestMethodUrlProtocol(String requestHeader) {
		String[] methodUrlProtocol = new String[3];

		StringTokenizer tokenizer = new StringTokenizer(requestHeader);
		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			methodUrlProtocol[i++] = tokenizer.nextToken();
		}

		// debug(Arrays.toString(methodUrlProtocol));

		return methodUrlProtocol;
	}

	protected String[] getRequestMethodBaseUrlQueryString(String requestHeader){
		String[] mup	 	= getRequestMethodUrlProtocol(requestHeader);
		
		_debug.println(mup[2]);
		
		String path			= mup[1];
		int queryIndex		= path.indexOf('?');
		String baseUrl		= null;
		String queryString	= null;
		if (queryIndex > 0){
			baseUrl	= path.substring(0, queryIndex);
			queryString	= path.substring(queryIndex+1, path.length());
		}else{
			baseUrl		= path;
			queryString	= "";
		}
			
		String[] mbq	= {mup[0], baseUrl, queryString};
		
		return mbq;
	}
	
	protected void addHeader(List<String> headers, String newHeader) {
		headers.add(newHeader);
	}
}
