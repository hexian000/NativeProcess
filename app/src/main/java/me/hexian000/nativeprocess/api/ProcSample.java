package me.hexian000.nativeprocess.api;

import java.util.Map;

public final class ProcSample {
    static final class ProcStat {
        int uid;
        String name;
        long time;
        long resident;
    }

    // pid -> proc
    public Map<Integer, ProcStat> data;

    public void add(final String line) {
        String[] tokens = line.split(",", 5);
        if (tokens.length != 5) {
            return;
        }
        final int pid = Integer.parseInt(tokens[0]);
        ProcStat stat = new ProcStat();
        stat.uid = Integer.parseInt(tokens[1]);
        stat.time = Long.parseLong(tokens[2]);
        stat.resident = Long.parseLong(tokens[3]);
        stat.name = tokens[4];

        data.put(pid, stat);
    }
}
