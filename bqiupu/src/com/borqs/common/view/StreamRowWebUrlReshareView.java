package com.borqs.common.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.borqs.qiupu.R;
import twitter4j.Stream;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-21
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */
class StreamRowWebUrlReshareView extends StreamRowWebUrlView {
    private static final String TAG = "StreamRowWebUrlReshareView";


    public StreamRowWebUrlReshareView(Context ctx, Stream stream, boolean isComments) {
        super(ctx, R.layout.stream_row_layout_reshare_weburl, stream, isComments);
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

    protected void setupUrlLinkAttachment(ViewGroup parent, Stream stream) {
    	super.setupUrlLinkAttachment(parent, stream.retweet);
    }
}
