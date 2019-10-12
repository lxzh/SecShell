package com.lxzh123.libsag;

import com.lxzh123.libsag.log.ILogger;
import com.lxzh123.libsag.log.Logger;
import com.lxzh123.libsag.sag.Sag;
import com.lxzh123.libsag.xml.AndroidManifestAnalyze;
import com.lxzh123.libsag.xml.XMLUtil;
import com.lxzh123.libsag.zip.ZipUtil;

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
     * java -jar libsag.jar pkgname sdkManifestFile stubManifestFile
     *
     * @param args array of input parameter:
     *             sag:
     *             0: command:sag
     *             1: jar file path to be parsed
     *             2: output folder of generated java file
     *             3: dependencies library (optional)
     *             java -jar libsag.jar sag jarFile outPath
     *             unzip
     *             0: command:unzip
     *             1: zip file path to be unzip
     *             2: output path
     *             java -jar libsag.jar unzip zipFile outPath
     *             pkgname
     *             0: command:pkgname
     *             1: sdk manifest file name
     *             2: stub manifest file name
     */
    public static void main(String[] args) {
//        args = new String[]{"sag", "D:\\Android\\Code\\reinforce-java-android\\sdk\\geegaurdsdk.jar", "D:\\Android\\Code\\reinforce-java-android\\corestub\\src\\main\\java"};
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
                String filePath = args[1];
                String folder = args[2];
                String libs = "";
                File file = new File(filePath);
                if (!file.exists()) {
                    log.e(TAG, "jar file not exists");
                    return;
                }
                log.d(TAG, file.toString() + " " + file.getAbsolutePath());

                if (argc > 3) {
                    libs = args[3];
                }
                sag(filePath, folder, libs);
                return;
            }
        } else if (args[0].equals("pkgname")) {
            if (argc == 3) {
                String sdkManifestFile = args[1];
                String stubManifestFile = args[2];
                File sdkFile = new File(sdkManifestFile);
                if (!sdkFile.exists()) {
                    log.e(TAG, "sdk manifest file not exists");
                    return;
                }
                File stubFile = new File(stubManifestFile);
                if (!stubFile.exists()) {
                    log.e(TAG, "stub manifest file not exists");
                    return;
                }
                AndroidManifestAnalyze analyze = new AndroidManifestAnalyze();
                analyze.parsePackage(sdkManifestFile);
                String pkgName = analyze.getAppPackage();
                System.out.println("pkgName:" + pkgName);//output to the output stream, don't use Logger
                analyze.updatePkgName(stubManifestFile, pkgName);
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
        System.out.println("3. pkgname:");
        System.out.println("java -jar libsag.jar pkgname sdkManifestFile stubManifestFile");
        System.out.println("pkgname:      command param");
        System.out.println("sdkManifestFile: sdk manifest file");
        System.out.println("stubManifestFile: stub manifest file");
    }
}
