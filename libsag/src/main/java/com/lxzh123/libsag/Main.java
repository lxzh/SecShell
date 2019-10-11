package com.lxzh123.libsag;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

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
     * java -jar libsag.jar srcJar outPath libs
     *
     * @param args array of input parameter:
     *             0: jar file path to be parsed
     *             1: output folder of generated java file
     *             2: dependencies library
     *             java -jar libsag.jar  unzip zipFile outPath
     * @param args array of input parameter:
     *             0: command
     *             1: zip file path to be unzip
     *             2: output path
     */
    public static void main(String[] args) {
//        args = new String[]{"D:\\Android\\Code\\SecShell\\sdk\\geeguardsdk.jar"};
//        args= new String[]{"D:\\Android\\Code\\SecShell\\sdk\\libcore.jar"};
//        args= new String[]{"/Users/a1239848066/Develop/AS/SecShell/sdk/libcore.jar"};
        int argc = args.length;
        if (argc < 1) {
            printUsage();
            return;
        }
        for(int i=0;i<argc;i++) {
            System.out.println("arg:i=" + i + "=" + args[i]);
        }

        if (args[0].equals("unzip")) {
            if (argc >= 3) {
                ZipUtil.unzip(args[1], args[2]);
            } else {
                printUsage();
            }
        } else {
            String filepath = args[0];
            String folder = OUTPUT_FOLDER;
            String libs = "";
            File file = new File(filepath);
            if (!file.exists()) {
                System.out.println("file not exists");
                return;
            }
            System.out.println(file.toString() + " " + file.getAbsolutePath());

            if (argc > 1) {
                folder = args[1];
            }
            if (argc > 2) {
                libs = args[2];
            }
            sag(filepath, folder, libs);
        }
    }

    private static void sag(String srcJar, String outPath, String libs) {
        List<URL> urlList = new ArrayList<>();
        if (libs != null && libs.length() > 0) {
            File libFile = new File(libs);
            File[] files = libFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return (pathname.getName().toLowerCase().endsWith(".jar"));
                }
            });
            for (File file : files) {
                URL url = null;
                try {
                    url = new URL("file:" + file.getAbsolutePath());
                } catch (Exception ex) {
                }
                if (url != null) {
                    urlList.add(url);
                }
            }
        }

        URL url = null;
        try {
            url = new URL("file:" + srcJar);
        } catch (Exception ex) {

        }
        if (url != null) {
            urlList.add(url);
        }
        URL[] urls = new URL[urlList.size()];
        if (url != null) {
            for (URL u : urlList) {
                Logger.get().d("Main", "URL:" + url.toString());
            }
            URLClassLoader myClassLoader = new URLClassLoader(urlList.toArray(urls), Thread.currentThread().getContextClassLoader());
            Sag.get(Logger.get()).generateSdkApi(outPath, srcJar, myClassLoader);
        }
    }

    private static void printUsage() {
        System.out.println("usage: ");
        System.out.println("1. generate stub sdk source file:");
        System.out.println("java -jar libsag.jar srcJar outPath");
        System.out.println("srcJar:       jar file path to be parsed");
        System.out.println("outPath:      output folder of generated java file");
        System.out.println("2. unzip:");
        System.out.println("java -jar libsag.jar unzip zipFile outPath");
        System.out.println("unzip:        command param");
        System.out.println("zipFile:      zip file path to be unzip");
        System.out.println("outPath:      output path");
    }
}
