package me.hexian000.nativeprocess;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import me.hexian000.nativeprocess.api.AppInfoCache;
import me.hexian000.nativeprocess.api.CachedAppInfo;
import me.hexian000.nativeprocess.api.Frame;

import java.util.List;
import java.util.Locale;

public class UserAdapter extends ArrayAdapter<Frame.UserStat> {
    private final String statusFormat, uidFormat;
    private final AppInfoCache cache;
    private LayoutInflater layoutInflater;
    private Drawable defaultIcon;

    UserAdapter(Activity activity, int textViewResourceId,
                List<Frame.UserStat> processList) {
        super(activity, textViewResourceId, processList);
        cache = ((MainActivity) activity).getCache();
        layoutInflater = activity.getLayoutInflater();
        defaultIcon = activity.getDrawable(R.mipmap.ic_launcher);
        statusFormat = activity.getString(R.string.status_format);
        uidFormat = activity.getString(R.string.uid_format);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            view = layoutInflater.inflate(R.layout.user_list_row, parent, false);
        }

        if (position < getCount()) {
            Frame.UserStat info = getItem(position);
            if (null != info) {
                TextView userView = view.findViewById(R.id.user);
                TextView processView = view.findViewById(R.id.process);
                TextView statusView = view.findViewById(R.id.status);
                ImageView iconView = view.findViewById(R.id.app_icon);

                CachedAppInfo app = cache.get(info.uid);

                if (app != null) {
                    userView.setText(app.label);
                    processView.setText(app.packageName);
                    iconView.setImageDrawable(app.icon);
                } else {
                    userView.setText(info.user);
                    processView.setText(String.format(Locale.getDefault(), uidFormat, info.uid));
                    iconView.setImageDrawable(defaultIcon);
                }
                statusView.setText(String.format(Locale.getDefault(), statusFormat,
                        NativeProcess.formatTime(info.time),
                        info.cpu,
                        NativeProcess.formatSize(info.resident)));
            }
        }
        return view;
    }
}
