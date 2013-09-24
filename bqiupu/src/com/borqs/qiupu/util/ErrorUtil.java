package com.borqs.qiupu.util;

import java.util.HashMap;

import android.content.Context;

import com.borqs.qiupu.R;

public class ErrorUtil {

	private static ErrorUtil mErrorUtil;
	public static final String ERROR_CODE = "ERROR_CODE";
	public static final String ERROR_MSG = "ERROR_MSG";

	private ErrorUtil(Context context) {
	}

	public static ErrorUtil getInstance(Context context) {
		if (mErrorUtil == null) {
			mErrorUtil = new ErrorUtil(context);
		}
		return mErrorUtil;
	}

	
	public static String getBindErrorMessage(int errorCode, Context context) {
		String[] bindErrorMessage = context.getResources().getStringArray(
				R.array.bind_error_message);
		int[] bindErrorCode = context.getResources().getIntArray(
				R.array.bind_errorcode);
		HashMap<Integer, String> bindErrorMap = new HashMap<Integer, String>();
		for (int i = 0, len = bindErrorMessage.length; i < len; i++) {
			bindErrorMap.put(bindErrorCode[i], bindErrorMessage[i]);
		}
		return bindErrorMap.get(errorCode);
	}
}
