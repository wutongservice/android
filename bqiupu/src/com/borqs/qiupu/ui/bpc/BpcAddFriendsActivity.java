package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.Circletemplate;
import twitter4j.PageInfo;
import twitter4j.UserCircle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.borqs.common.SelectionItem;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.SuggestionListFragment;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.ui.page.CreateCircleMainActivity;
import com.borqs.qiupu.util.ToastUtil;

public class BpcAddFriendsActivity extends BasicNavigationActivity {
    private static final String TAG = "BpcAddFriendsActivity";

    private SuggestionListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLeftNav();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_layout);

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            final boolean needHeader;
            Intent intent = getIntent();
            if (null != intent && null != intent.getScheme()) {
                Log.d(TAG, "scheme = " + intent.getScheme());
                needHeader = false;
                setHeadTitle(R.string.you_may_know);
            } else {
                setHeadTitle(R.string.find_friends);
                needHeader = true;
            }
            
            overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);
            mFragment = SuggestionListFragment.newInstance(needHeader);
            fragmentTransaction.add(R.id.request_container, mFragment);
            fragmentTransaction.commit();
        }

//        showRightActionBtn(false);
    }

    @Override
    protected void loadSearch() {
    	showSearhView();
//        gotoSearchActivity();
    }

    @Override
    protected void loadRefresh() {
        if (null != mFragment && mFragment.isVisible()) {
            mFragment.loadRefresh();
        }
    }

    @Override
    protected void createHandler() {
    }

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.find_friends);
//    }
//    
//    @Override
//    public boolean onQueryTextSubmit(String query) {
//    	Log.d(TAG, "IntentUtil onQueryTextSubmit: " + query);
//    	if(query != null && query.length() > 0) {
//			IntentUtil.startPeopleSearchIntent(this, query);
//		}else {
//			Log.d(TAG, "onQueryTextSubmit, query is null " );
//			ToastUtil.showShortToast(this, mHandler, R.string.search_recommend);
//		}
//    	return super.onQueryTextSubmit(query);
//    }
    
    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
    	Log.d(TAG, "onKeyUp: " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			loadSearch();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
    
    View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.menu_title_search)));
        	items.add(new SelectionItem("", getString(R.string.label_refresh)));
        	
        	showCorpusSelectionDialog(items);
        }
    };
    
    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
	    if(mRightActionBtn != null) {
	        int location[] = new int[2];
	        mRightActionBtn.getLocationInWindow(location);
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
    
    private void onCorpusSelected(String value) {
    	if(getString(R.string.menu_title_search).equals(value)) {
    		loadSearch();
    	}else if(getString(R.string.label_refresh).equals(value)) {
    		loadRefresh();
        }else {
            Log.d(TAG, "unsupported item action!");
        }
    }
}
