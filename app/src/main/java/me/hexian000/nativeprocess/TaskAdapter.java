package me.hexian000.nativeprocess;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import me.hexian000.nativeprocess.api.AppInfoCache;
import me.hexian000.nativeprocess.api.CachedAppInfo;
import me.hexian000.nativeprocess.api.Frame;
import me.hexian000.nativeprocess.api.ProcSample;

public class TaskAdapter extends ArrayAdapter<Frame.TaskStat> {
    private final String statusFormat, procFormat;
    private LayoutInflater layoutInflater;

    TaskAdapter(Activity activity, int textViewResourceId,
                List<Frame.TaskStat> appsList) {
        super(activity, textViewResourceId, appsList);
        layoutInflater = activity.getLayoutInflater();
        statusFormat = activity.getString(R.string.status_format);
        procFormat = activity.getString(R.string.proc_format);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            view = layoutInflater.inflate(R.layout.task_list_row, parent, false);
        }

        if (position < getCount()) {
            Frame.TaskStat stat = getItem(position);

            if (null != stat) {
                TextView titleView = view.findViewById(R.id.title);
                TextView cmdlineView = view.findViewById(R.id.cmdline);
                TextView statView = view.findViewById(R.id.stat);

                titleView.setText(String.format(Locale.getDefault(), procFormat, stat.pid, stat.name));
                cmdlineView.setText(stat.cmdline);
                statView.setText(String.format(Locale.getDefault(), statusFormat,
                        NativeProcess.formatTime(stat.time),
                        stat.cpu,
                        NativeProcess.formatSize(stat.resident)));
            }
        }
        return view;
    }
}