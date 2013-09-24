package com.borqs.account.login.util;

import android.os.AsyncTask;

public class SimpleTask extends AsyncTask<Runnable, Void, Runnable>{

    private boolean bresult;
    
    public boolean getBresult() {
        return bresult;
    }

    public void setBresult(boolean bresult) {
        this.bresult = bresult;
    }

    @Override
    protected Runnable doInBackground(Runnable... params) {
        params[0].run();
        return params[1];
    }

    @Override
    protected void onPostExecute(Runnable result) {
        if(isCancelled()){
            return ;
        }
        result.run();
    }
    
}
