/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import twitter4j.QiupuSimpleUser;
import twitter4j.QiupuUser;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.util.AlphaPost;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.CircleItemView;
import com.borqs.common.view.UserSelectItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.EmployeeColums;
import com.borqs.qiupu.db.QiupuORM;

public class PickCircleUserAdapter extends BaseAdapter {
    private static final String TAG = "Qiupu.PickCircleUserAdapter";	
    private ArrayList<QiupuUser> mUserList = new ArrayList<QiupuUser>();
    private boolean isUsedArrayList;
    private Cursor       mUserCursor;
    private Cursor       mCircleCursor;
    private Cursor       mFrequestUsers;
    private Cursor       mFrequestCircles;
    private Cursor       mEventCursor;
    private Cursor       mGroupCursor;
    
    private Cursor       mCompanyCursor;
    
    private Context mContext;
    private HashSet<Long> mSelectedUser = new HashSet<Long>();
    private HashSet<Long> mSelectedCircle = new HashSet<Long>();
    private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap = new HashMap<String, CheckBoxClickActionListener>();

    public void registerCheckClickActionListener(String key, CheckBoxClickActionListener rl) {
        mCheckClickListenerMap.put(key, rl);
    }

    public void unRegisterCheckClickActionListener(String key) {
        mCheckClickListenerMap.remove(key);
    }

    private MoreItemCheckListener mCheckerListener;

    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }

    public PickCircleUserAdapter(Context context, Cursor users, Cursor circles, AtoZ atoz) {
        super();

        mContext = context; 
        mUserCursor = users;
        mCircleCursor = circles;

        setATOZitem(atoz);
    }
    
    public PickCircleUserAdapter(Context context, Cursor users, Cursor circles, AtoZ atoz, MoreItemCheckListener listener) {
        super();

        mCheckerListener = listener;
        mContext = context;
        mUserCursor = users;
        mCircleCursor = circles;

        setATOZitem(atoz);
    }
    
    public PickCircleUserAdapter(Context context, Cursor users, Cursor circles,Cursor events, Cursor groups, Cursor requentCircle, Cursor requestUser, AtoZ atoz, MoreItemCheckListener listener, Cursor companyCursor) {
        super();

        mCheckerListener = listener;
        mContext = context;
        mUserCursor = users;
        mCircleCursor = circles;
        mFrequestCircles = requentCircle;
        mFrequestUsers = requestUser;
        mEventCursor = events;
        mGroupCursor = groups;
        mCompanyCursor = companyCursor;

        setATOZitem(atoz);
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
//        Integer newposition = posMap.get(new Long(position));
//		if(debugsort)
//		Log.d(TAG, "position="+position + " map to="+newposition);
		Object iteminfo = getItem(position);
		if(iteminfo != null){
		    if(UserCircle.class.isInstance(iteminfo)){
		        UserCircle circle = (UserCircle) iteminfo;
		        if(circle != null)
		        {
		            CircleItemView c = null;
		            if (convertView == null || false == (convertView instanceof CircleItemView)) 
		            {
		                holder = new ViewHolder();
		                c = new CircleItemView(mContext, circle);
		                c.attachCheckListener(mCheckClickListenerMap);
		                holder.circleView = c;
		                c.setTag(holder);
		            } 
		            else 
		            {
		                holder = (ViewHolder)convertView.getTag();
		                c = holder.circleView;
		                c.attachCheckListener(mCheckClickListenerMap);
		                c.setCircle(circle);                                  
		            }  
		            return c;
		        }
		    }
		    else if(QiupuSimpleUser.class.isInstance(iteminfo)){
		        QiupuSimpleUser di = (QiupuSimpleUser)iteminfo;
	            if(di != null)
	            {
	                UserSelectItemView v = null;
	                if (convertView == null || false == (convertView instanceof UserSelectItemView)) 
	                {
	                    holder = new ViewHolder();
	                    v = new UserSelectItemView(mContext, di);
	                    v.attachCheckListener(mCheckClickListenerMap);
	                    holder.view = v;
	                    v.setTag(holder);
	                } 
	                else 
	                {
	                    holder = (ViewHolder)convertView.getTag();
	                    v = holder.view;
	                    v.attachCheckListener(mCheckClickListenerMap);
	                    v.setUserItem(di);                                
	                }        
	                return v;
	            }
		    }
		}else{
		    if(isUsedArrayList) {
		        return generateMoreItem();
		        
		    }else {
		        Integer newposition = posMap.get(new Long(position));
		        
		        String dockStr = "";
		        if(newposition != null && newposition < 0)
		        {
		            final int pos = Math.abs(newposition);
		            if(pos == 10000) {
		            	dockStr = mContext.getString(R.string.frequently_contacted_label);
		            }else if(pos == 20000) {
		            	dockStr = mContext.getString(R.string.user_circles);
		            }else if(pos == 30000) {
		            	dockStr = mContext.getString(R.string.event);
		            }else if(pos == 40000) {
		            	dockStr = mContext.getString(R.string.home_friends);
		            }else if(pos == 50000) {
		            	dockStr = namePinYin.get(0);
		            }else if(pos == 60000) {
		            	dockStr = mContext.getString(R.string.organization_circle_label);
		            }else {
		                dockStr = namePinYin.get(pos - getCircleCount() - getFrequentCircleCount() - getFrequestUserCount() - getEventsCount() - getGroupsCount() - getCompanyCount());
		                if(debugsort)
		                    Log.d(TAG, "pos" + pos + " " + dockStr);
		            }
		            
		        }
		        TextView bt =  (TextView)generateATOZItem();
		        bt.setText(dockStr);
		        return bt;
		    }
		}
		return null;
    }

    private View generateMoreItem() {
        if (null != mCheckerListener) {
            Button but = generateMoreItemView();
            but.setOnClickListener(mCheckerListener.getMoreItemClickListener());
            but.setText(mCheckerListener.getMoreItemCaptionId());
            return but;
        }
        return null;
    }
    
    private Button generateMoreItemView() {
        Button but = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
        but.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
        but.setBackgroundResource(R.drawable.list_selector_background);
        return but;
    }
    
    public int getCount() {
        if(isUsedArrayList) {
            int count = mUserList.size();
            if (null != mCheckerListener && mCheckerListener.isMoreItemHidden()) {
                ++count;
            }
            return count;
        }else {
            int count = alphaPos.size();
            if(mUserCursor != null && mUserCursor.getCount() > 0)
                count = count + mUserCursor.getCount();
            if(mCircleCursor != null && mCircleCursor.getCount() > 0)
                count = count + mCircleCursor.getCount() + 1;
            if(mFrequestCircles != null && mFrequestCircles.getCount() > 0) {
            	count = count + mFrequestCircles.getCount() + 1;
            	if(mFrequestUsers != null && mFrequestUsers.getCount() > 0) {
            		count = count + mFrequestUsers.getCount();
            	}
            }else {
            	if(mFrequestUsers != null && mFrequestUsers.getCount() > 0) {
            		count = count + mFrequestUsers.getCount() + 1;
            	}
            }
            if(mEventCursor != null && mEventCursor.getCount() > 0) {
            	count = count + mEventCursor.getCount() + 1;
            }
            if(mGroupCursor != null && mGroupCursor.getCount() > 0) {
            	count = count + mGroupCursor.getCount() + 1;
            }
            if(mCompanyCursor != null && mCompanyCursor.getCount() > 0) {
            	count = count + mCompanyCursor.getCount() + 1;
            }
            return count;		
        }
    }

    public Object getItem(int position) {
        if(isUsedArrayList) {
            if(position < mUserList.size()) {
                QiupuUser tmpuser = mUserList.get(position);
                postSetupSimpleUser(tmpuser);
                return tmpuser;
            }else {
                return null;
            }
        }else {
            Integer newposition = posMap.get(new Long(position));
            if(debugsort)
                Log.d(TAG, "get Item position="+position + " map to="+ ( + newposition));
            if(newposition != null && newposition >= 0){
            	if(mCompanyCursor != null && mCompanyCursor.moveToPosition(newposition)) {
            		UserCircle circle = QiupuORM.parseSimpleCircleInfo(mCompanyCursor);
            		postSetupCircle(circle);
                    return circle;
            	} else if(mFrequestCircles != null && mFrequestCircles.moveToPosition(newposition - getCompanyCount())) {
            		UserCircle circle = QiupuORM.parseSimpleCircleInfo(mFrequestCircles);
            		postSetupCircle(circle);
                    return circle;
            	}else if(mFrequestUsers != null && mFrequestUsers.moveToPosition(newposition - getFrequentCircleCount()- getCompanyCount())) {
            		QiupuSimpleUser simpleUser = QiupuORM.parserSimpleUserCursor(mFrequestUsers);
                    postSetupSimpleUser(simpleUser);
                    return simpleUser;
            	} else if(mCircleCursor != null && mCircleCursor.moveToPosition(newposition - getFrequentCircleCount() - getFrequestUserCount()- getCompanyCount())){
                    UserCircle circle = QiupuORM.parseSimpleCircleInfo(mCircleCursor);
                    postSetupCircle(circle);
                    return circle;
                } else if(mEventCursor != null && mEventCursor.moveToPosition(newposition - getFrequentCircleCount() - getFrequestUserCount() - getCircleCount()- getCompanyCount())) {
                	UserCircle circle = QiupuORM.parseSimpleEventInfo(mEventCursor);
                    postSetupCircle(circle);
                    return circle;
                } else if(mGroupCursor != null && mGroupCursor.moveToPosition(newposition - getFrequentCircleCount() - getFrequestUserCount() - getCircleCount() - getEventsCount()- getCompanyCount())) {
                	UserCircle circle = QiupuORM.createCircleInformation(mGroupCursor);
                    postSetupCircle(circle);
                    return circle;
                } else if(mUserCursor != null && mUserCursor.moveToPosition(newposition - getCircleCount() - getFrequentCircleCount() - getFrequestUserCount() - getEventsCount() - getGroupsCount()- getCompanyCount())){
                    QiupuSimpleUser simpleUser = QiupuORM.parserSimpleUserCursor(mUserCursor);
                    postSetupSimpleUser(simpleUser);
                    return simpleUser;
                }else{
                	Log.d(TAG, "getitem: null");
                    return null;
                }
            }else{
                return null;
            }
        }
    }

    public long getItemId(int position) {
    	Integer newposition = posMap.get(new Long(position));		
		return newposition ==null ? -1 : newposition;
//        return position;
    }
    
    static class ViewHolder
	{
		public UserSelectItemView view;
		public CircleItemView circleView;
	}

	 private void postSetupSimpleUser(QiupuSimpleUser info) {
		 if (null != info) {
			 
			 if (mSelectedUser.contains(info.uid)) {
				 info.selected = true;
			 }
		 }
	 }
	 
	 private void postSetupCircle(UserCircle info) {
		 if (null != info) {
			 
			 if (mSelectedCircle.contains(info.circleid)) {
				 info.selected = true;
			 }
		 }
	 }
	 
	 private ArrayList<AlphaPost> alphaPos   = new ArrayList<AlphaPost>();
	 private ArrayList<String>    namePinYin = new ArrayList<String>();	
	 
	 private HashMap<Long, Integer> posMap = new HashMap<Long, Integer>();
	 private final static boolean debugsort = false;
	 public void alterDataList(Cursor userCursor, Cursor cirlceCursor, HashSet<Long> set, HashSet<Long> circleset, AtoZ atoz) {
	     isUsedArrayList = false;
		 mSelectedUser.clear();
		 mSelectedUser.addAll(set);
		 
		 mSelectedCircle.clear();
		 mSelectedCircle.addAll(circleset);
		 
		 if(mUserCursor != userCursor)
			 mUserCursor = userCursor;
		 if(mCircleCursor != cirlceCursor)
			 mCircleCursor = cirlceCursor;
		 
		 setATOZitem(atoz);
		 
		 notifyDataSetChanged();
	 }
	 
	 public void alterDataList(Cursor userCursor, Cursor cirlceCursor, Cursor eventsCursor, Cursor groupsCursor, Cursor frequestCircles, Cursor frequestUsers, HashSet<Long> set, HashSet<Long> circleset, AtoZ atoz, Cursor companyCursor) {
	     isUsedArrayList = false;
		 mSelectedUser.clear();
		 mSelectedUser.addAll(set);
		 
		 mSelectedCircle.clear();
		 mSelectedCircle.addAll(circleset);
		 
		 if(mUserCursor != userCursor)
			 mUserCursor = userCursor;
		 if(mCircleCursor != cirlceCursor)
			 mCircleCursor = cirlceCursor;
		 
		 if(mFrequestUsers != frequestUsers) {
			 mFrequestUsers = frequestUsers;
		 }
		 if(mFrequestCircles != frequestCircles) {
			 mFrequestCircles = frequestCircles;
		 }
		 if(mEventCursor != eventsCursor) {
			 mEventCursor = eventsCursor;
		 }
		 if(mGroupCursor != groupsCursor) { 
			 mGroupCursor = groupsCursor;
		 }
		 
		 if(mCompanyCursor != companyCursor) {
			 mCompanyCursor = companyCursor;
		 }
		 
		 setATOZitem(atoz);
		 
		 notifyDataSetChanged();
	 }
	 
	 public void alterDataArrayList(ArrayList<QiupuUser> userList) {
	     isUsedArrayList = true;
	     mUserList.clear();
	     mUserList.addAll(userList);
	     notifyDataSetChanged();
	 }
	
	 public void setSelectUser(HashSet<Long> set, HashSet<Long> circleset) {

		 mSelectedUser.clear();
		 mSelectedUser.addAll(set);
		 
		 mSelectedCircle.clear();
		 mSelectedCircle.addAll(circleset);
		 
		 if(mUserCursor != null)
		 {
			 mUserCursor.moveToPosition(-1);
		 }
		 if(mCircleCursor != null)
		 {
			 mCircleCursor.moveToPosition(-1);
		 }
		 
		 notifyDataSetChanged();
	 }
	 
	 private void setATOZitem(AtoZ atoz)
	 {
		 if(alphaPos != null)
			 alphaPos.clear();
		 
		 posMap.clear();
		 
		 if(mCompanyCursor != null) {
				 final int companyPos = mCompanyCursor.getCount();
				 if(companyPos > 0) {
					 posMap.put(new Long(posMap.size()), -60000);
					 for(int i=0; i<companyPos; i++)
					 {
					     if(debugsort)
					     Log.d(TAG, "companyoriginal="+posMap.size() +" map to="+(companyPos == 0 ? -60000 : (-1*(i))));
					     posMap.put(new Long(posMap.size()), i);
					 }
				 }
		 }
		 if(mFrequestCircles != null) {
			 final int frequestCirclePos = mFrequestCircles.getCount();
			 if(frequestCirclePos > 0) {
				 posMap.put(new Long(posMap.size()), -10000);
				 for(int i=0; i<frequestCirclePos; i++)
				 {
				     if(debugsort)
				     Log.d(TAG, "frequest circle original="+posMap.size() +" map to="+(frequestCirclePos == 0 ? -10000 : (-1*(i))));
				     posMap.put(new Long(posMap.size()), i + getCompanyCount());
				 }
			 }
		 }
		 
		 if(mFrequestUsers != null) {
			 final int frequestUserPos = mFrequestUsers.getCount();
			 if(frequestUserPos > 0) {
				 if(mFrequestCircles != null && mFrequestCircles.getCount() > 0) {
					 Log.d(TAG, "no need add span title.");
				 }else {
					 posMap.put(new Long(posMap.size()), -10000);
				 }
				 
				 for(int i=0; i<frequestUserPos; i++)
				 {
				     if(debugsort)
				     Log.d(TAG, "frequest user original="+posMap.size() +" map to="+(frequestUserPos == 0 ? -10000 : (-1*(i))));
				     posMap.put(new Long(posMap.size()), i + getFrequentCircleCount() + getCompanyCount() );
				 }
			 }
		 }
		 
		 if(mCircleCursor != null)
		 {
			 final int circlePos = mCircleCursor.getCount();
			 if(circlePos > 0)
			 {
				 posMap.put(new Long(posMap.size()), -20000);
				 for(int i=0; i<circlePos; i++)
				 {
				     if(debugsort)
				     Log.d(TAG, "circle original="+posMap.size() +" map to="+(circlePos == 0 ? -20000 : (-1*(i))));
				     posMap.put(new Long(posMap.size()), i + getFrequestUserCount() + getFrequentCircleCount() + getCompanyCount());
				 }
			 }
		 }
		 
		 if(mEventCursor != null) {
			 final int eventPos = mEventCursor.getCount();
			 if(eventPos > 0)
			 {
				 posMap.put(new Long(posMap.size()), -30000);
				 for(int i=0; i<eventPos; i++)
				 {
				     if(debugsort)
				     Log.d(TAG, "event original="+posMap.size() +" map to="+(eventPos == 0 ? -30000 : (-1*(i))));
				     posMap.put(new Long(posMap.size()), i + getFrequestUserCount() + getFrequentCircleCount() + getCircleCount() + getCompanyCount());
				 }
			 }
		 }
		 
		 if(mGroupCursor != null) {
			 final int groupPos = mGroupCursor.getCount();
			 if(groupPos > 0)
			 {
				 posMap.put(new Long(posMap.size()), -40000);
				 for(int i=0; i<groupPos; i++)
				 {
				     if(debugsort)
				     Log.d(TAG, "event original="+posMap.size() +" map to="+(groupPos == 0 ? -40000 : (-1*(i))));
				     posMap.put(new Long(posMap.size()), i + getFrequestUserCount() + getFrequentCircleCount() + getCircleCount() + getEventsCount() + getCompanyCount());
				 }
			 }
		 }
		 
		 if(mUserCursor != null)
		 {
			 final int prePos = mUserCursor.getPosition();
			 Log.d(TAG, "what is my current positin="+prePos);
			 getAlphabetPos(mUserCursor);		
			 //cursor.moveToPosition(prePos);
			 
			 //make the new map position
			 //0 ---A
			 //1 ---0
			 //12---B
			 //13---11
//		 posMap.clear();
			 
			 String sections[] = new String[alphaPos.size()];
			 int counts[] = new int[alphaPos.size()];
			 for(int i=0;i<alphaPos.size();i++)
			 {   
			     AlphaPost item = alphaPos.get(i);
			     sections[i] = item.alpha;           
			     
			     if(debugsort)
			         Log.d(TAG, "original="+posMap.size() +" map to="+(item.pos==0?-50000:(-1*item.pos)));
			     
			     item.setNewPosition(posMap.size());
			     posMap.put(new Long(posMap.size()), item.pos==0?-50000:(-1*item.pos));
			     
			     if((i+1) < alphaPos.size())
			     {
			         counts[i]   = alphaPos.get(i+1).pos - item.pos;
			         
			         for(int index=item.pos;index<alphaPos.get(i+1).pos;index++)
			         {           
			             if(debugsort)
			                 Log.d(TAG, "AAAAAAAAoriginal="+posMap.size() +" map to="+index);
			             posMap.put(new Long(posMap.size()), index);
			         }
			     }
			     else
			     {
			         counts[i] = namePinYin.size() - item.pos;
			         //for last one
			         for(int index=item.pos;index<namePinYin.size() + getCircleCount() + getFrequentCircleCount() + getFrequestUserCount() + getEventsCount() + getGroupsCount() + getCompanyCount() ;index++)
			         {   
			             if(debugsort)
			                 Log.d(TAG, "bbbbboriginal="+posMap.size() +" map to="+index);
			             posMap.put(new Long(posMap.size()), index);
			         }
			     }
	        }       
			 
		 }
		 
		 if(atoz != null) {
		     atoz.setAlphaMap(posMap, alphaPos);
		 }
	 }
	 
	 public void setSelectmap(HashSet<Long> set, HashSet<Long> circleset)
	 {
		 mSelectedUser.clear();
		 mSelectedUser.addAll(set);
		 
		 mSelectedCircle.clear();
		 mSelectedCircle.addAll(circleset);
	 }
		
	 private void getAlphabetPos(Cursor sortKey)
	 {
	     namePinYin.clear();
	     alphaPos.clear();
	     HashMap<String , Integer> pos = new HashMap<String , Integer>();	
	     if(sortKey != null && sortKey.moveToFirst())
	     {	
	         do {
                 int index = sortKey.getColumnIndex(QiupuORM.UsersColumns.NAME_PINGYIN);
                 String sortStr = null;
                 if (index >= 0) {
                     sortStr = sortKey.getString(index);
                 }
                 if (null == sortStr) {
                     index = sortKey.getColumnIndex(EmployeeColums.NAME_PINYIN);
                     if (index >= 0) {
                         sortStr = sortKey.getString(index);
                     }
                 }

                 String alpha = TextUtils.isEmpty(sortStr) ? "" : sortStr.subSequence(0, 1).toString();

                 if(debugsort)
                     Log.d(TAG, "name_pinyin="+sortStr + " alpha="+alpha);
                 
                 namePinYin.add(alpha);
                 
                 if(pos.get(alpha) == null)
                 {
                     pos.put(alpha, sortKey.getPosition());
                     if(debugsort)
                         Log.d(TAG, "add name_pinyin="+sortStr + "                   alpha="+alpha + " position="+sortKey.getPosition() + getCircleCount());
                     alphaPos.add(new AlphaPost(alpha, sortKey.getPosition() + getCircleCount() + getFrequentCircleCount() + getFrequestUserCount() + getEventsCount() + getGroupsCount() + getCompanyCount()));
                 }
	         }
	         while(sortKey.moveToNext());
	         
	         if(debugsort)
	         {
	             for(int i=0;i<alphaPos.size();i++)
	             {	    		
	                 Log.d(TAG, "alpha="+alphaPos.get(i).alpha + " pos="+alphaPos.get(i).pos);
	             }
	         }
	     }
	     
	     pos.clear();
	     pos = null;
	     
	     //Collections.sort(alphaPos);		
	 }
	 
	 private View generateATOZItem()
	 {
	     TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));
	     but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
	     but.setOnClickListener(null);
	     return but;
	 }
	 private int getCircleCount()
	 {
	     return mCircleCursor != null ? mCircleCursor.getCount() : 0;
	 }
	 private int getFrequentCircleCount () {
		 return mFrequestCircles != null ? mFrequestCircles.getCount() : 0;
	 }
	 private int getFrequestUserCount() {
		 return mFrequestUsers != null ? mFrequestUsers.getCount() : 0;
	 }
	 private int getEventsCount() {
		 return mEventCursor != null ? mEventCursor.getCount() : 0;
	 }
	 private int getGroupsCount() {
		 return mGroupCursor != null ? mGroupCursor.getCount() : 0;
	 }
	 private int getCompanyCount() {
		 return mCompanyCursor != null ? mCompanyCursor.getCount() : 0;
	 }
	 
	 public void closeAllCursor() {
		 if(mUserCursor != null) {
			 mUserCursor.close();
		 }
		 if(mCircleCursor != null) {
			 mCircleCursor.close();
		 }
		 if(mFrequestUsers != null) {
			 mFrequestUsers.close();
		 }
		 if(mFrequestCircles != null) {
			 mFrequestCircles.close();
		 }
		 if(mEventCursor != null) {
			 mEventCursor.close();
		 }
		 if(mGroupCursor != null) {
			 mGroupCursor.close();
		 }
		 if(mCompanyCursor != null) {
			 mCompanyCursor.close();
		 }
	 }
}
