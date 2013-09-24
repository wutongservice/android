package com.borqs.common.view;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-3
 * Time: 上午11:28
 * To change this template use File | Settings | File Templates.
 */

import android.text.style.URLSpan;
//import com.google.android.apps.plus.content.DbMedia;
//import com.google.wireless.tacotruck.proto.Data.Location;

public abstract interface ItemClickListener extends /*ClickableMediaImage.MediaImageClickListener,*/ ClickableStaticLayout.SpanClickListener, ClickableUserImage.UserImageClickListener
{
//  public abstract void onLocationClick(String paramString, Data.Location paramLocation);

//  public abstract void onMediaImageClick(String paramString, DbMedia paramDbMedia, int paramInt);

  public abstract void onSpanClick(URLSpan paramURLSpan);

  public abstract void onUserImageClick(long paramLong, String paramString);
}
