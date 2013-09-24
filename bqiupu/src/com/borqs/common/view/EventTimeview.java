package com.borqs.common.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

import twitter4j.UserCircle;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.borqs.qiupu.R;
import com.borqs.qiupu.util.CalendarMappingUtils;
import com.borqs.qiupu.util.EventRecurrence;

public class EventTimeview extends SNSItemView {

    private static final String TAG = "EventTimeview";
    private Context mContext;
    private View mStart_date_rl;
	private View mStart_time_rl;
	private View mEnd_date_rl;
	private View mEnd_time_rl;
	private TextView mStartDateView;
	private TextView mStartTimeView;
	private TextView mEndDateView;
	private TextView mEndTimeView;
	private Spinner mRepeatsSpinner;
	private Spinner mReminderSpinner;
	
	public Time mStartTime;
	public Time mEndTime;
	public long mStartMillis;
	public long mEndMillis;
	public int mRepeat_type ;
	public int mReminderTime;
	private static StringBuilder mSB = new StringBuilder(50);
	private static Formatter mF = new Formatter(mSB, Locale.getDefault());
	private UserCircle mCircle;
	private int[] remindersvalue;
	private EventRecurrence mEventRecurrence = new EventRecurrence();
	private ArrayList<Integer> mRecurrenceIndexes = new ArrayList<Integer>(0);
	ArrayList<Integer> recurrenceIndexes = new ArrayList<Integer>(0);
	
	private final static int DEFAULT_REMINDER_TIME = 10; 
	
    public EventTimeview(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }
    
    public EventTimeview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

    public EventTimeview(Context context, UserCircle circle) {
        super(context);
        mContext = context;
        mCircle = circle;
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
        View contentView = factory.inflate(R.layout.event_time_ui, null);
        addView(contentView);

        mStartDateView = (TextView) contentView.findViewById(R.id.start_date);
    	mStartTimeView = (TextView) contentView.findViewById(R.id.start_time);
    	
    	mEndDateView = (TextView) contentView.findViewById(R.id.end_date);
    	mEndTimeView = (TextView) contentView.findViewById(R.id.end_time);
    	mStart_date_rl = contentView.findViewById(R.id.start_date_rl);
        mStart_time_rl = contentView.findViewById(R.id.start_time_rl);
        mEnd_date_rl = contentView.findViewById(R.id.end_date_rl);
        mEnd_time_rl = contentView.findViewById(R.id.end_time_rl);
        
        remindersvalue = mContext.getResources().getIntArray(R.array.reminder_minutes_values);
        
        mRepeatsSpinner = (Spinner) contentView.findViewById(R.id.calendars_spinner);
        mRepeatsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

        	
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mRepeat_type = recurrenceIndexes.get(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
        
        mReminderSpinner = (Spinner) contentView.findViewById(R.id.calendars__reminder_spinner);
        mReminderSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mReminderTime = remindersvalue[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
        setUI();
    }

    private void setUI() {
    	if(mStartTime == null) {
    		mStartTime = new Time();
    	}
    	
    	if(mEndTime == null) {
    		mEndTime = new Time();
    	}
    	
    	if(mCircle == null) {
        	
    		mStartMillis =System.currentTimeMillis(); 
    		mStartTime.set(mStartMillis);
    		
    		mEndMillis = mStartMillis + DateUtils.HOUR_IN_MILLIS;
    		mEndTime.set(mEndMillis);
    		mRepeat_type = CalendarMappingUtils.DOES_NOT_REPEAT;
			mReminderTime = DEFAULT_REMINDER_TIME;
    	}else {
    		if(mCircle.mGroup != null) {
    			mStartMillis =mCircle.mGroup.startTime; 
    			mStartTime.set(mStartMillis);
    			
    			mEndMillis = mCircle.mGroup.endTime;
    			if(mEndMillis > 0) {
    				mEndTime.set(mEndMillis);
    			}else {
    				mEndTime.set(mStartMillis + DateUtils.HOUR_IN_MILLIS);
    			}
    			mRepeat_type = mCircle.mGroup.repeat_type;
    			mReminderTime = mCircle.mGroup.reminder_time;
    		}
    	}
    	initReminder();
        populateWhen();
        populateRepeats();
    }
    
    public void setContent(UserCircle circle) {
    	if(circle != null) {
    		mCircle = circle;
    		setUI();
    	}
    }
    
    private class DateClickListener implements View.OnClickListener {
        private Time mTime;

        public DateClickListener(Time time) {
            mTime = time;
        }

        public void onClick(View v) {
        	DatePickerDialog dialog = new DatePickerDialog(mContext, new DateListener(v), mTime.year,
                    mTime.month, mTime.monthDay);
        	if(v == mEnd_date_rl) {
        		dialog.setButton2(mContext.getString(R.string.clear), new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
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
        	TimePickerDialog dialog = new TimePickerDialog(mContext, new TimeListener(v),
                    mTime.hour, mTime.minute,
                    DateFormat.is24HourFormat(mContext));
        	if(v == mEnd_time_rl) {
        		dialog.setButton2(mContext.getString(R.string.clear), new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
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

        if(mEndMillis > 0) {
        	long endMillis = mEndTime.toMillis(false /* use isDst */);
        	setDate(mEndDateView, endMillis);
        	setTime(mEndTimeView, endMillis);
        }else {
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

                // Move to the next day if the end time is before the start time.
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
//                populateRepeats();
            } else {
                // The end date was changed.
                startMillis = startTime.toMillis(true);
                endTime.year = year;
                endTime.month = month;
                endTime.monthDay = monthDay;
                endMillis = endTime.normalize(true);

                // Do not allow an event to have an end time before the start time.
                if (endTime.before(startTime)) {
                    endTime.set(startTime);
                    endMillis = startMillis;
                }
            }

            setDate(mStartDateView, startMillis);
            setDate(mEndDateView, endMillis);
            setTime(mEndTimeView, endMillis); // In case end time had to be reset
            
            mStartMillis = startMillis;
            mEndMillis = endMillis;
            populateRepeats();
            
            Log.d(TAG, "setDate : " + mStartMillis + " " + mEndMillis);
        }
    }
    
    private void setDate(TextView view, long millis) {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR |
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH |
                DateUtils.FORMAT_ABBREV_WEEKDAY;
        mSB.setLength(0);
        Log.d(TAG, "setDate: " + TimeZone.getDefault().getDisplayName());
        String dateString = "";
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
        	dateString = DateUtils.formatDateRange(mContext, mF, millis, millis, flags, Time.getCurrentTimezone()).toString();
        }else {
        	dateString = DateUtils.formatDateRange(mContext, millis, millis, flags);
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
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
        	timeString = DateUtils.formatDateRange(mContext, mF, millis, millis, flags, Time.getCurrentTimezone()).toString();
        }else {
        	timeString = DateUtils.formatDateRange(mContext, millis, millis, flags);
        }
        view.setText(timeString);
    }
    
    public long getStartMillis() {
    	return mStartMillis;
    }
    public long getEndMillis() {
    	return mEndMillis;
    }
    public int getRepeatType() {
    	return mRepeat_type;
    }
    
    private void populateRepeats() {
        Time time = mStartTime;
        Resources r = mContext.getResources();

        String[] days = new String[] {
                DateUtils.getDayOfWeekString(Calendar.SUNDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.MONDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.TUESDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.WEDNESDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.THURSDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.FRIDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.SATURDAY, DateUtils.LENGTH_MEDIUM), };
        String[] ordinals = r.getStringArray(R.array.ordinal_labels);

        // Only display "Custom" in the spinner if the device does not support
        // the recurrence functionality of the event. Only display every weekday
        // if the event starts on a weekday.
//        boolean isCustomRecurrence = isCustomRecurrence();
        boolean isWeekdayEvent = isWeekdayEvent();

        ArrayList<String> repeatArray = new ArrayList<String>(0);

        repeatArray.add(r.getString(R.string.does_not_repeat));
        recurrenceIndexes.add(CalendarMappingUtils.DOES_NOT_REPEAT);

        repeatArray.add(r.getString(R.string.daily));
        recurrenceIndexes.add(CalendarMappingUtils.REPEATS_DAILY);

        if (isWeekdayEvent) {
            repeatArray.add(r.getString(R.string.every_weekday));
            recurrenceIndexes.add(CalendarMappingUtils.REPEATS_EVERY_WEEKDAY);
        }

        String format = r.getString(R.string.weekly);
        repeatArray.add(String.format(format, time.format("%A")));
        recurrenceIndexes.add(CalendarMappingUtils.REPEATS_WEEKLY_ON_DAY);

        // Calculate whether this is the 1st, 2nd, 3rd, 4th, or last appearance
        // of the given day.
        int dayNumber = (time.monthDay - 1) / 7;
        format = r.getString(R.string.monthly_on_day_count);
        repeatArray.add(String.format(format, ordinals[dayNumber], days[time.weekDay]));
        recurrenceIndexes.add(CalendarMappingUtils.REPEATS_MONTHLY_ON_DAY_COUNT);

        format = r.getString(R.string.monthly_on_day);
        repeatArray.add(String.format(format, time.monthDay));
        recurrenceIndexes.add(CalendarMappingUtils.REPEATS_MONTHLY_ON_DAY);

        long when = time.toMillis(false);
        format = r.getString(R.string.yearly);
        int flags = 0;
        if (DateFormat.is24HourFormat(mContext)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        repeatArray.add(String.format(format, DateUtils.formatDateTime(mContext, when, flags)));
        recurrenceIndexes.add(CalendarMappingUtils.REPEATS_YEARLY);

//        if (isCustomRecurrence) {
//            repeatArray.add(r.getString(R.string.custom));
//            recurrenceIndexes.add(CalendarMappingUtils.REPEATS_CUSTOM);
//        }
        mRecurrenceIndexes = recurrenceIndexes;

        int position = recurrenceIndexes.indexOf(CalendarMappingUtils.DOES_NOT_REPEAT);
//        if (!TextUtils.isEmpty(mModel.mRrule)) {
            if (mCircle != null) {
                position = mCircle.mGroup.repeat_type;
            } else {
                switch (mEventRecurrence.freq) {
                    case EventRecurrence.DAILY:
                        position = recurrenceIndexes.indexOf(CalendarMappingUtils.REPEATS_DAILY);
                        break;
                    case EventRecurrence.WEEKLY:
                        if (mEventRecurrence.repeatsOnEveryWeekDay()) {
                            position = recurrenceIndexes.indexOf(
                            		CalendarMappingUtils.REPEATS_EVERY_WEEKDAY);
                        } else {
                            position = recurrenceIndexes.indexOf(
                            		CalendarMappingUtils.REPEATS_WEEKLY_ON_DAY);
                        }
                        break;
                    case EventRecurrence.MONTHLY:
                        if (mEventRecurrence.repeatsMonthlyOnDayCount()) {
                            position = recurrenceIndexes.indexOf(
                            		CalendarMappingUtils.REPEATS_MONTHLY_ON_DAY_COUNT);
                        } else {
                            position = recurrenceIndexes.indexOf(
                            		CalendarMappingUtils.REPEATS_MONTHLY_ON_DAY);
                        }
                        break;
                    case EventRecurrence.YEARLY:
                        position = recurrenceIndexes.indexOf(CalendarMappingUtils.REPEATS_YEARLY);
                        break;
                }
            }
//        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                R.layout.event_spinner_textview, repeatArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRepeatsSpinner.setAdapter(adapter);
        mRepeatsSpinner.setSelection(position);

        // Don't allow the user to make exceptions recurring events.
//        if (mModel.mOriginalSyncId != null) {
//            mRepeatsSpinner.setEnabled(false);
//        }
    }
    
    private void initReminder() {
    	String[] reminders = mContext.getResources().getStringArray(R.array.reminder_minutes_labels);
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
    			R.layout.event_spinner_textview, reminders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mReminderSpinner.setAdapter(adapter);
        if(mCircle != null && mCircle.mGroup != null) {
        	mReminderSpinner.setSelection(getReminderSelectPos(mCircle.mGroup.reminder_time));
        }else {
        	mReminderSpinner.setSelection(getReminderSelectPos(DEFAULT_REMINDER_TIME));
        }
    }
    private int getReminderSelectPos(int value) {
    	int pos = 0;
    	for(int i=0; i<remindersvalue.length ; i++) {
    		if(value == remindersvalue[i]) {
    			pos = i;
    		}
    	}
    	return pos;
    }
    
    private boolean isCustomRecurrence() {

        if (mEventRecurrence.until != null
                || (mEventRecurrence.interval != 0 && mEventRecurrence.interval != 1)
                || mEventRecurrence.count != 0) {
            return true;
        }

        if (mEventRecurrence.freq == 0) {
            return false;
        }

        switch (mEventRecurrence.freq) {
            case EventRecurrence.DAILY:
                return false;
            case EventRecurrence.WEEKLY:
                if (mEventRecurrence.repeatsOnEveryWeekDay() && isWeekdayEvent()) {
                    return false;
                } else if (mEventRecurrence.bydayCount == 1) {
                    return false;
                }
                break;
            case EventRecurrence.MONTHLY:
                if (mEventRecurrence.repeatsMonthlyOnDayCount()) {
                    /* this is a "3rd Tuesday of every month" sort of rule */
                    return false;
                } else if (mEventRecurrence.bydayCount == 0
                        && mEventRecurrence.bymonthdayCount == 1
                        && mEventRecurrence.bymonthday[0] > 0) {
                    /* this is a "22nd day of every month" sort of rule */
                    return false;
                }
                break;
            case EventRecurrence.YEARLY:
                return false;
        }

        return true;
    }
    
    private boolean isWeekdayEvent() {
        if (mStartTime.weekDay != Time.SUNDAY && mStartTime.weekDay != Time.SATURDAY) {
            return true;
        }
        return false;
    }
}


