package com.spec.extender.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;

/**
 * Cloned from AWS4Signer
 */
/**
 * Utilities for encoding and decoding binary data to and from different forms.
 */
public class BinaryUtil {

    /** Default encoding when extracting binary data from a String */
    private static final String DEFAULT_ENCODING = "UTF-8";


    /**
     * Converts byte data to a Hex-encoded string.
     *
     * @param data
     *            data to hex encode.
     *
     * @return hex-encoded string.
     */
    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i]);
            if (hex.length() == 1) {
                // Append leading zero.
                sb.append("0");
            } else if (hex.length() == 8) {
                // Remove ff prefix from negative numbers.
                hex = hex.substring(6);
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase(Locale.getDefault());
    }

    /**
     * Converts a Hex-encoded data string to the original byte data.
     *
     * @param hexData
     *            hex-encoded data to decode.
     * @return decoded data from the hex string.
     */
    public static byte[] fromHex(String hexData) {
        byte[] result = new byte[(hexData.length() + 1) / 2];
        String hexNumber = null;
        int stringOffset = 0;
        int byteOffset = 0;
        while (stringOffset < hexData.length()) {
            hexNumber = hexData.substring(stringOffset, stringOffset + 2);
            stringOffset += 2;
            result[byteOffset++] = (byte) Integer.parseInt(hexNumber, 16);
        }
        return result;
    }

    /**
     * Converts byte data to a Base64-encoded string.
     *
     * @param data
     *            data to Base64 encode.
     * @return encoded Base64 string.
     */
    public static String toBase64(byte[] data) {
        byte[] b64 = Base64.encodeBase64(data);
        return new String(b64);
    }

    /**
     * Converts a Base64-encoded string to the original byte data.
     *
     * @param b64Data
     *            a Base64-encoded string to decode.
     *
     * @return bytes decoded from a Base64 string.
     */
    public static byte[] fromBase64(String b64Data) {
        byte[] decoded;
        try {
            decoded = Base64.decodeBase64(b64Data.getBytes(DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException uee) {
            // Shouldn't happen if the string is truly Base64 encoded.
            System.err.println("Tried to Base64-decode a String with the wrong encoding: " + uee.toString());
            decoded = Base64.decodeBase64(b64Data.getBytes());
        }
        return decoded;
    }

    /**
     * Wraps a ByteBuffer in an InputStream. 
     * 
     * @param byteBuffer The ByteBuffer to wrap.
     * 
     * @return An InputStream wrapping the ByteBuffer content.
     */
    public static InputStream toStream(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new ByteArrayInputStream(bytes);
    }

    
    public static String byteArrayToString(byte[] byteArray, String charCode) throws UnsupportedEncodingException{
    	String str	= new String(byteArray, charCode);
    	
    	return str;
    }
}
