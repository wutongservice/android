package com.borqs.notification;

import com.borqs.notification.INotificationListener;
import com.borqs.notification.IFileTransferListener;

interface INotificationService {

	/*
	 * App ID is used to identify the target app for a particular 
	 * notification.
	 *
	 * NotificationService(PushService): 99
	 * Sync: 11
	 * OpenFace: 12
	 * xMessage: 13
	 * Browser: 14
	 * ...
	 *
	 *
	 *
	 */
	boolean login();
	String getAccount();
	/*
	 * Queries an user for her/his online status and IP address.
	 *
	 * @param type: 0 - Borqs ID; 1 - Phone Number; 2 - Email.
	 */
	Map queryUser(in String name, in int type);

	/*
	 * Send a message to a peer app on other devices. You can capsulate 
	 * any data in @param data.
	 */
	boolean sendMessage(in int toAppId, String toUserName, String data);

	boolean isLogin();

	/*
	 * Send a file on local file system to a peer app on other devices. 
	 */
	void sendFile(String path, String toUserName, IFileTransferListener listener);

	boolean Poke();

	/*
	 * Register a notification listener. 
	 */
	void registerNotificationListener(INotificationListener listener);

}
