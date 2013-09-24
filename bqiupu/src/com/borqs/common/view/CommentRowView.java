package com.borqs.common.view;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-3
 * Time: 上午11:11
 * To change this template use File | Settings | File Templates.
 */
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Checkable;
import com.borqs.common.util.MentionDataPlusOne;
//import com.google.android.apps.plus.service.AvatarCache;
//import com.google.android.apps.plus.service.AvatarCache.OnAvatarChangeListener;
//import com.google.wireless.tacotruck.proto.Data.PlusOneData;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CommentRowView extends View
  implements Checkable
//        , AvatarCache.OnAvatarChangeListener
        , ClickableRect.ClickableRectListener
{
  private static int COMMENT_AUTHOR_BITMAP_DIMENSION;
  private static int COMMENT_SEPARATOR_HEIGHT;
  private static int CONTENT_BOTTOM_MARGIN;
  private static int CONTENT_TOP_MARGIN;
  private static int ICON_RIGHT_MARGIN;
  private static int PLUSONE_CLICKABLE_PADDING_X;
  private static int PLUSONE_CLICKABLE_PADDING_Y;
  private static int PLUSONE_RIGHT_MARGIN;
  private static boolean mInitialized;
  private static Drawable sCheckedStateBackground;
  private static Paint sCommentBackgroundPaint;
  private static TextPaint sContentPaint;
  private static TextPaint sNamePaint;
  private static Bitmap sPlusOneBitmap;
  private static Bitmap sPlusOneByMeBitmap;
  private static TextPaint sPlusOnePaint;
  private static TextPaint sTimePaint;
  private static Bitmap sUserImageBitmap;
  private long mAuthorId;
  private String mAuthorName;
//  private final AvatarCache mAvatarCache;
  private boolean mChecked;
  private ItemClickListener mClickListener;
  private final Set<ClickableItem> mClickableItems = new HashSet();
  private Spanned mContent;
  private ClickableStaticLayout mContentLayout;
  private ClickableItem mCurrentClickableItem;
  private StaticLayout mNameLayout;
  private ClickableRect mPlusOneClickableRect;
//  private Data.PlusOneData mPlusOneData;
    private MentionDataPlusOne mPlusOneData;
  private PlusOnePeopleClickHandler mPlusOnePeopleClickHandler;
  private String mPlusOneText;
  private int mPosition;
  private String mRelativeTime;
  private ClickableUserImage mUserImage;

  public CommentRowView(Context paramContext)
  {
    this(paramContext, null);
  }

  public CommentRowView(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    if (!mInitialized)
    {
      Resources localResources = paramContext.getApplicationContext().getResources();
      sUserImageBitmap = BitmapFactory.decodeResource(localResources, 2130837557);
      sPlusOneBitmap = BitmapFactory.decodeResource(localResources, 2130837676);
      sPlusOneByMeBitmap = BitmapFactory.decodeResource(localResources, 2130837677);
      sCheckedStateBackground = localResources.getDrawable(2130837722);
      sNamePaint = new TextPaint();
      sNamePaint.setAntiAlias(true);
      sNamePaint.setColor(localResources.getColor(2131296265));
      sNamePaint.setTextSize(localResources.getDimension(2131492929));
      sNamePaint.setTypeface(Typeface.DEFAULT_BOLD);
      sTimePaint = new TextPaint();
      sTimePaint.setAntiAlias(true);
      sTimePaint.setColor(localResources.getColor(2131296266));
      sTimePaint.setTextSize(localResources.getDimension(2131492930));
      sContentPaint = new TextPaint();
      sContentPaint.setAntiAlias(true);
      sContentPaint.setColor(localResources.getColor(2131296268));
      sContentPaint.setTextSize(localResources.getDimension(2131492931));
      sContentPaint.linkColor = localResources.getColor(2131296267);
      sPlusOnePaint = new TextPaint();
      sPlusOnePaint.setAntiAlias(true);
      sPlusOnePaint.setColor(localResources.getColor(2131296267));
      sPlusOnePaint.setTextSize(localResources.getDimension(2131492930));
      sCommentBackgroundPaint = new Paint();
      sCommentBackgroundPaint.setColor(localResources.getColor(2131296271));
      sCommentBackgroundPaint.setStyle(Paint.Style.FILL);
      CONTENT_TOP_MARGIN = (int)localResources.getDimension(2131492882);
      CONTENT_BOTTOM_MARGIN = (int)localResources.getDimension(2131492883);
      ICON_RIGHT_MARGIN = (int)localResources.getDimension(2131492884);
      COMMENT_AUTHOR_BITMAP_DIMENSION = (int)localResources.getDimension(2131492891);
      COMMENT_SEPARATOR_HEIGHT = (int)localResources.getDimension(2131492922);
      PLUSONE_RIGHT_MARGIN = (int)localResources.getDimension(2131492892);
      PLUSONE_CLICKABLE_PADDING_X = (int)localResources.getDimension(2131492932);
      PLUSONE_CLICKABLE_PADDING_Y = (int)localResources.getDimension(2131492933);
      mInitialized = true;
    }
//    this.mAvatarCache = AvatarCache.getInstance(paramContext);
  }

  private int measureHeight(int paramInt1, int paramInt2)
  {
    int k;
    if (paramInt1 > 0)
    {
      int j = View.MeasureSpec.getMode(paramInt2);
      int i = View.MeasureSpec.getSize(paramInt2);
      StringBuilder localStringBuilder = new StringBuilder();
      Context localContext = getContext();
      if (j != 1073741824)
      {
        int i2 = getPaddingTop();
        int n = paramInt1 - getPaddingRight() - getPaddingLeft() - COMMENT_AUTHOR_BITMAP_DIMENSION - ICON_RIGHT_MARGIN;
        int i3 = (int)sTimePaint.measureText(this.mRelativeTime);
        int m;
        if (this.mPlusOneData == null)
          m = 0;
        else
          m = this.mPlusOneData.getTotalPlusoneCount();
        if (m <= 0)
        {
          k = 0;
        }
        else
        {
          if (m <= 99)
            this.mPlusOneText = Integer.toString(this.mPlusOneData.getTotalPlusoneCount());
          else
            this.mPlusOneText = getResources().getString(2131165307);
          k = (int)sPlusOnePaint.measureText(this.mPlusOneText) + PLUSONE_RIGHT_MARGIN + sPlusOneBitmap.getWidth();
        }
        this.mUserImage.setRect(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + COMMENT_AUTHOR_BITMAP_DIMENSION, getPaddingTop() + COMMENT_AUTHOR_BITMAP_DIMENSION);
        this.mNameLayout = new StaticLayout(this.mAuthorName, sNamePaint, n - i3, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
        Object[] arrayOfObject = new Object[1];
        arrayOfObject[0] = this.mAuthorName;
        localStringBuilder.append(localContext.getString(2131165402, arrayOfObject));
        localStringBuilder.append(" ");
        arrayOfObject = new Object[1];
        arrayOfObject[0] = this.mRelativeTime;
        localStringBuilder.append(localContext.getString(2131165404, arrayOfObject));
        localStringBuilder.append(" ");
        int i1 = i2 + (this.mNameLayout.getHeight() + CONTENT_TOP_MARGIN);
        if (m > 0)
        {
          this.mClickableItems.remove(this.mPlusOneClickableRect);
          this.mPlusOneClickableRect = new ClickableRect(paramInt1 - getPaddingRight() - k - PLUSONE_CLICKABLE_PADDING_X, i1 - PLUSONE_CLICKABLE_PADDING_Y, k + 2 * PLUSONE_CLICKABLE_PADDING_X, sPlusOneBitmap.getHeight() + 2 * PLUSONE_CLICKABLE_PADDING_Y, this);
          this.mClickableItems.add(this.mPlusOneClickableRect);
        }
        if (this.mContent != null)
        {
          localStringBuilder.append(this.mContent);
          localStringBuilder.append(" ");
          this.mClickableItems.remove(this.mContentLayout);
          this.mContentLayout = new ClickableStaticLayout(this.mContent, sContentPaint, n - k, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false, this.mClickListener);
          this.mContentLayout.setPosition(getPaddingLeft() + COMMENT_AUTHOR_BITMAP_DIMENSION + ICON_RIGHT_MARGIN, getPaddingTop() + this.mNameLayout.getHeight() + CONTENT_BOTTOM_MARGIN);
          this.mClickableItems.add(this.mContentLayout);
          i1 += this.mContentLayout.getHeight();
        }
        k = COMMENT_AUTHOR_BITMAP_DIMENSION;
        k = Math.max(i1, k) + getPaddingBottom();
        if (j == -2147483648)
          k = Math.min(k, i);
      }
      else
      {
        k = i;
      }
      setContentDescription(localStringBuilder.toString());
    }
    else
    {
      k = 0;
    }
    return k;
  }

  private int measureWidth(int paramInt)
  {
    int i = View.MeasureSpec.getMode(paramInt);
    int j = View.MeasureSpec.getSize(paramInt);
    switch (i)
    {
    default:
      i = 0;
      break;
    case -2147483648:
      i = j;
      break;
    case 1073741824:
      i = j;
    }
    return i;
  }

  public void clear()
  {
    this.mPosition = -1;
    this.mUserImage = null;
    this.mNameLayout = null;
    this.mContentLayout = null;
    this.mContent = null;
    this.mPlusOneData = null;
    this.mClickableItems.clear();
  }

  public boolean dispatchTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = 1;
    int j = (int)paramMotionEvent.getX();
    int k = (int)paramMotionEvent.getY();
    Iterator localIterator = null;
    switch (paramMotionEvent.getAction())
    {
    case MotionEvent.ACTION_MOVE:
    default:
      i = 0;
      break;
    case MotionEvent.ACTION_DOWN:
      localIterator = this.mClickableItems.iterator();
    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_CANCEL:
      while (true)
        if (!localIterator.hasNext())
        {
          i = 0;
        }
        else
        {
          ClickableItem localClickableItem = (ClickableItem)localIterator.next();
          if (!localClickableItem.handleEvent(j, k, 0))
            continue;

          this.mCurrentClickableItem = null;
          localIterator = this.mClickableItems.iterator();
          while (true)
            if (!localIterator.hasNext())
            {
              invalidate();
              i = 0;
            }
            else
            {
              ((ClickableItem)localIterator.next()).handleEvent(j, k, i);
              if (this.mCurrentClickableItem == null)
              {
                i = 0;
//                  continue;
              }
              else
              {
                this.mCurrentClickableItem.handleEvent(j, k, 3);
                this.mCurrentClickableItem = null;
                invalidate();
              }
            }
//            this.mCurrentClickableItem = localClickableItem;
//            invalidate();
//            break;
        }
    }
    return i > 0;
  }

  public boolean isChecked()
  {
    return this.mChecked;
  }

  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
//    this.mAvatarCache.registerListener(this);
  }

//  public void onAvatarChanged(long paramLong)
//  {
//    if (this.mUserImage != null)
//      this.mUserImage.onAvatarChanged(paramLong);
//  }

  public void onClickableRectClick(ClickableRect paramClickableRect)
  {
    if ((this.mPlusOneData != null) && (this.mPlusOnePeopleClickHandler != null))
    {
      String str = this.mPlusOneData.getPlusoneId();
      if (!TextUtils.isEmpty(str))
        this.mPlusOnePeopleClickHandler.showPlusOnePeople(str, this.mPlusOneData.getTotalPlusoneCount());
    }
  }

  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
//    this.mAvatarCache.unregisterListener(this);
  }

  protected void onDraw(Canvas paramCanvas)
  {
    int k = 0;
    super.onDraw(paramCanvas);
    if (!this.mChecked)
    {
      paramCanvas.drawRect(0.0F, 0.0F, getWidth(), -1 + getHeight() - COMMENT_SEPARATOR_HEIGHT, sCommentBackgroundPaint);
    }
    else
    {
      sCheckedStateBackground.setBounds(0, 0, getWidth(), getHeight());
      sCheckedStateBackground.draw(paramCanvas);
    }
    Bitmap localBitmap;
    if (this.mUserImage.getBitmap() == null)
      localBitmap = sUserImageBitmap;
    else
      localBitmap = this.mUserImage.getBitmap();
    paramCanvas.drawBitmap(localBitmap, null, this.mUserImage.getRect(), null);
    if (this.mUserImage.isClicked())
      this.mUserImage.drawSelectionRect(paramCanvas);
    int i = (int)sTimePaint.measureText(this.mRelativeTime);
    i = getWidth() - getPaddingRight() - i;
    int n = getPaddingTop();
    paramCanvas.drawText(this.mRelativeTime, i, n - sTimePaint.ascent(), sTimePaint);
    int j = getPaddingLeft() + COMMENT_AUTHOR_BITMAP_DIMENSION + ICON_RIGHT_MARGIN;
    paramCanvas.translate(j, n);
    this.mNameLayout.draw(paramCanvas);
    paramCanvas.translate(-j, -n);
    int i1 = 0;
    n += this.mNameLayout.getHeight() + CONTENT_TOP_MARGIN;
    if (this.mPlusOneData != null)
      k = this.mPlusOneData.getTotalPlusoneCount();
    if (k > 0)
    {
      k = (int)sPlusOnePaint.measureText(this.mPlusOneText);
      k = getWidth() - getPaddingRight() - k;
      paramCanvas.drawText(this.mPlusOneText, k, i1 - sPlusOnePaint.ascent(), sPlusOnePaint);
      int m = 0;
      k -= PLUSONE_RIGHT_MARGIN + sPlusOneBitmap.getWidth();
      if (!this.mPlusOneData.getPlusonedByViewer())
        paramCanvas.drawBitmap(sPlusOneBitmap, m, i1, null);
      else
        paramCanvas.drawBitmap(sPlusOneByMeBitmap, m, i1, null);
    }
    if (this.mContentLayout != null)
    {
      paramCanvas.translate(j, i1);
      this.mContentLayout.draw(paramCanvas);
      paramCanvas.translate(-j, -i1);
    }
  }

  protected void onMeasure(int paramInt1, int paramInt2)
  {
    int i = measureWidth(paramInt1);
    setMeasuredDimension(i, measureHeight(i, paramInt2));
  }

  public void setAuthor(long paramLong, String paramString)
  {
    this.mAuthorId = paramLong;
    this.mAuthorName = paramString;
    this.mUserImage = new ClickableUserImage(this, paramLong, paramString, this.mClickListener);
    this.mClickableItems.add(this.mUserImage);
  }

  public void setChecked(boolean paramBoolean)
  {
    if (paramBoolean != this.mChecked)
    {
      this.mChecked = paramBoolean;
      invalidate();
    }
  }

  public void setClickListener(ItemClickListener paramItemClickListener)
  {
    this.mClickListener = paramItemClickListener;
  }

  public void setContent(String paramString, boolean paramBoolean)
  {
    if ((paramString != null) && (paramString.length() > 0))
    {
      if (paramBoolean)
        paramString = paramString + "...";
      this.mContent = ClickableStaticLayout.buildStateSpans(paramString);
    }
  }

//  public void setPlusOneData(Data.PlusOneData paramPlusOneData)
    public void setPlusOneData(MentionDataPlusOne paramPlusOneData)
  {
    this.mPlusOneData = paramPlusOneData;
  }

  public void setPlusOnePeopleClickHandler(PlusOnePeopleClickHandler paramPlusOnePeopleClickHandler)
  {
    this.mPlusOnePeopleClickHandler = paramPlusOnePeopleClickHandler;
  }

  public void setPosition(int paramInt)
  {
    this.mPosition = paramInt;
  }

  public void setTime(String paramString)
  {
    this.mRelativeTime = paramString;
  }

  public void toggle()
  {
    boolean bool;
    if (this.mChecked)
      bool = false;
    else
      bool = true;
    setChecked(bool);
  }

  public static abstract interface PlusOnePeopleClickHandler
  {
    public abstract void showPlusOnePeople(String paramString, int paramInt);
  }
}
