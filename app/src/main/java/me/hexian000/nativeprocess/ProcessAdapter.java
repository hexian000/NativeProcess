package me.hexian000.nativeprocess;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ProcessAdapter extends ArrayAdapter<NativeProcess> {
    private LayoutInflater layoutInflater;

    ProcessAdapter(Activity activity, int textViewResourceId,
                   List<NativeProcess> processList) {
        super(activity, textViewResourceId, processList);
        layoutInflater = activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        NativeProcess data = getItem(position);
        if (null != data) {
            TextView text1 = view.findViewById(android.R.id.text1);
            TextView text2 = view.findViewById(android.R.id.text2);
            text1.setText(data.name);
            text2.setHint(data.command);
        }
        return view;
    }
}

class NativeProcess {
    int pid;
    String name;
    String command;

    NativeProcess(String line) {
        String[] words = line.split(" ");
        pid = Integer.parseInt(words[0]);
        name = words[1];
        command = words[2];
    }
}
