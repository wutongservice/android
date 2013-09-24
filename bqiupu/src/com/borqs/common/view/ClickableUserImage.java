package com.borqs.common.view;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-3
 * Time: 上午11:41
 * To change this template use File | Settings | File Templates.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.View;
//import com.google.android.apps.plus.content.AvatarRequest;
//import com.google.android.apps.plus.service.AvatarCache;
//import com.google.android.apps.plus.service.AvatarCache.AvatarConsumer;

class ClickableUserImage implements /*AvatarCache.AvatarConsumer,*/ ClickableItem
{
  private static Paint sImageSelectedPaint;
//  private final AvatarCache mAvatarCache;
  private boolean mAvatarInvalidated;
  private boolean mAvatarLoaded;
//  private AvatarRequest mAvatarRequest;
  private Bitmap mBitmap;
  private final UserImageClickListener mClickListener;
  private boolean mClicked;
  private final Rect mContentRect;
  private final long mUserId;
  private final String mUserName;
  private final View mView;

  public ClickableUserImage(View paramView, long paramLong, String paramString, UserImageClickListener paramUserImageClickListener)
  {
    this.mView = paramView;
    Context localContext = paramView.getContext();
    this.mContentRect = new Rect();
    this.mClickListener = paramUserImageClickListener;
    this.mUserId = paramLong;
    this.mUserName = paramString;
//    this.mAvatarCache = AvatarCache.getInstance(localContext);
//    this.mAvatarRequest = new AvatarRequest(this.mUserId, 1);
//    this.mAvatarCache.loadAvatar(this, this.mAvatarRequest);
    if (sImageSelectedPaint == null)
    {
      sImageSelectedPaint = new Paint();
      sImageSelectedPaint.setStrokeWidth(4.0F);
      sImageSelectedPaint.setColor(localContext.getApplicationContext().getResources().getColor(2131296283));
      sImageSelectedPaint.setStyle(Paint.Style.STROKE);
    }
  }

  public void drawSelectionRect(Canvas paramCanvas)
  {
    paramCanvas.drawRect(this.mContentRect.left, this.mContentRect.top, 2 + this.mContentRect.right, 2 + this.mContentRect.bottom, sImageSelectedPaint);
  }

  public Bitmap getBitmap()
  {
    if (this.mAvatarInvalidated)
    {
      this.mAvatarInvalidated = false;
//      this.mAvatarCache.refreshAvatar(this, this.mAvatarRequest);
    }
    return this.mBitmap;
  }

  public Rect getRect()
  {
    return this.mContentRect;
  }

  public boolean handleEvent(int paramInt1, int paramInt2, int paramInt3)
  {
    int i = 1;
    if (paramInt3 != 3)
    {
      if (this.mContentRect.contains(paramInt1, paramInt2))
      {
        switch (paramInt3)
        {
        default:
          break;
        case 0:
          this.mClicked = i > 0;
          break;
        case 1:
          if ((this.mClicked) && (this.mClickListener != null))
            this.mClickListener.onUserImageClick(this.mUserId, this.mUserName);
          this.mClicked = false;
          break;
        }
      }
      else
      {
        if (paramInt3 == i)
          this.mClicked = false;
        i = 0;
      }
    }
    else
      this.mClicked = false;
    return i > 0;
  }

  public boolean isAvatarLoaded()
  {
    return this.mAvatarLoaded;
  }

  public boolean isClicked()
  {
    return this.mClicked;
  }

  public void onAvatarChanged(long paramLong)
  {
    if (paramLong == this.mUserId)
    {
      this.mAvatarInvalidated = true;
      this.mAvatarLoaded = false;
      this.mView.invalidate();
    }
  }

  public void setAvatarBitmap(Bitmap paramBitmap, boolean paramBoolean)
  {
    boolean bool;
    if (paramBoolean)
      bool = false;
    else
      bool = true;
    this.mAvatarLoaded = bool;
    this.mBitmap = paramBitmap;
    this.mView.invalidate();
  }

  public void setRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mContentRect.set(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public static abstract interface UserImageClickListener
  {
    public abstract void onUserImageClick(long paramLong, String paramString);
  }
}
