package com.lxzh123.mkstubs;

/** Logger that writes on stdout depending a conditional verbose mode. */
class Logger {
    private final boolean mVerbose;

    public Logger(boolean verbose) {
        mVerbose = verbose;
    }

    /** Writes to stdout only in verbose mode. */
    public void debug(String msg, Object...params) {
        if (mVerbose) {
            System.out.println(String.format(msg, params));
        }
    }

    /** Writes to stdout all the time. */
    public void info(String msg, Object...params) {
        System.out.println(String.format(msg, params));
    }
}
