package com.borqs.common.listener;

import com.borqs.qiupu.fragment.BpcShakeExchangeCardFragment;
import com.borqs.qiupu.fragment.FindContactsFragment;
import com.borqs.qiupu.fragment.NearByListFragment;
import com.borqs.qiupu.fragment.SuggestionListFragment;
import com.borqs.qiupu.ui.bpc.UsersArrayListActivity.UsersArrayListFragment;


public interface ShakeActionListener {

    public void showCustomFragmentToast(String msg);
    public void activateLocation();
    public void deactivateLocation();
    public void getShakeFragmentCallBack(BpcShakeExchangeCardFragment shakeFragment);
    public void getNearByFragmentCallBack(NearByListFragment nearbyFragment);
    public void getContactFragmentCallBack(FindContactsFragment contactFragment);
    public void getFansFragmentCallBack(UsersArrayListFragment fansFragment);
    public void getSuggestFragmentCallBack(SuggestionListFragment suggestFragment);
    public boolean checkLocationApi();
}
