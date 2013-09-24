/**
 * 
 */
package com.borqs.common.utils;

import java.util.ArrayList;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.borqs.common.util.FileUtils;

/**
 * @author b608
 *
 */
public class FileUtilsTests extends AndroidTestCase {
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
    public void testFilterPhotoVcard() {
        final String emptyVcard = "";
        assertEquals(FileUtils.filterPhotoVcard(emptyVcard), emptyVcard);

        final String fullVcard = "BEGIN:VCARD\nVERSION:2.1\nN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=E7=99=BD;=E7=92=90;;;\nFN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=E7=99=BD=E7=92=90\nTEL;CELL;PREF:1-581-091-3210\nORG:\nPHOTO;ENCODING=BASE64;JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQE\n CAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/\n 2wBDAQICAgICAgUDAwUKBwYHCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKC\n goKCgoKCgoKCgoKCgr/wAARCABQAFADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAA\n ECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaE\n II0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZn\n aGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJy\n tLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAw\n QFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobH\n BCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hp\n anN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0\n tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD2f/gnL+2d4l/bF+C+s+\n JfGdjFba5oOsS2V5d2ln5UV1/y1ilr5q8R/wDBSL9uLRPAXiTxH4a1bSNT1HRvFVrpsNp/YUX\n +qliu/wD41XlHw88S+K/hr+yP4k+AnhLXtS03WLnxJ5mpS2k32aWW1il/exeb5v8Ay1rvP2Vf\n gN8fP2rPCOqT3Hx0tvCXga6m/s3w3a6jqUv2a6v7W1/dReV/39llr85wOGwdKtVqVKR6mIw3s\n a1P2h2H7Hv/AAU8/bM+Kvj5PhD8V7HRP3l5YW013N4c8uWWK61rT7CX/Vfuv9Vc19H/ALS37Z\n Hxo/ZTv9L0H4c+GvD99b6ppuvXt3/blpLJL5thazSxf6mWL/nlXP8Axs+D0/7FnwN+GPjP4Q/\n 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\n\nBDAY:\nEND:VCARD\n";
        final String expectedVcard = "BEGIN:VCARD\nVERSION:2.1\nN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=E7=99=BD;=E7=92=90;;;\nFN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=E7=99=BD=E7=92=90\nTEL;CELL;PREF:1-581-091-3210\nORG:\n\nBDAY:\nEND:VCARD\n";

        assertEquals(FileUtils.filterPhotoVcard(fullVcard), expectedVcard);

        assertEquals(FileUtils.filterPhotoVcard(expectedVcard), expectedVcard);
    }

    @SmallTest
    public void testSubList() throws Exception{
        String address = "Beijing, ChaoYang, Wangjing, Suite A";
        String location = "<a href='http://maps.google.com/maps?q=39.986124%2C116.464048'>" + address + "</a>";
        ArrayList<String> list = new ArrayList<String>();
        list.add(location);

        ArrayList<String> subList = FileUtils.subList(list);
        assertEquals(address, subList.get(0));
    }

}
