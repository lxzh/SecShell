package com.lxzh123.libsag.sag;

public class ParseItem {
    public String result;
    public int parseLength;
    public boolean subItem;

    public ParseItem(String result, int parseLength, boolean subItem) {
        this.result = result;
        this.parseLength = parseLength;
        this.subItem = subItem;
    }

    @Override
    public String toString() {
        return "ParseItem{" +
                "result='" + result + '\'' +
                ", parseLength=" + parseLength +
                ", subItem=" + subItem +
                '}';
    }
}
