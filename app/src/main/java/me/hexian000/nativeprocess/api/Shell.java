package me.hexian000.nativeprocess.api;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Shell {
	private Process shell = null;
	private PrintStream out = null;
	private Scanner in = null;

	public Shell(String command) {
		try {
			shell = Runtime.getRuntime().exec(command);
			in = new Scanner(shell.getInputStream());
			out = new PrintStream(shell.getOutputStream());
		} catch (IOException ignored) {
		}
	}

	public List<String> run(String command) {
		List<String> list = new ArrayList<>();
		if (shell == null || out == null || in == null) {
			return list;
		}
		String uuid = UUID.randomUUID().toString();
		out.println(command);
		out.println("echo; echo '" + uuid + "'");
		out.flush();
		while (true) {
			String line = in.nextLine();
			if (line.equals(uuid)) {
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
		shell = null;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
