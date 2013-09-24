package com.borqs.sync.ds.datastore;

import java.io.UnsupportedEncodingException;

public class GeneralWordsUtil {
	private static final String ENCODE_UTF_8 = "UTF-8"; 
	private static final char CHAR_SEMICOLON = ';'; 
	private static final char CHAR_SLASH = '\\';
		
	/**
	 * get value by maxlength
	 * 
	 */
	public static String getValueByMaxLength(String value,
											int maxLength,
											boolean isHandleSemicolon,
											boolean isHandleSlash){
		String returnValue = value;
		// when out of length, cut
		if(GeneralWordsUtil.getEncodingLength(value, null, isHandleSemicolon, isHandleSlash) > maxLength){
			returnValue = GeneralWordsUtil.subByChar(value, null, maxLength, isHandleSemicolon, isHandleSlash);
		}
		return returnValue;
	}
	
	// sub String char by char
	public static String subByChar(String value,
								   String encoding,
								   int maxBytes,
								   boolean isHandleSemicolon,
									boolean isHandleSlash){
		if(value == null){
			return null;
		}
		
		String valueToHandle = value;
		int intSubLength = 0;// value to count length
		int intCountLength = 0;// value to count length
		// get encode
		if(encoding == null){
			encoding = ENCODE_UTF_8;
		}
		
		int valueLength = valueToHandle.length(); // value length
		
		char charToCheck; // char of value in each index
		for (int i = 0; i < valueLength; i++) {
			
			charToCheck = valueToHandle.charAt(i);
			
			try {
				// count char length
				intCountLength += String.valueOf(charToCheck).getBytes(encoding).length;
				
				if(isHandleSemicolon && CHAR_SEMICOLON == charToCheck){
					// count ';' length
					intCountLength += 1;
				} 
				if(isHandleSlash && CHAR_SLASH == charToCheck){
					// count '\' length
					intCountLength += 1;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			// length <= maxsize
			if(intCountLength <= maxBytes){
				intSubLength ++;
			} else {
				break;
			}
		}

	    return valueToHandle.substring(0, intSubLength);
	}
	
	/**
	 * get value length
	 * 
	 * @param value
	 * @param encoding
	 * @param isHandleSemicolon
	 * @return
	 */
	public static int getEncodingLength(String value,
										String encoding,
										boolean isHandleSemicolon,
										boolean isHandleSlash){
		if (value == null){
			return 0;
		}
		if (encoding == null){
			encoding = ENCODE_UTF_8;
		}
		String encodingValue = value;
		byte[] encodeValueBytes;
		try {
			encodeValueBytes = encodingValue.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		int returnLength = encodeValueBytes.length;
		// caculate num of ;
		if(isHandleSemicolon){
			returnLength += countCharNum(encodingValue, CHAR_SEMICOLON);
		}
		// caculate '\'
		if(isHandleSlash){
			returnLength += countCharNum(encodingValue, CHAR_SLASH);
		}
		return returnLength;
	}
	
	/*
	 * caculate number of ';'
	 */
	private static int countCharNum(String value, char toCount){
		if(value == null){
			return 0;
		}
		int num = 0;
		for(int i = 0; i < value.length(); i ++){
			if(toCount == value.charAt(i)){
				num += 1;
			}
		}
		return num;	
	}
}
