package me.hexian000.nativeprocess.api;

import android.util.Log;
import eu.chainfire.libsuperuser.Shell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.hexian000.nativeprocess.NativeProcess.LOG_TAG;

public class System {
	private static final Pattern linePattern = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.+?)\\s*$");

	public static List<ProcessInfo> listProcesses(final String sort) {
		final List<ProcessInfo> processes = new ArrayList<>();
		final List<String> lines = Shell.SU.run("ps -A -w -o PID,UID,TIME,PCPU,RSS,NAME,COMMAND -k " + sort);
		Log.d(LOG_TAG, "lines: " + lines.size());
		for (String line : lines) {
			try {
				Matcher m = linePattern.matcher(line);
				if (m.find()) {
					if ("PID".equals(m.group(1))) {
						continue;
					}
					ProcessInfo info = new ProcessInfo();
					info.pid = Integer.parseInt(m.group(1));
					info.uid = Integer.parseInt(m.group(2));
					info.time = m.group(3);
					info.cpu = Float.parseFloat(m.group(4));
					info.resident = Integer.parseInt(m.group(5));
					info.name = m.group(6);
					info.command = m.group(7);
					processes.add(info);
				} else {
					Log.w(LOG_TAG, "line mismatch: " + line);
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return Collections.unmodifiableList(processes);
	}

	public static class ProcessListSort {
		public static final String SORT_RESIDENT_DSC = "-RSS";
		public static final String SORT_CPU_DSC = "-PCPU";
		public static final String SORT_TIME_DSC = "-TIME";
	}
}
