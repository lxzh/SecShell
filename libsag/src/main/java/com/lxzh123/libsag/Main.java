package com.lxzh123.libsag;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * description main entry in PC mode
 * author      Created by lxzh
 * date        2019-09-28
 */
class Main {
    /**
     * default output folder for debug use, input it from the command for normal use
     */
    private final static String OUTPUT_FOLDER = "D:\\Android\\Code\\SecShell\\corestub\\src\\main\\java";
//    private final static String OUTPUT_FOLDER = "/Users/a1239848066/Develop/AS/SecShell/corestub/src/main/java";

    /**
     * java -jar libsag.jar srcJar outputFolder
     * @param args array of input parameter:
     *             0: jar file path to be parsed
     *             1: output folder of generated java file
     */
    public static void main(String[] args) {
//        args = new String[]{"D:\\\\Android\\\\Code\\\\SecShell\\\\sdk\\geeguardsdk.jar"};
//        args= new String[]{"D:\\Android\\Code\\SecShell\\sdk\\libcore.jar"};
//        args= new String[]{"/Users/a1239848066/Develop/AS/SecShell/sdk/libcore.jar"};
        if (args.length < 1) {
            System.out.println("add a filepath to extract api");
            printUsage();
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

    private static void printUsage() {
        System.out.println("usage: java -jar libsag.jar srcJar outputFolder");
        System.out.println("srcJar:       jar file path to be parsed");
        System.out.println("outputFolder: output folder of generated java file");
    }
}
