package com.borqs.sync.client.vdata.card;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//import android.os.SystemProperties;

public class VCardUtil {
	
	/**
	
	 * Convert escaped text into unescaped text.
	 */
	protected static String unescapeText(String text) {
		// Original vCard 2.1 specification does not allow transformation
		// "\:" -> ":", "\," -> ",", and "\\" -> "\", but previous
		// implementation of
		// this class allowed them, so keep it as is.
		// In String#replaceAll(), "\\\\" means single slash.
		return text.replaceAll("\\\\;", ";").replaceAll("\\\\:", ":")
				.replaceAll("\\\\,", ",").replaceAll("\\\\\\\\", "\\\\");
	}
	
	/**
	
	 * Splite value by ";",but "\;" exclude;
	 */
	public static ArrayList<String> spliteString(String value){

		final String IMPOSSIBLE_STRING = "\0";
		// First replace two backslashes with impossible strings.
		String propertyValue = value.replaceAll("\\\\\\\\", IMPOSSIBLE_STRING);

		// Now, split propertyValue with ; whose previous char is not back
		// slash.
		Pattern pattern = Pattern.compile("(?<!\\\\);");
		// TODO: limit should be set in accordance with propertyName?
		String[] strArray = pattern.split(propertyValue, -1);
		ArrayList<String> arrayList = new ArrayList<String>();
		for (String str : strArray) {
			// Replace impossible strings with original two backslashes
			arrayList.add(unescapeText(str.replaceAll(IMPOSSIBLE_STRING,
					"\\\\\\\\")));
		}
		return arrayList;
	}
	
	public static final int ZH = 1;
	public static final int EN = 2;
	
//	public static String getSystemLanguage(){
//		return SystemProperties.get("persist.sys.language");
//	}
	
	/**
	 * 
	 * @return if the system language is english
	 */
	public static boolean isEnglish(){
//		return Locale.ENGLISH.getLanguage().equalsIgnoreCase(getSystemLanguage());
		return false;
	}
	
	/**
	 * 
	 * @return if the system language is chinese
	 */
	public static boolean isChinese(){
//		return Locale.CHINESE.getLanguage().equalsIgnoreCase(getSystemLanguage())
//		|| "".equalsIgnoreCase(getSystemLanguage())
//		|| getSystemLanguage() == null;
	    return true;
	}
	
	/**
	 * convert List<Long> to long[]
	 * @param list<Long>
	 * @return
	 */
	static long[] toLongArray(List<Long> list){
		if(list != null){
			long[] longArray = new long[list.size()];
			for (int i = 0; i < list.size(); i++) {
				longArray[i] = list.get(i);
			}
			return longArray;
		}
		return new long[0];
	}
	
	static int getListSize(List<?> list){
		return list != null ?list.size():0;
	}
	
}
