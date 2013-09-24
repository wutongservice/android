/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.syncml.ds.protocol;

/**
 * created for client and server data change listener
 * @author b211
 *
 */
public interface IDataChangeListener {
    public class ContactsChangeData{
        public String name;
        public long rawContactId;
        public long contactId;
    }
    public void onBegin();
    public void onClientAdd(ContactsChangeData data);
    public void onClientDelete(ContactsChangeData data);
    public void onClientUpdate(ContactsChangeData data);
    public void onServerAdd(ContactsChangeData data);
    public void onServerUpdate(ContactsChangeData data);
    public void onServerDelete(ContactsChangeData data);
    public void onEnd(boolean isSuccess);
}
