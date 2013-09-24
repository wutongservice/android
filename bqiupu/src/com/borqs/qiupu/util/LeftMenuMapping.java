package com.borqs.qiupu.util;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class LeftMenuMapping {
    public static final int TYPE_UserProfileFragmentActivity = 0;
    public static final int TYPE_BpcPostsNewActivity = 1;
    public static final int TYPE_AlbumActivity = 2;
//    public static final int TYPE_BpcInformationActivity = 3;
    public static final int TYPE_FriendsFragmentActivity = 3;
    public static final int TYPE_BpcFriendsFragmentActivity = 4;
    public static final int TYPE_EventListActivity = 5;
    public static final int TYPE_PollListActivity = 6;
    public static final int TYPE_BpcAddFriendsActivity = 7;
//    public static final int TYPE_BpcSettingsActivity = 9; // dynamic, last position

    private static final String[] classNames = {
            "com.borqs.qiupu.ui.bpc.UserProfileFragmentActivity", // 0
            "com.borqs.qiupu.ui.bpc.BpcPostsNewActivity",         // 1
            "com.borqs.qiupu.ui.bpc.AlbumActivity",               // 2
            /*"com.borqs.qiupu.ui.bpc.BpcInformationActivity",*/      // 3
            "com.borqs.qiupu.ui.bpc.FriendsFragmentActivity",  // 5
            "com.borqs.qiupu.ui.bpc.BpcFriendsFragmentActivity",  // 6
            "com.borqs.qiupu.ui.bpc.EventListActivity",           // 7
            "com.borqs.qiupu.ui.bpc.PollListActivity",            // 8
            "com.borqs.qiupu.ui.bpc.BpcFindFriendsFragmentActivity", // 9
            "com.borqs.qiupu.ui.bpc.BpcSettingsActivity"          //10
    };

    private static final List itemList = Arrays.asList(classNames);

    public static int getIndex(String name) {
        if ("com.borqs.wutong.OrganizationHomeActivity".equals(name)) return 1;

        final int index = itemList.indexOf(name);
        return index;
    }

//    public static final int TYPE_UserProfileFragmentActivity = 0;
//	public static final int TYPE_BpcPostsNewActivity = TYPE_UserProfileFragmentActivity + 1;
//	public static final int TYPE_AlbumActivity = TYPE_BpcPostsNewActivity + 1;
//	public static final int TYPE_BpcInformationActivity = TYPE_AlbumActivity + 1;
//	public static final int TYPE_CompanyListActivity = TYPE_BpcInformationActivity + 1;
//	public static final int TYPE_BpcFriendsFragmentActivity = TYPE_CompanyListActivity + 1;
//	public static final int TYPE_EventListActivity = TYPE_BpcFriendsFragmentActivity + 1;
//	public static final int TYPE_PollListActivity = TYPE_EventListActivity + 1;
//	public static final int TYPE_BpcAddFriendsActivity = TYPE_PollListActivity + 1;
//	public static final int TYPE_BpcSettingsActivity = TYPE_BpcAddFriendsActivity + 1;
//
//
//	public static int getPositionForActivity(Activity activity) {
//		int position = -1;
//		if(activity instanceof UserProfileFragmentActivity) {
//			position = TYPE_UserProfileFragmentActivity;
//		}
//		else if(activity instanceof BpcPostsNewActivity) {
//			position = TYPE_BpcPostsNewActivity;
//
//		}
//		else if(activity instanceof BpcInformationActivity /*|| activity instanceof BpcNtfCenterActivity*/) {
//			position = TYPE_BpcInformationActivity;
//		}
//		else if(activity instanceof CompanyListActivity) {
//			position = TYPE_CompanyListActivity;
//		}
//		else if(activity instanceof BpcFriendsFragmentActivity) {
//			position = TYPE_BpcFriendsFragmentActivity;
//		}
//		else if(activity instanceof BpcAddFriendsActivity ||
//                activity instanceof BpcFindFriendsFragmentActivity) {
//			position = TYPE_BpcAddFriendsActivity;
//		}
//		else if(activity instanceof AlbumActivity) {
//			position = TYPE_AlbumActivity;
//		}
//		else if(activity instanceof BpcSettingsActivity) {
//			position = TYPE_BpcSettingsActivity;
//		}
//		else if(activity instanceof EventListActivity) {
//			position = TYPE_EventListActivity;
//		} else if (activity instanceof PollListActivity) {
//		    position = TYPE_PollListActivity;
//		}
//		return position;
//	}
}
