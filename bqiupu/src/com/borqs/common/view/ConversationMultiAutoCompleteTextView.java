/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.common.view;

import android.content.Context;
import android.database.Cursor;
import android.text.Annotation;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.MotionEvent;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.HtmlUtils;
import com.borqs.qiupu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide UI for editing the recipients of multi-media messages.
 */
public class ConversationMultiAutoCompleteTextView extends MultiAutoCompleteTextView {
    private int mLongPressedPosition = -1;
    private final RecipientsEditorTokenizer mTokenizer;

    public ConversationMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.autoCompleteTextViewStyle);
        mTokenizer = new RecipientsEditorTokenizer(context, this);
        setTokenizer(mTokenizer);
        // For the focus to move to the message body when soft Next is pressed
        setImeOptions(EditorInfo.IME_ACTION_NEXT);

        setThreshold(1);
        final ConversationAutoCompleteTextView.ConversationAdapter adapter = new ConversationAutoCompleteTextView.ConversationAdapter(context);
        setAdapter(adapter);
        setOnItemClickListener(adapter.getItemClickListener());
        /*
         * The point of this TextWatcher is that when the user chooses
         * an address completion from the AutoCompleteTextView menu, it
         * is marked up with Annotation objects to tie it back to the
         * address book entry that it came from.  If the user then goes
         * back and edits that part of the text, it no longer corresponds
         * to that address book entry and needs to have the Annotations
         * claiming that it does removed.
         */
        addTextChangedListener(new TextWatcher() {
            private Annotation[] mAffected;

            public void beforeTextChanged(CharSequence s, int start,
                    int count, int after) {
                mAffected = ((Spanned) s).getSpans(start, start + count,
                        Annotation.class);
            }

            public void onTextChanged(CharSequence s, int start,
                    int before, int after) {
                if (before == 0 && after == 1) {    // inserting a character
                    char c = s.charAt(start);
                    if (isMentionTrigger(c)) {
                        // Remember the delimiter the user typed to end this recipient. We'll
                        // need it shortly in terminateToken().
//                        PREFIX_SIGN = c;
                    }
                }
            }

            public void afterTextChanged(Editable s) {
                if (mAffected != null) {
                    for (Annotation a : mAffected) {
                        s.removeSpan(a);
                        String userId = getAnnotation(mAffected, "number");
                        adapter.rejectUser(userId);
                    }
                }

                mAffected = null;
            }
        });
    }

    @Override
    public boolean enoughToFilter() {
        if (!super.enoughToFilter()) {
            return false;
        }
        // If the user is in the middle of editing an existing recipient, don't offer the
        // auto-complete menu. Without this, when the user selects an auto-complete menu item,
        // it will get added to the list of recipients so we end up with the old before-editing
        // recipient and the new post-editing recipient. As a precedent, gmail does not show
        // the auto-complete menu when editing an existing recipient.
//        int end = getSelectionEnd();
//        int len = getText().length();

//        return end == len;
        return true;
    }

    public int getRecipientCount() {
        return mTokenizer.getNumbers().size();
    }

    public List<String> getNumbers() {
        return mTokenizer.getNumbers();
    }

//    public ContactList constructContactsFromInput() {
//        List<String> numbers = mTokenizer.getNumbers();
//        ContactList list = new ContactList();
//        for (String number : numbers) {
//            Contact contact = Contact.get(number, false);
//            contact.setNumber(number);
//            list.add(contact);
//        }
//        return list;
//    }
//
//    private boolean isValidAddress(String number, boolean isMms) {
//        if (isMms) {
//            return MessageUtils.isValidMmsAddress(number);
//        } else {
//            // TODO: PhoneNumberUtils.isWellFormedSmsAddress() only check if the number is a valid
//            // GSM SMS address. If the address contains a dialable char, it considers it a well
//            // formed SMS addr. CDMA doesn't work that way and has a different parser for SMS
//            // address (see CdmaSmsAddress.parse(String address)). We should definitely fix this!!!
//            return PhoneNumberUtils.isWellFormedSmsAddress(number)
//                    || Mms.isEmailAddress(number);
//        }
//    }
//
//    public boolean hasValidRecipient(boolean isMms) {
//        for (String number : mTokenizer.getNumbers()) {
//            if (isValidAddress(number, isMms))
//                return true;
//        }
//        return false;
//    }
//
//    public boolean hasInvalidRecipient(boolean isMms) {
//        for (String number : mTokenizer.getNumbers()) {
//            if (!isValidAddress(number, isMms)) {
//                if (MmsConfig.getEmailGateway() == null) {
//                    return true;
//                } else if (!MessageUtils.isAlias(number)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    public String formatInvalidNumbers(boolean isMms) {
//        StringBuilder sb = new StringBuilder();
//        for (String number : mTokenizer.getNumbers()) {
//            if (!isValidAddress(number, isMms)) {
//                if (sb.length() != 0) {
//                    sb.append(", ");
//                }
//                sb.append(number);
//            }
//        }
//        return sb.toString();
//    }
//
//    public boolean containsEmail() {
//        if (TextUtils.indexOf(getText(), '@') == -1)
//            return false;
//
//        List<String> numbers = mTokenizer.getNumbers();
//        for (String number : numbers) {
//            if (Mms.isEmailAddress(number))
//                return true;
//        }
//        return false;
//    }

//    public static CharSequence contactToToken(Contact c) {
//        SpannableString s = new SpannableString(c.getNameAndNumber());
//        int len = s.length();
//
//        if (len == 0) {
//            return s;
//        }
//
//        s.setSpan(new Annotation("number", c.getNumber()), 0, len,
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        return s;
//    }

//    public void populate(ContactList list) {
//        SpannableStringBuilder sb = new SpannableStringBuilder();
//
//        for (Contact c : list) {
//            if (sb.length() != 0) {
//                sb.append(", ");
//            }
//
//            sb.append(contactToToken(c));
//        }
//
//        setText(sb);
//    }

    private int pointToPosition(int x, int y) {
        x -= getCompoundPaddingLeft();
        y -= getExtendedPaddingTop();


        x += getScrollX();
        y += getScrollY();

        Layout layout = getLayout();
        if (layout == null) {
            return -1;
        }

        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        return off;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final int x = (int) ev.getX();
        final int y = (int) ev.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            mLongPressedPosition = pointToPosition(x, y);
        }

        return super.onTouchEvent(ev);
    }

    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        if ((mLongPressedPosition >= 0)) {
            Spanned text = getText();
            if (mLongPressedPosition <= text.length()) {
                int start = mTokenizer.findTokenStart(text, mLongPressedPosition);
                int end = mTokenizer.findTokenEnd(text, start);

                if (end != start) {
                    String number = getNumberAt(getText(), start, end, getContext());
//                    Contact c = Contact.get(number, false);
//                    return new RecipientContextMenuInfo(c);
                    return new RecipientContextMenuInfo();
                }
            }
        }
        return null;
    }

    private static String getNumberAt(Spanned sp, int start, int end, Context context) {
        return getFieldAt("number", sp, start, end, context);
    }
    private static String getNameAt(Spanned sp, int start, int end, Context context) {
        return getFieldAt("name", sp, start, end, context);
    }
    private static int getSpanLength(Spanned sp, int start, int end, Context context) {
        // TODO: there's a situation where the span can lose its annotations:
        //   - add an auto-complete contact
        //   - add another auto-complete contact
        //   - delete that second contact and keep deleting into the first
        //   - we lose the annotation and can no longer get the span.
        // Need to fix this case because it breaks auto-complete contacts with commas in the name.
        Annotation[] a = sp.getSpans(start, end, Annotation.class);
        if (a.length > 0) {
            return sp.getSpanEnd(a[0]);
        }
        return 0;
    }

    private static String getFieldAt(String field, Spanned sp, int start, int end,
            Context context) {
        Annotation[] a = sp.getSpans(start, end, Annotation.class);
        String fieldValue = getAnnotation(a, field);
        if (TextUtils.isEmpty(fieldValue)) {
            fieldValue = TextUtils.substring(sp, start, end);
        }
        return fieldValue;

    }

    private static String getAnnotation(Annotation[] a, String key) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].getKey().equals(key)) {
                return a[i].getValue();
            }
        }

        return "";
    }

    private class RecipientsEditorTokenizer
            implements MultiAutoCompleteTextView.Tokenizer {
        private final MultiAutoCompleteTextView mList;
        private final Context mContext;

        RecipientsEditorTokenizer(Context context, MultiAutoCompleteTextView list) {
            mList = list;
            mContext = context;
        }

        /**
         * Returns the start of the token that ends at offset
         * <code>cursor</code> within <code>text</code>.
         * It is a method from the MultiAutoCompleteTextView.Tokenizer interface.
         */
        public int findTokenStart(CharSequence text, int cursor) {
            if (cursor > 0 && Character.isSpace(text.charAt(cursor - 1))) {
                return cursor;
            }

            boolean wasTrigger = false;
            int i = cursor;

            while (i > 0) {
                --i;
                char ch = text.charAt(i);
                if (wasTrigger = isMentionTrigger(ch)) {
                    if (i == 0 || Character.isSpace(text.charAt(i - 1))) {
                        wasTrigger = true;
                        break;
                    }
                }
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }

            if (wasTrigger) {
                final int len = text.length();
                if (cursor == len) {
                    return i;
                } else {
                    char ch = text.charAt(cursor);
                    if (Character.isSpaceChar(ch)) {
                        return i;
                    }
                }
            }

            return cursor;
        }

        /**
         * Returns the end of the token (minus trailing punctuation)
         * that begins at offset <code>cursor</code> within <code>text</code>.
         * It is a method from the MultiAutoCompleteTextView.Tokenizer interface.
         */
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (isMentionTrigger(text.charAt(i))) {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        /**
         * Returns <code>text</code>, modified, if necessary, to ensure that
         * it ends with a token terminator (for example a space or comma).
         * It is a method from the MultiAutoCompleteTextView.Tokenizer interface.
         */
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

//            char c;
            if (i > 0 && isMentionTrigger(text.charAt(i - 1))) {
                return text;
            } else {
                return encodeTerminateToken(text);
            }
        }

        private static final String MENTION_PLUS_ONE_FORMATTER = "<a href='%1$s%2$s'>+%3$s</a>";
        public List<String> getNumbers() {
            Spanned sp = mList.getText();
            int len = sp.length();
            List<String> list = new ArrayList<String>();

            int start = 0;
            int i = 0;
            while (i < len + 1) {
                if ((i == len) || isMentionTrigger(sp.charAt(i))) {
                    if (i > start) {
                        
                        list.add(HtmlUtils.text2html(String.format(
                                MENTION_PLUS_ONE_FORMATTER,
                                BpcApiUtils.PROFILE_SEARCH_USERID_PREFIX,
                                getNumberAt(sp, start, i, mContext),
                                getNameAt(sp, start, i, mContext)
                        )));

                        // calculate the recipients total length. This is so if the name contains
                        // commas or semis, we'll skip over the whole name to the next
                        // recipient, rather than parsing this single name into multiple
                        // recipients.
                        int spanLen = getSpanLength(sp, start, i, mContext);
                        if (spanLen > i) {
                            i = spanLen;
                        }
                    }

                    i++;

                    while ((i < len) && (sp.charAt(i) == ' ')) {
                        i++;
                    }

                    start = i;
                } else {
                    i++;
                }
            }

            return list;
        }
    }

    // TODO : implement it if necessary
    static class RecipientContextMenuInfo implements ContextMenuInfo {
//        final Contact recipient;

        RecipientContextMenuInfo(/*Contact r*/) {
//            recipient = r;
        }
    }

    boolean isMentionTrigger(char paramChar) {
        if ((paramChar == '+') || (paramChar == '@'))
            return true;
        else
            return false;
    }

    public void destroy() {
    }

    CharSequence encodeTerminateToken(CharSequence text) {
        CharSequence encodedText = text + " ";
        if (text instanceof Spanned) {
            SpannableString sp = new SpannableString(encodedText);
            TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                    Object.class, sp, 0);

            return sp;
        } else {
            return encodedText;
        }
    }

//    public String getMentions() {
//        List<String> list = getNumbers();
//        return list.toString();
//    }

//    @Override
//    protected CharSequence convertSelectionToString(Object paramObject) {
//        SpannableString localSpannableString = new SpannableString("+" + super.convertSelectionToString(paramObject));
//        Cursor localCursor = (Cursor) paramObject;
//        int i = localCursor.getColumnIndex("person_id");
//        if (i != -1)
//            localSpannableString.setSpan(new MentionSpan(localCursor.getString(i)), 0, localSpannableString.length(), 33);
//        return localSpannableString;
//        return "+" + super.convertSelectionToString(paramObject);
//    }
    
    public static String getConversationText(ConversationMultiAutoCompleteTextView conversationTextView) {
        String content = null;
        if (null != conversationTextView) {
            CharSequence text = conversationTextView.getText();
            if (text instanceof Spanned) {
                Spanned spannedText = (Spanned) text;
                URLSpan[] urlSpans = spannedText.getSpans(0, text.length(), URLSpan.class);
                if (null != urlSpans && urlSpans.length > 0) {
                    int spanStart = 0, spanEnd = 0;
                    String url;
                    String name;
                    StringBuilder stringBuilder = new StringBuilder();
                    for(URLSpan item : urlSpans) {
                        spanStart = spannedText.getSpanStart(item);
                        // Append non-spanned text after the end of last URLSpan item.
                        if (spanEnd < spanStart) {
                            stringBuilder.append(text.subSequence(spanEnd, spanStart));
                        }

                        spanEnd = spannedText.getSpanEnd(item);
                        name = text.subSequence(spanStart, spanEnd).toString();
                        url = item.getURL();
                        // Append the encoding spanned text.
                        stringBuilder.append("<a href='").append(url).append("'>").
                                append(name).append("</a>");
                    }
                    // Append the non-spanned text on the tail of spans.
                    stringBuilder.append(text.subSequence(spanEnd, text.length()));
                    content = stringBuilder.toString();
                } else {
                    content = text.toString();
                }
            } else {
                content = text.toString();
            }
        }

        return content;
    }

    public static OnClickListener instanceMentionButtonClickListener(Context context, AutoCompleteTextView editText) {
        return new MentionButtonClickListener(context, editText);
    }

    private static class MentionButtonClickListener implements OnClickListener {
        private Context mContext;
        private AutoCompleteTextView mEditText;

        public MentionButtonClickListener(Context context, AutoCompleteTextView editText) {
            mContext = context;
            mEditText = editText;
        }

        private boolean isMentionTrigger(char paramChar) {
            if ((paramChar == '+') || (paramChar == '@'))
                return true;
            else
                return false;
        }

        private void showPrompt() {
            Toast.makeText(mContext, R.string.prompt_mention_typing_start, Toast.LENGTH_SHORT).show();
        }

        public void onClick(View arg0) {
            if (null != mEditText) {
                mEditText.clearComposingText();
                int index = mEditText.getSelectionStart();
                Editable editable = mEditText.getText();

                if (index < 0) {
                    editable.append("+");
                } else if (index >= editable.length()) {
                    if (0 == index) {
                        editable.append("+");
                    } else if (isMentionTrigger(editable.charAt(index - 1))) {
                        showPrompt();
                    } else {
                        editable.append(" +");
                    }
                } else {
                    final char preChar = index > 0 ? editable.charAt(index - 1) : ' ';
                    final char curChar = editable.charAt(index);
                    if (Character.isSpaceChar(preChar)) {
                        if (Character.isSpaceChar(curChar)) {
                            editable.insert(index, "+");
                        } else {
                            if (isMentionTrigger(curChar)) {
                                showPrompt();
                            } else {
                                editable.insert(index, "+ ");
                                mEditText.setSelection(index + 1);
                            }
                        }
                        // TODO : reform this code and consider bellowing cases.
//                    } else if (isMentionTrigger(preChar)) {
//                        if (Character.isSpaceChar(curChar)) {
//                            editable.insert(index, " +");
//                        } else {
//                            editable.insert(index, " + ");
//                            mEditText.setSelection(index + 2);
//                        }
                    } else {
                        if (Character.isSpaceChar(curChar)) {
                            editable.insert(index, " +");
                        } else {
                            editable.insert(index, " + ");
                            mEditText.setSelection(index + 2);
                        }
                    }
                }
                mEditText.requestFocus();
//                InputMethodManager imm = (InputMethodManager)mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(mEditText, 0);
//                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                mEditText.showDropDown();
            }
        }
    }
}
