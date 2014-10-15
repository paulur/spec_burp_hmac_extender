package spec.extender.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * 
 * @author paul
 *
 */
public class FileUtil {
	/**
	 * 
	 * @param srcFile
	 * @return Each element in the returned ArrayList is one line string in the source file.
	 */
	static public ArrayList<String> writeFileToStringList( String srcFile ) throws IOException{
		ArrayList<String> fileContentStrings = new ArrayList<String>();
		FileReader fileReader = new FileReader( srcFile );
		BufferedReader bufferReader = new BufferedReader( fileReader );	
		
		while ( true ){
			String oneLine = bufferReader.readLine();
		
			if ( oneLine != null )
				fileContentStrings.add( oneLine );
			else
				break;
		}
				
		bufferReader.close();
		fileReader.close();
		
		return fileContentStrings;
	}
	
	static public String writeFileToString(String srcFile) throws IOException {
		ArrayList<String> stringList = writeFileToStringList(srcFile);
		Iterator<String> stringIter	= stringList.iterator();
		
		StringBuffer fileContent = new StringBuffer();		
	
		while (stringIter.hasNext()){
			fileContent.append(stringIter.next());
		}
		
		return fileContent.toString();
	}
	
	static public void cleanRepeatedLine( String srcFile, String tarFile ) throws IOException{		
		ArrayList<String> fileContentStrings = new ArrayList<String>();
		FileReader fileReader = new FileReader( srcFile );
		BufferedReader bufferReader = new BufferedReader( fileReader );	
		
		String tmp = "";
		while ( true ){
			String oneLine = bufferReader.readLine();
					
			if ( oneLine != null ){
				if ( !tmp.equals( oneLine ) ){
					fileContentStrings.add( oneLine );
					tmp = oneLine;
				}
			}else{
				break;
			}
		}				
		bufferReader.close();
		fileReader.close();
		
		writeStringCollectionToFile( fileContentStrings, tarFile );
	}
	
	static public void writeStringCollectionToFile(Collection<String> strings, String filePath){
		try {
			FileWriter writer = new FileWriter( filePath , false );
			BufferedWriter bufferWriter = new BufferedWriter(writer);
			for ( String s : strings ){
				bufferWriter.write( s + "\n" );
			}			
			bufferWriter.flush();
			writer.flush();
			bufferWriter.close();
			writer.close();
		} catch (IOException e) {
			System.err.println( "\nError of write_to_file  [" + filePath + "]: \n" + e );
		}
	}
	
	/**
	 * Write the specified text to the specified file at the beginning
	 * @param file_name
	 * @param text
	 * @throws IOException
	 */
	static public void writeTextToFile( String filePath, String text ) throws IOException{
		writeTextToFile( new File( filePath ), text );
	}
	
	static public void writeTextToFile( File filePath, String text) throws IOException {
		FileWriter writer = new FileWriter( filePath, false );
		BufferedWriter bufferWriter = new BufferedWriter(writer);
		bufferWriter.write(text);
		
		bufferWriter.flush();
		writer.flush();
		bufferWriter.close();
		writer.close();
	}
	
//	append to a file
	static public void appendTextToFile( String file_name, String text ) throws IOException{
		appendTextToFile( new File( file_name ), text );
	}
	
	
	/**
	 * @param file
	 * @param text
	 * @throws IOException
	 */
	static public void appendTextToFile( File file, String text ){
		try{
			FileWriter writer	= new FileWriter( file , true );
	
			for ( int i = 0 ; i < text.length(); i++){
				int ch = (int)text.charAt( i );
				writer.write( ch );
			}	
	
			writer.write( '\n' );
	
			writer.flush();
			writer.close();
		}catch(IOException e){
			System.err.println( "\nError of append_to_file  [" + file + "]: \n" + e );
		}
	}

	static public void appendTextCollectionToFile( String file_name, Collection<Object> texts ) throws IOException{
		for ( Iterator<Object> _i = texts.iterator(); _i.hasNext(); ){
			String a_text_line	= (String)_i.next();

			appendTextToFile( file_name, a_text_line );
		}
	}
	
	static public boolean deleteFile( String file_name ){
		return (new File( file_name )).delete();
	}
}
