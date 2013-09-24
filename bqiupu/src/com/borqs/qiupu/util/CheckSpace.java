package com.borqs.qiupu.util;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import com.borqs.qiupu.QiupuConfig;

public class CheckSpace {
	// minimal remained space is about 2M
	public static final long MINIMAL_SPACE_IN_SDCARD = 2097152;
	private static final String TAG = "CheckSpace";

	public static boolean checkSDCardAvailable() {
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED))
			return false;
		else
		return true;
	}

	public static boolean checkSDCardFreeSpace() {
		File sdcardDir = Environment.getExternalStorageDirectory();
		if (sdcardDir == null) {
			Log.e(TAG, "checkSDCardFreeSpace: Environment.getExternalStorageDirectory() return null");
		    return false;	
		}
		
		long free = 0;
		try {
			String path = sdcardDir.getPath();
			if (path == null) {
				Log.e(TAG, "checkSDCardFreeSpace: sdcardDir.getPath() return null");
				return false;
			}
			if(QiupuConfig.DBLOGD){
				Log.d(TAG, "checkSDCardFreeSpace: path is:" + path);
			}
		    StatFs stats = new StatFs(path);
 		    free = stats.getAvailableBlocks();
 		    if(QiupuConfig.DBLOGD){
 		    	Log.d(TAG, "checkSDCardFreeSpace: free is:" + free);
 		    }
		} catch (Exception e) {
			Log.e(TAG, "checkSDCardFreeSpace failed");
			e.printStackTrace();
		}

		if (free > 0)
			return true;
		else
			return false;			
	}
	
	public static long getSDCardFreeSpace(){
		return 0;
	}
	
}
