package com.borqs.common.view;

import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

import twitter4j.UserCircle;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.borqs.qiupu.R;

public class PollTimeview extends SNSItemView {

    private static final String  TAG                   = "PollTimeview";
    private Context              mContext;
    private View                 mStart_date_rl;
    private View                 mStart_time_rl;
    private View                 mEnd_date_rl;
    private View                 mEnd_time_rl;
    private TextView             mStartDateView;
    private TextView             mStartTimeView;
    private TextView             mEndDateView;
    private TextView             mEndTimeView;

    public Time                  mStartTime;
    public Time                  mEndTime;
    public long                  mStartMillis;
    public long                  mEndMillis;
    private static StringBuilder mSB                   = new StringBuilder(50);
    private static Formatter     mF                    = new Formatter(
                                                               mSB,
                                                               Locale.getDefault());

    private final static int     DEFAULT_REMINDER_TIME = 10;

    public PollTimeview(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public PollTimeview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public PollTimeview(Context context, UserCircle circle) {
        super(context);
        mContext = context;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        removeAllViews();
        LayoutInflater factory = LayoutInflater.from(mContext);
        View contentView = factory.inflate(R.layout.poll_time_ui, null);
        addView(contentView);

        mStartDateView = (TextView) contentView.findViewById(R.id.start_date);
        mStartTimeView = (TextView) contentView.findViewById(R.id.start_time);

        mEndDateView = (TextView) contentView.findViewById(R.id.end_date);
        mEndTimeView = (TextView) contentView.findViewById(R.id.end_time);
        mStart_date_rl = contentView.findViewById(R.id.start_date_rl);
        mStart_time_rl = contentView.findViewById(R.id.start_time_rl);
        mEnd_date_rl = contentView.findViewById(R.id.end_date_rl);
        mEnd_time_rl = contentView.findViewById(R.id.end_time_rl);

        setUI();
    }

    private void setUI() {
        if (mStartTime == null) {
            mStartTime = new Time();
        }

        if (mEndTime == null) {
            mEndTime = new Time();
        }

        mStartMillis = System.currentTimeMillis();
        mStartTime.set(mStartMillis);
//        clearEndData();

        mEndMillis = mStartMillis + DateUtils.DAY_IN_MILLIS;
        mEndTime.set(mEndMillis);

        populateWhen();
    }

    public void setContent() {
        setUI();
    }

    private class DateClickListener implements View.OnClickListener {
        private Time mTime;

        public DateClickListener(Time time) {
            mTime = time;
        }

        public void onClick(View v) {
            DatePickerDialog dialog = new DatePickerDialog(mContext,
                    new DateListener(v), mTime.year, mTime.month,
                    mTime.monthDay);
            if (v == mEnd_date_rl) {
                dialog.setButton2(mContext.getString(R.string.clear),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                clearEndData();
                            }
                        });
            }
            dialog.show();
        }
    }

    private class TimeClickListener implements View.OnClickListener {
        private Time mTime;

        public TimeClickListener(Time time) {
            mTime = time;
        }

        public void onClick(View v) {
            TimePickerDialog dialog = new TimePickerDialog(mContext,
                    new TimeListener(v), mTime.hour, mTime.minute,
                    DateFormat.is24HourFormat(mContext));
            if (v == mEnd_time_rl) {
                dialog.setButton2(mContext.getString(R.string.clear),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                clearEndData();
                            }
                        });
            }
            dialog.show();
        }
    }

    private void populateWhen() {
        long startMillis = mStartTime.toMillis(false /* use isDst */);
        setDate(mStartDateView, startMillis);
        setTime(mStartTimeView, startMillis);

        if (mEndMillis > 0) {
            long endMillis = mEndTime.toMillis(false /* use isDst */);
//            setDate(mEndDateView, endMillis);
//            setTime(mEndTimeView, endMillis);
        } else {
            clearEndData();
        }

        mStart_date_rl.setOnClickListener(new DateClickListener(mStartTime));
        mEnd_date_rl.setOnClickListener(new DateClickListener(mEndTime));

        mStart_time_rl.setOnClickListener(new TimeClickListener(mStartTime));
        mEnd_time_rl.setOnClickListener(new TimeClickListener(mEndTime));
    }

    private void clearEndData() {
        mEndDateView.setText("");
        mEndTimeView.setText("");
        mEndMillis = 0;
    }

    private class TimeListener implements TimePickerDialog.OnTimeSetListener {
        private View mView;

        public TimeListener(View view) {
            mView = view;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Cache the member variables locally to avoid inner class overhead.
            Time startTime = mStartTime;
            Time endTime = mEndTime;

            // Cache the start and end millis so that we limit the number
            // of calls to normalize() and toMillis(), which are fairly
            // expensive.
            long startMillis;
            long endMillis;
            if (mView == mStart_time_rl) {
                // The start time was changed.
                int hourDuration = endTime.hour - startTime.hour;
                int minuteDuration = endTime.minute - startTime.minute;

                startTime.hour = hourOfDay;
                startTime.minute = minute;
                startMillis = startTime.normalize(true);

                // Also update the end time to keep the duration constant.
                endTime.hour = hourOfDay + hourDuration;
                endTime.minute = minute + minuteDuration;
            } else {
                // The end time was changed.
                startMillis = startTime.toMillis(true);
                endTime.hour = hourOfDay;
                endTime.minute = minute;

                // Move to the next day if the end time is before the start
                // time.
                if (endTime.before(startTime)) {
                    endTime.monthDay = startTime.monthDay + 1;
                }
            }

            endMillis = endTime.normalize(true);

            setDate(mEndDateView, endMillis);
            setTime(mStartTimeView, startMillis);
            setTime(mEndTimeView, endMillis);

            mStartMillis = startMillis;
            mEndMillis = endMillis;
            Log.d(TAG, "setTime : " + mStartMillis + " " + mEndMillis);
        }
    }

    private class DateListener implements DatePickerDialog.OnDateSetListener {
        View mView;

        public DateListener(View view) {
            mView = view;
        }

        public void onDateSet(DatePicker view, int year, int month, int monthDay) {
            // Cache the member variables locally to avoid inner class overhead.
            Time startTime = mStartTime;
            Time endTime = mEndTime;

            // Cache the start and end millis so that we limit the number
            // of calls to normalize() and toMillis(), which are fairly
            // expensive.
            long startMillis;
            long endMillis;
            if (mView == mStart_date_rl) {
                // The start date was changed.
                int yearDuration = endTime.year - startTime.year;
                int monthDuration = endTime.month - startTime.month;
                int monthDayDuration = endTime.monthDay - startTime.monthDay;

                startTime.year = year;
                startTime.month = month;
                startTime.monthDay = monthDay;
                startMillis = startTime.normalize(true);

                // Also update the end date to keep the duration constant.
                endTime.year = year + yearDuration;
                endTime.month = month + monthDuration;
                endTime.monthDay = monthDay + monthDayDuration;
                endMillis = endTime.normalize(true);

                // If the start date has changed then update the repeats.
                // populateRepeats();
            } else {
                // The end date was changed.
                startMillis = startTime.toMillis(true);
                endTime.year = year;
                endTime.month = month;
                endTime.monthDay = monthDay;
                endMillis = endTime.normalize(true);

                // Do not allow an event to have an end time before the start
                // time.
                if (endTime.before(startTime)) {
                    endTime.set(startTime);
                    endMillis = startMillis;
                }
            }

            setDate(mStartDateView, startMillis);
            setDate(mEndDateView, endMillis);
            setTime(mEndTimeView, endMillis); // In case end time had to be
                                              // reset

            mStartMillis = startMillis;
            mEndMillis = endMillis;

            Log.d(TAG, "setDate : " + mStartMillis + " " + mEndMillis);
        }
    }

    private void setDate(TextView view, long millis) {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
                | DateUtils.FORMAT_ABBREV_WEEKDAY;
        mSB.setLength(0);
        Log.d(TAG, "setDate: " + TimeZone.getDefault().getDisplayName());
        String dateString = "";
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
            dateString = DateUtils.formatDateRange(mContext, mF, millis,
                    millis, flags, Time.getCurrentTimezone()).toString();
        } else {
            dateString = DateUtils.formatDateRange(mContext, millis, millis,
                    flags);
        }
        view.setText(dateString);
    }

    private void setTime(TextView view, long millis) {
        int flags = DateUtils.FORMAT_SHOW_TIME;
        if (DateFormat.is24HourFormat(mContext)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        mSB.setLength(0);
        String timeString = "";
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
            timeString = DateUtils.formatDateRange(mContext, mF, millis,
                    millis, flags, Time.getCurrentTimezone()).toString();
        } else {
            timeString = DateUtils.formatDateRange(mContext, millis, millis,
                    flags);
        }
        view.setText(timeString);
    }

    public long getStartMillis() {
        return mStartMillis;
    }

    public long getEndMillis() {
        return mEndMillis;
    }

}