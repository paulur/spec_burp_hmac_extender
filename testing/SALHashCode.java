package spec.burp.hmac;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.util.Calendar;
import java.text.SimpleDateFormat;


public class SALHashCode {

        /** encryption algorithm. */
        public static final String MESSAGE_DIGEST_ALGORITHM = "SHA-1";
        public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

        /**
         * generates an encrypted, encoded HMAC hash of the given arguments.
         *
         * @param httpMethod
         * @param uri
         *            example: "http://mcpapp302p.dev.ch3.s.com:8280/mobileapi/v1/user/authenticate"
         * @param secretKey
         * @return String base-64 encoded HMAC hash.
         * 
         * Jemeter:
         * 	GET https://mcp-salvip.qa.ch3.s.com/mobileapi/v1/location/zipcode/60423
			Date: Mon, 11 Mar 2013 11:07:33 CDT
			Authorization: hmac-v1 SHOPSEARSLITE:LzlZ+3cpOJdO8TYD4Q29BAEHdCQ=	
			Authorization: hmac-v1 CISP:EhE4QMRlQg1pnE0pbiWdlV6Zz4Q%3D		
         */
        public static void main(String args[]) {

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                String dateString = dateFormat.format(calendar.getTime());
                dateString = "Mon, 11 Mar 2013 11:07:33 CDT";
                	//"Mon, 11 Mar 2013 11:24:32 CDT";//"Tue, 26 Jun 2012 16:01:13 GMT";
                System.out.println("Current Date: " + dateString);

                String secretKey = "password123";
                String method = "GET", url = "/mobileapi/v1/location/zipcode/60423";

                if (args.length < 2) {
                        printUsage();
                } else {
                        if(args.length == 3) {
                                secretKey = args[2];
                        }
                        String clearText = method + "\n" + url + "\n" + dateString + "\n";
                        System.out.print("clear text:\n----\n" + clearText + "\n-----\n" + "key: " + secretKey + "\ntimestampe: " + dateString);
                        try {
                                byte[] data_bytes = clearText.getBytes("UTF8");
                                byte[] key_bytes = secretKey.getBytes("UTF8");

                                SecretKeySpec key_spec = new SecretKeySpec(key_bytes, HMAC_SHA1_ALGORITHM);
                                Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
                                mac.init(key_spec);
                                byte[] raw_hash = mac.doFinal(data_bytes);

                                Base64 base64 = new Base64();
                                String hash = new String(base64.encode(raw_hash));
                                System.out.println("\n----------------------------------");
                                System.out.println("Values to be used in SAL Request Headers\n");
                                System.out.println("Method: " + method);
                                System.out.println("url: " + url);
                                System.out.println("Date: " + dateString);
                                System.out.println("Hash: " + hash);
                                System.out.println("----------------------------------");
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                }
        }

        private static void printUsage() {
                System.out.println("Parameters : httpM ethodrequestUrl ");
                System.out.println("----------------------------------");
                System.out.println("Example1 : POST /mobileapi/v1/user/authenticate ");
                System.out.println("Example2 : POST /mobileapi/v1/users/identity ");
        }
}