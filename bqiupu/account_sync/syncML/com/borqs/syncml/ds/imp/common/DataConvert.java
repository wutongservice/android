/**
 * Copy right Borqs 2009
 */
package com.borqs.syncml.ds.imp.common;

import com.borqs.util.android.Base64;


public class DataConvert {
	static public byte[] decode(String type, String data) {
		byte[] retValue = null;
		if ("b64".equals(type)) {
			if (data != null) {
				retValue = Base64.decode(data.getBytes(), Base64.DEFAULT);
			}
		} else if (data != null) {
			retValue = data.getBytes();
		}

		return retValue;
	}
	
}
