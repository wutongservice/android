package com.borqs.wutong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.listener.StreamActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;
import twitter4j.UserCircle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class HomePickerActivity extends BasicActivity {

    private final static String TAG = "BpcFriendsFragmentActivity";
    private HomePickerFragment mCirclesListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wutong_home_picker_activity);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mCirclesListFragment = (HomePickerFragment)getSupportFragmentManager().findFragmentById(R.id.stream_fragment);

        showLeftActionBtn(true);

        setHeadTitle(R.string.user_circles);
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadRefresh() {
        if(null != mCirclesListFragment){
        	mCirclesListFragment.loadRefresh();
        }
    }

    @Override
    protected void uiLoadEnd()  {
        if((mCirclesListFragment != null && mCirclesListFragment.getLoadStatus())){
            Log.d(TAG, "is loading ");
        }
        else {
            super.uiLoadEnd();
        }
    }

//    @Override
//    public void onListItemClick(View view, Fragment fg) {
//        if (CircleItemView.class.isInstance(view)) {
//            CircleItemView civ = (CircleItemView) view;
//            if(civ.getCircle() != null)
//            {
//                IntentUtil.startCircleDetailIntent(this, civ.getCircle(), fromtab);
//            }
//            else
//            {
//            	Log.d(TAG, "Item data is null");
////                CirclesListFragment circlefg = (CirclesListFragment) fg;
////                circlefg.showAddCircleDialog();
////                circlefg.setButtonEnable(false);
//            }
//        }
//        else if(PickPeopleItemView.class.isInstance(view)) {
//            PickPeopleItemView item = (PickPeopleItemView) view;
//            if(item != null){
//                IntentUtil.startContactDetailIntent(this, item.getContactSimpleInfo().mContactId);
//            }
//        }
//    }

    private final int CREATE_LOCAL_CIRCLE = 1;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CREATE_LOCAL_CIRCLE: {
                createCircle(msg.getData().getString("circleName"));
                break;
            }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


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
        if (getString(R.string.create_public_circle_title).equals(value)) {
        	IntentUtil.gotoEditPublicCircleActivity(HomePickerActivity.this, "", null, EditPublicCircleActivity.type_create);
        } else if (getString(R.string.create_group_label).equals(value)) {
        	showAddCircleDialog();
        }else {
            Log.d(TAG, "unsupported item action!");
        }
    }
    private AlertDialog mAlertDialog;
    public void showAddCircleDialog(){
        LayoutInflater factory = LayoutInflater.from(this);  
        final View textEntryView = factory.inflate(R.layout.create_circle_dialog, null);  
        final EditText textContext = (EditText) textEntryView.findViewById(R.id.new_circle_edt);

//        final CheckBox select_public_circle = (CheckBox) textEntryView.findViewById(R.id.select_public_circle);
//        if(orm.isOpenPublicCircle()) {
//            select_public_circle.setVisibility(View.VISIBLE);
//        }
//        select_public_circle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                setDialogButtonEnable(!isChecked);
//                if(isChecked) {
//                    IntentUtil.gotoEditPublicCircleActivity(BpcFriendsFragmentActivity.this, textContext.getText().toString().trim(), null, EditPublicCircleActivity.type_create);
//                    mAlertDialog.dismiss();
//                }
//            }
//        });
//        
        textContext.addTextChangedListener(new ButtonWatcher()); 
        
        mAlertDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.new_circle_dialog_title)
        .setView(textEntryView)
        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String textString = textContext.getText().toString().trim();
                boolean hasCirecle = false;
                if(textString.length() > 0)
                {
                    Cursor cursor = orm.queryAllCircleinfo(AccountServiceUtils.getBorqsAccountID());
                    final int size = null == cursor ? 0 : cursor.getCount();
                    for(int i=0; i < size; i++)
                    {
                        cursor.moveToPosition(i);
                        UserCircle tmpCircle = QiupuORM.createCircleInformation(cursor);
                        if(tmpCircle != null && tmpCircle.name != null && tmpCircle.name.equals(textString))
                        {
                            hasCirecle = true;
                            Toast.makeText(HomePickerActivity.this, getString(R.string.circle_exists), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    if (null != cursor) {
                        cursor.close();
                    }

                    if(!hasCirecle) {
                        mAlertDialog.dismiss();
                        Message msg = mHandler.obtainMessage(CREATE_LOCAL_CIRCLE);
                        msg.getData().putString("circleName",textString);
                        msg.sendToTarget();
                    }
                }
                else {
                    Toast.makeText(HomePickerActivity.this, getString(R.string.input_content), Toast.LENGTH_SHORT).show();
                }
                
            }
        })
        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        })
        .create();
        mAlertDialog.show();
        setDialogButtonEnable(false);
    }

    private class ButtonWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().length() > 0) {
                setDialogButtonEnable(true);
            } else {
                setDialogButtonEnable(false);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }
    }

    private void setDialogButtonEnable(boolean flag) {
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(flag);
    }

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			loadSearch();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
            HomePickerActivity.onPickerCancelled();
        }
		return super.onKeyUp(keyCode, event);
	}

    public static interface PickerInterface {
        public boolean onCancelled();
        public boolean onPicked(UserCircle circle);
    }
    final static HashMap<String,WeakReference<PickerInterface>> listeners = new HashMap<String,WeakReference<PickerInterface>>();
    public static void registerPickerListener(String key, PickerInterface listener){
        synchronized(listeners)
        {
            WeakReference<PickerInterface> ref = listeners.get(key);
            if(ref != null && ref.get() != null)
            {
                ref.clear();
            }
            listeners.put(key, new WeakReference<PickerInterface>(listener));
        }
    }

    public static void unregisterPickerListener(String key){
        synchronized(listeners)
        {
            WeakReference<PickerInterface> ref = listeners.get(key);
            if(ref != null && ref.get() != null)
            {
                ref.clear();
            }
            listeners.remove(key);
        }
    }
    public static void onPickerCancelled() {
        synchronized (listeners) {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (listeners.get(key) != null) {
                    PickerInterface listener = listeners.get(key).get();
                    if (listener != null) {
                        listener.onCancelled();
                    }
                }
            }
        }
        listeners.clear();
    }

    public static void onPickerSelected(UserCircle circle) {
        synchronized (listeners) {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (listeners.get(key) != null) {
                    PickerInterface listener = listeners.get(key).get();
                    if (listener != null) {
                        listener.onPicked(circle);
                    }
                }
            }
        }
        listeners.clear();
    }
}