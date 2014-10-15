package spec.extender.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StringUtil {

	public static String listToString(List<String> list){
		String[] array = list.toArray(new String[0]);	
		return Arrays.toString(array);
	}

	public static String listToStringLines(List<String> list){
		Iterator<String> headerIter = list.iterator();
		StringBuffer listBuffer	= new StringBuffer();
		while (headerIter.hasNext()){
			listBuffer.append( headerIter.next() + ",\n");
		}
		
		String listString = listBuffer.toString();
		return listString.substring(0, listString.length()-2);
	}
	
//	public static List<String>()

}
