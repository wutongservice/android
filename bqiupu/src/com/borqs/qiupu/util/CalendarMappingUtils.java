package com.borqs.qiupu.util;

import java.util.Calendar;

import twitter4j.UserCircle;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.text.format.Time;
import android.util.Log;

import com.borqs.account.login.service.ConstData;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;


public class CalendarMappingUtils {

    private static final String TAG = "CalendarMappingUtils";
    public static final String CALENDAR_PACKAGE = "com.android.calendar";
    public static Uri CALENDAR_URL ;
    public static Uri CALENDAR_REMINDERS_URL ;
    public static Uri CALENDAR_EVENTS_URL ;
    public static Uri CALENDAR_CALENDAR_ALERTS_URL;
    
    public static final String _ID = "_id";
    
    private static final long SNOOZE_DELAY = 5 * 60 * 1000L;
    private static final int EVNET_ALARM_MINI = 10;
    

    public static final int DOES_NOT_REPEAT = 0;
    public static final int REPEATS_DAILY = 1;
    public static final int REPEATS_EVERY_WEEKDAY = 2;
    public static final int REPEATS_WEEKLY_ON_DAY = 3;
    public static final int REPEATS_MONTHLY_ON_DAY_COUNT = 4;
    public static final int REPEATS_MONTHLY_ON_DAY = 5;
    public static final int REPEATS_YEARLY = 6;
    public static final int REPEATS_CUSTOM = 7;
    
    static {
    	if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
    		CALENDAR_URL = Uri.parse("content://calendar/calendars");
    		CALENDAR_REMINDERS_URL = Uri.parse("content://calendar/reminders");
    		CALENDAR_EVENTS_URL= Uri.parse("content://calendar/events");
    		CALENDAR_CALENDAR_ALERTS_URL = Uri.parse("content://calendar/calendar_alerts");
    	}else {
    		CALENDAR_URL = Uri.parse("content://com.android.calendar/calendars");
    		CALENDAR_REMINDERS_URL = Uri.parse("content://com.android.calendar/reminders");
    		CALENDAR_EVENTS_URL = Uri.parse("content://com.android.calendar/events");
    		CALENDAR_CALENDAR_ALERTS_URL = Uri.parse("content://com.android.calendar/calendar_alerts");
    	}
    }	
    
    public static ContentValues crateCalendarCv(Context context, Account tmpAccount) {
    	ContentValues cv = new ContentValues();
    	if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			cv.put(Calendars.CALENDAR_DISPLAY_NAME, tmpAccount.name);
	        cv.put(Calendars.ACCOUNT_NAME, tmpAccount.name);
	        cv.put(Calendars.ACCOUNT_TYPE, ConstData.BORQS_ACCOUNT_TYPE);
	        cv.put(Calendars.SYNC_EVENTS, 1);
	        cv.put(Calendars.VISIBLE, 1);
	        cv.put(Calendars.OWNER_ACCOUNT, tmpAccount.name);
	        cv.put(Calendars.CALENDAR_TIME_ZONE, Time.getCurrentTimezone());
	        cv.put(Calendars.CALENDAR_COLOR, context.getResources().getColor(R.color.calendar_color));
	        cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
    	}else {
    		cv.put("displayName", tmpAccount.name);
	        cv.put("_sync_account", tmpAccount.name);
	        cv.put("_sync_account_type", ConstData.BORQS_ACCOUNT_TYPE);
	        cv.put("sync_events", 1);
	        cv.put("hidden", 1);
	        cv.put("ownerAccount", tmpAccount.name);
	        cv.put("timezone", Time.getCurrentTimezone());
	        cv.put("color", context.getResources().getColor(R.color.calendar_color));
	        cv.put("access_level", 700);
    	}
    	return cv;
    }
    
    public static void insertToCalendar(Context context, UserCircle circle, QiupuORM orm) {
//		showDialog(DIALOG_SET_CIRCLE_PROCESS);
		try {
			// query borqs account from calendar
			final int tmpid = ContactUtils.getBorqsIdfromCalendar(context);
			if(tmpid > 0) {
				final ContentResolver cr = context.getContentResolver();
				
				//Insertion on the events of the calendar
				Uri uri = cr.insert(CalendarMappingUtils.CALENDAR_EVENTS_URL, makeEventContentValues(tmpid, circle));
				final long calendarEventId = ContentUris.parseId(uri);
				Log.d(TAG, " insert events : " + calendarEventId);
				
				//insert calendar reminder
				ContentValues reminderCv = new ContentValues();
				reminderCv.put(CalendarContract.Reminders.EVENT_ID, calendarEventId);
				if(circle.mGroup != null && circle.mGroup.reminder_time > 0) {
					reminderCv.put(CalendarContract.Reminders.MINUTES, circle.mGroup.reminder_time);
				}else {
					reminderCv.put(CalendarContract.Reminders.MINUTES, EVNET_ALARM_MINI);
				}
				reminderCv.put(CalendarContract.Reminders.METHOD, 1);
				cr.insert(CalendarMappingUtils.CALENDAR_REMINDERS_URL, reminderCv);

				//insert to calendar CalendarAlerts
				long alarmTime = System.currentTimeMillis() + SNOOZE_DELAY;
				ContentValues alertCv =
                        makeContentValues(calendarEventId, circle.mGroup.startTime, circle.mGroup.endTime, alarmTime, EVNET_ALARM_MINI);
				cr.insert(CalendarMappingUtils.CALENDAR_CALENDAR_ALERTS_URL, alertCv);
				//insert wutong event/calendar event mapping.
				orm.insertEventsCalendar(circle.circleid, calendarEventId, circle.mGroup.updated_time);
				//auto insert to calendar , no need show toast
//				ToastUtil.showShortToast(this, mHandler, R.string.import_to_calender_successful);
			}else {
				Log.d(TAG, "calendar don't have this account");
			}
			
		} catch (Exception e) {
			Log.d(TAG, "insert to Calender failed " + e.getMessage());
		}finally {
//			dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
		}
    }
    
    private static ContentValues makeEventContentValues(long calendarid, UserCircle circle) {
		ContentValues cv = new ContentValues();
		cv.put(CalendarContract.Events.CALENDAR_ID, calendarid);
		cv.put(CalendarContract.Events.TITLE, circle.name);
		cv.put(CalendarContract.Events.DESCRIPTION, circle.description );
		cv.put(CalendarContract.Events.EVENT_TIMEZONE, Time.getCurrentTimezone());
		cv.put(CalendarContract.Events.EVENT_LOCATION, circle.location);
		
		if(circle.mGroup != null) {
			cv.put(CalendarContract.Events.DTSTART, circle.mGroup.startTime);
			if(circle.mGroup.endTime <= 0) {
				cv.put(CalendarContract.Events.DTEND, circle.mGroup.startTime);	
			}else {
				cv.put(CalendarContract.Events.DTEND, circle.mGroup.endTime);
			}
			Time startTime = new Time();
			startTime.set(circle.mGroup.startTime);
			String rrule = updateRecurrenceRule(circle.mGroup.repeat_type, getFirstDayOfWeek() + 1, startTime);
			cv.put(CalendarContract.Events.RRULE, rrule);
		
			if(circle.mGroup.creator != null) {
				cv.put(CalendarContract.Events.ORGANIZER, circle.mGroup.creator.nick_name);
			}
		}
		return cv;
	}
    
    private static ContentValues makeContentValues(long eventId, long begin, long end,
            long alarmTime, int minutes) {
		ContentValues values = new ContentValues();
//		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			values.put(CalendarContract.CalendarAlerts.EVENT_ID, eventId);
			values.put(CalendarContract.CalendarAlerts.BEGIN, begin);
			values.put(CalendarContract.CalendarAlerts.END, end);
			values.put(CalendarContract.CalendarAlerts.ALARM_TIME, alarmTime);
			long currentTime = System.currentTimeMillis();
			values.put(CalendarContract.CalendarAlerts.CREATION_TIME, currentTime);
			values.put(CalendarContract.CalendarAlerts.RECEIVED_TIME, 0);
			values.put(CalendarContract.CalendarAlerts.NOTIFY_TIME, 0);
			values.put(CalendarContract.CalendarAlerts.STATE, CalendarContract.CalendarAlerts.STATE_SCHEDULED);
			values.put(CalendarContract.CalendarAlerts.MINUTES, minutes);
//		}
//		else {
//			values.put("event_id", eventId);
//			values.put("begin", begin);
//			values.put("end", end);
//			values.put("alarmTime", alarmTime);
//			long currentTime = System.currentTimeMillis();
//			values.put("creationTime", currentTime);
//			values.put("receivedTime", 0);
//			values.put("notifyTime", 0);
//			values.put("state", CalendarContract.CalendarAlerts.STATE_SCHEDULED);
//			values.put("minutes", minutes);
//		}
        return values;
    }
    
    public static boolean isImportedToCalendar(Context context, long id) {
		boolean ret = false;
		Cursor myCursor = null;
		try {
			String where = CalendarMappingUtils._ID + " = "+id;
			myCursor = context.getContentResolver().query(CALENDAR_EVENTS_URL, new String[]{_ID}, where, null, null);
			if(myCursor != null) {
				if(myCursor.getCount() > 0) {
					ret = true;
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "isImportedToCalendar, exception: " + e.getMessage());
		} finally {
			if(myCursor != null) {
				myCursor.close();
				myCursor = null;
			}
		}
		return ret;
	}
    
    public static void removeCalendarEvent(Context context, long id) {
    	if(checkApkExist(context)) {
    		try {
    			String where = CalendarContract.Events._ID + " = "+id;
    			ContentResolver cr = context.getContentResolver();
    			int count = cr.delete(CALENDAR_EVENTS_URL, where, null);
    			Log.i(TAG, "remove calendar event : " + count);
    			String reminderWhere = CalendarContract.Reminders.EVENT_ID + " = "+id;
    			cr.delete(CALENDAR_REMINDERS_URL, reminderWhere, null);
    			String alertWhere = CalendarContract.CalendarAlerts.EVENT_ID + " = "+id;
    			cr.delete(CALENDAR_CALENDAR_ALERTS_URL, alertWhere, null);
			} catch (Exception e) {
				Log.d(TAG, "removeCalendarEvent, exception: " + e.getMessage());
			}
    	}else {
    		Log.d(TAG, "Calendar is not exist");
    	}
	}
    
    public static void removeCalendarEvents(Context context, String ids) {
    	if(checkApkExist(context)) {
    		try {
    			String where = CalendarContract.Events._ID + " in ("+ids + ")";
    			ContentResolver cr = context.getContentResolver();
    			int count = cr.delete(CALENDAR_EVENTS_URL, where, null);
    			Log.i(TAG, "remove calendar event : " + count);
    			String reminderWhere = CalendarContract.Reminders.EVENT_ID + " in ("+ids + ")";
    			cr.delete(CALENDAR_REMINDERS_URL, reminderWhere, null);
    			String alertWhere = CalendarContract.CalendarAlerts.EVENT_ID + " in ("+ids + ")";
    			cr.delete(CALENDAR_CALENDAR_ALERTS_URL, alertWhere, null);
    		} catch (Exception e) {
    			Log.d(TAG, "removeCalendarEvents, exception: " + e.getMessage());	
    		}
    	}else {
    		Log.d(TAG, "Calendar is not exist");
    	}
	}
    
    public static boolean checkApkExist(Context context) {
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
			return false;
		}
		try {
			context.getPackageManager().getApplicationInfo(CALENDAR_PACKAGE,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (Exception e) {
			Log.d(TAG, "check calendar exist exception: " + e.getMessage());
			return false;
		}
	}
    
    public static void importEventTocalendar(Context context, UserCircle circle, QiupuORM orm) {
    	if(CalendarMappingUtils.checkApkExist(context)) {
    		long calendarEventId = orm.getEventCalendarid(circle.circleid);
    		if(isImportedToCalendar(context, calendarEventId)) {
    			removeCalendarEvent(context, calendarEventId);
    		}
    		insertToCalendar(context, circle, orm);
    	}else {
    		Log.d(TAG, "calendar is not exist");
    	}
	}
    
    public static String updateRecurrenceRule(int selection, int weekStart, Time startTime) {
    	// Make sure we don't have any leftover data from the previous setting
    	EventRecurrence eventRecurrence = new EventRecurrence();
    	
    	if (selection == DOES_NOT_REPEAT) {
    		return "";
    	} else if (selection == REPEATS_CUSTOM) {
    		// Keep custom recurrence as before.
    		return "";
    	} else if (selection == REPEATS_DAILY) {
    		eventRecurrence.freq = EventRecurrence.DAILY;
    	} else if (selection == REPEATS_EVERY_WEEKDAY) {
    		eventRecurrence.freq = EventRecurrence.WEEKLY;
    		int dayCount = 5;
    		int[] byday = new int[dayCount];
    		int[] bydayNum = new int[dayCount];
    		
    		byday[0] = EventRecurrence.MO;
    		byday[1] = EventRecurrence.TU;
    		byday[2] = EventRecurrence.WE;
    		byday[3] = EventRecurrence.TH;
    		byday[4] = EventRecurrence.FR;
    		for (int day = 0; day < dayCount; day++) {
    			bydayNum[day] = 0;
    		}
    		
    		eventRecurrence.byday = byday;
    		eventRecurrence.bydayNum = bydayNum;
    		eventRecurrence.bydayCount = dayCount;
    	} else if (selection == REPEATS_WEEKLY_ON_DAY) {
    		eventRecurrence.freq = EventRecurrence.WEEKLY;
    		int[] days = new int[1];
    		int dayCount = 1;
    		int[] dayNum = new int[dayCount];
//    		Time startTime = new Time(model.mTimezone);
//    		startTime.set(model.mStart);
    		
    		days[0] = EventRecurrence.timeDay2Day(startTime.weekDay);
    		// not sure why this needs to be zero, but set it for now.
    		dayNum[0] = 0;
    		
    		eventRecurrence.byday = days;
    		eventRecurrence.bydayNum = dayNum;
    		eventRecurrence.bydayCount = dayCount;
    	} else if (selection == REPEATS_MONTHLY_ON_DAY) {
    		eventRecurrence.freq = EventRecurrence.MONTHLY;
    		eventRecurrence.bydayCount = 0;
    		eventRecurrence.bymonthdayCount = 1;
    		int[] bymonthday = new int[1];
//    		Time startTime = new Time(model.mTimezone);
//    		startTime.set(model.mStart);
    		bymonthday[0] = startTime.monthDay;
    		eventRecurrence.bymonthday = bymonthday;
    	} else if (selection == REPEATS_MONTHLY_ON_DAY_COUNT) {
    		eventRecurrence.freq = EventRecurrence.MONTHLY;
    		eventRecurrence.bydayCount = 1;
    		eventRecurrence.bymonthdayCount = 0;
    		
    		int[] byday = new int[1];
    		int[] bydayNum = new int[1];
//    		Time startTime = new Time(model.mTimezone);
//    		startTime.set(model.mStart);
//    		 Compute the week number (for example, the "2nd" Monday)
    		int dayCount = 1 + ((startTime.monthDay - 1) / 7);
    		if (dayCount == 5) {
    			dayCount = -1;
    		}
    		bydayNum[0] = dayCount;
    		byday[0] = EventRecurrence.timeDay2Day(startTime.weekDay);
    		eventRecurrence.byday = byday;
    		eventRecurrence.bydayNum = bydayNum;
    	} else if (selection == REPEATS_YEARLY) {
    		eventRecurrence.freq = EventRecurrence.YEARLY;
    	}
    	
    	// Set the week start day.
    	eventRecurrence.wkst = EventRecurrence.calendarDay2Day(weekStart);
    	return eventRecurrence.toString();
    }
    
    public static int getFirstDayOfWeek() {
//        SharedPreferences prefs = GeneralPreferences.getSharedPreferences(context);
//        String pref = prefs.getString(
//                GeneralPreferences.KEY_WEEK_START_DAY, GeneralPreferences.WEEK_START_DEFAULT);

        int startDay;
//        if (GeneralPreferences.WEEK_START_DEFAULT.equals(pref)) {
            startDay = Calendar.getInstance().getFirstDayOfWeek();
            Log.d(TAG, "getFirstDayofWeek: " + startDay);
//        } else {
//            startDay = Integer.parseInt(pref);
//        }

        if (startDay == Calendar.SATURDAY) {
            return Time.SATURDAY;
        } else if (startDay == Calendar.MONDAY) {
            return Time.MONDAY;
        } else {
            return Time.SUNDAY;
        }
    }
}
