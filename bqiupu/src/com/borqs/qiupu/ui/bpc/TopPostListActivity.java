package com.borqs.qiupu.ui.bpc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.AbstractStreamRowView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.fragment.TopPostListFragment;
import com.borqs.qiupu.fragment.TopPostListFragment.TopPostListFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;

public class TopPostListActivity extends BasicActivity implements TopPostListFragmentCallBack{

    private static final String TAG = "TopPostListActivity";
    public static final String EXTRA_ID_KEY = "EXTRA_ID_KEY";
    public static final String EXTRA_TITLE_KEY = "EXTRA_TITLE_KEY";
    public static final String EXTRA_VIEWER_ROLE_KEY = "EXTRA_VIEWER_ROLE_KEY";
    private TopPostListFragment mFragment;
    private long id;
    private String mOldTitle;
    private EditText mEditText;
    private boolean mIsAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top_post_layout);
        mOldTitle = getIntent().getStringExtra(EXTRA_TITLE_KEY);
        mIsAdmin = getIntent().getBooleanExtra(EXTRA_VIEWER_ROLE_KEY, false);
        if(TextUtils.isEmpty(mOldTitle)) {
            setHeadTitle(R.string.view_top);
        }else {
            setHeadTitle(mOldTitle);
        }
        id = getIntent().getLongExtra(EXTRA_ID_KEY, 0);

        mFragment = new TopPostListFragment(String.valueOf(id), asyncQiupu);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction tra = manager.beginTransaction();
        tra.add(R.id.post_container, mFragment);
        tra.commit();
        showLeftActionBtn(true);
        showMiddleActionBtn(false);
        showRightActionBtn(false);

        if (mIsAdmin) {
            showEditTitleActionBtn(true);
            overrideEditTitleActionBtn(R.drawable.edit_title_icon, mClickTitleListener);
        }
    }

    private View.OnClickListener mClickTitleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showEditTitleDialog();
        }
    };

    private void showEditTitleDialog() {
        String title = getResources().getString(R.string.top_list_title);
        mEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        mEditText.setLayoutParams(params);
        mEditText.setMinLines(1);
        mEditText.setMaxLines(2);
        Log.d(TAG, "mOldTitle = " + mOldTitle);
        if (TextUtils.isEmpty(mOldTitle)) {
            mEditText.setText(getResources().getString(R.string.view_top));
        } else {
            mEditText.setText(mOldTitle);
        }

        DialogUtils.ShowDialogwithView(this, title, 0,
                mEditText, updateOkClickListener, updateCancleListener);
        mEditText.requestFocus();
    }

    private DialogInterface.OnClickListener updateOkClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String updateTitle = mEditText.getText().toString().trim();
            if(mOldTitle.equalsIgnoreCase(updateTitle)) {
                Log.d(TAG, "do nothing");
//                ToastUtil.showShortToast(TopPostListActivity.this, new Handler(), R.string.have_no_change);
//                showCustomToast(R.string.have_no_change);
            } else {
//                if (TextUtils.isEmpty(updateTitle)) {
//                    ToastUtil.showShortToast(TopPostListActivity.this, new Handler(), R.string.edit_profile_input_null);
//                    showCustomToast(R.string.edit_profile_input_null);
//                } else {
                    mFragment.updateTopListTitle(updateTitle);
//                }
            }
        }
    };

    private DialogInterface.OnClickListener updateCancleListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            
        }
    };

    @Override
    protected void loadRefresh() {
        if(mFragment != null) {
            mFragment.loadRefresh();
        }
    }


    protected void createHandler() {
    }


    @Override
    public String getSerializeFilePath() {
        return QiupuHelper.top_post + id;
    }


    @Override
    public long getId() {
        return id;
    }


    @Override
    public void setTitle(String newTitle) {
        setTopPostTitle(newTitle);
    }

    private void setTopPostTitle(String title) {
        if(TextUtils.isEmpty(title)) {
            setHeadTitle(R.string.view_top);
        }else {
            setHeadTitle(title);
        }
    }

    @Override
    public void showErrorToast(String reason, boolean isShort) {
        showOperationFailToast(reason, isShort);
    }

    @Override
    public long getTopStreamTargetId() {
        Log.d(TAG, "getTopStreamTargetId() mIsAdmin = " + mIsAdmin);

        if (mIsAdmin) {
            return id;
        }

        return super.getTopStreamTargetId();
    }

//    @Override
//    protected void setTopListMenuListener(int itemId, View targetView) {
//        if (mIsAdmin) {
//            if (itemId == R.id.bpc_item_set_top) {
//                setTopList(targetView, true);
//            } else if (itemId == R.id.bpc_item_unset_top) {
//                setTopList(targetView, false);
//            }
//        }
//    }
//
//    private void setTopList(View targetView, boolean setTop) {
//        if (AbstractStreamRowView.class.isInstance(targetView)) {
//            AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
//            String stream_id = streamRowView.getStream().post_id;
//            setTopList(String.valueOf(id), stream_id, setTop);
//        }
//    }

}
