/**
 * 
 */
package com.borqs.common.utils;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.DataConnectionUtils;

/**
 * @author b608
 *
 */
public class DataConnectionUtilsTests extends AndroidTestCase {
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
    public void testIsValidServerUrl() {
        assertEquals(DataConnectionUtils.getCurrentServerUrl(getContext(), "api_host"), "http://api.borqs.com/");
    }

    @SmallTest
    public void testIsValidStreamType() {
        assertTrue(String.format("BpcApiUtils.ALL_MUSIC_POST(%d) should be valid Stream type", BpcApiUtils.ALL_MUSIC_POST),
                BpcApiUtils.isValidStreamType(BpcApiUtils.ALL_MUSIC_POST));
    }
}
