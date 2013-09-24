package com.borqs.common.view;

import twitter4j.Circletemplate;
import twitter4j.Circletemplate.TemplateInfo;
import twitter4j.UserCircle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.util.StringUtil;

public class CreateCircleItemView extends SNSItemView {

    private static final String TAG = "CreateCircleItemView";
    private TemplateInfo mInfo;
    private ImageView mCover;
    private TextView mTitle;
    private TextView mSummary;
    
    public CreateCircleItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public CreateCircleItemView(Context context, TemplateInfo info) {
        super(context);

        mInfo = info;
        init();
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
        View convertView = factory.inflate(R.layout.create_circle_item_view, null);
//        int width = getResources().getDisplayMetrics().widthPixels;
//        int height = getResources().getDisplayMetrics().heightPixels;
//        width = (int) (width - 2 * getResources().getDimension(R.dimen.create_circle_item_padding));
//        
//        height = (int)(height - 2 * getResources().getDimension(R.dimen.title_bar_height) - 60);
//        convertView.setLayoutParams(new LayoutParams(width/2, height/2));
        addView(convertView);
        mCover = (ImageView) convertView.findViewById(R.id.circle_cover);
        mTitle = (TextView) convertView.findViewById(R.id.circle_title);
        mSummary = (TextView) convertView.findViewById(R.id.circle_summary);
        setUI();
    }

    public void setTemplate(TemplateInfo info) {
    	mInfo = info;
        setUI();
    }

    private void setUI() {
    	if (mInfo != null) {
    		if(QiupuHelper.isZhCNLanguage(mContext)) {
    			if(StringUtil.isValidString(mInfo.title)) {
    				mTitle.setText(mInfo.title);
    			}else {
    				mTitle.setText(mInfo.title_en);
    			}
    			
    			if(StringUtil.isValidString(mInfo.description)) {
    				mSummary.setText(mInfo.description);
    			}else {
    				mSummary.setText(mInfo.description_en);
    			}
    		}else {
    			if(StringUtil.isValidString(mInfo.title_en)) {
    				mTitle.setText(mInfo.title_en);
    			}else {
    				mTitle.setText(mInfo.title);
    			}
    			
    			if(StringUtil.isValidString(mInfo.description_en)) {
    				mSummary.setText(mInfo.description_en);
    			}else {
    				mSummary.setText(mInfo.description);
    			}
    		}
    			
    		setPageIcon();
    	}
    }
    
    private void setPageIcon() {
    	if(StringUtil.isValidString(mInfo.icon_url)) {
    		ImageRun imagerun = new ImageRun(null,mInfo.icon_url, 0);
    		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
    		imagerun.noimage = true;
    		imagerun.addHostAndPath = true;
    		imagerun.setRoundAngle=true;
    		imagerun.setImageView(mCover);
    		imagerun.post(null);
    	}else {
    		if(Circletemplate.TEMPLATE_FORMAL_NAME.equals(mInfo.name)) {
    			mCover.setImageResource(R.drawable.formal_circle_default_photo);
    		}else if(Circletemplate.TEMPLATE_FREE_NAME.equals(mInfo.name)){
    			mCover.setImageResource(R.drawable.free_circle_default_photo);
    		}else if(Circletemplate.TEMPLATE_NAME_PROJECT.equals(mInfo.name)) {
    			mCover.setImageResource(R.drawable.project_circle_default);
    		}else if(Circletemplate.TEMPLATE_NAME_APPLICATION.equals(mInfo.name)) {
    			mCover.setImageResource(R.drawable.app_circle_default);
    		}else if(mInfo.formal == UserCircle.circle_free) {
    			mCover.setImageResource(R.drawable.free_circle_default_photo);
    		}else {
    			mCover.setImageResource(R.drawable.formal_circle_default_photo);
    		}
    	}
    }
    
    
    public TemplateInfo getItem(){
    	return mInfo;
    }
}

