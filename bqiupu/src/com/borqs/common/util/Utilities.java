/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.common.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.Log;
import com.borqs.qiupu.QiupuConfig;

import java.lang.reflect.Method;

//import android.graphics.TableMaskFilter;
//import android.os.SystemProperties;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
final public class Utilities {
    private static final String TAG = "Qiupu.Utilities";

    private static final boolean TEXT_BURN = false;

    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    private static int sIconTextureWidth = -1;
    private static int sIconTextureHeight = -1;

    private static int sDesktopIconWidth = -1;
    private static int sDesktopIconHeight = -1;
//    private static int sDesktopIconTextureWidth = -1;
//    private static int sDesktopIconTextureHeight = -1;
    private static int sDesktopIconExtendWidth = -1;
    private static int sDesktopIconExtendHeight = -1;

//    private static int sTitleMargin = -1;
    private static float sBlurRadius = -1;
//    private static Rect sIconTextureRect;

//    private static final Paint sPaint = new Paint();
    private static final Paint sBlurPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sDisabledPaint = new Paint();
//    private static final Rect sBounds = new Rect();
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

//    private static float mBubbleWidth2D; //used just for 2d
    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }

    static void clearStaticData(){
        sIconWidth = -1;
        sIconHeight = -1;
        sIconTextureWidth = -1;
        sIconTextureHeight = -1;

        sDesktopIconWidth = -1;
        sDesktopIconHeight = -1;
//        sDesktopIconTextureWidth = -1;
//        sDesktopIconTextureHeight = -1;

        sDesktopIconExtendWidth = -1;
        sDesktopIconExtendHeight = -1;

//        sTitleMargin = -1;
        sBlurRadius = -1;
//      mBubbleWidth2D = -1.f;
    }

//    static Bitmap centerToFit(Bitmap bitmap, int width, int height, Context context) {
////      if(Launcher.LOGD)Log.d(TAG, "centerToFit w:"+width+" h:"+height);
//        final int bitmapWidth = bitmap.getWidth();
//        final int bitmapHeight = bitmap.getHeight();
//
//        if (bitmapWidth < width || bitmapHeight < height) {
//            int color = context.getResources().getColor(R.color.window_background);
//
//            Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width : bitmapWidth,
//                    bitmapHeight < height ? height : bitmapHeight, Bitmap.Config.RGB_565);
//            centered.setDensity(bitmap.getDensity());
//            Canvas canvas = new Canvas(centered);
//            canvas.drawColor(color);
//            canvas.drawBitmap(bitmap, (width - bitmapWidth) / 2.0f, (height - bitmapHeight) / 2.0f,
//                    null);
//
//            bitmap = centered;
//        }
//
//        return bitmap;
//    }

    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    /**
     * Returns a bitmap suitable for the all apps view.  The bitmap will be a power
     * of two sized ARGB_8888 bitmap that can be used as a gl texture.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (null != bitmap && bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }

            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();

            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // It's small, use the size they gave us.
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            if (false) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);

//            Log.d(TAG, "createIconBitmap icon:"+icon+" sIconWidth:"+sIconWidth+"  width:"+width+" height:"
//                      +height+" sourceWidth:"+sourceWidth+" sourceHeight:"+sourceHeight
//                      +" Bounds:"+icon.getBounds());

            icon.setBounds(sOldBounds);
            return bitmap;
        }
    }

    static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth, int destHeight,
            boolean pressed, Bitmap src) {
//          if(Launcher.LOGD)Log.d(TAG, "drawSelectedAllAppsBitmap w:"+destWidth+" h:"+destHeight);
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                // We can't have gotten to here without src being initialized, which
                // comes from this file already.  So just assert.
                //initStatics(context);
                throw new RuntimeException("Assertion failed: Utilities not initialized");
            }

            dest.drawColor(0, PorterDuff.Mode.CLEAR);

            int[] xy = new int[2];
            Bitmap mask = src.extractAlpha(sBlurPaint, xy);

            float px = (destWidth - src.getWidth()) / 2;
            float py = (destHeight - src.getHeight()) / 2;
            dest.drawBitmap(mask, px + xy[0], py + xy[1],
                    pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);

            mask.recycle();
        }
    }

    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            } else {
                return createIconBitmap(new BitmapDrawable(bitmap), context);
            }
        }
    }

    static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
            final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(disabled);

            canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);

            return disabled;
        }
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable, Context context){
       //if(Launcher.LOGD)Log.d(TAG, "getBitmapFromDrawable ");
       if (sIconWidth == -1) {
            initStatics(context);
       }

       Bitmap bitmap = Bitmap.createBitmap(sDesktopIconWidth,sDesktopIconHeight, Bitmap.Config.ARGB_8888);
       Canvas canvas = new Canvas(bitmap);
       drawable.setBounds(0, 0, sDesktopIconWidth, sDesktopIconHeight);
       drawable.draw(canvas);
       return bitmap;
    }

//    public static Bitmap getExtendBitmapFromDrawable(Drawable drawable, Context context){
////       if(Launcher.LOGD)Log.d(TAG, "getBitmapFromDrawable ");
//       if (sIconWidth == -1) {
//            initStatics(context);
//       }
//
//       Bitmap bitmap = Bitmap.createBitmap(sDesktopIconExtendWidth,sDesktopIconExtendHeight, Bitmap.Config.ARGB_8888);
//       Canvas canvas = new Canvas(bitmap);
//       drawable.setBounds(0, 0, sDesktopIconExtendWidth, sDesktopIconExtendHeight);
//       drawable.draw(canvas);
//       return bitmap;
//    }

//    public static Bitmap getExtendBitmapFromDrawableForAllApps(Drawable drawable, Context context){
//        if(Launcher.LOGD)Log.d(TAG, "getExtendBitmapFromDrawableForAllApps ");
//        if(-1 == sIconWidth){
//         initStatics(context);
//        }
//
//         Bitmap bitmap = Bitmap.createBitmap(sIconWidth+10,sIconHeight+10, Bitmap.Config.ARGB_8888);
//         Canvas canvas = new Canvas(bitmap);
//         drawable.setBounds(0, 0, sIconWidth+10, sIconHeight+10);
//         drawable.draw(canvas);
//
//         return bitmap;
//     }

//    static int[] bg = {
//        R.drawable.bg_0,
//        R.drawable.bg_1,
//        R.drawable.bg_2,
//        R.drawable.bg_3,
//        R.drawable.bg_4,
//        R.drawable.bg_5,
//        R.drawable.bg_6,
//        R.drawable.bg_7,
//        R.drawable.bg_8,
//        R.drawable.bg_9,
//        R.drawable.bg_10,
//        R.drawable.bg_11,
//    };
//    static int use_default_icon = -1;
//    public static Drawable createAllAppIconWithBg(Bitmap iconBitmap, Context context){
//        if(-1 == sIconWidth){
//            initStatics(context);
//        }
//
//        final int extendDimension = 10;
//        final int extendAppIconWidth  = sIconWidth+extendDimension;
//        final int extendAppIconHeight = sIconHeight+extendDimension;
//
////        if(Launcher.LOGD)Log.d(TAG, "createAllAppIconWithBg extendAppIconWidth:"+extendAppIconWidth+" extendAppIconHeight:"+extendAppIconHeight+" sIconTextureWidth:"+sIconTextureWidth+" sIconTextureHeight:"+sIconTextureHeight);
//
//        if(use_default_icon == -1)
//        {
//            use_default_icon = SystemProperties.getInt("use_default_icon", 0);
//        }
//
//        int res = R.drawable.all_apps_icon_bg;
//        if(use_default_icon > 0)
//        {
//            int randam = (int)(Math.random()*10000);
//            randam = randam % 12;
//            res = bg[randam];
//        }
//        Drawable bgDrawable = context.getResources().getDrawable(res);
//
//        Bitmap bitmap = Bitmap.createBitmap(extendAppIconWidth, extendAppIconHeight, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        if(SystemProperties.getInt("use_background_icon", 1) != 1)
//        {
//            bgDrawable.setBounds(0, 0, extendAppIconWidth, extendAppIconHeight);
//            bgDrawable.draw(canvas);
//        }
//
//        canvas.drawBitmap(iconBitmap, (extendAppIconWidth-sIconTextureWidth)/2, (extendAppIconHeight-sIconTextureHeight)/2, null);
//        Drawable draw = new BitmapDrawable(bitmap);
//        ((BitmapDrawable)draw).setTargetDensity(context.getResources().getDisplayMetrics());
//
//       return draw;
//     }


    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final float density = resources.getDisplayMetrics().density;

        sIconWidth = sIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth + 2;

        sDesktopIconWidth = sDesktopIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
//        sDesktopIconTextureWidth = sDesktopIconTextureHeight = roundToPow2(sDesktopIconWidth);
//        sDesktopIconExtendHeight = sDesktopIconExtendWidth = (int) resources.getDimension(R.dimen.app_icon_extend_size);

        final int left = (sIconTextureWidth-sIconWidth)/2;
        final int top = (int)(sBlurRadius) + 1;
//        sIconTextureRect = new Rect(left, top, left+sIconWidth, top+sIconHeight);

        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xff69b2dc);
//        sGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        sGlowColorFocusedPaint.setColor(0xff69b2dc);
//        sGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }

//    static class BubbleText {
//        private static final int MAX_LINES = 2;
//
//        private final TextPaint mTextPaint;
//
//        private final RectF mBubbleRect = new RectF();
//
//        private final float mTextWidth;
//        private final int mLeading;
//        private final int mFirstLineY;
//        private final int mLineHeight;
//
//        private final int mBitmapWidth;
//        private final int mBitmapHeight;
//        private final int mDensity;
//
//        BubbleText(Context context) {
//            final Resources resources = context.getResources();
//
//            final DisplayMetrics metrics = resources.getDisplayMetrics();
//            final float scale = metrics.density;
//            mDensity = metrics.densityDpi;
//
//            final float paddingLeft = 2.0f * scale;
//            final float paddingRight = 2.0f * scale;
//            final float cellWidth = resources.getDimension(R.dimen.title_texture_width);
//
//            RectF bubbleRect = mBubbleRect;
//            bubbleRect.left = 0;
//            bubbleRect.top = 0;
//            bubbleRect.right = (int) cellWidth;
//
//            mTextWidth = cellWidth - paddingLeft - paddingRight;
//
//            TextPaint textPaint = mTextPaint = new TextPaint();
//            textPaint.setTypeface(Typeface.DEFAULT);
//            textPaint.setTextSize(13*scale);
//            textPaint.setColor(0xffffffff);
//            textPaint.setAntiAlias(true);
//            if (TEXT_BURN) {
//                textPaint.setShadowLayer(8, 0, 0, 0xff000000);
//            }
//
//            float ascent = -textPaint.ascent();
//            float descent = textPaint.descent();
//            float leading = 0.0f;//(ascent+descent) * 0.1f;
//            mLeading = (int)(leading + 0.5f);
//            mFirstLineY = (int)(leading + ascent + 0.5f);
//            mLineHeight = (int)(leading + ascent + descent + 0.5f);
//
//            mBitmapWidth = (int)(mBubbleRect.width() + 0.5f);
//            mBitmapHeight = roundToPow2((int)((MAX_LINES * mLineHeight) + leading + 0.5f));
//
//            mBubbleRect.offsetTo((mBitmapWidth-mBubbleRect.width())/2, 0);
//
//            if (false) {
//                Log.d(TAG, "mBitmapWidth=" + mBitmapWidth + " mBitmapHeight="
//                        + mBitmapHeight + " w=" + ((int)(mBubbleRect.width() + 0.5f))
//                        + " h=" + ((int)((MAX_LINES * mLineHeight) + leading + 0.5f)));
//            }
//        }
//
//        /** You own the bitmap after this and you must call recycle on it. */
//        Bitmap createTextBitmap(String text) {
//            Bitmap b = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ALPHA_8);
//            b.setDensity(mDensity);
//            Canvas c = new Canvas(b);
//
//            StaticLayout layout = new StaticLayout(text, mTextPaint, (int)mTextWidth,
//                    Alignment.ALIGN_CENTER, 1, 0, true);
//            int lineCount = layout.getLineCount();
//            if (lineCount > MAX_LINES) {
//                lineCount = MAX_LINES;
//            }
//            //if (!TEXT_BURN && lineCount > 0) {
//                //RectF bubbleRect = mBubbleRect;
//                //bubbleRect.bottom = height(lineCount);
//                //c.drawRoundRect(bubbleRect, mCornerRadius, mCornerRadius, mRectPaint);
//            //}
//            for (int i=0; i<lineCount; i++) {
//                //int x = (int)((mBubbleRect.width() - layout.getLineMax(i)) / 2.0f);
//                //int y = mFirstLineY + (i * mLineHeight);
//                final String lineText = text.substring(layout.getLineStart(i), layout.getLineEnd(i));
//                int x = (int)(mBubbleRect.left
//                        + ((mBubbleRect.width() - mTextPaint.measureText(lineText)) * 0.5f));
//                int y = mFirstLineY + (i * mLineHeight);
//                c.drawText(lineText, x, y, mTextPaint);
//            }
//
//            return b;
//        }
//
//        private int height(int lineCount) {
//            return (int)((lineCount * mLineHeight) + mLeading + mLeading + 0.0f);
//        }
//
//        int getBubbleWidth() {
//            return (int)(mBubbleRect.width() + 0.5f);
//        }
//
//        int getMaxBubbleHeight() {
//            return height(MAX_LINES);
//        }
//
//        int getBitmapWidth() {
//            return mBitmapWidth;
//        }
//
//        int getBitmapHeight() {
//            return mBitmapHeight;
//        }
//    }

    /** Only works for positive numbers. */
    static int roundToPow2(int n) {
        int orig = n;
        n >>= 1;
        int mask = 0x8000000;
        while (mask != 0 && (n & mask) == 0) {
            mask >>= 1;
        }
        while (mask != 0) {
            n |= mask;
            mask >>= 1;
        }
        n += 1;
        if (n != orig) {
            n <<= 1;
        }
        return n;
    }

    public static String getPackageVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String versionName = info.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    // encoding version code and version name to return with the format of
    // code (name)
    public static String getPackageVersionString(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String versionName = String.format("%1$s (%2$s)" ,info.versionCode, info.versionName);
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    private static String sdkversion;
    private static String platform;
    public static String getSdkVersion() {
        if (null == sdkversion) {
            sdkversion = getplatform("ro.build.version.sdk");
            if (QiupuConfig.LOGD) Log.d(TAG, "sdkversion:" + sdkversion);
        }
        return sdkversion;
    }
    public static String getplatform(String key) {
        if (null == platform) {
            try {
                Method getInMethod = Class.forName("android.os.SystemProperties").getMethod("get", new Class[]{String.class});
                platform = (String) getInMethod.invoke(null, new Object[]{key});
            } catch (Exception e) {
            }
        }

        return platform;
    }

}
