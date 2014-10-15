package spec.extender.exception;

public class ConfigParserException extends ExtenderException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConfigParserException(){
		printException("Failed to parse config file.");
	}
	
	public ConfigParserException(String message){
		printException("Failed to parse config file: " + message);
	}
}
