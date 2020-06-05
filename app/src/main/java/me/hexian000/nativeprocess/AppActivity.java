package me.hexian000.nativeprocess;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.hexian000.nativeprocess.api.AppInfoCache;
import me.hexian000.nativeprocess.api.CachedAppInfo;
import me.hexian000.nativeprocess.api.DaemonService;
import me.hexian000.nativeprocess.api.Frame;
import me.hexian000.nativeprocess.api.FrameUpdateWatcher;
import me.hexian000.nativeprocess.api.ProcSample;

public class AppActivity extends ListActivity implements FrameUpdateWatcher {
    private Handler handler = new Handler();
    private ImageView imageView = null;
    private TextView textView = null;
    private TextView textHint = null;
    private ProgressBar progressBar = null;
    private Button killButton = null;

    private List<Frame.TaskStat> processList = null;
    private TaskAdapter adapter = null;

    private ServiceConnection mConnection;
    private DaemonService.Binder binder;
    private int uid, sort;

    @Override
    protected void onResume() {
        super.onResume();
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                binder = (DaemonService.Binder) service;
                binder.watch(AppActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
            }
        };
        final Intent intent = new Intent(getApplicationContext(), DaemonService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(mConnection);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        Intent intent = getIntent();
        uid = intent.getIntExtra("uid", -1);
        if (uid < 0) {
            finish();
            return;
        }

        sort = intent.getIntExtra("sort", -1);
        if (!ListSort.isValid(sort)) {
            finish();
            return;
        }

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        textHint = findViewById(R.id.textHint);
        progressBar = findViewById(R.id.ListLoading);
        killButton = findViewById(R.id.killButton);
        getListView().setOnItemClickListener((parent, view, position, id) -> {
            Frame.TaskStat item = ((TaskAdapter) getListAdapter()).getItem(position);
            if (item != null) {
            	/*
                Shell.SU.run(new String[]{
                        "kill -15 " + item.pid,
                        "sleep 1",
                        "kill -0 " + item.pid + " && kill -9 " + item.pid});
                Toast.makeText(AppActivity.this,
                        String.format(getResources().getString(R.string.process_killed), item.pid),
                        Toast.LENGTH_SHORT).show();
                refreshTimer.cancel();
                startRefreshTimer();*/
            }
        });
        killButton.setOnClickListener((v) -> {
            killButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                /*Shell.SU.run(new String[]{
                        "ps -u " + info.uid + " -o PID | grep -o -E \"[0-9]+\" | xargs -r kill -15",
                        "sleep 1",
                        "ps -u " + info.uid + " -o PID | grep -o -E \"[0-9]+\" | xargs -r kill -9"});*/
                handler.post(AppActivity.this::finish);
            }).start();
        });

        processList = new ArrayList<>();
        adapter = new TaskAdapter(AppActivity.this,
                android.R.layout.two_line_list_item, processList);
        setListAdapter(adapter);
    }

    @Override
    public void OnFrameUpdate(Frame frame) {
        handler.post(() -> {
            processList.clear();
            Frame.UserStat userStat = frame.data.get(uid);
            if (userStat == null) {
                finish();
                return;
            }
            processList.addAll(userStat.detail);
            processList.sort((o1, o2) -> {
                switch (sort) {
                    case ListSort.rss:
                        return (int) Math.signum(o2.resident - o1.resident);
                    case ListSort.cpu:
                        return (int) Math.signum(o2.cpu - o1.cpu);
                    case ListSort.time:
                        return (int) Math.signum(o2.time - o1.time);
                }
                return 0;
            });
            CachedAppInfo info = new AppInfoCache(getPackageManager()).get(uid);
            if (info != null) {
                setTitle(info.label);
                imageView.setImageDrawable(info.icon);
                textView.setText(info.label);
                textHint.setHint(info.packageName);
                killButton.setVisibility(View.VISIBLE);
            } else {
                setTitle(userStat.user);
                imageView.setImageDrawable(getDrawable(R.mipmap.ic_launcher));
                textView.setText(userStat.user);
                textHint.setHint(String.format(Locale.getDefault(), getString(R.string.status_format),
                        NativeProcess.formatTime(userStat.time),
                        userStat.cpu,
                        NativeProcess.formatSize(userStat.resident)));
                killButton.setVisibility(View.INVISIBLE);
            }
            progressBar.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
        });
    }
}
