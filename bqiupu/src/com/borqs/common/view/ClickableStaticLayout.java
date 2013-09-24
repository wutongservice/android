package com.borqs.common.view;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-3
 * Time: 上午11:45
 * To change this template use File | Settings | File Templates.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.*;
import android.text.Layout.Alignment;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

public class ClickableStaticLayout extends StaticLayout
  implements ClickableItem
{
  private final SpanClickListener mClickListener;
  private StateURLSpan mClickedSpan;
  private Rect mContentRect;
  private final Spanned mSpannedText;

  public ClickableStaticLayout(CharSequence paramCharSequence, TextPaint paramTextPaint, int paramInt, Layout.Alignment paramAlignment, float paramFloat1, float paramFloat2, boolean paramBoolean, SpanClickListener paramSpanClickListener)
  {
    super(paramCharSequence, paramTextPaint, paramInt, paramAlignment, paramFloat1, paramFloat2, paramBoolean);
    this.mClickListener = paramSpanClickListener;
    if (!(paramCharSequence instanceof Spanned))
      this.mSpannedText = null;
    else
      this.mSpannedText = ((Spanned)paramCharSequence);
  }

  public static SpannableStringBuilder buildStateSpans(Context paramContext, String paramString1, int paramInt, String paramString2, boolean paramBoolean)
  {
    SpannableStringBuilder localSpannableStringBuilder = buildStateSpans(paramContext, paramString1, paramInt, paramBoolean);
    String str1 = null;
    String str2 = null;
    int j = 0;
    int i = 0;
    if ((paramString2 != null) && (paramString2.length() > 0) && (localSpannableStringBuilder.length() > 0))
    {
      str1 = localSpannableStringBuilder.toString().toLowerCase();
      str2 = paramString2.toLowerCase();
      j = 0;
      i = str2.length();
    }
      final boolean validStr = !TextUtils.isEmpty(str1) && !TextUtils.isEmpty(str2);
    while (validStr)
    {
      j = str1.indexOf(str2, j);
      if (j < 0)
        return localSpannableStringBuilder;
      localSpannableStringBuilder.setSpan(new StyleSpan(1), j, j + i, 33);
      j += i;
    }
      return localSpannableStringBuilder;
  }

  public static SpannableStringBuilder buildStateSpans(Context paramContext, String paramString, int paramInt, boolean paramBoolean)
  {
      SpannableStringBuilder localSpannableStringBuilder = new SpannableStringBuilder();
      int i = 0;

    if (paramString != null)
    {
      Object localObject2 = Html.fromHtml(paramString);
      localSpannableStringBuilder = new SpannableStringBuilder((CharSequence)localObject2);
      URLSpan[] arrayOfURLSpan = (URLSpan[])localSpannableStringBuilder.getSpans(0, ((Spanned)localObject2).length(), URLSpan.class);
      while (true)
      {
        if (i >= arrayOfURLSpan.length)
        {
          if ((paramInt != -1) && (paramInt < ((Spanned)localObject2).length()))
          {
            localSpannableStringBuilder.delete(paramInt, ((Spanned)localObject2).length());
            localObject2 = paramContext.getResources();
            localSpannableStringBuilder.append(((Resources)localObject2).getString(2131165897));
            if (paramBoolean)
            {
              Object localObject1 = ((Resources)localObject2).getString(2131165898);
              localSpannableStringBuilder.append((CharSequence)localObject1);
              localObject2 = new ForegroundColorSpan(((Resources)localObject2).getColor(2131296267));
              localSpannableStringBuilder.setSpan(localObject2, localSpannableStringBuilder.length() - ((String)localObject1).length(), localSpannableStringBuilder.length(), localSpannableStringBuilder.getSpanFlags(localObject2));
            }
          }
          break;
        }
        Object localObject1 = arrayOfURLSpan[i];
        localSpannableStringBuilder.setSpan(new StateURLSpan(((URLSpan)localObject1).getURL()), localSpannableStringBuilder.getSpanStart(localObject1), localSpannableStringBuilder.getSpanEnd(localObject1), localSpannableStringBuilder.getSpanFlags(localObject1));
        localSpannableStringBuilder.removeSpan(localObject1);
        i++;
      }
    }
    return localSpannableStringBuilder;
  }

  public static SpannableStringBuilder buildStateSpans(String paramString)
  {
    return buildStateSpans(null, paramString, -1, false);
  }

  public static SpannableStringBuilder buildStateSpans(String paramString1, String paramString2)
  {
    return buildStateSpans(null, paramString1, -1, paramString2, false);
  }

  private float convertToLocalHorizontalCoordinate(float paramFloat)
  {
    float f = Math.max(0.0F, paramFloat);
    return Math.min(-1 + getWidth(), f);
  }

  private int getLineAtCoordinate(float paramFloat)
  {
    float f = Math.max(0.0F, paramFloat);
    return getLineForVertical((int)Math.min(-1 + getHeight(), f));
  }

  private int getOffsetAtCoordinate(int paramInt, float paramFloat)
  {
    return getOffsetForHorizontal(paramInt, convertToLocalHorizontalCoordinate(paramFloat));
  }

  public int getOffsetForPosition(float paramFloat1, float paramFloat2)
  {
    return getOffsetAtCoordinate(getLineAtCoordinate(paramFloat2), paramFloat1);
  }

  public boolean handleEvent(int paramInt1, int paramInt2, int paramInt3)
  {
    int j;
    if (paramInt3 != 3)
    {
      if (this.mSpannedText != null)
      {
        if (this.mContentRect.contains(paramInt1, paramInt2))
        {
          int i = getOffsetForPosition(paramInt1 - this.mContentRect.left, paramInt2 - this.mContentRect.top);
          if (i >= 0)
          {
            StateURLSpan[] arrayOfStateURLSpan = (StateURLSpan[])this.mSpannedText.getSpans(i, i, StateURLSpan.class);
            if (arrayOfStateURLSpan.length != 0)
            {
              switch (paramInt3)
              {
              case 0:
                this.mClickedSpan = arrayOfStateURLSpan[0];
                this.mClickedSpan.setClicked(true);
                break;
              case 1:
                if ((this.mClickedSpan == arrayOfStateURLSpan[0]) && (this.mClickListener != null))
                  this.mClickListener.onSpanClick(arrayOfStateURLSpan[0]);
                if (this.mClickedSpan == null)
                  break;
                this.mClickedSpan.setClicked(false);
                this.mClickedSpan = null;
              }
              j = 1;
            }
            else
            {
              j = 0;
            }
          }
          else
          {
            j = 0;
          }
        }
        else
        {
          if ((paramInt3 == 1) && (this.mClickedSpan != null))
          {
            this.mClickedSpan.setClicked(false);
            this.mClickedSpan = null;
          }
          j = 0;
        }
      }
      else
        j = 0;
    }
    else
    {
      if (this.mClickedSpan != null)
      {
        this.mClickedSpan.setClicked(false);
        this.mClickedSpan = null;
      }
      j = 1;
    }
    return j > 0;
  }

  public void setPosition(int paramInt1, int paramInt2)
  {
    this.mContentRect = new Rect(paramInt1, paramInt2, paramInt1 + getWidth(), paramInt2 + getHeight());
  }

  public static final class StateURLSpan extends URLSpan
  {
    private int mBgColor;
    private boolean mClicked;
    private boolean mFirstTime = true;

    public StateURLSpan(String paramString)
    {
      super(paramString);
    }

    public void setClicked(boolean paramBoolean)
    {
      this.mClicked = paramBoolean;
    }

    public void updateDrawState(TextPaint paramTextPaint)
    {
      if (this.mFirstTime)
      {
        this.mFirstTime = false;
        this.mBgColor = paramTextPaint.bgColor;
      }
      if (!this.mClicked)
        paramTextPaint.bgColor = this.mBgColor;
      else if (Build.VERSION.SDK_INT < 13)
        paramTextPaint.bgColor = -32768;
      else
        paramTextPaint.bgColor = -13388315;
      paramTextPaint.setColor(paramTextPaint.linkColor);
      paramTextPaint.setUnderlineText(false);
    }
  }

  public static abstract interface SpanClickListener
  {
    public abstract void onSpanClick(URLSpan paramURLSpan);
  }
}
