package spec.extender.exception;

public class UnimplementedException extends ExtenderException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnimplementedException(){
		printException("Unimplemented Exception");
	}
	
	public UnimplementedException(String message){
		printException("Unimplemented Exception: " + message);
	}
}
