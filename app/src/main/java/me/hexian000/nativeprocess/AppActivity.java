package me.hexian000.nativeprocess;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import eu.chainfire.libsuperuser.Shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static me.hexian000.nativeprocess.NativeProcessApp.LOG_TAG;

public class AppActivity extends ListActivity {
	private ImageView imageView = null;
	private TextView textView = null;
	private TextView textHint = null;
	private ProgressBar progressBar = null;
	private Button killButton = null;

	private ApplicationInfo info;
	private Handler handler = null;
	private PackageManager packageManager = null;

	private Timer refreshTimer = null;
	private List<NativeProcess> processList = null;
	private ProcessAdapter adapter = null;

	@Override
	protected void onResume() {
		super.onResume();
		startRefreshTimer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		refreshTimer.cancel();
		refreshTimer = null;
	}

	private void startRefreshTimer() {
		refreshTimer = new Timer();
		refreshTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				refresh();
			}
		}, 0, 5000);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras == null) {
			finish();
			return;
		}
		String packageName = extras.getString("packageName");

		handler = new Handler();
		packageManager = getPackageManager();
		try {
			info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(LOG_TAG, "", e);
			finish();
			return;
		}

		imageView = findViewById(R.id.imageView);
		textView = findViewById(R.id.textView);
		textHint = findViewById(R.id.textHint);
		progressBar = findViewById(R.id.ListLoading);
		killButton = findViewById(R.id.killButton);
		getListView().setOnItemClickListener((parent, view, position, id) -> {
			NativeProcess item = ((ProcessAdapter) getListAdapter()).getItem(position);
			if (item != null) {
				Shell.SU.run(new String[]{
						"kill -15 " + item.pid,
						"sleep 1",
						"kill -0 " + item.pid + " && kill -9 " + item.pid});
				Toast.makeText(AppActivity.this,
						String.format(getResources().getString(R.string.process_killed), item.pid),
						Toast.LENGTH_SHORT).show();
				refreshTimer.cancel();
				startRefreshTimer();
			}
		});
		killButton.setOnClickListener((v) -> {
			killButton.setEnabled(false);
			progressBar.setVisibility(View.VISIBLE);
			new Thread(() -> {
				Shell.SU.run(new String[]{
						"ps -u " + info.uid + " -o PID | grep -o -E \"[0-9]+\" | xargs -r kill -15",
						"sleep 1",
						"ps -u " + info.uid + " -o PID | grep -o -E \"[0-9]+\" | xargs -r kill -9"});
				handler.post(AppActivity.this::finish);
			}).start();
		});

		processList = new ArrayList<>();
		adapter = new ProcessAdapter(AppActivity.this,
				android.R.layout.two_line_list_item, processList);
		setListAdapter(adapter);
	}

	private void refresh() {
		List<String> processes = Shell.SU.run("ps -u " + info.uid +
				" -o PID,NAME,COMMAND | awk '{print $1,$2,$3}'");
		final Drawable icon = info.loadIcon(packageManager);
		final CharSequence label = info.loadLabel(packageManager);
		handler.post(() -> {
			processList.clear();
			for (String line : processes) {
				String text = line.trim();
				if (text.length() > 0 && !"PID NAME COMMAND".equals(text) && text.contains(" ")) {
					processList.add(new NativeProcess(text));
				}
			}
			progressBar.setVisibility(View.INVISIBLE);
			imageView.setImageDrawable(icon);
			textView.setText(label);
			textHint.setHint(info.packageName);
			killButton.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		});
	}

}
