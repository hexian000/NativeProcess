package me.hexian000.nativeprocess.api;

public class ProcessInfo {
	public int uid, pid;
	public float cpu;
	public long resident;
	public String time, name, command;
	public CachedAppInfo app;
}
