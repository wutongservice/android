package com.borqs.qiupu.ui;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.adapter.ContactUserAdapter;
import com.borqs.common.view.ContactUserSelectItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.StringUtil;

public class InviteContactActivity extends BasicActivity{
	private static final String TAG = "Qiupu.InviteContactActivity";
    private static final String SIM_CONTACT_URI = "content://icc/adn";

    private ListView mcontactListView;
	private ContactUserAdapter mcontactAdapter;

    private ImageView head_select_all;

    private TextView id_simnumber;
    private TextView id_phonenumber;
	private TextView id_email;
    private EditText keyEdit;

    private Button request_ok_btn;

    private boolean selectall = false;

    private Cursor mCursorSimContacts;  // SIM phone number
    private Cursor mCursorEmails;
    private Cursor mCursorPhone;

    private Cursor mCursorSearch;
    private static final String INTENT_SHARE_CONTACT = "android.intent.action.PICK_CONTACT";

//    private Cursor mCursorLocalContacts = null;

//    private ArrayList<ContactSimpleInfo> users_phone = new ArrayList<ContactSimpleInfo>();
//	private ArrayList<ContactSimpleInfo> users_email = new ArrayList<ContactSimpleInfo>();
//	private ArrayList<ContactSimpleInfo> searchUsers = new ArrayList<ContactSimpleInfo>();
//	private ArrayList<ContactSimpleInfo> mCopyUser = new ArrayList<ContactSimpleInfo>();

//    private Handler mHandler;

	public class UITAB {
        public static final int NONE = 0;
        public static final int DISPLAY_SIM = 1;
        public static final int DISPLAY_PHONE = 2;
        public static final int DISPLAY_EMAIL = 3;
        public static final int DISPLAY_SEARCH = 4;
    }
	private int currentTab;
	private static boolean mIsPickContact = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.request_contact_friends);
//		mSendType = getIntent().getIntExtra("InviteType",0);
        mcontactListView = (ListView) findViewById(R.id.contact_list_content);
		head_select_all    = (ImageView) findViewById(R.id.select_all_iv);
		request_ok_btn     = (Button) findViewById(R.id.select_ok);
        Button request_cancel_btn = (Button) findViewById(R.id.select_cancel);
        ImageView select_other_user = (ImageView) findViewById(R.id.select_other_user);
        id_simnumber       = (TextView) findViewById(R.id.id_simnumber);
		id_phonenumber     = (TextView) findViewById(R.id.id_phonenumber);
		id_email           = (TextView) findViewById(R.id.id_email);
		id_phonenumber.setText(String.format(getString(R.string.invite_phone), 0));
		id_email.setText(String.format(getString(R.string.invite_email), 0));
		
		request_ok_btn.setText(String.format(getString(R.string.contact_select), 0));
		request_cancel_btn.setText(String.format(getString(R.string.label_cancel), 0));
		request_ok_btn.setOnClickListener(this);
		request_cancel_btn.setOnClickListener(this);
		select_other_user.setOnClickListener(this);
        id_simnumber.setOnClickListener(this);
		id_phonenumber.setOnClickListener(this);
		id_email.setOnClickListener(this);
		
		
		head_select_all.setOnClickListener(selectAllListener);

        String action = getIntent().getAction();
        if (INTENT_SHARE_CONTACT.equals(action)) {
            mIsPickContact = true;
            hideTopAndBottomView();
        } else {
            mIsPickContact = false;
            setHeadTitle(R.string.qiupu_invite);
        }

		mcontactAdapter = new ContactUserAdapter(this, mIsPickContact);
		mcontactListView.setAdapter(mcontactAdapter);
		mcontactListView.setOnItemClickListener(contactitemClickListener);

        keyEdit = (EditText) this.findViewById(R.id.search_span);
        if (null != keyEdit) {
            keyEdit.addTextChangedListener(new MyWatcher());
        }
        
        ImageView mSearchBtn = (ImageView)findViewById(R.id.head_action_right);
        mSearchBtn.setOnClickListener(this);
        
        setTitleUI(UITAB.DISPLAY_PHONE);

        new DeSerializationTask().execute((Void[])null);
	}	

    private void hideTopAndBottomView() {
        View bottom = findViewById(R.id.action_region);
        bottom.setVisibility(View.GONE);
        setHeadTitle(R.string.status_share);
        View tab = findViewById(R.id.id_title_category);
        tab.setVisibility(View.GONE);
        View invite = findViewById(R.id.listview_title);
        invite.setVisibility(View.GONE);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
//		serialization(users_phone, posts_sfile);
//		serialization(users_email, posts_sfile_email);

        if (null != mCursorSimContacts) {
            mCursorSimContacts.close();
            mCursorSimContacts = null;
        }

        if (null != mCursorEmails) {
            mCursorEmails.close();
            mCursorEmails = null;
        }

        if (null != mCursorPhone) {
            mCursorPhone.close();
            mCursorPhone = null;
        }

        if (null != mCursorSearch) {
            mCursorSearch.close();
            mCursorSearch = null;
        }

//        if (null != mCursorLocalContacts) {
//            mCursorLocalContacts.close();
//        }
	}
	
	public void onClick(View view) 
	{
		final int id = view.getId();
        if (id == R.id.select_cancel) {
            onBackPressed();
        } else if (id == R.id.select_ok) {
            ArrayList<ContactSimpleInfo> phones = getSelectedUsersPhone();
            ArrayList<ContactSimpleInfo> emails = getSelectedUsersEmail();
            if (QiupuConfig.LOGD) Log.d(TAG, "toAddress :" + phones.size());
            if (phones.size() <= 0 && emails.size() <= 0) {
                Toast.makeText(this, R.string.select_one_user, Toast.LENGTH_SHORT).show();
//						showDialog(SEND_MESSAGE_DIALOG);
            } else {
                contactsPhone = phones;
                contactsEmail = emails;
                showDialog(SEND_MESSAGE_DIALOG);
            }
        } else if (id == R.id.id_email) {
            setTitleUI(UITAB.DISPLAY_EMAIL);
        } else if (id == R.id.id_simnumber) {
            setTitleUI(UITAB.DISPLAY_SIM);
        } else if (id == R.id.id_phonenumber) {
            setTitleUI(UITAB.DISPLAY_PHONE);
        } else if (id == R.id.select_other_user) {
            if (currentTab == UITAB.DISPLAY_EMAIL) {
                setInviteType(QiupuConfig.INVITE_TYPE_EMAIL);
            } else if (currentTab == UITAB.DISPLAY_PHONE || currentTab == UITAB.DISPLAY_SIM) {
                setInviteType(QiupuConfig.INVITE_TYPE_MESSAGE);
            }
            showDialog(DIALOG_INVITE_USERINFO);
        } else if (id == R.id.head_action_right) {
            View searchspan = findViewById(R.id.search_span);
            searchspan.setVisibility(searchspan.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        } else {
        }
		super.onClick(view);
	}
	
	protected void createHandler() {
		mHandler = new MainHandler();
	}

    private AdapterView.OnItemClickListener contactitemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            if(ContactUserSelectItemView.class.isInstance(view)) {
                if (mIsPickContact) {
                    ContactUserSelectItemView cv = (ContactUserSelectItemView) view;
                    ContactSimpleInfo info = cv.getContactSimpleInfo();

                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(QiupuMessage.BUNDLE_SHARE_CONTACT_INFO, info);
                    intent.putExtra(QiupuMessage.BUNDLE_SHARE_CONTACT, bundle);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    ContactUserSelectItemView uv = (ContactUserSelectItemView)view;
                    uv.switchCheck();
                }
            }
        }
    };

    private View.OnClickListener selectAllListener = new View.OnClickListener() {
		public void onClick(View arg0) {
            setSelectedAll(!selectall);

            final boolean isSearching = !TextUtils.isEmpty(keyEdit.getText().toString());

			if(currentTab == UITAB.DISPLAY_EMAIL)
			{
                if (selectall) {
                    final int size = null == mCursorEmails ? 0 : mCursorEmails.getCount();
                    final int curPos = mCursorEmails.getPosition();
                    for (int i = 0; i < size; ++i) {
                        mCursorEmails.moveToPosition(i);
                        final String emailAddress = mCursorEmails.getString(mCursorEmails.getColumnIndex(Email.DATA));
                        mSelectedEmail.add(emailAddress);
                    }
                    mCursorEmails.moveToPosition(curPos);
                } else {
                    mSelectedEmail.clear();
                }

                if (isSearching) {
                    mSelectedSearch = mSelectedEmail;
                }
			}
			else if(currentTab == UITAB.DISPLAY_PHONE)
			{
                if (selectall) {
                    final int size = null == mCursorPhone ? 0 : mCursorPhone.getCount();
                    final int curPos = mCursorPhone.getPosition();
                    for (int i = 0; i < size; ++i) {
                        mCursorPhone.moveToPosition(i);
                        final String phoneNumber = mCursorPhone.getString(mCursorPhone.getColumnIndex(Phone.NUMBER));
                        mSelectedPhone.add(phoneNumber);
                    }
                    mCursorPhone.moveToPosition(curPos);
                }else {
                    mSelectedPhone.clear();
                }

                if (isSearching) {
                    mSelectedSearch = mSelectedPhone;
                }
			} else if (currentTab == UITAB.DISPLAY_SIM) {
                if (selectall) {
                    final int size = null == mCursorSimContacts ? 0 : mCursorSimContacts.getCount();
                    final int curPos = mCursorSimContacts.getPosition();
                    for (int i = 0; i < size; ++i) {
                        mCursorSimContacts.moveToPosition(i);
                        final String simNumber = mCursorSimContacts.getString(mCursorSimContacts.getColumnIndex(SIM_COLUMN_NUMBER));
                        mSelectedSim.add(simNumber);
                    }
                    mCursorSimContacts.moveToPosition(curPos);
                } else {
                    mSelectedSim.clear();
                }

                if (isSearching) {
                    mSelectedSearch = mSelectedSim;
                }
            } else {
                Log.e(TAG, "selectAllListener, onClick, unexpected tab:" + currentTab);
                return;
            }

//            mcontactAdapter.checkAll(selectall);
            final int count = mcontactListView.getChildCount();
            View item;
            for (int i = 0; i < count; ++i) {
                item = mcontactListView.getChildAt(i);
                if (ContactUserSelectItemView.class.isInstance(item)) {
                    ((ContactUserSelectItemView)item).selectUser(selectall);
                } else {
                    Log.d(TAG, "selectAllListener, onClick, unexpected view item:" + item);
                }
            }
            mcontactAdapter.setSelectAll(selectall);
			refreshSelectedCount();
		}
	};
	
	public View.OnClickListener loadOlderClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "load older message");
			
//			if(users.size() > 0){
//				refreshUI(users);
//			}
		}
	};
	
//	public void changeSelect(){
//		ArrayList<ContactSimpleInfo> selectUsersPhone = getSelectedUsersPhone();
//		ArrayList<ContactSimpleInfo> selectUsersEmail = getSelectedUsersEmail();
//		request_ok_btn.setText(String.format(getString(R.string.contact_select),
//                selectUsersPhone.size() + selectUsersEmail.size()));
//	}

    private HashSet<String> mSelectedSim = new HashSet<String>();
    private HashSet<String> mSelectedPhone = new HashSet<String>();
    private HashSet<String> mSelectedEmail = new HashSet<String>();
    private HashSet<String> mSelectedSearch = mSelectedPhone;
//    private HashSet<String> mSelectedSearch = new HashSet<String>();
    public void changeSelect(int bindId, String itemId, boolean isSelected) {
        if (bindId == UITAB.DISPLAY_SEARCH) {
            bindId = currentTab;
        }

        if (bindId == UITAB.DISPLAY_SIM) {
            if (isSelected) {
                mSelectedSim.add(itemId);
            } else {
                mSelectedSim.remove(itemId);
            }
        } else if (bindId == UITAB.DISPLAY_PHONE) {
            if (isSelected) {
                mSelectedPhone.add(itemId);
            } else {
                mSelectedPhone.remove(itemId);
            }
        } else if (bindId == UITAB.DISPLAY_EMAIL) {
            if (isSelected) {
                mSelectedEmail.add(itemId);
            } else {
                mSelectedEmail.remove(itemId);
            }
        } else {
            Log.e(TAG, "changeSelect, unexpected tab id: " + bindId + ", itemId:" + itemId);
        }

        refreshSelectedCount();
    }

    private void refreshSelectedCount() {
		request_ok_btn.setText(String.format(getString(R.string.contact_select),
                (mSelectedSim.size() + mSelectedPhone.size()) + mSelectedEmail.size()));
		id_phonenumber.setText(String.format(getString(R.string.invite_phone), (mSelectedSim.size() + mSelectedPhone.size())));
		id_email.setText(String.format(getString(R.string.invite_email), mSelectedEmail.size()));
	}

//	private static final int SHOW_CONTACT = 1;
	private class MainHandler extends Handler{

		public void handleMessage(Message msg) {
//			switch(msg.what){
//		    	case SHOW_CONTACT:{
//		    		if(currentTab == UITAB.DISPLAY_PHONE)
//		    		{
//		    			refreshUI(users_phone);
//		    		}
//		    		else if(currentTab == UITAB.DISPLAY_EMAIL)
//		    		{
//		    			refreshUI(users_email);
//		    		} else if (currentTab == UITAB.DISPLAY_SIM) {
//                        refreshUI(mCursorSimContacts);
//                    } else {
//                        Log.e(TAG, "handleMessage, SHOW_CONTACT unexpected tab:" + currentTab);
//                    }
//		    		break;
//		    	}
//			}
		}
	}
	
//	private void refreshUI(ArrayList<ContactSimpleInfo> userlist)
//	{
//        mcontactAdapter.swapToList(userlist, currentTab);
//		mCopyUser.clear();
//		mCopyUser.addAll(userlist);
//		mcontactAdapter.notifyDataSetChanged();
//	}

    private void refreshUI(Cursor cursor, HashSet<String> selectedSet, int tab) {
    	if (null != cursor) {
    		mcontactAdapter.swapToCursor(cursor, selectedSet, tab);
            setSelectedAll(cursor.getCount() == selectedSet.size());
    	}
    }

    private void refreshUI(Cursor cursor, HashSet<String> selectedSet) {
    	if (null != cursor) {
    		mcontactAdapter.swapToCursor(cursor, selectedSet, currentTab);
            setSelectedAll(cursor.getCount() == selectedSet.size());
    	}
    }


	private ArrayList<ContactSimpleInfo> getSelectedUsersPhone()
	{
		ArrayList<ContactSimpleInfo> info = new ArrayList<ContactSimpleInfo>();
        final int selectedSize = mSelectedPhone.size();
        if (null != mCursorPhone && selectedSize > 0 && selectedSize <= mCursorPhone.getCount()) {
            final int size = mCursorPhone.getCount();
            final int curPos = mCursorPhone.getPosition();
            ContactSimpleInfo csi;
            for (int i = 0; i < size; ++i) {
                mCursorPhone.moveToPosition(i);
                final String phoneNumber = mCursorPhone.getString(mCursorPhone.getColumnIndex(Phone.NUMBER));
                if (!StringUtil.isEmpty(phoneNumber) && mSelectedPhone.contains(phoneNumber)) {
                    csi = new ContactSimpleInfo();
                    csi.display_name_primary = mCursorPhone.getString(mCursorPhone.getColumnIndex(Contacts.DISPLAY_NAME));
                    csi.phone_number = phoneNumber;
                    csi.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
                    info.add(csi);
                } else {
                    Log.i(TAG, String.format("getSelectedUsersPhone, fail without phone number: %s, position: %s, name: %s",
                            phoneNumber, i, mCursorPhone.getString(mCursorPhone.getColumnIndex(Contacts.DISPLAY_NAME))));
                }
            }
            mCursorPhone.moveToPosition(curPos);
        }

        Log.d(TAG, "getSelectedUsersPhone, after phone selected count: " + info.size());

        final int selectedSimCount = mSelectedSim.size();
        if (null != mCursorSimContacts && selectedSimCount > 0 && selectedSimCount <= mCursorSimContacts.getCount()) {
            final int size = mCursorSimContacts.getCount();
            final int curPos = mCursorSimContacts.getPosition();
            ContactSimpleInfo csi;
            for (int i = 0; i < size; ++i) {
                mCursorSimContacts.moveToPosition(i);
                final String sinNumber = mCursorSimContacts.getString(mCursorSimContacts.getColumnIndex(SIM_COLUMN_NUMBER));
                if (!StringUtil.isEmpty(sinNumber) && mSelectedSim.contains(sinNumber)) {
                    csi = new ContactSimpleInfo();
                    csi.display_name_primary = mCursorSimContacts.getString(mCursorSimContacts.getColumnIndex(SIM_COLUMN_NAME));
                    csi.phone_number = sinNumber;
                    csi.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
                    info.add(csi);
                } else {
                    Log.i(TAG, String.format("parserCursor, fail without sim number: %s, position: %s, name %s",
                            sinNumber, i, mCursorSimContacts.getString(mCursorSimContacts.getColumnIndex(SIM_COLUMN_NUMBER))));
                }
            }
            mCursorSimContacts.moveToPosition(curPos);
        }

        Log.d(TAG, "getSelectedUsersPhone, after sim selected count: " + info.size());

		return info;
	}

	private ArrayList<ContactSimpleInfo> getSelectedUsersEmail()
	{
		ArrayList<ContactSimpleInfo> info = new ArrayList<ContactSimpleInfo>();
        final int selectedSize = mSelectedEmail.size();
        if (null != mCursorEmails && selectedSize > 0 && selectedSize <= mCursorEmails.getCount()) {
            final int size = mCursorEmails.getCount();
            final int curPos = mCursorEmails.getPosition();
            ContactSimpleInfo csi;
            for (int i = 0; i < size; ++i) {
                mCursorEmails.moveToPosition(i);
                final String emailAddress = mCursorEmails.getString(mCursorEmails.getColumnIndex(Email.DATA));
                if (!StringUtil.isEmpty(emailAddress) && mSelectedEmail.contains(emailAddress)) {
                    csi = new ContactSimpleInfo();
                    csi.display_name_primary = mCursorEmails.getString(mCursorEmails.getColumnIndex(Contacts.DISPLAY_NAME));
                    csi.email = emailAddress;
                    csi.type = ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL;
                    info.add(csi);
                } else {
                    Log.i(TAG, String.format("parserCursor, fail without email: %s, position:%s, name: %s",
                            emailAddress, i, mCursorEmails.getString(mCursorEmails.getColumnIndex(Contacts.DISPLAY_NAME))));
                }
            }
            mCursorEmails.moveToPosition(curPos);
        }

        Log.d(TAG, "getSelectedUsersEmail, email selected count: " + info.size());

		return info;
	}
	
	private class DeSerializationTask extends android.os.AsyncTask<Void, Void, Void>
    {       
        public DeSerializationTask()
        {
            super();            
            Log.d(TAG, "create DeSerializationTask="+this);
        }

		@Override
		protected Void doInBackground(Void... params)			  
        {
//			deSerialization(users_phone, posts_sfile);
//			deSerialization(users_email, posts_sfile_email);
			new GetContactTask().execute("");
            return null;
        }
    }
	
//	private void serialization(ArrayList<ContactSimpleInfo> userinfo, String profileName) {
//	    if(userinfo != null && userinfo.size() > 0) {
//			synchronized(userinfo) {
//				FileOutputStream fos;
//				ObjectOutputStream out;
//				try {
//					new File(profileName).createNewFile();
//					fos = new FileOutputStream(profileName);
//
//					out = new ObjectOutputStream(fos);
//				    Date date = new Date();
//				    out.writeLong(date.getTime());
//
//				    int count = userinfo.size();
//
//				    out.writeInt(count);
//				    for(int i=0;i<count;i++)
//				    {
//				    	ContactSimpleInfo item = userinfo.get(i);
//				    	item.selected = false;
//				    	out.writeObject(item);
//				    }
//
//				    out.close();
//				} catch(IOException ex) {
//				    Log.d(TAG, "serialization fail="+ex.getMessage());
//				}
//			}
//		}
//	}
	
	
	@Override
	protected void loadRefresh() {
		new GetContactTask().execute("");
	}
	
//	private static String posts_sfile = QiupuHelper.contacts;
//	private static String posts_sfile_email = QiupuHelper.contacts_email;
//	private void deSerialization(ArrayList<ContactSimpleInfo> userinfo, String filename) {
//		synchronized(userinfo) {
//			FileInputStream fis;
//			ObjectInputStream in;
//			try {
//                if (FileUtils.testReadFile(filename)) {
//                    fis = new FileInputStream(filename);
//				    in = new ObjectInputStream(fis);
//				    long lastrecord = in.readLong();
//				    Date now = new Date();
//				    final long interval = now.getTime() -lastrecord;
//				    if(interval > QiupuConfig.A_DAY)
//				    {
//				    	Log.d(TAG, String.format("it is %1$s hours ago, ignore the data", interval / QiupuConfig.AN_HOUR));
//				    	in.close();
//				    	return ;
//				    }
//
//				    int count = in.readInt();
//				    for(int i=0; i<count; i++)
//				    {
//				    	ContactSimpleInfo item = (ContactSimpleInfo) in.readObject();
//				    	userinfo.add(item);
//				    }
//				    in.close();
//                }
//
//				    removeRepeatContact(userinfo);
//				    mHandler.obtainMessage(SHOW_CONTACT).sendToTarget();
//			} catch(IOException ex) {
//				ex.printStackTrace();
//				try{
//				    new File(posts_sfile).delete();
//				}catch(Exception ne){}
//
//				Log.d(TAG, "deserialization fail="+ex.getMessage());
//			} catch(ClassNotFoundException ex) {
//				ex.printStackTrace();
//				Log.d(TAG, "deserialization fail="+ex.getMessage());
//			}
//		}
//	}

//	private ArrayList<ContactSimpleInfo>  tempPhoneList = new ArrayList<ContactSimpleInfo> ();
//	private ArrayList<ContactSimpleInfo>  tempEmailList = new ArrayList<ContactSimpleInfo> ();
	//new a task to get contact
	private class GetContactTask extends AsyncTask<String, String, String> {
		public String doInBackground(String... params) {
			Log.d(TAG, "GetContactTask doInBackground enter.");
			begin();			

//			if(mSendType == QiupuConfig.INVITE_TYPE_EMAIL)
//			{
//				GetLocalContactEmail();
//			}
//			else if(mSendType == QiupuConfig.INVITE_TYPE_MESSAGE)
//			{
//				GetLocalContact();
            mCursorEmails = getLocalEmailCursor(null);
            mCursorPhone = getLocalPhoneCursor(null);
			mCursorSimContacts = GetSimContact(null);
//			}
			Log.d(TAG, "GetContactTask doInBackground exit.");
			return "";
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		public void onPostExecute(String Re) {
			Log.d(TAG, "GetContactTask onPostExecute enter.");
//            if (false) {
//			//sort with Pinying
//			users_email.clear();
//			users_phone.clear();
//			ArrayList<ContactSimpleInfo> tmp = new ArrayList<ContactSimpleInfo>();
//			refreshUI(tmp);
//
//			ArrayList<ContactSimpleInfo>  tempPhoneList = new ArrayList<ContactSimpleInfo> ();
//            ArrayList<ContactSimpleInfo>  tempEmailList = new ArrayList<ContactSimpleInfo> ();
//
//            Log.d(TAG, "GetContactTask onPostExecute parse contacts start.");
//            parseSimContactCursor(tempPhoneList);
//            parsePhoneInLocalContact(tempPhoneList);
////            parseLocalContactCursor(tempPhoneList, tempEmailList);
//            parseEmailInLocalContact(tempEmailList);
//            Log.d(TAG, String.format("GetContactTask onPostExecute parse contacts size phone/email:%d/%d.",
//                    tempPhoneList.size(), tempEmailList.size()));
//            Log.d(TAG, String.format("onPostExecute, sim/phone/email cursor size:%d/%d/%d",
//                    mCursorSimContacts.getCount(), mCursorPhone.getCount(), mCursorEmails.getCount()));
//
////			users.addAll(getCommonContact());
////			if(mSendType == QiupuConfig.INVITE_TYPE_EMAIL)
////			{
//				Collections.sort(tempEmailList,new EmailComparator());
////			}
////			else if(mSendType == QiupuConfig.INVITE_TYPE_MESSAGE)
////			{
//				Collections.sort(tempPhoneList,new PhoneComparator());
////			}
//
//			Log.d(TAG, String.format("GetContactTask onPostExecute sort contacts size phone/email: %d/%d.",
//                    tempPhoneList.size(), tempEmailList.size()));
//
//			users_email.addAll(tempEmailList);
//			users_phone.addAll(tempPhoneList);
//			removeRepeatContact(users_email);
//			removeRepeatContact(users_phone);
//			Log.d(TAG, String.format("GetContactTask onPostExecute unique contacts size phone/email:%d/%d.",
//                    users_phone.size(), users_email.size()));
//
//			Collections.sort(users_phone, new NameComparator());
//			Collections.sort(users_email, new NameComparator());
//			Log.d(TAG, String.format("GetContactTask onPostExecute final sort contacts size phone/email:%d/%d.",
//                    users_phone.size(), users_email.size()));
//            }
            if (null != mCursorSimContacts && mCursorSimContacts.getCount() > 0) {
//                id_simnumber.setVisibility(View.VISIBLE);
            }

			if(currentTab == UITAB.DISPLAY_EMAIL)
			{
				refreshUI(mCursorEmails, mSelectedEmail);
			}
			else if (currentTab == UITAB.DISPLAY_PHONE)
			{
				refreshUI(mCursorPhone, mSelectedPhone);
			} else if (currentTab == UITAB.DISPLAY_SIM) {
                refreshUI(mCursorSimContacts, mSelectedSim);
            } else {
                Log.e(TAG, "onPostExecute, unexpected tab: " + currentTab);
            }
			end();
			Log.d(TAG, "GetContactTask onPostExecute exit.");
		}
	}
	
//	private class NameComparator implements Comparator{
//	    public int compare(Object o1,Object o2) {
//	    	ContactSimpleInfo c1=(ContactSimpleInfo)o1;
//	    	ContactSimpleInfo c2=(ContactSimpleInfo)o2;
//	    	Comparator cmp = Collator.getInstance(java.util.Locale.CHINA);
//	    	return cmp.compare(c1.display_name_primary, c2.display_name_primary);
//	       }
//	}
//
//	private class PhoneComparator implements Comparator{
//	    public int compare(Object o1,Object o2) {
//	    	ContactSimpleInfo c1=(ContactSimpleInfo)o1;
//	    	ContactSimpleInfo c2=(ContactSimpleInfo)o2;
//	    	return c1.phone_number.compareToIgnoreCase(c2.phone_number);
//	       }
//	}
//	private class EmailComparator implements Comparator{
//	    public int compare(Object o1,Object o2) {
//	    	ContactSimpleInfo c1=(ContactSimpleInfo)o1;
//	    	ContactSimpleInfo c2=(ContactSimpleInfo)o2;
//	    	return c1.email.compareToIgnoreCase(c2.email);
//	       }
//	}


    //get contact form local
//    private void GetLocalContact() {
//		Log.d(TAG, "GetLocalContact, enter.");
//
//        mCursorEmails = getContentResolver().query(Email.CONTENT_URI,
//                    new String[]{Contacts.DISPLAY_NAME, Email.DATA},
//                    null, null, null);
//
//        mCursorPhone =  getContentResolver().query(Phone.CONTENT_URI,
//                    new String[]{Contacts.DISPLAY_NAME, Phone.NUMBER},
//                    null, null, null);
//        Log.d(TAG, "GetLocalContact, exit.");
//    }


    //get contact form local
//    private void GetLocalContactEmail() {
//        mCursorEmail = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
//                new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME},
//                null, null, null);
//    }

	//is or not in list
//	private void removeRepeatContact(ArrayList<ContactSimpleInfo> list){
//		synchronized(list)
//		{
//			for(int index=0;index<list.size();index++)
//			{
//				ContactSimpleInfo item = list.get(index);
//				for(int j=index+1;j<list.size();j++)
//				{
//					//avoid last items
//					if(j >= list.size()-1)
//						break;
//
//					ContactSimpleInfo next = list.get(j);
//					if(item.type == ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL)
//					{
//						if(next.email.equalsIgnoreCase(item.email))
//						{
//							list.remove(j);
//							j--;
//						}
//						else
//						{
//							break;
//						}
//					}
//                    else if(item.type == ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE)
//					{
//						if(next.phone_number != null && next.phone_number.equalsIgnoreCase(item.phone_number))
//						{
//							Log.v(TAG, "remove phone number="+next);
//							list.remove(j);
//							j--;
//						}
//						else
//						{
//							break;
//						}
//					} else {
//                        Log.e(TAG, "removeRepeatContact, unexpected type:" + item);
//                    }
//				}
//            }
//		}
//	}
	
	//is or not phone number
	public static boolean IsUserNumber(String num){
		boolean re = false;
		if(num.length()==11)
		{
			if(num.startsWith("13")){
				re = true;
			}
			else if(num.startsWith("15")){
				re = true;
			}
			else if(num.startsWith("18")){
				re = true;
			}
		}
		return re;
	}
	
	//revert to phone number with 11 words
	public static String GetNumber(String num){
		if(num!=null)
		{
			if (num.startsWith("+86"))
	        {
				num = num.substring(3);
	        }
	        else if (num.startsWith("86")){
	        	num = num.substring(2);
	        }
		}
		else{
			num="";
		}
		return num;
	}
	
	
	private class MyWatcher implements TextWatcher 
    {
       public void afterTextChanged(Editable s)
       {
           //do search
           doSearch(s.toString());
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }

	private void doSearch(String key)
    {
//		searchUsers.clear();
		if(isEmpty(key) == false)
		{
            if (null != mCursorSearch && !mCursorSearch.isClosed()) {
                mCursorSearch.close();
            }

			if(currentTab == UITAB.DISPLAY_EMAIL) {
                mCursorSearch = getLocalEmailCursor(key);
//				for(int i=0;i<users_email.size();i++)
//				{
//					ContactSimpleInfo user = users_email.get(i);
//					if(user.display_name_primary.toLowerCase().contains(key) ||
//							   user.email.toLowerCase().contains(key))
//					{
//						searchUsers.add(user);
//					}
//				}
			}
			else if (currentTab == UITAB.DISPLAY_PHONE)
			{
                mCursorSearch = getLocalPhoneCursor(key);
//				for(int i=0;i<users_phone.size();i++)
//				{
//					ContactSimpleInfo user = users_phone.get(i);
//					if(user.display_name_primary.toLowerCase().contains(key) ||
//							   user.phone_number.toLowerCase().contains(key))
//					{
//						searchUsers.add(user);
//					}
//				}
			} else if (currentTab == UITAB.DISPLAY_SIM) {
                mCursorSearch = GetSimContact(key);
            } else {
                Log.e(TAG, "doSearch, unexpected tab: " + currentTab);
                return;
            }

			refreshUI(mCursorSearch, mSelectedSearch, UITAB.DISPLAY_SEARCH);
		}
		else
		{
			if(currentTab == UITAB.DISPLAY_EMAIL)
			{
				refreshUI(mCursorEmails, mSelectedEmail);
			}
			else if(currentTab == UITAB.DISPLAY_PHONE)
			{
				refreshUI(mCursorPhone, mSelectedPhone);
            } else if (currentTab == UITAB.DISPLAY_SIM) {
                refreshUI(mCursorSimContacts, mSelectedSim);
			} else {
                Log.e(TAG, "doSearch, unexpected tab: " + currentTab);
            }
		}
    }
	
	@Override
	protected void inviteUserDialogCallBack(String nameString, String typeString) {
		if(currentTab == UITAB.DISPLAY_EMAIL) {
            ContactSimpleInfo simpleninfo = new ContactSimpleInfo();
            simpleninfo.display_name_primary = nameString;
            simpleninfo.selected = true;

			simpleninfo.email = typeString;
            simpleninfo.type = ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL;

            ArrayList<ContactSimpleInfo> emailList = new ArrayList<ContactSimpleInfo>();
            emailList.add(simpleninfo);
//            contactsPhone = phones;
            contactsEmail = emailList;
            showDialog(SEND_MESSAGE_DIALOG);
		} else if(currentTab == UITAB.DISPLAY_PHONE || currentTab == UITAB.DISPLAY_SIM) {
            ContactSimpleInfo simpleninfo = new ContactSimpleInfo();
            simpleninfo.display_name_primary = nameString;
            simpleninfo.selected = true;

			simpleninfo.phone_number = typeString;
            simpleninfo.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;

            ArrayList<ContactSimpleInfo> phoneList = new ArrayList<ContactSimpleInfo>();
            phoneList.add(simpleninfo);
            contactsPhone = phoneList;
//            contactsEmail = emailList;
            showDialog(SEND_MESSAGE_DIALOG);
		} else {
            Log.e(TAG, "inviteUserDialogCallBack, unexpected ");
        }
	}
	
	private void setTitleUI(int tab)
	{
		currentTab = tab;
//		mbpcFriendsAdapter.setType(currentTab);
        final String keyword = keyEdit.getText().toString();

        TypedArray sa = obtainStyledAttributes(null, R.styleable.ContactListItemView);
        Drawable da = sa.getDrawable(0);
		if(tab == UITAB.DISPLAY_PHONE)
		{
			id_phonenumber.setBackgroundResource(R.drawable.list_selected_holo);
            id_simnumber.setBackgroundDrawable(da);
			id_email.setBackgroundDrawable(da);
            if (TextUtils.isEmpty(keyword)) {
                refreshUI(mCursorPhone, mSelectedPhone);
            } else {
                mCursorSearch = getLocalPhoneCursor(keyword);
                refreshUI(mCursorSearch, mSelectedSearch, UITAB.DISPLAY_SEARCH);
            }
		}
		else if(tab == UITAB.DISPLAY_EMAIL)
		{
			id_phonenumber.setBackgroundDrawable(da);
            id_simnumber.setBackgroundDrawable(da);
			id_email.setBackgroundResource(R.drawable.list_selected_holo);
            if (TextUtils.isEmpty(keyword)) {
                refreshUI(mCursorEmails, mSelectedEmail);
            } else {
                mCursorSearch = getLocalEmailCursor(keyword);
                refreshUI(mCursorSearch, mSelectedSearch, UITAB.DISPLAY_SEARCH);
            }
        } else if (tab == UITAB.DISPLAY_SIM) {
            id_simnumber.setBackgroundResource(R.drawable.list_selected_holo);
            id_phonenumber.setBackgroundDrawable(da);
            id_email.setBackgroundDrawable(da);
            if (TextUtils.isEmpty(keyword)) {
                refreshUI(mCursorSimContacts, mSelectedSim);
            } else {
                mCursorSearch = GetSimContact(keyword);
                refreshUI(mCursorSearch, mSelectedSearch, UITAB.DISPLAY_SEARCH);
            }
		} else {
            Log.e(TAG, "setTitleUI, unexpected tab: " + tab);
//            return;
        }
	}
	
//	private void setContactListSelected(ArrayList<ContactSimpleInfo> userinfo)
//	{
//		for(ContactSimpleInfo user : userinfo)
//		{
//			user.selected = selectall;
//			Log.d(TAG, "selectall" + selectall);
//		}
//		selectall = !selectall;
//		mcontactAdapter.notifyDataSetChanged();
//	}
//
//    private void parseSimContactCursor(ArrayList<ContactSimpleInfo> list) {
//        Log.d(TAG, "parseSimContactCursor, enter with list size:" + list.size());
//        if (mCursorSimContacts != null) {
//            while (mCursorSimContacts.moveToNext()) {
//            	final String phoneNumber = mCursorSimContacts.getString(1);
//            	if (StringUtil.isValidMobileNumber(phoneNumber)) {
//                    ContactSimpleInfo sci = new ContactSimpleInfo();
//                    sci.display_name_primary = mCursorSimContacts.getString(0);
//                    sci.phone_number = phoneNumber;
//
//                    list.add(sci);
//            	}
//            }
//        }
//        Log.d(TAG, "parseSimContactCursor, exit with list size:" + list.size());
//    }

//    private void parseLocalContactCursor(ArrayList<ContactSimpleInfo> phoneList, ArrayList<ContactSimpleInfo> emailList) {
//        Log.d(TAG, String.format("parseLocalContactCursor, enter with phone/email list size: %d/%d",
//                phoneList.size(), emailList.size()));
//
//        while (mCursorLocalContacts.moveToNext()) {
//            int contactId = mCursorLocalContacts.getInt(0);
//            String display_name_primary = mCursorLocalContacts.getString(1);
//
//            //get phone number
//            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
//                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
//                    null, null);
//            if (phones != null && phones.moveToFirst()) {
//                do {
//                    String phone = phones.getString(0);
//                    if (StringUtil.isValidString(phone)) {
////						if(IsUserNumber(phone)) {
//                        ContactSimpleInfo cci = new ContactSimpleInfo();
//                        cci.display_name_primary = display_name_primary;
//                        cci.phone_number = phone;
//                        cci.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
//                        phoneList.add(cci);
////						}
//                    }
//                } while (phones.moveToNext());
//            }
//            phones.close();
//            Log.v(TAG, String.format("parseLocalContactCursor, after %d added for %s in phone list:",
//                    phones.getCount(), display_name_primary, phoneList.size()));
//
//            //get email
//            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
//                    new String[]{ContactsContract.CommonDataKinds.Email.DATA},
//                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
//                    null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    String email = cursor.getString(0);// "data1"
//                    if (StringUtil.isValidString(email)) {
//                        ContactSimpleInfo cci = new ContactSimpleInfo();
//                        cci.display_name_primary = display_name_primary;
//                        cci.email = email;
//                        cci.type = ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL;
//                        emailList.add(cci);
//                    }
//                } while (cursor.moveToNext());
//
//                cursor.close();
//                Log.v(TAG, String.format("parseLocalContactCursor, after %d added for %s in phone list:",
//                        cursor.getCount(), display_name_primary, emailList.size()));
//            }
//        }
//
//        Log.d(TAG, String.format("parseLocalContactCursor, exit with phone/email list size: %d/%d",
//                phoneList.size(), emailList.size()));
//    }

//    private void parsePhoneInLocalContact(ArrayList<ContactSimpleInfo> phoneList) {
//        Log.d(TAG, String.format("parsePhoneInLocalContact, enter with phone list size: %d",
//                phoneList.size()));
//
//        if (null == mCursorPhone) {
//            Log.d(TAG, "parsePhoneInLocalContact, ignore null cursor.");
//            return;
//        }
//
//        mCursorPhone.moveToFirst();
//        String phoneNumber;
//        do {
//            phoneNumber = mCursorPhone.getString(1);
//            if (!StringUtil.isValidMobileNumber(phoneNumber)) {
//                ContactSimpleInfo cci = new ContactSimpleInfo();
//                cci.display_name_primary = mCursorPhone.getString(0);
//                cci.phone_number = phoneNumber;
//                cci.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
//                phoneList.add(cci);
//            }
//        } while (mCursorPhone.moveToNext());
//
//        Log.d(TAG, String.format("parsePhoneInLocalContact, exit with phone list size: %d",
//                phoneList.size()));
//    }
//
//    private void parseEmailInLocalContact(ArrayList<ContactSimpleInfo> emailList) {
//        Log.d(TAG, String.format("parseEmailInLocalContact, enter with email list size: %d",
//                emailList.size()));
//
//        if (null == mCursorEmails) {
//            Log.d(TAG, "parsePhoneInLocalContact, ignore null cursor.");
//            return;
//        }
//
//        mCursorEmails.moveToFirst();
//        String emailAddress;
//        do {
//            emailAddress = mCursorEmails.getString(1);
//            if (StringUtil.isValidEmail(emailAddress)) {
//                ContactSimpleInfo cci = new ContactSimpleInfo();
//                cci.display_name_primary = mCursorEmails.getString(0);
//                cci.email = emailAddress;
//                cci.type = ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL;
//                emailList.add(cci);
//            }
//        } while (mCursorEmails.moveToNext());
//
//        Log.d(TAG, String.format("parseEmailInLocalContact, exit with email list size: %d",
//                emailList.size()));
//    }

    private void setSelectedAll(boolean value) {
        selectall = value;
        head_select_all.setImageResource(selectall?
                R.drawable.ic_btn_choice_press: R.drawable.ic_btn_choice);
    }

    private static final String SIM_COLUMN_NAME = "name";
    private static final String SIM_COLUMN_NUMBER = "number";
    public ContactSimpleInfo parserCursor(Cursor cursor, int bindId, int position) {
        if (null == cursor) {
            Log.d(TAG, "parserCursor, return null for empty cursor");
            return null;
        } else if (position < 0 || position >= cursor.getCount()) {
            Log.d(TAG, "parserCursor, return null for invalid position:" + position);
            return null;
        }

        final int parseTab = TextUtils.isEmpty(keyEdit.getText().toString()) ? bindId : currentTab;
        ContactSimpleInfo info = null;
        final int oldPosition = cursor.getPosition();
        cursor.moveToPosition(position);
        if (parseTab == InviteContactActivity.UITAB.DISPLAY_SIM) {
            final String phoneNumber = cursor.getString(cursor.getColumnIndex(SIM_COLUMN_NUMBER));
            if (!StringUtil.isEmpty(phoneNumber)) {
                info = new ContactSimpleInfo();
                info.display_name_primary = cursor.getString(cursor.getColumnIndex(SIM_COLUMN_NAME));
                info.phone_number = phoneNumber;
                info.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
            } else {
                Log.i(TAG, String.format("parserCursor, fail without sim number: %s, position: %s, name %s",
                        phoneNumber, position,cursor.getString(cursor.getColumnIndex(SIM_COLUMN_NUMBER))));
            }
        } else if (parseTab == InviteContactActivity.UITAB.DISPLAY_PHONE) {
            final String phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
            if (!StringUtil.isEmpty(phoneNumber)) {
                info = new ContactSimpleInfo();
                info.display_name_primary = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
                info.phone_number = phoneNumber;
                info.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
                if (mIsPickContact) {
                    info.mPhotoId = cursor.getLong(cursor.getColumnIndex(Contacts.PHOTO_ID));
                    info.mContactId = cursor.getLong(cursor.getColumnIndex(Phone.CONTACT_ID));
                }
            } else {
                Log.i(TAG, String.format("parserCursor, fail without phone number: %s, position: %s, name: %s",
                        phoneNumber, position,cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME))));
            }
        } else if (parseTab == InviteContactActivity.UITAB.DISPLAY_EMAIL) {
            final String emailAddress = cursor.getString(cursor.getColumnIndex(Email.DATA));
            if (!StringUtil.isEmpty(emailAddress)) {
                info = new ContactSimpleInfo();
                info.display_name_primary = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
                info.email = emailAddress;
                info.type = ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL;
            } else {
                Log.i(TAG, String.format("parserCursor, fail without email: %s, position:%s, name: %s",
                        emailAddress, position, cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME))));
            }
        }
        cursor.moveToPosition(oldPosition);

        return info;
    }

    private static final String SORT_ORDER = Contacts.DISPLAY_NAME + " ASC";
    private Cursor getLocalEmailCursor(String keyword) {
        final String where = TextUtils.isEmpty(keyword) ?
                Email.DATA +" IS NOT NULL and " + Email.DATA + "!= ''" :
                Email.DATA +" IS NOT NULL and " + Email.DATA + "!= '' and (" +
                        Contacts.DISPLAY_NAME + " like '%" + keyword + "%' or " +
                        Email.DATA + " like '%" + keyword + "%')";
        return getContentResolver().query(Email.CONTENT_URI,
                    new String[]{Contacts.DISPLAY_NAME, Email.DATA},
                    where, null, SORT_ORDER);
    }

    private Cursor getLocalPhoneCursor(String keyword) {
        final String where = TextUtils.isEmpty(keyword) ?
                Phone.NUMBER + " IS NOT NULL and " + Phone.NUMBER + "!= ''" :
                Phone.NUMBER + " IS NOT NULL and " + Phone.NUMBER + "!= '' and (" +
                        Contacts.DISPLAY_NAME + " like '%" + keyword + "%' or " +
                        Phone.NUMBER + " like '%" + keyword + "%')";
        String[] projection = null;
        if (mIsPickContact) {
            projection = new String[]{Contacts.DISPLAY_NAME, Phone.NUMBER, Contacts.PHOTO_ID, Phone.CONTACT_ID};
        } else {
            projection = new String[]{Contacts.DISPLAY_NAME, Phone.NUMBER};
        }
        return getContentResolver().query(Phone.CONTENT_URI, projection, where, null, SORT_ORDER);
    }

    //get contact for sim card
    private Cursor GetSimContact(String keyword) {
    	Log.d(TAG, "GetSimContact, enter.");
        try {
            Intent intent = new Intent();
            intent.setData(Uri.parse(SIM_CONTACT_URI));
            Uri uri = intent.getData();
            final String where = TextUtils.isEmpty(keyword) ?
                    SIM_COLUMN_NUMBER + " IS NOT NULL and " + SIM_COLUMN_NUMBER + " not like '% %'" :
                    SIM_COLUMN_NUMBER + " IS NOT NULL and " + SIM_COLUMN_NUMBER + " not like '% %' and (" +
                            SIM_COLUMN_NAME + " like '%" + keyword + "%' or " +
                            SIM_COLUMN_NUMBER + " like '%" + keyword + "%')";
            return getContentResolver().query(uri, new String[]{SIM_COLUMN_NAME, SIM_COLUMN_NUMBER},
                    where, null, SORT_ORDER);
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
        Log.d(TAG, "GetSimContact, exit.");
        return null;
    }
    
    @Override
    protected void onMessageInvited() {
    	mSelectedPhone.clear();
    	mSelectedSim.clear();
    	
//		refreshUI(mCursorEmails, mSelectedEmail);
		refreshUI(mCursorPhone, mSelectedPhone);
//        refreshUI(mCursorSimContacts, mSelectedSim);
    }
    
    @Override
    protected void onEmailInvited() {
    	mSelectedEmail.clear();
    	refreshUI(mCursorEmails, mSelectedEmail);
//		refreshUI(mCursorPhone, mSelectedPhone);
//        refreshUI(mCursorSimContacts, mSelectedSim);
    }
}
