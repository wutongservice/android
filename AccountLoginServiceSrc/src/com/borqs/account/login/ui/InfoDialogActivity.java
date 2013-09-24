package com.borqs.account.login.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.borqs.account.login.R;

public class InfoDialogActivity extends Activity{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.account_infodialog);
        
        Button btn = (Button)this.findViewById(R.id.acl_btnOK);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finishImpl();
            }
        });
    }

    protected void finishImpl() {
        super.finish();        
    }

    @Override
    public void onBackPressed() {
        finishImpl();
    }

    @Override
    public void finish() {
    }
    
    
}
