package com.borqs.qiupu.ui.page;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.MySuggestionProvider;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.page.PageSerachFragment.CallBackPageSearchFragment;
import com.borqs.qiupu.util.StringUtil;


public class PageSearchActivity extends BasicActivity implements TextView.OnEditorActionListener, 
                CallBackPageSearchFragment {
	private static final String TAG = "PageSearchActivity";
	private EditText keyEdit;
    private ImageView btn_clear_text;
    private View center_process;

    private PageSerachFragment mPageSearchFragment;
    private static final String PEOPLE_TAG = "people";

	private MyWatcher watcher = new MyWatcher();
	private FragmentManager mFragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(QiupuConfig.LOGD)Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.bpc_search_ui);
		center_process = findViewById(R.id.center_progress);
		
		keyEdit = (EditText) findViewById(R.id.search_span);
        if (null != keyEdit) {
            keyEdit.addTextChangedListener(watcher);
            keyEdit.setOnEditorActionListener(this);
        }

        View search_do = findViewById(R.id.search_do);
        search_do.setOnClickListener(this);

        btn_clear_text = (ImageView)findViewById(R.id.btn_clear_text);
		btn_clear_text.setOnClickListener(this);

		mFragmentManager = getSupportFragmentManager();
		
		parseIntentScheme(getIntent());
		
		initUI();
		
        handleIntent(getIntent());
	}

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            keyEdit.setText("");
            if(StringUtil.isEmpty(query) == false) {
            	keyEdit.append(query);
            	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
            			MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            	suggestions.saveRecentQuery(query, null);
            }
        }
    }
    
	@Override
	protected void createHandler()
	{
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	private class MyWatcher implements TextWatcher 
    {   
       public void afterTextChanged(Editable s) 
       {
    	   String key = s.toString().trim();

//    	   doSearch(key);
    		   
    	   btn_clear_text.setVisibility(key.length()  > 0 ? View.VISIBLE : View.GONE);
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
	
//	private void doSearch(String key)
//    {
//		Log.d(TAG, "doSearch search key : " + key);
//			if(mPeopleSearchFragment != null){
//				mPeopleSearchFragment.doInLineSearch(key);
//			}
//    }
	
	@Override
	public void onClick(View view)
	{
		int id = view.getId();
		if(id == R.id.btn_clear_text) {
			if(keyEdit.getText().toString().length() > 0)
			{
				keyEdit.setText("");
			}
		}else if(id == R.id.search_do) {
			gotoSearchPage();
		}
	}

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null && event.isShiftPressed()) {
            // if shift key is down, then we want to insert the '\n' char in the TextView;
            // otherwise, the default action is to send the message.
            return false;
        }
			if(mPageSearchFragment != null) {
				mPageSearchFragment.doMySearch(getKeyEditString());
			}
//			return true;
//		}

        return keyEdit.getText().toString().trim().length() > 0;
    }

    /**
     *  Override this method and do nothing but directly return true in order to
     *  prevent show quick-search-box mDenyProgress as it is the search activity itself.
     * @return
     */
    @Override
    public boolean onSearchRequested() {
        return false;
    }

	@Override
	protected void uiLoadBegin() {
		center_process.setVisibility(View.VISIBLE);		
	}

	@Override
    protected void uiLoadEnd ()  {
		center_process.setVisibility(View.GONE);
		
	}
	
	private void parseIntentScheme(Intent intent) {
        String url = getIntentURL(intent);

        if(isEmpty(url) == false) {
            Uri uri = Uri.parse(url);
            String seg = uri.getQueryParameter("q");
            if(isEmpty(seg)) {
                seg = uri.getQueryParameter("id");
            }
            if(isEmpty(seg) == false) {
                if(seg.startsWith("pname:")) {
                    seg = seg.substring(6);
                }
                keyEdit.setText(seg);
            }
        }
    }

	@Override
	public String getSearchKey() {
		return getKeyEditString();
	}

	private void initUI() {
		FragmentTransaction tra = mFragmentManager.beginTransaction();
			tra.add(R.id.contact_list, new PageSerachFragment(), PEOPLE_TAG);
		tra.commit();
	}
	
	private String getKeyEditString() {
		return keyEdit != null ? keyEdit.getText().toString().trim() : "";
	}
	
	private void gotoSearchPage() {
		if(mPageSearchFragment != null)
			mPageSearchFragment.doMySearch(getKeyEditString());
	}

	@Override
	public void getPageSerachFragment(PageSerachFragment fragment) {
		mPageSearchFragment = fragment;		
	}
	
//	@Override
//	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		if(keyCode == KeyEvent.KEYCODE_ENTER){
//			if(mPageSearchFragment != null) {
//				mPageSearchFragment.doMySearch(getKeyEditString());
//			}
//			return true;
//		}
//		return super.onKeyUp(keyCode, event);
//	}
}
