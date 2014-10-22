package com.spec.extender.exception;

public class ExtenderException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ExtenderException(){
		super();
	}
	
	public ExtenderException(String message){
		super(message);
	}
	
	protected void printException(String message){
		System.err.println("\n!!!Exception!!!" + message);
	}
}

