package com.borqs.sync.changelog;

public interface SelectionComparor {
	public abstract int compareKey(Object cur, Object pre);

	public abstract boolean compareHash(Object cur, Object pre);

}
