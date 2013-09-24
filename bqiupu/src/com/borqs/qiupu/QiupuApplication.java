/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.qiupu;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageState;

import java.io.File;

import twitter4j.UserCircle;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.SmileyParser;
import com.borqs.contacts.app.ContactsApp;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.calendar.CalendarSyncService;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.util.BaiduLocationProxy;
import com.borqs.qiupu.util.QiupuFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.URLConnectionImageDownloader;

public class QiupuApplication extends Application implements QiupuService.BorqsAccountAppInterface {
	private static final String TAG = "QiupuApplication";

	private ServiceConnection       mBorqsAccountServiceConnection;
	private ContactsApp mAccountSyncApp;

	private static final Uri ACCOUNT_CONTENT_URI = Uri.parse("content://com.borqs.account/account");

    public static final UserCircle VIEW_MODE_PERSONAL = null;
    public static UserCircle mTopOrganizationId;
    static {
        mTopOrganizationId = VIEW_MODE_PERSONAL;
    }

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate, " + this);
		super.onCreate();

        if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
            BaiduLocationProxy.getInstance(getApplicationContext()).initBaiduLocationEnv(getApplicationContext());
        }

        AccountServiceUtils.onCreate();
        
        QiupuHelper.setORM(QiupuORM.getInstance(getApplicationContext()));

		//start account service
//		try{
//			Intent intent = new Intent();
//			intent.setClassName(getPackageName(), IntentUtil.ACTION_BORQS_ACCOUNT_SERVICE);
//			startService(intent);
//		}catch(Exception ne){ne.printStackTrace();}
        
        Intent calendarIntent = new Intent(QiupuApplication.this, CalendarSyncService.class);
		startService(calendarIntent);
		
		mBorqsAccountServiceConnection = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected name:" + name);
				AccountServiceUtils.onServiceDisconnected();
				AccountServiceConnectObserver.onAccountServiceDisconnected();
				
				//re-connecte the service in background
				bindBorqsAccountService();
				
				//start service at background, make sure the service is running
				Intent in = new Intent(QiupuApplication.this, QiupuService.class);
				in.setAction(QiupuService.INTENT_QP_PHONE_BOOT);
				startService(in);
				
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected name:" + name);

                AccountServiceUtils.onServiceConnected(service);
				AccountServiceConnectObserver.onAccountServiceConnected();
			}
		};
		
//		bindBorqsAccountService();
		
		
	    ContentResolver resolver = getContentResolver();
	    resolver.registerContentObserver(ACCOUNT_CONTENT_URI, true, mAccountObserver);
	    
	    //for account sync runtime context
	    mAccountSyncApp = new ContactsApp();
	    mAccountSyncApp.onCreate(this);

        SmileyParser.init(this);
        
	}
	
	public static File  getImageCacheDir() {
		if (getExternalStorageState().equals(MEDIA_MOUNTED)) {
			return  new File(QiupuConfig.APP_ICON_SDCARD_PATH);
        }
		return  new File(QiupuConfig.APP_ICON_PHONE_PATH);
	}
	
	private static ImageLoader imageLoader;
	private ImageLoaderConfiguration imgeLoadConfig;
	
	public ImageLoaderConfiguration getImgeLoadConfig() {
		if(imgeLoadConfig == null)  {
			MemoryCacheAware memoryCache = null;
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
				memoryCache = new LRULimitedMemoryCache(1024 * 1024*memClass/8);
			}else {
				memoryCache = new WeakMemoryCache();
			}
			// Create configuration for ImageLoader (all options are optional, use only those you really want to customize)
			// DON'T COPY THIS CODE TO YOUR PROJECT! This is just example of using ALL options. Most of them have default values.
			imgeLoadConfig = new ImageLoaderConfiguration.Builder(getApplicationContext())
//						.memoryCacheExtraOptions(480, 800) // max width, max height
//						.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75) // Can slow ImageLoader, use it carefully (Better don't use it)
			.threadPoolSize(3)
			.threadPriority(Thread.NORM_PRIORITY - 1)
			.denyCacheImageMultipleSizesInMemory()
			.offOutOfMemoryHandling()
			.memoryCache(memoryCache) // You can pass your own memory cache implementation
			.discCache(new UnlimitedDiscCache(getImageCacheDir(),new QiupuFileNameGenerator())) // You can pass your own disc cache implementation
			.imageDownloader(new URLConnectionImageDownloader(20 * 1000, 120 * 1000)) // connectTimeout (5 s), readTimeout (20 s)
			.tasksProcessingOrder(QueueProcessingType.LIFO)
//						.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
			.enableLogging()
			.build();
		}
		return imgeLoadConfig;
	}
	public ImageLoader getImageLoader() {
		if(imageLoader == null)  {
//			File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "qiupu/cache");
			// Get singletone instance of ImageLoader
			imageLoader = ImageLoader.getInstance();
			
			// Initialize ImageLoader with created configuration. Do it once on Application start.
			imageLoader.init(getImgeLoadConfig());
			
		}
		return imageLoader;
	}

	public static QiupuApplication getApplication(Context context) {
		return (QiupuApplication) context.getApplicationContext();
	}
	
	
	@Override
	public void onTerminate() {
		Log.d(TAG, "onTerminate");
		mAccountSyncApp.onTerminate(this);
		super.onTerminate();
	}

	private final ContentObserver mAccountObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
           Log.d(TAG, "onChange selfChange:"+selfChange);
        }
    };

    // First, try to invoke global service if existing, otherwise directly invoke local service.
    public void bindBorqsAccountService() {
    	Log.d(TAG, "start  bind borqsAccountService !!");
    	Intent intent = IntentUtil.getBorqsAccountServiceBindIntent();
    	bindService(intent, mBorqsAccountServiceConnection, Context.BIND_AUTO_CREATE);  
    }
    
	

    public boolean ensureBorqsAccountService () {
        if (!AccountServiceUtils.isAccountServiceReady()) {
            bindBorqsAccountService();
            return false;
        }

        return true;
    }

    @Override
    public boolean isAppScanDependent() {
//        return mRunningMode == RUNNING_MODE_APP_SHARE || mRunningMode == RUNNING_MODE_BOX;
        // Force to be dependent for listening app install/uninstall, until completely separation.
        return true;
    }

}
