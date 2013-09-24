package com.borqs.syncml.ds.imp.tag;

import java.io.UnsupportedEncodingException;
import java.util.List;

/*
 * According to tag contect to caculate tag size
 */
public class CaculateTagSize {
	// final value of tag(<syncml></syncml>) for caculate
	public static final int TAG_SIZE = 4;
	
	/**
	 * get size of String
	 * 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	public static int getSize(String strObj, String encode) throws UnsupportedEncodingException{
		if(strObj == null){
			return 0;
		}
		return TAG_SIZE + strObj.getBytes(encode).length;
	}
	
	/**
	 * get size of int
	 * 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	public static int getSize(int intObj, String encode) throws UnsupportedEncodingException{
		return TAG_SIZE + Integer.toString(intObj).getBytes(encode).length;
	}
	
	/**
	 * get size of ITag
	 * 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	public static int getSize(ITag tag, String encode) throws UnsupportedEncodingException{
		if(tag == null){
			return 0;
		}
		return tag.size(encode);
	}
	
	/**
	 * get size of boolean
	 * 
	 */
	public static int getSize(boolean isNeedSize){
		if(isNeedSize){
			return TAG_SIZE;
		} else {
			return 0;
		}
	}
	
	/**
	 * get size of List<TagItem>
	 * 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	public static int getSize(List tagList, String encode) throws UnsupportedEncodingException{
		if(tagList == null){
			return 0;
		}
		int size = 0;
		for(Object tag : tagList){
			if(tag != null){
				if (tag instanceof ITag) {
					size += ((ITag) tag).size(encode);					
				}
			}
		}
		return size;
	}
}
