
package com.borqs.common.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.util.MyHtml;
import com.borqs.information.InformationBase;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.util.DateUtil;

public class InformationItemView extends SNSItemView {
    
    private static final String TAG = "InformationItemView";
    private InformationBase mInfoBase;
    private ImageView mImage;
    private TextView mContent;
    private TextView mDate;
    private ImageView mContactImage;
    
    public InformationBase getItem() {
        return mInfoBase;
    }
    
    public InformationItemView(Context context) {
        super(context);		
    }
    
    @Override
    public String getText() {		
        return null;
    }
    
    public InformationItemView(Context context, InformationBase informationbase) {
        super(context);
        mInfoBase = informationbase;
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
        View convertView  = factory.inflate(R.layout.information_list_item, null);      
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(convertView);
        
        mImage = (ImageView) convertView.findViewById(R.id.img);
		mContent = (TextView) convertView.findViewById(R.id.message_content);
		mDate = (TextView) convertView.findViewById(R.id.date);
		mContactImage = (ImageView) convertView.findViewById(R.id.contact_img);
        
        setUI();
    }

    public void setInformation(InformationBase informationbase) {
        mInfoBase = informationbase;
        setUI();
    }

    public void refreshUI() {
        setUI();
    }

    private void setUI() {
        if(mInfoBase != null) {
//        	final String image_url = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IMAGE_URL));
//			final String title = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TITLE));
//			final String title_html = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TITLE_HTML));
//			final String body = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.BODY));
//			holder.data_appId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.APP_ID));
//			holder.data_senderId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.SENDER_ID));
//			long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.M_ID));
//			final long time = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LAST_MODIFY));			
			mImage.setImageResource(R.drawable.default_user_icon);
			setUserPhoto(mInfoBase.image_url, mImage);
			mImage.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!TextUtils.isEmpty(mInfoBase.appId) && !TextUtils.isEmpty(mInfoBase.senderId) &&!mInfoBase.senderId.equals(mInfoBase.appId)) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("borqs://profile/details?uid=" + mInfoBase.senderId));
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						mContext.startActivity(intent);
					}
				}
			});
			
			if (mInfoBase.apppickurl != null && TextUtils.isEmpty(mInfoBase.apppickurl.toString()) == false)
			{
				mContactImage.setVisibility(View.VISIBLE);	
				mContactImage.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (mInfoBase.apppickurl != null) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(mInfoBase.apppickurl);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							mContext.startActivity(intent);
						}						
					}
				});
			}
			else
			{
				mContactImage.setVisibility(View.GONE);
			}
			
			String temp = TextUtils.isEmpty(mInfoBase.body) ? mInfoBase.title : mInfoBase.title + "\n\n" + mInfoBase.body;
			mContent.setTag(mInfoBase.id);
			if (TextUtils.isEmpty(mInfoBase.title_html)) {
				mContent.setText(temp);
			} else {
				String tempForHtml = TextUtils.isEmpty(mInfoBase.body) ? mInfoBase.title_html : mInfoBase.title_html + "<br><br>" + mInfoBase.body;
//				Spanned tempHtml = asyncHtmlTextLoader.loadHtmlText(itemId, tempForHtml, 
//						new AsyncHtmlTextLoader.HtmlTextCallback() {
//							@Override
//							public void textLoaded(Spanned htmlText, long idTag) {
//								TextView tv = (TextView) informationListView
//										.findViewWithTag(idTag);
//								if (tv != null) {
//									tv.setText(htmlText);
//								}
//							}
//						});
				
				mContent.setText(tempForHtml == null ? temp : MyHtml.fromHtml(tempForHtml));
				if(!mInfoBase.read) {
					mContent.getPaint().setFakeBoldText(true);
					mContent.postInvalidate();
				}else {
					mContent.getPaint().setFakeBoldText(false);
					mContent.postInvalidate();
				}
			}
//			boolean is_read = false;
//			if(readStatus.containsKey(itemId)) {
//				is_read = readStatus.get(itemId);
//			}else {
//				is_read = cursor.getInt(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IS_READ)) == 1;
//				readStatus.put(itemId, is_read);
//			}
//			final boolean readStatus = is_read;
//			mHandler.post(new Runnable() {
//				@Override
//				public void run() {
//					holder.content.getPaint().setFakeBoldText(!readStatus);
//					holder.content.postInvalidate();
//				}
//			});
//			mHandler.post(new Runnable() {
//				@Override
//				public void run() {
					mDate.setText(DateUtil.converToRelativeTime(mContext, mInfoBase.lastModified));
//				}
//			});
        	
        } else {
            Log.d(TAG, "the user is null");
        }
    }

    public void reverContentText(boolean read) {
    	if(mContent != null) {
//    		if(!read) {
    			mContent.getPaint().setFakeBoldText(false);
    			mContent.postInvalidate();
    			mInfoBase.read = true;
//    		}
    	}
    }


    private void setUserPhoto(String url, ImageView image) {
        ImageRun imagerun = new ImageRun(null,url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(image);
        imagerun.post(null);
    }
}
