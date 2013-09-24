package com.borqs.qiupu.ui.bpc;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.borqs.qiupu.ui.bpc.MainActivityTest \
 * com.borqs.qiupu.tests/android.test.InstrumentationTestRunner
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainSplashActivity> {

    public MainActivityTest() {
        super("com.borqs.qiupu", MainSplashActivity.class);
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
}
