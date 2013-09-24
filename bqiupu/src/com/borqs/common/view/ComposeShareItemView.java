package com.borqs.common.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import twitter4j.ComposeShareData;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.listener.ComposeActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.FileUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.ContactUtils;

public class ComposeShareItemView extends SNSItemView {

    private static final String TAG = "Qiupu.ComposeShareItemView";

    private String mTitle;
    private ComposeShareData mShareData;
    private ImageView mImageIcon, mRemove;
    private TextView mToUsersView;
    private TextView mCommentsView;
    private TextView mAttachmentView;

    private String mDeleteDialogTitle;
    private String mDeleteDialogMsg;
    private static final String GEO_TAG = "geo=";

    WeakReference<ComposeActionListener> mComposeListener;

    public int getID()
    {
        return mShareData.ID;
    }
    public ComposeShareItemView(Context context, ComposeShareData shareData) {
        super(context);
        mContext = context;
        mShareData = shareData;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setQiupuShareData(ComposeShareData shareData) {
        mShareData = shareData;
        setUI();
    }

    public String getTitle() {
        return mTitle;
    }

    private void init() {
        mDeleteDialogTitle = mContext.getResources().getString(R.string.app_delete);
        removeAllViews();
        View view = LayoutInflater.from(mContext).inflate(R.layout.compose_share_list_item, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(view);

        mImageIcon = (ImageView) view.findViewById(R.id.image_icon);

        mToUsersView = (TextView) view.findViewById(R.id.to_users);
        mCommentsView = (TextView) view.findViewById(R.id.post_comments);
        mAttachmentView = (TextView) view.findViewById(R.id.title_content);

        mRemove = (ImageView) findViewById(R.id.remove_icon);
        mRemove.setOnClickListener(mRemoveListener);

        setUI();
    }

    //TODO need format
    private void formatReceivers(StringBuilder sb)
    {
    	if(isEmpty(mShareData.mRecipient) == true)
    		return ;

    	QiupuORM orm = QiupuORM.getInstance(getContext());
    	String[] ids = mShareData.mRecipient.split(",");
    	if(ids != null && ids.length > 0)
    	{
    		for(int i=0;i<ids.length;i++)
    		{
    			try{
    			    String username = "";
    			    if (ids[i].contains("#")) {//#-2: public, #circle_id
    			        username = orm.getCircleName(Long.parseLong(ids[i].substring(1)));
    			    } else {
    			        username = orm.getUserName(Long.parseLong(ids[i]));
    			    }

		    		if(isEmpty(username) == false)
		    		{
			    		if(sb.length() > 0)
			    			sb.append(", ");
			    	    sb.append(username);
		    		}
    			}catch(Exception ne){
    			    
    			}
    		}
    	}
    }

    private void setRecipientAndCommentUI(String recipient, String attachmentTitle, boolean isLocaiton) {
        if (TextUtils.isEmpty(recipient) == false) {
            mToUsersView.setText(recipient);
            mCommentsView.setVisibility(View.VISIBLE);
        } else {
            mToUsersView.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(mShareData.mMessage) == false) {
            mCommentsView.setText(mShareData.mMessage);
            mCommentsView.setVisibility(View.VISIBLE);
        } else {
            if (isLocaiton) {
                setLocationUI();
            } else {
                mCommentsView.setVisibility(View.GONE);
            }
        }
        if (TextUtils.isEmpty(attachmentTitle) == false) {
            mAttachmentView.setText(attachmentTitle);
            mAttachmentView.setVisibility(View.VISIBLE);
        } else {
            mAttachmentView.setVisibility(View.GONE);
        }
    }

    private void setLocationUI() {
        if (TextUtils.isEmpty(mShareData.mLocaiton) == false) {
            int index = mShareData.mLocaiton.indexOf(GEO_TAG);
            if (index > 0) {
                String address = mShareData.mLocaiton.substring(index);
                if (TextUtils.isEmpty(address) == false) {
                    mCommentsView.setText(address.substring(GEO_TAG.length()));
                } else {
                    mCommentsView.setText(mContext.getResources().getString(R.string.location_at));
                }
            } else {
                mCommentsView.setText(mContext.getResources().getString(R.string.location_at));
            }
        } else {
            mCommentsView.setVisibility(View.GONE);
        }
    }

    private void setUI() {
        refreshStatusUI();

        String message = mContext.getResources().getString(R.string.delete_message);
        StringBuilder sb = new StringBuilder();        
        formatReceivers(sb);

        if (mShareData != null) {
            switch (mShareData.mType) {
                case ComposeShareData.LINK_TYPE:
                    mDeleteDialogMsg = String.format(message, mContext.getResources().getString(R.string.attach_link));
                    if (mShareData.mFavIcon == null) {
                        Bitmap default_icon = BitmapFactory.decodeResource(getResources(), R.drawable.share_link);
                        mImageIcon.setImageBitmap(default_icon);
                    } else {
                        mImageIcon.setImageBitmap(mShareData.mFavIcon);
                    }

                    if (TextUtils.isEmpty(mShareData.mTitle)) {
//                    	mShareData.mTitle = "No title";
                        mShareData.mTitle = "";
//                        mTitleContent.setVisibility(View.GONE);
                    }

                    setRecipientAndCommentUI(sb.toString(), mShareData.mTitle + " " + mShareData.mUrl, false);
                    break;
                case ComposeShareData.APK_TYPE:
                    mDeleteDialogMsg = String.format(message, mContext.getResources().getString(R.string.attach_app));
                    ImageRun imagerun = new ImageRun(null, mShareData.mUrl, 0);
                    imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_APK;
                    imagerun.noimage = false;
                    imagerun.addHostAndPath = true;
                    imagerun.setImageView(mImageIcon);
                    
                    if (TextUtils.isEmpty(mShareData.mVersion)) {
                        mShareData.mVersion = "null";
                    }

                    setRecipientAndCommentUI(sb.toString(), mShareData.mTitle + " " + mShareData.mVersion, false);
                    break;
                case ComposeShareData.PHOTO_TYPE:
                    mDeleteDialogMsg = String.format(message, mContext.getResources().getString(R.string.attach_photo));
                    setPhotoThumbnail();
                    setRecipientAndCommentUI(sb.toString(), mShareData.mTitle, false);
                    break;
                case ComposeShareData.VCARD_TYPE:
                    if (!TextUtils.isEmpty(mShareData.mVersion) && !TextUtils.isEmpty(mShareData.mTitle)) {
                        mDeleteDialogMsg = String.format(message, mContext.getResources().getString(R.string.attach_people));
                        Bitmap contactPhoto = null;
                        long photoId = Long.valueOf(mShareData.mVersion);
                        if (photoId > 0) {
                            contactPhoto = ContactUtils.getContactPhoto(mContext, Long.valueOf(mShareData.mUrl));
                        } else {
                            contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_icon);
                        }
                        mImageIcon.setImageBitmap(contactPhoto);

                        setRecipientAndCommentUI(sb.toString(), mShareData.mTitle, false);
                        contactPhoto = null;
                    }
                    break;
                case ComposeShareData.PURETEXT_TYPE:
                    setRecipientAndCommentUI(sb.toString(), "", true);
                    if (TextUtils.isEmpty(mShareData.mMessage)) {
                        if (TextUtils.isEmpty(mShareData.mLocaiton)) {
                            // do nothing, should not happen
                        } else {
                            mImageIcon.setBackgroundResource(R.drawable.location);
                        }
                    } else {
                        mImageIcon.setBackgroundResource(R.drawable.share_text);
                    }
                    break;
                case ComposeShareData.OTHER_TYPE:
                    mDeleteDialogMsg = String.format(message, mContext.getResources().getString(R.string.attach_file));
                    Bitmap icon;
                    if (mShareData.mVersion.contains("video/")) {
                        icon = ThumbnailUtils.createVideoThumbnail(mShareData.mUrl, Images.Thumbnails.MINI_KIND);
                        if (icon == null) {
                            // android can't screen shot this file, so use default folder icon.
                            icon = BitmapFactory.decodeResource(getResources(), R.drawable.folder);
                        }
                    } else {
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.folder);
                    }

                    mImageIcon.setImageBitmap(icon);
                    setRecipientAndCommentUI(sb.toString(), mShareData.mTitle, false);
                    break;
                default:
                    break;
            }
        }
    }

    private void setPhotoThumbnail() {
        BasicActivity.sWorker.post(new Runnable()
        {
            public void run()
            {
            	if(QiupuConfig.LOGD) Log.v(TAG, "setPhotoThumbnail begin");
                Bitmap photo = null;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                if (!TextUtils.isEmpty(mShareData.mUrl) && new File(mShareData.mUrl).exists()) 
                {
                	if(QiupuConfig.LOGD) Log.v(TAG, "setPhotoThumbnail mUrl is not null and file exists");
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(mShareData.mUrl, opts);

                    opts.inSampleSize = FileUtils.computeSampleSize(opts, -1, 100 * 100);
                    opts.inJustDecodeBounds = false;
                    opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
                    photo = BitmapFactory.decodeFile(mShareData.mUrl, opts);
                }

                if (photo == null && mShareData.mFile != null) 
                {
                	if(QiupuConfig.LOGD) Log.v(TAG, "setPhotoThumbnail photo is null");
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(mShareData.mFile);
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(fis, null, opts);

                        opts.inSampleSize = FileUtils.computeSampleSize(opts, -1, 100 * 100);
                        opts.inJustDecodeBounds = false;
                        opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
                        fis.close();
                        fis = new FileInputStream(mShareData.mFile);
                        photo = BitmapFactory.decodeStream(fis, null, opts);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fis != null) 
                        {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                final Bitmap image = photo;
                boolean postResult = ComposeShareItemView.this.post(new Runnable()
                {
                    public void run()
                    {
                    	if(QiupuConfig.LOGD) Log.v(TAG, "setPhotoThumbnail setImageBitmap");
                        mImageIcon.setImageBitmap(image);
                    }
                });
                
                if(QiupuConfig.LOGD) Log.v(TAG, "setPhotoThumbnail postResult="+postResult);
            }
        });
    }

    private View.OnClickListener mRemoveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (mShareData.mStatus) {
                case ComposeShareData.SUCCEED_STATUS:
                    delete();
                    break;
                case ComposeShareData.FAILED_STATUS:
                    DialogUtils.showRetryDialog(mContext,R.string.re_try_dialog_title, R.string.re_try_dialog_msg, 
                            R.string.re_try_button, R.string.delete, mRetryListener, mDeleteListener);
                    break;
                case ComposeShareData.UPLOADING_STATUS:
                    //do nothing
                    break;
                case ComposeShareData.WAITING_STATUS:
                    DialogUtils.showConfirmDialog(mContext, mDeleteDialogTitle, mDeleteDialogMsg, mDeleteListener);
                    break;
                default:
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener mRetryListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mRemove.setImageResource(R.drawable.menu_sync);
            if (null != mComposeListener && mComposeListener.get() != null) {
                mComposeListener.get().retryItem(mShareData);
            }
        }
    };

    private DialogInterface.OnClickListener mDeleteListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            delete();
        }
    };

    private void delete() {
        if (null != mComposeListener && mComposeListener.get() != null) {
            mComposeListener.get().deleteItem(mShareData);
        }
        
    }

    @Override
    public String getText() {
        return mTitle != null ? getTitle() : "";
    }

    public void attachActionListener(WeakReference<ComposeActionListener>  composeActionListener) {
        mComposeListener = composeActionListener;
    }

    public void refreshStatusUI() {
        if (mShareData != null) {
//            Log.d(TAG, "-------------->>>> setUI()  mShareData.mStatus = "  +mShareData.mStatus);
            switch(mShareData.mStatus)
            {
            case ComposeShareData.WAITING_STATUS:
                mRemove.setImageResource(R.drawable.delete_icon);
                break;
            case ComposeShareData.FAILED_STATUS:
                mRemove.setImageResource(R.drawable.send_failed_icon);
                break;
            case ComposeShareData.UPLOADING_STATUS:
                mRemove.setImageResource(R.drawable.send_icon);
                break;
            case ComposeShareData.SUCCEED_STATUS:
                mRemove.setImageResource(R.drawable.send_ok_icon);
                Log.d("Qiupu.QiupuComposeActivity", "composer: shared end:" + this);
                break;
            }
        }
    }    

}
