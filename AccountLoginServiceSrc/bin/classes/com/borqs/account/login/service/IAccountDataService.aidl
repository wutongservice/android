package com.borqs.account.login.service;
interface IAccountDataService {
    String getUserData(String key);
    void setUserData(String key, String value);
    String getVersion();
}