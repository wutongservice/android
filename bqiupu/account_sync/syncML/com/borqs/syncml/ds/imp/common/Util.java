/**
 * Copy right Borqs 2009
 */
package com.borqs.syncml.ds.imp.common;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.borqs.contacts_plus.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	//filtered string in phone number
	private static StringBuilder mFilteredString;
	
	public static String formatDate(long time) {
		Date d = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		return sdf.format(d);
	}
	
	/**
	 * 
	 * Enter the phone number can only support digital,(,),*, #, +, P or p, W or w
	 * 
	 * @param inputString (can't be null)
	 * @return 
	 */
	public static String regexPhone(String inputString){
		if(TextUtils.isEmpty(inputString)){
			return inputString;
		}
		char[] inputs = inputString.toCharArray();
		StringBuffer  correctStr= new StringBuffer(inputs.length);
		// only support digital,(,),*, #, +, P or p, W or w
		Pattern regex = Pattern.compile("([0-9]|\\(|\\)|\\*|#|\\+|P|p|W|w)*");
		Matcher matcher = regex.matcher(inputString);
		if(matcher.matches()){
			return inputString;
		}
		//Traverse and remove non-standard characters
		for(int i=0;i<inputs.length;i++){
			matcher = regex.matcher(String.valueOf(inputs[i]));
			if(matcher.matches()){
				correctStr.append(inputs[i]);
			}else{
				if(mFilteredString == null){
					mFilteredString = new StringBuilder();
				}
				mFilteredString.append(inputs[i]).append(" ");
			}
		}
		return correctStr.toString();
	}
	
	public static void clearFilterString(){
		mFilteredString = null;
	}
	
    public static String getTimeStr(Long time,Context context) {
        if (time == null || time == 0){
            return context.getString(R.string.last_sync_none);
        }

        SimpleDateFormat dsf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dsf2 = new SimpleDateFormat("yyyy-MM-dd a h:mm");
        SimpleDateFormat dsf3 = new SimpleDateFormat("MM-dd a h:mm");
        SimpleDateFormat dsf4 = new SimpleDateFormat(" a h:mm");
        
        Date date = new Date(time);

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        String paramYear = "" + cal1.get(Calendar.YEAR);
        String paramTime = dsf.format(date);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DATE, -1);
        String yesterday = dsf.format(cal2.getTime());

        Calendar cal3 = Calendar.getInstance();
        String nowYear = "" + cal3.get(Calendar.YEAR);
        
        String hoursMinutes = dsf4.format(date);
        
        if (paramTime.equals(dsf.format(new Date()))) {
            return context.getString(R.string.last_sync_today)+hoursMinutes;
        } else if (paramTime.equals(yesterday)) {
            return context.getString(R.string.last_sync_yesterday)+hoursMinutes;
        } else if (paramYear.equals(nowYear)) {
            return dsf3.format(date);
        } else {
            return dsf2.format(date);
        }

    }
    
    public static String getPassTime(long happenTime, Context context) {

        long hours, minutes, seconds, day, week;
        Calendar nowDate = Calendar.getInstance();
        long pass = nowDate.getTimeInMillis() - happenTime;
        String result = context.getString(R.string.send_default_string);

        day = pass / (24 * 60 * 60 * 1000);
        if (day > 0) {
            result = day + context.getString(R.string.send_day_string);
            if (day > 7) {
                week = day / 7;
                if (week > 0) {
                    result = context.getString(R.string.send_week_string);
                }
            }
            return result;
        }

        hours = pass / (60 * 60 * 1000);
        if (hours > 0) {
            result = hours + context.getString(R.string.send_hour_string);
            return result;
        }

        minutes = pass / 60000;
        if (minutes > 0) {
            result = minutes + context.getString(R.string.send_minite_string);
            return result;
        }

        seconds = pass / 1000;
        if (seconds > 0) {
            result = seconds + context.getString(R.string.send_second_string);
            return result;
        }

        return result;
    }
	 
	public static String getImei(Context context) {
        TelephonyManager phoneManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = phoneManager.getDeviceId();
        if (TextUtils.isEmpty(imei)) {
            imei = "";
        }
        return imei;
	}
}
