package me.hexian000.nativeprocess;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import me.hexian000.nativeprocess.api.AppInfoCache;
import me.hexian000.nativeprocess.api.Kernel;
import me.hexian000.nativeprocess.api.ProcessInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private Handler handler = null;
    private List<ProcessInfo> processList = null;
    private AppInfoCache cache = null;
    private ProcessAdapter listAdapter = null;
    private ProgressBar listLoading = null;
    private Timer refreshTimer = null;
    private boolean firstRefresh = false;
    private String sort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listLoading = findViewById(R.id.ListLoading);
        handler = new Handler();
        processList = new ArrayList<>();
        cache = new AppInfoCache(getPackageManager());
        listAdapter = new ProcessAdapter(MainActivity.this, R.layout.snippet_list_row, processList);
        sort = Kernel.ProcessListSort.SORT_CPU_DSC;
        final ListView listView = findViewById(R.id.List);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshTimer.cancel();
        refreshTimer = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        firstRefresh = true;
        listLoading.setVisibility(View.VISIBLE);
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        }, 0, 5000);
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
                sort = Kernel.ProcessListSort.SORT_CPU_DSC;
                return true;
            case R.id.menu_sort_by_rss:
                sort = Kernel.ProcessListSort.SORT_RESIDENT_DSC;
                return true;
            case R.id.menu_sort_by_time:
                sort = Kernel.ProcessListSort.SORT_TIME_DSC;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        cache.refresh();
        List<ProcessInfo> processes = Kernel.listProcesses(sort);
        for (ProcessInfo info : processes) {
            info.app = cache.get(info.uid);
        }
        handler.post(() -> {
            if (refreshTimer == null) {
                return;
            }
            processList.clear();
            processList.addAll(processes);
            listAdapter.notifyDataSetChanged();
            if (firstRefresh) {
                listLoading.setVisibility(View.INVISIBLE);
                firstRefresh = false;
            }
        });
    }
}