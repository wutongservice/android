
package com.borqs.common.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import twitter4j.QiupuUser;
import twitter4j.Requests;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.quickaction.QuickPeopleActivity;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.StringUtil;

public class BpcShakingItemView extends SNSItemView {
    
    private static final String TAG = "Qiupu.BpcShakingItemView";

    private ImageView icon;
    private TextView  name;
    private TextView distanceView;
    private TextView exchange_card_request;
    private ImageView add_friends;

    private QiupuUser mUser;
    private QiupuORM orm;
    private Context mContext;

    private boolean from_exchange;

    public long getUserID() {
        return mUser.uid;
    }

    public QiupuUser getUser() {
        return mUser;
    }

    public BpcShakingItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public BpcShakingItemView(Context context, QiupuUser user, boolean fromExchange) {
        super(context);
        mContext = context;
        mUser = user;
        from_exchange = fromExchange;
        Log.d(TAG, "BpcShakingItemView() from_exchange = " + from_exchange);
        orm = QiupuORM.getInstance(mContext);
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

        View convertView  = factory.inflate(getLayoutResourceId(), null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(convertView);

        icon = (ImageView)convertView.findViewById(R.id.id_friend_icon);
        name = (TextView)convertView.findViewById(R.id.id_friend_name);
        distanceView = (TextView) convertView.findViewById(R.id.id_friend_circle);
        exchange_card_request = (TextView) convertView.findViewById(R.id.exchange_card_request);
        add_friends = (ImageView) convertView.findViewById(R.id.add_friends);

        icon.setOnClickListener(iconListener);
        exchange_card_request.setOnClickListener(infoRequestClickListener);

        setUI();
    }

    private View.OnClickListener iconListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            Intent intent = new Intent(mContext, QuickPeopleActivity.class);
            intent.putExtra("user", mUser);
            mContext.startActivity(intent);
        }
    };

    public void setUser(QiupuUser user) {
        mUser = user;
        setUI();
    }

    public void refreshUI() {
        setUI();
    }

    private void setUI() {
        setIcon();
        setDistance();
        setExchangeStatus();
    }

    private static final int HAVE_SEND_REQUEST = 1;
    private static final int NEED_SEND_REQUEST = 2;
    private static final int IS_MY_FRIENDS     = 3;
    private static final int IS_MY_CONTACT = 4;

    private void setExchangeStatus() {
        Log.d(TAG, "setExchangeStatus()====== from_exchange = " + from_exchange);
        if (from_exchange) {
            setExchangeUI();
        } else {
            setFindFriendsUI();
        }
    }

    private void setFindFriendsUI() {
        int type = -1;
        if (mUser.uid > 0) {
            if (mUser.circleId != null && mUser.circleId.length() > 0) {
                type = IS_MY_FRIENDS;
                exchange_card_request.setVisibility(View.GONE);
                add_friends.setVisibility(View.GONE);
            } else {
                type = IS_MY_CONTACT;
                exchange_card_request.setVisibility(View.GONE);
                add_friends.setVisibility(View.VISIBLE);
                add_friends.setOnClickListener(addFriendsListener);
            }
        } else {
            type = IS_MY_CONTACT;
            exchange_card_request.setVisibility(View.GONE);
            add_friends.setVisibility(View.VISIBLE);
            add_friends.setOnClickListener(addFriendsListener);
        }

        setName(appendRelationShip(type));
    }

    private void setExchangeUI() {
        int type = -1;
        if (mUser.uid > 0) {
            exchange_card_request.setBackgroundResource(R.drawable.exchange_icon_bg);
            exchange_card_request.setOnClickListener(infoRequestClickListener);
            if (mUser.circleId != null && mUser.circleId.length() > 0) {
                if (mUser.profile_privacy) {
                    if (mUser.pedding_requests.length() > 0 ) {
                        type = HAVE_SEND_REQUEST;
                        exchange_card_request.setText(R.string.request_exchange_card_again);
                    } else {
                        type = NEED_SEND_REQUEST;
                        exchange_card_request.setText(R.string.friends_item_request_exchange);
                    }
                } else {
                    type = IS_MY_FRIENDS;
                    exchange_card_request.setText("");
                    exchange_card_request.setBackgroundResource(R.drawable.exchange_icon_bg_disabled);
                    exchange_card_request.setOnClickListener(null);
                }
            } else {
                type = NEED_SEND_REQUEST;
                exchange_card_request.setText(R.string.friends_item_request_exchange);
            }
        }else {
            type = IS_MY_CONTACT;
            exchange_card_request.setText(R.string.friends_item_request_exchange);
            exchange_card_request.setOnClickListener(addFriendsListener);
        }
        setName(appendRelationShip(type));
    }

    private View.OnClickListener addFriendsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != mUsersActionListenerMap) {
                Collection<UsersActionListner> listeners = mUsersActionListenerMap.values();
                Iterator<UsersActionListner> it = listeners.iterator();
                while (it.hasNext()) {
                    UsersActionListner checkListener = (UsersActionListner)it.next();
                    checkListener.addFriends(mUser);
                }
            }
        }
    };

    private String appendRelationShip(int type) {
        String append = "";
        switch (type) {
            case HAVE_SEND_REQUEST:
            case NEED_SEND_REQUEST:
            case IS_MY_FRIENDS:
                append = mContext.getResources().getString(R.string.home_friends);
                break;
            case IS_MY_CONTACT:
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

    private void setName(String append) {
        if(StringUtil.isValidString(mUser.remark)) {
            name.setText(mUser.remark + append);
        }else {
            if(mUser.perhapsNames != null && mUser.perhapsNames.size() > 0) {
                name.setText(mUser.perhapsNames.get(0).name + append);
            } else {
                name.setText(mUser.nick_name + append);
            }
        }
    }

    private void setIcon() {
        icon.setImageResource(R.drawable.default_user_icon);

        ImageRun imagerun = new ImageRun(null, mUser.profile_image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle = true;
        imagerun.setImageView(icon);
        imagerun.post(null);
    }

    private void setDistance() {
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

        distanceView.setText(distanceStr);
//        distanceView.setText(String.valueOf((int)(distance/1)));
    }

    private View.OnClickListener infoRequestClickListener = new OnClickListener() {
        public void onClick(View view) {
            Log.d(TAG, "======= flag = " + (null != mUsersActionListenerMap));
            if (null != mUsersActionListenerMap) {
                Collection<UsersActionListner> listeners = mUsersActionListenerMap.values();
                Iterator<UsersActionListner> it = listeners.iterator();
                while (it.hasNext()) {
                    UsersActionListner checkListener = (UsersActionListner)it.next();
                    checkListener.sendRequest(mUser);
                }
            }
        }
    };

    public static boolean isalreadyRequestProfile(String typeid) {
        if (isEmpty(typeid) == false) {
            final String tmpstr = typeid + ",";
            String[] ids = tmpstr.split(",");
            for (int i = 0; i < ids.length; i++) {
                try {
                    if (Requests.REQUEST_TYPE_EXCHANGE_VCARD == Integer.parseInt(ids[i])) {
                        return true;
                    }
                } catch (Exception ne) {
                    ne.printStackTrace();
                }
            }
        }
        return false;
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

    public void setFromStatus(boolean fromExchange) {
        from_exchange = fromExchange;
    }

    protected int getLayoutResourceId() {
        return R.layout.bpc_shaking_item_view;
    }

    public static BpcShakingItemView newInstance(Context context, QiupuUser user, boolean fromExchange) {
        return new BpcShakingItemView(context, user, fromExchange);
    }

}
