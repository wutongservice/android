package com.borqs.qiupu.ui.bpc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.borqs.common.adapter.PhotoSelectAdapter;
import com.borqs.common.view.TwoWayAdapterView;
import com.borqs.common.view.TwoWayGridView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicNavigationActivity;

public class PhotoSelectActivity extends BasicNavigationActivity {
    private static final String TAG = "PhotoSelectActivity";

    private static final boolean LOW_PERFORMANCE = true || QiupuConfig.LowPerformance;

    private TwoWayGridView gridview;
    private PhotoSelectAdapter photoAdapter;
    private int selection_index = 0;
    Cursor mPhotosCursor;
    private final int REQ_CODE = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        enableLeftNav(getIntent().getBooleanExtra("supportLeftNavigation", false));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_select_activity);
        
        
        gridview = (TwoWayGridView)findViewById(R.id.gridview);
        final Context activity = this;
        gridview.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(TwoWayAdapterView parent, View view,
                    int position, long id) {
//                Intent intent = new Intent(activity,PhotosViewActivity.class);
//                intent.putExtra("uid", uid);
//                intent.putExtra("current_item", position);
//                intent.putExtra("album_id", album_ids);
//                intent.putExtra("nick_name",getIntent().getStringExtra("nick_name"));
//                startActivityForResult(intent, REQ_CODE);
            }

        });
        
         
        showLeftActionBtn(true);
        showMiddleActionBtn(false);
        showRightActionBtn(false);
        overrideLeftActionBtn(R.drawable.attach_camera, refreshListener);
        scanImageData();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        final int preferSize = Math.min(dm.widthPixels, dm.heightPixels)/3;
        photoAdapter = new PhotoSelectAdapter(this, mPhotosCursor, preferSize);
        gridview.setAdapter(photoAdapter);
        setHeadTitle("选择照片");
        onConfigurationChanged(getResources().getConfiguration());
    }
    
    View.OnClickListener refreshListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated methrefreshListenerod stub
			
		}
	};
    
//	private void scanImageData() {
//		if(mPhotosCursor != null && !mPhotosCursor.isClosed()) {
//			mPhotosCursor.close();
//		}
//		ContentResolver contentResolver = getContentResolver();
//		String orderBy = MediaStore.Images.Thumbnails.IMAGE_ID + " asc";
//		String[] IMG_PROJECTION = { MediaStore.Images.Thumbnails._ID };
//		mPhotosCursor  = contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, 
//				IMG_PROJECTION,null, null,orderBy );
//	}
    private void scanImageData() {
		if(mPhotosCursor != null && !mPhotosCursor.isClosed()) {
			mPhotosCursor.close();
		}
		ContentResolver contentResolver = getContentResolver();
        String[] IMG_PROJECTION = { MediaStore.Images.Media.DATA,MediaStore.Images.Media._ID };
        mPhotosCursor  = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
        		IMG_PROJECTION,null, null,MediaStore.Images.Media._ID + "  desc");
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == REQ_CODE) {
//            refreshPhotosCursor();
            photoAdapter.changeCursor(mPhotosCursor);
            photoAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        selection_index = gridview.getFirstVisiblePosition();
        super.onConfigurationChanged(newConfig);
        gridview.setSelection(selection_index);
    }



//    @Override
//    public void setLeftMenuPosition() {
//        // TODO Auto-generated method stub
//
//    }

    @Override
    protected void createHandler() {
        mHandler = new AlbumsHandler();
    }
    
    final int GET_ALBUM_SUCCESS           = 0;
    final int GET_ALBUM_FAILED            = 1;
    final int GET_PHOTO_SUCCESS           = 2;
    final int GET_PHOTO_FAILED            = 3;
    
    private class AlbumsHandler extends Handler 
    {
        public AlbumsHandler()
        {
            super();            
            Log.d(TAG, "new AlbumsHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            end();
            switch(msg.what)
            {
                case GET_PHOTO_FAILED:  
                    showCustomToast(R.string.loading_failed);
                    break;
                    
                case GET_ALBUM_FAILED:
//                  progressBar1.setVisibility(View.GONE);
                    end();
                    showCustomToast(R.string.loading_failed);
                    break;
            }
        }
    }
    
//  ArrayList<QiupuPhoto> mPhotos = new ArrayList<QiupuPhoto>();
    Object mInfoLock = new Object();
    boolean inInfoProcess;
    
    @Override
    protected void onDestroy() {
        if(mPhotosCursor != null && !mPhotosCursor.isClosed()) {
            mPhotosCursor.close();
            mPhotosCursor = null;
        }
        super.onDestroy();
    }
    
    

}