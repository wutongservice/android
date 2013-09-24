package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import com.borqs.qiupu.ui.BasicNavigationActivity;
import twitter4j.QiupuAlbum;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.AlbumGridLayoutAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class AlbumActivity extends BasicNavigationActivity {
	private final AlbumActivity thiz = this;
	private static final String TAG = "AlbumActivity";
    private long uid = 0;
    private AlbumGridLayoutAdapter gridAdapter;
    private ProgressBar  progressBar1;
    private TextView tv_msg;
private GridView album_grid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
    	boolean isSupportLeftNavigation  = getIntent().getBooleanExtra("supportLeftNavigation", false);
        enableLeftNav(isSupportLeftNavigation);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_grid_view);
        progressBar1 = (ProgressBar)findViewById(R.id.progressBar1);
        tv_msg = (TextView)findViewById(R.id.tv_msg);
//        showMiddleActionBtn(false);
//        showRightActionBtn(false);
        
        overrideRightActionBtn(R.drawable.actionbar_icon_refresh_normal, refreshListener);
//        overrideSlideIcon(R.drawable.ic_ab_back_holo_dark);
//		showSlideToggle(overrideSlideToggleClickListener);
        album_grid = (GridView)findViewById(R.id.album_grid);
        uid = getIntent().getLongExtra("uid", 0);
        String username = null;
//        if(QiupuConfig.isPublicCircleProfile(uid)) {
//        	username = orm.queryPublicCircleNamesByIds(String.valueOf(uid));
//        }else {
//        	username = orm.getUserName(uid);
//        }
        username = getIntent().getStringExtra("nick_name");
        setHeadTitle(R.string.home_album);
        setSubTitle(username);
        mAlbums = QiupuORM.queryQiupuAlbums(thiz, uid);
        gridAdapter = new AlbumGridLayoutAdapter(thiz,480,mAlbums);
        album_grid.setAdapter(gridAdapter);
        album_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
//				Intent intent = new Intent(thiz,GridPicActivity.class);
//				intent.putExtra("album_ids", mAlbums.get(position).album_id);
//				intent.putExtra("album_name", mAlbums.get(position).title);
//				intent.putExtra("photo_count", mAlbums.get(position).photo_count);
//				intent.putExtra("uid", uid);
//				startActivity(intent);
				
				IntentUtil.startGridPicIntent(thiz, mAlbums.get(position).album_id, uid,getIntent().getStringExtra("nick_name"),false);
				
			}
        	
		});
        getAllAlbums();

   	 onConfigurationChanged(getResources().getConfiguration());
   	}
   	
   	@Override
   	public void onConfigurationChanged(Configuration newConfig) {
   		super.onConfigurationChanged(newConfig);
   	    if (getResources().getConfiguration().orientation !=  Configuration.ORIENTATION_LANDSCAPE)
   	    {
   	      if (getResources().getConfiguration().orientation ==  Configuration.ORIENTATION_PORTRAIT)
   	      {
   	    	album_grid.setNumColumns(2);
   	      }
   	    }
   	    else
   	    {
   	    	album_grid.setNumColumns(4);
   	    }
   	}
    
   	View.OnClickListener overrideSlideToggleClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			finish();
		}
	};
   	
    View.OnClickListener refreshListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			getAllAlbums();
		}
	};
    @Override
    protected void loadRefresh() {
    	// TODO Auto-generated method stub
    	super.loadRefresh();
    };
    

    @Override
	protected void createHandler() {
		mHandler = new AlbumsHandler();
	}
    
    final int GET_ALBUM_SUCCESS           = 0;
    final int GET_ALBUM_FAILED           = 1;
	
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
        	progressBar1.setVisibility(View.GONE);
            switch(msg.what)
            {
                case GET_ALBUM_SUCCESS:
                	if(mAlbums == null) {
                		return ;
                	}
                	gridAdapter.notifyDataSetChanged();
                	if(mAlbums.size() > 0) {
                		tv_msg.setVisibility(View.GONE);
                	}else {
                		tv_msg.setVisibility(View.VISIBLE);
                	}
                	break;
                case GET_ALBUM_FAILED:    
                    showCustomToast(R.string.loading_failed);
            	    break;
            }
        }
    }



//    public long getUserId(){
//        return mUserid;
//    }



	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAlbums.clear();
		mAlbums = null;
	}


	ArrayList<QiupuAlbum> mAlbums = new ArrayList<QiupuAlbum>();
	private void getAllAlbums()
	{
		begin();
		tv_msg.setVisibility(View.GONE);
		progressBar1.setVisibility(View.VISIBLE);
		asyncQiupu.getAllAlbums(AccountServiceUtils.getSessionID(), uid,false, new TwitterAdapter()
		{
			@Override
			public void getAllAlbums(ArrayList<QiupuAlbum> albums) {
				if(mAlbums != null) {
				    
				    for(int i=0;i<albums.size();i++) {
				        for(int j=0;j<mAlbums.size();j++) {
				            if(albums.get(i).album_id == mAlbums.get(j).album_id) {
				                albums.get(i).have_expired = albums.get(i).compareTo(mAlbums.get(j)) == 1;
				            }
				            
				        }
				    }
					mAlbums.clear();
					mAlbums.addAll(albums);
					
				}
				orm.insertQiupuAlbumList(albums,uid);
				Log.d(TAG,"finish getAllAlbums="+albums.size());
				Message mds = mHandler.obtainMessage(GET_ALBUM_SUCCESS);
				mds.getData().putBoolean(RESULT,true);
				mHandler.sendMessage(mds);
                
			}
			
			public void onException(TwitterException ex,TwitterMethod method) 
			{
				int error_code = ex.getStatusCode();
				Message mds = mHandler.obtainMessage(GET_ALBUM_FAILED);
    			mds.getData().putBoolean(RESULT,false);
    			mds.getData().putString(ERROR_MSG,ex.getMessage());
    			mHandler.sendMessage(mds);
			}
		});   
	}
	
	@Override
	protected void uiLoadBegin() {
		showProgressBtn(true);
        showRightActionBtn(false);
        
        if(isUsingActionBar() && getActionBar() != null)
        {
        	setProgress(500);
        }
	}
	
	@Override
	protected void uiLoadEnd() {
		showProgressBtn(false);
        showRightActionBtn(true);
        
        if(isUsingActionBar() && getActionBar() != null)
        {
        	setProgress(10000);
        }
	}
//	@Override
//	public void setLeftMenuPosition() {
//		mPosition = LeftMenuMapping.getPositionForActivity(this);
//		mTitle = getString(R.string.home_album);
//	}
	
}