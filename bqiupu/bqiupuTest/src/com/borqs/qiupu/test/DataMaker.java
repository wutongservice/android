package com.borqs.qiupu.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.borqs.contacts.model.ContactStruct;
import com.borqs.contacts.model.IOperator;

public class DataMaker {
	
	protected HashMap<Long,ContactStruct> mData = new HashMap<Long,ContactStruct>();
	protected HashSet<Long> mImport = new HashSet<Long>();
	protected ArrayList<HashSet<Long>> mSimple = new ArrayList<HashSet<Long>>();
	protected ArrayList<HashSet<Long>> mMerge = new ArrayList<HashSet<Long>>();
	protected final String borqsaccountname = "10214";
	protected final String borqsaccounttype = "com.borqs";
	protected final String notborqsaccountname = "huuzhou@gmail.com";
	protected final String notborqsaccounttype = "com.google";
	
	protected long mid;
	protected IOperator op;
	
	protected void genImportData()
	{		
		
	}	

	protected void genSimpleData()
	{
		
	}
	
	protected void genMergeData()
	{
		
	}
	
	protected void genOtherData()
	{
		
	}
	
	public void genData(IOperator contactOperator)
	{
		mData.clear();
		mImport.clear();
		mSimple.clear();
		mMerge.clear();
		mid = 0;
		//构造数据，分四种类型，必须保证数据的正确性以及各种数据之间
		//不会相互影响
		op = contactOperator;
		
		genImportData();//直接导入的数据
		genSimpleData();//不需要用户操作，可以直接合并的数据
		genMergeData();//需要用户选择名字之后进行合并的数据
		genOtherData();//其他不需要处理的数据
	}

	public long addNew(ContactStruct d) {
		long id = ++mid;
		mData.put(id, d);
		return id;
	}
}
