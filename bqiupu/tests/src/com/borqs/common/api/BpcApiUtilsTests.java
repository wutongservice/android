/**
 * 
 */
package com.borqs.common.api;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

/**
 * @author b608
 *
 */
public class BpcApiUtilsTests extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @SmallTest
    public void testPreconditions() {

    }

    @SmallTest
    public void testIsValidAppId() {
        assertFalse("BpcApiUtils.APPID.NONE should not be a valid App Id",
                BpcApiUtils.isValidAppId(BpcApiUtils.APPID.NONE));

        assertTrue("BpcApiUtils.APPID.Apk should be a valid App Id",
                BpcApiUtils.isValidAppId(BpcApiUtils.APPID.APK));

        assertTrue("BpcApiUtils.APPID.BOOK should be a valid App Id",
                BpcApiUtils.isValidAppId(BpcApiUtils.APPID.BOOK));

        assertTrue("BpcApiUtils.APPID.MUSIC should be a valid App Id",
                BpcApiUtils.isValidAppId(BpcApiUtils.APPID.MUSIC));

        assertTrue("BpcApiUtils.APPID.BPC_APPID should be a valid App Id",
                BpcApiUtils.isValidAppId(BpcApiUtils.APPID.BPC_APPID));
    }

    @SmallTest
    public void testIsValidStreamType() {
        assertTrue(String.format("BpcApiUtils.ALL_TYPE_POSTS(%d) should be valid Stream type", BpcApiUtils.ALL_TYPE_POSTS),
                BpcApiUtils.isValidStreamType(BpcApiUtils.ALL_TYPE_POSTS));

        for (int off = 0; off <= BpcApiUtils.MAX_STREAM_POST_OFFSET; ++off) {
            assertTrue(String.format("%d (1<<= %d) should be valid Stream type", (1<<off), off),
                    BpcApiUtils.isValidStreamType((1<<off)));
        }

        for (int i = BpcApiUtils.MAX_STREAM_POST_OFFSET + 1; i < BpcApiUtils.MAX_STREAM_POST_OFFSET + 5; ++i) {
            assertFalse(String.format("%d (1<<= %d) should be valid Stream type", (1<<i), i),
                    BpcApiUtils.isValidStreamType((1<<i)));
        }

        assertTrue(String.format("BpcApiUtils.ONLY_APK_POST(%d) should be valid Stream type", BpcApiUtils.ONLY_APK_POST),
                BpcApiUtils.isValidStreamType(BpcApiUtils.ONLY_APK_POST));
        assertTrue(String.format("BpcApiUtils.ALL_APK_POST(%d) should be valid Stream type", BpcApiUtils.ALL_APK_POST),
                BpcApiUtils.isValidStreamType(BpcApiUtils.ALL_APK_POST));

        assertTrue(String.format("BpcApiUtils.ONLY_BOOK_POST(%d) should be valid Stream type", BpcApiUtils.ONLY_BOOK_POST),
                BpcApiUtils.isValidStreamType(BpcApiUtils.ONLY_BOOK_POST));
        assertTrue(String.format("BpcApiUtils.ALL_BOOK_POST(%d) should be valid Stream type", BpcApiUtils.ALL_BOOK_POST),
                BpcApiUtils.isValidStreamType(BpcApiUtils.ALL_BOOK_POST));


        assertTrue(String.format("BpcApiUtils.ONLY_MUSIC_POST(%d) should be valid Stream type", BpcApiUtils.ONLY_MUSIC_POST),
                BpcApiUtils.isValidStreamType(BpcApiUtils.ONLY_MUSIC_POST));
        assertTrue(String.format("BpcApiUtils.ALL_MUSIC_POST(%d) should be valid Stream type", BpcApiUtils.ALL_MUSIC_POST),
                BpcApiUtils.isValidStreamType(BpcApiUtils.ALL_MUSIC_POST));
    }
}
