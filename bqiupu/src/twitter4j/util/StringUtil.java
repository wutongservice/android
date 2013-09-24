package twitter4j.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    //log

	public static boolean isEmpty(String str)
	{
		return str==null || str.length()==0;
	}
	
	public static int getWordLengthOfString(String s)
	{
		char[] chars = s.toCharArray();
		int len = 0;
		for(int i=0;i<chars.length;i++)
		{
			if(chars[i] < 128 && chars[i] > 0)
			{
				len += 1;
			}
			else
			{
				len +=2;
			}
		}		
		return Math.round(len/2.0f);
	}
	
	public static boolean isValidPwd(String pwd){
		boolean result = true;
		Pattern pwd_pattern = Pattern.compile("^[a-zA-Z0-9]{6,20}$");
		Matcher m = pwd_pattern.matcher(pwd);
		result = m.matches();
		return result;
	}
	
	public static boolean isValidEmail(String email){
		boolean result = true;
		Pattern email_pattern = Pattern.compile(
	            "[a-zA-Z0-9\\+\\.\\_\\%\\-]{1,256}" +
	            "\\@" +
	            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
	            "(" +
	                "\\." +
	                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
	            ")+"
	        );   
		Matcher m = email_pattern.matcher(email);
		result = m.matches();
		return result;
	}
}
