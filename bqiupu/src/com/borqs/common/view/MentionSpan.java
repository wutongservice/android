package com.borqs.common.view;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-2
 * Time: 下午3:19
 * To change this template use File | Settings | File Templates.
 */

import android.text.TextPaint;
import android.text.style.URLSpan;

public class MentionSpan extends URLSpan {
    MentionSpan(URLSpan paramURLSpan) {
        super(paramURLSpan.getURL());
        if (isMention(paramURLSpan))
            return;
        throw new IllegalArgumentException(paramURLSpan.getURL());
    }

    public MentionSpan(String paramString) {
        super("+" + paramString);
    }

    static boolean isMention(URLSpan paramURLSpan) {
        String str = paramURLSpan.getURL();
        boolean ret;
        if ((str == null) || (!str.startsWith("+")))
            ret = false;
        else
            ret = true;
        return ret;
    }

    public String getAggregateId() {
        return getURL().substring("+".length());
    }

    public void updateDrawState(TextPaint paramTextPaint) {
        paramTextPaint.setColor(-13408564);
        paramTextPaint.bgColor = 0;
        paramTextPaint.setUnderlineText(false);
    }
}
