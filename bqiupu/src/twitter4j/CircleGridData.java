package twitter4j;

import java.util.ArrayList;

import android.view.View;
import android.view.View.OnClickListener;


public class CircleGridData  {
	
	public int memberCount;
	public String name = null;
	public ArrayList<UserImage>  memberList;
	public View.OnClickListener  clickListener;
	public CircleGridData(int memberCount, String name,
			ArrayList<UserImage> memberList, OnClickListener clickListener) {
		super();
		this.memberCount = memberCount;
		this.name = name;
		this.memberList = memberList;
		this.clickListener = clickListener;
	}
	
	

}
