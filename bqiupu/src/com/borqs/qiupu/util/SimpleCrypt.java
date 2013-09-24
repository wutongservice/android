package com.borqs.qiupu.util;

public class SimpleCrypt
{

	public static String decode(String str) {
		// TODO Auto-generated method stub
				
		return new String(Base64.decode(str.getBytes(), Base64.DEFAULT));
	}

	public static String encode(String str) {
		// TODO Auto-generated method stub		
				
		return new String(Base64.encode(str.getBytes(), Base64.DEFAULT));
	}
	
	
	public static void main(String[] args)
	{
		String str = "abcd";
		String encode = SimpleCrypt.encode(str);
		System.out.println("encode: " + encode);		
		System.out.println("decode: " + SimpleCrypt.decode(encode));
	}
}