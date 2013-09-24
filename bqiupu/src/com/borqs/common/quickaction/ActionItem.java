package com.borqs.common.quickaction;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

/**
 * Action item, displayed as menu with icon and text.
 * 
 * @author Lorensius. W. L. T
 *
 */
public class ActionItem {
	private Bitmap icon;
	private String title;
	private OnClickListener listener;
	private String mtype;
	private String mUserId;
	private String mContactId;
	
	
	/**
	 * Constructor
	 */
	public ActionItem() {}
	
	/**
	 * Constructor
	 * 
	 * @param icon {@link Drawable} action icon
	 */
	public ActionItem(Bitmap icon) {
		this.icon = icon;
	}
	
	/**
	 * Set action title
	 * 
	 * @param title action title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Get action title
	 * 
	 * @return action title
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * Set action icon
	 * 
	 * @param icon {@link Drawable} action icon
	 */
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	
	/**
	 * Get action icon
	 * @return  {@link Drawable} action icon
	 */
	public Bitmap getIcon() {
		return this.icon;
	}
	
	/**
	 * Set on click listener
	 * 
	 * @param listener on click listener {@link View.OnClickListener}
	 */
	public void setOnClickListener(OnClickListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Get on click listener
	 * 
	 * @return on click listener {@link View.OnClickListener}
	 */
	public OnClickListener getListener() {
		return this.listener;
	}
	
	public void setType(String type) {
		mtype = type;
	}
	
	public String getType() {
		return this.mtype;
	}
	
	public void setUserId(String userid) {
		mUserId = userid;
	}
	
	public String getUserId() {
		return this.mUserId;
	}
	
	public void setContactId(String contactId) {
		mContactId = contactId;
	}
	
	public String getContactId() {
		return this.mContactId;
	}
}