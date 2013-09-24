package com.borqs.common.view;

import twitter4j.Requests;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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
import com.borqs.qiupu.util.StringUtil;

public class RequestItemView extends SNSItemView {

    private static final String TAG = "RequestItemView";
    private ImageView icon;
    private TextView name;
    private TextView message;
    private TextView DefaultMsg;
    private TextView time;
    private TextView okbtn;
    private TextView cancelbtn;
    private LinearLayout id_vcard;
    private Requests mrequest;
    private QiupuORM orm;
//    private HashMap<String, RequestActionListner> mRequestListenerMap;
    
    private RequestActionListner mRequestListener;

    public Requests getRequestItem() {
        return mrequest;
    }

    public RequestItemView(Context context) {
        super(context);
        orm = QiupuORM.getInstance(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public RequestItemView(Context context, Requests request) {
        super(context);
        orm = QiupuORM.getInstance(context);
        mrequest = request;
        init();
    }

    public void setRequest(Requests request){
    	mrequest = request;
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
        
        int resid;
//        if (mrequest != null && mrequest.type == Requests.REQUEST_TYPE_EXCHANGE_VCARD) {
//            resid = R.layout.request_item_view;
//        }else{
            resid = R.layout.request_suggest_item_view;
//        }

        //child 1
        View convertView = factory.inflate(resid, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        addView(convertView);

        icon = (ImageView) convertView.findViewById(R.id.id_user_icon);
        name = (TextView) convertView.findViewById(R.id.id_user_name);
        message = (TextView) convertView.findViewById(R.id.id_user_message);
        DefaultMsg = (TextView) convertView.findViewById(R.id.id_default_message);
        time = (TextView) convertView.findViewById(R.id.id_message_time);
        okbtn = (TextView) convertView.findViewById(R.id.id_action_ok);
        cancelbtn = (TextView) convertView.findViewById(R.id.id_action_cancel);
        id_vcard = (LinearLayout) convertView.findViewById(R.id.id_vcard);
        
        time.setMovementMethod(LinkMovementMethod.getInstance());
        time.setLinksClickable(true);
        
        icon.setOnClickListener(userOnClick);
        name.setOnClickListener(userOnClick);
        okbtn.setOnClickListener(okbtnOnClick);
        cancelbtn.setOnClickListener(cancelbtnOnClick);
        setUI();
    }

    private void setUI() {
    	
    	if(mrequest != null)
    	{
    	    id_vcard.removeAllViews();
    	    
    		// set user icon
    	    Log.d(TAG, "mrequest.user = " + mrequest.user);
    	    icon.setImageResource(R.drawable.default_user_icon);
    	    if (mrequest.user != null) {
                ImageRun imagerun = new ImageRun(null,mrequest.user.profile_image_url, 0);
                imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
                imagerun.noimage = true;
                imagerun.addHostAndPath = true;
                imagerun.setRoundAngle=true;
                imagerun.setImageView(icon);        
                imagerun.post(null);
                
                //set user name
                name.setText(mrequest.user.nick_name);
    	    } else {
    	      //set user name
                name.setText(mContext.getString(R.string.no_such_user));
    	    }
    		
    		//set default message
    		Resources res = mContext.getResources();
    		if(mrequest.type == Requests.REQUEST_TYPE_EXCHANGE_VCARD)
    		{
//    			DefaultMsg.setText(res.getString(R.string.request_see_profile));
//    			DefaultMsg.setVisibility(View.GONE);
    			DefaultMsg.setText(res.getString(R.string.request_exchange_info));
    			okbtn.setText(R.string.request_exchange);
    			cancelbtn.setText(R.string.request_ignore);
//    			setContactInfoUi();
    			
    		}
//    		else if(mrequest.type == Requests.REQUEST_TYPE_REQUEST_FOLLOW_BACK_ME)
//    		{
//    			DefaultMsg.setText(res.getString(R.string.request_to_concern));
//    			setContactInfoUi();
//    		}
//    		else if (mrequest.type == Requests.REQUEST_TYPE_QIU_GUANZHU)
//    		{
//    			DefaultMsg.setText(res.getString(R.string.request_to_add_friends));
//    		}
    		else if (mrequest.type == Requests.REQUEST_TYPE_CHANGE_EMAIL_1)
    		{
//    			QiupuUser myinfo = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
    			DefaultMsg.setText(String.format(res.getString(R.string.request_to_change_email), getMyPhoneEmailInfo(QiupuConfig.TYPE_EMAIL1), mrequest.data));
    		}
    		else if (mrequest.type == Requests.REQUEST_TYPE_CHANGE_EMAIL_2)
    		{
//    			QiupuUser myinfo = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
    			DefaultMsg.setText(String.format(res.getString(R.string.request_to_change_email), getMyPhoneEmailInfo(QiupuConfig.TYPE_EMAIL2), mrequest.data));
    		}
    		else if (mrequest.type == Requests.REQUEST_TYPE_CHANGE_EMAIL_3)
    		{
//    			QiupuUser myinfo = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
    			DefaultMsg.setText(String.format(res.getString(R.string.request_to_change_email), getMyPhoneEmailInfo(QiupuConfig.TYPE_EMAIL3), mrequest.data));
    		}
    		else if(mrequest.type == Requests.REQUEST_TYPE_CHANGE_PHONE_1)
    		{
//    			QiupuUser myinfo = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
    			DefaultMsg.setText(String.format(res.getString(R.string.request_to_change_phone), getMyPhoneEmailInfo(QiupuConfig.TYPE_PHONE1), mrequest.data));
    		}
    		else if(mrequest.type == Requests.REQUEST_TYPE_CHANGE_PHONE_2)
    		{
//    			QiupuUser myinfo = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
    			DefaultMsg.setText(String.format(res.getString(R.string.request_to_change_phone), getMyPhoneEmailInfo(QiupuConfig.TYPE_PHONE2), mrequest.data));
    		}
    		else if(mrequest.type == Requests.REQUEST_TYPE_CHANGE_PHONE_3)
    		{
//    			QiupuUser myinfo = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
    			DefaultMsg.setText(String.format(res.getString(R.string.request_to_change_phone), getMyPhoneEmailInfo(QiupuConfig.TYPE_PHONE3), mrequest.data));
    		}else if(mrequest.type == Requests.REQUEST_EVENT_INVITE) {
    			DefaultMsg.setText(String.format(res.getString(R.string.request_event_invite), pareData(mrequest.data)));
    		}else if(mrequest.type == Requests.REQUEST_EVENT_JOIN) {
    			DefaultMsg.setText(String.format(res.getString(R.string.request_event_join), pareData(mrequest.data)));
    		}else if(mrequest.type == Requests.REQUEST_PUBLIC_CIRCLE_INVITE) {
    			DefaultMsg.setText(String.format(res.getString(R.string.request_circle_invite), pareData(mrequest.data)));
    		}else if(mrequest.type == Requests.REQUEST_PUBLIC_CIRCLE_JOIN) {
    			DefaultMsg.setText(String.format(res.getString(R.string.request_circle_join), pareData(mrequest.data)));
    		}
    		
    		// set message
    		if(StringUtil.isValidString(mrequest.message))
    		{
    			message.setVisibility(View.VISIBLE);
    			message.setText(mrequest.message);
    		}
    		
    		//set time
    		String day = com.borqs.qiupu.util.DateUtil.converToRelativeTime(mContext, mrequest.createTime);
    		time.setText(day);
    	}
    }
    
	View.OnClickListener okbtnOnClick = new View.OnClickListener()
	{
		public void onClick(View arg0)
		{
//			if (null != mRequestListenerMap) {
//                Collection<RequestActionListner> listeners = mRequestListenerMap.values();
//                Iterator it = listeners.iterator();
//                while (it.hasNext()) {
//                	RequestActionListner requestListener = (RequestActionListner)it.next();
//                	requestListener.acceptRequest(mrequest, mrequest.type);
//                }
//            }
			
			if(null != mRequestListener) {
				mRequestListener.acceptRequest(mrequest, mrequest.type);
			}
		}
	};
	
	View.OnClickListener cancelbtnOnClick = new View.OnClickListener()
	{
		
		public void onClick(View arg0)
		{
//			if (null != mRequestListenerMap) {
//                Collection<RequestActionListner> listeners = mRequestListenerMap.values();
//                Iterator it = listeners.iterator();
//                while (it.hasNext()) {
//                	RequestActionListner requestListener = (RequestActionListner)it.next();
//                	requestListener.refuseRequest(mrequest);
//                }
//            }
			
			if(null != mRequestListener) {
				mRequestListener.refuseRequest(mrequest);
			}
		}
	};
	
	View.OnClickListener userOnClick = new View.OnClickListener()
	{
		
		public void onClick(View arg0)
		{
		    if (mrequest.user != null) {
		        IntentUtil.startUserDetailAboutIntent(getContext(), mrequest.user.uid, mrequest.user.nick_name);
		    }
		}
	};
	
	private void setContactInfoUi(){
		id_vcard.setVisibility(View.VISIBLE);
		if(mrequest.user != null && mrequest.user.phoneList != null && mrequest.user.phoneList.size() > 0) {
		    for(int i=0; i<mrequest.user.phoneList.size(); i++) {
		        if (TextUtils.isEmpty(mrequest.user.phoneList.get(i).info) == false) {
    		        RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(mContext, String.format(mContext.getString(R.string.user_detail_phone_number, mrequest.user.phoneList.get(i).info)));
    		        id_vcard.addView(info);
		        }
		    }
		}
		if(mrequest.user != null && mrequest.user.emailList != null && mrequest.user.emailList.size() > 0) {
            for(int i=0; i<mrequest.user.emailList.size(); i++) {
                if (TextUtils.isEmpty(mrequest.user.emailList.get(i).info) == false) {
                    RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(mContext, String.format(mContext.getString(R.string.user_detail_email, mrequest.user.emailList.get(i).info)));
                    id_vcard.addView(info);
                }
            }
        }
	}
	
	private String getMyPhoneEmailInfo(String type) {
	    return orm.getInfoWhitType(type, AccountServiceUtils.getBorqsAccountID());
	}
	
	private String pareData(String data) {
		String parename = "";
		try {
			JSONObject obj = new JSONObject(data);
			parename = obj.optString("group_name");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return parename;
	}
//	
//	public void attachRequestListener(HashMap<String, RequestActionListner> listenerMap) {
//        mRequestListenerMap = listenerMap;
//    }

	public void setRequestListener(RequestActionListner mListener) {
		mRequestListener = mListener;
	}
}

