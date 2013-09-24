package com.borqs.common.util;

import java.io.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.borqs.qiupu.QiupuConfig;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.bpc.BeamDataByNFCActivity;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-10-17
 * Time: 下午12:23
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    public static boolean testReadFile(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            if (file.exists()) {
                if (file.canRead()) {
                    return true;
                } else {
                    Log.i(TAG, "testReadFile, can NOT existing file: " + fileName);
                }
            } else {
                Log.i(TAG, "testReadFile, file not existing: " + fileName);
            }
        } else {
            Log.i(TAG, "testReadFile, empty file name.");
        }

        return false;
    }

    public static boolean testReadFile(File file) {
        if (file.exists()) {
            if (file.canRead()) {
                return true;
            } else {
                Log.i(TAG, "testReadFile, can NOT existing file: " + file.getName());
            }
        } else {
            Log.i(TAG, "testReadFile, file not existing: " + file.getName());
        }

        return false;
    }

    public static boolean testWriteFile(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            if (file.exists()) {
                if (file.canWrite()) {
                    return true;
                } else {
                    Log.i(TAG, "testReadFile, can NOT existing file: " + fileName);
                }
            } else {
                Log.i(TAG, "testReadFile, file not existing: " + fileName);
            }
        } else {
            Log.i(TAG, "testReadFile, empty file name.");
        }

        return false;
    }

    public static String formatPackageFileSizeString(Context context, long fileSize) {
        final String fileSizeText;
        fileSize >>= 10;
        if(fileSize < 1024){
            fileSizeText = String.format(context.getString(R.string.app_info_version_size_title),
                    String.valueOf(fileSize)+"KB");
        }else{
            fileSize >>= 10;
            fileSizeText = String.format(context.getString(R.string.app_info_version_size_title),
                    String.valueOf(fileSize)+"MB");
        }
        return fileSizeText;
    }
    
    public static String formatPackageFileSize(Context context, long fileSize) {
        fileSize >>= 10;
        if(fileSize < 1024){
            return String.valueOf(fileSize)+"KB";
        }else{
            fileSize >>= 10;
        return String.valueOf(fileSize)+"MB";
        }
    }

    private static final int KB_UNIT = 1024;
    private static final int MB_UNIT = 1024*1024;

    public static String formatFileSize(long fileSize, int accuracy) {
        String fileLength = "";
        if (fileSize <= 0) {
            fileLength = "0KB";
        } else if (fileSize > 0 && fileSize < MB_UNIT) {
            fileLength = String.valueOf(div(fileSize, KB_UNIT, accuracy)) + "KB";
        } else {
            fileLength = String.valueOf(div(fileSize, MB_UNIT, accuracy)) + "MB";
        }
        return fileLength;
    }

    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String filterPhotoVcard(String vcard) {
        if (vcard == null) {
            return "";
        }

        int photoIndexStart = vcard.indexOf("PHOTO;");
        if (photoIndexStart == -1) {
            return vcard;
        }

        int indexAfterFirstColon = vcard.indexOf(":", photoIndexStart) + 1;
        int indexOfSecondColon = vcard.indexOf(":", indexAfterFirstColon);
        String tmp = vcard.substring(photoIndexStart, indexOfSecondColon);
        int photoIndexOffset = tmp.lastIndexOf("\n");

        vcard = vcard.subSequence(0, photoIndexStart) + vcard.substring(photoIndexStart + photoIndexOffset);
        return vcard;
    }

    public static ArrayList<String> subList(ArrayList<String> list) {
        ArrayList<String> tmpList = new ArrayList<String>();
        for (int i = 0, len = list.size(); i < len; i++) {
            String str = list.get(i);
            int start = str.indexOf(">");
            int end = str.indexOf("<", start);
            tmpList.add(str.substring(start + 1, end));
        }
        return tmpList;
    }

    public static Uri createTmpFile(Context context, String fileName,
            byte[] bytes, String tmpPath) {
        if (bytes == null || fileName == null) {
            return null;
        }
        String tmpFileName = fileName;
        if (tmpFileName.contains("&")) {
            tmpFileName = tmpFileName.replace("&", "a");
        }

        if (tmpFileName.contains("/")) {
            tmpFileName = tmpFileName.replace("/", "");
        }

        File tmpFile;
        if (TextUtils.isEmpty(tmpPath)) {
            tmpFile = new File(QiupuConfig.getSdcardPath(tmpFileName));
        } else {
            tmpFile = new File(tmpPath, tmpFileName);
        }
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        try {
            tmpFile.createNewFile();
        } catch (Exception e) {
            Log.e(TAG, "create file fail " + e);
            return null;
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(tmpFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "uri not exist " + e);
        }

        try {
            out.write(bytes);
            Runtime runtime = Runtime.getRuntime();
            String [] cmd = new String[]{"chmod","644", tmpFile.toString()};
            runtime.exec(cmd);
            return Uri.fromFile(tmpFile);
        } catch (IOException e1) {
            Log.e(TAG, " write file " + tmpFile + " exception :" + e1);
            return null;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                Log.e(TAG, " stream close error: " + e);
            }
        }
    }

    public static boolean insertContact(Context context, Handler handler, String userName, ArrayList<String> phoneList, ArrayList<String> emailList) {
        String accountType = null;
        String accountName = null;

        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccounts();
        for (int i = 0; i < accounts.length; i++) {
            if ("com.borqs".equals(accounts[i].type)) {
                accountType = accounts[i].type;
                accountName = accounts[i].name;
                break;
            }
        }

        // insert raw_contact
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(RawContacts.ACCOUNT_TYPE, accountType);
        values.put(RawContacts.ACCOUNT_NAME, accountName);
        Uri rawContactUri = cr.insert(RawContacts.CONTENT_URI, values);
        values.clear();

        long rawContactId = ContentUris.parseId(rawContactUri);
        Log.d(TAG,"rawContactUri = " + rawContactUri.toString() + " rawContactId = " + rawContactId);

        // insert user name
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        values.put(StructuredName.DISPLAY_NAME, userName);
        Uri userNameUri = cr.insert(Data.CONTENT_URI, values);
        values.clear();

        // insert phone
        if (phoneList != null) {
            for (int i = 0; i< phoneList.size(); i++) {
                values.clear();
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                values.put(Phone.NUMBER, phoneList.get(i));
                values.put(Phone.TYPE, Phone.TYPE_CUSTOM);
                Uri phoneUri = cr.insert(Data.CONTENT_URI, values);
            }
        }

        // insert email
        if (emailList != null) {
            for (int i = 0; i< emailList.size(); i++) {
                values.clear();
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                values.put(Phone.NUMBER, emailList.get(i));
                values.put(Phone.TYPE, Email.TYPE_CUSTOM);
                Uri emailUri = cr.insert(Data.CONTENT_URI, values);
            }
        }

        Message msg = null;
        if (handler != null) {
            msg = handler.obtainMessage(BeamDataByNFCActivity.MESSAGE_END_IMPORT_VCARD);
        }

        if (rawContactUri == null || userNameUri == null) {
            if (msg != null) {
                msg.getData().putBoolean("RESULT", false);
                msg.sendToTarget();
            }
            return false;
        } else {
            if (msg != null) {
                msg.getData().putBoolean("RESULT", true);
                msg.sendToTarget();
            }
            return true;
        }
    }

    public static StringBuffer readAssetFileContent(Context context, String fileName, String charset) {
        StringBuffer buf = new StringBuffer();
        try {
            InputStream in = context.getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    in, charset));

            char[] chBuf = new char[1024];
            int lineLen = 0;
            while ((lineLen = br.read(chBuf, 0, 1024)) > 0) {
                buf.append(chBuf, 0, lineLen);
            }

            br.close();

            br = null;
            in = null;
            chBuf = null;
        } catch (java.io.IOException ne) {
        }

        return buf;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Drawable createProfileIcon(Context context, String photoUrl) {
        Bitmap baseBmp = null;
        // QiupuUser user = orm.queryOneUserInfo(getSaveUid());
        if (!TextUtils.isEmpty(photoUrl)) {
            URL imageurl;
            try {
                imageurl = new URL(photoUrl);

                String filepath = QiupuHelper.getImageFilePath(imageurl, true);
                if (new File(filepath).exists()) {
                    Bitmap tmp = BitmapFactory.decodeFile(filepath);

                    baseBmp = BitmapFactory
                            .decodeResource(
                                    context.getResources(),
                                    R.drawable.home_screen_profile_icon_overlay_default);

                    Bitmap newmap = Bitmap.createBitmap(baseBmp.getWidth(),
                            baseBmp.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas can = new Canvas();
                    can.setBitmap(newmap);

                    // scale tmp to fit background

                    final int profileHeight = (int) (0.8 * baseBmp.getWidth());

                    final int sourceWidth = tmp.getWidth();
                    final int sourceHeight = tmp.getHeight();

                    // scale the bitmap
                    float scale = 1.0f;
                    if (sourceHeight > sourceWidth) {
                        scale = (float) profileHeight / (float) tmp.getHeight();
                    } else {
                        scale = (float) profileHeight / (float) tmp.getWidth();
                    }

                    Matrix matrix = new Matrix();
                    matrix.setScale(scale, scale);
                    tmp = Bitmap.createBitmap(tmp, 0, 0, sourceWidth,
                            sourceHeight, matrix, true);

                    final int startPos = (baseBmp.getWidth() - tmp.getWidth()) / 2;
                    final int endPos = (baseBmp.getHeight() - tmp.getHeight()) / 2;

                    can.drawBitmap(tmp, startPos, endPos, null);
//                    can.drawBitmap(baseBmp, 0, 0, null);

                    tmp.recycle();
                    tmp = null;

                    baseBmp.recycle();
                    baseBmp = null;

                    Drawable draw = new BitmapDrawable(newmap);
                    ((BitmapDrawable) draw).setTargetDensity(context
                            .getResources().getDisplayMetrics());

                    return draw;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception ne) {
                ne.printStackTrace();
            }

            return context.getResources().getDrawable(R.drawable.main_profile);
        } else {
            return context.getResources().getDrawable(R.drawable.main_profile);
        }
    }
}
