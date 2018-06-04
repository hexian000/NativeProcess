package me.hexian000.nativeprocess.api;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class RootShell {
	private Process su = null;
	private PrintStream out = null;
	private Scanner in = null;

	public RootShell() {
		try {
			su = Runtime.getRuntime().exec("su");
			in = new Scanner(su.getInputStream());
			out = new PrintStream(su.getOutputStream());
		} catch (IOException ignored) {
		}
	}

	public List<String> run(String command) {
		List<String> list = new ArrayList<>();
		if (su == null || out == null || in == null) {
			return list;
		}
		String uuid = UUID.randomUUID().toString();
		out.println(command);
		out.println("echo '" + uuid + "'");
		out.flush();
		while (true) {
			String line = in.nextLine();
			if (line.endsWith(uuid)) {
				list.add(line.substring(0, line.length() - uuid.length()));
				return list;
			}
			list.add(line);
		}
	}

	public void close() {
		if (out != null) {
			out.println("exit");
			out.flush();
			out.close();
			out = null;
		}
		if (in != null) {
			in.close();
			in = null;
		}
		su = null;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
