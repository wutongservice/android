/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.common.dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.borqs.common.SelectionItem;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.cache.QiupuHelper.DropDownDialogListener;

/**
 * Corpus selection dialog.
 */
public class CorpusSelectionDialog extends Dialog implements DropDownDialogListener {

    private static final boolean DBG = false;
    private static final String TAG = "qiupu.CorpusSelectionDialog";

    private static final int NUM_COLUMNS = 1;

    private ListView mCorpusGrid;

    private OnCorpusSelectedListener mListener;

    private CorporaAdapter mAdapter;
    
    private OnItemClickListener mOnItemClickListener;
    
    private ArrayList<SelectionItem> allitems = new ArrayList<SelectionItem>();
    private boolean mIsdropDownMenu = false;

    public CorpusSelectionDialog(Context context, ArrayList<SelectionItem> items, OnItemClickListener listener) {
        super(context, R.style.dialog);
        allitems.clear();
        allitems.addAll(items);
        mOnItemClickListener = listener;
        Log.d(TAG, context.getClass().getName());
        QiupuHelper.registerDropDownListener(context.getClass().getName(), this);
    }
    
    public CorpusSelectionDialog(Context context) {
        super(context, R.style.Theme_SelectSearchSource);
    }


    public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
        mListener = listener;
    }
    
    public void setIsdropDownMenu (boolean flag) {
        mIsdropDownMenu = flag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	int layout_id = -1;
        if(mIsdropDownMenu) {
        	layout_id = R.layout.dropdown_popwindow;
        }else {
        	layout_id = R.layout.corpus_selection_dialog;
        }
        
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();
        
        Resources res = getContext().getResources();
        int itemHight = (int) res.getDimension(R.dimen.dialog_list_item_height);
        int titleBarHeight = (int) res.getDimension(R.dimen.title_bar_height);
        int bottomBarHeight = (int) res.getDimension(R.dimen.bottom_height);
        if(QiupuConfig.DBLOGD)Log.d(TAG, "d.getHeight(): " + d.getHeight() + " titleBarHeight: " + titleBarHeight 
        		+ " bottomBarHeight: " + bottomBarHeight + " allitems.size() * itemHight: " + allitems.size() * itemHight );
        int contentHeight = d.getHeight() - titleBarHeight - bottomBarHeight - 60; 
        if(contentHeight > allitems.size() * itemHight) {
        	setContentView(layout_id);
        }else {
        	View tmpView = getLayoutInflater().inflate(layout_id, null);
        	setContentView(tmpView, new LayoutParams((int)res.getDimension(R.dimen.drop_down_item_width), contentHeight));
        }
        
        mCorpusGrid = (ListView) findViewById(R.id.corpus_grid);
        mCorpusGrid.setOnItemClickListener(CorpusClickListener);
        
        // TODO: for some reason, putting this in the XML layout instead makes
        // the list items unclickable.
        mCorpusGrid.setFocusable(true);
        
//
//        Window window = getWindow();
//        WindowManager.LayoutParams lp = window.getAttributes();
//        lp.x = d.getWidth()/2;
//        lp.y = 0;
//        Log.d(TAG, "AAAAAAAAAAAAAAAaa " + d.getWidth() + " " + d.getHeight() + " " + lp.x + " " + lp.y);
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        // Put window on top of input method
//        lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;        
//        window.setAttributes(lp);
//        if (DBG) Log.d(TAG, "Window params: " + lp);
    } 

    @Override
    protected void onStart() {
        super.onStart();
        CorporaAdapter adapter = new CorporaAdapter(getContext(), allitems);
        setAdapter(adapter);
    }

    @Override
    protected void onStop() {
//        setAdapter(null);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);        
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Cancel dialog on any touch down event which is not handled by the corpus grid
            cancel();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if (handled) {
            return handled;
        }
        // Dismiss dialog on up move when nothing, or an item on the top row, is selected.
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            int selectedRow = mCorpusGrid.getSelectedItemPosition() / NUM_COLUMNS;
            if (selectedRow <= 0) {
                cancel();
                return true;
            }
        }
        // Dismiss dialog when typing on hard keyboard (soft keyboard is behind the dialog,
        // so that can't be typed on)
        if (event.isPrintingKey()) {
            cancel();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {       
        cancel();
    }   

    private void setAdapter(CorporaAdapter adapter) {
        if (adapter == mAdapter) return;
        
        mAdapter = adapter;
        mCorpusGrid.setAdapter(mAdapter);
    }
      
    protected void selectCorpus(SelectionItem corpus) {
        dismiss();
        if (mListener != null) {
            mListener.onCorpusSelected(corpus);
        }
    }

//    private class CorpusClickListener implements AdapterView.OnItemClickListener {
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            if(CorpusSelectionItemView.class.isInstance(view)) {
//                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
//                selectCorpus(item.getItem());
//            }
////            selectCorpus(mAdapter.getItem(position));
////        	if(TextView.class.isInstance(view))
////        	{
////        		TextView v = (TextView) view;
////        		selectCorpus(v.getText().toString());
////        	}
//        }
//    }
    
    OnItemClickListener CorpusClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            dismiss();
            if(mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(parent, view, position, id);
            }
        }
    };

    public interface OnCorpusSelectedListener {
        void onCorpusSelected(SelectionItem corpus);
    }
    
    public void refreshCorpus(ArrayList<SelectionItem> newList){
    	if(mAdapter!=null)mAdapter.alterDataList(newList);
    }
    
    public int getHeight()
    {
    	return mCorpusGrid.getHeight();
    }

	@Override
	public void DialogConfigurationChanged(Configuration newConfig) {
		if(isShowing()) {
			dismiss();
		}
	}
}
