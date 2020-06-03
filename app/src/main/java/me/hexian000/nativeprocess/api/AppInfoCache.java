package me.hexian000.nativeprocess.api;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import java.util.List;

public class AppInfoCache {
    private PackageManager pm;
    private SparseArray<CachedAppInfo> cache;

    public AppInfoCache(PackageManager packageManager) {
        pm = packageManager;
        cache = new SparseArray<>();
    }

    private CachedAppInfo loadInfo(int uid) {
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            return null;
        }
        StringBuilder label = null;
        Drawable icon = null;
        for (String p : packages) {
            try {
                ApplicationInfo app = pm.getApplicationInfo(p, PackageManager.GET_META_DATA);
                if (label == null) {
                    label = new StringBuilder(app.loadLabel(pm).toString());
                } else {
                    label.append(';').append(app.loadLabel(pm).toString());
                }
                if (icon == null) {
                    icon = app.loadIcon(pm);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        if (label == null) {
            return null;
        }
        CachedAppInfo info = new CachedAppInfo();
        info.label = label.toString();
        info.icon = icon;
        return info;
    }

    public CachedAppInfo get(int uid) {
        CachedAppInfo info = cache.get(uid);
        if (info == null) {
            info = loadInfo(uid);
            cache.put(uid, info);
        }
        if (info == null) {
            String name = pm.getNameForUid(uid);
            if (name != null) {
                info = new CachedAppInfo();
                info.label = name;
                cache.put(uid, info);
            }
        }
        return info;
    }
}
