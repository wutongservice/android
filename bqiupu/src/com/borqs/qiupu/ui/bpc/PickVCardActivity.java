package com.borqs.qiupu.ui.bpc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.view.PickPeopleItemView;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.FriendsContactFragment;
import com.borqs.qiupu.fragment.FriendsContactFragment.CallBackFriendsContactFragmentListener;
import com.borqs.qiupu.ui.BasicActivity;

public class PickVCardActivity extends BasicActivity implements OnListItemClickListener, 
                           CallBackFriendsContactFragmentListener {

    private static final String TAG = "PickVCardActivity";
    private FriendsContactFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_layout);
        setHeadTitle(R.string.attach_people);

        mFragment = new FriendsContactFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction tra = manager.beginTransaction();
        tra.add(R.id.request_container, mFragment);
        tra.commit();
        showRightActionBtn(false);
    }

    @Override
    public void onListItemClick(View view, Fragment fg) {
        if (PickPeopleItemView.class.isInstance(view)) {
            Log.d(TAG, "onListItemClick ");
            PickPeopleItemView pickView = (PickPeopleItemView) view;
            ContactSimpleInfo cinfo = pickView.getContactSimpleInfo();

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable(QiupuMessage.BUNDLE_SHARE_CONTACT_INFO, cinfo);
            intent.putExtra(QiupuMessage.BUNDLE_SHARE_CONTACT, bundle);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    protected void createHandler() {
    }

    @Override
    public int getMode() {
        return FriendsContactFragment.MODE_PICK_VCARD;
    }

    @Override
    public void getFriendsContactFragment(FriendsContactFragment fragment) {
        mFragment = fragment;
    }

    public static Intent getStartupIntent(Context context) {
        Intent intent = new Intent(context, PickVCardActivity.class);
        return intent;
    }
}
