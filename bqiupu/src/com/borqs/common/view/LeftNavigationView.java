package com.borqs.common.view;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.model.MainItemInfo;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.UserTask;
import com.borqs.information.db.Notification;
import com.borqs.information.db.Notification.NotificationColumns;
import com.borqs.information.db.NotificationOperator;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.service.RequestsService;
import com.borqs.qiupu.ui.AboutActivity;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.BpcExchangeCardActivity;
import com.borqs.qiupu.ui.bpc.BpcFriendsFragmentActivity;
import com.borqs.qiupu.ui.bpc.BpcPostsNewActivity;
import com.borqs.qiupu.ui.bpc.UserProfileFragmentActivity;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.Requests;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeftNavigationView extends LinearLayout implements RequestsService.RequestListener, LeftNavigationCallBack{

	final String TAG = "Qiupu.LeftNavigationView";
	public LeftNavigationView(Context context) {
		this(context, null);		
	}
	public LeftNavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		
        init();
	}
	
	public interface LeftNavigationListener{
		public void    closeSlider();
		public boolean isShowNotification();
		public void    finishCurrentActivity();
	}
	
	@Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
	
	GridView  gridView;
	boolean   supportDebug = false;
	QiupuORM  orm;	
	ImageView profileImageView;
	Handler mHandler = new Handler();
	
	WeakReference<LeftNavigationListener>   csl;
	NoticeContentObserver  noticeObserver;
	
	public void openLeftUI() {	
		initUI();
	}
	
	public void onCreate() {		
		//No fetch?, I think we can do this.
		if(QiupuConfig.LowPerformance)
		    initUI();
	}

	//
	//when create or open the UI, set the profile image, fetch the request
	//
	private void initUI()
	{
		mHandler.postDelayed(new Runnable() {
			public void run()
			{
				if(profileImageView != null)
				    profileImageView.setImageDrawable(createProfileIcon());		
				
				String username = getUserNickname(getSaveUid());
				if(username != null && username.length()>0)
				{
					//also set head title
					//setHeadTitle(username);					
				}
				
			}
		}, QiupuConfig.A_SECOND);
		
		//re-fetch request
		if(QiupuService.mRequestsService != null)
		{
			QiupuService.mRequestsService.regiestRequestListener(LeftNavigationView.class.getName(), LeftNavigationView.this);
			QiupuService.mRequestsService.rescheduleRequests(true);
		}
		else
		{
			mHandler.postDelayed(new Runnable()
	        {
	        	public void run()
	        	{
	        		QiupuService.mRequestsService.regiestRequestListener(LeftNavigationView.class.getName(), LeftNavigationView.this);
	        		QiupuService.mRequestsService.rescheduleRequests(true);
	        	}
	        }, 5 * QiupuConfig.A_SECOND);
		}
	}
	public void onResume()
	{	
		
		//re-fetch notification        
		getContext().getContentResolver().registerContentObserver(Notification.NotificationColumns.CONTENT_URI, false, noticeObserver);
		new NotificationEntry().execute();
		
		//just stream ui will fetch the data
		if(BpcPostsNewActivity.class.isInstance(csl.get()))
		{
		    InformationUtils.getInforByDelay(getContext(), 3 * QiupuConfig.A_SECOND);
		}
		else
		{
			//notify to update old UI, it is just to update the UI
			getContext().getContentResolver().notifyChange(NotificationColumns.CONTENT_URI, null);
		}
	}
	
	//process also for notification
	public void onPause()
	{
		getContext().getContentResolver().unregisterContentObserver(noticeObserver);
		if(QiupuService.mRequestsService != null)
            QiupuService.mRequestsService.unRegiestRequestListener(LeftNavigationView.class.getName());
	}
	
	public void onDestroy() {
		notificationButton.clear();
		notificationButton = null;
	}
	
	WeakReference<TextView> notificationButton;
	public void setNotificationTextView(TextView findViewById) {
		notificationButton = new WeakReference<TextView>(findViewById);
	}
	
    class NotificationEntry extends UserTask<Void, Void, Integer> {
    	
		@Override
        public Integer doInBackground(Void... params) {
			int count = new NotificationOperator(getContext()).getThisWeekUnReadCount();
			return count;
		}
		
		@Override
        public void onPostExecute(Integer result) {
			if(result == null) {
				Log.d(TAG, "no new notifications");
				result = 0;
			}			
			if(csl != null && csl.get() != null && csl.get().isShowNotification())
			{
				if(notificationButton != null && notificationButton.get() != null)
				{
		            notificationButton.get().setVisibility(View.VISIBLE);
					if(result == 0) {
						notificationButton.get().setTextColor(Color.WHITE);
					}else {
						notificationButton.get().setTextColor(Color.RED);
					}
					
					notificationButton.get().setText(result==0?"":String.valueOf(result));
					notificationButton.get().postInvalidate();
				}
			}
		}
		
    }
    
    private class NoticeContentObserver extends ContentObserver {
        
        public NoticeContentObserver(Handler handler) {
            super(handler);
        }
        
        public void onChange(boolean selfChange) {
            new NotificationEntry().execute();
        }
    }

	
	private void init() {
		orm = QiupuORM.getInstance(getContext());
		
		//create one grid view		
	    LayoutInflater factory = LayoutInflater.from(getContext());
        removeAllViews();
        
        //child 1
        View convertView  = factory.inflate(R.layout.left_whole, null);      
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.FILL_PARENT));
        gridView = (GridView) convertView.findViewById(R.id.grid);
		gridView.setAlwaysDrawnWithCacheEnabled(true);
		gridView.setFocusableInTouchMode(true);
		//gridView.setNumColumns(3);
		 
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
    	{
        	gridView.setNumColumns(3);
    	}
    	else
    	{
    		gridView.setNumColumns(2);
    	}
        
		gridView.setFocusable(true);
		gridView.setSelected(true);
		gridView.setClickable(true);  
        addView(convertView);
        
        View btn_search = findViewById(R.id.search_bar);
    	btn_search.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
                IntentUtil.startPeopleSearchIntent(getContext());
			}
		});
    	
    	noticeObserver = new NoticeContentObserver(mHandler);
    	setContent();       
	}
	
	final int Bring_history_activity_to_frong=Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
	private void setContent() {
		ArrayList<MainItemInfo> itemInfos = new ArrayList<MainItemInfo>();
		final Resources res = getResources();

//		if(QiupuORM.isUsingTabNavigation(getContext()) == false)
//		{
//			final MainItemInfo streamItem = createMainItem(res, R.drawable.main_stream,R.string.home_steam);
//	        itemInfos.add(streamItem);
//	        streamItem.mCallBack = new MainItemInfo.ItemCallBack() {
//				public void CallBack() {
//					if (BpcPostsNewActivity.class.isInstance(csl.get()) == false) {
//
//	                    final Intent intent = BpcApiUtils.encodeStreamTypeIntent(BpcApiUtils.ALL_TYPE_POSTS);
//	                    if (null == intent) {
//	                    } else {
//	                    	intent.setFlags(Bring_history_activity_to_frong);
//	                    	getContext().startActivity(intent);
//	                    }
//
//                        csl.get().finishCurrentActivity();
//					}
//					else
//					{
//						csl.get().closeSlider();
//					}
//				}
//			};
//		}
		
		//home_screen_profile_icon_overlay_default		
		final MainItemInfo profileItem = createMainProfileItem(res, R.drawable.main_profile, R.string.home_my_profile);
		profileItem.mIcon = createProfileIcon();
		itemInfos.add(profileItem);	
		profileItem.mCallBack = new MainItemInfo.ItemCallBack() {
			public void CallBack() {
				shootProfileActivity();				
			}
	    };
	    
//	    if(QiupuORM.isUsingTabNavigation(getContext()) == true)
//		{
//			final MainItemInfo albumItem = createMainItem(res, R.drawable.main_album, R.string.home_album);
//			itemInfos.add(albumItem);
//			albumItem.mCallBack = new MainItemInfo.ItemCallBack() {
//				public void CallBack() {
//					csl.get().finishCurrentActivity();
//				}
//		    };
//		}

//	    if(QiupuORM.isUsingTabNavigation(getContext()) == false)
//		{
//			final MainItemInfo friendItem = createMainItem(res, R.drawable.main_friends, R.string.home_friends);
//			itemInfos.add(friendItem);
//			friendItem.mCallBack = new MainItemInfo.ItemCallBack() {
//				public void CallBack() {
//					shootFriendActivity();
//				}
//		    };
//		}

//        if(QiupuORM.isUsingTabNavigation(getContext()) == false) {
//            final MainItemInfo exchangeItem = createMainItem(res, R.drawable.main_exchange, R.string.home_exchange);
//            itemInfos.add(exchangeItem);
//            exchangeItem.mCallBack = new MainItemInfo.ItemCallBack() {
//                public void CallBack() {
//                    shootExchangeCardActivity();
//                }
//            };
//        }

        final MainItemInfo requestsItem = createMainItem(res, R.drawable.main_exchange, R.string.home_exchange);
        requestsItem.iamrequest = true;
        itemInfos.add(requestsItem);
        requestsItem.mCallBack = new MainItemInfo.ItemCallBack() {
            public void CallBack() {
//                showRequestActivity();
                shootExchangeCardActivity();
            }
        };

		PackageManager pm = getContext().getPackageManager();

		final Intent launcherIntent = new Intent("com.borqs.bpc.action.APP_PLUGIN");
        launcherIntent.addCategory(Intent.CATEGORY_TEST);
        List<ResolveInfo> pluginActivity = pm.queryIntentActivities(launcherIntent, 0);
        final int size = pluginActivity.size();
        ArrayList<MainItemInfo> plugins = new ArrayList<MainItemInfo>();
        for (int i = 0; i < size; ++i) {
        	plugins.add(getMainItemForActivity(getContext(),pluginActivity.get(i).activityInfo));
        }
        
        Collections.sort(plugins);
        itemInfos.addAll(plugins);
        
        //make sure it is the last one
        final MainItemInfo settingsItem = createMainItem(res, R.drawable.main_about, R.string.about_title);
		itemInfos.add(settingsItem);
		settingsItem.mCallBack = new MainItemInfo.ItemCallBack() {
			public void CallBack() {
				Intent intent = new Intent(getContext(), AboutActivity.class);
				intent.putExtra("home", "main");
				getContext().startActivity(intent);
			}
	    };
        
        try {
            ApplicationInfo oldAccount= pm.getApplicationInfo(BpcApiUtils.TARGET_PKG_BACCOUNT, 0);
        	//prompt dialog to uninstall pre borqs sync app
			if(oldAccount != null)
			{
				AlertDialog dialog = new  AlertDialog.Builder(getContext())
                 .setTitle(R.string.app_title)
		         .setMessage(R.string.tutorial_uninstall)
		         .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int whichButton) {
		            	 Uri packageURI = Uri.parse("package:com.borqs.service.accountsync");
						 Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
						 getContext().startActivity(uninstallIntent);
		             }
		         })
		         .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int whichButton) {
		             }
		         })
		         .create();
				dialog.show();					
				
			}
			
			ApplicationInfo nitoficationAccount= pm.getApplicationInfo("com.borqs.notification", 0);
        	//prompt dialog to uninstall pre borqs sync app
			if(nitoficationAccount != null)
			{
				AlertDialog dialog = new  AlertDialog.Builder(getContext())
		         .setTitle(R.string.tutorial_uninstall_notification)
		         .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int whichButton) {
		            	 Uri packageURI = Uri.parse("package:com.borqs.notification");
						 Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
						 getContext().startActivity(uninstallIntent);
		             }
		         })
		         .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int whichButton) {
		             }
		         })
		         .create();
				dialog.show();					
				
			}
		} catch (NameNotFoundException e) {
//			e.printStackTrace();
		}

        if(supportDebug)
		{
        	/*
	        final MainItemInfo bookItem = createMainItem(res, R.drawable.app_book, R.string.home_book);
			itemInfos.add(bookItem);		

	        final MainItemInfo musicItem = createMainItem(res, R.drawable.app_music, R.string.home_music);
			itemInfos.add(musicItem);
			*/
			
			final MainItemInfo messageItem = createMainItem(res, R.drawable.inbox_home_nonew, R.string.home_msg);
			itemInfos.add(messageItem);
		}

        gridView.setAdapter(new DashBoardAdapter(getContext(), itemInfos));
		gridView.setOnItemClickListener(new DashBoardClickListener());
		
		mHandler.postDelayed(new Runnable()
		{
			public void run()
			{
				if(profileImageView != null)
				    profileImageView.setImageDrawable(createProfileIcon());		
			}
		}, 4 * QiupuConfig.A_SECOND);
		
	}
	
	
	class DashBoardAdapter extends BaseAdapter {
	    private final Context mContext;
	    private final ArrayList<MainItemInfo> mInfos;

	    public DashBoardAdapter(Context con, ArrayList<MainItemInfo> infos) {
	    	mContext = con;
	    	mInfos = infos;
	    }

	    public int getCount() {
	    	if(mInfos != null)
	    		return mInfos.size();

	    	return 0;
	    }

	    public MainItemInfo getItem(int position) {
	    	if(mInfos != null)
	    		return mInfos.get(position);
	    	return null;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	       View view = convertView;
	       if (view == null) {
               LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
               view = li.inflate(R.layout.main_item_view, null, false);
	       }

	       MainItemInfo info = getItem(position);
	       ImageView iv = (ImageView)view.findViewById(R.id.icon);
	       TextView tv = (TextView)view.findViewById(R.id.tv);

	       //if it is for request, need show the number
	       MissingNumberView request_count_iv = (MissingNumberView)view.findViewById(R.id.request_count_iv);
	       if(info.iamrequest == true){
	    	   //for test
//	    	   request_count_iv.setVisibility(View.VISIBLE);
//	    	   request_count_iv.setMissCallCount(20);
	    	   
	    	   request_count_view = request_count_iv;
	       }else{
	    	   request_count_iv.setVisibility(View.GONE);
	       }
	       
	       if(MainProfileItemInfo.class.isInstance(info))
	       {
	    	   profileImageView = iv;	    	   
	       }
	       
	       iv.setImageDrawable(info.mIcon);
	       tv.setText(info.mLabel);

	       return view;
	    }

        public void removePluginItem(String pkgName) {
            if (TextUtils.isEmpty(pkgName)) {
                Log.d(TAG, "removePluginItem, ignore empty name");
                return;
            }

            int index = getCount() - 1;
            if (index >= 0) {
                MainItemInfo info;
                do {
                    info = getItem(index);
                    if (null != info && null != info.mComponent) {
                        if (pkgName.equalsIgnoreCase(info.mComponent.getPackageName())) {
                            mInfos.remove(info);
                            notifyDataSetChanged();
                        }
                    }
                } while (index-- > 0);
            }
            
        }
        
        public void touchPluginItem(String pkgName) {
            //
            if (TextUtils.isEmpty(pkgName)) {
                Log.w(TAG, "touchPluginItem, skip empty package name.");
                return;
            }
            
            final int count = mInfos.size();
            for (int index = 0; index < count; ++index) {
                MainItemInfo itemInfo = mInfos.get(index);
                if (null != itemInfo.mComponent) {
                    if (pkgName.equals(itemInfo.mComponent.getPackageName())) {
                        Log.w(TAG, "touchPluginItem, skip for existing name: " + pkgName + ", index:" + index);
                        return;
                    }
                }
            }

            PackageManager manager = getContext().getPackageManager();
            Intent mainIntent = new Intent("com.borqs.bpc.action.APP_PLUGIN", null);
            mainIntent.addCategory(Intent.CATEGORY_TEST);
            final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
            final int size = apps.size();
            ActivityInfo info;
            for (int i = 0; i < size; ++i) {
                info = apps.get(i).activityInfo;
                if (null != info && pkgName.equals(info.packageName)) {
                    mInfos.add(getMainItemForActivity(getContext(), info));
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

	MissingNumberView request_count_view; 

	
	 private MainItemInfo createMainItem(Resources res, int drawableId, int nameId) {
	        final Drawable icon = drawableId > 0 ? res.getDrawable(drawableId) : null;
	        final String label = nameId > 0 ? res.getString(nameId) : "";
	        return new MainItemInfo(icon, label);
	    }

	    private MainItemInfo createMainProfileItem(Resources res, int drawableId, int nameId) {
	        final Drawable icon = drawableId > 0 ? res.getDrawable(drawableId) : null;
	        final String label = nameId > 0 ? res.getString(nameId) : "";
	        return new MainProfileItemInfo(icon, label);
	    }

	    private void shootPluginApp(Context con, final ComponentName component) {
	    	if(component.getClass().isInstance(csl.get()) == false)
        	{                
		        Intent intent = new Intent(Intent.ACTION_MAIN);
		        intent.setComponent(component);
		        intent.addCategory(Intent.CATEGORY_DEFAULT);
		        intent.putExtra("from_home", true);
		        con.startActivity(intent);
        	}
	    }
	    
	    private class DashBoardClickListener implements AdapterView.OnItemClickListener {
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            Object obj = gridView.getAdapter().getItem(position);
	            if (MainItemInfo.class.isInstance(obj)) {
	                MainItemInfo mainItemInfo = (MainItemInfo) obj;
	                if (null != mainItemInfo.mComponent) {
	                	shootPluginApp(getContext(),mainItemInfo.mComponent);
	    	        	
	                    return;
	                }
	            	
	                mainItemInfo.mCallBack.CallBack();
		        	
	            }
	        }
	    }	
	    
	    
//	    private void showRequestActivity()
//		{
//	    	if(RequestActivity.class.isInstance(csl.get()) == false)
//	    	{
//				Intent intent = new Intent(getContext(), RequestActivity.class);
//				intent.putExtra("home", "main");
//				intent.setFlags(Bring_history_activity_to_frong);
//				getContext().startActivity(intent);
//				csl.get().finishCurrentActivity();
//	    	}else{
//	    		csl.get().closeSlider();
//	    	}
//		}
	    
	    protected static boolean isValidAccountSession() {
	        return !StringUtil.isEmpty(AccountServiceUtils.getSessionID());
	    }
	    
	    private void shootNotificationActivity() {
	    	Intent intent = new Intent("com.borqs.bpc.action.NOTIFICATION");
			if(BpcApiUtils.isActivityReadyForIntent(getContext(), intent)) {
				intent.setFlags(Bring_history_activity_to_frong);
				getContext().startActivity(intent);
			}
	    }

	    private void shootProfileActivity() {
	        if (isValidAccountSession()) {
	        	if(UserProfileFragmentActivity.class.isInstance(csl.get()) == false)
		    	{
	                Intent intent = IntentUtil.buildUserDetailIntent(getContext(),  getSaveUid(), getUserNickname(getSaveUid()));
	                if(intent != null ) {
	                	intent.setFlags(Bring_history_activity_to_frong);
	                	intent.putExtra("supportLeftNavigation", true);
	                	getContext().startActivity(intent);
	                	
	                	csl.get().finishCurrentActivity();
	                }
		    	}
	        	else
	        	{
	        		csl.get().closeSlider();
	        	}
	        } else {
	            Log.d(TAG, "Fail to verify session account, could not start friend activity.");
	        }
	    }
	    
	    // Query valid nick name from local database first, then possible get
	    // from Account field.
	    protected final String getUserNickname(long uid) {
	        String nickName = orm.getUserName(uid);

	        if (TextUtils.isEmpty(nickName)) {
	            BorqsAccount borqsAccount = AccountServiceUtils.getBorqsAccount();
	            if (null != borqsAccount) {
	                nickName = borqsAccount.nickname;
	            }
	        }
	        return TextUtils.isEmpty(nickName) ? "" : nickName;
	    }

    private void shootExchangeCardActivity() {
        if(isValidAccountSession()) {
            if(BpcExchangeCardActivity.class.isInstance(csl.get()) == false) {
                Intent intent = new Intent(getContext(), BpcExchangeCardActivity.class);
                intent.setFlags(Bring_history_activity_to_frong);
                getContext().startActivity(intent);
                csl.get().finishCurrentActivity();
            } else {
                csl.get().closeSlider();
            }
        }
    }

	    private void shootFriendActivity() {
            if (isValidAccountSession()) {
                if (BpcFriendsFragmentActivity.class.isInstance(csl.get()) == false) {
                    Intent intent = new Intent(getContext(), BpcFriendsFragmentActivity.class);
                    Bundle bundle = BpcApiUtils.getUserBundle(Long.valueOf(getSaveUid()));
                    bundle.putString(BasicActivity.USER_NICKNAME, getUserNickname(getSaveUid()));
                    bundle.putInt(BasicActivity.USER_CONCERN_TYPE, BpcFriendsFragmentActivity.current_type_circle);
                    intent.putExtras(bundle);
                    intent.setFlags(Bring_history_activity_to_frong);
                    getContext().startActivity(intent);

                    csl.get().finishCurrentActivity();
                } else {
                    csl.get().closeSlider();
                }
            } else {
                Log.d(TAG, "Fail to verify session account, could not start profile activity.");
            }
	    }

	    
	    /**
	     * Wrapper class used to do nothing except addressing the Profile item on
	     * dashboard which will dynamically load user icon.
	     */
	    private class MainProfileItemInfo extends MainItemInfo {
	        public MainProfileItemInfo(Drawable ic, String label) {
	            super(ic, label);
	        }
	    }
	    
	    public static long getSaveUid(){
	        return AccountServiceUtils.getBorqsAccountID();
	    }
	    
	    private Drawable createProfileIcon()
		{
			Bitmap baseBmp = null;
//			QiupuUser user = orm.queryOneUserInfo(getSaveUid());
            final String photoUrl = orm.getUserImageUrl(getSaveUid());
			if(!TextUtils.isEmpty(photoUrl))
			{
				URL imageurl;
				try { 
					imageurl = new URL(photoUrl);
				
		            String filepath = QiupuHelper.getImageFilePath(imageurl, true);
		            if(new File(filepath).exists())
		            {
		            	Bitmap tmp = BitmapFactory.decodeFile(filepath);
		            	
		                baseBmp = BitmapFactory.decodeResource(getResources(), R.drawable.home_screen_profile_icon_overlay_default);
		                
		                Bitmap newmap = Bitmap.createBitmap(baseBmp.getWidth(), baseBmp.getHeight(), Bitmap.Config.ARGB_8888);
		                Canvas can = new Canvas();
						can.setBitmap(newmap);					
						
						//scale tmp to fit background
						
						final int profileHeight =  (int)(0.8*baseBmp.getWidth());
						
						final int sourceWidth  = tmp.getWidth();
						final int sourceHeight = tmp.getHeight();
						
						//scale the bitmap
						float scale = 1.0f;		    
						if(sourceHeight > sourceWidth)
						{
	   		    	       scale = (float)profileHeight/(float)tmp.getHeight();
						}
						else
						{
							scale = (float)profileHeight/(float)tmp.getWidth();
						}
							
	   		    	    
			    	    Matrix matrix = new Matrix();   		    	    
	   		            matrix.setScale(scale, scale);
	   		            tmp = Bitmap.createBitmap(tmp, 0, 0, sourceWidth, sourceHeight, matrix, true);
	   		         
	   		            final int startPos = (baseBmp.getWidth() - tmp.getWidth())/2;
	   		            final int endPos   = (baseBmp.getHeight() - tmp.getHeight())/2;   		            
	   		            
						can.drawBitmap(tmp, startPos, endPos, null);
						can.drawBitmap(baseBmp, 0, 0, null);
						
						 
						tmp.recycle();
						tmp = null;
						
						baseBmp.recycle();
						baseBmp = null;
						
					    Drawable draw = new BitmapDrawable(newmap);
					    ((BitmapDrawable)draw).setTargetDensity(getResources().getDisplayMetrics());
					    
					    return draw;
		            }	            

				} catch (MalformedURLException e) {	
					e.printStackTrace();
				}
				catch(Exception ne){
					ne.printStackTrace();
				}
				
				return getResources().getDrawable(R.drawable.main_profile);
			}
			else
			{
				return getResources().getDrawable(R.drawable.main_profile);
			}
		}
	    
	    private static MainItemInfo  getMainItemForActivity(Context con, ActivityInfo info) {
	        PackageManager pm = con.getPackageManager();
	        ComponentName componentName = new ComponentName(info.packageName, info.name);
	        Drawable icon = null;
	        if(IntentUtil.APPBOX_SPLASH_NAME.equals(info.name)){
	            icon = con.getResources().getDrawable(R.drawable.main_allapp);
	        }
	        else{
	            try {
	                icon = pm.getActivityIcon(componentName);
	            } catch (PackageManager.NameNotFoundException e) {
	                e.printStackTrace();
	            }
	            if (null == icon) {
	                try {
	                    icon = pm.getApplicationIcon(info.packageName);
	                } catch (PackageManager.NameNotFoundException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        
	        CharSequence label = null;
	        label = info.loadLabel(pm);
	        if (TextUtils.isEmpty(label)) {
	            label = pm.getApplicationLabel(info.applicationInfo);
	        }

	        final MainItemInfo itemInfo = new MainItemInfo(icon, label.toString());
	        itemInfo.mComponent = componentName;

	        return itemInfo;
	    }
	    
		public void setCloseLisner(LeftNavigationListener mlis) {
			csl = new WeakReference<LeftNavigationListener>(mlis);
		}
		
		
		
		private ArrayList<Requests> requestList = new ArrayList<Requests>();
		@Override
		public void requestUpdated(final ArrayList<Requests> data) {
			requestList.clear();
			requestList.addAll(data);
			mHandler.post(new Runnable()
			{
				public void run()
				{
					int count = data.size();
					Log.d(TAG, "update request count="+count);				
					if(count > 0)
					{
						request_count_view.setMissCallCount(count);
					}
					else
					{
						request_count_view.setMissCallCount(0);
						
						request_count_view.setVisibility(View.GONE);
					}
				}
			});
		}				
}
