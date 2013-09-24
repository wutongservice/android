package com.borqs.qiupu.util;

import java.util.ArrayList;

import twitter4j.UserCircle;
import android.content.Context;
import android.content.res.Resources;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class CircleUtils {

    private static final String TAG = "CircleUtils";
    public static final String CIRCLE_ID    = "CIRCLE_ID";
    public static final String CIRCLE_NAME  = "CIRCLE_NAME";
    public static final String CIRCLEINFO = "CIRCLEINFO";
    public static final String COMPANY_ID = "COMPANY_ID";
    public static final String EdIT_TYPE = "EDIT_TYPE";
    
    public static final String INTENT_PARENT_ID = "PARENT_ID";
    
    public static final long BORQS_CIRCLE_ID = 10000000072l;
	public static final String INTENT_SCENE = "intent_scene";
	public static final String INTENT_FROM_ID = "from_id";

    public static String getLocalCircleName(Context context, long circleId, String circleName)
    {
    	Resources res = context.getResources();
    	if(circleId == QiupuConfig.BLOCKED_CIRCLE){
    		return res.getString(R.string.bolcked_circle);
    	}else if(circleId == QiupuConfig.ADDRESS_BOOK_CIRCLE){
    		return res.getString(R.string.address_book_circle);
    	}else if(circleId == QiupuConfig.DEFAULT_CIRCLE){
    		return res.getString(R.string.default_circle);
    	}else if(circleId == QiupuConfig.FAMILY_CIRCLE){
    		return res.getString(R.string.family_circle);
    	}else if(circleId == QiupuConfig.CLOSE_FRIENDS_CIRCLE){
    		return res.getString(R.string.close_friends_circle);
    	}else if(circleId == QiupuConfig.ACQUAINTANCE_CIRCLE){
    		return res.getString(R.string.acquaintance_circle);
    	}else{
    		return circleName;
    	}
    }
    
    public static ArrayList<Long> getCirlceOrUserIds(String circle)
	{		
		ArrayList<Long> ids = new ArrayList<Long>();
		
		if(StringUtil.isValidString(circle))
		{
			String []cids = circle.split(",");
			for(int i=0;i<cids.length;i++)
			{
				ids.add(Long.parseLong(cids[i]));
			}
		}
		
		return ids;
	}

    public static String getDefaultCircleId() {
        return QiupuConfig.ADDRESS_BOOK_CIRCLE + ","
                + QiupuConfig.ACQUAINTANCE_CIRCLE;
    }

    public static String getDefaultCircleName(Resources res) {
        return res.getString(R.string.address_book_circle) + ","
                + res.getString(R.string.close_friends_circle);
    }
  	
  	public static void circleUpdateCallBack(String multiuids, String circleid, boolean isadd, QiupuORM orm, Context context) {
        String[] uids = multiuids.split(",");
        if (uids.length > 0) {
            for (int i = 0; i < uids.length; i++) {
                orm.updateUserInfoToDb(uids[i], circleid, isadd);
            }
        }

        //update count of circle
        UserCircle tmpcircle = orm.queryOneCircle(AccountServiceUtils.getBorqsAccountID(), Long.parseLong(circleid));
        if (tmpcircle != null) {
            if (isadd) {
                tmpcircle.memberCount = tmpcircle.memberCount + uids.length;
                orm.updateCircleInfo(tmpcircle);
            } else {
                int count = tmpcircle.memberCount - uids.length;
                tmpcircle.memberCount = count > 0 ? count : 0;
                orm.updateCircleInfo(tmpcircle);
            }
        }

        //remove user from circleUser
        ArrayList<Long> ids = getCirlceOrUserIds(multiuids);
        if (isadd == false) {
            orm.clearCircleUser(circleid, ids);
        } else {
            orm.updateCircleUsers(circleid, ids);
        }
    }

    public static String getCircleNameWithCount(Context context, long circleId, String circleName, int count)
    {
        if(QiupuConfig.CIRCLE_ID_ALL == circleId || QiupuConfig.CIRCLE_ID_HOT == circleId
                || QiupuConfig.CIRCLE_ID_NEAR_BY == circleId || QiupuConfig.CIRCLE_ID_PUBLIC == circleId) {
            return getCircleName(context, circleId, circleName);
        }else {
            String memberCount =  " (" + count + ")" ;
            return getCircleName(context, circleId, circleName) + memberCount;
        }
    }
    
    public static String getCircleName(Context context, long circleId, String circleName)
    {
    	Resources res = context.getResources();
    	if(circleId == QiupuConfig.BLOCKED_CIRCLE){
    		return res.getString(R.string.bolcked_circle);
    	}else if(circleId == QiupuConfig.ADDRESS_BOOK_CIRCLE){
    		return res.getString(R.string.address_book_circle);
    	}else if(circleId == QiupuConfig.DEFAULT_CIRCLE){
    		return res.getString(R.string.default_circle);
    	}else if(circleId == QiupuConfig.FAMILY_CIRCLE){
    		return res.getString(R.string.family_circle);
    	}else if(circleId == QiupuConfig.CLOSE_FRIENDS_CIRCLE){
    		return res.getString(R.string.close_friends_circle);
    	}else if(circleId == QiupuConfig.ACQUAINTANCE_CIRCLE){
    		return res.getString(R.string.acquaintance_circle);
    	}else if(circleId == QiupuConfig.CIRCLE_ID_ALL){
    		return res.getString(R.string.circle_id_all);
    	}else if(circleId == QiupuConfig.CIRCLE_ID_HOT){
    		return res.getString(R.string.circle_id_hot);
    	}else if(circleId == QiupuConfig.CIRCLE_ID_NEAR_BY){
    		return res.getString(R.string.circle_id_nearby);
    	}else if(circleId == QiupuConfig.CIRCLE_ID_PUBLIC){
    		return res.getString(R.string.circle_id_public);
    	}else{
    		return circleName;
    	}
    }
    
    public static String getCircleNameByCirlceId(Context context, String circleIds) {
        StringBuilder namebuilder = new StringBuilder();
        String[] ids = circleIds.split(",");
        for (int i = 0; i < ids.length; i++) {
            long circleId = Long.parseLong(ids[i]);
            if(QiupuConfig.ADDRESS_BOOK_CIRCLE == circleId || QiupuConfig.BLOCKED_CIRCLE == circleId) {
                continue;
            }
            String tmpname = getLocalCircleName(context, circleId, "");
            if (tmpname.length() > 0) {
                if (namebuilder.length() > 0) {
                    namebuilder.append(",");
                }
                namebuilder.append(tmpname);
            }
        }
        return namebuilder.toString();
    }
    
    public static final String getAllFilterCircleIds() {
    	StringBuilder ids = new StringBuilder();
    	ids.append(QiupuConfig.CIRCLE_ID_ALL).append(",").append(QiupuConfig.CIRCLE_ID_HOT).append(",")
    	   .append(QiupuConfig.CIRCLE_ID_NEAR_BY).append(",").append(QiupuConfig.CIRCLE_ID_PUBLIC).append(",")
    	   .append(QiupuConfig.ADDRESS_BOOK_CIRCLE).append(",").append(QiupuConfig.BLOCKED_CIRCLE);
    	return ids.toString();
    }
    
    public static final String getFilterCircleIdsWithOutPublic() {
    	StringBuilder ids = new StringBuilder();
    	ids.append(QiupuConfig.CIRCLE_ID_ALL).append(",").append(QiupuConfig.CIRCLE_ID_HOT).append(",")
    	   .append(QiupuConfig.CIRCLE_ID_NEAR_BY).append(",")
    	   .append(QiupuConfig.ADDRESS_BOOK_CIRCLE).append(",").append(QiupuConfig.BLOCKED_CIRCLE);
    	return ids.toString();
    } 

    public static final String getNativeCircleIds() {
        StringBuilder ids = new StringBuilder();
        ids.append(QiupuConfig.CIRCLE_ID_ALL).append(",").append(QiupuConfig.CIRCLE_ID_HOT).append(",")
           .append(QiupuConfig.CIRCLE_ID_NEAR_BY).append(",").append(QiupuConfig.CIRCLE_ID_PUBLIC);
        return ids.toString();
    }
}
