
package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.borqs.qiupu.db.EmployeeColums;
import twitter4j.QiupuUser;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.AlphaPost;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class BPCFriendsNewAdapter extends BaseAdapter{
    private static final String TAG = "Qiupu.BPCFriendsNewAdapter";
    private Cursor items;
    private ArrayList<QiupuUser> itemList = new ArrayList<QiupuUser>();
    private Context      mContext;
    private boolean misdelete;
    
    private QiupuORM orm;
    
    private boolean  userCuror;
    
    private HashMap<String, UsersActionListner> mUserActionMap = new HashMap<String, UsersActionListner>();

    private MoreItemCheckListener mCheckerListener;
    private boolean mIsVCard = false;
    private boolean mFindFriendsMode = false;

    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }

    public void registerUsersActionListner(String key, UsersActionListner rl) {
    	mUserActionMap.put(key, rl);
    }

    public void unregisterUsersActionListner(String key) {
    	mUserActionMap.remove(key);
    }

    public void setIsdeleteMode(boolean isdelete)
    {
        misdelete = isdelete;
    }
    
    public BPCFriendsNewAdapter(Context context, MoreItemCheckListener listener, boolean isVCard, boolean find_friends) {
        mCheckerListener = listener;
        mContext = context;
        mIsVCard = isVCard;
        orm = QiupuORM.getInstance(context);
        mFindFriendsMode = find_friends;
    }

    public int getCount() {
        if (userCuror == false) {
            if (null == itemList) {
                return 0;
            } else {
                int count = itemList.size();
                if (null != mCheckerListener && mCheckerListener.isMoreItemHidden()) {
                    ++count;
                }
                return count;
            }
        } else {
            if (items == null) {
                return 0;
            } else {
                int count = items.getCount() + alphaPos.size();
                if (null != mCheckerListener && mCheckerListener.isMoreItemHidden()) {
                    ++count;
                }
                return count;
            }
        }
    }
    
    public QiupuUser getItem(int position) {	
        if(userCuror == false)
        {
            if(itemList != null && position < itemList.size())
            {
                return itemList.get(position);
            }
            else
            {
                return null;
            }
        }
        else
        {
            if(items != null )
            {
                Integer newposition = posMap.get(new Long(position));
                if(debugsort)
                    Log.d(TAG, "position="+position + " map to="+newposition);
                if(newposition != null && newposition >= 0 && items.moveToPosition(newposition))
                {
                    return orm.createUserInfoWithOutExpandInfo(items);
                }
                else
                {
                    //TODO					
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }
    
    public long getItemId(int position) {		
        //QiupuUser user =  getItem(position);
        Integer newposition = posMap.get(new Long(position));		
        return newposition ==null?-1:newposition;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;		
        QiupuUser user = getItem(position);
        if(user != null)
        {
            BpcFriendsItemView FView;
            if (convertView == null || false == (convertView instanceof BpcFriendsItemView) )
            {
//                FView = new BpcFriendsItemView(mContext, user, misdelete);
                FView = BpcFriendsItemView.newInstance(mContext, user, misdelete, mIsVCard, mFindFriendsMode);
                holder = new ViewHolder();
                
                FView.setTag(holder);
                holder.view = FView;
                FView.attachActionListener(mUserActionMap);
                
                convertView = FView;
                
            } else {
                holder = (ViewHolder)convertView.getTag();
                holder.view.setIsDeleteModel(misdelete);
                holder.view.setUser(user);
                holder.view.attachActionListener(mUserActionMap);
            }
            
            return convertView;
        }
        else
        {
            Integer newposition = posMap.get(new Long(position));
            String dockStr = "";
            if(newposition != null)
            {	
                if(newposition < 0)
                {
                    final int pos = Math.abs(newposition);
                    if(pos == 10000)
                    {
                        dockStr = namePinYin.get(0);
                    }
                    else
                    {
                        dockStr = namePinYin.get(pos);
                    }
                    
                }
            }
            if(userCuror)
            {
                TextView tv = (TextView) generateATOZItem();
                tv.setText(dockStr);
                return tv;
            }
            else
            {
                return generateRefreshItem();
            }
        }
        
    }

    private View generateATOZItem()
    {
        TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));
        but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
        but.setOnClickListener(null);
        return but;
    }


//    private View generateMoreItem() {
//        if (mContext instanceof MoreItemCheckListener) {
//            Button but = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
//            but.setHeight((int) mContext.getResources().getDimension(R.dimen.more_button_height));
//            but.setBackgroundResource(R.drawable.list_selector_background);
//            MoreItemCheckListener listener = (MoreItemCheckListener) mContext;
//            but.setOnClickListener(listener.getMoreItemClickListener());
//            but.setText(listener.getMoreItemCaptionId());
//            return but;
//        } else {
//            return null;
//        }
//    }

    private Button generateMoreItem() {
        Button but = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
        but.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
        but.setBackgroundResource(R.drawable.list_selector_background);
        return but;
    }

    private View generateRefreshItem() {
        if (null != mCheckerListener) {
            Button but = generateMoreItem();
            but.setOnClickListener(mCheckerListener.getMoreItemClickListener());
            but.setText(mCheckerListener.getMoreItemCaptionId());
            return but;
        }
        return null;
    }

    static class ViewHolder
    {
        public BpcFriendsItemView view;
    }
    
    
    private ArrayList<AlphaPost> alphaPos   = new ArrayList<AlphaPost>();
    private ArrayList<String>    namePinYin = new ArrayList<String>();	
    
    private HashMap<Long, Integer> posMap = new HashMap<Long, Integer>();
    private final static boolean debugsort = false;
    public void alterDataList(Cursor cursor) {
        alterDataList(cursor, null);
    }
    
    public void alterDataList(Cursor cursor, AtoZ atoz) {
        if(items == cursor)
            return;
        
        items = cursor;
        userCuror = true;
        
        if(alphaPos != null)
            alphaPos.clear();
        
        final int prePos = items.getPosition();
        Log.d(TAG, "what is my current positin="+prePos);
        getAlphabetPos(items);		
        //cursor.moveToPosition(prePos);
        
        //make the new map position
        //0 ---A
        //1 ---0
        //12---B
        //13---11
        posMap.clear();
        String sections[] = new String[alphaPos.size()];
        int counts[] = new int[alphaPos.size()];
        for(int i=0;i<alphaPos.size();i++)
        {	
            AlphaPost item = alphaPos.get(i);
            sections[i] = item.alpha;			
            
            if(debugsort)
                Log.d(TAG, "original="+posMap.size() +" map to="+(item.pos==0?-10000:(-1*item.pos)));
            
            item.setNewPosition(posMap.size());
            posMap.put(new Long(posMap.size()), item.pos==0?-10000:(-1*item.pos));
            
            if((i+1) < alphaPos.size())
            {
                counts[i]   = alphaPos.get(i+1).pos - item.pos;
                
                for(int index=item.pos;index<alphaPos.get(i+1).pos;index++)
                {			
                    if(debugsort)
                        Log.d(TAG, "original="+posMap.size() +" map to="+index);
                    posMap.put(new Long(posMap.size()), index);
                }
            }
            else
            {
                counts[i] = namePinYin.size() - item.pos;
                //for last one
                for(int index=item.pos;index<namePinYin.size();index++)
                {	
                    if(debugsort)
                        Log.d(TAG, "original="+posMap.size() +" map to="+index);
                    posMap.put(new Long(posMap.size()), index);
                }
            }
        }		
        
        if(atoz != null) {
            atoz.setAlphaMap(posMap, alphaPos);
        }
        
        //calculator the position		
        notifyDataSetChanged();
    }
    
    public void alterDataList(ArrayList<QiupuUser> userList) {
        userCuror = false;
        itemList.clear();
        itemList.addAll(userList);
        
        notifyDataSetChanged();
    }	
    
//    public static class AlphaPost implements Comparable
//    {
//        public String alpha;
//        public int    pos;
//        public int    newPos;
//        
//        public void setNewPosition(int pos)
//        {
//            newPos = pos;
//        }
//        public AlphaPost(String alpha, int pos)
//        {
//            this.alpha = alpha;
//            this.pos = pos;
//        }
//        @Override
//        public int compareTo(Object obj) {			
//            return alpha.compareTo((String)obj);
//        }		
//        
//        public String toString()
//        {
//            return " alpha="+alpha;
//        }
//    }
    private static boolean isEmpty(String str)
    {
    	return str==null || str.length() == 0;
    }
    public void getAlphabetPos(Cursor sortKey)
    {
        namePinYin.clear();
        alphaPos.clear();
        HashMap<String , Integer> pos = new HashMap<String , Integer>();	
        if(sortKey != null && sortKey.moveToFirst())
        {	
            do {
                int index = sortKey.getColumnIndex(QiupuORM.UsersColumns.NAME_PINGYIN);
                if (index < 0) {
                    index = sortKey.getColumnIndex(EmployeeColums.NAME_PINYIN);
                }
                String sortStr = index < 0 ? "" : sortKey.getString((index));
                if(isEmpty(sortStr))
                {
                	sortStr = "ZZZZZZ";
                }
                String alpha = sortStr.subSequence(0, 1).toString();
                
                if(debugsort)
                    Log.d(TAG, "name_pinyin="+sortStr + " alpha="+alpha);
                
                namePinYin.add(alpha);
                
                if(pos.get(alpha) == null)
                {
                    pos.put(alpha, sortKey.getPosition());
                    if(debugsort)
                        Log.d(TAG, "add name_pinyin="+sortStr + "                   alpha="+alpha + " position="+sortKey.getPosition());
                    alphaPos.add(new AlphaPost(alpha, sortKey.getPosition()));
                }
            }while(sortKey.moveToNext());
            
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
    public int getRealCount() {		
        return items.getCount();
    }
    
    public void resetCursor(Cursor cursor) {
    	if(items != null ){
    		items.close();
    	}
    	items = cursor; 
    }
    

}