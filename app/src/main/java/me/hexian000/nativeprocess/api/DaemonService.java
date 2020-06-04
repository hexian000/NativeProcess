package me.hexian000.nativeprocess.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class DaemonService extends Service implements Runnable {
    private Daemon daemon;
    private Binder mBinder;
    private FrameUpdateWatcher watcher;
    private long clock_tick;

    @Override
    public void onDestroy() {
        daemon.close();
        daemon = null;
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
        }

        public void unwatch() {
            DaemonService.this.watcher = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        daemon = new Daemon("su", getApplicationInfo().nativeLibraryDir + "/libtasks.so");
        clock_tick = Long.parseLong(daemon.safeReadLine());
        new Thread(this).start();
        return super.onStartCommand(intent, flags, startId);
    }

    public DaemonService() {
        mBinder = new Binder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


}
