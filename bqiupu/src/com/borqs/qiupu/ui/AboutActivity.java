package com.borqs.qiupu.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.borqs.common.util.Utilities;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class AboutActivity extends BasicActivity.StatActivity {
	private static final String OFFICIAL_WEBSITE = "<a href="
			+ "\'http://www.borqs.com\'" + ">http://www.borqs.com</a>";
	private static final String OFFICIAL_WEIBO = "<a href=\'"
			+ "http://weibo.com/u/2649053483\'" + ">安卓云分享平台</a>";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bpc_about_ui);

        setupAboutUi(this);
    }

    public static void setupAboutUi(final Activity context) {
		TextView version_info = (TextView) context.findViewById(R.id.version_info);
		version_info.setText(String.format(context.getString(R.string.about_version_info),
				Utilities.getPackageVersionName(context)));

		TextView official_website = (TextView) context.findViewById(R.id.official_website);
		String website = String.format(context.getString(R.string.about_official_website), OFFICIAL_WEBSITE);
		official_website.setText(Html.fromHtml(website));

		TextView official_weibo = (TextView) context.findViewById(R.id.official_weibo);
		String weibo = String.format(context.getString(R.string.about_official_weibo),
				OFFICIAL_WEIBO);
		official_weibo.setText(Html.fromHtml(weibo));
		
		if(QiupuORM.isOpenNewPlatformSettings(context)) {
		    Button login_btn = (Button) context.findViewById(R.id.login_btn);
		    login_btn.setVisibility(View.VISIBLE);
		    login_btn.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                }
            });
		}
	}
    
    public static void setupLaunchUi(Activity context) {
		TextView version_info = (TextView) context.findViewById(R.id.version_info);
		version_info.setText(String.format(context.getString(R.string.about_version_info),
				Utilities.getPackageVersionName(context)));		
	}
}
