package com.borqs.common.adapter;

import java.util.List;

import twitter4j.Education;
import twitter4j.WorkExperience;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.EditProfilesActivity;

public class WorkExperienceAdapter extends ArrayAdapter<WorkExperience> {
	private final List<WorkExperience> workList;
	private final EditProfilesActivity context;
	public WorkExperienceAdapter(EditProfilesActivity context,List<WorkExperience> workList) {
		super(context,R.layout.work_view_item,workList);
		
		this.workList = workList;
		this.context = context;
	}
	static class ViewHolder {
		TextView tv_date;
		TextView tv_company;
		TextView tv_department;
		TextView tv_job;
		TextView tv_addr;
		TextView tv_job_des;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final WorkExperience we = workList.get(position);
		View view;
		if (null == convertView) {
			LayoutInflater mInflater = context.getLayoutInflater();
			view = mInflater.inflate(R.layout.work_view_item, null);
		} else {
			view = convertView;
		}
		final ViewHolder viewHolder = new ViewHolder();
		viewHolder.tv_date = (TextView)view.findViewById(R.id.tv_date);
		viewHolder.tv_company = (TextView)view.findViewById(R.id.tv_company);
		viewHolder.tv_department = (TextView)view.findViewById(R.id.tv_department);
		viewHolder.tv_job = (TextView)view.findViewById(R.id.tv_job);
		viewHolder.tv_addr = (TextView)view.findViewById(R.id.tv_addr);
		viewHolder.tv_job_des = (TextView)view.findViewById(R.id.tv_job_des);
		
		viewHolder.tv_date.setText(we.from+" ～ "+we.to);
		viewHolder.tv_company.setText(we.company);
		viewHolder.tv_department.setText(we.department);
		viewHolder.tv_job.setText(we.job_title);
		viewHolder.tv_addr.setText(we.office_address);
		viewHolder.tv_job_des.setText(we.job_description);
		view.setTag(viewHolder);
		return view;
	}
	
}
