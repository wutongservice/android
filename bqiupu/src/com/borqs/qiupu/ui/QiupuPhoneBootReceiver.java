package com.borqs.qiupu.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.QiupuService;

import java.io.File;

public class QiupuPhoneBootReceiver extends BroadcastReceiver{
	private static final String TAG = "QiupuPhoneBootReceiver";
	private QiupuORM orm;

	@Override
	public void onReceive(Context context, Intent intent) {
		orm = QiupuORM.getInstance(context);
		
		final String action = intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            /*if(!checkAccountLogin()) {
                int pid = android.os.Process.myPid();
                Log.d(TAG, "no valid session, no need start the service, kill the process="+pid);                
                android.os.Process.killProcess(pid);
                Log.d(TAG, "after kill qiupu process="+pid);
            } else {*/
            	Log.d(TAG, "boot complete, start Qiupu service");            	
                Intent in = new Intent(context, QiupuService.class);   
                in.setAction(QiupuService.INTENT_QP_PHONE_BOOT);
                context.startService(in);
            //}
            //start notification delay
            try{
            	InformationUtils.getInforByDelay(context, 20 * QiupuConfig.A_SECOND);
            }catch(Exception ne){}
        } 
        else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {            
        	Log.d(TAG, "remove all qiupu data");
	        	
        	File files = new File("/data/data/com.borqs.qiupu/files/");
        	if(files.exists()) {
        		try 
        		{
					Runtime.getRuntime().exec("rm /data/data/com.borqs.qiupu/files/* ");
					Log.d(TAG, "removed files");
				} catch (Exception e) 
				{					
					Log.e(TAG, "fail to delete files="+e.getMessage());
				}
        	}
        } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(intent.getAction())) {
            Log.d(TAG, "has storage now");
        } else if("oms.action.MASTERRESET".equals(action)) {            
        } else if ("android.permission.MASTER_CLEAR".equals(action)) {
//        } else if ("com.borqs.intent.action.BORQSID_READY".equalsIgnoreCase(action)) {
//            ProfileActionGatewayActivity.setBorqsPlusEntities(context);
        }
        
	}
	
//	private boolean checkAccountLogin() {
//    	mAccount = orm.getAccount();
//		if(mAccount == null || StringUtil.isEmpty(mAccount.session_id)){
//			return false;
//		}
//		
//		return true;
//	}
}
