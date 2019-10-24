package com.lxzh123.libzip.zip;

import com.lxzh123.libzip.log.Logger;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtil {
    private final static String TAG = "ZipUtil";

    private static int BUFFER_SIZE = 2 << 10;

    /**
     * 压缩
     *
     * @param paths
     * @param fileName
     */
    public static void zip(String[] paths, String fileName) {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(fileName));
            for (String filePath : paths) {
                //compress file by recursion
                File file = new File(filePath);
                String relativePath = file.getName();
                if (file.isDirectory()) {
                    relativePath += File.separator;
                }
                zipFile(file, relativePath, zos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * compress file use zip
     *
     * @param path
     * @param fileName
     */
    public static void zip(String path, String fileName) {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(fileName));
            File file = new File(path);
            String relativePath = "";
            if (file.isDirectory()) {
                relativePath += File.separator;
            }
            zipFile(file, relativePath, zos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void zipFile(File file, String relativePath, ZipOutputStream zos) {
        InputStream is = null;
        try {
            if (!file.isDirectory()) {
                ZipEntry zp = new ZipEntry(relativePath);
                zos.putNextEntry(zp);
                is = new FileInputStream(file);
                byte[] buffer = new byte[BUFFER_SIZE];
                int length = 0;
                while ((length = is.read(buffer)) >= 0) {
                    zos.write(buffer, 0, length);
                }
                zos.flush();
                zos.closeEntry();
            } else {
                String tempPath = null;
                for (File f : file.listFiles()) {
                    tempPath = relativePath + f.getName();
                    if (f.isDirectory()) {
                        tempPath += File.separator;
                    }
                    zipFile(f, tempPath, zos);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unzip(String fileName, String descDir) {
        File zipFile = new File(fileName);
        unzip(zipFile, descDir);
    }

    public static void unzip(File zipFile, String descDir) {
        int foldCnt = 0;
        int fileCnt = 0;
        try (ZipArchiveInputStream inputStream = getZipFile(zipFile)) {
            File pathFile = new File(descDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            ZipArchiveEntry entry = null;
            while ((entry = inputStream.getNextZipEntry()) != null) {
                if (entry.isDirectory()) {
                    File directory = new File(descDir, entry.getName());
                    directory.mkdirs();
                    Logger.get().d("mkdir:", directory.getAbsolutePath());
                    foldCnt++;
                } else {
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(new File(descDir, entry.getName())));

                        int len = 0;
                        byte bufer[] = new byte[BUFFER_SIZE];
                        while (-1 != (len = inputStream.read(bufer))) {
                            os.write(bufer, 0, len);
                        }
                        Logger.get().d("file:", descDir + (descDir.endsWith(File.separator) ? "" : File.separator) + entry.getName());
                        fileCnt++;
                        IOUtils.copy(inputStream, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.get().e(TAG, "unzip Exception:" + e.getMessage());
//            LOG.error("[unzip] unzip failed", e);
        }
        Logger.get().i(TAG, "unzip finished, get folder count=" + foldCnt + " and file count=" + fileCnt);
    }

    private static ZipArchiveInputStream getZipFile(File zipFile) throws Exception {
        return new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        unzip("D:\\Android\\Code\\SecShell\\sdk\\libshell.aar", "D:\\Android\\Code\\SecShell\\sdk\\shell");
//        unzip("D:\\Android\\Code\\SecShell\\sdk\\geeguardsdk.aar", "D:\\Android\\Code\\SecShell\\sdk\\geeguardsdk");
//        zip(new String[]{"D:\\Android\\Code\\SecShell\\demo\\build\\outputs\\apk\\debug\\demo-debug\\"}, "D:\\Android\\Code\\SecShell\\demo\\build\\outputs\\apk\\debug\\demo-debug1.apk");
//        zip("D:\\Android\\Code\\SecShell\\sdk\\libcore\\", "D:\\Android\\Code\\SecShell\\sdk\\libcore1.aar");

        //jar xvf  D:\Android\Code\SecShell\sdk\libcore.aar extract to current folder, use unzip instead
        //jar cvfM D:\Android\Code\SecShell\sdk\libcore2.aar -C D:\Android\Code\SecShell\sdk\libcore\ .
    }
}