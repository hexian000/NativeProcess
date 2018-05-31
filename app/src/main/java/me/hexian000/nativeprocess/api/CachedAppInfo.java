package me.hexian000.nativeprocess.api;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class CachedAppInfo {
	public CharSequence label;
	public Drawable icon;
	public ApplicationInfo info;

	public CachedAppInfo(PackageManager pm, ApplicationInfo info) {
		label = info.loadLabel(pm);
		icon = info.loadIcon(pm);
		this.info = info;
	}
}
