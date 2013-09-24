package com.android;

import android.os.Build;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-12-8
 * Time: 上午11:11
 * To change this template use File | Settings | File Templates.
 */
public class BuildTests extends TestCase {
    private static final String TAG = "BuildTests";
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

    @LargeTest
    public void testBuild() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("ID:").append(Build.ID).append("\n").
                append("DISPLAY:").append(Build.DISPLAY).append("\n").
                append("PRODUCT:").append(Build.PRODUCT).append("\n").
                append("DEVICE:").append(Build.DEVICE).append("\n").
                append("BOARD:").append(Build.BOARD).append("\n").
                append("CPU_ABI:").append(Build.CPU_ABI).append("\n").
                append("CPU_ABI2:").append(Build.CPU_ABI2).append("\n").
                append("MANUFACTURER:").append(Build.MANUFACTURER).append("\n").
                append("BRAND:").append(Build.BRAND).append("\n").
                append("MODEL:").append(Build.MODEL).append("\n").
                append("BOOTLOADER:").append(Build.BOOTLOADER).append("\n").
                append("RADIO:").append(Build.RADIO).append("\n").
                append("HARDWARE:").append(Build.HARDWARE).append("\n").
                append("SERIAL:").append(Build.SERIAL).append("\n").
                append("TYPE:").append(Build.TYPE).append("\n").
                append("TAGS:").append(Build.TAGS).append("\n").
                append("FINGERPRINT:").append(Build.FINGERPRINT).append("\n").
                append("TIME:").append(Build.TIME).append("\n").
                append("USER:").append(Build.USER).append("\n").
                append("HOST:").append(Build.HOST).append("\n");
        Log.d(TAG, stringBuffer.toString());
        assertTrue(stringBuffer.toString(), true);

        stringBuffer.setLength(0);
        stringBuffer.append("VERSION.INCREMENTAL:").append(Build.VERSION.INCREMENTAL).append("\n").
                append("VERSION.RELEASE:").append(Build.VERSION.RELEASE).append("\n").
                append("VERSION.SDK:").append(Build.VERSION.SDK).append("\n").
                append("VERSION.SDK_INT:").append(Build.VERSION.SDK_INT).append("\n").
                append("VERSION.CODENAME:").append(Build.VERSION.CODENAME).append("\n");

        Log.d(TAG, stringBuffer.toString());
        assertTrue(stringBuffer.toString(), true);
    }
}
