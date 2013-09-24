package com.borqs.qiupu;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import com.borqs.qiupu.ui.AboutActivity;

public class AboutActivityTests extends ActivityInstrumentationTestCase2<AboutActivity> {
    private final String[] LOOKUP_PROJECTION = new String[] {
            Contacts.LOOKUP_KEY
    };
    final Uri CONTENT_MULTI_VCARD_URI = Uri.withAppendedPath(Contacts.CONTENT_URI, "as_multi_vcard");

	/**
     * Creates an {@link ActivityInstrumentationTestCase2} for the {@link AboutActivity} activity.
     */
    public AboutActivityTests() {
        super(AboutActivity.class);
    }
    
    /**
     * Verifies that the activity under test can be launched.
     */
    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }
    
	public void testPreconditions() {
		// TODO Auto-generated method stub
	}

    // This code comes from ICS Contacts app: doShareVisibleContacts() in ImportExportDialogFragment.java
    @LargeTest
    public void testContactsVcardFormat() {
        final Cursor cursor = getActivity().getContentResolver().query(Contacts.CONTENT_URI,
                LOOKUP_PROJECTION, Contacts.IN_VISIBLE_GROUP + "!=0", null, null);
        if (cursor != null) {
            try {
                if (!cursor.moveToFirst()) {
                    Log.d("AboutActivityTests", "testValidFormat, get empty cursor.");
                    return;
                }

                StringBuilder uriListBuilder = new StringBuilder();
                int index = 0;
                do {
                    if (index != 0)
                        uriListBuilder.append(':');
                    uriListBuilder.append(cursor.getString(0));
                    index++;
                } while (cursor.moveToNext());

                final String uriListString = uriListBuilder.toString();
                Uri uri = Uri.withAppendedPath(
                        CONTENT_MULTI_VCARD_URI,
                        Uri.encode(uriListString));
                Log.d("AboutActivityTests", "testValidFormat, built string: " + uriListString);
                Log.d("AboutActivityTests", "testValidFormat, encoded uriString: " + uri.toString());

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(Contacts.CONTENT_VCARD_TYPE);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                getActivity().startActivity(intent);
            } finally {
                cursor.close();
            }
        }
    }
}
