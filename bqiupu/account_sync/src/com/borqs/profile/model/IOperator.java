package com.borqs.profile.model;



public interface IOperator{
	
	public long add(ContactProfileStruct d);	
	
	public boolean update(long id, ContactProfileStruct d);

	public boolean delete(long id);

	public Object load(long id);

	public long[] getCurrentItems(String accountName, String accountType);
	
	public void setTargetAccount(String accountName, String accountType);

	public boolean batchUpdate(long ids[], int start, int length,
			ContactProfileStruct[] contacts);
	
	public boolean batchDelete(long ids[], int start, int length);
	
	public long[] batchAdd(ContactProfileStruct[] contacts);
}
