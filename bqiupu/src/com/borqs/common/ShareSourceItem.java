package com.borqs.common;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.R;

public class ShareSourceItem implements java.io.Serializable {
	private static final long serialVersionUID = 8020166132262934739L;
	public String mId;
    public String mLabel;
    public String mScheme;
    public String mTarget;
    public int mCount;
    public int mType;

    public ShareSourceItem(String id) {
        mId = id;
    }
    
    public static String getSourceItemLabel(Context context, String label, int type) {
    	if(BpcApiUtils.TEXT_POST == type){
    		return context.getString(R.string.resource_share_text);
    	}else if(BpcApiUtils.APK_POST == type) {
    		return context.getString(R.string.resource_share_apps);
    	}else if(BpcApiUtils.BOOK_POST == type) {
    		return context.getString(R.string.resource_share_books);
    	}else if(BpcApiUtils.LINK_POST == type) {
    		return context.getString(R.string.resource_share_links);
    	}else if(BpcApiUtils.IMAGE_POST == type) {
    		return context.getString(R.string.resource_share_photos);
    	}else if(BpcApiUtils.STATIC_FILE_POST == type) {
    		return context.getString(R.string.resource_share_static_file);
    	}else if(BpcApiUtils.AUDIO_POST == type) {
    		return context.getString(R.string.resource_share_audio);
    	}else if(BpcApiUtils.VIDEO_POST == type) {
    		return context.getString(R.string.resource_share_video);
    	}else {
    		return label;
    	}
    }
    
    public static Drawable getSorceItemIcon(Context context, int type) {
    	if(BpcApiUtils.TEXT_POST == type){
    		return context.getResources().getDrawable(R.drawable.share_text);
    	}else if(BpcApiUtils.APK_POST == type) {
    		return context.getResources().getDrawable(R.drawable.share_app);
    	}else if(BpcApiUtils.BOOK_POST == type) {
    		return context.getResources().getDrawable(R.drawable.share_book);
    	}else if(BpcApiUtils.LINK_POST == type) {
    		return context.getResources().getDrawable(R.drawable.share_link);
    	}else if(BpcApiUtils.IMAGE_POST == type) {
    		return context.getResources().getDrawable(R.drawable.share_picture);
    	}else if(BpcApiUtils.STATIC_FILE_POST == type) {
    		return context.getResources().getDrawable(R.drawable.share_file);
    	}else if(BpcApiUtils.AUDIO_POST == type) {
    		return context.getResources().getDrawable(R.drawable.share_music);
    	}else if(BpcApiUtils.VIDEO_POST == type) {
    		return context.getResources().getDrawable(R.drawable.share_video);
    	}else {
    		return context.getResources().getDrawable(R.drawable.default_app_icon);
    	}
    }
}
