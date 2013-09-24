package com.borqs.qiupu.ui.bpc.fragment;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.borqs.qiupu.fragment.BasicFragment;
import twitter4j.Education;
import twitter4j.QiupuUser;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.common.adapter.EducationAdapter;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.EditProfilesActivity;
import com.borqs.qiupu.util.JSONUtil;

public class EducationFragment extends BasicFragment {
	private static final String TAG = "EducationFragment";
	private EditProfilesActivity myActivity;
	private QiupuUser mUser;
	private QiupuUser mCopyUser;
	// private LinearLayout edu_container;
	private View add_edu;
	private ListView eduListView;
	LayoutInflater flater;
	View btn_layout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myActivity = (EditProfilesActivity) getActivity();
		dialogitem = getResources().getTextArray(R.array.dialog_date_pick);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		flater = inflater;
		View v = inflater
				.inflate(R.layout.education_fragment, container, false);
		btn_layout = inflater.inflate(R.layout.btn_layout, null);
		eduListView = (ListView) v.findViewById(R.id.eduListView);
		eduListView.addFooterView(btn_layout);
		add_edu = btn_layout.findViewById(R.id.add);
		// edu_container = (LinearLayout)v.findViewById(R.id.edu_container);
		add_edu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				addEducationItem(-1);
			}
		});
		return v;
	}

	private void addEducationItem(final int position) {

		AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
		final View eduItem = flater.inflate(R.layout.education_item, null);
		final EditText et_begin_date = (EditText) eduItem
				.findViewById(R.id.et_begin_date);
		final EditText et_end_date = (EditText) eduItem
				.findViewById(R.id.et_end_date);
		if (position >= 0) {
			Education edu = mCopyUser.education_list.get(position);
			EditText edit_school = (EditText) eduItem
					.findViewById(R.id.edit_school);
			EditText edit_type = (EditText) eduItem
					.findViewById(R.id.edit_type);
			EditText edit_location = (EditText) eduItem
					.findViewById(R.id.edit_location);
			EditText et_class = (EditText) eduItem.findViewById(R.id.et_class);
			EditText et_degree = (EditText) eduItem
					.findViewById(R.id.et_degree);
			EditText et_major = (EditText) eduItem.findViewById(R.id.et_major);
			et_begin_date.setText(edu.from);
			et_end_date.setText(edu.to);
			edit_school.setText(edu.school);
			edit_type.setText(edu.type);
			edit_location.setText(edu.school_location);
			et_class.setText(edu.school_class);
			et_degree.setText(edu.degree);
			et_major.setText(edu.major);

		}
		et_begin_date.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (et_begin_date.getText().toString().trim().length() > 0) {
					showBirthdayDialog(et_begin_date, et_begin_date.getText()
							.toString().trim());
				} else {
					showPickData(et_begin_date, null);
				}
			}
		});
		et_end_date.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (et_end_date.getText().toString().trim().length() > 0) {
					showBirthdayDialog(et_end_date, et_end_date.getText()
							.toString().trim());
				} else {
					showPickData(et_end_date, null);
				}
			}
		});
		builder.setView(eduItem)
				.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                        setEnableDimissDialog(dialog, true);
                    }
                })
				.setPositiveButton(R.string.add,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								HashMap<String, String> map = new HashMap<String, String>();
								boolean isSubmit = editMap(map, eduItem,
										position);
								if (isSubmit) {
								    setEnableDimissDialog(dialog, true);
									listener.updateUser(map);
									// }else {
									// Toast.makeText(myActivity, "不能全部为空",
									// Toast.LENGTH_SHORT).show();
								}else {
								    setEnableDimissDialog(dialog, false);
								}
							}
						}).setTitle(R.string.add_edu);
		AlertDialog dialog = builder.show();
		// Button btn_del = (Button)dialog.findViewById(R.id.btn_del);
		// btn_del.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // listener.updateUser(map);
		//
		// }
		// });
		// dialog.show();
	}
	void setEnableDimissDialog(DialogInterface dialog,boolean flag) {
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private void showConfirmDel(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
		builder.setNegativeButton(R.string.label_cancel, null)
				.setPositiveButton(R.string.label_ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								listener.delete(position);
							}
						}).setMessage(R.string.confirm_delete_record).show();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mUser = myActivity.getmUser();
		mCopyUser = myActivity.getmCopyUser();
		refreshProfileUI();
		eduListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						addEducationItem(position);
					}
				});
		eduListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						final int index = position;
						String[] items = { getString(R.string.delete) };
						AlertDialog dialog = new AlertDialog.Builder(myActivity)
								.setItems(items,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												if (which == 0) {
													dialog.dismiss();
													showConfirmDel(index);
												}
											}
										}).show();
						return false;
					}
				});
	}

	public void refreshProfileUI() {
		ArrayList<Education> edu_list = mCopyUser.education_list;
		if (edu_list != null) {
			eduListView.setAdapter(new EducationAdapter(myActivity, edu_list));
		}
	}

	public boolean editMap(HashMap<String, String> map, View eduItem,
			int position) {

		// int child_count = edu_container.getChildCount();
		ArrayList<Education> eduList = mCopyUser.education_list;
		// for(int i=0;i<child_count;i++) {
		EditText edit_school = (EditText) eduItem
				.findViewById(R.id.edit_school);
		EditText edit_type = (EditText) eduItem.findViewById(R.id.edit_type);
		EditText edit_location = (EditText) eduItem
				.findViewById(R.id.edit_location);
		EditText et_class = (EditText) eduItem.findViewById(R.id.et_class);
		EditText et_begin_date = (EditText) eduItem
				.findViewById(R.id.et_begin_date);
		EditText et_end_date = (EditText) eduItem
				.findViewById(R.id.et_end_date);
		EditText et_degree = (EditText) eduItem.findViewById(R.id.et_degree);
		EditText et_major = (EditText) eduItem.findViewById(R.id.et_major);

		String str_school = edit_school.getText().toString().trim();
		String str_type = edit_type.getText().toString().trim();
		String str_location = edit_location.getText().toString().trim();
		String str_class = et_class.getText().toString().trim();
		String str_begin_date = et_begin_date.getText().toString().trim();
		String str_end_date = et_end_date.getText().toString().trim();
		String str_degree = et_degree.getText().toString().trim();
		String str_major = et_major.getText().toString().trim();
		if (TextUtils.isEmpty(str_begin_date)) {
			Toast.makeText(myActivity, R.string.begin_date_not_null,
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(str_end_date)) {
			Toast.makeText(myActivity, R.string.end_date_not_null,
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!(TextUtils.isEmpty(str_school) && TextUtils.isEmpty(str_type)
				&& TextUtils.isEmpty(str_location)
				&& TextUtils.isEmpty(str_class)
				&& TextUtils.isEmpty(str_degree) && TextUtils
					.isEmpty(str_major))) {
			if (position >= 0) {
				Education edu = mCopyUser.education_list.get(position);
				edu.from = str_begin_date;
				edu.to = str_end_date;
				edu.school = str_school;
				edu.type = str_type;
				edu.school_location = str_location;
				edu.school_class = str_class;
				edu.degree = str_degree;
				edu.major = str_major;
			} else {
				Education new_edu = new Education();
				new_edu.uid = mCopyUser.uid;
				new_edu.from = str_begin_date;
				new_edu.to = str_end_date;
				new_edu.school = str_school;
				new_edu.type = str_type;
				new_edu.school_location = str_location;
				new_edu.school_class = str_class;
				new_edu.degree = str_degree;
				new_edu.major = str_major;

				eduList.add(new_edu);
			}
			String education_history = createEducationJsonArray(eduList);
			map.put("education_history", education_history);
//			return true;
		}
//		return false;
		return true;
	}

	private String createEducationJsonArray(ArrayList<Education> eduList) {
		return JSONUtil.createEducationJSONArray(eduList);
	}

	onEducationListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (onEducationListener) activity;
			listener.getEducationFragment(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement onCallBackListener");
		}
	}

	public interface onEducationListener {
		public void getEducationFragment(EducationFragment f);

		public void updateUser(HashMap<String, String> map);

		public void delete(int position);
	}

	private void showPickData(final EditText et_date, final String strDate) {
		Calendar calendar = Calendar.getInstance();
		DatePickerDialog dateDialog = new DatePickerDialog(
				getActivity(),
				new DatePickerDialog.OnDateSetListener() {
					public void onDateSet(DatePicker view, int year,
							int monthofYear, int dayofMonth) {
						// displayType = myActivity.DISPLAY_BIRTHDAY;
						Date dt = new Date(year - 1900, monthofYear, dayofMonth);
						long birthday = dt.getTime();
						et_date.setText(formatDate(birthday));
					}
				}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));
		if (!TextUtils.isEmpty(strDate)) {
			formatDate(strDate, dateDialog);
		}
		dateDialog.show();
	}

	private String formatDate(long time) {
		Date date = new Date(Long.valueOf(time));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(date);
	}

	private void formatDate(String strDate, DatePickerDialog dateDialog) {
		String[] s = strDate.split("-");
		dateDialog.updateDate(Integer.valueOf(s[0]), Integer.valueOf(s[1]),
				Integer.valueOf(s[2]));
	}

	private int mSingleChoiceID = 0;
	private CharSequence[] dialogitem;

	private String formatbirthday(long time) {
		Date date = new Date(Long.valueOf(time));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(date);
	}

	private void showBirthdayDialog(final EditText detail_birthday,
			final String strDate) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.set_birthday_title);
		builder.setSingleChoiceItems(dialogitem, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSingleChoiceID = whichButton;
						Log.d(TAG, "you select id" + whichButton + " , "
								+ dialogitem[whichButton]);
					}
				});
		builder.setPositiveButton(R.string.label_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mSingleChoiceID == 0) {
							showPickData(detail_birthday, strDate);
						} else if (mSingleChoiceID == 1) {
							detail_birthday.setText("");
						}
					}
				});
		builder.setNegativeButton(R.string.label_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		builder.create().show();
	}
}
