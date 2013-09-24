package com.borqs.qiupu.ui.bpc;

import android.os.Bundle;

import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.FindContactsFragment;
import com.borqs.qiupu.ui.BasicActivity;

public class PickPeopleActivity extends BasicActivity implements
        FindContactsFragment.SingleWizardListener {

//    private static final String TAG = "PickPeopleActivity";

    private FindContactsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_layout);
        setHeadTitle(R.string.attach_people);

        mFragment = new FindContactsFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.request_container, mFragment).commit();
        mFragment.setSingleWizardListener(this);

        if (QiupuORM.isEnableFindContacts(this) == false) {
            showLeftActionBtn(false);
        }

        showRightActionBtn(false);
    }

    protected void createHandler() {
    }

    @Override
    protected void loadRefresh() {
        mFragment.loadRefresh();
    }

    @Override
    public void skip() {
        mFragment.showContactUI(true);
    }

    @Override
    public void invoke() {
        skip();
        QiupuORM.enableFindContacts(this, true);
    }

}
