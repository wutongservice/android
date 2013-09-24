package com.borqs.qiupu.fragment;

import twitter4j.AsyncQiupu;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle.RecieveSet;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class EditPublicCircleReceiveSetFragment extends BasicFragment {
	private final static String TAG = "EditPublicCircleReceiveSetFragment";

	private Activity mActivity;
	private AsyncQiupu asyncQiupu;
	private QiupuORM orm;
	private Handler mHandler;

	private ProgressDialog mprogressDialog;

	private View layout_phone;
	private View layout_email;

	private TextView phone_tv;
	private TextView phone_value;

	private TextView email_tv;
	private TextView email_value;
	private CompoundButton mToggleButton;

	private long mCircleId;
	private RecieveSet mSet;
	private static final String RESULT = "result";
	private static final int type_phone = 0;
	private static final int type_email = 1;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try {
			ReceiveSetFragmentCallBack callback = (ReceiveSetFragmentCallBack) mActivity;
			callback.getREceiveSetFragment(this);
			mCircleId = callback.getCircleId();

		} catch (ClassCastException e) {
			Log.d(TAG, activity.toString()
					+ "must implement CallBackAppsSearchFragment");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null,
				null);
		mHandler = new MainHandler();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View contentView = inflater.inflate(
				R.layout.public_circle_receive_set_ui, container, false);
		View enable_receive_rl = contentView
				.findViewById(R.id.enable_receive_rl);
		enable_receive_rl.setOnClickListener(receiveClickListener);
		
		TextView enable_label = (TextView) contentView.findViewById(R.id.item_title);
		enable_label.setText(R.string.circle_receive_enable_label);
		
		mToggleButton = (CompoundButton) contentView
				.findViewById(R.id.item_button);
		mToggleButton.setOnCheckedChangeListener(receiveChangedListener);

		layout_phone = contentView.findViewById(R.id.layout_phone);
		layout_email = contentView.findViewById(R.id.layout_email);

		phone_tv = (TextView) contentView.findViewById(R.id.phone_tv);
		phone_value = (TextView) contentView.findViewById(R.id.phone_value);

		email_tv = (TextView) contentView.findViewById(R.id.email_tv);
		email_value = (TextView) contentView.findViewById(R.id.email_value);

		mHandler.obtainMessage(GET_CIRCLE_RECEIVE_SET).sendToTarget();
		return contentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	OnCheckedChangeListener receiveChangedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			setViewEnable(isChecked);
			if (isChecked) {
				layout_phone.setOnClickListener(PhoneClickListener);
				layout_email.setOnClickListener(EmailClickListener);
			} else {
				layout_phone.setOnClickListener(null);
				layout_email.setOnClickListener(null);
			}
		}
	};

	View.OnClickListener PhoneClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			showAddCircleDialog(type_phone, phone_value.getText().toString()
					.trim());
		}
	};

	View.OnClickListener EmailClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			showAddCircleDialog(type_email, email_value.getText().toString()
					.trim());
		}
	};

	View.OnClickListener receiveClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mToggleButton.setChecked(!mToggleButton.isChecked());
		}
	};

	private final int GET_CIRCLE_RECEIVE_SET = 101;
	private final int GET_CIRCLE_RECEIVE_SET_END = 102;
	private final int SET_CIRCLE_RECEVIE_END = 103;

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_CIRCLE_RECEIVE_SET: {
				getCircleReceiveSet(mCircleId);
				break;
			}
			case GET_CIRCLE_RECEIVE_SET_END: {
				try {
					mprogressDialog.dismiss();
					mprogressDialog = null;
				} catch (Exception e) {
				}
				if (msg.getData().getBoolean(BasicActivity.RESULT)) {
					refreshReceiveSetUi();
				} else {
					showLoadFailedDailog();
//					ToastUtil.showShortToast(mActivity, mHandler,
//							R.string.error_toast_get_failed);
					// finish();
				}
				break;
			}
			case SET_CIRCLE_RECEVIE_END: {
				try {
					mprogressDialog.dismiss();
					mprogressDialog = null;
				} catch (Exception e) {
				}
				if (msg.getData().getBoolean(BasicActivity.RESULT)) {
					ToastUtil.showOperationOk(mActivity, mHandler, true);
					mActivity.finish();
				} else {
					ToastUtil.showOperationFailed(mActivity, mHandler, true);
				}

				break;
			}
			}
		}
	}

	public interface ReceiveSetFragmentCallBack {
		public void getREceiveSetFragment(
				EditPublicCircleReceiveSetFragment fragment);

		public long getCircleId();
	}

	boolean inLoadingReceiveSet = false;
	Object mReceiveSetLock = new Object();

	public void getCircleReceiveSet(final long circleId) {
		synchronized (mReceiveSetLock) {
			if (inLoadingReceiveSet == true) {
				Log.d(TAG, "in doing get Follower data");
				return;
			}
		}

		synchronized (mReceiveSetLock) {
			inLoadingReceiveSet = true;
		}
		showProcessDialog(R.string.loading, false, true, false);

		asyncQiupu.getCircleReceiveSet(AccountServiceUtils.getSessionID(),
				circleId, new TwitterAdapter() {
					public void getCircleReceiveSet(RecieveSet set) {

						mSet = set;
						synchronized (mReceiveSetLock) {
							inLoadingReceiveSet = false;
						}

						Message msg = mHandler
								.obtainMessage(GET_CIRCLE_RECEIVE_SET_END);
						msg.getData().putBoolean(BasicActivity.RESULT, true);
						msg.sendToTarget();
					}

					public void onException(TwitterException ex,
							TwitterMethod method) {
						synchronized (mReceiveSetLock) {
							inLoadingReceiveSet = false;
						}

						Message msg = mHandler
								.obtainMessage(GET_CIRCLE_RECEIVE_SET_END);
						msg.getData().putString(BasicActivity.ERROR_MSG,
								ex.getMessage());
						msg.getData().putBoolean(BasicActivity.RESULT, false);
						msg.sendToTarget();
					}
				});
	}

	boolean inSettingReceiveSet = false;
	Object mSetLock = new Object();

	public void setReceive(final long circleId, final int enable,
			final String phone, final String email) {
		synchronized (mSetLock) {
			if (inSettingReceiveSet == true) {
				Log.d(TAG, "in doing get Follower data");
				return;
			}
		}

		synchronized (mSetLock) {
			inSettingReceiveSet = true;
		}
		showProcessDialog(R.string.toast_setting, false, true, true);

		asyncQiupu.setCircleReceiveSet(AccountServiceUtils.getSessionID(),
				circleId, enable, phone, email, new TwitterAdapter() {
					public void setCircleReceiveSet(boolean result) {

						synchronized (mSetLock) {
							inSettingReceiveSet = false;
						}

						Message msg = mHandler
								.obtainMessage(SET_CIRCLE_RECEVIE_END);
						msg.getData().putBoolean(BasicActivity.RESULT, true);
						msg.sendToTarget();
					}

					public void onException(TwitterException ex,
							TwitterMethod method) {
						synchronized (mSetLock) {
							inSettingReceiveSet = false;
						}

						Message msg = mHandler
								.obtainMessage(SET_CIRCLE_RECEVIE_END);
						msg.getData().putString(BasicActivity.ERROR_MSG,
								ex.getMessage());
						msg.getData().putBoolean(BasicActivity.RESULT, false);
						msg.sendToTarget();
					}
				});
	}

	private void showProcessDialog(int resId, boolean CanceledOnTouchOutside,
			boolean Indeterminate, boolean cancelable) {
		mprogressDialog = DialogUtils.createProgressDialog(mActivity, resId,
				CanceledOnTouchOutside, Indeterminate, cancelable);
		mprogressDialog.show();
	}

	private void refreshReceiveSetUi() {
		if (mSet != null) {
			mToggleButton
					.setChecked(mSet.enable == RecieveSet.EBABLE_NOTIFICATION);

			if (StringUtil.isValidString(mSet.phone)) {
				phone_value.setText(mSet.phone);
			}
			if (StringUtil.isValidString(mSet.email)) {
				email_value.setText(mSet.email);
			}
		}
	}

	private EditText mEditContext;
	private int mType;

	public void showAddCircleDialog(int type, String content) {
		mType = type;
		String title = "";
		LayoutInflater factory = LayoutInflater.from(mActivity);
		final View textEntryView = factory.inflate(
				R.layout.create_circle_dialog, null);
		mEditContext = (EditText) textEntryView
				.findViewById(R.id.new_circle_edt);
		mEditContext.requestFocus();
		if (type_phone == type) {
			mEditContext.setInputType(InputType.TYPE_CLASS_NUMBER);
			title = getString(R.string.add_phone_title);
			mEditContext.setHint(R.string.circle_add_receive_phone);
		} else {
			title = getString(R.string.add_email_title);
			mEditContext.setHint(R.string.circle_add_receive_email);
		}
		if (StringUtil.isValidString(content)) {
			mEditContext.setText(content);
			mEditContext.selectAll();
		}
		DialogUtils.ShowDialogwithView(mActivity, title, -1, textEntryView, positiveListener, negativeListener);
	}

	DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mEditContext != null) {
				final String textContent = mEditContext.getText().toString()
						.trim();
				if (type_phone == mType) {
					phone_value.setText(textContent);
				} else if (type_email == mType) {
					email_value.setText(textContent);
				}
			}
		}
	};

	DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		}
	};

	private void setViewEnable(boolean isChecked) {
		layout_phone.setEnabled(isChecked);
		layout_email.setEnabled(isChecked);
		phone_tv.setEnabled(isChecked);
		phone_value.setEnabled(isChecked);
		email_tv.setEnabled(isChecked);
		email_value.setEnabled(isChecked);
	}
	
	private void showLoadFailedDailog() {
		AlertDialog dialog = DialogUtils.showConfirmDialog(mActivity, getString(R.string.public_circle_exit_toast), getString(R.string.error_toast_get_failed), failedPositiveListener, failedNegativeListener);
		dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
	}
	
	DialogInterface.OnClickListener failedNegativeListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			mActivity.finish();
		}
	};
	
	DialogInterface.OnClickListener failedPositiveListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			mHandler.obtainMessage(GET_CIRCLE_RECEIVE_SET).sendToTarget();			
		}
	};
	
	public void editReceiveSet() {
		boolean isNeedUpdate = false;
		final int isEnable = mToggleButton.isChecked() ? RecieveSet.EBABLE_NOTIFICATION
				: RecieveSet.DISABLE_NOTIFICATION;
		final String phone = phone_value.getText().toString().trim();
		final String email = email_value.getText().toString().trim();
		if (mSet != null) {
			if (mSet.enable == isEnable) {
				if(isEnable == RecieveSet.EBABLE_NOTIFICATION) {
					if (phone.equals(mSet.phone) == false) {
						isNeedUpdate = true;
					}
					if (email.equals(mSet.email) == false) {
						isNeedUpdate = true;
					}
				}
			} else {
				isNeedUpdate = true;
			}
		} else {
			isNeedUpdate = true;
		}
		if (isNeedUpdate) {
			setReceive(mCircleId, isEnable, phone, email);
		}else {
			mActivity.finish();
		}		
	}
}
