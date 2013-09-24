package com.borqs.common.view;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-3
 * Time: 上午11:37
 * To change this template use File | Settings | File Templates.
 */

import android.graphics.Rect;

public class ClickableRect implements ClickableItem
{
  private boolean mClicked;
  private ClickableRectListener mListener;
  private Rect mRect;

  public ClickableRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ClickableRectListener paramClickableRectListener)
  {
    this(new Rect(paramInt1, paramInt2, paramInt1 + paramInt3, paramInt2 + paramInt4), paramClickableRectListener);
  }

  public ClickableRect(Rect paramRect, ClickableRectListener paramClickableRectListener)
  {
    this.mRect = paramRect;
    this.mListener = paramClickableRectListener;
  }

  public boolean handleEvent(int paramInt1, int paramInt2, int paramInt3)
  {
    int i = 1;
    if (paramInt3 != 3)
    {
      if (this.mRect.contains(paramInt1, paramInt2))
      {
        switch (paramInt3)
        {
        default:
          break;
        case 0:
          this.mClicked = i > 0;
          break;
        case 1:
          if ((this.mClicked) && (this.mListener != null))
            this.mListener.onClickableRectClick(this);
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

  public static abstract interface ClickableRectListener
  {
    public abstract void onClickableRectClick(ClickableRect paramClickableRect);
  }
}
