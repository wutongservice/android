package com.borqs.syncml.ds.imp.tag;

public class AlertCode {
	// Alert Codes
	public static final int ALERT_CODE_FAST = 200;
	public static final int ALERT_CODE_SLOW = 201;
	public static final int ALERT_CODE_ONE_WAY_FROM_CLIENT = 202;
	public static final int ALERT_CODE_REFRESH_FROM_CLIENT = 203;
	public static final int ALERT_CODE_ONE_WAY_FROM_SERVER = 204;
	public static final int ALERT_CODE_REFRESH_FROM_SERVER = 205;
	public static final int ALERT_CODE_TWO_WAY_BY_SERVER = 206;
	public static final int ALERT_CODE_ONE_WAY_FROM_CLIENT_BY_SERVER = 207;
	public static final int ALERT_CODE_REFRESH_FROM_CLIENT_BY_SERVER = 208;
	public static final int ALERT_CODE_ONE_WAY_FROM_SERVER_BY_SERVER = 209;
	public static final int ALERT_CODE_REFRESH_FROM_SERVER_BY_SERVER = 210;

	// Specifies a request for sync results.
	public static final int ALERT_CODE_RESULT_ALERT = 221;

	// Specifies a request for the next message in the package.
	public static final int ALERT_CODE_NEXT_MESSAGE = 222;

	// End of Data for chunked object not received.
	public static final int ALERT_CODE_NO_END_OF_DATA = 223;

	/**
	 * No instances of this class
	 */
	private AlertCode() {

	}
}
