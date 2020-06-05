package me.hexian000.nativeprocess;

import android.app.Application;

import java.util.Locale;

public class NativeProcess extends Application {
    public final static String TAG = "NativeProcess";

    public static String formatSize(long size) {
        if (size == 0) {
            return "0";
        }
        final double n = Math.log(Math.abs(size)) / Math.log(1024.0);
        final double value = size / Math.pow(1024.0, Math.floor(n));
        if (n < 2.0) { // KB
            return String.format(Locale.getDefault(), "%.0fKB", value);
        }
        if (n < 3.0) { // MB
            return String.format(Locale.getDefault(), "%.1fMB", value);
        }
        if (n < 4.0) { // GB
            return String.format(Locale.getDefault(), "%.1fGB", value);
        }
        // TB
        return String.format(Locale.getDefault(), "%.1fTB", value);
    }
}
