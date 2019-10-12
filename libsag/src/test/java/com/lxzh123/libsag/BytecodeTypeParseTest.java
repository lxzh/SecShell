package com.lxzh123.libsag;

import com.lxzh123.libsag.log.Logger;
import com.lxzh123.libsag.sag.Sag;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * description $
 * author      Created by lxzh
 * date        2019-09-06
 */
public class BytecodeTypeParseTest {
    @Test
    public void addition_isCorrect() {
        String[] typeMap = new String[]{
                "boolean", "Z",
                "byte", "B",
                "char", "C",
                "short", "S",
                "int", "I",
                "long", "J",
                "float", "F",
                "double", "D",
                "int[]", "[I",
                "String", "Ljava/lang/String;",
                "String[]", "[Ljava/lang/String;",
                "java.util.Set<Integer>","Ljava/util/Set<Ljava/lang/Integer;>;",
                "java.util.Set<String>","Ljava/util/Set<Ljava/lang/String;>;",
                "java.util.Set<int[]>","Ljava/util/Set<[I>;",
                "java.util.Set<Void[]>","Ljava/util/Set<[Ljava/lang/Void;>;",
                "java.util.Set<? extends OutClass>","Ljava/util/Set<+Lcom/lxzh123/libcore/OutClass;>;",
                "java.util.List<String>", "Ljava/util/List<Ljava/lang/String;>;",
                "java.util.List<int[]>", "Ljava/util/List<[I>;",
                "java.util.List<? extends A>", "Ljava/util/List<+LA;>;",
                "java.util.Map<String, String>", "Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;",
                "java.util.Map<String, String[]>", "Ljava/util/Map<Ljava/lang/String;[Ljava/lang/String;>;",
                "java.util.Map<String, String[]>[]", "[Ljava/util/Map<Ljava/lang/String;[Ljava/lang/String;>;",
                "java.util.Map<String, String[][]>", "Ljava/util/Map<Ljava/lang/String;[[Ljava/lang/String;>;",
                "java.util.Map<int[], int[]>", "Ljava/util/Map<[I[I>;",
                "java.util.Map<int[], int[][]>", "Ljava/util/Map<[I[[I>;",
                "java.util.Map<int[][], int[][]>", "Ljava/util/Map<[[I[[I>;",
                "java.util.Map<int[][], int[][]>[]", "[Ljava/util/Map<[[I[[I>;",
                "java.util.Map<int[], ? extends A>", "Ljava.util.Map<[I+LA;>;",
                "java.util.Map<T, String>", "Ljava/util/Map<TT;Ljava/lang/String;>;",
                "java.util.List<java.util.Map<int[][], int[][]>[]>", "Ljava/util/List<[Ljava/util/Map<[[I[[I>;>;",
                "java.util.List<java.util.Map<int[][], int[][]>[]>[]", "[Ljava/util/List<[Ljava/util/Map<[[I[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][], java.util.List<String[]>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[ILjava/util/List<[Ljava/lang/String;>;>;[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][][][][], java.util.List<java.util.Map<T, java.util.Map<String[][], java.util.List<int[]>>>>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[[[[ILjava/util/List<Ljava/util/Map<TT;Ljava/util/Map<[[Ljava/lang/String;Ljava/util/List<[I>;>;>;>;>;[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][][][][], java.util.List<java.util.Map<T, java.util.Map<java.util.List<String>[][], java.util.List<int[]>>>>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[[[[ILjava/util/List<Ljava/util/Map<TT;Ljava/util/Map<[[Ljava/util/List<Ljava/lang/String;>;Ljava/util/List<[I>;>;>;>;>;[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][][][][], java.util.List<java.util.Map<T, java.util.Map<java.util.Map<String, Integer>[][], java.util.List<int[]>>>>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[[[[ILjava/util/List<Ljava/util/Map<TT;Ljava/util/Map<[[Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;Ljava/util/List<[I>;>;>;>;>;[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][][][][], java.util.List<java.util.Map<T, java.util.Map<java.util.Map<String, T>[][], java.util.List<int[]>>>>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[[[[ILjava/util/List<Ljava/util/Map<TT;Ljava/util/Map<[[Ljava/util/Map<Ljava/lang/String;TT;>;Ljava/util/List<[I>;>;>;>;>;[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][][][][], java.util.List<java.util.Map<T, java.util.Map<java.util.Map<String, T[]>[][], java.util.List<int[]>>>>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[[[[ILjava/util/List<Ljava/util/Map<TT;Ljava/util/Map<[[Ljava/util/Map<Ljava/lang/String;[TT;>;Ljava/util/List<[I>;>;>;>;>;[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][][][][], java.util.List<java.util.Map<T, java.util.Map<java.util.Map<String[], T[]>[][], java.util.List<int[]>>>>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[[[[ILjava/util/List<Ljava/util/Map<TT;Ljava/util/Map<[[Ljava/util/Map<[Ljava/lang/String;[TT;>;Ljava/util/List<[I>;>;>;>;>;[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][][][][], java.util.List<java.util.Map<T, java.util.Map<java.util.Map<String[], T[]>[][], java.util.List<int[]>[]>>>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[[[[ILjava/util/List<Ljava/util/Map<TT;Ljava/util/Map<[[Ljava/util/Map<[Ljava/lang/String;[TT;>;[Ljava/util/List<[I>;>;>;>;>;[[I>;>;",
                "java.util.List<java.util.Map<java.util.Map<int[][][][][], java.util.List<java.util.Map<T, java.util.Map<java.util.Map<String[], T[]>[][], java.util.Map<int[], T[][][]>[]>>>>, int[][]>[]>[]",
                "[Ljava/util/List<[Ljava/util/Map<Ljava/util/Map<[[[[[ILjava/util/List<Ljava/util/Map<TT;Ljava/util/Map<[[Ljava/util/Map<[Ljava/lang/String;[TT;>;[Ljava/util/Map<[I[[[TT;>;>;>;>;>;[[I>;>;",
                "L", "LL;",
                "T", "TT;",
                "K", "TK;",
                "TT", "TTT;"};
        assertEquals(4, 2 + 2);
        Sag sag = Sag.get(Logger.get());
        int len = typeMap.length;
//        String pkgName = getClass().getPackage().getName();
        String pkgName = "com.lxzh123.libcore";
        int equalCnt = 0;
        for (int i = 0; i < len; i += 2) {
            String expect = typeMap[i];
            String input = typeMap[i + 1];
            String actual = sag.parseType(pkgName, input);
            if (expect.equals(actual)) {
                equalCnt++;
            }
            Logger.get().d("UnitTest", "i=" + i + ":input=" + input + ", expect=" + expect + ", actual=" + actual + ", isEQ=" + expect.equals(actual));
//            assertEquals(expect, actual);
        }
        Logger.get().d("UnitTest", "len=" + len + ", equalCnt=" + equalCnt);
    }
}
