package com.borqs.common.view;

import android.widget.MultiAutoCompleteTextView;
import com.borqs.common.util.MentionTokenizer;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-3
 * Time: 下午1:34
 * To change this template use File | Settings | File Templates.
 */
public class MentionTokenizerTests extends TestCase {
    private static final String TAG = "MentionTokenizerTests";

    private static abstract class AbstractMentionTestsData {
        static class TestUnit {
            int expectedStartIndex;
            int expectedEndIndex;
            CharSequence segmentText;
            CharSequence expectedTermination;
            TestUnit(int start, int end, CharSequence segment, CharSequence termination) {
                expectedStartIndex = start;
                expectedEndIndex = end;
                segmentText = segment;
                expectedTermination = termination;
            }
        };

        private ArrayList<TestUnit> mTestUnitList = new ArrayList<TestUnit>();
        private int mCurrentIndex = 0;
        protected void init(char ch) {
            mCurrentIndex = 0;
            mTestUnitList.add(new TestUnit(0, 5, ch + "wuto ", ch + "wuto "));
            mTestUnitList.add(new TestUnit(0, 7, ch + "wuto  ", ch + "wuto  "));
            mTestUnitList.add(new TestUnit(8, 8, "Wu" + ch + "tong ", "Wu" + ch + "tong "));
            mTestUnitList.add(new TestUnit(12, 12, "Wu" + ch + "tong  shu", "Wu" + ch + "tong  shu "));
            mTestUnitList.add(new TestUnit(3, 9, "Wu " + ch + "tong ", "Wu " + ch + "tong "));
            mTestUnitList.add(new TestUnit(3, 13, "Wu " + ch + "tong  shu", "Wu " + ch + "tong  shu "));
        }
        public TestUnit getTestUnit() {
            final int size = mTestUnitList.size();
            if (mCurrentIndex < size) {
                ++mCurrentIndex;
                if (mCurrentIndex < size) {
                    return mTestUnitList.get(mCurrentIndex);
                }
            }
            return null;
        }

        public static void verifyTokenStartTestUnit(MentionTokenizer tokenizer, TestUnit testUnit) {
            assertEquals("token start of " + testUnit.segmentText + " is " + testUnit.expectedStartIndex,
                    testUnit.expectedStartIndex,
                    tokenizer.findTokenStart(testUnit.segmentText, testUnit.segmentText.length()));
        }

        public static void verifyTokenEndTestUnit(MentionTokenizer tokenizer, TestUnit testUnit) {
            assertEquals("token end of " + testUnit.segmentText + " is " + testUnit.expectedEndIndex,
                    testUnit.expectedEndIndex,
                    tokenizer.findTokenEnd(testUnit.segmentText, testUnit.segmentText.length()));
        }

        public static void verifyTokenTerminationTestUnit(MentionTokenizer tokenizer, TestUnit testUnit) {
            assertEquals("token Termination of " + testUnit.segmentText + " is " + testUnit.expectedTermination,
                    testUnit.expectedTermination,
                    tokenizer.terminateToken(testUnit.segmentText));
        }

    }

    private static class PlusMentionTestsData extends AbstractMentionTestsData {
        public PlusMentionTestsData() {
            init('+');
        }
    }

    private static class AtMentionTestsData extends AbstractMentionTestsData {
        public AtMentionTestsData() {
            init('@');
        }
    }

    private MentionTokenizer mTokenizer;
    private AbstractMentionTestsData mPlusMentions;
    private AbstractMentionTestsData mAtMentions;
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTokenizer = new MentionTokenizer();
        mPlusMentions = new PlusMentionTestsData();
        mAtMentions = new AtMentionTestsData();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mTokenizer = null;
        mPlusMentions = null;
        mAtMentions = null;
    }

    public void testIsMentionTrigger() {
        assertEquals("+ is mention trigger.", true, MentionTokenizer.isMentionTrigger('+'));
        assertEquals("@ is mention trigger.", true, MentionTokenizer.isMentionTrigger('+'));

        char ch;
        for (int i = 0; i < 255; ++i) {
            ch = (char)i;
            if (ch != '+' && ch != '@') {
                assertEquals("" + ch + " is NOT mention trigger.", false, MentionTokenizer.isMentionTrigger(ch));
            }
        }
    }

    public void testFindTokenStart() {
        AbstractMentionTestsData.TestUnit testUnit;
        while (true) {
            testUnit = mAtMentions.getTestUnit();
            if (null == testUnit) {
                break;
            }
            AbstractMentionTestsData.verifyTokenStartTestUnit(mTokenizer, testUnit);
        }

        while (true) {
            testUnit = mPlusMentions.getTestUnit();
            if (null == testUnit) {
                break;
            }
            AbstractMentionTestsData.verifyTokenStartTestUnit(mTokenizer, testUnit);
        }
    }

    public void testFindTokenEnd() {
        AbstractMentionTestsData.TestUnit testUnit;
        while (true) {
            testUnit = mAtMentions.getTestUnit();
            if (null == testUnit) {
                break;
            }
            AbstractMentionTestsData.verifyTokenEndTestUnit(mTokenizer, testUnit);
        }

        while (true) {
            testUnit = mPlusMentions.getTestUnit();
            if (null == testUnit) {
                break;
            }
            AbstractMentionTestsData.verifyTokenEndTestUnit(mTokenizer, testUnit);
        }
    }

    public void testTerminateToken() {
        AbstractMentionTestsData.TestUnit testUnit;
        while (true) {
            testUnit = mAtMentions.getTestUnit();
            if (null == testUnit) {
                break;
            }
            AbstractMentionTestsData.verifyTokenTerminationTestUnit(mTokenizer, testUnit);
        }

        while (true) {
            testUnit = mPlusMentions.getTestUnit();
            if (null == testUnit) {
                break;
            }
            AbstractMentionTestsData.verifyTokenTerminationTestUnit(mTokenizer, testUnit);
        }
    }

    public void testCommaTokenizer() {
        MultiAutoCompleteTextView.CommaTokenizer commaTokenizer = new MultiAutoCompleteTextView.CommaTokenizer();
        final CharSequence data1 = ",wuton ";
        assertEquals(1, commaTokenizer.findTokenStart(data1, data1.length()));
        assertEquals(7, commaTokenizer.findTokenEnd(data1, data1.length()));
        assertEquals(",wuton , ", commaTokenizer.terminateToken(data1));
    }
}
