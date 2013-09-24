package com.borqs.common.quickaction;


import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.QiupuComposeActivity;
import twitter4j.ApkBasicInfo;
import twitter4j.ApkResponse;


public class QuickLauncher {	
	private final static String TAG="Launcher.QuickLauncher";
	public  static QuickAction qa ;
    private BasicActivity mLauncher;
    public static boolean ChangedShortcut = true;
    private Object mObject = new Object();
    
    public void popupApplicationLauncher(final BasicActivity launcher, final View mCategoryView, final ApkResponse apk)
    {
    	mLauncher = launcher;    	
    	
    	if(qa != null)
    	{
    		qa.dismiss();
    		qa = null;
    	}
    	
    	synchronized(mObject)
		{		  
	    	qa = new QuickAction(mCategoryView);
	    	qa.setOnDismissListener( new PopupWindow.OnDismissListener()
	    	{
				public void onDismiss() {
					synchronized(mObject)
					{	
					    qa = null;
					}
				}    		
	    	});
	    		         
	    	qa.setAnimStyle(QuickAction.ANIM_GROW_FROM_RIGHT);
	    	
	    	final ActionItem showapk = new ActionItem();
	    	showapk.setTitle(launcher.getString(R.string.string_show));    			
	    	showapk.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), android.R.drawable.ic_menu_info_details));
	    	showapk.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					
					if(mLauncher.getORM().isShowMarketAppPage())
					{
						Intent intent = new Intent(Intent.ACTION_VIEW);						
						intent.setData(Uri.parse("http://market.android.com/details?id="+apk.packagename));
						try {
							mLauncher.startActivity(intent);
						}catch(Exception ne){}
					}
					else
					{
                        IntentUtil.startApkDetailActivity(mLauncher, ApkBasicInfo.APKStatus.STATUS_NEED_DOWNLOAD, apk);
					}
		            
		            dissmissQuickAction();
				}
			});			
			qa.addActionItem(showapk);
			
			final ActionItem share = new ActionItem();
			share.setTitle(launcher.getString(R.string.status_share));    			
			share.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), android.R.drawable.ic_menu_share));
			share.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
                    if (mLauncher.ensureAccountLogin()) {
                        QiupuComposeActivity.startShareIntent(mLauncher, apk);
                    }
		            dissmissQuickAction();
				}
			});
			
			qa.addActionItem(share);
			
			final ActionItem favor = new ActionItem();
			    			
			if(apk.isFavorite)
			{
			    favor.setTitle(launcher.getString(R.string.remove_favorites));
			    favor.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.tab_icon_collection_selected));
			}
			else
			{
				favor.setTitle(launcher.getString(R.string.add_favorites));
				favor.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.tab_icon_collection_normal));
			}
			favor.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {	
					if(apk.isFavorite == false)
					{
					    mLauncher.addFavorites(apk);
					}
					else
					{
						mLauncher.removeFavorites(apk);
					}
		            dissmissQuickAction();
				}
			});
			
			qa.addActionItem(favor);
			
			final ActionItem download = new ActionItem();
			download.setTitle(launcher.getString(R.string.apk_download));    			
			download.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.menu_icon_save));
			download.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					
					if(canDownload(mLauncher, apk))
					{
						if(apk.packagename.equals(QiupuConfig.APP_PACKAGE_NAME))
    					{
    						apk.apkurl = QiupuHelper.getAPKURL(apk.packagename);
    					}
						Intent service = new Intent(mLauncher, QiupuService.class);
						service.setAction(QiupuService.INTENT_QP_DOWNLOAD_APK);
						service.putExtra(QiupuMessage.BUNDLE_APKINFO, apk);
						mLauncher.startService(service);
					}
					
		            dissmissQuickAction();
				}
			});			
			qa.addActionItem(download);
			
	    	qa.show();
		}
    }
    
    public boolean canDownload(Context mContext, ApkResponse apk)
	{
		String esStatus = Environment.getExternalStorageState();		
		if (esStatus.equals(Environment.MEDIA_MOUNTED) == false) 
		{
			Toast.makeText(mContext, R.string.no_sdcard_no_download, Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(QiupuHelper.hasEnoughspace(apk.apksize) == false)
		{
			Toast.makeText(mContext, R.string.no_enough_space_sdcard_download, Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}
  
    public static void dissmissQuickAction()
    {
    	if(qa != null)
    	{
    		try{
    		    qa.dismiss();
    		}catch(Exception ne){}
    		qa = null;
    	}
    }
    
//    public void popupPeopleLauncher(final BasicActivity launcher, final View mCategoryView, final QiupuUser user)
//    {
//    	mLauncher = launcher;    	
//    	
//    	if(qa != null)
//    	{
//    		qa.dismiss();
//    		qa = null;
//    	}
//    	
//    	synchronized(mObject)
//		{		  
//	    	qa = new QuickAction(mCategoryView);
//	    	qa.setOnDismissListener( new PopupWindow.OnDismissListener()
//	    	{
//				public void onDismiss() {
//					synchronized(mObject)
//					{	
//					    qa = null;
//					}
//				}    		
//	    	});
//	    		         
//	    	qa.setAnimStyle(QuickAction.ANIM_GROW_FROM_RIGHT);
//	    	if(user != null && (!SNSItemView.isEmpty(user.contact_phone1)
//                    || !SNSItemView.isEmpty(user.contact_phone2)
//                    || !SNSItemView.isEmpty(user.contact_phone3))){
//	    			final ActionItem phone = new ActionItem();
//	    			phone.setTitle(launcher.getString(R.string.status_share));    			
//	    			phone.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.sym_action_call));
//	    			phone.setOnClickListener(new View.OnClickListener() {			
//	    				public void onClick(View v) {
//	    					showPhoneDialog(user);
//	    					
//	    		            dissmissQuickAction();
//	    				}
//	    			});
//	    			
//	    			qa.addActionItem(phone);
//	            }
//	    	
//	    	if(user != null && (SNSItemView.isEmail(user.contact_email1)
//                    || SNSItemView.isEmail(user.contact_email2)
//                    || SNSItemView.isEmail(user.contact_email3))){
//	    		final ActionItem phone = new ActionItem();
//    			phone.setTitle(launcher.getString(R.string.status_share));    			
//    			phone.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.sym_action_email));
//    			phone.setOnClickListener(new View.OnClickListener() {			
//    				public void onClick(View v) {
//    					try
//    		            {
//    		                List<String> listaction = getEmailList(user);
//    		                String items[] = new String[listaction.size()];
//    		                for(int i=0;i<listaction.size();i++)
//    		                {
//    		                    items[i] = listaction.get(i);
//    		                }
//    		                
//    		                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//    		                emailIntent.setType("plain/text");
//    		                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, items);
//    		                
//    		                String mySubject = mLauncher.getString(R.string.email_comefrom_qiupu);
//    		                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mySubject);
//    		                String myBodyText = "";
//    		                
//    		                mLauncher.startActivity(emailIntent);		
//    		            }
//    		            catch(ActivityNotFoundException ne)
//    		            {
//    		                Toast.makeText(mLauncher, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
//    		            }
//    					
//    		            dissmissQuickAction();
//    				}
//    			});
//    			
//    			qa.addActionItem(phone);
//	    	}
//	    	
//			
//	    	qa.show();
//		}
//    }
    
//    private List<String> getActionList(final QiupuUser user)
//    {
//        List<String> listaction= new ArrayList<String>();							
//        if(SNSItemView.isEmpty(user.contact_phone1) == false)
//        {
//            listaction.add(mLauncher.getString(R.string.phonebook_call) + " " + user.contact_phone1);
//            listaction.add(mLauncher.getString(R.string.phonebook_message) + " " + user.contact_phone1);
//        }
//        if(SNSItemView.isEmpty(user.contact_phone2) == false)
//        {
//            listaction.add(mLauncher.getString(R.string.phonebook_call) + " " + user.contact_phone2);
//            listaction.add(mLauncher.getString(R.string.phonebook_message) + " " + user.contact_phone2);
//        }
//        if(SNSItemView.isEmpty(user.contact_phone3) == false)
//        {
//            listaction.add(mLauncher.getString(R.string.phonebook_call) + " " + user.contact_phone3);
//            listaction.add(mLauncher.getString(R.string.phonebook_message) + " " + user.contact_phone3);
//        }
//        return listaction;
//    }
//    
//    private List<String> getEmailList(final QiupuUser user)
//    {
//        List<String> listaction= new ArrayList<String>();							
//        if(SNSItemView.isEmpty(user.contact_email1) == false)
//        {
//            listaction.add(user.contact_email1);
//        }
//        if(SNSItemView.isEmpty(user.contact_email2) == false)
//        {
//            listaction.add(user.contact_email2);
//        }
//        if(SNSItemView.isEmpty(user.contact_email3) == false)
//        {
//            listaction.add(user.contact_email3);
//        }
//        return listaction;
//    }
    
//    private void showPhoneDialog(final QiupuUser user) {
//    	List<String> listaction = getActionList(user);
//        String items[] = new String[listaction.size()];
//        for(int i=0;i<listaction.size();i++)
//        {
//            items[i] = listaction.get(i);
//        }
//        AlertDialog dialog = new AlertDialog.Builder(mLauncher)					
//        .setTitle(R.string.phone_action)
//        .setItems(items, new DialogInterface.OnClickListener() 
//        {
//            public void onClick(DialogInterface dialog, int which) 
//            {	
//                if(which == 0)
//                {
//                    gotoCallDial(user.contact_phone1);
//                }
//                else if(which == 1)
//                {
//                    gotoMessage(user.contact_phone1);
//                }
//                else if(which == 2)
//                {
//                    gotoCallDial(user.contact_phone2);
//                }
//                else if(which == 3)
//                {
//                    gotoMessage(user.contact_phone2);
//                }
//                else if(which == 4)
//                {
//                    gotoCallDial(user.contact_phone3);
//                }
//                else
//                {
//                    gotoMessage(user.contact_phone3);
//                }                	
//            }
//        }).create();						
//        dialog.show();		
//    }
//    
//    private void gotoCallDial(String phonenumber)
//    {
//        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phonenumber));
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        mLauncher.startActivity(intent);
//    }
//    
//    private void gotoMessage(String phonenumber)
//    {
//        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + phonenumber));
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        mLauncher.startActivity(intent);
//    }
}
