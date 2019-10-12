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
    private final static String TAG = "Main";

    /**
     * java -jar libsag.jar sag srcJar outPath [libDir]
     * java -jar libsag.jar unzip zipFile outPath
     *
     * @param args array of input parameter:
     *             0: command:sag
     *             1: jar file path to be parsed
     *             2: output folder of generated java file
     *             3: dependencies library (optional)
     *             java -jar libsag.jar sag jarFile outPath
     * @param args array of input parameter:
     *             0: command:unzip
     *             1: zip file path to be unzip
     *             2: output path
     *             java -jar libsag.jar unzip zipFile outPath
     */
    public static void main(String[] args) {
//        args = new String[]{"sag", "D:\\Android\\Code\\reinforce-java-android\\sdk\\geegaurdsdk.jar", "D:\\Android\\Code\\reinforce-java-android\\corestub\\src\\main\\java"};
//        args = new String[]{"D:\\Android\\Code\\reinforce-java-android\\sdk\\libcore.jar"};
        int argc = args.length;
        if (argc < 1) {
            printUsage();
            return;
        }
        Logger log = Logger.get();
        log.setLevel(ILogger.INFO);
        for (int i = 0; i < argc; i++) {
            log.d(TAG, "arg:i=" + i + "=" + args[i]);
        }

        if (args[0].equals("unzip")) {
            log.i(TAG, "command is unzip");
            if (argc >= 3) {
                String zipFile = args[1];
                File file = new File(zipFile);
                if (!file.exists()) {
                    log.e(TAG, "zip file not exists");
                    return;
                }
                String outPath = args[2];
                ZipUtil.unzip(zipFile, outPath);
                return;
            }
        } else if (args[0].equals("sag")) {
            log.i(TAG, "command is sag");
            if (argc >= 3) {
                String filepath = args[1];
                String folder = args[2];
                String libs = "";
                File file = new File(filepath);
                if (!file.exists()) {
                    log.e(TAG, "jar file not exists");
                    return;
                }
                log.d(TAG, file.toString() + " " + file.getAbsolutePath());

                if (argc > 3) {
                    libs = args[3];
                }
                sag(filepath, folder, libs);
                return;
            }
        }
        printUsage();
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
                Logger.get().d(TAG, "URL:" + url.toString());
            }
            URLClassLoader myClassLoader = new URLClassLoader(urlList.toArray(urls), Thread.currentThread().getContextClassLoader());
            Sag.get(Logger.get()).generateSdkApi(outPath, srcJar, myClassLoader);
        }
        Logger.get().i(TAG, "sag finished");
    }

    private static void printUsage() {
        System.out.println("usage: ");
        System.out.println("1. sag: (stub-sdk auto generate)generate stub sdk source file:");
        System.out.println("java -jar libsag.jar sag srcJar outPath");
        System.out.println("sag:          command param");
        System.out.println("srcJar:       jar file path to be parsed");
        System.out.println("outPath:      output folder of generated java file");
        System.out.println("[libDir]:     folder of dependency library");
        System.out.println("2. unzip:");
        System.out.println("java -jar libsag.jar unzip zipFile outPath");
        System.out.println("unzip:        command param");
        System.out.println("zipFile:      zip file path to be unzip");
        System.out.println("outPath:      output path");
    }
}
