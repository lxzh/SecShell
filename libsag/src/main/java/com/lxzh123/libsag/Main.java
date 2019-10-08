package com.lxzh123.libsag;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * description main entry in PC$
 * author      Created by lxzh
 * date        2019-09-28
 */
class Main {
    //private final static String OUTPUT_FOLDER = "D:\\Android\\Code\\SecShell\\corestub\\src\\main\\java";
    private final static String OUTPUT_FOLDER = "/Users/a1239848066/Develop/AS/SecShell/corestub/src/main/java";

    public static void main(String[] args) {
        System.out.println("PROJECT_ROOT_FOLDER:" + System.getProperty("PROJECT_ROOT_FOLDER"));
//        args= new String[]{"D:\\Android\\Code\\SecShell\\libsag\\output\\libcore.jar"};
//        args= new String[]{"/Users/a1239848066/Develop/AS/SecShell/libsag/output/libcore.jar"};
        if (args.length < 1) {
            System.out.println("add a filepath to extract api");
            return;
        }
        String filepath = args[0];
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("file not exists");
            return;
        }
        System.out.println(file.toString() + " " + file.getAbsolutePath());
//        String folder = file.getAbsoluteFile().getParent();
        String folder = OUTPUT_FOLDER;
        if (args.length == 2) {
            folder = args[1];
        }
        URL url = null;
        try {
            url = new URL("file:" + filepath);
        } catch (Exception ex) {

        }
        if (url != null) {
            URLClassLoader myClassLoader = new URLClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader());
            Sag.get(Logger.get()).generateSdkApi(folder, filepath, myClassLoader);
        }
    }
}
