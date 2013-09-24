package com.borqs.qiupu.test;

import android.content.Context;

import com.borqs.contacts.manage.ImportMergeService;

public class UTService extends ImportMergeService {

	DataMaker dm;
	public UTService(DataMaker dm,Context c)
	{
		this.dm = dm;
		mContext = c;
		initMember();
	}

	@Override
	protected void initMember() {
		contactOperator = new UTOperator(dm);
		dataAnalyser = new UTAnalyser(dm);
		dm.genData(contactOperator);
		super.initMember();
	}
	
	public void Test()
	{
		doAnalyseWork();
		doMergeWork(group);
	}
}
