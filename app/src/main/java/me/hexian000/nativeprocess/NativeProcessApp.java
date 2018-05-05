package me.hexian000.nativeprocess;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class NativeProcessApp extends Application {
	public final static String LOG_TAG = "NativeProcess";
}

class CachedAppInfo {
	public CharSequence label;
	public Drawable icon;
	public ApplicationInfo info;

	CachedAppInfo(PackageManager pm, ApplicationInfo info) {
		label = info.loadLabel(pm);
		icon = info.loadIcon(pm);
		this.info = info;
	}
}
