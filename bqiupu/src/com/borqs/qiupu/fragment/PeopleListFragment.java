package com.borqs.qiupu.fragment;

import java.lang.ref.WeakReference;

import twitter4j.QiupuUser;
import twitter4j.Requests;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;

public class PeopleListFragment extends BasicFragment.UserFragment implements AtoZ.MoveFilterListener {
    private static final String TAG = "PeopleListFragment";

    protected Activity mActivity;

    protected View mGuideContainer;
    protected TextView mSkipView;
    protected Button mFindView;

    protected ListView mListView;
    protected TextView mEmptyText;
    protected AtoZ mAtoZ;

    protected View mConvertView;
    protected String mSearchKey;
    protected boolean isNeedRefreshUi;
    protected boolean showEditText = true; 
    
    protected boolean isSearchMode = false;
    protected EditText mInputEditText;

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        mActivity = activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        parserSavedState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        parserSavedState(savedInstanceState);

        mConvertView = inflater.inflate(R.layout.friends_list_a2z, container, false);
        mListView = (ListView) mConvertView.findViewById(R.id.friends_list);
        mEmptyText = (TextView) mConvertView.findViewById(R.id.empty_text);

        AtoZ atoz = (AtoZ) mConvertView.findViewById(R.id.atoz);
        if (atoz != null) {
            mAtoZ = atoz;
            atoz.setFocusable(true);
            atoz.setMoveFilterListener(this);
            atoz.setVisibility(View.VISIBLE);
            mAtoZ.setListView(mListView);
        }

        View header = initHeadView();
        if (null != header) {
            mListView.addHeaderView(header);
        }

        return mConvertView;
    }

    protected View initHeadView() {
        return null;
    }

    @Override
    public void enterPosition(String alpha, int position) {
        mListView.setSelection(position);
    }

    @Override
    public void leavePosition(String alpha) {
    }

    @Override
    public void beginMove() {
    }

    @Override
    public void endMove() {
    }

    private View findFragmentViewById(int viewId) {
        if (null != mConvertView) {
            return mConvertView.findViewById(viewId);
        }

        return null;
    }

    private void parserSavedState(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
        }
    }

    protected void showAddPeopleButton(boolean show, View.OnClickListener callback) {
        View id_add_people = findFragmentViewById(R.id.id_add_people);
        if (null != id_add_people) {
            int visibility = View.GONE;
            View.OnClickListener listener = null;

            if (show) {
                visibility = View.VISIBLE;
                listener = callback;
            }

            id_add_people.setVisibility(visibility);
            if (null != id_add_people) {
                id_add_people.setOnClickListener(listener);
            }
        }
    }
    
    protected void showSearchFromServerButton(boolean show, final String key) {
        mSearchKey = key;
        showSearchFromServerButton(show, key, new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                IntentUtil.startPeopleSearchIntent(mActivity, key);
            }
        });
    }
    
    protected void showSearchFromServerButton(boolean show, final String key, View.OnClickListener callback) {
    	showSearchFromServerButton(show, key, R.string.search_from_internet, callback);
    }
    
    protected void showSearchFromServerButton(boolean show, final String key, final int res, View.OnClickListener callback) {
        View search = findFragmentViewById(R.id.id_search_people_from_internet);
        TextView search_text = (TextView) findFragmentViewById(R.id.search_text);
        if (null != search) {
            int visibility = View.GONE;
            View.OnClickListener listener = null;

            if (show) {
                visibility = View.VISIBLE;
                listener = callback;
                if (null != search_text) {
                    search_text.setText(String.format(getString(res), key));
                    search_text.setOnClickListener(listener);
                }
            }
            search.setVisibility(visibility);
        }
    }

    protected void showCustomToast(int resId) {
        showCustomToast(getString(resId));
    }

    protected void showCustomToast(final String textMsg) {
        if (null != mActivity) {
            if (mActivity instanceof BasicActivity) {
                ((BasicActivity)mActivity).showCustomToast(textMsg);
            } else {
                Toast.makeText(mActivity, textMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onVcardExchanged(final long uid) {
        for (int j = 0; j < mListView.getChildCount(); j++) {
            View v = mListView.getChildAt(j);
            if (BpcFriendsItemView.class.isInstance(v)) {
                BpcFriendsItemView fv = (BpcFriendsItemView) v;
                QiupuUser user = fv.getUser();
                if (user != null && user.uid == uid) {
                    String requestid = user.pedding_requests;
                    user.pedding_requests = Requests.getrequestTypeIds(requestid);
                    QiupuORM orm = QiupuORM.getInstance(mActivity);
                    orm.setRequestUser(user.uid, user.pedding_requests);
                    fv.refreshUI();
                    break;
                }
            }
        }
    }
    
    protected void showCenterProgress(boolean flag) {
        View center_progress = findFragmentViewById(R.id.center_progress);
        if(center_progress != null) {
            center_progress.setVisibility(flag ? View.VISIBLE : View.GONE);
        }
    }

    public interface SingleWizardListener {
        public void skip();
        public void invoke();
    }

    public WeakReference<SingleWizardListener> mWizardListener;

    public void setSingleWizardListener(SingleWizardListener listener) {
        mWizardListener = new WeakReference<SingleWizardListener>(listener);
    }

}
