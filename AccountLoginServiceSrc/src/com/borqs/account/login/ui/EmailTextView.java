package com.borqs.account.login.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

import com.borqs.account.login.R;

public class EmailTextView extends MultiAutoCompleteTextView {
    private static final String[] addrList = new String[]{
            "qq.com", "126.com", "163.com", "gmail.com",
            "sina.com", "sohu.com", "yahoo.com", "yahoo.cn",
            "borqs.com", 
    };
    
    public EmailTextView(Context ctx){
        super(ctx);
        setPromptList(ctx);
    }
    
    public EmailTextView(Context ctx, AttributeSet attrs){
        super(ctx, attrs);
        setPromptList(ctx);
    }
    
    public EmailTextView(Context ctx, AttributeSet attrs, int defStyle){
        super(ctx, attrs, defStyle);
        setPromptList(ctx);
    }
    
    private void setPromptList(Context ctx){
        ArrayAdapter<String> addrAdapter = new ArrayAdapter<String>(ctx,
                R.layout.account_username_suffix_list_item, 
                addrList);
        setAdapter(addrAdapter);
        setTokenizer(mEmailTokenizer);
    }
    
    private MultiAutoCompleteTextView.Tokenizer mEmailTokenizer =
            new MultiAutoCompleteTextView.Tokenizer() {
        public int findTokenStart(CharSequence text, int cursor) {
            int i = text.toString().indexOf('@');

            if(i == -1) {
                return cursor;
            }
            return i + 1;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int len = text.length();

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            return text;
        }
    };
}
