package com.borqs.common.adapter;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-10-11
 * Time: 下午12:13
 * To change this template use File | Settings | File Templates.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.ShareSourceItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

/**
 * GridView adapter to show the list of all installed applications.
 */
public class ShareSourceAdapter extends BaseAdapter{
    private static final String TAG = "Qiupu.ShareSourceAdapter";
    private Context mContext;
    private List<ShareSourceItem> mSources = new ArrayList<ShareSourceItem>();
    private QiupuORM orm;

    public ShareSourceAdapter(Context context) {
        mContext = context;
    }

    public int getCount() {
        return (mSources != null && mSources.size() > 0) ? (mSources.size()) : 0;
    }

    public ShareSourceItem getItem(int pos) {
        if (pos >= mSources.size()) {
            return null;
        }
        return mSources.get(pos);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final ShareSourceItem source = getItem(position);
        if (source != null) {
            if (convertView == null || false == (convertView instanceof ShareSourceItemView)) {
                ShareSourceItemView view = new ShareSourceItemView(mContext, source);
                view.setContent(source);
                holder = new ViewHolder();
                holder.view = view;
                view.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setContent(source);
            }
        } else {
            return null;
        }
        return holder.view;
    }

    class ViewHolder {
        public ShareSourceItemView view;
    }

    public void alterDataList(List<ShareSourceItem> dataList) {
        mSources.clear();
        mSources.addAll(dataList);

        notifyDataSetChanged();
    }

    public class ShareSourceItemView extends SNSItemView {

        private static final String TAG = "RequestItemView";
        private ImageView icon;
        private TextView label;

        private ShareSourceItem mSourceItem;

        public ShareSourceItem getShareSourceItem() {
            return mSourceItem;
        }

        public ShareSourceItemView(Context context) {
            super(context);
            orm = QiupuORM.getInstance(context);
        }

        @Override
        public String getText() {
            return null;
        }

        public ShareSourceItemView(Context context, ShareSourceItem request) {
            super(context);
            mSourceItem = request;
            init();
        }

        public void setContent(ShareSourceItem request) {
            mSourceItem = request;
            setUI();
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            init();
        }

        private void init() {

            LayoutInflater factory = LayoutInflater.from(mContext);
            removeAllViews();

            //child 1
            View convertView = factory.inflate(R.layout.share_source_item_view, null);
            convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            addView(convertView);

            icon = (ImageView) convertView.findViewById(R.id.app_icon);
            label = (TextView) convertView.findViewById(R.id.app_label);

            setUI();
        }

        private void setUI() {

            if (mSourceItem != null) {
                // set user icon
                try {
                	if(mSourceItem.mId != null && mSourceItem.mId.length() > 0){
                		icon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(mSourceItem.mId));
                	}else{
                		icon.setImageResource(getSorceItemIcon());
                	}
                } catch (PackageManager.NameNotFoundException e) {
                    icon.setImageResource(R.drawable.default_app_icon);
                }
            	
            	

                //set user label
                View head_progress = findViewById(R.id.head_progress);
                if (mSourceItem.mCount < 0) {
                    label.setText(getSourceItemLabel(mSourceItem.mLabel));
                    if (null != head_progress) {
                        head_progress.setVisibility(View.VISIBLE);
                    }
                } else {
                    label.setText(getSourceItemLabel(mSourceItem.mLabel) + " (" + mSourceItem.mCount + ")");
                    if (null != head_progress) {
                        head_progress.setVisibility(View.GONE);
                    }
                }
            }
        }
        
        public String getSourceItemLabel(String label) {
        	if(BpcApiUtils.TEXT_POST == mSourceItem.mType){
        		return mContext.getString(R.string.resource_share_text);
        	}else if(BpcApiUtils.APK_POST == mSourceItem.mType) {
        		return mContext.getString(R.string.resource_share_apps);
        	}else if(BpcApiUtils.BOOK_POST == mSourceItem.mType) {
        		return mContext.getString(R.string.resource_share_books);
        	}else if(BpcApiUtils.LINK_POST == mSourceItem.mType) {
        		return mContext.getString(R.string.resource_share_links);
        	}else if(BpcApiUtils.IMAGE_POST == mSourceItem.mType) {
        		return mContext.getString(R.string.resource_share_photos);
        	}else {
        		return label;
        	}
        }
        
        private int getSorceItemIcon() {
        	if(BpcApiUtils.TEXT_POST == mSourceItem.mType){
        		return R.drawable.share_text;
        	}else if(BpcApiUtils.APK_POST == mSourceItem.mType) {
        		return R.drawable.share_app;
        	}else if(BpcApiUtils.BOOK_POST == mSourceItem.mType) {
        		return R.drawable.share_book;
        	}else if(BpcApiUtils.LINK_POST == mSourceItem.mType) {
        		return R.drawable.share_link;
        	}else if(BpcApiUtils.IMAGE_POST == mSourceItem.mType) {
        		return R.drawable.share_picture;
        	}else {
        		return R.drawable.default_app_icon;
        	}
        }
    }
}
