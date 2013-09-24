package com.borqs.qiupu.test;

import com.borqs.contacts.model.ContactStruct;
import com.borqs.contacts.model.IOperator;

public class UTOperator implements IOperator {

	private String accountName;
	private String accountType;
	private DataMaker dm;
	public UTOperator(DataMaker dm) {
		this.dm = dm;
	}

	@Override
	public long add(ContactStruct d) {
		if(null != this.accountName && null != this.accountType) {
			d.setAccount(accountName, accountType);
		}
		return dm.addNew(d);
	}

	@Override
	public boolean update(long id, ContactStruct d) {
		if(dm.mData.containsKey(id))
		{
			dm.mData.put(id, d);
			return true;
		}
		return false;
	}

	@Override
	public boolean delete(long id) {
		if(dm.mData.containsKey(id))
		{
			dm.mData.remove(id);
			return true;
		}
		return false;
	}

	@Override
	public Object load(long id) {
		if(dm.mData.containsKey(id))
		{
			return dm.mData.get(id);
		}
		return null;
	}	

	@Override
	public long[] getCurrentItems(String accountName, String accountType) {
		return null;
	}

	@Override
	public void setTargetAccount(String accountName, String accountType) {
		this.accountName = accountName;
		this.accountType = accountType;
	}

	@Override
	public boolean batchUpdate(long[] ids, int start, int length,
			ContactStruct[] contacts) {
		for(int i = 0;i<length;i++)
		{
			if(!update(ids[start+i],contacts[start+i]))
				return false;
		}
		return true;
	}

	@Override
	public boolean batchDelete(long[] ids, int start, int length) {
		for(int i = 0;i<length;i++)
		{
			if(!delete(ids[start+i]))
				return false;
		}
		return true;
	}

	@Override
	public long[] batchAdd(ContactStruct[] contacts) {
		long[] ids = new long[contacts.length];
		for(int i=0;i<ids.length;i++)
		{
			long id = add(contacts[i]);
			ids[i] = id;
		}
		return ids;
	}

}
