package me.hexian000.nativeprocess;

import android.app.Application;

import me.hexian000.nativeprocess.api.Shell;

import java.text.DecimalFormat;
import java.util.Locale;

public class NativeProcess extends Application {
    public final static String LOG_TAG = "NativeProcess";
    public static Shell SU = null;

    public static String formatSize(long size) {
        if (size == 0) {
            return "0";
        }
        final double n = Math.log(Math.abs(size)) / Math.log(1024.0);
        final double value = size / Math.pow(1024.0, Math.floor(n));
        if (n < 1.0) { // Byte
            return String.format(Locale.getDefault(), "%.0fB", value);
        }
        if (n < 2.0) { // KB
            return String.format(Locale.getDefault(), "%.1fKB", value);
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

    @Override
    public void onTerminate() {
        if (SU != null) {
            SU.close();
        }
        super.onTerminate();
    }
}
