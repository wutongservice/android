package com.borqs.profile.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;

import java.util.ArrayList;

public class ContactProfileProviderOperation extends ArrayList<ContentProviderOperation> {
	
	static private final String TAG = "ContactOperations";
    private static final long serialVersionUID = 1L;
    private int mCount = 0;
    private int mContactBackValue = mCount;
	// Make an array big enough for the PIM window (max items we can get)
	//TODO:..SET THE SIZE
    public int[] mContactIndexArray = new int[500];
    public int mContactIndexCount = 0;
    public ContentProviderResult[] mResults = null;

    private ContentResolver mResolver;
    
    public ContactProfileProviderOperation(ContentResolver resolver){
    	mResolver = resolver;
    }
    
    @Override
    public boolean add(ContentProviderOperation op) {
        super.add(op);
        mCount++;
        return true;
    }

    private Uri addCallerIsSyncAdapterParameter(Uri uri) {
        //return uri;
       return uri.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, String.valueOf(true))
                .build();
    }
    
    public void newContact(ContactProfileStruct contact) {
		Builder builder = ContentProviderOperation
				.newInsert(addCallerIsSyncAdapterParameter(Profile.CONTENT_RAW_CONTACTS_URI));
		ContentValues values = new ContentValues();
		if(contact == null){
			contact = new ContactProfileStruct();
		}

		values.put(RawContacts.ACCOUNT_NAME, contact.getAccountName());
		values.put(RawContacts.ACCOUNT_TYPE, contact.getAccountType());

		values.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DEFAULT);
		builder.withValues(values);
		mContactBackValue = mCount;
		mContactIndexArray[mContactIndexCount++] = mCount;
		add(builder.build());
	}

	public void newUpdate(long people, ContentValues values) {
		add(ContentProviderOperation
				.newUpdate(addCallerIsSyncAdapterParameter(Profile.CONTENT_RAW_CONTACTS_URI)).withSelection("_id=?", new String[] {String.valueOf(people)}).withValues(values).build());
	}
    
	public void delete(long id) {
		add(ContentProviderOperation.newDelete(
				ContentUris.withAppendedId(addCallerIsSyncAdapterParameter(Profile.CONTENT_RAW_CONTACTS_URI), id))
				.build());
	}
    
    public void newData(ContentValues values){
    	Builder builder = ContentProviderOperation.
                newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI));
    	builder.withValues(values);
    	add(builder.build());
    }
    
	public void deleteData(long id) {
		add(ContentProviderOperation.newDelete(
				ContentUris.withAppendedId(Data.CONTENT_URI, id)).build());
	}

    public void execute() {
        try {
            if (!isEmpty()) {
                mResults = mResolver.applyBatch(
                        ContactsContract.AUTHORITY, this);
            }
        } catch (RemoteException e) {
            // There is nothing sensible to be done here
            Log.e(TAG, "problem inserting contact ", e);
        } catch (OperationApplicationException e) {
            // There is nothing sensible to be done here
            Log.e(TAG, "problem inserting contact", e);
        }
    }
    
	/**
	 * Given the list of NamedContentValues for an entity, a mime type, and a
	 * subtype, tries to find a match, returning it
	 * 
	 * @param list
	 *            the list of NCV's from the contact entity
	 * @param contentItemType
	 *            the mime type we're looking for
	 * @param type
	 *            the subtype (e.g. HOME, WORK, etc.)
	 * @return the matching NCV or null if not found
	 */
	private NamedContentValues findTypedData(
			ArrayList<NamedContentValues> list, String contentItemType,
			int type, String stringType) {
		NamedContentValues result = null;

		// Loop through the ncv's, looking for an existing row
		for (NamedContentValues namedContentValues : list) {
			Uri uri = namedContentValues.uri;
			ContentValues cv = namedContentValues.values;
			if (Data.CONTENT_URI.equals(uri)) {
				String mimeType = cv.getAsString(Data.MIMETYPE);
				if (mimeType.equals(contentItemType)) {
					if (stringType != null) {
						if (cv.getAsString(GroupMembership.GROUP_ROW_ID)
								.equals(stringType)) {
							result = namedContentValues;
						}
						// Note Email.TYPE could be ANY type column; they are
						// all defined in
						// the private CommonColumns class in ContactsContract
					} else if (type < 0 || cv.getAsInteger(Email.TYPE) == type) {
						result = namedContentValues;
					}
				}
			}
		}

		// If we've found an existing data row, we'll delete it. Any rows left
		// at the
		// end should be deleted...
		if (result != null) {
			list.remove(result);
		}

		// Return the row found (or null)
		return result;
	}

    /**
     * Generate the uri for the data row associated with this NamedContentValues object
     * @param ncv the NamedContentValues object
     * @return a uri that can be used to refer to this row
     */
    public Uri dataUriFromNamedContentValues(NamedContentValues ncv) {
        long id = ncv.values.getAsLong(Profile._ID);
        Uri dataUri = ContentUris.withAppendedId(ncv.uri, id);
        return dataUri;
    }
 
	/**
	 * Create a wrapper for a builder (insert or update) that also includes the
	 * NCV for an existing row of this type. If the SmartBuilder's cv field is
	 * not null, then it represents the current (old) values of this field. The
	 * caller can then check whether the field is now different and needs to be
	 * updated; if it's not different, the caller will simply return and not
	 * generate a new CPO. Otherwise, the builder should have its content values
	 * set, and the built CPO should be added to the ContactOperations list.
	 * 
	 * @param entity
	 *            the contact entity (or null if this is a new contact)
	 * @param mimeType
	 *            the mime type of this row
	 * @param type
	 *            the subtype of this row
	 * @param stringType
	 *            for groups, the name of the group (type will be ignored), or
	 *            null
	 * @return the created SmartBuilder
	 */
	public RowBuilder createBuilder(Entity entity, String mimeType, int type,
			String stringType) {
		RowBuilder builder = null;

		if (entity != null) {
			NamedContentValues ncv = findTypedData(entity.getSubValues(),
					mimeType, type, stringType);
			if (ncv != null) {
				builder = new RowBuilder(
						ContentProviderOperation
								.newUpdate(dataUriFromNamedContentValues(ncv)),
						ncv);
			}
		}

		if (builder == null) {
			builder = newRowBuilder(entity, mimeType);
		}

		// Return the appropriate builder (insert or update)
		// Caller will fill in the appropriate values; 4 MIMETYPE is already set
		return builder;
	}

	private RowBuilder typedRowBuilder(Entity entity, String mimeType, int type) {
		return createBuilder(entity, mimeType, type, null);
	}

	private RowBuilder untypedRowBuilder(Entity entity, String mimeType) {
		return createBuilder(entity, mimeType, -1, null);
	}

	private RowBuilder newRowBuilder(Entity entity, String mimeType) {
		// This is a new row; first get the contactId
		// If the Contact is new, use the saved back value; otherwise the value
		// in the entity
		int contactId = mContactBackValue;
		if (entity != null) {
			contactId = entity.getEntityValues().getAsInteger(Profile._ID);
		}

		// Create an insert operation with the proper contactId reference
		RowBuilder builder = new RowBuilder(
				ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(Data.CONTENT_URI)));
		if (entity == null) {
			builder.withValueBackReference(Data.RAW_CONTACT_ID, contactId);
		} else {
			builder.withValue(Data.RAW_CONTACT_ID, contactId);
		}

		// Set the mime type of the row
		builder.withValue(Data.MIMETYPE, mimeType);
		return builder;
	}

	/**
	 * Compare a column in a ContentValues with an (old) value, and see if they
	 * are the same. For this purpose, null and an empty string are considered
	 * the same.
	 * 
	 * @param cv
	 *            a ContentValues object, from a NamedContentValues
	 * @param column
	 *            a column that might be in the ContentValues
	 * @param oldValue
	 *            an old value (or null) to check against
	 * @return whether the column's value in the ContentValues matches oldValue
	 */
	private boolean cvCompareString(ContentValues cv, String column,
			String oldValue) {
		if (cv.containsKey(column)) {
			if (oldValue != null && cv.getAsString(column).equals(oldValue)) {
				return true;
			}
		} else if (oldValue == null || oldValue.length() == 0) {
			return true;
		}
		return false;
	}
	
	public void addBirthday(Entity entity, String birthday) {
		RowBuilder builder = typedRowBuilder(entity, Event.CONTENT_ITEM_TYPE,
				Event.TYPE_BIRTHDAY);
		ContentValues cv = builder.cv;
		if (cv != null && cvCompareString(cv, Event.START_DATE, birthday)) {
			return;
		}
		builder.withValue(Event.START_DATE, birthday);
		builder.withValue(Event.TYPE, Event.TYPE_BIRTHDAY);
		add(builder.build());
	}

	public void addName(Entity entity, String prefix, String givenName,
			String familyName, String middleName, String suffix, String yomiFirstName, String yomiLastName,
			String yomiMiddleName,String fileAs) {
		RowBuilder builder = untypedRowBuilder(entity,
				StructuredName.CONTENT_ITEM_TYPE);
		ContentValues cv = builder.cv;
		if (cv != null
				&& cvCompareString(cv, StructuredName.GIVEN_NAME, givenName)
				&& cvCompareString(cv, StructuredName.FAMILY_NAME, familyName)
				&& cvCompareString(cv, StructuredName.MIDDLE_NAME, middleName)
				&& cvCompareString(cv, StructuredName.PREFIX, prefix)
				&& cvCompareString(cv, StructuredName.PHONETIC_GIVEN_NAME,
						yomiFirstName)
				&& cvCompareString(cv, StructuredName.PHONETIC_FAMILY_NAME,
						yomiLastName)
				&& cvCompareString(cv, StructuredName.PHONETIC_MIDDLE_NAME,
						yomiMiddleName)
//				&& cvCompareString(cv, StructuredName.DISPLAY_NAME, displayName)
				&& cvCompareString(cv, StructuredName.SUFFIX, suffix)) {
			return;
		}
		builder.withValue(StructuredName.GIVEN_NAME, givenName);
		builder.withValue(StructuredName.FAMILY_NAME, familyName);
		builder.withValue(StructuredName.MIDDLE_NAME, middleName);
		builder.withValue(StructuredName.SUFFIX, suffix);
		builder.withValue(StructuredName.PHONETIC_GIVEN_NAME, yomiFirstName);
		builder.withValue(StructuredName.PHONETIC_FAMILY_NAME, yomiLastName);
		builder.withValue(StructuredName.PHONETIC_MIDDLE_NAME, yomiMiddleName);
		builder.withValue(StructuredName.PREFIX, prefix);
		//builder.withValue(StructuredName.DISPLAY_NAME, displayName);
		add(builder.build());
	}

	public void addPhoto(Entity entity, byte[] photoBytes) {
		RowBuilder builder = untypedRowBuilder(entity, Photo.CONTENT_ITEM_TYPE);
		if(photoBytes == null || photoBytes.length == 0){
			return;
		}
		builder.withValue(Photo.PHOTO, photoBytes);
		add(builder.build());
	}

	public void addPhone(Entity entity, int type, String phone,
							String label, boolean isPrimary) {
		RowBuilder builder = typedRowBuilder(entity, Phone.CONTENT_ITEM_TYPE,
				type);
		ContentValues cv = builder.cv;
		if (cv != null && cvCompareString(cv, Phone.NUMBER, phone)
				&& cvCompareString(cv, Phone.LABEL, label)) {
			return;
		}
		builder.withValue(Phone.TYPE, type);
		builder.withValue(Phone.NUMBER, phone);
		builder.withValue(Phone.LABEL, label);
		builder.withValue(Phone.IS_SUPER_PRIMARY, isPrimary ? 1 : 0);
		add(builder.build());
	}

	public void addNickname(Entity entity, String name) {
		RowBuilder builder = typedRowBuilder(entity,
				Nickname.CONTENT_ITEM_TYPE, Nickname.TYPE_DEFAULT);
		ContentValues cv = builder.cv;
		if (cv != null && cvCompareString(cv, Nickname.NAME, name)) {
			return;
		}
		builder.withValue(Nickname.TYPE, Nickname.TYPE_DEFAULT);
		builder.withValue(Nickname.NAME, name);
		add(builder.build());
	}

	public void addPostal(Entity entity,
							int type,
							String pobox,
							String extendedAddress,
							String street,
							String localty,//localty == city
							String region,
							String postalCode,
							String country,
							String label,
							boolean isPrimary) {
		RowBuilder builder = typedRowBuilder(entity,
				StructuredPostal.CONTENT_ITEM_TYPE, type);
		ContentValues cv = builder.cv;
		if (cv != null &&  cvCompareString(cv, StructuredPostal.POBOX, pobox)
				&& cvCompareString(cv, StructuredPostal.STREET, street)
				&& cvCompareString(cv, StructuredPostal.CITY, localty)
				&& cvCompareString(cv, StructuredPostal.REGION, region)
				&& cvCompareString(cv, StructuredPostal.POSTCODE, postalCode)
				&& cvCompareString(cv, StructuredPostal.COUNTRY, country)) {
			return;
		}
		builder.withValue(StructuredPostal.TYPE, type);
		builder.withValue(StructuredPostal.POBOX, pobox);
		builder.withValue(StructuredPostal.NEIGHBORHOOD, extendedAddress);// extendeAddress
		builder.withValue(StructuredPostal.STREET, street);
		builder.withValue(StructuredPostal.CITY, localty);
		builder.withValue(StructuredPostal.REGION, region);
		builder.withValue(StructuredPostal.POSTCODE, postalCode);
		builder.withValue(StructuredPostal.COUNTRY, country);
		builder.withValue(StructuredPostal.LABEL, label);
		builder.withValue(StructuredPostal.IS_SUPER_PRIMARY, isPrimary ? 1 : 0);
		add(builder.build());
	}

	public void addOrganization(Entity entity, int type, String company,
			String title, String department, String yomiCompanyName,
			String officeLocation, String label, boolean isPrimary) {
		RowBuilder builder = typedRowBuilder(entity,
				Organization.CONTENT_ITEM_TYPE, type);
		ContentValues cv = builder.cv;
		if (cv != null
				&& cvCompareString(cv, Organization.COMPANY, company)
				&& cvCompareString(cv, Organization.PHONETIC_NAME,
						yomiCompanyName)
				&& cvCompareString(cv, Organization.DEPARTMENT, department)
				&& cvCompareString(cv, Organization.TITLE, title)
				&& cvCompareString(cv, Organization.OFFICE_LOCATION,
						officeLocation)) {
			return;
		}
		builder.withValue(Organization.TYPE, type);
		builder.withValue(Organization.COMPANY, company);
		builder.withValue(Organization.TITLE, title);
		builder.withValue(Organization.DEPARTMENT, department);
		builder.withValue(Organization.PHONETIC_NAME, yomiCompanyName);
		builder.withValue(Organization.OFFICE_LOCATION, officeLocation);
		builder.withValue(Organization.LABEL, label);
		builder.withValue(Organization.IS_SUPER_PRIMARY, isPrimary ? 1 : 0);
		add(builder.build());
	}
	
	public void addEmail(Entity entity, int type, String email, String label, boolean isPrimary) {
		RowBuilder builder = typedRowBuilder(entity, Email.CONTENT_ITEM_TYPE,
				type);
		String email_data;
        String displayName;
		ContentValues cv = builder.cv;
		Rfc822Token[] tokens = null;
		if(email != null){
			tokens = Rfc822Tokenizer.tokenize(email);
		}
		// Can't happen, but belt & suspenders
		if (tokens == null || tokens.length == 0) {
        	email_data = "";
          	displayName = "";
        } else {
            Rfc822Token token = tokens[0];
            email_data = token.getAddress();
            displayName = token.getName();
        }
		if (cv != null && cvCompareString(cv, Email.DATA, email_data)
				&& cvCompareString(cv, Email.DISPLAY_NAME, displayName)) {
			return;
		}
		builder.withValue(Email.TYPE, type);
		builder.withValue(Email.DATA, email);
		builder.withValue(Email.DISPLAY_NAME, displayName);
		builder.withValue(Email.LABEL, label);
		builder.withValue(Email.IS_SUPER_PRIMARY, isPrimary ? 1 : 0);
		add(builder.build());
	}
	
}

/**
 * SmartBuilder is a wrapper for the Builder class that is used to create/update rows for a
 * ContentProvider.  It has, in addition to the Builder, ContentValues which, if present,
 * represent the current values of that row, that can be compared against current values to
 * see whether an update is even necessary.  The methods on SmartBuilder are delegated to
 * the Builder.
 */
class RowBuilder {
    Builder builder;
    ContentValues cv;

    public RowBuilder(Builder _builder) {
        builder = _builder;
    }

    public RowBuilder(Builder _builder, NamedContentValues _ncv) {
        builder = _builder;
        cv = _ncv.values;
    }

    RowBuilder withValues(ContentValues values) {
        builder.withValues(values);
        return this;
    }

    RowBuilder withValueBackReference(String key, int previousResult) {
        builder.withValueBackReference(key, previousResult);
        return this;
    }

    ContentProviderOperation build() {
        return builder.build();
    }

    RowBuilder withValue(String key, Object value) {
        builder.withValue(key, value);
        return this;
    }
}
