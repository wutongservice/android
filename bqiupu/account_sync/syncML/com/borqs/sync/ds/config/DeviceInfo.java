package com.borqs.sync.ds.config;

import android.content.Context;
import android.os.Build;
import com.borqs.common.account.Configuration;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.contacts_plus.R;
import com.borqs.sync.client.common.SyncDeviceContext;
import com.borqs.sync.service.Define;
import com.borqs.syncml.ds.protocol.IDeviceInfo;
import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.DevInf;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

//b335  removed reference to qiupu
//import com.borqs.qiupu.db.QiupuORM;

public class DeviceInfo implements IDeviceInfo {
	private static final String TAG = "DeviceInfo";
	private Context mContext;
	private int mMaxMsgSize;
	private String mUserAgent;
	private String mDevID;
	private int mMaxObjSize;
	private String mVerDTD;
	private String mMan;
	private String mMod;
	private String mOem;
	private String mFwv;
	private String mSwv;
	private String mHwv;
	private String mDevType;
	private boolean mUtc;
	private boolean mLargeObject;
	private boolean mNumberOfChange;
	private boolean mSyncContactsPhoto;
//	private boolean mDisplayUserInfo;
	private boolean mDisplaySettings;
	private boolean mPrintLog;
    private SyncDeviceContext mDevice;

	public DeviceInfo(Context context) {
		mContext = context;
        mDevice = new SyncDeviceContext(mContext);

		Properties prop = new Properties();
		InputStream fis;
		try {
			fis = mContext.getResources().openRawResource(
					R.raw.syncml_ds_dev_info);
			prop.loadFromXML(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mVerDTD = prop.getProperty("dev_ver_dtd", "1.1");
//		mMan = "Motorola";//prop.getProperty("dev_man", "borqs");
//		mMod = "E6"; //prop.getProperty("dev_mod", "o-phone166");
		mMan = prop.getProperty("dev_man", Build.MANUFACTURER);
		mMod = prop.getProperty("dev_mod", Build.MODEL);
		mOem = prop.getProperty("dev_oem", Build.TYPE);
		mFwv = prop.getProperty("dev_fwv", Build.DEVICE);
		mSwv = prop.getProperty("dev_swv", Build.PRODUCT);
		mHwv = prop.getProperty("dev_hwv", Build.HARDWARE);
		mDevType = prop.getProperty("dev_type", "phone");
		mUtc = "true".equals(prop.getProperty("dev_utc", "true"));
		mLargeObject = "true".equals(prop.getProperty("dev_large_object",
				"true"));
		mNumberOfChange = "true".equals(prop.getProperty(
				"dev_number_of_change", "true"));
		mMaxMsgSize = Integer.parseInt(prop.getProperty("dev_max_msg_size",
				"10240"));
		mMaxObjSize = Integer.parseInt(prop.getProperty("dev_max_obj_size",
				"163840"));

		mDevID = mDevice.getDeviceId();
		//
//		mUserAgent = "MOT-E6/unknown R533_G_11.10.44R/unknown Opera/8.0 Profile/MIDP-2.0 Configuration/CLDC-1.0";
		mUserAgent =prop.getProperty("user_agent",
				"o-phone166/CMCC(0.1) OMS1.0/MIDP-2.0 Configuration/CLDC-1.1");
		mSyncContactsPhoto = "true".equals(prop.getProperty("sync_photo",
				"false"));
		// in screen Setting ,control the user info display or not 
//		mDisplayUserInfo = "true".equals(prop.getProperty("display_user_info",
//		"false"));
		mDisplaySettings = "true".equals(prop.getProperty("display_settings",
				"false"));
		mPrintLog = "true".equals(prop.getProperty("print_log",	"false"));
	}

	public int getMaxMsgSize() {
		return mMaxMsgSize;
	}

	public String getUserAgent() {
		return mUserAgent;
	}

	public String deviceId() {
		return mDevID;
	}

	public int getMaxObjSize() {
		return mMaxObjSize;
	}

	public byte[] deviceData(boolean devCap, int syncItem) throws IOException {
		XmlSerializer writer = SyncmlXml.devInfSerializer();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writer.setOutput(output, "UTF-8");
		writer.startDocument("UTF-8", null);

		writer.startTag(null, DevInf.DevInf);
		SyncmlXml.putTagText(writer, DevInf.VerDTD, mVerDTD);
		SyncmlXml.putTagText(writer, DevInf.Man, mMan);
		SyncmlXml.putTagText(writer, DevInf.Mod, mMod);
		SyncmlXml.putTagText(writer, DevInf.OEM, mOem);
		SyncmlXml.putTagText(writer, DevInf.FwV, mFwv);
		SyncmlXml.putTagText(writer, DevInf.SwV, mSwv);
		SyncmlXml.putTagText(writer, DevInf.HwV, mHwv);
		SyncmlXml.putTagText(writer, DevInf.DevID, mDevID);
		SyncmlXml.putTagText(writer, DevInf.DevTyp, mDevType);

		if (mUtc) {
			SyncmlXml.putTagText(writer, DevInf.UTC, null);
		}
		if (mLargeObject) {
			SyncmlXml.putTagText(writer, DevInf.SupportLargeObjs, null);
		}
		if (mNumberOfChange) {
			SyncmlXml.putTagText(writer, DevInf.SupportNumberOfChanges, null);
		}

		// put DataStore
		InputStream inCapStream = null;
		switch (syncItem) {
			// put card
			case Define.SYNC_ITEMS_INT_CONTACTS:
				inCapStream = dataStoreCard();
				break;
			// put calendar
			case Define.SYNC_ITEMS_INT_CALENDAR:
				inCapStream = dataStoreCalendar();
				break;
		}
			
		if (inCapStream != null) {
			try {
				SyncmlXml.putFullData(writer, inCapStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			inCapStream.close();
		}

		// put CTCap
		if (devCap) {
			InputStream inStream = null;
			switch (syncItem) {
				// put card
				case Define.SYNC_ITEMS_INT_CONTACTS:
					inStream = capCard();
					break;
				// put calendar
				case Define.SYNC_ITEMS_INT_CALENDAR:
					inStream = capCalendar();
					break;
			}
			if (inStream != null) {
				try {
					SyncmlXml.putFullData(writer, inStream);
				} catch (Exception e) {
					e.printStackTrace();
				}
				inStream.close();
			}
		}
		
		writer.endTag(null, DevInf.DevInf);
		writer.endDocument();

		return output.toByteArray();
	}


	public boolean isSyncContactsPhoto() {
		return mSyncContactsPhoto;
	}
	
//	/**
//	 * get the flag that user info display or not
//	 */
//	public boolean isDisplayUserInfo() {
//		return mDisplayUserInfo;
//	}

	/**
	 * get the flag that settings display or not
	 */
	public boolean isDisplaySettings() {
		return mDisplaySettings;
	}
	
	public boolean isPrintLog(){
        //return false;
        //b335
        // QiupuORM.isDebugMode(ApplicationGlobals.getContext());
        return Configuration.isDebugMode(ApplicationGlobals.getContext());
	}
	
	public InputStream dataStoreCard() {
		return mContext.getResources().openRawResource(
				R.raw.syncml_ds_dev_info_data_store_card);
	}

	public InputStream dataStoreCalendar() {
		return mContext.getResources().openRawResource(
				R.raw.syncml_ds_dev_info_data_store_calendar);
	}

	public InputStream capCard() {
		return mContext.getResources().openRawResource(
				R.raw.syncml_ds_dev_info_cap_x_vcard);
	}

	public InputStream capCalendar() {
		return mContext.getResources().openRawResource(
				R.raw.syncml_ds_dev_info_cap_x_vcalendar);
	}
}
