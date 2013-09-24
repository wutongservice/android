package com.borqs.common.view;

import twitter4j.Stream;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.borqs.qiupu.R;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-21
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */
class StreamRowVideoReshareView extends StreamRowView{
    private static final String TAG = "StreamRowVideoReshareView";

    public StreamRowVideoReshareView(Context ctx, Stream stream, boolean isComments) {
        super(ctx, R.layout.stream_row_layout_reshare_video, stream, isComments);
    }

    @Override
    protected void setUI() {
//    	super.setUI();

        if (null != post && null != post.retweet) {
            setupVideoAttachment(this, post.retweet);
        }

        boolean isVisibleContainer = false;
        ViewGroup container = (ViewGroup)findViewById(R.id.stream_reshared_stream);
        if (null != container) {
            Stream origin = null != post ? post.retweet : null;

            if (null != origin) {
                isVisibleContainer = true;
                refreshPostedMessageTextContent(container, post);
                setStreamFooterUi(true);
//                refreshPostedMessageTextContent(container, origin);
                setupOriginalPosterName(container, origin);
            } else {
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
