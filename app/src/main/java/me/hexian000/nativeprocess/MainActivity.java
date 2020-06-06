package me.hexian000.nativeprocess;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import me.hexian000.nativeprocess.api.AppInfoCache;
import me.hexian000.nativeprocess.api.DaemonService;
import me.hexian000.nativeprocess.api.Frame;
import me.hexian000.nativeprocess.api.FrameUpdateWatcher;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements FrameUpdateWatcher {
    private Handler handler = new Handler();
    private List<Frame.UserStat> processList = null;
    private UserAdapter listAdapter = null;
    private ProgressBar listLoading = null;
    private boolean firstRefresh = false;
    private int sort;
    private ServiceConnection mConnection;
    private DaemonService.Binder binder;

    @NonNull
    public AppInfoCache getCache() {
        return binder.getCache();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listLoading = findViewById(R.id.ListLoading);
        processList = new ArrayList<>();
        listAdapter = new UserAdapter(MainActivity.this, R.layout.user_list_row, processList);
        sort = ListSort.rss;
        final ListView listView = findViewById(R.id.List);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Frame.UserStat info = listAdapter.getItem(position);
            if (info == null) {
                return;
            }
            final Intent intent = new Intent(getApplicationContext(), AppActivity.class);
            intent.putExtra("uid", info.uid);
            intent.putExtra("sort", sort);
            startActivity(intent);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (binder != null) {
            binder.unwatch(this);
            binder = null;
        }
        unbindService(mConnection);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firstRefresh = true;
        listLoading.setVisibility(View.VISIBLE);
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                binder = (DaemonService.Binder) service;
                binder.watch(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
            }
        };
        final Intent intent = new Intent(getApplicationContext(), DaemonService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_cpu:
                sort = ListSort.cpu;
                item.setChecked(true);
                return true;
            case R.id.menu_sort_by_rss:
                sort = ListSort.rss;
                item.setChecked(true);
                return true;
            case R.id.menu_sort_by_time:
                sort = ListSort.time;
                item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void OnFrameUpdate(final Frame frame) {
        handler.post(() -> {
            processList.clear();
            processList.addAll(frame.data.values());
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
            listAdapter.notifyDataSetChanged();
            if (firstRefresh) {
                listLoading.setVisibility(View.INVISIBLE);
                firstRefresh = false;
            }
        });
    }
}
