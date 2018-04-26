package me.hexian000.nativeprocess;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ApplicationAdapter extends ArrayAdapter<CachedAppInfo> {
    private LayoutInflater layoutInflater;

    ApplicationAdapter(Activity activity, int textViewResourceId,
                       List<CachedAppInfo> appsList) {
        super(activity, textViewResourceId, appsList);
        layoutInflater = activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            view = layoutInflater.inflate(R.layout.snippet_list_row, parent, false);
        }

        if (position < getCount()) {
            CachedAppInfo data = getItem(position);
            if (null != data) {
                TextView appName = view.findViewById(R.id.app_name);
                TextView packageName = view.findViewById(R.id.app_package);
                ImageView iconView = view.findViewById(R.id.app_icon);

                packageName.setText(data.info.packageName);
                appName.setText(data.label);
                iconView.setImageDrawable(data.icon);
            }
        }
        return view;
    }
}