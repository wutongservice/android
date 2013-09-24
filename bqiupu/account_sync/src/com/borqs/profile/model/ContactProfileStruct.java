package com.borqs.profile.model;

import com.borqs.sync.client.vdata.card.ContactStruct;

/**
 * 
 */
public class ContactProfileStruct extends ContactStruct {  

    public ContactProfileStruct(){
	    setAccount("Phone", "com.borqs.account");
	}
	
    
    public String getFamilyName() {
        return getLastName();
    }

    public void setFamilyName(String familyName){
        setLastName(familyName);
    }
    
    public String getGivenName() {
        return getFirstName();
    }

    public void setGivenName(String givenName){
    	setFirstName(givenName);
    }
    
    /**
     * @hide
     */
    public String getPhoneticFamilyName() {
        return getPhoneticLastName();
    }
    
    public void setPhoneticFamilyName(String phoneticFamilyName){
        setPhoneticLastName(phoneticFamilyName);
    }

    /**
     * @hide
     */
    public String getPhoneticGivenName() {
        return getPhoneticFirstName();
    }
    
    public void setPhoneticGivenName(String phoneticGivenName){
        setPhoneticFirstName(phoneticGivenName);
    }

    public final String getNickName() {
        String name = null;
        if (getNickNameList().size() > 0){
            name = getNickNameList().get(0); 
        }
        return name;
    }      
}
