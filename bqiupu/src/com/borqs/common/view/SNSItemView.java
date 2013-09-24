package com.borqs.common.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.CacheMap;
import twitter4j.QiupuUser;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.Browser;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.MyHtml;
import com.borqs.common.util.SmileyParser;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;

public abstract class SNSItemView extends LinearLayout
{
	private final static String TAG = "Qiupu.SNSItemView";
	protected Context mContext;
	public SNSItemView(Context context) {
		super(context);	
		mContext = context;
	}
	public SNSItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		mContext = ctx;
	}
	
	//TODO
	//performance issue, we should also cache the data
	protected static String formatHtmlContent(String content)
	{		
		String res = "";
		if(SNSItemView.isURL(content))
		{
			res = String.format("<a href='%1$s'>%1$s</a>", content);
		}
		else
		{
			List<String> hrefs = SNSItemView.getLinks(content);
			if(hrefs.size() > 0)
			{
				//format the href
				res = formatTextAsHtml(content);
			}
			else
			{
				//remove html text
				
				//has link, include borqs
				if(hasLinks(content) == true)
				{
					res = toHTMLString(content);
				}
				else
				{
//					res = removeHTML(content, false);
                    res = splitAndFilterString(content);
				    res = toHTMLString(res);
				}
			}
		}
		
		//res = res.replaceAll("<br><br><br>", "<br>");
		//Log.d(TAG, "pre data="+content + " later="+res);		
		return res;
	}

    private static Pattern htmlTagPattern = Pattern.compile((".+?"));
    private static void debugTest(String str) {
        String[] data = new String[] {str, "prefix<a href=>index</a>", "<good>", "<<better>>", "<test>ball<<book>>loon<debug>"};
        for (String input : data) {
            Log.d("debugTest", "input: " + input);
        }
    }
    public static String splitAndFilterString(String input/*, int length*/) {
        if (input == null || input.trim().equals("")) {
            return "";
        }
//        debugTest(input);
        // remove all html tag
//        String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
//                "<[^>]*>", "");
        String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "");
        str = str.replaceAll("<[/a-zA-Z][^>]*>", "");
//        str = str.replaceAll("[(/>)<]", "");
//        str = str.replaceAll("[(/>)]", "");
        str = str.replaceAll("[<]", "&lt;");
        str = str.replaceAll("[>]", "&gt;");

//        int len = str.length();
//        if (len <= length) {
//            return str;
//        } else {
//            str = str.substring(0, length);
//            str += "......";
//        }
        return str;
    }

    private static String formatTextAsHtml(String text)
	{
        final String htmlText = toHTMLString(text);

		StringBuilder resstr = new StringBuilder();
		if(isEmpty(htmlText) == false)		{

			Matcher m = utlp.matcher(htmlText);
			int preStart=0, preEnd=0;
			//if(m.matches())
			{
				while(m.find())
				{
					preEnd = m.start();
					resstr.append(htmlText.subSequence(preStart, preEnd));
					String str = String.format("<a href='%1$s'>%1$s</a>", htmlText.substring(m.start(), m.end()).trim());
					resstr.append(str);

					preStart = m.end();
				}
			}
			/*
			else
			{
				if(supportForgeURL)
				{
					Matcher mno = urlpno.matcher(text);
					preStart = 0;
					preEnd   = 0;
					while(mno.find())
					{
						preEnd = mno.start();
						resstr.append(text.subSequence(preStart, preEnd));
						String str = String.format("<a href='%1$s'>%1$s</a>", text.substring(mno.start(), mno.end()).trim());
						resstr.append(str);

						preStart = mno.end();
					}
				}
			}*/

			if(preStart <htmlText.length())
			{
				resstr.append(htmlText.subSequence(preStart, htmlText.length()-1));
			}
		}	
		
		return resstr.toString();
	}
	
	public boolean isProfile(String url)
	{
    	Uri uri = Uri.parse(url);
    	String path = uri.getPath();
    	Log.d("sns-link", "isProfile url="+url +" path="+path);
    	if(isEmpty(path) == false)
    	{
    	    if(path.equals("/profile.php"))
    	    {
    	    	return true;
    	    }
    	}
		return false;
	}
	 
	public boolean isPhoto(String url)
	{
		Uri uri = Uri.parse(url);
    	String path = uri.getPath();
    	
    	Log.d("sns-link", "isPhoto url="+url +" path="+path);
    	if(isEmpty(path) == false)
    	{
    	    if(path.equals("/album.php") || path.equals("/photo.php"))
    	    {
    	    	return true;
    	    }
    	}
		return false;
	}
	
	public abstract String getText();
	public static boolean isEmpty(String str)
	{
		return str == null || str.trim().length() == 0;
	}
	
	//http://hi.baidu.com/hongnix/blog/item/4b9e1b4209fdd41d73f05db5.html
	//http://liqingsong.007.blog.163.com/blog/static/48776737201011904616471/
	
	static String _email  ="^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	static final Pattern emailp = Pattern.compile(_email,Pattern.CASE_INSENSITIVE);
	public static boolean isEmail(String str)
	{
		if(isEmpty(str) == false)
		{
			
			Matcher m = emailp.matcher(str);
			
			return m.matches();
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isURL(String text)
	{
		if(isEmpty(text) == false)
		{
			Matcher m = utlp.matcher(text);
			return m.matches();
		}
		
		return false;
	}
	
	public static final String urlPartern="(" +
	"(ftp|http|https|gopher|mailto|tel|news|nntp|telnet|wais|file|prospero|aim|webcal)" +
	":" +
	"(" +
	"([A-Za-z0-9$_.+!*(),;/?:@&~=-])|%[A-Fa-f0-9]{2}){2,}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*(),;/?:@&~=%-]*))?([A-Za-z0-9$_+!*();/?:~-]))";
	
	public static final String urlborqsPartern="(" +
			"(borqs|ftp|http|https|gopher|mailto|tel|news|nntp|telnet|wais|file|prospero|aim|webcal)" +
			":" +
			"(" +
			"([A-Za-z0-9$_.+!*(),;/?:@&~=-])|%[A-Fa-f0-9]{2}){2,}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*(),;/?:@&~=%-]*))?([A-Za-z0-9$_+!*();/?:~-]))";
	public static final String noPrefixPartern="(" +
	"(ftp|http|https|gopher|mailto|tel|news|nntp|telnet|wais|file|prospero|aim|webcal)" +
	":" +
	"(" +
	"([A-Za-z0-9$_.+!*(),;/?:@&~=-])|%[A-Fa-f0-9]{2}){2,}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*(),;/?:@&~=%-]*))?([A-Za-z0-9$_+!*();/?:~-]))";
	
	static final boolean supportForgeURL = false;
	static final Pattern utlp   = Pattern.compile(urlPartern);
	static final Pattern urlpno = Pattern.compile(noPrefixPartern);
	static final Pattern borqsp   = Pattern.compile(urlborqsPartern);
	public List<String> getLinks()
	{
		return getLinks(getText());		
	}
	
	public static boolean hasLinks(String str)
	{
		boolean ret = false;
		String text = str;
		if(isEmpty(text) == false)
		{			
			Matcher m = borqsp.matcher(text);
			//if(m.matches())
			{
				while(m.find())
				{
					ret = true;
					break;
				}
			}
			
			m = null;
			
		}
		return ret;
	}

	
	public static List<String> getLinks(String str)
	{
		List<String> links = new ArrayList<String>();
		String text = str;
		if(isEmpty(text) == false)
		{			
			Matcher m = utlp.matcher(text);
			//if(m.matches())
			{
				while(m.find())
				{
					links.add(text.substring(m.start(), m.end()).trim());
				}
			}
			/*
			else
			{	
				if(supportForgeURL)
				{
					Matcher mno = urlpno.matcher(text);				
					while(mno.find())
					{
						links.add(text.substring(mno.start(), mno.end()).trim());
					}
					mno = null;
				}
			}*/
			
			m = null;
			
		}
		return links;
	}

	
	/**
     * Remove occurences of html, defined as any text
     * between the characters "&lt;" and "&gt;".
     * Optionally replace HTML tags with a space.
     *
     * @param str
     * @param addSpace
     * @return
     */
    public static String removeHTML(String str, boolean addSpace) 
    {
        if (str == null) return "";

        StringBuffer ret = new StringBuffer(str.length());
        int start = 0;
        int beginTag = str.indexOf("<");
        int endTag = 0;
        if (beginTag == -1)
            return str;
       
        while (beginTag >= start) {
            if (beginTag > 0) {
                ret.append(str.substring(start, beginTag));
               
                // replace each tag with a space (looks better)
                if (addSpace) ret.append(" ");
            }
            endTag = str.indexOf(">", beginTag);
           
            // if endTag found move "cursor" forward
            if (endTag > -1) {
                start = endTag + 1;
                beginTag = str.indexOf("<", start);
            }
            // if no endTag found, get rest of str and break
            else {
                ret.append(str.substring(beginTag));
                break;
            }
        }
        // append everything after the last endTag
        if (endTag > -1 && endTag + 1 < str.length()) {
            ret.append(str.substring(endTag + 1));
        }
        return ret.toString().trim();
    }

    protected boolean ensureAccountLogin() {
        if (mContext instanceof BasicActivity) {
            BasicActivity activity = (BasicActivity) mContext;
            return activity.ensureAccountLogin();
        }

        return false;
    }
    
    protected long getAccountID() {
        if (mContext instanceof BasicActivity) {
            BasicActivity activity = (BasicActivity) mContext;
            return activity.getSaveUid();
        }

        return -1;
    }
    
    private static String toHTMLString(String in) {
    	if(true)
    	    return in;
    	
        StringBuffer out = new StringBuffer();
        for (int i = 0; in != null && i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\'')
                out.append("&#039;");
            else if (c == '\"')
                out.append("&#034;");
            else if (c == '<')
                out.append("&lt;");
            else if (c == '>')
                out.append("&gt;");
            else if (c == '&')
                out.append("&amp;");
            else if (c == ' ')
                out.append("&nbsp;");
            else if (c == '\n')
                out.append("<br>");
            else
                out.append(c);
        }
        return out.toString();
    }

    protected void attachHtmlTextView(TextView textView, String content, String metaAtt) {
        if (null != textView) {
            if (TextUtils.isEmpty(content)) {
                textView.setText(null);
            } else {
                content = content.trim();
            	Object obj = getAttachedObject();
            	if(isEmpty(metaAtt) == false && CacheMap.class.isInstance(obj))
            	{
            		CacheMap cm = (CacheMap)obj;
            		String cachedData = cm.getCache(metaAtt);
            		if(isEmpty(cachedData) == false)
            		{
            			stripHtmlUnderlines(textView, cachedData);
	                    attachTextViewMovementMethod(textView);
            		}
            		else
            		{
            			String cachenewData = formatHtmlContent(content);
            			if (hasLinks(content)) {            				
    	                    stripHtmlUnderlines(textView, cachenewData);
    	                    attachTextViewMovementMethod(textView);
    	                } else {
    	                    stripHtmlUnderlines(textView, cachenewData);    	                    
    	                }
            			
            			cm.cacheMeta(metaAtt, cachenewData);
            		}
            	}
            	else
            	{
	                if (hasLinks(content)) {
	                    stripHtmlUnderlines(textView, formatHtmlContent(content));
	                    attachTextViewMovementMethod(textView);
	                } else {
	                    stripHtmlUnderlines(textView, formatHtmlContent(content));
	                    //attachTextViewMovementMethod(textView);
	                }
            	}
            }
        }
    }

    public static void attachHtml(TextView textView, String content) {
        content = content.trim();
        if (hasLinks(content)) {
            stripHtmlUnderlines(textView, formatHtmlContent(content));
            attachTextViewMovementMethod(textView);
        } else {
            stripHtmlUnderlines(textView, formatHtmlContent(content));
            //attachTextViewMovementMethod(textView);
        }
    }

    protected Object getAttachedObject()
    {
    	return null;
    }

    protected static void attachTextViewMovementMethod(TextView textView) {
        if (null != textView) {        	
            textView.setMovementMethod(MyLinkMovementMethod.getInstance());
            textView.setFocusable(true);
            textView.setLinksClickable(true);
        }
    }


    protected static void stripHtmlUnderlines(TextView textView, String content) {
        if (null != textView) {
            final CharSequence htmlText;
            if (TextUtils.isEmpty(content)) {
                htmlText = "";
            } else {
                final String firstString = content.trim().replace("\r\n", "<br>");
                final String finalString = firstString.replace("\n", "<br>");
                htmlText = MyHtml.fromHtml(finalString);
            }

            SmileyParser parser = SmileyParser.getInstance();
            SpannableStringBuilder buf = new SpannableStringBuilder(parser.addSmileySpans(htmlText));
            textView.setText(buf);

            /*
            final CharSequence charSequence = textView.getText();
            if (charSequence instanceof Spanned) {
                final Spanned sb = (Spanned) charSequence;
                String rawText = charSequence.toString();
                SpannableString ss = new SpannableString(rawText);
                URLSpan[] spans = sb.getSpans(0, sb.length(), URLSpan.class);
                for (URLSpan span1 : spans) {
                    int start = sb.getSpanStart(span1);
                    int end = sb.getSpanEnd(span1);
                    String text = sb.subSequence(start, end).toString();

                    int startpp = rawText.indexOf(text);
                    if (start < startpp) {
                        int span = (startpp - start);
                        start += (span);
                        end += (span);
                    }

                    MyURLSPan my = new MyURLSPan(span1.getURL());
                    ss.setSpan(my, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                textView.setText(ss);
            }*/
        }
    }

    public static class BorqsURLSPan extends URLSpan {

        String url;
        Context mContext;

        public BorqsURLSPan(Parcel src) {
            super(src);            
        }

        public BorqsURLSPan(String src, Context context) {
            super(src);
            url = src;
            mContext = context;
        }

        @Override
        public String getURL() {
            return super.getURL();
        }


        @Override
        public void updateDrawState(TextPaint ds) {
            //super.updateDrawState(ds);
        	Resources res = mContext.getResources();
        	ds.linkColor = res.getColor(R.color.textview_link_color);        	
            ds.setColor(res.getColor(R.color.link_text_color));        	
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View widget) {
            if (null != widget && widget instanceof TextView) {
                TextView textView = (TextView)widget;
                CharSequence charSequence = textView.getText();
                if (charSequence instanceof SpannableString) {
                    SpannableString sb = (SpannableString)charSequence;

                    int start = sb.getSpanStart(this);
                    int end = sb.getSpanEnd(this);
                    String text = sb.subSequence(start, end).toString();

                    if (QiupuConfig.LOGD)
                        Log.d("BorqsURLSPan", "click= text=" + text + " url=" + getURL());
                    Uri uri = Uri.parse(getURL());
                    
                    boolean noneedtakeaction = false;
                    String actions = uri.getQueryParameter("noneedtakeaction");
                    if(actions != null)
                    {
                    	noneedtakeaction = actions.equalsIgnoreCase("true");
                    }
                    
                    //use user defined click listener
                    if(noneedtakeaction == true)
                    {	
                    	widget.performClick();
                    	return ;
                    }
                    
                    String owner = uri.getQueryParameter("uid");
                    if (owner != null) {
                        try {
                            QiupuUser user = QiupuORM.queryOneUserInfo(textView.getContext(), Long.parseLong(owner));
                            if (user != null) {
                                IntentUtil.startUserDetailIntent(textView.getContext(), user.uid, user.nick_name, user.circleName);
                                user.despose();
                                user = null;
                            } else {
                                IntentUtil.startUserDetailIntent(textView.getContext(), Long.valueOf(owner), text);
                            }
                        } catch (Exception ne) {
                        }
                    } else {
                        //open in Browser
                    	/*
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String url = getURL();
                        if(url.startsWith("borqs://") == false && url.startsWith("http://") == false)
                        {
                        	uri = Uri.parse("http://"+url);
                        }
                        intent.setData(uri);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        textView.getContext().startActivity(intent);
                        */
                        
                        Uri ouri = Uri.parse(getURL());
                        Context context = widget.getContext();
                        Intent intent = new Intent(Intent.ACTION_VIEW, ouri);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                        context.startActivity(intent);
                    }
                }
                else
                {
                	Uri ouri = Uri.parse(getURL());
                    Context context = widget.getContext();
                    Intent intent = new Intent(Intent.ACTION_VIEW, ouri);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                    context.startActivity(intent);
                }
            }            
        }
    }

    protected Bundle getTagBundle() {
        Log.d(TAG, "tagItem with unknown view: " + this);
        return null;
    }

    public static void copyItem(View targetView) {
        if (SNSItemView.class.isInstance(targetView)) {
            final String text = ((SNSItemView) targetView).getText();
            copyItemText(targetView.getContext(), text);
        }
    }

    public static void copyItemText(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (!TextUtils.isEmpty(text)) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                        context.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            }
        } else {
            if (!TextUtils.isEmpty(text)) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                        context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(text, text);
                clipboard.setPrimaryClip(clip);
            }
        }
    }

    public static void tagItem(View targetView) {
        if (targetView instanceof SNSItemView) {
            SNSItemView itemView = (SNSItemView) targetView;
            Bundle bundle = itemView.getTagBundle();
            tagItem(targetView.getContext(), bundle);
        }
        Log.d(TAG, "tagItem, unknown view: " + targetView);
    }

    public static void tagItem(Context context, Bundle bundle) {
        if (null != bundle) {
            Intent intent = new Intent(IntentUtil.WUTONG_ACTION_TAGS);
            if (BpcApiUtils.isActivityReadyForIntent(context, intent)) {
                intent.putExtras(bundle);
                context.startActivity(intent);
            } else {
                Log.d(TAG, "tagItem not responding activity.");
            }
        }
    }
}
