package com.borqs.common.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.common.adapter.IconListAdapter;
import com.borqs.common.adapter.LeftMenuAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.UpdateNotificationListener;
import com.borqs.common.util.UserTask;
import com.borqs.information.db.Notification.NotificationColumns;
import com.borqs.information.db.NotificationOperator;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.bpc.BpcPostsNewActivity;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.wutong.OrganizationHomeActivity;

public class LeftMenuListView extends ListView implements /*RequestsService.RequestListener,*/ 
        LeftNavigationCallBack/*, UpdateRequestCountListener*/ {

    final String       TAG = "Qiupu.LeftMenuListView";

    private LeftMenuAdapter leftMenuAdapter;
//    WeakReference<UpdateNotificationListener> mUpdateNotificationListener;

    public LeftMenuListView(Context context) {
        this(context, null);
    }

    public LeftMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCacheColorHint(context.getResources().getColor(android.R.color.transparent));
//        init();
    }

    public interface LeftNavigationListener {
        public void closeSlider();
        public boolean isShowNotification();
        public void finishCurrentActivity();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    boolean                               supportDebug = false;
    QiupuORM                              orm;
    ImageView                             profileImageView;
    Handler                               mHandler     = new Handler();

    WeakReference<LeftNavigationListener> csl;
    NoticeContentObserver                 noticeObserver;

    public void openLeftUI() {
//        setRequestCount(getExchangeRequestCount());
        initUI();
    }

    public void setUpdateNotificationListener(UpdateNotificationListener updateNotificationListener) {
//        mUpdateNotificationListener = new WeakReference<UpdateNotificationListener>(
//                updateNotificationListener);
    }

    public void onCreate() {
        // No fetch?, I think we can do this.
        if (QiupuConfig.LowPerformance) {
            initUI();
        }
    }

	//when create or open the UI, set the profile image, fetch the request
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
//		if(QiupuService.mRequestsService != null)
//		{
//			QiupuService.mRequestsService.regiestRequestListener(LeftMenuListView.class.getName(), LeftMenuListView.this);
//			QiupuService.mRequestsService.rescheduleRequests(true);
//		}
//		else
//		{
//			mHandler.postDelayed(new Runnable()
//	        {
//	        	public void run()
//	        	{
//	        		QiupuService.mRequestsService.regiestRequestListener(LeftMenuListView.class.getName(), LeftMenuListView.this);
//	        		QiupuService.mRequestsService.rescheduleRequests(true);
//	        	}
//	        }, 5 * QiupuConfig.A_SECOND);
//		}
		
//		QiupuHelper.registerUpdateRequestCountListener(getClass().getName(), this);
	}
	public void onResume()
	{
		//re-fetch notification
		getContext().getContentResolver().registerContentObserver(NotificationColumns.CONTENT_URI, false, noticeObserver);
		new NotificationEntry().execute();

		//just stream ui will fetch the data
		if(null !=csl && (BpcPostsNewActivity.class.isInstance(csl.get()) || OrganizationHomeActivity.class.isInstance(csl.get())))
		{
		    InformationUtils.getInforByDelay(getContext(), 3 * QiupuConfig.A_SECOND);
		    // sync requests
		    if(QiupuService.mRequestsService != null) {
//		    	QiupuService.mRequestsService.regiestRequestListener(LeftMenuListView.class.getName(), LeftMenuListView.this);
		    	QiupuService.mRequestsService.rescheduleRequests(true);
		    }
		    else {
		    	mHandler.postDelayed(new Runnable() {
		    		public void run() {
//		    			QiupuService.mRequestsService.regiestRequestListener(LeftMenuListView.class.getName(), LeftMenuListView.this);
		    			QiupuService.mRequestsService.rescheduleRequests(true);
		    		}
		    	}, 5 * QiupuConfig.A_SECOND);
		    }
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
//		if(QiupuService.mRequestsService != null)
//            QiupuService.mRequestsService.unRegiestRequestListener(LeftMenuListView.class.getName());
	}

	public void onDestroy() {
//		notificationButton.clear();
//		notificationButton = null;
		QiupuHelper.unregisterUpdateRequestCountListener(getClass().getName());
	}

//	WeakReference<TextView> notificationButton;
	public void setNotificationTextView(TextView findViewById) {
//		notificationButton = new WeakReference<TextView>(findViewById);
	}

    class NotificationEntry extends UserTask<Void, Void, Integer> {

		@Override
        public Integer doInBackground(Void... params) {
			int count = new NotificationOperator(getContext()).getThisWeekUnReadCount();
			return count;
		}

        @Override
        public void onPostExecute(Integer result) {
            ArrayList<IconListAdapter.IconListItem> items = leftMenuAdapter.getListData();
            if (null == items || items.isEmpty()) {
                return;
            }

            
            if (result == null) {
            } else {
//            	mNtfcount = result.intValue();
//                items.get(LeftMenuMapping.TYPE_BpcInformationActivity - 1).setCount(mNtfcount + RequestCount);
//                leftMenuAdapter.notifyDataSetChanged();
            }

//            if (null != mUpdateNotificationListener && mUpdateNotificationListener.get() != null) {
//                mUpdateNotificationListener.get().updateNotificationIcon(mNtfcount);
//            }

//            Log.d(TAG, " notification count = " + mNtfcount);

//            if (csl != null && csl.get() != null
//                    && csl.get().isShowNotification()) {
//                if (notificationButton != null && notificationButton.get() != null) {
//                    // notificationButton.get().setVisibility(View.VISIBLE);
//                    notificationButton.get().setVisibility(View.GONE);
//                    if (result == 0) {
//                        notificationButton.get().setTextColor(Color.WHITE);
//                    } else {
//                        notificationButton.get().setTextColor(Color.RED);
//                    }
//
//                    notificationButton.get().setText(result == 0 ? "" : String.valueOf(result));
//                    notificationButton.get().postInvalidate();
//                }
//            }
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

    	noticeObserver = new NoticeContentObserver(mHandler);
    	mHandler.post(new Runnable() {
            @Override
            public void run() {
//                RequestCount = orm.getRequestCount("");
//                Log.d(TAG, " db cache exchange count = " + RequestCount);
            }
    	});
	}

	final int Bring_history_activity_to_frong=Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

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

	    public static long getSaveUid(){
	        return AccountServiceUtils.getBorqsAccountID();
	    }

	    private Drawable createProfileIcon()
		{
			Bitmap baseBmp = null;
//			QiupuUser user = orm.queryOneUserInfo(getSaveUid());
            String photoUrl = orm.getUserImageUrl(getSaveUid());
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


    public void setCloseLisner(LeftNavigationListener mlis) {
        csl = new WeakReference<LeftNavigationListener>(mlis);
    }

//    public static ArrayList<Requests> requestList = new ArrayList<Requests>();
//    public static int RequestCount = 0;
//    private static int mNtfcount = 0;

//    private void setRequestCount(int count) {
//        if (count >= 0) {
//            ArrayList<IconListAdapter.IconListItem> items = leftMenuAdapter.getListData();
//            if (null != items && !items.isEmpty()) {
//            	items.get(LeftMenuMapping.TYPE_BpcInformationActivity - 1).setCount(count + mNtfcount);
//                leftMenuAdapter.notifyDataSetChanged();
//            }
//        }
//    }

//    public static void setExchangeRequestCount(int request_count) {
//    	RequestCount = request_count;
//    }

//    public static int getExchangeRequestCount() {
//        return RequestCount;
//    }

//    @Override
//    public void requestUpdated(final ArrayList<Requests> data) {
//        if(data != null) {
//        	RequestCount = data.size();
//        }
//        Log.d(TAG, "===========>>>> exchange count = " + RequestCount);
////        requestList.addAll(data);
//        mHandler.post(new Runnable() {
//            public void run() {
//            	if (RequestCount > 0) {
//            		setRequestCount(RequestCount);
//            		
////            		if (null != mUpdateNotificationListener && mUpdateNotificationListener.get() != null) {
////            			mUpdateNotificationListener.get().updateRequestCount(RequestCount);
////            		}
//            	}
//            }
//        });
//    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        leftMenuAdapter = (LeftMenuAdapter) adapter;
        super.setAdapter(adapter);
    }

    // update request count.
//	@Override
//	public void updateRequestCount(int exchangeRequestCount, String type) {
//		RequestCount = orm.getRequestCount("");
//		Log.d(TAG, "updateReqeustCount : " + RequestCount);
//		setRequestCount(RequestCount);
//	}

}
