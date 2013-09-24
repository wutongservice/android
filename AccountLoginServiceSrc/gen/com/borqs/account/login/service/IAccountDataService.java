/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/b058/code/borqsservice/sandbox/AccountLoginServiceSrc/src/com/borqs/account/login/service/IAccountDataService.aidl
 */
package com.borqs.account.login.service;
public interface IAccountDataService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.borqs.account.login.service.IAccountDataService
{
private static final java.lang.String DESCRIPTOR = "com.borqs.account.login.service.IAccountDataService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.borqs.account.login.service.IAccountDataService interface,
 * generating a proxy if needed.
 */
public static com.borqs.account.login.service.IAccountDataService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.borqs.account.login.service.IAccountDataService))) {
return ((com.borqs.account.login.service.IAccountDataService)iin);
}
return new com.borqs.account.login.service.IAccountDataService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getUserData:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getUserData(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_setUserData:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.setUserData(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getVersion:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getVersion();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.borqs.account.login.service.IAccountDataService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public java.lang.String getUserData(java.lang.String key) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
mRemote.transact(Stub.TRANSACTION_getUserData, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setUserData(java.lang.String key, java.lang.String value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeString(value);
mRemote.transact(Stub.TRANSACTION_setUserData, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getVersion() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getVersion, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getUserData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_setUserData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public java.lang.String getUserData(java.lang.String key) throws android.os.RemoteException;
public void setUserData(java.lang.String key, java.lang.String value) throws android.os.RemoteException;
public java.lang.String getVersion() throws android.os.RemoteException;
}
