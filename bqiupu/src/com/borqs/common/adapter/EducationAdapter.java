package com.borqs.common.adapter;

import java.util.List;

import twitter4j.Education;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.EditProfilesActivity;

public class EducationAdapter extends ArrayAdapter<Education> {
	private final List<Education> eduList;
	private final EditProfilesActivity context;
	public EducationAdapter(EditProfilesActivity context,List<Education> eduList) {
		super(context,R.layout.education_view_item,eduList);
		
		this.eduList = eduList;
		this.context = context;
	}
	static class ViewHolder {
//		TextView tv_school = (TextView)findViewById(R.id.tv_school);
//		TextView tv_type = (TextView)findViewById(R.id.tv_type);
//		TextView tv_location = (TextView)findViewById(R.id.tv_location);
//		TextView tv_class = (TextView)findViewById(R.id.tv_class);
//		TextView tv_date = (TextView)findViewById(R.id.tv_date);
//		TextView tv_degree = (TextView)findViewById(R.id.tv_degree);
//		TextView tv_major = (TextView)findViewById(R.id.tv_major);
//		View img_remove = findViewById(R.id.img_remove);
		TextView tv_school;
		TextView tv_type;
		TextView tv_location;
		TextView tv_class;
		TextView tv_date;
		TextView tv_degree;
		TextView tv_major;
		View img_remove;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Education edu = eduList.get(position);
		View view;
		if (null == convertView) {
			LayoutInflater mInflater = context.getLayoutInflater();
			view = mInflater.inflate(R.layout.education_view_item, null);
		} else {
			view = convertView;
		}
		final ViewHolder viewHolder = new ViewHolder();
		viewHolder.tv_school = (TextView)view.findViewById(R.id.tv_school);
		viewHolder.tv_type = (TextView)view.findViewById(R.id.tv_type);
		viewHolder.tv_location = (TextView)view.findViewById(R.id.tv_location);
		viewHolder.tv_class = (TextView)view.findViewById(R.id.tv_class);
		viewHolder.tv_date = (TextView)view.findViewById(R.id.tv_date);
		viewHolder.tv_degree = (TextView)view.findViewById(R.id.tv_degree);
		viewHolder.tv_major = (TextView)view.findViewById(R.id.tv_major);
		viewHolder.img_remove = view.findViewById(R.id.img_remove);
		
		viewHolder.tv_date.setText(edu.from+" ～ "+edu.to);
		viewHolder.tv_school.setText(edu.school);
		viewHolder.tv_type.setText(edu.type);
		viewHolder.tv_location.setText(edu.school);
		viewHolder.tv_class.setText(edu.school_class);
		viewHolder.tv_degree.setText(edu.degree);
		viewHolder.tv_major.setText(edu.major);
		final int posi = position;
//		viewHolder.img_remove.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				listener.delete(posi);
//				
//			}
//		});
		view.setTag(viewHolder);
		return view;
	}
	
}
