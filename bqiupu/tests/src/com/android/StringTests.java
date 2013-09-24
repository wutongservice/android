package com.android;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-12-8
 * Time: 上午11:11
 * To change this template use File | Settings | File Templates.
 */
public class StringTests extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @LargeTest
    public void testValidFormat() {
        final String [] validFormat = {
                "%1$s", "%s", "%d"
        };

        final String msgTextSuffix = "should be a valid format in String.format method";
        final int testInterval = 10;
        final String expectedText = "it is 10 hours ago";
        String formatText;
        for (int i = 0; i < validFormat.length; ++i) {
            try {
                formatText = "it is " + validFormat[i] + " hours ago";
                final String actualText = String.format(formatText, testInterval);
                assertEquals((validFormat[i] + msgTextSuffix), expectedText, actualText);
            } catch (Exception e) {
                assertTrue((validFormat[i] + msgTextSuffix), false);
            }
        }
    }

    @LargeTest
    public void testInvalidFormat() {
        final String [] invalidFormat = {
                "%1%s", "%1", "%l"
        };

        final String msgTextSuffix = "should NOT be a valid format in String.format method";
        final int testInterval = 10;
        final String expectedText = "it is 10 hours ago";
        String formatText;
        for (int i = 0; i < invalidFormat.length; ++i) {
            try {
                formatText = "it is " + invalidFormat[i] + " hours ago";
                final String actualText = String.format(formatText, testInterval);
                assertNotSame((invalidFormat[i] + msgTextSuffix), expectedText, actualText);
            } catch (Exception e) {
                assertTrue((invalidFormat[i] + msgTextSuffix), true);
            }
        }
    }

    @SmallTest
    public void testIsEmpty() {
        assertEquals("null is empty", true, TextUtils.isEmpty(null));
        assertEquals("Empty string is empty", true, TextUtils.isEmpty(""));
        assertEquals("Only space text is NOT empty", false, TextUtils.isEmpty("    "));
        assertEquals("All blanket text is NOT empty", false, TextUtils.isEmpty("\n\t"));
    }
}
