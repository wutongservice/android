package com.borqs.qiupu.ui.bpc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.browser.BookmarkUtils;
import com.borqs.qiupu.ui.bpc.browser.BrowserBookmarksAdapter;
import com.borqs.qiupu.ui.bpc.browser.BrowserBookmarksAdapterItem;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class LinkSelectActivity extends BasicActivity {
	private GridView linkView;
	private Cursor mCursor;
	private EditText et_select;
	BrowserBookmarksAdapter marksAdapter = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.link_select);
		setHeadTitle(R.string.link_title);
		showLeftActionBtn(false);
		showRightActionBtn(false);
		showRightTextActionBtn(true);
		
		overrideRightTextActionBtn(R.string.label_ok, linkClickListener);
		linkView = (GridView)findViewById(R.id.link_listview);
		et_select = (EditText)findViewById(R.id.et_select);
		et_select.setSelection(et_select.getText().length());
		et_select.addTextChangedListener(new MyWatcher());
		
		queryBookMark(current_key);
		
//		linkListView.setAdapter(new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, mCursor,new String[]{Browser.BookmarkColumns.URL},new int[]{android.R.id.text1}));
		marksAdapter = new BrowserBookmarksAdapter(this);
		marksAdapter.changeCursor(mCursor);
		linkView.setAdapter(marksAdapter);
		linkView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				Intent shortIn = (Intent)intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
//	            link_title = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
//	            link_url = shortIn.getData().toString();
//	            mFavicon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
				
				Intent shortIn  = new Intent(Intent.EXTRA_SHORTCUT_INTENT);
				mCursor.moveToPosition(position);
				BrowserBookmarksAdapterItem item = marksAdapter.getRowObject(mCursor, null);
				shortIn.putExtra(Intent.EXTRA_SHORTCUT_NAME, item.title);
//				Bitmap bmp = null;
				Bitmap favbmp = null;
				Bitmap touch_icon = null;
//				if(item.thumbnail != null) {
//					bmp = item.thumbnail.getBitmap();
//				}
				if(item.favIcon != null) {
					favbmp = item.favIcon;
				}
				if(item.touch_icon != null) {
					touch_icon = item.touch_icon;
				}
				shortIn.putExtra(Intent.EXTRA_SHORTCUT_ICON,
						BookmarkUtils.createIcon(LinkSelectActivity.this,
								touch_icon, 
								favbmp,
								BookmarkUtils.BookmarkIconType.ICON_HOME_SHORTCUT));
				if (!TextUtils.isEmpty(item.url)) {
				    shortIn.setData(Uri.parse(item.url));
				}
				setResult(RESULT_OK, shortIn);
				finish();
			}
		});
	}
	
	View.OnClickListener linkClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String url = et_select.getText().toString().trim();
			
			if(TextUtils.isEmpty(url) || header_str.equals(url)) {
				ToastUtil.showShortToast(LinkSelectActivity.this, new Handler(), R.string.invalid_url);
			}else {
				Intent shortIn  = new Intent(Intent.EXTRA_SHORTCUT_INTENT);
//				shortIn.putExtra(Intent.EXTRA_SHORTCUT_NAME, url);
				Bitmap bmp = null;
//				if(item.thumbnail != null) {
//					bmp = item.thumbnail.getBitmap();
//				}
				shortIn.putExtra(Intent.EXTRA_SHORTCUT_ICON,
						BookmarkUtils.createIcon(LinkSelectActivity.this,
								bmp, 
								bmp,
								BookmarkUtils.BookmarkIconType.ICON_HOME_SHORTCUT));
				shortIn.setData(Uri.parse(url));
				setResult(RESULT_OK, shortIn);
				finish();
			}
			
		}
	};
	
	@Override
	protected void createHandler() {
		// TODO Auto-generated method stub
	}
	
	private void queryBookMark(String key) {
		if(mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		ContentResolver contentResolver = getContentResolver();
        String orderBy = Browser.BookmarkColumns.VISITS + " DESC";
        StringBuilder whereClause = new StringBuilder(Browser.BookmarkColumns.BOOKMARK + " = 1 ");
        if(!TextUtils.isEmpty(key)) {
        	whereClause.append(" and ");
        	whereClause.append(Browser.BookmarkColumns.URL);
        	whereClause.append(" like \'%");
        	whereClause.append(key);
        	whereClause.append("%\'");
        }
        mCursor  = contentResolver.query(Browser.BOOKMARKS_URI, Browser.HISTORY_PROJECTION, whereClause.toString(), null, orderBy);
	}
	
	
	String current_key = "";
	String header_str = "http://";
	private class MyWatcher implements TextWatcher {
		public void afterTextChanged(Editable s) {
			// do search
			String key = s.toString().trim();
			if(key.startsWith(header_str)) {
				key = key.substring(header_str.length());
			}
			if (!key.equals(current_key)) {
				current_key = key;
				doSearch(key);
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}

	protected void doSearch(String key) {
		queryBookMark(current_key);
		notifyDataChange();
	}
	
	private void notifyDataChange() {
		if (marksAdapter == null) {

			marksAdapter = new BrowserBookmarksAdapter(this);
			linkView.setAdapter(marksAdapter);
		}
		marksAdapter.changeCursor(mCursor);
		marksAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
	}
	
	
}
