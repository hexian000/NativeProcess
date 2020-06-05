package me.hexian000.nativeprocess;

final class ListSort {
    public static final int rss = 0;
    public static final int cpu = 1;
    public static final int time = 2;

    public static boolean isValid(int sort) {
        switch (sort) {
            case rss:
            case cpu:
            case time:
                break;
            default:
                return false;
        }
        return true;
    }
}
