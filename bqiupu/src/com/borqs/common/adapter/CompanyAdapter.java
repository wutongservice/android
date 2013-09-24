package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.Company;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.CompanyItemView;

public class CompanyAdapter extends BaseAdapter {

    private static final String TAG = "CompanyAdapter";

    private Context             mContext;
    private ArrayList<Company> mDataList = new ArrayList<Company>();
    

    public CompanyAdapter(Context context,ArrayList<Company> dataList) {
        mContext = context;
        mDataList.addAll(dataList);
    }

    public void alterDataList(ArrayList<Company> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public int getCount() {
        return (mDataList != null && mDataList.size()>0)? mDataList.size()
                :0;
    }

    public Company getItem(int position) {
        if(position >= mDataList.size())
        {
            return null;
        }
        return mDataList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        Company company = getItem(position);
        if (company != null) {
            if (convertView == null || false == (convertView instanceof CompanyItemView)) {
            	convertView = new CompanyItemView(mContext, company);
                holder = new ViewHolder();

                holder.view = (CompanyItemView)convertView;
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setCompanyInfo(company);
            }
        }
        return convertView;

    }

    static class ViewHolder {
        public CompanyItemView view;
    }
    
    

}
