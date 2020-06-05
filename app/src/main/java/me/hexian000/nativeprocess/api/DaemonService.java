package me.hexian000.nativeprocess.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static me.hexian000.nativeprocess.NativeProcess.TAG;

public class DaemonService extends Service implements Runnable {
    private Daemon daemon;
    private Binder mBinder;
    private FrameUpdateWatcher watcher;
    private Frame frame;
    private long clock_tick;

    @Override
    public void onDestroy() {
        if (daemon != null) {
            daemon.close();
            daemon = null;
        }
    }

    @Override
    public void run() {
        try {
            ProcSample last = null;
            ProcSample sample = new ProcSample();
            for (Daemon daemon = this.daemon; daemon != null; daemon = this.daemon) {
                String line = daemon.safeReadLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("END")) {
                    sample.freeze();
                    final FrameUpdateWatcher watcher = this.watcher;
                    if (watcher != null) {
                        final Frame frame = new Frame();
                        if (last == null) {
                            frame.fromSample(clock_tick, sample);
                        } else {
                            frame.fromSamples(clock_tick, last, sample);
                        }
                        watcher.OnFrameUpdate(frame);
                        this.frame = frame;
                    }
                    last = sample;
                    sample = new ProcSample();
                    continue;
                }
                sample.add(line);
            }
        } catch (IllegalStateException ignored) {
        }
    }

    public class Binder extends android.os.Binder {
        public void watch(FrameUpdateWatcher watcher) {
            DaemonService.this.watcher = watcher;
            if (frame != null) {
                watcher.OnFrameUpdate(frame);
            }
        }

        public void unwatch() {
            DaemonService.this.watcher = null;
        }
    }

    public DaemonService() {
        mBinder = new Binder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (daemon == null) {
            daemon = new Daemon("su", getApplicationInfo().nativeLibraryDir + "/libtasks.so");
            clock_tick = Long.parseLong(daemon.safeReadLine());
            Log.d(TAG, "clock_tick=" + clock_tick);
            new Thread(this).start();
        }
        return mBinder;
    }


}
