package com.borqs.qiupu.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;

import com.borqs.contacts.manage.IContactsManageAnalser;

public class UTAnalyser implements IContactsManageAnalser {

	private DataMaker dm;
	public UTAnalyser(DataMaker dm) {
		this.dm = dm;
	}

	@Override
	public ArrayList<HashSet<Long>> getDuplicatedContacts(Context context,
			String name, String type) {
		return dm.mMerge;
	}

	@Override
	public HashSet<Long> getNewContacts(Context context, String name,
			String type) {
		return dm.mImport;
	}

	@Override
	public ArrayList<HashSet<Long>> getMergeContacts(Context context,
			String name, String type, ArrayList<HashSet<Long>> list) {
		return dm.mSimple;
	}

	@Override
	public HashMap<Long, Boolean> getContactsBorqsFriend(Context context) {
		return new HashMap<Long, Boolean>();
	}

}
