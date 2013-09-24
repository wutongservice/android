package com.borqs.sync.ds.datastore;

import com.borqs.sync.ds.datastore.contacts.ContactFieldsProcess;
import com.borqs.syncml.ds.imp.common.Constant;




public class GeneralPimWordsProcess {
	
	/*
	 * according to server maxlength ,
	 * deal with the fields before send to server
	 */
	public static Object dealFieldsProcess(String prefix, Object struct){
		if(struct == null){
			return null;
		}
		IFieldsInterface<?> fInf = null;
		IFieldsInterface<Object> oInf;

		if(Constant.PREFIX_CONTACTS.equals(prefix)){
			// contact
			fInf = new ContactFieldsProcess();
		} else if(Constant.PREFIX_EVENT.equals(prefix) ){
//			// calendar	
//			fInf = new EventFieldsProcess();
		} else if(Constant.PREFIX_TASKS.equals(prefix) ){
//			// task	
//			fInf = new TaskFieldsProcess();
		}

		oInf = (IFieldsInterface<Object>) fInf;
		Object obj = oInf.structProcess(struct);
		return obj;
	}
	
	
	/**
	 * deal with the fields before save to phone
	 * @param prefix
	 * @param struct
	 * @return 
	 */
	public static Object dealFieldsProcessBeforeSave(String prefix, Object struct){
		if(struct == null){
			return null;
		}
		IFieldsInterface<?> fInf = null;
		IFieldsInterface<Object> oInf;

		if(Constant.PREFIX_CONTACTS.equals(prefix)){
			// contact
			fInf = new ContactFieldsProcess();
		} else if(Constant.PREFIX_EVENT.equals(prefix) ){
			// calendar	
		} else if(Constant.PREFIX_TASKS.equals(prefix) ){
			// task	
		}
		Object obj = null;
		if(fInf != null){
			oInf = (IFieldsInterface<Object>) fInf;
			obj = oInf.structProcessBeforeSave(struct);
		}else{
			obj = struct;
		}
		return obj;
	}
	

	

	
}
