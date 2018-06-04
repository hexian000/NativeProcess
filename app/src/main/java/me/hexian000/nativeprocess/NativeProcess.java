package me.hexian000.nativeprocess;

import android.app.Application;

import java.text.DecimalFormat;

public class NativeProcess extends Application {
	public final static String LOG_TAG = "NativeProcess";
	private final static DecimalFormat prettyFormat = new DecimalFormat("#.##");

	public static String formatDecimal(double decimal) {
		return prettyFormat.format(decimal);
	}

	public static String formatSize(double size) {
		if (size < 2.0 * 1024.0) { // Byte
			return prettyFormat.format(size) + "B";
		} else if (size < 2.0 * 1024.0 * 1024.0) { // KB
			return prettyFormat.format(size / 1024.0) + "KB";
		} else if (size < 2.0 * 1024.0 * 1024.0 * 1024.0) { // MB
			return prettyFormat.format(size / 1024.0 / 1024.0) + "MB";
		} else { // GB
			return prettyFormat.format(size / 1024.0 / 1024.0 / 1024.0) + "GB";
		}
	}
}
