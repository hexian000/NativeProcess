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

import me.hexian000.nativeprocess.api.ProcessInfo;

import java.util.List;
import java.util.Locale;

public class ProcessAdapter extends ArrayAdapter<ProcessInfo> {
    private final String statusFormat, rootTag, uidFormat;
    private LayoutInflater layoutInflater;
    private Drawable defaultIcon;

    ProcessAdapter(Activity activity, int textViewResourceId,
                   List<ProcessInfo> processList) {
        super(activity, textViewResourceId, processList);
        layoutInflater = activity.getLayoutInflater();
        defaultIcon = activity.getDrawable(R.mipmap.ic_launcher);
        statusFormat = activity.getString(R.string.status_format);
        uidFormat = activity.getString(R.string.uid_format);
        rootTag = activity.getString(R.string.root_tag);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            view = layoutInflater.inflate(R.layout.snippet_list_row, parent, false);
        }

        if (position < getCount()) {
            ProcessInfo info = getItem(position);
            if (null != info) {
                TextView userView = view.findViewById(R.id.user);
                TextView processView = view.findViewById(R.id.process);
                TextView statusView = view.findViewById(R.id.status);
                ImageView iconView = view.findViewById(R.id.app_icon);

                if (info.app != null) {
                    userView.setText(info.app.label);
                    processView.setText(info.name);
                    iconView.setImageDrawable(info.app.icon);
                } else {
                    if (info.uid != 0) {
                        userView.setText(String.format(Locale.getDefault(), uidFormat, info.uid));
                    } else {
                        userView.setText(rootTag);
                    }
                    processView.setText(info.name);
                    iconView.setImageDrawable(defaultIcon);
                }
                statusView.setText(String.format(Locale.getDefault(), statusFormat,
                        info.time,
                        info.cpu,
                        NativeProcess.formatSize(info.resident)));
            }
        }
        return view;
    }
}
