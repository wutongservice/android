package com.borqs.qiupu.ui.circle;

import android.os.Bundle;
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
import com.borqs.qiupu.fragment.PublicCircleSearchFragment;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.R;

public class PublicCircleSearchActivity extends BasicActivity implements TextView.OnEditorActionListener {
	private static final String TAG = "PublicCircleSearchActivity";
	private EditText keyEdit;
    private ImageView btn_clear_text;
    private View center_process;

    private PublicCircleSearchFragment mPublicCircleSearchFragment;

	private MyWatcher watcher = new MyWatcher();
	private FragmentManager mFragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(QiupuConfig.LOGD)Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.public_circle_search_ui);
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
		FragmentTransaction tra = mFragmentManager.beginTransaction();
		mPublicCircleSearchFragment = new PublicCircleSearchFragment();
		tra.add(R.id.contact_list, mPublicCircleSearchFragment);
		tra.commit();
	}
    
	@Override
	protected void createHandler()
	{ }
	
	@Override
    public void onClick(View view)
    {
        int id = view.getId();
        if(id == R.id.btn_clear_text) {
            if(keyEdit.getText().toString().length() > 0) {
                keyEdit.setText("");
            }
        }else if(id == R.id.search_do) {
                gotoSearchPublicCircle();
        }
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

    	   doSearch(key);
    		   
    	   btn_clear_text.setVisibility(key.length()  > 0 ? View.VISIBLE : View.GONE);
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
	
	private void doSearch(String key)
	{
	    Log.d(TAG, "doSearch search key : " + key);
	    if(mPublicCircleSearchFragment != null){
//	        mPublicCircleSearchFragment.doInLineSearch(key);
	    }
	}

    @Override
	protected void uiLoadBegin() {
		center_process.setVisibility(View.VISIBLE);
	}
	
    @Override
    protected void uiLoadEnd() {
    	center_process.setVisibility(View.GONE);
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null && event.isShiftPressed()) {
            // if shift key is down, then we want to insert the '\n' char in the TextView;
            // otherwise, the default action is to send the message.
            return false;
        }

        return keyEdit.getText().toString().trim().length() > 0;
    }

    /**
     *  Override this method and do nothing but directly return true in order to
     *  prevent show quick-search-box mDenyProgress as it is the search activity itself.
     * @return
     */
    @Override
    public boolean onSearchRequested() {
        return true;
    }

	private String getKeyEditString() {
		return keyEdit != null ? keyEdit.getText().toString().trim() : "";
	}
	
	private void gotoSearchPublicCircle() {
		if(mPublicCircleSearchFragment != null)
		    mPublicCircleSearchFragment.doMySearch(getKeyEditString());
	}
}
