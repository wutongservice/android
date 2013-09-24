package com.borqs.qiupu.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public static String toMd5(byte[] bytes) {
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(bytes);
			return toHexString(algorithm.digest(), "");
		} catch (NoSuchAlgorithmException e) {
			//Log.v("he--------------------------------ji", "toMd5(): " + e);
			throw new RuntimeException(e);
		}
	} 
	private static String toHexString(byte[] bytes, String separator) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			hexString.append(Integer.toHexString(0xFF & b)).append(separator);
		}
		return hexString.toString();
	}
}
