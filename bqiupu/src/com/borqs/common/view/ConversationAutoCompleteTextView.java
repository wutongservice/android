package com.borqs.common.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.text.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import com.borqs.common.adapter.RecipientsAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.HtmlUtils;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;

import java.util.ArrayList;


public class ConversationAutoCompleteTextView extends AddressPadMini {
    private static final String TAG =  "ConversationAutoCompleteTextView";
    // debug switcher 
    private static final boolean                    LOCAL_LOGV                   = QiupuConfig.LOGD;

    //-------------------------------------------------------------------------
    // init
    //-------------------------------------------------------------------------
    public ConversationAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public ConversationAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, android.R.attr.autoCompleteTextViewStyle);
	}
   
	public ConversationAutoCompleteTextView(Context context) {
		super(context);
		init(context, null, android.R.attr.autoCompleteTextViewStyle);
	}
	
	/**
	 * Internal initialization
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	private void init(Context context, AttributeSet attrs, int defStyle) {
        resetPadding();
        setThreshold(2);

        setAdapter(new ConversationAdapter(context));
	}

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        super.performFiltering(text, keyCode);
    }

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {   
        super.onFocusChanged(focused, direction, previouslyFocusedRect); 
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}
        
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
	}

    @Override 
	public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * Change padding
     */
    private void resetPadding() {
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

	/**
	 * <p>
	 * Closes the drop down if present on screen.
	 * </p>
	 */
	public  void dismissDropDown() {
        super.dismissDropDown();
	}

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l,t,oldl,oldt);
    }

    @Override
    protected  void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
    }

    /**
     * @hide 
     * This function is for easy debugging ....
     */
    private static void D(String msg) {
        String Tag = "xxx";
        if(LOCAL_LOGV)Log.i(Tag, msg);
    }

    public boolean performLongClick() {
        return super.performLongClick();

    }
    
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {   
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /**
     * @param number specify telephone number 
     * @return if given phone number existed in phone book return true otherwise false
     */
    public boolean isNumberSavedInContact(String number) {
        if (number == null || number.length() <= 0) {
            return false;
        }
        Context context = getContext();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                .query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number),
                                                        new String[]{ Phone.DISPLAY_NAME }, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch(Exception e) {
            D("SEARCH CONTACT FAILED : " + e);
        } finally {
        	if (cursor != null) {
                cursor.close(); 
        	}
        }
        return false;
    }


    public ArrayList getMentions() {
        return null;
    }

    public void destroy() {
    }

    public static class ConversationAdapter extends RecipientsAdapter {
        private AdapterView.OnItemClickListener mClickListener;
        private ArrayList<String> mUserIdList;
        Context mContext;
        public ConversationAdapter(Context context) {
            super(context);
            mContext = context;
            mClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Object item = getItem(i);
                    if (null != item && item instanceof Cursor) {
                        Cursor cursor = (Cursor) item;
                        String name = QiupuORM.parseUserName(cursor);
                        String userId = QiupuORM.parseUserId(cursor);
                        Log.d("", "onItemClick, i = " + i + ", l=" + l + ", name=" + name + ", uid=" + userId);
                        acceptUser(userId);
                        QiupuORM.acceptEmpolyee(mContext, userId);
                    }
                }
            };

            mUserIdList = new ArrayList<String>();
        }

        public void acceptUser(String userId) {
            mUserIdList.add(userId);
        }

        public void rejectUser(String userId) {
            mUserIdList.remove(userId);
        }

        public AdapterView.OnItemClickListener getItemClickListener() {
            return mClickListener;
        }

        private static final String MENTION_PLUS_ONE_FORMATTER = "<a href='%1$s%2$s'>+%3$s</a>";
        public Filter getFilter() {
            return new Filter()  {
                public CharSequence convertResultToString(Object paramObject) {
//                    return convertToString((Cursor) paramObject);
                    Cursor cursor = (Cursor)paramObject;
                    String name = QiupuORM.parseUserName(cursor);
                    String userid = QiupuORM.parseUserId(cursor);
                    if (userid.length() == 0) {
                        return userid;
                    }

                    final String profileUrl = String.format(MENTION_PLUS_ONE_FORMATTER,
                            BpcApiUtils.PROFILE_SEARCH_USERID_PREFIX, userid, name);

//                    SpannableString out = new SpannableString(userid);
                    SpannableString out = new SpannableString(Html.fromHtml(HtmlUtils.text2html(profileUrl)));
                    int len = out.length();
                    if (!TextUtils.isEmpty(name)) {
                        out.setSpan(new Annotation("name", name), 0, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        out.setSpan(new Annotation("name", userid), 0, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    out.setSpan(new Annotation("number", userid), 0, len,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    return out;
                }

                protected Filter.FilterResults performFiltering(CharSequence paramCharSequence) {
                    FilterResults results = new FilterResults();
                    final CharSequence queryString;
                    final char ch = null == paramCharSequence ? ' ' : paramCharSequence.charAt(0);
                    final int len = null == paramCharSequence ? 0 : paramCharSequence.length();
                    if (ch == '+' || ch == '@') {
                        queryString = paramCharSequence.subSequence(1, len);
                    } else {
                        queryString = paramCharSequence;
                    }

                    Cursor cursor = runQueryOnBackgroundThread(queryString, mUserIdList);
                    if (cursor != null) {
                        results.count = cursor.getCount();
                        results.values = cursor;
                    } else {
                        results.count = 0;
                        results.values = null;
                    }
                    // TODO: could not close directly, or crash while add people.
                    // fixme later.
//                    QiupuORM.closeCursor(cursor);

                    return  results;
                }

                protected void publishResults(CharSequence paramCharSequence, Filter.FilterResults results) {
                    Cursor oldCursor = getCursor();

                    if (results.values != null && results.values != oldCursor) {
                        changeCursor((Cursor) results.values);
                    }
                }
            };
        }
    } 
}
