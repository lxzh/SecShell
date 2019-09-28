package com.lxzh123.libsag;

/**
 * description Logger$
 * author      Created by lxzh
 * date        2019-09-28
 */
class Logger implements ILogger {

    private int level = VERBOSE;
    private static volatile Logger instance;

    public static Logger get() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public void v(String TAG, String msg) {
        if (level <= VERBOSE) {
            String message = "[Verbose][" + TAG + "]:" + msg;
            log(message);
        }
    }

    @Override
    public void d(String TAG, String msg) {
        if (level <= DEBUG) {
            String message = "[Debug][" + TAG + "]:" + msg;
            log(message);
        }
    }

    @Override
    public void i(String TAG, String msg) {
        if (level <= INFO) {
            String message = "[Info][" + TAG + "]:" + msg;
            log(message);
        }
    }

    @Override
    public void w(String TAG, String msg) {
        if (level <= WARN) {
            String message = "[Warn][" + TAG + "]:" + msg;
            log(message);
        }
    }

    @Override
    public void e(String TAG, String msg) {
        if (level <= ERROR) {
            String message = "[Error][" + TAG + "]:" + msg;
            log(message);
        }
    }

    @Override
    public void a(String TAG, String msg) {
        if (level <= ASSERT) {
            String message = "[Assert][" + TAG + "]:" + msg;
            log(message);
        }
    }

    private void log(String msg) {
        System.out.println(msg);
    }
}
