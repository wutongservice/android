package com.borqs.qiupu.cache;

import android.view.View;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;

/**
 * Created with IntelliJ IDEA.
 * User: yangfeng
 * Date: 13-2-17
 * Time: 下午3:45
 * To change this template use File | Settings | File Templates.
 */
public class NoCachedImageRun extends ImageRun {
    public NoCachedImageRun(String url) {
        super(null, url, 0);
        addHostAndPath = true;
        need_scale = false;
        noimage = true;
        forceweb = true;
        fromlocal = false;
    }

    @Override
    public boolean setImageView(View view) {
        iaminCache = false;
        imgView = view;
        imgView.setTag(url);

        //get from local, if has in local,
        final String localpath = QiupuHelper.isImageExistInPhone(url, addHostAndPath);
        if (localpath != null) {
            // todo: delete the path
        }

        iaminCache = false;
        //set as no image firstly, this will remove the pre-image
        if (noimage == false) {
            imgView.post(new Runnable() {
            public void run() {
                    final int res;
                    if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_USER) {
                        res = R.drawable.default_user_icon;
                    } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_BOOK) {
                        res = R.drawable.default_book;
                    } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT) {
                        res = R.drawable.photo_transparent;
                    } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_Music) {
                        res = R.drawable.music_default;
                    } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_APK) {
                        res = R.drawable.default_app_icon;
                    } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_LINK) {
                        res = R.drawable.list_public;
                    } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_GREEN) {
                        res = R.drawable.wutong_screen_bg_green;
                    } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_BLUE) {
                        res = R.drawable.wutong_screen_bg_blue;
                    } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_RED) {
                        res = R.drawable.wutong_screen_bg_red;
                    } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_YELLOW) {
                        res = R.drawable.wutong_screen_bg_yellow;
                    } else {
                        res = R.drawable.photo_transparent;
                    }

                    setImageBmp(res);
                }
            });
        }

        // always return false as we don't care cached data
        return false;
    }
}
