package com.borqs.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.borqs.qiupu.R;

public class ExpendableTextPanel extends LinearLayout {
    final String TAG = "Qiupu.ExpendableTextPanel";

    private static final int THRESHOLD_LINE_COUNT = 4;

    private TextView mIntro;
    private TextView mApp_intro_out_in;
//    private View mDividerLine;

    private int mMaxLineCount = THRESHOLD_LINE_COUNT;

    public ExpendableTextPanel(Context context) {
    	super(context, null);
        init();
    }

    public ExpendableTextPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ExpendableTextPanel(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);        
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        removeAllViews();

        LayoutInflater factory = LayoutInflater.from(getContext());
        View convertView = factory.inflate(R.layout.bpc_expandable_textview, this, true);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
//        addView(convertView);

//        factory.inflate(R.layout.bpc_expandable_textview, this, true).setLayoutParams(
//                new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        mIntro = (TextView) findViewById(R.id.summary_introduction);
        mApp_intro_out_in = (TextView) findViewById(R.id.expandable_toggle);
//        mDividerLine = findViewById(R.id.span_line);

        mApp_intro_out_in.setOnClickListener(toggleListener);

        mIntro.addTextChangedListener(watcher);
    }

    private OnClickListener toggleListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleExpansion();
        }
    };

    private void calculateExpandableStatus() {
        new UpdateAsyncTask().execute(null, null, null);
    }

    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {
            // It's okay to leave this as it is
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            super.onPostExecute(result);
            //DO YOUR TASK HERE, getLineCount(), etc.
            updateExpandableStatus();
        }
    }

    private void updateExpandableStatus() {
        final int lineCount = mIntro.getLineCount();
        if (lineCount > mMaxLineCount) {
            mApp_intro_out_in.setVisibility(View.VISIBLE);
//            mDividerLine.setVisibility(View.VISIBLE);
        } else {
            mApp_intro_out_in.setVisibility(View.GONE);
//            mDividerLine.setVisibility(View.GONE);
        }
    }

    private void toggleExpansion() {
        if (mApp_intro_out_in.getText().toString().equals(getContext().getString(R.string.about_expansion))) {
            mIntro.setMaxLines(mIntro.getLineCount());
            mApp_intro_out_in.setText(R.string.app_intro_in);
        } else {
            mIntro.setMaxLines(mMaxLineCount);
            mApp_intro_out_in.setText(R.string.about_expansion);
        }
    }

    public void initExpandableTextView(int threshold, boolean toggleAll) {
        mMaxLineCount = threshold;
        mIntro.setMaxLines(mMaxLineCount);

        if (toggleAll) {
            mIntro.setOnClickListener(toggleListener);
        }
        mApp_intro_out_in.setVisibility(View.GONE);
//        mDividerLine.setVisibility(View.GONE);
    }

    public void initExpandableTextView(boolean toggleAll) {
        initExpandableTextView(mMaxLineCount, toggleAll);
    }

    public void initExpandableTextView(int threshold) {
        initExpandableTextView(threshold, false);
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            calculateExpandableStatus();
        }
    };


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            calculateExpandableStatus();
        }
    }
}
