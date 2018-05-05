package me.hexian000.nativeprocess;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import eu.chainfire.libsuperuser.Shell;

import java.util.*;

public class MainActivity extends ListActivity {
	private Handler handler = null;
	private PackageManager packageManager = null;
	private List<CachedAppInfo> appList = null;
	private ApplicationAdapter listAdapter = null;
	private ProgressBar listLoading = null;
	private Timer refreshTimer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listLoading = findViewById(R.id.ListLoading);
		handler = new Handler();
		packageManager = getPackageManager();
		appList = new ArrayList<>();
		listAdapter = new ApplicationAdapter(MainActivity.this,
				R.layout.snippet_list_row, appList);
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
		List<ApplicationInfo> installed =
				packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		SparseArray<ApplicationInfo> uids = new SparseArray<>();
		for (ApplicationInfo info : installed) {
			uids.put(info.uid, info);
		}

		List<String> processes = Shell.SU.run("ps -A -o UID");
		Set<Integer> uidSet = new HashSet<>();
		for (String line : processes) {
			try {
				String data = line.trim();
				if (data.length() > 0 && !"UID".equals(data)) {
					int uid = Integer.parseInt(data);
					if (Process.isApplicationUid(uid)) {
						uidSet.add(uid);
					}
				}
			} catch (NumberFormatException ignored) {
			}
		}
		final SparseArray<CachedAppInfo> cacheSet = new SparseArray<>();
		for (CachedAppInfo old : appList) {
			cacheSet.put(old.info.uid, old);
		}
		final List<CachedAppInfo> tempList = new ArrayList<>();
		for (Integer uid : uidSet) {
			ApplicationInfo app = uids.get(uid);
			if (app != null) {
				CachedAppInfo cache = cacheSet.get(uid);
				if (cache == null) {
					cache = new CachedAppInfo(packageManager, app);
				}
				tempList.add(cache);
			}
		}
		handler.post(() -> {
			if (refreshTimer == null) {
				return;
			}
			appList.clear();
			appList.addAll(tempList);
			listAdapter.notifyDataSetChanged();
			listLoading.setVisibility(View.INVISIBLE);
		});
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		CachedAppInfo app = appList.get(position);
		Intent intent = new Intent(this, AppActivity.class);
		intent.putExtra("packageName", app.info.packageName);
		startActivity(intent);
	}
}