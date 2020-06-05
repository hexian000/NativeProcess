package me.hexian000.nativeprocess.api;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static me.hexian000.nativeprocess.NativeProcess.TAG;

public final class ProcSample {
    public static final class ProcStat {
        public int uid;
        public String user;
        public String name;
        public long time;
        public long resident;
    }

    public long timestamp;

    // pid -> proc
    public Map<Integer, ProcStat> data = new HashMap<>();

    public void add(final String line) {
        Log.d(TAG, "line: \"" + line + "\"");
        String[] tokens = line.split(",", 6);
        if (tokens.length != 6) {
            return;
        }
        final int pid = Integer.parseInt(tokens[0]);
        ProcStat stat = new ProcStat();
        stat.uid = Integer.parseInt(tokens[1]);
        stat.user = tokens[2];
        stat.time = Long.parseLong(tokens[3]);
        stat.resident = Long.parseLong(tokens[4]);
        stat.name = tokens[5];

        data.put(pid, stat);
    }

    public void freeze() {
        timestamp = System.currentTimeMillis();
    }
}
