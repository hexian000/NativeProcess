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
    private final String processFormat;
    private final String infoFormat;
    private final String kernelTag;
    private LayoutInflater layoutInflater;
    private Drawable defaultIcon;

    ProcessAdapter(Activity activity, int textViewResourceId,
                   List<ProcessInfo> processList) {
        super(activity, textViewResourceId, processList);
        layoutInflater = activity.getLayoutInflater();
        defaultIcon = activity.getDrawable(R.mipmap.ic_launcher);
        kernelTag = activity.getString(R.string.kernel_process_tag);
        processFormat = activity.getString(R.string.process_format);
        infoFormat = activity.getString(R.string.info_format);
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
                TextView appName = view.findViewById(R.id.app_name);
                TextView packageName = view.findViewById(R.id.app_package);
                ImageView iconView = view.findViewById(R.id.app_icon);

                if (info.app != null) {
                    appName.setText(String.format(Locale.getDefault(), processFormat, info.app.label, info.name));
                    packageName.setText(String.format(Locale.getDefault(), infoFormat,
                            info.time,
                            NativeProcess.formatSize(info.resident * 1024),
                            NativeProcess.formatDecimal(info.cpu)));
                    iconView.setImageDrawable(info.app.icon);
                } else {
                    appName.setText(String.format(Locale.getDefault(), processFormat, kernelTag, info.name));
                    packageName.setText(String.format(Locale.getDefault(), infoFormat,
                            info.time,
                            NativeProcess.formatSize(info.resident * 1024),
                            NativeProcess.formatDecimal(info.cpu)));
                    iconView.setImageDrawable(defaultIcon);
                }
            }
        }
        return view;
    }
}
