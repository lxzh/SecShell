package com.lxzh123.libmix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Mix {
    private static final int ARGC = 5;
    private static final String EN_CMD = "-en";
    private static final String DE_CMD = "-de";
    private static final String CM_CMD = "-cm";
    private static final int MASK = 0xAA;

    /**
     * java -jar -en -s srcFile -d dstFile
     * java -jar -de -s srcFile -d dstFile
     * java -jar -cm -s srcFile -d dstFile
     *
     * @param args
     */
    public static void main(String[] args) {
        int len = args.length;
        if (len != ARGC) {
            printUsage();
            return;
        }
        String srcFile = null;
        String dstFile = null;
        int type = -1;
        for (int i = 0; i < len; i++) {
            System.out.println("i=" + i + ":" + args[i]);
        }

        String cmd = args[0].toLowerCase();
        srcFile = args[2];
        dstFile = args[4];
        if (cmd.equals(EN_CMD)) {
            doEncrypt(srcFile, dstFile);
        } else if (cmd.equals(DE_CMD)) {
            doDecrypt(srcFile, dstFile);
        } else if (cmd.equals(CM_CMD)) {
            doCompare(srcFile, dstFile);
        } else {
            printUsage();
            return;
        }
    }

    /**
     * print usage of this tool
     */
    private static void printUsage() {
        System.out.println("encrypt: java -jar -en -s $srcFile -d $dstFile");
        System.out.println("decrypt: java -jar -de -s $srcFile -d $dstFile");
        System.out.println("compare: java -jar -cm -s $srcFile -d $dstFile");
    }

    /**
     * do encrypt of the source file, output to the destination file
     * @param srcFile
     * @param dstFile
     */
    private static void doEncrypt(String srcFile, String dstFile) {
        File inFile = new File(srcFile);
        File ouFile = new File(dstFile);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (!inFile.exists()) {
                System.out.println("Error: Input file is not exists");
            }
            if (ouFile.exists()) {
                ouFile.delete();
            }
            inputStream = new FileInputStream(inFile);
            outputStream = new FileOutputStream(ouFile);
            byte[] buffer = new byte[1024];
            int size, max;
            while ((size = inputStream.read(buffer)) > 0) {
                if (size % 2 != 0) {
                    max = size - 1;
                    buffer[max] = (byte) (buffer[max] ^ MASK);
                } else {
                    max = size;
                }
                byte l, r;
                for (int i = 0; i < max; i += 2) {
                    l = (byte) (buffer[i] ^ MASK);
                    r = (byte) (buffer[i + 1] ^ MASK);
                    buffer[i] = r;
                    buffer[i + 1] = l;
                }
                outputStream.write(buffer, 0, size);
            }
        } catch (Exception ex) {
            System.out.println("doEncrypt Exception: " + ex.toString());
            ex.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * do decrypt of the source file, output to the destination file
     * @param srcFile
     * @param dstFile
     */
    private static void doDecrypt(String srcFile, String dstFile) {
        File inFile = new File(srcFile);
        File ouFile = new File(dstFile);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (!inFile.exists()) {
                System.out.println("Error: Input file is not exists");
            }
            if (ouFile.exists()) {
                ouFile.delete();
            }
            inputStream = new FileInputStream(inFile);
            outputStream = new FileOutputStream(ouFile);
            byte[] buffer = new byte[1024];
            int size, max;
            while ((size = inputStream.read(buffer)) > 0) {
                if (size % 2 != 0) {
                    max = size - 1;
                    buffer[max] = (byte) (buffer[max] ^ MASK);
                } else {
                    max = size;
                }
                byte l, r;
                for (int i = 0; i < max; i += 2) {
                    l = (byte) (buffer[i] ^ MASK);
                    r = (byte) (buffer[i + 1] ^ MASK);
                    buffer[i] = r;
                    buffer[i + 1] = l;
                }
                outputStream.write(buffer, 0, size);
            }
        } catch (Exception ex) {
            System.out.println("doEncrypt Exception: " + ex.toString());
            ex.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * compare two file in binary mode for each byte
     * @param fileOne
     * @param fileTwo
     */
    private static void doCompare(String fileOne, String fileTwo) {
        File inFile1 = new File(fileOne);
        File inFile2 = new File(fileTwo);
        if (!inFile1.exists()) {
            System.out.println("Error: fileOne file is not exists");
            return;
        }
        if (!inFile2.exists()) {
            System.out.println("Error: fileTwo file is not exists");
            return;
        }
        InputStream inputStream1 = null;
        InputStream inputStream2 = null;
        boolean isEqual = true;
        try {

            inputStream1 = new FileInputStream(inFile1);
            inputStream2 = new FileInputStream(inFile2);
            int len1 = inputStream1.available();
            int len2 = inputStream2.available();
            if (len1 != len2) {
                isEqual = false;
                System.out.println("Not equal: size of fileOne:" + len1 + " size of fileTwo:" + len2);
            } else {
                byte[] buffer1 = new byte[1024];
                byte[] buffer2 = new byte[1024];
                int size1, size2;
                int cnt = 0;
                while (isEqual) {
                    size1 = inputStream1.read(buffer1);
                    size2 = inputStream2.read(buffer2);
                    cnt++;
                    if (size1 != size2) {
                        isEqual = false;
                        System.out.println("Not equal: circle:" + cnt + " read size of fileOne:" + size1 + " size of fileTwo:" + size2);
                    } else if (size1 < 0) {
                        break;
                    } else {
                        for (int i = 0; i < size1; i++) {
                            if (buffer1[i] != buffer2[i]) {
                                isEqual = false;
                                System.out.println("Not equal: circle:" + cnt + " index:" + i + " byte of fileOne:" + buffer1[i] + " byte of fileTwo:" + buffer2[i]);
                                break;
                            }
                        }
                    }
                }
            }
            if (isEqual) {
                System.out.println("Succeed: fileOne is equal to fileTwo");
            } else {
                System.out.println("Failed: fileOne is not equal to fileTwo");
            }
        } catch (Exception ex) {
            System.out.println("doEncrypt Exception: " + ex.toString());
            ex.printStackTrace();
        } finally {
            if (inputStream1 != null) {
                try {
                    inputStream1.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
