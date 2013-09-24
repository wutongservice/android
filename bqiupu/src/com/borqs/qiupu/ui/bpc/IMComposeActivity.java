package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.QiupuUser;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.borqs.common.SelectionItem;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.IMComposeFragment;
import com.borqs.qiupu.fragment.IMComposeFragment.GetFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;

public class IMComposeActivity extends BasicActivity implements GetFragmentCallBack {

    private final static String TAG = "Qiupu.IMComposeActivity";
    private IMComposeFragment mIMFragment;
//    private String from_url;
    private String to_url;
    private QiupuUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.im_compose_activity);

        mUser = (QiupuUser) getIntent().getSerializableExtra("user");
        to_url = getIntent().getStringExtra("to_url");
        setHeadTitle(getTitleFromIntent());

        mIMFragment = new IMComposeFragment(mUser, to_url, null);
        getSupportFragmentManager().beginTransaction().add(R.id.im_container, mIMFragment).commit();

        showMiddleActionBtn(false);
        showLeftActionBtn(false);
        showRightActionBtn(false);
//        alterMiddleActionBtnByComposer(R.drawable.ic_menu_moreoverflow, editProfileClick);

    }

    private String getTitleFromIntent() {
        String title;
        if (TextUtils.isEmpty(mUser.nick_name)) {
            title = getString(R.string.im_tmp_title);
        } else {
        	title = mUser.nick_name;
//            title = String.format(getString(R.string.im_title), mUser.nick_name);
        }
        return title;
    }

    View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
//            ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
//            items.add(new SelectionItem("", getString(R.string.label_refresh)));
//            showCorpusSelectionDialog(items);
        }
    };

    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
        if(mMiddleActionBtn != null) {
            int location[] = new int[2];
            mMiddleActionBtn.getLocationInWindow(location);
            int x = location[0];
            int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
            
            DialogUtils.showCorpusSelectionDialog(this, x, y, items, actionListItemClickListener);
        }
    }

    private AdapterView.OnItemClickListener actionListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                onCorpusSelected(item.getText());
            }
        }
    };

    private void onCorpusSelected(String value) {
        if (getString(R.string.label_refresh).equals(value)) {
            
        } else {
            
        }
    }

    @Override
    protected void createHandler() {
    }

    @Override
    public void getFragment(IMComposeFragment fragment) {
        mIMFragment = fragment;
    }

}
