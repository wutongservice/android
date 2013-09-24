package com.borqs.common.view;

import android.content.Context;
import android.content.res.Resources;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.RequestActionListner;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.RequestFragment;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.QiupuUser;
import twitter4j.Requests;

public class MulitPhoneEmailItemView extends SNSItemView {

    private static final String TAG = "MulitPhoneEmailItemView";
    private ImageView icon;
    private TextView name;
    private LinearLayout id_vcard;
    private QiupuUser mUser;
    private QiupuORM orm;

    public QiupuUser getItem() {
        return mUser;
    }

    public MulitPhoneEmailItemView(Context context) {
        super(context);
        orm = QiupuORM.getInstance(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public MulitPhoneEmailItemView(Context context, QiupuUser userinfo) {
        super(context);
        orm = QiupuORM.getInstance(context);
        mUser = userinfo;
        init();
    }

    public void setUserInfo(QiupuUser info){
        mUser = info;
    	init();
//    	setUI();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {

        removeAllViews();
        LayoutInflater factory = LayoutInflater.from(mContext);
        

        //child 1
        View convertView = factory.inflate(R.layout.mulit_phone_email_item_view, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        addView(convertView);

        icon = (ImageView) convertView.findViewById(R.id.id_user_icon);
        name = (TextView) convertView.findViewById(R.id.id_user_name);
        id_vcard = (LinearLayout) convertView.findViewById(R.id.id_vcard);
        
        name.setMovementMethod(LinkMovementMethod.getInstance());
        name.setLinksClickable(true);
        setUI();
    }

    private void setUI() {
    	
    	if(mUser != null) {
    	    id_vcard.removeAllViews();
    	    
    		// set user icon
    		icon.setImageResource(R.drawable.default_user_icon);
    		ImageRun imagerun = new ImageRun(null,mUser.profile_image_url, 0);
    		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
    		imagerun.noimage = true;
    		imagerun.addHostAndPath = true;
    		imagerun.setRoundAngle=true;
    		imagerun.setImageView(icon);        
    		imagerun.post(null);
    		
    		name.setText(mUser.nick_name);
    		
    		setContactInfoUi();
    	}
    }
	
	private void setContactInfoUi(){
		id_vcard.setVisibility(View.VISIBLE);
		if(mUser.phoneList != null && mUser.phoneList.size() > 0) {
		    for(int i=0; i<mUser.phoneList.size(); i++) {
		        MulitPhoneEmailSelectItemView info = new MulitPhoneEmailSelectItemView(mContext, mUser.phoneList.get(i).info, mUser.nick_name, QiupuConfig.TYPE_PHONE);
		        id_vcard.addView(info);
		    }
		}
		if(mUser.emailList != null && mUser.emailList.size() > 0) {
            for(int i=0; i<mUser.emailList.size(); i++) {
                MulitPhoneEmailSelectItemView info = new MulitPhoneEmailSelectItemView(mContext, mUser.emailList.get(i).info, mUser.nick_name, QiupuConfig.TYPE_EMAIL);
                id_vcard.addView(info);
            }
        }
	}
}

