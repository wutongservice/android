package com.borqs.qiupu.ui.bpc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.borqs.common.util.UserTask;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import twitter4j.QiupuAlbum;
import twitter4j.QiupuPhoto;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.RefreshPostProfileImageListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.common.view.ImageViewTouch.ImageViewTouchListener;
import com.borqs.common.view.SuperViewPager;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.PhotoFragment;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.QiupuCommentsPicActivity;
import com.borqs.qiupu.util.FileUtils;
import com.borqs.qiupu.util.MediaScannerNotifier;

public class PhotosViewActivity extends BasicNavigationActivity implements ImageViewTouchListener,PhotoFragment.ClickListener{
	private final PhotosViewActivity thiz = this;
	private static final String TAG = "PhotosViewActivity";
	private static final int REQ_COMMENT = 001;
	private int current_item;
	SuperViewPager mPager;
	TextView tv_photo_num;
	TextView tv_caption;
	PlacePicsAdapter mAdapter;
	// private TextView textTitle;
	ArrayList<QiupuPhoto> mPhotos = new ArrayList<QiupuPhoto>();
	private int page = 0;
//	private int count = 50;
	private long album_id;
	private long photo_id;
	private long uid = 0;
	QiupuAlbum mAlbum = null;
	private boolean fromStreamItem = false;
	private View progressBar1;
	private View progressbar_layout;
	TextView text_photo_comment;
	private View layout_titleBar;
	private View button_photo_comment;
	private View layout_bottom;
	private View img_slide;
	private TextView sub_head_title;
	private TextView head_title;
	private ImageView head_action_left;
	private ImageView head_action_middle;
	private ImageView head_action_right;
	private ImageView head_action_reshare;
	private boolean isRefresh; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.photos_view_activity);
//		setTitleBackGroundResource(R.drawable.flipper_head_black_background);
//		showRightActionBtn(false);
		tv_photo_num = (TextView) findViewById(R.id.tv_photo_num);
		tv_caption = (TextView) findViewById(R.id.tv_caption);
		head_title = (TextView) findViewById(R.id.head_title);
		sub_head_title = (TextView) findViewById(R.id.sub_head_title);
		head_action_left = (ImageView) findViewById(R.id.head_action_left);
		head_action_middle = (ImageView) findViewById(R.id.head_action_middle);
		head_action_right = (ImageView) findViewById(R.id.head_action_right);
		head_action_reshare = (ImageView) findViewById(R.id.head_action_reshare);
		img_slide =  findViewById(R.id.img_slide);
		progressBar1 =  findViewById(R.id.progressBar1);
		progressbar_layout =  findViewById(R.id.progressbar_layout);
		text_photo_comment = (TextView) findViewById(R.id.text_photo_comment);
//		button_photo_comment = findViewById(R.id.button_photo_comment);
		layout_titleBar = findViewById(R.id.layout_titleBar);
		layout_bottom = findViewById(R.id.layout_bottom);
		button_photo_comment = findViewById(R.id.layout_photo_buttons);
		
		button_photo_comment.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(thiz, QiupuCommentsPicActivity.class);
				intent.putExtra(BpcApiUtils.SEARCH_KEY_ID, String.valueOf(mPhotos.get(mPager.getCurrItem()).photo_id));
				intent.putExtra("qiupuPhoto", mPhotos.get(mPager.getCurrItem()));
				intent.putExtra("photo_position", mPager.getCurrItem());
				startActivityForResult(intent, REQ_COMMENT);
				
			}
		});
		img_slide.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
		
		mPager = (SuperViewPager) findViewById(R.id.pager);
		mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				setViewPager(position);
//				text_photo_comment.setText(String.valueOf(mPhotos.get(position).comments_count));
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		fromStreamItem = getIntent().getBooleanExtra("fromStreamItem", false);
		album_id = getIntent().getLongExtra("album_id", 0);
		uid = getIntent().getLongExtra("uid", 0);
		if (fromStreamItem) {
			mPhotos = (ArrayList<QiupuPhoto>)(getIntent().getSerializableExtra("photoList"));
//			head_title.setText(getIntent().getStringExtra("album_name"));
			head_title.setText(R.string.share_album_mame);
			sub_head_title.setText(getIntent().getStringExtra("user_name"));
//			button_photo_comment.setEnabled(false);
//			if(mAlbum != null) {
//				showMiddleActionBtn(true);
//				overrideMiddleActionBtn(R.drawable.icon_album, new View.OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						IntentUtil.startGridPicIntent(thiz, album_id, uid);
//						
//					}
//				});
			head_action_left.setVisibility(View.GONE);
				head_action_middle.setVisibility(View.VISIBLE);
				head_action_middle.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						IntentUtil.startGridPicIntent(thiz, album_id, uid,getIntent().getStringExtra("user_name"),true);
						
					}
				});
//				setHeadTitle();
//				mPhotos = QiupuORM.queryQiupuPhotos(thiz, uid, album_id);
//				getPositionByPhotoID();
//			}else {
//				enableMiddleActionBtn(false);
//			}
////			if(mPhotos.size() == 0) {
			progressBar1.setVisibility(View.GONE);
			progressbar_layout.setVisibility(View.GONE);
//			}
//			getAlbum();
	        if(mAdapter == null) {
				mAdapter = new PlacePicsAdapter(getSupportFragmentManager(),
						mPhotos,(mAlbum != null && mAlbum.album_type == 0));
				mPager.setAdapter(mAdapter);
			}else {
				mAdapter.notifyDataSetChanged();
			}
			setViewPager(getIntent().getIntExtra("position", 0));
		} else {
			photo_id = getIntent().getLongExtra("photo_id", 0);
			mAlbum = QiupuORM.queryQiupuAlbumById(thiz, album_id);
			progressBar1.setVisibility(View.GONE);
			progressbar_layout.setVisibility(View.GONE);
			setHeadTitle();
			current_item = getIntent().getIntExtra("current_item", 0);
//			mPhotos = (ArrayList<QiupuPhoto>) getIntent()
//					.getSerializableExtra("parser_list");
			mPhotos = QiupuORM.queryQiupuPhotos(thiz, uid, album_id);
			mAdapter = new PlacePicsAdapter(getSupportFragmentManager(),
					mPhotos,(mAlbum != null && mAlbum.album_type == 0));
			mPager.setAdapter(mAdapter);
			setViewPager(current_item);
			head_action_left.setVisibility(View.VISIBLE);
			head_action_left.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					page = 0;
					if(mPhotos.size() == 0) {
						progressBar1.setVisibility(View.VISIBLE);
						progressbar_layout.setVisibility(View.VISIBLE);
					}
					begin();
					getPhotosByAlbumId();
					
				}
			});
//			getAlbum();
		}
//		overrideLeftActionBtn(R.drawable.actionbar_icon_refresh_normal, new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				page = 0;
//				if(mPhotos.size() == 0) {
//					progressBar1.setVisibility(View.VISIBLE);
//					progressbar_layout.setVisibility(View.VISIBLE);
//				}
//				getAlbum();
//				
//			}
//		});
		if(uid != AccountServiceUtils.getBorqsAccountID()) {
			head_action_reshare.setVisibility(View.VISIBLE);
			head_action_reshare.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
//					final QiupuPhoto p = mPhotos.get(mPager.getCurrItem());
//					String localpath = QiupuHelper.isImageExistInPhone(p.photo_url_original, true);
//					IntentUtil.startComposeIntent(thiz, localpath, String.valueOf(p.photo_id));
					
					final QiupuPhoto p = mPhotos.get(mPager.getCurrItem());
					File imageFile = ((QiupuApplication)getApplication()).getImageLoader().getDiscCache().get(p.photo_url_big);
			    	boolean isImageCachedOnDisc = imageFile.exists();
			    	final String filePath = imageFile.getPath();
			    	if(!isImageCachedOnDisc) {
						Message mds = mHandler.obtainMessage(LOADING_WAIT);
						mds.getData().putBoolean(RESULT, true);
						mHandler.sendMessage(mds);
						showCustomToast(R.string.photo_loading_wait,R.id.layout_titleBar);
					}else {
						IntentUtil.startComposeIntent(thiz, filePath, String.valueOf(p.photo_id));
					}
				}
			});
		}
		head_action_right.setOnClickListener(actionListener);
		
		layout_titleBar.setVisibility(View.GONE);
		layout_bottom.setVisibility(View.GONE);

		 onConfigurationChanged(getResources().getConfiguration());
	}
	
	private void setHeadTitle() {
		head_title.setText(mAlbum.title);
		String username = getIntent().getStringExtra("nick_name");
//        if(QiupuConfig.isPublicCircleProfile(mAlbum.user_id)) {
//        	username = orm.queryPublicCircleNamesByIds(String.valueOf(mAlbum.user_id));
//        }else {
//        	username = orm.getUserName(mAlbum.user_id);
//        }
        sub_head_title.setText(username);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
//	    if (getResources().getConfiguration().orientation !=  Configuration.ORIENTATION_LANDSCAPE)
//	    {
//	      if (getResources().getConfiguration().orientation ==  Configuration.ORIENTATION_PORTRAIT)
//	      {
//	    	  layout_titleBar.setVisibility(View.GONE);
//	      }
//	    }
//	    else
//	    {
//	    	layout_titleBar.setVisibility(View.GONE);
//	    }
	    layout_titleBar.setVisibility(View.GONE);
	    layout_bottom.setVisibility(View.GONE);
	}
	
	void setViewPager(int position) {
		mPager.setCurrItem(position);
		if(mPhotos != null && mPhotos.size()>0) {
			text_photo_comment.setText(String.valueOf(mPhotos.get(position).comments_count));
			int p = position + 1;
//			if(isNeedLoadData) {
//				tv_photo_num.setText(p + "/" + mAlbum.photo_count);
//			}else {
				tv_photo_num.setText(p + "/" + mPhotos.size());
//			}
			if(mPhotos.get(position) != null && !TextUtils.isEmpty(mPhotos.get(position).caption)) {
				tv_caption.setText(mPhotos.get(position).caption);
				tv_caption.setVisibility(View.VISIBLE);
			}else {
				tv_caption.setVisibility(View.GONE);
				tv_caption.setText("");
			}
			mPager.setCurrentItem(position);
		}
	}

//	@Override
//	public void setLeftMenuPosition() {
//		// TODO Auto-generated method stub
//
//	}

	public class PlacePicsAdapter extends FragmentStatePagerAdapter {
		private final ArrayList<QiupuPhoto> pics;
		ArrayList<PhotoFragment> fragmentList = new ArrayList<PhotoFragment>();
		FragmentManager fm;
		boolean isProfileIcon;
		public PlacePicsAdapter(FragmentManager fm, ArrayList<QiupuPhoto> pics,boolean isProfileIcon) {
			super(fm);
			this.pics = pics;
			this.fm = fm;
			this.isProfileIcon = isProfileIcon;
		}

		@Override
		public Fragment getItem(int position) {
			if(isProfileIcon) {
				return  new PhotoFragment(pics.get(position).photo_url_big,pics.get(position).photo_url_big);
			}
			return new PhotoFragment(fromStreamItem?pics.get(position).photo_url_small:pics.get(position).photo_url_thumbnail,pics.get(position).photo_url_big);
		}
		

		@Override
		public int getCount() {
			return pics.size();
		}

		
		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return POSITION_NONE;
		}
	}

	@Override
	protected void createHandler() {
		mHandler = new AlbumsHandler();
	}

	final int GET_ALBUM_SUCCESS = 0;
	final int GET_ALBUM_FAILED = 1;
	final int GET_PHOTO_SUCCESS = 2;
	final int GET_PHOTO_FAILED = 3;
	final int LOADING_WAIT = 4;
	final int SAVED_PHOTO_SUCCESS = 5;
	final int NO_SDCARD = 6;
	final int DEL_PHOTO_SUCCESS = 7;
	final int DEL_PHOTO_FAILED = 8;
	final int EDIT_PROFILE_IMAGE_END = 9;

	private class AlbumsHandler extends Handler {
		public AlbumsHandler() {
			super();
			Log.d(TAG, "new AlbumsHandler");
		}

		@Override
		public void handleMessage(Message msg) {
			end();
			switch (msg.what) {
			case GET_ALBUM_SUCCESS:
				setHeadTitle();
				// albums
				if(mAlbum.have_expired || mPhotos.size() != mAlbum.photo_count) {
				    getPhotosByAlbumId();
				}else {
				    head_action_left.setVisibility(View.VISIBLE);
				}
				break;
			case GET_ALBUM_FAILED:
				progressBar1.setVisibility(View.GONE);
				end();
                showCustomToast(R.string.loading_failed,R.id.layout_titleBar);
				break;
			case GET_PHOTO_SUCCESS:
				progressBar1.setVisibility(View.GONE);
				progressbar_layout.setVisibility(View.GONE);
				end();
				page++;
				//判断photo的位置
				getPositionByPhotoID();
				button_photo_comment.setEnabled(true);
				break;
			case GET_PHOTO_FAILED:
				progressBar1.setVisibility(View.GONE);
				end();
				showCustomToast(R.string.loading_failed,R.id.layout_titleBar);
				break;
			case LOADING_WAIT:
				showCustomToast(R.string.photo_loading_wait,R.id.layout_titleBar);
				break;
			case SAVED_PHOTO_SUCCESS:
				String msgStr = msg.getData().getString("ERROR_MSG");
				showCustomToast(getResources().getString(R.string.photo_save_success) + msgStr,R.id.layout_titleBar);
				break;
			case NO_SDCARD:
                showCustomToast(R.string.no_sdcard_no_download,R.id.layout_titleBar);
				break;
			case DEL_PHOTO_SUCCESS:
				if(msg.getData().getBoolean(RESULT)) {
				    isRefresh = true;
				    showCustomToast(R.string.del_success,R.id.layout_titleBar);
					if(mPhotos.size() <= 0) {
						finish();
					}
					mAdapter.notifyDataSetChanged();
			        if(mPhotos != null && mPhotos.size()>0) {
			            text_photo_comment.setText(String.valueOf(mPhotos.get(mPager.getCurrItem()).comments_count));
			            int p = mPager.getCurrItem() + 1;
			            tv_photo_num.setText(p + "/" + mPhotos.size());
			        }
				}else {
					showCustomToast(R.string.del_failed,R.id.layout_titleBar);
				}
				break;
			case DEL_PHOTO_FAILED:
				showCustomToast(msg.getData().getString("ERROR_MSG"),R.id.layout_titleBar);
				break;
			case EDIT_PROFILE_IMAGE_END: {
                dimissProgressDialog();
                if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
                    mCurrentPhotoFile.delete();
                }
                if(msg.getData().getBoolean(RESULT, false)) {
                    updatePostProfileImage(msg.getData().getString(USER_IMAGE));
                }else {
                    Log.d(TAG, "edit profile iamge failed " );
                }
                break;
            }
			}
			
		}
	}
	
	private void getPositionByPhotoID() {
	    int position = 0;
		for(int i = 0; i < mPhotos.size(); i++) {
//			enableMiddleActionBtn(true);
			if(photo_id == mPhotos.get(i).photo_id) {
			    position = i;
				return;
			}
		}
		if(mAdapter == null) {
		    mAdapter = new PlacePicsAdapter(getSupportFragmentManager(),
		            mPhotos,(mAlbum != null && mAlbum.album_type == 0));
		    mPager.setAdapter(mAdapter);
		}else {
		    mAdapter.notifyDataSetChanged();
		}
		if(position != 0) {
		    setViewPager(position);
		}
//		getPhotosByAlbumId();
	}

	Object mInfoLock = new Object();
	boolean inInfoProcess;

	private void getPhotosByAlbumId() {
		synchronized (mInfoLock) {
			if (inInfoProcess == true) {
				Log.d(TAG, "in loading info data");
				return;
			}
		}

		synchronized (mInfoLock) {
			inInfoProcess = true;
		}
//		begin();
		asyncQiupu.getPhotosByAlbumId(AccountServiceUtils.getSessionID(),
				album_id, page, mAlbum.photo_count, new TwitterAdapter() {
					@Override
					public void getPhotosByAlbumId(ArrayList<QiupuPhoto> photos) {
						if (page == 0) {
							mPhotos.clear();
						}
						mPhotos.addAll(photos);
//						orm.bullinsertQiupuPhoto(mPhotos, uid, album_id,page==0);
						orm.bullinsertQiupuPhoto(mPhotos, uid, album_id);
						Message mds = mHandler.obtainMessage(GET_PHOTO_SUCCESS);
						mds.getData().putBoolean(RESULT, true);
						mHandler.sendMessage(mds);
						synchronized (mInfoLock) {
							inInfoProcess = false;
						}

					}

					public void onException(TwitterException ex,
							TwitterMethod method) {
						Message mds = mHandler.obtainMessage(GET_PHOTO_FAILED);
						mds.getData().putBoolean(RESULT, false);
						mds.getData().putString(ERROR_MSG, ex.getMessage());
						mHandler.sendMessage(mds);
						synchronized (mInfoLock) {
							inInfoProcess = false;
						}
					}
				});
	}

	private void getAlbum() {
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
		asyncQiupu.getAlbum(AccountServiceUtils.getSessionID(), album_id, uid,
				false, new TwitterAdapter() {
					@Override
					public void getAlbum(QiupuAlbum album) {
//						orm.insertQiupuAlbumInfo(album,true);
//						mAlbum = album.clone();
//						album = null;
//						Message mds = mHandler.obtainMessage(GET_ALBUM_SUCCESS);
//						mds.getData().putBoolean(RESULT, true);
//						mHandler.sendMessage(mds);
//						synchronized (mInfoLock) {
//							inInfoProcess = false;
//						}
//						
//						
						
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
						Message mds = mHandler.obtainMessage(GET_ALBUM_FAILED);
						mds.getData().putBoolean(RESULT, false);
						mds.getData().putString(ERROR_MSG, ex.getMessage());
						mHandler.sendMessage(mds);
						synchronized (mInfoLock) {
							inInfoProcess = false;
						}
					}
				});
	}
	
	class InsertAsyncTask extends UserTask<QiupuAlbum,Void,Void> {

        @Override
        public Void doInBackground(QiupuAlbum... params) {
            orm.insertQiupuAlbumInfo(params[0],true);
            return null;
        }
        
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mPager != null) {
			mPager.removeAllViews();
			mPager = null;
		}
		mAdapter = null;
	}
	
	@Override
	public void finish() {
	    if(isRefresh) {
            setResult(RESULT_OK);
        }
	    super.finish();
	}

	@Override
	public void onSingleTapConfirmed() {
		showBar();
		
	}
	
	public void showBar() {
		if(layout_bottom != null) {
			Animation am1 = AnimationUtils.loadAnimation(thiz,layout_bottom.getVisibility()==View.VISIBLE? R.anim.slide_out_down_self:R.anim.slide_in_up_self);
			layout_bottom.startAnimation(am1);
			layout_bottom.setVisibility(layout_bottom.getVisibility()==View.VISIBLE ? View.GONE:View.VISIBLE);
		}
		if(layout_titleBar != null) {
			Animation am = AnimationUtils.loadAnimation(thiz,layout_titleBar.getVisibility()==View.VISIBLE? R.anim.slide_out_up_self:R.anim.slide_in_down_self);
			layout_titleBar.startAnimation(am);
			layout_titleBar.setVisibility(layout_titleBar.getVisibility()==View.VISIBLE ? View.GONE:View.VISIBLE);
		}
	}
	
	View.OnClickListener actionListener = new View.OnClickListener() {
        public void onClick(View v) {
//            editProfile();
//        	String[] strings = getResources().getStringArray(R.array.user_detail_action);
//        	String[] items = getResources().getStringArray(R.array.user_detail_action);
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.photo_user_action_save)));
        	items.add(new SelectionItem("", getString(R.string.status_share)));
        	items.add(new SelectionItem("", getString(R.string.set_image_icon)));
        	if(uid == AccountServiceUtils.getBorqsAccountID() && !fromStreamItem) {
        		items.add(new SelectionItem("", getString(R.string.delete_photo)));
        	}
        	showCorpusSelectionDialog(items);
        }
    };
    
    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
	    if(mMiddleActionBtn != null) {
	        int location[] = new int[2];
	        mMiddleActionBtn.getLocationInWindow(location);
	        int x = location[0];
	        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
	        
	        DialogUtils.showCorpusSelectionDialog(this, x, y, items, actionListItemClickListener);
	    }
	}
    OnItemClickListener actionListItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                onCorpusSelected(item.getText());             
            }
        }
    };
    int delPhotoDialogId = 0x00101;
    private void onCorpusSelected(String value) {
    	if (getString(R.string.photo_user_action_save).equals(value)) {
//    		savePhoto();
    		savePhoto2();
    	}else if (getString(R.string.delete_photo).equals(value)) {
//    		DialogUtils.createDialog(thiz, R.string.delete_photo, 
//    				getString(R.string.sure_delete_photo), delPhotoListener, delPhotoDialogId).show();
//    		de();
    		showDeletePhotoDialog();
    	}else if (getString(R.string.status_share).equals(value)) {
    	    sharePhoto();
    	}else if (getString(R.string.set_image_icon).equals(value)) {
    	    File f = getPhoto();
    	    if(f != null ) {
    	        if(mAlbum != null && mAlbum.album_type == 0) {
    	            editProfileImage(f);
    	        }else {
    	            tryCropProfileImage(Uri.fromFile(f));
    	        }
    	    }
//    		sharePhoto();
    	}else {
            Log.d(TAG, "unsupported item action!");
        }
    }
    
    private void tryCropProfileImage(Uri uri) {
        try {
            // start gallery to crop photo
            final Intent intent = QiupuHelper.getCropImageIntent(uri);
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {}
    }
    File mCurrentPhotoFile = null;
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        
        switch (requestCode) {
          case PHOTO_PICKED_WITH_DATA: 
          {
              Bitmap photo = data.getParcelableExtra("data");
              mCurrentPhotoFile = new File(QiupuHelper.getTmpCachePath()+"screenshot.png");
              FileOutputStream fOut = null;
              try {
                  if (mCurrentPhotoFile.exists()) {
                      mCurrentPhotoFile.delete();
                  }                
                  mCurrentPhotoFile.createNewFile();
                  fOut = new FileOutputStream(mCurrentPhotoFile);                      
                  photo.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                  fOut.flush();
                  fOut.close();

                  editProfileImage(mCurrentPhotoFile);
              } catch (Exception e) {
                  e.printStackTrace();
              }

              //TODO
//              sharedImageUri = null;
              break;  
          }  
          case REQ_COMMENT:
          {
              int photo_position = data.getIntExtra("photo_position", 0);
              QiupuPhoto p = (QiupuPhoto)data.getSerializableExtra("qiupuPhoto");
              mPhotos.get(photo_position).iliked = p.iliked;
              mPhotos.get(photo_position).likes_count = p.likes_count;
              mPhotos.get(photo_position).comments_count = p.comments_count;
              if(mPager.getCurrItem() == photo_position) {
                  text_photo_comment.setText(String.valueOf(mPhotos.get(photo_position).comments_count));
              }
              p.despose();
              p = null;
              break;
          }
          default:
              super.onActivityResult(requestCode, resultCode, data);
        } 
        
    };
    
   Dialog.OnClickListener delPhotoListener = new Dialog.OnClickListener() {
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		QiupuPhoto p = mPhotos.get(mPager.getCurrItem());
		final String photo_id = String.valueOf(p.photo_id);
		deletePhoto(photo_id,mPager.getCurrItem());
	}
   };
    
    
   private File getPhoto() {
       final QiupuPhoto p = mPhotos.get(mPager.getCurrItem());
//       QiupuORM.sWorker.post(new Runnable() {
//           
//           @Override
//           public void run() {
       File imageFile = ((QiupuApplication)getApplication()).getImageLoader().getDiscCache().get(p.photo_url_big);
   	boolean isImageCachedOnDisc = imageFile.exists();
               if(!isImageCachedOnDisc) {
//                   Message mds = mHandler.obtainMessage(LOADING_WAIT);
//                   mds.getData().putBoolean(RESULT, true);
//                   mHandler.sendMessage(mds);
                   showCustomToast(R.string.photo_loading_wait,R.id.layout_titleBar);
                   return null;
               }else {
//                   if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                       try {
//                           if(FileUtils.isExisted(QiupuConfig.PHOTO_MEDIA_SDCARD_PATH) != 1) {
//                               FileUtils.createDirectory(QiupuConfig.PHOTO_MEDIA_SDCARD_PATH);
//                           }
//                           String fileName = QiupuConfig.PHOTO_MEDIA_SDCARD_PATH + p.photo_id+".jpg";
//                           FileUtils.copyfile(localpath, fileName);
//                           Message mds = mHandler.obtainMessage(SAVED_PHOTO_SUCCESS);
//                           mds.getData().putBoolean(RESULT, true);
//                           mds.getData().putString(ERROR_MSG, fileName);
//                           mHandler.sendMessage(mds);
//                       } catch (Exception e) {
//                           // TODO Auto-generated catch block
//                           e.printStackTrace();
//                       }
//                   }else {
//                       Message mds = mHandler.obtainMessage(NO_SDCARD);
//                       mds.getData().putBoolean(RESULT, true);
//                       mHandler.sendMessage(mds);
//                   }
                   
                   return imageFile;
               }
//           }
//       });
   }
   private void savePhoto() {
	   final QiupuPhoto p = mPhotos.get(mPager.getCurrItem());
	   QiupuORM.sWorker.post(new Runnable() {
		   
		   @Override
		   public void run() {
			   String localpath = QiupuHelper.isImageExistInPhone(p.photo_url_original, true);
			   if(TextUtils.isEmpty(localpath)) {
				   Message mds = mHandler.obtainMessage(LOADING_WAIT);
				   mds.getData().putBoolean(RESULT, true);
				   mHandler.sendMessage(mds);
				   showCustomToast(R.string.photo_loading_wait,R.id.layout_titleBar);
			   }else {
				   if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					   try {
						   if(FileUtils.isExisted(QiupuConfig.PHOTO_MEDIA_SDCARD_PATH) != 1) {
							   FileUtils.createDirectory(QiupuConfig.PHOTO_MEDIA_SDCARD_PATH);
						   }
						   String fileName = QiupuConfig.PHOTO_MEDIA_SDCARD_PATH + p.photo_id+".jpg";
						   FileUtils.copyfile(localpath, fileName);
						   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
							   new MediaScannerNotifier(thiz, fileName, "image/*");
						   }
						   Message mds = mHandler.obtainMessage(SAVED_PHOTO_SUCCESS);
						   mds.getData().putBoolean(RESULT, true);
						   mds.getData().putString(ERROR_MSG, fileName);
						   mHandler.sendMessage(mds);
					   } catch (Exception e) {
						   // TODO Auto-generated catch block
						   e.printStackTrace();
					   }
				   }else {
					   Message mds = mHandler.obtainMessage(NO_SDCARD);
					   mds.getData().putBoolean(RESULT, true);
					   mHandler.sendMessage(mds);
				   }
			   }
		   }
	   });
   }
    private void savePhoto2() {
    	final QiupuPhoto p = mPhotos.get(mPager.getCurrItem());
    	File imageFile = ((QiupuApplication)getApplication()).getImageLoader().getDiscCache().get(p.photo_url_big);
    	boolean isImageCachedOnDisc = imageFile.exists();
    	if(isImageCachedOnDisc) {
    		final String filePath = imageFile.getPath();
    		QiupuORM.sWorker.post(new Runnable() {
    			
    			@Override
    			public void run() {
    				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    					try {
    						if(FileUtils.isExisted(QiupuConfig.PHOTO_MEDIA_SDCARD_PATH) != 1) {
    							FileUtils.createDirectory(QiupuConfig.PHOTO_MEDIA_SDCARD_PATH);
    						}
    						String fileName = QiupuConfig.PHOTO_MEDIA_SDCARD_PATH + p.photo_id+".jpg";
    						FileUtils.copyfile(filePath, fileName);
    						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
    							new MediaScannerNotifier(thiz, fileName, "image/*");
    						}
    						Message mds = mHandler.obtainMessage(SAVED_PHOTO_SUCCESS);
    						mds.getData().putBoolean(RESULT, true);
    						mds.getData().putString(ERROR_MSG, fileName);
    						mHandler.sendMessage(mds);
    					} catch (Exception e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				}else {
    					Message mds = mHandler.obtainMessage(NO_SDCARD);
    					mds.getData().putBoolean(RESULT, true);
    					mHandler.sendMessage(mds);
    				}
    			}
    		});
    	}else {
    		Message mds = mHandler.obtainMessage(LOADING_WAIT);
			mds.getData().putBoolean(RESULT, true);
			mHandler.sendMessage(mds);
			showCustomToast(R.string.photo_loading_wait,R.id.layout_titleBar);
    	}
    }
    
    
    Object mDelInfoLock = new Object();
	boolean delInfoProcess;

	private void deletePhoto(final String photo_id,final int current_positon) {
		synchronized (mDelInfoLock) {
			if (delInfoProcess == true) {
				Log.d(TAG, "in deleting photo");
				return;
			}
		}

		synchronized (mDelInfoLock) {
			delInfoProcess = true;
		}
//		begin();
		asyncQiupu.deletePhoto(AccountServiceUtils.getSessionID(),photo_id,isDelAll.isChecked(), new TwitterAdapter() {
					@Override
					public void deletePhoto(boolean result) {
						Message mds = mHandler.obtainMessage(DEL_PHOTO_SUCCESS);
						mds.getData().putBoolean(RESULT, result);
						mds.getData().putInt("position", current_positon);
						orm.deleteQiupuPhotoInfo(AccountServiceUtils.getBorqsAccountID(), photo_id);
						mPhotos.remove(current_positon);
						mHandler.sendMessage(mds);
						synchronized (mDelInfoLock) {
							delInfoProcess = false;
						}
					}

					public void onException(TwitterException ex,
							TwitterMethod method) {
						Message mds = mHandler.obtainMessage(DEL_PHOTO_FAILED);
						mds.getData().putBoolean(RESULT, false);
						mds.getData().putString(ERROR_MSG, ex.getMessage());
						mHandler.sendMessage(mds);
						synchronized (mDelInfoLock) {
							delInfoProcess = false;
						}
					}
				});
	}
	
	void sharePhoto() {
		final QiupuPhoto p = mPhotos.get(mPager.getCurrItem());
//		String localpath = QiupuHelper.isImageExistInPhone(p.photo_url_original, true);
//		if(TextUtils.isEmpty(localpath)) {
		
		File imageFile = ((QiupuApplication)getApplication()).getImageLoader().getDiscCache().get(p.photo_url_big);
    	boolean isImageCachedOnDisc = imageFile.exists();
    	final String filePath = imageFile.getPath();
    	if(!isImageCachedOnDisc) {
//		if(TextUtils.isEmpty(filePath)) {
			Message mds = mHandler.obtainMessage(LOADING_WAIT);
			mds.getData().putBoolean(RESULT, true);
			mHandler.sendMessage(mds);
			showCustomToast(R.string.photo_loading_wait,R.id.layout_titleBar);
		}else {
			Intent shareIntent = new Intent(Intent.ACTION_SEND); 
			shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(new File(filePath))); 
			shareIntent.setType("image/jpeg"); 
			startActivity(Intent.createChooser(shareIntent,getTitle()));  
		}
	}
	
	CheckBox isDelAll;
	private void showDeletePhotoDialog(){
    	View selectview = LayoutInflater.from(this).inflate(R.layout.delete_photo_view, null);
    	
    	isDelAll = (CheckBox) selectview.findViewById(R.id.isDelAll);
    	QiupuPhoto p = mPhotos.get(mPager.getCurrItem());
    	if(p.photo_url_original.contains("/"+uid+"_")) {
    		isDelAll.setVisibility(View.VISIBLE);
    	}else {
    		isDelAll.setChecked(false);
    		isDelAll.setVisibility(View.GONE);
    	}
    	TextView profile_dialog_title = (TextView) selectview.findViewById(R.id.profile_dialog_title);
    	
    	profile_dialog_title.setVisibility(View.GONE);
    	
    	
    	DialogUtils.ShowDialogwithView(this, getString(R.string.delete_photo), 0,
    			selectview, delPhotoListener, ChangeRequestCancelListener);
    }
	
	DialogInterface.OnClickListener ChangeRequestCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {}
	};
	private ProgressDialog mprogressDialog;
	private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
        mprogressDialog = DialogUtils.createProgressDialog(this, 
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.show();     
    }
	
	private void dimissProgressDialog() {
        try {
            mprogressDialog.dismiss();
            mprogressDialog = null;
        }catch(Exception e){
            Log.d(TAG, "progress dialog dimiss exception !");
        }
        
    }
	Object mEditProfileImageLock = new Object();
    boolean inEditImageProcess;
    final String USER_IMAGE = "USER_IMAGE";
    private void editProfileImage(File file){
        if (this.asyncQiupu == null) {
            return;
        }
        
        synchronized(mEditProfileImageLock)
        {
            if(inEditImageProcess == true)
            {
                Log.d(TAG, "in loading info data");
                return ;
            }
        }
        
        showProcessDialog(R.string.edit_profile_update_dialog, false, true, true);
        
        synchronized(mEditProfileImageLock)
        {
            inEditImageProcess = true;          
        }
        asyncQiupu.editUserProfileImage(AccountServiceUtils.getSessionID(), file, new TwitterAdapter() {
            public void editUserProfileImage(String result) {
                Log.d(TAG, "finish edit user profile" + result);

                orm.updateProfileImageUrl(result);
                
                Message msg = mHandler.obtainMessage(EDIT_PROFILE_IMAGE_END);               
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.getData().putString(USER_IMAGE, result);
                msg.sendToTarget();
                synchronized(mEditProfileImageLock)
                {
                    inEditImageProcess = false;         
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                
                synchronized(mEditProfileImageLock)
                {
                    inEditImageProcess = false;         
                }
                TwitterExceptionUtils.printException(TAG, "editUserProfileImage, server exception:", ex, method);

                Message msg = mHandler.obtainMessage(EDIT_PROFILE_IMAGE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
            }
        });
    }
	
    private void updatePostProfileImage(final String imageUrl){

        synchronized(QiupuHelper.refreshProfileImageListeners)
        {
            Log.d(TAG, "refreshProfileImageListeners.size() : " + QiupuHelper.refreshProfileImageListeners.size());
            Set<String> set = QiupuHelper.refreshProfileImageListeners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                RefreshPostProfileImageListener listener = QiupuHelper.refreshProfileImageListeners.get(key).get();
                if(listener != null)
                {
                    listener.refreshPostProfileImage(imageUrl);
                }
            }      
        }      
    }

	@Override
	public void onclick() {
		showBar();
		
	}   
}