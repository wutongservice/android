package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import com.borqs.common.util.UserTask;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import twitter4j.QiupuAlbum;
import twitter4j.QiupuPhoto;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.PhotoGridAdapter;
import com.borqs.common.view.TwoWayAdapterView;
import com.borqs.common.view.TwoWayGridView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class GridPicActivity extends BasicNavigationActivity {
//  private final GridPicActivity thiz = this;
    private static final String TAG = "GridPicActivity";

    private static final boolean LOW_PERFORMANCE = true || QiupuConfig.LowPerformance;

//  ArrayList<PlacePicture> parser_list = new ArrayList<PlacePicture>();
    private TwoWayGridView gridview;
    private int page = 0;
//  private int count = 50;
    private long album_ids;
    private PhotoGridAdapter photoAdapter;
    int  getLastVisiblePosition = 0;
    int lastVisiblePositionY = 0;
    int photo_count = 0;
    long uid = 0;
    private int selection_index = 0;
    QiupuAlbum mAlbum = null;
    Cursor mPhotosCursor;
    private final int REQ_CODE = 0;
//    private ContentObserver mObserver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        enableLeftNav(getIntent().getBooleanExtra("supportLeftNavigation", false));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_pic_activity);
        
        
//      mObserver = new ContentObserver(new Handler()) {
//
//          @Override
//          public void onChange(boolean selfChange) {
//                if (LOW_PERFORMANCE) Log.d(TAG, "onChange, enter");
//                refreshPhotosCursor();
////                photoAdapter = new PhotoGridAdapter(thiz,mPhotosCursor);
////                gridview.setAdapter(photoAdapter);
//              photoAdapter.changeCursor(mPhotosCursor);
//              photoAdapter.notifyDataSetChanged();
//                if (LOW_PERFORMANCE) Log.d(TAG, "onChange, exit");
//          }
//      };
//      getContentResolver().registerContentObserver(QiupuORM.QIUPU_PHOTO_URI, true, mObserver);
        gridview = (TwoWayGridView)findViewById(R.id.gridview);
        uid = getIntent().getLongExtra("uid",0);
        final Context activity = this;
        gridview.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(TwoWayAdapterView parent, View view,
                    int position, long id) {
                Intent intent = new Intent(activity,PhotosViewActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("current_item", position);
                intent.putExtra("album_id", album_ids);
                intent.putExtra("nick_name",getIntent().getStringExtra("nick_name"));
                startActivityForResult(intent, REQ_CODE);
//                IntentUtil.startPhotosViewIntent(activity, uid, position,album_ids,getIntent().getStringExtra("nick_name"));
            }

        });
         
        showLeftActionBtn(true);
        showMiddleActionBtn(false);
        showRightActionBtn(false);
        album_ids = getIntent().getLongExtra("album_id", 0);
        overrideLeftActionBtn(R.drawable.actionbar_icon_refresh_normal, refreshListener);
        refreshPhotosCursor();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mAlbum = QiupuORM.queryQiupuAlbumById(this, album_ids);

        final int preferSize = Math.min(dm.widthPixels, dm.heightPixels)/3;
        if(mAlbum != null && mAlbum.album_type == 0) {
            photoAdapter = new PhotoGridAdapter(this, mPhotosCursor, preferSize,true);
        }else {
            photoAdapter = new PhotoGridAdapter(this, mPhotosCursor, preferSize,false);
        }
        gridview.setAdapter(photoAdapter);
        if(mAlbum != null) {
            setHeadTitle(mAlbum.title);
            String username = getIntent().getStringExtra("nick_name");
//          if(QiupuConfig.isPublicCircleProfile(mAlbum.user_id)) {
//              username = orm.queryPublicCircleNamesByIds(String.valueOf(mAlbum.user_id));
//          }else {
//              username = orm.getUserName(mAlbum.user_id);
//          }
            setSubTitle(username);
            if(getIntent().getBooleanExtra("fromStream", true)) {
                getAlbum();
            }else {
                if(mAlbum.have_expired || mPhotosCursor.getCount() != mAlbum.photo_count) {
                    getPhotosByAlbumId();
                }
            }
        }else {
            getAlbum();
        }
        onConfigurationChanged(getResources().getConfiguration());
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == REQ_CODE) {
            refreshPhotosCursor();
            photoAdapter.changeCursor(mPhotosCursor);
            photoAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void getAlbum() {
        if (LOW_PERFORMANCE) Log.d(TAG, "getAlbum, enter");
        synchronized (mInfoLock) {
            if (inInfoProcess == true) {
                Log.d(TAG, "in loading info data");
                return;
            }
        }

        synchronized (mInfoLock) {
            inInfoProcess = true;
        }
        begin();
        if (LOW_PERFORMANCE) Log.d(TAG, "getAlbum, request ...");
        asyncQiupu.getAlbum(AccountServiceUtils.getSessionID(), album_ids, uid,
                false, new TwitterAdapter() {
                    @Override
                    public void getAlbum(QiupuAlbum album) {
//                        albumIsrefresh = true;
////                        if (LOW_PERFORMANCE) Log.d(TAG, "getAlbum, result enter");
////                        orm.insertQiupuAlbumInfo(album,true);
////                        if (LOW_PERFORMANCE) Log.d(TAG, "getAlbum, result inserted");
//                        mAlbum = album.clone();
//                        album = null;
//                        Message mds = mHandler.obtainMessage(GET_ALBUM_SUCCESS);
//                        mds.getData().putBoolean(RESULT, true);
//                        mHandler.sendMessage(mds);
//                        synchronized (mInfoLock) {
//                            inInfoProcess = false;
//                        }
//                        if (LOW_PERFORMANCE) Log.d(TAG, "getAlbum, result exit");
                        
                        if(mAlbum != null) {
                            album.have_expired = album.compareTo(mAlbum) == 1;
                        }
                        new InsertAsyncTask().execute(album);
                        mAlbum = album;
                        Message mds = mHandler.obtainMessage(GET_ALBUM_SUCCESS);
                        mds.getData().putBoolean(RESULT, true);
                        mHandler.sendMessage(mds);
                        synchronized (mInfoLock) {
                            inInfoProcess = false;
                        }
                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        if (LOW_PERFORMANCE) Log.d(TAG, "getAlbum, exception enter");
                        Message mds = mHandler.obtainMessage(GET_ALBUM_FAILED);
                        mds.getData().putBoolean(RESULT, false);
                        mds.getData().putString(ERROR_MSG, ex.getMessage());
                        mHandler.sendMessage(mds);
                        synchronized (mInfoLock) {
                            inInfoProcess = false;
                        }
                        if (LOW_PERFORMANCE) Log.d(TAG, "getAlbum, exception exit");
                    }
                });
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        selection_index = gridview.getFirstVisiblePosition();
        super.onConfigurationChanged(newConfig);
        gridview.setSelection(selection_index);
    }


    View.OnClickListener refreshListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            page = 0;
            getPhotosByAlbumId();
        }
    };

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
                case GET_PHOTO_SUCCESS:
                    page++;
                    refreshPhotosCursor();
                    photoAdapter.changeCursor(mPhotosCursor);
                    photoAdapter.notifyDataSetChanged();
                    break;
                case GET_PHOTO_FAILED:  
                    showCustomToast(R.string.loading_failed);
                    break;
                    
                case GET_ALBUM_SUCCESS:
//                  setHeadTitle();
                    // albums
                	if(mPhotosCursor == null || mAlbum == null) return;
                    if(mAlbum.have_expired || mPhotosCursor.getCount() != mAlbum.photo_count) {
                        getPhotosByAlbumId();
                    }
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
    private void getPhotosByAlbumId()
    {
        if (LOW_PERFORMANCE) Log.d(TAG, "getPhotosByAlbumId, enter");
        synchronized (mInfoLock) {
            if (inInfoProcess == true) {
                Log.d(TAG, "in loading info data");
                return;
            }
        }

        synchronized (mInfoLock) {
            inInfoProcess = true;
        }
        begin();
        if (LOW_PERFORMANCE) Log.d(TAG, "getPhotosByAlbumId, requesting ...");
        final Context activity = this;
        asyncQiupu.getPhotosByAlbumId(AccountServiceUtils.getSessionID(), album_ids, page, mAlbum.photo_count, new TwitterAdapter()
        {
            @Override
            public void getPhotosByAlbumId(ArrayList<QiupuPhoto> photos) {
                if (LOW_PERFORMANCE) Log.d(TAG, "getPhotosByAlbumId, result enter");
//              if(page == 0) {
//                  mPhotos.clear();
//              }
//              mPhotos.addAll(photos);
//              orm.insertQiupuPhotoList(mPhotos, uid, album_ids);+
                final ArrayList<QiupuPhoto> mPhotos = new ArrayList<QiupuPhoto>();
                mPhotos.addAll(photos);
                QiupuORM.sWorker.post(new Runnable() {

                    @Override
                    public void run() {
                        orm.updateQiupuAlbum(album_ids, false);
                        orm.bullinsertQiupuPhoto(mPhotos, uid, album_ids);
                        synchronized (mInfoLock) {
                            inInfoProcess = false;
                        }
                        Message mds = mHandler.obtainMessage(GET_PHOTO_SUCCESS);
                        mds.getData().putBoolean(RESULT,true);
                        mHandler.sendMessage(mds);
                    }
                    
                });
//                if (LOW_PERFORMANCE) Log.d(TAG, "getPhotosByAlbumId, result inserted");
//              synchronized (mInfoLock) {
//                  inInfoProcess = false;
//              }
//              mPhotosCursor = QiupuORM.queryQiupuPhotosCursor(activity, uid,album_ids);
//              Message mds = mHandler.obtainMessage(GET_PHOTO_SUCCESS);
//              mds.getData().putBoolean(RESULT,true);
//              mHandler.sendMessage(mds);
//                if (LOW_PERFORMANCE) Log.d(TAG, "getPhotosByAlbumId, result exit");
                
            }
            
            public void onException(TwitterException ex,TwitterMethod method) 
            {
                if (LOW_PERFORMANCE) Log.d(TAG, "getPhotosByAlbumId, exception enter");
                int error_code = ex.getStatusCode();
                Message mds = mHandler.obtainMessage(GET_PHOTO_FAILED);
                mds.getData().putBoolean(RESULT,false);
                mds.getData().putString(ERROR_MSG,ex.getMessage());
                mHandler.sendMessage(mds);
                if (LOW_PERFORMANCE) Log.d(TAG, "getPhotosByAlbumId, exception exit");
            }
        });   
    }
    
    @Override
    protected void onDestroy() {
        if(mPhotosCursor != null && !mPhotosCursor.isClosed()) {
            mPhotosCursor.close();
            mPhotosCursor = null;
        }
        super.onDestroy();
    }
    
    
    class InsertAsyncTask extends UserTask<QiupuAlbum,Void,Void> {
        @Override
        public Void doInBackground(QiupuAlbum... params) {
            orm.insertQiupuAlbumInfo(params[0],true);
            return null;
        }
        
    }

    private void refreshPhotosCursor() {
        if (LOW_PERFORMANCE) Log.d(TAG, "refreshPhotosCursor, enter");
        mPhotosCursor = QiupuORM.queryQiupuPhotosCursor(this, uid, album_ids);
        if (LOW_PERFORMANCE) Log.d(TAG, "refreshPhotosCursor, exit");
    }
}