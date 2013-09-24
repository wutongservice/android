package com.borqs.common.adapter;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.Annotation;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.EmployeeColums;
import com.borqs.qiupu.db.QiupuORM;

public class RecipientsAdapter extends ResourceCursorAdapter {
    private static final String TAG = "RecipientsAdapter";
    public static final int SMART_INDEX_ID       = 0;
    public static final int SMART_INDEX_UID      = 1;
    public static final int SMART_INDEX_NICKNAME = 2;
    public static final int SMART_INDEX_EMAIL    = 3;
    
//    final static String[] PRO_USER = {
//    		QiupuORM.UsersColumns.ID,
//    		QiupuORM.UsersColumns.USERID,
//    		QiupuORM.UsersColumns.NICKNAME,
//    		QiupuORM.UsersColumns.PROFILE_IMAGE_URL,//image url
//    };
    

    private final ContentResolver mContentResolver;
    private final Context mContext;

    public RecipientsAdapter(Context context) {
        super(context,R.layout.recipient_filter_item, null);
        mContext = context;
        mContentResolver = context.getContentResolver();
    }  

    @Override
    public final CharSequence convertToString(Cursor cursor) {
    	Log.d(TAG, "entering convertToString");  	
        String name = QiupuORM.parseUserName(cursor);
        String userid = QiupuORM.parseUserId(cursor);
        if (userid.length() == 0) {
            return userid;
        }

        SpannableString out = new SpannableString(userid);
        int len = out.length();
        
        if (!TextUtils.isEmpty(name)) {
            out.setSpan(new Annotation("name", name), 0, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            out.setSpan(new Annotation("name", userid), 0, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        out.setSpan(new Annotation("number", userid), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return out;
    }

    @Override
    public final void bindView(View view, Context context, Cursor cursor) {
    	ImageView icon = (ImageView) view.findViewById(R.id.id_friend_icon);
    	String icon_url = QiupuORM.parseUserProfileImage(cursor);
    	
    	icon.setImageResource(R.drawable.default_user_icon);
    	ImageRun imagerun = new ImageRun(null,icon_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(icon);        
        imagerun.post(null);
        
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(QiupuORM.parseUserName(cursor));
//        name.setTextColor(Color.GRAY);
        
//        TextView number = (TextView) view.findViewById(R.id.number);
//        number.setText("   (" + cursor.getString(cursor.getColumnIndex(QiupuORM.UsersColumns.CONTACT_PHONE1)) + ")");
//        number.setTextColor(Color.GRAY);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        return runQueryOnBackgroundThread(constraint, null);
    }

    public Cursor runQueryOnBackgroundThread(CharSequence constraint, ArrayList<String> excludedList) {
//        return QiupuORM.querySuggestionUser(constraint, excludedList);
    	final String homeid = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
    	long sceneId = -1;
    	if(TextUtils.isEmpty(homeid) == false) {
    		try {
    			sceneId = Long.parseLong(homeid);
    		} catch (Exception e) {
    			Log.d(TAG, "homeid is null");
    		}
    	}
    	String filterIds = "";
    	if(excludedList != null && excludedList.size() > 0) {
    		filterIds = TextUtils.join(",", excludedList);
    	}
    	Log.d(TAG, "constraint.toString(): " + constraint.toString());
    	final String sortBy = EmployeeColums.REFERRED_COUNT + " DESC ," +
    			EmployeeColums.NAME_PINYIN + " ASC, " + EmployeeColums.NAME + " ASC";
        return QiupuORM.queryCircleEmployeeWithPinyinFilter(mContext, sceneId, filterIds, constraint.toString(), sortBy);
//        String query;
//        if (constraint == null) {
//            query = null;
//        } else {
//            String cons = constraint.toString().trim().replace("'", "");
//            String[] strSplit = cons.split(",");
//            final String arg = strSplit[strSplit.length - 1];
//            query = UsersColumns.NICKNAME + " LIKE '%" + arg + "%' OR " +
//                    UsersColumns.NAME_PINGYIN + " LIKE '%" + arg + "%'";
//        }
//
//        if (null == excludedList || excludedList.isEmpty()) {
//            // do nothing, and keep query value.
//        } else {
//            final String exclusion = QiupuORM.UsersColumns.USERID + " NOT IN (" + TextUtils.join(",", excludedList) + ")";
//            query = TextUtils.isEmpty(query) ? exclusion : exclusion + " AND (" + query + ")";
//
//        }
//
//        final String sortBy = UsersColumns.REFERRED_COUNT + " DESC ," +
//                UsersColumns.NAME_PINGYIN + " ASC, " + UsersColumns.NICKNAME + " ASC";
//
//        Cursor cursor = mContentResolver.query(QiupuORM.USERS_CONTENT_URI, PRO_USER, query, null, sortBy);
//        return cursor;
    }
}
