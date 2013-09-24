package com.borqs.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import twitter4j.QiupuUser;

public class DetailContactInfoItemView extends SNSItemView {
    private final String TAG="DetailContactInfoItemView";
    
    private TextView contactInfoitem;
    private Context  mContext;
    private String   itemString;
    private ImageView itemIcon; 
    private ImageView requestIcon;
    private TextView  contact_bind;
    private boolean  isphone;
    private boolean isshowchangeRequest;
    private boolean isbind;
    private int item_type;
    private long muid;
    private QiupuORM orm;

    public interface ChangeRequestInterface {
        public void setChangeRequest(int type, String text);
    }

    public DetailContactInfoItemView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;
        orm = QiupuORM.getInstance(ctx);
    }

    public DetailContactInfoItemView(Context context, String item, boolean flag, boolean ischangerequest, int type, long uid) 
    {       
        super(context);
        mContext = context;
        itemString = item;
        isphone = flag;
        item_type = type;
        isshowchangeRequest = ischangerequest;
        muid = uid;
        orm = QiupuORM.getInstance(context);
        init();
    }
    
    public void setIsPhone(boolean flag)
    {
    	isphone = flag;
    }
    public void setShowChangeRequset(boolean flag)
    {
    	isshowchangeRequest = flag;
    }
    
    private void init() 
    {
        //Log.d(TAG,  "call DetailContactInfoItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View v  = factory.inflate(R.layout.user_detail_contact_item_view, null);      
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        addView(v);            
        
        contactInfoitem  = (TextView) v.findViewById(R.id.profile_contact_item_tv);
        itemIcon         = (ImageView) v.findViewById(R.id.profile_contact_item_icon);
        requestIcon      = (ImageView) v.findViewById(R.id.request_change_icon);
        contact_bind     = (TextView) v.findViewById(R.id.contact_bind);
        requestIcon.setOnClickListener(requestClickListener);
        setUI();        
    
    }   
    
    private void setUI()
    { 
    	contactInfoitem.setText(itemString);
    	if(AccountServiceUtils.getBorqsAccountID() == muid)
    	{
    	    
    		isbind = orm.getbindInfo(itemString, muid);
    		if(isbind)
    		{
    			contact_bind.setVisibility(View.VISIBLE);
    			contact_bind.setText("( " + mContext.getResources().getString(R.string.already_bind) + " )");
    		}
    	}
    	itemIcon.setImageResource(isphone ? R.drawable.profile_phone : R.drawable.profile_email);
    	requestIcon.setVisibility(isshowchangeRequest ? View.VISIBLE : View.GONE);
    }
    
    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    
    public void setContactInfoItem(String item) 
    {
    	itemString = item;
        setUI();
    }   

    View.OnClickListener requestClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
            if (mContext instanceof ChangeRequestInterface) {
                ChangeRequestInterface changeRequestInterface = (ChangeRequestInterface)mContext;
                changeRequestInterface.setChangeRequest(item_type, itemString);
            }
		}
	};
	@Override
	public String getText() 
	{		
		return itemString;
	}
	
	public int gettype()
	{
		return item_type;
	}
	
	public boolean getbind()
	{
		return isbind;
	}
	
//	private boolean isbindWithLoginName(String tmp)
//	{
//		QiupuUser user = orm.queryOneUserInfo(muid);
//		if(!isEmpty(tmp) && user != null)
//		{
//			return tmp.equals(user.login_email1) 
//			       || tmp.equals(user.login_email2) 
//			       || tmp.equals(user.login_email3)
//			       || tmp.equals(user.login_phone1)
//			       || tmp.equals(user.login_phone2)
//			       || tmp.equals(user.login_phone3);
//		}
//		return false;
//			
//	}
}
