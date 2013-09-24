
package com.borqs.sync.client.vdata.card;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import com.borqs.common.transport.AccountClient;
import com.borqs.common.account.AccountAdapter;
import com.borqs.common.account.ProfileCircle;
import com.borqs.common.transport.SimpleHttpClient;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.sync.client.config.ProfileConfig;
import com.borqs.sync.client.vdata.card.ContactStruct.EmailData;
import com.borqs.sync.client.vdata.card.ContactStruct.ImData;
import com.borqs.sync.client.vdata.card.ContactStruct.OrganizationData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhoneData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhotoData;
import com.borqs.sync.client.vdata.card.ContactStruct.PostalData;
import com.borqs.sync.client.vdata.card.ContactStruct.WebsiteData;
import com.borqs.syncml.ds.imp.common.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactOperator {

    /*
     * copy from @com.android.providers.contacts.SQLiteContentProvider Google
     * design Maximum number of operations allowed in a batch between yield
     * points.
     */

    public static boolean update(long people, ContactStruct contact, ContentResolver resolver) {
        return ContactUpdate.update(people, contact, resolver);
    }

    public static boolean updateWithNotSyncCaller(long people, ContactStruct contact,
            ContentResolver resolver) {
        return ContactUpdate.updateWithNotSyncCaller(people, contact, resolver);
    }

    public static boolean delete(long people, ContentResolver resolver) {
        return delete(people, resolver, true);
    }

    public static boolean deleteWithNotSyncCaller(long people, ContentResolver resolver) {
        return delete(people, resolver, false);
    }

    public static boolean batchDeleteWithNotSyncCaller(long ids[], int start, int length,
            ContentResolver resolver) {
        return batchDelete(ids, start, length, resolver, false);
    }

    public static boolean batchDelete(long ids[], int start, int length, ContentResolver resolver) {
        return batchDelete(ids, start, length, resolver, true);
    }

    private static boolean delete(long people, ContentResolver resolver, boolean callIsSyncAdapter) {
        ContactProviderOperation co = new ContactProviderOperation(resolver, callIsSyncAdapter);
        co.delete(people);
        co.execute();
        return true;
    }

    private static boolean batchDelete(long ids[], int start, int length, ContentResolver resolver,
            boolean callIsSyncAdapter) {
        ContactProviderOperation co = new ContactProviderOperation(resolver, callIsSyncAdapter);
        for (int i = 0; i < length && start + i < ids.length; i++) {
            co.delete(ids[start + i]);
            if (co.size() == ContactProviderOperation.MAX_OPERATIONS_PER_YIELD_POINT - 1) {
                co.execute();
                // reset the operation
                co.clear();
            }
        }
        co.execute();
        return true;
    }

    public static long addWithNotSyncCaller(ContactStruct contact, ContentResolver resolver) {
        return add(contact, resolver, false);
    }

    public static long add(ContactStruct contact, ContentResolver resolver) {
        return add(contact, resolver, true);
    }

    public static long[] batchAddWithNotSyncCaller(ContactStruct[] contacts,
            ContentResolver resolver) {
        return batchAdd(contacts, resolver, false);
    }

    public static long[] batchAdd(ContactStruct[] contacts, ContentResolver resolver) {
        return batchAdd(contacts, resolver, true);
    }

    private static long add(ContactStruct contact, ContentResolver resolver,
            boolean callIsSyncAdapter) {
        long[] ids = batchAdd(new ContactStruct[] {
            contact
        }, resolver, callIsSyncAdapter);
        return ids[0];
    }

    private static long[] batchAdd(ContactStruct[] contacts, ContentResolver resolver,
            boolean callIsSyncAdapter) {
        ContactProviderOperation ops = new ContactProviderOperation(resolver, callIsSyncAdapter);
        List<Long> idsList = new ArrayList<Long>();

        for (ContactStruct contact : contacts) {
            if (contact == null) {
                continue;
            }

            ContactTransferStruct cts = new ContactTransferStruct(contact);
            int operationSize = ops.size() + cts.getContactListPropertySize() + 4/**raw_contact,birthday
            ,borqsname,name:TODO bad code**/;
            /*
             * if the previous loop size + current loop size is more than
             * CONTACT_FIXED_OPERATION_SIZE,we will commit previous. and reset
             * the ContactProviderOperation for new operation list count else
             * ,continue the loop After the loop finished,we will commit the
             * remaining ContentProviderOperations
             */
            if (operationSize >= ContactProviderOperation.MAX_OPERATIONS_PER_YIELD_POINT) {
                applyBatch(ops, idsList);
                // reset the operation
                ops = new ContactProviderOperation(resolver, callIsSyncAdapter);
            }
            ops.newContact(contact);
            // birthday
            ops.addBirthday(null, contact.getBirthday());

            // borqs name
            String borqsName = contact.getBorqsName();
            if (!TextUtils.isEmpty(contact.getBorqsUid()) && !TextUtils.isEmpty(borqsName)) {
                ops.addBorqsName(null, ContactProviderOperation.TEXT_BORQS_NAME_SUMMARY, borqsName);
            }

            // name
            ops.addName(null, contact.getPrefix(), contact.getFirstName(), contact.getLastName(),
                    contact.getMiddleName(), contact.getSuffix(), contact.getDisplayName(),
                    contact.getPhoneticFirstName(), contact.getPhoneticLastName(),
                    contact.getPhoneticMiddleName(), null);

            // photo
            if (contact.getPhotoList() != null && contact.getPhotoList().size() > 0) {
                for (ContactStruct.PhotoData photo : contact.getPhotoList()) {
                    ops.addPhoto(null, photo.photoBytes);
                }
            }

            if (cts.nicknameList != null) {
                for (String nickName : cts.nicknameList) {
                    ops.addNickname(null, nickName);
                }
            }
            // website
            if (cts.websiteList != null) {
                for (WebsiteData webSite : cts.websiteList) {
                    ops.addWebpage(null, webSite.type, webSite.data, webSite.label,
                            webSite.isPrimary);
                }
            }

            // notes
            if (cts.notes.size() > 0) {
                ops.addNote(null, cts.notes.get(0));
            }
            // phone
            if (cts.phoneList != null) {
                for (ContactStruct.PhoneData phone : cts.phoneList) {
                    ops.addPhone(null, phone.type, phone.data, phone.label, phone.isPrimary);
                }
            }
            // org
            if (cts.orgList != null) {
                for (ContactStruct.OrganizationData org : cts.orgList) {
                    ops.addOrganization(null, org.type, org.companyName, org.positionName,
                            org.department, null, null, org.label, org.isPrimary);
                }
            }

            // email
            if (cts.emailList != null) {
                for (ContactStruct.EmailData email : cts.emailList) {
                    ops.addEmail(null, email.type, email.data, email.label, email.isPrimary);
                }
            }
            // adr
            if (cts.addressList != null) {
                for (ContactStruct.PostalData adr : cts.addressList) {
                    ops.addPostal(null, adr.type, adr.pobox, adr.extendedAddress, adr.street,
                            adr.localty, adr.region, adr.postalCode, adr.country, adr.label,
                            adr.isPrimary);
                }
            }

            // imcard
            if (cts.imList != null) {
                for (ContactStruct.ImData im : cts.imList) {
                    ops.addIM(null, im.type, im.data, im.label, im.customProtocol, im.isPrimary);
                }
            }

            // group member
            if (cts.groupList != null) {
                String accountType = Constant.BORQS_ACCOUNT_TYPE;
                String accountName = ProfileConfig.getProfileName(ApplicationGlobals.getContext());
                for (String group : cts.groupList) {
                    ops.addGroupId(null, group,
                            getGroupIdByName(accountType, accountName, group, resolver));
                }
            }
        }

        applyBatch(ops, idsList);
        return VCardUtil.toLongArray(idsList);
    }

    /**
     * class for transfer the ContactStruct. we can get the List property of
     * ContactStruct.
     * 
     * @author b211
     */
    static class ContactTransferStruct {
        List<PhotoData> photoList = null;

        List<String> nicknameList = null;

        List<WebsiteData> websiteList = null;

        List<String> notes = null;

        List<PhoneData> phoneList = null;

        List<OrganizationData> orgList = null;

        List<EmailData> emailList = null;

        List<PostalData> addressList = null;

        List<ImData> imList = null;

        List<String> groupList = null;

        public ContactTransferStruct(ContactStruct contact) {
            photoList = contact.getPhotoList();
            nicknameList = contact.getNickNameList();
            websiteList = contact.getWebsiteList();
            notes = contact.getNotes();
            phoneList = contact.getPhoneList();
            orgList = contact.getOrganizationList();
            emailList = contact.getEmailList();
            addressList = contact.getPostalList();
            imList = contact.getImList();
            groupList = contact.getGroupList();
        }

        public int getContactListPropertySize() {
            int photoSize = VCardUtil.getListSize(photoList);
            int nickNameSize = VCardUtil.getListSize(nicknameList);
            int websiteSize = VCardUtil.getListSize(websiteList);
            int noteSize = VCardUtil.getListSize(notes);
            int phoneSize = VCardUtil.getListSize(phoneList);
            int orgSize = VCardUtil.getListSize(orgList);
            int emailSize = VCardUtil.getListSize(emailList);
            int addressSize = VCardUtil.getListSize(addressList);
            int imSize = VCardUtil.getListSize(imList);
            int groupSize = VCardUtil.getListSize(groupList);
            // compute the content provider operation size that we will commit.
            return photoSize + nickNameSize + websiteSize + noteSize + phoneSize + orgSize
                    + emailSize + addressSize + imSize + groupSize;
        }
    }

    /**
     * insert the contact data into db
     * 
     * @param ops ContentProviderOperation list
     * @param idList the added id list
     */
    private static void applyBatch(ContactProviderOperation ops, List<Long> idList) {
        ops.execute();
        if (ops.mResults != null) {
            for (int i = 0; i < ops.mContactIndexCount; i++) {
                int index = ops.mContactIndexArray[i];
                Uri u = ops.mResults[index].uri;
                if (u != null) {
                    String idString = u.getLastPathSegment();
                    idList.add(Long.parseLong(idString));
                }
            }
        }
    }

    public static long add(ContactStruct contact, long id, ContentResolver resolver) {
        return add(contact, resolver);
    }

    public static class ContactData {
        private String mMimetype;

        private boolean mIsPrimary;

        private String[] mData = new String[15];

        public ContactData(String mimetype, boolean isPrimary, String[] data) {
            this.mMimetype = mimetype;
            this.mIsPrimary = isPrimary;
            this.mData = data;
        }

        public String getMimeType() {
            return mMimetype;
        }

        public boolean isPrimary() {
            return mIsPrimary;
        }

        public String[] getData() {
            return mData;
        }
    }

    public static ContactStruct load(long people, ContentResolver resolver) {
        return load(ContentUris.withAppendedId(RawContacts.CONTENT_URI, people), resolver);
    }
    
    public static ContactStruct loadForSyncLog(long people, ContentResolver resolver) {
        ContactStruct cs = new ContactStruct();
        Cursor contactC = resolver.query(ContentUris.withAppendedId(RawContacts.CONTENT_URI, people),
        		new String[]{Contacts.DISPLAY_NAME,RawContacts._ID,RawContacts.CONTACT_ID}
        , null, null, null);
        if(contactC != null){
            try{
                if(contactC.moveToNext()){
                    cs.setDisplayName(contactC.getString(0));
                    cs.setRawContactId(contactC.getLong(1));
                    cs.setContactId(contactC.getLong(2));
                }
            }finally{
                contactC.close();
            }
        }
        return cs;
    }

    public static ContactStruct load(Uri uri, ContentResolver resolver) {
        Cursor contactC = resolver.query(uri, null, null, null, null);
        ContactStruct contactStruct = null;
        if (contactC != null) {
            if (contactC.moveToFirst()) {
                contactStruct = load(contactC, resolver);
            }
            contactC.close();
        }
        return contactStruct;
    }

    // check if the contact exists
    public static boolean existContact(ContentResolver resolver, long people) {
        Uri rawContactUri = Uri.withAppendedPath(RawContacts.CONTENT_URI, String.valueOf(people));
        Cursor c = resolver.query(rawContactUri, null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToNext()) {
                    return true;
                }
            } finally {
                c.close();
            }
        }
        return false;
    }

    public static ContactStruct load(Cursor contactC, ContentResolver resolver) {

        ContactStruct contactStruct = new ContactStruct();
        long id = contactC.getLong(contactC.getColumnIndexOrThrow(RawContacts._ID));

        Uri rawContactUri = Uri.withAppendedPath(RawContacts.CONTENT_URI, Long.toString(id));

        // Get people info.
        // contactStruct.setBlock((contactC.getInt(contactC
        // .getColumnIndexOrThrow("block")) != 0));
        // get ringtone uri from people,
        // change uri to file, then set contactStruct

        int starred = contactC.getInt(contactC.getColumnIndexOrThrow(RawContacts.STARRED));
        contactStruct.setFavorite(starred == 1);

        // String ringtoneFile = RingtoneVCard.changeUriToFile(
        // contactC.getString(contactC.getColumnIndexOrThrow(
        // RawContacts.CUSTOM_RINGTONE)),
        // resolver);
        // contactStruct.setRingtoneFile(ringtoneFile);

        // account type
        String accountType = contactC.getString(contactC
                .getColumnIndexOrThrow(RawContacts.ACCOUNT_TYPE));
        contactStruct.setAccountType(accountType);

        // account name
        String accountName = contactC.getString(contactC
                .getColumnIndexOrThrow(RawContacts.ACCOUNT_NAME));
        contactStruct.setAccountName(accountName);

        // BORQSID
        String borqsId = contactC.getString(contactC.getColumnIndexOrThrow(RawContacts.SYNC4));
        contactStruct.setBorqsUid(borqsId);

        // sourceID
        String srcID = contactC.getString(contactC.getColumnIndexOrThrow(RawContacts.SOURCE_ID));
        contactStruct.setSourceID(srcID);

        // display_name
        contactStruct.setDisplayName(contactC.getString(contactC
                .getColumnIndexOrThrow(Contacts.DISPLAY_NAME_PRIMARY)));

        Cursor dataC = resolver.query(
                Uri.withAppendedPath(rawContactUri, RawContacts.Data.CONTENT_DIRECTORY), null,
                null, null, null);
        String imieType = "";
        List<String> nameList = null;
        if (dataC != null) {
            while (dataC.moveToNext()) {
                imieType = dataC.getString(dataC.getColumnIndexOrThrow(Data.MIMETYPE));
                // name
                if (CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(imieType)) {
                    if (nameList == null) {
                        nameList = new ArrayList<String>();
                    }
                    contactStruct.setFirstName(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredName.GIVEN_NAME)));
                    contactStruct.setMiddleName(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredName.MIDDLE_NAME)));
                    contactStruct.setLastName(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredName.FAMILY_NAME)));
                    contactStruct.setPrefix(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredName.PREFIX)));
                    contactStruct.setSuffix(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredName.SUFFIX)));
                    contactStruct.setPhoneticLastName(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredName.PHONETIC_FAMILY_NAME)));
                    contactStruct.setPhoneticMiddleName(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredName.PHONETIC_MIDDLE_NAME)));
                    contactStruct.setPhoneticFirstName(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredName.PHONETIC_GIVEN_NAME)));
                    // contactStruct.setDisplayName(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.DISPLAY_NAME)));
                    // nickname
                } else if (Nickname.CONTENT_ITEM_TYPE.equals(imieType)) {
                    contactStruct.addNickNameList(dataC.getString(dataC
                            .getColumnIndexOrThrow(Nickname.NAME)));
                    // phone
                } else if (Phone.CONTENT_ITEM_TYPE.equals(imieType)) {
                    String strType = dataC.getString(dataC.getColumnIndexOrThrow(Phone.TYPE));
                    int type = strType != null ? Integer.parseInt(strType) : Phone.TYPE_OTHER;
                    contactStruct.addPhone(type, dataC.getString(dataC
                            .getColumnIndexOrThrow(Phone.NUMBER)), dataC.getString(dataC
                            .getColumnIndexOrThrow(Phone.LABEL)), !"0".equals(dataC.getString(dataC
                            .getColumnIndexOrThrow(Phone.IS_SUPER_PRIMARY))));
                    // email
                } else if (Email.CONTENT_ITEM_TYPE.equals(imieType)) {
                    String strType = dataC.getString(dataC.getColumnIndexOrThrow(Email.TYPE));
                    int type = strType != null ? Integer.parseInt(strType) : Email.TYPE_OTHER;
                    contactStruct.addEmail(type, dataC.getString(dataC
                            .getColumnIndexOrThrow(Email.DATA)), dataC.getString(dataC
                            .getColumnIndexOrThrow(Email.LABEL)), !"0".equals(dataC.getString(dataC
                            .getColumnIndexOrThrow(Email.IS_SUPER_PRIMARY))));
                    // adr
                } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(imieType)) {
                    if (dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.TYPE)) == null) {
                        continue;
                    }

                    List<String> adrValue = new ArrayList<String>();
                    adrValue.add(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredPostal.POBOX)));
                    adrValue.add(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredPostal.NEIGHBORHOOD)));
                    adrValue.add(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredPostal.STREET)));
                    adrValue.add(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.CITY)));
                    adrValue.add(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredPostal.REGION)));
                    adrValue.add(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredPostal.POSTCODE)));
                    adrValue.add(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredPostal.COUNTRY)));

                    contactStruct.addPostal(Integer.parseInt(dataC.getString(dataC
                            .getColumnIndexOrThrow(StructuredPostal.TYPE))), adrValue, dataC
                            .getString(dataC.getColumnIndexOrThrow(StructuredPostal.LABEL)), !"0"
                            .equals(dataC.getString(dataC
                                    .getColumnIndexOrThrow(StructuredPostal.IS_SUPER_PRIMARY))));
                    // IM
                } else if (Im.CONTENT_ITEM_TYPE.equals(imieType)) {
                    if (dataC.getString(dataC.getColumnIndexOrThrow(Im.PROTOCOL)) == null) {
                        continue;
                    }
                    contactStruct.addIm(Integer.parseInt(dataC.getString(dataC
                            .getColumnIndexOrThrow(Im.PROTOCOL))), dataC.getString(dataC
                            .getColumnIndexOrThrow(Im.DATA)), dataC.getString(dataC
                            .getColumnIndexOrThrow(Im.LABEL)), dataC.getString(dataC
                            .getColumnIndexOrThrow(Im.CUSTOM_PROTOCOL)), !"0".equals(dataC
                            .getString(dataC.getColumnIndexOrThrow(Im.IS_SUPER_PRIMARY))));
                    // org
                } else if (Organization.CONTENT_ITEM_TYPE.equals(imieType)) {
                    String strType = dataC
                            .getString(dataC.getColumnIndexOrThrow(Organization.TYPE));
                    int type = strType != null ? Integer.parseInt(strType)
                            : Organization.TYPE_OTHER;
                    contactStruct
                            .addOrganization(type, dataC.getString(dataC
                                    .getColumnIndexOrThrow(Organization.COMPANY)), dataC
                                    .getString(dataC.getColumnIndexOrThrow(Organization.TITLE)),
                                    dataC.getString(dataC
                                            .getColumnIndexOrThrow(Organization.DEPARTMENT)), dataC
                                            .getString(dataC
                                                    .getColumnIndexOrThrow(Organization.LABEL)),
                                    !"0".equals(dataC.getString(dataC
                                            .getColumnIndexOrThrow(Organization.IS_SUPER_PRIMARY))));
                    // notes
                } else if (Note.CONTENT_ITEM_TYPE.equals(imieType)) {
                    contactStruct.addNote(dataC.getString(dataC.getColumnIndexOrThrow(Note.NOTE)));
                } else if (Website.CONTENT_ITEM_TYPE.equals(imieType)) {
                    // Website
                    String strType = dataC.getString(dataC.getColumnIndexOrThrow(Website.TYPE));
                    int type = strType != null ? Integer.parseInt(strType) : Website.TYPE_OTHER;
                    contactStruct.addWebsite(type, dataC.getString(dataC
                            .getColumnIndexOrThrow(Website.URL)), dataC.getString(dataC
                            .getColumnIndexOrThrow(Website.LABEL)), !"0".equals(dataC
                            .getString(dataC.getColumnIndexOrThrow(Website.IS_SUPER_PRIMARY))));
                    // photo
                } else if (Photo.CONTENT_ITEM_TYPE.equals(imieType)) {
                    contactStruct.addPhotoBytes(null,
                            dataC.getBlob(dataC.getColumnIndexOrThrow(Photo.PHOTO)));
                    // birthday
                } else if (CommonDataKinds.Event.CONTENT_ITEM_TYPE.equals(imieType)) {
                    String type = dataC.getString(dataC
                            .getColumnIndexOrThrow(CommonDataKinds.Event.TYPE));
                    if (!TextUtils.isEmpty(type)
                            && CommonDataKinds.Event.TYPE_BIRTHDAY == Integer.parseInt(type)) {
                        contactStruct.setBirthday(dataC.getString(dataC
                                .getColumnIndexOrThrow(CommonDataKinds.Event.START_DATE)));
                    }
                    // Group member
                } else if (CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE.equals(imieType)) {
                    int groupId = dataC.getInt(dataC
                            .getColumnIndexOrThrow(CommonDataKinds.GroupMembership.GROUP_ROW_ID));
                    contactStruct.addGroup(getGroupNameById(groupId, resolver));
                    // borqs name
                } else if (ContactProviderOperation.MIME_TYPE_BORQS_NAME.equals(imieType)) {
                    String borqsName = dataC.getString(dataC
                            .getColumnIndexOrThrow(ContactProviderOperation.COLUMN_BORQS_NAME));
                    contactStruct.setBorqsName(borqsName);
                }
            }
            dataC.close();
        }
        // contactStruct.consolidateFields();
        return contactStruct;
    }

    protected static String getGroupNameById(int groupId, ContentResolver resolver) {
        String group = "";

        Uri groupUri = Uri.withAppendedPath(Groups.CONTENT_URI, Long.toString(groupId));
        Cursor dataC = resolver.query(Groups.CONTENT_URI, null, Groups._ID + " = \"" + groupId
                + "\" ", null, null);
        if (dataC != null) {
            if (dataC.moveToFirst()) {
                group = dataC.getString(dataC.getColumnIndexOrThrow(Groups.TITLE));
            }
            dataC.close();
        }
        return group;
    }

    protected static long getGroupIdByName(String accType, String accName, String group,
            ContentResolver resolver) {
        long groupId = -1;
        StringBuilder where = new StringBuilder(Groups.TITLE + " = ? AND ").append(
                Groups.ACCOUNT_TYPE + " = ? AND ").append(Groups.ACCOUNT_NAME + " = ? ");

        Cursor dataC = resolver.query(Groups.CONTENT_URI, null, where.toString(), new String[] {
                group, accType, accName
        }, null);
        if (dataC != null) {
            if (dataC.moveToFirst()) {
                groupId = dataC.getInt(dataC.getColumnIndexOrThrow(Groups._ID));
            } else {
                groupId = insertGroup(accType, accName, group, resolver);
            }
            dataC.close();
        } else {
            groupId = insertGroup(accType, accName, group, resolver);
        }
        return groupId;
    }

    protected static long insertGroup(String accType, String accName, String group,
            ContentResolver resolver) {
        long groupId = -1;
        ContentValues values = new ContentValues();
        values.put(Groups.ACCOUNT_NAME, accName);
        values.put(Groups.ACCOUNT_TYPE, accType);
        values.put(Groups.TITLE, group);
        values.put(Groups.NOTES, group);
        values.put(Groups.GROUP_VISIBLE, 1);
        // would be changed later
        int sourceId = matchSystemID(group);
        if (sourceId != -1) {
            values.put("sourceid", String.valueOf(sourceId));
        }
        Uri uri = resolver.insert(Groups.CONTENT_URI, values);
        groupId = ContentUris.parseId(uri);
        return groupId;
    }

    // walk-around solution for Circle ID
    private static int matchSystemID(String groupName) {
        final int CIRCLE_BLOCKED_ID = 4;
        final int CIRCLE_ADDRESS_BOOK_ID = 5;
        final int CIRCLE_DEFAULT_ID = 6;
        final int CIRCLE_FAMILY_ID = 9;
        final int CIRCLE_CLOSED_FRIENDS_ID = 10;
        final int CIRCLE_ACQUAINTANCE_ID = 11;

        try {
            if (CIRCLE_ID_MAPPING == null) {
                CIRCLE_ID_MAPPING = new HashMap<String, Integer>();
                String ticket = AccountAdapter.getUserData(ApplicationGlobals.getContext(),
                        AccountAdapter.BORQS_ACCOUNT_OPTIONS_KEY_SESSION);
                List<ProfileCircle> circles = new AccountClient(ApplicationGlobals.getContext(),
                        SimpleHttpClient.get()).retrieveCircleList(ticket);
                for (ProfileCircle c : circles) {
                    CIRCLE_ID_MAPPING.put(c.name(), c.id());
                }
            }

            
        } catch (Throwable e) {
            // ignore any error
        }
        
        //default group ID 
        if ("熟人".equals(groupName)) {
            return CIRCLE_ACQUAINTANCE_ID;
        } else if ("名片交换".equals(groupName)) {
            return CIRCLE_ADDRESS_BOOK_ID;
        } else if ("黑名单".equals(groupName)) {
            return CIRCLE_BLOCKED_ID;
        } else if ("朋友".equals(groupName)) {
            return CIRCLE_CLOSED_FRIENDS_ID;
        } else if ("关注对象".equals(groupName)) {
            return CIRCLE_DEFAULT_ID;
        } else if ("家庭".equals(groupName)) {
            return CIRCLE_FAMILY_ID;
        } else {
            if (CIRCLE_ID_MAPPING.containsKey(groupName)) {
                return CIRCLE_ID_MAPPING.get(groupName);
            }
        }
        return -1;
    }

    private static HashMap<String, Integer> CIRCLE_ID_MAPPING = null;
}
