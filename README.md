I. Introduction
This application is an extender of Burp (http://portswigger.net/burp/). This Burp extender recalculates the hmac signature of an HTTP request when the request is edited on Burp (e.g., intruder or repeater). Details of the use-cases are described in the pdf file, burp-extender.pdf.

Eeach application may have its own way to create hmac signature. Some applications may simply create the hmac signagure by using a timestamp and the request URL, some applications may create the signature using the URL and the request body, and others may have more complicated ways to generate the signature (e.g., AWS sig4 http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html). This extender provides the essential framework for penetration testers to implement their own solutions for a specific application. 

2. Extension
To create a new updater class for an application, pentester should follow the following steps:
i. Create a new class in the package of spec.extender.hmac.updater 
ii. The new class should extends BaseUpdater, which is the base class in the template patten for implementing the Updater interface
- Implement all the getters declared in Updater interface
	public String getHmacKey();
	public String getSignaturePlaceholder();
	public String getServiceBaseURL();
	public String getClientID();
- Implement the two abstract metods declared in BaseUpdater
	protected abstract List<String> updateHeaders(List<String> headers, String requestBody);
	protected abstract String updateBody(List<String> headers, String requestBody);

iv. Update the updater_config.xml, following the instructions inside the configure file

3. Installation
i. Export /src to a jar file.
ii. Add the jar file as an Burp extender. (See the screenshot of adding the extender in burp-extender.pdf). 
--Note there is a new tab named "Logger" in the second row of menu in Burp, after the extender is installed successfully, 
iii. Put the update_config.xml file edited in the "Extension" section to the directory of the burp jar file (e.g., burpsuite_pro_v1.6.05.jar). For example, using the "dir" command on windows , it should look like
08/27/2014  12:34 PM        12,890,423	burpsuite_pro_v1.6.05.jar
08/27/2014  12:38 PM                47			suite.bat
10/08/2014  10:51 PM             2,410		updater_config.xml

iv. Lanuch either intruder or repeater on burp
v. After the request is processed, click the "Logger" panel: observe those requests whose values are "Extender" under the "Tool" column, which are the edited requests using the Intruder's payload and having the recalculated hmac signatures.

4. Example
There are two example Updater clasess in this package: DemoURLUpdater and DemoRequestHeaderUpdater. DemoURLUpdater will append the hmac signature to the URL; DemoRequestHeaderUpdater will update the Authentication header with the new hmac signature. To use these examples, you just simply change the configuration file.

i. Use DemoURLUpdater
i.1 Add to the configuration file the following entry:
	<updater>
		<service-base-url>https://google.com:443/</service-base-url>
		<updater-class>spec.extender.hmac.updater.DemoURLUpdater</updater-class>
		<client-id>anID</client-id>
		<hmac-key>anSecretKey</hmac-key>
		<signature-place-holder>NotUsedHolder$</signature-place-holder>	
	</updater>
i.2 Add the extender to Burp. 
i.3 From the browser, send a request to https://google.com
i.4 On burp, intercept the request and send the request to repeater
i.5 On repeater, send the request out
i.6 On logger, find the request sent out from extender

ii. Use DemoURLUpdater
ii.1 Add to the configuration file the following entry:
	<updater>
		<service-base-url>https://google.com:443/</service-base-url>
		<updater-class>spec.extender.hmac.updater.DemoRequestHeaderUpdater</updater-class>
		<client-id>anID</client-id>
		<hmac-key>anSecretKey</hmac-key>
		<signature-place-holder>Authorization: hmac-v1 demo:$</signature-place-holder>	
	</updater>
ii.2 Add the extender to Burp. 
ii.3 From the browser, send a request to https://google.com
ii.4 On burp, intercept the request and send the request to repeater
ii.5 On repeater, add two headers to the request as the following
Authorization: hmac-v1 demo:zuKDafBvS0DSZ63s8MHjYOyp/8M=
Date: Mon, 13 Oct 2014 13:27:40 CDT
ii.6 On repeater, send out the edited request
ii.7 On logger, find the request sent out from extender

5. Misc
To turn on/off the debug information, change the debugOn at line 8 in  spec.extender.util._debug class.
