package spec.extender.exception;

public class HeaderUpdaterException extends ExtenderException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HeaderUpdaterException(){
		printException("Header Updater Exception");
	}
	
	public HeaderUpdaterException(String message){
		printException("Header Updater Exception: " + message);
	}
}
