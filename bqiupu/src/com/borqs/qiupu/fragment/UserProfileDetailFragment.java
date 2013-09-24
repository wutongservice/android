package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.AsyncQiupu;
import twitter4j.Education;
import twitter4j.QiupuUser;
import twitter4j.Requests;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.WorkExperience;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class UserProfileDetailFragment extends BasicFragment implements UsersActionListner{
	private final static String TAG = "UserProfileDetailFragment";
	private Activity mActivity; 
	private ImageView profile_img_ui;
	private TextView  user_detail_borqsid;
	private TextView id_user_in_privacy_circle;
	private TextView tv_user_name;
	private WebView webView1;
	
	private View mContentView;
	private View profile_control;
	private int width;
	private Handler mHandler;
	private QiupuUser mUser;
	private QiupuORM orm;
	private AsyncQiupu asyncQiupu;
	private userProfileDetailCallBack mCallBackListener;
	private ProgressDialog mprogressDialog;
	public static final int CAMERA_WITH_DATA = 3023;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int SELECT_USER_DATE = 3022;
    public static final String MAIL_URI = "mailto:";
    public static final String TEL_URI = "tel:";
    
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	mActivity = activity;

        if (mActivity instanceof userProfileDetailCallBack) {
        	mCallBackListener = (userProfileDetailCallBack)activity;
        	mCallBackListener.getUserProfileDetailFragment(this);
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	orm = QiupuORM.getInstance(mActivity);
		mHandler = new MainHandler();
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		QiupuHelper.registerUserListener(getClass().getName(), this);
		
    	width = this.getResources().getDisplayMetrics().widthPixels;
    	if(mCallBackListener != null) {
    		mUser = mCallBackListener.getUserInfo();
    	}
    	if(mUser == null) {
    		mUser = new QiupuUser();
    	}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	
    	mContentView = inflater.inflate(R.layout.bpc_user_detail_info_view, container, false);
    	initView(mContentView);
    	return mContentView;
    	
    }

    private View initView(View headerView) {
    	profile_img_ui       = (ImageView)headerView.findViewById(R.id.profile_img_ui);
		user_detail_borqsid  = (TextView) headerView.findViewById(R.id.user_detail_borqsid);
		id_user_in_privacy_circle = (TextView) headerView.findViewById(R.id.id_user_in_privacy_circle);
		tv_user_name = (TextView) headerView.findViewById(R.id.user_name);
		webView1 = (WebView) headerView.findViewById(R.id.webView1);
		
		profile_control = headerView.findViewById(R.id.profile_control_tv);
		profile_control.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				IntentUtil.startExchangeVCardActivity(mActivity, orm.getRequestCount(String.valueOf(Requests.REQUEST_TYPE_EXCHANGE_VCARD)));
			}
		});
		setUI();
    	return headerView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
//    	serialization();
    	QiupuHelper.unregisterUserListener(getClass().getName());
    }
    
    private void initImageUI(String image_url)
	{
		ImageRun imagerun = new ImageRun(mHandler, image_url, 0);
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.noimage = true;
	    imagerun.addHostAndPath = true;
	    imagerun.setRoundAngle=true;
        imagerun.setImageView(profile_img_ui);    
        imagerun.post(null);
	}
    
    private void setUI(){
		profile_img_ui.setImageResource(R.drawable.default_user_icon);//first set default icon
		refreshUserInfoUI();
	}

    private void refreshUserInfoUI(){
		if(mUser != null){
			setrequestStatusUi();
			initImageUI(mUser.profile_image_url);
			initUserInfoUI();
			setCircleUI();
			if(mUser.uid == AccountServiceUtils.getBorqsAccountID()) {
				profile_control.setVisibility(View.VISIBLE);
			}else {
				profile_control.setVisibility(View.GONE);
			}
		}
	}
    
    private void setrequestStatusUi()
	{
		// set attention
		if(mUser.uid == AccountServiceUtils.getBorqsAccountID())
		{
//			request_concern.setVisibility(View.GONE);
		}
		else
		{
			if(StringUtil.isValidString(mUser.circleId))
			{
				if(mUser.his_friend)
				{
					if(mUser.profile_privacy 
			        		&& (!StringUtil.isValidString(mUser.pedding_requests)
			        				|| !BpcFriendsItemView.isalreadyRequestProfile(mUser.pedding_requests)))
					{
//						request_concern.setVisibility(View.VISIBLE);
//						request_concern.setText(R.string.request_see_user_profile);
					}
					else
					{
//						request_concern.setVisibility(View.GONE);
					}
				}
				else
				{
//					request_concern.setText(R.string.request_concern);
//					request_concern.setVisibility(View.VISIBLE);
				}
			}
			else 
			{
//				request_concern.setVisibility(View.GONE);
			}
		}
	}
    
    private void initUserInfoUI()
	{
		showWebViewInfomation();
		setDisplayNameUi();
		user_detail_borqsid.setText(String.format(getString(R.string.user_detail_borqsid), mUser.uid!=-1 ? mUser.uid:"" ));
	}
    
    private void showWebViewInfomation() {
    	if(mUser!=null) {
			StringBuilder sb = new StringBuilder();
			formatPageContent(sb);
			webViewLoadData(sb);
		}
    }
    
    private void setDisplayNameUi () {
        if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
            tv_user_name.setText(mUser.nick_name);
            tv_user_name.setOnClickListener(null);
        }else {
            if(StringUtil.isValidString(mUser.circleId)) {
                tv_user_name.setText(StringUtil.formatRemarkHtmlString(mUser.nick_name, showRemarkName() ));
            }else {
                tv_user_name.setText(mUser.nick_name);
                tv_user_name.setOnClickListener(null);
            }
        }
    }
    
	private void setCircleUI() {
		setprivacyTextValue(mUser.circleId);
	}
    
    private void setprivacyTextValue(String circleid)
	{
		Resources res = getResources();
		if(StringUtil.isValidString(circleid))
		{
			if(isInMyprivacyCircle(circleid))
			{
				//set status of request
				id_user_in_privacy_circle.setVisibility(View.VISIBLE);
				id_user_in_privacy_circle.setText(res.getString(R.string.people_circle_summary));
				if(mUser.profile_privacy && BpcFriendsItemView.isalreadyRequestProfile(mUser.pedding_requests))
				{
					id_user_in_privacy_circle.setText(res.getString(R.string.already_send_request));
				}
				else if(!mUser.profile_privacy)
				{
					id_user_in_privacy_circle.setText(res.getString(R.string.already_exchange_info));
				}
			}
			else
			{
				if(mUser.profile_privacy && BpcFriendsItemView.isalreadyRequestProfile(mUser.pedding_requests))
				{
					id_user_in_privacy_circle.setVisibility(View.VISIBLE);
					id_user_in_privacy_circle.setText(res.getString(R.string.already_send_request));
				}
				else
				{
					id_user_in_privacy_circle.setVisibility(View.GONE);
				}
			}
		}
		else 
		{
			id_user_in_privacy_circle.setVisibility(View.GONE);
		}
	}
    
    private boolean isInMyprivacyCircle(String circleid){
		if(mUser.uid != AccountServiceUtils.getBorqsAccountID())
		{
			String[] ids = circleid.split(",");
			for(int i=0; i<ids.length; i++)
			{
				if(QiupuConfig.ADDRESS_BOOK_CIRCLE == Long.parseLong(ids[i]))
				{
					return true;
				}
			}
		}
		return false;
	}
    
	private void formatPageContent(StringBuilder sb)
	{
//		if(mUser!=null) {
			ArrayList<WorkExperience> w_list =  mUser.work_history_list;
			 ArrayList<Education> edu_list =  mUser.education_list;
			
			 sb.append("<html>");
				String meta = "<META http-equiv=\"Content-Type\" content=\"text/html\" charset='utf-8'/>"; 
				sb.append("<HEAD>"+meta+"</HEAD><body><table border=0 cellpadding=3>"); 
//			formatTitle(sb, R.string.base_info_title);
			String gender = "";
			if(TextUtils.isEmpty(mUser.gender)) {
				gender = getString(R.string.user_sex_unknow);
			}else if(("m").equals(mUser.gender)){
	    		gender =  getString(R.string.user_sex_man);
	    	}else if(("f").equals(mUser.gender)){
	    		gender = getString(R.string.user_sex_woman);
	    	}
//			if(mUser.uid == -1) {
//				formatPhoneNumberContent(sb, R.string.user_borqsid,"",TYPE_TEXT);
//			}else {
//				formatPhoneNumberContent(sb, R.string.user_borqsid,String.valueOf(mUser.uid),TYPE_TEXT);
//			}
			formatPhoneNumberContent(sb, R.string.user_gender,gender,TYPE_TEXT);
//			formatPhoneNumberContent(sb, R.string.nickname, mUser.nick_name,TYPE_TEXT);
			formatPhoneNumberContent(sb, R.string.user_birthday, mUser.date_of_birth,TYPE_TEXT);
			formatPhoneNumberContent(sb, R.string.user_address, mUser.location,TYPE_TEXT);
			
//			formatOneBlankLine(sb);
//			formatTitle(sb, R.string.phone_email_title);
			
			if(mUser.phoneList != null && mUser.phoneList.size() > 0) {
                for(int i=0; i<mUser.phoneList.size(); i++) {
                    formatPhoneHtml(sb, R.string.phone_number, mUser.phoneList.get(i).info, mUser.phoneList.get(i).type);
                }
            }
            if(mUser.emailList != null && mUser.emailList.size() > 0) {
                for(int i=0; i<mUser.emailList.size(); i++) {
                    formatEmailHtml(sb, R.string.email_address, mUser.emailList.get(i).info, mUser.emailList.get(i).type);
                }
            }
            
				formatOneBlankLine(sb);
				formatTitle(sb, R.string.work_experience);
//				formatOneImageLine(sb);
				formatPhoneNumberContent(sb, R.string.update_profile_company_hint, mUser.company,TYPE_TEXT, true);
				formatPhoneNumberContent(sb, R.string.update_profile_department_hint, mUser.department,TYPE_TEXT);
				formatPhoneNumberContent(sb, R.string.update_profile_job_hint, mUser.jobtitle,TYPE_TEXT);
				formatPhoneNumberContent(sb, R.string.user_address, mUser.office_address,TYPE_TEXT);
            	
				if(w_list!=null && w_list.size()>0) {
					sb.append("<tr align=\"left\"><td colspan=2></td></tr>");
					for(int i=0;i<w_list.size();i++) {
						WorkExperience we = w_list.get(i);
						
						sb.append(String.format("<tr align=\"left\" bgcolor=\"#b5e6f7\" ><td colspan=2>%1$s&nbsp;&nbsp;~&nbsp;&nbsp;%2$s</td></tr>", we.from, we.to));
						
						
						formatPhoneNumberContent(sb, R.string.update_profile_company_hint, we.company,TYPE_TEXT, true);
						formatPhoneNumberContent(sb, R.string.update_profile_department_hint, we.department,TYPE_TEXT);
						formatPhoneNumberContent(sb, R.string.update_profile_job_hint, we.job_title,TYPE_TEXT);
						formatPhoneNumberContent(sb, R.string.update_profile_address_hint, we.office_address,TYPE_TEXT);
						if(i<w_list.size()-1) {
							sb.append("<tr align=\"left\"><td colspan=2></td></tr>");
						}
					}
				}
				
				if(edu_list!=null && edu_list.size()>0) {
					formatOneBlankLine(sb);
					formatTitle(sb, R.string.education);
					for(int i=0;i<edu_list.size();i++) {
						Education edu = edu_list.get(i);
						
						sb.append(String.format("<tr align=\"left\" bgcolor=\"#b5e6f7\" ><td colspan=2>%1$s&nbsp;&nbsp;~&nbsp;&nbsp;%2$s</td></tr>", edu.from, edu.to));
						formatPhoneNumberContent(sb, R.string.school, edu.school,TYPE_TEXT, true);
						formatPhoneNumberContent(sb, R.string.school_type, edu.type,TYPE_TEXT);
						formatPhoneNumberContent(sb, R.string.school_class, edu.school_class,TYPE_TEXT);
						formatPhoneNumberContent(sb, R.string.degree, edu.degree,TYPE_TEXT);
						formatPhoneNumberContent(sb, R.string.major, edu.major,TYPE_TEXT);
						if(i<edu_list.size()-1) {
							sb.append("<tr align=\"left\"><td colspan=2></td></tr>");
						}
					}
				}
			
			sb.append("</table></body></html>");
//			webViewLoadData(sb);
//		}
	}
	
	private void webViewLoadData(StringBuilder sb) {
		if(webView1!=null&&sb!=null) {
            Log.d(TAG, "formatPageContent() has fixed this issue");
            MyWebViewClient wvc = new MyWebViewClient();
            webView1.setWebViewClient(wvc);
        	webView1.loadDataWithBaseURL("", sb.toString(), "text/html", "UTF-8","");
        }
	}
    
	private void validateStrApendHtml(StringBuilder sb,int resId,String val,int type) {
		if(!TextUtils.isEmpty(val)) {
			formatPhoneNumberContent(sb, resId,val,type);
		}
	}
    
    private void formatOneBlankLine(StringBuilder sb)
    {
    	//one blank line
    	sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2 ></td></tr>");
    }
    
//    private void formatOneImageLine(StringBuilder sb) {
//    	sb.append(String.format("<tr> <td height='%2$s' width='%1$s' bgcolor=\"#adbbe7\" colspan=2></td></tr>", width, mActivity.getResources().getDimension(R.dimen.profile_span_height)));
    	
//    	sb.append("<tr> <td colspan=2> <hr width=\"100%\" size=\"1\" color=\"#adbbe7\" style=\"filter:progid:DXImageTransform.Microsoft.Glow(color=#00ffff,strength=10)\"></hr></td></tr>");
//    }
    
    private void formatTitle(StringBuilder sb,int resId) {
		sb.append("<tr align=\"left\" valign=\"top\" ><td style=\"color: #000000;font-size: 16sp;\" colspan=2 ><b>");
		sb.append(getString(resId));
		sb.append(":</b></td></tr>");
	}
    
    public static final int TYPE_TEXT  = 0;
    private void formatPhoneHtml(StringBuilder sb, int resId, String val, String type) {
        sb.append("<tr valign=\"top\" >");
        sb.append(String.format("<td width='%1$s' align=\"left\" valign=\"top\">", width/2));
        sb.append(getString(resId));
        sb.append(":</td>");
        sb.append(String.format("<td style=\"color: #333333;\" \"word-WRAP:break-all\" align=\"left\" valign=\"top\" width='%2$s'><a href='%3$s%1$s'>%1$s</a></td>", formatHtml(val), width/2, type ));
    }
    
    private void formatEmailHtml(StringBuilder sb, int resId, String val, String type) {
        sb.append("<tr valign=\"top\" >");
        sb.append(String.format("<td width='%1$s' align=\"left\" valign=\"top\">", width/2));
        sb.append(getString(resId));
        sb.append(":</td>");
        sb.append(String.format("<td style=\"color: #333333;\" \"word-WRAP:break-all\" align=\"left\" valign=\"top\" width='%2$s'><a href='%3$s%1$s'>%1$s</a></td>", formatHtml(val), width/2, type ));
    }
    /**
     * 
     * @param sb
     * @param resId 
     * @param val
     * @param type 文本的显示类型(TYPE_TEXT、TYPE_PHONE、TYPE_EMAIL)
     */
    private void formatPhoneNumberContent(StringBuilder sb,int resId,String val,int type) {
    	 formatPhoneNumberContent(sb, resId,val,type, false);
    }
    
    private void formatPhoneNumberContent(StringBuilder sb,int resId,String val,int type, boolean title) {
    	if(StringUtil.isEmpty(val) == true)
    		return ;
    	
    	if(isDetached()) { // if fragment detached , do nothing
    	    return ;
    	}
    	
    	sb.append("<tr valign=\"top\" >");
     	sb.append(String.format("<td width='%1$s' align=\"left\" valign=\"top\">", width/2));
     	sb.append(getString(resId));
     	sb.append(":</td>");
     	switch(type) {
     		case  TYPE_TEXT:
     			if(title == true)
     				sb.append(String.format("<td style=\"color: #000000; font-size: 16sp\" \"word-WRAP:break-all\" align=\"left\" valign=\"top\" width='%2$s'><b>%1$s</b></td>", formatHtml(val, title), width/2 ));
     			else
     				sb.append(String.format("<td style=\"color: #333333;\" \"word-WRAP:break-all\" align=\"left\" valign=\"top\" width='%2$s'>%1$s</td>", formatHtml(val, title), width/2 ));
     			break;
     		default:
     			return;
     	}
     	sb.append("</tr>");
    }
    
    private String formatHtml(String source, boolean title)
	{
		if(source == null)
			return "";
		
		String tmp;
		try {
			tmp = new String(source.getBytes(), "UTF-8");			
			tmp = tmp.replaceAll("\r\n", "<br>");
			tmp = tmp.replaceAll("\r", "<br>");
			tmp = tmp.replaceAll("\n", "<br>");
			if(title)
				tmp = "<b>" + tmp + "</b>";
			
			return tmp;
		} catch (Exception e) {}					
		return source;
	}
    
    private String formatHtml(String source)
	{
		return formatHtml(source,false);
	}

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            int length = url.length();

            if (length > 0) {
                if (url.startsWith(QiupuConfig.TYPE_PHONE1)) {
                    chooseDialog(getString(R.string.change_request_phone), BasicActivity.DISPLAY_PHONE_NUMBER1, url.substring(QiupuConfig.TYPE_PHONE1.length(), url.length()));
//					setChangeRequest(DISPLAY_PHONE_NUMBER1, url.substring(5, url.length()));
                    return true;

                } else if (url.startsWith(QiupuConfig.TYPE_PHONE2)) {
                    chooseDialog(getString(R.string.change_request_phone), BasicActivity.DISPLAY_PHONE_NUMBER2, url.substring(QiupuConfig.TYPE_PHONE2.length(), url.length()));
                    return true;

                } else if (url.startsWith(QiupuConfig.TYPE_PHONE3)) {
                    chooseDialog(getString(R.string.change_request_phone), BasicActivity.DISPLAY_PHONE_NUMBER3, url.substring(QiupuConfig.TYPE_PHONE3.length(), url.length()));
                    return true;

                } else if (url.startsWith(QiupuConfig.TYPE_EMAIL1)) {
                    chooseDialog(getString(R.string.change_request_email), BasicActivity.DISPLAY_EMAIL1, url.substring(QiupuConfig.TYPE_EMAIL1.length(), url.length()));
                    return true;
                } else if (url.startsWith(QiupuConfig.TYPE_EMAIL2)) {
                    chooseDialog(getString(R.string.change_request_email), BasicActivity.DISPLAY_EMAIL2, url.substring(QiupuConfig.TYPE_EMAIL2.length(), url.length()));
                    return true;
                } else if (url.startsWith(QiupuConfig.TYPE_EMAIL3)) {
                    chooseDialog(getString(R.string.change_request_email), BasicActivity.DISPLAY_EMAIL3, url.substring(QiupuConfig.TYPE_EMAIL3.length(), url.length()));
                    return true;
                }

            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        private void chooseDialog(String requst_change_str ,final int type,final String url) {
			if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
				 return ;
			}
			String[] items = new String[]{requst_change_str, url};
			AlertDialog builder = new AlertDialog.Builder(mActivity)
			.setItems(items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						switchChangeRequest(type,url);
					} else {
						if(type==BasicActivity.DISPLAY_EMAIL1 || type==BasicActivity.DISPLAY_EMAIL2
								|| type==BasicActivity.DISPLAY_EMAIL3) {
						    try {
    							String new_url = new StringBuilder("mailto:").append(url).toString();
    							Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(new_url));
    					        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    					        startActivity(intent);
					        } catch (ActivityNotFoundException e) {
					            e.printStackTrace();
					        }
						}else {
						    try {
    							String new_url = new StringBuilder("tel:").append(url).toString();
    							Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(new_url));
    					        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    					        startActivity(intent);
						    } catch (ActivityNotFoundException e) {
						        e.printStackTrace();
						    }
						}
					}
				}
			}).show();
		}
	}
    
    private int mChangeRquestType;
    private View mChangeRquestView;
    private EditText mChangeRquestEdit;
   	private void switchChangeRequest(int type, String text) {
   		mChangeRquestType = type;
   		String dialogtilte = "";
   		String dialoghint = "";
        if (type == BasicActivity.DISPLAY_PHONE_NUMBER1
                || type == BasicActivity.DISPLAY_PHONE_NUMBER2
                || type == BasicActivity.DISPLAY_PHONE_NUMBER3) {
            dialogtilte = getString(R.string.change_request_phone);
            dialoghint = getString(R.string.edit_profile_phone_number_hint);
        } else if (type == BasicActivity.DISPLAY_EMAIL1
                || type == BasicActivity.DISPLAY_EMAIL2
                || type == BasicActivity.DISPLAY_EMAIL3) {
            dialogtilte = getString(R.string.change_request_email);
            dialoghint = getString(R.string.edit_profile_emmail_hint);
        }
        showEditRequestDialog(type, dialogtilte, dialoghint, text);
    }
    
   	private void showEditRequestDialog(int type, String title, String hint, String textContent) {
//   		if(mChangeRquestView == null) {
   			mChangeRquestView = LayoutInflater.from(mActivity).inflate(R.layout.edit_user_info_dialog, null);
   			mChangeRquestEdit = (EditText) mChangeRquestView.findViewById(R.id.edit_content);
//   		}
   		if (type == BasicActivity.DISPLAY_PHONE_NUMBER1
   				|| type == BasicActivity.DISPLAY_PHONE_NUMBER2
   				|| type == BasicActivity.DISPLAY_PHONE_NUMBER3) {
   			mChangeRquestEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
   		}
   		mChangeRquestEdit.setText(textContent);
   		mChangeRquestEdit.setHint(hint);
     
   		DialogUtils.ShowDialogwithView(mActivity, title, 0, mChangeRquestView, ChangeRequestOkListener, ChangeRequestCancelListener);
   	}
   	
   	
	private static final int CHANGE_REQUEST_END        = 106;
	
    private static final String RESULT = "result";
    
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHANGE_REQUEST_END: {
				dimissProgressDialog();
				//TODO
				if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
					ToastUtil.showShortToast(mActivity, mHandler, R.string.request_ok);
				}
				break;
			}
			}
		}
	}
	
	Object mSendRequestLock = new Object();
	boolean insendRequestProcess;
	private void sendchangeRequest(HashMap<String, String> map){
		synchronized(mSendRequestLock)
		{
			if(insendRequestProcess == true)
			{
				Log.d(TAG, "in loading info data");
				return ;
			}
		}
		showProcessDialog(R.string.request_process_title, false, true, true);
		
		synchronized(mSendRequestLock)
		{
			insendRequestProcess = true;			
		}
		asyncQiupu.sendChangeRequest(AccountServiceUtils.getSessionID(), map, new TwitterAdapter() {
			public void sendChangeRequest(boolean result) {
				Log.d(TAG, "finish edit user profile");
				Message msg = mHandler.obtainMessage(CHANGE_REQUEST_END);				
				msg.getData().putBoolean(BasicActivity.RESULT, result);
				msg.sendToTarget();
				synchronized(mSendRequestLock)
				{
					insendRequestProcess = false;			
				}
			}

			public void onException(TwitterException ex, TwitterMethod method) {
				
				synchronized(mSendRequestLock)
				{
					insendRequestProcess = false;			
				}
                TwitterExceptionUtils.printException(TAG, "sendchangeRequest, server exception:", ex, method);

				Message msg = mHandler.obtainMessage(CHANGE_REQUEST_END);
				msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
				msg.getData().putBoolean(BasicActivity.RESULT, false);
				msg.sendToTarget();
			}
		});
	}
	
	private void dimissProgressDialog() {
		try {
			mprogressDialog.dismiss();
			mprogressDialog = null;
		}catch(Exception e){
			Log.d(TAG, "progress dialog dimiss exception !");
		}
		
	}
	public void refreshUserInfo(QiupuUser user) {
		mUser = user;
		refreshUserInfoUI();
	}
	
	private void sendChangeRequest()
	{
		String editContent = mChangeRquestEdit.getText().toString();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("to", String.valueOf(mUser.uid));
		map.put("message", "");//TODO have no view to add message
		if(mChangeRquestType == BasicActivity.DISPLAY_PHONE_NUMBER1)
		{
			map.put(QiupuConfig.TYPE_PHONE1, editContent);
		}
		else if(mChangeRquestType == BasicActivity.DISPLAY_PHONE_NUMBER2)
		{
			map.put(QiupuConfig.TYPE_PHONE2, editContent);
		}
		else if(mChangeRquestType == BasicActivity.DISPLAY_PHONE_NUMBER3)
		{
			map.put(QiupuConfig.TYPE_PHONE3, editContent);
		}
		else if(mChangeRquestType == BasicActivity.DISPLAY_EMAIL1)
		{
			map.put(QiupuConfig.TYPE_EMAIL1, editContent);
		}
		else if(mChangeRquestType == BasicActivity.DISPLAY_EMAIL2)
		{
			map.put(QiupuConfig.TYPE_EMAIL2, editContent);
		}
		else if(mChangeRquestType == BasicActivity.DISPLAY_EMAIL3)
		{
			map.put(QiupuConfig.TYPE_EMAIL3, editContent);
		}
		sendchangeRequest(map);
	}
	
	private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
		mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
				resId, CanceledOnTouchOutside, Indeterminate, cancelable);
		mprogressDialog.show();    	
	}
	
	DialogInterface.OnClickListener ChangeRequestOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			sendChangeRequest();
		}
	};
	
	DialogInterface.OnClickListener ChangeRequestCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {}
	};
	
	public interface userProfileDetailCallBack {
		public void getUserProfileDetailFragment(UserProfileDetailFragment fragment);
		public QiupuUser getUserInfo();
	}
	
	@Override
	public void updateItemUI(QiupuUser user) {
		Log.d(TAG, "user: " + user);
		if(user != null) {
			Log.d(TAG, "updateItemUI: " + user.circleId);
			if(user.circleId.length() <= 0)
			{
				mUser.circleId = "";
				mUser.circleName = "";
			}
			else
			{
				mUser.circleId = user.circleId;
				mUser.circleName = user.circleName;
				mUser.pedding_requests = user.pedding_requests;
				mUser.profile_privacy = user.profile_privacy;
			}
			
			setrequestStatusUi();
			setCircleUI();
			setDisplayNameUi();
		}
	}

	@Override
	public void addFriends(QiupuUser user) {
	}

	@Override
	public void refuseUser(long uid) {
	}

	@Override
	public void deleteUser(QiupuUser user) {
	}

	@Override
	public void sendRequest(QiupuUser user) {
	}

    private String showRemarkName() {
        String remarkname = getString(R.string.profile_remark);
        if(StringUtil.isEmpty(mUser.remark) == false) {
            remarkname = mUser.remark;
        }else {
//            if(mUser.perhapsNames != null && mUser.perhapsNames.size() > 0) {
//                PerhapsName tmpPerhapsName = new PerhapsName();
//                tmpPerhapsName.name = mUser.nick_name;
//                
//                if(mUser.perhapsNames.contains(tmpPerhapsName)) {
//                    mUser.perhapsNames.remove(tmpPerhapsName);
//                }
//                
//                for(int i=0; i<mUser.perhapsNames.size(); i++) {
//                    if(mUser.nick_name.equals(mUser.perhapsNames.get(i).name) == false) {
//                        remarkname = mUser.perhapsNames.get(i).name;
//                        break;
//                    }
//                }
//            }
        }
//        if(remarkname.length() > 0) {
//            remarkname = "(" + remarkname + ")";
//        }
        
        return remarkname;
    }
    
    public QiupuUser getUserInfo() {
        return mUser;
    }
    public void setUser(QiupuUser user) {
    	mUser = user; 
    }
}
