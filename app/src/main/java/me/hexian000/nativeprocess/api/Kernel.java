package me.hexian000.nativeprocess.api;

import android.util.Log;

import me.hexian000.nativeprocess.NativeProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.hexian000.nativeprocess.NativeProcess.LOG_TAG;

public class Kernel {
    private static final Pattern linePattern = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.+?)\\s*$");

    private static List<String> run(String command) {
        if (NativeProcess.SU == null) {
            NativeProcess.SU = new Shell("su");
        }
        return NativeProcess.SU.run(command);
    }

    public static List<ProcessInfo> listProcesses(final String sort) {
        final List<ProcessInfo> processes = new ArrayList<>();
        final List<String> lines = run("ps -A -w -o PID,UID,TIME,PCPU,RSS,NAME,COMMAND -k " + sort);
        for (String line : lines) {
            if (line.length() < 1) {
                continue;
            }
            Matcher m = linePattern.matcher(line);
            if (m.find()) {
                if ("PID".equals(m.group(1))) {
                    continue;
                }
                try {
                    ProcessInfo info = new ProcessInfo();
                    info.pid = Integer.parseInt(m.group(1));
                    info.uid = Integer.parseInt(m.group(2));
                    info.time = m.group(3);
                    info.cpu = Float.parseFloat(m.group(4));
                    info.resident = Integer.parseInt(m.group(5));
                    info.name = m.group(6);
                    info.command = m.group(7);
                    processes.add(info);
                } catch (NumberFormatException e) {
                    Log.w(LOG_TAG, "NumberFormatException: " + line);
                }
            } else {
                Log.w(LOG_TAG, "line mismatch: " + line);
            }
        }
        // processes.sort((a, b) -> -Float.compare(a.cpu, b.cpu));
        return Collections.unmodifiableList(processes);
    }

    public static class ProcessListSort {
        public static final String SORT_RESIDENT_DSC = "-RSS";
        public static final String SORT_CPU_DSC = "-PCPU";
        public static final String SORT_TIME_DSC = "-TIME";
    }
}
