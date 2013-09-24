package com.borqs.qiupu.ui.bpc.fragment;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import twitter4j.QiupuUser;
import twitter4j.WorkExperience;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.common.adapter.WorkExperienceAdapter;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.BasicFragment;
import com.borqs.qiupu.ui.bpc.EditProfilesActivity;
import com.borqs.qiupu.util.JSONUtil;

public class EditWorkExperFragment extends BasicFragment {
	private static final String TAG = "EditWorkExperFragment";
	private EditProfilesActivity myActivity;
	private QiupuUser mUser;
	private QiupuUser mCopyUser;
	private View add_work;
	LayoutInflater flater;
	private ListView workListView;
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
		View v = inflater.inflate(R.layout.edit_work_experience, container,
				false);
		workListView = (ListView) v.findViewById(R.id.workListView);
		btn_layout = inflater.inflate(R.layout.btn_layout, null);
		workListView.addFooterView(btn_layout);
		add_work = btn_layout.findViewById(R.id.add);
		add_work.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				addWorkItem(-1);
			}
		});
		return v;
	}

	private void addWorkItem(final int position) {

		AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
		final View workItem = flater.inflate(R.layout.work_experience_item,
				null);
		final EditText et_begin_date = (EditText) workItem
				.findViewById(R.id.et_begin_date);
		final EditText et_end_date = (EditText) workItem
				.findViewById(R.id.et_end_date);
		if (position >= 0) {
			WorkExperience we = mCopyUser.work_history_list.get(position);
			EditText edit_company = (EditText) workItem
					.findViewById(R.id.edit_company);
			EditText edit_department = (EditText) workItem
					.findViewById(R.id.edit_department);
			EditText edit_job = (EditText) workItem.findViewById(R.id.edit_job);
			EditText et_office_address = (EditText) workItem
					.findViewById(R.id.et_office_address);
			EditText et_job_description = (EditText) workItem
					.findViewById(R.id.et_job_description);
			et_begin_date.setText(we.from);
			et_end_date.setText(we.to);
			edit_company.setText(we.company);
			edit_department.setText(we.department);
			edit_job.setText(we.job_title);
			et_office_address.setText(we.office_address);
			et_job_description.setText(we.job_description);

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
		builder.setView(workItem)
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
								boolean isSubmit = editMap(map, workItem,
										position);
								if (isSubmit) {
								    setEnableDimissDialog(dialog, true);
									listener.updateWorkExperience(map);
								}else {
								    setEnableDimissDialog(dialog, false);
								}
							}
						}).setTitle(R.string.add_work_history);
		AlertDialog dialog = builder.show();
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mUser = myActivity.getmUser();
		mCopyUser = myActivity.getmCopyUser();
		refreshProfileUI();
		workListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						addWorkItem(position);
					}
				});
		workListView
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

	private void showConfirmDel(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
		builder.setNegativeButton(R.string.label_cancel, null)
				.setPositiveButton(R.string.label_ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								listener.deleteWorkExperience(position);
							}
						}).setMessage(R.string.confirm_delete_record).show();
	}

	public void refreshProfileUI() {
		ArrayList<WorkExperience> w_list = mCopyUser.work_history_list;

		if (w_list != null) {
			workListView.setAdapter(new WorkExperienceAdapter(myActivity,
					w_list));
		}
	}

	public boolean editMap(HashMap<String, String> map, View workItem,
			int position) {
		ArrayList<WorkExperience> workList = mCopyUser.work_history_list;
		EditText et_address = (EditText) workItem
				.findViewById(R.id.et_office_address);
		EditText et_company = (EditText) workItem
				.findViewById(R.id.edit_company);
		EditText et_department = (EditText) workItem
				.findViewById(R.id.edit_department);
		EditText et_job_title = (EditText) workItem.findViewById(R.id.edit_job);
		EditText et_begin_date = (EditText) workItem
				.findViewById(R.id.et_begin_date);
		EditText et_end_date = (EditText) workItem
				.findViewById(R.id.et_end_date);
		EditText et_job_description = (EditText) workItem
				.findViewById(R.id.et_job_description);
		String str_addr = et_address.getText().toString().trim();
		String str_company = et_company.getText().toString().trim();
		String str_department = et_department.getText().toString().trim();
		String str_job_title = et_job_title.getText().toString().trim();
		String str_begin_date = et_begin_date.getText().toString().trim();
		String str_end_date = et_end_date.getText().toString().trim();
		String str_job_description = et_job_description.getText().toString()
				.trim();

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
		if (!(TextUtils.isEmpty(str_addr) && TextUtils.isEmpty(str_company)
				&& TextUtils.isEmpty(str_department)
				&& TextUtils.isEmpty(str_job_title) && TextUtils
					.isEmpty(str_job_description))) {
			WorkExperience w;
			if (position >= 0) {
				w = workList.get(position);
			} else {
				w = new WorkExperience();
				w.uid = mCopyUser.uid;
				workList.add(w);
			}
			w.from = str_begin_date;
			w.to = str_end_date;
			w.company = str_company;
			w.department = str_department;
			w.job_title = str_job_title;
			w.office_address = str_addr;
			w.job_description = str_job_description;
			String work_history = createWorkExperienceJsonArray(workList);
			map.put("work_history", work_history);
			mCopyUser.work_history_list = workList;
//			return true;
		}
		return true;
//		return false;
	}

	private String createWorkExperienceJsonArray(
			ArrayList<WorkExperience> workList) {
		return JSONUtil.createWorkExperienceJSONArray(workList);
	}

	onWorkFragmentListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (onWorkFragmentListener) activity;
			listener.getWorkFragment(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement onCallBackListener");
		}
	}

	public interface onWorkFragmentListener {
		public void getWorkFragment(EditWorkExperFragment work_fragment);

		public void updateWorkExperience(HashMap<String, String> map);

		public void deleteWorkExperience(int position);
	}

	private void showPickData(final EditText et_date) {
		Calendar calendar = Calendar.getInstance();
		new DatePickerDialog(
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
				calendar.get(Calendar.DAY_OF_MONTH)).show();
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
