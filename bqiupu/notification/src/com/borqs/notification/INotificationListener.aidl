package com.borqs.notification;

interface INotificationListener{
    /*
     * Notify user status of online and offline.
     */
    void onStatusChanged(boolean value);
}
