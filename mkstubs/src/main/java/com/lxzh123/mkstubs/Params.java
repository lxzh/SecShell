package com.lxzh123.mkstubs;

/**
 * A struct-like class to hold the various input values (e.g. command-line args)
 */
class Params {
    private String mInputJarPath;
    private String mOutputJarPath;
    private Filter mFilter;
    private boolean mVerbose;
    private boolean mDumpSource;

    public Params() {
        mFilter = new Filter();
    }

    /**
     * Sets the name of the input jar, where to read classes from. Must not be null.
     */
    public void setInputJarPath(String inputJarPath) {
        mInputJarPath = inputJarPath;
    }

    /**
     * Sets the name of the output jar, where to write classes to. Must not be null.
     */
    public void setOutputJarPath(String outputJarPath) {
        mOutputJarPath = outputJarPath;
    }

    /**
     * Returns the name of the input jar, where to read classes from.
     */
    public String getInputJarPath() {
        return mInputJarPath;
    }

    /**
     * Returns the name of the output jar, where to write classes to.
     */
    public String getOutputJarPath() {
        return mOutputJarPath;
    }

    /**
     * Returns the current instance of the filter, the include/exclude patterns.
     */
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * Sets verbose mode on. Default is off.
     */
    public void setVerbose() {
        mVerbose = true;
    }

    /**
     * Returns true if verbose mode is on.
     */
    public boolean isVerbose() {
        return mVerbose;
    }

    /**
     * Sets dump source mode on. Default is off.
     */
    public void setDumpSource() {
        mDumpSource = true;
    }

    /**
     * Returns true if source should be dumped.
     */
    public boolean isDumpSource() {
        return mDumpSource;
    }
}
