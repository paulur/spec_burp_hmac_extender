package com.spec.extender.updater;

import java.util.List;

public class UpdaterPayload {
	private List<String>	headers;
	private String 			requestBody;
	
	
	public UpdaterPayload(List<String> headers, String requestBody) {
		this.headers	 = headers;
		this.requestBody = requestBody;
	}
	
	public List<String> getHeaders() {
		return headers;
	}
	
	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
	
	public String getRequestBody() {
		return requestBody;
	}
	
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}
}
