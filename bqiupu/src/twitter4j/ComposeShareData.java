package twitter4j;

import java.io.File;

import android.graphics.Bitmap;
import android.text.TextUtils;

public class ComposeShareData implements java.io.Serializable {

    private static final long serialVersionUID = 1693747544561152180L;

    public static final int   LINK_TYPE        = 1;
    public static final int   APK_TYPE         = 2;
    public static final int   PHOTO_TYPE       = 3;
    public static final int   VCARD_TYPE       = 4;// pure text || VCard
    public static final int   RETWEET_TYPE     = 5;
    public static final int   PURETEXT_TYPE    = 6;
    public static final int   OTHER_TYPE       = 7;

    public static final int WAITING_STATUS   = 0; //initial status
    public static final int FAILED_STATUS    = 1; //failed, can re-send
    public static final int UPLOADING_STATUS = 2; //can show when enter compose again
    public static final int SUCCEED_STATUS   = 3; //

    public static final int COMMIT_STATUS = 1;

    public int                mStatus;
    public String             mRecipient;
    public boolean            mIsPrivate;
    public String             mMessage;
    public String             mLocation;
    public boolean            mAllowComment = true;
    public boolean            mAllowLike = true;
    public boolean            mAllowShare = true;
    public boolean            mReshare;
    public int                mType;
    public String             mUrl;
    public String             mTitle;
    // for link
    public Bitmap mFavIcon;
    // for apk
    public String             mVersion;
    public String             mApkServerId;
    public String             mAPkPackageName;
    // for photo
    public File mFile;
    // for vcard and photo
    public String mAppData;
    // for video, audio and other static files
    public String mSummary;
    public String mDescription;
    public File mScreenShotFile;
    public String mLocaiton;
    public int mCommit = -1;

    public boolean mSendEmail;
    public boolean mSendSms;
    public boolean mIsTop;
    public String mCategoryId;

    public int ID;    
    private static int _id = 0;
    
    public ComposeShareData()
    {
    	super();
    	
    	mStatus = WAITING_STATUS;
    	_id++;
    	
    	ID = _id;
    }

    public ComposeShareData clone() {
        ComposeShareData qiupuShareData = new ComposeShareData();

        qiupuShareData.mStatus = mStatus;
        qiupuShareData.mRecipient = mRecipient;
        qiupuShareData.mIsPrivate = mIsPrivate;
        qiupuShareData.mMessage = mMessage;
        qiupuShareData.mLocation = mLocation;
        
        qiupuShareData.mAllowComment = mAllowComment;
        qiupuShareData.mAllowLike = mAllowLike;
        qiupuShareData.mReshare = mReshare;
        
        qiupuShareData.mType = mType;
        qiupuShareData.mTitle = mTitle;
        qiupuShareData.mUrl = mUrl;

        // for link
        qiupuShareData.mFavIcon = mFavIcon;

        // for apk
        qiupuShareData.mApkServerId = mApkServerId;
        qiupuShareData.mAPkPackageName = mAPkPackageName;

        // for photo
        qiupuShareData.mFile = mFile;

        // for photo and vcard
        qiupuShareData.mAppData = mAppData;

        // for apk and vcard
        qiupuShareData.mVersion = mVersion;

        // for video, audio and other static files
        qiupuShareData.mSummary = mSummary;
        qiupuShareData.mDescription = mDescription;
        qiupuShareData.mScreenShotFile = mScreenShotFile;
        qiupuShareData.mCategoryId = mCategoryId;

        return qiupuShareData;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComposeShareData)) {
            return false;
        }
        ComposeShareData csd = (ComposeShareData) obj;

        if (TextUtils.isEmpty(csd.mUrl)) {
            if (!TextUtils.isEmpty(csd.mMessage)) {
                return csd.mMessage.equals(mMessage) && csd.mCommit == mCommit;
            } else if (!TextUtils.isEmpty(csd.mLocation)) {
                return csd.mLocation.equals(mLocation) && csd.mCommit == mCommit;
            } else {
                return csd.mType == mType && csd.mCommit == mCommit;
            }
        } else {
            return (csd.mUrl.equals(mUrl) && csd.mCommit == mCommit);
        }
    }

    public void despose() {

        mRecipient = null;
        mMessage = null;
        mLocation = null;

        mIsPrivate = false;
        mAllowComment = false;
        mAllowLike = false;
        mReshare = false;

        mType = 0;
        mTitle = null;
        mUrl = null;

        mVersion = null;
        mApkServerId = null;
        mAPkPackageName = null;
        mAppData = null;
        mFavIcon = null;

        mSummary = null;
        mDescription = null;
        mLocaiton = null;
    }

    @Override
    public String toString() {
        return "mType = " + mType 
                + " mUrl = " + mUrl
                + " mTitle = " + mTitle
                + " mStatus = " + mStatus
                + " mVersion = " + mVersion
                + " mCommit = " + mCommit;
    }
}
