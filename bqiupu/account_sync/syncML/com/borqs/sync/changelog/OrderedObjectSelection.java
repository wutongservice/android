package com.borqs.sync.changelog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class OrderedObjectSelection {
	private List<SimpleEntry> mModified;
	private List mDeleted;
	private List mAdded;
	private List mSame;

	private SelectionComparor mComparor;

	public OrderedObjectSelection(SelectionComparor comparor) {
		mModified = new ArrayList();
		mDeleted = new ArrayList();
		mAdded = new ArrayList();
		mSame = new ArrayList();
		mComparor = comparor;
	}

	public void selection(List cur, List pre) {
		Collections.sort(cur);
		Collections.sort(pre);

		Object curItem = null;
		Object preItem = null;

		boolean prevMoveFlag = true;
		boolean curMoveFlag = true;

		Iterator curIterator = cur.iterator();
		Iterator preIterator = pre.iterator();

		do {
			if (curMoveFlag) {
				if (curIterator.hasNext()) {
					curItem = curIterator.next();
				} else {
					curItem = null; // no item remains
				}
				curMoveFlag = false;
			}

			if (prevMoveFlag) {
				if (preIterator.hasNext()) {
					preItem = preIterator.next();
				} else {
					preItem = null;
				}
				prevMoveFlag = false;
			}

			// terminal condition
			if (curItem == null && preItem == null) {
				break;
			} else if (curItem != null && preItem == null) {
				mAdded.add(curItem);
				curMoveFlag = true;
			} else if (curItem == null && preItem != null) {
				mDeleted.add(preItem);
				prevMoveFlag = true;
			} else {
				int c = mComparor.compareKey(curItem, preItem);
				if (c == 0) {
					if (mComparor.compareHash(curItem, preItem)) {
						mSame.add(curItem);
					} else {
						mModified.add(new SimpleEntry(curItem,
								preItem));
					}
					curMoveFlag = true;
					prevMoveFlag = true;
				} else if (c < 0) {
					mAdded.add(curItem);
					curMoveFlag = true;
					prevMoveFlag = false;
				} else {
					mDeleted.add(preItem);
					prevMoveFlag = true;
					curMoveFlag = false;
				}
			}
		} while (true);
	}

	public void release() {
		mModified = null;
		mDeleted = null;
		mAdded = null;
		mSame = null;
	}

	public List<SimpleEntry> getModified() {
		return mModified;
	}

	public List getDeleted() {
		return mDeleted;
	}

	public List getAdded() {
		return mAdded;
	}

	public List getSame() {
		return mSame;
	}
	
	public int getChangedSize() {
        int addedSize = mAdded == null ? 0 : mAdded.size();
        int modifiedSize = mModified == null ? 0 : mModified.size();
        int deletedSize = mDeleted == null ? 0 : mDeleted.size();
        return addedSize + modifiedSize + deletedSize;
    }
}
