package com.borqs.account.service;

public interface AccountListener {
	public void onLogin();
	public void onLogout();
	public void onCancelLogin();
}
