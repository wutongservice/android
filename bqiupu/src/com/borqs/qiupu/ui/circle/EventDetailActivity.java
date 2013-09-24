package com.borqs.qiupu.ui.circle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.PublicCircleRequestUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.EventInfoDetailFragment;
import com.borqs.qiupu.fragment.EventInfoDetailFragment.EventDetailFragmentListenerCallBack;
import com.borqs.qiupu.fragment.EventInfoFragment;
import com.borqs.qiupu.fragment.EventInfoFragment.EventInfoFragmentListenerCallBack;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.fragment.StreamListFragment.StreamListFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CalendarMappingUtils;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class EventDetailActivity extends BasicActivity  implements
	EventInfoFragmentListenerCallBack, StreamListFragmentCallBack, EventDetailFragmentListenerCallBack {

	private static final String TAG = "EventDetailActivity";

    private int mCurrentpage;

    private UserCircle mCircle;
    private EventInfoFragment mEventInfoFragment;
    private EventInfoDetailFragment mEventInfoDetailFragment;
    
    StreamListFragment.MetaData mFragmentData = new StreamListFragment.MetaData();
    public static final int in_member_selectcode = 5555;
    

    private int mCurrentFragment;
    private static final int profile_main = 1;
    private static final int profile_detail = 2;
    private FragmentManager mFragmentManager;
    
    private final static String PROFILE_MAIN_TAG = "PROFILE_MAIN_TAG";
   	private final static String PROFILE_DETAIL_TAG = "PROFILE_DETAIL_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_main);

        parseActivityIntent(getIntent());

        setHeadTitle(getString(R.string.profile));

        showLeftActionBtn(false);
        refreshPostIcon();
        overrideMiddleActionBtn(R.drawable.actionbar_icon_release_normal , gotoComposeListener);
        overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);
        
        mCurrentFragment = profile_main;
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        mEventInfoFragment = new EventInfoFragment();
        ft.add(R.id.fragment_content, mEventInfoFragment, PROFILE_MAIN_TAG);
        ft.commit();
    }

    @Override
    protected void createHandler() {
    	mHandler = new MainHandler();
    }

    @Override
    protected void loadSearch()
    {
        gotoSearchActivity();
    }

    private void parseActivityIntent(Intent intent) {
        String url = getIntentURL(intent);
        if (TextUtils.isEmpty(url)) {
            String requestName = null;
            Bundle bundle = intent.getExtras();
            requestName = bundle.getString(CircleUtils.CIRCLE_NAME);
            mFragmentData.mCircleId = bundle.getLong(CircleUtils.CIRCLE_ID, -1);
            mFragmentData.mFragmentTitle = getString(R.string.circle_detail_post);

//            setHeadTitle(requestName);
        }else {
            final String circleId = BpcApiUtils.parseSchemeValue(intent,
                    BpcApiUtils.SEARHC_KEY_CIRCLEID);
            mFragmentData.mCircleId = Long.parseLong(circleId);
        }
        
        mCircle = orm.queryOneCircleWithGroup(mFragmentData.mCircleId);
        if (mCircle == null){
            mCircle = new UserCircle();
            mCircle.circleid = mFragmentData.mCircleId;
        }
    }
    
    private void refreshPostIcon() {
    	if(mCircle.mGroup != null) {
        	showMiddleActionBtn(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group));
        }else {
        	showMiddleActionBtn(false);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    };

    @Override
    protected void loadRefresh() {
        Log.d(TAG, "currentpage: " + mCurrentpage);
        if(mEventInfoFragment != null) {
        	mEventInfoFragment.refreshEventInfo();
        }
    }

    @Override
    protected void uiLoadEnd() {
        showProgressBtn(false);
        showLeftActionBtn(false);
    }

    @Override
    public StreamListFragment.MetaData getFragmentMetaData(int index) {
        return mFragmentData;
    }

    @Override
    public String getSerializeFilePath() {
        return QiupuHelper.circle + mFragmentData.mCircleId;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private boolean isDirectExit = false;
	View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.label_refresh)));
        	items.add(new SelectionItem("", getString(R.string.home_album)));
        	
        	if(mCircle.mGroup != null) {
        		if(mCircle.mGroup.viewer_can_update) {
        			items.add(new SelectionItem("", getString(R.string.edit_string)));
        		}
        	}
        	
        	items.add(new SelectionItem("", getString(R.string.public_circle_shortcut)));
        	
        	if(mCircle.mGroup != null && PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
        		items.add(new SelectionItem("", getString(R.string.public_circle_receive_set)));
        		if(mCircle.mGroup.viewer_can_destroy) {
        			items.add(new SelectionItem("", getString(R.string.delete)));
        		}
        		
        		if(mCircle.mGroup.viewer_can_quit) {
        			items.add(new SelectionItem("", getString(R.string.public_circle_exit)));
        			isDirectExit = true;
        		}else {
        			if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
        					|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
        				if(mCircle.mGroup.can_member_quit == UserCircle.VALUE_ALLOWED) {
        					items.add(new SelectionItem("", getString(R.string.public_circle_exit)));
        					isDirectExit = false;
        				}
        			}
        		}
        	}
        	
        	showCorpusSelectionDialog(items);
        }
    };
    
    View.OnClickListener gotoComposeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
//        	gotoComposeAcitvity();
        	startComposeActivity();
        }
    };

    private void startComposeActivity() {
        boolean isAdmin = false;
        if (mCircle != null) {
        	HashMap<String, String> receiverMap = new HashMap<String, String>();
        	String receiverid = "";
        	receiverMap.put(String.valueOf(mFragmentData.mCircleId), mCircle.name);
        	if(mCircle.mGroup != null && mCircle.mGroup.creator != null) {
        		isAdmin = PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
        				|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group);
        	}
//        	long parent_id = -1;
//        	if(mCircle.mGroup != null) {
//        		parent_id = mCircle.mGroup.parent_id;
//        	}
        	final String homeid = QiupuORM.getSettingValue(this, QiupuORM.HOME_ACTIVITY_ID);
        	long homeScene = TextUtils.isEmpty(homeid) ? -1 : Long.parseLong(homeid);
        	
        	IntentUtil.startComposeActivity(this, receiverid, true, isAdmin, receiverMap, homeScene, mCircle.circleid);
        }
    }

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

	@Override
	public UserCircle getCircleInfo() {
		return mCircle;
	}
	
	public static void startPublicCircleDetailIntent(Context context, UserCircle circle) {
        if (null != context && null != circle) {
            final Intent intent = new Intent(context, EventDetailActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString(CircleUtils.CIRCLE_NAME,
                    CircleUtils.getLocalCircleName(context, circle.circleid, circle.name));
            bundle.putLong(CircleUtils.CIRCLE_ID, circle.circleid);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

	private void gotoComposeAcitvity() {
		UserCircle circle = orm.queryOneCircle(-1, mFragmentData.mCircleId);
		HashMap<String, String> receiverMap = new HashMap<String, String>();
		String receiverid = "#" +  mFragmentData.mCircleId;
		receiverMap.put(receiverid, mCircle.name);
        if (circle != null && circle.memberCount <= 0) {
//            UserCircleDetailActivity.this.showShortToast(R.string.no_user_in_circle);
            IntentUtil.startComposeIntent(EventDetailActivity.this, receiverid, false, receiverMap);
        } else {
            IntentUtil.startComposeIntent(EventDetailActivity.this, receiverid, true, receiverMap);
        }
	}
	
	private final static int INVIT_EPEOPLE_END = 101;
	private final static int EXIT_CIRCLE_END = 102;
	private final static int CIRCLE_DELETE_END = 103;
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case INVIT_EPEOPLE_END: {
            	try {
                	dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                } catch (Exception ne) {
                }
            	boolean ret = msg.getData().getBoolean(RESULT, false);
            	if (ret == true) {
            		Log.d(TAG, "invite people end ");
                    showOperationSucToast(true);
                } else {
                    showOperationFailToast("", true);
                }
            	break;
            }
            case EXIT_CIRCLE_END: {
                try {
                    dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                } catch (Exception ne) {
                }
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret == true) {
                    Log.d(TAG, "exit circle end ");
                    showOperationSucToast(true);
                    QiupuHelper.updateActivityUI(null);
                    finish();
                } else {
                    showOperationFailToast("", true);
                }
                break;
            }case CIRCLE_DELETE_END: {
                try {
                    dismissDialog(DIALOG_DELETE_CIRCLE_PROCESS);
                } catch (Exception ne) {}
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret)
                {
                    showOperationSucToast(true);
                    QiupuHelper.updateActivityUI(null);
                    finish();
                } else {
                    ToastUtil.showOperationFailed(EventDetailActivity.this, mHandler, true);
                }
                break;
            }
            }
        }
    }
	
	boolean inDeletePeople;
    Object mLockDeletePeople = new Object();
    private void deletePublicCirclePeople(final long circleid, final String userids, final String admins) {
        if (inDeletePeople == true) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockDeletePeople) {
            inDeletePeople = true;
        }
        showDialog(DIALOG_SET_CIRCLE_PROCESS);
        
        asyncQiupu.deletePublicCirclePeople(AccountServiceUtils.getSessionID(), circleid, userids, admins, new TwitterAdapter() {
            public void deletePublicCirclePeople(boolean result) {
                Log.d(TAG, "finish deletePublicCirclePeople=" + result);
                if(result) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                          //delete circle in DB
                            orm.deleteCircleByCricleId(AccountServiceUtils.getBorqsAccountID(), String.valueOf(circleid));
                            //update user info
//                            orm.updateUserInfoInCircle(AccountServiceUtils.getBorqsAccountID(), circleid, circleName);
                            CalendarMappingUtils.removeCalendarEvent(EventDetailActivity.this, orm.getEventCalendarid(circleid));
                            orm.deleteEventsCalendar(circleid);
                        }
                    });
                }
                
                Message msg = mHandler.obtainMessage(EXIT_CIRCLE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                synchronized (mLockDeletePeople) {
                    inDeletePeople = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                Message msg = mHandler.obtainMessage(EXIT_CIRCLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
                synchronized (mLockDeletePeople) {
                    inDeletePeople = false;
                }
            }
        });
    }
    boolean inDeleteCircle;
    Object mLockdeleteCircle = new Object();
    
    public void deleteCircleFromServer(final String circleid, final String circleName, final int type) {
        if (inDeleteCircle == true) {
            Toast.makeText(this, R.string.string_in_processing,Toast.LENGTH_SHORT).show();
            return ;
        }
        
        synchronized (mLockdeleteCircle) {
            inDeleteCircle = true;
        }
        showDialog(DIALOG_DELETE_CIRCLE_PROCESS);
        asyncQiupu.deleteCircle(AccountServiceUtils.getSessionID(), circleid, type, new TwitterAdapter() {
            public void deleteCircle(boolean suc) {
                Log.d(TAG, "finish deleteCircle=" + suc);
                if(suc) {
                    //delete circle in DB
                    orm.deleteCircleByCricleId(AccountServiceUtils.getBorqsAccountID(), circleid);
                    //update user info
                    orm.updateUserInfoInCircle(AccountServiceUtils.getBorqsAccountID(), circleid, circleName);
                    CalendarMappingUtils.removeCalendarEvent(EventDetailActivity.this, orm.getEventCalendarid(Long.parseLong(circleid)));
                    orm.deleteEventsCalendar(Long.parseLong(circleid));
                }
                
                Message msg = mHandler.obtainMessage(CIRCLE_DELETE_END);
                msg.getData().putBoolean(RESULT, suc);
                msg.sendToTarget();
                
                synchronized (mLockdeleteCircle) {
                    inDeleteCircle = false;
                }
            }
            
            public void onException(TwitterException ex,TwitterMethod method) {
                synchronized (mLockdeleteCircle) {
                    inDeleteCircle = false;
                }
                
                Message msg = mHandler.obtainMessage(CIRCLE_DELETE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
	@Override
	public void refreshCircleInfo(UserCircle circle) {
	    mCircle = circle;
	    if(mEventInfoDetailFragment != null) {
	    	mEventInfoDetailFragment.refreshEvent(circle);
	    }
	    refreshPostIcon();
//		setHeadTitle(mCircle.name);
	}

    public void onCorpusSelected(String value) {
        if(getString(R.string.label_refresh).equals(value)) {
            loadRefresh();
        }else if(getString(R.string.public_circle_exit).equals(value)) {
            if(isDirectExit) {
                DialogUtils.showConfirmDialog(this, R.string.public_circle_exit, R.string.event_exit_message, 
                        R.string.label_ok, R.string.label_cancel, ExitListener);
            }else {
                DialogUtils.showExitPublicDialog(this,  R.string.public_circle_exit_toast, R.string.event_exit_prompt, ExitNeutralListener, ExitNegativeListener);
            }
        } 
        else if(getString(R.string.public_circle_shortcut).equals(value))
        {
        	final Intent intent = createShortcutIntent(mCircle.uid,  mCircle.name, mCircle.mGroup.coverUrl);            
            Activity a = getParent() == null ? this : getParent();
            a.sendBroadcast(intent);         
        }else if(getString(R.string.public_circle_receive_set).equals(value)) {
        	IntentUtil.gotoEditPublicCircleActivity(this, mCircle.name, mCircle, EditPublicCircleActivity.edit_type_receive_set);
        }else if(getString(R.string.edit_string).equals(value)) {
        	Intent intent = new Intent(this, EditEventActivity.class);
        	intent.putExtra(CircleUtils.CIRCLEINFO, mCircle);
        	startActivity(intent);
        }else if(getString(R.string.home_album).equals(value)) {
        	IntentUtil.startAlbumIntent(this, mCircle.circleid,mCircle.name);
        }else if(getString(R.string.delete).equals(value)) {
        	int dialogMes = -1;
        	if(QiupuConfig.isEventIds(mCircle.circleid)) {
        		dialogMes = R.string.delete_event_message;
        	}else {
        		dialogMes = R.string.delete_public_circle_message;
        	}
        	DialogUtils.showConfirmDialog(this, R.string.delete, dialogMes, 
                    R.string.label_ok, R.string.label_cancel, ExitNeutralListener);
        }
    }
    
    static Bitmap centerToFit(Bitmap bitmap, int width, int height, Context context) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        if (bitmapWidth < width || bitmapHeight < height) {
            int color = context.getResources().getColor(R.color.light_blue);

            Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width : bitmapWidth,
                    bitmapHeight < height ? height : bitmapHeight, Bitmap.Config.RGB_565);
            centered.setDensity(bitmap.getDensity());
            Canvas canvas = new Canvas(centered);
            canvas.drawColor(color);
            canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(2,2,centered.getWidth()-2, centered.getHeight()-2),
                    null);

            bitmap = centered;
        }

        return bitmap;
    }

    private Intent createShortcutIntent(long uid, String title, String pic_url) {
    	final Intent i = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
    	
    	final Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);	    
    	shortcutIntent.setData(Uri.parse("borqs://profile/details?uid="+uid));
    	i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    	i.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
    	
    	//this is photo
    	Bitmap bmp = null;
    	String filepath = QiupuHelper.getImagePathFromURL_noFetch(pic_url);
    	if(new File(filepath).exists())
    	{
    		try{				    
    			bmp = BitmapFactory.decodeFile(filepath);	    	    
    		}
    		catch(Exception ne){}
    	}
    	else
    	{
    		try{				    
    			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.home_screen_create_event_icon);	    	    
    		}
    		catch(Exception ne){}		    
    	}
    	
    	if(bmp == null)
    	{
    		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.home_screen_create_event_icon);
    	}
    	
    	//resize the bmp to 60dip
    	final int  width = (int)(48*getResources().getDisplayMetrics().density);
    	bmp = centerToFit(bmp, width, width, this);
    	
    	Bitmap favicon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_bpc_launcher);
    	
    	// Make a copy of the regular icon so we can modify the pixels.        
    	Bitmap copy = bmp.copy(Bitmap.Config.ARGB_8888, true);
    	Canvas canvas = new Canvas(copy);
    	
    	// Make a Paint for the white background rectangle and for
    	// filtering the favicon.
    	Paint p = new Paint(Paint.ANTI_ALIAS_FLAG
    			| Paint.FILTER_BITMAP_FLAG);
    	p.setStyle(Paint.Style.FILL_AND_STROKE);
    	p.setColor(Color.WHITE);
    	
    	final DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	final float density = metrics.density;
    	
    	// Create a rectangle that is slightly wider than the favicon
    	final float iconSize = 8*density; // 16x16 favicon
    	final float padding = 1*density;   // white padding around icon
    	final float rectSize = iconSize + 2 * padding;
    	final float y = bmp.getHeight() - rectSize;
    	RectF r = new RectF(0, y, rectSize, y + rectSize);
    	
    	// Draw a white rounded rectangle behind the favicon
    	canvas.drawRoundRect(r, 2, 2, p);
    	
    	// Draw the favicon in the same rectangle as the rounded rectangle
    	// but inset by the padding (results in a 16x16 favicon).
    	r.inset(padding, padding);
    	canvas.drawBitmap(favicon, null, r, p);
    	i.putExtra(Intent.EXTRA_SHORTCUT_ICON, copy);
    	
    	// Do not allow duplicate items
    	i.putExtra("duplicate", false);
    	return i;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
        if(requestCode == in_member_selectcode) {
            if(resultCode == Activity.RESULT_OK) {
                String selectUserIds = data.getStringExtra("toUsers");
                Log.d(TAG, "onActivityResult: " + selectUserIds);
                if(TextUtils.isEmpty(selectUserIds)) {
                    Log.d(TAG, "select null , do nothing ");
                }else {
                    deletePublicCirclePeople(mCircle.circleid, String.valueOf(AccountServiceUtils.getBorqsAccountID()), selectUserIds);
                }
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private DialogInterface.OnClickListener ExitListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            deletePublicCirclePeople(mCircle.circleid, String.valueOf(AccountServiceUtils.getBorqsAccountID()), null);
        }
    };
    
    private DialogInterface.OnClickListener ExitNeutralListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            deleteCircleFromServer(String.valueOf(mCircle.circleid), mCircle.name, mCircle.type);
        }
    };
    
    private DialogInterface.OnClickListener ExitNegativeListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(EventDetailActivity.this, PublicCircleMemberPickActivity.class);
            intent.putExtra(CircleUtils.CIRCLE_ID, mCircle.circleid);
            startActivityForResult(intent, in_member_selectcode);
        }
    };

	@Override
	public void getEventInfoFragment(EventInfoFragment fragment) {
		mEventInfoFragment = fragment;
	}

	@Override
	public void gotoProfileDetail() {
		if(mCurrentFragment != profile_detail){
			showSlideToggle(overrideSlideToggleClickListener);
			mCurrentFragment = profile_detail;
			FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
			if(mEventInfoFragment != null && !mEventInfoFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mEventInfoFragment).commit();
			}
			
			mEventInfoDetailFragment = (EventInfoDetailFragment) mFragmentManager.findFragmentByTag(PROFILE_DETAIL_TAG);
			if(mEventInfoDetailFragment == null){
				mEventInfoDetailFragment = new EventInfoDetailFragment();
				mFragmentTransaction.add(R.id.fragment_content, mEventInfoDetailFragment, PROFILE_DETAIL_TAG);
			}else {
				mFragmentTransaction.show(mEventInfoDetailFragment);
			}
			mFragmentTransaction.commit();
		}
	}
	
	View.OnClickListener overrideSlideToggleClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			handlerBackKey(false);
		}
	};
	
	private void handlerBackKey(boolean isBackKey) {
		if(mCurrentFragment == profile_detail){
			mCurrentFragment = profile_main;
			FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
			if(mEventInfoDetailFragment != null && !mEventInfoDetailFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mEventInfoDetailFragment).commit();
			}
			
			mEventInfoFragment = (EventInfoFragment) mFragmentManager.findFragmentByTag(PROFILE_MAIN_TAG);
			
			if(mEventInfoFragment == null){
				mEventInfoFragment = new EventInfoFragment();
				mFragmentTransaction.add(R.id.fragment_content, mEventInfoFragment, PROFILE_MAIN_TAG);
			}else {
				mFragmentTransaction.show(mEventInfoFragment);
			}
			mFragmentTransaction.commit();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void getEventDetailInfoFragment(EventInfoDetailFragment fragment) {
		mEventInfoDetailFragment = fragment;
	}
	
	@Override
	public void onBackPressed() {
	    handlerBackKey(true);
	}

    @Override
    public long getTopStreamTargetId() {
        Log.d(TAG, "getTopStreamTargetId() viewer_can_update = " + mCircle.mGroup.viewer_can_update);

        if (mCircle.mGroup.viewer_can_update) {
            return mCircle.circleid;
        }

        return super.getTopStreamTargetId();
    }

//    @Override
//    protected void setTopListMenuListener(int itemId, View targetView) {
//        if (mCircle.mGroup.viewer_can_update) {
//            if (itemId == R.id.bpc_item_set_top) {
//                setTopList(targetView, true);
//            } else if (itemId == R.id.bpc_item_unset_top) {
//                setTopList(targetView, false);
//            }
//        }
//    }
//
//    private void setTopList(View targetView, boolean setTop) {
//        if (AbstractStreamRowView.class.isInstance(targetView)) {
//            AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
//            String stream_id = streamRowView.getStream().post_id;
//            setTopList(String.valueOf(mCircle.circleid), stream_id, setTop);
//        }
//    }
    
    public long getEventId() {
    	if(mCircle != null) {
    		return mCircle.circleid;
    	}else {
    		return 0;
    	}
    }

}