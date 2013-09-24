package twitter4j;

import android.os.Parcel;
import android.os.Parcelable;



public class Requests implements java.io.Serializable, Comparable<Requests>, Parcelable{
	private static final long serialVersionUID = -6252503494004872356L;
	public String rid;
    public int    type;
    public String message;
    public long   createTime;
    public QiupuUser user;
    public String data;
    

	public static final int REQUEST_TYPE_EXCHANGE_VCARD  = 1;//friends request access profile
//	public static final int REQUEST_TYPE_QIU_GUANZHU   = 2;//request follow me
//	public static final int REQUEST_TYPE_REQUEST_FOLLOW_BACK_ME = 3;//friends added me into his circle
	public static final int REQUEST_TYPE_CHANGE_PHONE_1 = 4;
	public static final int REQUEST_TYPE_CHANGE_PHONE_2 = 5;
	public static final int REQUEST_TYPE_CHANGE_PHONE_3 = 6;
	public static final int REQUEST_TYPE_CHANGE_EMAIL_1 = 7;
	public static final int REQUEST_TYPE_CHANGE_EMAIL_2 = 8;
	public static final int REQUEST_TYPE_CHANGE_EMAIL_3 = 9;
	
    public static final int REQUEST_PUBLIC_CIRCLE_INVITE = 10;
    public static final int REQUEST_PUBLIC_CIRCLE_JOIN = 11;
    public static final int REQUEST_EVENT_INVITE= 18;
    public static final int REQUEST_EVENT_JOIN = 19;
    
    public static final String getWutongRequesttypes() {
    	StringBuilder types = new StringBuilder();
    	types.append(REQUEST_TYPE_EXCHANGE_VCARD).append(",")
    	.append(REQUEST_TYPE_CHANGE_PHONE_1).append(",")
    	.append(REQUEST_TYPE_CHANGE_PHONE_2).append(",")
    	.append(REQUEST_TYPE_CHANGE_PHONE_3).append(",")
    	.append(REQUEST_TYPE_CHANGE_EMAIL_1).append(",")
    	.append(REQUEST_TYPE_CHANGE_EMAIL_2).append(",")
    	.append(REQUEST_TYPE_CHANGE_EMAIL_3).append(",")
    	.append(REQUEST_EVENT_INVITE).append(",")
    	.append(REQUEST_EVENT_JOIN).append(",")
    	.append(REQUEST_PUBLIC_CIRCLE_INVITE).append(",")
    	.append(REQUEST_PUBLIC_CIRCLE_JOIN).toString();
    	
    	return types.toString();
    }
    
    public static final String getFriendsRequestTypes() {
    	StringBuilder types = new StringBuilder();
    	types.append(REQUEST_TYPE_CHANGE_PHONE_1).append(",")
    	     .append(REQUEST_TYPE_CHANGE_PHONE_2).append(",")
    	     .append(REQUEST_TYPE_CHANGE_PHONE_3).append(",")
    	     .append(REQUEST_TYPE_CHANGE_EMAIL_1).append(",")
    	     .append(REQUEST_TYPE_CHANGE_EMAIL_2).append(",")
    	     .append(REQUEST_TYPE_CHANGE_EMAIL_3);
    	
    	return types.toString(); 
    }
    
    public static final String getEventRequestTypes () {
    	return new StringBuilder().append(REQUEST_EVENT_INVITE).append(",")
    			.append(REQUEST_EVENT_JOIN).toString();
    }
    
    public static final String getPublicCircleRequestTypes () {
    	return new StringBuilder().append(REQUEST_PUBLIC_CIRCLE_INVITE).append(",")
    			.append(REQUEST_PUBLIC_CIRCLE_JOIN).toString();
    }
    
    public static final boolean isEventRequest(int type) {
    	return type == REQUEST_EVENT_INVITE || type == REQUEST_EVENT_JOIN;
    }
    public static  final boolean isCircleRequest(int type) {
    	return type == REQUEST_PUBLIC_CIRCLE_INVITE || type == REQUEST_PUBLIC_CIRCLE_JOIN;
    }
    public static final boolean isFriendRequest(int type) {
    	return type == REQUEST_TYPE_CHANGE_PHONE_1 || type == REQUEST_TYPE_CHANGE_PHONE_2
    			|| type == REQUEST_TYPE_CHANGE_PHONE_3 || type == REQUEST_TYPE_CHANGE_EMAIL_1
    			|| type == REQUEST_TYPE_CHANGE_EMAIL_2 || type == REQUEST_TYPE_CHANGE_EMAIL_3;
    }
    
    public int compareTo(Requests obj) {
		if(type > obj.type)
		{
			return -1;
		}
		else if(type < obj.type)
		{
			return 1;
		}
		else
		{
			if(createTime > obj.createTime)
			{
				return -1;
			}
			else if(createTime < obj.createTime)
			{
				return 1;
			}
		}
		return 0;
	}
    
    public boolean equals(Object obj)
	{
		if(!(obj instanceof Requests))
		{
			return false;
		}
		Requests rq = (Requests)obj;
		return (rq.rid.equals(rid));		
	}

	@Override
	public int describeContents()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg0)
	{
		dest.writeString(rid);
		dest.writeInt(type);
		dest.writeString(message);
		dest.writeLong(createTime);
		dest.writeString(data);
		dest.writeSerializable(user);
	}
	
	public static final Parcelable.Creator<Requests> CREATOR = new Creator<Requests>(){

		@Override
		public Requests createFromParcel(Parcel source)
		{
			 Requests re = new Requests();    
             re.rid = source.readString();    
             re.type = source.readInt();    
             re.message = source.readString();    
             re.createTime = source.readLong();
             re.data = source.readString();
             re.user = (QiupuUser) source.readSerializable();
             return re;    
		}

		@Override
		public Requests[] newArray(int size)
		{
			return new Requests[size];
		}  
		
	};
	
	public static String getrequestTypeIds(String requestId)
	{		
		if(requestId != null && requestId.length() > 0)
		{
			return requestId + "," + Requests.REQUEST_TYPE_EXCHANGE_VCARD;
		}
		else
		{
			return String.valueOf(Requests.REQUEST_TYPE_EXCHANGE_VCARD);
		}
	}
}
