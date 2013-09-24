package com.borqs.common.view;

import twitter4j.Stream;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.borqs.qiupu.R;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-21
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */
class StreamRowPhotoReshareView extends StreamRowPhotoView implements ViewSwitcher.ViewFactory{
    private static final String TAG = "StreamRowPhotoReshareView";


    public StreamRowPhotoReshareView(Context ctx, Stream stream, boolean isComments) {
        super(ctx, R.layout.stream_row_layout_reshare_photo, stream, isComments);
    }

    @Override
    protected int parsePhotoAttachment(Stream stream) {
    	int count = 0 ;
    	if(stream != null) {
    		count = super.parsePhotoAttachment(stream.retweet);
    	}
        return count;
    }
    
    @Override
    protected void setUI() {
    	super.setUI();
    	
    	boolean isVisibleContainer = false;
        ViewGroup container = (ViewGroup)findViewById(R.id.stream_reshared_stream);
        if (null != container) {
        	Stream origin = post.retweet;
            if (null != origin) {
                isVisibleContainer = true;
                refreshPostedMessageTextContent(container, origin);
//                setStreamUnitUi(container, origin, forcomments, true);
//                setupPosterImageRunner(container, origin);
                setupOriginalPosterName(container, origin);
            } else {
                // The origin stream has been removed.
                TextView textView = (TextView)container.findViewById(R.id.reshare_source);
                if (null != textView) {
                    isVisibleContainer = true;
                    textView.setText(R.string.exception_stream_removed);
                }
            }

            container.setVisibility(isVisibleContainer ? View.VISIBLE : View.GONE);
        }
    }
}
