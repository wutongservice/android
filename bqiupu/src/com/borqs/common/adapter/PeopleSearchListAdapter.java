package com.borqs.common.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import com.borqs.common.PeopleSearchResults;
import com.borqs.common.util.MentionDataCircle;
import com.borqs.common.util.MentionDataPerson;
import com.borqs.common.util.MentionTokenizer;
import com.borqs.qiupu.db.QiupuORM;

import java.util.concurrent.CountDownLatch;


public class PeopleSearchListAdapter extends CompositeCursorAdapter {
    private static final String TAG = "MentionPeopleAdapter";
    private boolean mIsMentionsAdapter = true;
    private boolean mPublicProfileSearchEnabled = true;
    private boolean mIncludePlusPages = true;
    private String mQuery;
    private boolean mResultsPreserved;

    public static abstract interface SearchListAdapterListener {
        public abstract void onAddPersonToCirclesAction(String paramString, MentionDataPerson paramPerson);
        public abstract void onCircleSelected(String paramString, MentionDataCircle paramCircle);
        public abstract void onPersonSelected(String paramString1, String paramString2, MentionDataPerson paramPerson);
        public abstract void onSearchListAdapterStateChange(PeopleSearchListAdapter paramPeopleSearchListAdapter);
    }

    private PeopleSearchResults mResults = new PeopleSearchResults();
    private SearchListAdapterListener mListener;
    private CompositeCursorAdapter.Partition[] mPartitions;
    private int mCount = 0;
    private boolean mCacheValid = true;
    private int mSize = 0;

    public PeopleSearchListAdapter(Context context, int paramInt) {
        super(context, 5);

        for (int i = 0; i < 5; i++)
            addPartition(false, false);
//        int j = 1024 + paramInt * 10;
//        int k = j + 1;
//        this.mCirclesLoaderId = j;
//        int m = k + 1;
//        this.mGaiaIdLoaderId = k;
//        int n = m + 1;
//        this.mPeopleLoaderId = m;
//        int i1 = n + 1;
//        this.mContactsLoaderId = n;
//        (i1 + 1);
//        this.mProfilesLoaderId = i1;
//        SearchResultsFragment localSearchResultsFragment = (SearchResultsFragment)paramFragmentManager.findFragmentByTag("people_search_results");
//        if (localSearchResultsFragment == null)
//        {
//            localSearchResultsFragment = new SearchResultsFragment();
//            paramFragmentManager.beginTransaction().add(localSearchResultsFragment, "people_search_results").commit();
//        }
//        while (true)
//        {
//            localSearchResultsFragment.setPeopleSearchResults(this.mResults);
//            this.mFragmentManager = paramFragmentManager;
//            this.mLoaderManager = paramLoaderManager;
//            this.mAccount = paramEsAccount;
//            this.mResults.setMyProfile(this.mAccount.getPersonId());
//            this.mCircleNameResolver = new CircleNameResolver(paramContext, paramLoaderManager, this.mAccount, paramInt);
//            this.mCircleNameResolver.registerObserver(this.mCircleContentObserver);
//            return;
//            PeopleSearchResults localPeopleSearchResults = localSearchResultsFragment.getPeopleSearchResults();
//            if (localPeopleSearchResults == null)
//                continue;
//            this.mResults = localPeopleSearchResults;
//            this.mResultsPreserved = true;
//        }
    }

    private volatile CountDownLatch mFilterLatch;

    private void releaseLatch() {
        CountDownLatch localCountDownLatch = this.mFilterLatch;
        if (localCountDownLatch != null)
            localCountDownLatch.countDown();
    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message paramMessage) {
            switch (paramMessage.what) {
                case 0:
                    PeopleSearchListAdapter.this.showEmptyPeopleSearchResults();
                    break;
                case 1:
                    PeopleSearchListAdapter.this.showProgressItem();
            }
        }
    };

    protected void showEmptyPeopleSearchResults()
    {
      this.mHandler.removeMessages(0);
      Cursor localCursor = mResults.getCursor();
      if (localCursor.getCount() == 0)
        changeCursor(3, localCursor);
    }

    private void showProgressItem() {
//        mPublicProfilesLoading = true;
//        updatePublicProfileSearchStatus();
        if (mListener != null) {
            mListener.onSearchListAdapterStateChange(this);
        }
    }

    public void changeCursor(int paramInt, Cursor paramCursor) {
        Cursor localCursor = mPartitions[paramInt].cursor;
        if (localCursor != paramCursor) {
            if ((localCursor != null) && (!localCursor.isClosed()))
                localCursor.close();
            mPartitions[paramInt].cursor = paramCursor;
            if (paramCursor != null) {
                mPartitions[paramInt].idColumnIndex = paramCursor.getColumnIndex("_id");
            }
            invalidate();
            notifyDataSetChanged();
        }
    }

    // Todo: implement the method
    public void setQueryString(String paramString) {
        if (!TextUtils.equals(this.mQuery, paramString)) {
            this.mResults.setQueryString(paramString);
            this.mHandler.removeMessages(0);
            this.mHandler.removeMessages(1);
            this.mQuery = paramString;
//            this.mActiveLoaderCount = 0;
            if (!TextUtils.isEmpty(paramString)) {
                Bundle localBundle = new Bundle();
                localBundle.putString("query", this.mQuery);
//                if (this.mCircleUsageType != -1) {
//                    this.mActiveLoaderCount = (1 + this.mActiveLoaderCount);
//                    this.mLoaderManager.restartLoader(this.mCirclesLoaderId, localBundle, this);
//                }
//                this.mActiveLoaderCount = (1 + this.mActiveLoaderCount);
//                this.mLoaderManager.restartLoader(this.mPeopleLoaderId, localBundle, this);
//                this.mActiveLoaderCount = (1 + this.mActiveLoaderCount);
//                this.mLoaderManager.restartLoader(this.mContactsLoaderId, localBundle, this);
                if (this.mPublicProfileSearchEnabled) {
//                    this.mPublicProfilesError = false;
//                    this.mPublicProfilesNotFound = false;
//                    this.mPublicProfilesLoading = false;
                    this.mHandler.sendEmptyMessageDelayed(1, 300L);
//                    this.mLoaderManager.destroyLoader(this.mProfilesLoaderId);
//                    this.mLoaderManager.initLoader(this.mProfilesLoaderId, localBundle, this);
//                    updatePublicProfileSearchStatus();
                }
            } else {
//                this.mLoaderManager.destroyLoader(this.mCirclesLoaderId);
//                this.mLoaderManager.destroyLoader(this.mPeopleLoaderId);
//                this.mLoaderManager.destroyLoader(this.mContactsLoaderId);
//                this.mLoaderManager.destroyLoader(this.mProfilesLoaderId);
                clearPartitions();
                releaseLatch();
                if (this.mListener != null)
                    this.mListener.onSearchListAdapterStateChange(this);
            }
        } else {
            releaseLatch();
        }
    }

    public void setListener(SearchListAdapterListener paramSearchListAdapterListener) {
        mListener = paramSearchListAdapterListener;
    }

    public boolean areAllItemsEnabled() {
        int i = 0;
        while (true)
            if (i >= this.mSize) {
                i = 1;
                break;
            } else {
                if (!this.mPartitions[i].hasHeader) {
                    i++;
                    continue;
                }
                i = 0;
            }
        return i > 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            public CharSequence convertResultToString(Object paramObject) {
                Cursor paramCursor = (Cursor) paramObject;
                final String result;
                if ((paramCursor != null) && (!paramCursor.isClosed())) {
                    int i = paramCursor.getColumnIndex(QiupuORM.UsersColumns.CIRCLE_NAME);
                    if (i == -1) {
                        i = paramCursor.getColumnIndex(QiupuORM.UsersColumns.NICKNAME);
                        if (i == -1)
                            result = "";
                        else
                            result = paramCursor.getString(i);
                    } else {
                        result = paramCursor.getString(i);
                    }
                } else {
                    result = "";
                }
                return  result;
            }

            protected Filter.FilterResults performFiltering(CharSequence paramCharSequence) {
                PeopleSearchListAdapter.this.releaseLatch();
                CountDownLatch localCountDownLatch = new CountDownLatch(1);
//                PeopleSearchListAdapter.access$202(PeopleSearchListAdapter.this, localCountDownLatch);
                final CharSequence queryString = paramCharSequence;
                PeopleSearchListAdapter.this.mHandler.post(new Runnable() {
                    public void run() {
                        String str2 = "";
                        if (queryString != null) {
                            if (!PeopleSearchListAdapter.this.mIsMentionsAdapter) {
                                str2 = queryString.toString();
                            } else {
                                int i = queryString.length();
                                if ((i <= 0) || (!MentionTokenizer.isMentionTrigger(queryString.charAt(0))))
                                    str2 = null;
                                else
                                    str2 = queryString.subSequence(1, i).toString();
                            }
                        } else
                            str2 = null;
                        PeopleSearchListAdapter.this.setQueryString(str2);
                    }
                });
                try {
                    localCountDownLatch.await();
//                    PeopleSearchListAdapter.access$202(PeopleSearchListAdapter.this, null);
                    return new Filter.FilterResults();
                } catch (InterruptedException localInterruptedException) {
                }
                return  null;
            }

            protected void publishResults(CharSequence paramCharSequence, Filter.FilterResults paramFilterResults) {
                paramFilterResults.count = PeopleSearchListAdapter.this.getCount();
            }
        };
    }

//    public void setPublicProfileSearchEnabled(boolean paramBoolean)
//    {
//        this.mPublicProfileSearchEnabled = paramBoolean;
//    }
//
//    public void setIncludePlusPages(boolean paramBoolean)
//    {
//        this.mIncludePlusPages = paramBoolean;
//    }

//    public void setMention(String paramString)
//    {
//        this.mActivityId = paramString;
//        this.mIsMentionsAdapter = true;
//    }

    // Todo: implement the method
    public void onStart()
    {
//        this.mCircleNameResolver.initLoader();
//        this.mLoaderManager.initLoader(this.mGaiaIdLoaderId, null, this);
        Bundle localBundle = new Bundle();
        localBundle.putString("query", this.mQuery);
//        if (this.mCircleUsageType != -1)
//            this.mLoaderManager.initLoader(this.mCirclesLoaderId, localBundle, this);
//        this.mLoaderManager.initLoader(this.mPeopleLoaderId, localBundle, this);
//        this.mLoaderManager.initLoader(this.mContactsLoaderId, localBundle, this);
//        if (this.mPublicProfileSearchEnabled)
//            this.mLoaderManager.initLoader(this.mProfilesLoaderId, localBundle, this);
//        updatePublicProfileSearchStatus();
//        AddEmailDialogListener localAddEmailDialogListener = (AddEmailDialogListener)this.mFragmentManager.findFragmentByTag("add_person_dialog_listener");
//        if (localAddEmailDialogListener != null)
//            localAddEmailDialogListener.setAdapter(this);
    }


    public void onStop() {
        mHandler.removeMessages(0);
    }

    public void onCreate(Bundle paramBundle)
    {
        if (paramBundle != null)
        {
            paramBundle.setClassLoader(getClass().getClassLoader());
            mQuery = paramBundle.getString("search_list_adapter.query");
            if ((paramBundle.containsKey("search_list_adapter.results")) && (!mResultsPreserved))
                this.mResults = ((PeopleSearchResults)paramBundle.getParcelable("search_list_adapter.results"));
        }
    }


    public void onSaveInstanceState(Bundle paramBundle)
    {
        paramBundle.putString("search_list_adapter.query", mQuery);
        if (this.mResults.isParcelable())
            paramBundle.putParcelable("search_list_adapter.results", mResults);
    }


    protected int getItemViewType(int paramInt1, int paramInt2)
    {
        return paramInt1;
    }

    // Todo: implement the method
    @Override
    protected View newView(Context paramContext, int paramInt1, Cursor paramCursor, int paramInt2, ViewGroup paramViewGroup) {
        return newView(paramContext, paramCursor, paramViewGroup);
//        Object localObject;
//        switch (paramInt1) {
//            default:
//                localObject = null;
//            case 0:
//            case 1:
//            case 2:
//            case 3:
//            case 4:
//        }
//        while (true) {
//            localObject = new CircleListItemView(paramContext);
//            continue;
//            localObject = PeopleListItemView.createInstance(paramContext);
//            continue;
//            localObject = LayoutInflater.from(paramContext).inflate(2130903134, paramViewGroup, false);
//        }
//        return localObject;
    }

    // Todo: implement the method
    @Override
    protected void bindView(View paramView, int paramInt1, Cursor paramCursor, int paramInt2) {
        bindView(paramView, getContext(), paramCursor);

//        if ((paramCursor == null) || (paramCursor.isClosed())) ;
//        while (true) {
//            return;
//            switch (paramInt1) {
//                default:
//                    break;
//                case 0:
//                    CircleListItemView localCircleListItemView = (CircleListItemView) paramView;
//                    localCircleListItemView.setHighlightedText(this.mQuery);
//                    localCircleListItemView.setCircle(paramCursor.getString(1), paramCursor.getInt(2), paramCursor.getString(3), paramCursor.getInt(4));
//                    break;
//                case 3:
//                    PeopleListItemView localPeopleListItemView3 = (PeopleListItemView) paramView;
//                    localPeopleListItemView3.setHighlightedText(this.mQuery);
//                    localPeopleListItemView3.setCircleNameResolver(this.mCircleNameResolver);
//                    String str1 = paramCursor.getString(1);
//                    localPeopleListItemView3.setPersonId(str1);
//                    String str2 = paramCursor.getString(2);
//                    localPeopleListItemView3.setContactId(paramCursor.getLong(3), str2);
//                    localPeopleListItemView3.setContactName(paramCursor.getString(4));
//                    String str3 = paramCursor.getString(10);
//                    String str4 = paramCursor.getString(5);
//                    int m;
//                    boolean bool1;
//                    boolean bool2;
//                    if (!TextUtils.isEmpty(str4)) {
//                        m = 1;
//                        String str5 = paramCursor.getString(7);
//                        String str6 = null;
//                        if (this.mIncludePhoneNumberContacts)
//                            str6 = paramCursor.getString(8);
//                        localPeopleListItemView3.setPackedCircleIdsEmailAddressPhoneNumberAndSnippet(str4, str5, paramCursor.getString(6), str6, paramCursor.getString(9), str3);
//                        if ((!this.mAddToCirclesActionEnabled) || (m != 0) || (this.mAccount.getPersonId().equals(str1)))
//                            break label378;
//                        bool1 = true;
//                        localPeopleListItemView3.setAddButtonVisible(bool1);
//                        if ((this.mAddToCirclesActionEnabled) && (this.mListener != null))
//                            localPeopleListItemView3.setOnActionButtonClickListener(this);
//                        bool2 = true;
//                        if (str2 != null) {
//                            if (paramInt2 != 0)
//                                break label384;
//                            bool2 = true;
//                        }
//                    }
//                    while (true) {
//                        localPeopleListItemView3.setFirstRow(bool2);
//                        if (paramInt2 == -1 + paramCursor.getCount())
//                            continueLoadingPublicProfiles();
//                        localPeopleListItemView3.updateContentDescription();
//                        break;
//                        m = 0;
//                        break label207;
//                        bool1 = false;
//                        break label296;
//                        if (!paramCursor.moveToPrevious())
//                            continue;
//                        if (TextUtils.equals(str2, paramCursor.getString(2)))
//                            bool2 = false;
//                        paramCursor.moveToNext();
//                    }
//                case 1:
//                    PeopleListItemView localPeopleListItemView2 = (PeopleListItemView) paramView;
//                    localPeopleListItemView2.setWellFormedEmail(this.mQuery);
//                    localPeopleListItemView2.setAddButtonVisible(this.mAddToCirclesActionEnabled);
//                    if ((this.mAddToCirclesActionEnabled) && (this.mListener != null))
//                        localPeopleListItemView2.setOnActionButtonClickListener(this);
//                    localPeopleListItemView2.updateContentDescription();
//                    break;
//                case 2:
//                    label207:
//                    label378:
//                    PeopleListItemView localPeopleListItemView1 = (PeopleListItemView) paramView;
//                    label296:
//                    localPeopleListItemView1.setWellFormedSms(this.mQuery);
//                    label384:
//                    localPeopleListItemView1.setAddButtonVisible(this.mAddToCirclesActionEnabled);
//                    if ((this.mAddToCirclesActionEnabled) && (this.mListener != null))
//                        localPeopleListItemView1.setOnActionButtonClickListener(this);
//                    localPeopleListItemView1.updateContentDescription();
//                case 4:
//            }
//        }
//        int i = 8;
//        int j = 8;
//        int k = 8;
//        switch (paramCursor.getInt(0)) {
//            default:
//            case 1:
//            case 2:
//            case 3:
//        }
//        while (true) {
//            paramView.findViewById(2131558638).setVisibility(i);
//            paramView.findViewById(2131558671).setVisibility(j);
//            paramView.findViewById(2131558639).setVisibility(k);
//            break;
//            i = 0;
//            continue;
//            j = 0;
//            continue;
//            k = 0;
//        }
    }
}
