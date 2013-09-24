package com.borqs.qiupu.ui.bpc;

import java.util.HashMap;

import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.TitlePageIndicator;
import com.borqs.qiupu.fragment.TitleProvider;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.fragment.EditPhoneEmailFragment;
import com.borqs.qiupu.ui.bpc.fragment.EditPhoneEmailFragment.onEditPhoneEmailFragmentListener;
import com.borqs.qiupu.ui.bpc.fragment.EditProfilesBaseInfoFragment;
import com.borqs.qiupu.ui.bpc.fragment.EditProfilesBaseInfoFragment.onCallBackListener;
import com.borqs.qiupu.ui.bpc.fragment.EditWorkExperFragment;
import com.borqs.qiupu.ui.bpc.fragment.EditWorkExperFragment.onWorkFragmentListener;
import com.borqs.qiupu.ui.bpc.fragment.EducationFragment;
import com.borqs.qiupu.ui.bpc.fragment.EducationFragment.onEducationListener;
import com.borqs.qiupu.util.JSONUtil;

public class EditProfilesActivity extends BasicActivity implements
		onCallBackListener, onWorkFragmentListener, onEducationListener,onEditPhoneEmailFragmentListener {
	private static final String TAG = "EditProfilesActivity";
	private static final int FRAGMENT_COUNT = 4;
	private static final int FRAGMENT_BASE = 0;
	private static final int FRAGMENT_CONTACT = 1;
	private static final int FRAGMENT_WORK_EXPERIENCE = 2;
	private static final int FRAGMENT_EDUCATION = 3;
	private QiupuORM orm;
	ViewPager mViewPager;
	TitlePageIndicator mIndicator;
	private QiupuUser mUser;
	private QiupuUser mCopyUser;
	private FragmentManager fm;
	private EditProfilesBaseInfoFragment base_fragment;
	private EditWorkExperFragment work_fragment;
	private EducationFragment edu_frag;
	private static String STR_BASE;
	private static String STR_CONTACT;
	private static String STR_WORK_EXPERIENCE;
	private static String STR_EDUCATION;
	private boolean isDataChanged = false; // Used to mark the user data has changed

	public QiupuORM getOrm() {
		return orm;
	}

	public void setOrm(QiupuORM orm) {
		this.orm = orm;
	}

	@Override
	protected void createHandler() {
		mHandler = new MainHandler();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_profiles_activity);
		STR_BASE = getString(R.string.base_info_title);
		STR_CONTACT = getString(R.string.phone_email_title);
		STR_WORK_EXPERIENCE = getString(R.string.work_experience);
		STR_EDUCATION = getString(R.string.education);
		mUser = (QiupuUser) getIntent().getSerializableExtra("bundle_userinfo");

		if (mUser == null) {
			mUser = new QiupuUser();
		}
		mCopyUser = mUser.clone();
		setHeadTitle(R.string.edit_profile_title);
		showRightActionBtn(false);
		fm = getSupportFragmentManager();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(new ProfilesAdapter(fm));
		mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);
		ProfilesAdapter ad = (ProfilesAdapter) mViewPager.getAdapter();
		orm = QiupuORM.getInstance(mApp);
		QiupuHelper.setORM(orm);

	}

	public static class ProfilesAdapter extends FragmentPagerAdapter implements
			TitleProvider {

		public ProfilesAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case FRAGMENT_BASE:
				return new EditProfilesBaseInfoFragment();

			case FRAGMENT_CONTACT:

				return new EditPhoneEmailFragment();
			case FRAGMENT_WORK_EXPERIENCE:
				return new EditWorkExperFragment();
			case FRAGMENT_EDUCATION:
				return new EducationFragment();
			}

			throw new IllegalStateException("No fragment at position "
					+ position);
		}

		@Override
		public int getCount() {
			return FRAGMENT_COUNT;
		}

		@Override
		public String getTitle(int position) {
			switch (position) {
			case FRAGMENT_BASE:
				return STR_BASE;

			case FRAGMENT_CONTACT:

				return STR_CONTACT;
			case FRAGMENT_WORK_EXPERIENCE:
				return STR_WORK_EXPERIENCE;
			case FRAGMENT_EDUCATION:
				return STR_EDUCATION;
			}

			throw new IllegalStateException("No fragment at position "
					+ position);
		}
	}

	HashMap<String, String> map;

	private static final int EDIT_PROFILE = 1;
	private static final int EDIT_PROFILE_END = 2;

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EDIT_PROFILE: {
				if (map.size() > 0) {
					updateUserInfo(map);
				} else {
					finish();
				}
				break;
			}
			case EDIT_PROFILE_END: {
				if (!msg.getData().getBoolean(RESULT, false)) {
					mCopyUser = mUser.clone();
                    //failed
                    showCustomToast(msg.getData().getString(ERROR_MSG));
                    QiupuHelper.updateActivityUI(mCopyUser);
                } else {
                    //succeed
                	orm.insertUserinfo(mCopyUser);
                	mUser = mCopyUser.clone();
                    showCustomToast(R.string.edit_profile_update_suc);
                    isDataChanged = true;
					if (edu_frag != null) {
						edu_frag.refreshProfileUI();
					}
					if (work_fragment != null) {
						work_fragment.refreshProfileUI();
					}
                }
				dismissDialog(DIALOG_PROFILE_UPDATE_SERVER);
				break;
			}
			}
		}
	}

	Object mEditInfoLock = new Object();
	boolean inEditProcess;

	@Override
	public void updateUserInfo(HashMap<String, String> coloumsMap) {
		if (!AccountServiceUtils.isAccountReady()) {
			return;
		}

		synchronized (mEditInfoLock) {
			if (inEditProcess == true) {
				Log.d(TAG, "in update info data");
				return;
			}
		}
		showDialog(DIALOG_PROFILE_UPDATE_SERVER);

		synchronized (mEditInfoLock) {
			inEditProcess = true;
		}
		asyncQiupu.updateUserInfo(AccountServiceUtils.getSessionID(),
				coloumsMap, new TwitterAdapter() {
					public void updateUserInfo(boolean result) {
						Log.d(TAG, "finish edit user profile");
						synchronized (mEditInfoLock) {
							inEditProcess = false;
						}

						Message msg = mHandler.obtainMessage(EDIT_PROFILE_END);
						msg.getData().putBoolean(RESULT, result);
						msg.sendToTarget();
					}

					public void onException(TwitterException ex,
							TwitterMethod method) {

						synchronized (mEditInfoLock) {
							inEditProcess = false;
						}
						TwitterExceptionUtils
								.printException(TAG,
										"updateUserInfo, server exception:",
										ex, method);

						Message msg = mHandler.obtainMessage(EDIT_PROFILE_END);
						msg.getData().putBoolean(RESULT, false);
						msg.getData().putString(ERROR_MSG, ex.getMessage());
						msg.sendToTarget();
					}
				});
	}

	public QiupuUser getmUser() {
		return mUser;
	}

	public QiupuUser getmCopyUser() {
		return mCopyUser;
	}

	@Override
	public void callback(EditProfilesBaseInfoFragment base_fragment) {
		this.base_fragment = base_fragment;
	}

	@Override
	public void getWorkFragment(EditWorkExperFragment work_fragment) {
		this.work_fragment = work_fragment;

	}

	public void showDialog(int message_resId,
			DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builer = new AlertDialog.Builder(this);
		AlertDialog dialog;
		builer.setPositiveButton(R.string.label_ok, listener)
				.setNegativeButton(R.string.label_cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
		dialog = builer.create();
		dialog.setMessage(getString(message_resId));
		dialog.show();

	}

	@Override
	public void getEducationFragment(EducationFragment f) {
		this.edu_frag = f;

	}

	@Override
	public void updateUser(HashMap<String, String> map) {
		updateUserInfo(map);

	}

	@Override
	public void delete(int position) {
		HashMap<String, String> eduMap = new HashMap<String, String>();
		mCopyUser.education_list.remove(position);
		String education_history = JSONUtil
				.createEducationJSONArray(mCopyUser.education_list);
		eduMap.put("education_history", education_history);
		updateUserInfo(eduMap);
	}

	@Override
	public void finish() {
		if (isDataChanged) {
			setResult(Activity.RESULT_OK);
		}
		super.finish();
	}

	@Override
	public void updateWorkExperience(HashMap<String, String> map) {
		updateUserInfo(map);

	}

	@Override
	public void deleteWorkExperience(int position) {
		HashMap<String, String> eduMap = new HashMap<String, String>();
		mCopyUser.work_history_list.remove(position);
		String work_history = JSONUtil
				.createWorkExperienceJSONArray(mCopyUser.work_history_list);
		eduMap.put("work_history", work_history);
		updateUserInfo(eduMap);

	}

	@Override
	public void updateBaseInfo(HashMap<String, String> map) {
		updateUserInfo(map);
	}

	@Override
	public QiupuUser getCopyUser() {
		// TODO Auto-generated method stub
		return mCopyUser;
	}

	@Override
	public QiupuUser getUser() {
		// TODO Auto-generated method stub
		return mUser;
	}
}
