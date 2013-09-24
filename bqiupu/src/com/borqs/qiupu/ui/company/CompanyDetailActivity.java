package com.borqs.qiupu.ui.company;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Company;
import twitter4j.UserCircle;
import android.content.Context;
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

import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.CompanyDetailFragment;
import com.borqs.qiupu.fragment.CompanyDetailFragment.CompanyDetailFragmentListenerCallBack;
import com.borqs.qiupu.fragment.CompanyFragment;
import com.borqs.qiupu.fragment.CompanyFragment.CompanyFragmentListenerCallBack;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.fragment.StreamListFragment.StreamListFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.BpcFriendsFragmentActivity;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;
import com.borqs.qiupu.ui.circle.EventDetailActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class CompanyDetailActivity extends BasicActivity  
	implements CompanyFragmentListenerCallBack, StreamListFragmentCallBack,CompanyDetailFragmentListenerCallBack {

	private static final String TAG = "CompanyDetailActivity";

	private long company_id = -1;
	private long department_id = -1;
    private int mCurrentpage;

    private Company mCompany;
    private UserCircle mCircle;
    private CompanyFragment mCompanyFragment;
    private CompanyDetailFragment mCompanyDetailFragment;
    
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
        setContentView(R.layout.company_detail);

        parseActivityIntent(getIntent());

        setHeadTitle(getString(R.string.profile));

        showLeftActionBtn(false);
        showMiddleActionBtn(true);
        overrideMiddleActionBtn(R.drawable.actionbar_icon_release_normal , gotoComposeListener);
        overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);
        
        mCurrentFragment = profile_main;
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        mCompanyFragment = new CompanyFragment();
        ft.add(R.id.fragment_content, mCompanyFragment, PROFILE_MAIN_TAG);
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
        if (!TextUtils.isEmpty(url)) {
        	final String cId = BpcApiUtils.parseSchemeValue(intent,
        			BpcApiUtils.SEARHC_KEY_CIRCLEID);
        	department_id = Long.parseLong(cId);
        	mFragmentData.mCircleId = department_id;
        	mCompany = QiupuORM.queryCompanyByCircleId(this,department_id);
        	company_id = mCompany.id;
        }else {
        	mCompany = (Company)intent.getSerializableExtra(Company.COMPANY_INFO);
//        	company_id = intent.getLongExtra(Company.COMPANY_ID, -1);
            company_id = mCompany.id;
            department_id = mCompany.department_id;
        	mFragmentData.mCircleId = department_id;
        }
        
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    };

    @Override
    protected void loadRefresh() {
        Log.d(TAG, "currentpage: " + mCurrentpage);
        if(mCompanyFragment != null) {
        	mCompanyFragment.refreshCompanyInfo();
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
//        	if(mCompany.role > 10) {
//        		
//        		items.add(new SelectionItem("", getString(R.string.create_public_circle_title)));
//        	}
        	items.add(new SelectionItem("", getString(R.string.home_album)));
//        	
//        	if(mCircle.mGroup != null) {
//        		if(mCircle.mGroup.viewer_can_update) {
//        			items.add(new SelectionItem("", getString(R.string.edit_string)));
//        		}
//        	}
//        	
//        	items.add(new SelectionItem("", getString(R.string.public_circle_shortcut)));
//        	
//        	if(mCircle.mGroup != null && PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
//        		items.add(new SelectionItem("", getString(R.string.public_circle_receive_set)));
//        		if(mCircle.mGroup.viewer_can_destroy) {
//        			items.add(new SelectionItem("", getString(R.string.delete)));
//        		}
//        		
//        		if(mCircle.mGroup.viewer_can_quit) {
//        			items.add(new SelectionItem("", getString(R.string.public_circle_exit)));
//        			isDirectExit = true;
//        		}else {
//        			if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
//        					|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
//        				if(mCircle.mGroup.can_member_quit == UserCircle.VALUE_ALLOWED) {
//        					items.add(new SelectionItem("", getString(R.string.public_circle_exit)));
//        					isDirectExit = false;
//        				}
//        			}
//        		}
//        	}
        	
        	showCorpusSelectionDialog(items);
        }
    };
    
    View.OnClickListener gotoComposeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	gotoComposeAcitvity();
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
	
//	public static void startPublicCircleDetailIntent(Context context, UserCircle circle) {
//        if (null != context && null != circle) {
//            final Intent intent = new Intent(context, CompanyDetailActivity.class);
//
//            Bundle bundle = new Bundle();
//            bundle.putString(CircleUtils.CIRCLE_NAME,
//                    CircleUtils.getLocalCircleName(context, circle.circleid, circle.name));
//            bundle.putLong(CircleUtils.CIRCLE_ID, circle.circleid);
//            intent.putExtras(bundle);
//            context.startActivity(intent);
//        }
//    }
	
	private void gotoComposeAcitvity() {
		if(mCompany == null) return;
		HashMap<String, String> receiverMap = new HashMap<String, String>();
		String receiverid = "#" +  mCompany.department_id;
		receiverMap.put(receiverid, mCompany.name);
		if (mCompany != null && mCompany.person_count <= 0) {
          IntentUtil.startComposeIntent(CompanyDetailActivity.this, receiverid, false, receiverMap);
      } else {
          IntentUtil.startComposeIntent(CompanyDetailActivity.this, receiverid, true, receiverMap);
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
                    ToastUtil.showOperationFailed(CompanyDetailActivity.this, mHandler, true);
                }
                break;
            }
            }
        }
    }
	

    public void onCorpusSelected(String value) {
        if(getString(R.string.label_refresh).equals(value)) {
            loadRefresh();
//        }else if(getString(R.string.public_circle_exit).equals(value)) {
//            if(isDirectExit) {
//                DialogUtils.showConfirmDialog(this, R.string.public_circle_exit, R.string.public_circle_exit_message, 
//                        R.string.label_ok, R.string.label_cancel, ExitListener);
//            }else {
//                DialogUtils.showExitPublicDialog(this,  R.string.public_circle_exit_toast, R.string.public_circle_exit_prompt, ExitNeutralListener, ExitNegativeListener);
//            }
//        } 
//        else if(getString(R.string.public_circle_shortcut).equals(value))
//        {
//        	final Intent intent = createShortcutIntent(mCircle.uid,  mCircle.name, mCircle.profile_image_url);            
//            Activity a = getParent() == null ? this : getParent();
//            a.sendBroadcast(intent);         
//        }else if(getString(R.string.public_circle_receive_set).equals(value)) {
//        	IntentUtil.gotoEditPublicCircleActivity(this, mCircle.name, mCircle, EditPublicCircleActivity.edit_type_receive_set);
//        }else if(getString(R.string.edit_string).equals(value)) {
//        	Intent intent = new Intent(this, EditEventActivity.class);
//        	intent.putExtra(CircleUtils.CIRCLEINFO, mCircle);
//        	startActivity(intent);
        }else if(getString(R.string.home_album).equals(value)) {
        	IntentUtil.startAlbumIntent(this, mCompany.department_id,mCompany.name);
//        }else if(getString(R.string.delete).equals(value)) {
//        	int dialogMes = -1;
//        	if(QiupuConfig.isEventIds(mCircle.circleid)) {
//        		dialogMes = R.string.delete_event_message;
//        	}else {
//        		dialogMes = R.string.delete_public_circle_message;
//        	}
//        	DialogUtils.showConfirmDialog(this, R.string.delete, dialogMes, 
//                    R.string.label_ok, R.string.label_cancel, ExitNeutralListener);
        }else if (getString(R.string.create_public_circle_title).equals(value)) {
        	IntentUtil.gotoEditPublicCircleActivity(this,String.valueOf(company_id), EditPublicCircleActivity.type_create);
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
    			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_public_circle);	    	    
    		}
    		catch(Exception ne){}		    
    	}
    	
    	if(bmp == null)
    	{
    		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_public_circle);
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
    
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
//        if(requestCode == in_member_selectcode) {
//            if(resultCode == Activity.RESULT_OK) {
//                String selectUserIds = data.getStringExtra("toUsers");
//                Log.d(TAG, "onActivityResult: " + selectUserIds);
//                if(TextUtils.isEmpty(selectUserIds)) {
//                    Log.d(TAG, "select null , do nothing ");
//                }else {
//                    deletePublicCirclePeople(mCircle.circleid, String.valueOf(AccountServiceUtils.getBorqsAccountID()), selectUserIds);
//                }
//            }
//        }else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
    
//    private DialogInterface.OnClickListener ExitListener = new DialogInterface.OnClickListener(){
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            deletePublicCirclePeople(mCircle.circleid, String.valueOf(AccountServiceUtils.getBorqsAccountID()), null);
//        }
//    };
//    
//    private DialogInterface.OnClickListener ExitNeutralListener = new DialogInterface.OnClickListener(){
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            deleteCircleFromServer(String.valueOf(mCircle.circleid), mCircle.name, mCircle.type);
//        }
//    };
    
//    private DialogInterface.OnClickListener ExitNegativeListener = new DialogInterface.OnClickListener(){
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            Intent intent = new Intent(CompanyDetailActivity.this, PublicCircleMemberPickActivity.class);
//            intent.putExtra(CircleUtils.CIRCLE_ID, mCircle.circleid);
//            startActivityForResult(intent, in_member_selectcode);
//        }
//    };

	
	
	
	
	

	@Override
	public void gotoProfileDetail() {
		if(mCurrentFragment != profile_detail){
			showSlideToggle(overrideSlideToggleClickListener);
			mCurrentFragment = profile_detail;
			FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
			if(mCompanyFragment != null && !mCompanyFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mCompanyFragment).commit();
			}
			
			mCompanyDetailFragment = (CompanyDetailFragment) mFragmentManager.findFragmentByTag(PROFILE_DETAIL_TAG);
			if(mCompanyDetailFragment == null){
				mCompanyDetailFragment = new CompanyDetailFragment();
				mFragmentTransaction.add(R.id.fragment_content, mCompanyDetailFragment, PROFILE_DETAIL_TAG);
			}else {
				mFragmentTransaction.show(mCompanyDetailFragment);
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
			if(mCompanyDetailFragment != null && !mCompanyDetailFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mCompanyDetailFragment).commit();
			}
			
			mCompanyFragment = (CompanyFragment) mFragmentManager.findFragmentByTag(PROFILE_MAIN_TAG);
			
			if(mCompanyFragment == null){
				mCompanyFragment = new CompanyFragment();
				mFragmentTransaction.add(R.id.fragment_content, mCompanyFragment, PROFILE_MAIN_TAG);
			}else {
				mFragmentTransaction.show(mCompanyFragment);
			}
			mFragmentTransaction.commit();
		} else {
			super.onBackPressed();
		}
	}

	
	@Override
	public void getCompanyInfoFragment(CompanyFragment fragment) {
		mCompanyFragment = fragment;
		
	}
	
	@Override
	public void onBackPressed() {
	    handlerBackKey(true);
	}


	@Override
	public Company getCompanyInfo() {
		return mCompany;
	}

	@Override
	public void refreshCompanyInfo(Company company) {
		mCompany = company;
	    if(mCompanyDetailFragment != null) {
	    	mCompanyDetailFragment.refreshCompany(company);
	    }
		
	}

	@Override
	public void getCompanyDetailInfoFragment(CompanyDetailFragment fragment) {
		mCompanyDetailFragment = fragment;
		
	}

	@Override
	public Company getCompanyDetailInfo() {
		return mCompany;
	}
}