package com.borqs.common.view;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-2
 * Time: 下午12:11
 * To change this template use File | Settings | File Templates.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.MultiAutoCompleteTextView;
import com.borqs.common.PeopleSearchResults;
import com.borqs.common.adapter.PeopleSearchListAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.MentionTokenizer;
import com.borqs.common.util.SoftInput;

import java.util.ArrayList;

public final class MentionMultiAutoCompleteTextView extends MultiAutoCompleteTextView {
    private PeopleSearchListAdapter mMentionCursorAdapter;

    public MentionMultiAutoCompleteTextView(Context paramContext) {
        super(themedApplicationContext(paramContext));
    }

    public MentionMultiAutoCompleteTextView(Context paramContext, AttributeSet paramAttributeSet) {
        super(themedApplicationContext(paramContext), paramAttributeSet);
    }

    public MentionMultiAutoCompleteTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(themedApplicationContext(paramContext), paramAttributeSet, paramInt);
    }

    private void adjustInputMethod() {
        int j = getInputType();
        int i;
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT)
            i = j | 0x10000;
        else
            i = j & 0xFFFEFFFF;
        if (j != i) {
            setRawInputType(i);
            SoftInput.restart(this);
        }
    }

    private static void replaceSpan(Editable paramEditable, Object paramObject1, Object paramObject2) {
        int k = paramEditable.getSpanStart(paramObject1);
        int i = paramEditable.getSpanEnd(paramObject1);
        int j = paramEditable.getSpanFlags(paramObject1);
        paramEditable.removeSpan(paramObject1);
        paramEditable.setSpan(paramObject2, k, i, j);
    }

    private static Context themedApplicationContext(Context paramContext) {
        return new ContextThemeWrapper(paramContext.getApplicationContext(), 2131689516);
    }

    protected CharSequence convertSelectionToString(Object paramObject) {
        Cursor localCursor = (Cursor) paramObject;
        SpannableString localSpannableString = new SpannableString("+" + super.convertSelectionToString(paramObject));
        int i = localCursor.getColumnIndex("person_id");
        if (i != -1)
            localSpannableString.setSpan(new MentionSpan(localCursor.getString(i)), 0, localSpannableString.length(), 33);
        return localSpannableString;
    }

    public void destroy() {
        if (this.mMentionCursorAdapter != null) {
            this.mMentionCursorAdapter.close();
            this.mMentionCursorAdapter = null;
        }
        setAdapter((CursorAdapter) null);
        ((ViewGroup) getParent()).removeView(this);
    }

    public ArrayList<MentionData> getMentions() {
        int i = 0;
        Editable localEditable = getText();
        MentionSpan[] arrayOfMentionSpan = (MentionSpan[]) localEditable.getSpans(0, localEditable.length(), MentionSpan.class);
        ArrayList localArrayList = new ArrayList(arrayOfMentionSpan.length);
        int m = arrayOfMentionSpan.length;
        while (true) {
            if (i >= m) {
                break;
            }
            MentionSpan localMentionSpan = arrayOfMentionSpan[i];
            MentionData.Builder localBuilder = MentionData.newBuilder();
            int j = localEditable.getSpanStart(localMentionSpan);
            int k = localEditable.getSpanEnd(localMentionSpan);
            localBuilder.setIndex(j);
            localBuilder.setLength(k - j);
            localBuilder.setAggregateId(localMentionSpan.getAggregateId());
            localArrayList.add(localBuilder.build());
            i++;
        }

        return localArrayList;
    }

    //  public void init(Fragment paramFragment, CircleNameResolver paramCircleNameResolver, EsAccount paramEsAccount, String paramString)
//  {
//    this.mMentionCursorAdapter = new RecipientsAdapter(getContext(), paramFragment.getFragmentManager(), paramFragment.getLoaderManager(), paramEsAccount, 1);
    public void init(String paramString) {
        this.mMentionCursorAdapter = new PeopleSearchListAdapter(getContext(), 1);
//        this.mMentionCursorAdapter.setPublicProfileSearchEnabled(true);
//        this.mMentionCursorAdapter.setIncludePlusPages(true);
//        this.mMentionCursorAdapter.setMention(paramString);
        adjustInputMethod();
        setAdapter(this.mMentionCursorAdapter);
        setTokenizer(new MentionTokenizer());
        setThreshold(3);
        addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable paramEditable) {
            }

            public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
                Spannable localSpannable;
                URLSpan[] arrayOfURLSpan;
                int i;
                if ((paramCharSequence instanceof Spannable)) {
                    localSpannable = (Spannable) paramCharSequence;
                    arrayOfURLSpan = localSpannable.getSpans(paramInt1, -1 + (paramInt1 + paramInt2), URLSpan.class);
                    i = arrayOfURLSpan.length;

                    for (int j = 0; ; j++) {
                        if (j >= i)
                            return;

                        URLSpan localURLSpan = arrayOfURLSpan[j];
                        if (!MentionSpan.isMention(localURLSpan))
                            continue;
                        localSpannable.removeSpan(localURLSpan);
                    }
                }
            }

            public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
            }
        });
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mMentionCursorAdapter != null)
            this.mMentionCursorAdapter.onStart();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mMentionCursorAdapter != null)
            this.mMentionCursorAdapter.onStop();
    }

    public void onRestoreInstanceState(Parcelable paramParcelable) {
        int i = 0;
        SavedState localSavedState = (SavedState) paramParcelable;
        super.onRestoreInstanceState(localSavedState.getSuperState());
        if (this.mMentionCursorAdapter != null)
            this.mMentionCursorAdapter.onCreate(localSavedState.adapterState);
        Editable localEditable = getEditableText();
        URLSpan[] arrayOfURLSpan = (URLSpan[]) localEditable.getSpans(0, localEditable.length(), URLSpan.class);
        int j = arrayOfURLSpan.length;
        URLSpan curUrlSpan;
        while (true) {
            if (i >= j)
                return;
            curUrlSpan = arrayOfURLSpan[i];
            if (MentionSpan.isMention(curUrlSpan))
                replaceSpan(localEditable, localSavedState, new MentionSpan(curUrlSpan));
            i++;
        }
    }

    public Parcelable onSaveInstanceState() {
        Parcelable localParcelable = super.onSaveInstanceState();
        Bundle localBundle = null;
        if (this.mMentionCursorAdapter != null) {
            localBundle = new Bundle();
            this.mMentionCursorAdapter.onSaveInstanceState(localBundle);
        }
        return new SavedState(localParcelable, localBundle);
    }

    public void setHtml(String paramString) {
        Spanned localSpanned = Html.fromHtml(paramString);
        Object[] arrayOfObject = localSpanned.getSpans(0, localSpanned.length(), Object.class);
        if (arrayOfObject != null) {
            SpannableStringBuilder localSpannableStringBuilder = new SpannableStringBuilder();
            localSpannableStringBuilder.append(localSpanned);
            Object curObject;
            for (int j = -1 + arrayOfObject.length; ; j--) {
                if (j < 0) {
                    setText(localSpannableStringBuilder);
                    break;
                }
                curObject = arrayOfObject[j];
                int i = localSpannableStringBuilder.getSpanStart(curObject);
                int k = localSpannableStringBuilder.getSpanEnd(curObject);
                if (!(curObject instanceof StyleSpan)) {
                    if ((curObject instanceof URLSpan)) {
                        String str = ((URLSpan) curObject).getURL();
                        if ((str == null) || (!IntentUtil.isProfileUrl(str))) {
                            localSpannableStringBuilder.replace(i, k, str);
                        } else {
                            if ((i == 0) || (localSpannableStringBuilder.charAt(i - 1) != '+'))
                                continue;
                            str = IntentUtil.getParameter(str, "pid=");
                            if (str != null)
                                localSpannableStringBuilder.setSpan(new MentionSpan(str), i - 1, k, 0);
                        }
                    }
                } else {
                    int m = ((StyleSpan) curObject).getStyle();
                    if (m != 1) {
                        if (m != 2) {
                            if (m == 3) {
                                localSpannableStringBuilder.insert(k, "*_");
                                localSpannableStringBuilder.insert(i, "_*");
                            }
                        } else {
                            localSpannableStringBuilder.insert(k, "_");
                            localSpannableStringBuilder.insert(i, "_");
                        }
                    } else {
                        localSpannableStringBuilder.insert(k, "*");
                        localSpannableStringBuilder.insert(i, "*");
                    }
                }
                localSpannableStringBuilder.removeSpan(curObject);
            }
        }
        setText(localSpanned.toString());
    }

    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator() {
            public MentionMultiAutoCompleteTextView.SavedState createFromParcel(Parcel paramParcel) {
                return new MentionMultiAutoCompleteTextView.SavedState(paramParcel);
            }

            public MentionMultiAutoCompleteTextView.SavedState[] newArray(int paramInt) {
                return new MentionMultiAutoCompleteTextView.SavedState[paramInt];
            }
        };
        final Bundle adapterState;

        private SavedState(Parcel paramParcel) {
            super(paramParcel);
            this.adapterState = ((Bundle) paramParcel.readParcelable(PeopleSearchResults.class.getClassLoader()));
        }

        SavedState(Parcelable paramParcelable, Bundle paramBundle) {
            super(paramParcelable);
            this.adapterState = paramBundle;
        }

        public String toString() {
            return "MentionMultiAutoComplete.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.adapterState + "}";
        }

        public void writeToParcel(Parcel paramParcel, int paramInt) {
            super.writeToParcel(paramParcel, paramInt);
            paramParcel.writeParcelable(this.adapterState, 0);
        }
    }
}


// todo : decompile Data.Mention from g+
class MentionData {
    static class Builder {
        public void setIndex(int j) {
        }

        public void setLength(int i) {

        }

        public void setAggregateId(String aggregateId) {

        }

        public MentionData build() {
            MentionData data = new MentionData();
            return data;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}
