package me.hexian000.nativeprocess.api;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame {
    public static class TaskStat {
        public int pid;
        public String name;
        public long resident;
        public double time;
        public double cpu;
    }

    public static class UserStat {
        public int uid;
        public long resident;
        public double time;
        public double cpu;
        public String user;
        public List<TaskStat> detail;
    }

    public Map<Integer, UserStat> data;

    void fromSample(long clock_tick, @NonNull ProcSample sample) {
        data = new HashMap<>();
        for (Map.Entry<Integer, ProcSample.ProcStat> entry : sample.data.entrySet()) {
            ProcSample.ProcStat stat = entry.getValue();
            UserStat userStat = data.get(stat.uid);
            if (userStat == null) {
                userStat = new UserStat();
                userStat.user = stat.user;
                userStat.detail = new ArrayList<>();
                data.put(stat.uid, userStat);
            }
            final TaskStat taskStat = new TaskStat();
            taskStat.pid = entry.getKey();
            taskStat.name = stat.name;
            taskStat.cpu = 0.0;
            taskStat.time = (double) stat.time / (double) clock_tick;
            taskStat.resident = stat.resident;
            userStat.uid = stat.uid;
            userStat.cpu += taskStat.cpu;
            userStat.time += taskStat.time;
            userStat.resident += taskStat.resident;
            userStat.detail.add(taskStat);
        }
    }

    void fromSamples(long clock_tick, @NonNull ProcSample last, @NonNull ProcSample sample) {
        data = new HashMap<>();
        final double timepast = (double) (sample.timestamp - last.timestamp) * 1e-3;
        for (Map.Entry<Integer, ProcSample.ProcStat> entry : sample.data.entrySet()) {
            ProcSample.ProcStat stat = entry.getValue();
            ProcSample.ProcStat lastStat = last.data.get(entry.getKey());
            if (lastStat == null || lastStat.uid != stat.uid) {
                continue;
            }
            UserStat userStat = data.get(stat.uid);
            if (userStat == null) {
                userStat = new UserStat();
                userStat.user = stat.user;
                userStat.detail = new ArrayList<>();
                data.put(stat.uid, userStat);
            }
            final TaskStat taskStat = new TaskStat();
            taskStat.pid = entry.getKey();
            taskStat.name = stat.name;
            taskStat.cpu = (double) (stat.time - lastStat.time) / (double) clock_tick / timepast;
            taskStat.time = (double) stat.time / (double) clock_tick;
            taskStat.resident = stat.resident;
            userStat.uid = stat.uid;
            userStat.cpu += taskStat.cpu;
            userStat.time += taskStat.time;
            userStat.resident += stat.resident;
            userStat.detail.add(taskStat);
        }
    }
}
