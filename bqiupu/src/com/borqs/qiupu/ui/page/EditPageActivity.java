package com.borqs.qiupu.ui.page;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import twitter4j.PageInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.PageActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.ToastUtil;

public class EditPageActivity extends BasicActivity {

	private static final String TAG = "EditPageActivity";
	
	private PageInfo mPage;
	private EditText mPage_name;
	private EditText mPage_name_en;
	private EditText mPage_description;
	private EditText mPage_description_en;
	private EditText mPage_location;
	private EditText mPage_location_en;
	
	private EditText mPage_phone;
	private EditText mPage_email;
	private EditText mPage_fax;
	private EditText mPage_website;
	
	private ImageView mCoverView;
	private Bitmap photo ;
	private ProgressDialog mprogressDialog;
	private Uri imageUri = Uri.fromFile(new File(QiupuHelper.getTmpCachePath()+"screenshot.png"));
	
	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_page_ui);
		setHeadTitle(R.string.edit_page);
		showRightActionBtn(false);
		showRightTextActionBtn(true);
		overrideRightTextActionBtn(R.string.label_save, editClickListener);
		
		mPage = (PageInfo) getIntent().getSerializableExtra(PageInfo.PAGE_INFO);
		
		mPage_name = (EditText) findViewById(R.id.page_name);
		mPage_name_en = (EditText) findViewById(R.id.page_name_en);
		
		mPage_description = (EditText) findViewById(R.id.page_description);
		mPage_description_en = (EditText) findViewById(R.id.page_description_en);
		mPage_location = (EditText) findViewById(R.id.page_location);
		mPage_location_en = (EditText) findViewById(R.id.page_location_en);
		mPage_phone = (EditText) findViewById(R.id.page_phone);
		mPage_email = (EditText) findViewById(R.id.page_email);
		mPage_fax = (EditText) findViewById(R.id.page_fax);
		mPage_website = (EditText) findViewById(R.id.page_website);
		
		mCoverView = (ImageView) findViewById(R.id.page_cover);
		View cover_rl = findViewById(R.id.cover_rl);
		cover_rl.setOnClickListener(editCoverListener);
			
		initUI();
	}
	
	private void initUI() {
		if(mPage != null ) {
			mPage_name.setText(mPage.name);
			mPage_name_en.setText(mPage.name_en);
			mPage_location.setText(mPage.address);
			mPage_location_en.setText(mPage.address_en);
			mPage_description.setText(mPage.description);
			mPage_description_en.setText(mPage.description_en);
			
			mPage_phone.setText(mPage.tel);
			mPage_email.setText(mPage.email);
			mPage_fax.setText(mPage.fax);
			mPage_website.setText(mPage.website);
			shootImageRunner(mPage.cover_url, mCoverView);
			
		}
	}
	
	View.OnClickListener editClickListener = new View.OnClickListener() {
        public void onClick(View v) {
                HashMap<String, String> tmpMap = getEditPageMap();
                if(tmpMap != null) {
                    if(tmpMap.size() > 0) {
                        tmpMap.put("page_id", String.valueOf(mPage.page_id));
                        editPage(tmpMap);
                    }else {
                        EditPageActivity.this.finish();
                    }
            }
        }
    };
    
    private HashMap<String, String> getEditPageMap() {
		final String pageName = mPage_name.getText().toString().trim();
		final String pageName_en = mPage_name_en.getText().toString().trim();
		
		if(TextUtils.isEmpty(pageName) && TextUtils.isEmpty(pageName_en)) {
			ToastUtil.showShortToast(this, mHandler, R.string.name_isnull_toast);
			mPage_name.requestFocus();
			return null;
		}
		
		final String des = mPage_description.getText().toString().trim();
		final String des_en = mPage_description_en.getText().toString().trim();
		
		final String location =  mPage_location.getText().toString().trim();
		final String location_en =  mPage_location_en.getText().toString().trim();
		
		final String phone = mPage_phone.getText().toString().trim();
		final String email = mPage_email.getText().toString().trim();
		final String fax = mPage_fax.getText().toString().trim();
		final String website = mPage_website.getText().toString().trim();
		
		
//		mCopyPage = mPage.clone();
//		mCopyPage.name = pageName;
//		mCopyPage.name_en = pageName_en;
//		mCopyPage.description = des;
//		mCopyPage.description_en = des_en;
//		mCopyPage.address = location;
//		mCopyPage.address_en = location_en;
//		mCopyPage.tel = phone;
//		mCopyPage.email = email;
//		mCopyPage.fax = fax;
//		mCopyPage.website = website;
		
		HashMap<String , String> editMap = new HashMap<String, String>();
		if(!(pageName.equals(mPage.name))) {
			editMap.put("name", pageName);
		}
		if(!(pageName_en.equals(mPage.name_en))) {
			editMap.put("name_en", pageName_en);
		}
		if(!(des.equals(mPage.description))) {
			editMap.put("description", des);
		}
		if(!(des_en.equals(mPage.description_en))) {
			editMap.put("description_en", des_en);
		}
		
		if(!(location.equals(mPage.address))) {
			editMap.put("address", location);
		}
		if(!(location_en.equals(mPage.address_en))) {
			editMap.put("address_en", location_en);
		}
		if(!phone.equals(mPage.tel)) {
			editMap.put("tel", phone);
		}
		if(!email.equals(mPage.email)) {
			editMap.put("email", email);
		}
		if(!fax.equals(mPage.fax)) {
			editMap.put("fax", fax);
		}
		if(!website.equals(mPage.website)) {
			editMap.put("website", website);
		}
		
		return editMap;
	}
    
	private final static int EDIT_PAGE = 101;
	private final static int EDIT_PAGE_END = 102;
	private final static int EDIT_PAGE_COVER_END = 103;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EDIT_PAGE: {
            	break;
            }
            case EDIT_PAGE_END: {
                try {
                    dismissDialog(DIALOG_SET_USER_PROCESS);
                } catch (Exception ne) {
                }
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret == true) {
                	QiupuHelper.updatePageActivityUI(mPage);
                    showOperationSucToast(true);
                    EditPageActivity.this.finish();
                } else {
                    showOperationFailToast("", true);
                }
                break;
            }
            case EDIT_PAGE_COVER_END: {
            	try {
                    mprogressDialog.dismiss();
                    mprogressDialog = null;
                } catch (Exception ne) { }
                if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
//                    mCurrentPhotoFile.delete();
                	if(new File(QiupuHelper.getTmpCachePath()+"screenshot.png").exists()) {
                		new File(QiupuHelper.getTmpCachePath()+"screenshot.png").delete();
                	}
                    mCoverView.setImageBitmap(photo);
                    
                    QiupuHelper.updatePageActivityUI(mPage);
//                    mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget(); 
                }else {
                    Log.d(TAG, "edit profile iamge end ");
                    ToastUtil.showShortToast(EditPageActivity.this, mHandler, R.string.toast_update_failed);
                }
            	break;
            }
            }
        }
    }
    
    boolean inEditPageInfo;
    Object mLockEditPageInfo = new Object();
    public void editPage(final HashMap<String, String> map) {
        if (inEditPageInfo == true) {
            ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
            return;
        }
        
        synchronized (mLockEditPageInfo) {
        	inEditPageInfo = true;
        }
        
        showDialog(DIALOG_SET_USER_PROCESS);
        asyncQiupu.editPage(AccountServiceUtils.getSessionID(), map, new TwitterAdapter() {
            public void editPage(PageInfo info) {
                Log.d(TAG, "finish editPage="  + info.toString());
                
                mPage = info;
                if(info != null) {
                	orm.insertOnePage(info);
                }
                
                Message msg = mHandler.obtainMessage(EDIT_PAGE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                synchronized (mLockEditPageInfo) {
                	inEditPageInfo = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockEditPageInfo) {
                	inEditPageInfo = false;
                }
                Message msg = mHandler.obtainMessage(EDIT_PAGE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    Object mEditPageCoverLock = new Object();
    boolean inEditPageCoverProcess;
    private void editPageCoverImage(File file){
        synchronized(mEditPageCoverLock)
        {
            if(inEditPageCoverProcess == true)
            {
                ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
                return ;
            }
        }
        
        showProcessDialog(R.string.edit_profile_update_dialog, false, true, true);
        
        synchronized(mEditPageCoverLock)
        {
        	inEditPageCoverProcess = true;          
        }
        asyncQiupu.editPageCover(AccountServiceUtils.getSessionID(), mPage.page_id, file, new TwitterAdapter() {
            public void editPageCover(PageInfo info) {
                Log.d(TAG, "finish edit page cover");
                //TODO  need update url to db 
                if(info != null) {
                	orm.insertOnePage(info);
                }
                
                Message msg = mHandler.obtainMessage(EDIT_PAGE_COVER_END);               
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.sendToTarget();
                synchronized(mEditPageCoverLock)
                {
                	inEditPageCoverProcess = false;         
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                
                synchronized(mEditPageCoverLock)
                {
                	inEditPageCoverProcess = false;         
                }
                TwitterExceptionUtils.printException(TAG, "editPageCover, server exception:", ex, method);

                Message msg = mHandler.obtainMessage(EDIT_PAGE_COVER_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    private void shootImageRunner(String photoUrl,ImageView img) {
    	ImageRun imagerun = new ImageRun(null, photoUrl, 0);
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.width = getResources().getDisplayMetrics().widthPixels;
		imagerun.height = imagerun.width;
		imagerun.noimage = true;
		imagerun.addHostAndPath = true;
		imagerun.setImageView(img);
		imagerun.post(null);
	}
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
    	if (resultCode != Activity.RESULT_OK) {
    		return ;
    	}
        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA: {
            	 Log.d(TAG, "CHOOSE_BIG_PICTURE: data = " + data);//it seems to be null
            	 
//            	 photo = data.getParcelableExtra("data");
            	 Uri originalUri = data.getData();
            	 if(originalUri != null) {
            		 try {
            			 tryCropProfileImage(originalUri);
            		 } catch (Exception e) {
            			 e.printStackTrace();
            		 }
             	}else {
             		if(imageUri != null){
             			photo = decodeUriAsBitmap(imageUri);//decode bitmap
             		}
             		try {
             			
             			editPageCoverImage(new File(QiupuHelper.getTmpCachePath()+"screenshot.png"));
             		} catch (Exception e) {
             			e.printStackTrace();
             		}
             	}
                break;  
            }  
            case CAMERA_WITH_DATA: {
                tryCropProfileImage(imageUri);
                break;  
            }  
        }
    }
    
    private Bitmap decodeUriAsBitmap(Uri uri){
    	 Bitmap bitmap = null;
    	 try {
    	  bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
    	 } catch (FileNotFoundException e) {
    	  e.printStackTrace();
    	  return null;
    	 }
    	 return bitmap;
    	}
    
    View.OnClickListener editCoverListener = new View.OnClickListener() {
    	
    	@Override
    	public void onClick(View v) {
    		String[] items = new String[] {
    				getString(R.string.take_photo),
    				getString(R.string.phone_album) };
    		DialogUtils.showItemsDialog(EditPageActivity.this, getResources().getString(R.string.select_cover_photo), 0, items,
    				ChooseEditImageItemClickListener);
    	}
    };
    
    DialogInterface.OnClickListener ChooseEditImageItemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == 0) {
                doTakePhoto();// from camera  
            } else {
                doPickPhotoFromGallery();// from  gallery
            }
        }
    };
    
    private void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact  
        	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//action is capture
        	intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        	startActivityForResult(intent, CAMERA_WITH_DATA);//or TAKE_SMALL_PICTURE
        	
        }
        catch (ActivityNotFoundException e) {}
    }
    
    private void doPickPhotoFromGallery() {
        try {
        	cropImage();
        }
        catch (ActivityNotFoundException e) {}
    }
    
    private void tryCropProfileImage(Uri url) {
        try {
            // start gallery to crop photo
        	getCropImageIntent(url);
        } catch (Exception e) {
        	Log.d(TAG, "tryCropProfileImage: " + e.getMessage());
        }
    }
    
    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
    	mprogressDialog = DialogUtils.createProgressDialog(this, 
    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
    	mprogressDialog.show();    	
    }
    
    private void getCropImageIntent(Uri url) { 
    	Intent intent = new Intent("com.android.camera.action.CROP");  
    	intent.setDataAndType(url, "image/*");  
    	intent.putExtra("crop", "true");  
    	intent.putExtra("aspectX", 5);  
    	intent.putExtra("aspectY", 2);  
    	intent.putExtra("outputX", 600);  
    	intent.putExtra("outputY", 240);  
    	intent.putExtra("scale", true);
    	intent.putExtra("return-data", false);  
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
    	intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
    	startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
    }
    
    private void cropImage() {
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
    	intent.setType("image/*");
    	intent.putExtra("crop", "true");
    	intent.putExtra("aspectX", 5);
    	intent.putExtra("aspectY", 2);
    	intent.putExtra("outputX", 600);
    	intent.putExtra("outputY", 240);
    	intent.putExtra("scale", true);
    	intent.putExtra("return-data", false);
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
    	intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
    	startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
    }
}
