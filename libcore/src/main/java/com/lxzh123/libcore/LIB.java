package com.lxzh123.libcore;

public class LIB {
    private static volatile LIB instance;

    public static LIB get() {
        if (instance == null) {
            synchronized (LIB.class) {
                if (instance == null) {
                    instance = new LIB();
                }
            }
        }
        return instance;
    }

    public int square(int x) {
        return InnerClass.innerMethod(x);
    }

    public byte getbyte(byte param1, Byte param2) {
        return 1;
    }

    public byte getshort(short param1, Short param2) {
        return 2;
    }

    public int getint(int param1, Integer param2) {
        return 3;
    }

    public long getlong(long param1, Long param2) {
        return 4;
    }

    public char getchar(char param1, Character param2) {
        return '\0';
    }

    public boolean getboolean(boolean param1, Boolean param2) {
        return true;
    }

    public float getfloat(float param1, Float param2) {
        return 1.5f;
    }

    public double getDouble(Double param1, double param2) {
        return 3.222;
    }

    public String getDouble(String param1, StringBuffer param2) {
        return "123232";
    }

    public Object getObject(String param1, StringBuffer param2) {
        return "123232";
    }

    private Object getObject(String param1) {
        return "123232";
    }

    private static Object getObject(String param1, String param2) {
        return "123232";
    }

    private static final Object getObject(String param1, String param2, String param3) {
        return "123232";
    }

    Object getObject(String param1, String param2, String param3, String param4) {
        return "123232";
    }

    public static String STATIC_FILED1 = "1111";
    public static String STATIC_FILED2;
    public static final String STATIC_FILED3 = "2222";
    private static final String STATIC_FILED4 = "4444";
    static final String STATIC_FILED5 = "5555";
    static String STATIC_FILED6 = "66666";
    static String STATIC_FILED7;
    final String STATIC_FILED8 = "88888";

    public static byte STATIC_FILED21 = 0;
    public static short STATIC_FILED22 = 0;
    public static int STATIC_FILED23 = 0;
    public static long STATIC_FILED24 = 0;
    public static char STATIC_FILED25 = 'a';
    public static boolean STATIC_FILED26 = true;
    public static float STATIC_FILED27 = 0;
    public static double STATIC_FILED28 = 333.44E2;
}
