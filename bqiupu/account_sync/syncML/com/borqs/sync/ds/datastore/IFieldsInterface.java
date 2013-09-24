package com.borqs.sync.ds.datastore;

public interface IFieldsInterface<T>  {
	public T structProcess(T d);
	public T structProcessBeforeSave(T d);
	
}