package com.borqs.qiupu.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.ProgressInterface;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-9-7
 * Time: 下午6:49
 * To change this template use File | Settings | File Templates.
 */
public class BasicFragment extends Fragment {

    protected void begin() {
        Activity activity = getActivity();
        if (ProgressInterface.class.isInstance(activity)) {
            ProgressInterface pi = (ProgressInterface) activity;
            pi.begin();
        }
    }

    protected void end() {
        Activity activity = getActivity();
        if (ProgressInterface.class.isInstance(activity)) {
            ProgressInterface pi = (ProgressInterface) activity;
            pi.end();
        }
    }
    
    protected void setViewIcon(final String url, final ImageView view, boolean isIcon) {
		ImageRun imagerun = new ImageRun(null, url, 0);
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.width = getResources().getDisplayMetrics().widthPixels;
		imagerun.height = imagerun.width;
		imagerun.noimage = true;
		imagerun.addHostAndPath = true;
		if(isIcon)
			imagerun.setRoundAngle=true;
		imagerun.setImageView(view);
		imagerun.post(null);
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Activity activity = getActivity();
        if (null != activity) {
            inflater.inflate(R.menu.comment_option_menu, menu);
        } else {
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    protected static class UserFragment extends BasicFragment {
        protected Cursor musers;
        protected QiupuORM orm;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d("UserFragment", "onCreate");
            super.onCreate(savedInstanceState);
            ensureOrm();
        }

        private boolean ensureOrm() {
            if (null == orm) {
                Context context = getActivity();
                if (null != context) {
                    orm = QiupuORM.getInstance(context);
                }
            }
            return null != orm;
        }

        protected void queryAllSimpleUser() {
            if (ensureOrm()) {
                musers = orm.queryAllSimpleUserInfo();
            } else {
                Log.w("UserFragment", "queryAllSimpleUser skip with uninitialized orm.");
            }
        }
    }
}
