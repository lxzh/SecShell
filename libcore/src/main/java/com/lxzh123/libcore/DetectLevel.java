package com.lxzh123.libcore;

public enum DetectLevel {
    LevelChild(0),
    Level1(1),
    Level2(2),
    Level3(3),
    Level4(4),
    Level5(5);
    private int l;

    DetectLevel(int l) {
        this.l = l;
    }

    public final static DetectLevel Default = DetectLevel.Level2;

    @Override
    public String toString() {
        switch (l) {
            case 0:
                return "LevelChild";
            case 1:
                return "Level1";
            case 2:
                return "Level2";
            case 3:
                return "Level3";
            case 4:
                return "Level4";
            case 5:
                return "Level5";
        }
        return super.toString();
    }
}