package com.borqs.information;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


public class InformationBase implements Parcelable{

	public long _id = 0;
	public long id;
	public String appId;
	public String type;
	public String image_url;
	public String receiverId;
	public String senderId;
	public long   date;
	public String title;
	public String body;
	public String uri;
	public String body_html;
	public String title_html;
	
	public Uri apppickurl;
	public String data;
	public long lastModified;
	public boolean read;
	
	public long scene;
	
	protected void parseData(){}
	protected void callAction(){}	
	
	public InformationBase() {
		super();
	}

	public InformationBase(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Notification id=" + id + " ");
		buffer.append("to=" + receiverId + " ");
		buffer.append("appid=" + appId + " ");
		buffer.append("type=" + type + " ");
		buffer.append("uri=" + uri + " ");
		buffer.append("title=" + title + " ");
		buffer.append("lastModified=" + lastModified + " ");
		return buffer.toString();
	}
	
	public void assignFrom(InformationBase info) {
		_id = info._id;
		id = info.id;
		appId = info.appId;
		type = info.type;
		image_url = info.image_url;
		receiverId = info.receiverId;
		senderId = info.senderId;
		date = info.date;
		title = info.title;
		title_html = info.title_html;
		body = info.body;
		body_html = info.body_html;
		uri = info.uri;	
		data = info.data;
		apppickurl = info.apppickurl == null?null:Uri.parse(info.apppickurl.toString());
		lastModified = info.lastModified;
		read = info.read;
		scene = info.scene;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		 dest.writeLong(_id);
		 dest.writeLong(id);
		 dest.writeString(appId == null ? "" : appId);
		 dest.writeString(image_url == null ? "" : image_url);
		 dest.writeString(receiverId == null ? "" : receiverId);
		 dest.writeString(senderId == null ? "" : senderId);
		 dest.writeLong(date);
		 dest.writeString(title == null ? "" : title);
		 dest.writeString(body == null ? "" : body);
		 dest.writeString(uri == null ? "" : uri);
		 dest.writeString(body_html == null ? "" : body_html);
		 dest.writeString(title_html == null ? "" : title_html);
		 dest.writeString(apppickurl == null ? "" : apppickurl.toString());
		 dest.writeString(data == null ? "" : data);
		 dest.writeLong(lastModified);
		 dest.writeString(String.valueOf(read));
		 dest.writeLong(scene);
	}	
	
	private void readFromParcel(Parcel in) {
		_id = in.readLong();
		id = in.readLong();
		appId = in.readString();
        image_url = in.readString();
        receiverId = in.readString();
        senderId = in.readString();
        date = in.readLong();
        title = in.readString();
        body = in.readString();
        uri = in.readString();
        body_html = in.readString();
        title_html = in.readString();
        apppickurl = Uri.parse(in.readString());
        data = in.readString();
        lastModified = in.readLong();
        Boolean Bl = new Boolean(in.readString());
        read = Bl.booleanValue();
        scene = in.readLong();
    }
	
	public static final Creator<InformationBase> CREATOR = new Creator<InformationBase>() {
        public InformationBase createFromParcel(Parcel source) {
            return new InformationBase(source);
        }

        public InformationBase[] newArray(int size) {
            return new InformationBase[size];
        }
    };
}
