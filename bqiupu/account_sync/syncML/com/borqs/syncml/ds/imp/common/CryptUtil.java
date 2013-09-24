/**
 * Copy right Borqs 2009
 */
package com.borqs.syncml.ds.imp.common;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import com.borqs.util.android.Base64;

import android.text.TextUtils;

public class CryptUtil {

	private Cipher cipher;
	private DESKeySpec desKeySpec;
	private SecretKeyFactory keyFactory;
	private SecretKey secretKey;

	public CryptUtil() {
		try {
			cipher = Cipher.getInstance("DES");
			byte[] desKeyData = { (byte) 0x04, (byte) 0x64, (byte) 0x01,
					(byte) 0x06, (byte) 0x62, (byte) 0x61, (byte) 0x01,
					(byte) 0x65 };
			desKeySpec = new DESKeySpec(desKeyData);
			keyFactory = SecretKeyFactory.getInstance("DES");
			secretKey = keyFactory.generateSecret(desKeySpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * encrypt a string
	 * 
	 * @param originaltext
	 * @return
	 */
	public String encrypt(String originaltext) {
		if (TextUtils.isEmpty(originaltext)) {
			return originaltext;
		}
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] originalbyte = originaltext.getBytes();
			byte[] decryptbyte = cipher.doFinal(originalbyte);
			return new String(Base64.encode(decryptbyte, Base64.DEFAULT));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * decrypt a string
	 * 
	 * @param decrypttext
	 * @return
	 */
	public String decrypt(String decrypttext) {
		if (TextUtils.isEmpty(decrypttext)) {
			return decrypttext;
		}
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decryptbyte = Base64.decode(decrypttext.getBytes(), Base64.DEFAULT);
			byte[] originalbyte = cipher.doFinal(decryptbyte);
			return new String(originalbyte);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
