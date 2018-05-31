package me.hexian000.nativeprocess;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import me.hexian000.nativeprocess.api.AppInfoCache;
import me.hexian000.nativeprocess.api.ProcessInfo;
import me.hexian000.nativeprocess.api.System;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ListActivity {
	private Handler handler = null;
	private List<ProcessInfo> processList = null;
	private AppInfoCache cache = null;
	private ProcessAdapter listAdapter = null;
	private ProgressBar listLoading = null;
	private Timer refreshTimer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listLoading = findViewById(R.id.ListLoading);
		handler = new Handler();
		processList = new ArrayList<>();
		cache = new AppInfoCache(getPackageManager());
		listAdapter = new ProcessAdapter(MainActivity.this, R.layout.snippet_list_row, processList);
		setListAdapter(listAdapter);
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
		refreshTimer = new Timer();
		refreshTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				refresh();
			}
		}, 0, 10000);
		refresh();
	}

	private void refresh() {
		cache.refresh();
		List<ProcessInfo> processes = System.listProcesses(System.ProcessListSort.SORT_CPU_DSC);
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
			listLoading.setVisibility(View.INVISIBLE);
		});
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}
}