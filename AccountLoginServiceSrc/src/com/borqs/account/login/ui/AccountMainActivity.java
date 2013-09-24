package com.borqs.account.login.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.borqs.account.login.R;
import com.borqs.account.login.service.AccountService;
import com.borqs.account.login.service.AccountService.AccountSessionData;
import com.borqs.account.login.service.AccountService.IOnAccountLogin;

/**
 * a test class used for AccountCommon
 * @author linxh
 *
 */
public class AccountMainActivity extends Activity implements IOnAccountLogin, View.OnClickListener{
    private AccountService mLoginService;    
    private AccountSessionData mSessionData;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_main);
        Button btn = (Button)this.findViewById(R.id.button1);
        btn.setOnClickListener(this);
        
        mLoginService = new AccountService(this);
        mLoginService.login(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1){
            Thread myThread = new Thread(){                
                public void run(){
                    AccountService acnService = new AccountService(getApplicationContext());
                    acnService.loadData(new AccountService.IOnAccountDataLoad() {
                        
                        @Override
                        public void onAccountDataLoad(boolean isSuccess, AccountSessionData data) {
                            // if null pass into AccountService constructor,
                            // you musn't call it's login and loadData function
                            AccountService service = new AccountService(null);
                            Log.i("syncml", "get session id:" + service.getSessionId());
                            Thread.currentThread().interrupt();
                        }
                    });
                    while (!isInterrupted()){                        
                    }
                }
            };
            
            myThread.start();
        }
    }
    
    public void onAccountLogin(boolean isSuccess, AccountSessionData data){
        mSessionData = data;
        if (isSuccess){
            Log.i("syncml", "onAccountLogin:" + mSessionData.getSessionId());
        }
    }        
}