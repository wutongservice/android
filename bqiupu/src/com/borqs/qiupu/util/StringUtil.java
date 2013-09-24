package com.borqs.qiupu.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.QiupuAccountInfo.Address.AddressInfo;
import twitter4j.QiupuUser;
import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import com.borqs.qiupu.R;

public class StringUtil {
	private static final String TAG = "StringUtil";
	public static boolean isValidString(String str) {
		if(str == null || str.equals("") || str.equals("null")) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isValidRating(String str)
	{
		if(str == null || str.equals("") || str.equals("null") || str.equals("0.0")) {
			return false;
		}
		
		return true;
	}
	

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static int getWordLengthOfString(String s) {
		char[] chars = s.toCharArray();
		int len = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] < 128 && chars[i] > 0) {
				len += 1;
			} else {
				len += 2;
			}
		}
		return Math.round(len / 2.0f);
	}

	public static boolean isValidPwd(String password) {
		// boolean result = true;
		// Pattern pwd_pattern = Pattern.compile("^[a-zA-Z0-9]{6,20}$");
		// Matcher m = pwd_pattern.matcher(pwd);
		// result = m.matches();
		// return result;
		if((password == null) || password.trim().equals(""))
            return false;
	    String regEx = "^\\w{4,15}$"; //至少5位
	    Pattern pat = Pattern.compile(regEx); 
	    Matcher mat = pat.matcher(password); 
	    boolean rs = mat.find();
	    return rs;
	}

	public static boolean isValidEmail(String email) {
		boolean result = false;
		Pattern email_pattern = Pattern
				.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-]{1,256}" + "\\@"
						+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
						+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");
		Matcher m = email_pattern.matcher(email);
		result = m.matches();
		return result;
	}

	public static boolean isValidURLName(String urlName) {
		Pattern urlname_pattern = Pattern.compile("^[a-zA-Z0-9]{1,}$");
		Matcher m = urlname_pattern.matcher(urlName);
		return m.matches();
	}

	private boolean validateUsername(String username) {
		if((username == null) || username.trim().equals(""))
            return false;
	    String regEx = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
	    Pattern pat = Pattern.compile(regEx); 
	    Matcher mat = pat.matcher(username); 
	    boolean rs = mat.find();
	    return rs;
	}

	private boolean validateNickname(String nickname) {
		  if((nickname == null) || nickname.trim().equals(""))
              return false;
		  else
			  return true;
	}

	private boolean validateInput(String username, String password,
			String nickname) {
		if (validateUsername(username) && isValidPwd(password)
				&& validateNickname(nickname))
			return true;
		else
			return false;
	}


   /** Charset from DialerKeyListener
     * char[] chars = DialerKeyListener.CHARACTERS;
     * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '#', '*',
            '+', '-', '(', ')', ',', '/', 'N', '.', ' ', ';'
       **/
    public static boolean isValidMobileNumber(String mobileNumber) {
        boolean ret = false;
        if (!TextUtils.isEmpty(mobileNumber)) {
//            final String regPattern = "^(130|131|132|133|134|135|136|137|138|139)/d{8}$";
//        	final String regPattern = "^+?\\d+$";
//            String regPattern = "^(13[0-9]|15[0-9]|18[7|8|9|6|5])\\d{4}$";
        	final String regPattern = "1[\\d]{10}";
            ret = mobileNumber.matches(regPattern);
        }

        return ret;
    }
    
    public static boolean isValidUrlAddress(String urlText) {
        boolean ret = false;
        if (!TextUtils.isEmpty(urlText)) {
            final String regPattern = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
//            ret = urlText.matches(regPattern);
            Pattern pat = Pattern.compile(regPattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pat.matcher(urlText);
            ret = matcher.find();
        }

        return ret;
    }

    public static String getTrimText(String origin, int limit) {
        if (isEmpty(origin)) {
            return origin;
        }

        String trimText = origin.trim();
        if (!isEmpty(trimText)) {
            if (trimText.length() > limit) {
                return trimText.substring(0, limit);
            }
        }

        return trimText;
    }
    
    public static void stripMobilePhoneNumber(String phoneNumber) {

    	phoneNumber = phoneNumber.replace(" ", "");
    }

    public static Spanned formatRemarkHtmlString(String name, String remark) {
        return Html.fromHtml(name + "<font color='#5f78ab'> ("+ remark + ")</font>");
    }

    public static Spanned formatStatusHtmlString(Context context, QiupuUser user) {
        String time = DateUtil.converToRelativeTime(context, user.status_time);
        return Html.fromHtml(user.status + "<font color='#aaaaaa'>(" + time + ")</font>");
    }

    public static Spanned formatOnOffHtmlString(String onLabel, ArrayList<String> onList,
                                                String offLabel, ArrayList<String> offList) {
        StringBuffer stringBuffer = new StringBuffer();
        if (onList.size() > 0) {
            stringBuffer.append(TextUtils.join(", ", onList)).append(" <font color='#5f78ab'>")
                    .append(onLabel).append("</font> ");
        }
        if (offList.size() > 0) {
            stringBuffer.append(TextUtils.join(", ", offList)).append(" <font color='#aaaaaa'>")
                    .append(offLabel).append("</font>");
        }
        return Html.fromHtml(stringBuffer.toString());
    }
    
    public static String createAddressJsonString(String tmpString) {
        ArrayList<AddressInfo> addressList = new ArrayList<AddressInfo>();
        AddressInfo info = new AddressInfo();
        info.street = tmpString;
        addressList.add(info);
        return JSONUtil.createAddressJSONArray(addressList);
    }
    
    public static SpannableStringBuilder setRadioBtnSpannable(Context context, String s) {
        SpannableStringBuilder style = new SpannableStringBuilder(s);
        int index = s.indexOf("\r\n");
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.text_grey)), index, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new RelativeSizeSpan(0.8f), index, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return style;
    }
    
    public static SpannableStringBuilder setVoteCountSpannable(Context context, String s) {
        SpannableStringBuilder style = new SpannableStringBuilder(s);
        int index = s.indexOf(":");
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.vote_count_color)), index + 1, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new RelativeSizeSpan(1f), index + 1, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return style;
    }

    private static Pattern patPunc;
    public static boolean isPunc(char c) {
        if (Character.isSpaceChar(c)) {
            return true;
        }

        if (null == patPunc) {
            patPunc = Pattern
                    .compile("[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]");
        }

        if (patPunc.matcher(String.valueOf(c)).matches()) {
            return true;
        }

        return false;
    }
    
    public static String loadResource(Context context) {
    	try {
    		InputStream is;
            is = context.getResources().getAssets().open("circle_template");

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String line;
            while (null != (line = br.readLine())) {
                buf.append(line).append("\n");
            }

            Log.d(TAG, "default configue string" + buf.toString());
            return buf.toString();
		} catch (Exception e) {
		}
    	 return null;
    }
    
    public static String loadResource(Context context, String fileName) {
    	try {
    		InputStream is;
            is = context.getResources().getAssets().open(fileName);

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String line;
            while (null != (line = br.readLine())) {
                buf.append(line).append("\n");
            }

            Log.d(TAG, "default configue string" + buf.toString());
            return buf.toString();
		} catch (Exception e) {
		}
    	 return null;
    }
}
