package com.borqs.qiupu.fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.borqs.qiupu.R;

public abstract class PeopleSearchableFragment extends PeopleListFragment {
    @Override
    protected View initHeadView() {
        View headView = LayoutInflater.from(mActivity).inflate(R.layout.friends_list_headview, null, false);
        
        View view = headView.findViewById(R.id.search_span);
        if(showEditText) {
        	if (null != view && view instanceof EditText) {
        		view.setVisibility(View.VISIBLE);
        		mInputEditText = (EditText) view;
        		mInputEditText.setHint(R.string.hint_search_people);
        		mInputEditText.addTextChangedListener(new MyWatcher());
        	}
        } else {
        	view.setVisibility(View.GONE);
        }
        
        return headView;
    }

    private class MyWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            // do search
            String key = s.toString().trim();
            doSearch(key);
            isNeedRefreshUi = false;
            isSearchMode = false;
            showSearchFromServerButton(key.length() > 0 ? true : false, key);
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    }

    abstract protected void doSearch(String key);
}
