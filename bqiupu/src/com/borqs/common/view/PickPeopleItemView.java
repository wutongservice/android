package com.borqs.common.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.listener.FriendsContactActionListner;
import com.borqs.common.quickaction.QuickPeopleActivity;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.LookUpPeopleColumns;
import com.borqs.qiupu.db.QiupuORM.UsersColumns;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ContactUtils;

import java.util.Collection;
import java.util.HashMap;

public class PickPeopleItemView extends SNSItemView {
    private final String TAG = "PickPeopleItemView";

    private TextView username;
    private ImageView portrait;
    private TextView mCircleView;
    private final QiupuORM orm;

    private ContactSimpleInfo user;
    private HashMap<String, FriendsContactActionListner> mCheckClickListenerMap;

    protected PickPeopleItemView(Context context, ContactSimpleInfo di, QiupuORM qiupuorm) {
        super(context);
        mContext = context;
        user = di;
        orm = qiupuorm;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public String getName() {
        return user.display_name_primary;
    }

    public boolean isSelected() {
        return user.selected;
    }

    protected int getLayoutResourceId() {
        return R.layout.contact_people_picker_item;
    }

    private void init() {
        Log.d(TAG, "call SelectUserItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        View view = factory.inflate(getLayoutResourceId(), null);
        view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int)mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(view);

        portrait = (ImageView) view.findViewById(R.id.head_portrait);
        username = (TextView) view.findViewById(R.id.contact_user_name);
        mCircleView = (TextView) view.findViewById(R.id.id_friend_circle);

        portrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, QuickPeopleActivity.class);
                intent.putExtra("name", user.display_name_primary);
                intent.putExtra("contactId", user.mContactId);
                mContext.startActivity(intent);
            }
        });

        setUI();
    }

    public ContactSimpleInfo getContactSimpleInfo() {
        return user;
    }

    private void setUI() {
        username.setText(user.display_name_primary);
        //need cache
        setContactPhoto();
        setPortraitV();
        
        final TextView invitation = (TextView) findViewById(R.id.invite_tips);
        if (user.mBorqsId <= 0) {
            if (null != invitation) {
//                invitation.setText(R.string.user_circles);
//                invitation.setBackgroundResource(R.drawable.list_item_add_action_bg);
                invitation.setOnClickListener(addFriends);
            }
            mCircleView.setVisibility(View.GONE);
        } else {
//            if (null != invitation) {
//                invitation.setText(R.string.user_circles);
//                invitation.setBackgroundResource(R.drawable.list_item_invite_action_bg);
//            }
            //do at background, or it make the UI show very slow
            mCircleView.setTag(user.mBorqsId);
            post(new Runnable() {
                public void run() {
                    long bid = (Long) mCircleView.getTag();
                    if (bid == user.mBorqsId) {
                        Cursor circleCursor = orm.queryOneUserCircleInfo(user.mBorqsId);
                        String circleId = "";
                        String circleName = "";
                        try {
                            if (circleCursor.moveToFirst()) {
                                circleId = circleCursor.getString(circleCursor.getColumnIndex(UsersColumns.CIRCLE_ID));
                                circleName = circleCursor.getString(circleCursor.getColumnIndex(UsersColumns.CIRCLE_NAME));
                            }
                        } catch (Exception ne) {
                        } finally {
                            circleCursor.close();
                        }

                        setCircleView(circleId, circleName);
                        if (null != invitation) {
                            final String tmpCircleId = circleId;
                            invitation.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    gotoEditFriendsCircle(user.mBorqsId, tmpCircleId);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void setContactPhoto() {
        portrait.setImageResource(R.drawable.default_user_icon);
        portrait.setTag(user.mContactId);
        post(new Runnable() {
            public void run() {
                long contactId = (Long) portrait.getTag();
                if (contactId == user.mContactId) {
                    Bitmap contactPhoto = ContactUtils.getContactPhoto(mContext, user.mContactId);
                    if (contactPhoto != null) {
                        portrait.setImageBitmap(contactPhoto);
                    }
                }
            }
        });
    }

    public void setUserItem(ContactSimpleInfo di) {
        user = di;
        setUI();
    }

    @Override
    public String getText() {
        return user != null ? user.display_name_primary : "";
    }

    private final OnClickListener addFriends = new OnClickListener() {
        public void onClick(View v) {
//            gotoAddFriends(user.mBorqsId);
            gotoInvites();
        }
    };

    public void attachActionListener(HashMap<String, FriendsContactActionListner> listenerMap) {
        mCheckClickListenerMap = listenerMap;
    }


    private void gotoAddFriends(final long uid) {

        if (null != mCheckClickListenerMap) {
            Collection<FriendsContactActionListner> listeners = mCheckClickListenerMap.values();
            for (FriendsContactActionListner checkListener : listeners) {
                checkListener.addFriends(uid);
            }
        }
    }
    
    private void gotoInvites() {

        if (null != mCheckClickListenerMap) {
            Collection<FriendsContactActionListner> listeners = mCheckClickListenerMap.values();
            for (FriendsContactActionListner checkListener : listeners) {
                checkListener.inviteFriends(user.mContactId, user.display_name_primary);
            }
        }
    }

    private void gotoEditFriendsCircle(final long uid, String circle_name) {

        if (null != mCheckClickListenerMap) {
            Collection<FriendsContactActionListner> listeners = mCheckClickListenerMap.values();
            for (FriendsContactActionListner checkListener : listeners) {
                checkListener.editFriendsCircle(uid, circle_name);
            }
        }
    }

    private String getCircles(String circleIdStr, String circleName) {
        String[] circleIds = circleIdStr.split(",");
        StringBuilder circleIdBuilder = new StringBuilder();
        for (String circleId : circleIds) {
            long cId = Long.parseLong(circleId);
            if (cId != QiupuConfig.ADDRESS_BOOK_CIRCLE) {
                String tmpName = CircleUtils.getLocalCircleName(mContext, cId, "");
                if (tmpName != null && tmpName.length() > 0) {
                    if (circleIdBuilder.length() > 0) {
                        circleIdBuilder.append(",");
                    }
                    circleIdBuilder.append(tmpName);
                }
            }
        }

        if (!isEmpty(circleName)) {
            if (circleIdBuilder.length() > 0) {
                circleIdBuilder.append(",");
            }
            circleIdBuilder.append(circleName);
        }

        return circleIdBuilder.toString();
    }

    private void setCircleView(String circleIdStr, String circleName) {
        if (!isEmpty(circleIdStr)) {
            String circle_name = getCircles(circleIdStr, circleName);

            if (!isEmpty(circle_name)) {
                mCircleView.setVisibility(View.VISIBLE);
                mCircleView.setText(circle_name);
            } else {
                mCircleView.setVisibility(View.GONE);
            }
        } else {
            mCircleView.setVisibility(View.GONE);
        }
    }

    public boolean refreshItem(long borqsId, long contactId) {
        if (contactId > 0 && user.mContactId == contactId) {
            Cursor cursor = orm.queryLookUpTypeAndBorqsId(contactId);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user.mBorqsId = cursor.getLong(cursor.getColumnIndex(LookUpPeopleColumns.UID));
                    setUI();
                    cursor.close();
                    return true;
                }
            }
        } else {
            if (user.mBorqsId == borqsId) {
                setUI();
                return true;
            }
        }
        return false;
    }
    
    private void setPortraitV() {
        ImageView view = (ImageView) findViewById(R.id.portrait_v);
        if(view != null) {
            if(user.mBorqsId > 0) {
                view.setVisibility(View.VISIBLE);
            }else {
                view.setVisibility(View.GONE);
            }
        }
    }

    public static PickPeopleItemView newInstance(Context mContext, ContactSimpleInfo di, QiupuORM orm, boolean noAction) {
        return noAction ? new PickPeopleItemView(mContext, di, orm) :
                new ContactPeopleItemView(mContext, di, orm);
    }

    static class ContactPeopleItemView extends PickPeopleItemView {
        protected ContactPeopleItemView(Context context, ContactSimpleInfo di, QiupuORM qiupuorm) {
            super(context, di, qiupuorm);
        }

        @Override
        protected int getLayoutResourceId() {
            return R.layout.show_people_item_layout;
        }
    }
}

