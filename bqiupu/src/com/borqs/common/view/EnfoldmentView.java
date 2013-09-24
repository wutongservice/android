package com.borqs.common.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.QiupuComposeActivity;

public class EnfoldmentView extends LinearLayout implements OnClickListener {
    private static final String TAG = "Qiupu.EnfoldmentView";
    private static final boolean debug = true;

    private Context mContext;
    public static boolean mIsClose = true;
    private static final int DURATION = 120;

    private ImageView          mComposerView;
    private RotateAnimation    mPlusRA;
    private RotateAnimation    mCloseRA;

    private ImageView          mCameraView;
    private TranslateAnimation mCameraOutTA;
    private TranslateAnimation mCameraInTA;

//    private static ImageView          mPeopleView;
//    private static TranslateAnimation mPeopleOutTA;
//    private static TranslateAnimation mPeopleInTA;

//    private static ImageView          mLocationView;
//    private static TranslateAnimation mLocationOutTA;
//    private static TranslateAnimation mLocationInTA;

    private ImageView          mAppView;
    private TranslateAnimation mAppOutTA;
    private TranslateAnimation mAppInTA;

    private ImageView          mEditView;
    private TranslateAnimation mEditOutTA;
    private TranslateAnimation mEditInTA;

    private ImageView          mLocationView;
    private TranslateAnimation mLinkOutTA;
    private TranslateAnimation mLinkInTA;

//    private static ImageView          mCardView;
//    private static TranslateAnimation mCardOutTA;
//    private static TranslateAnimation mCardInTA;

    public EnfoldmentView(Context context) {
        this(context, null);
        mContext = context;
        init();
    }

    public EnfoldmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIsClose = true;
        init();
    }

    private void init() {
        LayoutInflater factory = LayoutInflater.from(getContext());

        removeAllViews();
        View convertView = factory.inflate(R.layout.enfoldment_layout, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(convertView);

        setComposeView(convertView);
        setPhotoView(convertView);
//        setPeopleView(convertView);
//        setLocationView(convertView);
        setAppView(convertView);
        setEditView(convertView);
        setLinkView(convertView);
//        setVCardView(convertView);
    }

    private void setComposeView(View convertView) {
        mComposerView = (ImageView) convertView.findViewById(R.id.ivComposer);
        if (mComposerView != null) {
            mComposerView.setOnClickListener(this);
        }
        mPlusRA = new RotateAnimation(0, +360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mPlusRA.setDuration(300);
        LinearInterpolator lin = new LinearInterpolator();
        mPlusRA.setInterpolator(lin);
        mPlusRA.setAnimationListener(mPlusRAListener);

        mCloseRA = new RotateAnimation(0, -360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mCloseRA.setDuration(300);
        mCloseRA.setInterpolator(lin);
        mCloseRA.setAnimationListener(mCloseRAListener);
    }

    private void setPhotoView(View convertView) {
        mCameraView = (ImageView) convertView.findViewById(R.id.ivCamera);
        mCameraView.setOnClickListener(this);
        mCameraOutTA = new TranslateAnimation(Animation.ABSOLUTE, -10.0f, Animation.ABSOLUTE, -10.0f, Animation.ABSOLUTE, 240.0f, Animation.ABSOLUTE, 10.0f);
        mCameraOutTA.setDuration(DURATION);
        mCameraInTA = new TranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 240.0f);
        mCameraInTA.setDuration(DURATION);
    }

//    private void setPeopleView(View convertView) {
//        mPeopleView = (ImageView) convertView.findViewById(R.id.ivPeople);
//        mPeopleView.setOnClickListener(this);
//        mPeopleOutTA = new TranslateAnimation(Animation.ABSOLUTE, -75f, Animation.ABSOLUTE, -10.0f, Animation.ABSOLUTE, 225.0f, Animation.ABSOLUTE, 10.0f);
//        mPeopleOutTA.setDuration(DURATION);
//        mPeopleInTA = new TranslateAnimation(Animation.ABSOLUTE, 10.0f, Animation.ABSOLUTE, -75f, Animation.ABSOLUTE, 10.0f, Animation.ABSOLUTE, 225.0f);
//        mPeopleInTA.setDuration(DURATION);
//    }
//
//    private void setLocationView(View convertView) {
//        mLocationView = (ImageView) convertView.findViewById(R.id.ivLocation);
//        mLocationView.setOnClickListener(this);
//        mLocationOutTA = new TranslateAnimation(Animation.ABSOLUTE, -135f, Animation.ABSOLUTE, -10.0f, Animation.ABSOLUTE, 190f, Animation.ABSOLUTE, 10.0f);
//        mLocationOutTA.setDuration(DURATION);
//        mLocationInTA = new TranslateAnimation(Animation.ABSOLUTE, 10.0f, Animation.ABSOLUTE, -135f, Animation.ABSOLUTE, 10.0f, Animation.ABSOLUTE, 190f);
//        mLocationInTA.setDuration(DURATION);
//    }

    private void setAppView(View convertView) {
        mAppView = (ImageView) convertView.findViewById(R.id.ivApp);
        mAppView.setOnClickListener(this);
        mAppOutTA = new TranslateAnimation(Animation.ABSOLUTE, -105f, Animation.ABSOLUTE, -10.0f, Animation.ABSOLUTE, 225.0f, Animation.ABSOLUTE, 10.0f);
        mAppOutTA.setDuration(DURATION);
        mAppInTA = new TranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, -105f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 225.0f);
        mAppInTA.setDuration(DURATION);
    }

    private void setEditView(View convertView) {
        mEditView = (ImageView) convertView.findViewById(R.id.ivEdit);
        mEditView.setOnClickListener(this);
        mEditOutTA = new TranslateAnimation(Animation.ABSOLUTE, -165f, Animation.ABSOLUTE, -10.0f, Animation.ABSOLUTE, 190f, Animation.ABSOLUTE, 10.0f);
        mEditOutTA.setDuration(DURATION);
        mEditInTA = new TranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, -165f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 190f);
        mEditInTA.setDuration(DURATION);
    }

    private void setLinkView(View convertView) {
        mLocationView = (ImageView) convertView.findViewById(R.id.ivLocation);
        mLocationView.setOnClickListener(this);
        mLinkOutTA = new TranslateAnimation(Animation.ABSOLUTE, -185f, Animation.ABSOLUTE, -10.0f, Animation.ABSOLUTE, 5f, Animation.ABSOLUTE, 10.0f);
        mLinkOutTA.setDuration(DURATION);
        mLinkInTA = new TranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, -185f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 5f);
        mLinkInTA.setDuration(DURATION);
    }

//    private void setVCardView(View convertView) {
//        mCardView = (ImageView) convertView.findViewById(R.id.ivCard);
//        mCardView.setOnClickListener(this);
//        mCardOutTA = new TranslateAnimation(Animation.ABSOLUTE, -215f, Animation.ABSOLUTE, -10.0f, Animation.ABSOLUTE, 5f, Animation.ABSOLUTE, 10.0f);
//        mCardOutTA.setDuration(DURATION);
//        mCardInTA = new TranslateAnimation(Animation.ABSOLUTE, 10.0f, Animation.ABSOLUTE, -215f, Animation.ABSOLUTE, 10.0f, Animation.ABSOLUTE, 5f);
//        mCardInTA.setDuration(DURATION);
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivComposer:
                if(debug)
                Log.d(TAG, "onClick() -----> click menu");
                setClickComposeListener();
                break;
            case R.id.ivCamera:
                startComposeByType(QiupuComposeActivity.FROM_TYPE_CAMERA);
                break;
//            case R.id.ivPeople:
//                startComposeByType("from_people");
//                break;
//            case R.id.ivLocation:
//                startComposeByType("from_location");
//                break;
            case R.id.ivEdit:
                startComposeByType(null);
                break;
            case R.id.ivApp:
                startComposeByType(QiupuComposeActivity.FROM_TYPE_APP);
                break;
            case R.id.ivLocation:
                startComposeByType(QiupuComposeActivity.FROM_TYPE_LOCATION);
                break;
//            case R.id.ivCard:
//                startComposeByType("from_vcard");
//                break;
            default:
                Log.d(TAG, "onClick(): default case, do nothing.");
                break;
        }
        foldEnfoldmentView();
    }

    private void startComposeByType(String type) {
        Intent intent = new Intent(mContext, QiupuComposeActivity.class);
        intent.putExtra("from_type", type);
        mContext.startActivity(intent);
    }

    public void foldEnfoldmentView() {
        if (!mIsClose) {
            foldEnfoldView();
        }
    }

    private void setClickComposeListener() {
        reInflate();

        if(debug)
        Log.d(TAG, "setClickComposeListener  mIsClose = " + mIsClose);
        if (mIsClose) {
            unfoldEnfoldView();
        } else {
            foldEnfoldView();
        }
    }

    private void reInflate() { 
        if (mComposerView == null || mCameraView == null || mAppView == null
                || mEditView == null || mLocationView == null) {
            if(debug)
            Log.d(TAG, "reInflate() -----> call init() method");
            init();
        }
    }

    private void unfoldEnfoldView() {
        mComposerView.startAnimation(mPlusRA);
        setItemViewStatus(mCameraView, mCameraOutTA, View.VISIBLE, 0);
//        setItemViewStatus(mPeopleView, mPeopleOutTA, View.VISIBLE, 10);
//        setItemViewStatus(mLocationView, mLocationOutTA, View.VISIBLE, 20);
        setItemViewStatus(mAppView, mAppOutTA, View.VISIBLE, 10);
        setItemViewStatus(mEditView, mEditOutTA, View.VISIBLE, 20);
        setItemViewStatus(mLocationView, mLinkOutTA, View.VISIBLE, 30);
//        setItemViewStatus(mCardView, mCardOutTA, View.VISIBLE, 60);
    }

    private void foldEnfoldView() {
        mComposerView.startAnimation(mCloseRA);
//        setItemViewStatus(mCardView, mCardInTA, View.GONE, 0);
        setItemViewStatus(mLocationView, mLinkInTA, View.GONE, 0);
        setItemViewStatus(mEditView, mEditInTA, View.GONE, 10);
        setItemViewStatus(mAppView, mAppInTA, View.GONE, 20);
//        setItemViewStatus(mLocationView, mLocationInTA, View.GONE, 40);
//        setItemViewStatus(mPeopleView, mPeopleInTA, View.GONE, 50);
        setItemViewStatus(mCameraView, mCameraInTA, View.GONE, 30);
    }

    private static void setItemViewStatus(ImageView imageView, Animation animation, int visible, int offset) {
        animation.setStartOffset(offset);
        imageView.startAnimation(animation);
        imageView.setVisibility(visible);
    }

    public void clearStaticResource() {
        mIsClose = true;

        mComposerView = null;
        mCameraView = null;
        mAppView = null;
        mEditView = null;
        mLocationView = null;

        mPlusRA = null;
        mCloseRA = null;
        mCameraInTA = null;
        mCameraOutTA = null;
        mAppInTA = null;
        mAppOutTA = null;
        mEditInTA = null;
        mEditOutTA = null;
        mLinkInTA = null;
        mLinkOutTA = null;
    }

    private AnimationListener mPlusRAListener = new AnimationListener() {
        public void onAnimationEnd(Animation animation) {
            Matrix matrix = new Matrix(); 
            matrix.setRotate(45); 
            Bitmap source = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.composer_icn_plus);
            Bitmap resizedBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            mComposerView.setImageBitmap(resizedBitmap);
            if(source != null && !source.isRecycled())
                source.recycle();
            mIsClose = false;
        }
        public void onAnimationRepeat(Animation animation) {}
        public void onAnimationStart(Animation animation) {}
    };

    private AnimationListener mCloseRAListener = new AnimationListener() {
        public void onAnimationEnd(Animation animation) {
            Bitmap source = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.composer_icn_plus);
            mComposerView.setImageBitmap(source);
            mIsClose = true;
        }
        public void onAnimationRepeat(Animation animation) {}
        public void onAnimationStart(Animation animation) {}
    };

}
