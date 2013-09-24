package com.borqs.appupdate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TestActivity", "onCreate, " + this);
        super.onCreate(savedInstanceState);
        
        Button button1 = new Button(this);
        button1.setText("Button1");
        
        this.setContentView(button1);
        //this.addContentView(button1, null);
        /*
        Button button2 = new Button(this);
        button2.setText("Button2");
        this.addContentView(button2, LayoutParams.);
        */
        
        button1.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent("android.intent.action.BORQSAPP_VERSION_UPDATE");
				intent.putExtra("package",   "com.borqs.qiupu");
				intent.putExtra("version_code",           "140");
				intent.putExtra("force_install",          "false");
				TestActivity.this.startService(intent);
				
			}
		});
        
      
		
    }
}
