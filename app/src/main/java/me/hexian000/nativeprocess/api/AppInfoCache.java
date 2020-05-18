package me.hexian000.nativeprocess.api;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.SparseArray;

import java.util.List;

public class AppInfoCache {
    private PackageManager pm;
    private SparseArray<CachedAppInfo> cache;

    public AppInfoCache(PackageManager packageManager) {
        pm = packageManager;
        cache = new SparseArray<>();
    }

    public void refresh() {
        List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info : installed) {
            if (cache.get(info.uid) == null) {
                cache.put(info.uid, new CachedAppInfo(pm, info));
            }
        }
    }

    public CachedAppInfo get(int uid) {
        return cache.get(uid);
    }
}
