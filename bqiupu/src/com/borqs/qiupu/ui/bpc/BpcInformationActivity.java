package com.borqs.qiupu.ui.bpc;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import twitter4j.NotificationInfo;
import twitter4j.Requests;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.NotifyActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.UserTask;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.information.AsyncHtmlTextLoader;
import com.borqs.information.InformationBase;
import com.borqs.information.InformationDownloadService;
import com.borqs.information.InformationHttpPushReceiver;
import com.borqs.information.db.Notification;
import com.borqs.information.db.NotificationOperator;
import com.borqs.information.util.InformationConstant;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.RequestFragment;
import com.borqs.qiupu.fragment.RequestFragment.UpdateRequestCountListener;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.service.RequestsService.RequestListener;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class BpcInformationActivity extends BasicNavigationActivity implements RequestListener, UpdateRequestCountListener{
	private static final String TAG = "BpcInformationActivity";
	
    private ListView informationListView;
//	private View informationEmptyView;
	private MyCursorAdapter mAdapter;

	private gridViewAdapter mRequestAdapter;
	private NotificationOperator mOperator;
	private HashMap<Long, Boolean> readStatus;
	private DownloadServiceStatusReceiver downloadServiceReceiver;
	private IntentFilter serviceReceiverFilter;
	private DBContentObserver mContentObserver;
	private long lastModified = 0;
	private HashSet<Long> processedIds = null;

	private GridView mCenterGridView;
	private int mFriendRequestCount = 0;
    private int mEventRequestCount = 0;
    private int mCircleRequestCount = 0;
    private int mExchangeCardRequestCount = 0;

    private ArrayList<GridViewItem> gridViewItems = new ArrayList<GridViewItem>();
    private boolean mHasMore = false;
    private Button mMoreBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    enableLeftNav();
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.notification_list_view);
//		showLeftActionBtn(true);
		registerQiupuRequestListener();
		QiupuHelper.registerUpdateRequestCountListener(getClass().getName(), this);
		
		informationListView = (ListView) findViewById(R.id.information_list);
		mOperator = new NotificationOperator(this);
//		informationEmptyView = findViewById(android.R.id.empty);
		mContentObserver = new DBContentObserver(mHandler);
		mHandler.obtainMessage(DISABLE_ALL_TITLE_BUTTON).sendToTarget();
		if(mOperator.getUnProcessedNotification() >= InformationConstant.DEFAULT_NOTIFICATION_COUNT) {
			mHasMore = true;
		}
//		getUnProcessedNotification();
		initUI();
		initListView();

        showTitleSpinnerIcon(false);

        downloadServiceReceiver = new DownloadServiceStatusReceiver();
		serviceReceiverFilter = new IntentFilter(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_STATUS_ACTION);
		InformationUtils.cancelScheduledInforGet();
//		mHandler.obtainMessage(GET_INFORMATION_AUTO).sendToTarget();
		mHandler.obtainMessage(GET_REQUEST_SUMMARY).sendToTarget();

//        showMiddleActionBtn(true);
//        overrideMiddleActionBtn(R.drawable.ic_settings, new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NotificationSettingActivity.startActivity(BpcInformationActivity.this, true);
//            }
//        });
	}

    private void getUnProcessedNotification() {
        QiupuORM.sWorker.post(new Runnable() {
            @Override
            public void run() {
                int count = mOperator.getUnProcessedNotification();
//                Message msg = mHandler.obtainMessage(QUERY_ALL_NOTIFICATION_END);
//                if (count > 0) {
//                    msg.getData().putBoolean(RESULT, true);
//                } else {
//                    msg.getData().putBoolean(RESULT, false);
//                }
//                msg.sendToTarget();
            }
        });
    }

	public boolean isShowNotification()
    {
    	return false;
    }

	@Override
	protected void onResume() {
		super.onResume();

        mHandler.removeMessages(RELOAD_LIST_ITEM_DATE);

		if(isNeedToReload()) {
            reloadList();
		}

		getContentResolver().registerContentObserver(Notification.NotificationColumns.CONTENT_URI, false, mContentObserver);
		registerReceiver(downloadServiceReceiver, serviceReceiverFilter);
		if(InformationDownloadService.isDownloadServiceRunning()) {
			mHandler.obtainMessage(VISIBLE_PROGRESS_BUTTON).sendToTarget();
//			mHandler.obtainMessage(VISIBLE_EMPTYVIEW_PROGRESS).sendToTarget();
		}else {
			mHandler.obtainMessage(INVISIBLE_PROGRESS_BUTTON).sendToTarget();
//			mHandler.obtainMessage(INVISIBLE_EMPTYVIEW_PROGRESS).sendToTarget();
			mHandler.obtainMessage(SET_LISTVIEW_REFRESH_COMPLETE).sendToTarget();
		}
		mHandler.obtainMessage(REFRESH_TITLE).sendToTarget();
		cancelNotice();
	}
	
	private boolean isNeedToReload() {
		if(lastModified != mOperator.getLastModifyDate()) {
			return true;
		}
		if(processedIds == null) {
			return false;
		}
		HashSet<Long> temp = mOperator.getProcessedItems();
		boolean result = processedIds.hashCode() != temp.hashCode();
		temp.clear();
		processedIds.clear();
		return result;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.post(new Runnable() {
            @Override
            public void run() {
                lastModified = mOperator.getLastModifyDate();
                processedIds = mOperator.getProcessedItems();
            }
        });
		getContentResolver().unregisterContentObserver(mContentObserver);
		unregisterReceiver(downloadServiceReceiver);
		cancelNotice();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		setReadAllMessageStatus();
		unRegisterQiupuRequestListener();
		QiupuHelper.unregisterUpdateRequestCountListener(getClass().getName());
		mAdapter.changeCursor(null);
		readStatus.clear();
	}
	
//	@Override
//	public void onBackPressed() {
//	    setReadAllMessageStatus();
//		super.onBackPressed();
//	}

	private void setReadAllMessageStatus() {
	    new Handler().postDelayed(new Runnable(){
            public void run()
            {
                setRead(-1);
            }
        }, 1500);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return false;
	}
	
	private View initRequestHeadView(LayoutInflater inflater) {
    	View contentView = inflater.inflate(R.layout.ntf_request_headview, null, false);
    	mCenterGridView = (GridView) contentView.findViewById(R.id.main_gv);
    	refreshCounts();
		addGridViewItem();
		
		mRequestAdapter = new gridViewAdapter(gridViewItems);
		mCenterGridView.setAdapter(mRequestAdapter);
		mCenterGridView.setOnItemClickListener(new RequestItemClickListener());
    	return contentView;
    }
	
	private void refreshCounts() {
		mFriendRequestCount = orm.getRequestCount(Requests.getFriendsRequestTypes());
		mEventRequestCount = orm.getRequestCount(Requests.getEventRequestTypes());
		mCircleRequestCount = orm.getRequestCount(Requests.getPublicCircleRequestTypes());
		mExchangeCardRequestCount = orm.getRequestCount(String.valueOf(Requests.REQUEST_TYPE_EXCHANGE_VCARD));
	}
	
	private void addGridViewItem() {
		GridViewItem item;
		item = new GridViewItem();
        Resources res = getResources();
        item.icon = res.getDrawable(R.drawable.friend_request_icon);
        item.displayname = res.getString(R.string.home_friends);
        item.actionid = 0;
        item.requestCount = mFriendRequestCount;
        item.requesttypes = Requests.getFriendsRequestTypes();
//        item.requesttypes = String.valueOf(RequestFragment.FRIEND_REQUEST);
        gridViewItems.add(item);

        item = new GridViewItem();
        item.icon = res.getDrawable(R.drawable.event_request_icon);
        item.displayname = res.getString(R.string.event);
        item.actionid = 1;
        item.requestCount = mEventRequestCount;
        item.requesttypes = Requests.getEventRequestTypes();
//        item.requesttypes = String.valueOf(RequestFragment.EVENT_REQUEST);
        gridViewItems.add(item);

        item = new GridViewItem();
        item.icon = res.getDrawable(R.drawable.circle_request_icon);
        item.displayname = res.getString(R.string.user_circles);
        item.actionid = 3;
        item.requestCount = mCircleRequestCount;
        item.requesttypes = Requests.getPublicCircleRequestTypes();
//        item.requesttypes = String.valueOf(RequestFragment.CIRCLE_REQUEST);
        gridViewItems.add(item);
        
        item = new GridViewItem();
        item.icon = res.getDrawable(R.drawable.card_request_icon);
        item.displayname = res.getString(R.string.address_book_circle);
        item.actionid = 3;
        item.requestCount = mExchangeCardRequestCount;
        item.requesttypes = String.valueOf(Requests.REQUEST_TYPE_EXCHANGE_VCARD);
//        item.requesttypes = String.valueOf(RequestFragment.CHANGE_REQUEST);
        gridViewItems.add(item);
    }
	
	private void initListView() {
		informationListView.setDivider(getResources().getDrawable(R.drawable.divider));
		informationListView.setSelector(getResources().getDrawable(R.drawable.list_selector_background));
		informationListView.setCacheColorHint(getResources().getColor(R.color.qiupu_list_color_cache_hint));
		informationListView.setFastScrollEnabled(true);
		informationListView.setBackgroundResource(R.color.qiupu_list_color);
		
		informationListView.addHeaderView(initRequestHeadView(getLayoutInflater()));
		informationListView.setHeaderDividersEnabled(false);
		
//		informationEmptyView.findViewById(R.id.info_empty_refresh).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mHandler.obtainMessage(GET_INFORMATION_AUTO).sendToTarget();
//			}
//		});
//		informationListView.setEmptyView(informationEmptyView);
		readStatus = new HashMap<Long, Boolean>();
		mAdapter = new MyCursorAdapter(this, R.layout.information_list_item, null);
		informationListView.setAdapter(mAdapter);
//		informationListView.setonRefreshListener(new OnRefreshListener() {
//			@Override
//			public void onRefresh() {
//				mHandler.obtainMessage(GET_INFORMATION_AUTO).sendToTarget();
//			}
//		});
		
		informationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				InformationBase data = getCurData(mAdapter.getCursor());
				if(data.type.equalsIgnoreCase("ntf.create_account"))
				{
					boolean isRead = data.read;
					if(readStatus.containsKey(data.id)) {
						isRead = readStatus.get(data.id);
					}
					if(!isRead) {
						TextView tv = ((TextView)view.findViewById(R.id.message_content));
						tv.getPaint().setFakeBoldText(false);
						tv.postInvalidate();
						readStatus.put(data.id, true);
						setRead(data.id);
						mOperator.updateReadStatus(data.id, true);

					}
					
					Intent intent = new Intent(Intent.ACTION_VIEW);
					if (data.apppickurl == null) {
						return false;
					} else {
						intent.setData(data.apppickurl);
					}

					if (BpcApiUtils.isActivityReadyForIntent(BpcInformationActivity.this, intent)) {	
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivity(intent);
					}
					
					return true;
				}
				
				return false;				
			}
			
		});
		informationListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				InformationBase data = getCurData(mAdapter.getCursor());
				boolean isRead = data.read;
				if(readStatus.containsKey(data.id)) {
					isRead = readStatus.get(data.id);
				}
				if(!isRead) {
					TextView tv = ((TextView)view.findViewById(R.id.message_content));
					tv.getPaint().setFakeBoldText(false);
					tv.postInvalidate();
					readStatus.put(data.id, true);
					setRead(data.id);
					mOperator.updateReadStatus(data.id, true);

				}
				forwardInformation(data);
			}
		});
	}
	
	private void setRead(long id) {
		new BatchReadTask(id == -1).execute(id);
	}
	
	private class BatchReadTask extends UserTask<Long, Void, Void> {
		
		private boolean isAll;
		
		public BatchReadTask(boolean b) {
			this.isAll = b;
		}
		
		@Override
		public Void doInBackground(Long... params) {
			String ticket = AccountServiceUtils.getSessionID();
			if(!TextUtils.isEmpty(ticket)) {
				if(isAll) {
				    String unReadIds = mOperator.getAllUnReadInfoIds();
				    if(QiupuConfig.DBLOGD)Log.d(TAG, "unread ids : " + unReadIds);
				    mOperator.removeExcessntf();  // only save <= 20 ntf.
				    if(StringUtil.isValidString(unReadIds)) {
				        boolean succeed = mOperator.updateAllReadStatus(true);
				        if (succeed) {
				            updateNotificationUI(0);
				        }
				        InformationUtils.setReadStatus(BpcInformationActivity.this, unReadIds);
				    }
				} else {
				    mOperator.updateReadStatus(params[0], true);
				    InformationUtils.setReadStatus(BpcInformationActivity.this, String.valueOf(params[0]));
				}
			}
			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(isAll) {
                reloadList();
			}
			mHandler.obtainMessage(REFRESH_TITLE).sendToTarget();
		}

	}
	
	private void updateNotificationUI(final int count) {
	    synchronized (QiupuHelper.notificationListeners) {
	        Set<String> set = QiupuHelper.notificationListeners.keySet();
	        Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                WeakReference<NotifyActionListener> ref = QiupuHelper.notificationListeners.get(key);
                if (ref != null && ref.get() != null) {
                    ref.get().updateNotificationCountUI(count);
                }
            }
	    }
	}

	static class ViewHolder {
		public ImageView image;
		public ImageView contactImage;
		public TextView content;
		public TextView date;
		public String data_senderId = null;
		public String data_appId    = null;
		public String contact_uri   = null;
	}
	 
	private class MyCursorAdapter extends ResourceCursorAdapter implements View.OnClickListener {
		
		private Context mContext;
		private AsyncHtmlTextLoader asyncHtmlTextLoader;
		
		public MyCursorAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c, false);
			mContext = context;
			asyncHtmlTextLoader = new AsyncHtmlTextLoader();
		}
		
		@Override
		public void changeCursor(Cursor cursor) {
			readStatus.clear();
			if(cursor == null) {
				asyncHtmlTextLoader.clear();
			}
			super.changeCursor(cursor);
		}
		
		@Override
		public int getCount() {
			if(mHasMore) {
				return super.getCount() + 1;
			}else {
				return super.getCount();
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(position == super.getCount()) {
                if (position == 0) {
                    Log.e(TAG, "unexpected case: has more button without info data.");
                }

				if(mMoreBtn == null) {
					mMoreBtn = generateMoreItem();
//				}else {
//					setMoreBtnText(mMoreBtn);
				}
				return mMoreBtn;
			}else if (convertView != null && Button.class.isInstance(convertView)) {
//				getCursor().moveToPosition(position);
//				convertView = newView(mContext, getCursor(), parent);
//				bindView(convertView, mContext, getCursor());
//				return convertView;
				return super.getView(position, null, parent);
			}else {
				return super.getView(position, convertView, parent);
			}
		}

		@Override
		public void bindView(View view, final Context context, Cursor cursor) {
			final ViewHolder holder = (ViewHolder) view.getTag();
			if(cursor == null || cursor.getCount() ==0)
				return;
			
			final String image_url = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IMAGE_URL));
			final String title = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TITLE));
			final String title_html = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TITLE_HTML));
			final String body = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.BODY));
			holder.data_appId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.APP_ID));
			holder.data_senderId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.SENDER_ID));
			long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.M_ID));
			final long time = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LAST_MODIFY));			
			holder.image.setOnClickListener(this);
			holder.image.setImageResource(R.drawable.default_user_icon);
			setImage(image_url, holder.image);
			
			holder.contact_uri = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LONGPRESSURI));
			if (TextUtils.isEmpty(holder.contact_uri) == false)
			{
				holder.contactImage.setVisibility(View.VISIBLE);	
				holder.contact_uri = holder.contact_uri;
				holder.contactImage.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						ViewHolder holder = (ViewHolder) ((View) v.getParent()).getTag();
						if (!TextUtils.isEmpty(holder.contact_uri)) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(holder.contact_uri));
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							mContext.startActivity(intent);
						}						
					}
				});
			}
			else
			{
				holder.contactImage.setVisibility(View.GONE);
			}
			
			String temp = TextUtils.isEmpty(body) ? title : title + "\n\n" + body;
			holder.content.setTag(itemId);
			if (TextUtils.isEmpty(title_html)) {
				holder.content.setText(temp);
			} else {
				String tempForHtml = TextUtils.isEmpty(body) ? title_html : title_html + "<br><br>" + body;
				Spanned tempHtml = asyncHtmlTextLoader.loadHtmlText(itemId, tempForHtml, 
						new AsyncHtmlTextLoader.HtmlTextCallback() {
							@Override
							public void textLoaded(Spanned htmlText, long idTag) {
								TextView tv = (TextView) informationListView
										.findViewWithTag(idTag);
								if (tv != null) {
									tv.setText(htmlText);
								}
							}
						});
				holder.content.setText(tempHtml == null ? temp : tempHtml);
			}
			boolean is_read = false;
			if(readStatus.containsKey(itemId)) {
				is_read = readStatus.get(itemId);
			}else {
				is_read = cursor.getInt(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IS_READ)) == 1;
				readStatus.put(itemId, is_read);
			}
			final boolean readStatus = is_read;
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					holder.content.getPaint().setFakeBoldText(!readStatus);
					holder.content.postInvalidate();
				}
			});
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					holder.date.setText(DateUtil.converToRelativeTime(context, time));
				}
			});
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);
			ViewHolder holder = new ViewHolder();
			holder.image = (ImageView) view.findViewById(R.id.img);
			holder.content = (TextView) view.findViewById(R.id.message_content);
			holder.date = (TextView) view.findViewById(R.id.date);
			holder.contactImage = (ImageView) view.findViewById(R.id.contact_img);
			
			view.setTag(holder);
			return view;
		}
		
		private Button generateMoreItem() {
			mMoreBtn = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
			mMoreBtn.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
	        setMoreBtnText(mIsLoading);
	        return mMoreBtn;
	    }
		
		private void setImage(String url, ImageView image) {
    		ImageRun imagerun = new ImageRun(null, url, 0);
    		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
    		imagerun.noimage = true;
    		imagerun.addHostAndPath = true;
    		imagerun.setRoundAngle = true;
    		imagerun.setImageView(image);
    		imagerun.post(null);
    	}
		
		@Override
		public void onClick(final View v) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					ViewHolder holder = (ViewHolder) ((View) v.getParent()).getTag();
					if (!TextUtils.isEmpty(holder.data_appId) && !TextUtils.isEmpty(holder.data_senderId) &&!holder.data_senderId.equals(holder.data_appId)) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("borqs://profile/details?uid=" + holder.data_senderId));
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						mContext.startActivity(intent);
					}
				}
			});
		}
		
		
	}

	private void initUI() {
		mHandler.obtainMessage(INVISIBLE_PROGRESS_BUTTON).sendToTarget();
		mHandler.obtainMessage(REFRESH_TITLE).sendToTarget();
	}
	
	private void cancelNotice() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(InformationHttpPushReceiver.HTTPPUSH);
	}

	private void forwardInformation(InformationBase msg) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (TextUtils.isEmpty(msg.uri)) {
			return;
		} else {
			intent.setData(Uri.parse(msg.uri));
		}

		if (BpcApiUtils.isActivityReadyForIntent(this, intent)) {
			intent.putExtra("MSG_ID", msg.id);
			intent.putExtra("DATA", msg.data);
			intent.putExtra("SENDER_ID", msg.senderId);
			intent.putExtra("WHEN", msg.lastModified);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}
	}
	
	public void refresh_Without_Requery() {
		if(informationListView != null) {
			int firstOnShow = informationListView.getFirstVisiblePosition();
			int countOnShow = informationListView.getChildCount();
			for(int i=0; i<countOnShow; i++) {
				if(informationListView.getChildAt(i) != null) {
					Object tag = informationListView.getChildAt(i).getTag();
					if(tag != null && (tag instanceof ViewHolder)) {
						final ViewHolder holder = (ViewHolder) tag;
						mAdapter.getItem(firstOnShow+i-1);
						Cursor cursor = mAdapter.getCursor();
						if(!(cursor.isAfterLast() || cursor.isBeforeFirst())) {
							final long date = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LAST_MODIFY));
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									holder.date.setText(DateUtil.converToRelativeTime(getApplicationContext(), date));
								}
							});
						}
					}
				}
			}
		}
	}
	
	public void refresh_Title() {
		refresh_Title(0);
	}
	
	public void refresh_Title(int difference) {
		int count = mOperator.getThisWeekUnReadCount() - difference;
		if(count > 0) {
//			setHeadTitle(getText(R.string.string_notifications) + String.format(getText(R.string.notification_unread_count).toString(), count));
			setHeadTitle(getText(R.string.message_center) + String.format(getText(R.string.notification_unread_count).toString(), count));
		}else {
//			setHeadTitle(R.string.string_notifications);
		    setHeadTitle(R.string.message_center);
		}
	}

	public void getInformation(boolean isAuto, long fromTime) {
		if(!InformationDownloadService.isDownloadServiceRunning()) {
			Intent service = new Intent(this, InformationDownloadService.class);
			service.putExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE, 
					isAuto ? InformationConstant.NOTIFICATION_DOWNLOAD_MODE_ATUO : InformationConstant.NOTIFICATION_DOWNLOAD_MODE_MANUAL);
			service.putExtra(InformationConstant.NOTIFICATION_REQUEST_PARAM_FROM, fromTime);
			startService(service);  
		}
	}
	
	private InformationBase getCurData(Cursor cursor)
	{
		InformationBase data = new InformationBase();
		data._id = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns._ID));
		data.id = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.M_ID));
		data.type = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TYPE));
		data.appId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.APP_ID));
		data.image_url = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IMAGE_URL));
		data.receiverId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.RECEIVER_ID));
		data.senderId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.SENDER_ID));
		data.date = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.DATE));
		data.title = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TITLE));
		data.uri = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.URI));
		data.lastModified = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LAST_MODIFY));
		data.data = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.DATA));
		try{
		    data.apppickurl = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LONGPRESSURI)));
		}catch(Exception ne){}
		data.read = cursor.getInt(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IS_READ))==1;
		data.body = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.BODY));
		data.body_html = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.BODY_HTML));
		data.title_html = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TITLE_HTML));
		return data;
	}

//	private void setEmptyViewProgressRunning(boolean b) {
//		informationEmptyView.findViewById(R.id.info_empty_refresh).setVisibility(b ? View.GONE : View.VISIBLE);
//		informationEmptyView.findViewById(R.id.info_empty_progress).setVisibility(b ? View.VISIBLE : View.GONE);
//	}
	
	
//	private boolean isLoading;
	private class DownloadServiceStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_STATUS_ACTION)) {
				int status = intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOAD_STATUS, InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FINISHED);
				switch(status) {
				case InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_START:
					begin();
//					mHandler.obtainMessage(VISIBLE_PROGRESS_BUTTON).sendToTarget();
//					mHandler.obtainMessage(VISIBLE_EMPTYVIEW_PROGRESS).sendToTarget();
//					isLoading = true;
					break;
				case InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FINISHED:
					end();
//					mHandler.obtainMessage(INVISIBLE_PROGRESS_BUTTON).sendToTarget();
//					mHandler.obtainMessage(INVISIBLE_EMPTYVIEW_PROGRESS).sendToTarget();
					mHandler.obtainMessage(SET_LISTVIEW_REFRESH_COMPLETE).sendToTarget();
					mHandler.obtainMessage(RELOAD_LIST_ITEM_DATE).sendToTarget();
//					isLoading = false; 
					if(intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE,
									InformationConstant.NOTIFICATION_DOWNLOAD_MODE_ATUO) == InformationConstant.NOTIFICATION_DOWNLOAD_MODE_MANUAL
									&& (intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOADED_COUNT, 0) <= 0)) {
						showCustomToast(R.string.notification_none);
						mHasMore = false;
					} else {
						if(intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOADED_COUNT, 0) < InformationConstant.DEFAULT_NOTIFICATION_COUNT) {
							mHasMore = false;
						}else {
							mHasMore = true;
						}
						
					    getUnProcessedNotification();
					}
					break;
				case InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FAILED:
					end();
//					isLoading = false;
					showCustomToast(R.string.notification_download_failed);
					break;
				default:
					return;
				}
			}
		}
	}

	private class DBContentObserver extends ContentObserver {

		public DBContentObserver(Handler handler) {
			super(handler);
		}

		public void onChange(boolean selfChange) {
            reloadList();
			mHandler.obtainMessage(REFRESH_TITLE).sendToTarget();
		}
	}

	@Override
	protected void createHandler() {
		mHandler = new InforHandler();
	}
	
	private static final int GET_INFORMATION_AUTO          = 1;
	private static final int GET_INFORMATION_MANUAL        = 2;
	private static final int VISIBLE_PROGRESS_BUTTON       = 3;
	private static final int INVISIBLE_PROGRESS_BUTTON     = 4;
//	private static final int VISIBLE_EMPTYVIEW_PROGRESS    = 5;
//	private static final int INVISIBLE_EMPTYVIEW_PROGRESS  = 6;
	private static final int SET_LISTVIEW_REFRESH_COMPLETE = 7;
	private static final int RELOAD_LIST_FOR_WEEK          = 8;
	private static final int RELOAD_LIST_ITEM_DATE         = 9;
	private static final int REFRESH_TITLE                 = 10;
	private static final int DISABLE_ALL_TITLE_BUTTON      = 11;
    private static final int RELOAD_LIST_FOR_ALL           = 12;
    private static final int GET_REQUEST_SUMMARY           = 13;
    private static final int GET_REQUEST_SUMMARY_END       = 14;
	
	class InforHandler extends Handler {
		public void handleMessage(Message msg) {
			int what = msg.what;
			this.removeMessages(what);
			switch(what) {
			case GET_INFORMATION_AUTO:
				getInformation(true, -1);
				break;
			case GET_INFORMATION_MANUAL:
				getInformation(false, msg.getData().getLong("fromTime"));
				break;
			case VISIBLE_PROGRESS_BUTTON:
				showProgressBtn(true);
				break;
			case INVISIBLE_PROGRESS_BUTTON:
				showProgressBtn(false);
				break;
//			case VISIBLE_EMPTYVIEW_PROGRESS:
//				setEmptyViewProgressRunning(true);
//				break;
//			case INVISIBLE_EMPTYVIEW_PROGRESS:
//				setEmptyViewProgressRunning(false);
//				break;
			case SET_LISTVIEW_REFRESH_COMPLETE:
//				informationListView.onRefreshComplete();
				break;
            case RELOAD_LIST_FOR_ALL:
                loadAllMessages(BpcInformationActivity.this.getString(R.string.message_center), "");
                break;
			case RELOAD_LIST_FOR_WEEK:
                loadThisWeekMessages(BpcInformationActivity.this.getString(R.string.message_center), "");
				break;
			case RELOAD_LIST_ITEM_DATE:
				refresh_Without_Requery();
				break;
			case REFRESH_TITLE:
				refresh_Title();
				break;
			case DISABLE_ALL_TITLE_BUTTON:
//				enableMiddleActionBtn(false);
//				enableLeftActionBtn(false);
				enableRightActionBtn(false);
				break;
            case GET_REQUEST_SUMMARY:
                getRequestSummary();
                break;
            case GET_REQUEST_SUMMARY_END:
                end();
                if (msg.getData().getBoolean("RESULT")) {
                    refreshRequestSummary();
                } else {
                    ToastUtil.showOperationFailed(BpcInformationActivity.this, mHandler, false);
                }
                break;
			}
		}
	}

//	@Override
//	public void setLeftMenuPosition() {
//		mPosition = LeftMenuMapping.getPositionForActivity(this);
//		mTitle = getString(R.string.message_center);
//	}

    /**
     *  add category callbacks start
     */
    @Override
    protected void showCorpusSelectionDialog(View view) {
        int location[] = new int[2];
        view.getLocationInWindow(location);
        int x = location[0];
        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);

        ArrayList<SelectionItem> items = getMessageNameArray();
        DialogUtils.showCorpusSelectionDialog(this, x, y, items, circleListItemClickListener);
    }

    AdapterView.OnItemClickListener circleListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                setHeadTitle(item.getText());
//                loadThisWeekMessages(item.getText(), item.getItemId());
                loadAllMessages(item.getText(), item.getItemId());
            }
        }
    };

    ArrayList<SelectionItem> mMessageItems;
    StringBuffer mTypeList;
    private ArrayList<SelectionItem> getMessageNameArray() {
        if (null == mMessageItems) {
            mMessageItems = new ArrayList<SelectionItem>();
            mMessageItems.add(new SelectionItem("", getString(R.string.ntf_label_all_messages)));

            mTypeList = new StringBuffer();
            mMessageItems.add(new SelectionItem("'" + NotificationInfo.NTF_ACCEPT_SUGGEST + "', '" +
                    NotificationInfo.NTF_NEW_FOLLOWER + "', '" + NotificationInfo.NTF_SUGGEST_USER + "'",
                    getString(R.string.ntf_label_friend_suggestion)));
            mTypeList.append("'").append(NotificationInfo.NTF_ACCEPT_SUGGEST).append("'").
                    append(", '").append(NotificationInfo.NTF_NEW_FOLLOWER).append("'").
                    append(", '").append(NotificationInfo.NTF_SUGGEST_USER).append("'");
            mMessageItems.add(new SelectionItem("'" + NotificationInfo.NTF_MY_APP_COMMENT + "', '" +
                    NotificationInfo.NTF_MY_STREAM_COMMENT + "'",
                    getString(R.string.ntf_label_comment_to_me)));
            mTypeList.append(", '").append(NotificationInfo.NTF_MY_APP_COMMENT).append("'").
                    append(", '").append(NotificationInfo.NTF_MY_STREAM_COMMENT).append("'");
            mMessageItems.add(new SelectionItem("'" + NotificationInfo.NTF_MY_APP_LIKE + "', '" +
                    NotificationInfo.NTF_MY_STREAM_LIKE + "'",
                    getString(R.string.ntf_label_like_to_me)));
            mTypeList.append(", '").append(NotificationInfo.NTF_MY_APP_LIKE).append("'").
                    append(", '").append(NotificationInfo.NTF_MY_STREAM_LIKE).append("'");
            mMessageItems.add(new SelectionItem("'" + NotificationInfo.NTF_APP_SHARE + "', '"
                    + NotificationInfo.NTF_OTHER_SHARE + "'",
                    getString(R.string.ntf_label_share_to_me)));
            mTypeList.append(", '").append(NotificationInfo.NTF_APP_SHARE).append("'").
                    append(", '").append(NotificationInfo.NTF_OTHER_SHARE).append("'");
            mMessageItems.add(new SelectionItem("'" + NotificationInfo.NTF_MY_STREAM_RETWEET + "'", getString(R.string.ntf_label_reshare_mine)));
            mTypeList.append(", '").append(NotificationInfo.NTF_MY_STREAM_RETWEET).append("'");
            mMessageItems.add(new SelectionItem("'" + NotificationInfo.NTF_PROFILE_UPDATE + "'", getString(R.string.ntf_label_profile_update)));
            mTypeList.append(", '").append(NotificationInfo.NTF_PROFILE_UPDATE).append("'");

            mMessageItems.add(new SelectionItem("misc", getString(R.string.ntf_label_others)));
        }

        return mMessageItems;
    }

    private String getCategoryQuery(String id) {
        final String queryType;
        if (TextUtils.isEmpty(id)) {
            queryType = "";
        } else if (id.equals("misc")) {
            queryType = Notification.NotificationColumns.TYPE + " NOT IN (" + mTypeList.toString() + ") ";
        } else if (id.contains(",")) {
            queryType = Notification.NotificationColumns.TYPE + " IN (" + id + ") ";
        } else {
            queryType = Notification.NotificationColumns.TYPE + "=" + id;
        }
        return queryType;
    }

    private void loadAllMessages(String label, String id) {
        Log.d(TAG, "loadAllMessages, queried id = " + id);

        setHeadTitle(label);
        final String queryType = getCategoryQuery(id);
        mAdapter.changeCursor(mOperator.loadAll(queryType));
    }

    private void loadThisWeekMessages(String label, String id) {
        Log.d(TAG, "loadThisWeekMessages, queried id = " + id);

        setHeadTitle(label);

        final String queryType = getCategoryQuery(id);
        mAdapter.changeCursor(mOperator.loadThisWeek(queryType));
    }

    /**
     *  add category callback end.
     */

    private void reloadList () {
//        mHandler.obtainMessage(RELOAD_LIST_FOR_WEEK).sendToTarget();
        mHandler.obtainMessage(RELOAD_LIST_FOR_ALL).sendToTarget();
    }
    
    private class GridViewItem
	{
		public Drawable icon;
		public String   displayname;
		public String   requesttypes;
		public int      actionid;
		public int      requestCount;
	}
    
    private class gridViewAdapter extends BaseAdapter {
        private ArrayList<GridViewItem> actions = new ArrayList<GridViewItem>();
        
        public gridViewAdapter(ArrayList<GridViewItem> items) {
        	actions.clear();
        	actions.addAll(items);
        }

        public void alertData(ArrayList<GridViewItem> items) {
        	actions.clear();
        	actions.addAll(items);
        }
        public int getCount() {
            return actions.size();
        }

        public GridViewItem getItem(int pos) {
            return actions.get(pos);
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup arg2) {
            final ViewHolder holder;
            final GridViewItem action = actions.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(BpcInformationActivity.this).inflate(R.layout.ntf_center_item_view, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon_left);
                holder.title = (TextView) convertView.findViewById(R.id.id_title_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.icon.setBackgroundDrawable(action.icon);
            if(action.requestCount > 0) {
            	holder.title.setText(action.displayname + " (" + action.requestCount + ")");
            	holder.title.setTextColor(getResources().getColor(R.color.request_high_light));
            }else {
            	holder.title.setText(action.displayname);
            	holder.title.setTextColor(getResources().getColor(R.color.black));
//            	holder.reqCount.setVisibility(View.GONE);
            }

            return convertView;
        }

        class ViewHolder {
            public ImageView icon;
            public TextView title;
        }
    }
    
    private class RequestItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
            if(gridViewItems.get(position) != null) {
            	startRequestActivity(gridViewItems.get(position).requesttypes);
            }else {
            	Log.i(TAG, "request item is null");
            }
        }
    };
    
    private void startRequestActivity(String types) {
		Intent intent = new Intent(BpcInformationActivity.this, RequestActivity.class);
		intent.putExtra(RequestFragment.REQUEST_TYPES, types);
		startActivity(intent);
	}

	@Override
	public void updateRequestCount(int exchangeRequestCount, String type) {
		Log.d(TAG, "exchangeRequestCount: " + exchangeRequestCount + " type: " + type);
		for(int i=0; i<gridViewItems.size(); i++) {
			GridViewItem item = gridViewItems.get(i);
			if(item.requesttypes.equals(type)) {
				item.requestCount = exchangeRequestCount;
			}
		}
		refreshCount();		
	}

	@Override
	public void requestUpdated(ArrayList<Requests> data) {
		int circle_reqCount = 0;
		int event_reqCount = 0;
		int friends_reqCount = 0;
		int exchange_reqCount = 0;
		for(int i=0; i<data.size(); i++) {
			final Requests tmpreq = data.get(i);
			if(Requests.isCircleRequest(tmpreq.type)) {
				circle_reqCount ++;
			}else if(Requests.isEventRequest(tmpreq.type)) {
				event_reqCount ++;
			}else if(Requests.isFriendRequest(tmpreq.type)) {
				friends_reqCount ++;
			}else if(Requests.REQUEST_TYPE_EXCHANGE_VCARD == tmpreq.type) {
				exchange_reqCount ++;
			}
		}
		mFriendRequestCount = friends_reqCount;
		mEventRequestCount = event_reqCount;
		mCircleRequestCount = circle_reqCount;
		mExchangeCardRequestCount = exchange_reqCount;

		try {
			if(gridViewItems != null && gridViewItems.size() > 0) {
				gridViewItems.get(0).requestCount = mFriendRequestCount;
				gridViewItems.get(1).requestCount = mEventRequestCount;
				gridViewItems.get(2).requestCount = mCircleRequestCount;
				gridViewItems.get(3).requestCount = mExchangeCardRequestCount;
			}
		} catch (Exception e) {
			Log.d(TAG, "request updated throws exception ");
		}
		
		refreshCount();		
	}
	
	private void refreshCount() {
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if(mRequestAdapter != null) {
					mRequestAdapter.alertData(gridViewItems);
					mRequestAdapter.notifyDataSetChanged();
				}
			}
		});
	}

    private void refreshRequestSummary() {
        Resources res = getResources();
        for (GridViewItem item: gridViewItems) {
            if (item.displayname.equals(res.getString(R.string.home_friends))){
                item.requestCount = mRequestMap.get("num_friend");
            } else if (item.displayname.equals(res.getString(R.string.event))) {
                item.requestCount = mRequestMap.get("num_event");
            } else if (item.displayname.equals(res.getString(R.string.user_circles))) {
                item.requestCount = mRequestMap.get("num_circle");
            } else if (item.displayname.equals(res.getString(R.string.address_book_circle))) {
                item.requestCount = mRequestMap.get("num_change_profile");
            } else {
                Log.d(TAG, "unsupport request summary");
            }
        }

        if(mRequestAdapter != null) {
            mRequestAdapter.alertData(gridViewItems);
            mRequestAdapter.notifyDataSetChanged();
        }
    }

	private void registerQiupuRequestListener() {
        if (QiupuService.mRequestsService != null) {
            QiupuService.mRequestsService.regiestRequestListener(BpcExchangeCardActivity.class.getName(),
                    this);
            QiupuService.mRequestsService.rescheduleRequests(true);
        } else {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    QiupuService.mRequestsService.regiestRequestListener(
                    		BpcExchangeCardActivity.class.getName(), BpcInformationActivity.this);
                    QiupuService.mRequestsService.rescheduleRequests(true);
                }
            }, 5 * QiupuConfig.A_SECOND);
        }
    }

    private void unRegisterQiupuRequestListener() {
        if(QiupuService.mRequestsService != null) {
            QiupuService.mRequestsService.unRegiestRequestListener(BpcInformationActivity.class.getName());
        }
    }

	View.OnClickListener loadOldInformationListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			loadOlderINformation();
		}
	};
	
	protected void loadOlderINformation() {
		long time = mOperator.getEarliestModifyDate();
		Log.d(TAG, "earliest modify time is: " + time);
		Message msg = mHandler.obtainMessage(GET_INFORMATION_MANUAL);
        msg.getData().putLong("fromTime", time);
        msg.sendToTarget();
    }
	
	@Override
	protected void loadRefresh() {
		mHandler.obtainMessage(GET_INFORMATION_AUTO).sendToTarget();
		if(QiupuService.mRequestsService != null) {
			QiupuService.mRequestsService.rescheduleRequests(true);
		}
	}
	
	private boolean mIsLoading;
	@Override
	protected void uiLoadBegin() {
		super.uiLoadBegin();
		mIsLoading = true;
		setMoreBtnText(true);
	}
	
	@Override
	protected void uiLoadEnd() {
		super.uiLoadEnd();
		mIsLoading = false;
		setMoreBtnText(false);
	}
	
	private void setMoreBtnText(boolean isbegin) {
		if(isbegin) {
			if(mMoreBtn != null) {
				mMoreBtn.setText(getResources().getString(R.string.loading));
				mMoreBtn.setOnClickListener(null);
				mMoreBtn.setBackgroundDrawable(null);
			}
		}else {
			if(mMoreBtn != null) {
				mMoreBtn.setText(getResources().getString(R.string.list_view_more));
				mMoreBtn.setOnClickListener(loadOldInformationListener);
				mMoreBtn.setBackgroundResource(R.drawable.list_selector_background);
			}
		}
	}

    private boolean isGetRequest = false;
    private Object  mLockGetRequestObj = new Object();
    private HashMap<String, Integer> mRequestMap = new HashMap<String, Integer>();

    private void getRequestSummary() {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }

        if (isGetRequest == true) {
            ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
            return;
        }

        synchronized (mLockGetRequestObj) {
            isGetRequest = true;
        }

        begin();

        asyncQiupu.getRequestSummary(AccountServiceUtils.getSessionID(), new TwitterAdapter() {
                public void getRequestSummary(HashMap<String, Integer> requestMap) {
                    Log.d(TAG, "finish getRequestSummary");

                    mRequestMap.clear();
                    mRequestMap.putAll(requestMap);

                    Message msg = mHandler.obtainMessage(GET_REQUEST_SUMMARY_END);
                    msg.getData().putBoolean("RESULT", true);
                    msg.sendToTarget();
                    synchronized (mLockGetRequestObj) {
                        isGetRequest = false;
                    }
                }

                public void onException(TwitterException ex,
                        TwitterMethod method) {
                    synchronized (mLockGetRequestObj) {
                        isGetRequest = false;
                    }
                    Message msg = mHandler.obtainMessage(GET_REQUEST_SUMMARY_END);
                    msg.getData().putBoolean("RESULT", false);
                    msg.sendToTarget();
                }
            });
    }

}