package com.borqs.qiupu.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class MediaScannerNotifier implements MediaScannerConnectionClient {

	private MediaScannerConnection mConnection;
	private String mPath;
	private String mMimeType;

	/**
	 * Constructor
	 * @param context the context
	 * @param path the file to be indexed
	 * @param mimeType the mimeType (if null the type is automatically determined)
	 */
	public MediaScannerNotifier(Context context, String path, String mimeType) {
		mPath = path;
		mMimeType = mimeType;
		mConnection = new MediaScannerConnection(context, this);
		mConnection.connect();
	}

	/**
	 * reIndex the mPath file
	 * @param mPath the file to be indexed
	 * @see android.media.MediaScannerConnection.MediaScannerConnectionClient#onMediaScannerConnected()
	 */
	public void onMediaScannerConnected() {
		mConnection.scanFile(mPath, mMimeType);
	}

	/**
	 * disconnect on complete
	 */
	public void onScanCompleted(String path, Uri uri) {
		mConnection.disconnect();
	}

}
