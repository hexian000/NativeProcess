package me.hexian000.nativeprocess;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import me.hexian000.nativeprocess.api.AppInfoCache;
import me.hexian000.nativeprocess.api.CachedAppInfo;
import me.hexian000.nativeprocess.api.Frame;
import me.hexian000.nativeprocess.api.ProcSample;

public class TaskAdapter extends ArrayAdapter<ProcSample.ProcStat> {
    private LayoutInflater layoutInflater;

    TaskAdapter(Activity activity, int textViewResourceId,
                List<ProcSample.ProcStat> appsList) {
        super(activity, textViewResourceId, appsList);
        layoutInflater = activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            view = layoutInflater.inflate(android.R.layout.two_line_list_item, parent, false);
        }

        if (position < getCount()) {
            ProcSample.ProcStat data = getItem(position);

            if (null != data) {
                /*
                TextView appName = view.findViewById(android.R.id.text1);
                TextView packageName = view.findViewById(android.R.id.text2);
                ImageView iconView = view.findViewById(R.id.app_icon);


                CachedAppInfo info = cache.get(data.uid);
                if(info!=null) {
					packageName.setText(info.packageName);
					appName.setText(info.label);
					iconView.setImageDrawable(info.icon);
				}*/
            }
        }
        return view;
    }
}