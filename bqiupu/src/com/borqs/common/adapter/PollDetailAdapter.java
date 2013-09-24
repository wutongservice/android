package com.borqs.common.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import twitter4j.PollItemInfo;
import twitter4j.Stream.Comments.Stream_Post;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.CommentItemView;
import com.borqs.common.view.PollChildItemView;
import com.borqs.common.view.PollChildItemView.PollItemCheckActionListener;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.PollDetailFragment;

public class PollDetailAdapter extends BaseAdapter {

    private static final String TAG = "PollDetailAdapter";

    private Context             mContext;
    private HashMap<Long, Integer> posMap = new HashMap<Long, Integer>();
    private ArrayList<PollItemInfo> mPoolItemList = new ArrayList<PollItemInfo>();
    private ArrayList<String> mSelectItemList = new ArrayList<String>();
    private ArrayList<Stream_Post> mCommentList = new ArrayList<Stream_Post>();
    private boolean mHasMore = false;
    private boolean mViewerCanVote;
    private boolean mhasVoted;
    private boolean mCanAddItems = false;
    private boolean mIsCreator = false;
    private int mAttendStatus;
    private int mMulti;
    private int mCommentCount;
    private int mViewerLeft;
    private int mMode = -1;
    WeakReference<LoaderMoreListener> loaderMore;
    
    private HashMap<String, PollItemCheckActionListener> mCheckClickListenerMap = new HashMap<String, PollItemCheckActionListener>();

    public void registerCheckClickActionListener(String key, PollItemCheckActionListener rl) {
        mCheckClickListenerMap.put(key, rl);
    }

    public void unRegisterCheckClickActionListener(String key) {
        mCheckClickListenerMap.remove(key);
    }

    public interface LoaderMoreListener {
        public int getCaptionResourceId();
        public View.OnClickListener loaderMoreClickListener();
    }

    public PollDetailAdapter(Context context, LoaderMoreListener loadMoreListener) {
        mContext = context;
        loaderMore = new WeakReference<PollDetailAdapter.LoaderMoreListener>((LoaderMoreListener)loadMoreListener);
    }

    public PollDetailAdapter(Context context, ArrayList<PollItemInfo> pollInfoList) {
        mContext = context;
        mPoolItemList.clear();
        mPoolItemList.addAll(pollInfoList);
    }

    public void alterPollList(ArrayList<PollItemInfo> pollInfoList, ArrayList<String> selectedItems, 
            boolean view_can_vote, int multi, boolean hasVoted, ArrayList<Stream_Post> commentList, 
            boolean hasMore, int comment_count, boolean can_add_items, int attend_status, int viewer_left, 
            boolean isCreator, int mode) {
        mViewerCanVote = view_can_vote;
        mMulti = multi;
        mhasVoted = hasVoted;
        mCanAddItems = can_add_items;
        mAttendStatus = attend_status;
        mViewerLeft = viewer_left;
        mIsCreator = isCreator;
        mMode = mode;

        mPoolItemList.clear();
        mPoolItemList.addAll(pollInfoList);
        mSelectItemList.clear();
        mSelectItemList.addAll(selectedItems);
        mCommentList.clear();
        mCommentList.addAll(commentList);
        Collections.sort(mCommentList);

        mHasMore = hasMore;
        mCommentCount = comment_count;
        generateSpanitem();
        notifyDataSetChanged();
    }

    private void generateSpanitem() {
        posMap.clear();
        if (mPoolItemList.size() > 0) {
            for(int i = 0; i < mPoolItemList.size(); i++) {
//                Log.d(TAG, "generateSpanitem: "  + posMap.size() + " " + i );
                posMap.put(new Long(posMap.size()), i);
            }
            Log.d(TAG, "mCanAddItems = " + mCanAddItems + ", mViewerCanVote = " + mViewerCanVote + ", mIsCreator = " + mIsCreator);
            if (mAttendStatus != PollDetailFragment.ENDING && mCanAddItems && (mViewerCanVote || mIsCreator)) {
                posMap.put(new Long(posMap.size()), -3000);
            }
        }

        if (mCommentList.size() > 0) {
            posMap.put(new Long(posMap.size()), -1000);
            for(int i = 0; i < mCommentList.size(); i++) {
//                Log.d(TAG, "generateSpanitemaaaaaaaaaaaaaaa: "  + posMap.size() + " " + (i + mPoolItemList.size()) );
                posMap.put(new Long(posMap.size()), i + mPoolItemList.size());
            }
            if (mHasMore) {
                posMap.put(new Long(posMap.size()), -2000);
            }
        }
    }

    public int getCount() {
        int count = 0;
        if (mPoolItemList.size() > 0) {
            count = mPoolItemList.size();
            if (mAttendStatus != PollDetailFragment.ENDING && mCanAddItems && (mViewerCanVote || mIsCreator)) {
                count += 1;
            }
        }
        if (mCommentList.size() > 0) {
            count = count + mCommentList.size() + 1;
            if (mHasMore) {
                count = count + 1;
            } else {
//                Log.d(TAG, "getCount(): no more comments");
            }
        }
//        Log.d(TAG, "mPoolItemList.size() = " + mPoolItemList.size() + " mCommentList.size() = " + mCommentList.size() + "\n count = " + count);
        return count;
    }

    public Object getItem(int position) {
        Integer newposition = posMap.get(new Long(position));
//        Log.d(TAG, "get Item position= "+position + " map to= "+newposition + " postMap.size : " + posMap.size());
        if(newposition >= 0) {
            if(newposition < mPoolItemList.size()) {
//                Log.d(TAG, "mPoolItemList: " +  newposition);
                return mPoolItemList.get(newposition);
            }else if((newposition - mPoolItemList.size()) < mCommentList.size()) {
//                Log.d(TAG, "mCommentList: " +  newposition);
                return mCommentList.get(newposition - mPoolItemList.size());
            }else {
                return null;
            }
        } else {
            return null;
        }
//        if (position < mPoolItemList.size()) {
//        	PollItemInfo itemInfo = mPoolItemList.get(position);
//        	postSetUpPollItem(itemInfo);
//            return itemInfo;
//        } else {
//            return null;
//        }
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        Integer newposition = posMap.get(new Long(position));
        Object itemObj = getItem(position);
//        PollItemInfo pollItemInfo = getItem(position);
        if (itemObj != null) {
            if (PollItemInfo.class.isInstance(itemObj)) {
                PollItemInfo pollItem = (PollItemInfo) itemObj;

                if (pollItem != null) {
                    PollChildItemView itemView = null;
                    
                    if (convertView == null || false == (convertView instanceof PollChildItemView)) {
                        holder = new ViewHolder();
                        itemView = new PollChildItemView(mContext, pollItem, mViewerCanVote, mMulti, mhasVoted, mAttendStatus, mMode);
                        itemView.attachCheckListener(mCheckClickListenerMap);
                        holder.pollItemView = itemView;
                        itemView.setTag(holder);
                        convertView = itemView;
    
                    } else {
                        holder = (ViewHolder) convertView.getTag();
                        itemView = holder.pollItemView;
                        itemView.setPollItemInfo(pollItem, mViewerCanVote, mMulti, mhasVoted, mAttendStatus, mMode);
                        itemView.attachCheckListener(mCheckClickListenerMap);
                    }
                    return itemView;
                }
            } else {
                if (Stream_Post.class.isInstance(itemObj)) {
                    Stream_Post commentItem = (Stream_Post) itemObj;

                    if (commentItem != null) {
                        CommentItemView itemView = null;
//                        Log.d(TAG, "CommentItemView : convertView = " + convertView);
                        if (convertView == null || false == (convertView instanceof CommentItemView)) {
                            holder = new ViewHolder();
                            itemView = new CommentItemView(mContext, commentItem, false/*isOwner*/);
                            holder.commentItemView = itemView;
                            itemView.setTag(holder);
                            convertView = itemView;
                        } else {
                            holder = (ViewHolder) convertView.getTag();
                            itemView = holder.commentItemView;
                            itemView.setCommentItem(commentItem);
                        }
                        return itemView;
                    }
                }
            }
        } else {
            if(newposition != null) {
                if(newposition < 0) {
                    final int pos = Math.abs(newposition);
                    if(pos == 1000) {
                        return generateSpanItemView();
                    } if (pos == 2000) {
                        generateMoreItem();
                        refreshLoadingStatus();
                        return mLoadMoreButton;
                    } else{
                        return generateAddItem();
                    }
                }
            }
        }
        return null;
    }

//    private View generateSpanItemView() {
//        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
//        View view = inflater.inflate(R.layout.comment_span_view, null);
//        TextView but = (TextView) view.findViewById(R.id.comment_count_text);
//        String dockStr = String.format(mContext.getString(R.string.comment_count_title), mCommentCount);
//        but.setText(dockStr);
////        but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
//        but.setOnClickListener(null);
//        return view;
//    }

    private TextView generateSpanItemView() {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        TextView but = (TextView) inflater.inflate(R.layout.a_2_z_textview, null);
        String dockStr = String.format(mContext.getString(R.string.comment_count_title), mCommentCount);
        but.setText(dockStr);
        but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
        but.setOnClickListener(null);
        return but;
    }

    private View generateAddItem() {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.add_pool_item_tip, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                (int) mContext.getResources().getDimension(R.dimen.circle_list_item_height)));
        view.setBackgroundResource(R.drawable.list_selector_background);
        ImageView addImage = (ImageView) view.findViewById(R.id.add_image);
        addImage.setOnClickListener(addListener);
        view.setOnClickListener(addListener);
        return view;
    }

    private View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMode == 2) {
                addPollItem();
            } else {
                showDialog();
            }
        }
    };

    private void addPollItem() {
        if (addPollItemListener != null && addPollItemListener.get() != null) {
            int selected_count = addPollItemListener.get().getSelectedItemCount();
            if (selected_count >= mViewerLeft && mIsCreator == false) {
                Toast.makeText(mContext, R.string.revert_poll_add_item_msg, Toast.LENGTH_SHORT).show();
            } else {
                showDialog();
            }
        }
    }

    private LinearLayout container;
    private View addView;
    private void showDialog() {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.add_pool_item_view, null);
        view.setBackgroundResource(R.color.white);

        container = (LinearLayout) view.findViewById(R.id.item_container);
        View defaultItem = view.findViewById(R.id.default_item);
        ImageView icon = (ImageView) defaultItem.findViewById(R.id.remove_item);
        icon.setVisibility(View.GONE);

        TextView tipView = (TextView) view.findViewById(R.id.tip_title);
        addView = view.findViewById(R.id.add_id);
        if (mMode == 2) {
            if (addPollItemListener != null && addPollItemListener.get() != null) {
                int selected_count = addPollItemListener.get().getSelectedItemCount();
                int tmpCount = mViewerLeft - selected_count;
                if (container.getChildCount() >= tmpCount) {
                    if (mIsCreator) {
                        tipView.setVisibility(View.GONE);
                        addView.setOnClickListener(addItemListener);
                    } else {
                        tipView.setText(String.format(mContext.getString(R.string.add_poll_item_tip), tmpCount));
                        addView.setVisibility(View.GONE);
                    }
                } else {
                    if (mIsCreator) {
                        tipView.setVisibility(View.GONE);
                        addView.setOnClickListener(addItemListener);
                    } else {
                        tipView.setText(String.format(mContext.getString(R.string.add_poll_item_tip), tmpCount));
                        addView.setOnClickListener(addItemListener);
                    }
                }
            }
            
        } else {
            containerActionAndUI(tipView);
        }

        String title = mContext.getResources().getString(R.string.add_item_title);
        DialogUtils.ShowDialogwithView(mContext, title, 0, view, positiveListener, negativeListener);
    }

    private void containerActionAndUI(TextView tipView) {
        if (container.getChildCount() >= mViewerLeft) {
            if (mIsCreator) {
                tipView.setVisibility(View.GONE);
                addView.setOnClickListener(addItemListener);
            } else {
                tipView.setText(String.format(mContext.getString(R.string.add_poll_item_tip), mViewerLeft));
                addView.setVisibility(View.GONE);
            }
        } else {
            if (mIsCreator) {
                tipView.setVisibility(View.GONE);
                addView.setOnClickListener(addItemListener);
            } else {
                tipView.setText(String.format(mContext.getString(R.string.add_poll_item_tip), mViewerLeft));
                addView.setOnClickListener(addItemListener);
            }
        }
    }

    private View.OnClickListener addItemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addItem();
        }
    };

    private void addItem() {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.add_poll_item_subview, null);
        ImageView remove = (ImageView) view.findViewById(R.id.remove_item);
        remove.setOnClickListener(removeItemListener);
        remove.setTag(Math.random());
        container.addView(view);
        if (mMode == 2) {
            if (addPollItemListener != null && addPollItemListener.get() != null) {
                int selected_count = addPollItemListener.get().getSelectedItemCount();
                int tmpCount = mViewerLeft - selected_count;
                if (container.getChildCount() >= tmpCount && mIsCreator == false) {
                    addView.setVisibility(View.GONE);
                }
            }
        } else {
            if (container.getChildCount() >= mViewerLeft && mIsCreator == false) {
                addView.setVisibility(View.GONE);
            }
        }
    }

    private View.OnClickListener removeItemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            removeItem(v);
        }
    };

    private void removeItem(View v) {
        int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = container.getChildAt(i);
            if (view != null) {
                ImageView remove = (ImageView) view.findViewById(R.id.remove_item);
                if (v.getTag() == remove.getTag()) {
                    container.removeView(view);
                    if (mMode == 2) {
                        if (addPollItemListener != null && addPollItemListener.get() != null) {
                            int selected_count = addPollItemListener.get().getSelectedItemCount();
                            int tmpCount = mViewerLeft - selected_count;
                            if (container.getChildCount() < tmpCount) {
                                addView.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        if (container.getChildCount() < mViewerLeft) {
                            addView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
    }

    private DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ArrayList<String> msgList = new ArrayList<String>();
            int count = container.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = container.getChildAt(i);
                if (view != null) {
                    EditText editText = (EditText) view.findViewById(R.id.edit_text);
                    Editable editable = editText == null ? null : editText.getText();
                    if (editable != null && TextUtils.isEmpty(editable.toString()) == false) {
                        msgList.add(editable.toString());
                    }
                }
            }
            if (msgList.size() > 0) {
                if (addPollItemListener != null && addPollItemListener.get() != null) {
                    addPollItemListener.get().addPollItems(msgList);
                }
            }
        }
    };

    private DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //do nothing
        }
    };

    static class ViewHolder {
        public PollChildItemView pollItemView;
        public CommentItemView commentItemView;
    }
    
    private void postSetUpPollItem(PollItemInfo info) {
    	if (null != info) {
			 
			 if (mSelectItemList.contains(info.item_id)) {
				 info.selected = true;
			 }
		 }
    }

	public void setSelectItems(ArrayList<String> selectItemsList) {
		mSelectItemList.clear();
		mSelectItemList.addAll(selectItemsList);
	}

	Button mLoadMoreButton;
    private void generateMoreItem() {
        if (null == mLoadMoreButton) {
            mLoadMoreButton = (Button)(((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
            mLoadMoreButton.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
            mLoadMoreButton.setBackgroundResource(R.drawable.list_selector_background);
        }
    }

    public void refreshLoadingStatus() {
        if (loaderMore != null && loaderMore.get() != null) {
            int resId = loaderMore.get().getCaptionResourceId();
            if (null != mLoadMoreButton && resId > 0) {
                mLoadMoreButton.setHeight((int) mContext.getResources().getDimension(R.dimen.more_button_height));
                mLoadMoreButton.setOnClickListener(loaderMore.get().loaderMoreClickListener());
                mLoadMoreButton.setText(resId);
            }
        }

    }

    public interface AddPollItemListener {
        public void addPollItems(ArrayList<String> msgList);
        public int getSelectedItemCount();
    }

    private WeakReference<AddPollItemListener> addPollItemListener;

    public void setAddPollItemListener(AddPollItemListener listener) {
        addPollItemListener = new WeakReference<AddPollItemListener>(listener);
    }
}
