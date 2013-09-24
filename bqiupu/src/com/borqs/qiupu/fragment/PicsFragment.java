package com.borqs.qiupu.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class PicsFragment extends BasicFragment {
	private final String path;
	private int position;

	public PicsFragment() {
		super();
		this.path = "";
	}

	public PicsFragment(int position, String path) {
		super();
		this.path = path;
		this.position = position;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pics_fragment, null);
		ImageView img = (ImageView) view
				.findViewById(R.id.img);
		shootImageRunner(path,img);
		return view;
	}

	private void shootImageRunner(String photoUrl,ImageView img) {
		ImageRun photo_1 = new ImageRun(null, photoUrl, 0);
		photo_1.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
//		photo_1.SetOnImageRunListener(new ImageRun.OnImageRunListener() {
//
//			@Override
//			public void onLoadingFinished() {
//				Message msg = mHandler.obtainMessage(LOAD_IMAGE_SUCCESS);
//                msg.sendToTarget();
//			}
//
//			@Override
//			public void onLoadingFailed() {
//				Message msg = mHandler.obtainMessage(LOAD_IMAGE_FAILED);
//                msg.sendToTarget();
//			}});
		photo_1.addHostAndPath = true;
//		photo_1.need_scale = true;
		photo_1.noimage = true;
		photo_1.isRoate = true;
		photo_1.setImageView(img);
		photo_1.post(null);
	}
}
