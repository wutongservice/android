
package com.borqs.common.view;

import twitter4j.EventTheme;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class EventThemeItemView extends SNSItemView {
    
    private static final String TAG = "EventThemeItemView";
    private ImageView icon;
    private View mLayout_process;
    private MainHandler mHandler;
    private EventTheme mTheme;
    private Context mContext;

    public EventThemeItemView(Context context) {
        super(context);		
    }
    
    @Override
    public String getText() {		
        return null;
    }
    
    public EventThemeItemView(Context context, EventTheme theme) {
        super(context);
        mContext = context;
        mTheme = theme;
        mHandler = new MainHandler();
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
        
        View convertView  = factory.inflate(R.layout.event_theme_item_view, null);      
        addView(convertView);
        
        icon      = (ImageView)convertView.findViewById(R.id.img);
        mLayout_process = convertView.findViewById(R.id.layout_process);
        setUI();
    }

    public void setItem(EventTheme theme) {
        mTheme = theme;		
        setUI();
    }
    public EventTheme getItem() {
    	return mTheme;
    }

    public void refreshUI() {
        setUI();
    }

    private void setUI() {
        if(mTheme != null) {
        	shootImageRunner(mTheme.image_url, icon);
        } else {
            Log.d(TAG, "the user is null");
        }
    }

    public static final String RESULT = "RESULT";
	private final static int LOAD_SUCCESS = 0x0001;
	private final static int LOAD_FAILED = 0x0002;

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_SUCCESS: {
				mLayout_process.setVisibility(View.GONE);
				break;
			}
			case LOAD_FAILED: {
				mLayout_process.setVisibility(View.GONE);
				break;
			}
			}
		}
	}
	
    private void shootImageRunner(String photoUrl,ImageView img) {
    	img.setImageResource(R.drawable.photo_default_img);
    	ImageRun photo_1 = new ImageRun(mHandler, photoUrl, 0);
		photo_1.SetOnImageRunListener(new ImageRun.OnImageRunListener() {

			@Override
			public void onLoadingFinished() {
				Message msg = mHandler.obtainMessage(LOAD_SUCCESS);
				msg.sendToTarget();
			}

			@Override
			public void onLoadingFailed() {
				Message msg = mHandler.obtainMessage(LOAD_FAILED);
				msg.sendToTarget();
			}});
		photo_1.addHostAndPath = true;
		final Resources resources = mContext.getResources();
		photo_1.width = resources.getDisplayMetrics().widthPixels;
		photo_1.height = (int) resources.getDimension(R.dimen.event_cover_item_height);
		photo_1.need_scale = true;
		photo_1.setImageView(img);
		photo_1.post(null);
	}
}
