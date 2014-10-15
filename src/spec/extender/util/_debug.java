package spec.extender.util;

import java.util.Collection;
import java.util.Map;

public class _debug {

	static final boolean debugOn = true;
	
	static public void println(){
		print( "\n" );
	}
	public static void print(String info){
		if (debugOn) System.out.println("DEBUG: " + info);
	}
	
	public static void println(String info){
		print(info + "\n");;
	}
	
	static public void printCollection(Collection<Object> objects, int numberOfElementInOneLine, String endLine ){		
		int i = 0;
		
		println("{------------------");
		for (Object object : objects){
			i++;
			print(object + endLine);
			if (i%numberOfElementInOneLine==0)	
				print("\n");
		}
		println();
		println("------------------}");
	}
	
	static public void printIntArray( int[] intArray ){
		for ( int i = 0; i < intArray.length; i++ ){
			print( intArray[i] + ", ");
		}
		println();
	}
	
	static public void printlnStringMap( Map<String, String> strMap ){
		
		for ( String key : strMap.keySet() ){
			String value	= strMap.get( key );
			_debug.println( "key-value: [" + key + ", " + value + "]");
		}
		
		_debug.println( strMap.size() + " map item printed.");
	}
	
	/**
	 * Default number of elements in one line is 5.
	 * @param objects
	 */
	static public void printlnStringCollection( Collection<String> strings ){		
		for ( String s : strings ){
			println("\t" + s );
		}
		println( strings.size() + " strings printed.");
	}
	
	static public void printlnStringArray(String[] os){
		for (int i = 0; i < os.length; i++){
			println(os[i]);
		}
		println("====" + os.length + " strings printed.===");
	}
	
	
	static public void printDoubleCollection( Collection<Double> doubles ){
		for ( Double d : doubles ){
			print( d + "\t");
		}
		println( "." + doubles.size() + " double values printed.");
	}
	
	
	/**
	 * Default number of elements in one line is 5.
	 * @param objects
	 */
	static public void printCollection(Collection<Object> objects){		
		printCollection(objects, 5, ",");
	}
	
	static public void printlnCollection(Collection<Object> objects, int numberOfElementInOneLine){
		printCollection(objects, numberOfElementInOneLine, "\n");
	}
	
	static public void printlnCollection(Collection<Object> objects){
		printCollection(objects, 5, "\n");
	}
	
	static public void exit(int systemSignal){
		System.exit(systemSignal);
	}
	
	static public void exit(){
		System.exit(0);
	}

}
