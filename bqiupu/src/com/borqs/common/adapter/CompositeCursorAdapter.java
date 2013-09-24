package com.borqs.common.adapter;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-2
 * Time: 下午2:35
 * To change this template use File | Settings | File Templates.
 */

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CompositeCursorAdapter extends RecipientsAdapter {
    private boolean mCacheValid = true;
    private final Context mContext;
    private int mCount = 0;
    private boolean mNotificationNeeded;
    private boolean mNotificationsEnabled = true;
    private Partition[] mPartitions;
    private int mSize = 0;

    public CompositeCursorAdapter(Context paramContext, int paramInt) {
        super(paramContext);
        this.mContext = paramContext;
        this.mPartitions = new Partition[2];
    }

    public void addPartition(Partition paramPartition) {
        if (this.mSize >= this.mPartitions.length) {
            Partition[] arrayOfPartition1 = new Partition[2 + this.mSize];
            System.arraycopy(this.mPartitions, 0, arrayOfPartition1, 0, this.mSize);
            this.mPartitions = arrayOfPartition1;
        }
        Partition[] arrayOfPartition2 = this.mPartitions;
        int i = this.mSize;
        this.mSize = (i + 1);
        arrayOfPartition2[i] = paramPartition;
        invalidate();
        notifyDataSetChanged();
    }

    public void addPartition(boolean paramBoolean1, boolean paramBoolean2) {
        addPartition(new Partition(paramBoolean1, paramBoolean2));
    }

    // todo: verify if this method runs well
    public boolean areAllItemsEnabled() {
        int i = 0;
        while (true) {
            if (i >= this.mSize) {
                i = 1;
                break;
            } else {
                if (!this.mPartitions[i].hasHeader) {
                    i++;
                    continue;
                }
                i = 0;
                break;
            }
        }

        return i > 0;
    }

    protected void bindHeaderView(View paramView, int paramInt, Cursor paramCursor) {
    }

    protected abstract void bindView(View paramView, int paramInt1, Cursor paramCursor, int paramInt2);

    public void changeCursor(int paramInt, Cursor paramCursor) {
        Cursor localCursor = this.mPartitions[paramInt].cursor;
        if (localCursor != paramCursor) {
            if ((localCursor != null) && (!localCursor.isClosed()))
                localCursor.close();
            this.mPartitions[paramInt].cursor = paramCursor;
            if (paramCursor != null)
                this.mPartitions[paramInt].idColumnIndex = paramCursor.getColumnIndex("_id");
            invalidate();
            notifyDataSetChanged();
        }
    }

    public void clearPartitions() {
        for (int i = 0; ; i++) {
            if (i >= this.mSize) {
                invalidate();
                notifyDataSetChanged();
                return;
            }
            this.mPartitions[i].cursor = null;
        }
    }

    public void close() {
        for (int i = 0; ; i++) {
            if (i >= this.mSize) {
                this.mSize = 0;
                invalidate();
                notifyDataSetChanged();
                return;
            }
            Cursor localCursor = this.mPartitions[i].cursor;
            if ((localCursor == null) || (localCursor.isClosed()))
                continue;
            localCursor.close();
            this.mPartitions[i].cursor = null;
        }
    }

    protected void ensureCacheValid() {
        if (!this.mCacheValid)
            this.mCount = 0;

        for (int i = 0; ; i++) {
            if (i >= this.mSize) {
                this.mCacheValid = true;
                return;
            }

            Cursor localCursor = this.mPartitions[i].cursor;
            int j;
            if (localCursor == null)
                j = 0;
            else
                j = localCursor.getCount();

            if ((this.mPartitions[i].hasHeader) && ((j != 0) || (this.mPartitions[i].showIfEmpty)))
                j++;

            this.mPartitions[i].count = j;
            this.mCount = (j + this.mCount);
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public int getCount() {
        ensureCacheValid();
        return this.mCount;
    }

    public Cursor getCursor(int paramInt) {
        return this.mPartitions[paramInt].cursor;
    }

    protected View getHeaderView(int paramInt, Cursor paramCursor, View paramView, ViewGroup paramViewGroup) {
        View localView;
        if (paramView == null)
            localView = newHeaderView(this.mContext, paramInt, paramCursor, paramViewGroup);
        else
            localView = paramView;
        bindHeaderView(localView, paramInt, paramCursor);
        return localView;
    }

    public Object getItem(int paramInt) {
        Cursor localCursor = null;
        ensureCacheValid();
        int j = 0;
        int i = 0;
        while (i < this.mSize) {
            int k = j + this.mPartitions[i].count;
            if ((paramInt < j) || (paramInt >= k)) {
                j = k;
                i++;
                continue;
            }
            j = paramInt - j;
            if (this.mPartitions[i].hasHeader)
                j--;
            if (j == -1)
                break;
            localCursor = this.mPartitions[i].cursor;
            localCursor.moveToPosition(j);
        }
        return localCursor;
    }

    public long getItemId(int paramInt) {
        long l = 0L;
        ensureCacheValid();
        int j = 0;
        int i = 0;
        while (i < this.mSize) {
            int k = j + this.mPartitions[i].count;
            if ((paramInt < j) || (paramInt >= k)) {
                j = k;
                i++;
                continue;
            }
            j = paramInt - j;
            if (this.mPartitions[i].hasHeader)
                j--;
            if ((j == -1) || (this.mPartitions[i].idColumnIndex == -1))
                break;
            Cursor localCursor = this.mPartitions[i].cursor;
            if ((localCursor == null) || (localCursor.isClosed()) || (!localCursor.moveToPosition(j)))
                break;
            l = localCursor.getLong(this.mPartitions[i].idColumnIndex);
        }
        return l;
    }

    // todo: verify if this method runs well
    public int getItemViewType(int paramInt) {
        ensureCacheValid();
        int j = 0;
        int i = 0;
        for (; ; i++) {
            if (i >= this.mSize)
                throw new ArrayIndexOutOfBoundsException(paramInt);
            int k = j + this.mPartitions[i].count;
            if ((paramInt >= j) && (paramInt < k))
                break;
            j = k;
        }
        j = paramInt - j;

        if ((!this.mPartitions[i].hasHeader) || (j != 0))
            i = getItemViewType(i, paramInt);
        else
            i = -1;
        return i;
    }

    protected int getItemViewType(int paramInt1, int paramInt2) {
        return 1;
    }

    public int getItemViewTypeCount() {
        return 1;
    }

    public int getPartitionForPosition(int paramInt) {
        ensureCacheValid();
        int j = 0;
        int i = 0;
        for (; ; i++) {
            if (i >= this.mSize) {
                i = -1;
                break;
            }
            int k = j + this.mPartitions[i].count;
            if ((paramInt >= j) && (paramInt < k))
                break;
            j = k;
        }
        return i;
    }

    public int getPositionForPartition(int paramInt) {
        ensureCacheValid();
        int j = 0;
        for (int i = 0; ; i++) {
            if (i >= paramInt)
                return j;
            j += this.mPartitions[i].count;
        }
    }

    protected View getView(int paramInt1, Cursor paramCursor, int paramInt2, View paramView, ViewGroup paramViewGroup) {
        View localView;
        if (paramView == null)
            localView = newView(this.mContext, paramInt1, paramCursor, paramInt2, paramViewGroup);
        else
            localView = paramView;
        bindView(localView, paramInt1, paramCursor, paramInt2);
        return localView;
    }

    // todo: verify if this method runs well
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
        ensureCacheValid();
        int j = 0;
        int i = 0;
        for (; ; i++) {
            if (i >= this.mSize)
                throw new ArrayIndexOutOfBoundsException(paramInt);
            int k = j + this.mPartitions[i].count;
            if ((paramInt >= j) && (paramInt < k))
                break;
            j = k;
        }
        j = paramInt - j;
        if (this.mPartitions[i].hasHeader)
            j--;
        View localView;
        if (j != -1) {
            if (this.mPartitions[i].cursor.moveToPosition(j))
                localView = getView(i, this.mPartitions[i].cursor, j, paramView, paramViewGroup);
            else
                throw new IllegalStateException("Couldn't move cursor to position " + j);
        } else
            localView = getHeaderView(i, this.mPartitions[i].cursor, paramView, paramViewGroup);
        if (localView != null)
            return localView;
        throw new NullPointerException("View should not be null, partition: " + i + " position: " + j);
    }

    public int getViewTypeCount() {
        return 1 + getItemViewTypeCount();
    }

    protected void invalidate() {
        this.mCacheValid = false;
    }

    public boolean isEnabled(int paramInt) {
        boolean bool = false;
        ensureCacheValid();
        int k = 0;
        int i = 0;
        while (i < this.mSize) {
            int j = k + this.mPartitions[i].count;
            if ((paramInt < k) || (paramInt >= j)) {
                k = j;
                i++;
                continue;
            }
            j = paramInt - k;
            if ((this.mPartitions[i].hasHeader) && (j == 0))
                break;
            bool = isEnabled(i, j);
        }
        return bool;
    }

    protected boolean isEnabled(int paramInt1, int paramInt2) {
        return true;
    }

    public boolean isPartitionEmpty(int paramInt) {
        Cursor localCursor = this.mPartitions[paramInt].cursor;
        boolean ret;
        if ((localCursor != null) && (localCursor.getCount() != 0))
            ret = false;
        else
            ret = true;
        return ret;
    }

    protected View newHeaderView(Context paramContext, int paramInt, Cursor paramCursor, ViewGroup paramViewGroup) {
        return null;
    }

    protected abstract View newView(Context paramContext, int paramInt1, Cursor paramCursor, int paramInt2, ViewGroup paramViewGroup);

    public void notifyDataSetChanged() {
        if (!this.mNotificationsEnabled) {
            this.mNotificationNeeded = true;
        } else {
            this.mNotificationNeeded = false;
            super.notifyDataSetChanged();
        }
    }

    public static class Partition {
        int count;
        Cursor cursor;
        boolean hasHeader;
        int idColumnIndex;
        boolean showIfEmpty;

        public Partition(boolean paramBoolean1, boolean paramBoolean2) {
            this.showIfEmpty = paramBoolean1;
            this.hasHeader = paramBoolean2;
        }
    }
}