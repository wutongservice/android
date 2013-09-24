package com.borqs.qiupu.ui.bpc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.ui.BasicActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BpcPostsFilterActivity extends BasicActivity {
    final String TAG = "Qiupu.BpcPostsFilterActivity";

    protected static final String FILTER_TYPE_KEY = "FILTER_TYPE_KEY";
    protected static final String FILTER_APP_KEY = "FILTER_APP_KEY";

    private ListView filterTypeListView;
    private Button    select_ok;
    private Button    select_cancel;

    final static int appFilterToType[] = {
            BpcApiUtils.ALL_TYPE_POSTS,
            BpcApiUtils.TEXT_POST | BpcApiUtils.MAKE_FRIENDS_POST,
            BpcApiUtils.IMAGE_POST,
            BpcApiUtils.ONLY_APK_POST,
            BpcApiUtils.ONLY_BOOK_POST,
            BpcApiUtils.ONLY_MUSIC_POST,
            BpcApiUtils.LINK_POST
    };

    ImageView selectAllButton;
    boolean selectall;

    private StreamTypeFilterAdapter mFilterAdapter;
    private int mInitType;
    private int mInitApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(QiupuConfig.LOGD)Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stream_filter_activity);
		
		setHeadTitle(R.string.stream_filter);

		Intent intent = getIntent();
		
		select_ok = (Button)this.findViewById(R.id.select_ok);
		select_cancel = (Button)this.findViewById(R.id.select_cancel);

        showLeftActionBtn(false);
        showMiddleActionBtn(false);

	    selectAllButton = (ImageView)findViewById(R.id.head_action_right);

		select_ok.setOnClickListener(doSelectClick);
		select_cancel.setOnClickListener(doCancel);
        selectAllButton.setOnClickListener(selectAllListener);
		
        mInitType = intent.getIntExtra(BpcApiUtils.SEARCH_KEY_TYPE, -1);
        final int filterType = intent.getIntExtra(FILTER_TYPE_KEY, -1);
        mInitApp = intent.getIntExtra(BpcApiUtils.SEARCH_KEY_APPID, -1);

        if (mInitType < 0 && mInitApp < 0) {
            filterTypeListView = (ListView) this.findViewById(R.id.stream_type_list);
            filterTypeListView.setDivider(null);
            mFilterAdapter = new StreamTypeFilterAdapter(this, mInitApp, mInitType, filterType);
            filterTypeListView.setAdapter(mFilterAdapter);
            filterTypeListView.setOnItemClickListener(filterItemClick);

            selectall = !mFilterAdapter.isEmptyFilter();
            selectAllButton.setImageResource(selectall ?
                    R.drawable.ic_btn_choice : R.drawable.ic_btn_choice_press);
        } else {
            Log.w(TAG, "onCreate, finish while current mInitType:" + mInitType + ", app:" + mInitApp);
            finish();
        }
	}
	
	AdapterView.OnItemClickListener filterItemClick = new AdapterView.OnItemClickListener()
	{
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
        	if(StreamFilterItemView.class.isInstance(view))
        	{
        		StreamFilterItemView uv = (StreamFilterItemView)view;
        		uv.switchCheck();
        	}
        }
	};

	View.OnClickListener doSelectClick = new View.OnClickListener() {
		public void onClick(View arg0) {
            Intent data = new Intent();
            data.putExtra(BpcApiUtils.SEARCH_KEY_TYPE, mFilterAdapter.getCheckedFilter());
            BpcPostsFilterActivity.this.setResult(Activity.RESULT_OK, data);
            BpcPostsFilterActivity.this.finish();
        }
	};
	
    View.OnClickListener doCancel = new View.OnClickListener() {
		
		public void onClick(View arg0){
			BpcPostsFilterActivity.this.setResult(Activity.RESULT_CANCELED);
			BpcPostsFilterActivity.this.finish();
		}
	};

    private View.OnClickListener selectAllListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            selectall = !selectall;
            mFilterAdapter.setSelectAll(selectall);
            final int count = filterTypeListView.getChildCount();
            View item;
            for (int i = 0; i < count; ++i) {
                item = filterTypeListView.getChildAt(i);
                if (StreamFilterItemView.class.isInstance(item)) {
                    ((StreamFilterItemView) item).setCheckedValue(selectall);
                } else {
                    Log.d(TAG, "selectAllListener, onClick, unexpected view item:" + item);
                }
            }

            selectAllButton.setImageResource(selectall ?
                    R.drawable.ic_btn_choice_press : R.drawable.ic_btn_choice);
        }
	};

	
	@Override
	protected void onDestroy()  {
		super.onDestroy();
	}
	
	
	@Override
	protected void loadRefresh() {
		super.loadRefresh();
		selectall = !selectall;		
		selectAllButton.setImageResource(selectall ? R.drawable.ic_btn_choice_press : R.drawable.ic_btn_choice);
	}
	
	
	@Override
	protected void onPause() {
		FriendsManager.unregisterFriendsServiceListener(getClass().getName());
		super.onPause();
	}

	@Override
	protected void loadSearch() {		
		super.loadSearch();
	}


	@Override
	protected void createHandler() {}

	public void updateUI(int msgcode, Message msg) 
	{
	}

    void changeSelect(int id, boolean isSelected) {
        mFilterAdapter.changeSelect(id, isSelected);
        if (!isSelected && mFilterAdapter.isEmptyFilter()) {
            select_ok.setVisibility(View.GONE);
        } else if (View.VISIBLE != select_ok.getVisibility()) {
            select_ok.setVisibility(View.VISIBLE);
        }
    }

    public static void startActivityForResult(Activity context, int type, int appKey, int filter, int requestCode) {
        Intent intent = new Intent(context, BpcPostsFilterActivity.class);
        intent.putExtra(BpcApiUtils.SEARCH_KEY_TYPE, type);
        intent.putExtra(BpcApiUtils.SEARCH_KEY_APPID, appKey);
        intent.putExtra(BpcPostsFilterActivity.FILTER_TYPE_KEY, filter);
        context.startActivityForResult(intent, requestCode);
    }
}

class StreamTypeFilterAdapter extends BaseAdapter {
    private static final String TAG = "Qiupu.StreamTypeFilterAdapter";
    private Context mContext;
    private List mAllowSetItem = new ArrayList<Integer>();
    private static CharSequence[] strings;

    private boolean [] checkValues;
    private boolean  mSelectedAll;
    
    private int mInitApp;

    public StreamTypeFilterAdapter(BasicActivity activity, int initApp,
                                   int initType, int filterType) {
        super();
        mContext = activity;
        mInitApp = initApp;

        if (initApp < 0) {
            strings = mContext.getResources().getTextArray(R.array.stream_app_key_list);
            checkValues = new boolean[strings.length];

            final int size = BpcPostsFilterActivity.appFilterToType.length;
            final boolean noFilterSet = filterType < 0;
            int type;
            for (int i = 0; i < size; ++i) {
                mAllowSetItem.add(i);
                type = BpcPostsFilterActivity.appFilterToType[i];
                checkValues[i] = noFilterSet || (filterType & type) == filterType;
            }
//        } else {
//            strings = mContext.getResources().getTextArray(R.array.stream_type_list);
//            checkValues = new boolean[strings.length];
//            mSelectedAll = false;
//
//            for (int i = 0; i < checkValues.length; ++i) {
//                if (isBitConsist(initType, i)) {
//                    mAllowSetItem.add(i);
//                }
//            }
//
//            for (int i = 0; i < mAllowSetItem.size(); ++i) {
//                final int id = ((Integer) mAllowSetItem.get(i)).intValue();
//                checkValues[i] = isBitConsist(filterType, id);
//            }
        }
    }

//    static boolean isBitConsist(int base, int offset) {
//        boolean ret = 0x1 == ((base >> offset) & 0x1);
//        return  ret;
//
//    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        CharSequence label = (CharSequence)getItem(position);
        if(label != null)
        {
        	StreamFilterItemView v;
	         if (convertView == null || false == (convertView instanceof StreamFilterItemView))
	         {
	        	 holder = new ViewHolder();
                 final int id = ((Integer)mAllowSetItem.get(position)).intValue();
	             v = new StreamFilterItemView(mContext, id, label, mSelectedAll || checkValues[position]);
	             holder.view = v;
	             v.setTag(holder);
	         }
	         else
	         {
	        	 holder = (ViewHolder)convertView.getTag();
	        	 v = holder.view;
		         v.setUserItem(position, label, mSelectedAll || checkValues[position]);
	         }
	         return v;
        }


        return null;
    }

    public int getCount() {
        return mAllowSetItem.size();
//        int count = 0;
//        int value = mTypeFilter;
//        while (value > 0) {
//            count ++;
//            value >>= 1;
//        }
//
//        return count;
    }

    public Object getItem(int position) {
    	final int cursorSize = null == strings ? 0 : getCount();
    	if(position >= 0 && position < cursorSize) {
            Integer index = (Integer)mAllowSetItem.get(position);
    		return strings[index.intValue()];
    	}
        return null;
    }

    public long getItemId(int position) {
        return ((Integer)mAllowSetItem.get(position)).intValue();
    }

    static class ViewHolder
	{
		public StreamFilterItemView view;
	}

    int getCheckedFilter() {
        int ret = 0;
        final int len = checkValues.length;
        int index;
        for (int i = 0; i < len; ++i) {
            if (checkValues[i]) {
                index = ((Integer)mAllowSetItem.get(i)).intValue();
                if (0 == ret) {
                    ret = BpcPostsFilterActivity.appFilterToType[index];
                } else {
                    ret |= (BpcPostsFilterActivity.appFilterToType[index]);
                }
            }
        }
        return ret;
    }

    void changeSelect(int id, boolean isSelected) {
        final int index = mAllowSetItem.indexOf(id);
        checkValues[index] = isSelected;
    }

    void setSelectAll(boolean selected) {
        mSelectedAll = selected;
        for (int i = 0; i < checkValues.length; ++i) {
            checkValues[i] = mSelectedAll;
        }
    }

    public boolean isEmptyFilter() {
        boolean ret = true;
        for (int i = 0; i < checkValues.length; ++i) {
            if (checkValues[i]) {
                ret = false;
                break;
            }
        }
        return ret;
    }
}

class StreamFilterItemView extends SNSItemView
{
	private final String TAG="StreamFilterItemView";

	private TextView  username;
	private CheckBox  chekbox;

    private int mId;
    private CharSequence mLabel;
    private boolean mChecked;

	public StreamFilterItemView(Context context, int id, CharSequence label, boolean checked) {
		super(context);
		mContext = context;
		mId = id;
        mLabel = label;
        mChecked = checked;

		init();
	}
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		init();
	}

	private void init()
	{
		Log.d(TAG,  "init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();

		//child 1
		View v  = factory.inflate(R.layout.user_select_list_item, null);
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int)mContext.getResources().getDimension(R.dimen.filter_list_item_height)));
        addView(v);

		chekbox    = (CheckBox)v.findViewById(R.id.user_check);
		username   = (TextView)v.findViewById(R.id.user_name);
		v.findViewById(R.id.user_icon).setVisibility(View.GONE);


		chekbox.setOnClickListener(stOnClik);
		setUI();
	}

	private void setUI()
	{
		username.setText(mLabel);
		chekbox.setChecked(mChecked);
	}

	public void setUserItem(int id, CharSequence label, boolean checked)
	{
        mId = id;
        mLabel = label;
        mChecked = checked;
	    setUI();
	}

	public void switchCheck() {
        setCheckedValue(!mChecked);
    }

    public void setCheckedValue (boolean value) {
        mChecked = value;
		chekbox.setChecked(mChecked);
		 if(BpcPostsFilterActivity.class.isInstance(mContext)) {
			 BpcPostsFilterActivity re = (BpcPostsFilterActivity) mContext;
	            re.changeSelect(mId, mChecked);
	        }
		Log.d(TAG, "onClick select =" + mChecked);
	}

	View.OnClickListener stOnClik = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			switchCheck();
		}
	};

	@Override
	public String getText()
	{
		return null == mLabel ? "" : mLabel.toString();
	}
}
