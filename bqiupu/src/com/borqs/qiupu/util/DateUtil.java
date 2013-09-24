package com.borqs.qiupu.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.borqs.qiupu.R;

public class DateUtil {  
    
    public static boolean isCurrentWeek(long time)
    {
        Date date = new Date(time);
        return isCurrentWeek(date);
    }
    static String[] weekArray;
    static Object lock = new Object();
    public static String[] getWeekArray(Context con)
    {
        synchronized(lock)
        {
            if(weekArray == null)
            {
                weekArray = con.getResources().getStringArray(R.array.entries_week);
            }
        }
        return weekArray;
    }
    public static boolean isCurrentMonth(long time)
    {
        Date date = new Date(time);
        return isCurrentMonth(date);
    }
    
    public static boolean isCurrentWeek(Date date)
    {
        if(isCurrentMonth(date))
        {
            Date now = new Date();
            Calendar ca = Calendar.getInstance(Locale.getDefault());
            ca.setTime(now);
            int curWeek = ca.get(Calendar.WEEK_OF_MONTH);
            ca.setTime(date);
            int parWeek = ca.get(Calendar.WEEK_OF_MONTH);
            return (curWeek==parWeek);
        }
        else
        {
            return false;
        }
        
    }
    
    public static boolean isCurrentMonth(Date date)
    {
        Date now = new Date();
        if(isCurrentYear(date))
        {
             Calendar ca = Calendar.getInstance(Locale.getDefault());
             ca.setTime(now);
             int curMonth = ca.get(Calendar.MONTH);
             ca.setTime(date);
             int parMonth = ca.get(Calendar.MONTH);
             return (curMonth==parMonth);
        }
        else
        {
            return false;
        }
    }
    
    private static boolean isCurrentYear(Date date)
    {
        Date now = new Date();
        return (now.getYear()==date.getYear());
    }
    
    private static boolean isToday(Date date)
    {  
       return DateUtils.isToday(date.getTime());
    }
    
    private static boolean isYesterday(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, +1);
        return DateUtils.isToday(calendar.getTimeInMillis());
    }
    
    public static String converToRelativeTime(Context con,Date date)
    {
        long curtime = System.currentTimeMillis();
        long paramtime = date.getTime();       
        long interval = curtime-paramtime;       
        return calculateTime(con,paramtime,interval);
    }

    public static String converToRelativeTime(Context con,long time)
    {
        long curtime = System.currentTimeMillis();
        long paramtime = time;       
        long interval = curtime-paramtime;       
        return calculateTime(con,paramtime,interval);
    }
    
    public static String converToRelativeTime(Context con,long time,boolean withoutsecond)
    {
        long curtime = System.currentTimeMillis();
        long paramtime = time;       
        long interval = curtime-paramtime;       
        return calculateTime(con,paramtime,interval,withoutsecond);
    }
    
    private static String calculateTime(Context con,long realtime,long interval,boolean withoutsecond)
    {
    	init(con);    	
    	
        if(withoutsecond == false)
        {
            return calculateTime(con,realtime,interval);
        }
        
        Date realDate = new Date(realtime);
        if(interval >0 && interval < 60*1000L)
        {
            return Long.toString(interval/1000L)+ " " + sec;
        }
        else if(interval >=60*1000L && interval <2*60*1000L)
        {
            //>= 1min <2min about a minute ago
            return facebook_time_tracte_min;
        }
        else if( interval >= 2*60*1000L && interval <60*60*1000L)
        {
            //>2 mins <1 hour ? minutes ago
            return Long.toString(interval/(60*1000L))+" "+ min;
        }
        else if(interval >=60*60*1000L && interval < 2*60*60*1000L)
        {
            //>=1hour <2 hour about an hour ago
            return facebook_time_tracte_hour;
        }
        else if(interval >=2*60*60*1000L && interval < 12*60*60*1000L)
        {
            return Long.toString(interval/(60*60*1000L))+ " " +String.format(hour,getamORpm(realDate));
        }
        else if(isToday(realDate))
        { 
            //today
            return facebook_time_tracte_td+" "+ getamORpm(realDate);
        }
        else if(isYesterday(realDate))
        {
            return facebook_time_tracte_yd+" "+getamORpm(realDate);
        }
        else if(isCurrentWeek(realDate))
        {
            //is current week
            String[] array = getWeekArray(con);
            Calendar ca = Calendar.getInstance(Locale.getDefault());
            ca.setTime(realDate);
            int week_index = ca.get(Calendar.DAY_OF_WEEK);
            return array[week_index-1]+" "+getamORpm(realDate);
        }
        else if(isCurrentYear(realDate))
        {  
            //Mar 21 at 2:23pm
            return getMonthDate(realDate,con) + getamORpm(realDate);  
        }
        else 
        {
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,Locale.getDefault());
            Date realdate = new Date(realtime);
            return format.format(realdate);
        }
    }
    
    private static String facebook_time_tracte_min, 
                          min,sec,hour,                          
                          facebook_time_tracte_hour,
                          facebook_time_tracte_td,
                          facebook_time_tracte_yd,
                          facebook_time_tracte_at;
    
    private static int initialized = -1;
    private static void init(Context ctx)
    {
    	if(initialized == -1)
    	{
	    	facebook_time_tracte_min = ctx.getString(R.string.facebook_time_tracte_min);
	    	min  = ctx.getString(R.string.min);
	    	sec  = ctx.getString(R.string.sec);
	    	hour = ctx.getString(R.string.hour);
	    	facebook_time_tracte_hour = ctx.getString(R.string.facebook_time_tracte_hour);
	    	facebook_time_tracte_td   = ctx.getString(R.string.facebook_time_tracte_td);
	    	facebook_time_tracte_at   = ctx.getString(R.string.facebook_time_tracte_at);
	    	facebook_time_tracte_yd   = ctx.getString(R.string.facebook_time_tracte_yd);
	    	initialized = 1;
    	}
    }
    
    private static String calculateTime(Context con,long realtime,long interval)
    {
    	init(con);    	
    	
        Date realDate = new Date(realtime);
        if(interval >0 && interval < 60*1000L)
        {
            return Long.toString(interval/1000L)+ " " + sec;
        }
        else if(interval >=60*1000L && interval <2*60*1000L)
        {
            //>= 1min <2min about a minute ago
            return facebook_time_tracte_min;
        }
        else if( interval >= 2*60*1000L && interval <60*60*1000L)
        {
            //>2 mins <1 hour ? minutes ago
            return Long.toString(interval/(60*1000L))+" " + min;
        }
        else if(interval >=60*60*1000L && interval < 2*60*60*1000L)
        {
            //>=1hour <2 hour about an hour ago
            return facebook_time_tracte_hour;
        }
        else if(interval >=2*60*60*1000L && interval < 12*60*60*1000L)
        {
            //>2 hours < 12 hours ? hours ago
            return Long.toString(interval/(60*60*1000L))+ " " +String.format(hour,getamORpm(realDate));
        }
        else if(isToday(realDate))
        { 
            //today
            return facebook_time_tracte_td+" "+ getamORpm(realDate);
        }
        else if(isYesterday(realDate))
        {
            return facebook_time_tracte_yd+" "+ getamORpm(realDate);
        }
        else if(isCurrentWeek(realDate))
        {
            //is current week
            String[] array = getWeekArray(con);
            Calendar ca = Calendar.getInstance(Locale.getDefault());
            ca.setTime(realDate);
            int week_index = ca.get(Calendar.DAY_OF_WEEK);
            return array[week_index-1]+ " " +getamORpm(realDate);
        }
        else if(isCurrentYear(realDate))
        {  
            //Mar 21 at 2:23pm
            return getMonthDate(realDate,con) + getamORpm(realDate);  
        }
        else 
        {
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,Locale.getDefault());
            Date realdate = new Date(realtime);
            return format.format(realdate);
        }   
        
    }
    
    private static String getMonthDate(Date realdate,Context con)
    {
    	init(con);
    	
        if("zh".equals(Locale.getDefault().getLanguage()))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd ",Locale.getDefault());
            return sdf.format(realdate);
        }
        else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d ",Locale.getDefault());
            return sdf.format(realdate) + facebook_time_tracte_at+" ";    
        }  
    }
    
    private static String getamORpm(Date realdate)
    {
        DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        return format.format(realdate);
    }
    public static String calculateCurTimeForNote()
    {
        Date realdate = new Date();
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,Locale.getDefault());
        String str = format.format(realdate);
        return str;
    }
    
    public static int getPSTDate(Date date)
    {
    	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"), Locale.US);
    	calendar.setTime(date);
    	int day = calendar.get(Calendar.DATE);
    	return day;
    }
    
    
    public static long getCurrentTimeForEvent()
    {
        SimpleDateFormat objSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateStr = objSdf.format(new Date());
        objSdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        long now = System.currentTimeMillis();
        try{
            now = objSdf.parse(dateStr).getTime();
        }catch(Exception e)
        {
            
        }
        
        return now;
    }
}
