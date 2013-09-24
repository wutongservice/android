package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.borqs.common.SelectionItem;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class LinksTraActivity extends BasicActivity
{
	private static final String TAG = "LinksTraActivity";
	private Handler mhandler;
	private WebView mWebView;
	public static final String LINK_URL = "LINK_URL";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.link_tra_view);
		setHeadTitle(R.string.link_title);
		overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, dropdownActionListenter);
		mWebView = (WebView) findViewById(R.id.link_webview);
		init();
		loadurl(mWebView, getIntent().getStringExtra(LINK_URL));
	}
	
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	 private void init(){
		 WebSettings settings = mWebView.getSettings();
		 settings.setJavaScriptEnabled(true);
		 mWebView.setScrollBarStyle(0);
		 settings.setSupportZoom(true);
		 settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		 settings.setBuiltInZoomControls(true);
		 settings.setTextSize(WebSettings.TextSize.LARGER);
		 // set html Adaptive webview size
		 settings.setUseWideViewPort(true);
		 settings.setLoadWithOverviewMode(true);


		 mWebView.setWebViewClient(new WebViewClient() {  
		        @Override  
		        public boolean shouldOverrideUrlLoading(WebView view, String url) {
		        	Log.d(TAG, "shouldOverrideUrlLoading: " + url);
		        	view.loadUrl(url);
		            return true;  
		        }  
		          
		        @Override
		        public void onPageStarted(WebView view, String url, Bitmap favicon) {
		        	super.onPageStarted(view, url, favicon);
		        	mhandler.sendEmptyMessage(LOAD_WEB_BEGIN);
		        }
		        
		        
		        @Override  
		        public void onPageFinished(WebView view, String url) {  
		        	super.onPageFinished(view, url);  
		        	Log.d(TAG, "onPageFinished: " + url);
		        	mhandler.sendEmptyMessage(LOAD_WEB_END);
		        }  
		          
		        @Override  
		        public void onReceivedError(WebView view, int errorCode,  
		                String description, String failingUrl) {
		            super.onReceivedError(view, errorCode, description, failingUrl);  
		        }  
		    });
		 
		 mWebView.setDownloadListener(new DownloadListener() {
			 
			 @Override
			 public void onDownloadStart(String url, String userAgent,
					 String contentDisposition, String mimetype, long contentLength) {
				 Uri uri = Uri.parse(url);
//				 if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
//					 DownloadManager manager =(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
//					 DownloadManager.Request down=new DownloadManager.Request (uri);  
//					 down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);  
//					 down.setShowRunningNotification(true);  
//					 down.setVisibleInDownloadsUi(true);  
////					 down.setDestinationInExternalFilesDir(getApplicationContext(), null, DOWNLOAD_SERVICE.getExternalStoragePublicDirectory);  
//					 manager.enqueue(down);  
//				 }else {
					 // use browser open url.
					 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					 intent.setData(uri);
			        	try {
						    startActivity(intent);
						}catch(Exception ne){
							ne.printStackTrace();
						}
			        	
			        	finish();
//				 }
			 }
		 });
	    }
	 
	@Override
	protected void createHandler()
	{
		mhandler = new MainHandler();
	}
	
	
	private final static int LOAD_WEB_BEGIN        = 101;
	private final static int LOAD_WEB_END          = 102;
	
	private class MainHandler extends Handler{
	    public void handleMessage(Message msg) {
	    	switch(msg.what) {
		    	case LOAD_WEB_BEGIN: {
		    		begin();
		    		break;
		    	}
		    	case LOAD_WEB_END: {
		    		end();
		    		break;
		    	}
	    	}
	    }
    }
	
	private void loadurl(final WebView view,final String url){
    	new Thread(TAG){
        	public void run(){
        		mhandler.sendEmptyMessage(LOAD_WEB_BEGIN);
        		view.loadUrl(url);
        	}
        }.start();
    }
	
	@Override
	protected void uiLoadEnd() {
		super.uiLoadEnd();
		showLeftActionBtn(false);
	}
	
	View.OnClickListener dropdownActionListenter = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.label_refresh)));
        	items.add(new SelectionItem("", getString(R.string.save_link_label)));
        	items.add(new SelectionItem("", getString(R.string.share_link_label)));
        	items.add(new SelectionItem("", getString(R.string.open_with_browser)));
        	showCorpusSelectionDialog(items);
        }
    };
    
    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
        if(mMiddleActionBtn != null) {
            int location[] = new int[2];
            mMiddleActionBtn.getLocationInWindow(location);
            int x = location[0];
            int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
            
            DialogUtils.showCorpusSelectionDialog(this, x, y, items, actionListItemClickListener);
        }
    }
    
    OnItemClickListener actionListItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                onCorpusSelected(item.getText());             
            }
        }
    };
    
    public void onCorpusSelected(String value) {
        if(getString(R.string.label_refresh).equals(value)) {
        	if(mWebView != null) {
        		loadurl(mWebView, mWebView.getUrl());
        	}else {
        		if(QiupuConfig.LOGD)Log.e(TAG, "mWebView is null.");
        	}
        }else if(getString(R.string.save_link_label).equals(value)) {
        	if(mWebView != null) {
        		addBookmark(mWebView.getTitle(), mWebView.getUrl());
        	}else {
        		if(QiupuConfig.LOGD)Log.e(TAG, "mWebView is null.");
        	}
        } 
        else if(getString(R.string.share_link_label).equals(value)) {
        	if(mWebView != null) {
        		sharelink();
//        		Intent intent = new Intent(this, QiupuComposeActivity.class);
//        		intent.setAction(Intent.ACTION_SEND);
//        		intent.putExtra(Intent.EXTRA_TEXT, mWebView.getUrl());
//        		intent.putExtra(Intent.EXTRA_SUBJECT, mWebView.getTitle());
//        		intent.putExtra(QiupuComposeActivity.SHARE_FAVICON, mWebView.getFavicon());
//        		startActivity(intent);
        	}else {
        		if(QiupuConfig.LOGD)Log.e(TAG, "mWebView is null.");
        	}
        }else if(getString(R.string.open_with_browser).equals(value)) {
        	openWithBroser();
        }
    }
    
    private void openWithBroser() {
    	if(mWebView != null && StringUtil.isEmpty(mWebView.getUrl()) == false) {
    		Uri ouri = Uri.parse(mWebView.getUrl());
    		Intent intent = new Intent(Intent.ACTION_VIEW, ouri);
    		intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
    		startActivity(intent);
    	}else {
    		if(QiupuConfig.LOGD)Log.e(TAG, "mWebView is null. or url is null");
    	}
    }
    
    private void addBookmark(String title, String url){
    	String where = android.provider.Browser.BookmarkColumns.URL + "='" + url + "'";
    	ContentResolver cr = getContentResolver();
    	
    	Cursor cursor = cr.query(android.provider.Browser.BOOKMARKS_URI, new String[]{android.provider.Browser.BookmarkColumns.URL}, where, null, null);
    	if(cursor != null && cursor.getCount() > 0) {
    		ToastUtil.showShortToast(this, mhandler, R.string.already_import_link);
    	}else {
    		try {
    			ContentValues inputValue = new ContentValues();
    			inputValue.put(android.provider.Browser.BookmarkColumns.BOOKMARK, 1);//Bookmark
    			inputValue.put(android.provider.Browser.BookmarkColumns.TITLE, title);//Title
    			inputValue.put(android.provider.Browser.BookmarkColumns.URL, url); //URL                        
    			Uri uri = cr.insert(android.provider.Browser.BOOKMARKS_URI, inputValue);//insert to browser bookmark
    			ToastUtil.showShortToast(this, mhandler, R.string.import_to_calender_successful);
			} catch (Exception e) {
				Log.d(TAG, "export bookmarks failed");
			}
    	}
    }
    
    private void sharelink() {
    	if(mWebView != null) {
    		Intent send = new Intent(Intent.ACTION_SEND);
    		send.setType("text/plain");
    		send.putExtra(Intent.EXTRA_TEXT, mWebView.getUrl());
    		send.putExtra(Intent.EXTRA_SUBJECT, mWebView.getTitle());
    		
    		try {
    			startActivity(Intent.createChooser(send, getString(R.string.share_link_label)));//name
    		} catch (android.content.ActivityNotFoundException ex) {
    		}
    	}else {
    		Log.i(TAG, "webView is null ");
    	}
    }
    
//    private View.OnClickListener overrideSlideToggleClickListener = new View.OnClickListener() {
//    	
//    	@Override
//    	public void onClick(View v) {
//    		if(mWebView.canGoBack()) {
//    			mWebView.goBack();
//    		}else {
//    			onBackPressed();
//    		}
//    	}
//    };
	
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK) {
//        	if (event.isTracking() && !event.isCanceled()) {
//        		if(mWebView.canGoBack()) {
//        			mWebView.goBack();
//        		}
//        		return true;
//        	}
//        }
//        return super.onKeyUp(keyCode, event);
//    }
    
//    final class MyWebChromeClient extends WebChromeClient {
//        @Override
//        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//            Log.d(TAG, message);
//            result.confirm();
//            return true;
//        }
//    }
//    
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (mWebView.canGoBack() && event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
//			if(mFirstUrl != null && mFirstUrl.equals(mWebView.getUrl())) {
//				finish();
//			}else {
//				mWebView.goBack();
//			}
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	};
	
//	private void backWebViewEvent() {
//		if (mWebView != null && mWebView.canGoBack()) {
//			mWebView.goBack();
//		} else {
//			finish();
//		}
//	}
}
