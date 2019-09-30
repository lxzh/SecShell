package com.lxzh123.libsag;

public class ParseItem {
    public String result;
    public int parseLength;

    public ParseItem(String result, int parseLength) {
        this.result = result;
        this.parseLength = parseLength;
    }

    @Override
    public String toString() {
        return "ParseItem{" +
                "result='" + result + '\'' +
                ", parseLength=" + parseLength +
                '}';
    }
}
