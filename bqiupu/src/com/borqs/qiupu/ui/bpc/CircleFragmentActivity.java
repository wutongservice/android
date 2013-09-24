package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.QiupuUser;
import twitter4j.UserCircle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CircleItemView;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.AllCirclesListFragment;
import com.borqs.qiupu.fragment.AllCirclesListFragment.CallBackCircleListFragmentListener;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;

public class CircleFragmentActivity extends BasicNavigationActivity implements
                                 OnListItemClickListener,CallBackCircleListFragmentListener {

    private final static String TAG = "CircleFragmentActivity";
    private long mUserid;
    private AllCirclesListFragment mCirclesListFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        enableLeftNav();
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.default_fragment_activity);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        parseActivityIntent(getIntent());


        overrideRightActionBtn(R.drawable.create_group_icon, addCircleListener);

        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        mCirclesListFragment = new AllCirclesListFragment();
        mCirclesListFragment.setLocalCirle(true);
        ft.add(R.id.fragment_content, mCirclesListFragment);
        ft.commit();
        
        showLeftActionBtn(true);

        setHeadTitle(R.string.group_management);
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
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                mUserid = bundle.getLong(BpcApiUtils.User.USER_ID, -1L);
                if (mUserid == -1L) {
                    mUserid = AccountServiceUtils.getBorqsAccountID();
                    // mUserName = AccountServiceUtils.getAccountNickName();
                } else {
                    mUserid = bundle.getLong(BpcApiUtils.User.USER_ID);
                    // mUserName = bundle.getString(USER_NICKNAME);
                }
            } else {
                mUserid = AccountServiceUtils.getBorqsAccountID();
                // mUserName = AccountServiceUtils.getAccountNickName();
            }
        } else {
            final String uid = BpcApiUtils.parseSchemeValue(intent,
                    BpcApiUtils.SEARCH_KEY_UID);
            if (TextUtils.isEmpty(uid) || !TextUtils.isDigitsOnly(uid)) {
                mUserid = AccountServiceUtils.getBorqsAccountID();
                // mUserName = AccountServiceUtils.getAccountNickName();
            } else {
                mUserid = Long.parseLong(uid);
                final String tab = BpcApiUtils.parseSchemeValue(intent,
                        BpcApiUtils.SEARCH_KEY_TAB);
            }
        }
    }
    
    
    
    public long getUserId(){
        return mUserid;
    }
    
    @Override
    protected void loadRefresh() {
        if(null != mCirclesListFragment){
            mCirclesListFragment.loadRefresh();
        }
    }

    @Override
    protected void uiLoadEnd()  {
        if(mCirclesListFragment != null && mCirclesListFragment.getLoadStatus()){
            Log.d(TAG, "is loading ");
        }
        else {
            super.uiLoadEnd();
        }
    }

    @Override
    public void onListItemClick(View view, Fragment fg) {
        if (CircleItemView.class.isInstance(view)) {
            CircleItemView civ = (CircleItemView) view;
            if(civ.getCircle() != null)
            {
                IntentUtil.startCircleDetailIntent(this, civ.getCircle(), fromtab);
            }
            else
            {
            	Log.d(TAG, "circleItemView circle info is null");
//                CirclesListFragment circlefg = (CirclesListFragment) fg;
//                circlefg.showAddCircleDialog();
//                circlefg.setButtonEnable(false);
            }
        }
    }

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
    public void getCircleListFragment(AllCirclesListFragment fragment) {
        mCirclesListFragment = fragment;
    }


    @Override
    protected void doCircleActionCallBack(boolean isdelete) {
       if(mCirclesListFragment != null) {
           mCirclesListFragment.refreshUi();
       }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
    }

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.friends_circle);
//    }
    
    View.OnClickListener addCircleListener = new OnClickListener() {
        public void onClick(View v) {
        	showAddCircleDialog();
        }
    };
    
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
                            Toast.makeText(CircleFragmentActivity.this, getString(R.string.circle_exists), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CircleFragmentActivity.this, getString(R.string.input_content), Toast.LENGTH_SHORT).show();
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

}