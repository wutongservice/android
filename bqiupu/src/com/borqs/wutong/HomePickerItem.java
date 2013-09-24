package com.borqs.wutong;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.CircleUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import twitter4j.UserCircle;

/**
 * Created with IntelliJ IDEA.
 * User: yangfeng
 * Date: 13-1-16
 * Time: 下午2:15
 * To change this template use File | Settings | File Templates.
 */
public class HomePickerItem extends SNSItemView {
    private static final String TAG = "HomePickerItem";
    private ImageView icon;
    private TextView title;
    private TextView tv_member_count;

    private UserCircle mCircle;

    public UserCircle getCircle() {
        return mCircle;
    }

    public HomePickerItem(Context context) {
        super(context);
        init();
    }

    @Override
    public String getText() {
        return null;
    }

    public HomePickerItem(Context context, UserCircle circle) {
        super(context);

        mCircle = circle;
        init();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {

        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();

        View convertView = factory.inflate(R.layout.wutong_home_picker_item, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                (int)mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(convertView);

        icon = (ImageView) convertView.findViewById(R.id.id_circle_icon);
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)icon.getLayoutParams();
        params.height = (int)mContext.getResources().getDimension(R.dimen.stream_row_size_profile_icon);
        params.width = (int)mContext.getResources().getDimension(R.dimen.stream_row_size_profile_icon);
        title = (TextView) convertView.findViewById(R.id.id_circle_name);
        tv_member_count = (TextView) convertView.findViewById(R.id.tv_member_count);

        setUI();
    }

    public void setCircle(UserCircle user) {
        mCircle = user;
        setUI();
    }

    private void setUI() {
        if (mCircle != null) {
            title.setText(CircleUtils.getCircleName(mContext, mCircle.circleid, mCircle.name));
            tv_member_count.setText(String.format(mContext.getString(R.string.str_people), mCircle.memberCount));

            if(UserCircle.CIRLCE_TYPE_PUBLIC == mCircle.type) {
                shootImageRunner(mCircle.profile_image_url, icon,R.drawable.default_public_circle);
            }else if(UserCircle.CIRCLE_TYPE_EVENT == mCircle.type) {
                icon.setImageResource(R.drawable.list_event);
            }
        }
        else {
            Log.i(TAG, "do nothing while data is null");
        }

    }

    private void shootImageRunner(String photoUrl,final ImageView img,int imageRes) {
        // Get singletone instance of ImageLoader
        ImageLoader imageLoader = QiupuApplication.getApplication(img.getContext()).getImageLoader();
        // Creates display image options for custom display task (all options are optional)
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading().showStubImage(imageRes).showImageForEmptyUri(imageRes)
                .cacheInMemory()
                .cacheOnDisc()
                .loadFromWeb(!QiupuORM.isDataFlowAutoSaveMode(getContext()))
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565)
//				           .displayer(new RoundedBitmapDisplayer(5))
                .build();
        // Load and display image asynchronously
        imageLoader.displayImage(photoUrl, img,options,null );
    }
}
