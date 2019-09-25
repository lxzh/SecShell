package com.lxzh123.classloader;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

import dalvik.system.DexClassLoader;

/**
 * Created by tanzhenxing
 * Date: 2017年02月17日 11:21
 * Description:
 */

public class DexUtils {
    //和导出之前的包名和类名保持一致
    public static final String mClassName = "tzx.com.dynloadclass.Test";
    //assets目录下的dex文件名称
    public static final String dexPath = "Test.dex";

    /**
     * Description:获取动态加载的dex包的sdcard路径
     * created by tanzhenxing(谭振兴)
     * created data 17-2-20 下午3:51
     */
    public static String getDynamicDexPath() {
        return android.os.Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + dexPath;// 前半部分为获得SD卡的目录
    }

    /**
     * Description:获取制定parent-classloader的DexClassLoader对象
     * created by tanzhenxing(谭振兴)
     * created data 17-2-20 下午3:53
     */
    public static DexClassLoader getCustomerDexClassLoader(Context context, ClassLoader loader) {
        String mDexPath = getDynamicDexPath();
        ///data/user/0/tzx.com.dynloadclass/app_dex
        File dexOutputDir = context.getDir("dex", 0);
        File file = new File(mDexPath);
        DexClassLoader classLoader = new DexClassLoader(file.getAbsolutePath(),
                dexOutputDir.getAbsolutePath(), null, loader);
        return classLoader;
    }

    public static DexClassLoader getCustomerDexClassLoader(Context context) {
        return getCustomerDexClassLoader(context, context.getClassLoader());
    }

    /**
     * Description:合并两个Array，按照first-second顺序
     * created by tanzhenxing(谭振兴)
     * created data 17-2-20 下午3:54
     */
    public static Object combineArray(Object firstArray, Object secondArray) {
        Class<?> localClass = firstArray.getClass().getComponentType();
        int firstArrayLength = Array.getLength(firstArray);
        int allLength = firstArrayLength + Array.getLength(secondArray);
        Object result = Array.newInstance(localClass, allLength);
        for (int k = 0; k < allLength; ++k) {
            if (k < firstArrayLength) {
                Array.set(result, k, Array.get(firstArray, k));
            } else {
                Array.set(result, k, Array.get(secondArray, k - firstArrayLength));
            }
        }
        return result;
    }


    private static final int BUF_SIZE = 2048;

    /**
     * Description:  将assets目录下面的dex_file文件写入dexInternalStoragePath文件中
     * @param context 上下问环境
     * @param dexInternalStoragePath 存储在磁盘上的dex文件
     * @param dex_file assets目录下的dex文件名称
     */
    public static boolean prepareDex(Context context, File dexInternalStoragePath, String dex_file) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;
        try {
            bis = new BufferedInputStream(context.getAssets().open(dex_file));
            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;

    }

}