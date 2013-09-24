package com.borqs.qiupu.util;

import java.util.ArrayList;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author b518
 */
public class JSONUtilTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this test
     * doesn't pass, the test case was not set up properly and it might explain
     * any and all failures in other tests. This is not guaranteed to run before
     * other tests, as junit uses reflection to find the tests.
     */
    @SmallTest
    public void testPreconditions() {

    }

    @SmallTest
    public void testcreatePhoneAndEmailJSONArray() {
        String name = "test";
        ArrayList<String> phoneList = new ArrayList<String>();
        ArrayList<String> emailList = new ArrayList<String>();
        for (int i = 0; i < 3; i++) {
            phoneList.add("10000" + i);
            emailList.add("test" + i + "@test.com");
        }
        
        String expected = "{\"phone_email\":[{\"phone\":\"100000\"},{\"phone\":\"100001\"},{\"phone\":\"100002\"},"
                + "{\"email\":\"test0@test.com\"},{\"email\":\"test1@test.com\"},{\"email\":\"test2@test.com\"}]," 
                + "\"borqs_id\":10058,\"mime_type\":\"vcard\",\"name\":\"test\",\"version\":\"1.0\"}";

        String json = JSONUtil.createPhoneAndEmailJSONArray(10058, name, phoneList, emailList);
        assertEquals(expected, json);
    }
}
