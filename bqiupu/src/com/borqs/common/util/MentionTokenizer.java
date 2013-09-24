package com.borqs.common.util;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-2
 * Time: 下午3:17
 * To change this template use File | Settings | File Templates.
 */

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;
import com.borqs.common.view.MentionSpan;

public class MentionTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    private boolean isMentionSpan(CharSequence paramCharSequence, int paramInt) {
        int j;
        if (!(paramCharSequence instanceof Spannable)) {
            j = 0;
        } else {
            MentionSpan[] arrayOfMentionSpan = (MentionSpan[]) ((Spannable) paramCharSequence).getSpans(paramInt, paramInt, MentionSpan.class);
            if ((arrayOfMentionSpan == null) || (arrayOfMentionSpan.length == 0))
                j = 0;
            else
                j = 1;
        }
        return j > 0;
    }

    public static boolean isMentionTrigger(char paramChar) {
        int i;
        if ((paramChar != '+') && (paramChar != '@'))
            i = 0;
        else
            i = 1;
        return i > 0;
    }

    // todo : verify if run well.
    public int findTokenEnd(CharSequence paramCharSequence, int paramInt) {
        int i = paramCharSequence.length();
        int j = 0;
        int k = paramInt;
        while (true) {
            if (k >= i)
                break;

            char c = paramCharSequence.charAt(k);
            if (c == '\n')
                break;

            if (Character.isWhitespace(c)) {
                j++;
                if (j >= 4)
                    break;

                int m = k + 1;
                for (; m < i; m++) {
                    c = paramCharSequence.charAt(m);
                    if (c == '\n')
                        break;
                    if (!Character.isWhitespace(c))
                        break;
                }
                if (m == i)
                    break;
                k = m;
                c = paramCharSequence.charAt(k);
            } else {
                if ((!isMentionTrigger(c)) || ((k != 0) && (!Character.isWhitespace(paramCharSequence.charAt(k - 1))))) {
                    k++;
                    continue;
                }
                i = k;
                break;
            }
        }

        return i;
    }

    public int findTokenStart(CharSequence paramCharSequence, int paramInt) {
        int i = paramInt - 1;
        while (i >= 0) {
            char c = paramCharSequence.charAt(i);
            if (c == '\n')
                break;
            if ((!isMentionTrigger(c)) || ((i != 0) && (!Character.isWhitespace(paramCharSequence.charAt(i - 1))))) {
                i--;
                continue;
            }
            if (isMentionSpan(paramCharSequence, i))
                break;
            paramInt = i;
            --i;
        }
        return paramInt;
    }

    public CharSequence terminateToken(CharSequence paramCharSequence) {
        int i = paramCharSequence.length();
        Object localObject;
        if ((i != 0) && (!Character.isWhitespace(paramCharSequence.charAt(i - 1)))) {
            if (!(paramCharSequence instanceof Spanned)) {
                localObject = paramCharSequence + " ";
            } else {
                localObject = new SpannableString(paramCharSequence + " ");
                TextUtils.copySpansFrom((Spanned) paramCharSequence, 0, paramCharSequence.length(), Object.class, (Spannable) localObject, 0);
            }
        } else
            localObject = paramCharSequence;
        return (CharSequence) localObject;
    }
}
