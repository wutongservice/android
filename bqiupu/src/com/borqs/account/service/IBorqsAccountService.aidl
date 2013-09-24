package com.borqs.account.service;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.account.service.BorqsAccount;

interface IBorqsAccountService
{
   List<ContactSimpleInfo> getContactsSimepleInfos();
      
   BorqsAccount getAccount(); 
   
   void clearAccount();
   
   void login(in BorqsAccount loginAccount);
   
   void logout(String requestActivity);
   
   void updateValue(String value);
   
   void borqsLogout(boolean isNeedStartLogin ,String requestActivity);
}

  