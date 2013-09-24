package com.borqs.information;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import com.borqs.common.util.MyHtml;

public class AsyncHtmlTextLoader {
    private static final String TAG = "AsyncHtmlTextLoader";

	public Map<String, Reference<Spanned>> textCache;
	
	public AsyncHtmlTextLoader() {
		textCache = new HashMap<String, Reference<Spanned>>();
	}
	
	public void clear() {
		textCache.clear();
	}
	
	public Spanned loadHtmlText(final long idTag, final String text, final HtmlTextCallback callback){
		if(textCache.containsKey(text)) {
			Reference<Spanned> softReference=textCache.get(text);
			if(softReference.get()!=null){
				return softReference.get();
			}
		}
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				callback.textLoaded((Spanned) msg.obj, idTag); 
			}
		};
		new Thread(TAG) {
			public void run() {
				Spanned htmlText = loadHtmlTextFromString(text);
				textCache.put(text, new SoftReference<Spanned>(htmlText));
				handler.sendMessage(handler.obtainMessage(0, htmlText));
			}
		}.start();
		return null;
	}
	
	protected Spanned loadHtmlTextFromString(String text) {
		return MyHtml.fromHtml(text);
	}
	
	public interface HtmlTextCallback{
		public void textLoaded(Spanned htmlText, long idTag);
	}
}
