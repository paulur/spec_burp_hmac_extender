package com.spec.extender.hmac.updater;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.spec.extender.CONST;
import com.spec.extender.exception.ConfigParserException;
import com.spec.extender.exception.HeaderUpdaterException;
import com.spec.extender.util._debug;

public class UpdaterFactory {	
	private HashMap<String, Updater> updaterMap
							= new HashMap<String, Updater>();
	private File configFile	= null;
	
	public UpdaterFactory(){
		configFile = new File(CONST.CONFIG_FILE_NAME); 
		if (configFile == null) throw new ConfigParserException("No config file is found.");
		init();
	} 
	public static void main(String args[]){}
	
	public Updater create(String requestURL){
		Updater updater = null;
		
//		_debug.println("\nTo create a header udpater for " + requestURL);
		for (String configURL : this.updaterMap.keySet()){
//			_debug.println("\n Updater create() check config URL: |" + configURL + "|");
			if (requestURL.startsWith(configURL)){
//				_debug.println("find the request url in config");
				updater	= this.updaterMap.get(configURL);
				break;
			}
		}
		
		if (updater == null) 
			throw new HeaderUpdaterException("No HeaderUpdater is created for the request to: " + requestURL);
		
		System.out.println("SigUpdater class created as " + updater.toString());
		return updater;
	}
	
	private void init(){
		DocumentBuilderFactory dbFactory 	
							= DocumentBuilderFactory.newInstance();
		NodeList cList, bList, iList, kList, sList = null; 	
		try {
			DocumentBuilder dBuilder
							= dbFactory.newDocumentBuilder();
			Document doc 	= dBuilder.parse(this.configFile);
			doc.getDocumentElement().normalize();
			
			bList	= doc.getElementsByTagName("service-base-url"); 	
			cList 	= doc.getElementsByTagName("updater-class");				
			iList	= doc.getElementsByTagName("client-id");
			kList	= doc.getElementsByTagName("hmac-key");	
			sList	= doc.getElementsByTagName("signature-place-holder");		

			initHeaderMapWithParsedNodeLists(bList, cList, iList, kList, sList);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private void initHeaderMapWithParsedNodeLists(NodeList bList, NodeList cList, NodeList iList, NodeList kList, NodeList sList){
		String updaterClass	= null;
		String hmacKey		= null;
		String clientID		= null;
		String signaturePlaceholder
							= null;
		String baseURL		= null;
		
		int listLength = Math.max(Math.max(cList.getLength(), bList.getLength()), Math.max(kList.getLength(), sList.getLength()));
		for (int i=0; i < listLength; i++){
			updaterClass	= cList.item(i).getTextContent().trim();
			baseURL			= bList.item(i).getTextContent().trim();
			clientID		= iList.item(i).getTextContent().trim();
			hmacKey			= kList.item(i).getTextContent().trim();
			signaturePlaceholder
							= sList.item(i).getTextContent().trim();
			
			if (updaterClass == null) 
				throw new ConfigParserException("Item " + i + "no class name is retrieved from config file at: " 
						+ CONST.CONFIG_FILE_NAME);
			if (baseURL	== null)
				throw new ConfigParserException("Item " + i + " base url: " + baseURL + " no service base url is retrieved from config file at: " 
						+ configFile.getAbsolutePath());
			if (!baseURL.startsWith("http"))
				throw new ConfigParserException("Item " + i + " service base url does not have known protocol http or https: " 
						+ configFile.getAbsolutePath());
			if (hmacKey == null) 
				throw new ConfigParserException("Item " + i + "no hmac key is retrieved from config file at: " 
						+ configFile.getAbsolutePath());
			if (signaturePlaceholder == null)
				throw new ConfigParserException("Item " + i + "no signature placeholder is retrieved from config file at: "
						+ configFile.getAbsolutePath());
				
//			_debug.println("class name: " + updaterClass);
//			_debug.println("baseURL: " + baseURL);
//			_debug.println("hmacKey: " + hmacKey);
//			_debug.println("signaturePlaceholder: " + signaturePlaceholder);
			
			try {
				Updater hUpdater = null;
				Class<?> c 		= Class.forName(updaterClass);
				Constructor<?> cons 
								= c.getConstructor(String.class, String.class, String.class, String.class);			
				hUpdater 		= (Updater)cons.newInstance(baseURL, clientID, hmacKey, signaturePlaceholder);
				
//				_debug.println("UpdaterFactory init: " + hUpdater.getClass() 
//								+ " instance is created as : " 
//								+ hUpdater.presentString()
//								);

				updaterMap.put(baseURL, hUpdater);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		

}
