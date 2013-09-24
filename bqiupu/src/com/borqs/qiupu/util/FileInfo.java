package com.borqs.qiupu.util;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInfo {

    private FileInfo() {
    }

    /**
     * @param filename
     * @return
     */
    public static String mainName(String filename) {
        int start = filename.lastIndexOf("/");
        int stop = filename.lastIndexOf(".");
        if (stop < start)
            stop = filename.length();
        if (start >= 0) {
            return filename.substring(start + 1, stop);
        } else {
            return "";
        }
    }

    /**
     * @param filename
     * @return
     */
    public static String extension(String filename) {
        int start = filename.lastIndexOf("/");
        int stop = filename.lastIndexOf(".");
        if (stop < start || stop >= filename.length() - 1) {
            return "";
        } else {
            return filename.substring(stop + 1, filename.length());
        }
    }

    /**
     * @param filename
     * @return
     */
    public static String mimeType(String filename) {
        String ext = extension(filename);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return (mime == null) ? "*.*" : mime;
    }

    /**
     * @param size
     * @return
     */
    public static String sizeString(long size) {
        if (size < 1024)
            return String.format("%d B", size);
        else if (size < 1024 * 1024)
            return String.format("%.2f KB", (double) size / 1024);
        else if (size < 1024 * 1024 * 1024)
            return String.format("%.2f MB", (double) size / (1024 * 1024));
        else if (size < 1024L * 1024 * 1024 * 1024)
            return String.format("%.2f GB", (double) size
                    / (1024 * 1024 * 1024));
        else
            return String.format("%.2f EB", (double) size
                    / (1024L * 1024 * 1024 * 1024));
    }

    /**
     * @param sizeString
     * @return
     * @throws ParseException
     */
    public static long stringToSize(String sizeString) throws ParseException {
        Pattern pattern = Pattern.compile("(-?\\d+\\.?\\d*)([\\w]{0,2})",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sizeString);
        if (matcher.matches()) {
            double baseSize = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            if (unit.equals("b") || unit.length() == 0) {
                return (long) baseSize;
            } else if (unit.equals("k") || unit.equals("kb")) {
                return (long) (baseSize * 1024);
            } else if (unit.equals("m") || unit.equals("mb")) {
                return (long) (baseSize * (1024 * 1024));
            } else if (unit.equals("g") || unit.equals("gb")) {
                return (long) (baseSize * (1024 * 1024 * 1024));
            } else if (unit.equals("e") || unit.equals("eb")) {
                return (long) (baseSize * (1024L * 1024 * 1024 * 1024));
            }
        }
        throw new ParseException(sizeString, 0);
    }

    /**
     * @param timeString
     * @return
     * @throws ParseException
     */
    public static long timespanToMillis(String timeString)
            throws ParseException {
        Pattern pattern = Pattern.compile("(-?\\d+\\.?\\d*)([\\w]{0,1})",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(timeString);
        if (matcher.matches()) {
            double baseMillis = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            if (unit.equals("d") || unit.length() == 0) {
                return (long) (baseMillis * 1000 * 3600 * 24);
            } else if (unit.equals("h")) {
                return (long) (baseMillis * 1000 * 3600);
            } else if (unit.equals("w")) {
                return (long) (baseMillis * 1000 * 3600 * 24 * 7);
            } else if (unit.equals("m")) {
                return (long) (baseMillis * 1000 * 3600 * 24 * 30);
            } else if (unit.equals("y")) {
                return (long) (baseMillis * 1000 * 3600 * 24 * 360);
            }
        }
        throw new ParseException(timeString, 0);
    }

}
