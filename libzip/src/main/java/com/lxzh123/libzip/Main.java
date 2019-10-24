package com.lxzh123.libzip;

import com.lxzh123.libzip.log.ILogger;
import com.lxzh123.libzip.log.Logger;
import com.lxzh123.libzip.zip.ZipUtil;

import java.io.File;

/**
 * description main entry in PC mode
 * author      Created by lxzh
 * date        2019-09-28
 */
class Main {
    private final static String TAG = "Main";

    /**
     * java -jar libzip.jar unzip zipFile outPath
     *
     * @param args array of input parameter:
     *             0: command:unzip
     *             1: zip file path to be unzip
     *             2: output path
     *             java -jar libzip.jar unzip zipFile outPath
     */
    public static void main(String[] args) {
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
        }
        printUsage();
    }

    private static void printUsage() {
        System.out.println("usage: ");
        System.out.println("1. unzip:");
        System.out.println("java -jar libzip.jar unzip zipFile outPath");
        System.out.println("unzip:        command param");
        System.out.println("zipFile:      zip file path to be unzip");
        System.out.println("outPath:      output path");
    }
}
