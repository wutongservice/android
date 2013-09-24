
package com.borqs.common.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.PushingServiceAgent;
import twitter4j.QiupuSimpleUser;
import twitter4j.QiupuUser;
import twitter4j.QiupuUser.Recommendation;
import twitter4j.Requests;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.RecommendUserAdapter;
import com.borqs.common.listener.FriendsActionListner;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.quickaction.QuickPeopleActivity;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;

public class BpcFriendsItemView extends SNSItemView {
    
    private static final String TAG = "Qiupu.BpcFriendsItemView";
    private ImageView icon/*, friend_call, friend_email*/;
    private TextView  title;
    private TextView  recommendsFromUser;
    private TextView  userCircle;
//    private TextView id_friend_see_my_info;
    private ImageView delete_user;
//    private TextView friend_request;
    private TextView exchange_card_request;
    
//	private CheckBox user_select;
    private View may_add_delete_ll;
    private View user_call_email_ll;
    private QiupuUser mUser;
    private FriendsActionListner action; 
    private boolean misdelete;
    private QiupuORM orm;
    private boolean inMyPrivacyCircle;
    private ImageView add_other_user;
    private boolean mFindFriends = false;
//    private ImageView img_online;
    
//	public void setRequest(boolean ret)
//	{
//		mUser.isRequested = ret;
//	}
    public long getUserID()
    {
        return mUser.uid;
    }
    
    public QiupuUser getUser()
    {
        return mUser;
    }
    
    public void setIsDeleteModel(boolean isdelete){
        misdelete = isdelete;
    }
    
    public BpcFriendsItemView(Context context) {
        super(context);		
    }
    
    @Override
    public String getText() {		
        return null;
    }
    
    public BpcFriendsItemView(Context context, QiupuUser user, boolean isdelete, boolean find_friends) {
        super(context);
        if(FriendsActionListner.class.isInstance(mContext))
        {
            action = (FriendsActionListner) context;
        }			
        mUser = user;
        misdelete = isdelete;
        orm = QiupuORM.getInstance(mContext);
        mFindFriends = find_friends;
        init();
    }
    
    
    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    
    private void init() {
        
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View convertView  = factory.inflate(getLayoutResourceId(), null);      
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(convertView);
        
        
        icon      = (ImageView)convertView.findViewById(R.id.id_friend_icon);
        title     = (TextView)convertView.findViewById(R.id.id_friend_name);
        recommendsFromUser = (TextView) convertView.findViewById(R.id.id_friend_from_user);
        exchange_card_request = (TextView) convertView.findViewById(R.id.exchange_card_request);
        if (exchange_card_request != null) {
            exchange_card_request.setOnClickListener(infoRequestClickListener);
        }
        
        userCircle = (TextView) convertView.findViewById(R.id.id_friend_circle);
//        id_friend_see_my_info = (TextView) convertView.findViewById(R.id.id_friend_see_my_info);
        delete_user = (ImageView) convertView.findViewById(R.id.delete_user);
        may_add_delete_ll = convertView.findViewById(R.id.may_add_delete_ll);
        user_call_email_ll = convertView.findViewById(R.id.user_call_email_ll);
//        friend_request     = (TextView) convertView.findViewById(R.id.friend_request);
        add_other_user     = (ImageView) convertView.findViewById(R.id.add_other_user);
//        img_online     = (ImageView) convertView.findViewById(R.id.img_online);
        
        View may_know_add = convertView.findViewById(R.id.may_know_add);
        if (null != may_know_add) {
            may_know_add.setOnClickListener(addFriendsClickListener);
        }
        
        View may_know_delete = convertView.findViewById(R.id.may_know_delete);
        if (null != may_know_delete) {
            may_know_delete.setOnClickListener(refuseUserClickListener);
        }

        if (icon != null) {
            icon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
//                  QuickLauncher ql = new QuickLauncher();
//                  ql.popupPeopleLauncher((BasicActivity)getContext(), BpcFriendsItemView.this, mUser);
                	QiupuORM.queryEmailPhones(mContext, mUser);
                	
                    Intent intent = new Intent(mContext, QuickPeopleActivity.class);
                    intent.putExtra("user", mUser);
                    mContext.startActivity(intent);
                }
            });
        }

        if (delete_user != null) {
            delete_user.setOnClickListener(deleteFriendsClickListener);
        }

//        if (friend_request != null) {
//            friend_request.setOnClickListener(infoRequestClickListener);
//        }

        if (add_other_user != null) {
            add_other_user.setOnClickListener(addFriendsClickListener);
        }
//        user_select.setOnClickListener(stOnClick);
        setUI();
    }

    public void setUser(QiupuUser user) {
        mUser = user;		
        setUI();
    }

    public void refreshUI() {
        setUI();
    }

    private void setUI() {
        setViewVisible();

        if(mUser != null) {
            if (mFindFriends) {
                setDistance();
                setFindFriendsUserUI();
            } else {
                setUserCircle();
                setUserName("");
            }

            setUserPhoto();
            showRecommendsUi();

            setDeleteStatus();
            setAddStatus();
//            setOnlineStatus();
        } else {
            Log.d(TAG, "the user is null");
        }
    }

    private void setDistance() {
        userCircle.setCompoundDrawables(null, null, null, null);

        Float distance = Float.valueOf(mUser.distance);
        String format_near   = mContext.getString(R.string.shaking_distance_near);
        String format_remote = mContext.getString(R.string.shaking_distance_remote);

        String distanceStr = "";
        Log.d(TAG, "distance = " + distance + " " + (int)(distance/1000));

        if (distance >= 0 && distance <= 100) {
            distanceStr = String.format(format_near, 100);
        } else if (distance > 100 && distance <= 200) {
            distanceStr = String.format(format_near, 200);
        } else if (distance > 200 && distance <= 300) {
            distanceStr = String.format(format_near, 300);
        } else if (distance > 300 && distance <= 400) {
            distanceStr = String.format(format_near, 400);
        } else if (distance > 400 && distance <= 500) {
            distanceStr = String.format(format_near, 500);
        } else if (distance > 500 && distance <= 600) {
            distanceStr = String.format(format_near, 600);
        } else if (distance > 600 && distance <= 700) {
            distanceStr = String.format(format_near, 700);
        } else if (distance > 700 && distance <= 800) {
            distanceStr = String.format(format_near, 800);
        } else if (distance > 800 && distance <= 900) {
            distanceStr = String.format(format_near, 900);
        } else if (distance > 900 && distance <= 1000) {
            distanceStr = String.format(format_near, 1000);
        } else {
            distanceStr = String.format(format_remote, (int)(distance/1000));
        }

        userCircle.setText(distanceStr);
    }

    private void setAddStatus() {
        if(mUser.getRecommendingType() != QiupuUser.USER_SUGGEST_TYPE_NONE) {
            if (user_call_email_ll != null) {
                user_call_email_ll.setVisibility(View.GONE);
            }
            if (may_add_delete_ll != null) {
                may_add_delete_ll.setVisibility(View.VISIBLE);
            }
            if (add_other_user != null) { 
                add_other_user.setVisibility(View.GONE);
            }
        } else {
            if (user_call_email_ll != null) {
                user_call_email_ll.setVisibility(View.VISIBLE);
            }
            if (may_add_delete_ll != null) {
                may_add_delete_ll.setVisibility(View.GONE);
            }
        }
        
        if (mUser.uid == AccountServiceUtils.getBorqsAccountID()) {
            if (user_call_email_ll != null) {
                user_call_email_ll.setVisibility(View.GONE);
            }
            if (add_other_user != null) {
                add_other_user.setVisibility(View.GONE);
            }
        }
    }
    
//    private void setOnlineStatus() {
//    	if(img_online != null) {
//    		img_online.setVisibility(View.VISIBLE);
//    		boolean isOnline = PushingServiceAgent.queryOnlineUser(mContext, String.valueOf(mUser.uid));
//    		if(isOnline) {
//                img_online.setImageResource(R.drawable.icon_green);
//    		}else {
//    			img_online.setImageResource(R.drawable.icon_grey);
//    		}
//    	}
//    }

    private void setDeleteStatus() {
        if (delete_user != null) {
            delete_user.setVisibility(misdelete ? View.VISIBLE : View.GONE);
        }
    }

    private void setViewVisible() {
        if (icon != null) {
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.default_user_icon);
        }
        if (title != null) {
            title.setVisibility(View.VISIBLE);
        }
    }

    private static final int IS_MY_FRIENDS     = 1;
    private static final int IS_NOT_MY_FRIENDS = 2;

    private void setFindFriendsUserUI() {
        int type = -1;
        if(mUser.circleId != null && mUser.circleId.length() > 0) {
            type = IS_MY_FRIENDS;
            if (add_other_user != null) {
                add_other_user.setVisibility(View.GONE);
            }
        } else {
            type = IS_NOT_MY_FRIENDS;
            if (add_other_user != null) {
                add_other_user.setVisibility(View.VISIBLE);
            }
        }

        setUserName(appendRelationShip(type));
    }

    private String appendRelationShip(int type) {
        String append = "";
        switch (type) {
            case IS_MY_FRIENDS:
                append = mContext.getResources().getString(R.string.home_friends);
                break;
            case IS_NOT_MY_FRIENDS:
                break;
            default:
                Log.d(TAG, "setExchangeStatus() default case, no action");
                break;
        }

        if (TextUtils.isEmpty(append) == false) {
            append = "(" + append + ")";
        }
        return append;
    }

    private void setUserCircle() {
        if(mUser.circleId != null && mUser.circleId.length() > 0) {
            inMyPrivacyCircle = false;//first set false to avoid the error show from refresh item when scroll the list
            String[] circleIds = mUser.circleId.split(",");
            StringBuilder circleidBuilder = new StringBuilder();
            for(int i=0; i<circleIds.length; i++) {
                long circleid = Long.parseLong(circleIds[i]);
                if(circleid == QiupuConfig.ADDRESS_BOOK_CIRCLE) {
                    inMyPrivacyCircle = true;
                } else {
                    String tmpname = CircleUtils.getLocalCircleName(mContext, circleid, "");
                    if(tmpname != null && tmpname.length() > 0) {
                        if(circleidBuilder.length() > 0) {
                            circleidBuilder.append(",");
                        }
                        circleidBuilder.append(tmpname);
                    }
                }
            }

            if(mUser.circleName != null && mUser.circleName.length() > 0) {
                if(circleidBuilder.length() > 0) {
                    circleidBuilder.append(",");
                }
                circleidBuilder.append(mUser.circleName);
            }

            if(circleidBuilder.length() > 0) {
                if (userCircle != null) {
                    userCircle.setVisibility(View.VISIBLE);
                    userCircle.setText(circleidBuilder.toString());
                }
            } else {
                if (userCircle != null) {
                    userCircle.setVisibility(View.GONE);
                }
            }
            
            if (add_other_user != null) {
                add_other_user.setVisibility(View.GONE);
            }
        } else {
            if (userCircle != null) {
                userCircle.setVisibility(View.GONE);
            }

            if (add_other_user != null) {
                add_other_user.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setUserPhoto() {
        ImageRun imagerun = new ImageRun(null,mUser.profile_image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(icon);
        imagerun.post(null);
    }

    private void setUserName(String append) {
        if (title != null) {
            if(StringUtil.isValidString(mUser.remark)) {
                title.setText(mUser.remark + append);
            }else {
                if(mUser.perhapsNames != null && mUser.perhapsNames.size() > 0) {
                    title.setText(mUser.perhapsNames.get(0).name + append);
                }else {
                    title.setText(mUser.nick_name + append);
                }
            }
        }
    }

    View.OnClickListener addFriendsClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            if(UsersActionListner.class.isInstance(mContext)) {
                UsersActionListner ll = (UsersActionListner) getContext();
                ll.addFriends(mUser);
            }

            if (null != mUsersActionListenerMap) {
            	Log.d(TAG, "mUsersActionListenerMap size : " + mUsersActionListenerMap.size());
                Collection<UsersActionListner> listeners = mUsersActionListenerMap.values();
                Iterator<UsersActionListner> it = listeners.iterator();
                while (it.hasNext()) {
                	UsersActionListner checkListener = (UsersActionListner)it.next();
                    checkListener.addFriends(mUser);
                }
            }
        }
    };
    
    View.OnClickListener infoRequestClickListener = new OnClickListener()
    {
        public void onClick(View arg0)
        {
            if(UsersActionListner.class.isInstance(mContext))
            {
                Log.d(TAG, "request user info");
                if(isNotNeedPerfectMyInfo())
                {
                    UsersActionListner ll = (UsersActionListner) getContext();
                    ll.sendRequest(mUser);
                }
                else
                {
                    Log.d(TAG, "your need to perfect your profile");
                    //TODO
//					Intent intent = new Intent(mContext, SetProfileActivity.class);
//					mContext.startActivity(intent);
                }
            }
            
            if (null != mUsersActionListenerMap) {
            	Log.d(TAG, "mUsersActionListenerMap size : " + mUsersActionListenerMap.size());
                Collection<UsersActionListner> listeners = mUsersActionListenerMap.values();
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                	UsersActionListner checkListener = (UsersActionListner)it.next();
                    checkListener.sendRequest(mUser);
                }
            }
        }
    };
    
    View.OnClickListener deleteFriendsClickListener = new OnClickListener()
    {
        public void onClick(View arg0)
        {
            if(UsersActionListner.class.isInstance(mContext))
            {
                UsersActionListner ll = (UsersActionListner) getContext();
                ll.deleteUser(mUser);
            }
            if (null != mUsersActionListenerMap) {
                Log.d(TAG, "mUsersActionListenerMap size : " + mUsersActionListenerMap.size());
                Collection<UsersActionListner> listeners = mUsersActionListenerMap.values();
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    UsersActionListner checkListener = (UsersActionListner)it.next();
                    checkListener.deleteUser(mUser);
                }
            }
        }
    };
    
    OnClickListener refuseUserClickListener = new OnClickListener()
    {
        public void onClick(View arg0)
        {
            if(UsersActionListner.class.isInstance(mContext))
            {
                UsersActionListner ll = (UsersActionListner) getContext();
                ll.refuseUser(mUser.uid);
            }
            if (null != mUsersActionListenerMap) {
                Log.d(TAG, "mUsersActionListenerMap size : " + mUsersActionListenerMap.size());
                Collection<UsersActionListner> listeners = mUsersActionListenerMap.values();
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    UsersActionListner checkListener = (UsersActionListner)it.next();
                    checkListener.refuseUser(mUser.uid);
                }
            }
        }
    };
    
    public static boolean isalreadyRequestProfile(String typeid)
    {
        if(isEmpty(typeid) == false)
        {	
            final String tmpstr = typeid + ",";
            String[] ids = tmpstr.split(",");
            for(int i=0; i<ids.length; i++)
            {
                try{
                    if(Requests.REQUEST_TYPE_EXCHANGE_VCARD == Integer.parseInt(ids[i]))
                    {
                        return true;
                    }
                }catch(Exception ne){
                    ne.printStackTrace();
                }
            }			
        }
        return false;
    }
    
    private boolean isNotNeedPerfectMyInfo()
    {
        QiupuUser myinfo = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
        if(myinfo != null)
        {
//            return StringUtil.isValidString(myinfo.contact_email1)
//                    || StringUtil.isValidString(myinfo.contact_email2)
//                    || StringUtil.isValidString(myinfo.contact_email3)
//                    || StringUtil.isValidString(myinfo.contact_phone1)
//                    || StringUtil.isValidString(myinfo.contact_phone2)
//                    || StringUtil.isValidString(myinfo.contact_phone3);
        }
        return false;
    }

    private AlertDialog dialog;

    private void showCommonFriendsDialog() {
        ListView listView = (ListView) LayoutInflater.from(mContext).inflate(R.layout.default_listview, null);
        ArrayList<QiupuSimpleUser> userslist = new ArrayList<QiupuSimpleUser>();
        if (Recommendation.class.isInstance(mUser)) {
            Recommendation reUser = (Recommendation) mUser;
            if (reUser != null) {
                if (reUser.recommendUser != null) {
                    if (reUser.recommendUser.friends != null) {
                        userslist.addAll(reUser.recommendUser.friends);
                    } else {
                        Log.d(TAG, "reUser.recommendUser.friends is null");
                        return;
                    }
                } else {
                    Log.d(TAG, "reUser.recommendUser is null");
                    return;
                }
            }
        }
        RecommendUserAdapter reAdapter = new RecommendUserAdapter(mContext, userslist);
        listView.setAdapter(reAdapter);
        listView.setBackgroundResource(R.color.white);
        listView.setSelector(R.drawable.list_selector_background);
        listView.setOnItemClickListener(itemClickListener);
        String title = "";
        if (recommendType == QiupuUser.USER_SUGGEST_TYPE_RECOMMENDER) {
            title = mContext.getString(R.string.recommend_from_title);
        } else if (recommendType == QiupuUser.USER_SUGGEST_TYPE_BOTH_KNOW) {
            title = mContext.getString(R.string.user_suggest_both_know);
        } else if (recommendType == QiupuUser.USER_SUGGEST_TYPE_FROM_BOTH_ADDRESSBOOK) {
            title = mContext.getString(R.string.user_suggest_from_both_addressbook);
        } else if (recommendType == QiupuUser.USER_SUGGEST_TYPE_FROM_MY_ADDRESSBOOK) {
            title = mContext.getString(R.string.user_suggest_from_my_addressbook);
        } else if (recommendType == QiupuUser.USER_SUGGEST_FROM_WORK_INFO) {
            title = mContext.getString(R.string.user_suggest_from_work_info);
        } else if (recommendType == QiupuUser.USER_SUGGEST_FROM_EDUCATION_INFO) {
            title = mContext.getString(R.string.user_suggest_from_education_info);
        } else {
            Log.d(TAG, "showCommonFriendsDialog() unsupported type");
        }
        dialog = DialogUtils.ShowDialogwithView(mContext, title, 0, listView, null, null);
    }

    private View.OnClickListener recommendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (existCommonFriends()) {
                showCommonFriendsDialog();
            } else {
                Log.d(TAG, "unsupported function");
            }
        }
    };

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (RecommendItemView.class.isInstance(view)) {
                RecommendItemView itemView = (RecommendItemView) view;
                IntentUtil.startUserDetailIntent(getContext(), itemView.getUser());
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            } else {
                Log.d(TAG, "itemClickListener, view is not RecommendItemView");
            }
        }
    };

    private boolean existCommonFriends() {
        if (recommendType == QiupuUser.USER_SUGGEST_TYPE_RECOMMENDER 
                || recommendType == QiupuUser.USER_SUGGEST_TYPE_BOTH_KNOW
                || recommendType == QiupuUser.USER_SUGGEST_TYPE_FROM_BOTH_ADDRESSBOOK
                /*|| recommendType == QiupuUser.USER_SUGGEST_TYPE_FROM_MY_ADDRESSBOOK
                || recommendType == QiupuUser.USER_SUGGEST_FROM_WORK_INFO
                || recommendType == QiupuUser.USER_SUGGEST_FROM_EDUCATION_INFO*/) {
            return true;
        } else {
            return false;
        }
    }

    private int recommendType = -1;
    private void showRecommendsUi() {
        if (recommendsFromUser != null) {
            recommendsFromUser.setVisibility(View.VISIBLE);
            recommendType = mUser.getRecommendingType();

            if (existCommonFriends()) {
                recommendsFromUser.setOnClickListener(recommendListener);
            } else {
                recommendsFromUser.setOnClickListener(null);
            }

            if(recommendType == QiupuUser.USER_SUGGEST_TYPE_RECOMMENDER) {
                final QiupuUser.RecommendUser recommendUser;
                if (mUser != null && mUser instanceof QiupuUser.Recommendation) {
                    recommendUser = ((QiupuUser.Recommendation)mUser).getRecommendUser();
                } else {
                    recommendUser = null;
                }

                if(recommendUser != null && !recommendUser.friends.isEmpty()) {
                    ArrayList<QiupuSimpleUser> recommendUsers = recommendUser.friends;
                    StringBuilder recommenderName = new StringBuilder();
                    for(int i=0;i<recommendUsers.size();i++) {
                        if(recommenderName.length() > 0) {
                            recommenderName.append(",");
                        }
                        recommenderName.append(recommendUsers.get(i).nick_name);
                    }
                    recommendsFromUser.setText(String.format(getContext().getString(R.string.recommend_from_user), recommenderName.toString()));
                }
                else{
                    Log.d(TAG, "have no recommender. ");
                    recommendsFromUser.setVisibility(View.GONE);
                }
            }
            else if(recommendType == QiupuUser.USER_SUGGEST_TYPE_BOTH_KNOW) {
                recommendsFromUser.setText(R.string.user_suggest_both_know);
            }
            else if(recommendType == QiupuUser.USER_SUGGEST_TYPE_FROM_MY_ADDRESSBOOK)
            {
                recommendsFromUser.setText(R.string.user_suggest_from_my_addressbook);
            }
            else if(recommendType == QiupuUser.USER_SUGGEST_TYPE_FROM_BOTH_ADDRESSBOOK) {
                recommendsFromUser.setText(R.string.user_suggest_from_both_addressbook);
            }
            else if(recommendType == QiupuUser.USER_SUGGEST_FROM_WORK_INFO) {
                recommendsFromUser.setText(R.string.user_suggest_from_work_info);
            }
            else if(recommendType == QiupuUser.USER_SUGGEST_FROM_EDUCATION_INFO) {
                recommendsFromUser.setText(R.string.user_suggest_from_education_info);
            }
            else if(recommendType == QiupuUser.USER_SUGGEST_FROM_SERVER) {
                recommendsFromUser.setText(R.string.user_suggest_from_server);
            }
            else if(recommendType == QiupuUser.USER_SUGGEST_TYPE_HAVE_MY_ADDRESSBOOK) {
                recommendsFromUser.setText(R.string.user_suggest_have_my_contact);
            }
            else if(recommendType == QiupuUser.USER_SUGGEST_TYPE_ATTENTION) {
                recommendsFromUser.setText(R.string.user_suggest_attention);
            }
            else {
//                Log.d(TAG, "have no this suggest_type: " + recommendType);
                recommendsFromUser.setVisibility(View.GONE);
            }
        }
    }
    
    public boolean refreshItem(long borqsid) {
        if (mUser.uid == borqsid) {
        	mUser = orm.queryOneUserInfo(borqsid);
            setUI();
            return true;
        }
        return false;
    }
    
    private HashMap<String, UsersActionListner> mUsersActionListenerMap;
    
    public void attachActionListener(HashMap<String, UsersActionListner> listenerMap) {
        mUsersActionListenerMap = listenerMap;
    }

    protected int getLayoutResourceId() {
        return R.layout.bpc_friends_item_view;
    }

    public static BpcFriendsItemView newInstance(Context context, QiupuUser user, boolean isdelete, boolean isVCard, boolean find_friends) {
        return isVCard ? new SendCardListItemView(context, user, isdelete)
                : new BpcFriendsItemView(context, user, isdelete, find_friends);
    }

    static class SendCardListItemView extends BpcFriendsItemView {
        protected SendCardListItemView(Context context, QiupuUser user, boolean isdelete) {
            super(context, user, isdelete, false);
        }

        @Override
        protected int getLayoutResourceId() {
            return R.layout.request_card_item_view;
        }
    }
}
